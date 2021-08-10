package org.apache.directory.scim.test.arguments.provider;

import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.ADD;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.REMOVE;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.REPLACE;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.spec.schema.ResourceReference;
import org.apache.directory.scim.test.ScimTestHelper;
import org.apache.directory.scim.test.arguments.provider.args.PatchArgs;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import com.google.common.collect.ImmutableList;

public class GroupMembershipArgumentsProvider implements ArgumentsProvider {
  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
    List<Arguments> values = new ArrayList<>();

    final ScimUser user = ScimTestHelper.generateScimUser();
    final ScimUser newUserMember = ScimTestHelper.generateScimUser();

    final ResourceReference expectedResourceReference = ScimTestHelper.member(ResourceReference.ReferenceType.DIRECT, newUserMember);

    final List<ResourceReference> members = new ArrayList<>();
    final List<ResourceReference> replaceMembers = new ArrayList<>();
    final int memberCount = 10;
    for(int i = 0; i < memberCount; i++) {
      members.add(ScimTestHelper.member(ResourceReference.ReferenceType.DIRECT, ScimTestHelper.generateScimUser()));
      replaceMembers.add(ScimTestHelper.member(ResourceReference.ReferenceType.INDIRECT, ScimTestHelper.generateScimUser()));
    }

    // ADD
    values.add(Arguments.of(new PatchArgs.Builder<ScimGroup>()
      .type(ADD)
      .path("members")
      .value(ImmutableList.of(ScimTestHelper.member(ResourceReference.ReferenceType.DIRECT, user)))
      .validate(group -> group.getMembers() != null, true)
      .validate(group -> group.getMembers().size(), 1)
      .build()));
    values.add(Arguments.of(new PatchArgs.Builder<ScimGroup>()
      .type(ADD)
      .path("members")
      .value(members)
      .validate(group -> group.getMembers() != null, true)
      .validate(group -> group.getMembers().size(), memberCount)
      .validate(group -> group.getMembers().equals(members), true)
      .build()));

    // REMOVE
    values.add(Arguments.of(new PatchArgs.Builder<ScimGroup>()
      .type(REMOVE)
      .path("members")
      .setter( group -> {
        group.setMembers(members);
        return null;
      })
      .validate(group -> group.getMembers() == null, true)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimGroup>()
      .type(REMOVE)
      .path(String.format("members[value EQ \"%s\"]", expectedResourceReference.getValue()))
      .setter( group -> {
        List<ResourceReference> initialMemberList = new ArrayList<>();
        initialMemberList.addAll(members);
        initialMemberList.addAll(replaceMembers);
        initialMemberList.add(expectedResourceReference);
        group.setMembers(initialMemberList);
        return null;
      })
      .validate(group -> group.getMembers() != null, true)
      .validate(group -> group.getMembers().size(), (members.size() + replaceMembers.size()))
      .build()));

    // REPLACE
    values.add(Arguments.of(new PatchArgs.Builder<ScimGroup>()
      .type(REPLACE)
      .path("members")
      .value(ImmutableList.of(expectedResourceReference))
      .setter( group -> {
        group.setMembers(new ArrayList<>());
        group.getMembers().add(ScimTestHelper.member(ResourceReference.ReferenceType.DIRECT, user));
        return null;
      })
      .validate(group -> group.getMembers() != null, true)
      .validate(group -> group.getMembers().size(), 1)
      .validate(group -> group.getMembers().get(0), expectedResourceReference)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimGroup>()
      .type(REPLACE)
      .path("members")
      .value(replaceMembers)
      .setter( group -> {
        group.setMembers(members);
        return null;
      })
      .validate(group -> group.getMembers() != null, true)
      .validate(group -> group.getMembers().size(), replaceMembers.size())
      .validate(group -> group.getMembers().equals(replaceMembers), true)
      .build()));

    return values.stream();
  }
}
