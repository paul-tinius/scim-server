package org.apache.directory.scim.test.arguments.provider;

import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.ADD;
import static org.apache.directory.scim.test.ScimTestHelper.faker;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.test.arguments.provider.args.PatchArgs;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class TargetLocationDoesNotExistArgumentsProvider implements ArgumentsProvider {
  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
    List<Arguments> values = new ArrayList<>();

    // traditional for loop, don't really care about the exception from PatchOperationPath, if exception thrown fail the tests.
    for (Type type : Type.values()) {
      /*
       * skip REPLACE/REMOVE since We can't replace or remove what isn't there. - see section 3.5.2.3/4 of RFC7644
       */
      if (type.equals(ADD)) {
        // Scim User
        final String displayNameValue = "** DISPLAY NAME **";
        values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
          .type(type)
          .path("displayName")
          .value(displayNameValue)
          .validate(ScimUser::getDisplayName, displayNameValue)
          .build()));

        values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
          .type(type)
          .path("name.givenName")
          .value("** NAME(GIVEN NAME) **")
          .validate(scimUser -> scimUser.getName().getGivenName(), "** NAME(GIVEN NAME) **")
          .build()));

        values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
          .type(type)
          .path("addresses[type EQ \"home\"].country")
          .value("US")
          .validate(scimUser -> scimUser.getAddresses().get(0).getCountry(), "US")
          .validate(scimUser -> scimUser.getAddresses().get(0).getType(), "home")
          .build()));

        values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
          .type(type)
          .path("emails[type EQ \"work\"].value")
          .value("first.last@domain.domainSuffix")
          .validate(scimUser -> scimUser.getEmails().get(0).getValue(), "first.last@domain.domainSuffix")
          .validate(scimUser -> scimUser.getEmails().get(0).getType(), "work")
          .build()));

        final String entitlementValue = faker().numerify("## ENTITLEMENT(VALUE) ##");
        values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
          .type(type)
          .path("entitlements[display EQ \"entitled\"].value")
          .value(entitlementValue)
          .validate(scimUser -> scimUser.getEntitlements().get(0).getValue(), entitlementValue)
          .validate(scimUser -> scimUser.getEntitlements().get(0).getDisplay(), "entitled")
          .build()));

        final String imValue = faker().numerify("## IMS(VALUE) ##");
        values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
          .type(type)
          .path("ims[primary EQ true].value")
          .value(imValue)
          .validate(scimUser -> scimUser.getIms().get(0).getValue(), imValue)
          .validate(scimUser -> scimUser.getIms().get(0).getPrimary(), true)
          .build()));

        final String phoneNumberDisplay = faker().numerify("## PHONE NUMBERS(DISPLAY) ##");
        final String phoneNumber = faker().phoneNumber().phoneNumber();
        values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
          .type(type)
          .path("phoneNumbers[value EQ \"" + phoneNumber + "\"].display")
          .value(phoneNumberDisplay)
          .validate(scimUser -> scimUser.getPhoneNumbers().get(0).getDisplay(), phoneNumberDisplay)
          .validate(scimUser -> scimUser.getPhoneNumbers().get(0).getValue(), phoneNumber)
          .build()));

        values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
          .type(type)
          .path("roles[type EQ \"ADMIN\"].primary")
          .value(true)
          .validate(scimUser -> scimUser.getRoles().get(0).getPrimary(), true)
          .validate(scimUser -> scimUser.getRoles().get(0).getType(), "ADMIN")
          .build()));

        values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
          .type(type)
          .path("x509Certificates[type EQ \"TLS\"].primary")
          .value(false)
          .validate(scimUser -> scimUser.getX509Certificates().get(0).getPrimary(), false)
          .validate(scimUser -> scimUser.getX509Certificates().get(0).getType(), "TLS")
          .build()));
      }
    }

    return values.stream();
  }
}
