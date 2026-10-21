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
package org.openhab.binding.e3dc.internal.handler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.E3DCCommandSpec;
import org.openhab.binding.e3dc.internal.rscp.RscpException;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
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
    }

    /**
     * Builds the request for a command on a settable channel.
     *
     * @param spec the command specification of the channel
     * @param command the command received from openHAB
     * @param deviceIndex the index of the addressed device for containers that hold several
     * @return the request to send, or {@code null} when the command cannot be represented
     */
    public @Nullable List<RscpValue> buildRequest(E3DCCommandSpec spec, Command command, int deviceIndex) {
        RscpValue value = spec.toValue(command);
        if (value == null) {
            return null;
        }
        if (spec.container() == 0) {
            return List.of(value);
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
        return List.of(RscpValue.container(spec.container(), items));
    }

    /**
     * Assembles the complete settings container, replacing the parameter being written and carrying
     * the remaining ones from the last poll. Parameters that have never been read are left out rather
     * than written as zero, which would change a limit the user did not touch.
     */
    private List<RscpValue> buildPowerSettingsRequest(int changedTag, RscpValue changedValue) {
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
        return List.of(RscpValue.container(RscpTag.EMS_REQ_SET_POWER_SETTINGS, parameters));
    }

    /** Sends the request and reports whether the device answered without an error. */
    public boolean send(E3DCBridgeHandler bridge, List<RscpValue> request) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Sending command: {}", RscpValue.container(0, request).describeTree());
            }
            RscpValue response = bridge.sendRequest(request);
            if (logger.isDebugEnabled()) {
                logger.debug("Command answered with: {}", response.describeTree());
            }
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
        if (container == RscpTag.WB_DATA || container == RscpTag.WB_REQ_DATA) {
            return RscpTag.WB_INDEX;
        }
        if (container == RscpTag.PM_DATA) {
            return RscpTag.PM_INDEX;
        }
        return 0;
    }
}
