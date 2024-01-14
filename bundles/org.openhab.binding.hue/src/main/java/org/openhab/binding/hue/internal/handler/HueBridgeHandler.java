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
package org.openhab.binding.hue.internal.handler;

import static org.openhab.binding.hue.internal.HueBindingConstants.*;
import static org.openhab.core.thing.Thing.*;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.hue.internal.api.dto.clip1.ApiVersionUtils;
import org.openhab.binding.hue.internal.api.dto.clip1.Config;
import org.openhab.binding.hue.internal.api.dto.clip1.ConfigUpdate;
import org.openhab.binding.hue.internal.api.dto.clip1.FullConfig;
import org.openhab.binding.hue.internal.api.dto.clip1.FullGroup;
import org.openhab.binding.hue.internal.api.dto.clip1.FullLight;
import org.openhab.binding.hue.internal.api.dto.clip1.FullSensor;
import org.openhab.binding.hue.internal.api.dto.clip1.Scene;
import org.openhab.binding.hue.internal.api.dto.clip1.State;
import org.openhab.binding.hue.internal.api.dto.clip1.StateUpdate;
import org.openhab.binding.hue.internal.config.HueBridgeConfig;
import org.openhab.binding.hue.internal.connection.HueBridge;
import org.openhab.binding.hue.internal.connection.HueTlsTrustManagerProvider;
import org.openhab.binding.hue.internal.discovery.HueDeviceDiscoveryService;
import org.openhab.binding.hue.internal.exceptions.ApiException;
import org.openhab.binding.hue.internal.exceptions.DeviceOffException;
import org.openhab.binding.hue.internal.exceptions.EmptyResponseException;
import org.openhab.binding.hue.internal.exceptions.EntityNotAvailableException;
import org.openhab.binding.hue.internal.exceptions.LinkButtonException;
import org.openhab.binding.hue.internal.exceptions.UnauthorizedException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ConfigStatusBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HueBridgeHandler} is the handler for a Hue Bridge and connects it to
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
 * @author Laurent Garnier - Added support for groups
 */
