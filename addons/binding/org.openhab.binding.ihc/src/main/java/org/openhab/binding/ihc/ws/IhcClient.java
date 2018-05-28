/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.ws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.openhab.binding.ihc.ws.datatypes.WSControllerState;
import org.openhab.binding.ihc.ws.datatypes.WSFile;
import org.openhab.binding.ihc.ws.datatypes.WSLoginResult;
import org.openhab.binding.ihc.ws.datatypes.WSProjectInfo;
import org.openhab.binding.ihc.ws.datatypes.WSRFDevice;
import org.openhab.binding.ihc.ws.datatypes.WSSystemInfo;
import org.openhab.binding.ihc.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.ws.resourcevalues.WSResourceValue;
import org.openhab.binding.ihc.ws.services.IhcAirlinkManagementService;
import org.openhab.binding.ihc.ws.services.IhcAuthenticationService;
import org.openhab.binding.ihc.ws.services.IhcConfigurationService;
import org.openhab.binding.ihc.ws.services.IhcControllerService;
import org.openhab.binding.ihc.ws.services.IhcResourceInteractionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IhcClient provides interface to communicate IHC / ELKO LS Controller.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcClient {

    /** Current state of the connection */
    public enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    public final static String CONTROLLER_STATE_READY = "text.ctrl.state.ready";
    public final static String CONTROLLER_STATE_INITIALIZE = "text.ctrl.state.initialize";

    private final static int NOTIFICATION_WAIT_TIMEOUT_IN_SEC = 5;

    private static final Logger logger = LoggerFactory.getLogger(IhcClient.class);

    private static ConnectionState connState = ConnectionState.DISCONNECTED;

    /** Controller services */
    private static IhcAuthenticationService authenticationService = null;
    private static IhcResourceInteractionService resourceInteractionService = null;
    private static IhcControllerService controllerService = null;
    private static IhcConfigurationService configurationService = null;
    private static IhcAirlinkManagementService airlinkManagementService = null;

    /** Thread to handle resource value notifications from the controller */
    private IhcResourceValueNotificationListener resourceValueNotificationListener = null;

    /** Thread to handle controller's state change notifications */
    private IhcControllerStateListener controllerStateListener = null;

    private String username = "";
    private String password = "";
    private String ip = "";
    private int timeout = 5000; // milliseconds

    private Map<Integer, WSResourceValue> resourceValues = new HashMap<Integer, WSResourceValue>();
    private List<IhcEventListener> eventListeners = new ArrayList<IhcEventListener>();

    public IhcClient(String ip, String username, String password) {
        this.ip = ip;
        this.username = username;
        this.password = password;
    }

    public IhcClient(String ip, String username, String password, int timeout) {
        this(ip, username, password);
        this.timeout = timeout;
    }

    public synchronized ConnectionState getConnectionState() {
        return connState;
    }

    private synchronized void setConnectionState(ConnectionState newState) {
        IhcClient.connState = newState;
    }

    public void addEventListener(IhcEventListener listener) {
        eventListeners.add(listener);
    }

    public void removeEventListener(IhcEventListener listener) {
        eventListeners.remove(listener);
    }

    /**
     * Open connection and authenticate session to IHC / ELKO LS controller.
     *
     * @return
     */
    public void closeConnection() throws IhcExecption {
        logger.debug("Closing connection");

        // interrupt

        if (resourceValueNotificationListener != null) {
            resourceValueNotificationListener.setInterrupted(true);
        }
        if (controllerStateListener != null) {
            controllerStateListener.setInterrupted(true);
        }

        // wait to stop

        if (resourceValueNotificationListener != null) {
            logger.debug("Waiting resource value notification listener to stop");
            try {
                resourceValueNotificationListener.join(NOTIFICATION_WAIT_TIMEOUT_IN_SEC * 1000);
            } catch (InterruptedException e) {
                // do nothing
            }
        }

        if (controllerStateListener != null) {
            logger.debug("Waiting controller state listener to stop");
            try {
                controllerStateListener.join(NOTIFICATION_WAIT_TIMEOUT_IN_SEC * 1000);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
        logger.debug("Connection closed");
        setConnectionState(ConnectionState.DISCONNECTED);
    }

    /**
     * Open connection and authenticate session to IHC / ELKO LS controller.
     *
     * @throws IhcExecption
     */
    public void openConnection() throws IhcExecption {

        logger.debug("Opening connection");

        setConnectionState(ConnectionState.CONNECTING);

        authenticationService = new IhcAuthenticationService(ip, timeout);
        WSLoginResult loginResult = authenticationService.authenticate(username, password, "treeview");

        if (!loginResult.isLoginWasSuccessful()) {

            // Login failed

            setConnectionState(ConnectionState.DISCONNECTED);

            if (loginResult.isLoginFailedDueToAccountInvalid()) {
                throw new IhcExecption("login failed because of invalid account");
            }

            if (loginResult.isLoginFailedDueToConnectionRestrictions()) {
                throw new IhcExecption("login failed because of connection restrictions");
            }

            if (loginResult.isLoginFailedDueToInsufficientUserRights()) {
                throw new IhcExecption("login failed because of insufficient user rights");
            }

            throw new IhcExecption("login failed because of unknown reason");
        }

        logger.debug("Connection successfully opened");

        resourceInteractionService = new IhcResourceInteractionService(ip, timeout);
        controllerService = new IhcControllerService(ip, timeout);
        configurationService = new IhcConfigurationService(ip, timeout);
        airlinkManagementService = new IhcAirlinkManagementService(ip, timeout);
        setConnectionState(ConnectionState.CONNECTED);
    }

    /**
     * Start event listener to get notifications from IHC / ELKO LS controller.
     *
     */
    public void startControllerEventListeners() {
        if (getConnectionState() == ConnectionState.CONNECTED) {
            logger.debug("Start IHC / ELKO listeners");
            resourceValueNotificationListener = new IhcResourceValueNotificationListener();
            resourceValueNotificationListener.start();
            controllerStateListener = new IhcControllerStateListener();
            controllerStateListener.start();
        }
    }

    /**
     * Query project information from the controller.
     *
     * @return project information.
     * @throws IhcExecption
     */
    public synchronized WSProjectInfo getProjectInfo() throws IhcExecption {
        return controllerService.getProjectInfo();
    }

    /**
     * Query system information from the controller.
     *
     * @return system information.
     * @throws IhcExecption
     */
    public synchronized WSSystemInfo getSystemInfo() throws IhcExecption {
        return configurationService.getSystemInfo();
    }

    /**
     * Query detected RF devices from the controller.
     *
     * @return List of RF devices.
     * @throws IhcExecption
     */
    public synchronized List<WSRFDevice> getDetectedRFDevices() throws IhcExecption {
        return airlinkManagementService.getDetectedDeviceList();
    }

    /**
     * Query controller current state.
     *
     * @return controller's current state.
     */
    public WSControllerState getControllerState() throws IhcExecption {
        return controllerService.getControllerState();
    }

    /**
     * Fetch project file from controller.
     *
     * @return project file.
     */
    public byte[] LoadProjectFileFromControllerAsByteArray() throws IhcExecption {
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
            GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(decodedBytes));
            InputStreamReader in = new InputStreamReader(gzis, "ISO-8859-1");
            return IOUtils.toByteArray(in, "ISO-8859-1");
        } catch (Exception e) {
            throw new IhcExecption(e);
        }
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
            throws IhcExecption {

        // IhcControllerService service = new IhcControllerService(ip, timeout);
        // return service.waitStateChangeNotifications(previousState, timeoutInSeconds);
        return controllerService.waitStateChangeNotifications(previousState, timeoutInSeconds);
    }

    /**
     * Enable resources runtime value notifications.
     *
     * @param resourceIdList
     *            List of resource Identifiers.
     * @return True is connection successfully opened.
     */
    public synchronized void enableRuntimeValueNotifications(Set<Integer> resourceIdList) throws IhcExecption {

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
    private List<WSResourceValue> waitResourceValueNotifications(int timeoutInSeconds)
            throws IhcExecption, SocketTimeoutException {

        // IhcResourceInteractionService service = new IhcResourceInteractionService(ip, timeout);
        // List<WSResourceValue> list = service.waitResourceValueNotifications(timeoutInSeconds);
        List<WSResourceValue> list = resourceInteractionService.waitResourceValueNotifications(timeoutInSeconds);

        for (WSResourceValue val : list) {
            resourceValues.put(val.getResourceID(), val);
        }
        return list;
    }

    /**
     * Query resource value from controller.
     *
     *
     * @param resoureId Resource Identifier.
     * @return Resource value.
     */
    public WSResourceValue resourceQuery(int resoureId) throws IhcExecption {
        return resourceInteractionService.resourceQuery(resoureId);
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
     * @param resoureId Resource Identifier.
     * @return Resource value.
     */
    public WSResourceValue getResourceValueInformation(int resourceId) throws IhcExecption {

        WSResourceValue data = resourceValues.get(resourceId);

        if (data == null) {
            // data is not available, read it from the controller
            data = resourceInteractionService.resourceQuery(resourceId);
        }
        return data;
    }

    /**
     * Update resource value to controller.
     *
     *
     * @param value Resource value.
     * @return True if value is successfully updated.
     */
    public boolean resourceUpdate(WSResourceValue value) throws IhcExecption {
        return resourceInteractionService.resourceUpdate(value);
    }

    /**
     * The IhcReader runs as a separate thread.
     *
     * Thread listen resource value notifications from IHC / ELKO LS controller.
     */
    private class IhcResourceValueNotificationListener extends Thread {
        private boolean interrupted = false;

        public void setInterrupted(boolean interrupted) {
            this.interrupted = interrupted;
            this.interrupt();
        }

        @Override
        public void run() {
            logger.debug("IHC resource value listener started");

            // as long as no interrupt is requested, continue running
            while (!interrupted) {
                waitResourceNotifications();
            }
            logger.debug("IHC resource value listener stopped");
        }

        private void waitResourceNotifications() {
            try {
                logger.trace("Wait new resource value notifications from controller");
                List<WSResourceValue> resourceValueList = waitResourceValueNotifications(
                        NOTIFICATION_WAIT_TIMEOUT_IN_SEC);
                logger.debug("{} new notifications received from controller", resourceValueList.size());
                for (WSResourceValue value : resourceValueList) {
                    sendResourceValueUpdateEvent(value);
                }
            } catch (SocketTimeoutException e) {
                logger.trace("Notifications timeout - no new notifications");
            } catch (IhcExecption e) {
                if (!interrupted) {
                    logger.warn("New notifications wait failed...", e);
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

    /**
     * The IhcReader runs as a separate thread.
     *
     * Thread listen controller state change notifications from IHC / ELKO LS
     * controller.
     *
     */
    private class IhcControllerStateListener extends Thread {
        private boolean interrupted = false;

        public void setInterrupted(boolean interrupted) {
            this.interrupted = interrupted;
            this.interrupt();
        }

        @Override
        public void run() {
            logger.debug("IHC controller state listener started");
            WSControllerState previousState = null;

            // as long as no interrupt is requested, continue running
            while (!interrupted) {
                try {
                    if (previousState == null) {
                        // initialize previous state
                        previousState = controllerService.getControllerState();
                        logger.debug("Controller initial state {}", previousState.getState());
                    }
                    logger.trace("Wait new state change notification from controller");
                    WSControllerState currentState = waitStateChangeNotifications(previousState,
                            NOTIFICATION_WAIT_TIMEOUT_IN_SEC);
                    logger.trace("Controller state {}", currentState.getState());

                    if (previousState.getState().equals(currentState.getState()) == false) {
                        logger.debug("Controller state change detected ({} -> {})", previousState.getState(),
                                currentState.getState());
                        sendControllerStateUpdateEvent(currentState);
                        previousState.setState(currentState.getState());
                    }
                } catch (IhcExecption e) {
                    if (!interrupted) {
                        logger.error("New controller state change notification wait failed...", e);
                        sendErrorEvent(e);
                        mysleep(1000L);
                    }
                }
            }
            logger.debug("IHC controller state listener stopped");
        }

        private void mysleep(long milli) {
            try {
                sleep(milli);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }
    }

    private void sendErrorEvent(IhcExecption err) {
        try {
            Iterator<IhcEventListener> iterator = eventListeners.iterator();

            while (iterator.hasNext()) {
                iterator.next().errorOccured(err);
            }

        } catch (Exception e) {
            logger.error("Event listener invoking error", e);
        }
    }

    private void sendControllerStateUpdateEvent(WSControllerState state) {
        try {
            Iterator<IhcEventListener> iterator = eventListeners.iterator();

            while (iterator.hasNext()) {
                iterator.next().statusUpdateReceived(state);
            }

        } catch (Exception e) {
            logger.error("Event listener invoking error", e);
        }
    }

    private void sendResourceValueUpdateEvent(WSResourceValue value) {
        try {
            Iterator<IhcEventListener> iterator = eventListeners.iterator();

            while (iterator.hasNext()) {
                iterator.next().resourceValueUpdateReceived(value);
            }

        } catch (Exception e) {
            logger.error("Event listener invoking error", e);
        }
    }
}
