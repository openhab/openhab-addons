/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solarlog.internal;

/**
 * The {@link SolarLogChannel} enum defines common constants, which are
 * used across the whole binding.
 *
 * @author Johann Richard - Initial contribution
 */
public enum SolarLogChannel {

    CHANNEL_LASTUPDATETIME("lastupdate", "100", "DateTime"),
    CHANNEL_PAC("pac", "101", "Number"),
    CHANNEL_PDC("pdc", "102", "Number"),
    CHANNEL_UAC("uac", "103", "Number"),
    CHANNEL_UDC("udc", "104", "Number"),
    CHANNEL_YIELDDAY("yieldday", "105", "Number"),
    CHANNEL_YIELDYESTERDAY("yieldyesterday", "106", "Number"),
    CHANNEL_YIELDMONTH("yieldmonth", "107", "Number"),
    CHANNEL_YIELDYEAR("yieldyear", "108", "Number"),
    CHANNEL_YIELDTOTAL("yieldtotal", "109", "Number"),
    CHANNEL_CONSPAC("conspac", "110", "Number"),
    CHANNEL_CONSYIELDDAY("consyieldday", "111", "Number"),
    CHANNEL_CONSYIELDYESTERDAY("consyieldyesterday", "112", "Number"),
    CHANNEL_CONSYIELDMONTH("consyieldmonth", "113", "Number"),
    CHANNEL_CONSYIELDYEAR("consyieldyear", "114", "Number"),
    CHANNEL_CONSYIELDTOTAL("consyieldtotal", "115", "Number"),
    CHANNEL_TOTALPOWER("totalpower", "116", "Number");

    private final String id;
    private final String index;
    private final String channelType;

    SolarLogChannel(String id, String index, String channelType) {
        this.id = id;
        this.index = index;
        this.channelType = channelType;
    }

    public String getId() {
        return id;
    }

    public String getIndex() {
        return index;
    }

    public String getType() {
        return channelType;
    }
}
