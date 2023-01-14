/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * The {@link SVDRPVolume} contains SVDRP Response Data for Volume Object
 *
 * @author Matthias Klocke - Initial contribution
 */
@NonNullByDefault
public class SVDRPVolume {

    private int volume = -1;

    private SVDRPVolume() {
    }

    /**
     * parse object from SVDRP Client Response
     *
     * @param message SVDRP Client Response
     * @return Volume Object
     * @throws SVDRPParseResponseException thrown if response data is not parseable
     */
    public static SVDRPVolume parse(String message) throws SVDRPParseResponseException {
        SVDRPVolume volume = new SVDRPVolume();
        try {
            String vol = message.substring(message.lastIndexOf(" ") + 1, message.length());
            if ("mute".equals(vol)) {
                volume.setVolume(0);
            } else {
                int val = Integer.parseInt(vol);
                val = val * 100 / 255;
                volume.setVolume(val);
            }
        } catch (NumberFormatException nex) {
            throw new SVDRPParseResponseException(nex.getMessage(), nex);
        } catch (IndexOutOfBoundsException ie) {
            throw new SVDRPParseResponseException(ie.getMessage(), ie);
        }
        return volume;
    }

    /**
     * Get Volume in Percent
     *
     * @param volume Volume in Percent
     */
    private void setVolume(int volume) {
        this.volume = volume;
    }

    /**
     * Set Volume in Percent
     *
     * @return Volume in Percent
     */
    public int getVolume() {
        return volume;
    }

    /**
     * String Representation of SVDRPDiskStatus Object
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (volume > -1) {
            sb.append("Volume: ");
            sb.append(String.valueOf(volume));
        }
        return sb.toString();
    }
}
