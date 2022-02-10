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

import static org.openhab.binding.e3dc.internal.E3DCBindingConstants.*;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
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
 * The {@link E3DCConnector} is responsible for handling the connection to E3DC,
 * frame sending and receiving.
 *
 * @author Brendon Votteler - Initial Contribution
 * @author Björn Brings - Rework for OpenHAB
 * @author Marco Loose - Extensions & Refactorings
 */
public class E3DCConnector {

    private static final int SOCKET_TIMEOUT = 10000;
    private static final int maxRetries = 3;
    private static final long sleepMillisBeforeRetry = 5000;
    private static final Logger logger = LoggerFactory.getLogger(E3DCConnector.class);

    private @NonNull E3DCConfiguration config;
    private @NonNull E3DCHandler handle;
    private AES256Helper aesHelper;
    private @Nullable Socket socket;
    private boolean doDebugQuery = false;

    public E3DCConnector(E3DCHandler handle, E3DCConfiguration config) {
        this.handle = handle;
        this.config = config;

        final String aesPwd = config.getRscppassword(); // password set on E3DC for AES
        aesHelper = BouncyAES256Helper.createBouncyAES256Helper(aesPwd);

        if (connectE3DC()) {
            queryDevices();
            handle.addChannelsFromDevices();
        requestInitialData();
    }
    }

    /**
     * Connect & authenticate
     */
    public boolean connectE3DC() {
        boolean success = false;

            final String address = config.getIp();
            final int port = config.getPort();
            final String user = config.getWebusername(); // typically email address
            final String pwd = config.getWebpassword(); // used to log into E3DC portal

        // TODO move to E3DCHandler
        E3DCRequests.setDcCount(config.getTrackerCount());

        logger.debug("getPmCount:{}  getWbCount:{} getDcCount:{} getPviCount:{}", E3DCRequests.getPmCount(),
                E3DCRequests.getWbCount(), E3DCRequests.getDcCount(), E3DCRequests.getPviCount());

        logger.debug("Opening connection to server {}:{} ...", address, port);
            try {
                openConnection(address, port);

                logger.debug("Sending authentication frame to server...");
                byte[] authFrame = E3DCRequests.buildAuthenticationMessage(user, pwd);
                Integer bytesSent = sendFrameToServer(aesHelper::encrypt, authFrame);
                byte[] decBytesReceived = receiveFrameFromServer(aesHelper::decrypt);

                if (logger.isDebugEnabled()) {
                    logger.debug("Authentication: Sent {} bytes to server. Received {} decrypted bytes from server.",
                            bytesSent, decBytesReceived == null ? decBytesReceived : decBytesReceived.length);
                }

                if (decBytesReceived == null || decBytesReceived.length == 0) {
                    handle.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "No data received for authentication request");
                    close();
                } else {
                    handle.updateStatus(ThingStatus.ONLINE);
                    success = true;
                }

            } catch (UnknownHostException e) {
                handle.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Could not connect to host");
            } catch (IOException e) {
            handle.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Connection error: " + e.getMessage());
        } catch (RuntimeException e) {
            handle.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Connection error: " + e.getMessage());
            }

        return success;
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

    public void setDebugQuery(boolean value) {
        doDebugQuery = value;
        handle.updateState(CHANNEL_GROUP_DEBUG + "#" + CHANNEL_DebugQuery,
                (doDebugQuery ? OnOffType.ON : OnOffType.OFF));
    }

    public void setuint32CharValue(RSCPTag containerTag, RSCPTag tag, int value) {
        logger.trace("setuint32CharValue container:{} tag:{} vale:{}", containerTag.name(), tag.name(), value);
        byte[] reqFrame = E3DCRequests.buildRequestSetFrame(containerTag, tag, value);
        handleRequest(reqFrame);
    }

    public void setCharValue(RSCPTag containerTag, RSCPTag tag, char value) {
        logger.trace("setCharValue container:{} tag:{} vale:{}", containerTag.name(), tag.name(), value);
        byte[] reqFrame = E3DCRequests.buildRequestSetFrame(containerTag, tag, value);
        handleRequest(reqFrame);
    }

    public void setBoolValue(RSCPTag containerTag, RSCPTag tag, Boolean value) {
        logger.trace("setBoolValue container:{} tag:{} vale:{}", containerTag.name(), tag.name(), value);
        byte[] reqFrame = E3DCRequests.buildRequestSetFrame(containerTag, tag, value);
        handleRequest(reqFrame);
    }

