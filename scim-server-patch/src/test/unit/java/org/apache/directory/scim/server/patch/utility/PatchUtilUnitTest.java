package org.apache.directory.scim.server.patch.utility;

import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type;
import static org.apache.directory.scim.test.ScimTestHelper.MAP_TYPE;
import static org.apache.directory.scim.test.ScimTestHelper.assertScimException;
import static org.apache.directory.scim.test.ScimTestHelper.createRegistry;
import static org.apache.directory.scim.test.ScimTestHelper.faker;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.Response;

import org.antlr.v4.runtime.misc.Pair;
import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.spec.protocol.ErrorMessageType;
import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.protocol.exception.ScimException;
import org.apache.directory.scim.spec.resources.Address;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.Schema;
import org.apache.directory.scim.test.ScimTestHelper;
import org.apache.directory.scim.test.builder.PatchOperationBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class PatchUtilUnitTest {

  private static Registry registry;
  private static Schema schema;

  @BeforeAll
  static void setUp() throws Exception {
    registry = createRegistry();
    schema = registry.getSchema(ScimUser.SCHEMA_URI);
  }

  @Test
  void throwScimException_generateScimException_returnsScimException() {
    ScimException exception = PatchUtil.throwScimException(Response.Status.BAD_REQUEST, ErrorMessageType.INVALID_VALUE);
    assertScimException(exception,
      Response.Status.BAD_REQUEST,
      ErrorMessageType.INVALID_VALUE,
      ErrorMessageType.INVALID_VALUE.getDetail());
  }

  @Test
  void checkMutability_removeMutabilityAttribute_throwsScimException() throws Exception {
    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(Type.REMOVE)
      .path("userName")
      .build();

    Throwable t = catchThrowable( () -> PatchUtil.checkRequired(patchOperation,
      schema, registry));

    assertScimException(t,
      Response.Status.BAD_REQUEST,
      ErrorMessageType.MUTABILITY,
      ErrorMessageType.MUTABILITY.getDetail());
  }

  @Test
  void checkRequired_removeRequiredAttribute_throwsScimException() throws Exception {
    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(Type.REMOVE)
      .path("id")
      .build();

    Throwable t = catchThrowable( () -> PatchUtil.checkRequired(patchOperation,
      schema, registry));

    assertScimException(t,
      Response.Status.BAD_REQUEST,
      ErrorMessageType.MUTABILITY,
      ErrorMessageType.MUTABILITY.getDetail());
  }

  @Test
  void checkRequired_removeNonRequiredAttribute_successful() throws Exception {
    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(Type.REMOVE)
      .path("title")
      .build();

    PatchUtil.checkRequired(patchOperation, schema, registry);
  }

  @Test
  void checkSchema_findSchemaForPatchOperation_successful() throws Exception {
    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(Type.ADD)
      .path("title")
      .value("title")
      .build();

    final Schema actual = PatchUtil.checkSchema(patchOperation, registry);
    assertThat(actual).isEqualTo(schema);
  }

  @Test
  void checkTarget_targetValid_successful() throws Exception {
    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(Type.ADD)
      .path("title")
      .value("title")
      .build();

    PatchUtil.checkTarget(patchOperation);
  }

  @Test
  void checkTarget_targetInvalid_throwsScimException() {
    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(Type.REMOVE)
      .build();

    Throwable t = catchThrowable( () -> PatchUtil.checkTarget(patchOperation));

    assertScimException(t,
      Response.Status.BAD_REQUEST,
      ErrorMessageType.NO_TARGET,
      ErrorMessageType.NO_TARGET.getDetail());  }

  @Test
  void checkValue_validValue_successful() throws Exception {
    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(Type.ADD)
      .path("title")
      .value("title")
      .build();

    PatchUtil.checkValue(patchOperation, schema);
  }

  @Test
  void checkValue_invalidValue_throwsScimException() throws Exception {
    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(Type.ADD)
      .path("title")
      .value(true)
      .build();

    Throwable t = catchThrowable( () ->  PatchUtil.checkValue(patchOperation,
      schema));

    assertScimException(t,
      Response.Status.BAD_REQUEST,
      ErrorMessageType.INVALID_VALUE,
      ErrorMessageType.INVALID_VALUE.getDetail());
  }

  @Test
  void checkValue_invalidValueComplex_throwsScimException() throws Exception {
    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(Type.REPLACE)
      .path("name.givenName")
      .value(Integer.MAX_VALUE)
      .build();

    Throwable t = catchThrowable( () ->  PatchUtil.checkValue(patchOperation,
      schema));

    assertScimException(t,
      Response.Status.BAD_REQUEST,
      ErrorMessageType.INVALID_VALUE,
      ErrorMessageType.INVALID_VALUE.getDetail());
  }

  @Test
  void checkValue_validValueComplex_successful() throws Exception {
    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(Type.REPLACE)
      .path("name.givenName")
      .value("givenName")
      .build();

    PatchUtil.checkValue(patchOperation, schema);
  }

  @Test
  void checkValue_invalidValueMultiValuedComplex_throwsScimException() throws Exception {
    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(Type.REPLACE)
      .path("addresses[type EQ \"home\"].streetAddress")
      .value(true)
      .build();

    Throwable t = catchThrowable( () ->  PatchUtil.checkValue(patchOperation,
      schema));

    assertScimException(t,
      Response.Status.BAD_REQUEST,
      ErrorMessageType.INVALID_VALUE,
      ErrorMessageType.INVALID_VALUE.getDetail());
  }

  @Test
  void checkValue_validValueMultiValuedComplex_successful() throws Exception {
    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(Type.REPLACE)
      .path("addresses[type EQ \"home\"].streetAddress")
      .value("123 Any Street")
      .build();

    PatchUtil.checkValue(patchOperation, schema);
  }

  @Test
  void checkSupported_invalidPath_throwsScimException() throws Exception {
    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(Type.REMOVE)
      .path("active")
      .build();

    Throwable t = catchThrowable( () -> PatchUtil.checkSupported(patchOperation,
      schema, registry));

    assertScimException(t,
      Response.Status.BAD_REQUEST,
      ErrorMessageType.INVALID_PATH,
      ErrorMessageType.INVALID_PATH.getDetail());
  }

  @Test
  void checkValueEquals_valuesEquals_successful() {
    assertThat(PatchUtil.checkValueEquals(true, true)).isTrue();
  }

  @Test
  void checkValueEquals_valuesNotEquals_successful() {
    assertThat(PatchUtil.checkValueEquals(true, "true")).isFalse();
  }

  @Test
  void validateFilterPath_validFilterPath_successful() throws Exception {
    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(Type.REPLACE)
      .path("addresses[type EQ \"home\"].streetAddress")
      .value("123 Any Street")
      .build();

    Pair<Boolean, String> pair = PatchUtil.validateFilterPath(patchOperation);
    assertThat(pair).isNotNull();
    assertThat(pair.a).isInstanceOf(Boolean.class);
    assertThat(pair.a).isTrue();
    assertThat(pair.b).isInstanceOf(String.class);
  }

  @Test
  void validateFilterPath_invalidFilterPath_successful() throws Exception {
    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(Type.REPLACE)
      .path("addresses{type EQ \"home\"}streetAddress")
      .value("123 Any Street")
      .build();

    Pair<Boolean, String> pair = PatchUtil.validateFilterPath(patchOperation);
    assertThat(pair).isNotNull();
    assertThat(pair.a).isInstanceOf(Boolean.class);
    assertThat(pair.a).isFalse();
    assertThat(pair.b).isNull();
  }

  @Test
  void attributeReference_nullPatchOperation_throwsNullPointer() {
    Throwable t = catchThrowable( () -> PatchUtil.attributeReference(null));

    assertThat(t).isInstanceOf(NullPointerException.class);

    NullPointerException ne = (NullPointerException) t;
    assertThat(ne.getMessage()).isEqualTo("Patch operation must not be null");
  }

  @Test
  void attributeReference_nullPatchOperationPath_throwsNUllPointer() {
    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(Type.REPLACE)
      .value(true)
      .build();

    Throwable t = catchThrowable( () -> PatchUtil.attributeReference(patchOperation));

    assertThat(t).isInstanceOf(NullPointerException.class);

    NullPointerException ne = (NullPointerException) t;
    assertThat(ne.getMessage()).isEqualTo("Patch Operation Path must not be null");
  }

  @Test
  void multiValuedPrimaryUniqueness_multipleMultiValuedComplexElementWithFalsePrimary_successful() throws Exception {
    ScimUser user = ScimTestHelper.generateScimUser();
    ScimTestHelper.address(user, faker().address(), "home", false);
    ScimTestHelper.address(user, faker().address(), "work", true);
    ScimTestHelper.address(user, faker().address(), "other", false);

    final ObjectMapper objectMapper = ScimTestHelper.getObjectMapper(registry);
    Map<String,Object> map = objectMapper.convertValue(user, MAP_TYPE);

    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(Type.ADD)
      .path("addresses[type EQ \"school\"].primary")
      .value(true)
      .build();

    PatchUtil.multiValuedPrimaryUniqueness(map,patchOperation,registry);

    ScimUser actual = objectMapper.convertValue(map, ScimUser.class);

    assertThat(actual).isNotNull();

    Optional<Address> optional = actual.getAddresses()
      .stream()
      .filter(Address::getPrimary)
      .findFirst();

    // since the patch isn't applied we should end up with no address elements set to primary
    assertThat(optional).isEmpty();
  }
}
