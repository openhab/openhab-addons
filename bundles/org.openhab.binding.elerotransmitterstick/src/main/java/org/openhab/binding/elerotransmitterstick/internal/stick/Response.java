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
package org.openhab.binding.elerotransmitterstick.internal.stick;

import java.util.Arrays;

/**
 * @author Volker Bier - Initial contribution
 */
public class Response {
    private ResponseStatus status;
    private int[] channels;

    public Response(ResponseStatus status, int[] channels) {
        this.status = status;
        this.channels = channels;
    }

    public Response(int[] channels) {
        this.channels = channels;
    }

    public boolean isMoving() {
        return status == ResponseStatus.MOVING_DOWN || status == ResponseStatus.MOVING_UP;
    }

    public int[] getChannelIds() {
        return channels;
    }

    public boolean hasStatus() {
        return status != null;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public boolean isResponseFor(CommandPacket cmd) {
        if (cmd == null || cmd.isEasyCheck()) {
            return false;
        }

        byte[] cmdBytes = cmd.getBytes();
        int[] cmdChannels = ResponseUtil.getChannelIds(cmdBytes[3], cmdBytes[4]);
        return Arrays.equals(channels, cmdChannels);
    }

    @Override
    public String toString() {
        return status + " for channels " + Arrays.toString(channels);
    }
}
