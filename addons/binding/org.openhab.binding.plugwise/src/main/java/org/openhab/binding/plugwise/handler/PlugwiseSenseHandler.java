/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.handler;

import static org.openhab.binding.plugwise.PlugwiseBindingConstants.*;

import java.time.Duration;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.plugwise.internal.config.PlugwiseSenseConfig;
import org.openhab.binding.plugwise.internal.protocol.AcknowledgementMessage;
import org.openhab.binding.plugwise.internal.protocol.Message;
import org.openhab.binding.plugwise.internal.protocol.SenseBoundariesSetRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.SenseReportIntervalSetRequest;
import org.openhab.binding.plugwise.internal.protocol.SenseReportRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.SleepSetRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.field.BoundaryType;
import org.openhab.binding.plugwise.internal.protocol.field.DeviceType;
import org.openhab.binding.plugwise.internal.protocol.field.MACAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * The {@link PlugwiseSenseHandler} handles channel updates and commands for a Plugwise Sense device.
 * </p>
 * <p>
 * The Sense is a wireless temperature/humidity sensor that switches on groups of devices depending on the current
 * temperature or humidity level. It also periodically reports back the current temperature and humidity levels.
 * </p>
 *
 * @author Wouter Born - Initial contribution
 */
public class PlugwiseSenseHandler extends AbstractSleepingEndDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(PlugwiseSenseHandler.class);

    private PlugwiseSenseConfig configuration;
    private DeviceType deviceType = DeviceType.SENSE;
    private MACAddress macAddress;

    // Flags that keep track of the pending Sense configuration updates. When the corresponding Thing configuration
    // parameters change a flag is set to true. When the Sense goes online the respective command is sent to update the
    // device configuration. When the Sense acknowledges a command the respective flag is again set to false.
    private boolean updateBoundaryParameters;
    private boolean updateMeasurementInterval;
    private boolean updateSleepParameters;

    public PlugwiseSenseHandler(Thing thing) {
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
            case SENSE_BOUNDARIES_SET_ACK:
                logger.debug("Received ACK for boundaries parameters set of {} ({})", deviceType, macAddress);
                updateBoundaryParameters = false;
                break;
            case SENSE_BOUNDARIES_SET_NACK:
                logger.debug("Received NACK for boundaries parameters set of {} ({})", deviceType, macAddress);
                break;
            case SENSE_INTERVAL_SET_ACK:
                logger.debug("Received ACK for measurement interval set of {} ({})", deviceType, macAddress);
                updateMeasurementInterval = false;
                break;
            case SENSE_INTERVAL_SET_NACK:
                logger.debug("Received NACK for measurement interval set of {} ({})", deviceType, macAddress);
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
    public void handleReponseMessage(Message message) {
        switch (message.getType()) {
            case SENSE_REPORT_REQUEST:
                handleSenseReportRequestMessage((SenseReportRequestMessage) message);
                break;
            default:
                super.handleReponseMessage(message);
                break;
        }
    }

    private void handleSenseReportRequestMessage(SenseReportRequestMessage message) {
        updateLastSeen();
        updateState(CHANNEL_HUMIDITY, new QuantityType<>(message.getHumidity().getValue(), SmartHomeUnits.PERCENT));
        updateState(CHANNEL_TEMPERATURE, new QuantityType<>(message.getTemperature().getValue(), SIUnits.CELSIUS));
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(PlugwiseSenseConfig.class);
        macAddress = configuration.getMACAddress();
        if (!isInitialized()) {
            setUpdateCommandFlags(null, configuration);
        }
        super.initialize();
    }

    @Override
    protected boolean isConfigurationPending() {
        return updateBoundaryParameters || updateMeasurementInterval || updateSleepParameters;
    }

    @Override
    protected void sendConfigurationUpdateCommands() {
        logger.debug("Sending {} ({}) configuration update commands", deviceType, macAddress);

        if (updateBoundaryParameters) {
            SenseBoundariesSetRequestMessage message;
            if (configuration.getBoundaryType() == BoundaryType.HUMIDITY) {
                message = new SenseBoundariesSetRequestMessage(macAddress, configuration.getHumidityBoundaryMin(),
                        configuration.getHumidityBoundaryMax(), configuration.getBoundaryAction());
            } else if (configuration.getBoundaryType() == BoundaryType.TEMPERATURE) {
                message = new SenseBoundariesSetRequestMessage(macAddress, configuration.getTemperatureBoundaryMin(),
                        configuration.getTemperatureBoundaryMax(), configuration.getBoundaryAction());
            } else {
                message = new SenseBoundariesSetRequestMessage(macAddress);
            }

            logger.debug("Sending command to update {} ({}) boundary parameters", deviceType, macAddress);
            sendCommandMessage(message);
        }
        if (updateMeasurementInterval) {
            logger.debug("Sending command to update {} ({}) measurement interval", deviceType, macAddress);
            sendCommandMessage(new SenseReportIntervalSetRequest(macAddress, configuration.getMeasurementInterval()));
        }
        if (updateSleepParameters) {
            logger.debug("Sending command to update {} ({}) sleep parameters", deviceType, macAddress);
            sendCommandMessage(new SleepSetRequestMessage(macAddress, configuration.getWakeupDuration(),
                    configuration.getWakeupInterval()));
        }

        super.sendConfigurationUpdateCommands();
    }

    private void setUpdateCommandFlags(PlugwiseSenseConfig oldConfiguration, PlugwiseSenseConfig newConfiguration) {
        boolean fullUpdate = newConfiguration.isUpdateConfiguration() && !isConfigurationPending();
        if (fullUpdate) {
            logger.debug("Updating all configuration properties of {} ({})", deviceType, macAddress);
        }

        updateBoundaryParameters = fullUpdate
                || (oldConfiguration != null && !oldConfiguration.equalBoundaryParameters(newConfiguration));
        if (updateBoundaryParameters) {
            logger.debug("Updating {} ({}) boundary parameters when online", deviceType, macAddress);
        }

        updateMeasurementInterval = fullUpdate || (oldConfiguration != null
                && !oldConfiguration.getMeasurementInterval().equals(newConfiguration.getMeasurementInterval()));
        if (updateMeasurementInterval) {
            logger.debug("Updating {} ({}) measurement interval when online", deviceType, macAddress);
        }

        updateSleepParameters = fullUpdate
                || (oldConfiguration != null && !oldConfiguration.equalSleepParameters(newConfiguration));
        if (updateSleepParameters) {
            logger.debug("Updating {} ({}) sleep parameters when online", deviceType, macAddress);
        }
    }

    @Override
    protected void updateConfiguration(Configuration configuration) {
        PlugwiseSenseConfig oldConfiguration = this.configuration;
        PlugwiseSenseConfig newConfiguration = configuration.as(PlugwiseSenseConfig.class);

        setUpdateCommandFlags(oldConfiguration, newConfiguration);
        configuration.put(CONFIG_PROPERTY_UPDATE_CONFIGURATION, isConfigurationPending());

        super.updateConfiguration(configuration);
    }

}
