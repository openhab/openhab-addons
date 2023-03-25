/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.connection;

import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.net.ssl.SSLHandshakeException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.hue.internal.dto.ApiVersion;
import org.openhab.binding.hue.internal.dto.ApiVersionUtils;
import org.openhab.binding.hue.internal.dto.Config;
import org.openhab.binding.hue.internal.dto.ConfigUpdate;
import org.openhab.binding.hue.internal.dto.CreateUserRequest;
import org.openhab.binding.hue.internal.dto.ErrorResponse;
import org.openhab.binding.hue.internal.dto.FullConfig;
import org.openhab.binding.hue.internal.dto.FullGroup;
import org.openhab.binding.hue.internal.dto.FullHueObject;
import org.openhab.binding.hue.internal.dto.FullLight;
import org.openhab.binding.hue.internal.dto.FullSensor;
import org.openhab.binding.hue.internal.dto.Group;
import org.openhab.binding.hue.internal.dto.HueObject;
import org.openhab.binding.hue.internal.dto.NewLightsResponse;
import org.openhab.binding.hue.internal.dto.Scene;
import org.openhab.binding.hue.internal.dto.Schedule;
import org.openhab.binding.hue.internal.dto.ScheduleUpdate;
import org.openhab.binding.hue.internal.dto.SearchForLightsRequest;
import org.openhab.binding.hue.internal.dto.SetAttributesRequest;
import org.openhab.binding.hue.internal.dto.StateUpdate;
import org.openhab.binding.hue.internal.dto.SuccessResponse;
import org.openhab.binding.hue.internal.dto.Util;
import org.openhab.binding.hue.internal.exceptions.ApiException;
import org.openhab.binding.hue.internal.exceptions.DeviceOffException;
import org.openhab.binding.hue.internal.exceptions.EmptyResponseException;
import org.openhab.binding.hue.internal.exceptions.EntityNotAvailableException;
import org.openhab.binding.hue.internal.exceptions.GroupTableFullException;
import org.openhab.binding.hue.internal.exceptions.InvalidCommandException;
import org.openhab.binding.hue.internal.exceptions.LinkButtonException;
import org.openhab.binding.hue.internal.exceptions.UnauthorizedException;
import org.openhab.core.i18n.CommunicationException;
import org.openhab.core.i18n.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * Representation of a connection with a Hue Bridge.
 *
 * @author Q42 - Initial contribution
 * @author Andre Fuechsel - search for lights with given serial number added
 * @author Denis Dudnik - moved Jue library source code inside the smarthome Hue binding, minor code cleanup
 * @author Samuel Leisering - added cached config and API-Version
 * @author Laurent Garnier - change the return type of getGroups
 */
@NonNullByDefault
public class HueBridge {

