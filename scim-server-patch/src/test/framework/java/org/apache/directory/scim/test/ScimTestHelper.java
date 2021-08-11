package org.apache.directory.scim.test;

import static java.util.stream.Collectors.toSet;
import static org.apache.directory.scim.server.patch.utility.PatchUtil.attributeLoggable;
import static org.apache.directory.scim.server.patch.utility.PatchUtil.genericClass;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.ADD;
import static org.apache.directory.scim.spec.schema.Schema.Attribute;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.time.LocalDateTime;
import javax.ws.rs.core.Response;

import org.apache.directory.scim.common.ScimUtils;
import org.apache.directory.scim.server.exception.InvalidProviderException;
import org.apache.directory.scim.server.provider.ProviderRegistry;
import org.apache.directory.scim.server.rest.ObjectMapperFactory;
import org.apache.directory.scim.server.schema.Registry;
import org.apache.directory.scim.server.utility.EtagGenerator;
import org.apache.directory.scim.spec.annotation.ScimExtensionType;
import org.apache.directory.scim.spec.annotation.ScimResourceType;
import org.apache.directory.scim.spec.extension.EnterpriseExtension;
import org.apache.directory.scim.spec.extension.ScimExtensionRegistry;
import org.apache.directory.scim.spec.phonenumber.PhoneNumberParseException;
import org.apache.directory.scim.spec.protocol.ErrorMessageType;
import org.apache.directory.scim.spec.protocol.attribute.AttributeReference;
import org.apache.directory.scim.spec.protocol.data.ErrorResponse;
import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.protocol.exception.ScimException;
import org.apache.directory.scim.spec.resources.Address;
import org.apache.directory.scim.spec.resources.BaseResource;
import org.apache.directory.scim.spec.resources.Email;
import org.apache.directory.scim.spec.resources.Entitlement;
import org.apache.directory.scim.spec.resources.Im;
import org.apache.directory.scim.spec.resources.KeyedResource;
import org.apache.directory.scim.spec.resources.PhoneNumber;
import org.apache.directory.scim.spec.resources.Photo;
import org.apache.directory.scim.spec.resources.Role;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimResource;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.resources.X509Certificate;
import org.apache.directory.scim.spec.schema.Meta;
import org.apache.directory.scim.spec.schema.ResourceReference;
import org.apache.directory.scim.spec.schema.ResourceType;
import org.apache.directory.scim.spec.schema.Schema;
import org.apache.directory.scim.test.builder.AddressBuilder;
import org.apache.directory.scim.test.builder.EmailBuilder;
import org.apache.directory.scim.test.builder.ListPatchOperationBuilder;
import org.apache.directory.scim.test.builder.MetaBuilder;
import org.apache.directory.scim.test.builder.NameBuilder;
import org.apache.directory.scim.test.builder.ScimGroupBuilder;
import org.apache.directory.scim.test.builder.ScimUserBuilder;
import org.apache.directory.scim.test.extensions.ExampleObjectExtension;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.github.javafaker.Name;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({"unused", "HttpUrlsUsage"})
@Slf4j
public class ScimTestHelper {
  public static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {};
  public static final TypeReference<List<Map<String, Object>>> LIST_MAP_TYPE = new TypeReference<List<Map<String, Object>>>() {
  };

  private static final EtagGenerator etagGenerator = createEtagGenerator();
  private static final Faker faker = Faker.instance();

  /**
   * Build a List of patch operations representing the differences between the two SCIM resources.
   *
   * @param registry the {@link Registry}
   * @param source the {@link ScimResource} representing the source
   * @param target the {@link ScimResource} representing the target
   * @param <T> the {@link ScimResource}
   * @return Returns a {@link List} of {@link PatchOperation}s representing the differences between {@code source} and {@code target}
   * @throws IllegalAccessException if {@code source} and/or {@code destination} are not compatible {@link ScimResource}
   */
  public static <T extends ScimResource> List<PatchOperation> diff(final Registry registry, final T source, final T target) throws IllegalAccessException {
    return ListPatchOperationBuilder.builder()
      .sourceResource(source)
      .targetResource(target)
      .registry(registry)
      .build();
  }

  public static ScimGroup generateScimGroup(final String id) {
    return ScimGroupBuilder.builder()
      .id(id)
      .meta(MetaBuilder.builder()
        .created(LocalDateTime.now())
        .lastModified(LocalDateTime.now())
        .resourceType("Group")
        .location("http://example.com/Groups/" + id)
        .build())
      .build();
  }

  public static ScimGroup generateScimGroup() {
    return generateScimGroup(UUID.randomUUID().toString());
  }

