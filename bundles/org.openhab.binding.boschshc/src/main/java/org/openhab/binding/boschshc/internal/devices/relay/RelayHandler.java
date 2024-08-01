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
package org.openhab.binding.boschshc.internal.devices.relay;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_CHILD_PROTECTION;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_IMPULSE_LENGTH;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_IMPULSE_SWITCH;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_INSTANT_OF_LAST_IMPULSE;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_POWER_SWITCH;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_SIGNAL_STRENGTH;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Provider;
import javax.measure.Unit;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.AbstractPowerSwitchHandler;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Device;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.childprotection.ChildProtectionService;
import org.openhab.binding.boschshc.internal.services.childprotection.dto.ChildProtectionServiceState;
import org.openhab.binding.boschshc.internal.services.communicationquality.CommunicationQualityService;
import org.openhab.binding.boschshc.internal.services.communicationquality.dto.CommunicationQualityServiceState;
import org.openhab.binding.boschshc.internal.services.impulseswitch.ImpulseSwitchService;
import org.openhab.binding.boschshc.internal.services.impulseswitch.dto.ImpulseSwitchServiceState;
import org.openhab.binding.boschshc.internal.services.powerswitch.PowerSwitchService;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for smart relays.
 * <p>
 * Relays are in one of two possible modes:
 * <ul>
 * <li>Power switch mode: a switch is used to toggle the relay on / off</li>
 * <li>Impulse switch: the relay is triggered by an impulse and automatically
 * switches off after a configured period of time</li>
 * </ul>
 * <p>
 * Every time the thing is initialized, we detect dynamically which mode was
 * configured for the relay and reconfigure the channels accordingly, if
 * required.
 * <p>
 * In common usage scenarios, this will be the case upon the very first
 * initialization only, or if the device is re-purposed.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class RelayHandler extends AbstractPowerSwitchHandler {

    private final Logger logger = LoggerFactory.getLogger(RelayHandler.class);

    protected static final String PROPERTY_MODE = "mode";

    /**
     * Unit for the impulse length, which is specified in deciseconds (tenth seconds)
     */
    private static final Unit<Time> UNIT_DECISECOND = MetricPrefix.DECI(Units.SECOND);

    private ChildProtectionService childProtectionService;
    private ImpulseSwitchService impulseSwitchService;

    /**
     * Indicates whether the relay is configured in impulse switch mode. If this is
     * <code>false</code>, the relay is in the default power switch (toggle) mode
     */
    private boolean isInImpulseSwitchMode;

    /**
     * A provider for the current date/time.
     * <p>
     * It is exchanged in unit tests in order to be able to assert that a certain
     * date is contained in the result.
     */
    private Provider<Instant> currentDateTimeProvider = Instant::now;

    @Nullable
    private ImpulseSwitchServiceState currentImpulseSwitchServiceState;

    public RelayHandler(Thing thing) {
        super(thing);
        this.childProtectionService = new ChildProtectionService();
        this.impulseSwitchService = new ImpulseSwitchService();
    }

    @Override
    protected boolean processDeviceInfo(Device deviceInfo) {
        this.isInImpulseSwitchMode = isRelayInImpulseSwitchMode(deviceInfo);
        boolean isChannelConfigurationValid = configureChannels();
        if (!isChannelConfigurationValid) {
            return false;
        }

        updateModePropertyIfApplicable();
        return super.processDeviceInfo(deviceInfo);
    }

    private void updateModePropertyIfApplicable() {
        String modePropertyValue = isInImpulseSwitchMode ? ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME
                : PowerSwitchService.POWER_SWITCH_SERVICE_NAME;
        updateProperty(PROPERTY_MODE, modePropertyValue);
    }

    /**
     * Dynamically configures the channels according to the device mode.
     * <p>
     * Two configurations are possible:
     * 
     * <ul>
     * <li>Power Switch Mode (relay stays on indefinitely when switched on)</li>
     * <li>Impulse Switch Mode (relay stays on for a configured amount of time and
     * then switches off automatically)</li>
     * </ul>
     */
    private boolean configureChannels() {
        return isInImpulseSwitchMode ? configureImpulseSwitchModeChannels() : configurePowerSwitchModeChannels();
    }

    private boolean configureImpulseSwitchModeChannels() {
        List<String> channelsToBePresent = List.of(CHANNEL_IMPULSE_SWITCH, CHANNEL_IMPULSE_LENGTH,
                CHANNEL_INSTANT_OF_LAST_IMPULSE);
        List<String> channelsToBeAbsent = List.of(CHANNEL_POWER_SWITCH);
        return configureChannels(channelsToBePresent, channelsToBeAbsent);
    }

    private boolean configurePowerSwitchModeChannels() {
        List<String> channelsToBePresent = List.of(CHANNEL_POWER_SWITCH);
        List<String> channelsToBeAbsent = List.of(CHANNEL_IMPULSE_SWITCH, CHANNEL_IMPULSE_LENGTH,
                CHANNEL_INSTANT_OF_LAST_IMPULSE);
        return configureChannels(channelsToBePresent, channelsToBeAbsent);
    }

    /**
     * Re-configures the channels of the associated thing, if applicable.
     * 
     * @param channelsToBePresent channels expected to be present according to the current device mode
     * @param channelsToBeAbsent channels to be removed, if present
     * 
     * @return <code>true</code> if the channels were reconfigured or no re-configuration is necessary,
     *         <code>false</code> if the thing has to be re-created manually
     */
    private boolean configureChannels(List<String> channelsToBePresent, List<String> channelsToBeAbsent) {
        Optional<String> anyChannelMissing = channelsToBePresent.stream().filter(c -> getThing().getChannel(c) == null)
                .findAny();

        if (anyChannelMissing.isPresent()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.relay-recreation-required");
            return false;
        }

        List<Channel> channelsToRemove = channelsToBeAbsent.stream().map(c -> getThing().getChannel(c))
                .filter(Objects::nonNull).map(Objects::requireNonNull).toList();

        if (channelsToRemove.isEmpty()) {
            return true;
        }

        ThingBuilder thingBuilder = editThing();
        thingBuilder.withoutChannels(channelsToRemove);
        updateThing(thingBuilder.build());
        return true;
    }

    private boolean isRelayInImpulseSwitchMode(Device deviceInfo) {
        List<String> serviceIds = deviceInfo.deviceServiceIds;
        return serviceIds != null && serviceIds.contains(ImpulseSwitchService.IMPULSE_SWITCH_SERVICE_NAME);
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        if (!isInImpulseSwitchMode) {
            // initialize PowerSwitch service only if the relay is not configured as impulse
            // switch
            super.initializeServices();
        } else {
            // initialize impulse switch service only if the relay is configured as impulse
            // switch
            registerService(impulseSwitchService, this::updateChannels,
                    List.of(CHANNEL_IMPULSE_SWITCH, CHANNEL_IMPULSE_LENGTH, CHANNEL_INSTANT_OF_LAST_IMPULSE), true);
        }

        createService(CommunicationQualityService::new, this::updateChannels, List.of(CHANNEL_SIGNAL_STRENGTH), true);
        registerService(childProtectionService, this::updateChannels, List.of(CHANNEL_CHILD_PROTECTION), true);
    }

    private void updateChannels(CommunicationQualityServiceState communicationQualityServiceState) {
        updateState(CHANNEL_SIGNAL_STRENGTH, communicationQualityServiceState.quality.toSystemSignalStrength());
    }

    private void updateChannels(ChildProtectionServiceState childProtectionServiceState) {
        updateState(CHANNEL_CHILD_PROTECTION, OnOffType.from(childProtectionServiceState.childLockActive));
    }

    private void updateChannels(ImpulseSwitchServiceState impulseSwitchServiceState) {
        this.currentImpulseSwitchServiceState = impulseSwitchServiceState;

        updateState(CHANNEL_IMPULSE_SWITCH, OnOffType.from(impulseSwitchServiceState.impulseState));
        updateState(CHANNEL_IMPULSE_LENGTH, new DecimalType(impulseSwitchServiceState.impulseLength));

        State newInstantOfLastImpulse = impulseSwitchServiceState.instantOfLastImpulse != null
                ? new DateTimeType(impulseSwitchServiceState.instantOfLastImpulse)
                : UnDefType.NULL;
        updateState(CHANNEL_INSTANT_OF_LAST_IMPULSE, newInstantOfLastImpulse);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (CHANNEL_CHILD_PROTECTION.equals(channelUID.getId()) && (command instanceof OnOffType onOffCommand)) {
            updateChildProtectionState(onOffCommand);
        } else if (CHANNEL_IMPULSE_SWITCH.equals(channelUID.getId()) && command instanceof OnOffType onOffCommand) {
            triggerImpulse(onOffCommand);
        } else if (CHANNEL_IMPULSE_LENGTH.equals(channelUID.getId())) {
            updateImpulseLength(command);
        }
    }

    private void updateChildProtectionState(OnOffType onOffCommand) {
        ChildProtectionServiceState childProtectionServiceState = new ChildProtectionServiceState();
        childProtectionServiceState.childLockActive = onOffCommand == OnOffType.ON;
        updateServiceState(childProtectionService, childProtectionServiceState);
    }

    private void triggerImpulse(OnOffType onOffCommand) {
        if (onOffCommand != OnOffType.ON) {
            return;
        }

        ImpulseSwitchServiceState newState = cloneCurrentImpulseSwitchServiceState();
        if (newState != null) {
            newState.impulseState = true;
            newState.instantOfLastImpulse = currentDateTimeProvider.get().toString();
            this.currentImpulseSwitchServiceState = newState;
            updateServiceState(impulseSwitchService, newState);
        }
    }

    private void updateImpulseLength(Command command) {
        Integer impulseLength = getImpulseLength(command);
        if (impulseLength == null) {
            return;
        }

        ImpulseSwitchServiceState newState = cloneCurrentImpulseSwitchServiceState();
        if (newState != null) {
            newState.impulseLength = impulseLength;
            this.currentImpulseSwitchServiceState = newState;
            logger.debug("New impulse length setting for relay: {} deciseconds", newState.impulseLength);

            updateServiceState(impulseSwitchService, newState);
            logger.debug("Successfully sent state with new impulse length to controller.");
        }
    }

    private @Nullable Integer getImpulseLength(Command command) {
        if (command instanceof DecimalType decimalCommand) {
            return decimalCommand.intValue();
        } else if (command instanceof QuantityType<?> quantityCommand) {
            @Nullable
            QuantityType<?> convertedQuantity = quantityCommand.toUnit(UNIT_DECISECOND);
            return convertedQuantity != null ? convertedQuantity.intValue() : null;
        } else {
            return null;
        }
    }

    private @Nullable ImpulseSwitchServiceState cloneCurrentImpulseSwitchServiceState() {
        if (currentImpulseSwitchServiceState != null) {
            ImpulseSwitchServiceState clonedState = new ImpulseSwitchServiceState();
            clonedState.impulseState = currentImpulseSwitchServiceState.impulseState;
            clonedState.impulseLength = currentImpulseSwitchServiceState.impulseLength;
            clonedState.instantOfLastImpulse = currentImpulseSwitchServiceState.instantOfLastImpulse;
            return clonedState;
        } else {
            logger.warn("Could not obtain current impulse switch state, command will not be processed.");
        }
        return null;
    }

    void setCurrentDateTimeProvider(Provider<Instant> currentDateTimeProvider) {
        this.currentDateTimeProvider = currentDateTimeProvider;
    }
}
