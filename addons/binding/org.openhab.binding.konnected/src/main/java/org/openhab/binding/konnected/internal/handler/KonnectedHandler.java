/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.konnected.internal.handler;

import static org.openhab.binding.konnected.internal.KonnectedBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.konnected.internal.KonnectedConfiguration;
import org.openhab.binding.konnected.internal.KonnectedDynamicStateDescriptionProvider;
import org.openhab.binding.konnected.internal.KonnectedHTTPUtils;
import org.openhab.binding.konnected.internal.KonnectedPutSettingsTimer;
import org.openhab.binding.konnected.internal.servelet.KonnectedHTTPServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private KonnectedPutSettingsTimer putSettingsTimer = new KonnectedPutSettingsTimer();;
    private final List<String> sensors = new LinkedList<String>();;
    private final List<String> actuators = new LinkedList<String>();
    private final List<Boolean> isAct = new ArrayList<>();
    private final KonnectedHTTPUtils http = new KonnectedHTTPUtils();
    private KonnectedDynamicStateDescriptionProvider dynamicStateDescriptionProvider;

    public KonnectedHandler(Thing thing, KonnectedHTTPServlet webHookServlet,
            KonnectedDynamicStateDescriptionProvider provider) {
        super(thing);
        this.webHookServlet = webHookServlet;
        this.dynamicStateDescriptionProvider = provider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // get the zone number in integer form
        Integer zone = Integer.parseInt(channelUID.getId().substring((channelUID.getId().length() - 1)));

        // convert the zone to the pin based on value at index of zone
        Integer pin = Arrays.asList(PIN_TO_ZONE).get(zone);
        String scommand = command.toString();

        if (scommand.endsWith("N") || scommand.endsWith("1")) {
            scommand = "1";
        } else if (scommand.endsWith("F") || scommand.endsWith("0")) {
            scommand = "0";
        } else {
            logger.debug(
                    "The command string was not a 1 or a 0, setting to off item to off, please fix your item to correct this error");
            scommand = "0";
            StringType state = new StringType("0");
            updateState(channelUID, state);
        }

        // if the pin is an actuator pin process the command
        if (isAct.get(zone)) {
            try {
                String payload = "{\"pin\":" + pin + ",\"state\":" + scommand + "}";
                logger.debug("The command payload  is: {}", payload);
                String data = http.doPut(HOST + "/device", payload);
            } catch (IOException e) {
                logger.debug("Getting the state of the pin failed: {}", e);
            }
        }
        // if it is is a sensor pin do nothing
        else {
            logger.debug("The command was for a sensor pin so we are ignoring the command");
        }
    }

    /**
     * Process a {@link WebHookEvent that has been received by the Servlet from a Konnected module
     *
     * @param pin the pin number which which was activated
     *
     * @param state the state of the pin
     *
     * @param setAuth the token sent with the response
     */
    public void handleWebHookEvent(String pin, String State, String sentAuth) {
        // get the zone number based off of the index location of the pin value
        String channelid = "Zone_" + Integer.toString(Arrays.asList(PIN_TO_ZONE).indexOf(Integer.parseInt(pin)));
        logger.debug("The channelid of the event is: {}, the Auth Token is: {}", channelid, sentAuth);
        StringType channelstate = new StringType(State);
        if (sentAuth.endsWith(config.authToken)) {
            updateState(channelid, channelstate);
        } else {
            logger.debug("The auth token sent did not match what was expected so the state was not accepted.");
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(KonnectedConfiguration.class);

        // add the isActuator elements to the boolean array
        setisAct();

        logger.debug("Setting up Konnected Module WebHook");
        webHookServlet.activate(this);
        updateKonnectedModule();
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    @Override
    public void dispose() {
        logger.debug("Running dispose()");
        logger.debug("Releasing Konnected WebHook");
        webHookServlet.deactivate();
    }

    @Override
    public synchronized void channelLinked(ChannelUID channel) {
        // adds linked channels to list based on last value of Channel ID
        // which is set to a number
        logger.debug("Channel {} has been linked", channel.getId());
        setisAct();
        // get the zone number in integer form
        Integer zone = Integer.parseInt(channel.getId().substring((channel.getId().length() - 1)));

        // convert the zone to the pin based on value at index of zone
        Integer pin = Arrays.asList(PIN_TO_ZONE).get(zone);
        // if the pin is an actuator add to actuator string
        // else add to sensor string
        if (isAct.get(zone)) {
            actuators.add("{\"pin\":" + Integer.toString(pin) + "}");
            StringType state = new StringType("0");
            updateState(channel, state);
        } else {
            sensors.add("{\"pin\":" + Integer.toString(pin) + "}");
        }
        logger.debug(sensors.toString());
        // launch the update so the new channel settings can be sent
        updateKonnectedModule();
    }

    @Override
    public synchronized void channelUnlinked(ChannelUID channel) {
        logger.debug("Channel {} has been unlinked", channel.toString());
        // get the zone number in integer form
        setisAct();
        Integer zone = Integer.parseInt(channel.getId().substring((channel.getId().length() - 1)));
        // convert the zone to the pin based on value at index of zone
        Integer pin = Arrays.asList(PIN_TO_ZONE).get(zone);

        actuators.remove("{\"pin\":" + Integer.toString(pin) + "}");

        sensors.remove("{\"pin\":" + Integer.toString(pin) + "}");

        logger.debug(sensors.toString());
        updateKonnectedModule();
    }

    /*
     * This method constructs the payload that will be sent
     * to the konnected module via the put request
     * it adds the appropriate senors and actuators
     * as well as the location of the callback servelet
     * and auth_token which can be used for validation
     *
     */
    private String contructSettingsPayload() {
        String hostPath = "";
        try {
            hostPath = getHostName() + webHookServlet.getPath();
            logger.debug("The host path is: {}", hostPath);
        }

        catch (UnknownHostException e) {
            logger.debug("Unable to obtain hostname: {}", e);
            return "none";
        }

        String authToken = config.authToken;
        logger.debug("The Auth_Token is: {}", authToken);
        logger.debug("The Sensor String is: {}", sensors.toString());
        logger.debug("The Actuator String is: {}", actuators.toString());

        updatepinstate();
        String payload = "{\"sensors\":" + sensors.toString() + ",\"actuators\": " + actuators.toString()
                + ",\"token\": \"" + authToken + "\",\"apiUrl\": \"http://" + hostPath + "\"}";
        logger.debug("The payload is: {}", payload);
        return payload;
    }

    protected String getHostName() throws UnknownHostException {
        // returns the local address of the openHAB server, or uses the configured path from the thing if the user adds
        // it

        if (config.hostAddress == null) {

            String hostname = http.getHostAddresses() + ":8080";

            return hostname;
        }

        else {
            logger.debug("User has provided an Ip address on the thing for the openhab host of : {}",
                    config.hostAddress);
            return config.hostAddress;
        }
    }

    /*
     * this method sends the payload to the putsettings timer
     * where it can then be sent to the module
     * the actual request is held off for 30 seconds and is reset if
     * another instance requests new settings due to an update
     */
    private void updateKonnectedModule() {
        String payload = contructSettingsPayload();
        // setting it up to wait 30 seconds before sending the put request
        logger.debug("creating new timer");
        putSettingsTimer = putSettingsTimer.startTimer(HOST + "/settings", payload);
    }

    // this method sets the list of boolean variables of which pins are actuators
    private void setisAct() {
        isAct.add(0, false);
        isAct.add(1, config.isAct1);
        isAct.add(2, config.isAct2);
        isAct.add(3, config.isAct3);
        isAct.add(4, config.isAct4);
        isAct.add(5, config.isAct5);
        // pins 6 can never be an actuator and pin 7 is always out
        // so we set them accordingly for all things
        isAct.add(6, false);
        isAct.add(7, true);
    }

    /*
     * This method ensures that the pins that are in the sensor/actuator
     * lists match the boolean types the user has set
     * it will remove any that don't match
     */
    private void updatepinstate() {
        for (int i = 1; i < numberPin; i++) {
            Integer pin = Arrays.asList(PIN_TO_ZONE).get(i);
            // update the read only status of the actuators/sensors
            ChannelUID channel = new ChannelUID(this.thing.getUID().toString() + ":Zone_" + i);
            logger.debug("We are setting the value of readonly for channel {} to: {}", channel.getId(), !isAct.get(i));
            setStateDescription(channel, !isAct.get(i));

            // if it is supposed to be an actuator make sure its not
            // in the sensor list
            if (isAct.get(i)) {
                sensors.remove("{\"pin\":" + Integer.toString(pin) + "}");
            }

            else {
                actuators.remove("{\"pin\":" + Integer.toString(pin) + "}");
            }
        }
    }

    /**
     * Sets a new {@link StateDescription} for a channel that has multiple options to select from or a custom format
     * string. A previous description, if existed, will be replaced.
     *
     * @param channelUID channel UID
     *
     * @param readOnly true if this control does not accept commands
     */
    private void setStateDescription(ChannelUID channelUID, boolean readOnly) {
        if (channelUID != null) {
            dynamicStateDescriptionProvider.setDescription(channelUID,
                    new StateDescription(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE, null, readOnly, null));
        }
    }

}