  public static ScimUser generateScimUser(final String id, final boolean includeScimName) {
    final Name name = faker.name();

    ScimUser user = ScimUserBuilder.builder()
      .id(id)
      .active(true)
      .meta(MetaBuilder.builder()
        .created(LocalDateTime.now())
        .lastModified(LocalDateTime.now())
        .resourceType("User")
        .location("http://example.com/Users/" + id)
        .build())
      .build();

    if(includeScimName) {
      name(user, name);
      username(user);
    } else {
      username(user,name);
    }

    return user;
  }

  public static ScimUser generateScimUser() {
    final String id = UUID.randomUUID().toString();

    return generateScimUser(id, false);
  }

  /**
   * Calculate the ETag version of the provided {@link ScimResource}s
   * @param group the {@link ScimResource} presenting the Scim Group
   * @param user the {@link ScimResource} presenting the Scim User
   */
  public static void versions(ScimGroup group, ScimUser user) {
    version(group);
    version(user);
  }

  /**
   * Calculate the ETag version of the provided {@link ScimResource}
   *
   * @param user the {@link ScimResource} presenting the Scim User
   */
  public static void version(ScimUser user) {
    if(user.getMeta() == null) {
      user.setMeta(new Meta());
    }

    try {
      user.getMeta().setVersion(etagGenerator.generateEtag(user).getValue());
    } catch (Exception e) {
      user.getMeta().setVersion(faker().internet().uuid());
    }
  }

  /**
   * Calculate the ETag version of the provided {@link ScimResource}
   * @param group the {@link ScimResource} presenting the Scim Group
   */
  public static void version(ScimGroup group) {
    if(group.getMeta() == null) {
      group.setMeta(new Meta());
    }

    try {
      group.getMeta().setVersion(etagGenerator.generateEtag(group).getValue());
    } catch (Exception e) {
      group.getMeta().setVersion(faker().internet().uuid());
    }
  }

  public static void name(ScimUser user, final com.github.javafaker.Name name) {
    user.setName(name(name));
  }

  public static org.apache.directory.scim.spec.resources.Name name(final com.github.javafaker.Name name) {
    final String prefix = name.prefix();
    final String first = name.firstName();
    final String middle = faker.funnyName().name().substring(0, 1).toUpperCase();
    final String last = name.lastName();
    final String suffix = name.suffix();

    return NameBuilder.builder()
      .honorificPrefix(prefix)
      .givenName(first)
      .middleName(middle)
      .familyName(last)
      .honorificSuffix(suffix)
      // preferred format "Mr. Richard E. Doe Jr."
      .formatted(String.format("%s %s %s %s %s", prefix, first, middle, last, suffix))
      .build();
  }

  public static Email email(final String type, final boolean primary) {
    Name name = faker().name();
    final String firstName = name.firstName();
    final String lastName = name.lastName();

    final String display = String.format("%s - %s %s", type.toUpperCase(),firstName, lastName);
    final String value = String.format("%s.%s@%s", firstName, lastName, faker().internet().domainName());

    return EmailBuilder.builder()
      .display(display)
      .primary(primary)
      .type(type)
      .value(value)
      .build();
  }

  public static void email(ScimUser user, final String type, final boolean primary) {
    Objects.requireNonNull(user.getUserName(), "SCIM username must not be null.");

    String emailAddress;
    if(user.getName() != null) {
      Objects.requireNonNull(user.getName().getGivenName(), "SCIM given name must not be null.");
      Objects.requireNonNull(user.getName().getFamilyName(), "SCIM family name must not be null.");
      emailAddress = String.format("%s.%s@%s", user.getName().getGivenName(), user.getName().getFamilyName(),
        faker().internet().domainName());
    } else {
      emailAddress = user.getUserName();
    }

    if (user.getEmails()==null) {
      user.setEmails(new ArrayList<>());
    }

    final String display = String.format("%s - Email address", type.toUpperCase());

    user.getEmails().add(EmailBuilder.builder()
      .display(display)
      .primary(primary)
      .type(type)
      .value(emailAddress)
      .build());
  }

  public static void localPhoneNumber(ScimUser user, final com.github.javafaker.PhoneNumber phoneNumber,
                                      final String type, final boolean primary) throws PhoneNumberParseException {
    if (user.getPhoneNumbers()==null) {
      user.setPhoneNumbers(new ArrayList<>());
    }

    user.getPhoneNumbers().add(localPhoneNumber(phoneNumber, type, primary));
  }

