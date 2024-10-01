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
package org.openhab.binding.boschshc.internal.devices.lightcontrol;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION_1;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION_2;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_ENERGY_CONSUMPTION;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_POWER_CONSUMPTION;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_POWER_SWITCH_1;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_POWER_SWITCH_2;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_SIGNAL_STRENGTH;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.BoschSHCDeviceHandler;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Device;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.childprotection.ChildProtectionService;
import org.openhab.binding.boschshc.internal.services.childprotection.dto.ChildProtectionServiceState;
import org.openhab.binding.boschshc.internal.services.communicationquality.CommunicationQualityService;
import org.openhab.binding.boschshc.internal.services.communicationquality.dto.CommunicationQualityServiceState;
import org.openhab.binding.boschshc.internal.services.powermeter.PowerMeterService;
import org.openhab.binding.boschshc.internal.services.powermeter.dto.PowerMeterServiceState;
import org.openhab.binding.boschshc.internal.services.powerswitch.PowerSwitchService;
import org.openhab.binding.boschshc.internal.services.powerswitch.PowerSwitchState;
import org.openhab.binding.boschshc.internal.services.powerswitch.dto.PowerSwitchServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * Handler for Light Control II devices.
 * <p>
 * This implementation handles both common channels and specific channels of the
 * two logical child devices.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class LightControl2Handler extends BoschSHCDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(LightControl2Handler.class);

    private @Nullable String childDeviceId1;
    private @Nullable String childDeviceId2;

    private PowerSwitchService lightSwitchCircuit1PowerSwitchService;
    private PowerSwitchService lightSwitchCircuit2PowerSwitchService;

    private ChildProtectionService lightSwitchCircuit1ChildProtectionService;
    private ChildProtectionService lightSwitchCircuit2ChildProtectionService;

    public LightControl2Handler(Thing thing) {
        super(thing);

        lightSwitchCircuit1PowerSwitchService = new PowerSwitchService();
        lightSwitchCircuit2PowerSwitchService = new PowerSwitchService();

        lightSwitchCircuit1ChildProtectionService = new ChildProtectionService();
        lightSwitchCircuit2ChildProtectionService = new ChildProtectionService();
    }

    @Override
    protected boolean processDeviceInfo(Device deviceInfo) {
        super.processDeviceInfo(deviceInfo);

        logger.debug("Initializing child devices of Light Control II, child device IDs from device info: {}",
                deviceInfo.childDeviceIds);

        if (deviceInfo.childDeviceIds == null || deviceInfo.childDeviceIds.size() != 2) {
            updateStatusChildDeviceIDsNotObtainable();
            return false;
        }

        List<String> childDeviceIds = new ArrayList<>(deviceInfo.childDeviceIds);
        // since we were not sure whether the child device ID order is always the same,
        // we ensure a deterministic order by sorting the child IDs
        // see https://github.com/openhab/openhab-addons/pull/16400#discussion_r1497762612
        Collections.sort(childDeviceIds);

        logger.trace("Child device IDs for Light Control II after sorting: {}", childDeviceIds);

        if (validateDeviceId(childDeviceIds.get(0)) == null || validateDeviceId(childDeviceIds.get(1)) == null) {
            updateStatusChildDeviceIDsNotObtainable();
            return false;
        }

        childDeviceId1 = childDeviceIds.get(0);
        childDeviceId2 = childDeviceIds.get(1);

        logger.debug("Child device IDs for Light Control II configured successfully.");
        return true;
    }

    private void updateStatusChildDeviceIDsNotObtainable() {
        super.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/offline.conf-error.child-device-ids-not-obtainable");
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        createService(CommunicationQualityService::new, this::updateChannels, List.of(CHANNEL_SIGNAL_STRENGTH), true);
        createService(PowerMeterService::new, this::updateChannels,
                List.of(CHANNEL_POWER_CONSUMPTION, CHANNEL_ENERGY_CONSUMPTION), true);

        // local variable required to ensure non-nullness, member can theoretically be modified
        String lChildDeviceId1 = childDeviceId1;
        if (lChildDeviceId1 == null) {
            throw new BoschSHCException("Child device ID 1 is not set for thing " + getThing().getUID());
        }

        // local variable required to ensure non-nullness, member can theoretically be modified
        String lChildDeviceId2 = childDeviceId2;
        if (lChildDeviceId2 == null) {
            throw new BoschSHCException("Child device ID 2 is not set for thing " + getThing().getUID());
        }

        lightSwitchCircuit1PowerSwitchService.initialize(getBridgeHandler(), lChildDeviceId1,
                state -> updatePowerSwitchChannel(state, CHANNEL_POWER_SWITCH_1));
        lightSwitchCircuit2PowerSwitchService.initialize(getBridgeHandler(), lChildDeviceId2,
                state -> updatePowerSwitchChannel(state, CHANNEL_POWER_SWITCH_2));

        lightSwitchCircuit1ChildProtectionService.initialize(getBridgeHandler(), lChildDeviceId1,
                state -> updateChildProtectionChannel(state, CHANNEL_CHILD_PROTECTION_1));
        lightSwitchCircuit2ChildProtectionService.initialize(getBridgeHandler(), lChildDeviceId2,
                state -> updateChildProtectionChannel(state, CHANNEL_CHILD_PROTECTION_2));
    }

    private void updateChannels(CommunicationQualityServiceState communicationQualityServiceState) {
        updateState(CHANNEL_SIGNAL_STRENGTH, communicationQualityServiceState.quality.toSystemSignalStrength());
    }

    /**
     * Updates the channels which are linked to the {@link PowerMeterService} of the
     * device.
     *
     * @param state Current state of {@link PowerMeterService}.
     */
    private void updateChannels(PowerMeterServiceState state) {
        super.updateState(CHANNEL_POWER_CONSUMPTION, new QuantityType<>(state.powerConsumption, Units.WATT));
        super.updateState(CHANNEL_ENERGY_CONSUMPTION, new QuantityType<>(state.energyConsumption, Units.WATT_HOUR));
    }

    @Override
    public void processChildUpdate(String childDeviceId, String serviceName, @Nullable JsonElement stateData) {
        super.processChildUpdate(childDeviceId, serviceName, stateData);

        if (PowerSwitchService.POWER_SWITCH_SERVICE_NAME.equals(serviceName)) {
            if (childDeviceId.equals(childDeviceId1)) {
                lightSwitchCircuit1PowerSwitchService.onStateUpdate(stateData);
            } else if (childDeviceId.equals(childDeviceId2)) {
                lightSwitchCircuit2PowerSwitchService.onStateUpdate(stateData);
            }
        } else if (ChildProtectionService.CHILD_PROTECTION_SERVICE_NAME.equals(serviceName)) {
            if (childDeviceId.equals(childDeviceId1)) {
                lightSwitchCircuit1ChildProtectionService.onStateUpdate(stateData);
            } else if (childDeviceId.equals(childDeviceId2)) {
                lightSwitchCircuit2ChildProtectionService.onStateUpdate(stateData);
            }
        }
    }

    /**
     * Updates the power switch channel for one of the child devices.
     *
     * @param state the new {@link PowerSwitchServiceState}
     * @param channelId the power switch channel ID associated with the child device
     */
    private void updatePowerSwitchChannel(PowerSwitchServiceState state, String channelId) {
        State powerState = OnOffType.from(state.switchState.toString());
        super.updateState(channelId, powerState);
    }

    /**
     * Updates the child protection channel for one of the child devices.
     * 
     * @param state the new {@link ChildProtectionServiceState}
     * @param channelId the child protection channel ID associated with the child
     *            device
     */
    private void updateChildProtectionChannel(ChildProtectionServiceState state, String channelId) {
        super.updateState(channelId, OnOffType.from(state.childLockActive));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (CHANNEL_POWER_SWITCH_1.equals(channelUID.getId()) && (command instanceof OnOffType onOffCommand)) {
            updatePowerSwitchState(onOffCommand, lightSwitchCircuit1PowerSwitchService);
        } else if (CHANNEL_POWER_SWITCH_2.equals(channelUID.getId()) && (command instanceof OnOffType onOffCommand)) {
            updatePowerSwitchState(onOffCommand, lightSwitchCircuit2PowerSwitchService);
        } else if (CHANNEL_CHILD_PROTECTION_1.equals(channelUID.getId())
                && (command instanceof OnOffType onOffCommand)) {
            updateChildProtectionState(onOffCommand, lightSwitchCircuit1ChildProtectionService);
        } else if (CHANNEL_CHILD_PROTECTION_2.equals(channelUID.getId())
                && (command instanceof OnOffType onOffCommand)) {
            updateChildProtectionState(onOffCommand, lightSwitchCircuit2ChildProtectionService);
        }
    }

    private void updatePowerSwitchState(OnOffType command, PowerSwitchService powerSwitchService) {
        PowerSwitchServiceState state = new PowerSwitchServiceState();
        state.switchState = PowerSwitchState.valueOf(command.toFullString());
        this.updateServiceState(powerSwitchService, state);
    }

    private void updateChildProtectionState(OnOffType onOffCommand, ChildProtectionService childProtectionService) {
        ChildProtectionServiceState childProtectionServiceState = new ChildProtectionServiceState();
        childProtectionServiceState.childLockActive = onOffCommand == OnOffType.ON;
        updateServiceState(childProtectionService, childProtectionServiceState);
    }
}