    public void requestInitialData() {
        logger.trace("requestInitialData");
        byte[] reqFrame = E3DCRequests.buildRequestFrameDebug(4);
        handleRequest(reqFrame);
    }

    public void queryDevices() {
        logger.trace("queryDevices");
        byte[] reqFrame = E3DCRequests.buildRequestDevices();
        ;
        handleRequest(reqFrame);
    }

    public void requestE3DCData() {

        byte[] reqFrame;

        if (doDebugQuery) {
            setDebugQuery(false);

            logger.trace("START of complete data query -------------------------- ");

            reqFrame = E3DCRequests.buildRequestFrameDebug(0);
            handleRequest(reqFrame);

            reqFrame = E3DCRequests.buildRequestFrameDebug(1);
            handleRequest(reqFrame);

            reqFrame = E3DCRequests.buildRequestFrameDebug(2);
            handleRequest(reqFrame);

            reqFrame = E3DCRequests.buildRequestFrameDebug(3);
            handleRequest(reqFrame);

            for (int i = 10; i < 15; i++) {
                try {
                    reqFrame = E3DCRequests.buildRequestFrameDebug(i);
                    handleRequest(reqFrame);
                } catch (Exception e) {
                    logger.trace("requestE3DCData experiment {} failed.", i, e);
                }
            }

            logger.trace("END of complete data query -------------------------- ");

        } else {
            // reqFrame = E3DCRequests.buildRequestFrameBase();
            // handleRequest(reqFrame);

            reqFrame = E3DCRequests.buildRequestFrameDebug(5);
            handleRequest(reqFrame);

            reqFrame = E3DCRequests.buildRequestFrameDebug(3);
            handleRequest(reqFrame);
        }
    }

    long connnectFails = 0L;
    long requestOKs = 0L;
    long lastRequestOKs = 0L;

    public void handleRequest(byte[] reqFrame) {

        if (isNotConnected() && !connectE3DC()) {
            connnectFails++;
            lastRequestOKs = 0L;
            return;
        }

        if (reqFrame == null || reqFrame.length == 0) {
            logger.warn("Skipping empty RSCP request...");
            return;
        }

        lastRequestOKs++;
        requestOKs++;

        Integer bytesSent = 0;
        byte[] decBytesReceived = null;

        try {
        logger.trace("OK: {} - OK ges.: {} - Fail: {}", lastRequestOKs, requestOKs, connnectFails);

        logger.trace("Unencrypted frame to send: {}", ByteUtils.byteArrayToHexString(reqFrame));
            bytesSent = sendFrameToServer(aesHelper::encrypt, reqFrame);
        } catch (IllegalStateException e) {
            logger.error("Error on sending data request.", e);
            close();
        }

        try {
            decBytesReceived = receiveFrameFromServer(aesHelper::decrypt);

        if (logger.isTraceEnabled())
            logger.trace("Sent {} bytes to server. Decrypted frame received: {}", bytesSent,
                    decBytesReceived == null ? decBytesReceived : ByteUtils.byteArrayToHexString(decBytesReceived));
        } catch (IllegalStateException e) {
            logger.error("Error on receiving data.", e);
            close();
        }

        try {
        if (decBytesReceived != null && decBytesReceived.length > 0) {
            RSCPFrame responseFrame = RSCPFrame.builder().buildFromRawBytes(decBytesReceived);

            handleE3DCResponse(responseFrame);
            FrameLoggerHelper.logFrame(responseFrame);
        }
        } catch (Exception e) {
            logger.error("Error on reading received data.", e);
        }
    }

    public void handleE3DCResponse(RSCPFrame responseFrame) {
        List<RSCPData> dataList = responseFrame.getData();
        for (RSCPData data : dataList) {

            handleUpdateData(data);
        }
    }

    public String padLeftZeros(int inputString, int length) {
        return String.format("%1$" + length + "s", inputString).replace(' ', '0');
    }

    public String padLeftZeros(String inputString, int length) {
        return String.format("%1$" + length + "s", inputString).replace(' ', '0');
    }

