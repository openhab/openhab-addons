/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpResponseException;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.gardena.internal.config.GardenaConfig;
import org.openhab.binding.gardena.internal.config.GardenaConfigWrapper;
import org.openhab.binding.gardena.internal.exception.GardenaDeviceNotFoundException;
import org.openhab.binding.gardena.internal.exception.GardenaException;
import org.openhab.binding.gardena.internal.exception.GardenaUnauthorizedException;
import org.openhab.binding.gardena.internal.model.Ability;
import org.openhab.binding.gardena.internal.model.Device;
import org.openhab.binding.gardena.internal.model.Devices;
import org.openhab.binding.gardena.internal.model.Errors;
import org.openhab.binding.gardena.internal.model.Location;
import org.openhab.binding.gardena.internal.model.Locations;
import org.openhab.binding.gardena.internal.model.NoResult;
import org.openhab.binding.gardena.internal.model.Property;
import org.openhab.binding.gardena.internal.model.Session;
import org.openhab.binding.gardena.internal.model.SessionWrapper;
import org.openhab.binding.gardena.internal.model.Setting;
import org.openhab.binding.gardena.internal.model.command.Command;
import org.openhab.binding.gardena.internal.model.command.MowerParkUntilFurtherNoticeCommand;
import org.openhab.binding.gardena.internal.model.command.MowerParkUntilNextTimerCommand;
import org.openhab.binding.gardena.internal.model.command.MowerStartOverrideTimerCommand;
import org.openhab.binding.gardena.internal.model.command.MowerStartResumeScheduleCommand;
import org.openhab.binding.gardena.internal.model.command.SensorMeasureAmbientTemperatureCommand;
import org.openhab.binding.gardena.internal.model.command.SensorMeasureLightCommand;
import org.openhab.binding.gardena.internal.model.command.SensorMeasureSoilHumidityCommand;
import org.openhab.binding.gardena.internal.model.command.SensorMeasureSoilTemperatureCommand;
import org.openhab.binding.gardena.internal.model.command.SettingCommand;
import org.openhab.binding.gardena.internal.model.command.SettingCommandWrapper;
import org.openhab.binding.gardena.internal.model.command.WateringCancelOverrideCommand;
import org.openhab.binding.gardena.internal.model.command.WateringManualOverrideCommand;
import org.openhab.binding.gardena.internal.model.deser.DateDeserializer;
import org.openhab.binding.gardena.internal.model.property.SimpleProperties;
import org.openhab.binding.gardena.internal.model.property.SimplePropertiesWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * {@link GardenaSmart} implementation to access Gardena Smart Home.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GardenaSmartImpl implements GardenaSmart {
    private final Logger logger = LoggerFactory.getLogger(GardenaSmartImpl.class);

    public static final String DEVICE_CATEGORY_PUMP = "electronic_pressure_pump";
    private static final String ABILITY_MOWER = "mower";
    private static final String ABILITY_OUTLET = "outlet";
    private static final String ABILITY_HUMIDITY = "humidity";
    private static final String ABILITY_LIGHT = "light";
    private static final String ABILITY_AMBIENT_TEMPERATURE = "ambient_temperature";
    private static final String ABILITY_SOIL_TEMPERATURE = "soil_temperature";
    private static final String ABILITY_PUMP_ON_OFF = "pump_on_off";

    private static final String PROPERTY_BUTTON_MANUAL_OVERRIDE_TIME = "button_manual_override_time";

    private static final String DEVICE_CATEGORY_MOWER = "mower";
    private static final String DEVICE_CATEGORY_GATEWAY = "gateway";

    private static final String DEFAULT_MOWER_DURATION = "180";

    private static final String URL = "https://smart.gardena.com";
    private static final String URL_LOGIN = URL + "/sg-1/sessions";
    private static final String URL_LOCATIONS = URL + "/sg-1/locations/?user_id=";
    private static final String URL_DEVICES = URL + "/sg-1/devices/?locationId=";
    private static final String URL_COMMAND = URL + "/sg-1/devices/%s/abilities/%s/command?locationId=%s";
    private static final String URL_PROPERTY = URL + "/sg-1/devices/%s/abilities/%s/properties/%s?locationId=%s";
    private static final String URL_SETTING = URL + "/sg-1/devices/%s/settings/%s?locationId=%s";

    private Gson gson = new GsonBuilder().registerTypeAdapter(Date.class, new DateDeserializer()).create();
    private HttpClient httpClient;

    private String mowerDuration = DEFAULT_MOWER_DURATION;
    private Session session;
    private GardenaConfig config;
    private String id;

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> refreshThreadFuture;
    private RefreshDevicesThread refreshDevicesThread = new RefreshDevicesThread();

    private GardenaSmartEventListener eventListener;

    private Map<String, Device> allDevicesById = new HashMap<>();
    private Set<Location> allLocations = new HashSet<>();

    @Override
    public void init(String id, GardenaConfig config, GardenaSmartEventListener eventListener,
            ScheduledExecutorService scheduler) throws GardenaException {
        this.id = id;
        this.config = config;
        this.eventListener = eventListener;
        this.scheduler = scheduler;

        if (!config.isValid()) {
            throw new GardenaException("Invalid config, no email or password specified");
        }

        httpClient = new HttpClient(new SslContextFactory(true));
        httpClient.setConnectTimeout(config.getConnectionTimeout() * 1000L);

        try {
            httpClient.start();
        } catch (Exception ex) {
            throw new GardenaException(ex.getMessage(), ex);
        }

        loadAllDevices();
    }

    @Override
    public void dispose() {
        stopRefreshThread(true);
        if (httpClient != null) {
            try {
                httpClient.stop();
            } catch (Exception e) {
                // ignore
            }
            httpClient.destroy();
        }
        allLocations.clear();
        allDevicesById.clear();
    }

    /**
     * Schedules the device refresh thread.
     */
    private void startRefreshThread() {
        refreshThreadFuture = scheduler.scheduleWithFixedDelay(refreshDevicesThread, 6, config.getRefresh(),
                TimeUnit.SECONDS);
    }

    /**
     * Stops the device refresh thread.
     */
    private void stopRefreshThread(boolean force) {
        if (refreshThreadFuture != null) {
            refreshThreadFuture.cancel(force);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Set<Location> getLocations() {
        return allLocations;
    }

    @Override
    public Device getDevice(String deviceId) throws GardenaException {
        Device device = allDevicesById.get(deviceId);
        if (device == null) {
            throw new GardenaDeviceNotFoundException(
                    String.format("Device with id '%s' not found on gateway '%s'", deviceId, id));
        }
        return device;
    }

    @Override
    public void loadAllDevices() throws GardenaException {
        stopRefreshThread(false);
        try {
            allLocations.clear();
            allDevicesById.clear();

            verifySession();
            Locations locations = executeRequest(HttpMethod.GET, URL_LOCATIONS + session.getUserId(), null,
                    Locations.class);

            for (Location location : locations.getLocations()) {
                allLocations.add(location);
                Devices devices = loadDevices(location);
                for (Device device : devices.getDevices()) {
                    if (DEVICE_CATEGORY_GATEWAY.equals(device.getCategory())) {
                        location.getDeviceIds().remove(device.getId());
                    } else {
                        allDevicesById.put(device.getId(), device);
                    }
                }
            }
        } finally {
            startRefreshThread();
        }
    }

    /**
     * Loads all devices for the location, adds virtual properties for commands.
     */
    private Devices loadDevices(Location location) throws GardenaException {
        Devices devices = executeRequest(HttpMethod.GET, URL_DEVICES + location.getId(), null, Devices.class);
        for (Device device : devices.getDevices()) {
            device.setLocation(location);
            for (Ability ability : device.getAbilities()) {
                ability.setDevice(device);
                for (Property property : ability.getProperties()) {
                    property.setAbility(ability);

                    // special conversion for pump, convert on/off to boolean
                    if (device.getCategory().equals(DEVICE_CATEGORY_PUMP)
                            && property.getName().equals(ABILITY_PUMP_ON_OFF)) {
                        property.setValue(String.valueOf("on".equalsIgnoreCase(property.getValue())));
                    }

                }
            }
            for (Setting setting : device.getSettings()) {
                setting.setDevice(device);
            }

            if (DEVICE_CATEGORY_MOWER.equals(device.getCategory())) {
                Ability mower = device.getAbility(ABILITY_MOWER);
                mower.addProperty(new Property(GardenaSmartCommandName.PARK_UNTIL_NEXT_TIMER, "false"));
                mower.addProperty(new Property(GardenaSmartCommandName.PARK_UNTIL_FURTHER_NOTICE, "false"));
                mower.addProperty(new Property(GardenaSmartCommandName.START_RESUME_SCHEDULE, "false"));
                mower.addProperty(new Property(GardenaSmartCommandName.START_OVERRIDE_TIMER, "false"));

                mower.addProperty(new Property(GardenaSmartCommandName.DURATION_PROPERTY, mowerDuration));
            }
        }
        return devices;
    }

    @Override
    public void sendCommand(Device device, GardenaSmartCommandName commandName, Object value) throws GardenaException {
        Ability ability = null;
        Command command = null;

        switch (commandName) {
            case PARK_UNTIL_NEXT_TIMER:
                ability = device.getAbility(ABILITY_MOWER);
                command = new MowerParkUntilNextTimerCommand();
                break;
            case PARK_UNTIL_FURTHER_NOTICE:
                ability = device.getAbility(ABILITY_MOWER);
                command = new MowerParkUntilFurtherNoticeCommand();
                break;
            case START_RESUME_SCHEDULE:
                ability = device.getAbility(ABILITY_MOWER);
                command = new MowerStartResumeScheduleCommand();
                break;
            case START_OVERRIDE_TIMER:
                ability = device.getAbility(ABILITY_MOWER);
                command = new MowerStartOverrideTimerCommand(mowerDuration);
                break;
            case DURATION_PROPERTY:
                if (value == null) {
                    throw new GardenaException("Command '" + commandName + "' requires a value");
                }
                mowerDuration = ObjectUtils.toString(value);
                return;
            case MEASURE_AMBIENT_TEMPERATURE:
                ability = device.getAbility(ABILITY_AMBIENT_TEMPERATURE);
                command = new SensorMeasureAmbientTemperatureCommand();
                break;
            case MEASURE_LIGHT:
                ability = device.getAbility(ABILITY_LIGHT);
                command = new SensorMeasureLightCommand();
                break;
            case MEASURE_SOIL_HUMIDITY:
                ability = device.getAbility(ABILITY_HUMIDITY);
                command = new SensorMeasureSoilHumidityCommand();
                break;
            case MEASURE_SOIL_TEMPERATURE:
                ability = device.getAbility(ABILITY_SOIL_TEMPERATURE);
                command = new SensorMeasureSoilTemperatureCommand();
                break;
            case OUTLET_MANUAL_OVERRIDE_TIME:
                if (value == null) {
                    throw new GardenaException("Command '" + commandName + "' requires a value");
                }
                SimpleProperties prop = new SimpleProperties(PROPERTY_BUTTON_MANUAL_OVERRIDE_TIME,
                        ObjectUtils.toString(value));
                String propertyUrl = String.format(URL_PROPERTY, device.getId(), ABILITY_OUTLET,
                        PROPERTY_BUTTON_MANUAL_OVERRIDE_TIME, device.getLocation().getId());

                stopRefreshThread(false);
                executeRequest(HttpMethod.PUT, propertyUrl, new SimplePropertiesWrapper(prop), NoResult.class);
                device.getAbility(ABILITY_OUTLET).getProperty(PROPERTY_BUTTON_MANUAL_OVERRIDE_TIME)
                        .setValue(prop.getValue());
                startRefreshThread();

                break;
            case OUTLET_VALVE:
                ability = device.getAbility(ABILITY_OUTLET);
                if (value != null && value == Boolean.TRUE) {
                    String wateringDuration = device.getAbility(ABILITY_OUTLET)
                            .getProperty(PROPERTY_BUTTON_MANUAL_OVERRIDE_TIME).getValue();
                    command = new WateringManualOverrideCommand(wateringDuration);
                } else {
                    command = new WateringCancelOverrideCommand();
                }
                break;

            default:
                throw new GardenaException("Unknown command " + commandName);
        }

        if (command != null) {
            stopRefreshThread(false);
            executeRequest(HttpMethod.POST, getCommandUrl(device, ability), command, NoResult.class);
            startRefreshThread();
        }
    }

    @Override
    public void sendSetting(Setting setting, Object value) throws GardenaException {
        SettingCommand settingCommand = new SettingCommand(setting.getName());
        settingCommand.setDeviceId(setting.getDevice().getId());
        settingCommand.setValue(value);

        stopRefreshThread(false);
        executeRequest(HttpMethod.PUT, getSettingUrl(setting), new SettingCommandWrapper(settingCommand),
                NoResult.class);
        startRefreshThread();
    }

    /**
     * Returns the command url.
     */
    private String getCommandUrl(Device device, Ability ability) throws GardenaException {
        return String.format(URL_COMMAND, device.getId(), ability.getName(), device.getLocation().getId());
    }

    /**
     * Returns the settings url.
     */
    private String getSettingUrl(Setting setting) {
        Device device = setting.getDevice();
        return String.format(URL_SETTING, device.getId(), setting.getId(), device.getLocation().getId());
    }

    /**
     * Communicates with Gardena Smart Home and parses the result.
     */
    private synchronized <T> T executeRequest(HttpMethod method, String url, Object contentObject, Class<T> result)
            throws GardenaException {

        try {
            if (logger.isTraceEnabled()) {
                logger.trace("{} request:  {}", method, url);
                if (contentObject != null) {
                    logger.trace("{} data   :  {}", method, gson.toJson(contentObject));
                }
            }

            Request request = httpClient.newRequest(url).method(method)
                    .timeout(config.getConnectionTimeout(), TimeUnit.SECONDS)
                    .header(HttpHeader.CONTENT_TYPE, "application/json").header(HttpHeader.ACCEPT, "application/json")
                    .header(HttpHeader.ACCEPT_ENCODING, "gzip");

            if (contentObject != null) {
                StringContentProvider content = new StringContentProvider(gson.toJson(contentObject));
                request.content(content);
            }

            if (!result.equals(SessionWrapper.class)) {
                verifySession();
                request.header("X-Session", session.getToken());
            }

            ContentResponse contentResponse = request.send();
            int status = contentResponse.getStatus();
            if (logger.isTraceEnabled()) {
                logger.trace("Status  : {}", status);
                logger.trace("Response: {}", contentResponse.getContentAsString());
            }

            if (status == 500) {
                throw new GardenaException(
                        gson.fromJson(contentResponse.getContentAsString(), Errors.class).toString());
            } else if (status != 200 && status != 204) {
                throw new GardenaException(String.format("Error %s %s", status, contentResponse.getReason()));
            }

            if (result == NoResult.class) {
                return null;
            }

            return gson.fromJson(contentResponse.getContentAsString(), result);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof HttpResponseException) {
                HttpResponseException responseException = (HttpResponseException) ex.getCause();
                int status = responseException.getResponse().getStatus();
                if (status == 401) {
                    throw new GardenaUnauthorizedException(ex.getCause());
                }
            }
            throw new GardenaException(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new GardenaException(ex.getMessage(), ex);
        }
    }

    /**
     * Verifies the Gardena Smart Home session and reconnects if necessary.
     */
    private void verifySession() throws GardenaException {
        if (session == null
                || session.getCreated() + (config.getSessionTimeout() * 60000) <= System.currentTimeMillis()) {
            logger.trace("(Re)logging in to Gardena Smart Home");
            session = executeRequest(HttpMethod.POST, URL_LOGIN, new GardenaConfigWrapper(config), SessionWrapper.class)
                    .getSession();
        }
    }

    /**
     * Thread which refreshes the data from Gardena Smart Home.
     */
    private class RefreshDevicesThread implements Runnable {
        private boolean connectionLost = false;

        @Override
        public void run() {
            try {
                logger.debug("Refreshing gardena device data");
                Map<String, Device> newDevicesById = new HashMap<String, Device>();

                for (Location location : allLocations) {
                    Devices devices = loadDevices(location);
                    for (Device device : devices.getDevices()) {
                        if (DEVICE_CATEGORY_GATEWAY.equals(device.getCategory())) {
                            location.getDeviceIds().remove(device.getId());
                        } else {
                            newDevicesById.put(device.getId(), device);
                        }
                    }
                }

                if (connectionLost) {
                    connectionLost = false;
                    logger.info("Connection resumed to Gardena Smart Home with id '{}'", id);
                    eventListener.onConnectionResumed();
                }

                // determine deleted devices
                @SuppressWarnings("unchecked")
                Collection<Device> deletedDevices = CollectionUtils.subtract(allDevicesById.values(),
                        newDevicesById.values());

                // determine new devices
                @SuppressWarnings("unchecked")
                Collection<Device> newDevices = CollectionUtils.subtract(newDevicesById.values(),
                        allDevicesById.values());

                // determine updated devices
                @SuppressWarnings("unchecked")
                Collection<Device> updatedDevices = CollectionUtils.intersection(allDevicesById.values(),
                        newDevicesById.values());

                allDevicesById = newDevicesById;
                newDevicesById = null;

                for (Device deletedDevice : deletedDevices) {
                    eventListener.onDeviceDeleted(deletedDevice);
                }

                for (Device newDevice : newDevices) {
                    eventListener.onNewDevice(newDevice);
                }

                for (Device updatedDevice : updatedDevices) {
                    eventListener.onDeviceUpdated(updatedDevice);
                }

            } catch (GardenaException ex) {
                if (!connectionLost) {
                    connectionLost = true;
                    logger.warn("Connection lost to Gardena Smart Home with id '{}'", id);
                    logger.trace("{}", ex.getMessage(), ex);
                    eventListener.onConnectionLost();
                }
            }
        }
    }
}
