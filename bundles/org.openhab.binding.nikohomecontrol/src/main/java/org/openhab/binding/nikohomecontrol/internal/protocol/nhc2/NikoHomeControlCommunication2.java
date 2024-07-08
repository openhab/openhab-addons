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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc2;

import static org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.*;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcAccess;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcAction;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcAlarm;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcControllerEvent;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcMeter;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcThermostat;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcVideo;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.AccessType;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.ActionType;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.MeterType;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NhcDevice2.NhcParameter;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NhcDevice2.NhcProperty;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NhcDevice2.NhcTrait;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NhcMessage2.NhcMessageParam;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.io.transport.mqtt.MqttException;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link NikoHomeControlCommunication2} class is able to do the following tasks with Niko Home Control II
 * systems:
 * <ul>
 * <li>Start and stop MQTT connection with Niko Home Control II Connected Controller.
 * <li>Read all setup and status information from the Niko Home Control Controller.
 * <li>Execute Niko Home Control commands.
 * <li>Listen for events from Niko Home Control.
 * </ul>
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NikoHomeControlCommunication2 extends NikoHomeControlCommunication
        implements MqttMessageSubscriber, MqttConnectionObserver {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlCommunication2.class);

    private final NhcMqttConnection2 mqttConnection;

    private final List<NhcService2> services = new CopyOnWriteArrayList<>();

    private volatile String profile = "";

    private volatile @Nullable NhcSystemInfo2 nhcSystemInfo;
    private volatile @Nullable NhcTimeInfo2 nhcTimeInfo;

    private volatile boolean initStarted = false;
    private volatile @Nullable CompletableFuture<Boolean> communicationStarted;

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    /**
     * Constructor for Niko Home Control communication object, manages communication with
     * Niko Home Control II Connected Controller.
     *
     * @throws CertificateException when the SSL context for MQTT communication cannot be created
     * @throws java.net.UnknownHostException when the IP address is not provided
     *
     */
    public NikoHomeControlCommunication2(NhcControllerEvent handler, String clientId,
            ScheduledExecutorService scheduler) throws CertificateException {
        super(handler, scheduler);
        mqttConnection = new NhcMqttConnection2(clientId, this, this);
    }

    @Override
    public synchronized void startCommunication() {
        initStarted = false;
        communicationStarted = new CompletableFuture<>();

        InetAddress addr = handler.getAddr();
        if (addr == null) {
            logger.warn("IP address cannot be empty");
            stopCommunication();
            return;
        }
        String addrString = addr.getHostAddress();
        int port = handler.getPort();
        logger.debug("initializing for mqtt connection to CoCo on {}:{}", addrString, port);

        profile = handler.getProfile();

        String token = handler.getToken();
        if (token.isEmpty()) {
            logger.warn("JWT token cannot be empty");
            stopCommunication();
            return;
        }

        try {
            mqttConnection.startConnection(addrString, port, profile, token);
        } catch (MqttException e) {
            logger.debug("error in mqtt communication");
            handler.controllerOffline("@text/offline.communication-error");
            scheduleRestartCommunication();
        }
    }

    @Override
    public synchronized void resetCommunication() {
        CompletableFuture<Boolean> started = communicationStarted;
        if (started != null) {
            started.complete(false);
        }
        communicationStarted = null;
        initStarted = false;

        mqttConnection.stopConnection();
    }

    @Override
    public boolean communicationActive() {
        CompletableFuture<Boolean> started = communicationStarted;
        if (started == null) {
            return false;
        }
        try {
            // Wait until we received all devices info to confirm we are active.
            return started.get(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("exception waiting for connection start: {}", e.toString());
            return false;
        }
    }

    /**
     * After setting up the communication with the Niko Home Control Connected Controller, send all initialization
     * messages.
     *
     */
    private synchronized void initialize() {
        initStarted = true;

        NhcMessage2 message = new NhcMessage2();

        try {
            message.method = "systeminfo.publish";
            mqttConnection.connectionPublish(profile + "/system/cmd", gson.toJson(message));

            message.method = "services.list";
            mqttConnection.connectionPublish(profile + "/authentication/cmd", gson.toJson(message));

            message.method = "devices.list";
            mqttConnection.connectionPublish(profile + "/control/devices/cmd", gson.toJson(message));

            message.method = "notifications.list";
            mqttConnection.connectionPublish(profile + "/notification/cmd", gson.toJson(message));
        } catch (MqttException e) {
            initStarted = false;
            logger.debug("error in mqtt communication during initialization");
            resetCommunication();
        }
    }

    private void connectionLost(String message) {
        logger.debug("connection lost");
        resetCommunication();
        handler.controllerOffline(message);
    }

    private void systemEvt(String response) {
        Type messageType = new TypeToken<NhcMessage2>() {
        }.getType();
        List<NhcTimeInfo2> timeInfo = null;
        List<NhcSystemInfo2> systemInfo = null;
        try {
            NhcMessage2 message = gson.fromJson(response, messageType);
            List<NhcMessageParam> messageParams = (message != null) ? message.params : null;
            if (messageParams != null) {
                timeInfo = messageParams.stream().filter(p -> (p.timeInfo != null)).findFirst().get().timeInfo;
                systemInfo = messageParams.stream().filter(p -> (p.systemInfo != null)).findFirst().get().systemInfo;
            }
        } catch (JsonSyntaxException e) {
            logger.debug("unexpected json {}", response);
        } catch (NoSuchElementException ignore) {
            // Ignore if timeInfo not present in response, this should not happen in a timeInfo response
        }
        if (timeInfo != null) {
            nhcTimeInfo = timeInfo.get(0);
        }
        if (systemInfo != null) {
            nhcSystemInfo = systemInfo.get(0);
            handler.updatePropertiesEvent();
        }
    }

    private void systeminfoPublishRsp(String response) {
        Type messageType = new TypeToken<NhcMessage2>() {
        }.getType();
        List<NhcSystemInfo2> systemInfo = null;
        try {
            NhcMessage2 message = gson.fromJson(response, messageType);
            List<NhcMessageParam> messageParams = (message != null) ? message.params : null;
            if (messageParams != null) {
                systemInfo = messageParams.stream().filter(p -> (p.systemInfo != null)).findFirst().get().systemInfo;
            }
        } catch (JsonSyntaxException e) {
            logger.debug("unexpected json {}", response);
        } catch (NoSuchElementException ignore) {
            // Ignore if systemInfo not present in response, this should not happen in a systemInfo response
        }
        if (systemInfo != null) {
            nhcSystemInfo = systemInfo.get(0);
        }
    }

    private void servicesListRsp(String response) {
        Type messageType = new TypeToken<NhcMessage2>() {
        }.getType();
        List<NhcService2> serviceList = null;
        try {
            NhcMessage2 message = gson.fromJson(response, messageType);
            List<NhcMessageParam> messageParams = (message != null) ? message.params : null;
            if (messageParams != null) {
                serviceList = messageParams.stream().filter(p -> (p.services != null)).findFirst().get().services;
            }
        } catch (JsonSyntaxException e) {
            logger.debug("unexpected json {}", response);
        } catch (NoSuchElementException ignore) {
            // Ignore if services not present in response, this should not happen in a services response
        }
        services.clear();
        if (serviceList != null) {
            services.addAll(serviceList);
        }
    }

    private void devicesListRsp(String response) {
        Type messageType = new TypeToken<NhcMessage2>() {
        }.getType();
        List<NhcDevice2> deviceList = null;
        try {
            NhcMessage2 message = gson.fromJson(response, messageType);
            List<NhcMessageParam> messageParams = (message != null) ? message.params : null;
            if (messageParams != null) {
                deviceList = messageParams.stream().filter(p -> (p.devices != null)).findFirst().get().devices;
            }
        } catch (JsonSyntaxException e) {
            logger.debug("unexpected json {}", response);
        } catch (NoSuchElementException ignore) {
            // Ignore if devices not present in response, this should not happen in a devices response
        }
        if (deviceList == null) {
            return;
        }

        for (NhcDevice2 device : deviceList) {
            addDevice(device);
            updateState(device);
        }

        // Once a devices list response is received, we know the communication is fully started.
        logger.debug("Communication start complete.");
        handler.controllerOnline();
        CompletableFuture<Boolean> future = communicationStarted;
        if (future != null) {
            future.complete(true);
        }
    }

    private void devicesEvt(String response) {
        Type messageType = new TypeToken<NhcMessage2>() {
        }.getType();
        List<NhcDevice2> deviceList = null;
        String method = null;
        try {
            NhcMessage2 message = gson.fromJson(response, messageType);
            method = (message != null) ? message.method : null;
            List<NhcMessageParam> messageParams = (message != null) ? message.params : null;
            if (messageParams != null) {
                deviceList = messageParams.stream().filter(p -> (p.devices != null)).findFirst().get().devices;
            }
        } catch (JsonSyntaxException e) {
            logger.debug("unexpected json {}", response);
        } catch (NoSuchElementException ignore) {
            // Ignore if devices not present in response, this should not happen in a devices event
        }
        if (deviceList == null) {
            return;
        }

        if ("devices.removed".equals(method)) {
            deviceList.forEach(this::removeDevice);
            return;
        } else if ("devices.added".equals(method)) {
            deviceList.forEach(this::addDevice);
        }

        deviceList.forEach(this::updateState);
    }

    private void notificationEvt(String response) {
        Type messageType = new TypeToken<NhcMessage2>() {
        }.getType();
        List<NhcNotification2> notificationList = null;
        try {
            NhcMessage2 message = gson.fromJson(response, messageType);
            List<NhcMessageParam> messageParams = (message != null) ? message.params : null;
            if (messageParams != null) {
                notificationList = messageParams.stream().filter(p -> (p.notifications != null)).findFirst()
                        .get().notifications;
            }
        } catch (JsonSyntaxException e) {
            logger.debug("unexpected json {}", response);
        } catch (NoSuchElementException ignore) {
            // Ignore if notifications not present in response, this should not happen in a notifications event
        }
        logger.debug("notifications {}", notificationList);
        if (notificationList == null) {
            return;
        }

        for (NhcNotification2 notification : notificationList) {
            if ("new".equals(notification.status)) {
                String alarmText = notification.text;
                switch (notification.type) {
                    case "alarm":
                        handler.alarmEvent(alarmText);
                        break;
                    case "notification":
                        handler.noticeEvent(alarmText);
                        break;
                    default:
                        logger.debug("unexpected message type {}", notification.type);
                }
            }
        }
    }

    private void addDevice(NhcDevice2 device) {
        String location = null;
        List<NhcParameter> parameters = device.parameters;
        if (parameters != null) {
            location = parameters.stream().map(p -> p.locationName).filter(Objects::nonNull).findFirst().orElse(null);
        }

        if ("videodoorstation".equals(device.type) || "vds".equals(device.type)) {
            addVideoDevice(device);
        } else if ("accesscontrol".equals(device.model) || "bellbutton".equals(device.model)) {
            addAccessDevice(device, location);
        } else if ("alarms".equals(device.model)) {
            addAlarmDevice(device, location);
        } else if ("action".equals(device.type) || "virtual".equals(device.type)) {
            addActionDevice(device, location);
        } else if ("thermostat".equals(device.type)) {
            addThermostatDevice(device, location);
        } else if ("centralmeter".equals(device.type) || "energyhome".equals(device.type)) {
            addMeterDevice(device, location);
        } else {
            logger.debug("device type {} and model {} not supported for {}, {}", device.type, device.model, device.uuid,
                    device.name);
        }
    }

    private void addActionDevice(NhcDevice2 device, @Nullable String location) {
        ActionType actionType;
        switch (device.model) {
            case "generic":
            case "pir":
            case "simulation":
            case "comfort":
            case "alloff":
            case "overallcomfort":
            case "garagedoor":
                actionType = ActionType.TRIGGER;
                break;
            case "light":
            case "socket":
            case "switched-generic":
            case "switched-fan":
            case "flag":
                actionType = ActionType.RELAY;
                break;
            case "dimmer":
                actionType = ActionType.DIMMER;
                break;
            case "rolldownshutter":
            case "sunblind":
            case "venetianblind":
            case "gate":
                actionType = ActionType.ROLLERSHUTTER;
                break;
            default:
                actionType = ActionType.GENERIC;
                logger.debug("device type {} and model {} not recognised for {}, {}, ignoring", device.type,
                        device.model, device.uuid, device.name);
                return;
        }

        NhcAction nhcAction = actions.get(device.uuid);
        if (nhcAction != null) {
            // update name and location so discovery will see updated name and location
            nhcAction.setName(device.name);
            nhcAction.setLocation(location);
        } else {
            logger.debug("adding action device {} model {}, {}", device.uuid, device.model, device.name);
            nhcAction = new NhcAction2(device.uuid, device.name, device.type, device.technology, device.model, location,
                    actionType, this);
        }
        actions.put(device.uuid, nhcAction);
    }

    private void addThermostatDevice(NhcDevice2 device, @Nullable String location) {
        NhcThermostat nhcThermostat = thermostats.get(device.uuid);
        if (nhcThermostat != null) {
            nhcThermostat.setName(device.name);
            nhcThermostat.setLocation(location);
        } else {
            logger.debug("adding thermostat device {} model {}, {}", device.uuid, device.model, device.name);
            nhcThermostat = new NhcThermostat2(device.uuid, device.name, device.type, device.technology, device.model,
                    location, this);
        }
        thermostats.put(device.uuid, nhcThermostat);
    }

    private void addMeterDevice(NhcDevice2 device, @Nullable String location) {
        NhcMeter nhcMeter = meters.get(device.uuid);
        if (nhcMeter != null) {
            nhcMeter.setName(device.name);
            nhcMeter.setLocation(location);
        } else {
            logger.debug("adding energy meter device {} model {}, {}", device.uuid, device.model, device.name);
            nhcMeter = new NhcMeter2(device.uuid, device.name, MeterType.ENERGY_LIVE, device.type, device.technology,
                    device.model, null, location, this, scheduler);
        }
        meters.put(device.uuid, nhcMeter);
    }

    private void addAccessDevice(NhcDevice2 device, @Nullable String location) {
        AccessType accessType = AccessType.BASE;
        if ("bellbutton".equals(device.model)) {
            accessType = AccessType.BELLBUTTON;
        } else {
            List<NhcProperty> properties = device.properties;
            if (properties != null) {
                boolean hasBasicState = properties.stream().anyMatch(p -> (p.basicState != null));
                if (hasBasicState) {
                    accessType = AccessType.RINGANDCOMEIN;
                }
            }
        }

        NhcAccess2 nhcAccess = (NhcAccess2) accessDevices.get(device.uuid);
        if (nhcAccess != null) {
            nhcAccess.setName(device.name);
            nhcAccess.setLocation(location);
        } else {
            String buttonId = null;
            List<NhcParameter> parameters = device.parameters;
            if (parameters != null) {
                buttonId = parameters.stream().map(p -> p.buttonId).filter(Objects::nonNull).findFirst().orElse(null);
            }

            logger.debug("adding access device {} model {} type {}, {}", device.uuid, device.model, accessType,
                    device.name);
            nhcAccess = new NhcAccess2(device.uuid, device.name, device.type, device.technology, device.model, location,
                    accessType, buttonId, this);

            if (buttonId != null) {
                NhcAccess2 access = nhcAccess;
                String macAddress = buttonId.split("_")[0];
                videoDevices.forEach((key, videoDevice) -> {
                    if (macAddress.equals(videoDevice.getMacAddress())) {
                        int buttonIndex = access.getButtonIndex();
                        logger.debug("link access device {} to video device {} button {}", device.uuid,
                                videoDevice.getId(), buttonIndex);
                        videoDevice.setNhcAccess(buttonIndex, access);
                        access.setNhcVideo(videoDevice);
                    }
                });
            }
        }
        accessDevices.put(device.uuid, nhcAccess);
    }

    private void addVideoDevice(NhcDevice2 device) {
        NhcVideo2 nhcVideo = (NhcVideo2) videoDevices.get(device.uuid);
        if (nhcVideo != null) {
            nhcVideo.setName(device.name);
        } else {
            String macAddress = null;
            String ipAddress = null;
            String mjpegUri = null;
            String tnUri = null;
            List<NhcTrait> traits = device.traits;
            if (traits != null) {
                macAddress = traits.stream().map(t -> t.macAddress).filter(Objects::nonNull).findFirst().orElse(null);
            }
            List<NhcParameter> parameters = device.parameters;
            if (parameters != null) {
                mjpegUri = parameters.stream().map(p -> p.mjpegUri).filter(Objects::nonNull).findFirst().orElse(null);
                tnUri = parameters.stream().map(p -> p.tnUri).filter(Objects::nonNull).findFirst().orElse(null);
            }
            List<NhcProperty> properties = device.properties;
            if (properties != null) {
                ipAddress = properties.stream().map(p -> p.ipAddress).filter(Objects::nonNull).findFirst().orElse(null);
            }

            logger.debug("adding video device {} model {}, {}", device.uuid, device.model, device.name);
            nhcVideo = new NhcVideo2(device.uuid, device.name, device.type, device.technology, device.model, macAddress,
                    ipAddress, mjpegUri, tnUri, this);

            if (macAddress != null) {
                NhcVideo2 video = nhcVideo;
                String mac = macAddress;
                accessDevices.forEach((key, accessDevice) -> {
                    NhcAccess2 access = (NhcAccess2) accessDevice;
                    String buttonMac = access.getButtonId();
                    if (buttonMac != null) {
                        buttonMac = buttonMac.split("_")[0];
                        if (mac.equals(buttonMac)) {
                            int buttonIndex = access.getButtonIndex();
                            logger.debug("link access device {} to video device {} button {}", accessDevice.getId(),
                                    device.uuid, buttonIndex);
                            video.setNhcAccess(buttonIndex, access);
                            access.setNhcVideo(video);
                        }
                    }
                });
            }
        }
        videoDevices.put(device.uuid, nhcVideo);
    }

    private void addAlarmDevice(NhcDevice2 device, @Nullable String location) {
        NhcAlarm nhcAlarm = alarmDevices.get(device.uuid);
        if (nhcAlarm != null) {
            nhcAlarm.setName(device.name);
            nhcAlarm.setLocation(location);
        } else {
            logger.debug("adding alarm device {} model {}, {}", device.uuid, device.model, device.name);
            nhcAlarm = new NhcAlarm2(device.uuid, device.name, device.type, device.technology, device.model, location,
                    this);
        }
        alarmDevices.put(device.uuid, nhcAlarm);
    }

    private void removeDevice(NhcDevice2 device) {
        NhcAction action = actions.get(device.uuid);
        NhcThermostat thermostat = thermostats.get(device.uuid);
        NhcMeter meter = meters.get(device.uuid);
        NhcAccess access = accessDevices.get(device.uuid);
        NhcVideo video = videoDevices.get(device.uuid);
        NhcAlarm alarm = alarmDevices.get(device.uuid);
        if (action != null) {
            action.actionRemoved();
            actions.remove(device.uuid);
        } else if (thermostat != null) {
            thermostat.thermostatRemoved();
            thermostats.remove(device.uuid);
        } else if (meter != null) {
            meter.meterRemoved();
            meters.remove(device.uuid);
        } else if (access != null) {
            access.accessDeviceRemoved();
            accessDevices.remove(device.uuid);
        } else if (video != null) {
            video.videoDeviceRemoved();
            videoDevices.remove(device.uuid);
        } else if (alarm != null) {
            alarm.alarmDeviceRemoved();
            alarmDevices.remove(device.uuid);
        }
    }

    private void updateState(NhcDevice2 device) {
        List<NhcProperty> deviceProperties = device.properties;
        if (deviceProperties == null) {
            logger.debug("Cannot Update state for {} as no properties defined in device message", device.uuid);
            return;
        }

        NhcAction action = actions.get(device.uuid);
        NhcThermostat thermostat = thermostats.get(device.uuid);
        NhcMeter meter = meters.get(device.uuid);
        NhcAccess accessDevice = accessDevices.get(device.uuid);
        NhcVideo videoDevice = videoDevices.get(device.uuid);
        NhcAlarm alarm = alarmDevices.get(device.uuid);

        if (action != null) {
            updateActionState((NhcAction2) action, deviceProperties);
        } else if (thermostat != null) {
            updateThermostatState((NhcThermostat2) thermostat, deviceProperties);
        } else if (meter != null) {
            updateMeterState((NhcMeter2) meter, deviceProperties);
        } else if (accessDevice != null) {
            updateAccessState((NhcAccess2) accessDevice, deviceProperties);
        } else if (videoDevice != null) {
            updateVideoState((NhcVideo2) videoDevice, deviceProperties);
        } else if (alarm != null) {
            updateAlarmState((NhcAlarm2) alarm, deviceProperties);
        } else {
            logger.trace("No known device for {}", device.uuid);
        }
    }

    private void updateActionState(NhcAction2 action, List<NhcProperty> deviceProperties) {
        if (action.getType() == ActionType.ROLLERSHUTTER) {
            updateRollershutterState(action, deviceProperties);
        } else {
            updateLightState(action, deviceProperties);
        }
    }

    private void updateLightState(NhcAction2 action, List<NhcProperty> deviceProperties) {
        Optional<NhcProperty> statusProperty = deviceProperties.stream().filter(p -> (p.status != null)).findFirst();
        Optional<NhcProperty> dimmerProperty = deviceProperties.stream().filter(p -> (p.brightness != null))
                .findFirst();
        Optional<NhcProperty> basicStateProperty = deviceProperties.stream().filter(p -> (p.basicState != null))
                .findFirst();

        String booleanState = null;
        if (statusProperty.isPresent()) {
            booleanState = statusProperty.get().status;
        } else if (basicStateProperty.isPresent()) {
            booleanState = basicStateProperty.get().basicState;
        }

        if (NHCOFF.equals(booleanState) || NHCFALSE.equals(booleanState)) {
            action.setBooleanState(false);
            logger.debug("setting action {} internally to OFF", action.getId());
        }

        if (dimmerProperty.isPresent()) {
            String brightness = dimmerProperty.get().brightness;
            if (brightness != null) {
                try {
                    logger.debug("setting action {} internally to {}", action.getId(), dimmerProperty.get().brightness);
                    action.setState(Integer.parseInt(brightness));
                } catch (NumberFormatException e) {
                    logger.debug("received invalid brightness value {} for dimmer {}", brightness, action.getId());
                }
            }
        }

        if (NHCON.equals(booleanState) || NHCTRUE.equals(booleanState)) {
            logger.debug("setting action {} internally to ON", action.getId());
            action.setBooleanState(true);
        }
    }

    private void updateRollershutterState(NhcAction2 action, List<NhcProperty> deviceProperties) {
        deviceProperties.stream().map(p -> p.position).filter(Objects::nonNull).findFirst().ifPresent(position -> {
            try {
                logger.debug("setting action {} internally to {}", action.getId(), position);
                action.setState(Integer.parseInt(position));
            } catch (NumberFormatException e) {
                logger.trace("received empty or invalid rollershutter {} position info {}", action.getId(), position);
            }
        });
    }

    private void updateThermostatState(NhcThermostat2 thermostat, List<NhcProperty> deviceProperties) {
        Optional<Boolean> overruleActiveProperty = deviceProperties.stream().map(p -> p.overruleActive)
                .filter(Objects::nonNull).map(t -> Boolean.parseBoolean(t)).findFirst();
        Optional<Integer> overruleSetpointProperty = deviceProperties.stream().map(p -> p.overruleSetpoint)
                .map(s -> (!((s == null) || s.isEmpty())) ? Math.round(Float.parseFloat(s) * 10) : null)
                .filter(Objects::nonNull).findFirst();
        Optional<Integer> overruleTimeProperty = deviceProperties.stream().map(p -> p.overruleTime)
                .map(s -> (!((s == null) || s.isEmpty())) ? Math.round(Float.parseFloat(s)) : null)
                .filter(Objects::nonNull).findFirst();
        Optional<Integer> setpointTemperatureProperty = deviceProperties.stream().map(p -> p.setpointTemperature)
                .map(s -> (!((s == null) || s.isEmpty())) ? Math.round(Float.parseFloat(s) * 10) : null)
                .filter(Objects::nonNull).findFirst();
        Optional<Boolean> ecoSaveProperty = deviceProperties.stream().map(p -> p.ecoSave)
                .map(s -> s != null ? Boolean.parseBoolean(s) : null).filter(Objects::nonNull).findFirst();
        Optional<Integer> ambientTemperatureProperty = deviceProperties.stream().map(p -> p.ambientTemperature)
                .map(s -> (!((s == null) || s.isEmpty())) ? Math.round(Float.parseFloat(s) * 10) : null)
                .filter(Objects::nonNull).findFirst();
        Optional<@Nullable String> demandProperty = deviceProperties.stream().map(p -> p.demand)
                .filter(Objects::nonNull).findFirst();
        Optional<@Nullable String> operationModeProperty = deviceProperties.stream().map(p -> p.operationMode)
                .filter(Objects::nonNull).findFirst();

        String modeString = deviceProperties.stream().map(p -> p.program).filter(Objects::nonNull).findFirst()
                .orElse("");
        int mode = IntStream.range(0, THERMOSTATMODES.length).filter(i -> THERMOSTATMODES[i].equals(modeString))
                .findFirst().orElse(thermostat.getMode());

        int measured = ambientTemperatureProperty.orElse(thermostat.getMeasured());
        int setpoint = setpointTemperatureProperty.orElse(thermostat.getSetpoint());

        int overrule = 0;
        int overruletime = 0;
        if (overruleActiveProperty.orElse(true)) {
            overrule = overruleSetpointProperty.orElse(thermostat.getOverrule());
            overruletime = overruleTimeProperty.orElse(thermostat.getRemainingOverruletime());
        }

        int ecosave = thermostat.getEcosave();
        if (ecoSaveProperty.orElse(false)) {
            ecosave = 1;
        }

        int demand = thermostat.getDemand();
        String demandString = demandProperty.orElse(operationModeProperty.orElse(""));
        demandString = demandString == null ? "" : demandString;
        switch (demandString) {
            case "None":
                demand = 0;
                break;
            case "Heating":
                demand = 1;
                break;
            case "Cooling":
                demand = -1;
                break;
        }

        logger.debug(
                "setting thermostat {} with measured {}, setpoint {}, mode {}, overrule {}, overruletime {}, ecosave {}, demand {}",
                thermostat.getId(), measured, setpoint, mode, overrule, overruletime, ecosave, demand);
        thermostat.setState(measured, setpoint, mode, overrule, overruletime, ecosave, demand);
    }

    private void updateMeterState(NhcMeter2 meter, List<NhcProperty> deviceProperties) {
        try {
            Optional<Integer> electricalPower = deviceProperties.stream().map(p -> p.electricalPower)
                    .map(s -> (!((s == null) || s.isEmpty())) ? Math.round(Float.parseFloat(s)) : null)
                    .filter(Objects::nonNull).findFirst();
            Optional<Integer> powerFromGrid = deviceProperties.stream().map(p -> p.electricalPowerFromGrid)
                    .map(s -> (!((s == null) || s.isEmpty())) ? Math.round(Float.parseFloat(s)) : null)
                    .filter(Objects::nonNull).findFirst();
            Optional<Integer> powerToGrid = deviceProperties.stream().map(p -> p.electricalPowerToGrid)
                    .map(s -> (!((s == null) || s.isEmpty())) ? Math.round(Float.parseFloat(s)) : null)
                    .filter(Objects::nonNull).findFirst();
            int power = electricalPower.orElse(powerFromGrid.orElse(0) - powerToGrid.orElse(0));
            logger.trace("setting energy meter {} power to {}", meter.getId(), power);
            meter.setPower(power);
        } catch (NumberFormatException e) {
            logger.trace("wrong format in energy meter {} power reading", meter.getId());
            meter.setPower(null);
        }
    }

    private void updateAccessState(NhcAccess2 accessDevice, List<NhcProperty> deviceProperties) {
        Optional<NhcProperty> basicStateProperty = deviceProperties.stream().filter(p -> (p.basicState != null))
                .findFirst();
        Optional<NhcProperty> doorLockProperty = deviceProperties.stream().filter(p -> (p.doorlock != null))
                .findFirst();

        if (basicStateProperty.isPresent()) {
            String basicState = basicStateProperty.get().basicState;
            boolean state = false;
            if (NHCON.equals(basicState) || NHCTRUE.equals(basicState)) {
                state = true;
            }
            switch (accessDevice.getType()) {
                case RINGANDCOMEIN:
                    accessDevice.updateRingAndComeInState(state);
                    logger.debug("setting access device {} ring and come in to {}", accessDevice.getId(), state);
                    break;
                case BELLBUTTON:
                    accessDevice.updateBellState(state);
                    logger.debug("setting access device {} bell to {}", accessDevice.getId(), state);
                    break;
                default:
                    break;
            }
        }

        if (doorLockProperty.isPresent()) {
            String doorLockState = doorLockProperty.get().doorlock;
            boolean state = false;
            if (NHCCLOSED.equals(doorLockState)) {
                state = true;
            }
            logger.debug("setting access device {} doorlock to {}", accessDevice.getId(), state);
            accessDevice.updateDoorLockState(state);
        }
    }

    private void updateVideoState(NhcVideo2 videoDevice, List<NhcProperty> deviceProperties) {
        String callStatus01 = deviceProperties.stream().map(p -> p.callStatus01).filter(Objects::nonNull).findFirst()
                .orElse(null);
        String callStatus02 = deviceProperties.stream().map(p -> p.callStatus02).filter(Objects::nonNull).findFirst()
                .orElse(null);
        String callStatus03 = deviceProperties.stream().map(p -> p.callStatus03).filter(Objects::nonNull).findFirst()
                .orElse(null);
        String callStatus04 = deviceProperties.stream().map(p -> p.callStatus04).filter(Objects::nonNull).findFirst()
                .orElse(null);

        logger.debug("setting video device {} call status to {}, {}, {}, {}", videoDevice.getId(), callStatus01,
                callStatus02, callStatus03, callStatus04);
        videoDevice.updateState(callStatus01, callStatus02, callStatus03, callStatus04);
    }

    private void updateAlarmState(NhcAlarm2 alarmDevice, List<NhcProperty> deviceProperties) {
        String state = deviceProperties.stream().map(p -> p.internalState).filter(Objects::nonNull).findFirst()
                .orElse(null);
        if (state != null) {
            logger.debug("setting alarm device {} state to {}", alarmDevice.getId(), state);
            alarmDevice.setState(state);
        }
        String triggered = deviceProperties.stream().map(p -> p.alarmTriggered).filter(Objects::nonNull).findFirst()
                .orElse(null);
        if (Boolean.valueOf(triggered)) {
            logger.debug("triggering alarm device {}", alarmDevice.getId());
            alarmDevice.triggerAlarm();
        }
    }

    @Override
    public void executeAction(String actionId, String value) {
        NhcMessage2 message = new NhcMessage2();

        message.method = "devices.control";
        List<NhcMessageParam> params = new ArrayList<>();
        NhcMessageParam param = new NhcMessageParam();
        params.add(param);
        message.params = params;
        List<NhcDevice2> devices = new ArrayList<>();
        NhcDevice2 device = new NhcDevice2();
        devices.add(device);
        param.devices = devices;
        device.uuid = actionId;
        List<NhcProperty> deviceProperties = new ArrayList<>();
        NhcProperty property = new NhcProperty();
        deviceProperties.add(property);
        device.properties = deviceProperties;

        NhcAction2 action = (NhcAction2) actions.get(actionId);
        if (action == null) {
            return;
        }

        switch (action.getType()) {
            case GENERIC:
            case TRIGGER:
                property.basicState = NHCTRIGGERED;
                break;
            case RELAY:
                property.status = value;
                break;
            case DIMMER:
                if (NHCON.equals(value)) {
                    action.setBooleanState(true); // this will trigger sending the stored brightness value event out
                    property.status = value;
                } else if (NHCOFF.equals(value)) {
                    property.status = value;
                } else {
                    try {
                        action.setState(Integer.parseInt(value)); // set cached state to new brightness value to avoid
                                                                  // switching on with old brightness value before
                                                                  // updating
                                                                  // to new value
                    } catch (NumberFormatException e) {
                        logger.debug("internal error, trying to set invalid brightness value {} for dimmer {}", value,
                                action.getId());
                        return;
                    }

                    // If the light is off, turn the light on before sending the brightness value, needs to happen
                    // in 2 separate messages.
                    if (!action.booleanState()) {
                        executeAction(actionId, NHCON);
                    }
                    property.brightness = value;
                }
                break;
            case ROLLERSHUTTER:
                if (NHCSTOP.equals(value)) {
                    property.action = value;
                } else if (NHCUP.equals(value)) {
                    property.position = "100";
                } else if (NHCDOWN.equals(value)) {
                    property.position = "0";
                } else {
                    property.position = value;
                }
                break;
        }

        String topic = profile + "/control/devices/cmd";
        String gsonMessage = gson.toJson(message);
        sendDeviceMessage(topic, gsonMessage);
    }

    @Override
    public void executeThermostat(String thermostatId, String mode) {
        NhcMessage2 message = new NhcMessage2();

        message.method = "devices.control";
        List<NhcMessageParam> params = new ArrayList<>();
        NhcMessageParam param = new NhcMessageParam();
        params.add(param);
        message.params = params;
        List<NhcDevice2> devices = new ArrayList<>();
        NhcDevice2 device = new NhcDevice2();
        devices.add(device);
        param.devices = devices;
        device.uuid = thermostatId;
        List<NhcProperty> deviceProperties = new ArrayList<>();

        NhcProperty overruleActiveProp = new NhcProperty();
        deviceProperties.add(overruleActiveProp);
        overruleActiveProp.overruleActive = "False";

        NhcProperty program = new NhcProperty();
        deviceProperties.add(program);
        program.program = mode;

        device.properties = deviceProperties;

        String topic = profile + "/control/devices/cmd";
        String gsonMessage = gson.toJson(message);
        sendDeviceMessage(topic, gsonMessage);
    }

    @Override
    public void executeThermostat(String thermostatId, int overruleTemp, int overruleTime) {
        NhcMessage2 message = new NhcMessage2();

        message.method = "devices.control";
        List<NhcMessageParam> params = new ArrayList<>();
        NhcMessageParam param = new NhcMessageParam();
        params.add(param);
        message.params = params;
        List<NhcDevice2> devices = new ArrayList<>();
        NhcDevice2 device = new NhcDevice2();
        devices.add(device);
        param.devices = devices;
        device.uuid = thermostatId;
        List<NhcProperty> deviceProperties = new ArrayList<>();

        if (overruleTime > 0) {
            NhcProperty overruleActiveProp = new NhcProperty();
            overruleActiveProp.overruleActive = "True";
            deviceProperties.add(overruleActiveProp);

            NhcProperty overruleSetpointProp = new NhcProperty();
            overruleSetpointProp.overruleSetpoint = String.valueOf(overruleTemp / 10.0);
            deviceProperties.add(overruleSetpointProp);

            NhcProperty overruleTimeProp = new NhcProperty();
            overruleTimeProp.overruleTime = String.valueOf(overruleTime);
            deviceProperties.add(overruleTimeProp);
        } else {
            NhcProperty overruleActiveProp = new NhcProperty();
            overruleActiveProp.overruleActive = "False";
            deviceProperties.add(overruleActiveProp);
        }
        device.properties = deviceProperties;

        String topic = profile + "/control/devices/cmd";
        String gsonMessage = gson.toJson(message);
        sendDeviceMessage(topic, gsonMessage);
    }

    @Override
    public void executeMeter(String meterId) {
        // Nothing to do, individual meter readings not supported in NHC II at this point in time
    }

    @Override
    public void retriggerMeterLive(String meterId) {
        NhcMessage2 message = new NhcMessage2();

        message.method = "devices.control";
        List<NhcMessageParam> params = new ArrayList<>();
        NhcMessageParam param = new NhcMessageParam();
        params.add(param);
        message.params = params;
        List<NhcDevice2> devices = new ArrayList<>();
        NhcDevice2 device = new NhcDevice2();
        devices.add(device);
        param.devices = devices;
        device.uuid = meterId;
        List<NhcProperty> deviceProperties = new ArrayList<>();

        NhcProperty reportInstantUsageProp = new NhcProperty();
        deviceProperties.add(reportInstantUsageProp);
        reportInstantUsageProp.reportInstantUsage = "True";
        device.properties = deviceProperties;

        String topic = profile + "/control/devices/cmd";
        String gsonMessage = gson.toJson(message);

        sendDeviceMessage(topic, gsonMessage);
    }

    @Override
    public void executeAccessBell(String accessId) {
        executeAccess(accessId);
    }

    @Override
    public void executeAccessRingAndComeIn(String accessId, boolean ringAndComeIn) {
        NhcAccess2 accessDevice = (NhcAccess2) accessDevices.get(accessId);
        if (accessDevice == null) {
            return;
        }

        boolean current = accessDevice.getRingAndComeInState();
        if ((ringAndComeIn && !current) || (!ringAndComeIn && current)) {
            executeAccess(accessId);
        } else {
            logger.trace("Not updating ring and come in as state did not change");
        }
    }

    private void executeAccess(String accessId) {
        NhcMessage2 message = new NhcMessage2();

        message.method = "devices.control";
        List<NhcMessageParam> params = new ArrayList<>();
        NhcMessageParam param = new NhcMessageParam();
        params.add(param);
        message.params = params;
        List<NhcDevice2> devices = new ArrayList<>();
        NhcDevice2 device = new NhcDevice2();
        devices.add(device);
        param.devices = devices;
        device.uuid = accessId;
        List<NhcProperty> deviceProperties = new ArrayList<>();
        NhcProperty property = new NhcProperty();
        deviceProperties.add(property);
        device.properties = deviceProperties;

        NhcAccess2 accessDevice = (NhcAccess2) accessDevices.get(accessId);
        if (accessDevice == null) {
            return;
        }

        property.basicState = NHCTRIGGERED;

        String topic = profile + "/control/devices/cmd";
        String gsonMessage = gson.toJson(message);
        sendDeviceMessage(topic, gsonMessage);
    }

    @Override
    public void executeVideoBell(String accessId, int buttonIndex) {
        NhcMessage2 message = new NhcMessage2();

        message.method = "devices.control";
        List<NhcMessageParam> params = new ArrayList<>();
        NhcMessageParam param = new NhcMessageParam();
        params.add(param);
        message.params = params;
        List<NhcDevice2> devices = new ArrayList<>();
        NhcDevice2 device = new NhcDevice2();
        devices.add(device);
        param.devices = devices;
        device.uuid = accessId;
        List<NhcProperty> deviceProperties = new ArrayList<>();
        NhcProperty property = new NhcProperty();
        deviceProperties.add(property);
        device.properties = deviceProperties;

        NhcVideo videoDevice = videoDevices.get(accessId);
        if (videoDevice == null) {
            return;
        }

        switch (buttonIndex) {
            case 1:
                property.callStatus01 = NHCRINGING;
                break;
            case 2:
                property.callStatus02 = NHCRINGING;
                break;
            case 3:
                property.callStatus03 = NHCRINGING;
                break;
            case 4:
                property.callStatus04 = NHCRINGING;
                break;
            default:
                break;
        }

        String topic = profile + "/control/devices/cmd";
        String gsonMessage = gson.toJson(message);
        sendDeviceMessage(topic, gsonMessage);
    }

    @Override
    public void executeAccessUnlock(String accessId) {
        NhcMessage2 message = new NhcMessage2();

        message.method = "devices.control";
        List<NhcMessageParam> params = new ArrayList<>();
        NhcMessageParam param = new NhcMessageParam();
        params.add(param);
        message.params = params;
        List<NhcDevice2> devices = new ArrayList<>();
        NhcDevice2 device = new NhcDevice2();
        devices.add(device);
        param.devices = devices;
        device.uuid = accessId;
        List<NhcProperty> deviceProperties = new ArrayList<>();
        NhcProperty property = new NhcProperty();
        deviceProperties.add(property);
        device.properties = deviceProperties;

        NhcAccess2 accessDevice = (NhcAccess2) accessDevices.get(accessId);
        if (accessDevice == null) {
            return;
        }

        property.doorlock = NHCOPEN;

        String topic = profile + "/control/devices/cmd";
        String gsonMessage = gson.toJson(message);
        sendDeviceMessage(topic, gsonMessage);
    }

    @Override
    public void executeArm(String alarmId) {
        executeAlarm(alarmId, NHCARM);
    }

    @Override
    public void executeDisarm(String alarmId) {
        executeAlarm(alarmId, NHCDISARM);
    }

    private void executeAlarm(String alarmId, String state) {
        NhcMessage2 message = new NhcMessage2();

        message.method = "devices.control";
        List<NhcMessageParam> params = new ArrayList<>();
        NhcMessageParam param = new NhcMessageParam();
        params.add(param);
        message.params = params;
        List<NhcDevice2> devices = new ArrayList<>();
        NhcDevice2 device = new NhcDevice2();
        devices.add(device);
        param.devices = devices;
        device.uuid = alarmId;
        List<NhcProperty> deviceProperties = new ArrayList<>();
        NhcProperty property = new NhcProperty();
        deviceProperties.add(property);
        device.properties = deviceProperties;

        NhcAlarm2 alarmDevice = (NhcAlarm2) alarmDevices.get(alarmId);
        if (alarmDevice == null) {
            return;
        }

        property.control = state;

        String topic = profile + "/control/devices/cmd";
        String gsonMessage = gson.toJson(message);
        sendDeviceMessage(topic, gsonMessage);
    }

    private void sendDeviceMessage(String topic, String gsonMessage) {
        try {
            mqttConnection.connectionPublish(topic, gsonMessage);

        } catch (MqttException e) {
            String message = e.getLocalizedMessage();

            logger.debug("sending command failed, trying to restart communication");
            restartCommunication();
            // retry sending after restart
            try {
                if (communicationActive()) {
                    mqttConnection.connectionPublish(topic, gsonMessage);
                } else {
                    logger.debug("failed to restart communication");
                }
            } catch (MqttException e1) {
                message = e1.getLocalizedMessage();

                logger.debug("error resending device command");
            }
            if (!communicationActive()) {
                message = (message != null) ? message : "@text/offline.communication-error";
                connectionLost(message);
                // Keep on trying to restart, but don't send message anymore
                scheduleRestartCommunication();
            }
        }
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        String message = new String(payload);
        if ((profile + "/system/evt").equals(topic)) {
            systemEvt(message);
        } else if ((profile + "/system/rsp").equals(topic)) {
            logger.debug("received topic {}, payload {}", topic, message);
            systeminfoPublishRsp(message);
        } else if ((profile + "/notification/evt").equals(topic)) {
            logger.debug("received topic {}, payload {}", topic, message);
            notificationEvt(message);
        } else if ((profile + "/control/devices/evt").equals(topic)) {
            logger.trace("received topic {}, payload {}", topic, message);
            devicesEvt(message);
        } else if ((profile + "/control/devices/rsp").equals(topic)) {
            logger.debug("received topic {}, payload {}", topic, message);
            devicesListRsp(message);
        } else if ((profile + "/authentication/rsp").equals(topic)) {
            logger.debug("received topic {}, payload {}", topic, message);
            servicesListRsp(message);
        } else if ((profile + "/control/devices.error").equals(topic)) {
            logger.warn("received error {}", message);
        } else {
            logger.trace("not acted on received message topic {}, payload {}", topic, message);
        }
    }

    /**
     * @return system info retrieved from Connected Controller
     */
    public NhcSystemInfo2 getSystemInfo() {
        NhcSystemInfo2 systemInfo = nhcSystemInfo;
        if (systemInfo == null) {
            systemInfo = new NhcSystemInfo2();
        }
        return systemInfo;
    }

    /**
     * @return time info retrieved from Connected Controller
     */
    public NhcTimeInfo2 getTimeInfo() {
        NhcTimeInfo2 timeInfo = nhcTimeInfo;
        if (timeInfo == null) {
            timeInfo = new NhcTimeInfo2();
        }
        return timeInfo;
    }

    /**
     * @return comma separated list of services retrieved from Connected Controller
     */
    public String getServices() {
        return services.stream().map(NhcService2::name).collect(Collectors.joining(", "));
    }

    @Override
    public void connectionStateChanged(MqttConnectionState state, @Nullable Throwable error) {
        // do in separate thread as this method needs to return early
        scheduler.submit(() -> {
            if (error != null) {
                logger.debug("Connection state: {}, error", state, error);
                String localizedMessage = error.getLocalizedMessage();
                String message = (localizedMessage != null) ? localizedMessage : "@text/offline.communication-error";
                connectionLost(message);
                scheduleRestartCommunication();
            } else if ((state == MqttConnectionState.CONNECTED) && !initStarted) {
                initialize();
            } else {
                logger.trace("Connection state: {}", state);
            }
        });
    }
}
