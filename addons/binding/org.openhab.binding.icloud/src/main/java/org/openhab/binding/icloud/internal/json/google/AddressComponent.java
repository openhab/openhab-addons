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
public class AddressComponent {
    private String long_name;

    public String getLongName() {
        return this.long_name;
    }

    public void setLongName(String long_name) {
        this.long_name = long_name;
    }

    private String short_name;

    public String getShortName() {
        return this.short_name;
    }

    public void setShortName(String short_name) {
        this.short_name = short_name;
    }

    private ArrayList<String> types;

    public ArrayList<String> getTypes() {
        return this.types;
    }

    public void setTypes(ArrayList<String> types) {
        this.types = types;
    }
}
