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
public class SmartthingsJSonCapabilities {
    protected String id;
    protected String version;
    protected String status;
    protected String name;
    protected boolean ephemeral;

    protected List<SmartthingsJSonAttributes> attributes;
    protected List<SmartthingsJSonCommands> commands;

    public SmartthingsJSonCapabilities() {
        // attributes = new ArrayList<Object>();
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        id = value;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String value) {
        version = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        status = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public boolean getEphemeral() {
        return ephemeral;
    }

    public void setEphemeral(boolean value) {
        ephemeral = value;
    }

    public List<SmartthingsJSonAttributes> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<SmartthingsJSonAttributes> value) {
        attributes = value;
    }

    public List<SmartthingsJSonCommands> getCommands() {
        return commands;
    }

    public void setCommands(List<SmartthingsJSonCommands> value) {
        commands = value;
    }
}
