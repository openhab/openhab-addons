/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.gree.internal.handler;

import static org.openhab.binding.gree.internal.GreeBindingConstants.*;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gree.internal.GreeCryptoUtil;
import org.openhab.binding.gree.internal.GreeException;
import org.openhab.binding.gree.internal.gson.GreeBindRequestPackDTO;
import org.openhab.binding.gree.internal.gson.GreeBindResponseDTO;
import org.openhab.binding.gree.internal.gson.GreeBindResponsePackDTO;
import org.openhab.binding.gree.internal.gson.GreeExecResponseDTO;
import org.openhab.binding.gree.internal.gson.GreeExecResponsePackDTO;
import org.openhab.binding.gree.internal.gson.GreeExecuteCommandPackDTO;
import org.openhab.binding.gree.internal.gson.GreeReqStatusPackDTO;
import org.openhab.binding.gree.internal.gson.GreeRequestDTO;
import org.openhab.binding.gree.internal.gson.GreeScanResponseDTO;
import org.openhab.binding.gree.internal.gson.GreeStatusResponseDTO;
import org.openhab.binding.gree.internal.gson.GreeStatusResponsePackDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

/**
 * The GreeDevice object repesents a Gree Airconditioner and provides
 * device specific attributes as well a the functionality for the Air Conditioner
 *
 * @author John Cunha - Initial contribution
 * @author Markus Michels - Refactoring, adapted to OH 2.5x
 */
@NonNullByDefault
public class GreeAirDevice {
    private final Logger logger = LoggerFactory.getLogger(GreeAirDevice.class);
    private final static Charset UTF8_CHARSET = StandardCharsets.UTF_8;
    private final static HashMap<String, HashMap<String, Integer>> tempRanges = createTempRangeMap();
    private final static Gson gson = new Gson();
    private boolean isBound = false;
    private InetAddress ipAddress = InetAddress.getLoopbackAddress();
    private int port = 0;
    private String encKey = "";
    private Optional<GreeScanResponseDTO> scanResponseGson = Optional.empty();
    private Optional<GreeStatusResponseDTO> statusResponseGson = Optional.empty();
    private Optional<GreeStatusResponsePackDTO> prevStatusResponsePackGson = Optional.empty();

    public void getDeviceStatus(DatagramSocket clientSocket) throws GreeException {
        try {
            // Set the values in the HashMap
            ArrayList<String> columns = new ArrayList<>();
            columns.add(GREE_PROP_POWER);
            columns.add(GREE_PROP_MODE);
            columns.add(GREE_PROP_SETTEMP);
            columns.add(GREE_PROP_WINDSPEED);
            columns.add(GREE_PROP_AIR);
            columns.add(GREE_PROP_DRY);
            columns.add(GREE_PROP_HEALTH);
            columns.add(GREE_PROP_SLEEP);
            columns.add(GREE_PROP_LIGHT);
            columns.add(GREE_PROP_SWINGLEFTRIGHT);
            columns.add(GREE_PROP_SWINGUPDOWN);
            columns.add(GREE_PROP_QUIET);
            columns.add(GREE_PROP_TURBO);
            columns.add(GREE_PROP_TEMPUNIT);
            columns.add(GREE_PROP_HEAT);
            columns.add(GREE_PROP_HEATCOOL);
            columns.add(GREE_PROP_TEMPREC);
            columns.add(GREE_PROP_PWR_SAVING);
            columns.add("NoiseSet");

            // Convert the parameter map values to arrays
            String[] colArray = columns.toArray(new String[0]);

            // Prep the Command Request pack
            GreeReqStatusPackDTO reqStatusPackGson = new GreeReqStatusPackDTO();
            reqStatusPackGson.t = GREE_CMDT_STATUS;
            reqStatusPackGson.cols = colArray;
            reqStatusPackGson.mac = getId();
            String reqStatusPackStr = gson.toJson(reqStatusPackGson);

            // Encrypt the Binding Request pack
            // Prep and send Status Request
            String encryptedStatusReqPacket = GreeCryptoUtil.encryptPack(getKey().getBytes(), reqStatusPackStr);
            DatagramPacket sendPacket = createPackRequest(0,
                    new String(encryptedStatusReqPacket.getBytes(), UTF8_CHARSET));
            clientSocket.send(sendPacket);

            // Recieve a response
            JsonReader receivedData = receiveResponse(clientSocket);

            // Keep a copy of the old response to be used to check if values have changed
            // If first time running, there will not be a previous GreeStatusResponsePack4Gson
            if (statusResponseGson.isPresent() && statusResponseGson.get().packJson != null) {
                prevStatusResponsePackGson = Optional
                        .of(new GreeStatusResponsePackDTO(statusResponseGson.get().packJson));
            }

            // Read the response
            GreeStatusResponseDTO resp = gson.fromJson(receivedData, GreeStatusResponseDTO.class);
            resp.decryptedPack = GreeCryptoUtil.decryptPack(this.getKey().getBytes(), resp.pack);
            logger.trace("Response from device: {}", resp.decryptedPack);

            // Create the JSON to hold the response values
            resp.packJson = gson.fromJson(new JsonReader(new StringReader(resp.decryptedPack)),
                    GreeStatusResponsePackDTO.class);

            // save the results
            statusResponseGson = Optional.of(resp);
            updateTempFtoC();
        } catch (IOException e) {
            throw new GreeException("I/O exception while receiving data", e);
        }
    }

