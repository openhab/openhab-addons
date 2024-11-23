/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.vizio.internal.dto.app;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ItemApp} class contains data from the Vizio TV JSON response
 *
 * @author Michael Lobstein - Initial contribution
 */
public class ItemApp {
    @SerializedName("TYPE")
    private String type;

    @SerializedName("VALUE")
    private ItemAppValue value = new ItemAppValue();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ItemAppValue getValue() {
        return value;
    }

    public void setValue(ItemAppValue value) {
        this.value = value;
    }
}
