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

import java.util.List;

/**
 *
 * @author Laurent Arnal - Initial contribution
 *
 */
public class SmartthingsJSonSchema {
    protected String type;
    protected List<SmartthingsJSonProperties> properties;
    protected boolean additionalProperties;
    protected String title;

    public SmartthingsJSonSchema() {
        // attributes = new ArrayList<Object>();
    }

    public String getType() {
        return type;
    }

    public void setType(String value) {
        type = value;
    }

    public List<SmartthingsJSonProperties> getProperties() {
        return properties;
    }

    public void setAttributes(List<SmartthingsJSonProperties> value) {
        properties = value;
    }

    public boolean getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(boolean value) {
        additionalProperties = value;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String value) {
        title = value;
    }
}
