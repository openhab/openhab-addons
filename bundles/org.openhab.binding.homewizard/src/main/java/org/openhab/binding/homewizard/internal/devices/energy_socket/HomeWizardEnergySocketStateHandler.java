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
package org.openhab.binding.homewizard.internal.devices.energy_socket;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homewizard.internal.devices.HomeWizardEnergyMeterHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * The {@link HomeWizardEnergySocketStateHandler} extends the base class
 * to provide support for devices that also have a 'state' interface.
 * This interface can be used to query and control the state of a device.
 *
 * @author Daniël van Os - Initial contribution
 * @author Gearrel Welvaart - Several minor tweaks
 *
 */
@NonNullByDefault
public abstract class HomeWizardEnergySocketStateHandler extends HomeWizardEnergyMeterHandler {

    private final String STATE_URL = "v1/state";

    /**
     * Constructor
     *
     * @param thing The thing to handle
     *
     */
    public HomeWizardEnergySocketStateHandler(Thing thing) {
        super(thing);
    }

    /**
     * Device specific handling of the returned state payload.
     *
     * @param payload The data parsed from the state Json file
     */
    protected abstract void handleStatePayload(HomeWizardEnergySocketStatePayload payload);

    /**
     * @return json response from the state api
     * @throws Exception
     */
    public String getStateData() throws Exception {
        return getResponseFrom(apiURL + STATE_URL).getContentAsString();
    }

    protected void pollState() {
        final String stateResult;

        try {
            stateResult = getStateData();
        } catch (Exception ex) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Unable to query device state: %s", ex.getMessage()));
            return;
        }

        if (stateResult.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device returned empty state");
            return;
        }

        HomeWizardEnergySocketStatePayload statePayload = gson.fromJson(stateResult,
                HomeWizardEnergySocketStatePayload.class);
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
    protected @Nullable HomeWizardEnergySocketStatePayload sendStateCommand(String command) {
        String updatedState = "";
        try {
            updatedState = putDataTo(apiURL + STATE_URL, command).getContentAsString();
        } catch (Exception ex) {
            logger.warn("Failed to send command {} to {}", command, apiURL + STATE_URL);
        }

        return gson.fromJson(updatedState, HomeWizardEnergySocketStatePayload.class);
    }

    /*
     * This overrides the original polling loop by including a request for the current state..
     */
    @Override
    protected void retrieveData() {
        retrieveMeasurementData();
        pollState();
    }
}
