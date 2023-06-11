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
package org.openhab.binding.neeo.internal;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.neeo.internal.models.ExecuteResult;
import org.openhab.binding.neeo.internal.models.NeeoBrain;
import org.openhab.binding.neeo.internal.models.NeeoForwardActions;
import org.openhab.binding.neeo.internal.models.NeeoRoom;
import org.openhab.binding.neeo.internal.net.HttpRequest;
import org.openhab.binding.neeo.internal.net.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The class provides the API for communicating with a NEEO brain
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoBrainApi implements AutoCloseable {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoBrainApi.class);

    /** The gson used in communications */
    private final Gson gson = NeeoUtil.getGson();

    /** The {@link HttpRequest} used for making requests */
    private final AtomicReference<HttpRequest> request;

    /** The {@link ClientBuilder} to use */
    private final ClientBuilder clientBuilder;

    /** The IP address of the neeo brain */
    private final NeeoUrlBuilder urlBuilder;

    /**
     * Instantiates the API using the specified IP Address
     *
     * @param ipAddress the non-empty ip address
     */
    public NeeoBrainApi(String ipAddress, ClientBuilder clientBuilder) {
        NeeoUtil.requireNotEmpty(ipAddress, "ipAddress cannot be empty");

        this.urlBuilder = new NeeoUrlBuilder(
                NeeoConstants.PROTOCOL + ipAddress + ":" + NeeoConstants.DEFAULT_BRAIN_PORT);
        this.clientBuilder = clientBuilder;

        request = new AtomicReference<>(new HttpRequest(clientBuilder));
    }

    /**
     * Gets the {@link NeeoBrain}
     *
     * @return the non-null {@link NeeoBrain}
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public NeeoBrain getBrain() throws IOException {
        final String url = urlBuilder.append(NeeoConstants.PROJECTS_HOME).toString();

        final HttpRequest rqst = request.get();
        final HttpResponse resp = rqst.sendGetCommand(url);
        if (resp.getHttpCode() != HttpStatus.OK_200) {
            throw resp.createException();
        }

        return Objects.requireNonNull(gson.fromJson(resp.getContent(), NeeoBrain.class));
    }

    /**
     * Gets the {@link NeeoRoom} from the brain for the specified room key
     *
     * @param roomKey the non-empty room key
     * @return the {@link NeeoRoom}
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public NeeoRoom getRoom(String roomKey) throws IOException {
        NeeoUtil.requireNotEmpty(roomKey, "roomKey cannot be empty");

        final String url = urlBuilder.append(NeeoConstants.GET_ROOM).sub("roomkey", roomKey).toString();

        final HttpRequest rqst = request.get();
        final HttpResponse resp = rqst.sendGetCommand(url);
        if (resp.getHttpCode() != HttpStatus.OK_200) {
            throw resp.createException();
        }

        return Objects.requireNonNull(gson.fromJson(resp.getContent(), NeeoRoom.class));
    }

    /**
     * Execute the specified recipe in the specified room
     *
     * @param roomKey the non-empty room key
     * @param recipeKey the non-empty recipe key
     * @return the execute result
     * @throws IOException Signals that an I/O exception has occurred.
     */
    ExecuteResult executeRecipe(String roomKey, String recipeKey) throws IOException {
        NeeoUtil.requireNotEmpty(roomKey, "roomKey cannot be empty");
        NeeoUtil.requireNotEmpty(recipeKey, "recipeKey cannot be empty");

        final String url = urlBuilder.append(NeeoConstants.EXECUTE_RECIPE).sub("roomkey", roomKey)
                .sub("recipekey", recipeKey).toString();

        final HttpRequest rqst = request.get();
        final HttpResponse resp = rqst.sendGetCommand(url);
        if (resp.getHttpCode() != HttpStatus.OK_200) {
            throw resp.createException();
        }

        return Objects.requireNonNull(gson.fromJson(resp.getContent(), ExecuteResult.class));
    }

    /**
     * Stops the specified scenario in the specified room
     *
     * @param roomKey the non-empty room key
     * @param scenarioKey the non-empty scenario key
     * @return the execute result
     * @throws IOException Signals that an I/O exception has occurred.
     */
    ExecuteResult stopScenario(String roomKey, String scenarioKey) throws IOException {
        NeeoUtil.requireNotEmpty(roomKey, "roomKey cannot be empty");
        NeeoUtil.requireNotEmpty(scenarioKey, "scenarioKey cannot be empty");

        final String url = urlBuilder.append(NeeoConstants.STOP_SCENARIO).sub("roomkey", roomKey)
                .sub("scenariokey", scenarioKey).toString();

        final HttpRequest rqst = request.get();
        final HttpResponse resp = rqst.sendGetCommand(url);
        if (resp.getHttpCode() != HttpStatus.OK_200) {
            throw resp.createException();
        }

        return Objects.requireNonNull(gson.fromJson(resp.getContent(), ExecuteResult.class));
    }

    /**
     * Gets the active scenarios.
     *
     * @return the non-null, possibly empty list of active scenarios keys
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public List<String> getActiveScenarios() throws IOException {
        final String url = urlBuilder.append(NeeoConstants.GET_ACTIVESCENARIOS).toString();

        final HttpRequest rqst = request.get();
        final HttpResponse resp = rqst.sendGetCommand(url);
        if (resp.getHttpCode() != HttpStatus.OK_200) {
            throw resp.createException();
        }

        Type arrayListType = new TypeToken<ArrayList<String>>() {
        }.getType();
        return Objects.requireNonNull(gson.fromJson(resp.getContent(), arrayListType));
    }

    /**
     * Trigger macro on a specified device in the specified room.
     *
     * @param roomKey the non-null room key
     * @param deviceKey the non-null device key
     * @param macroKey the non-null macro key
     * @return the execute result
     * @throws IOException Signals that an I/O exception has occurred.
     */
    ExecuteResult triggerMacro(String roomKey, String deviceKey, String macroKey) throws IOException {
        NeeoUtil.requireNotEmpty(roomKey, "roomKey cannot be empty");
        NeeoUtil.requireNotEmpty(deviceKey, "deviceKey cannot be empty");
        NeeoUtil.requireNotEmpty(macroKey, "macroKey cannot be empty");

        final String url = urlBuilder.append(NeeoConstants.TRIGGER_MACRO).sub("roomkey", roomKey)
                .sub("devicekey", deviceKey).sub("macrokey", macroKey).toString();

        final HttpRequest rqst = request.get();
        final HttpResponse resp = rqst.sendGetCommand(url);
        if (resp.getHttpCode() != HttpStatus.OK_200) {
            throw resp.createException();
        }

        return Objects.requireNonNull(gson.fromJson(resp.getContent(), ExecuteResult.class));
    }

    /**
     * Register our API with the brain's forward actions.
     *
     * @param url the non-null URL to register to
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void registerForwardActions(URL url) throws IOException {
        Objects.requireNonNull(url, "url cannot be null");

        final String brainUrl = urlBuilder.append(NeeoConstants.FORWARD_ACTIONS).toString();

        logger.debug("Registering forward actions {} using callback {}", brainUrl, url.toExternalForm());

        final String forwardActions = gson.toJson(new NeeoForwardActions(url.getHost(), url.getPort(), url.getPath()));

        final HttpRequest rqst = request.get();
        final HttpResponse resp = rqst.sendPostJsonCommand(brainUrl, forwardActions);
        if (resp.getHttpCode() != HttpStatus.OK_200) {
            throw resp.createException();
        }
    }

    /**
     * Deregister our API with the brain's forward actions.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void deregisterForwardActions() throws IOException {
        final String brainUrl = urlBuilder.append(NeeoConstants.FORWARD_ACTIONS).toString();

        logger.debug("Deregistering forward actions callback {}", brainUrl);
        final HttpRequest rqst = request.get();
        final HttpResponse resp = rqst.sendPostJsonCommand(brainUrl, "");
        if (resp.getHttpCode() != HttpStatus.OK_200) {
            throw resp.createException();
        }
    }

    @Override
    public void close() throws Exception {
        NeeoUtil.close(request.getAndSet(new HttpRequest(clientBuilder)));
    }

    /**
     * Helper class to help build NEEO URLs
     *
     * @author Tim Roberts - Initial contribution
     */
    private class NeeoUrlBuilder {
        /** The current url */
        private final String url;

        /**
         * Create a new class from the given URL
         *
         * @param url a non-null, non-empty URL
         */
        NeeoUrlBuilder(final String url) {
            NeeoUtil.requireNotEmpty(url, "url cannot be empty");
            this.url = url;
        }

        /**
         * Substitutes '{key}' into value from the URL and returns a new {@link NeeoUrlBuilder} with the new URL
         *
         * @param key a non-null, non-empty key
         * @param value a non-null, non-empty key
         * @return a non-null {@link NeeoUrlBuilder} with the new URL
         */
        NeeoUrlBuilder sub(String key, String value) {
            NeeoUtil.requireNotEmpty(key, "key cannot be empty");
            NeeoUtil.requireNotEmpty(value, "value cannot be empty");

            final String newUrl = url.replace("{" + key + "}", value);
            return new NeeoUrlBuilder(newUrl);
        }

        /**
         * Appends the specified value to the URL and returns the new {@link NeeoUrlBuilder} with the new URL
         *
         * @param value a non-null, non-empty value
         * @return a non-null {@link NeeoUrlBuilder} with the new URL
         */
        NeeoUrlBuilder append(String value) {
            NeeoUtil.requireNotEmpty(value, "value cannot be empty");

            return new NeeoUrlBuilder(url + (value.startsWith("/") ? value : ("/" + value)));
        }

        @Override
        public String toString() {
            return url;
        }
    }
}
