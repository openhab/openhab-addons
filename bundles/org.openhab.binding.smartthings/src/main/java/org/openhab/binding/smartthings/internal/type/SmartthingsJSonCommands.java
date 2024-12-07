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
public class SmartthingsJSonCommands {
    protected String name;

    protected List<SmartthingsJSonArguments> arguments;

    public SmartthingsJSonCommands() {
        // attributes = new ArrayList<Object>();
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public List<SmartthingsJSonArguments> getCommands() {
        return arguments;
    }

    public void setCommands(List<SmartthingsJSonArguments> value) {
        arguments = value;
    }
}
