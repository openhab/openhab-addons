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

public class SmartthingsJSonArguments {
    protected String name;
    protected boolean optional;
    protected SmartthingsJSonSchema schema;

    public SmartthingsJSonArguments() {
        // attributes = new ArrayList<Object>();
    }

    public void setName(String value) {
        name = value;
    }

    public String getName() {
        return name;
    }

    public void setOptional(boolean value) {
        optional = value;
    }

    public boolean getOptional() {
        return optional;
    }

    public void setSchema(SmartthingsJSonSchema value) {
        schema = value;
    }

    public SmartthingsJSonSchema getSchema() {
        return schema;
    }
}
