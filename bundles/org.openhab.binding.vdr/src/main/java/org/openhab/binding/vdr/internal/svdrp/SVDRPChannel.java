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
package org.openhab.binding.vdr.internal.svdrp;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SVDRPChannel} contains SVDRP Response Data for Channels
 *
 * @author Matthias Klocke - Initial contribution
 */
@NonNullByDefault
public class SVDRPChannel {
    private int number;
    private String name = "";

    private SVDRPChannel() {
    }

    /**
     * parse object from SVDRP Client Response
     *
     * @param message SVDRP Client Response
     * @return Channel Object
     * @throws SVDRPParseResponseException thrown if response data is not parseable
     */
    public static SVDRPChannel parse(String message) throws SVDRPParseResponseException {
        String number = message.substring(0, message.indexOf(" "));
        String name = message.substring(message.indexOf(" ") + 1, message.length());
        SVDRPChannel channel = new SVDRPChannel();
        try {
            channel.setNumber(Integer.parseInt(number));
        } catch (NumberFormatException e) {
            throw new SVDRPParseResponseException(e.getMessage(), e);
        }
        channel.setName(name);
        return channel;
    }

    /**
     * Get Channel Number
     *
     * @return Channel Number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Set Channel Number
     *
     * @param number Channel Number
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * Get Channel Name
     *
     * @return Channel Name
     */
    public String getName() {
        return name;
    }

    /**
     * Set Channel Name
     *
     * @param name Channel Name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * String Representation of SVDRPChannel Object
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (number >= 0) {
            sb.append("Number: " + String.valueOf(number) + System.lineSeparator());
        }
        sb.append("Name: " + name + System.lineSeparator());
        return sb.toString();
    }
}
