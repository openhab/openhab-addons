/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.max.internal.device;

/**
 * Room information provided by the M message meta information.
 *
 * @author Andreas Heil (info@aheil.de) - Initial contribution
 * @author Marcel Verpaalen (marcel@verpaalen.com) - OH2 update
 */
public class RoomInformation {
    private int position;
    private String name;
    private String rfAddress;

    public RoomInformation(int position, String name, String rfAddress) {
        this.position = position;
        this.name = name;
        this.rfAddress = rfAddress;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRFAddress() {
        return rfAddress;
    }

    public void setRFAddress(String rfAddress) {
        this.rfAddress = rfAddress;
    }

    @Override
    public String toString() {
        return "Room " + position + " (" + rfAddress + ") ='" + name + "'";
    }
}
