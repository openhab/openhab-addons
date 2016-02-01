package org.openhab.binding.mysensors.handler;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.mysensors.config.MySensorsBridgeConfiguration;
import org.openhab.binding.mysensors.discovery.MySensorsDiscoveryService;
import org.openhab.binding.mysensors.internal.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.MySensorsMessage;
import org.openhab.binding.mysensors.protocol.ip.MySensorsIpConnection;
import org.openhab.binding.mysensors.protocol.serial.MySensorsSerialConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * @author Tim Oberföll
 *
 *         MySensorsBridgeHandler is used to initialize a new bridge (in MySensors: Gateway)
 *         The sensors are connected via the gateway/bridge to the controller
 */
public class MySensorsBridgeHandler extends BaseBridgeHandler implements MySensorsUpdateListener {

    private Logger logger = LoggerFactory.getLogger(MySensorsBridgeHandler.class);

    public Collection<Thing> connectedThings = Lists.newArrayList();

    // List of Ids that OpenHAB has given, in response to an id request from a sensor node
    private List<Number> givenIds = new ArrayList<Number>();

    // Executor that hold the bridge reader thread
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> future = null;
    private MySensorsBridgeConnection mysCon = null;

    public MySensorsBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#initialize()
     */
    @Override
    public void initialize() {
        logger.debug("Initialization of the MySensors Bridge");

        MySensorsBridgeConfiguration configuration = getConfigAs(MySensorsBridgeConfiguration.class);

        if (getThing().getThingTypeUID().equals(THING_TYPE_BRIDGE_SER)) {
            mysCon = new MySensorsSerialConnection(configuration.serialPort, configuration.baudRate,
                    configuration.sendDelay);
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_BRIDGE_ETH)) {
            mysCon = new MySensorsIpConnection(configuration.ipAddress, configuration.tcpPort, configuration.sendDelay);
        }

        if (mysCon.connect()) {
            future = executor.submit(mysCon);
            mysCon.addUpdateListener(this);
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }

        // Start discovery service
        MySensorsDiscoveryService discoveryService = new MySensorsDiscoveryService(this);
        discoveryService.activate();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#dispose()
     */
    @Override
    public void dispose() {
        if (mysCon != null) {
            mysCon.disconnect();
        }

        if (future != null) {
            future.cancel(true);
        }

        if (executor != null) {
            executor.shutdown();
            executor.shutdownNow();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.ThingHandler#handleCommand(org.eclipse.smarthome.core.thing.ChannelUID,
     * org.eclipse.smarthome.core.types.Command)
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openhab.binding.mysensors.handler.MySensorsUpdateListener#statusUpdateReceived(org.openhab.binding.mysensors.
     * handler.MySensorsStatusUpdateEvent)
     */
    @Override
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event) {
        MySensorsMessage msg = event.getData();

        // Are we getting a Request ID Message?
        if (msg.getNodeId() == 255) {
            if (msg.getChildId() == 255) {
                if (msg.getMsgType() == MYSENSORS_MSG_TYPE_INTERNAL) {
                    if (msg.getAck() == 0) {
                        if (msg.getSubType() == 3) {
                            answerIDRequest();
                        }
                    }
                }
            }
        }
    }

    /**
     * If an ID -Request from a sensor is received the controller will send an id to the sensor
     */
    private void answerIDRequest() {
        logger.debug("ID Request received");

        int newId = getFreeId();
        givenIds.add(newId);
        MySensorsMessage newMsg = new MySensorsMessage(255, 255, 3, 0, 4, newId + "");
        mysCon.addMySensorsOutboundMessage(newMsg);
    }

    private int getFreeId() {
        int id = 1;

        List<Number> takenIds = new ArrayList<Number>();

        // Which ids are taken in Thing list of OpenHAB
        Collection<Thing> thingList = thingRegistry.getAll();
        Iterator<Thing> iterator = thingList.iterator();

        while (iterator.hasNext()) {
            Thing thing = iterator.next();
            Configuration conf = thing.getConfiguration();
            if (conf != null) {
                Object nodeIdobj = conf.get("nodeId");
                if (nodeIdobj != null) {
                    int nodeId = Integer.parseInt(nodeIdobj.toString());
                    takenIds.add(nodeId);
                }
            }
        }

        // Which ids are already given by the binding, but not yet in the thing list?
        Iterator<Number> iteratorGiven = givenIds.iterator();
        while (iteratorGiven.hasNext()) {
            takenIds.add(iteratorGiven.next());
        }

        // generate new id
        boolean foundId = false;
        while (!foundId) {
            Random rand = new Random(System.currentTimeMillis());
            int newId = rand.nextInt((254 - 1) + 1) + 1;
            if (!takenIds.contains(newId)) {
                id = newId;
                foundId = true;
            }
        }

        return id;
    }

    @Override
    public void revertToOldStatus(MySensorsStatusUpdateEvent event) {
        // TODO Auto-generated method stub

    }

    public MySensorsBridgeConnection getBridgeConnection() {
        return mysCon;
    }

}
