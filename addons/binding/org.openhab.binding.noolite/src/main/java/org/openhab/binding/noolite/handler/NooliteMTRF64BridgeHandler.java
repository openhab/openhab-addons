package org.openhab.binding.noolite.handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.noolite.NooliteBindingConstants;
import org.openhab.binding.noolite.internal.config.NooliteBridgeConfiguration;
import org.openhab.binding.noolite.internal.watcher.NooliteMTRF64Adapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NooliteMTRF64BridgeHandler} is responsible for bridge between USB stick and noolite binding
 *
 * @author Petr Shatsillo - Initial contribution
 */

public class NooliteMTRF64BridgeHandler extends BaseBridgeHandler {

    private static Logger logger = LoggerFactory.getLogger(NooliteMTRF64BridgeHandler.class);
    static NooliteMTRF64Adapter adapter;
    private NooliteBridgeConfiguration bridgeConfig;
    private ScheduledFuture<?> connectorTask;
    public static Map<String, NooliteHandler> thingHandlerMap = new HashMap<String, NooliteHandler>();
    static NooliteHandler nooliteHandler;

    public NooliteMTRF64BridgeHandler(Bridge bridge) {

        super(bridge);
        // comport = bridge.getConfiguration().get("comport").toString();
        // logger.debug("comport is {}", comport);
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
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Initializing...");
        bridgeConfig = getConfigAs(NooliteBridgeConfiguration.class);

        if (connectorTask == null || connectorTask.isCancelled()) {
            connectorTask = scheduler.scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    // logger.debug("Checking Noolite adapter connection. {}", thing.getStatus());
                    if (thing.getStatus() != ThingStatus.ONLINE) {
                        connect();
                    }
                }

            }, 0, 60, TimeUnit.SECONDS);

        }
    }

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
                // adapter.connect("fake");
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE);
            e.printStackTrace();
        }
    }

    public void registerMegadThingListener(NooliteHandler thingHandler) {
        if (thingHandler == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null ThingHandler.");
        } else {

            String thingID = thingHandler.getThing().getConfiguration().get("type").toString() + "."
                    + (Integer.parseInt(thingHandler.getThing().getConfiguration().get("port").toString()) - 1);
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
    }

    public ThingStatus getStatus() {
        return getThing().getStatus();
    }

    private void updateThingHandlerStatus(NooliteHandler thingHandler, ThingStatus status) {
        thingHandler.updateStatus(status);
    }

    public static void updateValues(byte[] data) {

        String thingID = data[1] + "." + data[4];
        nooliteHandler = thingHandlerMap.get(thingID);
        if (nooliteHandler != null) {
            nooliteHandler.updateValues(data);
        }
    }

    public synchronized void sendMessage(NooliteHandler nooliteHandler, ChannelUID channel, Command command) {

        logger.debug("{}", command);

        byte[] data = new byte[17];
        data[0] = (byte) 0b10101011;
        data[1] = (byte) Integer.parseInt(nooliteHandler.getThing().getConfiguration().get("type").toString());
        data[2] = 0;
        data[3] = 0;
        if ((channel != null) && (channel.getId().equals(NooliteBindingConstants.CHANNEL_BINDCHANNEL))
                && !(command.toString().equals("REFRESH"))) {
            if (Integer.parseInt(nooliteHandler.getThing().getConfiguration().get("type").toString()) == 3) {
                data[2] = 3;
            }
            data[4] = (byte) (Integer.parseInt(command.toString()) - 1);
            data[5] = 15;
            data[6] = 0;
            data[7] = 0;
            data[8] = 0;
            data[9] = 0;
            data[10] = 0;
        } else {
            data[4] = (byte) (Integer.parseInt(nooliteHandler.getThing().getConfiguration().get("port").toString())
                    - 1);
        }
        if (command.toString().equals("ON")) {
            data[5] = 2;
            data[6] = 0;
            data[7] = 0;
            data[8] = 0;
            data[9] = 0;
            data[10] = 0;
        } else if (command.toString().equals("OFF")) {
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
        if (!command.toString().equals("REFRESH")) {
            try {
                adapter.sendData(data);
            } catch (IOException e) {
            }
        }

    }

}
