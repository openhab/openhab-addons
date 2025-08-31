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
package org.openhab.binding.smartthings.internal.type;

/**
 *
 * @author Laurent Arnal - Initial contribution
 *
 */
public class SmartthingsJSonAttributes {
    protected String name;
    protected String setter;
    protected SmartthingsJSonSchema schema;

    public SmartthingsJSonAttributes() {
        // attributes = new ArrayList<Object>();
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public String getSetter() {
        return setter;
    }

    public void setSetter(String value) {
        setter = value;
    }

    public SmartthingsJSonSchema getSchema() {
        return schema;
    }

    public void setSchema(SmartthingsJSonSchema value) {
        schema = value;
    }
}
