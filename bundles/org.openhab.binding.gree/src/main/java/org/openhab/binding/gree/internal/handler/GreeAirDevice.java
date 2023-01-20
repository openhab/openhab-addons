/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
    private static final Gson GSON = new Gson();
    private boolean isBound = false;
    private final InetAddress ipAddress;
    private int port = 0;
    private String encKey = "";
    private Optional<GreeScanResponseDTO> scanResponseGson = Optional.empty();
    private Optional<GreeStatusResponseDTO> statusResponseGson = Optional.empty();
    private Optional<GreeStatusResponsePackDTO> prevStatusResponsePackGson = Optional.empty();

    public GreeAirDevice() {
        ipAddress = InetAddress.getLoopbackAddress();
    }

    public GreeAirDevice(InetAddress ipAddress, int port, GreeScanResponseDTO scanResponse) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.scanResponseGson = Optional.of(scanResponse);
    }

    public void getDeviceStatus(DatagramSocket clientSocket) throws GreeException {
        if (!isBound) {
            throw new GreeException("Device not bound");
        }
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
            columns.add(GREE_PROP_NOISESET);
            columns.add(GREE_PROP_CURRENT_TEMP_SENSOR);

            // Convert the parameter map values to arrays
            String[] colArray = columns.toArray(new String[0]);

            // Prep the Command Request pack
            GreeReqStatusPackDTO reqStatusPackGson = new GreeReqStatusPackDTO();
            reqStatusPackGson.t = GREE_CMDT_STATUS;
            reqStatusPackGson.cols = colArray;
            reqStatusPackGson.mac = getId();
            String reqStatusPackStr = GSON.toJson(reqStatusPackGson);

            // Encrypt and send the Status Request pack
            String encryptedStatusReqPacket = GreeCryptoUtil.encryptPack(getKey(), reqStatusPackStr);
            DatagramPacket sendPacket = createPackRequest(0,
                    new String(encryptedStatusReqPacket.getBytes(), StandardCharsets.UTF_8));
            clientSocket.send(sendPacket);

            // Keep a copy of the old response to be used to check if values have changed
            // If first time running, there will not be a previous GreeStatusResponsePack4Gson
            if (statusResponseGson.isPresent() && statusResponseGson.get().packJson != null) {
                prevStatusResponsePackGson = Optional
                        .of(new GreeStatusResponsePackDTO(statusResponseGson.get().packJson));
            }

            // Read the response, create the JSON to hold the response values
            GreeStatusResponseDTO resp = receiveResponse(clientSocket, GreeStatusResponseDTO.class);
            resp.decryptedPack = GreeCryptoUtil.decryptPack(getKey(), resp.pack);
            logger.debug("Response from device: {}", resp.decryptedPack);
            resp.packJson = GSON.fromJson(resp.decryptedPack, GreeStatusResponsePackDTO.class);

            // save the results
            statusResponseGson = Optional.of(resp);
            updateTempFtoC();
        } catch (IOException | JsonSyntaxException e) {
            throw new GreeException("I/O exception while updating status", e);
        } catch (RuntimeException e) {
            logger.debug("Exception", e);
            String json = statusResponseGson.map(r -> r.packJson.toString()).orElse("n/a");
            throw new GreeException("Exception while updating status, JSON=" + json, e);
        }
    }

    public void bindWithDevice(DatagramSocket clientSocket) throws GreeException {
        try {
            // Prep the Binding Request pack
            GreeBindRequestPackDTO bindReqPackGson = new GreeBindRequestPackDTO();
            bindReqPackGson.mac = getId();
            bindReqPackGson.t = GREE_CMDT_BIND;
            bindReqPackGson.uid = 0;
            String bindReqPackStr = GSON.toJson(bindReqPackGson);

            // Encrypt and send the Binding Request pack
            String encryptedBindReqPacket = GreeCryptoUtil.encryptPack(GreeCryptoUtil.getAESGeneralKeyByteArray(),
                    bindReqPackStr);
            DatagramPacket sendPacket = createPackRequest(1, encryptedBindReqPacket);
            clientSocket.send(sendPacket);

            // Recieve a response, create the JSON to hold the response values
            GreeBindResponseDTO resp = receiveResponse(clientSocket, GreeBindResponseDTO.class);
            resp.decryptedPack = GreeCryptoUtil.decryptPack(GreeCryptoUtil.getAESGeneralKeyByteArray(), resp.pack);
            resp.packJson = GSON.fromJson(resp.decryptedPack, GreeBindResponsePackDTO.class);

            // Now set the key and flag to indicate the bind was successful
            encKey = resp.packJson.key;

            // save the outcome
            isBound = true;
        } catch (IOException | JsonSyntaxException e) {
            throw new GreeException("Unable to bind to device", e);
        }
    }

    public void setDevicePower(DatagramSocket clientSocket, int value) throws GreeException {
        setCommandValue(clientSocket, GREE_PROP_POWER, value);
    }

    public void setDeviceMode(DatagramSocket clientSocket, int value) throws GreeException {
        if ((value < 0 || value > 4)) {
            throw new GreeException("Device mode out of range!");
        }
        setCommandValue(clientSocket, GREE_PROP_MODE, value);
    }

    /**
     * SwUpDn: controls the swing mode of the vertical air blades
     *
     * 0: default
     * 1: swing in full range
     * 2: fixed in the upmost position (1/5)
     * 3: fixed in the middle-up position (2/5)
     * 4: fixed in the middle position (3/5)
     * 5: fixed in the middle-low position (4/5)
     * 6: fixed in the lowest position (5/5)
     * 7: swing in the downmost region (5/5)
     * 8: swing in the middle-low region (4/5)
     * 9: swing in the middle region (3/5)
     * 10: swing in the middle-up region (2/5)
     * 11: swing in the upmost region (1/5)
     */
    public void setDeviceSwingUpDown(DatagramSocket clientSocket, int value) throws GreeException {
        if (value < 0 || value > 11) {
            throw new GreeException("SwingUpDown value is out of range!");
        }
        setCommandValue(clientSocket, GREE_PROP_SWINGUPDOWN, value);
    }

    /**
     * SwingLfRig: controls the swing mode of the horizontal air blades (available on limited number of devices, e.g.
     * some Cooper & Hunter units - thanks to mvmn)
     *
     * 0: default
     * 1: full swing
     * 2-6: fixed position from leftmost to rightmost
     * Full swing, like for SwUpDn is not supported
     */
    public void setDeviceSwingLeftRight(DatagramSocket clientSocket, int value) throws GreeException {
        if (value < 0 || value > 6) {
            throw new GreeException("SwingLeftRight value is out of range!");
        }
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

        HashMap<String, Integer> parameters = new HashMap<>();
        parameters.put(GREE_PROP_WINDSPEED, value);
        parameters.put(GREE_PROP_QUIET, 0);
        parameters.put(GREE_PROP_TURBO, 0);
        parameters.put(GREE_PROP_NOISE, 0);
        executeCommand(clientSocket, parameters);
    }

    /**
     * Tur: sets fan speed to the maximum. Fan speed cannot be changed while active and only available in Dry and Cool
     * mode.
     *
     * 0: off
     * 1: on
     */
    public void setDeviceTurbo(DatagramSocket clientSocket, int value) throws GreeException {
        setCommandValue(clientSocket, GREE_PROP_TURBO, value, 0, 1);
    }

    public void setQuietMode(DatagramSocket clientSocket, int value) throws GreeException {
        setCommandValue(clientSocket, GREE_PROP_QUIET, value, 0, 2);
    }

    public void setDeviceLight(DatagramSocket clientSocket, int value) throws GreeException {
        setCommandValue(clientSocket, GREE_PROP_LIGHT, value);
    }

    /**
     * @param value set temperature in degrees Celsius or Fahrenheit
     */
    public void setDeviceTempSet(DatagramSocket clientSocket, QuantityType<?> temp) throws GreeException {
        // If commanding Fahrenheit set halfStep to 1 or 0 to tell the A/C which F integer
        // temperature to use as celsius alone is ambigious
        double newVal = temp.doubleValue();
        int celsiusOrFahrenheit = SIUnits.CELSIUS.equals(temp.getUnit()) ? TEMP_UNIT_CELSIUS : TEMP_UNIT_FAHRENHEIT; // 0=Celsius,
        // 1=Fahrenheit
        if (((celsiusOrFahrenheit == TEMP_UNIT_CELSIUS) && (newVal < TEMP_MIN_C || newVal > TEMP_MAX_C))
                || ((celsiusOrFahrenheit == TEMP_UNIT_FAHRENHEIT) && (newVal < TEMP_MIN_F || newVal > TEMP_MAX_F))) {
            throw new IllegalArgumentException("Temp Value out of Range");
        }

        // Default for Celsius
        int outVal = (int) newVal;
        int halfStep = TEMP_HALFSTEP_NO; // for whatever reason halfStep is not supported for Celsius

        // If value argument is degrees F, convert Fahrenheit to Celsius,
        // SetTem input to A/C always in Celsius despite passing in 1 to TemUn
        // ******************TempRec TemSet Mapping for setting Fahrenheit****************************
        // F = [68...86]
        // C = [20.0, 20.5, 21.1, 21.6, 22.2, 22.7, 23.3, 23.8, 24.4, 25.0, 25.5, 26.1, 26.6, 27.2, 27.7, 28.3,
        // 28.8, 29.4, 30.0]
        //
        // TemSet = [20..30] or [68..86]
        // TemRec = value - (value) > 0 ? 1 : 1 -> when xx.5 is request xx will become TemSet and halfStep the indicator
        // for "half on top of TemSet"
        // ******************TempRec TemSet Mapping for setting Fahrenheit****************************
        // subtract the float version - the int version to get the fractional difference
        // if the difference is positive set halfStep to 1, negative to 0
        if (celsiusOrFahrenheit == TEMP_UNIT_FAHRENHEIT) { // If Fahrenheit,
            halfStep = newVal - outVal > 0 ? TEMP_HALFSTEP_YES : TEMP_HALFSTEP_NO;
        }
        logger.debug("Converted temp from {}{} to temp={}, halfStep={}, unit={})", newVal, temp.getUnit(), outVal,
                halfStep, celsiusOrFahrenheit == TEMP_UNIT_CELSIUS ? "C" : "F");

        // Set the values in the HashMap
        HashMap<String, Integer> parameters = new HashMap<>();
        parameters.put(GREE_PROP_TEMPUNIT, celsiusOrFahrenheit);
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
        if (isStatusAvailable()) {
            List<String> colList = Arrays.asList(statusResponseGson.get().packJson.cols);
            List<Integer> valList = Arrays.asList(statusResponseGson.get().packJson.dat);
            int valueArrayposition = colList.indexOf(valueName);
            if (valueArrayposition != -1) {
                // get the Corresponding value
                Integer value = valList.get(valueArrayposition);
                return value;
            }
        }

        return -1;
    }

    public boolean isStatusAvailable() {
        return statusResponseGson.isPresent() && (statusResponseGson.get().packJson.cols != null)
                && (statusResponseGson.get().packJson.dat != null);
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
        return !Objects.equals(currvalList.get(currvalueArrayposition), prevvalList.get(prevvalueArrayposition));
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
            String execCmdPackStr = GSON.toJson(execCmdPackGson);

            // Now encrypt and send the Command Request pack
            String encryptedCommandReqPacket = GreeCryptoUtil.encryptPack(getKey(), execCmdPackStr);
            DatagramPacket sendPacket = createPackRequest(0, encryptedCommandReqPacket);
            clientSocket.send(sendPacket);

            // Receive and decode result
            GreeExecResponseDTO execResponseGson = receiveResponse(clientSocket, GreeExecResponseDTO.class);
            execResponseGson.decryptedPack = GreeCryptoUtil.decryptPack(getKey(), execResponseGson.pack);

            // Create the JSON to hold the response values
            execResponseGson.packJson = GSON.fromJson(execResponseGson.decryptedPack, GreeExecResponsePackDTO.class);
        } catch (IOException | JsonSyntaxException e) {
            throw new GreeException("Exception on command execution", e);
        }
    }

    private void setCommandValue(DatagramSocket clientSocket, String command, int value) throws GreeException {
        executeCommand(clientSocket, Collections.singletonMap(command, value));
    }

    private void setCommandValue(DatagramSocket clientSocket, String command, int value, int min, int max)
            throws GreeException {
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
        byte[] sendData = GSON.toJson(request).getBytes(StandardCharsets.UTF_8);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, port);
        return sendPacket;
    }

    private <T> T receiveResponse(DatagramSocket clientSocket, Class<T> classOfT)
            throws IOException, JsonSyntaxException {
        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        String json = new String(receivePacket.getData(), StandardCharsets.UTF_8).replace("\\u0000", "").trim();
        return GSON.fromJson(json, classOfT);
    }

    private void updateTempFtoC() {
        // Status message back from A/C always reports degrees C
        // If using Fahrenheit, us SetTem, TemUn and TemRec to reconstruct the Fahrenheit temperature
        // Get Celsius or Fahrenheit from status message
        int celsiusOrFahrenheit = getIntStatusVal(GREE_PROP_TEMPUNIT);
        int newVal = getIntStatusVal(GREE_PROP_SETTEMP);
        int halfStep = getIntStatusVal(GREE_PROP_TEMPREC);

        if ((celsiusOrFahrenheit == -1) || (newVal == -1) || (halfStep == -1)) {
            throw new IllegalArgumentException("SetTem,TemUn or TemRec is invalid, not performing conversion");
        } else if (celsiusOrFahrenheit == 1) { // convert SetTem to Fahrenheit
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

    public InetAddress getAddress() {
        return ipAddress;
    }

    public boolean getIsBound() {
        return isBound;
    }

    public byte[] getKey() {
        return encKey.getBytes(StandardCharsets.UTF_8);
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
