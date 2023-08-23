/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.keypress;

import org.openhab.binding.androidtv.internal.protocol.philipstv.service.KeyPress;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link KeyPressDto} class defines the Data Transfer Object
 * for the Philips TV API /input/key endpoint for remote controller emulation.
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */

public class KeyPressDto {

    @JsonProperty("key")
    private KeyPress key;

    public KeyPressDto() {
    }

    public KeyPressDto(KeyPress key) {
        this.key = key;
    }

    public KeyPress getKey() {
        return key;
    }

    public void setKey(KeyPress key) {
        this.key = key;
    }
}
