package org.apache.directory.scim.test.arguments.provider;

import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.REMOVE;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type.REPLACE;
import static org.apache.directory.scim.test.ScimTestHelper.address;
import static org.apache.directory.scim.test.ScimTestHelper.faker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.directory.scim.spec.resources.Address;
import org.apache.directory.scim.spec.resources.ScimUser;
import org.apache.directory.scim.test.arguments.provider.args.PatchArgs;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TargetLocationSpecifiesMultiValuedNoFilter implements ArgumentsProvider {
  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
    List<Arguments> values = new ArrayList<>();

    final Address scimAddress = address(faker().address(), "home", true);

    // generate fake data
    final com.github.javafaker.Address address = faker().address();
    final String streetAddress = address.streetAddress();
    final String country = address.countryCode();
    final String region = address.stateAbbr();
    final String locality = address.city();
    final String postalCode = address.zipCode();

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
      .path("addresses")
      .setter( user -> {
        user.setAddresses(new ArrayList<>());
        user.getAddresses().add(scimAddress);
        return null;
      })
      .value(addressesList)
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

    final Address scimAddress2 = address(faker().address(), "home", true);

    values.add(Arguments.of(new PatchArgs.Builder<ScimUser>()
      .type(REMOVE)
      .path("addresses")
      .setter( user -> {
        user.setAddresses(new ArrayList<>());
        user.getAddresses().add(scimAddress);
        user.getAddresses().add(scimAddress2);
        return null;
      })
      .validate(user -> user.getAddresses() == null, true)
      .build()));

    return values.stream();
  }
}
