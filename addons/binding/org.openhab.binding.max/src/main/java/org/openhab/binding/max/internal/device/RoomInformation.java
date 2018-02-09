/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.device;

/**
 * Room information provided by the M message meta information.
 *
 * @author Andreas Heil (info@aheil.de)
 * @author Marcel Verpaalen (marcel@verpaalen.com)
 * @since 1.4.0
 */
public class RoomInformation {
    private int position = -1;
    private String name = "";
    private String rfAddress = "";

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
