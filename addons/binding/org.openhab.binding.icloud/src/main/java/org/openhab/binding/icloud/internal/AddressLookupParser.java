/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal;

import org.openhab.binding.icloud.internal.json.google.AddressComponent;
import org.openhab.binding.icloud.internal.json.google.JSONRootObject;
import org.openhab.binding.icloud.internal.json.google.Result;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Parses a google API address lookup response (json).
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class AddressLookupParser {
    private final Gson gson = new GsonBuilder().create();
    private JSONRootObject data;

    public Address getAddress(String json) {
        data = gson.fromJson(json, JSONRootObject.class);
        Address address = new Address();
        Result result = data.getResults().get(0);

        String street = "";
        String streetNumber = "";
        String postalCode = "";
        String city = "";

        address.formattedAddress = result.getFormattedAddress();

        for (AddressComponent component : result.getAddressComponents()) {
            String componentType = component.getTypes().get(0);

            switch (componentType) {
                case "street_number":
                    streetNumber = component.getLongName();
                    break;
                case "route":
                    street = component.getLongName();
                    break;
                case "locality":
                    city = component.getLongName();
                    break;
                case "country":
                    address.country = component.getLongName();
                    break;
                case "postal_code":
                    postalCode = component.getLongName();
                    break;
            }
        }

        address.street = street + " " + streetNumber;
        address.city = postalCode + " " + city;

        return address;
    }
}
