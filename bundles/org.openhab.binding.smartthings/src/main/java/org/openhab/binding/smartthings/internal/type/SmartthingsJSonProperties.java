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
public class SmartthingsJSonProperties {
    protected String name;
    protected String title;
    protected String type;

    public SmartthingsJSonProperties() {
        // attributes = new ArrayList<Object>();
    }

    public void setType(String value) {
        type = value;
    }

    public String getType() {
        return type;
    }

    public void setName(String value) {
        name = value;
    }

    public String getName() {
        return name;
    }

    public void setTitle(String value) {
        title = value;
    }

    public String getTitle() {
        return title;
    }
}
