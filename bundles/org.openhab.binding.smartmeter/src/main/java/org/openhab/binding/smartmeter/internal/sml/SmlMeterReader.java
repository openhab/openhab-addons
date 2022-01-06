/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.smartmeter.internal.sml;

import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartmeter.connectors.IMeterReaderConnector;
import org.openhab.binding.smartmeter.internal.MeterDevice;
import org.openhab.binding.smartmeter.internal.MeterValue;
import org.openhab.binding.smartmeter.internal.helper.ProtocolMode;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openmuc.jsml.structures.ASNObject;
import org.openmuc.jsml.structures.EMessageBody;
import org.openmuc.jsml.structures.SmlFile;
import org.openmuc.jsml.structures.SmlList;
import org.openmuc.jsml.structures.SmlListEntry;
import org.openmuc.jsml.structures.SmlMessage;
import org.openmuc.jsml.structures.SmlStatus;
import org.openmuc.jsml.structures.responses.SmlGetListRes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a SML capable device.
 *
 * @author Matthias Steigenberger - Initial contribution
 * @author Mathias Gilhuber - Also-By
 */
@NonNullByDefault
public final class SmlMeterReader extends MeterDevice<SmlFile> {

    protected final Logger logger = LoggerFactory.getLogger(SmlMeterReader.class);

    /**
     * Static factory method to create a SmlDevice object with a serial connector member.
     *
     * @param serialPortManagerSupplier
     *
     * @param deviceId the id of the device as defined in openHAB configuration.
     * @param pullRequestRequired identicates if SML values have to be actively requested.
     * @param serialPort the port where the device is connected as defined in openHAB configuration.
     * @param serialParameter
     * @param initMessage
     */
    public static SmlMeterReader createInstance(Supplier<SerialPortManager> serialPortManagerSupplier, String deviceId,
            String serialPort, byte @Nullable [] initMessage, int baudrate, int baudrateChangeDelay) {
        SmlMeterReader device = new SmlMeterReader(serialPortManagerSupplier, deviceId, serialPort, initMessage,
                baudrate, baudrateChangeDelay, ProtocolMode.SML);

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
    private SmlMeterReader(Supplier<SerialPortManager> serialPortManagerSupplier, String deviceId, String serialPort,
            byte @Nullable [] initMessage, int baudrate, int baudrateChangeDelay, ProtocolMode protocolMode) {
        super(serialPortManagerSupplier, deviceId, serialPort, initMessage, baudrate, baudrateChangeDelay,
                protocolMode);

        logger.debug("Created SmlDevice instance {} with serial connector on port {}", deviceId, serialPort);
    }

    /**
     * Decodes native SML informations from the device and stores them locally until the next read request.
     *
     * @param smlFile the native SML informations from the device
     */
    @Override
    protected void populateValueCache(SmlFile smlFile) {
        if (logger.isTraceEnabled()) {
            logger.trace("Read out following SML file: {}", System.lineSeparator());
            SmlFileDebugOutput.printFile(smlFile, (msg) -> logger.trace(msg));
        }
        List<SmlMessage> smlMessages = smlFile.getMessages();

        if (smlMessages != null) {
            int messageCount = smlMessages.size();

            if (messageCount <= 0) {
                logger.warn("{}: no valid SML messages list retrieved.", this.toString());
            }

            for (int i = 0; i < messageCount; i++) {
                SmlMessage smlMessage = smlMessages.get(i);

                int tag = smlMessage.getMessageBody().getTag().id();

                if (tag != EMessageBody.GET_LIST_RESPONSE.id()) {
                    continue;
                }

                SmlGetListRes listResponse = (SmlGetListRes) smlMessage.getMessageBody().getChoice();
                SmlList smlValueList = listResponse.getValList();
                SmlListEntry[] smlListEntries = smlValueList.getValListEntry();

                for (SmlListEntry entry : smlListEntries) {
                    SmlValueExtractor valueExtractor = new SmlValueExtractor(entry);
                    String obis = valueExtractor.getObisCode();

                    MeterValue<?> smlValue = getMeterValue(obis);

                    if (smlValue == null) {
                        smlValue = valueExtractor.getSmlValue();
                    }

                    SmlStatus status = entry.getStatus();
                    if (status != null) {
                        String statusValue = readStatus(status, obis);
                        if (statusValue != null) {
                            smlValue.setStatus(statusValue);
                        }
                    }

                    addObisCache(smlValue);
                }
            }
        } else {
            logger.warn("{}: no valid SML messages list retrieved.", this.toString());
        }
    }

    private @Nullable String readStatus(SmlStatus status, String obis) {
        ASNObject choice = status.getChoice();
        if (choice != null) {
            String statusValue = choice.toString();
            return statusValue;
        }
        return null;
    }

    @Override
    protected IMeterReaderConnector<SmlFile> createConnector(Supplier<SerialPortManager> serialPortManagerSupplier,
            String serialPort, int baudrate, int baudrateChangeDelay, ProtocolMode protocolMode) {
        return new SmlSerialConnector(serialPortManagerSupplier, serialPort, baudrate, baudrateChangeDelay);
    }

    @Override
    protected void printInfo() {
        super.printInfo();
    }
}
