package org.apache.directory.scim.test.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.schema.Meta;
import org.apache.directory.scim.spec.schema.ResourceReference;

public class ScimGroupBuilder {
  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Builder data) {
    return new Builder(data);
  }

  public static Builder builder(ScimGroup scimGroup) {
    return new Builder(scimGroup);
  }

  public static final class Builder {
    private String id = null;
    private String displayName;
    private String externalId;
    private List<ResourceReference> members;
    private Meta meta;

    private Builder() {
    }

    private Builder(Builder initialData) {
      this.id = initialData.id;
      this.displayName = initialData.displayName;
      this.externalId = initialData.externalId;
      this.members = initialData.members;
      this.meta = initialData.meta;
    }

    private Builder(ScimGroup scimGroup) {
      this.id = scimGroup.getId();
      this.displayName = scimGroup.getDisplayName();
      this.externalId = scimGroup.getExternalId();
      this.members = scimGroup.getMembers();
      this.meta = scimGroup.getMeta();
    }

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder displayName(String displayName) {
      this.displayName = displayName;
      return this;
    }

    public Builder externalId(String externalId) {
      this.externalId = externalId;
      return this;
    }

    public Builder members(List<ResourceReference> members) {
      this.members = members;
      return this;
    }

    public Builder member(ResourceReference member) {
      if (this.members==null) {
        this.members = new ArrayList<>();
      }

      this.members.add(member);
      return this;
    }

    public Builder meta(Meta meta) {
      this.meta = meta;
      return this;
    }

    public ScimGroup build() {
      final ScimGroup scimGroup = new ScimGroup();
      scimGroup.setDisplayName(this.displayName);
      scimGroup.setExternalId(this.externalId);
      scimGroup.setMembers(this.members);

      return scimGroup;
    }
  }
}