    public void handleUpdateData(RSCPData data) {
        var tag = data.getDataTag();
        var tagNamespace = data.getDataNamespace();

        if (logger.isTraceEnabled()) {
            final int RES_TAG_MASK = 0x00800000;
            var tagBytes = tag.getValueAsBytes();
            int tagInt = int32Converter(tagBytes, 0);
            String tagName = tag.name();

            // int subTag = tagInt & SUB_TAG_MASK;
            int reply = tagInt & RES_TAG_MASK;

            logger.trace(" >> INCOMING TAG: {} - namespace: {} - subTag: {} - response: {}", //
                    String.format("%1$" + 45 + "s (%2$s)", tagName, bytesToHex(tagBytes)), //
                    tagNamespace, //
                    bytesToHex(tagBytes).substring(2, 8), //
                    String.format("%1$" + 5 + "s", reply == RES_TAG_MASK));
        }

        switch (tagNamespace) { // TODO to map namespace to interface/class with updater method
            case PM:
                handlePM(data);
                break;
            case EMS:
                handleEMS(data);
                break;
            case PVI:
                handlePVI(data);
                break;
            case BAT:
                handleBAT(data);
                break;
            case DCDC:
                handleDCDC(data);
                break;
            case INFO:
                handleINFO(data);
                break;
            case RSCP:
                handleRSCP(data);
                break;

            case DB:
                handleDB(data);
                break;

            case FMS:
                handleFMS(data);
                break;

            case SRV:
                handleSRV(data);
                break;
            case HA:
                handleHA(data);
                break;
            case EP:
                handleEP(data);
                break;
            case SYS:
                handleSYS(data);
                break;
            case UM:
                handleUM(data);
                break;
            case WB:
                handleWB(data);
                break;
            case MBS:
                handleMBS(data);
                break;

            default:
                break;
        }

        switch (tag) {   

            case TAG_WB_CONNECTED_DEVICES:
                handle.setCount_WB(data.getValueAsInt().orElse(-1));
                break;

            case TAG_FMS_CONNECTED_DEVICES:
                handle.setCount_FMS(data.getValueAsInt().orElse(-1));
                break;

            case TAG_FMS_CONNECTED_DEVICES_REV:
                handle.setCount_FMS_REV(data.getValueAsInt().orElse(-1));
                break;

            case TAG_PVI_USED_STRING_COUNT:
                handle.setCount_PVI(data.getValueAsInt().orElse(-1));
                break;

            case TAG_QPI_INVERTER_COUNT:
                handle.setCount_QPI(data.getValueAsInt().orElse(-1));
                break;

            case TAG_SE_SE_COUNT:
                handle.setCount_SE(data.getValueAsInt().orElse(-1));
                break;

            default:
                break;
        }
    }

    public void handleMBS(RSCPData data) {
        var tag = data.getDataTag();
        String groupPref = CHANNEL_GROUP_MBS + "#";

        switch (tag) {

            case TAG_SYS_IS_SYSTEM_REBOOTING:
                handle.updateState(groupPref + CHANNEL_IsModbusEnabled,
                        OnOffType.from(data.getValueAsBool().orElse(false)));
                break;

            default:
                break;
        }
    }

    public void handleSYS(RSCPData data) {
        var tag = data.getDataTag();
        String groupPref = CHANNEL_GROUP_SYS + "#";

        switch (tag) {                    

            case TAG_SYS_IS_SYSTEM_REBOOTING:
                handle.updateState(groupPref + CHANNEL_Rebooting, OnOffType.from(data.getValueAsBool().orElse(false)));
                break;

            default:
                break;
        }
    }

    public void handlePVI(RSCPData data) {
        var tag = data.getDataTag();
        String groupPref = CHANNEL_GROUP_PVI + "#";
    }

    public void handleBAT(RSCPData data) {
        var tag = data.getDataTag();
        String groupPref = CHANNEL_GROUP_BAT + "#";
    }

    public void handleDCDC(RSCPData data) {
        var tag = data.getDataTag();
        String groupPref = CHANNEL_GROUP_DCDC + "#";
    }

    public void handleRSCP(RSCPData data) {
        var tag = data.getDataTag();
        String groupPref = CHANNEL_GROUP_RSCP + "#";
    }

    public void handleDB(RSCPData data) {
        var tag = data.getDataTag();
        String groupPref = CHANNEL_GROUP_DB + "#";
    }

    public void handleFMS(RSCPData data) {
        var tag = data.getDataTag();
        String groupPref = CHANNEL_GROUP_FMS + "#";
    }

    public void handleSRV(RSCPData data) {
        var tag = data.getDataTag();
        String groupPref = CHANNEL_GROUP_SRV + "#";
    }

    public void handleHA(RSCPData data) {
        var tag = data.getDataTag();
        String groupPref = CHANNEL_GROUP_HA + "#";
    }