  public static PhoneNumber localPhoneNumber(final com.github.javafaker.PhoneNumber phoneNumber, final String type, final boolean primary) throws PhoneNumberParseException {
    final String display = String.format("%s - Phone #", type.toUpperCase());

    PhoneNumber p = new PhoneNumber();
    p.setDisplay(display);
    p.setPrimary(primary);
    p.setType(type);
    p.setValue(phoneNumber.phoneNumber());

    return p;
  }

  public static void externalId(ScimUser user) {
    user.setExternalId(String.format("e-%s", user.getId()));
  }

  public static void address(ScimUser user, final com.github.javafaker.Address address, final String type, final boolean primary) {
    if (user.getAddresses()==null) {
      user.setAddresses(new ArrayList<>());
    }

    user.getAddresses().add(address(address, type, primary));
  }

  public static Address address(final com.github.javafaker.Address address, final String type, final boolean primary) {
    final String streetAddress = address.streetAddress();
    final String city = address.city();
    final String state = address.state();
    final String zipCode = address.zipCode();
    final String countryCode = address.countryCode();

    final String display = String.format("%s - %s", type.toUpperCase(), streetAddress);
    // preferred format "123 Main St. State College, PA 16801"
    final String formatted = String.format("%s, %s, %s %s", streetAddress, city, state, zipCode);

    return AddressBuilder.builder()
      .streetAddress(streetAddress)
      .locality(city)
      .region(state)
      .postalCode(zipCode)
      .country(countryCode)
      .type(type)
      .primary(primary)
      .display(display)
      .formatted(formatted)
      .build();
  }

  public static void entitlement(ScimUser user, final String type, final boolean primary) {
    if(user.getEntitlements() == null) {
      user.setEntitlements(new ArrayList<>());
    }

    user.getEntitlements().add(entitlement(type, primary));
  }

  public static Entitlement entitlement(final String type, final boolean primary) {
    final String display = String.format("%s Entitlement", type.toUpperCase());
    final String value = faker().letterify("entitlement");

    Entitlement o = new Entitlement();
    o.setDisplay(display);
    o.setPrimary(primary);
    o.setType(type);
    o.setValue(value);

    return o;
  }

  public static void ims(ScimUser user, final String type, final boolean primary) {
    if(user.getIms() == null) {
      user.setIms(new ArrayList<>());
    }

    user.getIms().add(ims(type, primary));
  }

  public static Im ims(final String type, final boolean primary) {
    final String display = String.format("%s Instant messaging", type.toUpperCase());
    final String value = faker().letterify("im");

    Im o = new Im();
    o.setDisplay(display);
    o.setPrimary(primary);
    o.setType(type);
    o.setValue(value);

    return o;
  }

  public static void photo(ScimUser user, final String type, final boolean primary) {
    if(user.getPhotos() == null) {
      user.setPhotos(new ArrayList<>());
    }

    user.getPhotos().add(photo(type, primary));
  }

  public static Photo photo(final String type, final boolean primary) {
    final String display = String.format("%s Photo", type.toUpperCase());
    final String value = faker().letterify("photo");

    Photo o = new Photo();
    o.setDisplay(display);
    o.setPrimary(primary);
    o.setType(type);
    o.setValue(value);

    return o;
  }

  public static void group(ScimUser user, final ResourceReference.ReferenceType type, final boolean primary) {
    if(user.getGroups() == null) {
      user.setGroups(new ArrayList<>());
    }

    user.getGroups().add(group(type, primary));
  }

  public static ResourceReference group(final ResourceReference.ReferenceType type, final boolean primary) {
    final String display = String.format("%s Group -- %s", type.name().toUpperCase(), faker().random().hex());
    final String value = faker().letterify("group");

    ResourceReference o = new ResourceReference();
    o.setDisplay(display);
    o.setType(type);
    o.setValue(value);

    return o;
  }

  public static void role(ScimUser user, final String type, final boolean primary) {
    if(user.getRoles() == null) {
      user.setRoles(new ArrayList<>());
    }

    user.getRoles().add(role(type, primary));
  }

  public static Role role(final String type, final boolean primary) {
    final String display = String.format("%s Role", type.toUpperCase());
    final String value = faker().letterify("role");

    Role o = new Role();
    o.setDisplay(display);
    o.setPrimary(primary);
    o.setType(type);
    o.setValue(value);

   return o;
  }

  public static void x509Certificate(ScimUser user, final String type, final boolean primary, final String value) {
    if(user.getX509Certificates() == null) {
      user.setX509Certificates(new ArrayList<>());
    }

    user.getX509Certificates().add(x509Certificate(type, primary, value));
  }

