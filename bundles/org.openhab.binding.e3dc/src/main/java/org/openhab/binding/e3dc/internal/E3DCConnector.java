/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.e3dc.internal.rscp.RSCPData;
import org.openhab.binding.e3dc.internal.rscp.RSCPFrame;
import org.openhab.binding.e3dc.internal.rscp.RSCPTag;
import org.openhab.binding.e3dc.internal.rscp.util.AES256Helper;
import org.openhab.binding.e3dc.internal.rscp.util.BouncyAES256Helper;
import org.openhab.binding.e3dc.internal.rscp.util.ByteUtils;
import org.openhab.binding.e3dc.internal.rscp.util.FrameLoggerHelper;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link E3DCConnector} is responsible for handling the connection to E3DC, frame sending and receiving.
 *
 * @author Brendon Votteler - Initial Contribution
 * @author Björn Brings - Rework for OpenHAB
 */
public class E3DCConnector {
    private static final int maxRetries = 3;
    private static final long sleepMillisBeforeRetry = 5000;
    private static final Logger logger = LoggerFactory.getLogger(E3DCConnector.class);

    private @Nullable E3DCConfiguration config;
    private @Nullable E3DCHandler handle;
    private AES256Helper aesHelper;
    private Socket socket;

    public E3DCConnector(@NonNull E3DCHandler handle, E3DCConfiguration config) {
        this.handle = handle;
        this.config = config;

        final String aesPwd = config.getRscppassword(); // password set on E3DC for AES
        aesHelper = BouncyAES256Helper.createBouncyAES256Helper(aesPwd);

        connectE3DC();
    }

    /**
     * Connect & authenticate
     */
    public void connectE3DC() {
        if (config != null) {
            final String address = config.getIp();
            final int port = config.getPort();
            final String user = config.getWebusername(); // typically email address
            final String pwd = config.getWebpassword(); // used to log into E3DC portal

            logger.warn("Open connection to server {}:{} ...", address, port);
            try {
                openConnection(address, port);
                logger.warn("Sending authentication frame to server...");
                byte[] authFrame = E3DCRequests.buildAuthenticationMessage(user, pwd);
                Integer bytesSent = sendFrameToServer(aesHelper::encrypt, authFrame);
                byte[] decBytesReceived = receiveFrameFromServer(aesHelper::decrypt);
                logger.warn("Authentication: Received {} decrypted bytes from server.", decBytesReceived.length);
            } catch (UnknownHostException e) {
                handle.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Could not connect to host");
            } catch (IOException e) {
                handle.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Connection error");
            }
        }
    }

    public void setPowerLimitsUsed(Boolean value) {
        setBoolValue(RSCPTag.TAG_EMS_REQ_SET_POWER_SETTINGS, RSCPTag.TAG_EMS_POWER_LIMITS_USED, value);
    }

    public void setMaxDischargePower(int value) {
        setuint32CharValue(RSCPTag.TAG_EMS_REQ_SET_POWER_SETTINGS, RSCPTag.TAG_EMS_MAX_DISCHARGE_POWER, value);
    }

    public void setMaxChargePower(int value) {
        setuint32CharValue(RSCPTag.TAG_EMS_REQ_SET_POWER_SETTINGS, RSCPTag.TAG_EMS_MAX_CHARGE_POWER, value);
    }

    public void setDischargeStartPower(int value) {
        setuint32CharValue(RSCPTag.TAG_EMS_REQ_SET_POWER_SETTINGS, RSCPTag.TAG_EMS_DISCHARGE_START_POWER, value);
    }

    public void setWeatherRegulatedChargeEnable(Boolean value) {
        char charValue = (char) (value ? 1 : 0);
        setCharValue(RSCPTag.TAG_EMS_REQ_SET_POWER_SETTINGS, RSCPTag.TAG_EMS_WEATHER_REGULATED_CHARGE_ENABLED,
                charValue);
    }

    public void setPowerSaveEnable(Boolean value) {
        char charValue = (char) (value ? 1 : 0);
        setCharValue(RSCPTag.TAG_EMS_REQ_SET_POWER_SETTINGS, RSCPTag.TAG_EMS_POWERSAVE_ENABLED, charValue);
    }

