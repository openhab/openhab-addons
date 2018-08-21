/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.internal.ws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.ihc2.internal.discovery.Ihc2DiscoveredThing;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSControllerState;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSDate;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSFile;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSLoginResult;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSProjectInfo;
import org.openhab.binding.ihc2.internal.ws.datatypes.WSResourceValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IhcClient provides interface to communicate IHC / ELKO LS Controller.
 *
 * Controller interface is SOAP web service based via HTTPS link.
 *
 * @author Pauli Anttila
 * @since 1.1.0
 */
public class Ihc2Client {

    /*
     * If you wonder, why e.g. Axis(2) or JAX-WS is not used to handle SOAP
     * interface...
     *
     * WSDL files are included, so feel free to try ;) Pauli Anttila
     *
     * I tried. But IHC SOAP does not follow the SOAP standard to the point.
     * So I could only get it to work for some of the SOAP calls. Niels Peter Enemark
     */

    // Singleton
    private static class InstanceHolder {
        static final Ihc2Client INSTANCE = new Ihc2Client();
    }

    private Ihc2Client() {
    } // Prevent instantiation.

    public static Ihc2Client getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /** Current state of the connection */
    public enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    /** Current state of the connection */
    public enum DiscoveryLevel {
        NOTHING,
        LINKED_RESOURCES,
        ALL,
        CLEAN
    }

    private final Logger logger = LoggerFactory.getLogger(Ihc2Client.class);

    private static ConnectionState connState = ConnectionState.DISCONNECTED;

    /** Controller services */
    private static Ihc2AuthenticationService authenticationService = null;
    private static Ihc2ResourceInteractionService resourceInteractionService = null;
    private static Ihc2ControllerService controllerService = null;

    /** Thread to handle resource value notifications from the controller */
    private IhcResourceValueNotificationListener resourceValueNotificationListener = null;

    /** Thread to handle controller's state change notifications */
    private IhcControllerStateListener controllerStateListener = null;

    private String username = "";
    private String password = "";
    private String ip = "";
    private int timeout = 5000; // milliseconds
    private String projectFile = null;
    private String dumpResourcesToFile = null;

    private Map<Integer, WSResourceValue> resourceValues = new HashMap<Integer, WSResourceValue>();
    private HashMap<Integer, ArrayList<Ihc2EnumValue>> enumDictionary = new HashMap<Integer, ArrayList<Ihc2EnumValue>>();
    private List<ListenerResourcePair> eventListeners = new ArrayList<ListenerResourcePair>();
    private WSControllerState controllerState = null;

    private DiscoveryLevel discoveryLevel = DiscoveryLevel.NOTHING;
    private List<Ihc2DiscoveredThing> discoveredThingsList;

    private WSProjectInfo projectInfo = null;

    private HashSet<Integer> resourceIdList = new HashSet<Integer>();

    private HashMap<ChannelUID, StateDescription> stateDescriptionDictionary = new HashMap<ChannelUID, StateDescription>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getTimeoutInMillisecods() {
        return timeout;
    }

    public void setTimeoutInMillisecods(int timeout) {
        this.timeout = timeout;
    }

    public String getProjectFile() {
        return projectFile;
    }

    public void setProjectFile(String path) {
        this.projectFile = path;
    }

    public String getDumpResourceInformationToFile() {
        return dumpResourcesToFile;
    }

    public void setDumpResourceInformationToFile(String value) {
        this.dumpResourcesToFile = value;
    }

    public synchronized ConnectionState getConnectionState() {
        return connState;
    }

    private synchronized void setConnectionState(ConnectionState newState) {
        Ihc2Client.connState = newState;
    }

    public void addEventListener(Ihc2EventListener listener, int resourceId) {
        Iterator<ListenerResourcePair> iterator = eventListeners.iterator();
        while (iterator.hasNext()) {
            ListenerResourcePair lrp = iterator.next();
            if (lrp.resourceId == resourceId && lrp.listener == listener) {
                return;
            }
        }
        ListenerResourcePair lrp = new ListenerResourcePair(listener, resourceId);
        eventListeners.add(lrp);
    }

    public void removeEventListener(Ihc2EventListener listener) {
        Iterator<ListenerResourcePair> iterator = eventListeners.iterator();
        while (iterator.hasNext()) {
            ListenerResourcePair lrp = iterator.next();
            if (lrp.listener == listener) {
                iterator.remove();
            }
        }
    }

    public void addResourceId(int resourceId) throws Ihc2Execption {
        resourceIdList.add(resourceId);
        enableRuntimeValueNotifications();
    }

