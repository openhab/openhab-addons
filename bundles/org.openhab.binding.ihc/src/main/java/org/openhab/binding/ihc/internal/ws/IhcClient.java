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
package org.openhab.binding.ihc.internal.ws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.openhab.binding.ihc.internal.ws.datatypes.WSControllerState;
import org.openhab.binding.ihc.internal.ws.datatypes.WSFile;
import org.openhab.binding.ihc.internal.ws.datatypes.WSLoginResult;
import org.openhab.binding.ihc.internal.ws.datatypes.WSProjectInfo;
import org.openhab.binding.ihc.internal.ws.datatypes.WSRFDevice;
import org.openhab.binding.ihc.internal.ws.datatypes.WSSystemInfo;
import org.openhab.binding.ihc.internal.ws.datatypes.WSTimeManagerSettings;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcFatalExecption;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcTlsExecption;
import org.openhab.binding.ihc.internal.ws.http.IhcConnectionPool;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSResourceValue;
import org.openhab.binding.ihc.internal.ws.services.IhcAirlinkManagementService;
import org.openhab.binding.ihc.internal.ws.services.IhcAuthenticationService;
import org.openhab.binding.ihc.internal.ws.services.IhcConfigurationService;
import org.openhab.binding.ihc.internal.ws.services.IhcControllerService;
import org.openhab.binding.ihc.internal.ws.services.IhcResourceInteractionService;
import org.openhab.binding.ihc.internal.ws.services.IhcTimeService;
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

    public static final String TLS_VER_AUTO = "AUTO";
    public static final String TLS_VER_V1 = "TLSv1";
    public static final String TLS_VER_V1_2 = "TLSv1.2";

    public static final String CONTROLLER_STATE_READY = "text.ctrl.state.ready";
    public static final String CONTROLLER_STATE_INITIALIZE = "text.ctrl.state.initialize";

    private static final int NOTIFICATION_WAIT_TIMEOUT_IN_SEC = 5;

    private final Logger logger = LoggerFactory.getLogger(IhcClient.class);

    private ConnectionState connState = ConnectionState.DISCONNECTED;

    private IhcConnectionPool ihcConnectionPool;

    /** Controller services */
    private IhcAuthenticationService authenticationService;
    private IhcResourceInteractionService resourceInteractionService;
    private IhcControllerService controllerService;
    private IhcConfigurationService configurationService;
    private IhcAirlinkManagementService airlinkManagementService;
    private IhcTimeService timeService;

    /** Thread to handle resource value notifications from the controller */
    private IhcResourceValueNotificationListener resourceValueNotificationListener;

    /** Thread to handle controller's state change notifications */
    private IhcControllerStateListener controllerStateListener;

    private String username;
    private String password;
    private String host;
    private String tlsVersion;

    /** Timeout in milliseconds */
    private int timeout;

    private Map<Integer, WSResourceValue> resourceValues = new HashMap<>();
    private List<IhcEventListener> eventListeners = new ArrayList<>();

    public IhcClient(String host, String username, String password) {
        this(host, username, password, 5000, TLS_VER_V1);
    }

    public IhcClient(String host, String username, String password, int timeout, String tlsVersion) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.timeout = timeout;
        this.tlsVersion = tlsVersion;
    }

    public synchronized ConnectionState getConnectionState() {
        return connState;
    }

    private synchronized void setConnectionState(ConnectionState newState) {
        connState = newState;
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
     * @throws IhcExecption
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
        if (TLS_VER_AUTO.equalsIgnoreCase(tlsVersion)) {
            try {
                openConnection(TLS_VER_V1);
            } catch (IhcTlsExecption e) {
                logger.debug("Connection failed with TLS {}, trying with TLS {}", TLS_VER_V1, TLS_VER_V1_2);
                openConnection(TLS_VER_V1_2);
            }
        } else {
            openConnection(tlsVersion);
        }
    }

    private void openConnection(String tlsVersion) throws IhcExecption {
        logger.debug("Opening connection with TLS version {}", tlsVersion);

        setConnectionState(ConnectionState.CONNECTING);
        ihcConnectionPool = new IhcConnectionPool(tlsVersion);
        authenticationService = new IhcAuthenticationService(host, timeout, ihcConnectionPool);
        WSLoginResult loginResult = authenticationService.authenticate(username, password, "treeview");

        if (!loginResult.isLoginWasSuccessful()) {
            // Login failed

            setConnectionState(ConnectionState.DISCONNECTED);

            if (loginResult.isLoginFailedDueToAccountInvalid()) {
                throw new IhcFatalExecption("login failed because of invalid account");
            }

            if (loginResult.isLoginFailedDueToConnectionRestrictions()) {
                throw new IhcFatalExecption("login failed because of connection restrictions");
            }

            if (loginResult.isLoginFailedDueToInsufficientUserRights()) {
                throw new IhcFatalExecption("login failed because of insufficient user rights");
            }

            throw new IhcFatalExecption("login failed because of unknown reason");
        }

        logger.debug("Connection successfully opened");

        resourceInteractionService = new IhcResourceInteractionService(host, timeout, ihcConnectionPool);
        controllerService = new IhcControllerService(host, timeout, ihcConnectionPool);
        configurationService = new IhcConfigurationService(host, timeout, ihcConnectionPool);
        airlinkManagementService = new IhcAirlinkManagementService(host, timeout, ihcConnectionPool);
        timeService = new IhcTimeService(host, timeout, ihcConnectionPool);
        setConnectionState(ConnectionState.CONNECTED);
    }

    /**
     * Start event listener to get notifications from IHC / ELKO LS controller.
     *
     * @throws IhcExecption
     *
     */
    public void startControllerEventListeners() throws IhcExecption {
        if (getConnectionState() == ConnectionState.CONNECTED) {
            logger.debug("Start IHC / ELKO listeners");
            resourceValueNotificationListener = new IhcResourceValueNotificationListener();
            resourceValueNotificationListener.start();
            controllerStateListener = new IhcControllerStateListener();
            controllerStateListener.start();
        } else {
            throw new IhcExecption("Connection to controller not open");
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
     * Query time settings from the controller.
     *
     * @return time settings.
     * @throws IhcExecption
     */
    public synchronized WSTimeManagerSettings getTimeSettings() throws IhcExecption {
        return timeService.getTimeSettings();
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
     * Query project number of segments.
     *
     * @return number of segments.
     */
    public int getProjectNumberOfSegments() throws IhcExecption {
        return controllerService.getProjectNumberOfSegments();
    }

    /**
     * Query project segmentation size.
     *
     * @return segmentation size in bytes.
     */
    public int getProjectSegmentationSize() throws IhcExecption {
        return controllerService.getProjectSegmentationSize();
    }

    /**
     * Query project segments data.
     *
     * @return segments data.
     */
    public WSFile getProjectSegment(int index, int major, int minor) throws IhcExecption {
        return controllerService.getProjectSegment(index, major, minor);
    }

    /**
     * Fetch project file from controller.
     *
     * @return project file.
     */
    public byte[] getProjectFileFromController() throws IhcExecption {
        try {
            WSProjectInfo projectInfo = getProjectInfo();
            int numberOfSegments = getProjectNumberOfSegments();
            int segmentationSize = getProjectSegmentationSize();

            logger.debug("Number of segments: {}", numberOfSegments);
            logger.debug("Segmentation size: {}", segmentationSize);

            try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
                for (int i = 0; i < numberOfSegments; i++) {
                    logger.debug("Downloading segment {}", i);

                    WSFile data = getProjectSegment(i, projectInfo.getProjectMajorRevision(),
                            projectInfo.getProjectMinorRevision());
                    byteStream.write(data.getData());
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("File size before base64 encoding: {} bytes", byteStream.size());
                }
                byte[] decodedBytes = Base64.getDecoder().decode(byteStream.toString());
                logger.debug("File size after base64 encoding: {} bytes", decodedBytes.length);
                try (GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(decodedBytes));
                        InputStreamReader reader = new InputStreamReader(gzis, StandardCharsets.ISO_8859_1);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        OutputStreamWriter writer = new OutputStreamWriter(baos)) {
                    reader.transferTo(writer);
                    writer.flush();
                    return baos.toByteArray();
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new IhcExecption(e);
        }
    }

    /**
     * Wait controller state change notification.
     *
     * @param previousState Previous controller state.
     * @param timeoutInSecondscHow many seconds to wait notifications.
     * @return current controller state.
     */
    private WSControllerState waitStateChangeNotifications(WSControllerState previousState, int timeoutInSeconds)
            throws IhcExecption {
        return controllerService.waitStateChangeNotifications(previousState, timeoutInSeconds);
    }

    /**
     * Enable resources runtime value notifications.
     *
     * @param resourceIdList List of resource Identifiers.
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
     * @param timeoutInSeconds How many seconds to wait notifications.
     * @return List of received runtime value notifications.
     * @throws SocketTimeoutException
     */
    private List<WSResourceValue> waitResourceValueNotifications(int timeoutInSeconds) throws IhcExecption {
        List<WSResourceValue> list = resourceInteractionService.waitResourceValueNotifications(timeoutInSeconds);

        for (WSResourceValue val : list) {
            resourceValues.put(val.resourceID, val);
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
     * @param resourceId Resource Identifier.
     * @return Resource value.
     */
    public WSResourceValue getResourceValueInformation(int resourceId) throws IhcExecption {
        if (resourceId != 0) {
            WSResourceValue data = resourceValues.get(resourceId);
            if (data == null) {
                // data is not available, read it from the controller
                data = resourceInteractionService.resourceQuery(resourceId);
            }
            return data;
        } else {
            return null;
        }
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
        private volatile boolean interrupted = false;

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
        private volatile boolean interrupted = false;

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

                    if (!previousState.getState().equals(currentState.getState())) {
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
        eventListeners.forEach(listener -> {
            try {
                listener.errorOccured(err);
            } catch (RuntimeException e) {
                logger.debug("Event listener invoking error.", e);
            }
        });
    }

    private void sendControllerStateUpdateEvent(WSControllerState state) {
        eventListeners.forEach(listener -> {
            try {
                listener.statusUpdateReceived(state);
            } catch (RuntimeException e) {
                logger.debug("Event listener invoking error.", e);
            }
        });
    }

    private void sendResourceValueUpdateEvent(WSResourceValue value) {
        eventListeners.forEach(listener -> {
            try {
                listener.resourceValueUpdateReceived(value);
            } catch (RuntimeException e) {
                logger.debug("Event listener invoking error.", e);
            }
        });
    }
}
