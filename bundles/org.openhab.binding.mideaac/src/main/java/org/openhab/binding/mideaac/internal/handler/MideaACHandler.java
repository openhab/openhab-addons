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
package org.openhab.binding.mideaac.internal.handler;

import static org.openhab.binding.mideaac.internal.MideaACBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;
import javax.measure.spi.SystemOfUnits;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.mideaac.internal.MideaACConfiguration;
import org.openhab.binding.mideaac.internal.Utils;
import org.openhab.binding.mideaac.internal.discovery.DiscoveryHandler;
import org.openhab.binding.mideaac.internal.discovery.MideaACDiscoveryService;
import org.openhab.binding.mideaac.internal.dto.CloudDTO;
import org.openhab.binding.mideaac.internal.dto.CloudProviderDTO;
import org.openhab.binding.mideaac.internal.dto.CloudsDTO;
import org.openhab.binding.mideaac.internal.handler.CommandBase.FanSpeed;
import org.openhab.binding.mideaac.internal.handler.CommandBase.OperationalMode;
import org.openhab.binding.mideaac.internal.handler.CommandBase.SwingMode;
import org.openhab.binding.mideaac.internal.handler.Timer.TimeParser;
import org.openhab.binding.mideaac.internal.security.Decryption8370Result;
import org.openhab.binding.mideaac.internal.security.Security;
import org.openhab.binding.mideaac.internal.security.Security.MsgType;
import org.openhab.binding.mideaac.internal.security.TokenKey;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MideaACHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jacek Dobrowolski - Initial contribution
 * @author Justan Oldman - Last Response added
 * @author Bob Eckhoff - Longer Polls and OH developer guidelines
 * 
 */
@NonNullByDefault
public class MideaACHandler extends BaseThingHandler implements DiscoveryHandler {

    private final Logger logger = LoggerFactory.getLogger(MideaACHandler.class);

    private MideaACConfiguration config = new MideaACConfiguration();
    private Map<String, String> properties = new HashMap<>();

    // Initialize variables to allow the @NonNullByDefault check
    private String ipAddress = "";
    private String ipPort = "";
    private String deviceId = "";
    private int version = 3;

    /**
     * Create new nonnull cloud provider to start
     */
    public CloudProviderDTO cloudProvider = new CloudProviderDTO("", "", "", "", "", "", "", "");
    private Security security = new Security(cloudProvider);

    /**
     * Gets the users Cloud provider
     * 
     * @return cloud Provider
     */
    public CloudProviderDTO getCloudProvider() {
        return cloudProvider;
    }

    /**
     * Gets the Security class
     * 
     * @return security
     */
    public Security getSecurity() {
        return security;
    }

    /**
     * Gets the Device Version (2 or 3)
     * 
     * @return version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Set the device version
     * 
     * @param version device version
     */
    public void setVersion(int version) {
        this.version = version;
    }

    private static final StringType OPERATIONAL_MODE_OFF = new StringType("OFF");
    private static final StringType OPERATIONAL_MODE_AUTO = new StringType("AUTO");
    private static final StringType OPERATIONAL_MODE_COOL = new StringType("COOL");
    private static final StringType OPERATIONAL_MODE_DRY = new StringType("DRY");
    private static final StringType OPERATIONAL_MODE_HEAT = new StringType("HEAT");
    private static final StringType OPERATIONAL_MODE_FAN_ONLY = new StringType("FAN_ONLY");

    private static final StringType FAN_SPEED_OFF = new StringType("OFF");
    private static final StringType FAN_SPEED_SILENT = new StringType("SILENT");
    private static final StringType FAN_SPEED_LOW = new StringType("LOW");
    private static final StringType FAN_SPEED_MEDIUM = new StringType("MEDIUM");
    private static final StringType FAN_SPEED_HIGH = new StringType("HIGH");
    private static final StringType FAN_SPEED_FULL = new StringType("FULL");
    private static final StringType FAN_SPEED_AUTO = new StringType("AUTO");

    private static final StringType SWING_MODE_OFF = new StringType("OFF");
    private static final StringType SWING_MODE_VERTICAL = new StringType("VERTICAL");
    private static final StringType SWING_MODE_HORIZONTAL = new StringType("HORIZONTAL");
    private static final StringType SWING_MODE_BOTH = new StringType("BOTH");
    private CloudsDTO clouds;

    private ConnectionManager connectionManager;

    private final SystemOfUnits systemOfUnits;

    private final HttpClient httpClient;

    /**
     * Set to false when Set Command recieved to speed response
     */
    public boolean doPoll = true;

    /**
     * True allows one short retry after connection problem
     */
    public boolean retry = true;

    /**
     * Suppresses the connection message if was online before
     */
    public boolean connectionMessage = true;

    private ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    private Response getLastResponse() {
        return getConnectionManager().getLastResponse();
    }

    /**
     * Initial creation of the Midea AC Handler
     * 
     * @param thing thing name
     * @param unitProvider OH core unit provider
     * @param httpClient http Client
     * @param clouds cloud
     */
    public MideaACHandler(Thing thing, UnitProvider unitProvider, HttpClient httpClient, CloudsDTO clouds) {
        super(thing);
        this.thing = thing;
        this.systemOfUnits = unitProvider.getMeasurementSystem();
        this.httpClient = httpClient;
        this.clouds = clouds;
        connectionManager = new ConnectionManager(this);
    }

    /**
     * Returns Cloud Provider
     * 
     * @return clouds
     */
    public CloudsDTO getClouds() {
        return clouds;
    }

    protected boolean isImperial() {
        return systemOfUnits instanceof ImperialUnits ? true : false;
    }

    /**
     * This method handles the Channels that can be set (non-read only)
     * First the Routine polling is stopped so there is no conflict
     * Then connects and authorizes (if necessary) and returns here to
     * create the command set which is then sent to the device.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling channelUID {} with command {}", channelUID.getId(), command.toString());
        connectionManager.disconnect();
        getConnectionManager().cancelConnectionMonitorJob();

        /**
         * Alternate to routine polling; Use rule to refresh at the desired interval
         */
        if (command instanceof RefreshType) {
            connectionManager.connect();
            return;
        }

