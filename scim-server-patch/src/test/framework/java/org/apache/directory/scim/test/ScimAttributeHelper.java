package org.apache.directory.scim.test;

import java.util.function.BiConsumer;

import org.apache.directory.scim.spec.resources.ScimUser;

public class ScimAttributeHelper {
  public static final BiConsumer<ScimUser, String> setDisplayName = ScimUser::setDisplayName;
  public static final BiConsumer<ScimUser, String> setLocale = ScimUser::setLocale;
  public static final BiConsumer<ScimUser, String> setNickName = ScimUser::setNickName;
  public static final BiConsumer<ScimUser, String> setPreferredLanguage = ScimUser::setPreferredLanguage;
  public static final BiConsumer<ScimUser, String> setProfileUrl = ScimUser::setProfileUrl;
}
