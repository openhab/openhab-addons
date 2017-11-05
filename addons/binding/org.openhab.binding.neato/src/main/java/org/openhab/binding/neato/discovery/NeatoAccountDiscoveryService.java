/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neato.discovery;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.neato.NeatoBindingConstants;
import org.openhab.binding.neato.internal.NeatoHandlerFactory;
import org.openhab.binding.neato.internal.classes.BeehiveAuthentication;
import org.openhab.binding.neato.internal.classes.NeatoAccountInformation;
import org.openhab.binding.neato.internal.classes.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link NeatoAccountDiscoveryService} is responsible for starting the discovery procedure
 * that connects to Neato Web and imports all registered vacuum cleaners.
 *
 * @author Patrik Wimnell - Initial contribution
 */
public class NeatoAccountDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(NeatoAccountDiscoveryService.class);

    private static final int TIMEOUT = 15;
    private SecureRandom random = new SecureRandom();

    private String accessToken;

    public NeatoAccountDiscoveryService() {
        super(NeatoHandlerFactory.SUPPORTED_THING_TYPES_UIDS, TIMEOUT);
        this.accessToken = null;
    }

    private String sendAuthRequestToNeato(String data) throws IOException {

        Properties headers = new Properties();
        headers.setProperty("Accept", "application/vnd.neato.nucleo.v1");

        if (this.accessToken != null) {
            headers.setProperty("Token token", this.accessToken);
        }

        String resultString = "";

        try {

            InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

            resultString = HttpUtil.executeUrl("POST", "https://beehive.neatocloud.com/sessions", headers, stream,
                    "application/json", 20000);

            logger.trace("Received response from neatocloud: {}", resultString);

        } catch (IOException e) {
            throw e;
        }
        return resultString;
    }

    private Boolean authenticate(String username, String password) {

        String authenticationString;
        try {
            authenticationString = "{\"email\": \"" + username + "\", \"password\": \"" + password
                    + "\", \"os\": \"ios\", \"token\": \"" + new BigInteger(130, random).toString(64).getBytes("UTF-8")
                    + "\"}";
        } catch (Exception e) {
            logger.error("Error during Authentication procedure. Error: {}", e.getMessage());
            return false;
        }

        String authenticationResponse = "";
        try {
            authenticationResponse = sendAuthRequestToNeato(authenticationString);
        } catch (Exception e) {
            logger.error("Error when sending Authentication request to Neato. Error: {}", e.getMessage());
            return false;
        }

        logger.debug("Authentication Response: {}", authenticationResponse);

        Gson gson = new Gson();

        BeehiveAuthentication authenticationObject = gson.fromJson(authenticationResponse, BeehiveAuthentication.class);
        this.accessToken = authenticationObject.getAccessToken();

        return true;
    }

    public Boolean sendGetRobots() {

        Properties headers = new Properties();
        headers.setProperty("Accept", "application/vnd.neato.nucleo.v1");

        if (this.accessToken != null) {
            headers.setProperty("Authorization", "Token token=" + this.accessToken);
        }

        try {
            String resultString = HttpUtil.executeUrl("GET", "https://beehive.neatocloud.com/dashboard", headers, null,
                    "application/json", 20000);

            Gson gson = new Gson();
            NeatoAccountInformation accountInformation = gson.fromJson(resultString, NeatoAccountInformation.class);

            logger.debug("sendGetRobots: Result from WS call to get Robots: {}", resultString);

            List<Robot> mrRobots = accountInformation.getRobots();

            for (Robot mrRobot : mrRobots) {
                addThing(mrRobot);
            }
        } catch (IOException e) {
            logger.error("sendGetRobots: Error when sending getRobots request to Neato. Error: {}", e.getMessage());
            return false;
        }

        return true;
    }

    public void getRobotsFromNeato() {
        if (NeatoHandlerFactory.getEmail() != null) {
            authenticate(NeatoHandlerFactory.getEmail(), NeatoHandlerFactory.getPassword());
        }

        if (this.accessToken != null) {
            sendGetRobots();
        }

    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("First - let's authenticate!");
        getRobotsFromNeato();
    }

    @Override
    protected void startScan() {
        getRobotsFromNeato();
    }

    private void addThing(Robot mrRobot) {
        logger.trace("addThing(): Adding new Neato unit {} to the smarthome inbox", mrRobot.getName());

        Map<String, Object> properties = null;

        properties = new HashMap<>();
        ThingUID thingUID = new ThingUID(NeatoBindingConstants.THING_TYPE_VACUUMCLEANER, mrRobot.getSerial());
        properties.put(NeatoBindingConstants.CONFIG_NAME, mrRobot.getName());
        properties.put(NeatoBindingConstants.CONFIG_SECRET, mrRobot.getSecretKey());
        properties.put(NeatoBindingConstants.CONFIG_SERIAL, mrRobot.getSerial());

        if (properties != null) {
            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withProperties(properties).build());
        }
    }
}
