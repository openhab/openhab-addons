/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.HttpClient.Result;
import org.openhab.binding.hue.internal.exceptions.ApiException;
import org.openhab.binding.hue.internal.exceptions.DeviceOffException;
import org.openhab.binding.hue.internal.exceptions.EntityNotAvailableException;
import org.openhab.binding.hue.internal.exceptions.GroupTableFullException;
import org.openhab.binding.hue.internal.exceptions.InvalidCommandException;
import org.openhab.binding.hue.internal.exceptions.LinkButtonException;
import org.openhab.binding.hue.internal.exceptions.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Representation of a connection with a Hue bridge.
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

    private final String ip;
    private final String baseUrl;
    private @Nullable String username;

    private final Gson gson = new GsonBuilder().setDateFormat(DATE_FORMAT).create();
    private HttpClient http = new HttpClient();
    private final ScheduledExecutorService scheduler;

    @Nullable
    private Config cachedConfig;

    /**
     * Connect with a bridge as a new user.
     *
     * @param ip ip address of bridge
     * @param port port of bridge
     * @param protocol protocol to connect to the bridge
     */
    public HueBridge(String ip, int port, String protocol, ScheduledExecutorService scheduler) {
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
     * @param ip ip address of bridge
     * @param port port of bridge
     * @param protocol protocol to connect to the bridge
     * @param username username to authenticate with
     */
    public HueBridge(String ip, int port, String protocol, String username, ScheduledExecutorService scheduler)
            throws IOException, ApiException {
        this(ip, port, protocol, scheduler);
        authenticate(username);
    }

    /**
     * Test constructor
     */
    HueBridge(String ip, String baseUrl, String username, ScheduledExecutorService scheduler, HttpClient http) {
        this.ip = ip;
        this.baseUrl = baseUrl;
        this.username = username;
        this.scheduler = scheduler;
        this.http = http;
    }

    /**
     * Set the connect and read timeout for HTTP requests.
     *
     * @param timeout timeout in milliseconds or 0 for indefinitely
     */
    public void setTimeout(int timeout) {
        http.setTimeout(timeout);
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
        if (this.cachedConfig == null) {
            this.cachedConfig = getConfig();
        }

        return Objects.requireNonNull(this.cachedConfig);
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

    private <T extends HueObject> List<T> getTypedLights(Type gsonType) throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL("lights"));

        handleErrors(result);

        Map<String, T> lightMap = safeFromJson(result.getBody(), gsonType);
        ArrayList<T> lightList = new ArrayList<>();

        for (String id : lightMap.keySet()) {
            @Nullable
            T light = lightMap.get(id);
            if (light != null) {
                light.setId(id);
                lightList.add(light);
            }
        }

        return lightList;
    }

    /**
     * Returns a list of sensors known to the bridge
     *
     * @return list of sensors
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public List<FullSensor> getSensors() throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL("sensors"));

        handleErrors(result);

        Map<String, FullSensor> sensorMap = safeFromJson(result.getBody(), FullSensor.GSON_TYPE);
        ArrayList<FullSensor> sensorList = new ArrayList<>();

        for (String id : sensorMap.keySet()) {
            FullSensor sensor = sensorMap.get(id);
            sensor.setId(id);
            sensorList.add(sensor);
        }

        return sensorList;
    }

    /**
     * Returns the last time a search for new lights was started.
     * If a search is currently running, the current time will be
     * returned or null if a search has never been started.
     *
     * @return last search time
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public @Nullable Date getLastSearch() throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL("lights/new"));

        handleErrors(result);

        String lastScan = safeFromJson(result.getBody(), NewLightsResponse.class).lastscan;

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
    public void startSearch() throws IOException, ApiException {
        requireAuthentication();

        Result result = http.post(getRelativeURL("lights"), "");

        handleErrors(result);
    }

    /**
     * Start searching for new lights with given serial numbers for 1 minute.
     * A maximum amount of 15 new lights will be added.
     *
     * @param serialNumbers list of serial numbers
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public void startSearch(List<String> serialNumbers) throws IOException, ApiException {
        requireAuthentication();

        String body = gson.toJson(new SearchForLightsRequest(serialNumbers));
        Result result = http.post(getRelativeURL("lights"), body);

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
    public FullHueObject getLight(HueObject light) throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL("lights/" + enc(light.getId())));

        handleErrors(result);

        FullHueObject fullLight = safeFromJson(result.getBody(), FullLight.class);
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
    public String setLightName(HueObject light, String name) throws IOException, ApiException {
        requireAuthentication();

        String body = gson.toJson(new SetAttributesRequest(name));
        Result result = http.put(getRelativeURL("lights/" + enc(light.getId())), body);

        handleErrors(result);

        List<SuccessResponse> entries = safeFromJson(result.getBody(), SuccessResponse.GSON_TYPE);
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
    public CompletableFuture<Result> setLightState(FullLight light, StateUpdate update) {
        requireAuthentication();

        String body = update.toJson();
        return http.putAsync(getRelativeURL("lights/" + enc(light.getId()) + "/state"), body, update.getMessageDelay(),
                scheduler);
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
    public CompletableFuture<Result> setSensorState(FullSensor sensor, StateUpdate update) {
        requireAuthentication();

        String body = update.toJson();
        return http.putAsync(getRelativeURL("sensors/" + enc(sensor.getId()) + "/state"), body,
                update.getMessageDelay(), scheduler);
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
    public CompletableFuture<Result> updateSensorConfig(FullSensor sensor, ConfigUpdate update) {
        requireAuthentication();

        String body = update.toJson();
        return http.putAsync(getRelativeURL("sensors/" + enc(sensor.getId()) + "/config"), body,
                update.getMessageDelay(), scheduler);
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
    public List<FullGroup> getGroups() throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL("groups"));

        handleErrors(result);

        Map<String, FullGroup> groupMap = safeFromJson(result.getBody(), FullGroup.GSON_TYPE);
        ArrayList<FullGroup> groupList = new ArrayList<>();

        if (groupMap.get("0") == null) {
            // Group 0 is not returned, we create it as in fact it exists
            try {
                groupList.add(getGroup(getAllGroup()));
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
            groupList.add(group);
        });

        return groupList;
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
    public Group createGroup(List<HueObject> lights) throws IOException, ApiException {
        requireAuthentication();

        String body = gson.toJson(new SetAttributesRequest(lights));
        Result result = http.post(getRelativeURL("groups"), body);

        handleErrors(result);

        List<SuccessResponse> entries = safeFromJson(result.getBody(), SuccessResponse.GSON_TYPE);
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
    public Group createGroup(String name, List<HueObject> lights) throws IOException, ApiException {
        requireAuthentication();

        String body = gson.toJson(new SetAttributesRequest(name, lights));
        Result result = http.post(getRelativeURL("groups"), body);

        handleErrors(result);

        List<SuccessResponse> entries = safeFromJson(result.getBody(), SuccessResponse.GSON_TYPE);
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
    public FullGroup getGroup(Group group) throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL("groups/" + enc(group.getId())));

        handleErrors(result);

        FullGroup fullGroup = safeFromJson(result.getBody(), FullGroup.class);
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
    public String setGroupName(Group group, String name) throws IOException, ApiException {
        requireAuthentication();

        if (!group.isModifiable()) {
            throw new IllegalArgumentException("Group cannot be modified");
        }

        String body = gson.toJson(new SetAttributesRequest(name));
        Result result = http.put(getRelativeURL("groups/" + enc(group.getId())), body);

        handleErrors(result);

        List<SuccessResponse> entries = safeFromJson(result.getBody(), SuccessResponse.GSON_TYPE);
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
    public void setGroupLights(Group group, List<HueObject> lights) throws IOException, ApiException {
        requireAuthentication();

        if (!group.isModifiable()) {
            throw new IllegalArgumentException("Group cannot be modified");
        }

        String body = gson.toJson(new SetAttributesRequest(lights));
        Result result = http.put(getRelativeURL("groups/" + enc(group.getId())), body);

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
            throws IOException, ApiException {
        requireAuthentication();

        if (!group.isModifiable()) {
            throw new IllegalArgumentException("Group cannot be modified");
        }

        String body = gson.toJson(new SetAttributesRequest(name, lights));
        Result result = http.put(getRelativeURL("groups/" + enc(group.getId())), body);

        handleErrors(result);

        List<SuccessResponse> entries = safeFromJson(result.getBody(), SuccessResponse.GSON_TYPE);
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
    public CompletableFuture<Result> setGroupState(Group group, StateUpdate update) {
        requireAuthentication();

        String body = update.toJson();
        return http.putAsync(getRelativeURL("groups/" + enc(group.getId()) + "/action"), body, update.getMessageDelay(),
                scheduler);
    }

    /**
     * Delete a group.
     *
     * @param group group
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified group no longer exists
     */
    public void deleteGroup(Group group) throws IOException, ApiException {
        requireAuthentication();

        if (!group.isModifiable()) {
            throw new IllegalArgumentException("Group cannot be modified");
        }

        Result result = http.delete(getRelativeURL("groups/" + enc(group.getId())));

        handleErrors(result);
    }

    /**
     * Returns a list of schedules on the bridge.
     *
     * @return schedules
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public List<Schedule> getSchedules() throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL("schedules"));

        handleErrors(result);

        Map<String, Schedule> scheduleMap = safeFromJson(result.getBody(), Schedule.GSON_TYPE);

        ArrayList<Schedule> scheduleList = new ArrayList<>();

        for (String id : scheduleMap.keySet()) {
            Schedule schedule = scheduleMap.get(id);
            schedule.setId(id);
            scheduleList.add(schedule);
        }

        return scheduleList;
    }

    /**
     * Schedules a new command to be run at the specified time.
     * To select the command for the new schedule, simply run it
     * as you normally would in the callback. Instead of it running
     * immediately, it will be scheduled to run at the specified time.
     * It will automatically fail with an IOException, because there
     * will be no response. Note that GET methods cannot be scheduled,
     * so those will still run and return results immediately.
     *
     * @param time time to run command
     * @param callback callback in which the command is specified
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws InvalidCommandException thrown if the scheduled command is larger than 90 bytes or otherwise invalid
     */
    public void createSchedule(Date time, ScheduleCallback callback) throws IOException, ApiException {
        createSchedule(null, null, time, callback);
    }

    /**
     * Schedules a new command to be run at the specified time.
     * To select the command for the new schedule, simply run it
     * as you normally would in the callback. Instead of it running
     * immediately, it will be scheduled to run at the specified time.
     * It will automatically fail with an IOException, because there
     * will be no response. Note that GET methods cannot be scheduled,
     * so those will still run and return results immediately.
     *
     * @param name name [0..32]
     * @param time time to run command
     * @param callback callback in which the command is specified
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws InvalidCommandException thrown if the scheduled command is larger than 90 bytes or otherwise invalid
     */
    public void createSchedule(String name, Date time, ScheduleCallback callback) throws IOException, ApiException {
        createSchedule(name, null, time, callback);
    }

    /**
     * Schedules a new command to be run at the specified time.
     * To select the command for the new schedule, simply run it
     * as you normally would in the callback. Instead of it running
     * immediately, it will be scheduled to run at the specified time.
     * It will automatically fail with an IOException, because there
     * will be no response. Note that GET methods cannot be scheduled,
     * so those will still run and return results immediately.
     *
     * @param name name [0..32]
     * @param description description [0..64]
     * @param time time to run command
     * @param callback callback in which the command is specified
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws InvalidCommandException thrown if the scheduled command is larger than 90 bytes or otherwise invalid
     */
    public void createSchedule(@Nullable String name, @Nullable String description, Date time,
            ScheduleCallback callback) throws IOException, ApiException {
        requireAuthentication();

        handleCommandCallback(callback);

        String body = gson.toJson(new CreateScheduleRequest(name, description, scheduleCommand, time));
        Result result = http.post(getRelativeURL("schedules"), body);

        handleErrors(result);
    }

    /**
     * Returns detailed information for the given schedule.
     *
     * @param schedule schedule
     * @return detailed schedule information
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified schedule no longer exists
     */
    public FullSchedule getSchedule(Schedule schedule) throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL("schedules/" + enc(schedule.getId())));

        handleErrors(result);

        FullSchedule fullSchedule = safeFromJson(result.getBody(), FullSchedule.class);
        fullSchedule.setId(schedule.getId());
        return fullSchedule;
    }

    /**
     * Changes a schedule.
     *
     * @param schedule schedule
     * @param update changes
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the specified schedule no longer exists
     */
    public void setSchedule(Schedule schedule, ScheduleUpdate update) throws IOException, ApiException {
        requireAuthentication();

        String body = update.toJson();
        Result result = http.put(getRelativeURL("schedules/" + enc(schedule.getId())), body);

        handleErrors(result);
    }

    /**
     * Changes the command of a schedule.
     *
     * @param schedule schedule
     * @param callback callback for new command
     * @see #createSchedule(String, String, Date, ScheduleCallback)
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws InvalidCommandException thrown if the scheduled command is larger than 90 bytes or otherwise invalid
     */
    public void setScheduleCommand(Schedule schedule, ScheduleCallback callback) throws IOException, ApiException {
        requireAuthentication();

        handleCommandCallback(callback);

        String body = gson.toJson(new CreateScheduleRequest(null, null, scheduleCommand, null));
        Result result = http.put(getRelativeURL("schedules/" + enc(schedule.getId())), body);

        handleErrors(result);
    }

    /**
     * Callback to specify a schedule command.
     */
    public interface ScheduleCallback {
        /**
         * Run the command you want to schedule as if you're executing
         * it normally. The request will automatically fail to produce
         * a result by throwing an IOException. Requests that only
         * get data (e.g. getGroups) will still execute immediately,
         * because those cannot be scheduled.
         *
         * @param bridge this bridge for convenience
         * @throws IOException always thrown right after executing a command
         */
        public void onScheduleCommand(HueBridge bridge) throws IOException, ApiException;
    }

    private @Nullable ScheduleCommand scheduleCommand = null;

    private @Nullable ScheduleCommand handleCommandCallback(ScheduleCallback callback) throws ApiException {
        // Temporarily reroute requests to a fake HTTP client
        HttpClient realClient = http;
        http = new HttpClient() {
            @Override
            protected Result doNetwork(String address, String requestMethod, @Nullable String body) throws IOException {
                // GET requests cannot be scheduled, so will continue working normally for convenience
                if ("GET".equals(requestMethod)) {
                    return super.doNetwork(address, requestMethod, body);
                } else {
                    String extractedAddress = Util.quickMatch("^http://[^/]+(.+)$", address);
                    JsonElement commandBody = body == null ? null : JsonParser.parseString(body);
                    scheduleCommand = new ScheduleCommand(extractedAddress, requestMethod, commandBody);

                    // Return a fake result that will cause an exception and the callback to end
                    return new Result("", 405);
                }
            }
        };

        // Run command
        try {
            scheduleCommand = null;
            callback.onScheduleCommand(this);
        } catch (IOException | RuntimeException e) {
            // Command will automatically fail to return a result because of deferred execution
        }
        if (scheduleCommand != null && Util.stringSize(scheduleCommand.getBody()) > 90) {
            throw new InvalidCommandException("Commmand body is larger than 90 bytes");
        }

        // Restore HTTP client
        http = realClient;

        return scheduleCommand;
    }

    /**
     * Delete a schedule.
     *
     * @param schedule schedule
     * @throws UnauthorizedException thrown if the user no longer exists
     * @throws EntityNotAvailableException thrown if the schedule no longer exists
     */
    public void deleteSchedule(Schedule schedule) throws IOException, ApiException {
        requireAuthentication();

        Result result = http.delete(getRelativeURL("schedules/" + enc(schedule.getId())));

        handleErrors(result);
    }

    /**
     * Returns the list of scenes that are not recyclable.
     *
     * @return all scenes that can be activated
     */
    public List<Scene> getScenes() throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL("scenes"));
        handleErrors(result);

        Map<String, Scene> sceneMap = safeFromJson(result.getBody(), Scene.GSON_TYPE);
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
    public CompletableFuture<Result> recallScene(String id) {
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
     * @throws UnauthorizedException thrown if authentication failed
     */
    public void authenticate(String username) throws IOException, ApiException {
        try {
            this.username = username;
            getLights();
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
    public void link(String username, String devicetype) throws IOException, ApiException {
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
    public String link(String devicetype) throws IOException, ApiException {
        return (this.username = link(new CreateUserRequest(devicetype)));
    }

    private String link(CreateUserRequest request) throws IOException, ApiException {
        if (this.username != null) {
            throw new IllegalStateException("already linked");
        }

        String body = gson.toJson(request);
        Result result = http.post(getRelativeURL(""), body);

        handleErrors(result);

        List<SuccessResponse> entries = safeFromJson(result.getBody(), SuccessResponse.GSON_TYPE);
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
    public Config getConfig() throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL("config"));

        handleErrors(result);

        return safeFromJson(result.getBody(), Config.class);
    }

    /**
     * Change the configuration of the bridge.
     *
     * @param update changes to the configuration
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public void setConfig(ConfigUpdate update) throws IOException, ApiException {
        requireAuthentication();

        String body = update.toJson();
        Result result = http.put(getRelativeURL("config"), body);

        handleErrors(result);
    }

    /**
     * Unlink the current user from the bridge.
     *
     * @throws UnauthorizedException thrown if the user no longer exists
     */
    public void unlink() throws IOException, ApiException {
        requireAuthentication();

        Result result = http.delete(getRelativeURL("config/whitelist/" + enc(username)));

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
    public FullConfig getFullConfig() throws IOException, ApiException {
        requireAuthentication();

        Result result = http.get(getRelativeURL(""));

        handleErrors(result);

        FullConfig fullConfig = gson.fromJson(result.getBody(), FullConfig.class);
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
    public void handleErrors(Result result) throws IOException, ApiException {
        if (result.getResponseCode() != 200) {
            throw new IOException();
        } else {
            try {
                List<ErrorResponse> errors = gson.fromJson(result.getBody(), ErrorResponse.GSON_TYPE);
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
        if (str != null) {
            try {
                return URLEncoder.encode(str, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException e) {
                throw new UnsupportedOperationException("UTF-8 not supported");
            }
        } else {
            return "";
        }
    }

    private String getRelativeURL(String path) {
        String relativeUrl = baseUrl;
        if (username != null) {
            relativeUrl += "/" + enc(username);
        }
        return path.isEmpty() ? relativeUrl : relativeUrl + "/" + path;
    }
}