  public static X509Certificate x509Certificate(final String type, final boolean primary, final String value) {
    final String display = String.format("%s x509Certificate", type.toUpperCase());

    X509Certificate o = new X509Certificate();
    o.setDisplay(display);
    o.setPrimary(primary);
    o.setType(type);
    o.setValue(value);

    return o;
  }

  public static void enterpriseExtension(ScimUser user, final boolean manager) {
    user.getSchemas().add(EnterpriseExtension.URN);
    user.setExtensions(EnterpriseExtension.URN, enterpriseExtension(manager));
  }

  public static EnterpriseExtension enterpriseExtension(final boolean manager) {

    EnterpriseExtension extension = new EnterpriseExtension();
    extension.setCostCenter(String.valueOf(faker.number().numberBetween(1024, 4096)));
    extension.setDepartment(faker.commerce().department());
    extension.setDivision(faker.job().position());
    extension.setEmployeeNumber(String.valueOf(faker.number().numberBetween(10000L, 999999L)));
    extension.setOrganization(faker.company().industry());

    if (manager) {
      EnterpriseExtension.Manager mgr = new EnterpriseExtension.Manager();
      Name name = faker.name();
      mgr.setDisplayName(name.username());
      mgr.setValue(faker.internet().uuid());

      extension.setManager(mgr);
    }

    return extension;
  }

  public static void member(ScimGroup group, final ResourceReference.ReferenceType type, final ScimUser user) {
    if(group.getMembers() == null) {
      group.setMembers(new ArrayList<>());
    }

    group.getMembers().add(member(type, user));
    group(user, type,false);
  }

  public static ResourceReference member(final ResourceReference.ReferenceType type, final ScimUser user) {
    final String display = String.format("%s Member -- %s", type.name().toUpperCase(), user.getUserName());

    ResourceReference o = new ResourceReference();
    o.setDisplay(display);
    o.setType(type);
    o.setValue(user.getId());

    return o;
  }

  public static void displayName(ScimGroup group) {
    group.setDisplayName("");
  }

  private static void username(ScimUser user, final Name name) {
    Objects.requireNonNull(user, "SCIM User resource must not be null.");
    Objects.requireNonNull(name, "name must not be null.");

    user.setDisplayName(String.format("%s %s", name.firstName(), name.lastName()));
    user.setUserName(String.format("%s.%s@%s", name.firstName(), name.lastName(), faker().internet().domainName()));
  }

  private static void username(final ScimUser user) {
    Objects.requireNonNull(user.getName(), "SCIM User name resource must not be null.");
    Objects.requireNonNull(user.getName().getGivenName(), "SCIM User name given name must not be null.");
    Objects.requireNonNull(user.getName().getFamilyName(), "SCIM User name family name must not be null.");

    user.setDisplayName(String.format("%s %s", user.getName().getGivenName(), user.getName().getGivenName()));
    user.setUserName(String.format("%s.%s@%s", user.getName().getGivenName(), user.getName().getGivenName(),
      faker().internet().domainName()));
  }

  public static AttributeReference attributeReference(final PatchOperation operation) {
    return Objects.requireNonNull(
      Objects.requireNonNull(
        Objects.requireNonNull(operation, "Patch operation must not be null")
          .getPath(), "Patch Operation Path must not be null")
        .getValuePathExpression(), "Value Path Expression must not be null")
      .getAttributePath();
  }

  public static <T> void printField(T obj, Function<? super T,?> getter) {
    log.info(getter.apply(obj).toString());
  }

  public static <T,R> void setField(T obj, R value, BiConsumer<T, R> setter) {
    setter.accept(obj, value);
  }

  public static Object mapValue(final PatchOperation.Type type, final Map<String, Object> map) {
    switch (type) {
      case ADD:
      case REPLACE:
        return map.values().toArray()[0];
      case REMOVE:
        return null;
    }

    return null;
  }

  public static int multiValuedPrimaryUniquenessCount(List<?> multiValuedList) {
    int primaryCount = 0;

    for (final Object object : multiValuedList) {
      if (object instanceof Address && ((Address) object).getPrimary()) {
        primaryCount++;
      } else if (object instanceof Email && ((Email) object).getPrimary()) {
        primaryCount++;
      } else if (object instanceof Entitlement && ((Entitlement) object).getPrimary()) {
        primaryCount++;
      } else if (object instanceof Im && ((Im) object).getPrimary()) {
        primaryCount++;
      } else if (object instanceof PhoneNumber && ((PhoneNumber) object).getPrimary()) {
        primaryCount++;
      } else if (object instanceof Photo && ((Photo) object).getPrimary()) {
        primaryCount++;
      } else if (object instanceof Role && ((Role) object).getPrimary()) {
        primaryCount++;
      } else if (object instanceof X509Certificate && ((X509Certificate) object).getPrimary()) {
        primaryCount++;
      }
    }

    return primaryCount;
  }

