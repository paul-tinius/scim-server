package org.apache.directory.scim.test;

import static org.apache.directory.scim.server.patch.utility.PatchUtil.attributeLoggable;
import static org.apache.directory.scim.server.patch.utility.PatchUtil.checkValueEquals;
import static org.apache.directory.scim.server.patch.utility.PatchUtil.subAttributeLoggable;
import static org.apache.directory.scim.server.patch.utility.PatchUtil.throwScimException;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.ADD;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.REMOVE;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.REPLACE;
import static org.apache.directory.scim.test.ScimTestHelper.createRegistry;
import static org.apache.directory.scim.test.ScimTestHelper.get;
import static org.apache.directory.scim.test.ScimTestHelper.initialize;
import static org.apache.directory.scim.test.ScimTestHelper.set;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response;

import org.apache.directory.scim.server.rest.ObjectMapperFactory;
import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.spec.protocol.ErrorMessageType;
import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.protocol.exception.ScimException;
import org.apache.directory.scim.spec.resources.PhoneNumber;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.ResourceReference;
import org.apache.directory.scim.spec.schema.Schema;
import org.apache.directory.scim.test.builder.PatchOperationBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ScratchUnitTest {
  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {};
  private static final String NO_CHANGE = "Current value and requested target value are the same, no changes made for {}: '{}'";

  private static Registry registry;
  private static ObjectMapper objectMapper;

  ScimUser user = new ScimUser();

  public ScratchUnitTest() {
    PhoneNumber.setStrict(false);
  }

  @BeforeAll
  static void setUp() throws Exception {
    registry = createRegistry();
    objectMapper = new ObjectMapperFactory(registry).createObjectMapper();
  }

  @Test
  void singularValuedAttribute_patchOperation_successful() throws Exception {
    Set<Schema.Attribute> attributes = ScimTestHelper.singularValuedAttributes(registry, ScimUser.SCHEMA_URI);

    for (PatchOperation.Type operation : PatchOperation.Type.values()) {
      for (Schema.Attribute attribute : attributes) {
        initialize(user, operation, attribute);

        Object value = set(attribute);
        if (operation.equals(REPLACE) && value instanceof Boolean) {
          value = !((Boolean) value); // flip the current value
        }

        PatchOperation patchOperations =
          PatchOperationBuilder.builder()
            .operation(operation)
            .path(attribute.getName())
            .value(value)
            .build();

        Map<String, Object> source = objectMapper.convertValue(user, MAP_TYPE);
        Object oldValue = get(objectMapper.convertValue(source, ScimUser.class), attribute);

        singularValuedAttribute(attribute, source, patchOperations);

        Object actual = get(objectMapper.convertValue(source, ScimUser.class), attribute);

        if(attribute.getType().equals(Schema.Attribute.Type.BOOLEAN)) {
          assertThat(actual).isNotNull();
        } else if (operation.equals(ADD)) {
          assertThat(actual).isNotNull();
        } else if (operation.equals(REMOVE)) {
          assertThat(actual).isNull();
        } else { // type == REPLACE
          assertThat(actual).isNotEqualTo(oldValue);
        }
      }
    }
  }

  @Test
  void complexValuedAttribute_patchOperation_successful() throws Exception {
    Set<Schema.Attribute> attributes = ScimTestHelper.complexValuedAttributes(registry, ScimUser.SCHEMA_URI);
    for (final PatchOperation.Type operation : PatchOperation.Type.values()) {
      for (final Schema.Attribute attribute : attributes) {
        /*
         * Patching is only allowed for Complex Attributes "name", "schema" and "meta" are managed by the SCIM server.
         */
        if(!attribute.getName().equals("name")) { continue; }

        List<Schema.Attribute> subAttributes = attribute.getSubAttributes();
        for (final Schema.Attribute subAttribute : subAttributes) {
          initialize(user, operation, attribute, subAttribute);

          PatchOperation patchOperation =
            PatchOperationBuilder.builder()
              .operation(operation)
              .path(String.format("%s.%s", attribute.getName(), subAttribute.getName()))
              .value(set(attribute, subAttribute))
              .build();

          Map<String, Object> source = objectMapper.convertValue(user, MAP_TYPE);
          Object oldValue = get(objectMapper.convertValue(source, ScimUser.class), attribute, subAttribute);

          complexValuedAttribute(attribute, subAttribute, source, patchOperation);

          Object actual = get(objectMapper.convertValue(source, ScimUser.class), attribute, subAttribute);

          if (attribute.getType().equals(Schema.Attribute.Type.BOOLEAN) && operation.equals(REMOVE)) {
            assertThat(actual).isNotNull();
          } else if (operation.equals(ADD)) {
            assertThat(actual).isNotNull();
          } else if (operation.equals(REMOVE)) {
            assertThat(actual).isNull();
          } else { // type == REPLACE
            assertThat(actual).isNotEqualTo(oldValue);
          }
        }
      }
    }
  }

  @Test
  void multiValuedComplexAttribute_patchOperation_successful() throws Exception {
    Set<Schema.Attribute> attributes = ScimTestHelper.multiValuedComplexAttributes(registry, ScimUser.SCHEMA_URI);
    for (final PatchOperation.Type operation : PatchOperation.Type.values()) {
      for (final Schema.Attribute attribute : attributes) {
        List<Schema.Attribute> subAttributes = attribute.getSubAttributes();
        for (final Schema.Attribute subAttribute : subAttributes) {
          // skip ref
          if(subAttribute.getName().equals("ref")) {
            log.info("SKIP: {}", subAttributeLoggable(subAttribute));
            continue;
          }

          initialize(user, operation, attribute, subAttribute);

          PatchOperation patchOperation =
            PatchOperationBuilder.builder()
              .operation(operation)
              .path(attribute.getName())
              .value(set(subAttribute))
              .build();

          Map<String, Object> source = objectMapper.convertValue(user, MAP_TYPE);
          Object oldValue = get(objectMapper.convertValue(source, ScimUser.class), attribute, subAttribute);

          multiValuedComplexAttribute(attribute, subAttribute, source, patchOperation);

          Object actual = get(objectMapper.convertValue(source, ScimUser.class), attribute, subAttribute);

          // prevent primary from causing a test failure.
          if (subAttribute.getType().equals(Schema.Attribute.Type.BOOLEAN)) {
            assertThat(actual).isNotNull();
          } else if (operation.equals(ADD)) {
            assertThat(actual).isNotNull();
          } else if (operation.equals(REMOVE)) {
            assertThat(actual).isNull();
          } else { // type == REPLACE
            // prevent Reference Type from causing a test failure.
            if(actual instanceof ResourceReference.ReferenceType) {
              assertThat(actual).isNotNull();
            } else {
              assertThat(actual).isNotEqualTo(oldValue);
            }
          }
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void multiValuedComplexAttribute(final Schema.Attribute attribute, final Schema.Attribute subAttribute,
                                           Map<String, Object> source, final PatchOperation patchOperation) throws ScimException {

    log.info("Multi-Valued Complex attribute - Operation: {} {} {}",
      patchOperation.getOperation(), attributeLoggable(attribute), subAttributeLoggable(subAttribute));

    switch (patchOperation.getOperation()) {
      case ADD:
        /*
         * https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2.1
         */
      case REPLACE:
        /*
         * https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2.1
         */
        if(!source.containsKey(attribute.getName())) {
          source.put(attribute.getName(), new ArrayList<Map<String,Object>>());
          ((List<Map<String,Object>>) source.get(attribute.getName())).add(new HashMap<>());
        }

        List<Map<String,Object>> list = (List<Map<String,Object>>) source.get(attribute.getName());
        if(!list.isEmpty()) {
          if (list.size() > 1) {
            /*
             * 3.5.2.1.  Add Operation
             *  o If the target location exists, the value is replaced.
             *
             * 3.5.2.3.  Replace Operation
             *  o If the target location is a multi-valued attribute and no filter is specified, the attribute and
             *    all values are replaced.
             */
            if(patchOperation.getValue() instanceof List) {
              list = (List<Map<String,Object>>) patchOperation.getValue();
              source.replace(attribute.getName(), list);
              return;
            }
          }

          // replace the sub-attributes
          if(subAttribute != null) {
            Map<String, Object> element = list.get(0);
            complexValuedAttribute(subAttribute, null, element, patchOperation);
            list.add(element);
            // replace all sub-attributes
          } else {
            if(patchOperation.getValue() instanceof Map) {
              list.remove(0);
              list.add((Map<String, Object>) patchOperation.getValue());
            } else if(patchOperation.getValue() instanceof List) {
              list = (List<Map<String, Object>>) patchOperation.getValue();
            }

            source.replace(attribute.getName(), list);
          }
        }
        break;
      case REMOVE:
        /*
         * https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2.2
         */
        if(subAttribute == null) {
          source.remove(attribute.getName());
        } else {
          if(source.get(attribute.getName()) instanceof List) {
            List<Map<String,Object>> removeList = (List<Map<String,Object>>) source.get(attribute.getName());
            if(!removeList.isEmpty()) {
              if (removeList.size() > 1) {
                log.error("There are {} existing entries for {}, use a filter to narrow the results.",
                  removeList.size(), patchOperation.getPath());
                throw throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.TOO_MANY);
              }
              Map<String, Object> map = removeList.get(0);
              map.remove(subAttribute.getName());
            }
          }
        }
        break;
    }
  }

  /**
   * @param attribute the attribute
   * @param subAttribute the sub-attribute
   * @param source the {@link Map} representing the SCIM resource
   * @param patchOperation the {@link PatchOperation}.
   */
  @SuppressWarnings("unchecked")
  private void complexValuedAttribute(final Schema.Attribute attribute, final Schema.Attribute subAttribute,
                                      Map<String, Object> source, final PatchOperation patchOperation) throws ScimException {
    log.info("Complex Valued attribute - Operation: {} {} {}",
      patchOperation.getOperation(), attributeLoggable(attribute), subAttributeLoggable(subAttribute));

    switch (patchOperation.getOperation()) {
      case ADD:
        /*
         * https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2.1
         */
      case REPLACE:
        /*
         * https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2.1
         */

        /*
         * If omitted, the target location is assumed to be the resource itself.  The "value" parameter contains a set
         * of attributes to be added to the resource.
         */
        if(subAttribute == null) {
          singularValuedAttribute(attribute, source, patchOperation);
        } else {
          Object complexSource = source.get(attribute.getName());
          if(complexSource == null) {
            complexSource = new HashMap<>();
          }

          if(complexSource instanceof Map) {
            Map<String,Object> complexSourceMap = (Map<String,Object>) complexSource;
            singularValuedAttribute(subAttribute, complexSourceMap, patchOperation);

            if(source.containsKey(attribute.getName())) {
              source.replace(attribute.getName(), complexSourceMap);
            } else {
              source.put(attribute.getName(), complexSourceMap);
            }
          }
        }

        break;
      case REMOVE:
        /*
         * https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2.2
         */
        if(subAttribute == null) {
          source.remove(attribute.getName());
        } else {
          if(source.get(attribute.getName()) instanceof Map) {
            ((Map<String,Object>)source.get(attribute.getName())).remove(subAttribute.getName());
          }
        }
        break;
    }
  }

  /**
   * @param attribute      the attribute
   * @param singularAttributeSource   the {@link Map} representing the SCIM resource
   * @param patchOperation the {@link PatchOperation}.
   */
  private void singularValuedAttribute(final Schema.Attribute attribute, Map<String, Object> singularAttributeSource,
                                       final PatchOperation patchOperation) throws ScimException {
    log.info("Singular Valued attribute - Operation: {} {}", patchOperation.getOperation(), attributeLoggable(attribute));

    final Object oldValue = singularAttributeSource.get(attribute.getName());

    switch (patchOperation.getOperation()) {
      /*
       * https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2.1
       */
      case ADD:
      case REPLACE:
        /*
         * https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2.3
         */
        if (!singularAttributeSource.containsKey(attribute.getName())) {
          singularAttributeSource.put(attribute.getName(), patchOperation.getValue());
        } else { // otherwise, it does contain the attribute
          if (!checkValueEquals(oldValue, patchOperation.getValue())) {
            singularAttributeSource.replace(attribute.getName(), patchOperation.getValue());
          } else {
            log.info(NO_CHANGE, patchOperation.getOperation(), attribute.getName());
          }
        }
        break;
      /*
       * https://datatracker.ietf.org/doc/html/rfc7644#section-3.5.2.2
       */
      case REMOVE:
        singularAttributeSource.remove(attribute.getName());
        break;
    }
  }
}
