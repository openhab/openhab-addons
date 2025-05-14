/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.device.database.LinkDBRecord;
import org.openhab.binding.insteon.internal.device.database.ModemDBRecord;

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

    public DefaultLink(String name, LinkDBRecord linkDBRecord, ModemDBRecord modemDBRecord) {
        this.name = name;
        this.linkDBRecord = linkDBRecord;
        this.modemDBRecord = modemDBRecord;
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

    @Override
    public String toString() {
        return name + "|linkDB:" + linkDBRecord + "|modemDB:" + modemDBRecord;
    }
}
