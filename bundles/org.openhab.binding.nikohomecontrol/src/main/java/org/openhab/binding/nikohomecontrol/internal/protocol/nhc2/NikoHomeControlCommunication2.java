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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc2;

import static org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.*;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.eclipse.smarthome.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcControllerEvent;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.ActionType;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NhcDevice2.NhcProperty;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NhcMessage2.NhcMessageParam;
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
public class NikoHomeControlCommunication2 extends NikoHomeControlCommunication implements MqttMessageSubscriber {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlCommunication2.class);

    private final NhcMqttConnection2 mqttConnection;

    private final List<NhcProfile2> profiles = new CopyOnWriteArrayList<>();
    private final List<NhcService2> services = new CopyOnWriteArrayList<>();
    private final List<NhcLocation2> locations = new CopyOnWriteArrayList<>();

    private volatile @Nullable NhcSystemInfo2 nhcSystemInfo;
    private volatile @Nullable NhcTimeInfo2 nhcTimeInfo;

    private volatile String profileUuid = "";

    private volatile @Nullable CompletableFuture<Boolean> communicationStarted;

    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    /**
     * Constructor for Niko Home Control communication object, manages communication with
     * Niko Home Control II Connected Controller.
     *
     * @throws CertificateException when the SSL context for MQTT communication cannot be created
     * @throws UnknownHostException when the IP address is not provided
     *
     */
    public NikoHomeControlCommunication2(NhcControllerEvent handler, String clientId, String persistencePath)
            throws CertificateException {
        super(handler);
        mqttConnection = new NhcMqttConnection2(clientId, persistencePath);
    }

    @Override
    public synchronized void startCommunication() {
        communicationStarted = new CompletableFuture<>();

        startPublicCommunication();
    }

    /**
     * This method executes the first part of the communication start. A public connection (no username or password, but
     * secured with SSL) will be started and the controller will be queried for existing capabilities and profiles.
     */
    private void startPublicCommunication() {
        InetAddress addr = handler.getAddr();
        if (addr == null) {
            logger.warn("Niko Home Control: IP address cannot be empty");
            stopCommunication();
            return;
        }
        String addrString = addr.getHostAddress();
        @SuppressWarnings("null") // default provided, so cannot be null
        int port = handler.getPort();
        logger.debug("Niko Home Control: initializing for mqtt connection to CoCo on {}:{}", addrString, port);

        try {
            mqttConnection.startPublicConnection(this, addrString, port);
            initializePublic();
        } catch (MqttException e) {
            logger.debug("Niko Home Control: error in mqtt communication");
            stopCommunication();
        }
    }

    /**
     * This method executes the second part of the communication start. After the list of profiles are received on the
     * public MQTT connection, this method should be called to stop the general connection and start a touch profile
     * specific MQTT connection. This will allow receiving state information and updating state of devices.
     */
    private void startProfileCommunication() {
        String profile = handler.getProfile();
        String password = handler.getPassword();

        if (profile.isEmpty()) {
            logger.warn("Niko Home Control: no profile set");
            stopCommunication();
            return;
        }
        try {
            profileUuid = profiles.stream().filter(p -> profile.equals(p.name)).findFirst().get().uuid;
        } catch (NoSuchElementException e) {
            logger.warn("Niko Home Control: profile '{}' does not match a profile in the controller", profile);
            stopCommunication();
            return;
        }

        if (password.isEmpty()) {
            logger.warn("Niko Home Control: password for profile cannot be empty");
            stopCommunication();
            return;
        }

        mqttConnection.stopPublicConnection();
        try {
            mqttConnection.startProfileConnection(this, profileUuid, password);
            initializeProfile();
        } catch (MqttException e) {
            logger.warn("Niko Home Control: error in mqtt communication");
            stopCommunication();
        }
    }

    @Override
    public synchronized void stopCommunication() {
        if (communicationStarted != null) {
            communicationStarted.complete(false);
        }
        communicationStarted = null;
        mqttConnection.stopPublicConnection();
        mqttConnection.stopProfileConnection();
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
            logger.debug("Niko Home Control: exception waiting for connection start");
            return false;
        }
    }

    /**
     * After setting up the communication with the Niko Home Control Connected Controller, send all initialization
     * messages.
     *
     */
    private void initializePublic() throws MqttException {
        NhcMessage2 message = new NhcMessage2();

        message.method = "systeminfo.publish";
        mqttConnection.publicConnectionPublish("public/system/cmd", gson.toJson(message));

        message.method = "profiles.list";
        mqttConnection.publicConnectionPublish("public/authentication/cmd", gson.toJson(message));
    }

    /**
     * After setting up the profile communication with the Niko Home Control Connected Controller, send all profile
     * specific initialization messages.
     *
     */
    private void initializeProfile() throws MqttException {
        NhcMessage2 message = new NhcMessage2();

        message.method = "services.list";
        mqttConnection.profileConnectionPublish(profileUuid + "/authentication/cmd", gson.toJson(message));

        message.method = "devices.list";
        mqttConnection.profileConnectionPublish(profileUuid + "/control/devices/cmd", gson.toJson(message));

        message.method = "locations.list";
        mqttConnection.profileConnectionPublish(profileUuid + "/control/locations/cmd", gson.toJson(message));

        message.method = "notifications.list";
        mqttConnection.profileConnectionPublish(profileUuid + "/notification/cmd", gson.toJson(message));
    }

    private void connectionLost() {
        logger.debug("Niko Home Control: connection lost");
        stopCommunication();
        handler.controllerOffline();
    }

    private void timePublishEvt(String response) {
        Type messageType = new TypeToken<NhcMessage2>() {
        }.getType();
        List<NhcTimeInfo2> timeInfo = null;
        try {
            NhcMessage2 message = gson.fromJson(response, messageType);
            if (message.params != null) {
                timeInfo = message.params.stream().filter(p -> (p.timeInfo != null)).findFirst().get().timeInfo;
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Niko Home Control: unexpected json {}", response);
        } catch (NoSuchElementException ignore) {
            // Ignore if timeInfo not present in response, this should not happen in a timeInfo response
        }
        if (timeInfo != null) {
            nhcTimeInfo = timeInfo.get(0);
        }
    }

    private void systeminfoPublishRsp(String response) {
        Type messageType = new TypeToken<NhcMessage2>() {
        }.getType();
        List<NhcSystemInfo2> systemInfo = null;
        try {
            NhcMessage2 message = gson.fromJson(response, messageType);
            if (message.params != null) {
                systemInfo = message.params.stream().filter(p -> (p.systemInfo != null)).findFirst().get().systemInfo;
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Niko Home Control: unexpected json {}", response);
        } catch (NoSuchElementException ignore) {
            // Ignore if systemInfo not present in response, this should not happen in a systemInfo response
        }
        if (systemInfo != null) {
            nhcSystemInfo = systemInfo.get(0);
        }
    }

    private void profilesListRsp(String response) {
        Type messageType = new TypeToken<NhcMessage2>() {
        }.getType();
        List<NhcProfile2> profileList = null;
        try {
            NhcMessage2 message = gson.fromJson(response, messageType);
            if (message.params != null) {
                profileList = message.params.stream().filter(p -> (p.profiles != null)).findFirst().get().profiles;
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Niko Home Control: unexpected json {}", response);
        } catch (NoSuchElementException ignore) {
            // Ignore if profiles not present in response, this should not happen in a profiles response
        }
        profiles.clear();
        if (profileList != null) {
            profiles.addAll(profileList);
        }
    }

    private void servicesListRsp(String response) {
        Type messageType = new TypeToken<NhcMessage2>() {
        }.getType();
        List<NhcService2> serviceList = null;
        try {
            NhcMessage2 message = gson.fromJson(response, messageType);
            if (message.params != null) {
                serviceList = message.params.stream().filter(p -> (p.services != null)).findFirst().get().services;
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Niko Home Control: unexpected json {}", response);
        } catch (NoSuchElementException ignore) {
            // Ignore if services not present in response, this should not happen in a services response
        }
        services.clear();
        if (serviceList != null) {
            services.addAll(serviceList);
        }
    }

    private void locationsListRsp(String response) {
        Type messageType = new TypeToken<NhcMessage2>() {
        }.getType();
        List<NhcLocation2> locationList = null;
        try {
            NhcMessage2 message = gson.fromJson(response, messageType);
            if (message.params != null) {
                locationList = message.params.stream().filter(p -> (p.locations != null)).findFirst().get().locations;
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Niko Home Control: unexpected json {}", response);
        } catch (NoSuchElementException ignore) {
            // Ignore if locations not present in response, this should not happen in a locations response
        }
        locations.clear();
        if (locationList != null) {
            locations.addAll(locationList);
        }
    }

    private void devicesListRsp(String response) {
        Type messageType = new TypeToken<NhcMessage2>() {
        }.getType();
        List<NhcDevice2> deviceList = null;
        try {
            NhcMessage2 message = gson.fromJson(response, messageType);
            if (message.params != null) {
                deviceList = message.params.stream().filter(p -> (p.devices != null)).findFirst().get().devices;
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Niko Home Control: unexpected json {}", response);
        } catch (NoSuchElementException ignore) {
            // Ignore if devices not present in response, this should not happen in a devices response
        }
        if (deviceList == null) {
            return;
        }

        for (NhcDevice2 device : deviceList) {
            String location;
            try {
                location = device.parameters.stream().filter(p -> (p.locationId != null)).findFirst()
                        .get().locationName;
            } catch (NoSuchElementException e) {
                location = null;
            }

            if ("action".equals(device.type)) {
                ActionType actionType = ActionType.GENERIC;
                switch (device.model) {
                    case "generic":
                    case "pir":
                    case "simulation":
                    case "comfort":
                    case "alarms":
                    case "alloff":
                        actionType = ActionType.TRIGGER;
                        break;
                    case "light":
                    case "socket":
                    case "switched-generic":
                        actionType = ActionType.RELAY;
                        break;
                    case "dimmer":
                        actionType = ActionType.DIMMER;
                        break;
                    case "rolldownshutter":
                    case "sunblind":
                    case "venetianblind":
                        actionType = ActionType.ROLLERSHUTTER;
                        break;
                }

                if (!actions.containsKey(device.uuid)) {
                    logger.debug("Niko Home Control: adding action device {}, {}", device.uuid, device.name);

                    NhcAction2 nhcAction = new NhcAction2(device.uuid, device.name, device.model, device.technology,
                            actionType, location);
                    nhcAction.setNhcComm(this);
                    actions.put(device.uuid, nhcAction);
                }

                updateActionState((NhcAction2) actions.get(device.uuid), device);
            } else if ("thermostat".equals(device.type)) {
                if (!thermostats.containsKey(device.uuid)) {
                    logger.debug("Niko Home Control: adding thermostatdevice {}, {}", device.uuid, device.name);

                    NhcThermostat2 nhcThermostat = new NhcThermostat2(device.uuid, device.name, location);
                    nhcThermostat.setNhcComm(this);
                    thermostats.put(device.uuid, nhcThermostat);
                }

                updateThermostatState((NhcThermostat2) thermostats.get(device.uuid), device);
            } else {
                logger.debug("Niko Home Control: device type {} not supported for {}, {}", device.type, device.uuid,
                        device.name);
            }
        }

        // Once a devices list response is received, we know the communication is fully started.
        logger.debug("Niko Home Control: Communication start complete.");
        handler.controllerOnline();
        if (communicationStarted != null) {
            communicationStarted.complete(true);
        }
    }

    private void devicesStatusEvt(String response) {
        Type messageType = new TypeToken<NhcMessage2>() {
        }.getType();
        List<NhcDevice2> deviceList = null;
        try {
            NhcMessage2 message = gson.fromJson(response, messageType);
            if (message.params != null) {
                deviceList = message.params.stream().filter(p -> (p.devices != null)).findFirst().get().devices;
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Niko Home Control: unexpected json {}", response);
        } catch (NoSuchElementException ignore) {
            // Ignore if devices not present in response, this should not happen in a devices event
        }
        if (deviceList == null) {
            return;
        }

        for (NhcDevice2 device : deviceList) {
            if (actions.containsKey(device.uuid)) {
                updateActionState((NhcAction2) actions.get(device.uuid), device);
            } else if (thermostats.containsKey(device.uuid)) {
                updateThermostatState((NhcThermostat2) thermostats.get(device.uuid), device);
            }
        }
    }

    private void notificationEvt(String response) {
        Type messageType = new TypeToken<NhcMessage2>() {
        }.getType();
        List<NhcNotification2> notificationList = null;
        try {
            NhcMessage2 message = gson.fromJson(response, messageType);
            if (message.params != null) {
                notificationList = message.params.stream().filter(p -> (p.notifications != null)).findFirst()
                        .get().notifications;
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Niko Home Control: unexpected json {}", response);
        } catch (NoSuchElementException ignore) {
            // Ignore if notifications not present in response, this should not happen in a notifications event
        }
        logger.debug("Niko Home Control: notifications {}", notificationList);
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
                        logger.debug("Niko Home Control: unexpected message type {}", notification.type);
                }
            }
        }
    }

    private void updateActionState(NhcAction2 action, NhcDevice2 device) {
        if (action.getType() == ActionType.ROLLERSHUTTER) {
            updateRollershutterState(action, device);
        } else {
            updateLightState(action, device);
        }
    }

    private void updateLightState(NhcAction2 action, NhcDevice2 device) {
        Optional<NhcProperty> statusProperty = device.properties.stream().filter(p -> (p.status != null)).findFirst();
        Optional<NhcProperty> dimmerProperty = device.properties.stream().filter(p -> (p.brightness != null))
                .findFirst();
        Optional<NhcProperty> basicStateProperty = device.properties.stream().filter(p -> (p.basicState != null))
                .findFirst();

        String booleanState = null;
        if (statusProperty.isPresent()) {
            booleanState = statusProperty.get().status;
        } else if (basicStateProperty.isPresent()) {
            booleanState = basicStateProperty.get().basicState;
        }

        if (booleanState != null) {
            if (NHCON.equals(booleanState)) {
                action.setBooleanState(true);
                logger.debug("Niko Home Control: setting action {} internally to ON", action.getId());
            } else if (NHCOFF.equals(booleanState)) {
                action.setBooleanState(false);
                logger.debug("Niko Home Control: setting action {} internally to OFF", action.getId());
            }
        }

        if (dimmerProperty.isPresent()) {
            action.setState(Integer.parseInt(dimmerProperty.get().brightness));
            logger.debug("Niko Home Control: setting action {} internally to {}", action.getId(),
                    dimmerProperty.get().brightness);
        }
    }

    private void updateRollershutterState(NhcAction2 action, NhcDevice2 device) {
        Optional<NhcProperty> positionProperty = device.properties.stream().filter(p -> (p.position != null))
                .findFirst();
        Optional<NhcProperty> movingProperty = device.properties.stream().filter(p -> (p.moving != null)).findFirst();

        if (!(movingProperty.isPresent() && Boolean.parseBoolean(movingProperty.get().moving))
                && positionProperty.isPresent()) {
            action.setState(Integer.parseInt(positionProperty.get().position));
            logger.debug("Niko Home Control: setting action {} internally to {}", action.getId(),
                    positionProperty.get().position);
        }
    }

    private void updateThermostatState(NhcThermostat2 thermostat, NhcDevice2 device) {
        Optional<NhcProperty> overruleActiveProperty = device.properties.stream()
                .filter(p -> (p.overruleActive != null)).findFirst();
        Optional<NhcProperty> overruleSetpointProperty = device.properties.stream()
                .filter(p -> (p.overruleSetpoint != null)).findFirst();
        Optional<NhcProperty> overruleTimeProperty = device.properties.stream().filter(p -> (p.overruleTime != null))
                .findFirst();
        Optional<NhcProperty> programProperty = device.properties.stream().filter(p -> (p.program != null)).findFirst();

        Optional<NhcProperty> setpointTemperatureProperty = device.properties.stream()
                .filter(p -> (p.setpointTemperature != null)).findFirst();
        Optional<NhcProperty> ecoSaveProperty = device.properties.stream().filter(p -> (p.ecoSave != null)).findFirst();
        Optional<NhcProperty> ambientTemperatureProperty = device.properties.stream()
                .filter(p -> (p.ambientTemperature != null)).findFirst();

        Optional<NhcProperty> demandProperty = device.properties.stream().filter(p -> (p.demand != null)).findFirst();

        String modeString = programProperty.isPresent() ? programProperty.get().program : "";
        int mode = IntStream.range(0, THERMOSTATMODES.length).filter(i -> THERMOSTATMODES[i].equals(modeString))
                .findFirst().orElse(thermostat.getMode());

        int measured = ambientTemperatureProperty.isPresent()
                ? Math.round(Float.parseFloat(ambientTemperatureProperty.get().ambientTemperature) * 10)
                : thermostat.getMeasured();
        int setpoint = setpointTemperatureProperty.isPresent()
                ? Math.round(Float.parseFloat(setpointTemperatureProperty.get().setpointTemperature) * 10)
                : thermostat.getSetpoint();

        int overrule = 0;
        int overruletime = 0;
        if (overruleActiveProperty.isPresent() && "True".equals(overruleActiveProperty.get().overruleActive)) {
            overrule = overruleSetpointProperty.isPresent()
                    ? Math.round(Float.parseFloat(overruleSetpointProperty.get().overruleSetpoint) * 10)
                    : 0;
            overruletime = overruleTimeProperty.isPresent() ? Integer.parseInt(overruleTimeProperty.get().overruleTime)
                    : 0;
        }

        int ecosave = ecoSaveProperty.isPresent() ? ("True".equals(ecoSaveProperty.get().ecoSave) ? 1 : 0)
                : thermostat.getEcosave();

        int demand = thermostat.getDemand();
        if (demandProperty.isPresent()) {
            switch (demandProperty.get().demand) {
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
        }

        logger.debug(
                "Niko Home Control: setting thermostat {} with measured {}, setpoint {}, mode {}, overrule {}, overruletime {}, ecosave {}, demand {}",
                thermostat.getId(), measured, setpoint, mode, overrule, overruletime, ecosave, demand);
        thermostat.updateState(measured, setpoint, mode, overrule, overruletime, ecosave, demand);
    }

    @Override
    public void executeAction(String actionId, String value) {
        NhcMessage2 message = new NhcMessage2();

        message.method = "devices.control";
        ArrayList<NhcMessageParam> params = new ArrayList<>();
        NhcMessageParam param = message.new NhcMessageParam();
        params.add(param);
        message.params = params;
        ArrayList<NhcDevice2> devices = new ArrayList<>();
        NhcDevice2 device = new NhcDevice2();
        devices.add(device);
        param.devices = devices;
        device.uuid = actionId;
        device.properties = new ArrayList<>();
        NhcProperty property = device.new NhcProperty();
        device.properties.add(property);

        NhcAction2 action = (NhcAction2) actions.get(actionId);

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
                    int position = 100 - Integer.parseInt(value);
                    property.position = String.valueOf(position);
                }
                break;
        }

        String topic = profileUuid + "/control/devices/cmd";
        String gsonMessage = gson.toJson(message);
        sendDeviceMessage(topic, gsonMessage);
    }

    @Override
    public void executeThermostat(String thermostatId, String mode) {
        NhcMessage2 message = new NhcMessage2();

        message.method = "devices.control";
        ArrayList<NhcMessageParam> params = new ArrayList<>();
        NhcMessageParam param = message.new NhcMessageParam();
        params.add(param);
        message.params = params;
        ArrayList<NhcDevice2> devices = new ArrayList<>();
        NhcDevice2 device = new NhcDevice2();
        devices.add(device);
        param.devices = devices;
        device.uuid = thermostatId;
        device.properties = new ArrayList<>();

        NhcProperty overruleActiveProp = device.new NhcProperty();
        device.properties.add(overruleActiveProp);
        overruleActiveProp.overruleActive = "False";

        NhcProperty program = device.new NhcProperty();
        device.properties.add(program);
        program.program = mode;

        String topic = profileUuid + "/control/devices/cmd";
        String gsonMessage = gson.toJson(message);
        sendDeviceMessage(topic, gsonMessage);
    }

    @Override
    public void executeThermostat(String thermostatId, int overruleTemp, int overruleTime) {
        NhcMessage2 message = new NhcMessage2();

        message.method = "devices.control";
        ArrayList<NhcMessageParam> params = new ArrayList<>();
        NhcMessageParam param = message.new NhcMessageParam();
        params.add(param);
        message.params = params;
        ArrayList<NhcDevice2> devices = new ArrayList<>();
        NhcDevice2 device = new NhcDevice2();
        devices.add(device);
        param.devices = devices;
        device.uuid = thermostatId;
        device.properties = new ArrayList<>();

        if (overruleTime > 0) {
            NhcProperty overruleActiveProp = device.new NhcProperty();
            overruleActiveProp.overruleActive = "True";
            device.properties.add(overruleActiveProp);

            NhcProperty overruleSetpointProp = device.new NhcProperty();
            overruleSetpointProp.overruleSetpoint = String.valueOf(overruleTemp);
            device.properties.add(overruleSetpointProp);

            NhcProperty overruleTimeProp = device.new NhcProperty();
            overruleTimeProp.overruleTime = String.valueOf(overruleTime);
            device.properties.add(overruleTimeProp);
        } else {
            NhcProperty overruleActiveProp = device.new NhcProperty();
            overruleActiveProp.overruleActive = "False";
            device.properties.add(overruleActiveProp);
        }

        String topic = profileUuid + "/control/devices/cmd";
        String gsonMessage = gson.toJson(message);
        sendDeviceMessage(topic, gsonMessage);
    }

    private void sendDeviceMessage(String topic, String gsonMessage) {
        try {
            mqttConnection.profileConnectionPublish(topic, gsonMessage);

        } catch (MqttException e) {
            logger.warn("Niko Home Control: sending command failed, trying to restart communication");
            restartCommunication();
            // retry sending after restart
            try {
                if (communicationActive()) {
                    mqttConnection.profileConnectionPublish(topic, gsonMessage);
                } else {
                    logger.warn("Niko Home Control: failed to restart communication");
                    connectionLost();
                }
            } catch (MqttException e1) {
                logger.warn("Niko Home Control: error resending thermostat command");
                connectionLost();
            }
        }
    }

    @Override
    public void processMessage(String topic, byte[] payload) {
        String message = new String(payload);
        if ("public/system/evt".equals(topic)) {
            timePublishEvt(message);
        } else if ((profileUuid + "/system/evt").equals(topic)) {
            // ignore
        } else if ("public/system/rsp".equals(topic)) {
            logger.debug("Niko Home Control: received topic {}, payload {}", topic, message);
            systeminfoPublishRsp(message);
        } else if ("public/authentication/rsp".equals(topic)) {
            logger.debug("Niko Home Control: received topic {}, payload {}", topic, message);
            profilesListRsp(message);
            startProfileCommunication();
        } else if ((profileUuid + "/notification/evt").equals(topic)) {
            logger.debug("Niko Home Control: received topic {}, payload {}", topic, message);
            notificationEvt(message);
        } else if ((profileUuid + "/control/devices/evt").equals(topic)) {
            logger.debug("Niko Home Control: received topic {}, payload {}", topic, message);
            devicesStatusEvt(message);
        } else if ((profileUuid + "/control/devices/rsp").equals(topic)) {
            logger.debug("Niko Home Control: received topic {}, payload {}", topic, message);
            devicesListRsp(message);
        } else if ((profileUuid + "/authentication/rsp").equals(topic)) {
            logger.debug("Niko Home Control: received topic {}, payload {}", topic, message);
            servicesListRsp(message);
        } else if ((profileUuid + "/locations/rsp").equals(topic)) {
            logger.debug("Niko Home Control: received topic {}, payload {}", topic, message);
            locationsListRsp(message);
        } else {
            logger.trace("Niko Home Control: not acted on received message topic {}, payload {}", topic, message);
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
}
