/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal.dto;

/**
 * Mapping object for data returned from smartthings hub
 *
 * @author Bob Raker - Initial contribution
 */
public class SmartthingsDeviceData {
    public String capability;
    public String attribute;
    public String name;
    public String id;

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("capability :").append(capability);
        sb.append(", attribute :").append(attribute);
        sb.append(", name: ").append(name);
        sb.append(", id: ").append(id);
        return sb.toString();
    }
}
