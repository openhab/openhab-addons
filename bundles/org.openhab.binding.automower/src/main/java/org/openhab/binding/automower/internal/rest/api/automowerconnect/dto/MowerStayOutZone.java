/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
 * @author MikeTheTux - Initial contribution
 */
public class MowerStayOutZone {
    private String type;
    private String id;
    private MowerStayOutZoneAttributes attributes;

    public String getType() {
        return type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MowerStayOutZoneAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(MowerStayOutZoneAttributes attributes) {
        this.attributes = attributes;
    }
}