    public void bindWithDevice(DatagramSocket clientSocket) throws GreeException {
        byte[] receiveData = new byte[347];
        try {
            // Prep the Binding Request pack
            GreeBindRequestPackDTO bindReqPackGson = new GreeBindRequestPackDTO();
            bindReqPackGson.mac = getId();
            bindReqPackGson.t = GREE_CMDT_BIND;
            bindReqPackGson.uid = 0;
            String bindReqPackStr = gson.toJson(bindReqPackGson);

            // Now Encrypt the Binding Request pack
            String encryptedBindReqPacket = GreeCryptoUtil.encryptPack(GreeCryptoUtil.getAESGeneralKeyByteArray(),
                    bindReqPackStr);

            // Create and send bind request
            DatagramPacket sendPacket = createPackRequest(1,
                    new String(encryptedBindReqPacket.getBytes(), UTF8_CHARSET));
            clientSocket.send(sendPacket);

            // Recieve a response
            GreeBindResponseDTO resp = gson.fromJson(receiveResponse(clientSocket), GreeBindResponseDTO.class);
            resp.decryptedPack = GreeCryptoUtil.decryptPack(GreeCryptoUtil.getAESGeneralKeyByteArray(), resp.pack);

            // Create the JSON to hold the response values
            resp.packJson = gson.fromJson(new JsonReader(new StringReader(resp.decryptedPack)),
                    GreeBindResponsePackDTO.class);

            // Now set the key and flag to indicate the bind was succesful
            encKey = resp.packJson.key;

            // save the outcome
            setIsBound(true);
        } catch (IOException e) {
            throw new GreeException("Unable to bind to device", e);
        }
    }

    public void setDevicePower(DatagramSocket clientSocket, int value) throws GreeException {
        setCommandValue(clientSocket, GREE_PROP_POWER, value);
    }

    public void SetDeviceMode(DatagramSocket clientSocket, int value) throws GreeException {
        // Only allow this to happen if this device has been bound and values are valid
        if ((value < 0 || value > 4)) {
            throw new GreeException("Device mode out of range!");
        }
        setCommandValue(clientSocket, GREE_PROP_MODE, value);
    }

    public void setDeviceSwingUpDown(DatagramSocket clientSocket, int value) throws GreeException {
        // Only values 0,1,2,3,4,5,6,10,11 allowed
        if ((value < 0 || value > 11) || (value > 6 && value < 10)) {
            throw new GreeException("SwingUpDown value out of range!");
        }
        // Set the values in the HashMap
        setCommandValue(clientSocket, GREE_PROP_SWINGUPDOWN, value);
    }

    public void setDeviceSwingLeftRight(DatagramSocket clientSocket, int value) throws GreeException {
        // Set the values in the HashMap
        setCommandValue(clientSocket, GREE_PROP_SWINGLEFTRIGHT, value, 0, 6);
    }