  public static void assertScimException(Throwable t,
                                         Response.Status expectedStatus,
                                         ErrorMessageType expectedErrorMessageType,
                                         String expectedMessageFragment) {

    assertThat(t).isInstanceOf(ScimException.class);

    ScimException sre = (ScimException) t;
    assertThat(sre.getStatus()).isEqualTo(expectedStatus);
    assertThat(sre.getError()).isNotNull();
    ErrorResponse errorResponse = sre.getError();

    assertThat(errorResponse.getScimType()).isEqualTo(expectedErrorMessageType);
    assertThat(errorResponse.getDetail()).isEqualTo(expectedMessageFragment);
  }

  public static EtagGenerator createEtagGenerator() {
    return new EtagGenerator();
  }

  public static Registry createRegistry() throws Exception {

    // Scim resources
    Schema scimUserSchema = ProviderRegistry.generateSchema(ScimUser.class,
      ScimUtils.getFieldsUpTo(ScimUser.class, BaseResource.class));
    Schema scimGroupSchema = ProviderRegistry.generateSchema(ScimGroup.class,
      ScimUtils.getFieldsUpTo(ScimGroup.class, BaseResource.class));

    // Scim extensions
    Schema scimEnterpriseUserSchema = ProviderRegistry.generateSchema(EnterpriseExtension.class,
      ScimUtils.getFieldsUpTo(EnterpriseExtension.class, Object.class));
    Schema scimExampleSchema = ProviderRegistry.generateSchema(ExampleObjectExtension.class,
      ScimUtils.getFieldsUpTo(ExampleObjectExtension.class, BaseResource.class));

    Registry registry = mock(Registry.class);

    when(registry.getBaseSchemaOfResourceType(ScimUser.RESOURCE_NAME)).thenReturn(scimUserSchema);
    when(registry.getBaseSchemaOfResourceType(ScimGroup.RESOURCE_NAME)).thenReturn(scimGroupSchema);
    when(registry.getSchema(ScimUser.SCHEMA_URI)).thenReturn(scimUserSchema);
    when(registry.getSchema(ScimGroup.SCHEMA_URI)).thenReturn(scimGroupSchema);
    when(registry.getSchema(EnterpriseExtension.URN)).thenReturn(scimEnterpriseUserSchema);
    when(registry.getSchema(ExampleObjectExtension.URN)).thenReturn(scimExampleSchema);
    when(registry.getAllSchemas()).thenReturn(Arrays.asList(scimUserSchema, scimGroupSchema, scimEnterpriseUserSchema, scimExampleSchema));
    when(registry.getAllSchemaUrns()).thenReturn(new HashSet<>(Arrays.asList(ScimUser.SCHEMA_URI, ScimGroup.SCHEMA_URI, EnterpriseExtension.URN, ExampleObjectExtension.URN)));
    when(registry.getAllResourceTypes()).thenReturn(new HashSet<>(Arrays.asList(scimUserResourceType(), scimGroupResourceType())));

    return registry;
  }

  public static Set<Attribute> singularValuedAttributes(Registry registry, final String schemaUrn) {
    Function<Attribute,Boolean> function = (attribute) -> attribute!=null && !attribute.isMultiValued() && !attribute.getType().equals(Attribute.Type.COMPLEX);
    return valuedAttributes(registry, schemaUrn, function);
  }

  public static Set<Attribute> complexValuedAttributes(Registry registry, final String schemaUrn) {
    Function<Attribute,Boolean> function = (attribute) -> attribute!=null && !attribute.isMultiValued() && attribute.getType().equals(Attribute.Type.COMPLEX);
    return valuedAttributes(registry, schemaUrn, function);
  }

  public static Set<Attribute> multiValuedComplexAttributes(Registry registry, final String schemaUrn) {
    Function<Attribute,Boolean> function = (attribute) -> attribute!=null && attribute.isMultiValued() && attribute.getType().equals(Attribute.Type.COMPLEX);
    return valuedAttributes(registry, schemaUrn, function);
  }

  private static Set<Attribute> valuedAttributes(Registry registry, final String schemaUrn, final Function<Attribute,Boolean> function) {
    Schema schema = registry.getSchema(schemaUrn);
    return schema.getAttributes()
      .stream()
      .filter(function::apply)
      .collect(toSet());
  }

  public static ObjectMapper getObjectMapper() throws Exception {
    return new ObjectMapperFactory(createRegistry()).createObjectMapper();
  }
  public static ObjectMapper getObjectMapper(final Registry registry) {
    return new ObjectMapperFactory(registry).createObjectMapper();
  }

