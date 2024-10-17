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
package org.openhab.binding.insteon.internal.device.database;

import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.transport.LegacyPort;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.utils.HexUtils;

/**
 * The ModemDBEntry class holds a modem device type record
 * an xml file.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public class LegacyModemDBEntry {
    private @Nullable InsteonAddress address = null;
    private boolean isModem;
    private @Nullable LegacyPort port = null;
    private ArrayList<Msg> linkRecords = new ArrayList<>();
    private ArrayList<Byte> controls = new ArrayList<>();
    private ArrayList<Byte> respondsTo = new ArrayList<>();

    public LegacyModemDBEntry(InsteonAddress address, boolean isModem) {
        this.address = address;
        this.isModem = isModem;
    }

    public boolean isModem() {
        return isModem;
    }

    public ArrayList<Msg> getLinkRecords() {
        return linkRecords;
    }

    public void addLinkRecord(Msg m) {
        linkRecords.add(m);
    }

    public void addControls(byte c) {
        controls.add(c);
    }

    public ArrayList<Byte> getControls() {
        return controls;
    }

    public void addRespondsTo(byte r) {
        respondsTo.add(r);
    }

    public ArrayList<Byte> getRespondsTo() {
        return respondsTo;
    }

    public void setPort(LegacyPort p) {
        port = p;
    }

    public @Nullable LegacyPort getPort() {
        return port;
    }

    @Override
    public String toString() {
        String s = "addr:" + address + "|controls:[" + toGroupString(controls) + "]|responds_to:["
                + toGroupString(respondsTo) + "]|link_recors";
        for (Msg msg : linkRecords) {
            s += ":(" + msg + ")";
        }
        return s;
    }

    private String toGroupString(ArrayList<Byte> group) {
        ArrayList<Byte> sorted = new ArrayList<>(group);
        Collections.sort(sorted);

        StringBuilder buf = new StringBuilder();
        for (Byte b : sorted) {
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append("0x");
            buf.append(HexUtils.getHexString(b));
        }

        return buf.toString();
    }
}
