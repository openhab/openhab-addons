/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tailwind.internal.api;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.tailwind.internal.TailwindBindingConstants;
import org.openhab.binding.tailwind.internal.api.dto.TailwindStatus;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * Sends requests to the local control API of a TailWind controller.
 *
 * The API is described in the
 * <a href="https://github.com/Scott--R/Tailwind_Local_Control_API">Tailwind Local Control API</a>.
 *
 * @author Dave J. Schoepel - Initial contribution
 * @author Christopher Smith - One method per request, typed failures
 */
@NonNullByDefault
public class TailwindApi {
    private static final String TOKEN_HEADER = "TOKEN";
    /** The information a controller gives when it rejects the Local Control Key. */
    private static final String INFO_TOKEN_FAIL = "token check fail";

    private static final int TIMEOUT_SECONDS = 10;
    private static final String VERSION_0_1 = "0.1";
    private static final String VERSION_0_2 = "0.2";
    private static final String TYPE_GET = "get";
    private static final String TYPE_SET = "set";

    private static final Gson GSON = new Gson();

    private final HttpClient httpClient;
    private final String url;
    private final String token;

    public TailwindApi(HttpClient httpClient, String hostname, String token) {
        this.httpClient = httpClient;
        this.url = "http://" + hostname + "/json";
        this.token = token;
    }

    public TailwindStatus getStatus() throws TailwindException {
        return send(VERSION_0_1, TYPE_GET, "dev_st", null);
    }

    /**
     * Makes the controller send its status reports as UDP messages to the given address.
     *
     * @param hostAndPort the address, such as {@code 192.168.1.10:50904}
     */
    public void setNotifyUrl(String hostAndPort) throws TailwindException {
        JsonObject value = new JsonObject();
        value.addProperty("url", hostAndPort);
        value.addProperty("proto", "udp");
        value.addProperty("enable", 1);
        send(VERSION_0_1, TYPE_SET, "notify_url", value);
    }

    public void openDoor(int doorIndex) throws TailwindException {
        send(VERSION_0_1, TYPE_SET, "door_op", doorOperation(doorIndex, TailwindBindingConstants.DOOR_OPEN));
    }

    /**
     * Opens a door partially.
     *
     * @param partialTimeMillis the time the door runs, in milliseconds
     */
    public void openDoorPartially(int doorIndex, long partialTimeMillis) throws TailwindException {
        JsonObject value = doorOperation(doorIndex, TailwindBindingConstants.DOOR_OPEN);
        value.addProperty("partial_time", partialTimeMillis);
        send(VERSION_0_1, TYPE_SET, "door_op", value);
    }

    public void closeDoor(int doorIndex) throws TailwindException {
        send(VERSION_0_1, TYPE_SET, "door_op", doorOperation(doorIndex, TailwindBindingConstants.DOOR_CLOSE));
    }

    /**
     * @param brightness the brightness of the status LED, from 0 to 100
     */
    public void setLedBrightness(int brightness) throws TailwindException {
        JsonObject value = new JsonObject();
        value.addProperty("brightness", brightness);
        send(VERSION_0_1, TYPE_SET, "status_led", value);
    }

    public void identify() throws TailwindException {
        send(VERSION_0_2, TYPE_SET, "identify", null);
    }

    public void reboot() throws TailwindException {
        send(VERSION_0_2, TYPE_SET, "reboot", null);
    }

    public void setServerMonitor(boolean enable) throws TailwindException {
        JsonObject value = new JsonObject();
        value.addProperty("enable", enable);
        send(VERSION_0_2, TYPE_SET, "server_monitor", value);
    }

    private static JsonObject doorOperation(int doorIndex, String command) {
        JsonObject value = new JsonObject();
        value.addProperty("door_idx", doorIndex);
        value.addProperty("cmd", command);
        return value;
    }

    private TailwindStatus send(String version, String type, String name, @Nullable JsonObject value)
            throws TailwindException {
        JsonObject data = new JsonObject();
        data.addProperty("type", type);
        data.addProperty("name", name);
        if (value != null) {
            data.add("value", value);
        }
        JsonObject request = new JsonObject();
        request.addProperty("product", TailwindBindingConstants.PRODUCT_IQ3);
        request.addProperty("version", version);
        request.add("data", data);

        ContentResponse response;
        try {
            response = httpClient.newRequest(url).method(HttpMethod.POST).header(TOKEN_HEADER, token)
                    .content(new StringContentProvider(GSON.toJson(request))).timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .send();
        } catch (TimeoutException | ExecutionException e) {
            throw new TailwindCommunicationException("No answer from the controller: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TailwindCommunicationException("Interrupted while waiting for the controller", e);
        }

        TailwindStatus status = parse(response.getContentAsString());
        // Controllers answer a wrong key with HTTP 400 and this information
        if ((status != null && INFO_TOKEN_FAIL.equals(status.info))
                || response.getStatus() == HttpStatus.UNAUTHORIZED_401
                || response.getStatus() == HttpStatus.FORBIDDEN_403) {
            throw new TailwindUnauthorizedException("The controller rejected the Local Control Key");
        }
        if (response.getStatus() != HttpStatus.OK_200 || status == null) {
            String info = status == null ? null : status.info;
            throw new TailwindCommunicationException("Unexpected answer from the controller: HTTP "
                    + response.getStatus() + (info == null ? "" : ", " + info));
        }
        if (!status.isOk()) {
            throw new TailwindException("The controller refused the request '" + name + "': " + status.info);
        }
        return status;
    }

    /**
     * Parses an answer or a status report of a controller.
     *
     * @return the status, or {@code null} when the content is not a valid status
     */
    public static @Nullable TailwindStatus parse(@Nullable String content) {
        try {
            return GSON.fromJson(content, TailwindStatus.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
}