    public void setuint32CharValue(RSCPTag containerTag, RSCPTag tag, int value) {
        logger.debug("setuint32CharValue container:{} tag:{} vale:{}", containerTag.name(), tag.name(), value);
        byte[] reqFrame = E3DCRequests.buildRequestSetFrame(containerTag, tag, value);
        handleRequest(reqFrame);
    }

    public void setCharValue(RSCPTag containerTag, RSCPTag tag, char value) {
        logger.tracetrace("setCharValue container:{} tag:{} vale:{}", containerTag.name(), tag.name(), value);
        byte[] reqFrame = E3DCRequests.buildRequestSetFrame(containerTag, tag, value);
        handleRequest(reqFrame);
    }

    public void setBoolValue(RSCPTag containerTag, RSCPTag tag, Boolean value) {
        logger.trace("setBoolValue container:{} tag:{} vale:{}", containerTag.name(), tag.name(), value);
        byte[] reqFrame = E3DCRequests.buildRequestSetFrame(containerTag, tag, value);
        handleRequest(reqFrame);
    }

    public void requestE3DCData() {
        byte[] reqFrame = E3DCRequests.buildRequestFrame();
        handleRequest(reqFrame);
    }

    public void handleRequest(byte[] reqFrame) {
        if (isNotConnected()) {
            connectE3DC();
        }
        logger.debug("Unencrypted frame to send: {}", ByteUtils.byteArrayToHexString(reqFrame));
        Integer bytesSent = sendFrameToServer(aesHelper::encrypt, reqFrame);
        byte[] decBytesReceived = receiveFrameFromServer(aesHelper::decrypt);
        logger.debug("Decrypted frame received: {}", ByteUtils.byteArrayToHexString(decBytesReceived));
        RSCPFrame responseFrame = RSCPFrame.builder().buildFromRawBytes(decBytesReceived);

        handleE3DCResponse(responseFrame);
        FrameLoggerHelper.logFrame(responseFrame);
    }

    public void handleE3DCResponse(RSCPFrame responseFrame) {
        List<RSCPData> dataList = responseFrame.getData();
        for (RSCPData data : dataList) {
            handleUpdateData(data);
        }
    }