  public static Faker faker() {
    return faker;
  }

  public static Object get(final ScimUser user, final Schema.Attribute attribute) throws Exception {
    return attribute.getField().get(user);
  }

  @SuppressWarnings("unchecked")
  public static Object get(final ScimUser user, final Schema.Attribute attribute, final Schema.Attribute subAttribute) throws Exception {
    Object attributeValue = attribute.getField().get(user);
    if(subAttribute != null) {
      if(attribute.isMultiValued()) {
        if (!subAttribute.getType().equals(Schema.Attribute.Type.COMPLEX)) {
          if(attributeValue instanceof List) {
            List<Map<String,Object>> list = (List<Map<String,Object>>) attributeValue;
            Object element = list.get(0);
            return subAttribute.getField().get(element);
          }

          if(attributeValue != null) {
            return subAttribute.getField().get(attributeValue);
          }

          return null;
        } else { // else sub-attribute is a complex type.
          if(attributeValue instanceof List) {
            List<Map<String,Object>> list = (List<Map<String,Object>>) attributeValue;
            Object element = list.get(0);
            return subAttribute.getField().get(element);
          }
        }
        // isMultiValued == false
      } else {
        if (!subAttribute.getType().equals(Schema.Attribute.Type.COMPLEX)) {
          if(attributeValue instanceof List) {
            List<Map<String,Object>> list = (List<Map<String,Object>>) attributeValue;
            Object element = list.get(0);
            return subAttribute.getField().get(element);
          }

          if(attributeValue != null) {
            return subAttribute.getField().get(attributeValue);
          }

          return null;
        }
      }
    }

    return attributeValue;
  }

  public static Object set(final Schema.Attribute attribute) throws Exception {
    return set(attribute, null);
  }

  public static Object set(final Schema.Attribute attribute, final Schema.Attribute subAttribute) throws Exception {
    switch (attribute.getType()) {
      case BOOLEAN:
        return faker().bool().bool();
      case COMPLEX:
        if(subAttribute == null) {
          if (attribute.isMultiValued()) {
            return multiValueSet(attribute);
          } else {
            switch(attribute.getName()) {
              case "name":
                return ScimTestHelper.name(faker().name());
              case "groups":
                return ScimTestHelper.group(ResourceReference.ReferenceType.values()[faker().random().nextInt(0, 1)],
                  faker().random().nextBoolean());
              case "type":
                return ResourceReference.ReferenceType.values()[faker().random().nextInt(0, 1)].name().toLowerCase();
              default:
                log.error("Unhandled switch set: {}", attributeLoggable(attribute));
            }
          }
        } else if(attribute.isMultiValued()) {
          return multiValueSet(attribute, subAttribute);
        } else {
          return set(subAttribute);
        }
        break;
      case DATE_TIME:
        return LocalDateTime.now();
      case DECIMAL:
        return faker().number().randomDouble(2, 0, 1024);
      case INTEGER:
        return faker().number().randomDigit();
      case REFERENCE:
        return new URI(faker().internet().url());
      case BINARY:
      case STRING:
        return faker().letterify("?? " + attribute.getType().name() + " ??");
    }

    return faker().letterify("?? unknown attribute type(" + attribute.getType() + ") ??");
  }

  public static Object multiValueSet(final Schema.Attribute attribute, final Schema.Attribute subAttribute) throws Exception {
    if(subAttribute == null) {
      return multiValueSet(attribute);
    } else {
      List<KeyedResource> keyedResources = new ArrayList<>();
      KeyedResource keyedResource;

      switch (attribute.getName()) {
        case "emails":
          keyedResource = new Email();
          break;
        case "addresses":
          keyedResource = new Address();
          break;
        case "Entitlement":
          keyedResource = new Entitlement();
          break;
        case "PhoneNumber":
          keyedResource = new PhoneNumber();
          break;
        case "Photo":
          keyedResource = new Photo();
          break;
        case "Role":
          keyedResource = new Role();
          break;
        default:
          throw new IllegalStateException("Unexpected value: " + attribute.getName());
      }

      subAttribute.getField().set(keyedResource, set(subAttribute));

      keyedResources.add(keyedResource);
      return keyedResources;
    }
  }

