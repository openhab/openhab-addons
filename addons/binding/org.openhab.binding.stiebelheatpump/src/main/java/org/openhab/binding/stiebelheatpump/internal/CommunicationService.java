/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.stiebelheatpump.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.joda.time.DateTime;
import org.openhab.binding.stiebelheatpump.stiebelheatpumpBindingConstants;
import org.openhab.binding.stiebelheatpump.protocol.DataParser;
import org.openhab.binding.stiebelheatpump.protocol.ProtocolConnector;
import org.openhab.binding.stiebelheatpump.protocol.RecordDefinition;
import org.openhab.binding.stiebelheatpump.protocol.Request;
import org.openhab.binding.stiebelheatpump.protocol.SerialConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommunicationService {

    private ProtocolConnector connector;
    private static int MAXRETRIES = 5;
    private final int INPUT_BUFFER_LENGTH = 1024;
    private byte buffer[] = new byte[INPUT_BUFFER_LENGTH];

    private static int waitingTime = 1200;

    DataParser parser = new DataParser();

    private Logger logger = LoggerFactory.getLogger(CommunicationService.class);

    public CommunicationService(List<Request> configuration, String serialPortName, int baudRate, int waitingTime)
            throws StiebelHeatPumpException {
        this.connector = new SerialConnector();
        connector.connect(serialPortName, baudRate);
        CommunicationService.waitingTime = waitingTime;
    }

    public void finalizer() {
        connector.disconnect();
    }

    /**
     * This method reads the version information from the heat pump
     *
     * @return version string, e.g: 2.06
     */
    public String getversion(Request versionRequest) throws StiebelHeatPumpException {
        String version = "";
        logger.debug("Loading version info ...");
        Map<String, String> data = readData(versionRequest);
        String versionKey = stiebelheatpumpBindingConstants.CHANNEL_VERSION;
        version = data.get(versionKey);
        return version;
    }

    /**
     * This method reads all settings defined in the heat pump configuration
     * from the heat pump
     *
     * @return map of heat pump setting values
     */
    public Map<String, String> getRequestData(List<Request> requests) throws StiebelHeatPumpException {
        Map<String, String> data = new HashMap<String, String>();
        for (Request request : requests) {
            logger.debug("Loading data for request {} ...", request.getName());
            try {
                Map<String, String> newData = readData(request);
                data.putAll(newData);
                if (requests.size() > 1) {
                    Thread.sleep(waitingTime);
                }
            } catch (InterruptedException e) {
                throw new StiebelHeatPumpException(e.toString());
            }
        }
        return data;
    }

    /**
     * This method set the time of the heat pump to the current time
     *
     * @return true if time has been updated
     */
    public Map<String, String> setTime(Request timeRequest) throws StiebelHeatPumpException {

        startCommunication();
        Map<String, String> data = new HashMap<String, String>();

        if (timeRequest == null) {
            logger.warn("Could not find request definition for time settings! Skip setting time.");
            return data;
        }

        try {
            // get time from heat pump
            logger.debug("Loading current time data ...");
            byte[] requestMessage = createRequestMessage(timeRequest);
            byte[] response = getData(requestMessage);
            data = parser.parseRecords(response, timeRequest);

            // get current time from local machine
            DateTime dt = DateTime.now();
            logger.debug("Current time is : {}", dt.toString());
            String weekday = Integer.toString(dt.getDayOfWeek());
            String day = Integer.toString(dt.getDayOfMonth());
            String month = Integer.toString(dt.getMonthOfYear());
            String year = Integer.toString(dt.getYearOfCentury());
            String seconds = Integer.toString(dt.getSecondOfMinute());
            String hours = Integer.toString(dt.getHourOfDay());
            String minutes = Integer.toString(dt.getMinuteOfHour());

            boolean updateRequired = false;

            for (RecordDefinition record : timeRequest.getRecordDefinitions()) {
                String fullname = record.getName();
                String entryValue = data.get(timeRequest.getName()
                        + stiebelheatpumpBindingConstants.CHANNELGROUPSEPERATOR + record.getName());

                if (fullname.equalsIgnoreCase("weekday") && !entryValue.equals(weekday)) {
                    updateRequired = true;
                    response = parser.composeRecord(weekday, response, record);
                    logger.debug("weekday needs update from {} to {}", entryValue, weekday);
                    continue;
                }
                if (fullname.equalsIgnoreCase("hours") && !entryValue.equals(hours)) {
                    updateRequired = true;
                    response = parser.composeRecord(hours, response, record);
                    logger.debug("Hours needs update from {} to {}", entryValue, hours);
                    continue;
                }
                if (fullname.equalsIgnoreCase("minutes") && !entryValue.equals(minutes)) {
                    updateRequired = true;
                    response = parser.composeRecord(minutes, response, record);
                    logger.debug("Minutes needs update from {} to {}", entryValue, minutes);
                    continue;
                }
                if (fullname.equalsIgnoreCase("seconds") && !entryValue.equals(seconds)) {
                    updateRequired = true;
                    response = parser.composeRecord(seconds, response, record);
                    logger.debug("Seconds needs update from {} to {}", entryValue, seconds);
                    continue;
                }
                if (fullname.equalsIgnoreCase("year") && !entryValue.equals(year)) {
                    updateRequired = true;
                    response = parser.composeRecord(year, response, record);
                    logger.debug("Year needs update from {} to {}", entryValue, year);
                    continue;
                }
                if (fullname.equalsIgnoreCase("month") && !entryValue.equals(month)) {
                    updateRequired = true;
                    response = parser.composeRecord(month, response, record);
                    logger.debug("Month needs update from {} to {}", entryValue, month);
                    continue;
                }
                if (fullname.equalsIgnoreCase("day") && !entryValue.equals(day)) {
                    updateRequired = true;
                    response = parser.composeRecord(day, response, record);
                    logger.debug("Day needs update from {} to {}", entryValue, day);
                    continue;
                }
            }

            if (updateRequired) {
                Thread.sleep(waitingTime);
                logger.info("Time need update. Set time to " + dt.toString());
                setData(response);

                Thread.sleep(waitingTime);
                response = getData(requestMessage);
                data = parser.parseRecords(response, timeRequest);
                dt = DateTime.now();
                logger.debug("Current time is : {}", dt.toString());

            }
            return data;

        } catch (InterruptedException e) {
            throw new StiebelHeatPumpException(e.toString());
        }
    }

    /**
     * This method reads all values defined in the request from the heat pump
     *
     * @param request
     *            definition to load the values from
     * @return map of heat pump values according request definition
     */
    public Map<String, String> readData(Request request) throws StiebelHeatPumpException {
        Map<String, String> data = new HashMap<String, String>();
        logger.debug("Request : Name -> {}, Description -> {} , RequestByte -> {}", request.getName(),
                request.getDescription(), DatatypeConverter.printHexBinary(new byte[] { request.getRequestByte() }));
        byte responseAvailable[] = new byte[0];
        byte requestMessage[] = createRequestMessage(request);
        boolean validData = false;
        try {
            while (!validData) {
                startCommunication();
                responseAvailable = getData(requestMessage);
                responseAvailable = parser.fixDuplicatedBytes(responseAvailable);
                validData = parser.headerCheck(responseAvailable);
                if (validData) {
                    data = parser.parseRecords(responseAvailable, request);
                    break;
                }
            }
        } catch (StiebelHeatPumpException e) {
            logger.error("Error reading data : {}", e.toString());
        }
        return data;
    }

    /**
     * This method updates the parameter item of a heat pump request
     *
     * @param value
     *            the new value of the item
     * @param parameter
     *            to be update in the heat pump
     */
    public Map<String, String> setData(String value, String parameter, List<Request> heatPumpSettingConfiguration)
            throws StiebelHeatPumpException {
        Request updateRequest = null;
        RecordDefinition updateRecord = null;
        Map<String, String> data = new HashMap<String, String>();

        if (parameter == null) {
            logger.debug("update parameter is NULL");
            return data;
        }

        String[] parts = parameter.split(Pattern.quote(stiebelheatpumpBindingConstants.CHANNELGROUPSEPERATOR), 2);
        if (parts.length != 2) {
            logger.debug("Parameter {} to update has invalid structure requestName#recordname", parameter);
        }
        String requestName = parts[0];
        String recordName = parts[1];

        // we lookup the right request definition that contains the parameter to
        // be updated
        for (Request request : heatPumpSettingConfiguration) {
            if (request.getName().equalsIgnoreCase(requestName)) {
                updateRequest = request;
                for (RecordDefinition record : request.getRecordDefinitions()) {
                    if (record.getName().equalsIgnoreCase(recordName)) {
                        updateRecord = record;
                        logger.debug("Found valid record definition {} in request {}:{}", record.getName(),
                                request.getName(), request.getDescription());
                        break;
                    }
                }
            }
        }

        if (updateRecord == null || updateRequest == null) {
            // did not find any valid record, do nothing
            logger.warn("Could not find valid record definition for {},  please verify thing definition.", parameter);
            return data;
        }

        if (Integer.parseInt(value) > updateRecord.getMax() || Integer.parseInt(value) < updateRecord.getMin()) {
            logger.warn("The record {} can not be set to value {} as allowed range is {}<-->{} !",
                    updateRecord.getName(), value, updateRecord.getMax(), updateRecord.getMin());
            return data;
        }

        try {
            // get actual value for the corresponding request, in case settings have changed locally
            // as we do no have individual requests for each settings we need to
            // decode the new value
            // into a current response , the response is available in the
            // connector object
            byte[] requestMessage = createRequestMessage(updateRequest);
            byte[] response = getData(requestMessage);
            response = parser.fixDuplicatedBytes(response);

            data = parser.parseRecords(response, updateRequest);

            // lookup parameter value in the data
            String currentState = data.get(parameter);
            if (currentState.equals(value)) {
                // current State is already same as new values!
                logger.debug("Current State for {} is already {}.", parameter, value);
                return data;
            }

            // create new set request out from the existing read response
            byte[] requestUpdateMessage = parser.composeRecord(value, response, updateRecord);
            logger.debug("Setting new value [{}] for parameter [{}]", value, parameter);

            Thread.sleep(waitingTime);

            response = setData(requestUpdateMessage);
            response = parser.fixDuplicatedBytes(response);

            if (parser.setDataCheck(response)) {
                logger.debug("Updated parameter {} successfully.", parameter);
            } else {
                logger.debug("Update for parameter {} failed!", parameter);
            }

        } catch (StiebelHeatPumpException e) {
            logger.error("Stiebel heat pump communication error during update of value! " + e.toString());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
        }
        return data;
    }

    /**
     * dumps response of connected heat pump by request byte
     *
     * @param requestByte
     *            request byte to send to heat pump
     */
    public void dumpResponse(byte requestByte) {
        int tmp = MAXRETRIES;
        MAXRETRIES = 1;
        logger.info(String.format("Prepare response for request byte %02X", requestByte));
        Request request = new Request();
        request.setRequestByte(requestByte);

        byte requestMessage[] = createRequestMessage(request);

        if (!establishRequest(requestMessage)) {
            logger.info(String.format("Could not get response for request byte %02X", requestByte));
            return;
        }
        MAXRETRIES = tmp;
        try {
            connector.write(DataParser.ESCAPE);
            byte[] response = receiveData();
            response = parser.fixDuplicatedBytes(response);
            logger.info("Request {} received response : {}", DataParser.bytesToHex(response),
                    DataParser.bytesToHex(response));

            boolean validData = parser.headerCheck(response);
            if (validData) {
                parser.parseRecords(response, request);
            }
            return;
        } catch (Exception e) {
            logger.error(String.format("Could not get data from heat pump! for request %02X, {}", requestByte,
                    e.toString()));
        }
        return;
    }

    /**
     * Gets data from connected heat pump
     *
     * @param request
     *            request bytes to send to heat pump
     * @return response bytes from heat pump
     *
     *         General overview of handshake between application and serial
     *         interface of heat pump
     *
     *         1. Sending request bytes ,
     *         e.g.: 01 00 FD FC 10 03 for version request
     *         01 -> header start
     *         00 -> get request
     *         FD -> checksum of request
     *         FC -> request byte
     *         10 03 -> Footer ending the communication
     *
     *         2. Receive a data available
     *         10 -> ok
     *         02 -> it does have data,which wants to send now
     *
     *         3. acknowledge sending data
     *         10 -> ok
     *
     *         4. receive data until footer
     *         01 -> header start
     *         00 -> get request
     *         CC -> checksum of send data
     *         FD -> request byte
     *         00 CE -> data, e.g. short value as 2 bytes -> 206 -> 2.06 version
     *         10 03 -> Footer ending the communication
     */
    private byte[] getData(byte request[]) {
        if (!establishRequest(request)) {
            return new byte[0];
        }
        try {
            connector.write(DataParser.ESCAPE);
            byte[] response = receiveData();
            return response;
        } catch (Exception e) {
            logger.error("Could not get data from heat pump! {}", e.toString());
            return buffer;
        }
    }

    /**
     * Sets setting value in heat pump
     *
     * @param request
     *            request bytes to send to heat pump
     * @return response bytes from heat pump
     *
     *         General overview of handshake between application and serial
     *         interface of heat pump
     *
     *         1. Sending request bytes, e.g update time in heat pump
     *         01 -> header start
     *         80 -> set request
     *         F1 -> checksum of request
     *         FC -> request byte
     *         00 02 0a 22 1b 0e 00 03 1a -> new values according record definition for time
     *         10 03 -> Footer ending the communication
     *
     *         2. Receive response message the confirmation message is ready for sending
     *         10 -> ok
     *         02 -> it does have data, which wants to send now
     *
     *         3. acknowledge sending data
     *         10 -> ok
     *
     *         4. receive confirmation message until footer
     *         01 -> header start
     *         80 -> set request
     *         7D -> checksum of send data
     *         FC -> request byte
     *         10 03 -> Footer ending the communication
     */
    private byte[] setData(byte[] request) throws StiebelHeatPumpException {
        try {
            startCommunication();
            establishRequest(request);
            // Acknowledge sending data
            connector.write(DataParser.ESCAPE);

        } catch (Exception e) {
            logger.error("Could not set data to heat pump! {}", e.toString());
            return new byte[0];
        }

        // finally receive data
        return receiveData();
    }

    /**
     * This method start the communication for the request It send the initial
     * handshake and expects a response
     */
    private void startCommunication() throws StiebelHeatPumpException {
        logger.debug("Sending start communication");
        byte response;
        try {
            connector.write(DataParser.STARTCOMMUNICATION);
            response = connector.get();
        } catch (Exception e) {
            logger.error("heat pump communication could not be established ! {}", e.getMessage());
            throw new StiebelHeatPumpException();
        }
        if (response != DataParser.ESCAPE) {
            logger.warn("heat pump is communicating, but did not receive Escape message in initial handshake!");
            throw new StiebelHeatPumpException(
                    "heat pump is communicating, but did not receive Escape message in initial handshake!");
        }
    }

    /**
     * This method establish the connection for the request It send the request
     * and expects a data available response
     *
     * @param request
     *            to be send to heat pump
     * @return true if data are available from heatpump
     */
    private boolean establishRequest(byte[] request) {
        int numBytesReadTotal = 0;
        boolean dataAvailable = false;
        int requestRetry = 0;
        int retry = 0;
        try {
            while (requestRetry < MAXRETRIES) {
                connector.write(request);
                retry = 0;
                byte singleByte;
                while ((!dataAvailable) & (retry < MAXRETRIES)) {
                    try {
                        singleByte = connector.get();
                    } catch (Exception e) {
                        retry++;
                        continue;
                    }
                    buffer[numBytesReadTotal] = singleByte;
                    numBytesReadTotal++;
                    if (buffer[0] != DataParser.DATAAVAILABLE[0] || buffer[1] != DataParser.DATAAVAILABLE[1]) {
                        continue;
                    }
                    dataAvailable = true;
                    return true;
                }
                logger.debug("retry request!");
                retry++;
                startCommunication();
            }
            if (!dataAvailable) {

                logger.warn("heat pump has no data available for request!");
                return false;
            }
        } catch (Exception e1) {
            logger.error("Could not get data from heat pump! {}", e1.toString());
            return false;
        }
        return true;
    }

    /**
     * This method receive the response from the heat pump It receive single
     * bytes until the end of message s detected
     *
     * @return bytes representing the data send from heat pump
     */
    private byte[] receiveData() {
        byte singleByte;
        int numBytesReadTotal;
        int retry;
        buffer = new byte[INPUT_BUFFER_LENGTH];
        retry = 0;
        numBytesReadTotal = 0;
        boolean endOfMessage = false;

        while (!endOfMessage & retry < MAXRETRIES) {
            try {
                singleByte = connector.get();
            } catch (Exception e) {
                // reconnect and try again to send request
                retry++;
                continue;
            }

            buffer[numBytesReadTotal] = singleByte;
            numBytesReadTotal++;

            if (numBytesReadTotal > 4 && buffer[numBytesReadTotal - 2] == DataParser.ESCAPE
                    && buffer[numBytesReadTotal - 1] == DataParser.END) {
                // we have reached the end of the response
                endOfMessage = true;
                logger.debug("reached end of response message.");
                break;
            }
        }

        byte[] responseBuffer = new byte[numBytesReadTotal];
        System.arraycopy(buffer, 0, responseBuffer, 0, numBytesReadTotal);
        return responseBuffer;
    }

    /**
     * This creates the request message ready to be send to heat pump
     *
     * @param request
     *            object containing necessary information to build request
     *            message
     * @return request message byte[]
     */
    private byte[] createRequestMessage(Request request) {
        short checkSum;
        byte[] requestMessage = new byte[] { DataParser.HEADERSTART, DataParser.GET, (byte) 0x00,
                request.getRequestByte(), DataParser.ESCAPE, DataParser.END };
        try {
            // prepare request message
            checkSum = parser.calculateChecksum(requestMessage);
            requestMessage[2] = parser.shortToByte(checkSum)[0];
            requestMessage = parser.addDuplicatedBytes(requestMessage);
        } catch (StiebelHeatPumpException e) {
        }
        return requestMessage;
    }
}
