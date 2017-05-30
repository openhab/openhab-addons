/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal;

import java.io.IOException;
import java.util.Objects;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.neeo.NeeoConstants;
import org.openhab.binding.neeo.NeeoUtil;
import org.openhab.binding.neeo.internal.models.NeeoDevice;
import org.openhab.binding.neeo.internal.models.NeeoMacro;
import org.openhab.binding.neeo.internal.models.NeeoRoom;
import org.openhab.binding.neeo.internal.type.UidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This protocol class for a Neeo Device
 *
 * @author Tim Roberts - Initial contribution
 */
public class NeeoDeviceProtocol {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoDeviceProtocol.class);

    /** The {@link NeeoHandlerCallback} */
    private final NeeoHandlerCallback callback;

    /** The {@link NeeoRoom} */
    private final NeeoRoom neeoRoom;

    /** The {@link NeeoDevice} */
    private final NeeoDevice neeoDevice;

    /**
     * Instantiates a new neeo device protocol.
     *
     * @param callback the non-null callback
     * @param roomKey the non-empty room key
     * @param deviceKey the non-empty device key
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public NeeoDeviceProtocol(NeeoHandlerCallback callback, String roomKey, String deviceKey) throws IOException {
        Objects.requireNonNull(callback, "callback cannot be null");
        Objects.requireNonNull(roomKey, "roomKey cannot be empty");
        Objects.requireNonNull(deviceKey, "deviceKey cannot be empty");

        this.callback = callback;

        final NeeoBrainApi api = callback.getApi();
        if (api == null) {
            throw new IllegalArgumentException("NeeoBrainApi cannot be null");
        }

        neeoRoom = api.getRoom(roomKey);
        if (neeoRoom == null) {
            throw new IllegalArgumentException("Room (" + roomKey + ") was not found in the NEEO Brain");
        }

        neeoDevice = neeoRoom.getDevices().getDevice(deviceKey);
        if (neeoDevice == null) {
            throw new IllegalArgumentException(
                    "Device (" + deviceKey + ") was not found in the NEEO Brain for room (" + roomKey + ")");
        }
    }

    /**
     * Returns the callback being used
     *
     * @return the non-null callback
     */
    public NeeoHandlerCallback getCallback() {
        return callback;
    }

    /**
     * Refresh the macro status.
     *
     * @param macroKey the non-null macro key
     */
    public void refreshMacroStatus(String macroKey) {
        NeeoUtil.requireNotEmpty(macroKey, "macroKey cannot be empty");

        final NeeoMacro macro = neeoDevice.getMacros().getMacro(macroKey);
        if (macro != null) {
            callback.stateChanged(UidUtils.createChannelId(NeeoConstants.DEVICE_GROUP_MACROSID, macroKey),
                    OnOffType.OFF);
        }
    }

    /**
     * Sets the macro status. If the status is true, the macro will be triggered. If false, nothing occurs
     *
     * @param macroKey the non-null macro key
     * @param start whether to start (true) or stop (false) the recipe
     */
    public void setMacroStatus(String macroKey, boolean start) {
        NeeoUtil.requireNotEmpty(macroKey, "macroKey cannot be empty");

        final NeeoBrainApi api = callback.getApi();
        if (api == null) {
            logger.debug("API is null [likely bridge is offline]");
        } else {
            try {
                if (start) {
                    api.triggerMacro(neeoRoom.getKey(), neeoDevice.getKey(), macroKey);
                    callback.scheduleTask(() -> {
                        refreshMacroStatus(macroKey);
                    }, 500);
                }
            } catch (IOException e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                // callback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }
}
