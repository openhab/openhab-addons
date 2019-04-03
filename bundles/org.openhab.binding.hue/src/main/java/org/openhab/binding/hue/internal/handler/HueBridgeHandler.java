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
package org.openhab.binding.hue.internal.handler;

import static org.eclipse.smarthome.core.thing.Thing.*;
import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hue.internal.ApiVersionUtils;
import org.openhab.binding.hue.internal.Config;
import org.openhab.binding.hue.internal.ConfigUpdate;
import org.openhab.binding.hue.internal.FullConfig;
import org.openhab.binding.hue.internal.FullLight;
import org.openhab.binding.hue.internal.FullSensor;
import org.openhab.binding.hue.internal.HueBridge;
import org.openhab.binding.hue.internal.HueConfigStatusMessage;
import org.openhab.binding.hue.internal.State;
import org.openhab.binding.hue.internal.StateUpdate;
import org.openhab.binding.hue.internal.config.HueBridgeConfig;
import org.openhab.binding.hue.internal.exceptions.ApiException;
import org.openhab.binding.hue.internal.exceptions.DeviceOffException;
import org.openhab.binding.hue.internal.exceptions.EntityNotAvailableException;
import org.openhab.binding.hue.internal.exceptions.LinkButtonException;
import org.openhab.binding.hue.internal.exceptions.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HueBridgeHandler} is the handler for a hue bridge and connects it to
 * the framework. All {@link HueLightHandler}s use the {@link HueBridgeHandler} to execute the actual commands.
 *
 * @author Dennis Nobel - Initial contribution
 * @author Oliver Libutzki - Adjustments
 * @author Kai Kreuzer - improved state handling
 * @author Andre Fuechsel - implemented getFullLights(), startSearch()
 * @author Thomas Höfer - added thing properties
 * @author Stefan Bußweiler - Added new thing status handling
 * @author Jochen Hiller - fixed status updates, use reachable=true/false for state compare
 * @author Denis Dudnik - switched to internally integrated source of Jue library
 * @author Samuel Leisering - Added support for sensor API
 * @author Christoph Weitkamp - Added support for sensor API
 */
@NonNullByDefault
public class HueBridgeHandler extends ConfigStatusBridgeHandler implements HueClient {

    private long lightPollingInterval = TimeUnit.SECONDS.toSeconds(10);
    private long sensorPollingInterval = TimeUnit.MILLISECONDS.toMillis(500);

    final ReentrantLock pollingLock = new ReentrantLock();

    abstract class PollingRunnable implements Runnable {
        @Override
        public void run() {
            try {
                pollingLock.lock();
                if (!lastBridgeConnectionState) {
                    // if user is not set in configuration try to create a new user on Hue bridge
                    if (hueBridgeConfig.getUserName() == null) {
                        hueBridge.getFullConfig();
                    }
                    lastBridgeConnectionState = tryResumeBridgeConnection();
                }
                if (lastBridgeConnectionState) {
                    doConnectedRun();
                }
            } catch (UnauthorizedException | IllegalStateException e) {
                if (isReachable(hueBridge.getIPAddress())) {
                    lastBridgeConnectionState = false;
                    onNotAuthenticated();
                } else if (lastBridgeConnectionState || thing.getStatus() == ThingStatus.INITIALIZING) {
                    lastBridgeConnectionState = false;
                    onConnectionLost();
                }
            } catch (ApiException | IOException e) {
                if (hueBridge != null && lastBridgeConnectionState) {
                    logger.debug("Connection to Hue Bridge {} lost.", hueBridge.getIPAddress());
                    lastBridgeConnectionState = false;
                    onConnectionLost();
                }
            } catch (RuntimeException e) {
                logger.warn("An unexpected error occurred: {}", e.getMessage(), e);
                lastBridgeConnectionState = false;
                onConnectionLost();
            } finally {
                pollingLock.unlock();
            }
        }

        protected abstract void doConnectedRun() throws IOException, ApiException;

        private boolean isReachable(String ipAddress) {
            try {
                // note that InetAddress.isReachable is unreliable, see
                // http://stackoverflow.com/questions/9922543/why-does-inetaddress-isreachable-return-false-when-i-can-ping-the-ip-address
                // That's why we do an HTTP access instead

                // If there is no connection, this line will fail
                hueBridge.authenticate("invalid");
            } catch (IOException e) {
                return false;
            } catch (ApiException e) {
                if (e.getMessage().contains("SocketTimeout") || e.getMessage().contains("ConnectException")
                        || e.getMessage().contains("SocketException")
                        || e.getMessage().contains("NoRouteToHostException")) {
                    return false;
                } else {
                    // this seems to be only an authentication issue
                    return true;
                }
            }
            return true;
        }
    }

