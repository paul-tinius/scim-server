package org.apache.directory.scim.test.arguments.provider;

import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.ADD;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.REMOVE;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.REPLACE;
import static org.apache.directory.scim.test.ScimTestHelper.faker;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.test.arguments.provider.args.PatchArgs;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import com.github.javafaker.Name;

public class TargetLocationSpecifiedSingularValuedAttribute implements ArgumentsProvider {
  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
    final List<Arguments> values = new ArrayList<>();

    // ScimTestHelper.faker()
    Name name = faker().name();

    final String expectedDisplayNameValue = name.name();
    final String expectedLocaleValue = faker().nation().language();
    final String expectedNickNameValue = name.suffix();
    final String expectedProfileUrlValue = faker().internet().url();
    final String expectedPreferredLanguageValue = faker().nation().language();
    final String expectedTimezoneValue = faker().address().timeZone();
    final String expectedTitleValue = faker().job().title();
    final String expectedUsernameValue = name.username();
    final String expectedUserTypeValue = faker().job().position();

    // ADD
    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      .path("displayName")
      .value(expectedDisplayNameValue)
      .validate(ScimUser::getDisplayName, expectedDisplayNameValue)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      .path("locale")
      .value(expectedLocaleValue)
      .validate(ScimUser::getLocale, expectedLocaleValue)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      .path("nickName")
      .value(expectedNickNameValue)
      .validate(ScimUser::getNickName, expectedNickNameValue)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      .path("profileUrl")
      .value(expectedProfileUrlValue)
      .validate(ScimUser::getProfileUrl, expectedProfileUrlValue)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      .path("preferredLanguage")
      .value(expectedPreferredLanguageValue)
      .validate(ScimUser::getPreferredLanguage, expectedPreferredLanguageValue)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      .path("timezone")
      .value(expectedTimezoneValue)
      .validate(ScimUser::getTimezone, expectedTimezoneValue)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      .path("title")
      .value(expectedTitleValue)
      .validate(ScimUser::getTitle, expectedTitleValue)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      .path("userName")
      .value(expectedUsernameValue)
      .validate(ScimUser::getUserName, expectedUsernameValue)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      .path("userType")
      .value(expectedUserTypeValue)
      .validate(ScimUser::getUserType, expectedUserTypeValue)
      .build()));

    // REMOVE
    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REMOVE)
      .path("displayName")
      .setter( user ->  {
        user.setDisplayName("displayName");
        return null; // return ignored.
      })
      .validate(ScimUser::getDisplayName, null)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REMOVE)
      .path("locale")
      .setter( user ->  {
        user.setLocale("locale");
        return null; // return ignored.
      })
      .validate(ScimUser::getLocale, null)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REMOVE)
      .path("nickName")
      .setter( user ->  {
        user.setNickName("nickName");
        return null; // return ignored.
      })
      .validate(ScimUser::getNickName, null)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REMOVE)
      .path("profileUrl")
      .setter( user ->  {
        user.setProfileUrl("profileUrl");
        return null; // return ignored.
      })
      .validate(ScimUser::getProfileUrl, null)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REMOVE)
      .path("preferredLanguage")
      .setter( user ->  {
        user.setPreferredLanguage("preferredLanguage");
        return null; // return ignored.
      })
      .validate(ScimUser::getPreferredLanguage, null)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REMOVE)
      .path("timezone")
      .setter( user ->  {
        user.setTimezone("timezone");
        return null; // return ignored.
      })
      .validate(ScimUser::getTimezone, null)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REMOVE)
      .path("title")
      .setter( user ->  {
        user.setTitle("title");
        return null; // return ignored.
      })
      .validate(ScimUser::getTitle, null)
      .build()));

    /*
     * Can not remove the ScimUser userName attribute; it's read-only, as far as patch remove operations are concerned.
     */

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REMOVE)
      .path("userType")
      .value(expectedUserTypeValue)
      .setter( user ->  {
        user.setUserType("userType");
        return null; // return ignored.
      })
      .validate(ScimUser::getUserType, null)
      .build()));

    // REPLACE
    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REPLACE)
      .path("active")
      .value(false)
      .setter( user ->  {
        user.setActive(true);
        return null; // return ignored.
      })
      .validate(ScimUser::getActive, false)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REPLACE)
      .path("active")
      .value(true)
      .setter( user ->  {
        user.setActive(false);
        return null; // return ignored.
      })
      .validate(ScimUser::getActive, true)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REPLACE)
      .path("displayName")
      .value(expectedDisplayNameValue)
      .setter( user ->  {
        user.setDisplayName("displayName");
        return null; // return ignored.
      })
      .validate(ScimUser::getDisplayName, expectedDisplayNameValue)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REPLACE)
      .path("locale")
      .value(expectedLocaleValue)
      .setter( user ->  {
        user.setLocale("locale");
        return null; // return ignored.
      })
      .validate(ScimUser::getLocale, expectedLocaleValue)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REPLACE)
      .path("nickName")
      .value(expectedNickNameValue)
      .setter( user ->  {
        user.setNickName("nickName");
        return null; // return ignored.
      })
      .validate(ScimUser::getNickName, expectedNickNameValue)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REPLACE)
      .path("profileUrl")
      .value(expectedProfileUrlValue)
      .setter( user ->  {
        user.setProfileUrl("profileUrl");
        return null; // return ignored.
      })
      .validate(ScimUser::getProfileUrl, expectedProfileUrlValue)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REPLACE)
      .path("preferredLanguage")
      .value(expectedPreferredLanguageValue)
      .setter( user ->  {
        user.setPreferredLanguage("preferredLanguage");
        return null; // return ignored.
      })
      .validate(ScimUser::getPreferredLanguage, expectedPreferredLanguageValue)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REPLACE)
      .path("timezone")
      .value(expectedTimezoneValue)
      .setter( user ->  {
        user.setTimezone("timezone");
        return null; // return ignored.
      })
      .validate(ScimUser::getTimezone, expectedTimezoneValue)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REPLACE)
      .path("title")
      .value(expectedTitleValue)
      .setter( user ->  {
        user.setTitle("title");
        return null; // return ignored.
      })
      .validate(ScimUser::getTitle, expectedTitleValue)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REPLACE)
      .path("userName")
      .value(expectedUsernameValue)
      .setter( user ->  {
        user.setUserName("userName");
        return null; // return ignored.
      })
      .validate(ScimUser::getUserName, expectedUsernameValue)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REPLACE)
      .path("userType")
      .setter( user ->  {
        user.setUserType("userType");
        return null; // return ignored.
      })
      .value(expectedUserTypeValue)
      .validate(ScimUser::getUserType, expectedUserTypeValue)
      .build()));

    return values.stream();
  }
}