    /**
     * Only allow this to happen if this device has been bound and values are valid
     * Possible values are :
     * 0 : Auto
     * 1 : Low
     * 2 : Medium Low
     * 3 : Medium
     * 4 : Medium High
     * 5 : High
     */
    public void setDeviceWindspeed(DatagramSocket clientSocket, int value) throws GreeException {
        if (value < 0 || value > 5) {
            throw new GreeException("Value out of range!");
        }

        // Set the values in the HashMap
        HashMap<String, Integer> parameters = new HashMap<>();
        parameters.put(GREE_PROP_WINDSPEED, value);
        parameters.put(GREE_PROP_QUIET, 0);
        parameters.put(GREE_PROP_TURBO, 0);
        parameters.put(GREE_PROP_NOISE, 0);
        executeCommand(clientSocket, parameters);
    }

    public void setDeviceTurbo(DatagramSocket clientSocket, int value) throws GreeException {
        // Only allow this to happen if this device has been bound and values are valid
        // Set the values in the HashMap
        setCommandValue(clientSocket, GREE_PROP_TURBO, value, 0, 1);
    }

    public void setQuietMode(DatagramSocket clientSocket, int value) throws GreeException {
        // Set the values in the HashMap
        setCommandValue(clientSocket, GREE_PROP_QUIET, value, 0, 2);
    }

    public int getDeviceTurbo() {
        return getIntStatusVal(GREE_PROP_TURBO);
    }

    public void setDeviceLight(DatagramSocket clientSocket, int value) throws GreeException {
        setCommandValue(clientSocket, GREE_PROP_LIGHT, value);
    }

    /**
     * @param value set temperature in degrees Celsius or Fahrenheit
     */
    public void setDeviceTempSet(DatagramSocket clientSocket, int value) throws GreeException {
        int newVal = value;
        int outVal = value;
        // Get Celsius or Fahrenheit from status message
        Integer CorF = getIntStatusVal(GREE_PROP_TEMPUNIT);
        // TODO put a param in openhab to allow setting this from the config

        // If commanding Fahrenheit set halfStep to 1 or 0 to tell the A/C which F integer
        // temperature to use as celsius alone is ambigious
        int halfStep = 0; // default to C

        int[] retList = validateTemperatureRangeForTempSet(newVal, CorF);
        newVal = retList[0];
        CorF = retList[1];

        if (CorF == 1) { // If Fahrenheit,
            // value argument is degrees F, convert Fahrenheit to Celsius,
            // SetTem input to A/C always in Celsius despite passing in 1 to TemUn
            outVal = (int) (Math.round((newVal - 32.) * 5.0 / 9.0)); // Integer Truncated
            // ******************TempRec TemSet Mapping for setting Fahrenheit****************************
            // F = [68. , 69. , 70. , 71. , 72. , 73. , 74. , 75. , 76. , 77. , 78. , 79. , 80. , 81. , 82. , 83. ,
            // 84. , 85. , 86. ]
            // C = [20.0, 20.5, 21.1, 21.6, 22.2, 22.7, 23.3, 23.8, 24.4, 25.0, 25.5, 26.1, 26.6, 27.2, 27.7, 28.3,
            // 28.8, 29.4, 30.0]
            // TemSet = [20, 21, 21, 22, 22, 23, 23, 24, 25, 25, 26, 26, 27, 27, 28, 28, 29, 29, 30, 30]
            // TemRec = [ 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0]
            // ******************TempRec TemSet Mapping for setting Fahrenheit****************************
            // subtract the float version - the int version to get the fractional difference
            // if the difference is positive set halfStep to 1, negative to 0
            halfStep = ((((newVal - 32.) * 5.0 / 9.0) - outVal) > 0) ? 1 : 0;
        }

        // Set the values in the HashMap
        HashMap<String, Integer> parameters = new HashMap<>();
        parameters.put(GREE_PROP_TEMPUNIT, CorF);
        parameters.put(GREE_PROP_SETTEMP, outVal);
        parameters.put(GREE_PROP_TEMPREC, halfStep);
        executeCommand(clientSocket, parameters);
    }

    public void setDeviceAir(DatagramSocket clientSocket, int value) throws GreeException {
        setCommandValue(clientSocket, GREE_PROP_AIR, value);
    }

