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

import com.google.gson.annotations.SerializedName;

/**
 * Object use the de-serialize json response from google API.
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class AddressComponent {
    @SerializedName("long_name")
    private String longName;

    @SerializedName("short_name")
    private String shortName;

    private ArrayList<String> types;

    public String getLongName() {
        return this.longName;
    }

    public String getShortName() {
        return this.shortName;
    }

    public ArrayList<String> getTypes() {
        return this.types;
    }

    public void setLongName(String long_name) {
        this.longName = long_name;
    }

    public void setShortName(String short_name) {
        this.shortName = short_name;
    }

    public void setTypes(ArrayList<String> types) {
        this.types = types;
    }
}