    /**
     * Close connection.
     *
     * @return
     */
    public void closeConnection() throws Ihc2Execption {
        logger.debug("Close connection");

        if (resourceValueNotificationListener != null) {
            resourceValueNotificationListener.setInterrupted(true);
        }
        if (controllerStateListener != null) {
            controllerStateListener.setInterrupted(true);
        }

        setConnectionState(ConnectionState.DISCONNECTED);
    }

    /**
     * Open connection and authenticate session to IHC / ELKO LS controller.
     *
     * @throws Ihc2Execption
     */
    public void openConnection() throws Ihc2Execption {
        logger.debug("Open connection");

        setConnectionState(ConnectionState.CONNECTING);

        authenticationService = new Ihc2AuthenticationService(ip, timeout);
        WSLoginResult loginResult = authenticationService.authenticate(username, password, "treeview");

        if (!loginResult.isLoginWasSuccessful()) {
            // Login failed

            setConnectionState(ConnectionState.DISCONNECTED);

            if (loginResult.isLoginFailedDueToAccountInvalid()) {
                throw new Ihc2Execption("login failed because of invalid account");
            }

            if (loginResult.isLoginFailedDueToConnectionRestrictions()) {
                throw new Ihc2Execption("login failed because of connection restrictions");
            }

            if (loginResult.isLoginFailedDueToInsufficientUserRights()) {
                throw new Ihc2Execption("login failed because of insufficient user rights");
            }

            throw new Ihc2Execption("login failed because of unknown reason");
        }

        logger.info("Connection successfully opened");

        resourceInteractionService = new Ihc2ResourceInteractionService(ip, timeout);
        controllerService = new Ihc2ControllerService(ip, timeout);
        controllerState = controllerService.getControllerState();

        projectInfo = controllerService.getProjectInfo();
        WSDate wsdate = projectInfo.getLastmodified();

        loadProject(wsdate);
        startIhcListeners();
        enableRuntimeValueNotifications();
        setConnectionState(ConnectionState.CONNECTED);
    }

    private void startIhcListeners() {
        logger.debug("startIhcListeners");
        resourceValueNotificationListener = new IhcResourceValueNotificationListener();
        resourceValueNotificationListener.start();
        controllerStateListener = new IhcControllerStateListener();
        controllerStateListener.start();
    }

    // /**
    // * Query project information from the controller.
    // *
    // * @return project information.
    // * @throws Ihc2Execption
    // */
    // public synchronized WSProjectInfo getProjectInfo() throws Ihc2Execption {
    //
    // return controllerService.getProjectInfo();
    // }

    public WSProjectInfo getProjectInfo() {
        return projectInfo;
    }

    /**
     * Query controller current state.
     *
     * @return controller's current state.
     */
    public WSControllerState getControllerState() {
        return controllerState;
    }

    /**
     * Load IHC / ELKO LS project file.
     *
     */
    private synchronized void loadProject(WSDate ctrlProjectModified) throws Ihc2Execption {
        File f = new File(projectFile);
        boolean controllerProjectHasChanged = false;

        if (f == null) {
            throw new Ihc2Execption("IHC Project File and Location must be set");
        }

        if (!f.exists()) {
            loadProjectFileFromController();
            controllerProjectHasChanged = true;
        }

        Ihc2ProjectFile ihcProjectFile = new Ihc2ProjectFile();
        ihcProjectFile.parseProject(projectFile);
        WSDate flieModifiedDate = ihcProjectFile.getProjectFileModifiedDate();

        if (!flieModifiedDate.equals(ctrlProjectModified)) {
            logger.info("IHC Project has changed since last download");
            loadProjectFileFromController();
            controllerProjectHasChanged = true;
        }
        ihcProjectFile.parseProject(projectFile);

        enumDictionary = ihcProjectFile.getEnumDictonary();

        discoveredThingsList = ihcProjectFile.getThingsFromProjectFile(discoveryLevel);

        if (StringUtils.isNotBlank(dumpResourcesToFile)) {
            ihcProjectFile.saveResourcesInFile(dumpResourcesToFile);
        }
    }

