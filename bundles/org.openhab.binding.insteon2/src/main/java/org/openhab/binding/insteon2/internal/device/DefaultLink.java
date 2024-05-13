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
package org.openhab.binding.insteon2.internal.device;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon2.internal.database.LinkDBRecord;
import org.openhab.binding.insteon2.internal.database.ModemDBRecord;
import org.openhab.binding.insteon2.internal.transport.message.Msg;

/**
 * The {@link DefaultLink} represents a device default link
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class DefaultLink {
    private String name;
    private LinkDBRecord linkDBRecord;
    private ModemDBRecord modemDBRecord;
    private List<Msg> commands;

    public DefaultLink(String name, LinkDBRecord linkDBRecord, ModemDBRecord modemDBRecord, List<Msg> commands) {
        this.name = name;
        this.linkDBRecord = linkDBRecord;
        this.modemDBRecord = modemDBRecord;
        this.commands = commands;
    }

    public String getName() {
        return name;
    }

    public LinkDBRecord getLinkDBRecord() {
        return linkDBRecord;
    }

    public ModemDBRecord getModemDBRecord() {
        return modemDBRecord;
    }

    public List<Msg> getCommands() {
        return commands;
    }

    @Override
    public String toString() {
        String s = name;
        s += "|linkDB:" + linkDBRecord;
        s += "|modemDB:" + modemDBRecord;
        if (!commands.isEmpty()) {
            s += "|commands:" + commands;
        }
        return s;
    }
}