@NonNullByDefault
public class HueBridgeHandler extends ConfigStatusBridgeHandler implements HueClient {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE);

    private static final long BYPASS_MIN_DURATION_BEFORE_CMD = 1500L;
    private static final long SCENE_POLLING_INTERVAL = TimeUnit.SECONDS.convert(10, TimeUnit.MINUTES);

    private static final String DEVICE_TYPE = "openHAB";

    private final Logger logger = LoggerFactory.getLogger(HueBridgeHandler.class);
    private @Nullable ServiceRegistration<?> serviceRegistration;
    private final HttpClient httpClient;
    private final HueStateDescriptionProvider stateDescriptionOptionProvider;

    private final Map<String, FullLight> lastLightStates = new ConcurrentHashMap<>();
    private final Map<String, FullSensor> lastSensorStates = new ConcurrentHashMap<>();
    private final Map<String, FullGroup> lastGroupStates = new ConcurrentHashMap<>();

    private @Nullable HueDeviceDiscoveryService discoveryService;
    private final Map<String, LightStatusListener> lightStatusListeners = new ConcurrentHashMap<>();
    private final Map<String, SensorStatusListener> sensorStatusListeners = new ConcurrentHashMap<>();
    private final Map<String, GroupStatusListener> groupStatusListeners = new ConcurrentHashMap<>();

    private List<Scene> lastScenes = new CopyOnWriteArrayList<>();
    private Instant lastScenesRetrieval = Instant.MIN;

    final ReentrantLock pollingLock = new ReentrantLock();

    abstract class PollingRunnable implements Runnable {
        @Override
        public void run() {
            try {
                pollingLock.lock();
                if (!lastBridgeConnectionState) {
                    // if user is not set in configuration try to create a new user on Hue Bridge
                    if (hueBridgeConfig.userName == null) {
                        hueBridge.getFullConfig();
                    }
                    lastBridgeConnectionState = tryResumeBridgeConnection();
                }
                if (lastBridgeConnectionState) {
                    doConnectedRun();
                    if (thing.getStatus() != ThingStatus.ONLINE) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                }
            } catch (ConfigurationException e) {
                handleConfigurationFailure(e);
            } catch (UnauthorizedException | IllegalStateException e) {
                if (isReachable(hueBridge.getIPAddress())) {
                    lastBridgeConnectionState = false;
                    if (onNotAuthenticated()) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                } else if (lastBridgeConnectionState || thing.getStatus() == ThingStatus.INITIALIZING) {
                    lastBridgeConnectionState = false;
                    onConnectionLost();
                }
            } catch (EmptyResponseException e) {
                // Unexpected empty response is ignored
                logger.debug("{}", e.getMessage());
            } catch (ApiException | CommunicationException | IOException e) {
                if (hueBridge != null && lastBridgeConnectionState) {
                    logger.debug("Connection to Hue Bridge {} lost: {}", hueBridge.getIPAddress(), e.getMessage(), e);
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

        private boolean isReachable(String ipAddress) {
            try {
                // note that InetAddress.isReachable is unreliable, see
                // http://stackoverflow.com/questions/9922543/why-does-inetaddress-isreachable-return-false-when-i-can-ping-the-ip-address
                // That's why we do an HTTP access instead

                // If there is no connection, this line will fail
                hueBridge.authenticate("invalid");
            } catch (ConfigurationException | IOException e) {
                return false;
            } catch (ApiException e) {
                String message = e.getMessage();
                return message != null && //
                        !message.contains("SocketTimeout") && //
                        !message.contains("ConnectException") && //
                        !message.contains("SocketException") && //
                        !message.contains("NoRouteToHostException");
            }
            return true;
        }

        protected abstract void doConnectedRun() throws IOException, ApiException;
    }

    private final Runnable sensorPollingRunnable = new PollingRunnable() {
        @Override
        protected void doConnectedRun() throws IOException, ApiException {
            Map<String, FullSensor> lastSensorStateCopy = new HashMap<>(lastSensorStates);

            final HueDeviceDiscoveryService discovery = discoveryService;

            for (final FullSensor sensor : hueBridge.getSensors()) {
                String sensorId = sensor.getId();

                final SensorStatusListener sensorStatusListener = sensorStatusListeners.get(sensorId);
                if (sensorStatusListener == null) {
                    logger.trace("Hue sensor '{}' added.", sensorId);

                    if (discovery != null && !lastSensorStateCopy.containsKey(sensorId)) {
                        discovery.addSensorDiscovery(sensor);
                    }

                    lastSensorStates.put(sensorId, sensor);
                } else {
                    if (sensorStatusListener.onSensorStateChanged(sensor)) {
                        lastSensorStates.put(sensorId, sensor);
                    }
                }
                lastSensorStateCopy.remove(sensorId);
            }

            // Check for removed sensors
            lastSensorStateCopy.forEach((sensorId, sensor) -> {
                logger.trace("Hue sensor '{}' removed.", sensorId);
                lastSensorStates.remove(sensorId);

                final SensorStatusListener sensorStatusListener = sensorStatusListeners.get(sensorId);
                if (sensorStatusListener != null) {
                    sensorStatusListener.onSensorRemoved();
                }

                if (discovery != null) {
                    discovery.removeSensorDiscovery(sensor);
                }
            });
        }
    };

    private final Runnable lightPollingRunnable = new PollingRunnable() {
        @Override
        protected void doConnectedRun() throws IOException, ApiException {
            updateLights();
            updateGroups();
            if (lastScenesRetrieval.isBefore(Instant.now().minusSeconds(SCENE_POLLING_INTERVAL))) {
                updateScenes();
                lastScenesRetrieval = Instant.now();
            }
        }

        private void updateLights() throws IOException, ApiException {
            Map<String, FullLight> lastLightStateCopy = new HashMap<>(lastLightStates);

            List<FullLight> lights;
            if (ApiVersionUtils.supportsFullLights(hueBridge.getVersion())) {
                lights = hueBridge.getFullLights();
            } else {
                lights = hueBridge.getFullConfig().getLights();
            }

            final HueDeviceDiscoveryService discovery = discoveryService;

            for (final FullLight fullLight : lights) {
                final String lightId = fullLight.getId();

                final LightStatusListener lightStatusListener = lightStatusListeners.get(lightId);
                if (lightStatusListener == null) {
                    logger.trace("Hue light '{}' added.", lightId);

                    if (discovery != null && !lastLightStateCopy.containsKey(lightId)) {
                        discovery.addLightDiscovery(fullLight);
                    }

                    lastLightStates.put(lightId, fullLight);
                } else {
                    if (lightStatusListener.onLightStateChanged(fullLight)) {
                        lastLightStates.put(lightId, fullLight);
                    }
                }
                lastLightStateCopy.remove(lightId);
            }

            // Check for removed lights
            lastLightStateCopy.forEach((lightId, light) -> {
                logger.trace("Hue light '{}' removed.", lightId);
                lastLightStates.remove(lightId);

                final LightStatusListener lightStatusListener = lightStatusListeners.get(lightId);
                if (lightStatusListener != null) {
                    lightStatusListener.onLightRemoved();
                }

                if (discovery != null) {
                    discovery.removeLightDiscovery(light);
                }
            });
        }

        private void updateGroups() throws IOException, ApiException {
            Map<String, FullGroup> lastGroupStateCopy = new HashMap<>(lastGroupStates);

            List<FullGroup> groups = hueBridge.getGroups();

            final HueDeviceDiscoveryService discovery = discoveryService;

            for (final FullGroup fullGroup : groups) {
                State groupState = new State();
                boolean on = false;
                int sumBri = 0;
                int nbBri = 0;
                State colorRef = null;
                HSBType firstColorHsb = null;
                for (String lightId : fullGroup.getLightIds()) {
                    FullLight light = lastLightStates.get(lightId);
                    if (light != null) {
                        final State lightState = light.getState();
                        logger.trace("Group {}: light {}: on {} bri {} hue {} sat {} temp {} mode {} XY {}",
                                fullGroup.getName(), light.getName(), lightState.isOn(), lightState.getBrightness(),
                                lightState.getHue(), lightState.getSaturation(), lightState.getColorTemperature(),
                                lightState.getColorMode(), lightState.getXY());
                        if (lightState.isOn()) {
                            on = true;
                            sumBri += lightState.getBrightness();
                            nbBri++;
                            if (lightState.getColorMode() != null) {
                                HSBType lightHsb = LightStateConverter.toHSBType(lightState);
                                if (firstColorHsb == null) {
                                    // first color light
                                    firstColorHsb = lightHsb;
                                    colorRef = lightState;
                                } else if (!lightHsb.equals(firstColorHsb)) {
                                    colorRef = null;
                                }
                            }
                        }
                    }
                }
                groupState.setOn(on);
                groupState.setBri(nbBri == 0 ? 0 : sumBri / nbBri);
                if (colorRef != null) {
                    groupState.setColormode(colorRef.getColorMode());
                    groupState.setHue(colorRef.getHue());
                    groupState.setSaturation(colorRef.getSaturation());
                    groupState.setColorTemperature(colorRef.getColorTemperature());
                    groupState.setXY(colorRef.getXY());
                }
                fullGroup.setState(groupState);
                logger.trace("Group {} ({}): on {} bri {} hue {} sat {} temp {} mode {} XY {}", fullGroup.getName(),
                        fullGroup.getType(), groupState.isOn(), groupState.getBrightness(), groupState.getHue(),
                        groupState.getSaturation(), groupState.getColorTemperature(), groupState.getColorMode(),
                        groupState.getXY());

                String groupId = fullGroup.getId();

                final GroupStatusListener groupStatusListener = groupStatusListeners.get(groupId);
                if (groupStatusListener == null) {
                    logger.trace("Hue group '{}' ({}) added (nb lights {}).", groupId, fullGroup.getName(),
                            fullGroup.getLightIds().size());

                    if (discovery != null && !lastGroupStateCopy.containsKey(groupId)) {
                        discovery.addGroupDiscovery(fullGroup);
                    }

                    lastGroupStates.put(groupId, fullGroup);
                } else {
                    if (groupStatusListener.onGroupStateChanged(fullGroup)) {
                        lastGroupStates.put(groupId, fullGroup);
                    }
                }
                lastGroupStateCopy.remove(groupId);
            }

            // Check for removed groups
            lastGroupStateCopy.forEach((groupId, group) -> {
                logger.trace("Hue group '{}' removed.", groupId);
                lastGroupStates.remove(groupId);

                final GroupStatusListener groupStatusListener = groupStatusListeners.get(groupId);
                if (groupStatusListener != null) {
                    groupStatusListener.onGroupRemoved();
                }

                if (discovery != null) {
                    discovery.removeGroupDiscovery(group);
                }
            });
        }

        private void updateScenes() throws IOException, ApiException {
            lastScenes = hueBridge.getScenes();
            logger.trace("Scenes detected: {}", lastScenes);

            setBridgeSceneChannelStateOptions(lastScenes, lastGroupStates);
            notifyGroupSceneUpdate(lastScenes);
        }

        private void setBridgeSceneChannelStateOptions(List<Scene> scenes, Map<String, FullGroup> groups) {
            Map<String, String> groupNames = groups.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getName()));
            List<StateOption> stateOptions = scenes.stream().map(scene -> scene.toStateOption(groupNames)).toList();
            stateDescriptionOptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_SCENE),
                    stateOptions);
            consoleScenesList = scenes.stream().map(scene -> "Id is \"" + scene.getId() + "\" for scene \""
                    + scene.toStateOption(groupNames).getLabel() + "\"").toList();
        }
    };

    private boolean lastBridgeConnectionState = false;

    private boolean propertiesInitializedSuccessfully = false;

    private @Nullable Future<?> initJob;
    private @Nullable ScheduledFuture<?> lightPollingJob;
    private @Nullable ScheduledFuture<?> sensorPollingJob;

    private @NonNullByDefault({}) HueBridge hueBridge = null;
    private @NonNullByDefault({}) HueBridgeConfig hueBridgeConfig = null;

    private List<String> consoleScenesList = new ArrayList<>();

    public HueBridgeHandler(Bridge bridge, HttpClient httpClient,
            HueStateDescriptionProvider stateDescriptionOptionProvider) {
        super(bridge);
        this.httpClient = httpClient;
        this.stateDescriptionOptionProvider = stateDescriptionOptionProvider;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(HueDeviceDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_SCENE.equals(channelUID.getId()) && command instanceof StringType) {
            recallScene(command.toString());
        }
    }

    @Override
    public void updateLightState(LightStatusListener listener, FullLight light, StateUpdate stateUpdate,
            long fadeTime) {
        if (hueBridge != null) {
            listener.setPollBypass(BYPASS_MIN_DURATION_BEFORE_CMD);
            hueBridge.setLightState(light, stateUpdate).thenAccept(result -> {
                try {
                    hueBridge.handleErrors(result);
                    listener.setPollBypass(fadeTime);
                } catch (Exception e) {
                    listener.unsetPollBypass();
                    handleLightUpdateException(listener, light, stateUpdate, fadeTime, e);
                }
            }).exceptionally(e -> {
                listener.unsetPollBypass();
                handleLightUpdateException(listener, light, stateUpdate, fadeTime, e);
                return null;
            });
        } else {
            logger.debug("No bridge connected or selected. Cannot set light state.");
        }
    }

    @Override
    public void updateSensorState(FullSensor sensor, StateUpdate stateUpdate) {
        if (hueBridge != null) {
            hueBridge.setSensorState(sensor, stateUpdate).thenAccept(result -> {
                try {
                    hueBridge.handleErrors(result);
                } catch (Exception e) {
                    handleSensorUpdateException(sensor, e);
                }
            }).exceptionally(e -> {
                handleSensorUpdateException(sensor, e);
                return null;
            });
        } else {
            logger.debug("No bridge connected or selected. Cannot set sensor state.");
        }
    }

    @Override
    public void updateSensorConfig(FullSensor sensor, ConfigUpdate configUpdate) {
        if (hueBridge != null) {
            hueBridge.updateSensorConfig(sensor, configUpdate).thenAccept(result -> {
                try {
                    hueBridge.handleErrors(result);
                } catch (Exception e) {
                    handleSensorUpdateException(sensor, e);
                }
            }).exceptionally(e -> {
                handleSensorUpdateException(sensor, e);
                return null;
            });
        } else {
            logger.debug("No bridge connected or selected. Cannot set sensor config.");
        }
    }

    @Override
    public void updateGroupState(FullGroup group, StateUpdate stateUpdate, long fadeTime) {
        if (hueBridge != null) {
            setGroupPollBypass(group, BYPASS_MIN_DURATION_BEFORE_CMD);
            hueBridge.setGroupState(group, stateUpdate).thenAccept(result -> {
                try {
                    hueBridge.handleErrors(result);
                    setGroupPollBypass(group, fadeTime);
                } catch (Exception e) {
                    unsetGroupPollBypass(group);
                    handleGroupUpdateException(group, e);
                }
            }).exceptionally(e -> {
                unsetGroupPollBypass(group);
                handleGroupUpdateException(group, e);
                return null;
            });
        } else {
            logger.debug("No bridge connected or selected. Cannot set group state.");
        }
    }

    private void setGroupPollBypass(FullGroup group, long bypassTime) {
        group.getLightIds().forEach((lightId) -> {
            final LightStatusListener listener = lightStatusListeners.get(lightId);
            if (listener != null) {
                listener.setPollBypass(bypassTime);
            }
        });
    }

    private void unsetGroupPollBypass(FullGroup group) {
        group.getLightIds().forEach((lightId) -> {
            final LightStatusListener listener = lightStatusListeners.get(lightId);
            if (listener != null) {
                listener.unsetPollBypass();
            }
        });
    }

    private void handleLightUpdateException(LightStatusListener listener, FullLight light, StateUpdate stateUpdate,
            long fadeTime, Throwable e) {
        if (e instanceof DeviceOffException) {
            if (stateUpdate.getColorTemperature() != null && stateUpdate.getBrightness() == null) {
                // If there is only a change of the color temperature, we do not want the light
                // to be turned on (i.e. change its brightness).
                return;
            } else {
                updateLightState(listener, light, LightStateConverter.toOnOffLightState(OnOffType.ON), fadeTime);
                updateLightState(listener, light, stateUpdate, fadeTime);
            }
        } else if (e instanceof EntityNotAvailableException) {
            logger.debug("Error while accessing light: {}", e.getMessage(), e);
            final HueDeviceDiscoveryService discovery = discoveryService;
            if (discovery != null) {
                discovery.removeLightDiscovery(light);
            }
            listener.onLightGone();
        } else {
            handleThingUpdateException("light", e);
        }
    }

    private void handleSensorUpdateException(FullSensor sensor, Throwable e) {
        if (e instanceof EntityNotAvailableException) {
            logger.debug("Error while accessing sensor: {}", e.getMessage(), e);
            final HueDeviceDiscoveryService discovery = discoveryService;
            if (discovery != null) {
                discovery.removeSensorDiscovery(sensor);
            }
            final SensorStatusListener listener = sensorStatusListeners.get(sensor.getId());
            if (listener != null) {
                listener.onSensorGone();
            }
        } else {
            handleThingUpdateException("sensor", e);
        }
    }

    private void handleGroupUpdateException(FullGroup group, Throwable e) {
        if (e instanceof EntityNotAvailableException) {
            logger.debug("Error while accessing group: {}", e.getMessage(), e);
            final HueDeviceDiscoveryService discovery = discoveryService;
            if (discovery != null) {
                discovery.removeGroupDiscovery(group);
            }
            final GroupStatusListener listener = groupStatusListeners.get(group.getId());
            if (listener != null) {
                listener.onGroupGone();
            }
        } else {
            handleThingUpdateException("group", e);
        }
    }

    private void handleThingUpdateException(String thingType, Throwable e) {
        if (e instanceof IOException) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } else if (e instanceof ApiException) {
            // This should not happen - if it does, it is most likely some bug that should be reported.
            logger.warn("Error while accessing {}: {}", thingType, e.getMessage());
        } else if (e instanceof IllegalStateException) {
            logger.trace("Error while accessing {}: {}", thingType, e.getMessage());
        }
    }

    private void startLightPolling() {
        ScheduledFuture<?> job = lightPollingJob;
        if (job == null || job.isCancelled()) {
            long lightPollingInterval;
            int configPollingInterval = hueBridgeConfig.pollingInterval;
            if (configPollingInterval < 1) {
                lightPollingInterval = TimeUnit.SECONDS.toSeconds(10);
                logger.warn("Wrong configuration value for polling interval. Using default value: {}s",
                        lightPollingInterval);
            } else {
                lightPollingInterval = configPollingInterval;
            }
            // Delay the first execution to give a chance to have all light and group things registered
            lightPollingJob = scheduler.scheduleWithFixedDelay(lightPollingRunnable, 3, lightPollingInterval,
                    TimeUnit.SECONDS);
        }
    }

    private void stopLightPolling() {
        ScheduledFuture<?> job = lightPollingJob;
        if (job != null) {
            job.cancel(true);
        }
        lightPollingJob = null;
    }

    private void startSensorPolling() {
        ScheduledFuture<?> job = sensorPollingJob;
        if (job == null || job.isCancelled()) {
            int configSensorPollingInterval = hueBridgeConfig.sensorPollingInterval;
            if (configSensorPollingInterval > 0) {
                long sensorPollingInterval;
                if (configSensorPollingInterval < 50) {
                    sensorPollingInterval = TimeUnit.MILLISECONDS.toMillis(500);
                    logger.warn("Wrong configuration value for sensor polling interval. Using default value: {}ms",
                            sensorPollingInterval);
                } else {
                    sensorPollingInterval = configSensorPollingInterval;
                }
                // Delay the first execution to give a chance to have all sensor things registered
                sensorPollingJob = scheduler.scheduleWithFixedDelay(sensorPollingRunnable, 4000, sensorPollingInterval,
                        TimeUnit.MILLISECONDS);
            }
        }
    }

    private void stopSensorPolling() {
        ScheduledFuture<?> job = sensorPollingJob;
        if (job != null) {
            job.cancel(true);
        }
        sensorPollingJob = null;
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Hue Bridge handler ...");
        Future<?> job = initJob;
        if (job != null) {
            job.cancel(true);
        }
        stopLightPolling();
        stopSensorPolling();
        if (hueBridge != null) {
            hueBridge = null;
        }
        ServiceRegistration<?> localServiceRegistration = serviceRegistration;
        if (localServiceRegistration != null) {
            // remove trustmanager service
            localServiceRegistration.unregister();
            serviceRegistration = null;
        }
        propertiesInitializedSuccessfully = false;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Hue Bridge handler ...");
        hueBridgeConfig = getConfigAs(HueBridgeConfig.class);

        String ip = hueBridgeConfig.ipAddress;
        if (ip == null || ip.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-no-ip-address");
        } else {
            if (hueBridge == null) {
                hueBridge = new HueBridge(httpClient, ip, hueBridgeConfig.getPort(), hueBridgeConfig.protocol,
                        scheduler);

                updateStatus(ThingStatus.UNKNOWN);

                if (HueBridgeConfig.HTTPS.equals(hueBridgeConfig.protocol)) {
                    scheduler.submit(() -> {
                        // register trustmanager service
                        HueTlsTrustManagerProvider tlsTrustManagerProvider = new HueTlsTrustManagerProvider(
                                ip + ":" + hueBridgeConfig.getPort(), hueBridgeConfig.useSelfSignedCertificate);

                        // Check before registering that the PEM certificate can be downloaded
                        if (tlsTrustManagerProvider.getPEMTrustManager() == null) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                    "@text/offline.conf-error-https-connection");
                            return;
                        }

                        serviceRegistration = FrameworkUtil.getBundle(getClass()).getBundleContext().registerService(
                                TlsTrustManagerProvider.class.getName(), tlsTrustManagerProvider, null);

                        onUpdate();
                    });
                } else {
                    onUpdate();
                }
            } else {
                onUpdate();
            }
        }
    }

    public @Nullable String getUserName() {
        return hueBridgeConfig == null ? null : hueBridgeConfig.userName;
    }

    private synchronized void onUpdate() {
        startLightPolling();
        startSensorPolling();
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
        logger.debug("Bridge connection resumed.");

        if (!propertiesInitializedSuccessfully) {
            FullConfig fullConfig = hueBridge.getFullConfig();
            Config config = fullConfig.getConfig();
            if (config != null) {
                Map<String, String> properties = editProperties();
                String serialNumber = config.getBridgeId().substring(0, 6) + config.getBridgeId().substring(10);
                serialNumber = serialNumber.toLowerCase();
                properties.put(PROPERTY_SERIAL_NUMBER, serialNumber);
                properties.put(PROPERTY_MODEL_ID, config.getModelId());
                properties.put(PROPERTY_MAC_ADDRESS, config.getMACAddress());
                properties.put(PROPERTY_FIRMWARE_VERSION, config.getSoftwareVersion());
                updateProperties(properties);
                propertiesInitializedSuccessfully = true;
            }
        }
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
        if (hueBridgeConfig.userName == null) {
            logger.warn(
                    "User name for Hue Bridge authentication not available in configuration. Setting ThingStatus to OFFLINE.");
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
     * @return returns {@code true} if re-authentication was successful, {@code false} otherwise
     */
    public boolean onNotAuthenticated() {
        if (hueBridge == null) {
            return false;
        }
        String userName = hueBridgeConfig.userName;
        if (userName == null) {
            createUser();
        } else {
            try {
                hueBridge.authenticate(userName);
                return true;
            } catch (ConfigurationException e) {
                handleConfigurationFailure(e);
            } catch (Exception e) {
                logger.trace("", e);
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
        logger.info("Creating new user on Hue Bridge {} - please press the pairing button on the bridge.",
                hueBridgeConfig.ipAddress);
        String userName = hueBridge.link(DEVICE_TYPE);
        logger.info("User has been successfully added to Hue Bridge.");
        return userName;
    }

    private void updateBridgeThingConfiguration(String userName) {
        Configuration config = editConfiguration();
        config.put(USER_NAME, userName);
        try {
            updateConfiguration(config);
            logger.debug("Updated configuration parameter '{}'", USER_NAME);
            hueBridgeConfig = getConfigAs(HueBridgeConfig.class);
        } catch (IllegalStateException e) {
            logger.trace("Configuration update failed.", e);
            logger.warn("Unable to update configuration of Hue Bridge.");
            logger.warn("Please configure the user name manually.");
        }
    }

    private void handleConfigurationFailure(ConfigurationException ex) {
        logger.warn(
                "Invalid certificate for secured connection. You might want to enable the \"Use Self-Signed Certificate\" configuration.");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.getRawMessage());
    }

    private void handleAuthenticationFailure(Exception ex, String userName) {
        logger.warn("User is not authenticated on Hue Bridge {}", hueBridgeConfig.ipAddress);
        logger.warn("Please configure a valid user or remove user from configuration to generate a new one.");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/offline.conf-error-invalid-username");
    }

    private void handleLinkButtonNotPressed(LinkButtonException ex) {
        logger.debug("Failed creating new user on Hue Bridge: {}", ex.getMessage());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/offline.conf-error-press-pairing-button");
    }

    private void handleExceptionWhileCreatingUser(Exception ex) {
        logger.warn("Failed creating new user on Hue Bridge", ex);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/offline.conf-error-creation-username");
    }

    @Override
    public boolean registerDiscoveryListener(HueDeviceDiscoveryService listener) {
        if (discoveryService == null) {
            discoveryService = listener;
            getFullLights().forEach(listener::addLightDiscovery);
            getFullSensors().forEach(listener::addSensorDiscovery);
            getFullGroups().forEach(listener::addGroupDiscovery);
            return true;
        }

        return false;
    }

    @Override
    public boolean unregisterDiscoveryListener() {
        if (discoveryService != null) {
            discoveryService = null;
            return true;
        }

        return false;
    }

    @Override
    public boolean registerLightStatusListener(LightStatusListener lightStatusListener) {
        final String lightId = lightStatusListener.getLightId();
        if (!lightStatusListeners.containsKey(lightId)) {
            lightStatusListeners.put(lightId, lightStatusListener);
            final FullLight lastLightState = lastLightStates.get(lightId);
            if (lastLightState != null) {
                lightStatusListener.onLightAdded(lastLightState);
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean unregisterLightStatusListener(LightStatusListener lightStatusListener) {
        return lightStatusListeners.remove(lightStatusListener.getLightId()) != null;
    }

    @Override
    public boolean registerSensorStatusListener(SensorStatusListener sensorStatusListener) {
        final String sensorId = sensorStatusListener.getSensorId();
        if (!sensorStatusListeners.containsKey(sensorId)) {
            sensorStatusListeners.put(sensorId, sensorStatusListener);
            final FullSensor lastSensorState = lastSensorStates.get(sensorId);
            if (lastSensorState != null) {
                sensorStatusListener.onSensorAdded(lastSensorState);
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean unregisterSensorStatusListener(SensorStatusListener sensorStatusListener) {
        return sensorStatusListeners.remove(sensorStatusListener.getSensorId()) != null;
    }

    @Override
    public boolean registerGroupStatusListener(GroupStatusListener groupStatusListener) {
        final String groupId = groupStatusListener.getGroupId();
        if (!groupStatusListeners.containsKey(groupId)) {
            groupStatusListeners.put(groupId, groupStatusListener);
            final FullGroup lastGroupState = lastGroupStates.get(groupId);
            if (lastGroupState != null) {
                groupStatusListener.onGroupAdded(lastGroupState);
                if (!lastScenes.isEmpty()) {
                    groupStatusListener.onScenesUpdated(lastScenes);
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean unregisterGroupStatusListener(GroupStatusListener groupStatusListener) {
        return groupStatusListeners.remove(groupStatusListener.getGroupId()) != null;
    }

    /**
     * Recall scene to all lights that belong to the scene.
     *
     * @param id the ID of the scene to activate
     */
    @Override
    public void recallScene(String id) {
        if (hueBridge != null) {
            hueBridge.recallScene(id).thenAccept(result -> {
                try {
                    hueBridge.handleErrors(result);
                } catch (Exception e) {
                    logger.debug("Error while recalling scene: {}", e.getMessage());
                }
            }).exceptionally(e -> {
                logger.debug("Error while recalling scene: {}", e.getMessage());
                return null;
            });
        } else {
            logger.debug("No bridge connected or selected. Cannot activate scene.");
        }
    }

    @Override
    public @Nullable FullLight getLightById(String lightId) {
        return lastLightStates.get(lightId);
    }

    @Override
    public @Nullable FullSensor getSensorById(String sensorId) {
        return lastSensorStates.get(sensorId);
    }

    @Override
    public @Nullable FullGroup getGroupById(String groupId) {
        return lastGroupStates.get(groupId);
    }

    public List<FullLight> getFullLights() {
        List<FullLight> ret = withReAuthentication("search for new lights", () -> {
            return hueBridge.getFullLights();
        });
        return ret != null ? ret : List.of();
    }

    public List<FullSensor> getFullSensors() {
        List<FullSensor> ret = withReAuthentication("search for new sensors", () -> {
            return hueBridge.getSensors();
        });
        return ret != null ? ret : List.of();
    }

    public List<FullGroup> getFullGroups() {
        List<FullGroup> ret = withReAuthentication("search for new groups", () -> {
            return hueBridge.getGroups();
        });
        return ret != null ? ret : List.of();
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

    private @Nullable <T> T withReAuthentication(String taskDescription, Callable<T> runnable) {
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
                logger.debug("Bridge cannot {}.", taskDescription, e);
            }
        }
        return null;
    }

    private void notifyGroupSceneUpdate(List<Scene> scenes) {
        groupStatusListeners.forEach((groupId, listener) -> listener.onScenesUpdated(scenes));
    }

    public List<String> listScenesForConsole() {
        return consoleScenesList;
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        // The bridge IP address to be used for checks
        // Check whether an IP address is provided
        hueBridgeConfig = getConfigAs(HueBridgeConfig.class);

        String ip = hueBridgeConfig.ipAddress;
        if (ip == null || ip.isEmpty()) {
            return List.of(ConfigStatusMessage.Builder.error(HOST).withMessageKeySuffix(IP_ADDRESS_MISSING)
                    .withArguments(HOST).build());
        } else {
            return List.of();
        }
    }
}
