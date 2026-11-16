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

import java.net.UnknownHostException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger logger = LoggerFactory.getLogger(TailwindApi.class);
    private final HttpClient httpClient;
    private volatile String url;
    private final String token;

    public TailwindApi(HttpClient httpClient, String hostname, String token) {
        this.httpClient = httpClient;
        this.url = "http://" + hostname + "/json";
        this.token = token;
    }

    public void setHost(String hostname) {
        url = "http://" + hostname + "/json";
    }

    public TailwindStatus getStatus() throws TailwindException, InterruptedException {
        return send(VERSION_0_1, TYPE_GET, "dev_st", null);
    }

    public void setNotifyUrl(String hostAndPort) throws TailwindException, InterruptedException {
        send(VERSION_0_1, TYPE_SET, "notify_url", notifyUrl(hostAndPort, true));
    }

    public void disableNotifyUrl(String hostAndPort) throws TailwindException, InterruptedException {
        send(VERSION_0_1, TYPE_SET, "notify_url", notifyUrl(hostAndPort, false));
    }

    private static JsonObject notifyUrl(String hostAndPort, boolean enable) {
        JsonObject value = new JsonObject();
        value.addProperty("url", hostAndPort);
        value.addProperty("proto", "udp");
        value.addProperty("enable", enable ? 1 : 0);
        return value;
    }

    public void openDoor(int doorIndex) throws TailwindException, InterruptedException {
        send(VERSION_0_1, TYPE_SET, "door_op", doorOperation(doorIndex, TailwindBindingConstants.DOOR_OPEN));
    }

    public void openDoorPartially(int doorIndex, long partialTimeMillis)
            throws TailwindException, InterruptedException {
        JsonObject value = doorOperation(doorIndex, TailwindBindingConstants.DOOR_OPEN);
        value.addProperty("partial_time", partialTimeMillis);
        send(VERSION_0_1, TYPE_SET, "door_op", value);
    }

    public void closeDoor(int doorIndex) throws TailwindException, InterruptedException {
        send(VERSION_0_1, TYPE_SET, "door_op", doorOperation(doorIndex, TailwindBindingConstants.DOOR_CLOSE));
    }

    public void setLedBrightness(int brightness) throws TailwindException, InterruptedException {
        JsonObject value = new JsonObject();
        value.addProperty("brightness", brightness);
        send(VERSION_0_1, TYPE_SET, "status_led", value);
    }

    public void identify() throws TailwindException, InterruptedException {
        send(VERSION_0_2, TYPE_SET, "identify", null);
    }

    public void reboot() throws TailwindException, InterruptedException {
        send(VERSION_0_2, TYPE_SET, "reboot", null);
    }

    public void setServerMonitor(boolean enable) throws TailwindException, InterruptedException {
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
            throws TailwindException, InterruptedException {
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

        String requestUrl = url;
        String body = GSON.toJson(request);
        // The Local Control Key is sent as a header, so the body can be logged
        logger.trace("Request to {}: {}", host(requestUrl), body);
        long start = System.nanoTime();
        ContentResponse response;
        try {
            response = httpClient.newRequest(requestUrl).method(HttpMethod.POST).header(TOKEN_HEADER, token)
                    .content(new StringContentProvider(body)).timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
        } catch (ExecutionException e) {
            logger.debug("{} to {} failed after {} ms: {}", name, host(requestUrl), millisSince(start), e.getMessage());
            if (e.getCause() instanceof UnknownHostException) {
                throw new TailwindCommunicationException("Cannot find the controller at " + host(requestUrl)
                        + ". If this openHAB server cannot resolve .local names, enter the IP address of the controller instead.",
                        e);
            }
            throw new TailwindCommunicationException("No answer from the controller: " + e.getMessage(), e);
        } catch (TimeoutException e) {
            logger.debug("{} to {} failed after {} ms: {}", name, host(requestUrl), millisSince(start), e.getMessage());
            throw new TailwindCommunicationException("No answer from the controller: " + e.getMessage(), e);
        }

        String answer = response.getContentAsString();
        logger.trace("Answer from {}: {}", host(requestUrl), answer);
        TailwindStatus status = parse(answer);
        logger.debug("{}{} answered in {} ms: HTTP {}, {}", name, value == null ? "" : " " + value, millisSince(start),
                response.getStatus(), status == null ? "no valid JSON"
                        : status.result + (status.info == null ? "" : " (" + status.info + ")"));
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

    private static long millisSince(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    private static String host(String url) {
        return url.substring("http://".length(), url.length() - "/json".length());
    }

    public static @Nullable TailwindStatus parse(@Nullable String content) {
        try {
            return GSON.fromJson(content, TailwindStatus.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
}
