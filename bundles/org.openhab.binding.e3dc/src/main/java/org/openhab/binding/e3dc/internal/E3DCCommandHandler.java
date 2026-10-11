/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.rscp.RscpException;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds and sends the write requests of the binding.
 *
 * <p>
 * The power settings are one container holding the charge and discharge limits together with the
 * weather regulated charge flag. The device resets the enclosed limits when the power limits flag is
 * switched off, so a write has to carry every parameter of that container. This class remembers the
 * last values so writing one parameter does not discard the others.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCCommandHandler {

    private final Logger logger = LoggerFactory.getLogger(E3DCCommandHandler.class);

    /** Last known value of each parameter of the power settings container. */
    private final Map<Integer, RscpValue> powerSettings = new LinkedHashMap<>();

    /** Last known wallbox state; the packed command has to carry the unchanged fields as well. */
    private volatile @Nullable E3DCWallboxCommand wallboxState;

    /** Remembers the device state so a later write can complete the parameters it does not change. */
    public void updateFromResponse(RscpValue response) {
        RscpValue settings = response.find(RscpTag.EMS_GET_POWER_SETTINGS);
        if (settings != null) {
            for (int tag : E3DCCommandSpec.POWER_SETTINGS_PARAMETERS) {
                RscpValue parameter = settings.find(tag);
                if (parameter != null && parameter.getValue() != null) {
                    powerSettings.put(tag, parameter);
                }
            }
        }
        updateWallboxState(response);
    }

    /**
     * Reads the wallbox status bytes so a command for one field can carry the others. The status byte
     * at index 2 holds the charging mode in bit 7, the maximum current follows in the next byte.
     */
    private void updateWallboxState(RscpValue response) {
        RscpValue data = response.find(RscpTag.WB_EXTERN_DATA);
        if (data == null) {
            return;
        }
        byte[] bytes = data.asBytes();
        if (bytes == null || bytes.length < 4) {
            return;
        }
        boolean sunMode = (bytes[2] & 0x80) != 0;
        int maxCurrent = E3DCWallboxCommand.clampCurrent(bytes[3] & 0xFF);
        wallboxState = new E3DCWallboxCommand(sunMode, maxCurrent, false);
    }

    /** The wallbox state used to complete a packed command, or {@code null} when it is unknown. */
    public @Nullable E3DCWallboxCommand getWallboxState() {
        return wallboxState;
    }

    /**
     * Builds the request for a command on a settable channel.
     *
     * @param spec the command specification of the channel
     * @param command the command received from openHAB
     * @param deviceIndex the index of the addressed device for containers that hold several
     * @return the request to send, or {@code null} when the command cannot be represented
     */
    public @Nullable RscpValue buildRequest(E3DCCommandSpec spec, Command command, int deviceIndex) {
        RscpValue value = spec.toValue(command);
        if (value == null) {
            return null;
        }
        if (spec.isWallboxCommand()) {
            return buildWallboxRequest(spec, value, deviceIndex);
        }
        if (spec.container() == 0) {
            return RscpValue.container(0, List.of(value));
        }
        if (spec.container() == RscpTag.EMS_REQ_SET_POWER_SETTINGS) {
            return buildPowerSettingsRequest(spec.tag(), value);
        }
        // a container addressing one of several devices needs the index as its first child
        List<RscpValue> items = new ArrayList<>();
        int indexTag = indexTagFor(spec.container());
        if (indexTag != 0) {
            items.add(RscpValue.uchar8(indexTag, deviceIndex));
        }
        items.add(value);
        return RscpValue.container(0, List.of(RscpValue.container(spec.container(), items)));
    }

    /**
     * Assembles the packed wallbox command.
     *
     * <p>
     * The device expects one complete command payload, so the commanded field is merged into the last
     * known state. A write without a known state is refused rather than sent with default values,
     * because the remaining fields would then silently change the charging behaviour.
     */
    private @Nullable RscpValue buildWallboxRequest(E3DCCommandSpec spec, RscpValue value, int deviceIndex) {
        E3DCWallboxCommand state = wallboxState;
        if (state == null) {
            logger.debug("Cannot write to the wallbox before its state has been read");
            return null;
        }
        E3DCWallboxCommand next = state;
        Number commanded = value.asNumber();
        if (spec.tag() == RscpTag.WB_MAX_CHARGE_CURRENT) {
            if (commanded == null) {
                return null;
            }
            next = state.withMaxCurrent(commanded.intValue());
        } else if (spec.tag() == E3DCWallboxField.SUN_MODE) {
            next = state.withSunMode(commanded != null && commanded.intValue() != 0);
        }
        byte[] payload = next.toPayload();

        List<RscpValue> extern = List.of(
                RscpValue.of(RscpTag.WB_EXTERN_DATA_LEN, RscpType.UINT32, (long) payload.length),
                RscpValue.of(RscpTag.WB_EXTERN_DATA, RscpType.BYTEARRAY, payload));
        List<RscpValue> items = List.of(RscpValue.uchar8(RscpTag.WB_INDEX, deviceIndex),
                RscpValue.container(RscpTag.WB_REQ_SET_EXTERN, extern));
        wallboxState = next;
        return RscpValue.container(0, List.of(RscpValue.container(RscpTag.WB_REQ_DATA, items)));
    }

    /**
     * Assembles the complete settings container, replacing the parameter being written and carrying
     * the remaining ones from the last poll. Parameters that have never been read are left out rather
     * than written as zero, which would change a limit the user did not touch.
     */
    private RscpValue buildPowerSettingsRequest(int changedTag, RscpValue changedValue) {
        List<RscpValue> parameters = new ArrayList<>();
        for (int tag : E3DCCommandSpec.POWER_SETTINGS_PARAMETERS) {
            if (tag == changedTag) {
                parameters.add(changedValue);
                continue;
            }
            RscpValue previous = powerSettings.get(tag);
            if (previous != null) {
                parameters.add(previous);
            }
        }
        return RscpValue.container(0, List.of(RscpValue.container(RscpTag.EMS_REQ_SET_POWER_SETTINGS, parameters)));
    }

    /** Sends the request and reports whether the device answered without an error. */
    public boolean send(E3DCBridgeHandler bridge, RscpValue request) {
        try {
            RscpValue response = bridge.sendRequest(request);
            if (response.containsError()) {
                logger.debug("The device rejected the command: {}", response.findError());
                return false;
            }
            // refresh the snapshot so a following write keeps working with current values
            updateFromResponse(response);
            return true;
        } catch (RscpException e) {
            logger.debug("Cannot send the command: {}", e.getMessage());
            return false;
        }
    }

    /** The index tag of a container that addresses one of several devices. */
    private int indexTagFor(int container) {
        if (container == RscpTag.WB_DATA) {
            return RscpTag.WB_INDEX;
        }
        if (container == RscpTag.PM_DATA) {
            return RscpTag.PM_INDEX;
        }
        return 0;
    }
}
