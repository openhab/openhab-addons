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
import java.util.Set;

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
 * Builds and sends the write requests of the binding, serialized against the poll. The device resets the
 * power limits when the power limits flag is off, so a write has to carry all remembered parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCCommandHandler {

    private final Logger logger = LoggerFactory.getLogger(E3DCCommandHandler.class);

    private static final Set<Integer> POWER_SETTINGS_CONTAINERS = Set.of(RscpTag.EMS_GET_POWER_SETTINGS,
            RscpTag.EMS_SET_POWER_SETTINGS);

    private final Map<Integer, RscpValue> powerSettings = new LinkedHashMap<>();

    /** Remembers the device state so a later write can complete the parameters it does not change. */
    public synchronized void updateFromResponse(RscpValue response) {
        for (int container : POWER_SETTINGS_CONTAINERS) {
            RscpValue settings = response.find(container);
            if (settings != null) {
                remember(settings);
            }
        }
    }

    /** Takes the parameters of a settings container into the snapshot as the typed items the device sent. */
    private void remember(RscpValue settings) {
        for (int tag : E3DCCommandSpec.POWER_SETTINGS_PARAMETERS) {
            RscpValue parameter = settings.find(tag);
            // an error item carries a value of its own; writing it back would fail, so it is no setting
            if (parameter != null && !parameter.isError() && parameter.getValue() != null) {
                powerSettings.put(tag, parameter);
            }
        }
    }

    /** Builds the request of a converted command, sends it and applies the acknowledgement as one transaction. */
    public synchronized boolean execute(E3DCBridgeHandler bridge, E3DCCommandSpec spec, RscpValue value,
            int deviceIndex) {
        List<RscpValue> request = buildRequest(spec, value, deviceIndex);
        return request != null && send(bridge, request);
    }

    /** Builds the request for a command on a settable channel, or {@code null} when it cannot be represented. */
    synchronized @Nullable List<RscpValue> buildRequest(E3DCCommandSpec spec, Command command, int deviceIndex) {
        RscpValue value = spec.toValue(command);
        return value == null ? null : buildRequest(spec, value, deviceIndex);
    }

    private @Nullable List<RscpValue> buildRequest(E3DCCommandSpec spec, RscpValue value, int deviceIndex) {
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
        if (spec.container() == RscpTag.SE_REQ_SET_EP_RESERVE) {
            // the reserve addresses no device, but the device rejects a write without its int32 data set index
            items.add(RscpValue.int32(RscpTag.SE_PARAM_INDEX, 0));
        }
        items.add(value);
        return List.of(RscpValue.container(spec.container(), items));
    }

    /** The complete settings container; parameters never read are left out rather than written as zero. */
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
    public synchronized boolean send(E3DCBridgeHandler bridge, List<RscpValue> request) {
        try {
            if (logger.isDebugEnabled()) {
                logger.trace("Sending command: {}", RscpValue.container(0, request).describeTree());
            }
            RscpValue response = bridge.sendRequest(request);
            if (logger.isDebugEnabled()) {
                logger.trace("Command answered with: {}", response.describeTree());
            }
            if (response.containsError()) {
                logger.debug("The device rejected the command: {}", response.findError());
                return false;
            }
            // the acknowledged write is the new device state, so a later write must not undo it
            rememberAcknowledged(request);
            updateFromResponse(response);
            return true;
        } catch (RscpException e) {
            logger.debug("Cannot send the command: {}", e.getMessage());
            return false;
        }
    }

    /** Remembers the values a write carried, which the device accepted. */
    void rememberAcknowledged(List<RscpValue> request) {
        for (RscpValue item : request) {
            if (item.getTag() == RscpTag.EMS_REQ_SET_POWER_SETTINGS) {
                remember(item);
            }
        }
    }

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