    public void setDeviceDry(DatagramSocket clientSocket, int value) throws GreeException {
        setCommandValue(clientSocket, GREE_PROP_DRY, value);
    }

    public void setDeviceHealth(DatagramSocket clientSocket, int value) throws GreeException {
        setCommandValue(clientSocket, GREE_PROP_HEALTH, value);
    }

    public void setDevicePwrSaving(DatagramSocket clientSocket, int value) throws GreeException {
        // Set the values in the HashMap
        HashMap<String, Integer> parameters = new HashMap<>();
        parameters.put(GREE_PROP_PWR_SAVING, value);
        parameters.put(GREE_PROP_WINDSPEED, 0);
        parameters.put(GREE_PROP_QUIET, 0);
        parameters.put(GREE_PROP_TURBO, 0);
        parameters.put(GREE_PROP_SLEEP, 0);
        parameters.put(GREE_PROP_SLEEPMODE, 0);
        executeCommand(clientSocket, parameters);
    }

    public int getIntStatusVal(String valueName) {
        /*
         * Note : Values can be:
         * "Pow": Power (0 or 1)
         * "Mod": Mode: Auto: 0, Cool: 1, Dry: 2, Fan: 3, Heat: 4
         * "SetTem": Requested Temperature
         * "WdSpd": Fan Speed : Low:1, Medium Low:2, Medium :3, Medium High :4, High :5
         * "Air": Air Mode Enabled
         * "Blo": Dry
         * "Health": Health
         * "SwhSlp": Sleep
         * "SlpMod": ???
         * "Lig": Light On
         * "SwingLfRig": Swing Left Right
         * "SwUpDn": Swing Up Down: // Ceiling:0, Upwards : 10, Downwards : 11, Full range : 1
         * "Quiet": Quiet mode
         * "Tur": Turbo
         * "StHt": 0,
         * "TemUn": Temperature unit, 0 for Celsius, 1 for Fahrenheit
         * "HeatCoolType"
         * "TemRec": (0 or 1), Send with SetTem, when TemUn==1, distinguishes between upper and lower integer Fahrenheit
         * temp
         * "SvSt": Power Saving
         */
        // Find the valueName in the Returned Status object
        List<String> colList = Arrays.asList(statusResponseGson.get().packJson.cols);
        List<Integer> valList = Arrays.asList(statusResponseGson.get().packJson.dat);
        int valueArrayposition = colList.indexOf(valueName);
        if (valueArrayposition == -1) {
            return -1;
        }

        // Now get the Corresponding value
        Integer value = valList.get(valueArrayposition);
        return value;
    }

    public boolean hasStatusValChanged(String valueName) throws GreeException {
        if (!prevStatusResponsePackGson.isPresent()) {
            return true; // update value if there is no previous one
        }
        // Find the valueName in the Current Status object
        List<String> currcolList = Arrays.asList(statusResponseGson.get().packJson.cols);
        List<Integer> currvalList = Arrays.asList(statusResponseGson.get().packJson.dat);
        int currvalueArrayposition = currcolList.indexOf(valueName);
        if (currvalueArrayposition == -1) {
            throw new GreeException("Unable to decode device status");
        }

        // Find the valueName in the Previous Status object
        List<String> prevcolList = Arrays.asList(prevStatusResponsePackGson.get().cols);
        List<Integer> prevvalList = Arrays.asList(prevStatusResponsePackGson.get().dat);
        int prevvalueArrayposition = prevcolList.indexOf(valueName);
        if (prevvalueArrayposition == -1) {
            throw new GreeException("Unable to get status value");
        }

        // Finally Compare the values
        return currvalList.get(currvalueArrayposition) != prevvalList.get(prevvalueArrayposition);
    }