    public void handleUM(RSCPData data) {
        var tag = data.getDataTag();
        String groupPref = CHANNEL_GROUP_UM + "#";

        switch (tag) {            

            case TAG_UM_UPDATE_STATUS:
                handle.updateState(groupPref + CHANNEL_UpdateStatus, new DecimalType(data.getValueAsInt().orElse(-1)));
                break;

            default:
                break;
        }
    }

    public void handleEP(RSCPData data) {
        var tag = data.getDataTag();
        String groupPref = CHANNEL_GROUP_EP + "#";

        switch (tag) {

            case TAG_EP_IS_READY_FOR_SWITCH:
                handle.updateState(groupPref + CHANNEL_IsReadyForSwitch,
                        OnOffType.from(data.getValueAsBool().orElse(false)));
                break;
            case TAG_EP_IS_GRID_CONNECTED:
                handle.updateState(groupPref + CHANNEL_GridConnected,
                        OnOffType.from(data.getValueAsBool().orElse(false)));
                break;
            case TAG_EP_IS_ISLAND_GRID:
                handle.updateState(groupPref + CHANNEL_IsIslandGrid,
                        OnOffType.from(data.getValueAsBool().orElse(false)));
                break;
            case TAG_EP_IS_INVALID_STATE:
                handle.updateState(groupPref + CHANNEL_IsInvalidState,
                        OnOffType.from(data.getValueAsBool().orElse(false)));
                break;
            case TAG_EP_IS_POSSIBLE:
                handle.updateState(groupPref + CHANNEL_IsPossible, OnOffType.from(data.getValueAsBool().orElse(false)));
                break;

            default:
                break;
        }
    }

    public void handleINFO(RSCPData data) {
        var tag = data.getDataTag();
        String sValue;

        String groupPref = CHANNEL_GROUP_INFO + "#";

        switch (tag) {

            case TAG_INFO_SERIAL_NUMBER:
                sValue = data.getValueAsString().orElse("ERR");
                handle.updateProperty(PROPERTY_SERIAL, sValue);
                handle.updateState(groupPref + CHANNEL_SerialNumber, new StringType(sValue));
                break;
            case TAG_INFO_PRODUCTION_DATE:
                sValue = data.getValueAsString().orElse("ERR");
                handle.updateProperty(PROPERTY_PROD, sValue);
                handle.updateState(groupPref + CHANNEL_ProductionDate, new StringType(sValue));
                break;
            case TAG_INFO_IP_ADDRESS:
                sValue = data.getValueAsString().orElse("ERR");
                handle.updateProperty(PROPERTY_IP, sValue);
                handle.updateState(groupPref + CHANNEL_IPAddress, new StringType(sValue));
                break;
            case TAG_INFO_SUBNET_MASK:
                sValue = data.getValueAsString().orElse("ERR");
                handle.updateProperty(PROPERTY_SUBNET, sValue);
                handle.updateState(groupPref + CHANNEL_SubnetMask, new StringType(sValue));
                break;
            case TAG_INFO_MAC_ADDRESS:
                sValue = data.getValueAsString().orElse("ERR");
                handle.updateProperty(PROPERTY_MAC, sValue);
                handle.updateState(groupPref + CHANNEL_MACAddress, new StringType(sValue));
                break;
            case TAG_INFO_GATEWAY:
                sValue = data.getValueAsString().orElse("ERR");
                handle.updateProperty(PROPERTY_GW, sValue);
                handle.updateState(groupPref + CHANNEL_Gateway, new StringType(sValue));
                break;
            case TAG_INFO_DNS:
                sValue = data.getValueAsString().orElse("ERR");
                handle.updateProperty(PROPERTY_DNS, sValue);
                handle.updateState(groupPref + CHANNEL_DNS, new StringType(sValue));
                break;
            case TAG_INFO_DHCP_STATUS:
                sValue = data.getValueAsString().orElse("ERR");
                handle.updateProperty(PROPERTY_DHCP, sValue);
                handle.updateState(groupPref + CHANNEL_DHCP, OnOffType.from(data.getValueAsBool().orElse(false)));
                break;
            case TAG_INFO_TIME:
                handle.updateState(groupPref + CHANNEL_Time, new StringType(data.getValueAsString().orElse("ERR")));
                break;
            case TAG_INFO_UTC_TIME:
                handle.updateState(groupPref + CHANNEL_UTCTime, new StringType(data.getValueAsString().orElse("ERR")));
                break;
            case TAG_INFO_TIME_ZONE:
                handle.updateState(groupPref + CHANNEL_TimeZone, new StringType(data.getValueAsString().orElse("ERR")));
                break;
            case TAG_INFO_SW_RELEASE:
                sValue = data.getValueAsString().orElse("ERR");
                handle.updateProperty(PROPERTY_VERSION, sValue);
                handle.updateState(groupPref + CHANNEL_SWRelease, new StringType(sValue));
                break;

            default:
                break;
        }
    }

