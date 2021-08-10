package org.apache.directory.scim.test.builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.directory.scim.spec.resources.Address;
import org.apache.directory.scim.spec.resources.Email;
import org.apache.directory.scim.spec.resources.Entitlement;
import org.apache.directory.scim.spec.resources.Im;
import org.apache.directory.scim.spec.resources.Name;
import org.apache.directory.scim.spec.resources.PhoneNumber;
import org.apache.directory.scim.spec.resources.Photo;
import org.apache.directory.scim.spec.resources.Role;
import org.apache.directory.scim.spec.resources.ScimExtension;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.resources.X509Certificate;
import org.apache.directory.scim.spec.schema.Meta;
import org.apache.directory.scim.spec.schema.ResourceReference;

public class ScimUserBuilder {
  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Builder data) {
    return new Builder(data);
  }

  public static Builder builder(ScimUser scimUser) {
    return new Builder(scimUser);
  }

  public static final class Builder {
    private String id = null;
    private String externalId = null;
    private Boolean active;
    private List<Address> addresses = null;
    private String displayName = null;
    private List<Email> emails = null;
    private List<Entitlement> entitlements = null;
    private Map<String, ScimExtension> extensions = null;
    private List<ResourceReference> groups = null;
    private List<Im> ims = null;
    private String locale = null;
    private Name name = null;
    private String nickName = null;
    private List<PhoneNumber> phoneNumbers = null;
    private List<Photo> photos = null;
    private String profileUrl = null;
    private String preferredLanguage;
    private List<Role> roles = null;
    private String timezone = null;
    private String title = null;
    private String userName = null;
    private String userType = ScimUser.RESOURCE_NAME;
    private List<X509Certificate> x509Certificates = null;

    private Meta meta;

    private Builder() {
    }

    private Builder(Builder initialData) {
      this.id = initialData.id;
      this.externalId = initialData.externalId;
      this.active = initialData.active;
      this.addresses = initialData.addresses;
      this.displayName = initialData.displayName;
      this.emails = initialData.emails;
      this.entitlements = initialData.entitlements;
      this.extensions = initialData.extensions;
      this.groups = initialData.groups;
      this.ims = initialData.ims;
      this.locale = initialData.locale;
      this.name = initialData.name;
      this.nickName = initialData.nickName;
      this.phoneNumbers = initialData.phoneNumbers;
      this.photos = initialData.photos;
      this.profileUrl = initialData.profileUrl;
      this.preferredLanguage = initialData.preferredLanguage;
      this.roles = initialData.roles;
      this.timezone = initialData.timezone;
      this.title = initialData.title;
      this.userName = initialData.userName;
      this.userType = initialData.userType;
      this.x509Certificates = initialData.x509Certificates;
      this.meta = initialData.meta;
    }

    private Builder(ScimUser scimUser) {
      this.id = scimUser.getId();
      this.externalId = scimUser.getExternalId();
      this.active = scimUser.getActive();
      this.addresses = scimUser.getAddresses();
      this.displayName = scimUser.getDisplayName();
      this.emails = scimUser.getEmails();
      this.entitlements = scimUser.getEntitlements();
      this.extensions = scimUser.getExtensions();
      this.groups = scimUser.getGroups();
      this.ims = scimUser.getIms();
      this.locale = scimUser.getLocale();
      this.name = scimUser.getName();
      this.nickName = scimUser.getNickName();
      this.phoneNumbers = scimUser.getPhoneNumbers();
      this.photos = scimUser.getPhotos();
      this.profileUrl = scimUser.getProfileUrl();
      this.preferredLanguage = scimUser.getPreferredLanguage();
      this.roles = scimUser.getRoles();
      this.timezone = scimUser.getTimezone();
      this.title = scimUser.getTitle();
      this.userName = scimUser.getUserName();
      this.userType = scimUser.getUserType();
      this.x509Certificates = scimUser.getX509Certificates();
      this.meta = scimUser.getMeta();
    }

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder externalId(String externalId) {
      this.externalId = externalId;
      return this;
    }

    public Builder active(Boolean active) {
      this.active = active;
      return this;
    }

    public Builder addresses(List<Address> addresses) {
      this.addresses = addresses;
      return this;
    }

    public Builder displayName(String displayName) {
      this.displayName = displayName;
      return this;
    }

    public Builder emails(List<Email> emails) {
      this.emails = emails;
      return this;
    }

    public Builder entitlements(List<Entitlement> entitlements) {
      this.entitlements = entitlements;
      return this;
    }

    public Builder groups(List<ResourceReference> groups) {
      this.groups = groups;
      return this;
    }

    public Builder ims(List<Im> ims) {
      this.ims = ims;
      return this;
    }

    public Builder locale(String locale) {
      this.locale = locale;
      return this;
    }

    public Builder name(Name name) {
      this.name = name;
      return this;
    }

    public Builder nickName(String nickName) {
      this.nickName = nickName;
      return this;
    }

    public Builder phoneNumbers(List<PhoneNumber> phoneNumbers) {
      this.phoneNumbers = phoneNumbers;
      return this;
    }

    public Builder photos(List<Photo> photos) {
      this.photos = photos;
      return this;
    }

    public Builder profileUrl(String profileUrl) {
      this.profileUrl = profileUrl;
      return this;
    }

    public Builder preferredLanguage(String preferredLanguage) {
      this.preferredLanguage = preferredLanguage;
      return this;
    }

    public Builder roles(List<Role> roles) {
      this.roles = roles;
      return this;
    }

    public Builder timezone(String timezone) {
      this.timezone = timezone;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder userName(String userName) {
      this.userName = userName;
      return this;
    }

    public Builder userType(String userType) {
      this.userType = userType;
      return this;
    }

    public Builder x509Certificates(List<X509Certificate> x509Certificates) {
      this.x509Certificates = x509Certificates;
      return this;
    }

    public Builder meta(Meta meta) {
      this.meta = meta;
      return this;
    }

    public Builder extensions(Map<String, ScimExtension> extensions) {
      this.extensions = extensions;
      return this;
    }

    public Builder extension(ScimExtension extension) {
      if (this.extensions==null) {
        this.extensions = new HashMap<>();
      }

      this.extensions.put(extension.getUrn(), extension);
      return this;
    }

    public ScimUser build() {
      final ScimUser scimUser = new ScimUser();

      if(this.extensions != null) {
        this.extensions.forEach((k, v) -> scimUser.getSchemas().add(k));
      }

      scimUser.setId(this.id);
      scimUser.setExternalId(this.externalId);
      scimUser.setActive(this.active);
      scimUser.setAddresses(this.addresses);
      scimUser.setDisplayName(this.displayName);
      scimUser.setEmails(this.emails);
      scimUser.setEntitlements(this.entitlements);
      scimUser.setExtensions(this.extensions);
      scimUser.setGroups(this.groups);
      scimUser.setIms(this.ims);
      scimUser.setLocale(this.locale);
      scimUser.setName(this.name);
      scimUser.setNickName(this.nickName);
      scimUser.setPhoneNumbers(this.phoneNumbers);
      scimUser.setPhotos(this.photos);
      scimUser.setProfileUrl(this.profileUrl);
      scimUser.setPreferredLanguage(this.preferredLanguage);
      scimUser.setRoles(this.roles);
      scimUser.setTimezone(this.timezone);
      scimUser.setTitle(this.title);
      scimUser.setUserName(this.userName);
      scimUser.setUserType(this.userType);
      scimUser.setX509Certificates(this.x509Certificates);

      scimUser.setMeta(this.meta);

      return scimUser;
    }
  }
}
