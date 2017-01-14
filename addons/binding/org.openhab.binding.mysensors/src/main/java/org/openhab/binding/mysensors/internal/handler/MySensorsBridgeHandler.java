/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.handler;

import static org.openhab.binding.mysensors.MySensorsBindingConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.mysensors.config.MySensorsBridgeConfiguration;
import org.openhab.binding.mysensors.internal.event.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.internal.event.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.factory.MySensorsCacheFactory;
import org.openhab.binding.mysensors.internal.protocol.MySensorsBridgeConnection;
import org.openhab.binding.mysensors.internal.protocol.ip.MySensorsIpConnection;
import org.openhab.binding.mysensors.internal.protocol.serial.MySensorsSerialConnection;
import org.openhab.binding.mysensors.internal.sensors.MySensorsDeviceManager;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

/**
 * MySensorsBridgeHandler is used to initialize a new bridge (in MySensors: Gateway)
 * The sensors are connected via the gateway/bridge to the controller
 *
 * @author Tim Oberföll
 *
 */
public class MySensorsBridgeHandler extends BaseBridgeHandler implements MySensorsUpdateListener {

    private Logger logger = LoggerFactory.getLogger(MySensorsBridgeHandler.class);

    // Bridge connection
    private MySensorsBridgeConnection myCon = null;

    // Device manager
    private MySensorsDeviceManager myDevManager = null;

    // Configuration from thing file
    private MySensorsBridgeConfiguration myConfiguration = null;

    // Cache file
    private MySensorsCacheFactory bindingCacheFile = null;

    public MySensorsBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initialization of the MySensors bridge");

        myConfiguration = getConfigAs(MySensorsBridgeConfiguration.class);

