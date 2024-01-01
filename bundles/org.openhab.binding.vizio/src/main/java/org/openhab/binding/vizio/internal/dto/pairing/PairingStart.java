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
package org.openhab.binding.vizio.internal.dto.pairing;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PairingStart} class maps the JSON data response from the Vizio TV endpoint:
 * '/pairing/start'
 *
 * @author Michael Lobstein - Initial contribution
 */
public class PairingStart {
    @SerializedName("ITEM")
    private ItemPairing item = new ItemPairing();

    public ItemPairing getItem() {
        return item;
    }

    public void setItem(ItemPairing item) {
        this.item = item;
    }
}