    protected void executeCommand(DatagramSocket clientSocket, Map<String, Integer> parameters) throws GreeException {
        // Only allow this to happen if this device has been bound
        if (!getIsBound()) {
            throw new GreeException("Device is not bound!");
        }

        try {
            // Convert the parameter map values to arrays
            String[] keyArray = parameters.keySet().toArray(new String[0]);
            Integer[] valueArray = parameters.values().toArray(new Integer[0]);

            // Prep the Command Request pack
            GreeExecuteCommandPackDTO execCmdPackGson = new GreeExecuteCommandPackDTO();
            execCmdPackGson.opt = keyArray;
            execCmdPackGson.p = valueArray;
            execCmdPackGson.t = GREE_CMDT_CMD;
            String execCmdPackStr = gson.toJson(execCmdPackGson);

            // Now Encrypt the Binding Request pack
            String encryptedCommandReqPacket = GreeCryptoUtil.encryptPack(getKey().getBytes(), execCmdPackStr);
            // String unencryptedCommandReqPacket = CryptoUtil.decryptPack(device.getKey().getBytes(),
            // encryptedCommandReqPacket);

            // Prep the Command Request
            DatagramPacket sendPacket = createPackRequest(0,
                    new String(encryptedCommandReqPacket.getBytes(), UTF8_CHARSET));
            clientSocket.send(sendPacket);

            GreeExecResponseDTO execResponseGson = gson.fromJson(receiveResponse(clientSocket),
                    GreeExecResponseDTO.class);
            execResponseGson.decryptedPack = GreeCryptoUtil.decryptPack(this.getKey().getBytes(),
                    execResponseGson.pack);

            // Create the JSON to hold the response values
            execResponseGson.packJson = gson.fromJson(new JsonReader(new StringReader(execResponseGson.decryptedPack)),
                    GreeExecResponsePackDTO.class);
        } catch (IOException e) {
            throw new GreeException("Exception on command execution", e);
        }
    }

    private void setCommandValue(DatagramSocket clientSocket, String command, int value) throws GreeException {
        executeCommand(clientSocket, Collections.singletonMap(command, value));
    }

    private void setCommandValue(DatagramSocket clientSocket, String command, int value, int min, int max)
            throws GreeException {
        // Only values 0,1,2,3,4,5,6 allowed
        if ((value < min) || (value > max)) {
            throw new GreeException("Command value out of range!");
        }
        executeCommand(clientSocket, Collections.singletonMap(command, value));
    }

