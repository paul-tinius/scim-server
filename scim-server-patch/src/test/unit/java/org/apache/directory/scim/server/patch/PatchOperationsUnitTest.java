package org.apache.directory.scim.server.patch;

import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.REMOVE;
import static org.apache.directory.scim.test.ScimTestHelper.assertScimException;
import static org.apache.directory.scim.test.ScimTestHelper.faker;
import static org.apache.directory.scim.test.ScimTestHelper.multiValuedPrimaryUniquenessCount;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;
import javax.ws.rs.core.Response;

import org.apache.directory.scim.spec.protocol.ErrorMessageType;
import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.resources.ScimGroup;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.test.ScimTestHelper;
import org.apache.directory.scim.test.arguments.provider.ExtensionArgumentProvider;
import org.apache.directory.scim.test.arguments.provider.GroupMembershipArgumentsProvider;
import org.apache.directory.scim.test.arguments.provider.OmittedTargetLocationArgumentsProvider;
import org.apache.directory.scim.test.arguments.provider.TargetLocationAlreadyExistsAndValuesMatch;
import org.apache.directory.scim.test.arguments.provider.TargetLocationDoesNotExistArgumentsProvider;
import org.apache.directory.scim.test.arguments.provider.TargetLocationSpecifiedSingularValuedAttribute;
import org.apache.directory.scim.test.arguments.provider.TargetLocationSpecifiesComplexAttribute;
import org.apache.directory.scim.test.arguments.provider.TargetLocationSpecifiesMultiValuedComplexAttribute;
import org.apache.directory.scim.test.arguments.provider.TargetLocationSpecifiesMultiValuedNoFilter;
import org.apache.directory.scim.test.arguments.provider.args.PatchArgs;
import org.apache.directory.scim.test.builder.PatchOperationBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;