    private final Logger logger = LoggerFactory.getLogger(HueBridge.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private final HttpClient httpClient;
    private final String ip;
    private final String baseUrl;
    private @Nullable String username;
    private long timeout = TimeUnit.MILLISECONDS.convert(5, TimeUnit.SECONDS);

    private final Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();

    private final LinkedList<AsyncPutParameters> commandsQueue = new LinkedList<>();
    private @Nullable Future<?> job;
    private final ScheduledExecutorService scheduler;

    private @Nullable Config cachedConfig;

    /**
     * Connect with a bridge as a new user.
     *
     * @param httpClient instance of the Jetty shared client
     * @param ip ip address of bridge
     * @param port port of bridge
     * @param protocol protocol to connect to the bridge
     * @param scheduler the ExecutorService to schedule commands
     */
    public HueBridge(HttpClient httpClient, String ip, int port, String protocol, ScheduledExecutorService scheduler) {
        this.httpClient = httpClient;
        this.ip = ip;
        String baseUrl;
        try {
            URI uri = new URI(protocol, null, ip, port, "/api", null, null);
            baseUrl = uri.toString();
        } catch (URISyntaxException e) {
            logger.error("exception during constructing URI protocol={}, host={}, port={}", protocol, ip, port, e);
            baseUrl = protocol + "://" + ip + ":" + port + "/api";
        }
        this.baseUrl = baseUrl;
        this.scheduler = scheduler;
    }

    /**
     * Connect with a bridge as an existing user.
     *
     * The username is verified by requesting the list of lights.
     * Use the ip only constructor and authenticate() function if
     * you don't want to connect right now.
     *
     * @param httpClient instance of the Jetty shared client
     * @param ip ip address of bridge
     * @param port port of bridge
     * @param protocol protocol to connect to the bridge
     * @param username username to authenticate with
     * @param scheduler the ExecutorService to schedule commands
     */
    public HueBridge(HttpClient httpClient, String ip, int port, String protocol, String username,
            ScheduledExecutorService scheduler)
            throws IOException, ApiException, ConfigurationException, UnauthorizedException {
        this(httpClient, ip, port, protocol, scheduler);
        authenticate(username);
    }

    /**
     * Set the connect and read timeout for HTTP requests.
     *
     * @param timeout timeout in milliseconds or 0 for indefinitely
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns the IP address of the bridge.
     *
     * @return ip address of bridge
     */
    public String getIPAddress() {
        return ip;
    }

    public ApiVersion getVersion() throws IOException, ApiException {
        Config c = getCachedConfig();
        return ApiVersion.of(c.getApiVersion());
    }

    /**
     * Returns a cached version of the basic {@link Config} mostly immutable configuration.
     * This can be used to reduce load on the bridge.
     *
     * @return The {@link Config} of the Hue Bridge, loaded and cached lazily on the first call
     * @throws IOException
     * @throws ApiException
     */
    private Config getCachedConfig() throws IOException, ApiException {
        if (cachedConfig == null) {
            cachedConfig = getConfig();
        }

        return Objects.requireNonNull(cachedConfig);
    }

    /**
     * Returns the username currently authenticated with or null if there isn't one.
     *
     * @return username or null
     */
    public @Nullable String getUsername() {
        return username;
    }

    /**
     * Returns if authentication was successful on the bridge.
     *
     * @return true if authenticated on the bridge, false otherwise
     */
    public boolean isAuthenticated() {
        return getUsername() != null;
    }

    /**
     * Returns a list of lights known to the bridge.
     *
     * @return list of known lights as {@link FullLight}s
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public List<FullLight> getFullLights() throws IOException, ApiException {
        if (ApiVersionUtils.supportsFullLights(getVersion())) {
            Type gsonType = FullLight.GSON_TYPE;
            return getTypedLights(gsonType);
        } else {
            return getFullConfig().getLights();
        }
    }

    /**
     * Returns a list of lights known to the bridge.
     *
     * @return list of known lights
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public List<HueObject> getLights() throws IOException, ApiException {
        Type gsonType = HueObject.GSON_TYPE;
        return getTypedLights(gsonType);
    }

    private <T extends HueObject> List<T> getTypedLights(Type gsonType)
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = get(getRelativeURL("lights"));

        handleErrors(result);

        if (result.body.isBlank()) {
            throw new EmptyResponseException("GET request 'lights' returned an unexpected empty reponse");
        }

        Map<String, T> lightMap = safeFromJson(result.body, gsonType);
        List<T> lights = new ArrayList<>();
        lightMap.forEach((id, light) -> {
            light.setId(id);
            lights.add(light);
        });
        return lights;
    }

    /**
     * Returns a list of sensors known to the bridge
     *
     * @return list of sensors
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public List<FullSensor> getSensors()
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = get(getRelativeURL("sensors"));

        handleErrors(result);

        if (result.body.isBlank()) {
            throw new EmptyResponseException("GET request 'sensors' returned an unexpected empty reponse");
        }

        Map<String, FullSensor> sensorMap = safeFromJson(result.body, FullSensor.GSON_TYPE);
        List<FullSensor> sensors = new ArrayList<>();
        sensorMap.forEach((id, sensor) -> {
            sensor.setId(id);
            sensors.add(sensor);
        });
        return sensors;
    }

    /**
     * Returns the last time a search for new lights was started.
     * If a search is currently running, the current time will be
     * returned or null if a search has never been started.
     *
     * @return last search time
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public @Nullable Date getLastSearch()
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = get(getRelativeURL("lights/new"));

        handleErrors(result);

        if (result.body.isBlank()) {
            throw new EmptyResponseException("GET request 'lights/new' returned an unexpected empty reponse");
        }

        String lastScan = safeFromJson(result.body, NewLightsResponse.class).lastscan;

        switch (lastScan) {
            case "none":
                return null;
            case "active":
                return new Date();
            default:
                try {
                    return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(lastScan);
                } catch (ParseException e) {
                    return null;
                }
        }
    }

    /**
     * Start searching for new lights for 1 minute.
     * A maximum amount of 15 new lights will be added.
     *
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public void startSearch() throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = post(getRelativeURL("lights"), "");

        handleErrors(result);
    }

    /**
     * Start searching for new lights with given serial numbers for 1 minute.
     * A maximum amount of 15 new lights will be added.
     *
     * @param serialNumbers list of serial numbers
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public void startSearch(List<String> serialNumbers)
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = post(getRelativeURL("lights"), gson.toJson(new SearchForLightsRequest(serialNumbers)));

        handleErrors(result);
    }

    /**
     * Returns detailed information for the given light.
     *
     * @param light light
     * @return detailed light information
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if a light with the given id doesn't exist
     */
    public FullHueObject getLight(HueObject light)
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = get(getRelativeURL("lights/" + enc(light.getId())));

        handleErrors(result);

        if (result.body.isBlank()) {
            throw new EmptyResponseException(
                    "GET request 'lights/" + enc(light.getId()) + "' returned an unexpected empty reponse");
        }

        FullHueObject fullLight = safeFromJson(result.body, FullLight.class);
        fullLight.setId(light.getId());
        return fullLight;
    }