    public void handleUpdateData(RSCPData data) {
        String dt = data.getDataTag().name();

        if ("TAG_EMS_POWER_PV".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPowerPV,
                    new QuantityType<>(data.getValueAsInt().orElse(-1), Units.WATT));
        } else if ("TAG_EMS_POWER_BAT".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPowerBat,
                    new QuantityType<>(data.getValueAsInt().orElse(-1), Units.WATT));
        } else if ("TAG_EMS_POWER_HOME".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPowerHome,
                    new QuantityType<>(data.getValueAsInt().orElse(-1), Units.WATT));
        } else if ("TAG_EMS_POWER_GRID".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPowerGrid,
                    new QuantityType<>(data.getValueAsInt().orElse(-1), Units.WATT));
        } else if ("TAG_EMS_POWER_ADD".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPowerAdd,
                    new QuantityType<>(data.getValueAsInt().orElse(-1), Units.WATT));
        } else if ("TAG_EMS_BAT_SOC".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_BatterySOC,
                    new QuantityType<>(data.getValueAsInt().orElse(-1), Units.PERCENT));
        } else if ("TAG_EMS_SELF_CONSUMPTION".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_SelfConsumption,
                    new QuantityType<>(data.getValueAsFloat().orElse((float) -1.0), Units.PERCENT));
        } else if ("TAG_EMS_AUTARKY".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_Autarky,
                    new QuantityType<>(data.getValueAsFloat().orElse((float) -1.0), Units.PERCENT));
        } else if ("TAG_PM_DATA".equals(dt)) {
            List<RSCPData> containedDataList = data.getContainerData();
            for (RSCPData containedData : containedDataList) {
                handleUpdatePMData(containedData);
            }
        } else if ("TAG_EMS_GET_POWER_SETTINGS".equals(dt)) {
            List<RSCPData> containedDataList = data.getContainerData();
            for (RSCPData containedData : containedDataList) {
                handleUpdatePowerSettingsData(containedData);
            }
        } else if ("TAG_EMS_SET_POWER_SETTINGS".equals(dt)) {
            List<RSCPData> containedDataList = data.getContainerData();
            for (RSCPData containedData : containedDataList) {
                handleUpdatePowerSettingsData(containedData);
            }
        } else if ("TAG_EMS_EMERGENCY_POWER_STATUS".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_EmergencyPowerStatus,
                    new DecimalType(data.getValueAsInt().orElse(-1)));
        } else if ("TAG_EP_IS_GRID_CONNECTED".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_GridConnected,
                    OnOffType.from(data.getValueAsBool().orElse(false)));
        } else if ("TAG_INFO_SW_RELEASE".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_SWRelease,
                    new StringType(data.getValueAsString().orElse("ERR")));
        }
    }

    private void handleUpdatePMData(RSCPData data) {
        String dt = data.getDataTag().name();

        if ("TAG_PM_ENERGY_L1".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPMEnergyL1,
                    new QuantityType<>(data.getValueAsDouble().orElse(-1.0), Units.WATT_HOUR));
        } else if ("TAG_PM_ENERGY_L2".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPMEnergyL2,
                    new QuantityType<>(data.getValueAsDouble().orElse(-1.0), Units.WATT_HOUR));
        } else if ("TAG_PM_ENERGY_L3".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPMEnergyL3,
                    new QuantityType<>(data.getValueAsDouble().orElse(-1.0), Units.WATT_HOUR));
        } else if ("TAG_PM_POWER_L1".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPMPowerL1,
                    new QuantityType<>(data.getValueAsDouble().orElse(-1.0), Units.WATT));
        } else if ("TAG_PM_POWER_L2".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPMPowerL2,
                    new QuantityType<>(data.getValueAsDouble().orElse(-1.0), Units.WATT));
        } else if ("TAG_PM_POWER_L3".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPMPowerL3,
                    new QuantityType<>(data.getValueAsDouble().orElse(-1.0), Units.WATT));
        } else if ("TAG_PM_VOLTAGE_L1".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPMVoltageL1,
                    new QuantityType<>(data.getValueAsFloat().orElse((float) -1.0), Units.VOLT));
        } else if ("TAG_PM_VOLTAGE_L2".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPMVoltageL2,
                    new QuantityType<>(data.getValueAsFloat().orElse((float) -1.0), Units.VOLT));
        } else if ("TAG_PM_VOLTAGE_L3".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_CurrentPMVoltageL3,
                    new QuantityType<>(data.getValueAsFloat().orElse((float) -1.0), Units.VOLT));
        } else if ("TAG_PM_MODE".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_Mode, new DecimalType(data.getValueAsInt().get()));
        }
    }

    private void handleUpdatePowerSettingsData(RSCPData data) {
        String dt = data.getDataTag().name();
        logger.debug("handleUpdatePowerSettingsData  : {}: {}", dt, data.getValueAsString());

        if ("TAG_EMS_POWER_LIMITS_USED".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_PowerLimitsUsed,
                    OnOffType.from(data.getValueAsBool().orElse(false)));
        } else if ("TAG_EMS_RES_TAG_EMS_POWER_LIMITS_USED".equals(dt)) {
            // maybe update TAG_EMS_POWER_LIMITS_USED...?
        } else if ("TAG_EMS_MAX_DISCHARGE_POWER".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_MaxDischarge,
                    new QuantityType<>(data.getValueAsInt().get(), Units.WATT));
        } else if ("TAG_EMS_RES_MAX_DISCHARGE_POWER".equals(dt)) {
            // maybe update TAG_EMS_MAX_DISCHARGE_POWER...?
        } else if ("TAG_EMS_MAX_CHARGE_POWER".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_MaxCharge,
                    new QuantityType<>(data.getValueAsInt().get(), Units.WATT));
        } else if ("TAG_EMS_RES_MAX_CHARGE_POWER".equals(dt)) {
            // maybe update TAG_EMS_MAX_CHARGE_POWER...?
        } else if ("TAG_EMS_DISCHARGE_START_POWER".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_DischargeStart,
                    new QuantityType<>(data.getValueAsInt().get(), Units.WATT));
        } else if ("TAG_EMS_RES_DISCHARGE_START_POWER".equals(dt)) {
            // maybe update TAG_EMS_DISCHARGE_START_POWER...?
        } else if ("TAG_EMS_WEATHER_REGULATED_CHARGE_ENABLED".equals(dt)) {
            handle.updateState(E3DCBindingConstants.CHANNEL_WeatherRegulatedCharge,
                    OnOffType.from(data.getValueAsBool().orElse(false)));
        } else if ("TAG_EMS_RES_WEATHER_REGULATED_CHARGE_ENABLE".equals(dt)) {
            // maybe update TAG_EMS_WEATHER_REGULATED_CHARGE_ENABLED...?
        } else if ("TAG_EMS_POWERSAVE_ENABLED".equals(dt)) {
            boolean result = data.getValueAsBool().orElse(false);

            handle.updateState(E3DCBindingConstants.CHANNEL_PowerSave, OnOffType.from(result));
        } else if ("TAG_EMS_RES_POWERSAVE_ENABLED".equals(dt)) {
            // maybe update TAG_EMS_POWERSAVE_ENABLED...?
        }
    }

    private boolean isNotConnected() {
        return socket == null || socket.isClosed();
    }

    public void close() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            logger.info("Couldn't close connection: {}", e);
        }
    }

    public void openConnection(String ipAddress, int port, int maxRetries, long sleepMillisBeforeRetry)
            throws IOException {
        socket = null;
        int retries = 0;
        while (isNotConnected() && retries++ < maxRetries) {
            try {
                socket = new Socket(ipAddress, port);
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(10000);
            } catch (UnknownHostException e) {
                socket.close();
                throw e;
            } catch (IOException e) {
                socket.close();
                if (retries < maxRetries) {
                    try {
                        Thread.sleep(sleepMillisBeforeRetry);
                    } catch (Exception ex) {
                        // ignore
                    }
                }
            } catch (Exception e) {
                throw e;
            }
        }

        if (socket == null) {
            // retries exhausted, still no connection
            throw new RuntimeException("Failed to establish connection to server.");
        }
    }

    public void openConnection(String ipAddress, int port) throws IOException {
        openConnection(ipAddress, port, maxRetries, sleepMillisBeforeRetry);
    }

    /**
     * Send a encrypt and send a byte array through a provided socket.
     *
     * @param socket The socket to write to.
     * @param encryptFunc A function to encrypt the provided frame.
     * @param frame The unencrypted frame as byte array.
     * @return Either an exception or the number of bytes sent.
     */
    public Integer sendFrameToServer(Function<byte[], byte[]> encryptFunc, byte[] frame) {
        if (isNotConnected()) {
            throw new IllegalStateException("Not connected to server. Must connect to server first before sending.");
        }

        try {
            byte[] encryptedFrame = encryptFunc.apply(frame);
            DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
            dOut.write(encryptedFrame);
            dOut.flush();
            return encryptedFrame.length;
        } catch (Exception e) {
            logger.error("Error while encrypting and sending frame.", e);
        }
        return null;
    }

    /**
     * Receive a frame from a socket and decrypted it.
     *
     * @param socket A socket to read from.
     * @param decryptFunc A function to decrypt the received byte array.
     * @return Either an exception or the decrypted response as byte array.
     */
    public byte[] receiveFrameFromServer(Function<byte[], byte[]> decryptFunc) {
        if (isNotConnected()) {
            throw new IllegalStateException("Not connected to server. Must connect to server first before sending.");
        }

        try {
            int totalBytesRead = 0;
            DataInputStream dIn = new DataInputStream(socket.getInputStream());
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            do {
                int bytesRead = dIn.read(data, 0, data.length);
                logger.info("Received {} bytes, append to buffer... ", bytesRead);
                if (bytesRead == -1) {
                    logger.warn("Socket closed unexpectedly by server.");
                    break;
                }
                buffer.write(data, 0, bytesRead);
                totalBytesRead += bytesRead;
            } while (dIn.available() > 0);

            logger.info("Finished reading {} bytes.", totalBytesRead);
            buffer.flush();

            byte[] decryptedData = decryptFunc.apply(buffer.toByteArray());
            logger.debug("Decrypted frame data.");

            return decryptedData;
        } catch (Exception e) {
            logger.error("Error while receiving and decrypting frame.", e);
        }
        return null;
    }
}
