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
package org.openhab.binding.konnected.internal.handler;

import static org.openhab.binding.konnected.internal.KonnectedBindingConstants.*;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.konnected.internal.KonnectedConfiguration;
import org.openhab.binding.konnected.internal.KonnectedHTTPUtils;
import org.openhab.binding.konnected.internal.KonnectedHttpRetryExceeded;
import org.openhab.binding.konnected.internal.ZoneConfiguration;
import org.openhab.binding.konnected.internal.gson.KonnectedModuleGson;
import org.openhab.binding.konnected.internal.gson.KonnectedModulePayload;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.validation.ConfigValidationException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link KonnectedHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Zachary Christiansen - Initial contribution
 */
public class KonnectedHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(KonnectedHandler.class);
    private KonnectedConfiguration config;
    private final KonnectedHTTPUtils http = new KonnectedHTTPUtils(30);
    private String callbackUrl;
    private String baseUrl;
    private final Gson gson = new GsonBuilder().create();
    private int retryCount;
    private final String thingID;
    public String authToken;

    /**
     * This is the constructor of the Konnected Handler.
     *
     * @param thing the instance of the Konnected thing
     * @param webHookServlet the instance of the callback servlet that is running for communication with the Konnected
     *            Module
     * @param hostAddress the webaddress of the openHAB server instance obtained by the runtime
     * @param port the port on which the openHAB instance is running that was obtained by the runtime.
     */
    public KonnectedHandler(Thing thing, String callbackUrl) {
        super(thing);
        this.callbackUrl = callbackUrl;
        logger.debug("The auto discovered callback URL is: {}", this.callbackUrl);
        retryCount = 2;
        thingID = getThing().getThingTypeUID().getId();
        authToken = getThing().getUID().getAsString();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // get the zone number in integer form
        Channel channel = this.getThing().getChannel(channelUID.getId());
        String channelType = channel.getChannelTypeUID().getAsString();
        ZoneConfiguration zoneConfig = channel.getConfiguration().as(ZoneConfiguration.class);
        String zone = zoneConfig.zone;
        logger.debug("The channelUID is: {} and the zone is : {}", channelUID.getAsString(), zone);
        // if the command is OnOfftype
        if (command instanceof OnOffType) {
            if (channelType.contains(CHANNEL_SWITCH)) {
                logger.debug("A command was sent to a sensor type so we are ignoring the command");
            } else {
                sendActuatorCommand((OnOffType) command, zone, channelUID);
            }
        } else if (command instanceof RefreshType) {
            // check to see if handler has been initialized before attempting to get state of pin, else wait one minute
            if (this.isInitialized()) {
                getSwitchState(zone, channelUID);
            } else {
                scheduler.schedule(() -> {
                    handleCommand(channelUID, command);
                }, 1, TimeUnit.MINUTES);
            }
        }
    }

    /**
     * Process a {@link WebHookEvent} that has been received by the Servlet from a Konnected module with respect to a
     * sensor event or status update request
     *
     * @param event the {@link KonnectedModuleGson} event that contains the state and pin information to be processed
     */
    public void handleWebHookEvent(KonnectedModuleGson event) {
        // if we receive a command update the thing status to being online
        updateStatus(ThingStatus.ONLINE);
        // get the zone number based off of the index location of the pin value
        // String sentZone = Integer.toString(Arrays.asList(PIN_TO_ZONE).indexOf(event.getPin()));
        String zone = event.getZone(thingID);
        // check that the zone number is in one of the channelUID definitions
        logger.debug("Looping Through all channels on thing: {} to find a match for {}", thing.getUID().getAsString(),
                zone);
        getThing().getChannels().forEach(channel -> {
            ChannelUID channelId = channel.getUID();
            ZoneConfiguration zoneConfig = channel.getConfiguration().as(ZoneConfiguration.class);
            // if the string zone that was sent equals the last digit of the channelId found process it as the
            // channelId else do nothing
            if (zone.equalsIgnoreCase(zoneConfig.zone)) {
                logger.debug(
                        "The configrued zone of channelID: {}  was a match for the zone sent by the alarm panel: {} on thing: {}",
                        channelId, zone, this.getThing().getUID().getId());
                String channelType = channel.getChannelTypeUID().getAsString();
                logger.debug("The channeltypeID is: {}", channelType);
                // check if the itemType has been defined for the zone received
                // check the itemType of the Zone, if Contact, send the State if Temp send Temp, etc.
                if (channelType.contains(CHANNEL_SWITCH) || channelType.contains(CHANNEL_ACTUATOR)) {
                    Integer state = event.getState();
                    logger.debug("The event state is: {}", state);
                    if (state != null) {
                        OnOffType onOffType = state == zoneConfig.onValue ? OnOffType.ON : OnOffType.OFF;
                        updateState(channelId, onOffType);
                    }
                } else if (channelType.contains(CHANNEL_HUMIDITY)) {
                    // if the state is of type number then this means it is the humidity channel of the dht22
                    updateState(channelId, new QuantityType<>(Double.parseDouble(event.getHumi()), Units.PERCENT));
                } else if (channelType.contains(CHANNEL_TEMPERATURE)) {
                    if (zoneConfig.dht22) {
                        updateState(channelId,
                                new QuantityType<>(Double.parseDouble(event.getTemp()), SIUnits.CELSIUS));
                    } else {
                        // need to check to make sure right dsb1820 address
                        logger.debug("The address of the DSB1820 sensor received from module {} is: {}",
                                this.thing.getUID(), event.getAddr());

                        if (event.getAddr().equalsIgnoreCase(zoneConfig.ds18b20Address)) {
                            updateState(channelId,
                                    new QuantityType<>(Double.parseDouble(event.getTemp()), SIUnits.CELSIUS));
                        } else {
                            logger.debug("The address of {} does not match {} not updating this channel",
                                    event.getAddr(), zoneConfig.ds18b20Address);
                        }
                    }
                }
            } else {
                logger.trace(
                        "The zone number sent by the alarm panel: {} was not a match the configured zone for channelId: {} for thing {}",
                        zone, channelId, getThing().getThingTypeUID());
            }
        });
    }

    private void checkConfiguration() throws ConfigValidationException {
        logger.debug("Checking configuration on thing {}", this.getThing().getUID().getAsString());
        Configuration testConfig = this.getConfig();
        String testRetryCount = testConfig.get(RETRY_COUNT).toString();
        String testRequestTimeout = testConfig.get(REQUEST_TIMEOUT).toString();
        baseUrl = testConfig.get(BASE_URL).toString();
        String configuredCallbackUrl = (String) getThing().getConfiguration().get(CALLBACK_URL);
        if (configuredCallbackUrl != null) {
            callbackUrl = configuredCallbackUrl;
        } else {
            getThing().getConfiguration().put(CALLBACK_URL, callbackUrl);
        }
        logger.debug("The RequestTimeout Parameter is Configured as: {}", testRequestTimeout);
        logger.debug("The Retry Count Parameter is Configured as: {}", testRetryCount);
        logger.debug("Base URL is Configured as: {}", baseUrl);
        logger.debug("The callback URL is: {}", callbackUrl);
        try {
            this.retryCount = Integer.parseInt(testRetryCount);
        } catch (NumberFormatException e) {
            logger.debug(
                    "Please check your configuration of the Retry Count as it is not an Integer. It is configured as: {}, will contintue to configure the binding with the default of 2",
                    testRetryCount);
            this.retryCount = 2;
        }
        try {
            this.http.setRequestTimeout(Integer.parseInt(testRequestTimeout));
        } catch (NumberFormatException e) {
            logger.debug(
                    "Please check your configuration of the Request Timeout as it is not an Integer. It is configured as: {}, will contintue to configure the binding with the default of 30",
                    testRequestTimeout);
        }

        if ((callbackUrl == null)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unable to obtain callback URL");
        }

        else {
            this.config = getConfigAs(KonnectedConfiguration.class);
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters)
            throws ConfigValidationException {
        this.validateConfigurationParameters(configurationParameters);
        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            Object value = configurationParameter.getValue();
            logger.debug("Controller Configuration update {} to {}", configurationParameter.getKey(), value);

            if (value == null) {
                continue;
            }
            // this is a nonstandard implementation to to address the configuration of the konnected alarm panel (as
            // opposed to the handler) until
            // https://github.com/eclipse/smarthome/issues/3484 has been implemented in the framework
            String[] cfg = configurationParameter.getKey().split("_");
            if ("controller".equals(cfg[0])) {
                if (cfg[1].equals("softreset") && value instanceof Boolean && (Boolean) value) {
                    scheduler.execute(() -> {
                        try {
                            http.doGet(baseUrl + "/settings?restart=true", null, retryCount);
                        } catch (KonnectedHttpRetryExceeded e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                        }
                    });
                } else if (cfg[1].equals("removewifi") && value instanceof Boolean && (Boolean) value) {
                    scheduler.execute(() -> {
                        try {
                            http.doGet(baseUrl + "/settings?restore=true", null, retryCount);
                        } catch (KonnectedHttpRetryExceeded e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                        }
                    });
                } else if (cfg[1].equals("sendConfig") && value instanceof Boolean && (Boolean) value) {
                    scheduler.execute(() -> {
                        try {
                            String response = updateKonnectedModule();
                            logger.trace("The response from the konnected module with thingID {} was {}",
                                    getThing().getUID(), response);
                            if (response == null) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "Unable to communicate with Konnected Module.");
                            } else {
                                updateStatus(ThingStatus.ONLINE);
                            }
                        } catch (KonnectedHttpRetryExceeded e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                        }
                    });
                }
            }
        }

        super.handleConfigurationUpdate(configurationParameters);
        try

        {
            String response = updateKonnectedModule();
            logger.trace("The response from the konnected module with thingID {} was {}", getThing().getUID(),
                    response);
            if (response == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unable to communicate with Konnected Module confirm settings.");
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (KonnectedHttpRetryExceeded e) {
            logger.trace("The number of retries was exceeeded during the HandleConfigurationUpdate(): {}",
                    e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        try {
            checkConfiguration();
        } catch (ConfigValidationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
        scheduler.execute(() -> {
            try {
                String response = updateKonnectedModule();
                if (response == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unable to communicate with Konnected Module confirm settings or readd thing.");
                } else {
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (KonnectedHttpRetryExceeded e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        super.dispose();
    }

    /**
     * This method constructs the payload that will be sent
     * to the Konnected module via the put request
     * it adds the appropriate sensors and actuators to the {@link KonnectedModulePayload}
     * as well as the location of the callback {@link KonnectedJTTPServlet}
     * and auth_token which can be used for validation
     *
     * @return a json settings payload which can be sent to the Konnected Module based on the Thing
     */
    private String constructSettingsPayload() {
        logger.debug("The Auth_Token is: {}", authToken);
        KonnectedModulePayload payload = new KonnectedModulePayload(authToken, callbackUrl);
        payload.setBlink(config.blink);
        payload.setDiscovery(config.discovery);
        this.getThing().getChannels().forEach(channel -> {
            // ChannelUID channelId = channel.getUID();
            // adds channels to list based on last value of Channel ID
            // which is set to a number
            // get the zone number in integer form
            ZoneConfiguration zoneConfig = channel.getConfiguration().as(ZoneConfiguration.class);
            // if the pin is an actuator add to actuator string
            // else add to sensor string
            // This is determined based off of the accepted item type, contact types are sensors
            // switch types are actuators
            String channelType = channel.getChannelTypeUID().getAsString();
            logger.debug("The channeltypeID is: {}", channelType);
            KonnectedModuleGson module = new KonnectedModuleGson();
            module.setZone(thingID, zoneConfig.zone);
            if (channelType.contains(CHANNEL_SWITCH)) {
                payload.addSensor(module);
                logger.trace("Channel {} will be configured on the konnected alarm panel as a switch", channel);
            } else if (channelType.contains(CHANNEL_ACTUATOR)) {
                payload.addActuators(module);
                logger.trace("Channel {} will be configured on the konnected alarm panel as an actuator", channel);
            } else if (channelType.contains(CHANNEL_HUMIDITY)) {
                // the humidity channels do not need to be added because the supported sensor (dht22) is added under
                // the temp sensor
                logger.trace("Channel {} is a humidity channel.", channel);
            } else if (channelType.contains(CHANNEL_TEMPERATURE)) {
                logger.trace("Channel {} will be configured on the konnected alarm panel as a temperature sensor",
                        channel);
                module.setPollInterval(zoneConfig.pollInterval);
                logger.trace("The Temperature Sensor Type is: {} ", zoneConfig.dht22);
                if (zoneConfig.dht22) {
                    // add it as a dht22 module
                    payload.addDht22(module);
                    logger.trace(
                            "Channel {} will be configured on the konnected alarm panel as a DHT22 temperature sensor",
                            channel);
                } else {
                    // add to payload as a DS18B20 module if the parameter is false
                    payload.addDs18b20(module);
                    logger.trace(
                            "Channel {} will be configured on the konnected alarm panel as a DS18B20 temperature sensor",
                            channel);
                }
            } else {
                logger.debug("Channel {} is of type {} which is not supported by the konnected binding", channel,
                        channelType);
            }
        });
        // Create Json to Send to Konnected Module

        String payloadString = gson.toJson(payload);
        logger.debug("The payload is: {}", payloadString);
        return payloadString;
    }

    /*
     * Prepares and sends the {@link KonnectedModulePayload} via the {@link KonnectedHttpUtils}
     *
     * @return response obtained from sending the settings payload to Konnected module defined by the thing
     *
     * @throws KonnectedHttpRetryExceeded if unable to communicate with the Konnected module defined by the Thing
     */
    private String updateKonnectedModule() throws KonnectedHttpRetryExceeded {
        String payload = constructSettingsPayload();
        String response = http.doPut(baseUrl + "/settings", payload, retryCount);
        logger.debug("The response of the put request was: {}", response);
        return response;
    }

    /**
     * Sends a command to the module via {@link KonnectedHTTPUtils}
     *
     * @param command the state to send to the actuator
     * @param zone the zone to send the command to on the Konnected Module
     */
    private void sendActuatorCommand(OnOffType command, String zone, ChannelUID channelId) {
        try {
            Channel channel = getThing().getChannel(channelId.getId());
            if (channel != null) {
                logger.debug("getasstring: {} getID: {} getGroupId: {} toString:{}", channelId.getAsString(),
                        channelId.getId(), channelId.getGroupId(), channelId);
                ZoneConfiguration zoneConfig = channel.getConfiguration().as(ZoneConfiguration.class);
                KonnectedModuleGson payload = new KonnectedModuleGson();
                payload.setZone(thingID, zone);

                if (command == OnOffType.ON) {
                    payload.setState(zoneConfig.onValue);
                    payload.setTimes(zoneConfig.times);
                    payload.setMomentary(zoneConfig.momentary);
                    payload.setPause(zoneConfig.pause);
                } else {
                    payload.setState(zoneConfig.onValue == 1 ? 0 : 1);
                }

                String payloadString = gson.toJson(payload);
                logger.debug("The command payload  is: {}", payloadString);
                String path = "";
                switch (this.thingID) {
                    case PRO_MODULE:
                        path = "/zone";
                        break;
                    case WIFI_MODULE:
                        path = "/device";
                        break;
                }
                http.doPut(baseUrl + path, payloadString, retryCount);
            } else {
                logger.debug("The channel {} returned null for channelId.getID(): {}", channelId, channelId.getId());
            }
        } catch (KonnectedHttpRetryExceeded e) {
            logger.debug("Attempting to set the state of the actuator on thing {} failed: {}",
                    this.thing.getUID().getId(), e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to communicate with Konnected Alarm Panel confirm settings, and that module is online.");
        }
    }

    private void getSwitchState(String zone, ChannelUID channelId) {
        Channel channel = getThing().getChannel(channelId.getId());
        if (!(channel == null)) {
            logger.debug("getasstring: {} getID: {} getGroupId: {} toString:{}", channelId.getAsString(),
                    channelId.getId(), channelId.getGroupId(), channelId);
            KonnectedModuleGson payload = new KonnectedModuleGson();
            payload.setZone(thingID, zone);
            String payloadString = gson.toJson(payload);
            logger.debug("The command payload  is: {}", payloadString);
            try {
                sendSetSwitchState(thingID, payloadString);
            } catch (KonnectedHttpRetryExceeded e) {
                // try to get the state of the device one more time 30 seconds later. This way it can be confirmed if
                // the device was simply in a reboot loop when device state was attempted the first time
                scheduler.schedule(() -> {
                    try {
                        sendSetSwitchState(thingID, payloadString);
                    } catch (KonnectedHttpRetryExceeded ex) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Unable to communicate with Konnected Alarm Panel confirm settings, and that module is online.");
                        logger.debug("Attempting to get the state of the zone on thing {} failed for channel: {} : {}",
                                this.thing.getUID().getId(), channelId.getAsString(), ex.getMessage());
                    }
                }, 2, TimeUnit.MINUTES);
            }
        } else {
            logger.debug("The channel {} returned null for channelId.getID(): {}", channelId, channelId.getId());
        }
    }

    private void sendSetSwitchState(String thingId, String payloadString) throws KonnectedHttpRetryExceeded {
        String path = thingId.equals(WIFI_MODULE) ? "/device" : "/zone";
        String response = http.doGet(baseUrl + path, payloadString, retryCount);
        KonnectedModuleGson[] events = gson.fromJson(response, KonnectedModuleGson[].class);
        for (KonnectedModuleGson event : events) {
            this.handleWebHookEvent(event);
        }
    }
}