    private void loadProjectFileFromController() throws Ihc2Execption {
        try {
            WSProjectInfo projectInfo = getProjectInfo();
            int numberOfSegments = controllerService.getProjectNumberOfSegments();
            int segmentationSize = controllerService.getProjectSegmentationSize();

            logger.debug("Number of segments: {}", numberOfSegments);
            logger.debug("Segmentation size: {}", segmentationSize);

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

            for (int i = 0; i < numberOfSegments; i++) {
                logger.debug("Downloading segment {}", i);

                WSFile data = controllerService.getProjectSegment(i, projectInfo.getProjectMajorRevision(),
                        projectInfo.getProjectMinorRevision());
                byteStream.write(data.getData());
            }

            logger.debug("File size before base64 encoding: {} bytes", byteStream.size());

            byte[] decodedBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(byteStream.toString());

            logger.debug("File size after base64 encoding: {} bytes", decodedBytes.length);

            ByteArrayInputStream bais = new ByteArrayInputStream(decodedBytes);

            GZIPInputStream gzis = new GZIPInputStream(bais);

            FileOutputStream fos = new FileOutputStream(projectFile);

            byte[] buffer = new byte[1024];
            int len = 0;

            while ((len = gzis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            gzis.close();
        } catch (Exception e) {
            throw new Ihc2Execption(e);
        }
    }

    public DiscoveryLevel getDiscoveryLevel() {
        return discoveryLevel;
    }

    public void setDiscoveryLevel(DiscoveryLevel discoveryLevel) {
        this.discoveryLevel = discoveryLevel;
    }

    public List<Ihc2DiscoveredThing> getDiscoveredThingsList() {
        return discoveredThingsList;
    }

    /**
     * Wait controller state change notification.
     *
     * @param previousState
     *            Previous controller state.
     * @param timeoutInSeconds
     *            How many seconds to wait notifications.
     * @return current controller state.
     */
    private WSControllerState waitStateChangeNotifications(WSControllerState previousState, int timeoutInSeconds)
            throws Ihc2Execption {
        Ihc2ControllerService service = new Ihc2ControllerService(ip, timeout);
        return service.waitStateChangeNotifications(previousState, timeoutInSeconds);
    }

    /**
     * Returns all possible enumerated values for corresponding enum type.
     *
     * @param typedefId
     *            Enum type definition identifier.
     * @return list of enum values.
     */
    public ArrayList<Ihc2EnumValue> getEnumValues(int typedefId) {
        return enumDictionary.get(typedefId);
    }

    /**
     * Enable resources runtime value notifications.
     *
     * @param resourceIdList
     *            List of resource Identifiers.
     * @return True is connection successfully opened.
     */
    private synchronized void enableRuntimeValueNotifications() throws Ihc2Execption {
        if (resourceInteractionService == null) {
            return;
        }
        if (resourceIdList.size() == 0) {
            return;
        }

        resourceInteractionService.enableRuntimeValueNotifications(resourceIdList);
    }

    /**
     * Wait runtime value notifications.
     *
     * Runtime value notification should firstly be activated by
     * enableRuntimeValueNotifications function.
     *
     * @param timeoutInSeconds
     *            How many seconds to wait notifications.
     * @return List of received runtime value notifications.
     * @throws SocketTimeoutException
     */
    private List<? extends WSResourceValue> waitResourceValueNotifications(int timeoutInSeconds)
            throws Ihc2Execption, SocketTimeoutException {
        Ihc2ResourceInteractionService service = new Ihc2ResourceInteractionService(ip, timeout);
        List<? extends WSResourceValue> list = service.waitResourceValueNotifications(timeoutInSeconds);

        for (WSResourceValue val : list) {
            resourceValues.put(val.getResourceID(), val);
        }

        return list;
    }

    /**
     * Query resource value from controller.
     *
     *
     * @param resoureId
     *            Resource Identifier.
     * @return Resource value.
     */
    public WSResourceValue resourceQuery(int resoureId) throws Ihc2Execption {
        logger.debug("resourceQuery() {}", getConnectionState().toString());

        if (getConnectionState() != ConnectionState.CONNECTED) {
            throw new Ihc2Execption("Not connected to IHC Controller");
        }

        WSResourceValue data = resourceInteractionService.resourceQuery(resoureId);
        resourceValues.put(resoureId, data);
        return data;
    }

    /**
     * Get resource value information.
     *
     * Function return resource value from internal memory, if data is not
     * available information is read from the controller.
     *
     * Resource value's value field (e.g. floatingPointValue) could be old
     * information.
     *
     * @param resoureId
     *            Resource Identifier.
     * @return Resource value.
     */
    public WSResourceValue getResourceValueInformation(int resourceId) throws Ihc2Execption {
        logger.debug("getResourceValueInformation()");
        WSResourceValue data = resourceValues.get(resourceId);

        if (data == null) {
            // data is not available, read it from the controller
            data = resourceInteractionService.resourceQuery(resourceId);
            resourceValues.put(resourceId, data);
        }
        return data;
    }

    /**
     * Update resource value to controller.
     *
     *
     * @param value
     *            Resource value.
     * @return True if value is successfully updated.
     */
    public boolean resourceUpdate(WSResourceValue value) throws Ihc2Execption {
        logger.debug("resourceUpdate() {}({})", value.getResourceID(),
                "0x" + Integer.toHexString(value.getResourceID()));
        return resourceInteractionService.resourceUpdate(value);
    }

    /**
     * The IhcReader runs as a separate thread.
     *
     * Thread listen resource value notifications from IHC / ELKO LS controller
     * and post updates to openHAB bus when notifications are received.
     *
     */
    private class IhcResourceValueNotificationListener extends Thread {
        private boolean interrupted = false;

        public void setInterrupted(boolean interrupted) {
            this.interrupted = interrupted;
        }

        @Override
        public void run() {
            logger.info("IHC resource value listener started");

            // as long as no interrupt is requested, continue running
            while (!interrupted) {
                waitResourceNotifications();
            }
            logger.info("IHC Listener stopped");
        }

        private void waitResourceNotifications() {
            try {
                logger.trace("Wait new resource value notifications from controller");
                List<? extends WSResourceValue> resourceValueList = waitResourceValueNotifications(10);

                logger.debug("{} new notifications received from controller", resourceValueList.size());

                Ihc2StatusUpdateEvent event = new Ihc2StatusUpdateEvent(this);

                for (int i = 0; i < resourceValueList.size(); i++) {
                    if (interrupted) {
                        return;
                    }
                    try {
                        Iterator<ListenerResourcePair> iterator = eventListeners.iterator();
                        WSResourceValue rv = resourceValueList.get(i);
                        logger.debug("resourceUpdate() {}({})", rv.getResourceID(),
                                "0x" + Integer.toHexString(rv.getResourceID()));
                        while (iterator.hasNext()) {
                            ListenerResourcePair lrp = iterator.next();
                            if (lrp.resourceId == rv.getResourceID()) {
                                lrp.listener.resourceValueUpdateReceived(event, rv);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Event listener invoking error", e);
                    }
                }
            } catch (SocketTimeoutException e) {
                logger.debug("Notifications timeout - no new notifications");
            } catch (Ihc2Execption e) {
                logger.error("New notifications wait failed...", e);
                sendErrorEvent(e);
                mysleep(1000L);
            }
        }

        private void mysleep(long milli) {
            try {
                sleep(milli);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
    }

    /**
     * The IhcReader runs as a separate thread.
     *
     * Thread listen controller state change notifications from IHC / ELKO LS
     * controller and .
     *
     */
    private class IhcControllerStateListener extends Thread {

        private boolean interrupted = false;

        public void setInterrupted(boolean interrupted) {
            this.interrupted = interrupted;
        }

        @Override
        public void run() {
            logger.info("IHC controller state listener started");

            WSControllerState oldState = null;

            // as long as no interrupt is requested, continue running
            while (!interrupted) {
                try {
                    if (oldState == null) {
                        // oldState = getControllerState();
                        oldState = new WSControllerState();
                        oldState.setState("UNKNOWN");
                        logger.debug("Controller initial state {}", oldState.getState());
                    }
                    logger.debug("Wait new state change notification from controller");
                    WSControllerState currentState = waitStateChangeNotifications(oldState, 10);
                    logger.debug("Controller state {}", currentState.getState());
                    if (!oldState.getState().equals(currentState.getState())) {
                        logger.info("Controller state change detected ({} -> {})", oldState.getState(),
                                currentState.getState());
                        // send message to event listeners
                        try {
                            Ihc2StatusUpdateEvent event = new Ihc2StatusUpdateEvent(this);
                            Iterator<ListenerResourcePair> iterator = eventListeners.iterator();
                            while (iterator.hasNext()) {
                                iterator.next().listener.statusUpdateReceived(event, currentState);
                            }
                        } catch (Exception e) {
                            logger.error("Event listener invoking error", e);
                        }
                        oldState.setState(currentState.getState());
                    }
                } catch (Ihc2Execption e) {
                    logger.error("New controller state change notification wait failed...", e);
                    sendErrorEvent(e);
                    mysleep(1000L);
                }
            }
        }

        private void mysleep(long milli) {
            try {
                sleep(milli);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
    }

    private void sendErrorEvent(Ihc2Execption err) {
        // send error to event listeners
        logger.debug("sendErrorEvent()  {}", err.getMessage());
        try {
            Iterator<ListenerResourcePair> iterator = eventListeners.iterator();

            Ihc2ErrorEvent event = new Ihc2ErrorEvent(this);

            while (iterator.hasNext()) {
                iterator.next().listener.errorOccured(event, err);
            }
        } catch (Exception e) {
            logger.error("Event listener invoking error", e);
        }
    }

    public StateDescription getStateDescription(ChannelUID channelUID) {
        return stateDescriptionDictionary.get(channelUID);
    }

    public void setStateDecription(ChannelUID channelUID, StateDescription stateDictionary) {
        this.stateDescriptionDictionary.put(channelUID, stateDictionary);
    }

    private class ListenerResourcePair {

        public ListenerResourcePair(Ihc2EventListener listener, int resourceId) {
            this.listener = listener;
            this.resourceId = resourceId;
        }

        public Ihc2EventListener listener;
        public int resourceId;

    }

}
