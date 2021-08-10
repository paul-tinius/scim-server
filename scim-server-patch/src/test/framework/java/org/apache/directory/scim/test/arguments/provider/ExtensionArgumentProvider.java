package org.apache.directory.scim.test.arguments.provider;

import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.ADD;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.REMOVE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import org.apache.directory.scim.spec.extension.EnterpriseExtension;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.test.ScimTestHelper;
import org.apache.directory.scim.test.arguments.provider.args.PatchArgs;
import org.apache.directory.scim.test.builder.EnterpriseExtensionBuilder;
import org.apache.directory.scim.test.builder.EnterpriseExtensionManagerBuilder;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class ExtensionArgumentProvider implements ArgumentsProvider {

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
    List<Arguments> values = new ArrayList<>();

    // ADD
    /* target doesn't exists */
    String newValue = ScimTestHelper.faker().letterify("?? Cost Center ??");
    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      .path("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:costCenter")
      .value(newValue)
      .validate(user -> user.getExtensions() != null, true)
      .validate(user -> user.getExtension(EnterpriseExtension.URN) != null, true)
      .validate(user -> ((EnterpriseExtension) user.getExtension(EnterpriseExtension.URN)).getCostCenter(), newValue)
      .validate(user -> user.getSchemas().contains(EnterpriseExtension.URN), true)
      .build()));

    newValue = ScimTestHelper.faker().letterify("?? Department ??");
    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      .path("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:department")
      .value(newValue)
      .validate(user -> user.getExtensions() != null, true)
      .validate(user -> user.getExtension(EnterpriseExtension.URN) != null, true)
      .validate(user -> ((EnterpriseExtension) user.getExtension(EnterpriseExtension.URN)).getDepartment(), newValue)
      .build()));

    newValue = ScimTestHelper.faker().letterify("?? Division ??");
    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      .path("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:division")
      .value(newValue)
      .validate(user -> user.getExtensions() != null, true)
      .validate(user -> user.getExtension(EnterpriseExtension.URN) != null, true)
      .validate(user -> ((EnterpriseExtension) user.getExtension(EnterpriseExtension.URN)).getDivision(), newValue)
      .build()));

    newValue = ScimTestHelper.faker().letterify("?? Employee Number ??");
    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      .path("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:employeeNumber")
      .value(newValue)
      .validate(user -> user.getExtensions() != null, true)
      .validate(user -> user.getExtension(EnterpriseExtension.URN) != null, true)
      .validate(user -> ((EnterpriseExtension) user.getExtension(EnterpriseExtension.URN)).getEmployeeNumber(), newValue)
      .build()));

    newValue = ScimTestHelper.faker().letterify("?? Organization ??");
    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      .path("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:organization")
      .value(newValue)
      .validate(user -> user.getExtensions() != null, true)
      .validate(user -> user.getExtension(EnterpriseExtension.URN) != null, true)
      .validate(user -> ((EnterpriseExtension) user.getExtension(EnterpriseExtension.URN)).getOrganization(), newValue)
      .build()));

    newValue = ScimTestHelper.faker().letterify("?? Display Name ??");
    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      .path("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:manager.displayName")
      .value(newValue)
      .validate(user -> user.getExtensions() != null, true)
      .validate(user -> user.getExtension(EnterpriseExtension.URN) != null, true)
      .validate(user -> user.getExtension(EnterpriseExtension.URN) != null, true)
      .validate(user -> ((EnterpriseExtension) user.getExtension(EnterpriseExtension.URN)).getManager() != null, true)
      .validate(user -> ((EnterpriseExtension) user.getExtension(EnterpriseExtension.URN)).getManager().getDisplayName() != null, true)
      .build()));

    newValue = ScimTestHelper.faker().letterify("?? Value ??");
    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      .path("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:manager.value")
      .value(newValue)
      .validate(user -> user.getExtensions() != null, true)
      .validate(user -> user.getExtension(EnterpriseExtension.URN) != null, true)
      .validate(user -> ((EnterpriseExtension) user.getExtension(EnterpriseExtension.URN)).getManager() != null, true)
      .validate(user -> ((EnterpriseExtension) user.getExtension(EnterpriseExtension.URN)).getManager().getValue() != null, true)
      .build()));

    final EnterpriseExtension enterpriseExtension = EnterpriseExtensionBuilder.builder()
      .costCenter(ScimTestHelper.faker().letterify("?? Cost Center ??"))
      .department(ScimTestHelper.faker().letterify("?? Department ??"))
      .division(ScimTestHelper.faker().letterify("?? Division ??"))
      .employeeNumber(ScimTestHelper.faker().letterify("?? Employee Number ??"))
      .manager(EnterpriseExtensionManagerBuilder.builder()
        .displayName(ScimTestHelper.faker().letterify("?? Manager Display Name ??"))
        .value(ScimTestHelper.faker().internet().uuid())
        .build())
      .organization(ScimTestHelper.faker().letterify("?? Organization ??"))
      .build();

    // REMOVE
    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REMOVE)
      .path("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:costCenter")
      .setter(user -> {
        user.setExtensions(new HashMap<>());
        user.getExtensions().put(EnterpriseExtension.URN, enterpriseExtension);
        return null;
      })
      .validate(user -> user.getExtensions() != null, true)
      .validate(user -> user.getExtension(EnterpriseExtension.URN) != null, true)
      .validate(user -> ((EnterpriseExtension) user.getExtension(EnterpriseExtension.URN)).getCostCenter() == null, true)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REMOVE)
      .path("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:department")
      .setter(user -> {
        user.setExtensions(new HashMap<>());
        user.getExtensions().put(EnterpriseExtension.URN, enterpriseExtension);
        return null;
      })
      .validate(user -> user.getExtensions() != null, true)
      .validate(user -> user.getExtension(EnterpriseExtension.URN) != null, true)
      .validate(user -> ((EnterpriseExtension) user.getExtension(EnterpriseExtension.URN)).getDepartment() == null, true)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REMOVE)
      .path("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:division")
      .setter(user -> {
        user.setExtensions(new HashMap<>());
        user.getExtensions().put(EnterpriseExtension.URN, enterpriseExtension);
        return null;
      })
      .validate(user -> user.getExtensions() != null, true)
      .validate(user -> user.getExtension(EnterpriseExtension.URN) != null, true)
      .validate(user -> ((EnterpriseExtension) user.getExtension(EnterpriseExtension.URN)).getDivision() == null, true)
      .build()));

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REMOVE)
      .path("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:employeeNumber")
      .setter(user -> {
        user.setExtensions(new HashMap<>());
        user.getExtensions().put(EnterpriseExtension.URN, enterpriseExtension);
        return null;
      })
      .validate(user -> user.getExtensions() != null, true)
      .validate(user -> user.getExtension(EnterpriseExtension.URN) != null, true)
      .validate(user -> ((EnterpriseExtension) user.getExtension(EnterpriseExtension.URN)).getEmployeeNumber() == null, true)
      .build()));

    /*
     * Can not remove Enterprise Extension Manager (displayName) attribute, its a read-only as far as patch remove operations are concerned.
     */

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REMOVE)
      .path("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User:organization")
      .setter(user -> {
        user.setExtensions(new HashMap<>());
        user.getExtensions().put(EnterpriseExtension.URN, enterpriseExtension);
        return null;
      })
      .validate(user -> user.getExtensions() != null, true)
      .validate(user -> user.getExtension(EnterpriseExtension.URN) != null, true)
      .validate(user -> ((EnterpriseExtension) user.getExtension(EnterpriseExtension.URN)).getOrganization() == null, true)
      .build()));

    // REPLACE Target Location already exists

    return values.stream();
  }
}