import com.google.common.collect.ImmutableList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PatchOperationsUnitTest extends PatchOperationTest {

  ScimUser user = ScimTestHelper.generateScimUser();
  ScimGroup group = ScimTestHelper.generateScimGroup();

  public PatchOperationsUnitTest() {
    super();
  }

  /*
   * 3.5.2.1.  Add Operation
   *
   *    o  If omitted, the target location is assumed to be the resource
   *       itself.  The "value" parameter contains a set of attributes to be
   *       added to the resource.
   *
   * 3.5.2.3.  Replace Operation
   *
   *    o  If the "path" parameter is omitted, the target is assumed to be
   *       the resource itself.  In this case, the "value" attribute SHALL
   *       contain a list of one or more attributes that are to be replaced.
   */
  @ParameterizedTest()
  @ArgumentsSource(OmittedTargetLocationArgumentsProvider.class)
  void apply_omittedTargetLocation_successfullyPatched(final PatchArgs<ScimUser> args) throws Exception {
    applyPatch(user, args);
  }

  /*
   * 3.5.2.1.  Add Operation
   *
   *    o  If the target location does not exist, the attribute and value are
   *       added.
   *
   *    o  If the target location specifies an attribute that does not exist
   *       (has no value), the attribute is added with the new value.
   *
   * 3.5.2.3.  Replace Operation
   *
   *    o  If the target location path specifies an attribute that does not
   *       exist, the service provider SHALL treat the operation as an "add".
   */
  @ParameterizedTest()
  @ArgumentsSource(TargetLocationDoesNotExistArgumentsProvider.class)
  void apply_targetLocationDoesNotExists_successfullyPatched(final PatchArgs<ScimUser> args) throws Exception {
    applyPatch(user, args);
  }

  /*
   * 3.5.2.1.  Add Operation
   *
   *    o  If the target location specifies a single-valued attribute, the
   *       existing value is replaced.
   *
   *    o  If the target location exists, the value is replaced.
   *
   * 3.5.2.2.  Remove Operation
   *
   *    o  If the target location is a single-value attribute, the attribute
   *       and its associated value is removed, and the attribute SHALL be
   *       considered unassigned.
   *
   * 3.5.2.3.  Replace Operation
   *
   *    o  If the target location is a single-value attribute, the attributes
   *       value is replaced.
   */
  @ParameterizedTest()
  @ArgumentsSource(TargetLocationSpecifiedSingularValuedAttribute.class)
  void apply_targetLocationSpecifiesSingleValuedAttribute_expectedResult(final PatchArgs<ScimUser> args) throws Exception {
    applyPatch(user, args);
  }

  /*
   * 3.5.2.1. Add Operation
   *
   *    o  If the target location specifies a complex attribute, a set of
   *       sub-attributes SHALL be specified in the "value" parameter.
   *
   * 3.5.2.3. Replace Operation
   *
   *    o  If the target location specifies a complex attribute, a set of
   *       sub-attributes SHALL be specified in the "value" parameter, which
   *       replaces any existing values or adds where an attribute did not
   *       previously exist.  Sub-attributes that are not specified in the
   *       "value" parameter are left unchanged.
   */
  @ParameterizedTest()
  @ArgumentsSource(TargetLocationSpecifiesComplexAttribute.class)
  void apply_targetLocationSpecifiesComplexAttribute_successfullyPatched(final PatchArgs<ScimUser> args) throws Exception {
    applyPatch(user, args);
  }

  /*
   * 3.5.2.1.  Add Operation
   *
   *    o  If the target location specifies a multi-valued attribute, a new
   *       value is added to the attribute.
   *
   * 3.5.2.2.  Remove Operation
   *
   *    o  If the target location is a complex multi-valued attribute and a
   *       complex filter is specified based on the attribute's
   *       sub-attributes, the matching records are removed.  Sub-attributes
   *       whose values have been removed SHALL be considered unassigned.  If
   *       the complex multi-valued attribute has no remaining records, the
   *       attribute SHALL be considered unassigned.
   *
   *    o  If the target location is a multi-valued attribute and a complex
   *       filter is specified comparing a "value", the values matched by the
   *       filter are removed.  If no other values remain after removal of
   *       the selected values, the multi-valued attribute SHALL be
   *       considered unassigned.
   *
   * 3.5.2.3.  Replace Operation
   *
   *    o  If the target location is a complex multi-valued attribute with a
   *       value selection filter ("valuePath") and a specific sub-attribute
   *       (e.g., "addresses[type eq "work"].streetAddress"), the matching
   *       sub-attribute of all matching records is replaced.
   *
   *    o  If the target location and a value selection ("valuePath") filter is specified that matches one or
   *       more values of the multi-valued attribute, then all matching record values SHALL be replaced.
   */
  @ParameterizedTest()
  @ArgumentsSource(TargetLocationSpecifiesMultiValuedComplexAttribute.class)
  void apply_targetLocationSpecifiesMultiValuedComplexAttribute_successfullyPatched(final PatchArgs<ScimUser> args) throws Exception {
    applyPatch(user, args);
  }

  /*
   * 3.5.2.2.  Remove Operation
   *
   *    o  If the target location is a multi-valued attribute and no filter
   *       is specified, the attribute and all values are removed, and the
   *       attribute SHALL be considered unassigned.
   *
   * 3.5.2.3.  Replace Operation
   *
   *    o  If the target location is a multi-valued attribute and no filter
   *       is specified, the attribute and all values are replaced.
   */
  @ParameterizedTest()
  @ArgumentsSource(TargetLocationSpecifiesMultiValuedNoFilter.class)
  void apply_targetLocationSpecifiesMultiValuedNoFilter_successfullyPatched(final PatchArgs<ScimUser> args) throws Exception {
    applyPatch(user, args);
  }

  /*
   * 3.5.2.  Modifying with PATCH
   *
   *  For multi-valued attributes, a PATCH operation that sets a value's "primary" sub-attribute to "true" SHALL cause
   *  the server to automatically set "primary" to "false" for any other values in the array.
   */
  @ParameterizedTest()
  @EnumSource(value = PatchOperation.Type.class, names = {"ADD", "REPLACE"})
  void apply_multipleMultiValuedPrimaryEqualToTrue_onlyOnePrimaryEqualToTrue(PatchOperation.Type type) throws Exception {
    ScimTestHelper.address(user, faker().address(), "home", false);
    ScimTestHelper.address(user, faker().address(), "work", true);

    ScimTestHelper.email(user,"home", true);
    ScimTestHelper.email(user,"work", false);

    final List<PatchOperation> operations = ImmutableList.of(
      PatchOperationBuilder.builder()
        .operation(type)
        .path("addresses[type EQ \"home\"].primary")
        .value(true)
        .build(),
      PatchOperationBuilder.builder()
        .operation(type)
        .path("emails[type EQ \"work\"].primary")
        .value(true)
        .build()
    );

    final ScimUser result = patchOperations.apply(user, operations);

    assertThat(result).isNotNull();
    assertThat(result.getAddresses()).hasSize(2);

    assertThat(multiValuedPrimaryUniquenessCount(result.getAddresses())).isEqualTo(1);
    assertThat(multiValuedPrimaryUniquenessCount(result.getEmails())).isEqualTo(1);
  }

  /*
   * 3.5.2.1.  Add Operation
   *
   *    o  If the target location already contains the value specified, no
   *       changes SHOULD be made to the resource, and a success response
   *       SHOULD be returned.  Unless other operations change the resource,
   *       this operation SHALL NOT change the modify timestamp of the
   *       resource.
   */

  @ParameterizedTest()
  @ArgumentsSource(TargetLocationAlreadyExistsAndValuesMatch.class)
  void apply_targetAlreadyExistsAndValueSame_noModificationAreMade(final PatchArgs<ScimUser> args) throws Exception {
    applyPatch(user, args);
  }

  @ParameterizedTest()
  @ArgumentsSource(GroupMembershipArgumentsProvider.class)
  void apply_groupMembership_successfullyPatched(final PatchArgs<ScimGroup> args) throws Exception {
    applyPatch(group, args);
  }

  @ParameterizedTest()
  @ArgumentsSource(value = ExtensionArgumentProvider.class)
  void apply_extension_successfullyPatched(final PatchArgs<ScimUser> args) throws Exception {
    applyPatch(user, args);
  }

  /*
   * 3.5.2.3.  Replace Operation
   *
   *  If an attribute is removed or becomes unassigned and is defined as a
   *  required attribute or a read-only attribute, the server SHALL return
   *  an HTTP response status code and a JSON detail error response as
   *  defined in Section 3.12, with a "scimType" error code of
   *  "mutability".
   */

  @ParameterizedTest()
  @EnumSource(value = PatchOperation.Type.class, names = {"REMOVE"})
  void apply_mutability_throwsScimException(final PatchOperation.Type operation) throws Exception {
    PatchOperationBuilder.Builder builder = PatchOperationBuilder.builder()
      .operation(operation)
      .path("username");

    PatchOperation patchOperation = builder.build();

    Throwable t = catchThrowable(() -> patchOperations.apply(user,
      ImmutableList.of(patchOperation)));

    assertScimException(t,
      Response.Status.BAD_REQUEST,
      ErrorMessageType.MUTABILITY,
      ErrorMessageType.MUTABILITY.getDetail());
  }

  @ParameterizedTest()
  @EnumSource(PatchOperation.Type.class)
  void apply_invalidScimResource_throwsScimException(final PatchOperation.Type operation) throws Exception {
    PatchOperationBuilder.Builder builder = PatchOperationBuilder.builder()
      .operation(operation)
      .path("nonExistentPath");

    if (!operation.equals(REMOVE)) {
      builder.value("dummyValue");
    }

    PatchOperation patchOperation = builder.build();

    Throwable t = catchThrowable(() -> patchOperations.apply(null,
      ImmutableList.of(patchOperation)));

    assertScimException(t,
      Response.Status.BAD_REQUEST,
      ErrorMessageType.NO_TARGET,
      ErrorMessageType.NO_TARGET.getDetail());
  }

  @Test
  void apply_invalidSyntax_throwsScimException() {
    Throwable t = catchThrowable(() -> patchOperations.apply(user, null));

    assertScimException(t,
      Response.Status.BAD_REQUEST,
      ErrorMessageType.INVALID_SYNTAX,
      ErrorMessageType.INVALID_SYNTAX.getDetail());
  }

  @ParameterizedTest()
  @EnumSource(PatchOperation.Type.class)
  void apply_invalidPath_throwsScimException(final PatchOperation.Type operation) throws Exception {
    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(operation)
      .path("garbage")
      .build();

    Throwable t = catchThrowable(() -> patchOperations.apply(user,
      ImmutableList.of(patchOperation)));

    assertScimException(t,
      Response.Status.BAD_REQUEST,
      ErrorMessageType.INVALID_PATH,
      ErrorMessageType.INVALID_PATH.getDetail());
  }

  /*
   * 3.5.2.2.  Remove Operation
   *
   *   o  If "path" is unspecified, the operation fails with HTTP status
   *       code 400 and a "scimType" error code of "noTarget".
   */
  @ParameterizedTest()
  @EnumSource(value = PatchOperation.Type.class, names = {"REMOVE"})
  void apply_noTargetNotSpecified_throwsScimException(final PatchOperation.Type operation) {
    PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(operation)
      .build();

    Throwable t = catchThrowable(() -> patchOperations.apply(user,
      ImmutableList.of(patchOperation)));

    assertScimException(t,
      Response.Status.BAD_REQUEST,
      ErrorMessageType.NO_TARGET,
      ErrorMessageType.NO_TARGET.getDetail());
  }

  /*
   * 3.5.2.3.  Replace Operation
   *
   *    o  If the target location is a multi-valued attribute for which a
   *       value selection filter ("valuePath") has been supplied and no
   *       record match was made, the service provider SHALL indicate failure
   *       by returning HTTP status code 400 and a "scimType" error code of
   *       "noTarget".
   */
  @ParameterizedTest()
  @EnumSource(value = PatchOperation.Type.class, names = {"REPLACE"})
  void apply_noTargetNoFilterMatch_throwsScimException(final PatchOperation.Type operation) throws Exception {
        PatchOperation patchOperation = PatchOperationBuilder.builder()
      .operation(operation)
      .path("addresses[type EQ \"work\"].locality")
      .value("Rochester")
      .build();

    Throwable t = catchThrowable(() -> patchOperations.apply(user,
      ImmutableList.of(patchOperation)));

    assertScimException(t,
      Response.Status.BAD_REQUEST,
      ErrorMessageType.NO_TARGET,
      ErrorMessageType.NO_TARGET.getDetail());
  }
}
