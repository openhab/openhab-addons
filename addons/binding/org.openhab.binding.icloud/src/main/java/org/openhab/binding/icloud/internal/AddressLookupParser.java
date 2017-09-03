/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal;

import org.openhab.binding.icloud.handler.iCloudBridgeHandler;
import org.openhab.binding.icloud.internal.json.google.AddressComponent;
import org.openhab.binding.icloud.internal.json.google.JSONRootObject;
import org.openhab.binding.icloud.internal.json.google.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * @author Patrik Gfeller
 *
 */
public class AddressLookupParser {
    private final Logger logger = LoggerFactory.getLogger(iCloudBridgeHandler.class);
    private JSONRootObject data;

    public AddressLookupParser(String json) {

        try {
            Gson gson = new GsonBuilder().create();
            data = gson.fromJson(json, JSONRootObject.class);
        } catch (Exception e) {
            logger.error(e.getMessage(), e.getStackTrace().toString());
        }
    }

    public Address getAddress() {
        Address address = new Address();
        Result result = data.getResults().get(0);

        String street = "";
        String streetNumber = "";
        String postalCode = "";
        String city = "";

        address.FormattedAddress = result.getFormattedAddress();

        for (int i = 0; i < result.getAddressComponents().size(); i++) {
            AddressComponent component = result.getAddressComponents().get(i);
            String componentType = component.getTypes().get(0).toString();
            if (componentType.equals("street_number")) {
                streetNumber = component.getLongName();
            }
            if (componentType.equals("route")) {
                street = component.getLongName();
            }
            if (componentType.equals("locality")) {
                city = component.getLongName();
            }
            if (componentType.equals("country")) {
                address.Country = component.getLongName();
            }
            if (componentType.equals("postal_code")) {
                postalCode = component.getLongName();
            }
        }

        address.Street = street + " " + streetNumber;
        address.City = postalCode + " " + city;

        return address;
    }
}
