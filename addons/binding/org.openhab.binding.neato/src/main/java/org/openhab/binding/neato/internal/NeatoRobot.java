/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neato.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.neato.internal.classes.ErrorMessage;
import org.openhab.binding.neato.internal.classes.NeatoGeneralInfo;
import org.openhab.binding.neato.internal.classes.NeatoRobotInfo;
import org.openhab.binding.neato.internal.classes.NeatoState;
import org.openhab.binding.neato.internal.exceptions.CouldNotFindRobotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link NeatoBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Patrik Wimnell - Initial contribution
 */

public class NeatoRobot {

    private final Logger logger = LoggerFactory.getLogger(NeatoRobot.class);

    private String serialNumber;
    private String secret;
    private String name;

    private NeatoState state;
    private NeatoRobotInfo info;
    private NeatoGeneralInfo generalInfo;

    public NeatoState getState() {
        return this.state;
    }

    public NeatoRobotInfo getInfo() {
        return this.info;
    }

    public NeatoGeneralInfo getGeneralInfo() {
        return this.generalInfo;
    }

    public String getName() {
        return this.name;
    }

    public NeatoRobot(String serial, String secret, String name) {
        this.serialNumber = serial;
        this.secret = secret;
        this.name = name;

        this.state = null;
        this.info = null;
        this.generalInfo = null;
    }

    public String callNeatoWS(String body) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        SimpleDateFormat dateFormatGmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));

        // Time in GMT
        String dateString = dateFormatGmt.format(new Date());

        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        String stringToSign = this.serialNumber.toLowerCase() + "\n" + dateString + "\n" + body;

        SecretKeySpec secret_key = new SecretKeySpec(this.secret.getBytes("UTF-8"), "HmacSHA256");
        sha256Hmac.init(secret_key);

        byte[] signature = sha256Hmac.doFinal(stringToSign.getBytes("UTF-8"));
        String hexString = Hex.encodeHexString(signature);

        // Properties headers = new Properties
        Properties headers = new Properties();
        headers.setProperty("Date", dateString);
        headers.setProperty("Authorization", "NEATOAPP " + hexString);
        headers.setProperty("Accept", "application/vnd.neato.nucleo.v1");

        InputStream stream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));

        String result = HttpUtil.executeUrl("POST",
                "https://nucleo.neatocloud.com:4443/vendors/neato/robots/" + this.serialNumber + "/messages", headers,
                stream, "text/html; charset=ISO-8859-1", 20000);

        return result;
    }

    public Boolean sendCommand(Command command) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
        String body = "";

        if (command.toString().toLowerCase() == "clean") {

            String houseCleaningStr = this.state.getAvailableServices().getHouseCleaning();
            if (houseCleaningStr.toLowerCase() == "basic-1") {
                body = "{\"reqId\": \"1\", \"cmd\": \"startCleaning\", \"params\": { \"category\": 2, \"mode\": 2, \"modifier\": 2}}";
            } else if (houseCleaningStr.toLowerCase() == "minimal-2") {
                body = "{\"reqId\": \"1\", \"cmd\": \"startCleaning\", \"params\": { \"category\": 2, \"navigationMode\": 2}}";
            } else if (houseCleaningStr.toLowerCase() == "basic-2") {
                body = "{\"reqId\": \"1\", \"cmd\": \"startCleaning\", \"params\": { \"category\": 2, \"mode\": 2, \"modifier\": 1, \"navigationMode\": 1}}";
            } else {
                logger.error("No matching service for houseCleaning. Will not start house cleaning!");
            }
        } else if (command.toString().toLowerCase() == "pause") {
            body = "{\"reqId\": \"1\", \"cmd\": \"pauseCleaning\"}";
        } else if (command.toString().toLowerCase() == "stop") {
            body = "{\"reqId\": \"1\", \"cmd\": \"stopCleaning\"}";
        } else if (command.toString().toLowerCase() == "resume") {
            body = "{\"reqId\": \"1\", \"cmd\": \"resumeCleaning\"}";
        } else if (command.toString().toLowerCase() == "dock") {
            body = "{\"reqId\": \"1\", \"cmd\": \"sendToBase\"}";
        } else if (command.toString().toLowerCase() == "dismissAlert") {
            body = "{\"reqId\": \"1\", \"cmd\": \"dismissCurrentAlert\"}";
        }

        if (body.isEmpty()) {
            return false;
        }

        this.callNeatoWS(body);

        return true;
    }

    public Boolean sendGetRobotInfo() throws InvalidKeyException, NoSuchAlgorithmException, IOException {

        logger.debug("Will get INFO for Robot {}", this.name);

        String body = "{\"reqId\": \"abc\",\"cmd\": \"getRobotInfo\" }";

        String result = this.callNeatoWS(body);
        result = result.replaceAll("ModelName", "InternalModelName");
        logger.debug("Result from getRobotInfo: {}", result);

        Gson gson = new Gson();

        this.info = gson.fromJson(result, NeatoRobotInfo.class);

        return true;
    }

    public Boolean sendGetState()
            throws InvalidKeyException, NoSuchAlgorithmException, IOException, CouldNotFindRobotException {
        logger.debug("Will get STATE for Robot {}", this.name);

        String body = "{\"reqId\": \"abc\",\"cmd\": \"getRobotState\" }";

        Gson gson = new Gson();

        String result = this.callNeatoWS(body);
        logger.debug("Result from getRobotState: {}", result);

        ErrorMessage eMessage = gson.fromJson(result, ErrorMessage.class);

        if (eMessage.getMessage() != null) {
            logger.error("Error when getting Robot State. Error message {}", eMessage.getMessage());
            throw new CouldNotFindRobotException(eMessage.getMessage());
        }

        this.state = gson.fromJson(result, NeatoState.class);

        logger.debug("Successfully got and parsed new state for {}", this.name);
        return true;

    }

    public Boolean sendGetGeneralInfo() throws InvalidKeyException, NoSuchAlgorithmException, IOException {

        if (state.getAvailableServices().getGeneralInfo() == "basic-1"
                || state.getAvailableServices().getGeneralInfo() == "advanced-1") {
            logger.debug("Will get GENERAL INFO for Robot {}", this.name);

            String body = "{\"reqId\": \"abc\",\"cmd\": \"getGeneralInfo\" }";

            String result = this.callNeatoWS(body);
            logger.debug("Result from getRobotState: {}", result);

            Gson gson = new Gson();

            this.generalInfo = gson.fromJson(result, NeatoGeneralInfo.class);

            return true;

        } else {
            logger.debug("Your vacuum cleaner does not support General Info messages");
            this.generalInfo = null;
            return false;
        }

    }

}
