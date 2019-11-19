/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.noolite.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.noolite.NooLiteBindingConstants;
import org.openhab.binding.noolite.internal.NooliteMTRF64Adapter;
import org.openhab.binding.noolite.internal.config.NooliteBridgeConfiguration;
/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Shatsillo - Initial contribution
 *
 */
@NonNullByDefault
public class NooliteMTRF64BridgeHandler extends BaseBridgeHandler {

    private static final Logger logger = LoggerFactory.getLogger(NooliteMTRF64BridgeHandler.class);
    @Nullable
    static NooliteMTRF64Adapter adapter;
    @Nullable
    private @Nullable NooliteBridgeConfiguration bridgeConfig;
    public static Map<String, NooliteHandler> thingHandlerMap = new HashMap<String, NooliteHandler>();
    @Nullable
    static NooliteHandler nooliteHandler;

    public NooliteMTRF64BridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void dispose() {
        logger.debug("Dispose Noolite bridge handler {}", this.toString());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Close bridge");
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Noolite bridge handler {}", this.toString());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Initializing...");
        bridgeConfig = getConfigAs(NooliteBridgeConfiguration.class);

        // logger.debug("Checking Noolite adapter connection. {}", thing.getStatus());
        if (thing.getStatus() != ThingStatus.ONLINE) {
            connect();
        }
    }

    @SuppressWarnings("null")
    private void connect() {
        logger.debug("Connecting to Noolite adapter");
        try {
            if (bridgeConfig.serial != null) {
                if (adapter == null) {
                    adapter = new NooliteMTRF64Adapter();
                }
            }
            if (adapter != null) {
                adapter.disconnect();
                adapter.connect(bridgeConfig);
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Com-port error");
            logger.debug(e.getMessage());
        }
    }

    @SuppressWarnings({ "null", "unused" })
    public void registerMegadThingListener(NooliteHandler thingHandler) {
        String thingID = thingHandler.getThing().getConfiguration().get("type").toString() + "."
                + (Integer.parseInt(thingHandler.getThing().getConfiguration().get("port").toString()));
        if (thingHandlerMap.get(thingID) != null) {
            thingHandlerMap.remove(thingID);
        }
        logger.debug("thingHandler for thing: '{}'", thingID);
        if (thingHandlerMap.get(thingID) == null) {
            thingHandlerMap.put(thingID, thingHandler);
            logger.debug("register thingHandler for thing: {}", thingHandler);
            updateThingHandlerStatus(thingHandler, this.getStatus());
            if (thingID.equals("localhost.")) {
                updateThingHandlerStatus(thingHandler, ThingStatus.OFFLINE);
            }
        } else {
            logger.debug("thingHandler for thing: '{}' already registerd", thingID);
            updateThingHandlerStatus(thingHandler, this.getStatus());
        }
    }

    public ThingStatus getStatus() {
        return getThing().getStatus();
    }

    private void updateThingHandlerStatus(NooliteHandler thingHandler, ThingStatus status) {
        thingHandler.updateStatus(status);
    }

    @SuppressWarnings("null")
    public static void updateValues(byte[] data) {
        String thingID = data[1] + "." + data[4];
        nooliteHandler = thingHandlerMap.get(thingID);
        nooliteHandler.updateValues(data);
    }

    @SuppressWarnings("null")
    public synchronized void sendMessage(NooliteHandler nooliteHandler, @Nullable ChannelUID channel, Command command) {
        logger.debug("{}", command);

        byte[] data = new byte[17];
        data[0] = (byte) 0b10101011;
        data[1] = (byte) Integer.parseInt(nooliteHandler.getThing().getConfiguration().get("type").toString());
        data[2] = 0;
        data[3] = 0;
        if ((channel != null) && (channel.getId().equals(NooLiteBindingConstants.CHANNEL_BINDCHANNEL))
                && !(command.toString().equals("REFRESH"))) {
            if (Integer.parseInt(nooliteHandler.getThing().getConfiguration().get("type").toString()) == 3) {
                data[2] = 3;
            }
            data[4] = (byte) (Integer.parseInt(command.toString()));
            data[5] = 15;
            data[6] = 0;
            data[7] = 0;
            data[8] = 0;
            data[9] = 0;
            data[10] = 0;
        } else {
            data[4] = (byte) (Integer.parseInt(nooliteHandler.getThing().getConfiguration().get("port").toString()));
        }
        } else if (OnOffType.ON.equals(command)) {

            data[5] = 2;
            data[6] = 0;
            data[7] = 0;
            data[8] = 0;
            data[9] = 0;
            data[10] = 0;
        } else if (OnOffType.OFF.equals(command)) {
            data[5] = 0;
            data[6] = 0;
            data[7] = 0;
            data[8] = 0;
            data[9] = 0;
            data[10] = 0;
        }

        data[11] = 0;
        data[12] = 0;
        data[13] = 0;
        data[14] = 0;

        short sum = 0;
        for (int i = 0; i < 14; i++) {
            sum += data[i];
        }

        data[15] = (byte) sum;
        data[16] = (byte) 0b10101100;
        if (!(command instanceof RefreshType)) {
            try {
                if (adapter != null) {
                    adapter.sendData(data);
                }
            } catch (IOException e) {
            }
        }
    }

}
