/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.webthings.internal.dto;

/**
 * The {@link ItemStateEventPayload} can be used to save event types and values as string
 *
 * @author schneider_sven - Initial contribution
 */
public class ItemStateEventPayload {
    private String type;
    private String value;

    public ItemStateEventPayload(String type, String value){
        this.type = type;
        this.value = value;
    }

    public String getType() { return type; }
    public void setType(String value) { this.type = value; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
