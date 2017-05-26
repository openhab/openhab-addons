/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hs110.internal;

import static org.eclipse.smarthome.core.library.types.OnOffType.ON;
import static org.openhab.binding.hs110.internal.Util.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 *
 * The {@link HS110} acts as a protocol parser and interface to the physical device
 *
 * @author Christian Fischer - Initial contribution
 */
public class HS110 {

    private Logger logger = LoggerFactory.getLogger(HS110.class);

    private static final int HS100_PORT = 9999;

    public enum Command {
        SWITCH_ON("{\"system\":{\"set_relay_state\":{\"state\":1}}}}"),
        SWITCH_OFF("{\"system\":{\"set_relay_state\":{\"state\":0}}}}"),
        SYSINFO("{\"system\":{\"get_sysinfo\":null}}"),
        ENERGY("{\"emeter\":{\"get_realtime\":null}}");

        public String value;

        Command(String value) {
            this.value = value;
        }
    }

    public String ip;

    public HS110(String ip) {
        this.ip = ip;
    }

    public boolean isPresent() {

        try {

            InetAddress ip = InetAddress.getByName(this.ip);
            return ip.isReachable(500);
        } catch (IOException ex) {
        }
        return false;
    }

    public boolean sendSwitch(OnOffType command) throws IOException {
        String jsonData = sendCommand(command == ON ? Command.SWITCH_ON : Command.SWITCH_OFF);
        if (jsonData.length() > 0) {

            JsonObject jo = new JsonParser().parse(jsonData).getAsJsonObject();
            int errorCode = jo.get("system").getAsJsonObject().get("set_relay_state").getAsJsonObject().get("err_code")
                    .getAsInt();
            return errorCode == 0;
        }
        return false;
    }

    /* SysInfo relatet parsing */

    public static JsonObject parseSysinfoObject(String data) {
        JsonObject dataObject = new JsonParser().parse(data).getAsJsonObject();
        JsonObject systemObject = dataObject.getAsJsonObject("system");
        JsonObject sysInfo = systemObject.getAsJsonObject("get_sysinfo");
        return sysInfo;
    }

    public static String parseSysinfo(String data) {
        return parseSysinfoObject(data).toString();
    }

    public static String parseAddress(String data) {
        return parseSysinfoObject(data).get("address").getAsString();
    }

    public static BigDecimal parsePort(String data) {
        return parseSysinfoObject(data).get("port").getAsBigDecimal();

    }

    public static String parseDeviceId(String data) {
        return parseSysinfoObject(data).get("deviceId").getAsString();

    }

    public static OnOffType parseState(String data) {
        int state = parseSysinfoObject(data).get("relay_state").getAsInt();
        return state == 1 ? OnOffType.ON : OnOffType.OFF;
    }

    /* Energy related parsing */
    private static JsonObject parseEnergyObject(String data) {
        return new JsonParser().parse(data).getAsJsonObject().getAsJsonObject("emeter").getAsJsonObject("get_realtime");
    }

    public static BigDecimal parseWattage(String data) {
        return parseEnergyObject(data).get("power").getAsBigDecimal();
    }

    public static BigDecimal parseTotal(String data) {
        return parseEnergyObject(data).get("total").getAsBigDecimal();
    }

    public String sendCommand(Command command) throws IOException, ConnectException {

        logger.debug("Executing command {}", command.value);

        Socket socket = new Socket(ip, HS100_PORT);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(encryptWithHeader(command.value));

        InputStream inputStream = socket.getInputStream();
        String data = decrypt(inputStream, false);

        outputStream.close();
        inputStream.close();
        socket.close();

        logger.trace("Received answer {}", data);

        return data;
    }

}
