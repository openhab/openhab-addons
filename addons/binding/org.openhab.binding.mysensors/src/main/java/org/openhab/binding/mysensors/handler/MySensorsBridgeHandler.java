package org.openhab.binding.mysensors.handler;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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

    private MySensorsBridgeConnection mysCon = null;

    // Is (I)mperial or (M)etric?
    private String iConfig = null;

    private boolean skipStartupCheck = false;

    public MySensorsBridgeHandler(Bridge bridge) {
        super(bridge);

        boolean imperial = getConfigAs(MySensorsBridgeConfiguration.class).imperial;
        iConfig = imperial ? "I" : "M";

        skipStartupCheck = getConfigAs(MySensorsBridgeConfiguration.class).skipStartupCheck;
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
                    configuration.sendDelay, skipStartupCheck);
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_BRIDGE_ETH)) {
            mysCon = new MySensorsIpConnection(configuration.ipAddress, configuration.tcpPort, configuration.sendDelay,
                    skipStartupCheck);
        }

        mysCon.addUpdateListener(this);

        if (mysCon.connect()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            mysCon.removeUpdateListener(this);
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

        // Do we get an ACK?
        if (msg.getAck() == 1) {
            logger.debug("ACK received!");
            mysCon.removeMySensorsOutboundMessage(msg);
        }

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

        // Have we get a I_VERSION message?
        if (msg.getNodeId() == 0) {
            if (msg.getChildId() == 0 || msg.getChildId() == 255) {
                if (msg.getMsgType() == MYSENSORS_MSG_TYPE_INTERNAL) {
                    if (msg.getAck() == 0) {
                        if (msg.getSubType() == MYSENSORS_SUBTYPE_I_VERSION) {
                            handleIncomingVersionMessage(msg.msg);
                        }
                    }
                }
            }
        }

        // Have we get a I_CONFIG message?
        if (msg.getNodeId() == 0) {
            if (msg.getChildId() == 0 || msg.getChildId() == 255) {
                if (msg.getMsgType() == MYSENSORS_MSG_TYPE_INTERNAL) {
                    if (msg.getAck() == 0) {
                        if (msg.getSubType() == MYSENSORS_SUBTYPE_I_CONFIG) {
                            answerIConfigMessage(msg);
                        }
                    }
                }
            }
        }

        // Have we get a I_TIME message?
        if (msg.getNodeId() == 0) {
            if (msg.getChildId() == 0 || msg.getChildId() == 255) {
                if (msg.getMsgType() == MYSENSORS_MSG_TYPE_INTERNAL) {
                    if (msg.getAck() == 0) {
                        if (msg.getSubType() == MYSENSORS_SUBTYPE_I_TIME) {
                            answerITimeMessage(msg);
                        }
                    }
                }
            }
        }
    }

    /**
     * Answer to I_TIME message for gateway time request from sensor
     *
     * @param msg, the incoming I_TIME message from sensor
     */
    private void answerITimeMessage(MySensorsMessage msg) {
        logger.info("I_TIME request received from {}, answering...", msg.nodeId);

        String time = Long.toString(System.currentTimeMillis());
        MySensorsMessage newMsg = new MySensorsMessage(msg.nodeId, msg.childId, MYSENSORS_MSG_TYPE_INTERNAL, 0,
                MYSENSORS_SUBTYPE_I_TIME, time);
        mysCon.addMySensorsOutboundMessage(newMsg);

    }

    /**
     * Answer to I_CONFIG message for imperial/metric request from sensor
     *
     * @param msg, the incoming I_CONFIG message from sensor
     */
    private void answerIConfigMessage(MySensorsMessage msg) {
        logger.info("I_CONFIG request received from {}, answering...", msg.nodeId);

        MySensorsMessage newMsg = new MySensorsMessage(msg.nodeId, msg.childId, MYSENSORS_MSG_TYPE_INTERNAL, 0,
                MYSENSORS_SUBTYPE_I_CONFIG, iConfig);
        mysCon.addMySensorsOutboundMessage(newMsg);

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
        logger.info("New Node in the MySensors network has requested an ID. ID is: {}", newId);
    }

    /**
     * Wake up main thread that is waiting for confirmation of link up
     */
    private void handleIncomingVersionMessage(String message) {
        mysCon.iVersionMessageReceived(message);
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

    public MySensorsBridgeConnection getBridgeConnection() {
        return mysCon;
    }

    @Override
    public void disconnectEvent() {
        updateStatus(ThingStatus.OFFLINE);
    }
}