    public void handleEMS(RSCPData data) {
        var tag = data.getDataTag();
        String groupPref = CHANNEL_GROUP_EMS + "#";

        List<RSCPData> containedDataList;

        switch (tag) {

            case TAG_EMS_POWER_PV:
                handle.updateState(groupPref + CHANNEL_CurrentPowerPV,
                        new QuantityType<>(data.getValueAsInt().orElse(-1), Units.WATT));
                break;
            case TAG_EMS_POWER_BAT:
                handle.updateState(groupPref + CHANNEL_CurrentPowerBat,
                        new QuantityType<>(data.getValueAsInt().orElse(-1), Units.WATT));
                break;
            case TAG_EMS_POWER_HOME:
                handle.updateState(groupPref + CHANNEL_CurrentPowerHome,
                        new QuantityType<>(data.getValueAsInt().orElse(-1), Units.WATT));
                break;
            case TAG_EMS_POWER_GRID:
                handle.updateState(groupPref + CHANNEL_CurrentPowerGrid,
                        new QuantityType<>(data.getValueAsInt().orElse(-1), Units.WATT));
                break;
            case TAG_EMS_POWER_ADD:
                handle.updateState(groupPref + CHANNEL_CurrentPowerAdd,
                        new QuantityType<>(data.getValueAsInt().orElse(-1), Units.WATT));
                break;
            case TAG_EMS_BAT_SOC:
                handle.updateState(groupPref + CHANNEL_BatterySOC,
                        new QuantityType<>(data.getValueAsInt().orElse(-1), Units.PERCENT));
                break;
            case TAG_EMS_SELF_CONSUMPTION:
                handle.updateState(groupPref + CHANNEL_SelfConsumption,
                        new QuantityType<>(data.getValueAsFloat().orElse((float) -1.0), Units.PERCENT));
                break;
            case TAG_EMS_AUTARKY:
                handle.updateState(groupPref + CHANNEL_Autarky,
                        new QuantityType<>(data.getValueAsFloat().orElse((float) -1.0), Units.PERCENT));
                break;

            case TAG_EMS_EMERGENCY_POWER_STATUS:
                handle.updateState(groupPref + CHANNEL_EmergencyPowerStatus,
                        new DecimalType(data.getValueAsInt().orElse(-1)));
                break;

            case TAG_EMS_GET_POWER_SETTINGS:
            case TAG_EMS_SET_POWER_SETTINGS:
                containedDataList = data.getContainerData();
                for (RSCPData containedData : containedDataList) {
                    handleUpdatePowerSettingsData(containedData);
                }
                break;

            default:
                break;
                }
    }
    
    private void handleUpdatePowerSettingsData(RSCPData data) {
        var tag = data.getDataTag();
        String groupPref = CHANNEL_GROUP_EMS + "#";

        switch (tag) {
            // case TAG_EMS_RES_POWER_LIMITS_USED:
            case TAG_EMS_POWER_LIMITS_USED:
                handle.updateState(groupPref + CHANNEL_PowerLimitsUsed,
                        OnOffType.from(data.getValueAsBool().orElse(false)));
                break;

            // case TAG_EMS_RES_MAX_DISCHARGE_POWER:
            case TAG_EMS_MAX_DISCHARGE_POWER:
                handle.updateState(groupPref + CHANNEL_MaxDischarge,
                        new QuantityType<>(data.getValueAsInt().get(), Units.WATT));
                break;

            // case TAG_EMS_RES_MAX_CHARGE_POWER:
            case TAG_EMS_MAX_CHARGE_POWER:
                handle.updateState(groupPref + CHANNEL_MaxCharge,
                        new QuantityType<>(data.getValueAsInt().get(), Units.WATT));
                break;

            // case TAG_EMS_RES_DISCHARGE_START_POWER:
            case TAG_EMS_DISCHARGE_START_POWER:
                handle.updateState(groupPref + CHANNEL_DischargeStart, new DecimalType(data.getValueAsInt().get()));
                break;

            // case TAG_EMS_RES_WEATHER_REGULATED_CHARGE_ENABLE:
            case TAG_EMS_WEATHER_REGULATED_CHARGE_ENABLED:
                handle.updateState(groupPref + CHANNEL_WeatherRegulatedCharge,
                        OnOffType.from(data.getValueAsBool().orElse(false)));
                break;

            // case TAG_EMS_RES_POWERSAVE_ENABLED:
            case TAG_EMS_POWERSAVE_ENABLED:
                boolean result = data.getValueAsBool().orElse(false);
                handle.updateState(groupPref + CHANNEL_PowerSave, OnOffType.from(result));
                break;

            default:
                break;
        }
    }

