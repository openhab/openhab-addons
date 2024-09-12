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
package org.openhab.binding.automower.internal.rest.api.automowerconnect.dto;

/**
 * @author Markus Pfleger - Initial contribution
 */
public class Mower {
    private String type;
    private String id;
    private MowerData attributes;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MowerData getAttributes() {
        return attributes;
    }

    public void setAttributes(MowerData attributes) {
        this.attributes = attributes;
    }
}