    private static final String STATE_ADDED = "added";
    private static final String STATE_GONE = "gone";
    private static final String STATE_CHANGED = "changed";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_BRIDGE);

    private static final String DEVICE_TYPE = "EclipseSmartHome";

    private final Logger logger = LoggerFactory.getLogger(HueBridgeHandler.class);

    private final Map<String, FullLight> lastLightStates = new ConcurrentHashMap<>();
    private final Map<String, FullSensor> lastSensorStates = new ConcurrentHashMap<>();

    private boolean lastBridgeConnectionState = false;

    private boolean propertiesInitializedSuccessfully = false;

    private final List<LightStatusListener> lightStatusListeners = new CopyOnWriteArrayList<>();
    private final List<SensorStatusListener> sensorStatusListeners = new CopyOnWriteArrayList<>();

    private @Nullable ScheduledFuture<?> lightPollingJob;
    private @Nullable ScheduledFuture<?> sensorPollingJob;

    private @NonNullByDefault({}) HueBridge hueBridge = null;
    private @NonNullByDefault({}) HueBridgeConfig hueBridgeConfig = null;

    private final Runnable sensorPollingRunnable = new PollingRunnable() {
        @Override
        protected void doConnectedRun() throws IOException, ApiException {
            Map<String, FullSensor> lastSensorStateCopy = new HashMap<>(lastSensorStates);

            for (final FullSensor sensor : hueBridge.getSensors()) {
                String sensorId = sensor.getId();
                if (lastSensorStateCopy.containsKey(sensorId)) {
                    final FullSensor lastFullSensor = lastSensorStateCopy.remove(sensorId);
                    final Map<String, Object> lastFullSensorState = lastFullSensor.getState();
                    lastSensorStates.put(sensorId, sensor);
                    if (!lastFullSensorState.equals(sensor.getState())) {
                        logger.debug("Status update for Hue sensor '{}' detected: {}", sensorId, sensor.getState());
                        notifySensorStatusListeners(sensor, STATE_CHANGED);
                    }
                } else {
                    lastSensorStates.put(sensorId, sensor);
                    logger.debug("Hue sensor '{}' added.", sensorId);
                    notifySensorStatusListeners(sensor, STATE_ADDED);

                }
            }

            // Check for removed sensors
            for (Entry<String, FullSensor> fullSensorEntry : lastSensorStateCopy.entrySet()) {
                lastSensorStates.remove(fullSensorEntry.getKey());
                logger.debug("Hue sensor '{}' removed.", fullSensorEntry.getKey());
                for (SensorStatusListener sensorStatusListener : sensorStatusListeners) {
                    try {
                        sensorStatusListener.onSensorRemoved(hueBridge, fullSensorEntry.getValue());
                    } catch (Exception e) {
                        logger.error("An exception occurred while calling the Sensor Listeners", e);
                    }
                }
            }
        }
    };

    private final Runnable lightPollingRunnable = new PollingRunnable() {
        @Override
        protected void doConnectedRun() throws IOException, ApiException {
            Map<String, FullLight> lastLightStateCopy = new HashMap<>(lastLightStates);

            List<FullLight> lights;
            if (ApiVersionUtils.supportsFullLights(hueBridge.getVersion())) {
                lights = hueBridge.getFullLights();
            } else {
                lights = hueBridge.getFullConfig().getLights();
            }

            for (final FullLight fullLight : lights) {
                final String lightId = fullLight.getId();
                if (lastLightStateCopy.containsKey(lightId)) {
                    final FullLight lastFullLight = lastLightStateCopy.remove(lightId);
                    final State lastFullLightState = lastFullLight.getState();
                    lastLightStates.put(lightId, fullLight);
                    if (!isEqual(lastFullLightState, fullLight.getState())) {
                        logger.debug("Status update for Hue light '{}' detected.", lightId);
                        notifyLightStatusListeners(fullLight, STATE_CHANGED);
                    }
                } else {
                    lastLightStates.put(lightId, fullLight);
                    logger.debug("Hue light '{}' added.", lightId);
                    notifyLightStatusListeners(fullLight, STATE_ADDED);
                }
            }

            // Check for removed lights
            for (Entry<String, FullLight> fullLightEntry : lastLightStateCopy.entrySet()) {
                lastLightStates.remove(fullLightEntry.getKey());
                logger.debug("Hue light '{}' removed.", fullLightEntry.getKey());
                for (LightStatusListener lightStatusListener : lightStatusListeners) {
                    try {
                        lightStatusListener.onLightRemoved(hueBridge, fullLightEntry.getValue());
                    } catch (Exception e) {
                        logger.error("An exception occurred while calling the BridgeHeartbeatListener", e);
                    }
                }
            }
        }
    };

    public HueBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // not needed
    }

    @Override
    public void updateLightState(FullLight light, StateUpdate stateUpdate) {
        if (hueBridge != null) {
            hueBridge.setLightState(light, stateUpdate).thenAccept(result -> {
                try {
                    hueBridge.handleErrors(result);
                } catch (Exception e) {
                    handleStateUpdateException(light, stateUpdate, e);
                }
            }).exceptionally(e -> {
                handleStateUpdateException(light, stateUpdate, e);
                return null;
            });
        } else {
            logger.warn("No bridge connected or selected. Cannot set light state.");
        }
    }

    @Override
    public void updateSensorConfig(FullSensor sensor, ConfigUpdate configUpdate) {
        if (hueBridge != null) {
            hueBridge.updateSensorConfig(sensor, configUpdate).thenAccept(result -> {
                try {
                    hueBridge.handleErrors(result);
                } catch (Exception e) {
                    handleConfigUpdateException(sensor, configUpdate, e);
                }
            }).exceptionally(e -> {
                handleConfigUpdateException(sensor, configUpdate, e);
                return null;
            });
        } else {
            logger.warn("No bridge connected or selected. Cannot set sensor config.");
        }
    }

    private void handleStateUpdateException(FullLight light, StateUpdate stateUpdate, Throwable e) {
        if (e instanceof DeviceOffException) {
            if (stateUpdate.getColorTemperature() != null && stateUpdate.getBrightness() == null) {
                // If there is only a change of the color temperature, we do not want the light
                // to be turned on (i.e. change its brightness).
                return;
            } else {
                updateLightState(light, LightStateConverter.toOnOffLightState(OnOffType.ON));
                updateLightState(light, stateUpdate);
            }
        } else if (e instanceof IOException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } else if (e instanceof EntityNotAvailableException) {
            logger.debug("Error while accessing light: {}", e.getMessage(), e);
            notifyLightStatusListeners(light, STATE_GONE);
        } else if (e instanceof ApiException) {
            // This should not happen - if it does, it is most likely some bug that should be reported.
            logger.warn("Error while accessing light: {}", e.getMessage(), e);
        } else if (e instanceof IllegalStateException) {
            logger.trace("Error while accessing light: {}", e.getMessage());
        }
    }

    private void handleConfigUpdateException(FullSensor sensor, ConfigUpdate configUpdate, Throwable e) {
        if (e instanceof IOException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } else if (e instanceof EntityNotAvailableException) {
            logger.debug("Error while accessing sensor: {}", e.getMessage(), e);
            notifySensorStatusListeners(sensor, STATE_GONE);
        } else if (e instanceof ApiException) {
            // This should not happen - if it does, it is most likely some bug that should be reported.
            logger.warn("Error while accessing sensor: {}", e.getMessage(), e);
        } else if (e instanceof IllegalStateException) {
            logger.trace("Error while accessing sensor: {}", e.getMessage());
        }
    }

    private void startLightPolling() {
        if (lightPollingJob == null || lightPollingJob.isCancelled()) {
            if (hueBridgeConfig.getPollingInterval() < 1) {
                logger.info("Wrong configuration value for polling interval. Using default value: {}s",
                        lightPollingInterval);
            } else {
                lightPollingInterval = hueBridgeConfig.getPollingInterval();
            }
            lightPollingJob = scheduler.scheduleWithFixedDelay(lightPollingRunnable, 1, lightPollingInterval,
                    TimeUnit.SECONDS);
        }
    }

    private void stopLightPolling() {
        if (lightPollingJob != null && !lightPollingJob.isCancelled()) {
            lightPollingJob.cancel(true);
            lightPollingJob = null;
        }
    }

    private void startSensorPolling() {
        if (sensorPollingJob == null || sensorPollingJob.isCancelled()) {
            if (hueBridgeConfig.getSensorPollingInterval() < 50) {
                logger.info("Wrong configuration value for sensor polling interval. Using default value: {}ms",
                        sensorPollingInterval);
            } else {
                sensorPollingInterval = hueBridgeConfig.getSensorPollingInterval();
            }
            sensorPollingJob = scheduler.scheduleWithFixedDelay(sensorPollingRunnable, 1, sensorPollingInterval,
                    TimeUnit.MILLISECONDS);
        }
    }

    private void stopSensorPolling() {
        if (sensorPollingJob != null && !sensorPollingJob.isCancelled()) {
            sensorPollingJob.cancel(true);
            sensorPollingJob = null;
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        stopLightPolling();
        stopSensorPolling();
        if (hueBridge != null) {
            hueBridge = null;
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing hue bridge handler.");
        hueBridgeConfig = getConfigAs(HueBridgeConfig.class);

        String ip = hueBridgeConfig.getIpAddress();
        if (ip == null || ip.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-ip-address");
        } else {
            if (hueBridge == null) {
                hueBridge = new HueBridge(ip, hueBridgeConfig.getPort(), hueBridgeConfig.getProtocol(), scheduler);
                hueBridge.setTimeout(5000);
            }
            onUpdate();
        }
    }

    private synchronized void onUpdate() {
        if (hueBridge != null) {
            // start light polling only if a light handler has been registered, otherwise stop polling
            if (lightStatusListeners.isEmpty()) {
                stopLightPolling();
            } else {
                startLightPolling();
            }
            // start sensor polling only if a sensor handler has been registered, otherwise stop polling
            if (sensorStatusListeners.isEmpty()) {
                stopSensorPolling();
            } else {
                startSensorPolling();
            }
        }
    }

    /**
     * This method is called whenever the connection to the {@link HueBridge} is lost.
     */
    public void onConnectionLost() {
        logger.debug("Bridge connection lost. Updating thing status to OFFLINE.");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.bridge-connection-lost");
    }

    /**
     * This method is called whenever the connection to the {@link HueBridge} is resumed.
     *
     * @throws ApiException if the physical device does not support this API call
     * @throws IOException if the physical device could not be reached
     */
    private void onConnectionResumed() throws IOException, ApiException {
        logger.debug("Bridge connection resumed. Updating thing status to ONLINE.");

        if (!propertiesInitializedSuccessfully) {
            FullConfig fullConfig = hueBridge.getFullConfig();
            Config config = fullConfig.getConfig();
            if (config != null) {
                Map<String, String> properties = editProperties();
                properties.put(PROPERTY_SERIAL_NUMBER, config.getMACAddress().replaceAll(":", "").toLowerCase());
                properties.put(PROPERTY_FIRMWARE_VERSION, config.getSoftwareVersion());
                updateProperties(properties);
                propertiesInitializedSuccessfully = true;
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Check USER_NAME config for null. Call onConnectionResumed() otherwise.
     *
     * @return True if USER_NAME was not null.
     * @throws ApiException if the physical device does not support this API call
     * @throws IOException if the physical device could not be reached
     */
    private boolean tryResumeBridgeConnection() throws IOException, ApiException {
        logger.debug("Connection to Hue Bridge {} established.", hueBridge.getIPAddress());
        if (hueBridgeConfig.getUserName() == null) {
            logger.warn(
                    "User name for Hue bridge authentication not available in configuration. Setting ThingStatus to OFFLINE.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-username");
            return false;
        } else {
            onConnectionResumed();
            return true;
        }
    }

    /**
     * This method is called whenever the connection to the {@link HueBridge} is available,
     * but requests are not allowed due to a missing or invalid authentication.
     * <p>
     * If there is a user name available, it attempts to re-authenticate. Otherwise new authentication credentials will
     * be requested from the bridge.
     *
     * @param bridge the hue bridge the connection is not authorized
     * @return returns {@code true} if re-authentication was successful, {@code false} otherwise
     */
    public boolean onNotAuthenticated() {
        if (hueBridge == null) {
            return false;
        }
        String userName = hueBridgeConfig.getUserName();
        if (userName == null) {
            createUser();
        } else {
            try {
                hueBridge.authenticate(userName);
                return true;
            } catch (Exception e) {
                handleAuthenticationFailure(e, userName);
            }
        }
        return false;
    }

    private void createUser() {
        try {
            String newUser = createUserOnPhysicalBridge();
            updateBridgeThingConfiguration(newUser);
        } catch (LinkButtonException ex) {
            handleLinkButtonNotPressed(ex);
        } catch (Exception ex) {
            handleExceptionWhileCreatingUser(ex);
        }
    }

    private String createUserOnPhysicalBridge() throws IOException, ApiException {
        logger.info("Creating new user on Hue bridge {} - please press the pairing button on the bridge.",
                hueBridgeConfig.getIpAddress());
        String userName = hueBridge.link(DEVICE_TYPE);
        logger.info("User '{}' has been successfully added to Hue bridge.", userName);
        return userName;
    }

    private void updateBridgeThingConfiguration(String userName) {
        Configuration config = editConfiguration();
        config.put(USER_NAME, userName);
        try {
            updateConfiguration(config);
            logger.debug("Updated configuration parameter '{}' to '{}'", USER_NAME, userName);
            hueBridgeConfig = getConfigAs(HueBridgeConfig.class);
        } catch (IllegalStateException e) {
            logger.trace("Configuration update failed.", e);
            logger.warn("Unable to update configuration of Hue bridge.");
            logger.warn("Please configure the following user name manually: {}", userName);
        }
    }

    private void handleAuthenticationFailure(Exception ex, String userName) {
        logger.warn("User {} is not authenticated on Hue bridge {}", userName, hueBridgeConfig.getIpAddress());
        logger.warn("Please configure a valid user or remove user from configuration to generate a new one.");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                "@text/offline.conf-error-invalid-username");
    }

    private void handleLinkButtonNotPressed(LinkButtonException ex) {
        logger.debug("Failed creating new user on Hue bridge: {}", ex.getMessage());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                "@text/offline.conf-error-press-pairing-button");
    }

    private void handleExceptionWhileCreatingUser(Exception ex) {
        logger.warn("Failed creating new user on Hue bridge", ex);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                "@text/offline.conf-error-creation-username");
    }

    @Override
    public boolean registerLightStatusListener(LightStatusListener lightStatusListener) {
        boolean result = lightStatusListeners.add(lightStatusListener);
        if (result && hueBridge != null) {
            // start light polling only if a light handler has been registered
            startLightPolling();
            // inform the listener initially about all lights and their states
            for (FullLight light : lastLightStates.values()) {
                lightStatusListener.onLightAdded(hueBridge, light);
            }
        }
        return result;
    }

    @Override
    public boolean unregisterLightStatusListener(LightStatusListener lightStatusListener) {
        boolean result = lightStatusListeners.remove(lightStatusListener);
        if (result) {
            // stop stop light polling
            if (lightStatusListeners.isEmpty()) {
                stopLightPolling();
            }
        }
        return result;
    }

    @Override
    public boolean registerSensorStatusListener(SensorStatusListener sensorStatusListener) {
        boolean result = sensorStatusListeners.add(sensorStatusListener);
        if (result && hueBridge != null) {
            // start sensor polling only if a sensor handler has been registered
            startSensorPolling();
            // inform the listener initially about all sensors and their states
            for (FullSensor sensor : lastSensorStates.values()) {
                sensorStatusListener.onSensorAdded(hueBridge, sensor);
            }
        }
        return result;
    }

    @Override
    public boolean unregisterSensorStatusListener(SensorStatusListener sensorStatusListener) {
        boolean result = sensorStatusListeners.remove(sensorStatusListener);
        if (result) {
            // stop sensor polling
            if (sensorStatusListeners.isEmpty()) {
                stopSensorPolling();
            }
        }
        return result;
    }

    @Override
    public @Nullable FullLight getLightById(String lightId) {
        return lastLightStates.get(lightId);
    }

    @Override
    public @Nullable FullSensor getSensorById(String sensorId) {
        return lastSensorStates.get(sensorId);
    }

    public List<FullLight> getFullLights() {
        List<FullLight> ret = withReAuthentication("search for new lights", () -> {
            return hueBridge.getFullLights();
        });
        return ret != null ? ret : Collections.emptyList();
    }

    public List<FullSensor> getFullSensors() {
        List<FullSensor> ret = withReAuthentication("search for new sensors", () -> {
            return hueBridge.getSensors();
        });
        return ret != null ? ret : Collections.emptyList();
    }

    public void startSearch() {
        withReAuthentication("start search mode", () -> {
            hueBridge.startSearch();
            return null;
        });
    }

    public void startSearch(List<String> serialNumbers) {
        withReAuthentication("start search mode", () -> {
            hueBridge.startSearch(serialNumbers);
            return null;
        });
    }

    private <T> T withReAuthentication(String taskDescription, Callable<T> runnable) {
        if (hueBridge != null) {
            try {
                try {
                    return runnable.call();
                } catch (UnauthorizedException | IllegalStateException e) {
                    lastBridgeConnectionState = false;
                    if (onNotAuthenticated()) {
                        return runnable.call();
                    }
                }
            } catch (Exception e) {
                logger.error("Bridge cannot {}.", taskDescription, e);
            }
        }
        return null;
    }

    /**
     * Iterate through lightStatusListeners and notify them about a changed or added light state.
     *
     * @param fullLight
     * @param type Can be "changed" if just a state has changed or "added" if this is a new light on the bridge.
     */
    private void notifyLightStatusListeners(final FullLight fullLight, final String type) {
        if (lightStatusListeners.isEmpty()) {
            logger.debug("No light status listeners to notify of light change for light '{}'", fullLight.getId());
            return;
        }

        for (LightStatusListener lightStatusListener : lightStatusListeners) {
            try {
                switch (type) {
                    case STATE_ADDED:
                        logger.debug("Sending lightAdded for light '{}'", fullLight.getId());
                        lightStatusListener.onLightAdded(hueBridge, fullLight);
                        break;
                    case STATE_GONE:
                        lightStatusListener.onLightGone(hueBridge, fullLight);
                        break;
                    case STATE_CHANGED:
                        logger.debug("Sending lightStateChanged for light '{}'", fullLight.getId());
                        lightStatusListener.onLightStateChanged(hueBridge, fullLight);
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Could not notify lightStatusListeners for unknown event type " + type);
                }
            } catch (Exception e) {
                logger.error("An exception occurred while calling the BridgeHeartbeatListener", e);
            }
        }
    }

    private void notifySensorStatusListeners(final FullSensor fullSensor, final String type) {
        if (sensorStatusListeners.isEmpty()) {
            logger.debug("No sensor status listeners to notify of sensor change for sensor '{}'", fullSensor.getId());
            return;
        }

        for (SensorStatusListener sensorStatusListener : sensorStatusListeners) {
            try {
                switch (type) {
                    case STATE_ADDED:
                        logger.debug("Sending sensorAdded for sensor '{}'", fullSensor.getId());
                        sensorStatusListener.onSensorAdded(hueBridge, fullSensor);
                        break;
                    case STATE_GONE:
                        sensorStatusListener.onSensorGone(hueBridge, fullSensor);
                        break;
                    case STATE_CHANGED:
                        logger.debug("Sending sensorStateChanged for sensor '{}'", fullSensor.getId());
                        sensorStatusListener.onSensorStateChanged(hueBridge, fullSensor);
                        break;
                    default:
                        throw new IllegalArgumentException(
                                "Could not notify sensorStatusListeners for unknown event type " + type);
                }
            } catch (Exception e) {
                logger.error("An exception occurred while calling the Sensor Listeners", e);
            }
        }
    }

    /**
     * Compare to states for equality.
     *
     * @param state1 Reference state
     * @param state2 State which is checked for equality.
     * @return {@code true} if the available information of both states are equal.
     */
    private boolean isEqual(State state1, State state2) {
        return state1.getAlertMode().equals(state2.getAlertMode()) && state1.isOn() == state2.isOn()
                && state1.getBrightness() == state2.getBrightness()
                && state1.getColorTemperature() == state2.getColorTemperature() && state1.getHue() == state2.getHue()
                && state1.getSaturation() == state2.getSaturation() && state1.isReachable() == state2.isReachable()
                && Objects.equals(state1.getColorMode(), state2.getColorMode())
                && Objects.equals(state1.getEffect(), state2.getEffect());
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        // The bridge IP address to be used for checks
        Collection<ConfigStatusMessage> configStatusMessages;

        // Check whether an IP address is provided
        if (hueBridgeConfig.getIpAddress() == null || hueBridgeConfig.getIpAddress().isEmpty()) {
            configStatusMessages = Collections.singletonList(ConfigStatusMessage.Builder.error(HOST)
                    .withMessageKeySuffix(HueConfigStatusMessage.IP_ADDRESS_MISSING).withArguments(HOST).build());
        } else {
            configStatusMessages = Collections.emptyList();
        }

        return configStatusMessages;
    }

    public long getSensorPollingInterval() {
        return sensorPollingInterval;
    }
}