        /**
         * @param doPoll is set to skip poll after authorization and go directly
         *            to command set execution
         */
        doPoll = false;
        connectionManager.connect();

        if (channelUID.getId().equals(CHANNEL_POWER)) {
            handlePower(command);
        } else if (channelUID.getId().equals(CHANNEL_OPERATIONAL_MODE)) {
            handleOperationalMode(command);
        } else if (channelUID.getId().equals(CHANNEL_TARGET_TEMPERATURE)) {
            handleTargetTemperature(command);
        } else if (channelUID.getId().equals(CHANNEL_FAN_SPEED)) {
            handleFanSpeed(command);
        } else if (channelUID.getId().equals(CHANNEL_ECO_MODE)) {
            handleEcoMode(command);
        } else if (channelUID.getId().equals(CHANNEL_TURBO_MODE)) {
            handleTurboMode(command);
        } else if (channelUID.getId().equals(CHANNEL_SWING_MODE)) {
            handleSwingMode(command);
        } else if (channelUID.getId().equals(CHANNEL_SCREEN_DISPLAY)) {
            handleScreenDisplay(command);
        } else if (channelUID.getId().equals(CHANNEL_TEMPERATURE_UNIT)) {
            handleTempUnit(command);
        } else if (channelUID.getId().equals(CHANNEL_SLEEP_FUNCTION)) {
            handleSleepFunction(command);
        } else if (channelUID.getId().equals(CHANNEL_ON_TIMER)) {
            handleOnTimer(command);
        } else if (channelUID.getId().equals(CHANNEL_OFF_TIMER)) {
            handleOffTimer(command);
        }
    }

    /**
     * Device Power ON OFF
     * 
     * @param command On or Off
     */
    public void handlePower(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        if (command.equals(OnOffType.OFF)) {
            commandSet.setPowerState(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setPowerState(true);
        } else {
            logger.debug("Unknown power state command: {}", command);
            return;
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    /**
     * Supported AC - Heat Pump modes
     * 
     * @param command Operational Mode Cool, Heat, etc.
     */
    public void handleOperationalMode(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        if (command instanceof StringType) {
            if (command.equals(OPERATIONAL_MODE_OFF)) {
                commandSet.setPowerState(false);
                return;
            } else if (command.equals(OPERATIONAL_MODE_AUTO)) {
                commandSet.setOperationalMode(OperationalMode.AUTO);
            } else if (command.equals(OPERATIONAL_MODE_COOL)) {
                commandSet.setOperationalMode(OperationalMode.COOL);
            } else if (command.equals(OPERATIONAL_MODE_DRY)) {
                commandSet.setOperationalMode(OperationalMode.DRY);
            } else if (command.equals(OPERATIONAL_MODE_HEAT)) {
                commandSet.setOperationalMode(OperationalMode.HEAT);
            } else if (command.equals(OPERATIONAL_MODE_FAN_ONLY)) {
                commandSet.setOperationalMode(OperationalMode.FAN_ONLY);
            } else {
                logger.debug("Unknown operational mode command: {}", command);
                return;
            }
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    private static float convertTargetCelsiusTemperatureToInRange(float temperature) {
        if (temperature < 17.0f) {
            return 17.0f;
        }
        if (temperature > 30.0f) {
            return 30.0f;
        }

        return temperature;
    }

    /**
     * Device only uses Celsius in 0.5 degree increments
     * Fahrenheit is rounded to fit (example
     * setting to 64 F is 18 C but will result in 64.4 F display in OH)
     * The evaporator only displays 2 digits, so will show 64.
     * 
     * @param command Target Temperature
     */
    public void handleTargetTemperature(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        if (command instanceof DecimalType) {
            logger.debug("Handle Target Temperature as DecimalType in degrees C");
            commandSet.setTargetTemperature(
                    convertTargetCelsiusTemperatureToInRange(((DecimalType) command).floatValue()));
            getConnectionManager().sendCommandAndMonitor(commandSet);
        } else if (command instanceof QuantityType) {
            QuantityType<?> quantity = (QuantityType<?>) command;
            Unit<?> unit = quantity.getUnit();

            if (unit.equals(ImperialUnits.FAHRENHEIT) || unit.equals(SIUnits.CELSIUS)) {
                logger.debug("Handle Target Temperature with unit {} to degrees C", unit);
                if (unit.equals(SIUnits.CELSIUS)) {
                    commandSet.setTargetTemperature(convertTargetCelsiusTemperatureToInRange(quantity.floatValue()));
                } else {
                    QuantityType<?> celsiusQuantity = quantity.toUnit(SIUnits.CELSIUS);
                    if (celsiusQuantity != null) {
                        commandSet.setTargetTemperature(
                                convertTargetCelsiusTemperatureToInRange(celsiusQuantity.floatValue()));
                    } else {
                        logger.warn("Failed to convert quantity to Celsius unit.");
                    }
                }

                getConnectionManager().sendCommandAndMonitor(commandSet);
            }
        } else {
            logger.debug("Handle Target Temperature unsupported commandType:{}", command.getClass().getTypeName());
        }
    }

    /**
     * Fan Speeds vary by V2 or V3 and device. This command also turns the power ON
     * 
     * @param command Fan Speed Auto, Low, High, etc.
     */
    public void handleFanSpeed(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        if (command instanceof StringType) {
            commandSet.setPowerState(true);
            if (command.equals(FAN_SPEED_OFF)) {
                commandSet.setPowerState(false);
            } else if (command.equals(FAN_SPEED_SILENT)) {
                if (getVersion() == 2) {
                    commandSet.setFanSpeed(FanSpeed.SILENT2);
                } else if (getVersion() == 3) {
                    commandSet.setFanSpeed(FanSpeed.SILENT3);
                }
            } else if (command.equals(FAN_SPEED_LOW)) {
                if (getVersion() == 2) {
                    commandSet.setFanSpeed(FanSpeed.LOW2);
                } else if (getVersion() == 3) {
                    commandSet.setFanSpeed(FanSpeed.LOW3);
                }
            } else if (command.equals(FAN_SPEED_MEDIUM)) {
                if (getVersion() == 2) {
                    commandSet.setFanSpeed(FanSpeed.MEDIUM2);
                } else if (getVersion() == 3) {
                    commandSet.setFanSpeed(FanSpeed.MEDIUM3);
                }
            } else if (command.equals(FAN_SPEED_HIGH)) {
                if (getVersion() == 2) {
                    commandSet.setFanSpeed(FanSpeed.HIGH2);
                } else if (getVersion() == 3) {
                    commandSet.setFanSpeed(FanSpeed.HIGH3);
                }
            } else if (command.equals(FAN_SPEED_FULL)) {
                if (getVersion() == 2) {
                    commandSet.setFanSpeed(FanSpeed.FULL2);
                } else if (getVersion() == 3) {
                    commandSet.setFanSpeed(FanSpeed.FULL3);
                }
            } else if (command.equals(FAN_SPEED_AUTO)) {
                if (getVersion() == 2) {
                    commandSet.setFanSpeed(FanSpeed.AUTO2);
                } else if (getVersion() == 3) {
                    commandSet.setFanSpeed(FanSpeed.AUTO3);
                }
            } else {
                logger.debug("Unknown fan speed command: {}", command);
                return;
            }
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    /**
     * Must be set in Cool mode. Fan will switch to Auto
     * and temp will be 24 C or 75 F on unit (75.2 F in OH)
     * 
     * @param command Eco Mode
     */
    public void handleEcoMode(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        if (command.equals(OnOffType.OFF)) {
            commandSet.setEcoMode(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setEcoMode(true);
        } else {
            logger.debug("Unknown eco mode command: {}", command);
            return;
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    /**
     * Modes supported depends on the device
     * Power is turned on when swing mode is changed
     * 
     * @param command Swing Mode
     */
    public void handleSwingMode(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        commandSet.setPowerState(true);

        if (command instanceof StringType) {
            if (command.equals(SWING_MODE_OFF)) {
                if (getVersion() == 2) {
                    commandSet.setSwingMode(SwingMode.OFF2);
                } else if (getVersion() == 3) {
                    commandSet.setSwingMode(SwingMode.OFF3);
                }
            } else if (command.equals(SWING_MODE_VERTICAL)) {
                if (getVersion() == 2) {
                    commandSet.setSwingMode(SwingMode.VERTICAL2);
                } else if (getVersion() == 3) {
                    commandSet.setSwingMode(SwingMode.VERTICAL3);
                }
            } else if (command.equals(SWING_MODE_HORIZONTAL)) {
                if (getVersion() == 2) {
                    commandSet.setSwingMode(SwingMode.HORIZONTAL2);
                } else if (getVersion() == 3) {
                    commandSet.setSwingMode(SwingMode.HORIZONTAL3);
                }
            } else if (command.equals(SWING_MODE_BOTH)) {
                if (getVersion() == 2) {
                    commandSet.setSwingMode(SwingMode.BOTH2);
                } else if (getVersion() == 3) {
                    commandSet.setSwingMode(SwingMode.BOTH3);
                }
            } else {
                logger.debug("Unknown swing mode command: {}", command);
                return;
            }
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    /**
     * Turbo mode is only with Heat or Cool to quickly change
     * Room temperature. Power is turned on.
     * 
     * @param command Turbo mode - Fast cooling or Heating
     */
    public void handleTurboMode(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        commandSet.setPowerState(true);

        if (command.equals(OnOffType.OFF)) {
            commandSet.setTurboMode(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setTurboMode(true);
        } else {
            logger.debug("Unknown turbo mode command: {}", command);
            return;
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    /**
     * May not be supported via LAN in all models - IR only
     * 
     * @param command Screen Display Toggle to ON or Off - One command
     */
    public void handleScreenDisplay(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        if (command.equals(OnOffType.OFF)) {
            commandSet.setScreenDisplay(true);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setScreenDisplay(true);
        } else {
            logger.debug("Unknown screen display command: {}", command);
            return;
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    /**
     * This is only for the AC LED device display units, calcs always in Celsius
     * 
     * @param command Temp unit on the indoor evaporator
     */
    public void handleTempUnit(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        if (command.equals(OnOffType.OFF)) {
            commandSet.setFahrenheit(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setFahrenheit(true);
        } else {
            logger.debug("Unknown temperature unit/farenheit command: {}", command);
            return;
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    /**
     * Power turned on with Sleep Mode Change
     * Sleep mode increases temp slightly in first 2 hours of sleep
     * 
     * @param command Sleep function
     */
    public void handleSleepFunction(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());

        commandSet.setPowerState(true);

        if (command.equals(OnOffType.OFF)) {
            commandSet.setSleepMode(false);
        } else if (command.equals(OnOffType.ON)) {
            commandSet.setSleepMode(true);
        } else {
            logger.debug("Unknown sleep Mode command: {}", command);
            return;
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    /**
     * Sets the time (from now) that the device will turn on at it's current settings
     * 
     * @param command Sets On Timer
     */
    public void handleOnTimer(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());
        int hours = 0;
        int minutes = 0;
        Timer timer = new Timer(true, hours, minutes);
        TimeParser timeParser = timer.new TimeParser();
        if (command instanceof StringType) {
            String timeString = ((StringType) command).toString();
            if (!timeString.matches("\\d{2}:\\d{2}")) {
                logger.debug("Invalid time format. Expected HH:MM.");
                commandSet.setOnTimer(false, hours, minutes);
            } else {
                int[] timeParts = timeParser.parseTime(timeString);
                boolean on = true;
                hours = timeParts[0];
                minutes = timeParts[1];
                // Validate minutes and hours
                if (minutes < 0 || minutes > 59 || hours > 24 || hours < 0) {
                    logger.debug("Invalid hours (24 max) and or minutes (59 max)");
                    hours = 0;
                    minutes = 0;
                }
                if (hours == 0 && minutes == 0) {
                    commandSet.setOnTimer(false, hours, minutes);
                } else {
                    commandSet.setOnTimer(on, hours, minutes);
                }
            }
        } else {
            logger.debug("Command must be of type StringType: {}", command);
            commandSet.setOnTimer(false, hours, minutes);
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    /**
     * Sets the time (from now) that the device will turn off
     * 
     * @param command Sets Off Timer
     */
    public void handleOffTimer(Command command) {
        CommandSet commandSet = CommandSet.fromResponse(getLastResponse());
        int hours = 0;
        int minutes = 0;
        Timer timer = new Timer(true, hours, minutes);
        TimeParser timeParser = timer.new TimeParser();
        if (command instanceof StringType) {
            String timeString = ((StringType) command).toString();
            if (!timeString.matches("\\d{2}:\\d{2}")) {
                logger.debug("Invalid time format. Expected HH:MM.");
                commandSet.setOffTimer(false, hours, minutes);
            } else {
                int[] timeParts = timeParser.parseTime(timeString);
                boolean on = true;
                hours = timeParts[0];
                minutes = timeParts[1];
                // Validate minutes and hours
                if (minutes < 0 || minutes > 59 || hours > 24 || hours < 0) {
                    logger.debug("Invalid hours (24 max) and or minutes (59 max)");
                    hours = 0;
                    minutes = 0;
                }
                if (hours == 0 && minutes == 0) {
                    commandSet.setOffTimer(false, hours, minutes);
                } else {
                    commandSet.setOffTimer(on, hours, minutes);
                }
            }
        } else {
            logger.debug("Command must be of type StringType: {}", command);
            commandSet.setOffTimer(false, hours, minutes);
        }

        getConnectionManager().sendCommandAndMonitor(commandSet);
    }

    /**
     * Initialize is called on first pass or when a device parameter is changed
     * The basic check is if the information from Discovery (or the user update)
     * is valid. Because V2 devices do not require a cloud provider (or token/key)
     * The check is for the IP, port and deviceID. This method also resets the dropped
     * commands, disconnects the socket and stops the connection monitor (if these were
     * running)
     */
    @Override
    public void initialize() {
        connectionManager.disconnect();
        getConnectionManager().cancelConnectionMonitorJob();
        connectionManager.resetDroppedCommands();
        connectionManager.updateChannel(DROPPED_COMMANDS, new DecimalType(connectionManager.getDroppedCommands()));

        config = getConfigAs(MideaACConfiguration.class);

        setCloudProvider(CloudProviderDTO.getCloudProvider(config.cloud));
        setSecurity(new Security(cloudProvider));

        logger.debug("MideaACHandler config for {} is {}", thing.getUID(), config);

        if (!config.isValid()) {
            logger.warn("Configuration invalid for {}", thing.getUID());
            if (config.isDiscoveryNeeded()) {
                logger.warn("Discovery needed, discovering....{}", thing.getUID());
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING,
                        "Configuration missing, discovery needed. Discovering...");
                MideaACDiscoveryService discoveryService = new MideaACDiscoveryService();

                try {
                    discoveryService.discoverThing(config.ipAddress, this);
                } catch (Exception e) {
                    logger.error("Discovery failure for {}: {}", thing.getUID(), e.getMessage());
                }
                return;
            } else {
                logger.debug("MideaACHandler config of {} is invalid. Check configuration", thing.getUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Invalid MideaAC config. Check configuration.");
                return;
            }
        } else {
            logger.debug("Configuration valid for {}", thing.getUID());
        }

        ipAddress = config.ipAddress;
        ipPort = config.ipPort;
        deviceId = config.deviceId;
        version = Integer.parseInt(config.version);

        logger.debug("IPAddress: {}", ipAddress);
        logger.debug("IPPort: {}", ipPort);
        logger.debug("ID: {}", deviceId);
        logger.debug("Version: {}", version);

        updateStatus(ThingStatus.UNKNOWN);

        connectionManager.connect();
    }

    @Override
    public void discovered(DiscoveryResult discoveryResult) {
        logger.debug("Discovered {}", thing.getUID());
        Map<String, Object> discoveryProps = discoveryResult.getProperties();
        Configuration configuration = editConfiguration();

        Object propertyDeviceId = Objects.requireNonNull(discoveryProps.get(CONFIG_DEVICEID));
        configuration.put(CONFIG_DEVICEID, propertyDeviceId.toString());

        Object propertyIpPort = Objects.requireNonNull(discoveryProps.get(CONFIG_IP_PORT));
        configuration.put(CONFIG_IP_PORT, propertyIpPort.toString());

        updateConfiguration(configuration);

        properties = editProperties();

        Object propertyVersion = Objects.requireNonNull(discoveryProps.get(PROPERTY_VERSION));
        properties.put(PROPERTY_VERSION, propertyVersion.toString());

        Object propertySN = Objects.requireNonNull(discoveryProps.get(PROPERTY_SN));
        properties.put(PROPERTY_SN, propertySN.toString());

        Object propertySSID = Objects.requireNonNull(discoveryProps.get(PROPERTY_SSID));
        properties.put(PROPERTY_SSID, propertySSID.toString());

        Object propertyType = Objects.requireNonNull(discoveryProps.get(PROPERTY_TYPE));
        properties.put(PROPERTY_TYPE, propertyType.toString());

        updateProperties(properties);

        initialize();
    }

    /**
     * Manage the ONLINE/OFFLINE statuses of the thing with problems (or lack thereof)
     */
    private void markOnline() {
        if (!isOnline()) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void markOffline() {
        if (isOnline()) {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private void markOfflineWithMessage(ThingStatusDetail statusDetail, String statusMessage) {
        if (!isOffline()) {
            updateStatus(ThingStatus.OFFLINE, statusDetail, statusMessage);
        }

        /**
         * This is to space out the looping with a short (5 second) then long (30 second) pause(s).
         * Generally a WiFi issue triggers the offline. Could be a blip or something longer term
         * Only info log (Connection issue ..) prior to first long pause.
         */
        if (retry) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                logger.debug("An interupted error (pause) has occured {}", e.getMessage());
            }
            getConnectionManager().cancelConnectionMonitorJob();
            getConnectionManager().disconnect();
            retry = false;
            getConnectionManager().connect();
        } else {
            if (connectionMessage) {
                logger.info("Connection issue, resetting, please wait ...");
            }
            connectionMessage = false;
            getConnectionManager().cancelConnectionMonitorJob();
            getConnectionManager().disconnect();
            getConnectionManager().scheduleConnectionMonitorJob();
        }
    }

    private boolean isOnline() {
        return thing.getStatus().equals(ThingStatus.ONLINE);
    }

    private boolean isOffline() {
        return thing.getStatus().equals(ThingStatus.OFFLINE);
    }

    /**
     * Cancel the connection manager job which will keep going
     * even with the binding removed and cause warnings about
     * trying to update Thing Channels with the Handler disposed
     */
    @Override
    public void dispose() {
        connectionManager.cancelConnectionMonitorJob();
        markOffline();
    }

    /**
     * DoPoll is set to false in the MideaAC Handler
     * if a Command is being sent and picked up by
     * the Connection Manager. Then is reset to true
     * after the Set command is complete
     * 
     * @return doPoll Sets if the binding will poll after authorization
     */
    public boolean getDoPoll() {
        return doPoll;
    }

    /**
     * Resets the doPoll switch
     */
    public void resetDoPoll() {
        doPoll = true;
    }

    /**
     * Reset Retry controls the short 5 second delay
     * Before starting 30 second delays. (More severe Wifi issue)
     * It is reset after a successful connection
     */
    public void resetRetry() {
        retry = true;
    }

    /**
     * Limit logging of INFO connection messages to
     * only when the device was Offline in its prior
     * state
     */
    public void resetConnectionMessage() {
        connectionMessage = true;
    }

    private ThingStatusDetail getDetail() {
        return thing.getStatusInfo().getStatusDetail();
    }

    /**
     * Sets Cloud Provider
     * 
     * @param cloudProvider Cloud Provider
     */
    public void setCloudProvider(CloudProviderDTO cloudProvider) {
        this.cloudProvider = cloudProvider;
    }

    /**
     * Security methods
     * 
     * @param security security class
     */
    public void setSecurity(Security security) {
        this.security = security;
    }

    /**
     * The {@link ConnectionManager} class is responsible for managing the state of the TCP connection to the
     * indoor AC unit evaporator.
     *
     * @author Jacek Dobrowolski - Initial Contribution
     * @author Bob Eckhoff - Revised logic to reconnect with security before each poll or command
     * 
     *         This gets around the issue that any command needs to be within 30 seconds of the authorization
     *         in testing this only adds 50 ms, but allows polls at longer intervals
     */
    private class ConnectionManager {
        private Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

        private boolean deviceIsConnected;
        private int droppedCommands = 0;

        private Socket socket = new Socket();
        private InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        private DataOutputStream writer = new DataOutputStream(System.out);

        private @Nullable ScheduledFuture<?> connectionMonitorJob = null;

        private byte[] data = HexFormat.of().parseHex("C00042667F7F003C0000046066000000000000000000F9ECDB");

        private String responseType = "query";

        private byte bodyType = (byte) 0xc0;

        private Response lastResponse = new Response(data, getVersion(), responseType, bodyType);
        private MideaACHandler mideaACHandler;

        /**
         * Gets last response
         * 
         * @return byte array of last response
         */
        public Response getLastResponse() {
            return this.lastResponse;
        }

        Runnable connectionMonitorRunnable = () -> {
            logger.debug("Connecting to {} at IP {} for Poll", thing.getUID(), ipAddress);
            disconnect();
            connect();
        };

        /**
         * Set the parameters for the connection manager
         * 
         * @param mideaACHandler mideaACHandler class
         */
        public ConnectionManager(MideaACHandler mideaACHandler) {
            deviceIsConnected = false;
            this.mideaACHandler = mideaACHandler;
        }

        /**
         * Validate if String is blank
         * 
         * @param str string to be evaluated
         * @return boolean true or false
         */
        public static boolean isBlank(String str) {
            return str.trim().isEmpty();
        }

        /**
         * Reset dropped commands from initialization in MideaACHandler
         * Channel created for easy observation
         * Dropped commands when no bytes to read after two tries or other
         * byte reading problem. Device not responding.
         */
        public void resetDroppedCommands() {
            droppedCommands = 0;
        }

        /**
         * Resets Dropped command
         * 
         * @return dropped commands
         */
        public int getDroppedCommands() {
            return droppedCommands = 0;
        }

        /**
         * After checking if the key and token need to be updated (Default = 0 Never)
         * The socket is established with the writer and inputStream (for reading responses)
         * The device is considered connected. V2 devices will proceed to send the poll or the
         * set command. V3 devices will proceed to authenticate
         */
        protected synchronized void connect() {
            logger.trace("Connecting to {} at {}:{}", thing.getUID(), ipAddress, ipPort);

            // Open socket
            try {
                socket = new Socket();
                socket.setSoTimeout(config.timeout * 1000);
                int port = Integer.parseInt(ipPort);
                socket.connect(new InetSocketAddress(ipAddress, port), config.timeout * 1000);
            } catch (IOException e) {
                logger.debug("IOException connecting to  {} at {}: {}", thing.getUID(), ipAddress, e.getMessage());
                String message = e.getMessage();
                if (message != null) {
                    markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, message);
                } else {
                    markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "");
                }
            }

            // Create streams
            try {
                writer = new DataOutputStream(socket.getOutputStream());
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                logger.debug("IOException getting streams for {} at {}: {}", thing.getUID(), ipAddress, e.getMessage(),
                        e);
                String message = e.getMessage();
                if (message != null) {
                    markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, message);
                } else {
                    markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "");
                }
            }
            if (!deviceIsConnected || !connectionMessage) {
                logger.info("Connected to {} at {}", thing.getUID(), ipAddress);
                mideaACHandler.resetRetry();
                mideaACHandler.resetConnectionMessage();
            }
            logger.debug("Connected to {} at {}", thing.getUID(), ipAddress);
            deviceIsConnected = true;
            markOnline();
            if (getVersion() != 3) {
                logger.debug("Device {}@{} does not require authentication, updating status", thing.getUID(),
                        ipAddress);
                requestStatus(mideaACHandler.getDoPoll());
            } else {
                logger.debug("Device {}@{} require authentication, going to authenticate", thing.getUID(), ipAddress);
                authenticate();
            }
        }

        /**
         * For V3 devices only. This method checks for the Cloud Provider
         * key and token (and goes offline if any are missing). It will retrieve the
         * missing key and/or token if the account email and password are provided.
         */
        public void authenticate() {
            logger.trace("Version: {}", getVersion());
            logger.trace("Key: {}", config.key);
            logger.trace("Token: {}", config.token);

            if (!isBlank(config.token) && !isBlank(config.key) && !config.cloud.equals("")) {
                logger.debug("Device {}@{} authenticating", thing.getUID(), ipAddress);
                doAuthentication();
            } else {
                if (!isBlank(config.email) && !isBlank(config.password) && !config.cloud.equals("")) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "Retrieving Token and/or Key from cloud.");
                    logger.info("Retrieving Token and/or Key from cloud");
                    CloudProviderDTO cloudProvider = CloudProviderDTO.getCloudProvider(config.cloud);
                    getTokenKeyCloud(cloudProvider);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Token and/or Key missing, missing cloud provider information to fetch it");
                    logger.warn("Token, Key and or Cloud provider data missing, V3 device {}@{} cannot authenticate",
                            thing.getUID(), ipAddress);
                }
            }
        }

        private void getTokenKeyCloud(CloudProviderDTO cloudProvider) {
            CloudDTO cloud = mideaACHandler.getClouds().get(config.email, config.password, cloudProvider);
            if (cloud != null) {
                cloud.setHttpClient(httpClient);
                if (cloud.login()) {
                    TokenKey tk = cloud.getToken(config.deviceId);
                    Configuration configuration = editConfiguration();

                    configuration.put(CONFIG_TOKEN, tk.token());
                    configuration.put(CONFIG_KEY, tk.key());
                    updateConfiguration(configuration);

                    logger.trace("Token: {}", tk.token());
                    logger.trace("Key: {}", tk.key());
                    logger.info("Token and Key obtained from cloud, saving, initializing");
                    initialize();
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String.format(
                            "Can't retrieve Token and Key from Cloud; email, password and/or cloud parameter error"));
                    logger.warn(
                            "Can't retrieve Token and Key from Cloud; email, password and/or cloud parameter error");
                }
            }
        }

        /**
         * Sends the Handshake Request to the V3 device. Generally quick response
         * Without the 1000 ms sleep delay there are problems in sending the Poll/Command
         * Suspect that the socket write and read streams need a moment to clear
         * as they will be reused in the SendCommand method
         */
        private void doAuthentication() {
            byte[] request = mideaACHandler.getSecurity().encode8370(Utils.hexStringToByteArray(config.token),
                    MsgType.MSGTYPE_HANDSHAKE_REQUEST);
            try {
                logger.trace("Device {}@{} writing handshake_request: {}", thing.getUID(), ipAddress,
                        Utils.bytesToHex(request));

                write(request);
                byte[] response = read();

                if (response != null && response.length > 0) {
                    logger.trace("Device {}@{} response for handshake_request length: {}", thing.getUID(), ipAddress,
                            response.length);
                    if (response.length == 72) {
                        boolean success = mideaACHandler.getSecurity().tcpKey(Arrays.copyOfRange(response, 8, 72),
                                Utils.hexStringToByteArray(config.key));
                        if (success) {
                            logger.debug("Authentication successful");
                            // Altering the sleep caused or can cause write errors problems. Use caution.
                            // At 500 ms the first write usually fails. Works, but no backup
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                logger.debug("An interupted error (success) has occured {}", e.getMessage());
                            }
                            requestStatus(mideaACHandler.getDoPoll());
                        } else {
                            logger.debug("Invalid Key. Correct Key in configuration");
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                    "Invalid Key. Correct Key in configuration.");
                        }
                    } else if (Arrays.equals(new String("ERROR").getBytes(), response)) {
                        logger.warn("Authentication failed!");
                    } else {
                        logger.warn("Authentication reponse unexpected data length ({} instead of 72)!",
                                response.length);
                        logger.debug("Invalid Token. Correct Token in configuration");
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Invalid Token. Correct Token in configuration.");
                    }
                }
            } catch (IOException e) {
                logger.warn("An IO error in doAuthentication has occured {}", e.getMessage());
                String message = e.getMessage();
                if (message != null) {
                    markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, message);
                } else {
                    markOfflineWithMessage(ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "");
                }
            }
        }

        /**
         * After authentication, this switch to either send a
         * Poll or the Set command
         * 
         * @param polling polling true or false
         */
        public void requestStatus(boolean polling) {
            if (polling) {
                CommandBase requestStatusCommand = new CommandBase();
                sendCommandAndMonitor(requestStatusCommand);
            }
        }

        /**
         * Calls the sendCommand method, resets the doPoll to true
         * Disconnects the socket and schedules the connection manager
         * job, if was stopped (to avoid collision) due to a Set command
         * 
         * @param command either the set or polling command
         */
        public void sendCommandAndMonitor(CommandBase command) {
            sendCommand(command);
            mideaACHandler.resetDoPoll();
            if (connectionMonitorJob == null) {
                scheduleConnectionMonitorJob();
            }
        }

        /**
         * Pulls the packet byte array together. There is a check to
         * make sure to make sure the input stream is empty before sending
         * the new command and another check if input stream is empty after 1.5 seconds.
         * Normal device response in 0.75 - 1 second range
         * If still empty, send the bytes again. If there are bytes, the read method is called.
         * If the socket times out with no response the command is dropped. There will be another poll
         * in the time set by the user (30 seconds min) or the set command can be retried
         * 
         * @param command either the set or polling command
         */
        public void sendCommand(CommandBase command) {
            if (command instanceof CommandSet) {
                ((CommandSet) command).setPromptTone(config.promptTone);
            }
            Packet packet = new Packet(command, deviceId, mideaACHandler);
            packet.compose();

            try {
                byte[] bytes = packet.getBytes();
                logger.debug("Writing to {} at {} bytes.length: {}", thing.getUID(), ipAddress, bytes.length);

                if (getVersion() == 3) {
                    bytes = mideaACHandler.getSecurity().encode8370(bytes, MsgType.MSGTYPE_ENCRYPTED_REQUEST);
                }

                // Ensure input stream is empty before writing packet
                if (inputStream.available() == 0) {
                    logger.debug("Input stream empty sending write {}", command);
                    write(bytes);
                }

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    logger.debug("An interupted error (retrycommand2) has occured {}", e.getMessage());
                }

                if (inputStream.available() == 0) {
                    logger.debug("Input stream empty sending second write {}", command);
                    write(bytes);
                }

                // Socket timeout (UI parameter) 2 seconds minimum up to 10 seconds.
                byte[] responseBytes = read();

                if (responseBytes != null) {
                    if (getVersion() == 3) {
                        Decryption8370Result result = mideaACHandler.getSecurity().decode8370(responseBytes);
                        for (byte[] response : result.getResponses()) {
                            logger.debug("Response length:{} thing:{} ", response.length, thing.getUID());
                            if (response.length > 40 + 16) {
                                byte[] data = mideaACHandler.getSecurity()
                                        .aesDecrypt(Arrays.copyOfRange(response, 40, response.length - 16));

                                logger.trace("Bytes in HEX, decoded and with header: length: {}, data: {}", data.length,
                                        Utils.bytesToHex(data));
                                byte bodyType2 = data[0xa];

                                // data[3]: Device Type - 0xAC = AC
                                // https://github.com/georgezhao2010/midea_ac_lan/blob/06fc4b582a012bbbfd6bd5942c92034270eca0eb/custom_components/midea_ac_lan/midea_devices.py#L96

                                // data[9]: MessageType - set, query, notify1, notify2, exception, querySN, exception2,
                                // querySubtype
                                // https://github.com/georgezhao2010/midea_ac_lan/blob/30d0ff5ff14f150da10b883e97b2f280767aa89a/custom_components/midea_ac_lan/midea/core/message.py#L22-L29
                                String responseType = "";
                                switch (data[0x9]) {
                                    case 0x02:
                                        responseType = "set";
                                        break;
                                    case 0x03:
                                        responseType = "query";
                                        break;
                                    case 0x04:
                                        responseType = "notify1";
                                        break;
                                    case 0x05:
                                        responseType = "notify2";
                                        break;
                                    case 0x06:
                                        responseType = "exception";
                                        break;
                                    case 0x07:
                                        responseType = "querySN";
                                        break;
                                    case 0x0A:
                                        responseType = "exception2";
                                        break;
                                    case 0x09: // Helyesen: 0xA0
                                        responseType = "querySubtype";
                                        break;
                                    default:
                                        logger.debug("Invalid response type: {}", data[0x9]);
                                }
                                logger.trace("Response Type: {} and bodyType:{}", responseType, bodyType2);

                                // The response data from the appliance includes a packet header which we don't want
                                data = Arrays.copyOfRange(data, 10, data.length);
                                byte bodyType = data[0x0];
                                logger.trace("Response Type expected: {} and bodyType:{}", responseType, bodyType);
                                logger.trace("Bytes in HEX, decoded and stripped without header: length: {}, data: {}",
                                        data.length, Utils.bytesToHex(data));
                                logger.debug(
                                        "Bytes in BINARY, decoded and stripped without header: length: {}, data: {}",
                                        data.length, Utils.bytesToBinary(data));

                                if (data.length > 0) {
                                    if (data.length < 21) {
                                        logger.warn("Response data is {} long, minimum is 21!", data.length);
                                        return;
                                    }
                                    if (bodyType != -64) {
                                        if (bodyType == 30) {
                                            logger.warn("Error response 0x1E received {} from:{}", bodyType,
                                                    thing.getUID());
                                            return;
                                        }
                                        logger.warn("Unexpected response bodyType {}", bodyType);
                                        return;
                                    }
                                    lastResponse = new Response(data, getVersion(), responseType, bodyType);
                                    try {
                                        processMessage(lastResponse);
                                        logger.trace("data length is {} version is {} thing is {}", data.length,
                                                version, thing.getUID());
                                    } catch (Exception ex) {
                                        logger.warn("Processing response exception: {}", ex.getMessage());
                                    }
                                }
                            }
                        }
                    } else {
                        byte[] data = mideaACHandler.getSecurity()
                                .aesDecrypt(Arrays.copyOfRange(responseBytes, 40, responseBytes.length - 16));
                        // The response data from the appliance includes a packet header which we don't want
                        logger.trace("V2 Bytes decoded with header: length: {}, data: {}", data.length,
                                Utils.bytesToHex(data));
                        if (data.length > 0) {
                            data = Arrays.copyOfRange(data, 10, data.length);
                            logger.trace("V2 Bytes decoded and stripped without header: length: {}, data: {}",
                                    data.length, Utils.bytesToHex(data));

                            lastResponse = new Response(data, getVersion(), "", (byte) 0x00);
                            processMessage(lastResponse);
                            logger.debug("V2 data length is {} version is {} thing is {}", data.length, version,
                                    thing.getUID());
                        } else {
                            logger.debug("Problem with reading V2 response, skipping command {}", command);
                            droppedCommands = droppedCommands + 1;
                            updateChannel(DROPPED_COMMANDS, new DecimalType(droppedCommands));
                        }
                    }
                    return;
                } else {
                    logger.debug("Problem with reading response, skipping command {}", command);
                    droppedCommands = droppedCommands + 1;
                    updateChannel(DROPPED_COMMANDS, new DecimalType(droppedCommands));
                    return;
                }
            } catch (SocketException e) {
                logger.debug("SocketException writing to  {} at {}: {}", thing.getUID(), ipAddress, e.getMessage());
                String message = e.getMessage();
                droppedCommands = droppedCommands + 1;
                updateChannel(DROPPED_COMMANDS, new DecimalType(droppedCommands));
                updateStatus(ThingStatus.OFFLINE, getDetail(), message);
                return;
            } catch (IOException e) {
                logger.debug(" Send IOException writing to  {} at {}: {}", thing.getUID(), ipAddress, e.getMessage());
                String message = e.getMessage();
                droppedCommands = droppedCommands + 1;
                updateChannel(DROPPED_COMMANDS, new DecimalType(droppedCommands));
                updateStatus(ThingStatus.OFFLINE, getDetail(), message);
                return;
            }
        }

        /**
         * Closes all elements of the connection before starting a new one
         */
        protected synchronized void disconnect() {
            // Make sure writer, inputStream and socket are closed before each command is started
            logger.debug("Disconnecting from {} at {}", thing.getUID(), ipAddress);

            InputStream inputStream = this.inputStream;
            DataOutputStream writer = this.writer;
            Socket socket = this.socket;
            try {
                writer.close();
                inputStream.close();
                socket.close();

            } catch (IOException e) {
                logger.warn("IOException closing connection to {} at {}: {}", thing.getUID(), ipAddress, e.getMessage(),
                        e);
            }
            socket = null;
            inputStream = null;
            writer = null;
        }

        private void updateChannel(String channelName, State state) {
            if (isOffline()) {
                return;
            }
            Channel channel = thing.getChannel(channelName);
            if (channel != null) {
                updateState(channel.getUID(), state);
            }
        }

        private void processMessage(Response response) {
            updateChannel(CHANNEL_POWER, response.getPowerState() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_APPLIANCE_ERROR, response.getApplianceError() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_TARGET_TEMPERATURE,
                    new QuantityType<Temperature>(response.getTargetTemperature(), SIUnits.CELSIUS));
            updateChannel(CHANNEL_OPERATIONAL_MODE, new StringType(response.getOperationalMode().toString()));
            updateChannel(CHANNEL_FAN_SPEED, new StringType(response.getFanSpeed().toString()));
            updateChannel(CHANNEL_ON_TIMER, new StringType(response.getOnTimer().toChannel()));
            updateChannel(CHANNEL_OFF_TIMER, new StringType(response.getOffTimer().toChannel()));
            updateChannel(CHANNEL_SWING_MODE, new StringType(response.getSwingMode().toString()));
            updateChannel(CHANNEL_AUXILIARY_HEAT, response.getAuxHeat() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_ECO_MODE, response.getEcoMode() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_TEMPERATURE_UNIT, response.getFahrenheit() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_SLEEP_FUNCTION, response.getSleepFunction() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_TURBO_MODE, response.getTurboMode() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_SCREEN_DISPLAY, response.getDisplayOn() ? OnOffType.ON : OnOffType.OFF);
            updateChannel(CHANNEL_ALTERNATE_TARGET_TEMPERATURE,
                    new QuantityType<Temperature>(response.getAlternateTargetTemperature(), SIUnits.CELSIUS));
            updateChannel(CHANNEL_INDOOR_TEMPERATURE,
                    new QuantityType<Temperature>(response.getIndoorTemperature(), SIUnits.CELSIUS));
            updateChannel(CHANNEL_OUTDOOR_TEMPERATURE,
                    new QuantityType<Temperature>(response.getOutdoorTemperature(), SIUnits.CELSIUS));
            updateChannel(CHANNEL_HUMIDITY, new DecimalType(response.getHumidity()));
        }

        /**
         * Reads the inputStream byte array
         * 
         * @return byte array
         */
        public synchronized byte @Nullable [] read() {
            byte[] bytes = new byte[512];
            InputStream inputStream = this.inputStream;

            try {
                int len = inputStream.read(bytes);
                if (len > 0) {
                    logger.debug("Response received length: {} Thing:{}", len, thing.getUID());
                    bytes = Arrays.copyOfRange(bytes, 0, len);
                    return bytes;
                }
            } catch (IOException e) {
                String message = e.getMessage();
                logger.debug(" Byte read exception {}", message);
            }
            return null;
        }

        /**
         * Writes the packet that will be sent to the device
         * 
         * @param buffer socket writer
         * @throws IOException writer could be null
         */
        public synchronized void write(byte[] buffer) throws IOException {
            DataOutputStream writer = this.writer;

            try {
                writer.write(buffer, 0, buffer.length);
            } catch (IOException e) {
                String message = e.getMessage();
                logger.debug("Write error {}", message);
            }
        }

        /**
         * Periodical polling. Thirty seconds minimum
         */
        private void scheduleConnectionMonitorJob() {
            if (connectionMonitorJob == null) {
                logger.debug("Starting connection monitor job in {} seconds for {} at {} after 30 second delay",
                        config.pollingTime, thing.getUID(), ipAddress);
                long frequency = config.pollingTime;
                long delay = 30L;
                connectionMonitorJob = scheduler.scheduleWithFixedDelay(connectionMonitorRunnable, delay, frequency,
                        TimeUnit.SECONDS);
            }
        }

        private void cancelConnectionMonitorJob() {
            ScheduledFuture<?> connectionMonitorJob = this.connectionMonitorJob;
            if (connectionMonitorJob != null) {
                connectionMonitorJob.cancel(true);
                logger.debug("Cancelling connection monitor job for {} at {}", thing.getUID(), ipAddress);
                this.connectionMonitorJob = null;
            }
        }
    }
}
