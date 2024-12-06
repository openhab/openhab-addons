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
package org.openhab.binding.homewizard.internal.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homewizard.internal.dto.StatePayload;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * The {@link HomeWizardStatefulDeviceHandler} extends the base class
 * to provide support for devices that also have a 'state' interface.
 * This interface can be used to query and control the state of a device.
 *
 * @author Daniël van Os - Initial contribution
 */
@NonNullByDefault
public abstract class HomeWizardStatefulDeviceHandler extends HomeWizardP1MeterHandler {

    /**
     * Constructor
     *
     * @param thing The thing to handle
     * @param timeZoneProvider The TimeZoneProvider
     */
    public HomeWizardStatefulDeviceHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    /**
     * Device specific handling of the returned state payload.
     *
     * @param payload The data parsed from the state Json file
     */
    protected abstract void handleStatePayload(StatePayload payload);

    protected void pollState() {
        final String stateResult;

        try {
            stateResult = HttpUtil.executeUrl("GET", apiURL + "state", 30000);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Unable to query device state: %s", e.getMessage()));
            return;
        }

        if (stateResult.trim().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device returned empty state");
            return;
        }

        StatePayload statePayload = gson.fromJson(stateResult, StatePayload.class);
        if (statePayload == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to parse state response from device");
            return;
        }

        handleStatePayload(statePayload);
    }

    /**
     * Sends a command to the state interface of the device.
     *
     * @param command The command to send.
     */
    protected @Nullable StatePayload sendStateCommand(String command) {
        try (InputStream is = new ByteArrayInputStream(command.getBytes())) {
            String updatedState = HttpUtil.executeUrl("PUT", apiURL + "state", is, "application/json", 30000);
            return gson.fromJson(updatedState, StatePayload.class);
        } catch (IOException e) {
            logger.warn("Failed to send command {} to {}", command, apiURL + "state");
            return null;
        }
    }

    /*
     * This overrides the original polling loop by including a request for the current state..
     */
    @Override
    protected void pollingCode() {
        pollData();
        pollState();
    }
}