        if (getThing().getThingTypeUID().equals(THING_TYPE_BRIDGE_SER)) {
            myCon = new MySensorsSerialConnection(this, myConfiguration.serialPort, myConfiguration.baudRate,
                    myConfiguration.sendDelay);
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_BRIDGE_ETH)) {
            myCon = new MySensorsIpConnection(this, myConfiguration.ipAddress, myConfiguration.tcpPort,
                    myConfiguration.sendDelay);
        } else {
            logger.error("Not recognized bridge: {}", getThing().getThingTypeUID());
        }

        if (myCon != null) {
            myCon.initialize();
            myCon.addEventListener(this);
        }

        myDevManager = new MySensorsDeviceManager(myCon, loadCacheFile());
        myCon.addEventListener(myDevManager);

        logger.debug("Initialization of the MySensors bridge DONE!");
    }

    @Override
    public void dispose() {
        logger.debug("Disposing of the MySensors bridge");
        if (myCon != null) {
            myCon.removeEventListener(myDevManager);
            myCon.removeEventListener(this);
            myCon.destroy();
        }

        saveCacheFile();

        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    /**
     * Getter for the configuration of the bridge.
     *
     * @return Configuration of the MySensors bridge.
     */
    public MySensorsBridgeConfiguration getBridgeConfiguration() {
        return myConfiguration;
    }

    /**
     * Getter for the connection to the MySensors bridge / gateway.
     * Used for receiving (register handler) and sending of messages.
     *
     * @return Connection to the MySensors bridge / gateway.
     */
    public MySensorsBridgeConnection getBridgeConnection() {
        return myCon;
    }

    /**
     * Getter for the device manager of the MySensors binding.
     *
     * @return Link to the device manager of the MySensors binding.
     */
    public MySensorsDeviceManager getDeviceManager() {
        return myDevManager;
    }

    @Override
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event) {
        switch (event.getEventType()) {
            case NEW_NODE_DISCOVERED:
                updateCacheFile((MySensorsNode) event.getData());
                break;
            case BRIDGE_STATUS_UPDATE:
                if (((MySensorsBridgeConnection) event.getData()).isConnected()) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
                break;
            default:
                break;
        }

    }

    /**
     * Reads the cache file in which the known node ids from the MySensors network are stored.
     *
     * @return A list of known node ids.
     */
    private List<MySensorsNode> loadCacheFile() {
        MySensorsCacheFactory cacheFactory = MySensorsCacheFactory.getCacheFactory();
        List<MySensorsNode> nodes = new ArrayList<MySensorsNode>();

        List<Integer> givenIds = cacheFactory.readCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE,
                new ArrayList<Integer>(), new TypeToken<ArrayList<Integer>>() {
                }.getType());

        // Add ids taken by Thing list of OpenHAB
        Collection<Thing> thingList = thingRegistry.getAll();
        Iterator<Thing> iterator = thingList.iterator();
        while (iterator.hasNext()) {
            Thing thing = iterator.next();
            Configuration conf = thing.getConfiguration();
            if (conf != null) {
                Object nodeIdobj = conf.get("nodeId");
                if (nodeIdobj != null) {
                    int nodeId = Integer.parseInt(nodeIdobj.toString());
                    if (!givenIds.contains(nodeId)) {
                        givenIds.add(nodeId);
                    }
                }
            }
        }

        for (Integer i : givenIds) {
            if (i != null) {
                nodes.add(new MySensorsNode(i));
            }
        }

        return nodes;
    }

    /**
     * Stores a new node id in the cache file. The node ids are persisted during restarts.
     *
     * @param newNode The new discovered / configured node.
     */
    private void updateCacheFile(MySensorsNode newNode) {

        MySensorsCacheFactory cacheFactory = MySensorsCacheFactory.getCacheFactory();

        List<Integer> givenIds = cacheFactory.readCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE,
                new ArrayList<Integer>(), new TypeToken<ArrayList<Integer>>() {
                }.getType());

        // Add ids taken by Thing list of OpenHAB
        Collection<Thing> thingList = thingRegistry.getAll();
        Iterator<Thing> iterator = thingList.iterator();

        while (iterator.hasNext()) {
            Thing thing = iterator.next();
            Configuration conf = thing.getConfiguration();
            if (conf != null) {
                Object nodeIdobj = conf.get("nodeId");
                if (nodeIdobj != null) {
                    int nodeId = Integer.parseInt(nodeIdobj.toString());
                    if (!givenIds.contains(nodeId)) {
                        givenIds.add(nodeId);
                    }
                }
            }
        }

        if (newNode != null) {
            givenIds.add(newNode.getNodeId());
        }

        cacheFactory.writeCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE, givenIds.toArray(new Integer[] {}),
                Integer[].class);

    }

    /**
     * Save the id cache to the filesystem to persist the data during restarts.
     *
     */
    private void saveCacheFile() {

        if (myDevManager != null) {

            MySensorsCacheFactory cacheFactory = MySensorsCacheFactory.getCacheFactory();

            List<Integer> givenIds = cacheFactory.readCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE,
                    new ArrayList<Integer>(), new TypeToken<ArrayList<Integer>>() {
                    }.getType());

            // Add ids taken by Thing list of OpenHAB
            Collection<Thing> thingList = thingRegistry.getAll();
            Iterator<Thing> iterator = thingList.iterator();

            while (iterator.hasNext()) {
                Thing thing = iterator.next();
                Configuration conf = thing.getConfiguration();
                if (conf != null) {
                    Object nodeIdobj = conf.get("nodeId");
                    if (nodeIdobj != null) {
                        int nodeId = Integer.parseInt(nodeIdobj.toString());
                        if (!givenIds.contains(nodeId)) {
                            givenIds.add(nodeId);
                        }
                    }
                }
            }

            Set<Integer> onDeviceManager = getDeviceManager().getGivenIds();
            for (Integer i : onDeviceManager) {
                if (i != null && !givenIds.contains(i)) {
                    givenIds.add(i);
                }
            }

            cacheFactory.writeCache(MySensorsCacheFactory.GIVEN_IDS_CACHE_FILE, givenIds.toArray(new Integer[] {}),
                    Integer[].class);
        }

    }
}
