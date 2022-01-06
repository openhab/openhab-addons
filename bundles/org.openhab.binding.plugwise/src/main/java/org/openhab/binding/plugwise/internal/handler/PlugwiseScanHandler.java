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
package org.openhab.binding.plugwise.internal.handler;

import static org.openhab.binding.plugwise.internal.PlugwiseBindingConstants.*;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plugwise.internal.config.PlugwiseScanConfig;
import org.openhab.binding.plugwise.internal.protocol.AcknowledgementMessage;
import org.openhab.binding.plugwise.internal.protocol.LightCalibrationRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.ScanParametersSetRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.SleepSetRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.field.DeviceType;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The {@link PlugwiseScanHandler} handles channel updates and commands for a Plugwise Scan device.
 * </p>
 * <p>
 * The Scan is a wireless PIR sensor that switches on groups of devices depending on the amount of daylight and whether
 * motion is detected. When the daylight override setting is enabled on a Scan, the state of triggered behaves like that
 * of a normal motion sensor.
 * </p>
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class PlugwiseScanHandler extends AbstractSleepingEndDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(PlugwiseScanHandler.class);
    private final DeviceType deviceType = DeviceType.SCAN;

    private @NonNullByDefault({}) PlugwiseScanConfig configuration;
    private @NonNullByDefault({}) MACAddress macAddress;

    // Flags that keep track of the pending Scan configuration updates. When the corresponding Thing configuration
    // parameters change a flag is set to true. When the Scan goes online the respective command is sent to update the
    // device configuration. When the Scan acknowledges a command the respective flag is again set to false.
    private boolean updateScanParameters;
    private boolean updateSleepParameters;
    private boolean recalibrate;

    public PlugwiseScanHandler(Thing thing) {
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

        switch (message.getExtensionCode()) {
            case LIGHT_CALIBRATION_ACK:
                logger.debug("Received ACK for daylight override calibration of {} ({})", deviceType, macAddress);
                recalibrate = false;
                Configuration configuration = editConfiguration();
                configuration.put(CONFIG_PROPERTY_RECALIBRATE, Boolean.FALSE);
                updateConfiguration(configuration);
                break;
            case SCAN_PARAMETERS_SET_ACK:
                logger.debug("Received ACK for parameters set of {} ({})", deviceType, macAddress);
                updateScanParameters = false;
                break;
            case SCAN_PARAMETERS_SET_NACK:
                logger.debug("Received NACK for parameters set of {} ({})", deviceType, macAddress);
                break;
            case SLEEP_SET_ACK:
                logger.debug("Received ACK for sleep set of {} ({})", deviceType, macAddress);
                updateSleepParameters = false;
                break;
            default:
                logger.trace("Received unhandled {} message from {} ({})", message.getType(), deviceType, macAddress);
                break;
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
    public void initialize() {
        configuration = getConfigAs(PlugwiseScanConfig.class);
        macAddress = configuration.getMACAddress();
        if (!isInitialized()) {
            setUpdateCommandFlags(null, configuration);
        }
        super.initialize();
    }

    @Override
    protected boolean isConfigurationPending() {
        return updateScanParameters || updateSleepParameters || recalibrate;
    }

    @Override
    protected void sendConfigurationUpdateCommands() {
        logger.debug("Sending {} ({}) configuration update commands", deviceType, macAddress);

        if (updateScanParameters) {
            logger.debug("Sending command to update {} ({}) parameters", deviceType, macAddress);
            sendCommandMessage(new ScanParametersSetRequestMessage(macAddress, configuration.getSensitivity(),
                    configuration.isDaylightOverride(), configuration.getSwitchOffDelay()));
        }
        if (updateSleepParameters) {
            logger.debug("Sending command to update {} ({}) sleep parameters", deviceType, macAddress);
            sendCommandMessage(new SleepSetRequestMessage(macAddress, configuration.getWakeupDuration(),
                    configuration.getWakeupInterval()));
        }
        if (recalibrate) {
            logger.debug("Sending command to recalibrate {} ({}) daylight override", deviceType, macAddress);
            sendCommandMessage(new LightCalibrationRequestMessage(macAddress));
        }

        super.sendConfigurationUpdateCommands();
    }

    private void setUpdateCommandFlags(@Nullable PlugwiseScanConfig oldConfiguration,
            PlugwiseScanConfig newConfiguration) {
        boolean fullUpdate = newConfiguration.isUpdateConfiguration() && !isConfigurationPending();
        if (fullUpdate) {
            logger.debug("Updating all configuration properties of {} ({})", deviceType, macAddress);
        }

        updateScanParameters = fullUpdate
                || (oldConfiguration != null && !oldConfiguration.equalScanParameters(newConfiguration));
        if (updateScanParameters) {
            logger.debug("Updating {} ({}) parameters when online", deviceType, macAddress);
        }

        updateSleepParameters = fullUpdate
                || (oldConfiguration != null && !oldConfiguration.equalSleepParameters(newConfiguration));
        if (updateSleepParameters) {
            logger.debug("Updating {} ({}) sleep parameters when online", deviceType, macAddress);
        }

        recalibrate = fullUpdate || newConfiguration.isRecalibrate();
        if (recalibrate) {
            logger.debug("Recalibrating {} ({}) daylight override when online", deviceType, macAddress);
        }
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        PlugwiseScanConfig oldConfiguration = this.configuration;
        PlugwiseScanConfig newConfiguration = configuration.as(PlugwiseScanConfig.class);

        setUpdateCommandFlags(oldConfiguration, newConfiguration);

        configuration.put(CONFIG_PROPERTY_UPDATE_CONFIGURATION, isConfigurationPending());

        super.updateConfiguration(configuration);
    }
}
