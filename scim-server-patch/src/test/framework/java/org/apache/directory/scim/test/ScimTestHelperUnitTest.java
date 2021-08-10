package org.apache.directory.scim.test;

import static org.apache.directory.scim.spec.schema.Schema.Attribute;
import static org.apache.directory.scim.spec.schema.Schema.Attribute.Type.COMPLEX;
import static org.apache.directory.scim.test.ScimTestHelper.createRegistry;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class ScimTestHelperUnitTest {

  private static Registry registry;

  private static final Set<String> EXPECTED_GROUP_SINGULAR_VALUED = new HashSet<>();
  static {
    EXPECTED_GROUP_SINGULAR_VALUED.add("externalId");
    EXPECTED_GROUP_SINGULAR_VALUED.add("id");
    EXPECTED_GROUP_SINGULAR_VALUED.add("displayName");
  }

  private static final Set<String> EXPECTED_USER_SINGULAR_VALUED = new HashSet<>();
  static {
    EXPECTED_USER_SINGULAR_VALUED.add("userName");
    EXPECTED_USER_SINGULAR_VALUED.add("timezone");
    EXPECTED_USER_SINGULAR_VALUED.add("title");
    EXPECTED_USER_SINGULAR_VALUED.add("userType");
    EXPECTED_USER_SINGULAR_VALUED.add("password");
    EXPECTED_USER_SINGULAR_VALUED.add("profileUrl");
    EXPECTED_USER_SINGULAR_VALUED.add("preferredLanguage");
    EXPECTED_USER_SINGULAR_VALUED.add("externalId");
    EXPECTED_USER_SINGULAR_VALUED.add("nickName");
    EXPECTED_USER_SINGULAR_VALUED.add("id");
    EXPECTED_USER_SINGULAR_VALUED.add("locale");
    EXPECTED_USER_SINGULAR_VALUED.add("active");
    EXPECTED_USER_SINGULAR_VALUED.add("displayName");
  }

  private static final Set<String> EXPECTED_USER_COMPLEX_VALUED = new HashSet<>();
  static {
    EXPECTED_USER_COMPLEX_VALUED.add("name");
    EXPECTED_USER_COMPLEX_VALUED.add("meta");
  }

  private static final Set<String> EXPECTED_USER_MULTI_VALUED = new HashSet<>();
  static {
    EXPECTED_USER_MULTI_VALUED.add("phoneNumbers");
    EXPECTED_USER_MULTI_VALUED.add("addresses");
    EXPECTED_USER_MULTI_VALUED.add("x509Certificates");
    EXPECTED_USER_MULTI_VALUED.add("entitlements");
    EXPECTED_USER_MULTI_VALUED.add("emails");
    EXPECTED_USER_MULTI_VALUED.add("ims");
    EXPECTED_USER_MULTI_VALUED.add("roles");
    EXPECTED_USER_MULTI_VALUED.add("groups");
    EXPECTED_USER_MULTI_VALUED.add("photos");
  }

  @BeforeAll
  static void setUp() throws Exception {
    registry = createRegistry();
  }

  @Test
  void userSingularValuedAttributes_userSingularValued_successful() {
    Set<Attribute> attributes = ScimTestHelper.singularValuedAttributes(registry, ScimUser.SCHEMA_URI);
    attributes.forEach(attribute -> {
      assertThat(EXPECTED_USER_SINGULAR_VALUED.contains(attribute.getName())).isTrue();
      assertThat(attribute.isMultiValued()).isFalse();
      assertThat(attribute.getType()).isNotEqualTo(COMPLEX);
    });
  }

  @Test
  void groupSingularValuedAttributes_groupSingularValued_successful() {
    Set<Attribute> attributes = ScimTestHelper.singularValuedAttributes(registry, ScimGroup.SCHEMA_URI);
    attributes.forEach(attribute -> {
      assertThat(EXPECTED_GROUP_SINGULAR_VALUED.contains(attribute.getName())).isTrue();
      assertThat(attribute.isMultiValued()).isFalse();
      assertThat(attribute.getType()).isNotEqualTo(COMPLEX);
    });
  }

  @Test
  void complexValuedAttributes_scenarioBeingTested_expectedResult() {
    Set<Attribute> attributes = ScimTestHelper.complexValuedAttributes(registry, ScimUser.SCHEMA_URI);
    attributes.forEach(attribute -> {
      assertThat(EXPECTED_USER_COMPLEX_VALUED.contains(attribute.getName())).isTrue();
      assertThat(attribute.isMultiValued()).isFalse();
      assertThat(attribute.getType()).isEqualTo(COMPLEX);
    });
  }

  @Test
  void multiValuedComplexAttributes_scenarioBeingTested_expectedResult() {
    Set<Attribute> attributes = ScimTestHelper.multiValuedComplexAttributes(registry, ScimUser.SCHEMA_URI);
    attributes.forEach(attribute -> {
      assertThat(EXPECTED_USER_MULTI_VALUED.contains(attribute.getName())).isTrue();
      assertThat(attribute.isMultiValued()).isTrue();
      assertThat(attribute.getType()).isEqualTo(COMPLEX);
    });  }
}
