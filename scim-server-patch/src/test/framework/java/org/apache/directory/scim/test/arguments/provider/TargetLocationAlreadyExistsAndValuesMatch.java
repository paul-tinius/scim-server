package org.apache.directory.scim.test.arguments.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.resources.Address;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.test.ScimTestHelper;
import org.apache.directory.scim.test.arguments.provider.args.PatchArgs;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class TargetLocationAlreadyExistsAndValuesMatch implements ArgumentsProvider {
  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
      List<Arguments> values = new ArrayList<>();

      final String singularAttr = "title";
      final String singularValue = "** TITLE **";

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(PatchOperation.Type.REPLACE)
      .path(singularAttr)
      .value(singularValue)
      .setter(user -> {
        user.setTitle(singularValue);
        return null;
      })
      .validate(ScimUser::getTitle, singularValue)
      .build()));

    final String complexAttr = "name.givenName";
    final String complexValue = "** Given Name **";

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(PatchOperation.Type.REPLACE)
      .path(complexAttr)
      .value(complexValue)
      .setter(user -> {
        user.setName(new Name());
        user.getName().setGivenName(complexValue);
        return null;
      })
      .validate(user -> user.getName() != null, true)
      .validate(user -> user.getName().getGivenName(), complexValue)
      .build()));

    final String complexMultiValuedAttr = "addresses[type EQ \"home\"].streetAddress";
    final Address address = ScimTestHelper.address(ScimTestHelper.faker().address(), "home", true);
    final String complexMultiValuedValue = address.getStreetAddress();

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(PatchOperation.Type.REPLACE)
      .path(complexMultiValuedAttr)
      .value(complexMultiValuedValue)
      .setter(user -> {
        user.setAddresses(new ArrayList<>());
        user.getAddresses().add(address);
        return null;
      })
      .validate(user -> user.getAddresses() != null, true)
      .validate(user -> user.getAddresses().get(0).getStreetAddress(), complexMultiValuedValue)
      .build()));

      return values.stream();
    }
}
