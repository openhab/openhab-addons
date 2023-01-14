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
package org.openhab.binding.iaqualink.internal.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.iaqualink.internal.api.dto.AccountInfo;
import org.openhab.binding.iaqualink.internal.api.dto.Auxiliary;
import org.openhab.binding.iaqualink.internal.api.dto.Device;
import org.openhab.binding.iaqualink.internal.api.dto.Home;
import org.openhab.binding.iaqualink.internal.api.dto.OneTouch;
import org.openhab.binding.iaqualink.internal.api.dto.SignIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

/**
 * IAqualink HTTP Client
 *
 * The {@link IAqualinkClient} provides basic HTTP commands to control and monitor an iAquaLink
 * based system.
 *
 * GSON is used to provide custom deserialization on the JSON results. These results
 * unfortunately are not returned as normalized JSON objects and require complex deserialization
 * handlers.
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
@NonNullByDefault
public class IAqualinkClient {
    private final Logger logger = LoggerFactory.getLogger(IAqualinkClient.class);

    private static final String HEADER_AGENT = "iAqualink/98 CFNetwork/978.0.7 Darwin/18.6.0";
    private static final String HEADER_ACCEPT = "*/*";
    private static final String HEADER_ACCEPT_LANGUAGE = "en-us";
    private static final String HEADER_ACCEPT_ENCODING = "br, gzip, deflate";

    private static final String AUTH_URL = "https://prod.zodiac-io.com/users/v1/login";
    private static final String DEVICES_URL = "https://r-api.iaqualink.net/devices.json";
    private static final String IAQUALINK_BASE_URL = "https://p-api.iaqualink.net/v1/mobile/session.json";

    private Gson gson = new GsonBuilder().registerTypeAdapter(Home.class, new HomeDeserializer())
            .registerTypeAdapter(OneTouch[].class, new OneTouchDeserializer())
            .registerTypeAdapter(Auxiliary[].class, new AuxDeserializer())
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    private Gson gsonInternal = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();
    private HttpClient httpClient;

    @SuppressWarnings("serial")
    public class NotAuthorizedException extends Exception {
        public NotAuthorizedException(String message) {
            super(message);
        }
    }

    /**
     *
     * @param httpClient
     */
    public IAqualinkClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Initial login to service
     *
     * @param username
     * @param password
     * @param apiKey
     * @return
     * @throws IOException
     * @throws NotAuthorizedException
     */
    public AccountInfo login(@Nullable String username, @Nullable String password, @Nullable String apiKey)
            throws IOException, NotAuthorizedException {
        String signIn = gson.toJson(new SignIn(apiKey, username, password)).toString();
        try {
            ContentResponse response = httpClient.newRequest(AUTH_URL).method(HttpMethod.POST)
                    .content(new StringContentProvider(signIn), "application/json").send();
            if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                throw new NotAuthorizedException(response.getReason());
            }
            if (response.getStatus() != HttpStatus.OK_200) {
                throw new IOException(response.getReason());
            }
            return Objects.requireNonNull(gson.fromJson(response.getContentAsString(), AccountInfo.class));
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    /**
     * List all devices (pools) registered to an account
     *
     * @param apiKey
     * @param token
     * @param id
     * @return {@link Device[]}
     * @throws IOException
     * @throws NotAuthorizedException
     */
    public Device[] getDevices(@Nullable String apiKey, @Nullable String token, int id)
            throws IOException, NotAuthorizedException {
        return getAqualinkObject(UriBuilder.fromUri(DEVICES_URL). //
                queryParam("api_key", apiKey). //
                queryParam("authentication_token", token). //
                queryParam("user_id", id).build(), Device[].class);
    }

    /**
     * Retrieves the HomeScreen
     *
     * @param serialNumber
     * @param sessionId
     * @return {@link Home}
     * @throws IOException
     * @throws NotAuthorizedException
     */
    public Home getHome(@Nullable String serialNumber, @Nullable String sessionId)
            throws IOException, NotAuthorizedException {
        return homeScreenCommand(serialNumber, sessionId, "get_home");
    }

    /**
     * Retrieves {@link OneTouch[]} macros
     *
     * @param serialNumber
     * @param sessionId
     * @return {@link OneTouch[]}
     * @throws IOException
     * @throws NotAuthorizedException
     */
    public OneTouch[] getOneTouch(@Nullable String serialNumber, @Nullable String sessionId)
            throws IOException, NotAuthorizedException {
        return oneTouchCommand(serialNumber, sessionId, "get_onetouch");
    }

    /**
     * Retrieves {@link Auxiliary[]} devices
     *
     * @param serialNumber
     * @param sessionId
     * @return {@link Auxiliary[]}
     * @throws IOException
     * @throws NotAuthorizedException
     */
    public Auxiliary[] getAux(@Nullable String serial, @Nullable String sessionID)
            throws IOException, NotAuthorizedException {
        return auxCommand(serial, sessionID, "get_devices");
    }

    /**
     * Sends a HomeScreen Set command
     *
     * @param serial
     * @param sessionID
     * @param homeElementID
     * @return
     * @throws IOException
     * @throws NotAuthorizedException
     */
    public Home homeScreenSetCommand(@Nullable String serial, @Nullable String sessionID, String homeElementID)
            throws IOException, NotAuthorizedException {
        return homeScreenCommand(serial, sessionID, "set_" + homeElementID);
    }

    /**
     * Sends an Auxiliary Set command
     *
     * @param serial
     * @param sessionID
     * @param auxID
     * @return
     * @throws IOException
     * @throws NotAuthorizedException
     */
    public Auxiliary[] auxSetCommand(@Nullable String serial, @Nullable String sessionID, String auxID)
            throws IOException, NotAuthorizedException {
        return auxCommand(serial, sessionID, "set_" + auxID);
    }

    /**
     * Sends an Auxiliary light command
     *
     * @param serialNumber
     * @param sessionId
     * @param command
     * @param lightValue
     * @return
     * @throws IOException
     * @throws NotAuthorizedException
     */
    public Auxiliary[] lightCommand(@Nullable String serial, @Nullable String sessionID, String auxID,
            String lightValue, String subType) throws IOException, NotAuthorizedException {
        return getAqualinkObject(baseURI(). //
                queryParam("aux", auxID). //
                queryParam("command", "set_light"). //
                queryParam("light", lightValue). //
                queryParam("serial", serial). //
                queryParam("subtype", subType). //
                queryParam("sessionID", sessionID).build(), Auxiliary[].class);
    }

    /**
     * Sends an Auxiliary dimmer command
     *
     * @param serialNumber
     * @param sessionId
     * @param auxId
     * @param lightValue
     * @return
     * @throws IOException
     * @throws NotAuthorizedException
     */
    public Auxiliary[] dimmerCommand(@Nullable String serial, @Nullable String sessionID, String auxID, String level)
            throws IOException, NotAuthorizedException {
        return getAqualinkObject(baseURI().queryParam("aux", auxID). //
                queryParam("command", "set_dimmer"). //
                queryParam("level", level). //
                queryParam("serial", serial). //
                queryParam("sessionID", sessionID).build(), Auxiliary[].class);
    }

    /**
     * Sets the Spa Temperature Setpoint
     *
     * @param serialNumber
     * @param sessionId
     * @param spaSetpoint
     * @return
     * @throws IOException
     * @throws NotAuthorizedException
     */
    public Home setSpaTemp(@Nullable String serial, @Nullable String sessionID, float spaSetpoint)
            throws IOException, NotAuthorizedException {
        return getAqualinkObject(baseURI(). //
                queryParam("command", "set_temps"). //
                queryParam("temp1", spaSetpoint). //
                queryParam("serial", serial). //
                queryParam("sessionID", sessionID).build(), Home.class);
    }

    /**
     * Sets the Pool Temperature Setpoint
     *
     * @param serialNumber
     * @param sessionId
     * @param poolSetpoint
     * @return
     * @throws IOException
     * @throws NotAuthorizedException
     */
    public Home setPoolTemp(@Nullable String serial, @Nullable String sessionID, float poolSetpoint)
            throws IOException, NotAuthorizedException {
        return getAqualinkObject(baseURI(). //
                queryParam("command", "set_temps"). //
                queryParam("temp2", poolSetpoint). //
                queryParam("serial", serial). //
                queryParam("sessionID", sessionID).build(), Home.class);
    }

    /**
     * Sends a OneTouch set command
     *
     * @param serial
     * @param sessionID
     * @param oneTouchID
     * @return
     * @throws IOException
     * @throws NotAuthorizedException
     */
    public OneTouch[] oneTouchSetCommand(@Nullable String serial, @Nullable String sessionID, String oneTouchID)
            throws IOException, NotAuthorizedException {
        return oneTouchCommand(serial, sessionID, "set_" + oneTouchID);
    }

    private Home homeScreenCommand(@Nullable String serial, @Nullable String sessionID, String command)
            throws IOException, NotAuthorizedException {
        return getAqualinkObject(baseURI().queryParam("command", command). //
                queryParam("serial", serial). //
                queryParam("sessionID", sessionID).build(), Home.class);
    }

    private Auxiliary[] auxCommand(@Nullable String serial, @Nullable String sessionID, String command)
            throws IOException, NotAuthorizedException {
        return getAqualinkObject(baseURI(). //
                queryParam("command", command). //
                queryParam("serial", serial). //
                queryParam("sessionID", sessionID).build(), Auxiliary[].class);
    }

    private OneTouch[] oneTouchCommand(@Nullable String serial, @Nullable String sessionID, String command)
            throws IOException, NotAuthorizedException {
        return getAqualinkObject(baseURI().queryParam("command", command). //
                queryParam("serial", serial). //
                queryParam("sessionID", sessionID).build(), OneTouch[].class);
    }

    private UriBuilder baseURI() {
        return UriBuilder.fromUri(IAQUALINK_BASE_URL).queryParam("actionID", "command");
    }

    /**
     *
     * @param <T>
     * @param url
     * @param typeOfT
     * @return
     * @throws IOException
     * @throws NotAuthorizedException
     */
    private <T> T getAqualinkObject(URI uri, Type typeOfT) throws IOException, NotAuthorizedException {
        return Objects.requireNonNull(gson.fromJson(getRequest(uri), typeOfT));
    }

    /**
     *
     * @param url
     * @return
     * @throws IOException
     * @throws NotAuthorizedException
     */
    private String getRequest(URI uri) throws IOException, NotAuthorizedException {
        try {
            logger.trace("Trying {}", uri);
            ContentResponse response = httpClient.newRequest(uri).method(HttpMethod.GET) //
                    .agent(HEADER_AGENT) //
                    .header(HttpHeader.ACCEPT_LANGUAGE, HEADER_ACCEPT_LANGUAGE) //
                    .header(HttpHeader.ACCEPT_ENCODING, HEADER_ACCEPT_ENCODING) //
                    .header(HttpHeader.ACCEPT, HEADER_ACCEPT) //
                    .send();
            logger.trace("Response {}", response);
            if (response.getStatus() == HttpStatus.UNAUTHORIZED_401) {
                throw new NotAuthorizedException(response.getReason());
            }
            if (response.getStatus() != HttpStatus.OK_200) {
                throw new IOException(response.getReason());
            }
            return response.getContentAsString();
        } catch (InterruptedException | TimeoutException | ExecutionException | JsonParseException e) {
            throw new IOException(e);
        }
    }

    /////////////// .........Here be dragons...../////////////////////////

    class HomeDeserializer implements JsonDeserializer<Home> {
        @Override
        public @Nullable Home deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            JsonArray homeScreen = jsonObject.getAsJsonArray("home_screen");
            JsonObject home = new JsonObject();
            JsonObject serializedMap = new JsonObject();
            if (homeScreen != null) {
                homeScreen.forEach(element -> {
                    element.getAsJsonObject().entrySet().forEach(entry -> {
                        JsonElement value = entry.getValue();
                        home.add(entry.getKey(), value);
                        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                            serializedMap.add(entry.getKey(), value);
                        }
                    });
                });
                home.add("serialized_map", serializedMap);
                return gsonInternal.fromJson(home, Home.class);
            }
            throw new JsonParseException("Invalid structure for Home class");
        }
    }

    class OneTouchDeserializer implements JsonDeserializer<OneTouch[]> {
        @Override
        public OneTouch @Nullable [] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            JsonArray oneTouchScreen = jsonObject.getAsJsonArray("onetouch_screen");
            List<OneTouch> list = new ArrayList<>();
            if (oneTouchScreen != null) {
                oneTouchScreen.forEach(oneTouchScreenElement -> {
                    oneTouchScreenElement.getAsJsonObject().entrySet().forEach(oneTouchScreenEntry -> {
                        if (oneTouchScreenEntry.getKey().startsWith("onetouch_")) {
                            JsonArray oneTouchArray = oneTouchScreenEntry.getValue().getAsJsonArray();
                            if (oneTouchArray != null) {
                                JsonObject oneTouchJson = new JsonObject();
                                oneTouchJson.add("name", new JsonPrimitive(oneTouchScreenEntry.getKey()));
                                oneTouchArray.forEach(arrayElement -> {
                                    arrayElement.getAsJsonObject().entrySet().forEach(oneTouchEntry -> {
                                        oneTouchJson.add(oneTouchEntry.getKey(), oneTouchEntry.getValue());
                                    });
                                });
                                list.add(Objects.requireNonNull(gsonInternal.fromJson(oneTouchJson, OneTouch.class)));
                            }
                        }
                    });
                });
            }
            return list.toArray(new OneTouch[list.size()]);
        }
    }

    class AuxDeserializer implements JsonDeserializer<Auxiliary[]> {
        @Override
        public Auxiliary @Nullable [] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            JsonArray auxScreen = jsonObject.getAsJsonArray("devices_screen");
            List<Auxiliary> list = new ArrayList<>();
            if (auxScreen != null) {
                auxScreen.forEach(auxElement -> {
                    auxElement.getAsJsonObject().entrySet().forEach(auxScreenEntry -> {
                        if (auxScreenEntry.getKey().startsWith("aux_")) {
                            JsonArray auxArray = auxScreenEntry.getValue().getAsJsonArray();
                            if (auxArray != null) {
                                JsonObject auxJson = new JsonObject();
                                auxJson.add("name", new JsonPrimitive(auxScreenEntry.getKey()));
                                auxArray.forEach(arrayElement -> {
                                    arrayElement.getAsJsonObject().entrySet().forEach(auxEntry -> {
                                        auxJson.add(auxEntry.getKey(), auxEntry.getValue());
                                    });
                                });
                                list.add(Objects.requireNonNull(gsonInternal.fromJson(auxJson, Auxiliary.class)));
                            }
                        }
                    });
                });
            }
            return list.toArray(new Auxiliary[list.size()]);
        }
    }
}
