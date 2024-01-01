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
package org.openhab.binding.homematic.internal.communicator.client;

import static org.openhab.binding.homematic.internal.HomematicBindingConstants.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openhab.binding.homematic.internal.HomematicBindingConstants;
import org.openhab.binding.homematic.internal.common.HomematicConfig;
import org.openhab.binding.homematic.internal.communicator.message.RpcRequest;
import org.openhab.binding.homematic.internal.communicator.parser.GetAllScriptsParser;
import org.openhab.binding.homematic.internal.communicator.parser.GetAllSystemVariablesParser;
import org.openhab.binding.homematic.internal.communicator.parser.GetDeviceDescriptionParser;
import org.openhab.binding.homematic.internal.communicator.parser.GetParamsetDescriptionParser;
import org.openhab.binding.homematic.internal.communicator.parser.GetParamsetParser;
import org.openhab.binding.homematic.internal.communicator.parser.GetValueParser;
import org.openhab.binding.homematic.internal.communicator.parser.HomegearLoadDeviceNamesParser;
import org.openhab.binding.homematic.internal.communicator.parser.ListBidcosInterfacesParser;
import org.openhab.binding.homematic.internal.communicator.parser.ListDevicesParser;
import org.openhab.binding.homematic.internal.communicator.parser.RssiInfoParser;
import org.openhab.binding.homematic.internal.misc.MiscUtils;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmGatewayInfo;
import org.openhab.binding.homematic.internal.model.HmInterface;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.model.HmRssiInfo;
import org.openhab.core.common.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client implementation for sending messages via BIN-RPC to a Homematic gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public abstract class RpcClient<T> {
    private final Logger logger = LoggerFactory.getLogger(RpcClient.class);
    protected static final int MAX_RPC_RETRY = 3;
    protected static final int RESP_BUFFER_SIZE = 8192;
    private static final int INITIAL_CALLBACK_REG_DELAY = 20; // 20 s before first attempt
    private static final int CALLBACK_REG_DELAY = 10; // 10 s between two attempts

    protected HomematicConfig config;
    private String thisUID = UUID.randomUUID().toString();
    private ScheduledFuture<?> future = null;
    private int attempt;

    public RpcClient(HomematicConfig config) {
        this.config = config;
    }

    /**
     * Returns a RpcRequest for this client.
     */
    protected abstract RpcRequest<T> createRpcRequest(String methodName);

    /**
     * Returns the callback url for this client.
     */
    protected abstract String getRpcCallbackUrl();

    /**
     * Sends the RPC message to the gateway.
     */
    protected abstract Object[] sendMessage(int port, RpcRequest<T> request) throws IOException;

    /**
     * Register a callback for the specified interface where the Homematic gateway can send its events.
     */
    public void init(HmInterface hmInterface) throws IOException {
        ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(GATEWAY_POOL_NAME);
        RpcRequest<T> request = createRpcRequest("init");
        request.addArg(getRpcCallbackUrl());
        request.addArg(thisUID);
        if (config.getGatewayInfo().isHomegear()) {
            request.addArg(Integer.valueOf(0x22));
        }
        logger.debug("Register callback for interface {}", hmInterface.getName());
        try {
            attempt = 1;
            sendMessage(config.getRpcPort(hmInterface), request); // first attempt without delay
        } catch (IOException e) {
            future = scheduler.scheduleWithFixedDelay(() -> {
                logger.debug("Register callback for interface {}, attempt {}", hmInterface.getName(), ++attempt);
                try {
                    sendMessage(config.getRpcPort(hmInterface), request);
                    future.cancel(true);
                } catch (IOException ex) {
                    // Ignore, retry
                }
            }, INITIAL_CALLBACK_REG_DELAY, CALLBACK_REG_DELAY, TimeUnit.SECONDS);
            try {
                future.get(config.getCallbackRegTimeout(), TimeUnit.SECONDS);
            } catch (CancellationException e1) {
                logger.debug("Callback for interface {} successfully registered", hmInterface.getName());
            } catch (InterruptedException | ExecutionException e1) {
                throw new IOException("Callback reg. thread interrupted", e1);
            } catch (TimeoutException e1) {
                logger.error("Callback registration for interface {} timed out", hmInterface.getName());
                throw new IOException("Unable to reconnect in time");
            }
            future = null;
        }
    }

    /**
     * Disposes the client.
     */
    public void dispose() {
        if (future != null) {
            future.cancel(true);
        }
    }

    /**
     * Release a callback for the specified interface.
     */
    public void release(HmInterface hmInterface) throws IOException {
        RpcRequest<T> request = createRpcRequest("init");
        request.addArg(getRpcCallbackUrl());
        sendMessage(config.getRpcPort(hmInterface), request);
    }

    /**
     * Sends a ping to the specified interface.
     */
    public void ping(HmInterface hmInterface, String callerId) throws IOException {
        RpcRequest<T> request = createRpcRequest("ping");
        request.addArg(callerId);
        sendMessage(config.getRpcPort(hmInterface), request);
    }

    /**
     * Returns the info of all BidCos interfaces available on the gateway.
     */
    public ListBidcosInterfacesParser listBidcosInterfaces(HmInterface hmInterface) throws IOException {
        RpcRequest<T> request = createRpcRequest("listBidcosInterfaces");
        return new ListBidcosInterfacesParser().parse(sendMessage(config.getRpcPort(hmInterface), request));
    }

    /**
     * Returns some infos of the gateway.
     */
    private GetDeviceDescriptionParser getDeviceDescription(HmInterface hmInterface) throws IOException {
        RpcRequest<T> request = createRpcRequest("getDeviceDescription");
        request.addArg("BidCoS-RF");
        return new GetDeviceDescriptionParser().parse(sendMessage(config.getRpcPort(hmInterface), request));
    }

    /**
     * Returns all variable metadata and values from a Homegear gateway.
     */
    public void getAllSystemVariables(HmChannel channel) throws IOException {
        RpcRequest<T> request = createRpcRequest("getAllSystemVariables");
        new GetAllSystemVariablesParser(channel).parse(sendMessage(config.getRpcPort(channel), request));
    }

    /**
     * Loads all device names from a Homegear gateway.
     */
    public void loadDeviceNames(HmInterface hmInterface, Collection<HmDevice> devices) throws IOException {
        RpcRequest<T> request = createRpcRequest("getDeviceInfo");
        new HomegearLoadDeviceNamesParser(devices).parse(sendMessage(config.getRpcPort(hmInterface), request));
    }

    /**
     * Returns true, if the interface is available on the gateway.
     */
    public void checkInterface(HmInterface hmInterface) throws IOException {
        RpcRequest<T> request = createRpcRequest("init");
        request.addArg("http://openhab.validation:1000");
        sendMessage(config.getRpcPort(hmInterface), request);
    }

    /**
     * Returns all script metadata from a Homegear gateway.
     */
    public void getAllScripts(HmChannel channel) throws IOException {
        RpcRequest<T> request = createRpcRequest("getAllScripts");
        new GetAllScriptsParser(channel).parse(sendMessage(config.getRpcPort(channel), request));
    }

    /**
     * Returns all device and channel metadata.
     */
    public Collection<HmDevice> listDevices(HmInterface hmInterface) throws IOException {
        RpcRequest<T> request = createRpcRequest("listDevices");
        return new ListDevicesParser(hmInterface, config).parse(sendMessage(config.getRpcPort(hmInterface), request));
    }

    /**
     * Loads all datapoint metadata into the given channel.
     */
    public void addChannelDatapoints(HmChannel channel, HmParamsetType paramsetType) throws IOException {
        if (isConfigurationChannel(channel) && paramsetType != HmParamsetType.MASTER) {
            // The configuration channel only has a MASTER Paramset, so there is nothing to load
            return;
        }
        RpcRequest<T> request = createRpcRequest("getParamsetDescription");
        request.addArg(getRpcAddress(channel.getDevice().getAddress()) + getChannelSuffix(channel));
        request.addArg(paramsetType.toString());
        new GetParamsetDescriptionParser(channel, paramsetType).parse(sendMessage(config.getRpcPort(channel), request));
    }

    /**
     * Sets all datapoint values for the given channel.
     */
    public void setChannelDatapointValues(HmChannel channel, HmParamsetType paramsetType) throws IOException {
        if (isConfigurationChannel(channel) && paramsetType != HmParamsetType.MASTER) {
            // The configuration channel only has a MASTER Paramset, so there is nothing to load
            return;
        }

        RpcRequest<T> request = createRpcRequest("getParamset");
        request.addArg(getRpcAddress(channel.getDevice().getAddress()) + getChannelSuffix(channel));
        request.addArg(paramsetType.toString());
        if (channel.getDevice().getHmInterface() == HmInterface.CUXD && paramsetType == HmParamsetType.VALUES) {
            setChannelDatapointValues(channel);
        } else {
            try {
                new GetParamsetParser(channel, paramsetType).parse(sendMessage(config.getRpcPort(channel), request));
            } catch (UnknownRpcFailureException ex) {
                if (paramsetType == HmParamsetType.VALUES) {
                    logger.debug(
                            "RpcResponse unknown RPC failure (-1 Failure), fetching values with another API method for device: {}, channel: {}, paramset: {}",
                            channel.getDevice().getAddress(), channel.getNumber(), paramsetType);
                    setChannelDatapointValues(channel);
                } else {
                    throw ex;
                }
            }
        }
    }

    /**
     * Reads all VALUES datapoints individually, fallback method if setChannelDatapointValues throws a -1 Failure
     * exception.
     */
    private void setChannelDatapointValues(HmChannel channel) throws IOException {
        for (HmDatapoint dp : channel.getDatapoints()) {
            getDatapointValue(dp);
        }
    }

    /**
     * Tries to identify the gateway and returns the GatewayInfo.
     */
    public HmGatewayInfo getGatewayInfo(String id) throws IOException {
        boolean isHomegear = false;
        GetDeviceDescriptionParser ddParser;
        ListBidcosInterfacesParser biParser;

        try {
            ddParser = getDeviceDescription(HmInterface.RF);
            isHomegear = "Homegear".equalsIgnoreCase(ddParser.getType());
        } catch (IOException ex) {
            // can't load gateway infos via RF interface
            ddParser = new GetDeviceDescriptionParser();
        }

        try {
            biParser = listBidcosInterfaces(HmInterface.RF);
        } catch (IOException ex) {
            biParser = listBidcosInterfaces(HmInterface.HMIP);
        }

        HmGatewayInfo gatewayInfo = new HmGatewayInfo();
        gatewayInfo.setAddress(biParser.getGatewayAddress());
        String gwType = biParser.getType();
        if (isHomegear) {
            gatewayInfo.setId(HmGatewayInfo.ID_HOMEGEAR);
            gatewayInfo.setType(ddParser.getType());
            gatewayInfo.setFirmware(ddParser.getFirmware());
        } else if ((MiscUtils.strStartsWithIgnoreCase(gwType, "CCU")
                || MiscUtils.strStartsWithIgnoreCase(gwType, "HMIP_CCU")
                || MiscUtils.strStartsWithIgnoreCase(ddParser.getType(), "HM-RCV-50") || config.isCCUType())
                && !config.isNoCCUType()) {
            gatewayInfo.setId(HmGatewayInfo.ID_CCU);
            String type = gwType.isBlank() ? "CCU" : gwType;
            gatewayInfo.setType(type);
            gatewayInfo
                    .setFirmware(!ddParser.getFirmware().isEmpty() ? ddParser.getFirmware() : biParser.getFirmware());
        } else {
            gatewayInfo.setId(HmGatewayInfo.ID_DEFAULT);
            gatewayInfo.setType(gwType);
            gatewayInfo.setFirmware(biParser.getFirmware());
        }

        if (gatewayInfo.isCCU() || config.hasRfPort()) {
            gatewayInfo.setRfInterface(hasInterface(HmInterface.RF, id));
        }

        if (gatewayInfo.isCCU() || config.hasWiredPort()) {
            gatewayInfo.setWiredInterface(hasInterface(HmInterface.WIRED, id));
        }

        if (gatewayInfo.isCCU() || config.hasHmIpPort()) {
            gatewayInfo.setHmipInterface(hasInterface(HmInterface.HMIP, id));
        }

        if (gatewayInfo.isCCU() || config.hasCuxdPort()) {
            gatewayInfo.setCuxdInterface(hasInterface(HmInterface.CUXD, id));
        }

        if (gatewayInfo.isCCU() || config.hasGroupPort()) {
            gatewayInfo.setGroupInterface(hasInterface(HmInterface.GROUP, id));
        }

        return gatewayInfo;
    }

    /**
     * Returns true, if a connection is possible with the given interface.
     */
    private boolean hasInterface(HmInterface hmInterface, String id) throws IOException {
        try {
            checkInterface(hmInterface);
            return true;
        } catch (IOException ex) {
            logger.info("Interface '{}' on gateway '{}' not available, disabling support", hmInterface, id);
            return false;
        }
    }

    /**
     * Sets the value of the datapoint using the provided rx transmission mode.
     *
     * @param dp The datapoint to set
     * @param value The new value to set on the datapoint
     * @param rxMode The rx mode to use for the transmission of the datapoint value
     *            ({@link HomematicBindingConstants#RX_BURST_MODE "BURST"} for burst mode,
     *            {@link HomematicBindingConstants#RX_WAKEUP_MODE "WAKEUP"} for wakeup mode, or null for the default
     *            mode)
     */
    public void setDatapointValue(HmDatapoint dp, Object value, String rxMode) throws IOException {
        if (dp.isIntegerType() && value instanceof Double) {
            value = ((Number) value).intValue();
        }

        RpcRequest<T> request;
        if (HmParamsetType.VALUES == dp.getParamsetType()) {
            request = createRpcRequest("setValue");
            request.addArg(getRpcAddress(dp.getChannel().getDevice().getAddress()) + getChannelSuffix(dp.getChannel()));
            request.addArg(dp.getName());
            request.addArg(value);
            configureRxMode(request, rxMode);
        } else {
            request = createRpcRequest("putParamset");
            request.addArg(getRpcAddress(dp.getChannel().getDevice().getAddress()) + getChannelSuffix(dp.getChannel()));
            request.addArg(HmParamsetType.MASTER.toString());
            Map<String, Object> paramSet = new HashMap<>();
            paramSet.put(dp.getName(), value);
            request.addArg(paramSet);
            configureRxMode(request, rxMode);
        }
        sendMessage(config.getRpcPort(dp.getChannel()), request);
    }

    protected void configureRxMode(RpcRequest<T> request, String rxMode) {
        if (rxMode != null) {
            if (RX_BURST_MODE.equals(rxMode) || RX_WAKEUP_MODE.equals(rxMode)) {
                request.addArg(rxMode);
            }
        }
    }

    /**
     * Retrieves the value of a single {@link HmDatapoint} from the device. Can only be used for the paramset "VALUES".
     *
     * @param dp The HmDatapoint that shall be loaded
     * @throws IOException If there is a problem while communicating to the gateway
     */
    public void getDatapointValue(HmDatapoint dp) throws IOException {
        if (dp.isReadable() && !dp.isVirtual() && dp.getParamsetType() == HmParamsetType.VALUES) {
            RpcRequest<T> request = createRpcRequest("getValue");
            request.addArg(getRpcAddress(dp.getChannel().getDevice().getAddress()) + getChannelSuffix(dp.getChannel()));
            request.addArg(dp.getName());
            new GetValueParser(dp).parse(sendMessage(config.getRpcPort(dp.getChannel()), request));
        }
    }

    /**
     * Sets the value of a system variable on a Homegear gateway.
     */
    public void setSystemVariable(HmDatapoint dp, Object value) throws IOException {
        RpcRequest<T> request = createRpcRequest("setSystemVariable");
        request.addArg(dp.getInfo());
        request.addArg(value);
        sendMessage(config.getRpcPort(dp.getChannel()), request);
    }

    /**
     * Executes a script on the Homegear gateway.
     */
    public void executeScript(HmDatapoint dp) throws IOException {
        RpcRequest<T> request = createRpcRequest("runScript");
        request.addArg(dp.getInfo());
        sendMessage(config.getRpcPort(dp.getChannel()), request);
    }

    /**
     * Enables/disables the install mode for given seconds.
     *
     * @param hmInterface specifies the interface to enable / disable install mode on
     * @param enable if <i>true</i> it will be enabled, otherwise disabled
     * @param seconds desired duration of install mode
     * @throws IOException if RpcClient fails to propagate command
     */
    public void setInstallMode(HmInterface hmInterface, boolean enable, int seconds) throws IOException {
        RpcRequest<T> request = createRpcRequest("setInstallMode");
        request.addArg(enable);
        request.addArg(seconds);
        request.addArg(INSTALL_MODE_NORMAL);
        logger.debug("Submitting setInstallMode(on={}, time={}, mode={}) ", enable, seconds, INSTALL_MODE_NORMAL);
        sendMessage(config.getRpcPort(hmInterface), request);
    }

    /**
     * Returns the remaining time of <i>install_mode==true</i>
     *
     * @param hmInterface specifies the interface on which install mode status is requested
     * @return current duration in seconds that the controller will remain in install mode,
     *         value of 0 means that the install mode is disabled
     * @throws IOException if RpcClient fails to propagate command
     */
    public int getInstallMode(HmInterface hmInterface) throws IOException {
        RpcRequest<T> request = createRpcRequest("getInstallMode");
        Object[] result = sendMessage(config.getRpcPort(hmInterface), request);
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "Checking InstallMode: getInstallMode() request returned {} (remaining seconds in InstallMode=true)",
                    result);
        }
        try {
            return (int) result[0];
        } catch (Exception cause) {
            IOException wrappedException = new IOException(
                    "Failed to request install mode from interface " + hmInterface);
            wrappedException.initCause(cause);
            throw wrappedException;
        }
    }

    /**
     * Deletes the device from the gateway.
     */
    public void deleteDevice(HmDevice device, int flags) throws IOException {
        RpcRequest<T> request = createRpcRequest("deleteDevice");
        request.addArg(device.getAddress());
        request.addArg(flags);
        sendMessage(config.getRpcPort(device.getHmInterface()), request);
    }

    /**
     * Returns the rpc address from a device address, correctly handling groups.
     */
    private String getRpcAddress(String address) {
        if (address != null && address.startsWith("T-")) {
            address = "*" + address.substring(2);
        }
        return address;
    }

    /**
     * Returns the rssi values for all devices.
     */
    public List<HmRssiInfo> loadRssiInfo(HmInterface hmInterface) throws IOException {
        RpcRequest<T> request = createRpcRequest("rssiInfo");
        return new RssiInfoParser(config).parse(sendMessage(config.getRpcPort(hmInterface), request));
    }

    /**
     * Returns the address suffix that specifies the channel for a given HmChannel. This is either a colon ":" followed
     * by the channel number, or the empty string for a configuration channel.
     */
    private String getChannelSuffix(HmChannel channel) {
        return isConfigurationChannel(channel) ? "" : ":" + channel.getNumber();
    }

    /**
     * Checks whether a channel is a configuration channel. The configuration channel of a device encapsulates the
     * MASTER Paramset that does not belong to one of its actual channels.
     */
    private boolean isConfigurationChannel(HmChannel channel) {
        return channel.getNumber() == CONFIGURATION_CHANNEL_NUMBER;
    }
}
