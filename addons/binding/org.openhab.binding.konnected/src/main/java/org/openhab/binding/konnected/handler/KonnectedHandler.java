/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.konnected.handler;

import static org.openhab.binding.konnected.KonnectedBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.validation.ConfigValidationException;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.konnected.internal.KonnectedConfiguration;
import org.openhab.binding.konnected.internal.KonnectedHTTPUtils;
import org.openhab.binding.konnected.internal.gson.KonnectedModuleGson;
import org.openhab.binding.konnected.internal.gson.KonnectedModulePayload;
import org.openhab.binding.konnected.internal.servelet.KonnectedHTTPServlet;
import org.openhab.binding.konnected.internal.servelet.KonnectedWebHookFail;
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
    private KonnectedHTTPServlet webHookServlet;
    private final KonnectedHTTPUtils http = new KonnectedHTTPUtils();
    private String callbackIpAddress = null;
    private String moduleIpAddress;
    private Gson gson = new GsonBuilder().create();

    /**
     * This is the constructor of the Konnected Handler.
     *
     * @param thing          the instance of the Konnected thing
     * @param webHookServlet the instance of the callback servlet that is running for communication with the Konnected
     *                           Module
     * @param hostAddress    the webaddress of the OpenHAB server instance obtained by the runtime
     * @param port           the port on which the OpenHAB instance is running that was obtained by the runtime.
     */
    public KonnectedHandler(Thing thing, KonnectedHTTPServlet webHookServlet, String hostAddress, String port) {
        super(thing);
        this.webHookServlet = webHookServlet;
        callbackIpAddress = hostAddress + ":" + port;
        logger.debug("The callback ip address is: {}", callbackIpAddress);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // get the zone number in integer form
        Integer zone = Integer.parseInt(channelUID.getId().substring((channelUID.getId().length() - 1)));
        logger.debug("The channelUID is: {} and the zone is :", channelUID.getAsString(), zone);
        // convert the zone to the pin based on value at index of zone
        Integer pin = Arrays.asList(PIN_TO_ZONE).get(zone);
        logger.debug("Zachary test {}", command instanceof OnOffType);
        // if the command is OnOfftype
        if (command instanceof OnOffType) {
            int sendCommand = (OnOffType.OFF.compareTo((OnOffType) command));
            logger.debug("The command being sent to pin {} for channel:  is {}", channelUID.getAsString(), pin,
                    sendCommand);
            sendActuatorCommand(Integer.toString(sendCommand), pin, channelUID);
        } else if (command instanceof OpenClosedType) {
            logger.debug("A command was sent to a sensor type so we are ignoring the command");
        }
    }

    /**
     * Process a {@link WebHookEvent} that has been received by the Servlet from a Konnected module with respect to a
     * sensor event
     *
     * @param pin     the pin number which which was activated
     * @param state   the state of the pin
     * @param setAuth the token sent with the response
     */
    public void handleWebHookEvent(KonnectedModuleGson event, String sentAuth) {
        // if we receive a command upteate the thing status to being online
        updateStatus(ThingStatus.ONLINE);
        // get the zone number based off of the index location of the pin value
        String sentZone = Integer.toString(Arrays.asList(PIN_TO_ZONE).indexOf(event.getPin()));
        // check that the zone number is in one of the channelUID definitions
        getThing().getChannels().forEach(channel -> {
            ChannelUID channelId = channel.getUID();
            String zoneNumber = channelId.getId().substring((channelId.getId().length() - 1));
            // if the string zone that was sent equals the last digit of the channelId found process it as the
            // channelId else do nothing
            if (sentZone.equalsIgnoreCase(zoneNumber)) {
                logger.debug("The channelID of the event is: {}, the Auth Token is: {}", channelId, sentAuth);
                // check that the auth token sent matches the authToken configured on the Thing
                if (sentAuth.endsWith(config.authToken)) {
                    String itemType = channel.getAcceptedItemType();
                    // check if the itemType has been defined for the zone received
                    if (itemType == null) {
                        logger.error(
                                "The itemType is not configured for channel {} on thing {}, please check your configuration.",
                                channelId, getThing().getThingTypeUID().toString());
                    } else {
                        // check the itemType of the Zone, if Contact, send the State if Temp send Temp, etc.
                        if (itemType.equalsIgnoreCase("Contact")) {
                            OpenClosedType openClosedType = event.getState().equalsIgnoreCase("1") ? OpenClosedType.OPEN
                                    : OpenClosedType.CLOSED;
                            updateState(channelId, openClosedType);
                        } else if (itemType.equalsIgnoreCase("Switch")) {
                            OnOffType onOffType = event.getState().equalsIgnoreCase("1") ? OnOffType.ON : OnOffType.OFF;
                            updateState(channelId, onOffType);
                        } else if (itemType.equalsIgnoreCase("Number:Dimensionless")) {
                            // if the state is of type number then this means it is the humidity channel of the dht22
                            updateState(channelId, new QuantityType<Dimensionless>(Double.parseDouble(event.getHumi()),
                                    SmartHomeUnits.PERCENT));
                        } else if ((itemType.equalsIgnoreCase("Number:Temperature"))) {
                            Configuration configuration = channel.getConfiguration();
                            if (((Boolean) configuration.get("tempsensorType") == true)) {
                                updateState(channelId, new QuantityType<Temperature>(
                                        Double.parseDouble(event.getTemp()), SIUnits.CELSIUS));
                            } else {
                                // need to check to make sure right dsb1820 address
                                logger.debug("The address of the DSB1820 sensor received from modeule {} is: {}",
                                        this.thing.getBridgeUID(), event.getAddr());
                                if (event.getAddr().toString()
                                        .equalsIgnoreCase((String) (configuration.get("ds18b20_address")))) {
                                    updateState(channelId, new QuantityType<Temperature>(
                                            Double.parseDouble(event.getTemp()), SIUnits.CELSIUS));
                                } else {
                                    logger.debug("The address of {} does not match {} not updating this channel",
                                            event.getAddr().toString(), (configuration.get("ds18b20_address")));
                                }
                            }
                        }
                    }
                } else {
                    logger.warn(
                            "The auth token: {}  did not match {} configured in the thing {} so the state was not accepted.",
                            sentAuth, config.authToken, getThing().getThingTypeUID().toString());
                }
            } else {
                // by logging all of the ones that don't match in debug we can see all of the channel ids that have been
                // defined
                logger.debug("The channel sent by the module: {} did not match channelId: {} for thing {}", sentZone,
                        channelId, getThing().getThingTypeUID().toString());
            }
        });
    }

    private void checkConfigruation() throws ConfigValidationException {
        KonnectedConfiguration testConfig = getConfigAs(KonnectedConfiguration.class);

        if ((testConfig.hostAddress == null) && (callbackIpAddress == null)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unable to obtain hostaddress from OSGI service, please manually configure hostaddress");
        }

        else {
            this.config = testConfig;
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

            String[] cfg = configurationParameter.getKey().split("_");
            if ("controller".equals(cfg[0])) {
                if (cfg[1].equals("softreset") && value instanceof Boolean && ((Boolean) value) == true) {
                    try {
                        http.doGet(moduleIpAddress + "/settings?restart=true");
                    } catch (IOException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    }
                    value = false;
                } else if (cfg[1].equals("removewifi") && value instanceof Boolean && ((Boolean) value) == true) {
                    try {
                        http.doGet(moduleIpAddress + "/settings?restore=true");
                    } catch (IOException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    }
                    value = false;
                } else if (cfg[1].equals("sendConfig") && value instanceof Boolean && ((Boolean) value) == true) {
                    try {
                        String response = updateKonnectedModule();
                        logger.trace("The response from the konnected module with thingID {} was {}",
                                getThing().getUID().toString(), response);
                        if (response == null) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "Unable to communicate with Konnected Module confirm settings.");
                        } else {
                            updateStatus(ThingStatus.ONLINE);
                        }
                    } catch (IOException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    }
                    value = false;
                }
            }
        }

        super.handleConfigurationUpdate(configurationParameters);
        try {
            String response = updateKonnectedModule();
            logger.trace("The response from the konnected module with thingID {} was {}",
                    getThing().getUID().toString(), response);
            if (response == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unable to communicate with Konnected Module confirm settings.");
            } else {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (IOException e) {
            logger.trace("There was an IOEcption Error thrown during HandleConfigurationUpdate()");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> {
            try {
                checkConfigruation();
            } catch (ConfigValidationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
            this.moduleIpAddress = this.getThing().getProperties().get(HOST).toString();
            // add the isActuator elements to the boolean array
            logger.debug("Setting up Konnected Module WebHook");
            try {
                webHookServlet.activate(this);
            } catch (KonnectedWebHookFail e) {
                logger.trace("there was an error activating the servelet: {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
            try {
                String response = updateKonnectedModule();
                if (response == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unable to communicate with Konnected Module confirm settings or readd thing.");
                } else {
                    updateStatus(ThingStatus.ONLINE);
                }
            }

            catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        logger.debug("Releasing Konnected WebHook");
        webHookServlet.deactivate();
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
    private String contructSettingsPayload() {
        String hostPath = "";
        try {
            hostPath = getHostName() + webHookServlet.getPath();
            logger.debug("The host path is: {}", hostPath);
        } catch (UnknownHostException e) {
            logger.debug("Unable to obtain hostname: {}", e.getMessage());
            return "none";
        }
        String authToken = config.authToken;
        logger.debug("The Auth_Token is: {}", authToken);
        KonnectedModulePayload payload = new KonnectedModulePayload(authToken, "http://" + hostPath);
        payload.setBlink(config.blink);
        payload.setDiscovery(config.discovery);
        getThing().getChannels().forEach(channel -> {
            ChannelUID channelId = channel.getUID();
            if (isLinked(channel.getUID())) {
                // adds linked channels to list based on last value of Channel ID
                // which is set to a number
                // get the zone number in integer form
                Integer zone = Integer.parseInt(channelId.getId().substring((channelId.getId().length() - 1)));

                // convert the zone to the pin based on value at index of zone
                Integer pin = Arrays.asList(PIN_TO_ZONE).get(zone);

                // if the pin is an actuator add to actuator string
                // else add to sensor string
                // This is determined based off of the accepted item type, contact types are sensors
                // switch types are actuators
                String itemType = channel.getAcceptedItemType();
                logger.trace("The itemType is: {} ", itemType);

                // if the itemType is a contact then the user has configured this to be a sensor
                KonnectedModuleGson module = new KonnectedModuleGson();
                module.setPin(pin);
                if (itemType.equalsIgnoreCase("contact")) {
                    payload.addSensor(module);
                    logger.trace("Channel {} is of type Contact", channel.toString());
                }
                // if the itemType is a switch then the user has configure this to be a switch
                if (itemType.equalsIgnoreCase("Switch")) {
                    payload.addActuators(module);
                    StringType state = new StringType("0");
                    updateState(channelId, state);
                    logger.trace("Channel {} is of type Switch", channel.toString());
                } // if the itemType is dimensionless then this is the humididty of the dht22, which we add in the Temp,
                  // so we don't add anything here
                if (itemType.equalsIgnoreCase("Number:Dimensionless")) {
                    logger.trace("Channel {} is of type Number:Dimensionless", channel.toString());
                } // if the itemType is Number:Temperature then this is a Temp Sensor
                if (itemType.equalsIgnoreCase("Number:Temperature")) {
                    logger.trace("Channel {} is of type Number:Temperature", channel.toString());
                    Configuration configuration = channel.getConfiguration();
                    if (configuration.get("pollinterval") == null) {
                        module.setPollInterval(3);
                    } else {
                        module.setPollInterval(((BigDecimal) configuration.get("pollinterval")).intValue());
                    }
                    logger.trace("The Temperature Sensor Type is: ", configuration.get("tempsensorType").toString());
                    if ((boolean) configuration.get("tempsensorType")) {
                        // add it as a dht22 module
                        payload.addDht22(module);
                    } else {
                        // add to payload as a DS18B20 module if the parameter is false
                        payload.addDs18b20(module);
                    }
                } else {
                    logger.debug("Channel {} is of type {} which is not supported by the konnected binding",
                            channel.toString(), itemType);
                }
            } else {
                logger.debug("The Channel {} is not linked to an item", channel.getUID());
            }
        });
        // Create Json to Send to Konnected Module

        String payloadString = gson.toJson(payload);
        logger.debug("The payload is: {}", payloadString);
        return payloadString;
    }

    /**
     * Obtains the configured hostname of the openHAB server to send to the moduele.
     *
     * @return either the ipaddress obtained by the runtime or defined by the user in the thing configuration
     * @throws UnknownHostException if no host defined
     */
    protected String getHostName() throws UnknownHostException {
        // returns the local address of the openHAB server from the NetworkAddressService,
        // or uses the configured path from the thing if the user adds one
        if (config.hostAddress == null) {
            return callbackIpAddress;
        } else {
            logger.debug("User has provided an Ip address on the thing for the openhab host of : {}",
                    config.hostAddress);
            return config.hostAddress;
        }
    }

    /**
     * Prepares and sends the {@link KonnectedModulePayload} via the {@link KonnectedHttpUtils}
     *
     * @return response obtained from sending the settings payload to Konnected module defined by the thing
     * @throws IOException if unable to communicate with the Konnected module defined by the Thing
     */
    private String updateKonnectedModule() throws IOException {
        String payload = contructSettingsPayload();
        String response = http.doPut(moduleIpAddress + "/settings", payload);
        logger.debug("The resposne of the put request was: {}", response);
        return response;
    }

    /**
     * Sends a command to the module via {@link KonnectedHTTPUtils}
     *
     * @param scommand the string command, either 0 or 1 to send to the actutor pin on the Konnected module
     * @param pin      the pin to send the command to on the Konnected Module
     */
    private void sendActuatorCommand(String scommand, Integer pin, ChannelUID channelId) {
        try {
            Channel channel = getThing().getChannel(channelId.getId());
            if (!(channel == null)) {
                logger.debug("getasstring: {} getID: {} getGroupId: {} toString:{}", channelId.getAsString(),
                        channelId.getId(), channelId.getGroupId(), channelId.toString());
                Configuration configuration = channel.getConfiguration();
                KonnectedModuleGson payload = new KonnectedModuleGson();
                payload.setState(scommand);
                payload.setPin(pin);

                if (configuration.get("times") == null) {
                    logger.debug("The times configuration was not set for channelID: {}, not adding it to the payload.",
                            channelId.toString());
                } else {
                    payload.setTimes(configuration.get("times").toString());
                    logger.debug("The times configuration was set to: {} for channelID: {}.",
                            configuration.get("times").toString(), channelId.toString());
                }
                if (configuration.get("momentary") == null) {
                    logger.debug(
                            "The momentary configuration was not set for channelID: {}, not adding it to the payload.",
                            channelId.toString());
                } else {
                    payload.setMomentary(configuration.get("momentary").toString());
                    logger.debug("The momentary configuration set to: {} channelID: {}.",
                            configuration.get("momentary").toString(), channelId.toString());
                }
                if (configuration.get("pause") == null) {
                    logger.debug("The pause configuration was not set for channelID: {}, not adding it to the payload.",
                            channelId.toString());
                } else {
                    payload.setPause(configuration.get("pause").toString());
                    logger.debug("The pause configuration was set to: {} for channelID: {}.",
                            configuration.get("pause").toString(), channelId.toString());
                }

                String payloadString = gson.toJson(payload);
                logger.debug("The command payload  is: {}", payloadString);
                http.doPut(moduleIpAddress + "/device", payloadString);
            } else {
                logger.debug("The channel {} returned null for channelId.getID(): {}", channelId.toString(),
                        channelId.getId());
                KonnectedModuleGson payload = new KonnectedModuleGson();
                payload.setState(scommand);
                payload.setPin(pin);
            }
        } catch (IOException e) {
            logger.debug("Attempting to set the state of the actuator failed: {}", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to communicate with Konnected Module confirm settings, and that module is online.");
        }
    }
}
