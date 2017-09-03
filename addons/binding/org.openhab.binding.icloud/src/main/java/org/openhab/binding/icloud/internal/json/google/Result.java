/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal.json.google;

import java.util.ArrayList;

/**
 *
 * @author Patrik Gfeller
 *
 */
public class Result {
    private ArrayList<AddressComponent> address_components;

    public ArrayList<AddressComponent> getAddressComponents() {
        return this.address_components;
    }

    public void setAddressComponents(ArrayList<AddressComponent> address_components) {
        this.address_components = address_components;
    }

    private String formatted_address;

    public String getFormattedAddress() {
        return this.formatted_address;
    }

    public void setFormattedAddress(String formatted_address) {
        this.formatted_address = formatted_address;
    }

    private Geometry geometry;

    public Geometry getGeometry() {
        return this.geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    private String place_id;

    public String getPlaceId() {
        return this.place_id;
    }

    public void setPlaceId(String place_id) {
        this.place_id = place_id;
    }

    private ArrayList<String> types;

    public ArrayList<String> getTypes() {
        return this.types;
    }

    public void setTypes(ArrayList<String> types) {
        this.types = types;
    }
}