    public void handlePM(RSCPData data) {
        var tag = data.getDataTag();

        List<RSCPData> containedDataList;
        int index = 0;

        switch (tag) {
            
            case TAG_PM_DATA:
                containedDataList = data.getContainerData();

                index = 0;
                for (RSCPData containedData : containedDataList) {
                    index = handleContainerPMData(containedData, index);
                }
                break;

            case TAG_PM_CONNECTED_DEVICES:
                containedDataList = data.getContainerData();

                index = 0;
                for (RSCPData devicesContainer : containedDataList) {
                    var deviceList = devicesContainer.getContainerData();

                    for (RSCPData deviceContainer : deviceList) {
                        index = handleContainerPMDevices(deviceContainer, index);
                    }
                }
                break;

            default:
                break;
        }
    }

    private int handleContainerPMDevices(RSCPData data, int index) {
        final String dt = data.getDataTag().name();
        final var tag = data.getDataTag();

        int newIndex = index;

        switch (tag) {
            case TAG_PM_INDEX:
                newIndex = data.getValueAsInt().get();
                logger.debug("{} with index of {}", dt, newIndex);

                if (handle.getCount_PM() < newIndex + 1) // increase only
                    handle.setCount_PM(newIndex + 1);

                break;
            case TAG_PM_TYPE:

                break;
            default:
                break;
        }
        return newIndex;
    }

    private int handleContainerPMData(RSCPData data, int index) {
        final String dt = data.getDataTag().name();
        final var tag = data.getDataTag();

        final String groupPref = CHANNEL_GROUP_PM + ((index + 1 < 10) ? "0" : "") + Integer.toString(index + 1) + "#";

        int newIndex = index;

        switch (tag) {
            case TAG_PM_INDEX:
                newIndex = data.getValueAsInt().get();
                logger.debug("{} with index of {}", dt, newIndex);
                break;
            case TAG_PM_ENERGY_L1:
                handle.updateState(groupPref + CHANNEL_PMCurrentEnergyL1,
                        new QuantityType<>(data.getValueAsDouble().orElse(-1.0), Units.WATT_HOUR));
                break;
            case TAG_PM_ENERGY_L2:
                handle.updateState(groupPref + CHANNEL_PMCurrentEnergyL2,
                        new QuantityType<>(data.getValueAsDouble().orElse(-1.0), Units.WATT_HOUR));
                break;
            case TAG_PM_ENERGY_L3:
                handle.updateState(groupPref + CHANNEL_PMCurrentEnergyL3,
                        new QuantityType<>(data.getValueAsDouble().orElse(-1.0), Units.WATT_HOUR));
                break;
            case TAG_PM_POWER_L1:
                handle.updateState(groupPref + CHANNEL_PMCurrentPowerL1,
                        new QuantityType<>(data.getValueAsDouble().orElse(-1.0), Units.WATT));
                break;
            case TAG_PM_POWER_L2:
                handle.updateState(groupPref + CHANNEL_PMCurrentPowerL2,
                        new QuantityType<>(data.getValueAsDouble().orElse(-1.0), Units.WATT));
                break;
            case TAG_PM_POWER_L3:
                handle.updateState(groupPref + CHANNEL_PMCurrentPowerL3,
                        new QuantityType<>(data.getValueAsDouble().orElse(-1.0), Units.WATT));
                break;
            case TAG_PM_VOLTAGE_L1:
                handle.updateState(groupPref + CHANNEL_PMCurrentVoltageL1,
                        new QuantityType<>(data.getValueAsFloat().orElse((float) -1.0), Units.VOLT));
                break;
            case TAG_PM_VOLTAGE_L2:
                handle.updateState(groupPref + CHANNEL_PMCurrentVoltageL2,
                        new QuantityType<>(data.getValueAsFloat().orElse((float) -1.0), Units.VOLT));
                break;
            case TAG_PM_VOLTAGE_L3:
                handle.updateState(groupPref + CHANNEL_PMCurrentVoltageL3,
                        new QuantityType<>(data.getValueAsFloat().orElse((float) -1.0), Units.VOLT));
                break;
            case TAG_PM_MODE:
                handle.updateState(groupPref + CHANNEL_PMMode, new DecimalType(data.getValueAsInt().get()));
                break;
            case TAG_PM_TYPE:
                handle.updateState(groupPref + CHANNEL_PMType, new DecimalType(data.getValueAsString().get()));                 
                break;
            case TAG_PM_ACTIVE_PHASES:
                Integer phases = data.getValueAsInt().get();

                Boolean first = ((phases >> 0) & 1) != 0;
                Boolean second = ((phases >> 0) & 2) != 0;
                Boolean third = ((phases >> 0) & 3) != 0;

                handle.updateState(groupPref + CHANNEL_PhaseActive_1, OnOffType.from(first));
                handle.updateState(groupPref + CHANNEL_PhaseActive_2, OnOffType.from(second));
                handle.updateState(groupPref + CHANNEL_PhaseActive_3, OnOffType.from(third));
                break;
            default:
                break;
        }
        return newIndex;
    }