  public static Object multiValueSet(final Schema.Attribute attribute) throws Exception {
    Field field = attribute.getField();

    if(field.getType().equals(List.class)) {
      ParameterizedType listType = (ParameterizedType) field.getGenericType();
      Type actualTypeArgument = listType.getActualTypeArguments()[0];
      Class<?> clazz = Class.forName(actualTypeArgument.getTypeName());

      log.info("CLASS: {} {}", clazz.getName(), attributeLoggable(attribute));
      List<KeyedResource> keyedResources = new ArrayList<>();
      KeyedResource keyedResource;

      keyedResource = (KeyedResource) clazz.newInstance();

      switch (attribute.getName()) {
        case "emails":
          keyedResource = ScimTestHelper.email(new String[]{"home", "work"}[faker().random().nextInt(0, 1)],
            faker().random().nextBoolean());
          break;
        case "addresses":
          com.github.javafaker.Address address = faker().address();
          keyedResource = ScimTestHelper.address(address,
            new String[]{"home", "work"}[faker().random().nextInt(0, 1)], faker().random().nextBoolean());
          break;
        case "Entitlement":
          keyedResource = ScimTestHelper.entitlement(new String[]{"home", "work"}[faker().random().nextInt(0, 1)],
            faker().random().nextBoolean());
          break;
        case "PhoneNumber":
          keyedResource = ScimTestHelper.localPhoneNumber(faker().phoneNumber(),
            new String[]{"home", "work"}[faker().random().nextInt(0, 1)], faker().random().nextBoolean());
          break;
        case "Photo":
          keyedResource = ScimTestHelper.photo(new String[]{"home", "work"}[faker().random().nextInt(0, 1)],
            faker().random().nextBoolean());
          break;
        case "Role":
          keyedResource = ScimTestHelper.role(new String[]{"home", "work"}[faker().random().nextInt(0, 1)],
            faker().random().nextBoolean());
          break;
      }

      keyedResources.add(keyedResource);
      return keyedResources;
    } else {
      throw new IllegalArgumentException("Unsupported class '" + field.getType().getName() + "'");
    }
  }

  public static void initialize(Object object, final PatchOperation.Type type, final Schema.Attribute attribute) throws Exception {
    initialize(object, type, attribute, null);
  }

  @SuppressWarnings("unchecked")
  public static void initialize(Object object, final PatchOperation.Type operation, final Schema.Attribute attribute, final Schema.Attribute subAttribute) throws Exception {
    if (!operation.equals(ADD)) {
      switch (attribute.getType()) {
        case COMPLEX:
          Field field = attribute.getField();
          Object instance;
          if(field.getType().equals(List.class)) {
            Class<?> clazz = genericClass(attribute);
            instance = new ArrayList<>();
            Object genericInstance;
            if(clazz.equals(ResourceReference.class)) {
              genericInstance = ScimTestHelper.group(ResourceReference.ReferenceType.values()[faker().random().nextInt(0, 1)],
                faker().random().nextBoolean());
            } else {
              genericInstance = clazz.newInstance();
              initialize(genericInstance, operation, subAttribute);
            }

            ((List<Object>) instance).add(genericInstance);
          } else {
            instance = createContents(field.getType());
            initialize(instance, operation, subAttribute);
          }

          attribute.getField().set(object, instance);
          break;
        case BOOLEAN:
        case DATE_TIME:
        case DECIMAL:
        case INTEGER:
        case REFERENCE:
        case BINARY:
        case STRING:
          if(subAttribute == null) {
            attribute.getField().set(object, set(attribute));
          } else {
            attribute.getField().set(object, set(attribute, subAttribute));
          }
          break;
      }
    }
  }

  public static <E> E createContents(Class<E> clazz) throws InstantiationException, IllegalAccessException {
    return clazz.newInstance();
  }

  private static ResourceType scimUserResourceType() throws InvalidProviderException {
    /*
     * the following section is required to get the Scim User Extensions to serialize/deserialize to/from
     * Map<String,Object> representations.
     *
     * This code was copied here so that the mock is usable without instantiating real objects when testing.
     */
    ScimExtensionRegistry extensionRegistry = ScimExtensionRegistry.getInstance();
    final List<Class<? extends ScimExtension>> extensions = new ArrayList<>();
    extensions.add(EnterpriseExtension.class);
    extensions.add(ExampleObjectExtension.class);

    List<ResourceType.SchemaExtentionConfiguration> extensionSchemaList = new ArrayList<>();

    ScimResourceType scimResourceType = ScimUser.class.getAnnotation(ScimResourceType.class);
    ResourceType resourceType = new ResourceType();
    resourceType.setDescription(scimResourceType.description());
    resourceType.setId(scimResourceType.id());
    resourceType.setName(scimResourceType.name());
    resourceType.setEndpoint(scimResourceType.endpoint());
    resourceType.setSchemaUrn(scimResourceType.schema());

    for (Class<? extends ScimExtension> se : extensions) {

      ScimExtensionType extensionType = se.getAnnotation(ScimExtensionType.class);

      if (extensionType==null) {
        throw new InvalidProviderException(
          "Missing annotation: ScimExtensionType must be at the top of scim extension classes");
      }

      extensionRegistry.registerExtension(ScimUser.class, se);

      ResourceType.SchemaExtentionConfiguration ext = new ResourceType.SchemaExtentionConfiguration();
      ext.setRequired(extensionType.required());
      ext.setSchemaUrn(extensionType.id());
      extensionSchemaList.add(ext);
    }

    resourceType.setSchemaExtensions(extensionSchemaList);

    return resourceType;
  }

