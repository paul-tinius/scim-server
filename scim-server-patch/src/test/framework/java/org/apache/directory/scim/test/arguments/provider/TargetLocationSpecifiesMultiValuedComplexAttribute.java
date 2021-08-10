package org.apache.directory.scim.test.arguments.provider;

import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.ADD;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.REMOVE;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.REPLACE;
import static org.apache.directory.scim.test.ScimTestHelper.faker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.directory.scim.spec.resources.Address;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.test.ScimTestHelper;
import org.apache.directory.scim.test.arguments.provider.args.PatchArgs;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TargetLocationSpecifiesMultiValuedComplexAttribute implements ArgumentsProvider {

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
    List<Arguments> values = new ArrayList<>();

    // ADD
    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(ADD)
      .path("addresses[type EQ \"work\"].primary")
      .value(true)
      .validate(user -> user.getAddresses().size(), 1)
      .validate(user -> user.getAddresses().get(0).getType(), "work")
      .validate(user -> user.getAddresses().get(0).getPrimary(), true)
      .build()));

    // REMOVE
    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REMOVE)
      .path("addresses[type EQ \"work\"]")
      .setter(user -> {
        user.setAddresses(new ArrayList<>());
        user.getAddresses().add((ScimTestHelper.address(ScimTestHelper.faker().address(), "work", true)));
        return null; // return is ignored
      })
      .validate(ScimUser::getAddresses, null)
      .build()));

    // REPLACE
    final Address address = ScimTestHelper.address(ScimTestHelper.faker().address(), "work", true);
    // generate fake data for replace patch operation
    final com.github.javafaker.Address fakeAddress = faker().address();
    final String streetAddress = fakeAddress.streetAddress();
    final String country = fakeAddress.countryCode();
    final String region = fakeAddress.stateAbbr();
    final String locality = fakeAddress.city();
    final String postalCode = fakeAddress.zipCode();

    final Map<String,Object> addressMap = new HashMap<>();
    addressMap.put("streetAddress", streetAddress);
    addressMap.put("country", country);
    addressMap.put("region", region);
    addressMap.put("locality", locality);
    addressMap.put("postalCode", postalCode);
    addressMap.put("type", "work");
    addressMap.put("primary", true);

    final List<Map<String,Object>> addressesList = new ArrayList<>();
    addressesList.add(addressMap);

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REPLACE)
      .path("addresses[type EQ \"work\"]")
      .value(addressesList)
      .setter(user -> {
        user.setAddresses(new ArrayList<>());
        user.getAddresses().add(address);
        return null; // return is ignored
      })
      .validate(user -> user.getAddresses() != null, true)
      .validate(user -> user.getAddresses().size(), 1)
      .validate(user -> user.getAddresses().get(0).getStreetAddress(), streetAddress)
      .validate(user -> user.getAddresses().get(0).getCountry(), country)
      .validate(user -> user.getAddresses().get(0).getRegion(), region)
      .validate(user -> user.getAddresses().get(0).getLocality(), locality)
      .validate(user -> user.getAddresses().get(0).getPostalCode(), postalCode)
      .validate(user -> user.getAddresses().get(0).getType(), "work")
      .validate(user -> user.getAddresses().get(0).getPrimary(), true)
      .build()));

    final String replaceAddress = faker().address().streetAddress();
    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REPLACE)
      .path("addresses[type EQ \"work\"].streetAddress")
      .value(replaceAddress)
      .setter(user -> {
        user.setAddresses(new ArrayList<>());
        user.getAddresses().add(address);
        user.getAddresses().add(ScimTestHelper.address(ScimTestHelper.faker().address(), "work", false));
        user.getAddresses().add(ScimTestHelper.address(ScimTestHelper.faker().address(), "home", false));
        return null; // return is ignored
      })
      .validate(user -> user.getAddresses() != null, true)
      .validate(user -> user.getAddresses().size(), 3)
      .validate(user -> user.getAddresses().get(0).getStreetAddress(), replaceAddress)
      .validate(user -> user.getAddresses().get(1).getStreetAddress(), replaceAddress)
      .validate(user -> !user.getAddresses().get(2).getStreetAddress().equalsIgnoreCase(replaceAddress), true)
      .build()));

    return values.stream();
  }
}