    public void handleWB(RSCPData data) {
        var tag = data.getDataTag();
        
        List<RSCPData> containedDataList;
        int index = 0;

        switch (tag) {           

            case TAG_WB_DATA:
                containedDataList = data.getContainerData();

                index = 0;
                for (RSCPData containedData : containedDataList) {
                    index = handleContainerWBData(containedData, index);
                }
                break;

            case TAG_WB_CONNECTED_DEVICES:
                containedDataList = data.getContainerData();

                index = 0;
                for (RSCPData containedData : containedDataList) {
                    index = handleContainerWBDevices(containedData, index);
                }
                break;

            default:
                break;
        }
    }

    private int handleContainerWBData(RSCPData data, int index) {
        final String dt = data.getDataTag().name();
        final var tag = data.getDataTag();

        final String groupPref = CHANNEL_GROUP_WB + ((index + 1 < 10) ? "0" : "") + Integer.toString(index + 1) + "#";

        if (logger.isDebugEnabled())
            logger.debug("handleUpdateWBData: {}: {} (index: {})", dt, data.getValueAsString(), index);

        int newIndex = index;

        switch (tag) {
            case TAG_WB_INDEX:
                newIndex = data.getValueAsInt().get();
                logger.debug("{} with index of {}", dt, newIndex);
                break;
            case TAG_WB_PM_ENERGY_L1:
                handle.updateState(groupPref + CHANNEL_WB_EnergyL1,
                        new QuantityType<>(data.getValueAsDouble().orElse(-1.0), Units.WATT_HOUR));
                break;
            case TAG_WB_PM_ENERGY_L2:
                handle.updateState(groupPref + CHANNEL_WB_EnergyL2,
                        new QuantityType<>(data.getValueAsDouble().orElse(-1.0), Units.WATT_HOUR));
                break;
            case TAG_WB_PM_ENERGY_L3:
                handle.updateState(groupPref + CHANNEL_WB_EnergyL3,
                        new QuantityType<>(data.getValueAsDouble().orElse(-1.0), Units.WATT_HOUR));
                break;
            case TAG_WB_PM_POWER_L1:
                handle.updateState(groupPref + CHANNEL_WB_PowerL1,
                        new QuantityType<>(data.getValueAsDouble().orElse(-1.0), Units.WATT));
                break;
            case TAG_WB_PM_POWER_L2:
                handle.updateState(groupPref + CHANNEL_WB_PowerL2,
                        new QuantityType<>(data.getValueAsDouble().orElse(-1.0), Units.WATT));
                break;
            case TAG_WB_PM_POWER_L3:
                handle.updateState(groupPref + CHANNEL_WB_PowerL3,
                        new QuantityType<>(data.getValueAsDouble().orElse(-1.0), Units.WATT));
                break;
            case TAG_WB_MODE:
                handle.updateState(groupPref + CHANNEL_WB_Mode, new DecimalType(data.getValueAsInt().get()));
                break;
            case TAG_WB_PM_ACTIVE_PHASES:
                Integer phases = data.getValueAsInt().get();

                Boolean first = ((phases >> 0) & 1) != 0;
                Boolean second = ((phases >> 0) & 2) != 0;
                Boolean third = ((phases >> 0) & 3) != 0;

                handle.updateState(groupPref + CHANNEL_WB_PhaseActive_1, OnOffType.from(first));
                handle.updateState(groupPref + CHANNEL_WB_PhaseActive_2, OnOffType.from(second));
                handle.updateState(groupPref + CHANNEL_WB_PhaseActive_3, OnOffType.from(third));
                break;
            default:
                break;
        }
        return newIndex;
    }

