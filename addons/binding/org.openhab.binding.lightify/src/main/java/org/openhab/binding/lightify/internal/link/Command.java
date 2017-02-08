/*
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify.internal.link;

/**
 * @author Christoph Engelbert (@noctarius2k) - Initial contribution
 */
public enum Command {
    STATUS_ALL(0x13, true),
    STATUS_SINGLE(0x68, false),
    ZONE_LIST(0x1E, true),
    ZONE_INFO(0x26, false),
    LIGHT_LUMINANCE(0x31, false),
    LIGHT_SWITCH(0x32, false),
    LIGHT_TEMPERATURE(0x33, false),
    LIGHT_COLOR(0x36, false);

    private final byte id;
    private final boolean zone;

    Command(int id, boolean zone) {
        this.id = (byte) id;
        this.zone = zone;
    }

    public byte getId() {
        return id;
    }

    public boolean isZone() {
        return zone;
    }
}