    private DatagramPacket createPackRequest(int i, String pack) {
        GreeRequestDTO request = new GreeRequestDTO();
        request.cid = GREE_CID;
        request.i = i;
        request.t = GREE_CMDT_PACK;
        request.uid = 0;
        request.tcid = getId();
        request.pack = pack;
        byte[] sendData = gson.toJson(request).getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getAddress(), getPort());
        return sendPacket;
    }

    private JsonReader receiveResponse(DatagramSocket clientSocket) throws GreeException {
        try {
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            String data = new String(receivePacket.getData(), UTF8_CHARSET);
            return new JsonReader(new StringReader(data));
        } catch (IOException e) {
            throw new GreeException("Unable to receive response", e);
        }
    }

    /**
     * Checks input ranges for validity and TempUn for validity
     * Uses newVal as priority and tries to validate and determine intent
     * For example if value is 75 and TempUn says Celsius, change TempUn to Fahrenheit
     */
    private int[] validateTemperatureRangeForTempSet(int newValIn, @Nullable Integer CorFIn) {
        final String[] minMaxLUT = { "max", "min" }; // looks up 0 = C = max, 1 = F = min
        final String[] tempScaleLUT = { "C", "F" }; // Look Up Table used to convert TempUn integer 0,1 to "C" to "F"
                                                    // string for hashmap
        HashMap<String, Integer> nullCorFLUT = new HashMap<>(); // simple look up table for logic
        nullCorFLUT.put("C", 0);
        nullCorFLUT.put("F", 1);
        nullCorFLUT.put("INVALID", 0);

        String validRangeCorF; // stores if the input range is a valid C or F temperature

        // force to global min/max
        int newVal = (Math.max(newValIn, Math.min(tempRanges.get("C").get("min"), tempRanges.get("F").get("min"))));
        newVal = Math.min(newVal, Math.max(tempRanges.get("C").get("max"), tempRanges.get("F").get("max")));

        if ((newVal >= tempRanges.get("C").get("min")) && (newVal <= tempRanges.get("C").get("max"))) {
            validRangeCorF = "C";
        } else if ((newVal >= tempRanges.get("F").get("min")) && (newVal <= tempRanges.get("F").get("max"))) {
            validRangeCorF = "F";
        } else {
            logger.warn("Input Temp request {} is invalid", newVal);
            validRangeCorF = "INVALID";
        }

        // if CorF wasnt initialized or is null set it from lookup
        Integer CorF = CorFIn != null ? CorFIn : nullCorFLUT.get(validRangeCorF);

        if ((CorF == 1) && validRangeCorF.equals("C")) {
            CorF = 0; // input temp takes priority
        } else if ((CorF == 0) && validRangeCorF.equals("F")) {
            CorF = 1; // input temp takes priority
        } else if (validRangeCorF.equals("INVALID")) {
            // force min or max temp based on CorF scale to be used
            newVal = tempRanges.get(tempScaleLUT[CorF]).get(minMaxLUT[CorF]);
        }

        return new int[] { newVal, CorF };
    }

    /**
     * Create Hash Look Up for C and F
     * Temperature Ranges for gree A/C units (f_range = {86,61}, c_range={16,30}
     */
    private static HashMap<String, HashMap<String, Integer>> createTempRangeMap() {
        HashMap<String, HashMap<String, Integer>> tempRanges = new HashMap<>();
        HashMap<String, Integer> hmf = new HashMap<>();
        HashMap<String, Integer> hmc = new HashMap<>();

        hmf.put("min", 61); // F
        hmf.put("max", 86);
        tempRanges.put("F", hmf);

        hmc.put("min", 16); // C
        hmc.put("max", 30);
        tempRanges.put("C", hmc);

        return tempRanges;
    }

    private void updateTempFtoC() {
        // Status message back from A/C always reports degrees C
        // If using Fahrenheit, us SetTem, TemUn and TemRec to reconstruct the Fahrenheit temperature
        // Get Celsius or Fahrenheit from status message
        int CorF = getIntStatusVal(GREE_PROP_TEMPUNIT);
        int newVal = getIntStatusVal(GREE_PROP_SETTEMP);
        int halfStep = getIntStatusVal(GREE_PROP_TEMPREC);

        if ((CorF == -1) || (newVal == -1) || (halfStep == -1)) {
            throw new IllegalArgumentException("SetTem,TemUn or TemRec is invalid, not performing conversion");
        } else if (CorF == 1) { // convert SetTem to Fahrenheit
            // Find the valueName in the Returned Status object
            String[] columns = statusResponseGson.get().packJson.cols;
            Integer[] values = statusResponseGson.get().packJson.dat;
            List<String> colList = Arrays.asList(columns);
            int valueArrayposition = colList.indexOf(GREE_PROP_SETTEMP);
            if (valueArrayposition != -1) {
                // convert Celsius to Fahrenheit,
                // SetTem status returns degrees C regardless of TempUn setting

                // Perform the float Celsius to Fahrenheit conversion add or subtract 0.5 based on the value of TemRec
                // (0 = -0.5, 1 = +0.5). Pass into a rounding function, this yeild the correct Fahrenheit Temperature to
                // match A/C display
                newVal = (int) (Math.round(((newVal * 9.0 / 5.0) + 32.0) + halfStep - 0.5));

                // Update the status array with F temp, assume this is updating the array in situ
                values[valueArrayposition] = newVal;
            }
        }
    }

    public boolean getIsBound() {
        return isBound;
    }

    public void setIsBound(boolean isBound) {
        this.isBound = isBound;
    }

    public InetAddress getAddress() {
        return ipAddress;
    }

    public void setAddress(InetAddress address) {
        this.ipAddress = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getKey() {
        return encKey;
    }

    public String getId() {
        return scanResponseGson.isPresent() ? scanResponseGson.get().packJson.mac : "";
    }

    public String getName() {
        return scanResponseGson.isPresent() ? scanResponseGson.get().packJson.name : "";
    }

    public String getVendor() {
        return scanResponseGson.isPresent()
                ? scanResponseGson.get().packJson.brand + " " + scanResponseGson.get().packJson.vender
                : "";
    }

    public String getModel() {
        return scanResponseGson.isPresent()
                ? scanResponseGson.get().packJson.series + " " + scanResponseGson.get().packJson.model
                : "";
    }

    public void setScanResponseGson(GreeScanResponseDTO gson) {
        scanResponseGson = Optional.of(gson);
    }
}