    private int handleContainerWBDevices(RSCPData data, int index) { // TODO device == wallbox or car?!
        final String dt = data.getDataTag().name();
        final var tag = data.getDataTag();

        int newIndex = index;

        switch (tag) {
            case TAG_WB_INDEX:
                newIndex = data.getValueAsInt().get();
                logger.debug("{} with index of {}", dt, newIndex);

                if (handle.getCount_WB() < newIndex + 1) // increase only
                    handle.setCount_WB(newIndex + 1);

                break;
            default:
                break;
        }
        return newIndex;
    }

    @SuppressWarnings("null")
    public boolean isNotConnected() {
        return socket == null || socket.isClosed();
    }

    @SuppressWarnings("null")
    public void close() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            logger.error("Couldn't close connection.", e);
        }
    }

    @SuppressWarnings("null")
    public void openConnection(String ipAddress, int port, int maxRetries, long sleepMillisBeforeRetry)
            throws IOException, RuntimeException {

            try {
                close();
            socket = new Socket();
            if (socket != null) {
                socket.connect(new InetSocketAddress(ipAddress, port), SOCKET_TIMEOUT);
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(SOCKET_TIMEOUT);
                }
            } catch (Exception e) {
            close();
                throw e;
            }

        // socket = null;
        // int retries = 0;
        // while (handle != null && isNotConnected() && retries++ < maxRetries) {
        // try {
        // socket = new Socket();
        // socket.connect(new InetSocketAddress(ipAddress, port), SOCKET_TIMEOUT);
        // socket.setTcpNoDelay(true);
        // socket.setSoTimeout(SOCKET_TIMEOUT);
        // } catch (UnknownHostException e) {
        // close();
        // throw e;
        // } catch (IOException e) {
        // close();
        // if (retries < maxRetries) {
        // try {
        // Thread.sleep(sleepMillisBeforeRetry);
        // } catch (Exception ex) {
        // // ignore
        // }
        // }
        // } catch (Exception e) {
        // throw e;
        // }
        // }

        // if (socket == null) {
        // // retries exhausted, still no connection
        // String msg = String.format("Failed to establish connection to server {}:{}",
        // ipAddress, port);
        // throw new RuntimeException(msg);
        // }
    }

    public void openConnection(String ipAddress, int port) throws IOException, RuntimeException {
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
    @SuppressWarnings("null")
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
            byte[] data = new byte[65535];
            do {
                logger.trace("dIn.available {}", dIn.available());

                int bytesRead = dIn.read(data, 0, data.length);
                logger.debug("Received {} bytes, append to buffer... ", bytesRead);
                if (bytesRead == -1) {
                    logger.warn("Socket closed unexpectedly by server.");
                    break;
                }
                buffer.write(data, 0, bytesRead);
                totalBytesRead += bytesRead;
            } while (dIn.available() > 0);

            if (totalBytesRead == 1448 || totalBytesRead == 2896) {
                logger.warn("Double check at {} bytes: dIn.available {}", totalBytesRead, dIn.available());
            }

            logger.debug("Finished reading {} bytes.", totalBytesRead);
            buffer.flush();

            byte[] decryptedData = new byte[0];
            byte[] receivedBytes = buffer.toByteArray();
            if (receivedBytes.length > 0) {
                decryptedData = decryptFunc.apply(receivedBytes);
                logger.debug("Decrypted frame data.");
            }

            return decryptedData;
        } catch (Exception e) {
            logger.error("Error while receiving and decrypting frame.", e);
        }
        return null;
    }

    public static int int32Converter(byte b[], int start) {
        return ((b[start] << 24) & 0xff000000 | (b[start + 1] << 16) & 0xff0000 | (b[start + 2] << 8) & 0xff00
                | (b[start + 3]) & 0xff);
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
