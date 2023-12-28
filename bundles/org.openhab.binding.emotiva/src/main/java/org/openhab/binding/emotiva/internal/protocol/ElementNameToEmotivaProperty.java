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
package org.openhab.binding.emotiva.internal.protocol;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Helper class to provide XML message element name as actual name of command.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public class ElementNameToEmotivaProperty extends XmlAdapter<String, EmotivaControlCommands> {
    @Override
    public EmotivaControlCommands unmarshal(String command) {
        return EmotivaControlCommands.valueOf(command);
    }

    @Override
    public String marshal(EmotivaControlCommands command) {
        return command.toString();
    }
}
