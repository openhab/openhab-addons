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
package org.openhab.binding.neeo.internal;

import java.io.IOException;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.neeo.internal.models.NeeoDevice;
import org.openhab.binding.neeo.internal.models.NeeoDevices;
import org.openhab.binding.neeo.internal.models.NeeoMacro;
import org.openhab.binding.neeo.internal.models.NeeoRoom;
import org.openhab.core.library.types.OnOffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This protocol class for a Neeo Device
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoDeviceProtocol {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoDeviceProtocol.class);

    /** The {@link NeeoHandlerCallback} */
    private final NeeoHandlerCallback callback;

    /** The room key */
    private final String roomKey;

    /** The device key */
    private final String deviceKey;

    /** The {@link NeeoDevice} in the room */
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
        NeeoUtil.requireNotEmpty(roomKey, "roomKey cannot be empty");
        NeeoUtil.requireNotEmpty(deviceKey, "deviceKey cannot be empty");

        this.roomKey = roomKey;
        this.callback = callback;
        this.deviceKey = deviceKey;

        final NeeoBrainApi api = callback.getApi();
        if (api == null) {
            throw new IllegalArgumentException("NeeoBrainApi cannot be null");
        }

        final NeeoRoom neeoRoom = api.getRoom(roomKey);

        final NeeoDevices devices = neeoRoom.getDevices();
        final NeeoDevice device = devices.getDevice(deviceKey);
        if (device == null) {
            throw new IllegalArgumentException(
                    "Device (" + deviceKey + ") was not found in the NEEO Brain for room (" + roomKey + ")");
        }

        neeoDevice = device;
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
            callback.stateChanged(UidUtils.createChannelId(NeeoConstants.DEVICE_GROUP_MACROS_ID,
                    NeeoConstants.DEVICE_CHANNEL_STATUS, macroKey), OnOffType.OFF);
        }
    }

    /**
     * Sets the macro status. If the status is true, the macro will be triggered. If false, nothing occurs
     *
     * @param macroKey the non-null macro key
     * @param start whether to start (true) or stop (false) the macro
     */
    public void setMacroStatus(String macroKey, boolean start) {
        NeeoUtil.requireNotEmpty(macroKey, "macroKey cannot be empty");

        final NeeoBrainApi api = callback.getApi();
        if (api == null) {
            logger.debug("API is null [likely bridge is offline]");
        } else {
            try {
                if (start) {
                    api.triggerMacro(roomKey, deviceKey, macroKey);

                    // NEEO macros are not what we generally think of for macros
                    // Trigger a NEEO macro is simply asking the brain to send an IR pulse
                    // for whatever the macro is linked up to (POWER ON would send the IR
                    // pulse for the specified device). Because of this, the execution of the
                    // macro will never take more than 100ms to complete. Since we get no
                    // feedback from the brain whether the macro has executed or completed
                    // AND it's impossible to tell if any macro is executing or not (no equivalent
                    // API to poll for), we simply refresh the status back to OFF after 500ms
                    callback.scheduleTask(() -> {
                        callback.stateChanged(UidUtils.createChannelId(NeeoConstants.DEVICE_GROUP_MACROS_ID,
                                NeeoConstants.DEVICE_CHANNEL_STATUS, macroKey), OnOffType.OFF);
                    }, 500);
                }
            } catch (IOException e) {
                // Some macros have issues executing on the NEEO Brain (depends on the firmware)
                // and IO exception will be thrown if the macro encounters an issue
                // (mostly it depends on the state of the brain - if it's starting up or in the process
                // of executing a long scenario - the macro will likely timeout or simply throw an exception)
                // Because of this, we simply log the error versus taking the binding offline
                logger.warn(
                        "Exception occurred during execution of a macro (may need to update the brain firmware): {}",
                        e.getMessage(), e);
                // callback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }
}
