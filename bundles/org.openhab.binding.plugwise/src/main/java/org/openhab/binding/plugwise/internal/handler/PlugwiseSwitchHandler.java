/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.plugwise.internal.handler;

import static org.openhab.binding.plugwise.internal.PlugwiseBindingConstants.*;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plugwise.internal.config.PlugwiseSwitchConfig;
import org.openhab.binding.plugwise.internal.protocol.AcknowledgementMessage;
import org.openhab.binding.plugwise.internal.protocol.AcknowledgementMessage.ExtensionCode;
import org.openhab.binding.plugwise.internal.protocol.BroadcastGroupSwitchResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.SleepSetRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.field.DeviceType;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The {@link PlugwiseSwitchHandler} handles channel updates and commands for a Plugwise Switch device.
 * </p>
 * <p>
 * The Switch is a mountable wireless switch with one or two buttons depending on what parts are in place. When one
 * button is used this corresponds to only using the left button.
 * </p>
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class PlugwiseSwitchHandler extends AbstractSleepingEndDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(PlugwiseSwitchHandler.class);
    private final DeviceType deviceType = DeviceType.SWITCH;

    private @NonNullByDefault({}) PlugwiseSwitchConfig configuration;
    private @NonNullByDefault({}) MACAddress macAddress;

    // Flag that keeps track of the pending "sleep parameters" Switch configuration update. When the corresponding
    // Thing configuration parameters change it is set to true. When the Switch goes online a command is sent to
    // update the device configuration. When the Switch acknowledges the command the flag is again set to false.
    private boolean updateSleepParameters;

    public PlugwiseSwitchHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected MACAddress getMACAddress() {
        return macAddress;
    }

    @Override
    protected Duration getWakeupDuration() {
        return configuration.getWakeupDuration();
    }

    @Override
    protected void handleAcknowledgement(AcknowledgementMessage message) {
        boolean oldConfigurationPending = isConfigurationPending();

        if (message.getExtensionCode() == ExtensionCode.SLEEP_SET_ACK) {
            logger.debug("Received ACK for sleep set of {} ({})", deviceType, macAddress);
            updateSleepParameters = false;
        }

        boolean newConfigurationPending = isConfigurationPending();

        if (oldConfigurationPending != newConfigurationPending && !newConfigurationPending) {
            Configuration newConfiguration = editConfiguration();
            newConfiguration.put(CONFIG_PROPERTY_UPDATE_CONFIGURATION, false);
            updateConfiguration(newConfiguration);
        }

        super.handleAcknowledgement(message);
    }

    @Override
    protected void handleBroadcastGroupSwitchResponseMessage(BroadcastGroupSwitchResponseMessage message) {
        if (message.getPortMask() == 1) {
            updateState(CHANNEL_LEFT_BUTTON_STATE, message.getPowerState() ? OnOffType.ON : OnOffType.OFF);
        } else if (message.getPortMask() == 2) {
            updateState(CHANNEL_RIGHT_BUTTON_STATE, message.getPowerState() ? OnOffType.ON : OnOffType.OFF);
        }
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(PlugwiseSwitchConfig.class);
        macAddress = configuration.getMACAddress();
        if (!isInitialized()) {
            setUpdateCommandFlags(null, configuration);
        }
        super.initialize();
    }

    @Override
    protected boolean isConfigurationPending() {
        return updateSleepParameters;
    }

    @Override
    protected void sendConfigurationUpdateCommands() {
        logger.debug("Sending {} ({}) configuration update commands", deviceType, macAddress);

        if (updateSleepParameters) {
            logger.debug("Sending command to update {} ({}) sleep parameters", deviceType, macAddress);
            sendCommandMessage(new SleepSetRequestMessage(macAddress, configuration.getWakeupDuration(),
                    configuration.getWakeupInterval()));
        }

        super.sendConfigurationUpdateCommands();
    }

    private void setUpdateCommandFlags(@Nullable PlugwiseSwitchConfig oldConfiguration,
            PlugwiseSwitchConfig newConfiguration) {
        boolean fullUpdate = newConfiguration.isUpdateConfiguration() && !isConfigurationPending();
        if (fullUpdate) {
            logger.debug("Updating all configuration properties of {} ({})", deviceType, macAddress);
        }

        updateSleepParameters = fullUpdate
                || (oldConfiguration != null && !oldConfiguration.equalSleepParameters(newConfiguration));
        if (updateSleepParameters) {
            logger.debug("Updating {} ({}) sleep parameters when online", deviceType, macAddress);
        }
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        PlugwiseSwitchConfig oldConfiguration = this.configuration;
        PlugwiseSwitchConfig newConfiguration = configuration.as(PlugwiseSwitchConfig.class);

        setUpdateCommandFlags(oldConfiguration, newConfiguration);
        configuration.put(CONFIG_PROPERTY_UPDATE_CONFIGURATION, isConfigurationPending());

        super.updateConfiguration(configuration);
    }
}
