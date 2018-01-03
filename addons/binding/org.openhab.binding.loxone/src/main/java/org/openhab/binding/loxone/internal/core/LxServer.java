/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.openhab.binding.loxone.internal.core.LxServerEvent.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loxone Miniserver representaton.
 * <p>
 * A Miniserver is identified by host address, user name and password used for logging into it.
 * They are provided to the constructor of this class. Upon creation of the object, you need to call
 * {@link #start()} method to initiate communication with the Miniserver, obtain its runtime configuration and
 * process live updates of controls' state changes.
 * <p>
 * Runtime configuration consists of following items:
 * <ul>
 * <li>server parameters: serial number, location, project name, server name
 * <li>'room' and 'category' proprietary display names
 * <li>a list of rooms
 * <li>a list of control categories
 * <li>a list of controls, each may be assigned to a room and a category
 * <li>a list of states for each of the controls
 * </ul>
 * <p>
 * Once server is populated with runtime configuration, its controls may be used to perform operations.
 * <p>
 * If server is not needed anymore, a {@link #stop()} method should be called to close open connections and stop
 * processing thread.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxServer {

    // Configuration
    private final InetAddress host;
    private final int port;
    private final String user;
    private final String password;

    private String miniserverName = "";
    private String projectName = "";
    private String location = "";
    private String serial = "";
    private String cloudAddress = "";

    private int firstConDelay = 1;
    private int connectErrDelay = 10;
    private int userErrorDelay = 60;
    private int comErrorDelay = 30;

    // Data structures
    private Map<LxUuid, LxControl> controls = new HashMap<>();
    private Map<LxUuid, LxContainer> rooms = new HashMap<>();
    private Map<LxUuid, LxCategory> categories = new HashMap<>();
    // Map of state UUID to a map of control UUID and state objects
    // State with a unique UUID can be configured in many controls and each control can even have a different name of
    // the state. It must be ensured that updates received for this state UUID are passed to all controls that have this
    // state UUID configured.
    private Map<LxUuid, Map<LxUuid, LxControlState>> states = new HashMap<>();
    private List<LxServerListener> listeners = new ArrayList<>();

    // Services
    private boolean running = true;
    private LxWsClient socketClient;
    private Thread monitorThread;
    private BlockingQueue<LxServerEvent> queue = new LinkedBlockingQueue<>();

    private Logger logger = LoggerFactory.getLogger(LxServer.class);

    private int debugId;
    private static AtomicInteger staticDebugId = new AtomicInteger(1);

    /**
     * Creates a new instance of Loxone Miniserver with provided host address and credentials.
     *
     * @param host
     *            host address of the Miniserver
     * @param port
     *            web service port of the Miniserver
     * @param user
     *            user name used for logging in
     * @param password
     *            password used for logging in
     */
    public LxServer(InetAddress host, int port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;

        debugId = staticDebugId.getAndIncrement();
        socketClient = new LxWsClient(debugId, queue, host, port, user, password);
    }

    /**
     * Initiate communication with the Miniserver.
     * Starts thread that handles communication.
     */
    public void start() {
        logger.debug("[{}] Server start", debugId);
        if (monitorThread == null) {
            monitorThread = new LxServerThread(this);
            monitorThread.start();
        }
    }

    /**
     * Stop server thread, close communication with Miniserver.
     */
    public void stop() {
        if (monitorThread != null) {
            logger.debug("[{}] Server stop", debugId);
            synchronized (monitorThread) {
                if (queue != null) {
                    LxServerEvent event = new LxServerEvent(EventType.CLIENT_CLOSING, LxOfflineReason.NONE, null);
                    try {
                        queue.put(event);
                        monitorThread.notify();
                    } catch (InterruptedException e) {
                        monitorThread.interrupt();
                    }
                } else {
                    monitorThread.interrupt();
                }
            }
        } else {
            logger.debug("[{}] Server stop - no thread", debugId);
        }
    }

    /**
     * Update server's configuration.
     * <p>
     * Only timeout parameters can be updated in runtime without breaking connection to the Miniserver.
     * If other parameters must be changed, server should be stopped and a new instance created.
     *
     * @param firstConDelay
     *            Time in seconds between binding initialization and first connection attempt
     * @param keepAlivePeriod
     *            Time in seconds between sending two consecutive keep-alive messages
     * @param connectErrDelay
     *            Time in seconds between failed websocket connect attempts
     * @param connectTimeout
     *            Time to wait for websocket connect response from the Miniserver
     * @param userErrorDelay
     *            Time in seconds between user login error as a result of wrong name/password or no authority and next
     *            connection attempt
     * @param comErrorDelay
     *            Time in seconds between connection close (as a result of some communication error) and next connection
     *            attempt
     * @param maxBinMsgSize
     *            maximum binary message size of websocket client (in kB)
     * @param maxTextMsgSize
     *            maximum text message size of websocket client (in kB)
     */
    public void update(int firstConDelay, int keepAlivePeriod, int connectErrDelay, int connectTimeout,
            int userErrorDelay, int comErrorDelay, int maxBinMsgSize, int maxTextMsgSize) {
        logger.debug("[{}] Server update configuration", debugId);

        if (firstConDelay >= 0 && this.firstConDelay != firstConDelay) {
            logger.debug("[{}] Changing firstConDelay to {}", debugId, firstConDelay);
            this.firstConDelay = firstConDelay;
        }
        if (connectErrDelay >= 0 && this.connectErrDelay != connectErrDelay) {
            logger.debug("[{}] Changing connectErrDelay to {}", debugId, connectErrDelay);
            this.connectErrDelay = connectErrDelay;
        }
        if (userErrorDelay >= 0 && this.userErrorDelay != userErrorDelay) {
            logger.debug("[{}] Changing userErrorDelay to {}", debugId, userErrorDelay);
            this.userErrorDelay = userErrorDelay;
        }
        if (comErrorDelay >= 0 && this.comErrorDelay != comErrorDelay) {
            logger.debug("[{}] Changing comErrorDelay to {}", debugId, comErrorDelay);
            this.comErrorDelay = comErrorDelay;
        }
        if (socketClient != null) {
            socketClient.update(keepAlivePeriod, connectTimeout, maxBinMsgSize, maxTextMsgSize);
        }
    }

    /**
     * Adds a listener to server's events
     *
     * @param listener
     *            an object implementing server's listener interface
     */
    public void addListener(LxServerListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener of server's events
     *
     * @param listener
     *            listener object to remove
     */
    public void removeListener(LxServerListener listener) {
        listeners.remove(listener);
    }

    /**
     * Checks if current Miniserver configuration differs from provided parameters.
     *
     * @param host
     *            A new host address to check against
     * @param user
     *            A new user name to check against
     * @param port
     *            A new web service port to check against
     * @param password
     *            A new password to check against
     * @return
     *         true if current Miniserver configuration is different
     */
    public boolean isChanged(InetAddress host, int port, String user, String password) {
        return (!(this.port == port && this.host.toString().equals(host.toString()) && this.user.equals(user)
                && this.password.equals(password)));
    }

    /**
     * Searches for a control with given UUID
     *
     * @param id
     *            UUID of the control to locate
     * @return
     *         Found control or null if not found
     */
    public LxControl findControl(LxUuid id) {
        if (controls == null || id == null) {
            return null;
        }
        if (controls.containsKey(id)) {
            return controls.get(id);
        }
        return null;
    }

    /**
     * Searches for a control with given name (descriptive)
     *
     * @param name
     *            A name of the control to locate
     * @return
     *         Found control or null if not found
     */
    public LxControl findControl(String name) {
        for (LxControl l : controls.values()) {
            if (l.getName().equals(name)) {
                return l;
            }
        }
        return null;
    }

    /**
     * Gets a set of all controls for this Miniserver
     *
     * @return Map of controls with UUID as a key
     */
    public Map<LxUuid, LxControl> getControls() {
        return controls;
    }

    /**
     * Gets Miniserver name
     *
     * @return Miniserver name
     */
    public String getMiniserverName() {
        return miniserverName;
    }

    /**
     * Gets project name as configured on the Miniserver
     *
     * @return project name
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Gets Miniserver cloud address
     *
     * @return cloud URL
     */
    public String getCloudAddress() {
        return cloudAddress;
    }

    /**
     * Gets device location as configured on the Miniserver
     *
     * @return Description of the device location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Gets device serial number as configured on the Miniserver
     *
     * @return Device serial number
     */
    public String getSerial() {
        return serial;
    }

    /**
     * Thread that performs and supervises communication with the Miniserver.
     * <p>
     * It will try to maintain the connection as long as possible, handling errors and interruptions. There are two
     * reasons when this thread will terminate and stop connecting to the Miniserver:
     * when it receives close command from supervisor ({@link LxServer} or when Miniserver locks out user due to too
     * many unsuccessful login attempts.
     *
     * @author Pawel Pieczul - initial contribution
     *
     */
    private class LxServerThread extends Thread {
        LxServer server;

        LxServerThread(LxServer server) {
            this.server = server;
        }

        @Override
        public void run() {
            logger.debug("[{}] Thread starting", debugId);

            // initial delay to initiate connection
            int waitTime = firstConDelay * 1000;

            while (running) {
                // wait until next connect attempt, this time depends on what happened before
                synchronized (monitorThread) {
                    try {
                        monitorThread.wait(waitTime);
                    } catch (InterruptedException e) {
                        logger.debug("[{}] Server thread sleep interrupted, terminating", debugId);
                        running = false;
                        break;
                    }
                }

                // attempt to connect to the Miniserver
                logger.debug("[{}] Server connecting to websocket", debugId);
                boolean connected = socketClient.connect();
                if (!connected) {
                    logger.debug("[{}] Websocket connect failed, retrying after pause", debugId);
                    waitTime = connectErrDelay * 1000;
                    continue;
                }

                while (connected) {
                    try {
                        LxServerEvent wsMsg = queue.take();
                        EventType event = wsMsg.getEvent();
                        logger.trace("[{}] Server received event: {}", debugId, event);

                        switch (event) {
                            case RECEIVED_CONFIG:
                                LxJsonApp3 config = (LxJsonApp3) wsMsg.getObject();
                                if (config != null) {
                                    updateConfig(config);
                                    for (LxServerListener listener : listeners) {
                                        listener.onNewConfig(server);
                                    }
                                } else {
                                    logger.debug("[{}] Server failed processing received configuration", debugId);
                                }
                                break;
                            case STATE_UPDATE:
                                LxWsStateUpdateEvent update = (LxWsStateUpdateEvent) wsMsg.getObject();
                                Map<LxUuid, LxControlState> perStateUuid = findState(update.getUuid());
                                if (perStateUuid != null) {
                                    perStateUuid.forEach((controlUuid, state) -> {
                                        state.setValue(update.getValue(), update.getText());
                                        LxControl control = state.getControl();
                                        if (control != null) {
                                            logger.debug("[{}] State update {} ({}:{}) to value {}, text '{}'", debugId,
                                                    update.getUuid(), control.getName(), state.getName(),
                                                    update.getValue(), update.getText());
                                            for (LxServerListener listener : listeners) {
                                                listener.onControlStateUpdate(control, state.getName().toLowerCase());
                                            }
                                        } else {
                                            logger.debug("[{}] State update {} ({}) of unknown control", debugId,
                                                    update.getUuid(), state.getName());
                                        }
                                    });
                                }
                                break;
                            case SERVER_ONLINE:
                                for (LxServerListener listener : listeners) {
                                    listener.onServerGoesOnline();
                                }
                                break;
                            case SERVER_OFFLINE:
                                LxOfflineReason reason = wsMsg.getOfflineReason();
                                String details = null;
                                if (wsMsg.getObject() instanceof String) {
                                    details = (String) wsMsg.getObject();
                                }
                                logger.debug("[{}] Websocket goes OFFLINE, reason {} : {}.", debugId, reason, details);

                                if (reason == LxOfflineReason.TOO_MANY_FAILED_LOGIN_ATTEMPTS) {
                                    // assume credentials are wrong, do not re-attempt connections
                                    // close thread and expect a new LxServer object will have to be re-created
                                    // with corrected configuration
                                    running = false;
                                } else {
                                    if (reason == LxOfflineReason.UNAUTHORIZED) {
                                        waitTime = userErrorDelay * 1000;
                                    } else {
                                        waitTime = comErrorDelay * 1000;
                                    }
                                    socketClient.disconnect();
                                }
                                connected = false;
                                for (LxServerListener listener : listeners) {
                                    listener.onServerGoesOffline(reason, details);
                                }
                                break;
                            case CLIENT_CLOSING:
                                connected = false;
                                running = false;
                                break;
                            default:
                                logger.debug("[{}] Received unknown request {}", debugId, wsMsg.getEvent().name());
                                break;
                        }
                    } catch (InterruptedException e) {
                        logger.debug("[{}] Waiting for sync event interrupted, reason = {}", debugId, e.getMessage());
                        connected = false;
                        running = false;
                    }
                }
            }
            logger.debug("[{}] Thread ending", debugId);
            socketClient.disconnect();
            monitorThread = null;
            queue = null;
        }
    }

    /**
     * Updates runtime configuration from parsed JSON configuration file of Loxone Miniserver (LoxApp3.json)
     *
     * @param config
     *            parsed JSON LoxApp3.json file
     */
    private void updateConfig(LxJsonApp3 config) {
        logger.trace("[{}] Updating configuration from Miniserver", debugId);

        invalidateMap(rooms);
        invalidateMap(categories);
        invalidateMap(controls);
        invalidateMap(states);

        if (config.msInfo != null) {
            logger.trace("[{}] updating global config", debugId);
            miniserverName = buildName(config.msInfo.msName);
            projectName = buildName(config.msInfo.projectName);
            location = buildName(config.msInfo.location);
            serial = buildName(config.msInfo.serialNr);
            cloudAddress = buildName(config.msInfo.remoteUrl);
        } else {
            logger.warn("[{}] missing global configuration msInfo on Loxone", debugId);
        }

        // create internal structures based on configuration file
        if (config.rooms != null) {
            logger.trace("[{}] creating rooms", debugId);
            for (LxJsonApp3.LxJsonRoom room : config.rooms.values()) {
                addOrUpdateRoom(new LxUuid(room.uuid), room.name);
            }
        }
        if (config.cats != null) {
            logger.trace("[{}] creating categories", debugId);
            for (LxJsonApp3.LxJsonCat cat : config.cats.values()) {
                addOrUpdateCategory(new LxUuid(cat.uuid), cat.name, cat.type);
            }
        }
        if (config.controls != null) {
            logger.trace("[{}] creating controls", debugId);
            for (LxJsonApp3.LxJsonControl ctrl : config.controls.values()) {
                // create a new control or update existing one
                try {
                    addOrUpdateControl(ctrl);
                } catch (Exception e) {
                    logger.error("[{}] exception creating control {}: ", debugId, ctrl.name, e);
                }
            }
        }
        // remove items that do not exist anymore in Miniserver
        logger.trace("[{}] removing unused objects", debugId);
        removeUnusedFromMap(rooms);
        removeUnusedFromMap(categories);
        removeUnusedFromMap(controls);
        removeUnusedFromMap(states);
    }

    /**
     * Removes all entries from a map, that do not have the 'updated' flag set on UUID key
     *
     * @param <T>
     *            any type of container used in the map
     * @param map
     *            map to remove entries from
     */

    private <T> void removeUnusedFromMap(Map<LxUuid, T> map) {
        for (Iterator<Map.Entry<LxUuid, T>> it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry<LxUuid, T> entry = it.next();
            if (!entry.getKey().getUpdate()) {
                it.remove();
                if (entry.getValue() instanceof LxControl) {
                    ((LxControl) entry.getValue()).dispose();
                }
            }
        }
    }

    /**
     * Sets all entries in a map to not updated
     *
     * @param map
     *            map to invalidate entries in
     */
    private void invalidateMap(Map<LxUuid, ?> map) {
        map.keySet().forEach(k -> {
            k.setUpdate(false);
        });
    }

    /**
     * Search for a room with given UUID
     *
     * @param id
     *            UUID of a room to search for
     * @return
     *         found room on null if not found
     */
    private LxContainer findRoom(LxUuid id) {
        if (rooms == null || id == null) {
            return null;
        }
        if (rooms.containsKey(id)) {
            return rooms.get(id);
        }
        return null;
    }

    /**
     * Add a room to the server, if a room with same UUID already does not exist, otherwise update it with new name.
     *
     * @param id
     *            UUID of the room to add
     * @param name
     *            name of the room to add
     * @return
     *         room object (either newly created or already existing) or null if wrong parameters
     */
    private LxContainer addOrUpdateRoom(LxUuid id, String name) {
        if (rooms == null) {
            return null;
        }
        LxContainer r = findRoom(id);
        if (r != null) {
            r.setName(name);
            return r;
        }
        LxContainer nr = new LxContainer(id, name);
        rooms.put(id, nr);
        return nr;
    }

    /**
     * Search for a state with given UUID
     *
     * @param id
     *            UUID of state to locate
     * @return
     *         map of all state objects with control UUID as key
     */
    private Map<LxUuid, LxControlState> findState(LxUuid id) {
        if (states == null || id == null) {
            return null;
        }
        if (states.containsKey(id)) {
            return states.get(id);
        }
        return null;
    }

    /**
     * Search for a category on the server
     *
     * @param id
     *            UUID of the category to find
     * @return
     *         category object found or null if not found
     */
    private LxCategory findCategory(LxUuid id) {
        if (categories == null || id == null) {
            return null;
        }
        if (categories.containsKey(id)) {
            return categories.get(id);
        }
        return null;
    }

    /**
     * Add a new category or update and return existing one with same UUID
     *
     * @param id
     *            UUID of the category to add or update
     * @param name
     *            name of the category
     * @param type
     *            type of the category
     * @return
     *         newly added category or already existing and updated, null if wrong parameters/configuration
     */
    private LxCategory addOrUpdateCategory(LxUuid id, String name, String type) {
        if (categories == null) {
            return null;
        }
        LxCategory c = findCategory(id);
        if (c != null) {
            c.setName(name);
            c.setType(type);
            return c;
        }
        LxCategory nc = new LxCategory(id, name, type);
        categories.put(id, nc);
        return nc;
    }

    /**
     * Add a new control and its states or update and return existing one with same UUID
     *
     * @param json
     *            JSON original object of this control to get extra parameters
     */
    private void addOrUpdateControl(LxJsonApp3.LxJsonControl json) {
        if (json == null || json.uuidAction == null || json.name == null || json.type == null) {
            return;
        }

        LxUuid categoryId = null;
        if (json.cat != null) {
            categoryId = new LxUuid(json.cat);
        }
        LxUuid roomId = null;
        if (json.room != null) {
            roomId = new LxUuid(json.room);
        }
        LxContainer room = findRoom(roomId);
        LxCategory category = findCategory(categoryId);

        LxUuid id = new LxUuid(json.uuidAction);
        LxControl control = findControl(id);
        if (control != null) {
            control.update(json, room, category);
        } else {
            control = LxControlFactory.createControl(socketClient, id, json, room, category);
        }
        if (control != null) {
            updateControls(control);
        }
    }

    /**
     * Updates server structures with a new or updated control and its states and subcontrols
     *
     * @param control
     *            control to update in server structures
     */
    private void updateControls(LxControl control) {
        for (LxControlState state : control.getStates().values()) {
            state.getUuid().setUpdate(true);
            Map<LxUuid, LxControlState> perUuid = states.get(state.getUuid());
            if (perUuid == null) {
                perUuid = new HashMap<>();
                states.put(state.getUuid(), perUuid);
            }
            perUuid.put(control.uuid, state);
        }
        controls.put(control.uuid, control);
        control.uuid.setUpdate(true);
        for (LxControl subControl : control.getSubControls().values()) {
            updateControls(subControl);
        }
    }

    /**
     * Check and convert null string to empty string.
     *
     * @param name
     *            string to check
     * @return
     *         string guaranteed to be not null
     */
    private String buildName(String name) {
        if (name == null) {
            return "";
        }
        return name;
    }
}
