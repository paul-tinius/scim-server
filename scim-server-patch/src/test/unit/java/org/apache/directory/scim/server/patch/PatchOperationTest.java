package org.apache.directory.scim.server.patch;

import static org.apache.directory.scim.test.ScimTestHelper.createRegistry;

import java.util.List;

import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.resources.PhoneNumber;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.test.arguments.provider.args.PatchArgs;
import org.apache.directory.scim.test.builder.PatchOperationBuilder;
import org.junit.jupiter.api.BeforeAll;

import com.google.common.collect.ImmutableList;

abstract class PatchOperationTest {
  protected static PatchOperations patchOperations;

  public PatchOperationTest() {
    PhoneNumber.setStrict(false);
  }

  @BeforeAll
  static void setUp() throws Exception {
    patchOperations = new PatchOperations(createRegistry());
  }

  // test helper methods
  protected void applyPatch(final ScimUser user,  final PatchArgs<ScimUser> args) throws Exception {
    List<PatchOperation> operations = ImmutableList.of(
      PatchOperationBuilder.builder()
        .operation(args.type())
        .path(args.path())
        .value(args.value())
        .build()
    );

    args.setter(user);
    args.validate(patchOperations.apply(user, operations));
  }

  protected void applyPatch(final ScimGroup group,final PatchArgs<ScimGroup> args) throws Exception {
    List<PatchOperation> operations = ImmutableList.of(
      PatchOperationBuilder.builder()
        .operation(args.type())
        .path(args.path())
        .value(args.value())
        .build()
    );

    args.setter(group);
    args.validate(patchOperations.apply(group, operations));
  }
}