  private static ResourceType scimGroupResourceType() throws InvalidProviderException {
    /*
     * the following section is required to get the Scim User Extensions to serialize/deserialize to/from
     * Map<String,Object> representations.
     *
     * This code was copied here so that the mock is usable without instantiating real objects when testing.
     */
    ScimExtensionRegistry extensionRegistry = ScimExtensionRegistry.getInstance();
    final List<Class<? extends ScimExtension>> extensions = new ArrayList<>();
    extensions.add(EnterpriseExtension.class);
    extensions.add(ExampleObjectExtension.class);

    List<ResourceType.SchemaExtentionConfiguration> extensionSchemaList = new ArrayList<>();

    ScimResourceType scimResourceType = ScimGroup.class.getAnnotation(ScimResourceType.class);
    ResourceType resourceType = new ResourceType();
    resourceType.setDescription(scimResourceType.description());
    resourceType.setId(scimResourceType.id());
    resourceType.setName(scimResourceType.name());
    resourceType.setEndpoint(scimResourceType.endpoint());
    resourceType.setSchemaUrn(scimResourceType.schema());

    for (Class<? extends ScimExtension> se : extensions) {

      ScimExtensionType extensionType = se.getAnnotation(ScimExtensionType.class);

      if (extensionType==null) {
        throw new InvalidProviderException(
          "Missing annotation: ScimExtensionType must be at the top of scim extension classes");
      }

      extensionRegistry.registerExtension(ScimUser.class, se);

      ResourceType.SchemaExtentionConfiguration ext = new ResourceType.SchemaExtentionConfiguration();
      ext.setRequired(extensionType.required());
      ext.setSchemaUrn(extensionType.id());
      extensionSchemaList.add(ext);
    }

    resourceType.setSchemaExtensions(extensionSchemaList);

    return resourceType;
  }

  public static void logPatchOperationBuilder(final List<PatchOperation> operations) {
    operations.forEach(ScimTestHelper::logPatchOperationBuilder);
  }

  public static void logPatchOperationBuilder(final PatchOperation operation) {
    final StringBuilder sb = new StringBuilder();

    sb.append(System.getProperty("line.separator"));
    sb.append(System.getProperty("line.separator"));
    sb.append("Patch Operation Builder:");
    sb.append(System.getProperty("line.separator"));
    sb.append(System.getProperty("line.separator"));
    sb.append("PatchOperation patchOperation = PatchOperationBuilder.builder()");
    sb.append(System.getProperty("line.separator"));
    sb.append("\t\t\t.operation(").append(operation.getOperation()).append(")");
    sb.append(System.getProperty("line.separator"));
    sb.append("\t\t\t.path(\"").append(operation.getPath().toString()).append("\")");
    sb.append(System.getProperty("line.separator"));

    Object value = operation.getValue();
    if (value != null) {
      if (value instanceof String) {
        sb.append("\t\t\t.value(\"").append(operation.getValue()).append("\")");
      } else {
        sb.append("\t\t\t.value(").append(value).append(")");
      }

      sb.append(System.getProperty("line.separator"));
    }

    sb.append("\t\t\t.build();");
    sb.append(System.getProperty("line.separator"));
    log.info(sb.toString());
  }

  static void logAttributeReference(final PatchOperation operation) {
    final AttributeReference reference = operation.getPath().getValuePathExpression().getAttributePath();
    logAttributeReference(reference);
  }

  static void logAttributeReference(final AttributeReference reference) {
    log.info("ATTRIBUTE REFERENCE URN             : {}", reference.getUrn());
    log.info("ATTRIBUTE REFERENCE NAME            : {}", reference.getAttributeName());
    log.info("ATTRIBUTE REFERENCE SUB-NAME        : {}", reference.getSubAttributeName());
    log.info("ATTRIBUTE REFERENCE FULL NAME       : {}", reference.getFullAttributeName());
    log.info("ATTRIBUTE REFERENCE FULLY QUALIFIED : {}", reference.getFullyQualifiedAttributeName());
  }
}
