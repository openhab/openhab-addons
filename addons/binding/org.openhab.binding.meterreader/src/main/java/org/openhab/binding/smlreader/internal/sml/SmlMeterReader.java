/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.smlreader.internal.sml;

import java.util.List;
import java.util.Map.Entry;

import org.openhab.binding.smlreader.MeterReaderBindingConstants;
import org.openhab.binding.smlreader.connectors.IMeterReaderConnector;
import org.openhab.binding.smlreader.connectors.SerialConnector;
import org.openhab.binding.smlreader.internal.MeterDevice;
import org.openhab.binding.smlreader.internal.MeterValue;
import org.openhab.binding.smlreader.internal.helper.ProtocolMode;
import org.openmuc.jsml.structures.EMessageBody;
import org.openmuc.jsml.structures.SmlFile;
import org.openmuc.jsml.structures.SmlList;
import org.openmuc.jsml.structures.SmlListEntry;
import org.openmuc.jsml.structures.SmlMessage;
import org.openmuc.jsml.structures.responses.SmlGetListRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a SML capable device.
 *
 * @author Mathias Gilhuber
 * @since 1.7.0
 */
public final class SmlMeterReader extends MeterDevice<SmlFile> {

    protected final Logger logger = LoggerFactory.getLogger(SmlMeterReader.class);

    /**
     * Static factory method to create a SmlDevice object with a serial connector member.
     *
     * @param deviceId the id of the device as defined in openHAB configuration.
     * @param pullRequestRequired identicates if SML values have to be actively requested.
     * @param serialPort the port where the device is connected as defined in openHAB configuration.
     * @param serialParameter
     * @param initMessage
     */
    public static SmlMeterReader createInstance(String deviceId, String serialPort, byte[] initMessage, int baudrate,
            int baudrateChangeDelay) {
        SmlMeterReader device = new SmlMeterReader(deviceId, serialPort, initMessage, baudrate, baudrateChangeDelay,
                ProtocolMode.SML);

        return device;
    }

    /**
     * Constructor to create a SmlDevice object with a serial connector member.
     *
     * @param deviceId the id of the device as defined in openHAB configuration.
     * @param serialPort the port where the device is connected as defined in openHAB configuration.
     * @param serialParameter
     * @param initMessage
     * @param baudrate
     */
    private SmlMeterReader(String deviceId, String serialPort, byte[] initMessage, int baudrate,
            int baudrateChangeDelay, ProtocolMode protocolMode) {
        super(deviceId, serialPort, initMessage, baudrate, baudrateChangeDelay, protocolMode);
        this.printMeterInfo = true;

        this.initMessage = initMessage;
        logger.debug("Created SmlDevice instance {} with serial connector on port {}", deviceId, serialPort);
    }

    /**
     * Logs the object information with all given SML values to OSGi console.
     *
     * It's only called once - except the config was updated.
     */
    private void printInfo() {
        if (this.getPrintMeterInfo()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.toString());
            stringBuilder.append(System.lineSeparator());

            for (Entry<String, MeterValue> entry : valueCache.entrySet()) {
                stringBuilder.append("Obis: " + entry.getKey() + " " + entry.getValue().toString());
                stringBuilder.append(System.lineSeparator());
            }

            logger.info("", stringBuilder);
            setPrintMeterInfo(false);
        }
    }

    /**
     * Gets if the object information has to be logged to OSGi console.
     *
     * @return true if the object information should be logged, otherwise false.
     */
    private Boolean getPrintMeterInfo() {
        return this.printMeterInfo;
    }

    /**
     * Sets if the object information has to be logged to OSGi console.
     */
    private void setPrintMeterInfo(Boolean printMeterInfo) {
        this.printMeterInfo = printMeterInfo;
    }

    /**
     * Converts hex encoded OBIS to formatted string.
     *
     * @return the hex encoded OBIS code as readable string.
     */
    private String getObisAsString(byte[] octetBytes) {
        String formattedObis = String.format(MeterReaderBindingConstants.OBIS_FORMAT, byteToInt(octetBytes[0]),
                byteToInt(octetBytes[1]), byteToInt(octetBytes[2]), byteToInt(octetBytes[3]), byteToInt(octetBytes[4]));

        return formattedObis;
    }

    /**
     * Byte to Integer conversion.
     *
     * @param byte to convert to Integer.
     */
    private int byteToInt(byte b) {
        return Integer.parseInt(String.format("%02x", b), 16);
    }

    /**
     * Decodes native SML informations from the device and stores them locally until the next read request.
     *
     * @param smlFile the native SML informations from the device
     */
    @Override
    protected void populateValueCache(SmlFile smlFile) {
        if (smlFile != null) {
            List<SmlMessage> smlMessages = smlFile.getMessages();

            if (smlMessages != null) {
                int messageCount = smlMessages.size();

                if (messageCount <= 0) {
                    logger.warn("{}: no valid SML messages list retrieved.", this.toString());
                }

                for (int i = 0; i < messageCount; i++) {
                    SmlMessage smlMessage = smlMessages.get(i);

                    if (smlMessage == null) {
                        logger.warn("{}: no valid SML message.", this.toString());
                        continue;
                    }

                    int tag = smlMessage.getMessageBody().getTag().getVal();

                    if (tag != EMessageBody.GET_LIST_RESPONSE.id()) {
                        continue;
                    }

                    SmlGetListRes listResponse = (SmlGetListRes) smlMessage.getMessageBody().getChoice();
                    SmlList smlValueList = listResponse.getValList();
                    SmlListEntry[] smlListEntries = smlValueList.getValListEntry();

                    for (SmlListEntry entry : smlListEntries) {
                        String obis = getObisAsString(entry.getObjName().getValue());

                        MeterValue smlValue = valueCache.get(obis);

                        if (smlValue == null) {
                            smlValue = new SmlValueExtractor(entry).getSmlValue();
                        }

                        valueCache.put(obis, smlValue);
                    }
                }

                printInfo();

            } else {
                logger.warn("{}: no valid SML messages list retrieved.", this.toString());
            }
        } else {
            logger.warn("{}: no valid SML File.", this.toString());
        }
    }

    @Override
    protected IMeterReaderConnector<SmlFile> createConnector(String serialPort, int baudrate, int baudrateChangeDelay,
            ProtocolMode protocolMode) {
        return new SerialConnector(serialPort, baudrate, baudrateChangeDelay);
    }

}
