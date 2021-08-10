package org.apache.directory.scim.test.arguments.provider;

import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.ADD;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.REPLACE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.test.ScimTestHelper;
import org.apache.directory.scim.test.arguments.provider.args.PatchArgs;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import com.google.common.collect.ImmutableMap;

/**
 * Omitted Target Location means there isn't path included with the patch operations, the value contains map of
 * key/value pairs of attribute names and values.
 */
public class OmittedTargetLocationArgumentsProvider implements ArgumentsProvider {
  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
    List<Arguments> values = new ArrayList<>();

    final String expectedValue = ScimTestHelper.faker().numerify("## Display Name ##");

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      // target omitted i.e. no path
      .value(ImmutableMap.of("displayName",expectedValue))
      .validate(ScimUser::getDisplayName, expectedValue)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REPLACE)
      // target omitted i.e. no path
      .value(ImmutableMap.of("displayName",expectedValue))
      // ensure that there is an existing value
      .setter( user -> {
        user.setDisplayName(ScimTestHelper.faker().numerify("?? display name ??"));
        return null; // return ignored.
      })
      .validate(ScimUser::getDisplayName, expectedValue)
      .build()));

    return values.stream();
  }
}
