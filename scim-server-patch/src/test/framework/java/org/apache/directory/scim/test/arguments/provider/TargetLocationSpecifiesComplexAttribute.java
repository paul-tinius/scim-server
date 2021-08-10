package org.apache.directory.scim.test.arguments.provider;

import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.ADD;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.REPLACE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.test.ScimTestHelper;
import org.apache.directory.scim.test.arguments.provider.args.PatchArgs;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import com.google.common.collect.ImmutableMap;

public class TargetLocationSpecifiesComplexAttribute implements ArgumentsProvider {
  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
    List<Arguments> values = new ArrayList<>();

    // generate fake data
    final com.github.javafaker.Name name = ScimTestHelper.faker().name();
    final String givenName = name.firstName();
    final String familyName = name.lastName();

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      .path("name")
      .value(ImmutableMap.of(
        "givenName",givenName,
        "familyName",familyName))
      .validate(user -> user.getName() != null, true)
      .validate(user -> user.getName().getGivenName(), givenName)
      .validate(user -> user.getName().getFamilyName(), familyName)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REPLACE)
      .path("name")
      .setter(user -> {
        user.setName(new Name());
        user.getName().setGivenName("givenName");
        user.getName().setFamilyName("familyName");
        return null; // return value is ignored
      })
      .value(ImmutableMap.of(
        "givenName",givenName,
        "familyName",familyName))
      .validate(user -> user.getName() != null, true)
      .validate(user -> user.getName().getGivenName(), givenName)
      .validate(user -> user.getName().getFamilyName(), familyName)
      .build()));

    return values.stream();  }
}