    /**
     * Changes the name of the light and returns the new name.
     * A number will be appended to duplicate names, which may result in a new name exceeding 32 characters.
     *
     * @param light light
     * @param name new name [0..32]
     * @return new name
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified light no longer exists
     */
    public String setLightName(HueObject light, String name)
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = put(getRelativeURL("lights/" + enc(light.getId())),
                gson.toJson(new SetAttributesRequest(name)));

        handleErrors(result);

        if (result.body.isBlank()) {
            throw new EmptyResponseException(
                    "PUT request 'lights/" + enc(light.getId()) + "' returned an unexpected empty reponse");
        }

        List<SuccessResponse> entries = safeFromJson(result.body, SuccessResponse.GSON_TYPE);
        SuccessResponse response = entries.get(0);

        String lightName = (String) response.success.get("/lights/" + enc(light.getId()) + "/name");
        if (lightName == null) {
            throw new ApiException("Response didn't contain light name.");
        }
        return lightName;
    }

    /**
     * Changes the state of a light.
     *
     * @param light light
     * @param update changes to the state
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified light no longer exists
     * @throws DeviceOffException thrown if the specified light is turned off
     * @throws IOException if the bridge cannot be reached
     */
    public CompletableFuture<HueResult> setLightState(FullLight light, StateUpdate update) {
        requireAuthentication();

        return putAsync(getRelativeURL("lights/" + enc(light.getId()) + "/state"), update.toJson(),
                update.getMessageDelay());
    }

    /**
     * Changes the state of a clip sensor.
     *
     * @param sensor sensor
     * @param update changes to the state
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified sensor no longer exists
     * @throws DeviceOffException thrown if the specified sensor is turned off
     * @throws IOException if the bridge cannot be reached
     */
    public CompletableFuture<HueResult> setSensorState(FullSensor sensor, StateUpdate update) {
        requireAuthentication();

        return putAsync(getRelativeURL("sensors/" + enc(sensor.getId()) + "/state"), update.toJson(),
                update.getMessageDelay());
    }

    /**
     * Changes the config of a sensor.
     *
     * @param sensor sensor
     * @param update changes to the config
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified sensor no longer exists
     * @throws IOException if the bridge cannot be reached
     */
    public CompletableFuture<HueResult> updateSensorConfig(FullSensor sensor, ConfigUpdate update) {
        requireAuthentication();

        return putAsync(getRelativeURL("sensors/" + enc(sensor.getId()) + "/config"), update.toJson(),
                update.getMessageDelay());
    }

    /**
     * Returns a group object representing all lights.
     *
     * @return all lights pseudo group
     */
    public Group getAllGroup() {
        return new Group();
    }

    /**
     * Returns the list of groups, including the unmodifiable all lights group.
     *
     * @return list of groups
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public List<FullGroup> getGroups()
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = get(getRelativeURL("groups"));

        handleErrors(result);

        if (result.body.isBlank()) {
            throw new EmptyResponseException("GET request 'groups' returned an unexpected empty reponse");
        }

        Map<String, FullGroup> groupMap = safeFromJson(result.body, FullGroup.GSON_TYPE);
        List<FullGroup> groups = new ArrayList<>();
        if (groupMap.get("0") == null) {
            // Group 0 is not returned, we create it as in fact it exists
            try {
                groups.add(getGroup(getAllGroup()));
            } catch (FileNotFoundException e) {
                // We need a special exception handling here to further support deCONZ REST API. On deCONZ group "0" may
                // not exist and the APIs will return a different HTTP status code if requesting a non existing group
                // (Hue: 200, deCONZ: 404).
                // see https://github.com/openhab/openhab-addons/issues/9175
                logger.debug("Cannot find AllGroup with id \"0\" on Hue Bridge. Skipping it.");
            }
        }
        groupMap.forEach((id, group) -> {
            group.setId(id);
            groups.add(group);
        });
        return groups;
    }

    /**
     * Creates a new group and returns it.
     * Due to API limitations, the name of the returned object
     * will simply be "Group". The bridge will append a number to this
     * name if it's a duplicate. To get the final name, call getGroup
     * with the returned object.
     *
     * @param lights lights in group
     * @return object representing new group
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws GroupTableFullException thrown if the group limit has been reached
     */
    public Group createGroup(List<HueObject> lights)
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = post(getRelativeURL("groups"), gson.toJson(new SetAttributesRequest(lights)));

        handleErrors(result);

        if (result.body.isBlank()) {
            throw new EmptyResponseException("POST request 'groups' returned an unexpected empty reponse");
        }

        List<SuccessResponse> entries = safeFromJson(result.body, SuccessResponse.GSON_TYPE);
        SuccessResponse response = entries.get(0);

        Group group = new Group();
        group.setName("Group");
        group.setId(Util.quickMatch("^/groups/([0-9]+)$", (String) response.success.values().toArray()[0]));
        return group;
    }

    /**
     * Creates a new group and returns it.
     * Due to API limitations, the name of the returned object
     * will simply be the same as the name parameter. The bridge will
     * append a number to the name if it's a duplicate. To get the final
     * name, call getGroup with the returned object.
     *
     * @param name new group name
     * @param lights lights in group
     * @return object representing new group
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws GroupTableFullException thrown if the group limit has been reached
     */
    public Group createGroup(String name, List<HueObject> lights)
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = post(getRelativeURL("groups"), gson.toJson(new SetAttributesRequest(name, lights)));

        handleErrors(result);

        if (result.body.isBlank()) {
            throw new EmptyResponseException("POST request 'groups' returned an unexpected empty reponse");
        }

        List<SuccessResponse> entries = safeFromJson(result.body, SuccessResponse.GSON_TYPE);
        SuccessResponse response = entries.get(0);

        Group group = new Group();
        group.setName(name);
        group.setId(Util.quickMatch("^/groups/([0-9]+)$", (String) response.success.values().toArray()[0]));
        return group;
    }

    /**
     * Returns detailed information for the given group.
     *
     * @param group group
     * @return detailed group information
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if a group with the given id doesn't exist
     */
    public FullGroup getGroup(Group group)
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = get(getRelativeURL("groups/" + enc(group.getId())));

        handleErrors(result);

        if (result.body.isBlank()) {
            throw new EmptyResponseException(
                    "GET request 'groups/" + enc(group.getId()) + "' returned an unexpected empty reponse");
        }

        FullGroup fullGroup = safeFromJson(result.body, FullGroup.class);
        fullGroup.setId(group.getId());
        return fullGroup;
    }

    /**
     * Changes the name of the group and returns the new name.
     * A number will be appended to duplicate names, which may result in a new name exceeding 32 characters.
     *
     * @param group group
     * @param name new name [0..32]
     * @return new name
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified group no longer exists
     */
    public String setGroupName(Group group, String name)
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        if (!group.isModifiable()) {
            throw new IllegalArgumentException("Group cannot be modified");
        }

        HueResult result = put(getRelativeURL("groups/" + enc(group.getId())),
                gson.toJson(new SetAttributesRequest(name)));

        handleErrors(result);

        if (result.body.isBlank()) {
            throw new EmptyResponseException(
                    "PUT request 'groups/" + enc(group.getId()) + "' returned an unexpected empty reponse");
        }

        List<SuccessResponse> entries = safeFromJson(result.body, SuccessResponse.GSON_TYPE);
        SuccessResponse response = entries.get(0);

        String groupName = (String) response.success.get("/groups/" + enc(group.getId()) + "/name");
        if (groupName == null) {
            throw new ApiException("Response didn't contain group name.");
        }
        return groupName;
    }

    /**
     * Changes the lights in the group.
     *
     * @param group group
     * @param lights new lights [1..16]
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified group no longer exists
     */
    public void setGroupLights(Group group, List<HueObject> lights)
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        if (!group.isModifiable()) {
            throw new IllegalArgumentException("Group cannot be modified");
        }

        HueResult result = put(getRelativeURL("groups/" + enc(group.getId())),
                gson.toJson(new SetAttributesRequest(lights)));

        handleErrors(result);
    }

    /**
     * Changes the name and the lights of a group and returns the new name.
     *
     * @param group group
     * @param name new name [0..32]
     * @param lights [1..16]
     * @return new name
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified group no longer exists
     */
    public String setGroupAttributes(Group group, String name, List<HueObject> lights)
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        if (!group.isModifiable()) {
            throw new IllegalArgumentException("Group cannot be modified");
        }

        HueResult result = put(getRelativeURL("groups/" + enc(group.getId())),
                gson.toJson(new SetAttributesRequest(name, lights)));

        handleErrors(result);

        if (result.body.isBlank()) {
            throw new EmptyResponseException(
                    "PUT request 'groups/" + enc(group.getId()) + "' returned an unexpected empty reponse");
        }

        List<SuccessResponse> entries = safeFromJson(result.body, SuccessResponse.GSON_TYPE);
        SuccessResponse response = entries.get(0);

        String groupName = (String) response.success.get("/groups/" + enc(group.getId()) + "/name");
        if (groupName == null) {
            throw new ApiException("Response didn't contain group name.");
        }
        return groupName;
    }

    /**
     * Changes the state of a group.
     *
     * @param group group
     * @param update changes to the state
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified group no longer exists
     */
    public CompletableFuture<HueResult> setGroupState(Group group, StateUpdate update) {
        requireAuthentication();

        return putAsync(getRelativeURL("groups/" + enc(group.getId()) + "/action"), update.toJson(),
                update.getMessageDelay());
    }

    /**
     * Delete a group.
     *
     * @param group group
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified group no longer exists
     */
    public void deleteGroup(Group group)
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        if (!group.isModifiable()) {
            throw new IllegalArgumentException("Group cannot be modified");
        }

        HueResult result = delete(getRelativeURL("groups/" + enc(group.getId())));

        handleErrors(result);
    }

    /**
     * Returns a list of schedules on the bridge.
     *
     * @return schedules
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public List<Schedule> getSchedules()
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = get(getRelativeURL("schedules"));

        handleErrors(result);

        if (result.body.isBlank()) {
            throw new EmptyResponseException("GET request 'schedules' returned an unexpected empty reponse");
        }

        Map<String, Schedule> scheduleMap = safeFromJson(result.body, Schedule.GSON_TYPE);
        List<Schedule> schedules = new ArrayList<>();
        scheduleMap.forEach((id, schedule) -> {
            schedule.setId(id);
            schedules.add(schedule);
        });
        return schedules;
    }

    /**
     * Changes a schedule.
     *
     * @param schedule schedule
     * @param update changes
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified schedule no longer exists
     */
    public void setSchedule(Schedule schedule, ScheduleUpdate update)
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = put(getRelativeURL("schedules/" + enc(schedule.getId())), update.toJson());

        handleErrors(result);
    }

    /**
     * Delete a schedule.
     *
     * @param schedule schedule
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the schedule no longer exists
     */
    public void deleteSchedule(Schedule schedule)
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = delete(getRelativeURL("schedules/" + enc(schedule.getId())));

        handleErrors(result);
    }

    /**
     * Returns the list of scenes that are not recyclable.
     *
     * @return all scenes that can be activated
     */
    public List<Scene> getScenes() throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = get(getRelativeURL("scenes"));

        handleErrors(result);

        if (result.body.isBlank()) {
            throw new EmptyResponseException("GET request 'scenes' returned an unexpected empty reponse");
        }

        Map<String, Scene> sceneMap = safeFromJson(result.body, Scene.GSON_TYPE);
        return sceneMap.entrySet().stream()//
                .map(e -> {
                    e.getValue().setId(e.getKey());
                    return e.getValue();
                })//
                .filter(scene -> !scene.isRecycle())//
                .sorted(Comparator.comparing(Scene::extractKeyForComparator))//
                .collect(Collectors.toList());
    }

    /**
     * Activate scene to all lights that belong to the scene.
     *
     * @param id the scene to be activated
     * @throws IOException if the bridge cannot be reached
     */
    public CompletableFuture<HueResult> recallScene(String id) {
        Group allLightsGroup = new Group();
        return setGroupState(allLightsGroup, new StateUpdate().setScene(id));
    }

    /**
     * Authenticate on the bridge as the specified user.
     * This function verifies that the specified username is valid and will use
     * it for subsequent requests if it is, otherwise an UnauthorizedException
     * is thrown and the internal username is not changed.
     *
     * @param username username to authenticate
     * @throws ConfigurationException thrown on ssl failure
     * @throws UnauthorizedException thrown if authentication failed
     */
    public void authenticate(String username)
            throws IOException, ApiException, ConfigurationException, UnauthorizedException {
        try {
            this.username = username;
            getLights();
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            this.username = null;
            throw new UnauthorizedException(e.toString());
        }
    }

    /**
     * Link with bridge using the specified username and device type.
     *
     * @param username username for new user [10..40]
     * @param devicetype identifier of application [0..40]
     * @throws LinkButtonException thrown if the bridge button has not been pressed
     */
    public void link(String username, String devicetype)
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        this.username = link(new CreateUserRequest(username, devicetype));
    }

    /**
     * Link with bridge using the specified device type. A random valid username will be generated by the bridge and
     * returned.
     *
     * @return new random username generated by bridge
     * @param devicetype identifier of application [0..40]
     * @throws LinkButtonException thrown if the bridge button has not been pressed
     */
    public String link(String devicetype)
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        return (this.username = link(new CreateUserRequest(devicetype)));
    }

    private String link(CreateUserRequest request)
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        if (this.username != null) {
            throw new IllegalStateException("already linked");
        }

        HueResult result = post(getRelativeURL(""), gson.toJson(request));

        handleErrors(result);

        if (result.body.isBlank()) {
            throw new EmptyResponseException("POST request (link) returned an unexpected empty reponse");
        }

        List<SuccessResponse> entries = safeFromJson(result.body, SuccessResponse.GSON_TYPE);
        SuccessResponse response = entries.get(0);

        String username = (String) response.success.get("username");
        if (username == null) {
            throw new ApiException("Response didn't contain username");
        }
        return username;
    }

    /**
     * Returns bridge configuration.
     *
     * @see Config
     * @return bridge configuration
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public Config getConfig() throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = get(getRelativeURL("config"));

        handleErrors(result);

        if (result.body.isBlank()) {
            throw new EmptyResponseException("GET request 'config' returned an unexpected empty reponse");
        }

        return safeFromJson(result.body, Config.class);
    }

    /**
     * Change the configuration of the bridge.
     *
     * @param update changes to the configuration
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public void setConfig(ConfigUpdate update)
            throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = put(getRelativeURL("config"), update.toJson());

        handleErrors(result);
    }

    /**
     * Unlink the current user from the bridge.
     *
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public void unlink() throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = delete(getRelativeURL("config/whitelist/" + enc(username)));

        handleErrors(result);
    }

    /**
     * Returns the entire bridge configuration.
     * This request is rather resource intensive for the bridge,
     * don't use it more often than necessary. Prefer using requests for
     * specific information your app needs.
     *
     * @return full bridge configuration
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public FullConfig getFullConfig() throws IOException, ApiException, ConfigurationException, CommunicationException {
        requireAuthentication();

        HueResult result = get(getRelativeURL(""));

        handleErrors(result);

        if (result.body.isBlank()) {
            throw new EmptyResponseException("GET request (getFullConfig) returned an unexpected empty reponse");
        }

        FullConfig fullConfig = gson.fromJson(result.body, FullConfig.class);
        return Objects.requireNonNull(fullConfig);
    }

    // Used as assert in requests that require authentication
    private void requireAuthentication() {
        if (this.username == null) {
            throw new IllegalStateException("linking is required before interacting with the bridge");
        }
    }

    // Methods that convert gson exceptions into ApiExceptions
    private <T> T safeFromJson(String json, Type typeOfT) throws ApiException {
        try {
            return gson.fromJson(json, typeOfT);
        } catch (JsonParseException e) {
            throw new ApiException("API returned unexpected result: " + e.getMessage());
        }
    }

    private <T> T safeFromJson(String json, Class<T> classOfT) throws ApiException {
        try {
            return gson.fromJson(json, classOfT);
        } catch (JsonParseException e) {
            throw new ApiException("API returned unexpected result: " + e.getMessage());
        }
    }

    // Used as assert in all requests to elegantly catch common errors
    public void handleErrors(HueResult result) throws IOException, ApiException {
        if (result.responseCode != HttpStatus.OK_200) {
            throw new IOException();
        } else {
            try {
                List<ErrorResponse> errors = gson.fromJson(result.body, ErrorResponse.GSON_TYPE);
                if (errors == null) {
                    return;
                }

                for (ErrorResponse error : errors) {
                    if (error.getType() == null) {
                        continue;
                    }

                    switch (error.getType()) {
                        case 1:
                            username = null;
                            throw new UnauthorizedException(error.getDescription());
                        case 3:
                            throw new EntityNotAvailableException(error.getDescription());
                        case 7:
                            throw new InvalidCommandException(error.getDescription());
                        case 101:
                            throw new LinkButtonException(error.getDescription());
                        case 201:
                            throw new DeviceOffException(error.getDescription());
                        case 301:
                            throw new GroupTableFullException(error.getDescription());
                        default:
                            throw new ApiException(error.getDescription());
                    }
                }
            } catch (JsonParseException e) {
                // Not an error
            }
        }
    }

    // UTF-8 URL encode
    private String enc(@Nullable String str) {
        return str == null ? "" : URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    private String getRelativeURL(String path) {
        String relativeUrl = baseUrl;
        if (username != null) {
            relativeUrl += "/" + enc(username);
        }
        return path.isEmpty() ? relativeUrl : relativeUrl + "/" + path;
    }

    public HueResult get(String address) throws ConfigurationException, CommunicationException {
        return doNetwork(address, HttpMethod.GET);
    }

    public HueResult post(String address, String body) throws ConfigurationException, CommunicationException {
        return doNetwork(address, HttpMethod.POST, body);
    }

    public HueResult put(String address, String body) throws ConfigurationException, CommunicationException {
        return doNetwork(address, HttpMethod.PUT, body);
    }

    public HueResult delete(String address) throws ConfigurationException, CommunicationException {
        return doNetwork(address, HttpMethod.DELETE);
    }

    private HueResult doNetwork(String address, HttpMethod requestMethod)
            throws ConfigurationException, CommunicationException {
        return doNetwork(address, requestMethod, null);
    }

    private HueResult doNetwork(String address, HttpMethod requestMethod, @Nullable String body)
            throws ConfigurationException, CommunicationException {
        logger.trace("Hue request: {} - URL = '{}'", requestMethod, address);
        try {
            final Request request = httpClient.newRequest(address).method(requestMethod).timeout(timeout,
                    TimeUnit.MILLISECONDS);

            if (body != null) {
                logger.trace("Hue request body: '{}'", body);
                request.content(new StringContentProvider(body), "application/json");
            }

            final ContentResponse contentResponse = request.send();

            final int httpStatus = contentResponse.getStatus();
            final String content = contentResponse.getContentAsString();
            logger.trace("Hue response: status = {}, content = '{}'", httpStatus, content);
            return new HueResult(content, httpStatus);
        } catch (ExecutionException e) {
            String message = e.getMessage();
            if (e.getCause() instanceof SSLHandshakeException) {
                logger.debug("SSLHandshakeException occurred during execution: {}", message, e);
                throw new ConfigurationException(TEXT_OFFLINE_CONFIGURATION_ERROR_INVALID_SSL_CERIFICATE, e.getCause());
            } else {
                logger.debug("ExecutionException occurred during execution: {}", message, e);
                throw new CommunicationException(message == null ? TEXT_OFFLINE_COMMUNICATION_ERROR : message,
                        e.getCause());
            }
        } catch (TimeoutException e) {
            String message = e.getMessage();
            logger.debug("TimeoutException occurred during execution: {}", message, e);
            throw new CommunicationException(message == null ? TEXT_OFFLINE_COMMUNICATION_ERROR : message);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String message = e.getMessage();
            logger.debug("InterruptedException occurred during execution: {}", message, e);
            throw new CommunicationException(message == null ? TEXT_OFFLINE_COMMUNICATION_ERROR : message);
        }
    }

    private CompletableFuture<HueResult> putAsync(String address, String body, long delay) {
        AsyncPutParameters asyncPutParameters = new AsyncPutParameters(address, body, delay);
        synchronized (commandsQueue) {
            if (commandsQueue.isEmpty()) {
                commandsQueue.offer(asyncPutParameters);
                Future<?> localJob = job;
                if (localJob == null || localJob.isDone()) {
                    job = scheduler.submit(this::executeCommands);
                }
            } else {
                commandsQueue.offer(asyncPutParameters);
            }
        }
        return asyncPutParameters.future;
    }

    private void executeCommands() {
        while (true) {
            try {
                long delayTime = 0;
                synchronized (commandsQueue) {
                    AsyncPutParameters payloadCallbackPair = commandsQueue.poll();
                    if (payloadCallbackPair != null) {
                        logger.debug("Async sending put to address: {} delay: {} body: {}", payloadCallbackPair.address,
                                payloadCallbackPair.delay, payloadCallbackPair.body);
                        try {
                            HueResult result = doNetwork(payloadCallbackPair.address, HttpMethod.PUT,
                                    payloadCallbackPair.body);
                            payloadCallbackPair.future.complete(result);
                        } catch (ConfigurationException | CommunicationException e) {
                            payloadCallbackPair.future.completeExceptionally(e);
                        }
                        delayTime = payloadCallbackPair.delay;
                    } else {
                        return;
                    }
                }
                Thread.sleep(delayTime);
            } catch (InterruptedException e) {
                logger.debug("commandExecutorThread was interrupted", e);
            }
        }
    }

    public static class HueResult {
        public final String body;
        public final int responseCode;

        public HueResult(String body, int responseCode) {
            this.body = body;
            this.responseCode = responseCode;
        }
    }

    public final class AsyncPutParameters {
        public final String address;
        public final String body;
        public final CompletableFuture<HueResult> future;
        public final long delay;

        public AsyncPutParameters(String address, String body, long delay) {
            this.address = address;
            this.body = body;
            this.future = new CompletableFuture<>();
            this.delay = delay;
        }
    }
}
