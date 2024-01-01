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
package org.openhab.binding.neato.internal;

import static org.openhab.binding.neato.internal.classes.Category.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.openhab.binding.neato.internal.classes.ErrorMessage;
import org.openhab.binding.neato.internal.classes.NeatoGeneralInfo;
import org.openhab.binding.neato.internal.classes.NeatoRobotInfo;
import org.openhab.binding.neato.internal.classes.NeatoState;
import org.openhab.binding.neato.internal.config.NeatoRobotConfig;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link NeatoRobot} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Patrik Wimnell - Initial contribution
 * @author Jeff Lauterbach - Code Cleanup and Refactor
 */

public class NeatoRobot {

    private final Logger logger = LoggerFactory.getLogger(NeatoRobot.class);

    private String serialNumber;
    private String secret;

    private NeatoState state;
    private NeatoRobotInfo info;
    private NeatoGeneralInfo generalInfo;

    private Gson gson = new Gson();

    public NeatoRobot(NeatoRobotConfig config) {
        this.serialNumber = config.getSerial();
        this.secret = config.getSecret();
    }

    public NeatoState getState() {
        return this.state;
    }

    public NeatoRobotInfo getInfo() {
        return this.info;
    }

    public NeatoGeneralInfo getGeneralInfo() {
        return this.generalInfo;
    }

    private String callNeatoWS(String body) throws NeatoCommunicationException {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));

        // Time in GMT
        String dateString = dateFormatGmt.format(new Date());

        Mac sha256Hmac;
        try {
            sha256Hmac = Mac.getInstance("HmacSHA256");
            String stringToSign = this.serialNumber.toLowerCase() + "\n" + dateString + "\n" + body;

            SecretKeySpec secretKey = new SecretKeySpec(this.secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);

            byte[] signature = sha256Hmac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String hexString = HexUtils.bytesToHex(signature);

            // Properties headers = new Properties
            Properties headers = new Properties();
            headers.setProperty("Date", dateString);
            headers.setProperty("Authorization", "NEATOAPP " + hexString);
            headers.setProperty("Accept", "application/vnd.neato.nucleo.v1");

            logger.debug("Calling Neato WS with body: {}", body);

            InputStream stream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));

            return HttpUtil.executeUrl("POST",
                    "https://nucleo.neatocloud.com:4443/vendors/neato/robots/" + this.serialNumber + "/messages",
                    headers, stream, "text/html; charset=ISO-8859-1", 20000);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new NeatoCommunicationException(e);
        }
    }

    public void sendCommand(String command) throws NeatoCommunicationException {
        CommandRequest request = new CommandRequest();
        logger.debug("State neato : {}", this.state);

        if ("clean".equalsIgnoreCase(command)) {
            String houseCleaningStr = this.state.getAvailableServices().getHouseCleaning();
            logger.debug("Mode cleaning : {}", houseCleaningStr);
            logger.debug("State neato : {}", this.state);

            request.setCmd("startCleaning");
            request.addParam("category", HOUSE.getCategory());

            if ("basic-1".equalsIgnoreCase(houseCleaningStr)) {
                request.addParam("mode", this.state.getCleaning().getModeValue());
                request.addParam("modifier", 1);
            } else if ("minimal-2".equalsIgnoreCase(houseCleaningStr)) {
                request.addParam("navigationMode", this.state.getCleaning().getNavigationModeValue());
            } else {
                if (!"basic-3".equalsIgnoreCase(houseCleaningStr) && !"basic-4".equalsIgnoreCase(houseCleaningStr)) {
                    logger.debug(
                            "Unknown service for houseCleaning: {}. Will attempt to start cleaning using basic-4 message",
                            houseCleaningStr);
                }

                request.addParam("mode", this.state.getCleaning().getModeValue());
                request.addParam("category", HOUSE.getCategory());
                request.addParam("navigationMode", this.state.getCleaning().getNavigationModeValue());
            }
        } else if ("cleanWithMap".equalsIgnoreCase(command)) {
            request.setCmd("startCleaning");
            request.addParam("category", MAP.getCategory());
            request.addParam("mode", this.state.getCleaning().getModeValue());
            request.addParam("navigationMode", this.state.getCleaning().getNavigationModeValue());
        } else if ("pause".equalsIgnoreCase(command)) {
            request.setCmd("pauseCleaning");
        } else if ("stop".equalsIgnoreCase(command)) {
            request.setCmd("stopCleaning");
        } else if ("resume".equalsIgnoreCase(command)) {
            request.setCmd("resumeCleaning");
        } else if ("dock".equalsIgnoreCase(command)) {
            request.setCmd("sendToBase");
        } else if ("dismissAlert".equalsIgnoreCase(command)) {
            request.setCmd("dismissCurrentAlert");
        } else {
            logger.debug("Unexpected command received: {}", command);
            return;
        }

        String result = this.callNeatoWS(gson.toJson(request));
        logger.debug("Result from sendCommand: {}", result);
    }

    public Boolean sendGetRobotInfo() throws NeatoCommunicationException {
        logger.debug("Will get INFO for Robot {}", this.serialNumber);

        CommandRequest request = new CommandRequest();
        request.setCmd("getRobotInfo");

        String result = this.callNeatoWS(gson.toJson(request));
        logger.debug("Result from getRobotInfo: {}", result);

        this.info = gson.fromJson(result, NeatoRobotInfo.class);

        return true;
    }

    public void sendGetState() throws NeatoCommunicationException, CouldNotFindRobotException {
        logger.debug("Will get STATE for Robot {}", this.serialNumber);

        CommandRequest request = new CommandRequest();
        request.setCmd("getRobotState");

        String result = this.callNeatoWS(gson.toJson(request));
        logger.debug("Result from getRobotState: {}", result);

        ErrorMessage eMessage = gson.fromJson(result, ErrorMessage.class);
        if (eMessage.getMessage() != null) {
            logger.error("Error when getting Robot State. Error message: {}", eMessage.getMessage());
            throw new CouldNotFindRobotException(eMessage.getMessage());
        }

        this.state = gson.fromJson(result, NeatoState.class);

        logger.debug("Successfully got and parsed new state for {}", this.serialNumber);
    }

    public void sendGetGeneralInfo() throws NeatoCommunicationException {
        if ("basic-1".equals(state.getAvailableServices().getGeneralInfo())
                || "advanced-1".equals(state.getAvailableServices().getGeneralInfo())) {
            logger.debug("Will get GENERAL INFO for Robot {}", this.serialNumber);

            CommandRequest request = new CommandRequest();
            request.setCmd("getGeneralInfo");

            String result = this.callNeatoWS(gson.toJson(request));
            logger.debug("Result from getGeneralInfo: {}", result);

            this.generalInfo = gson.fromJson(result, NeatoGeneralInfo.class);
        } else {
            logger.debug("Your vacuum cleaner does not support General Info messages");
            this.generalInfo = null;
        }
    }

    private static class CommandRequest {
        private String reqId = "1";
        private String cmd;
        private Map<String, Object> params = new HashMap<>();

        public String getReqId() {
            return reqId;
        }

        public void setReqId(String reqId) {
            this.reqId = reqId;
        }

        public String getCmd() {
            return cmd;
        }

        public void setCmd(String cmd) {
            this.cmd = cmd;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public void addParam(String param, String value) {
            this.params.put(param, value);
        }

        public void addParam(String param, int value) {
            this.params.put(param, value);
        }
    }
}
