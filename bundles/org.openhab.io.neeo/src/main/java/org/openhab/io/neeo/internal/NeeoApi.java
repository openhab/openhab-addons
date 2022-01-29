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
package org.openhab.io.neeo.internal;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.id.InstanceUUID;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.io.neeo.internal.models.NeeoAdapterRegistration;
import org.openhab.io.neeo.internal.models.NeeoRecipe;
import org.openhab.io.neeo.internal.models.NeeoRecipeUrls;
import org.openhab.io.neeo.internal.models.NeeoSystemInfo;
import org.openhab.io.neeo.internal.net.HttpRequest;
import org.openhab.io.neeo.internal.net.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * The class provides the API for communicating with a NEEO brain
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoApi implements AutoCloseable {

    /** The property name used for connection change notifications */
    public static final String CONNECTED = "connected";

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoApi.class);

    /** The gson used in communications */
    private static final Gson GSON = NeeoUtil.createGson();

    /** The brain's IP address */
    private final String brainIpAddress;

    private final ClientBuilder clientBuilder;

    /** The URL of the brain */
    private final String brainUrl;

    /** The brain identifier */
    private final String brainId;

    /** The name of the brain */
    private final String brainName;

    /** The known device keys on the brain. */
    private final NeeoDeviceKeys deviceKeys;

    /** The brain's system information */
    private final NeeoSystemInfo systemInfo;

    /** The property change support */
    private final PropertyChangeSupport propertySupport = new PropertyChangeSupport(this);

    /** The interval, in seconds, to do a check status call */
    private final int checkStatusInterval;

    /** The callback url registered with the brain */
    private final URL callbackUrl;

    /** The ansi escape color conversion */
    private static final String[] ANSICOLORS = new String[] { "black", "red", "green", "yellow", "blue", "magenta",
            "cyan", "white" };

    /** The scheduler used to schedule tasks */
    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(NeeoConstants.THREAD_POOL_NAME);

    /** The connection task (not-null when connecting, null otherwise) */
    private final AtomicReference<@Nullable Future<?>> connect = new AtomicReference<>(null);

    /** The check status task (not-null when connecting, null otherwise) */
    private final AtomicReference<@Nullable ScheduledFuture<?>> checkStatus = new AtomicReference<>(null);

    /** The {@link HttpRequest} used for making requests */
    private final AtomicReference<HttpRequest> request;
    /** Whether the brain is currently connected */
    private final AtomicBoolean connected = new AtomicBoolean(false);

    /**
     * Constructs the APi from the given IP address, brain identifier and {@link ServiceContext}
     *
     * @param ipAddress the non-empty ip address
     * @param brainId the non-empty brain id
     * @param context the non-null {@link ServiceContext}
     * @throws IOException if an exception occurs connecting to the brain
     */
    public NeeoApi(String ipAddress, String brainId, ServiceContext context, ClientBuilder clientBuilder)
            throws IOException {
        NeeoUtil.requireNotEmpty(ipAddress, "ipAddress cannot be empty");
        NeeoUtil.requireNotEmpty(brainId, "brainId cannot be empty");
        Objects.requireNonNull(context, "context cannot be null");

        this.brainIpAddress = ipAddress;
        this.brainId = brainId;
        this.clientBuilder = clientBuilder;
        this.brainUrl = NeeoConstants.PROTOCOL + (ipAddress.startsWith("/") ? ipAddress.substring(1) : ipAddress) + ":"
                + NeeoConstants.DEFAULT_BRAIN_PORT;
        deviceKeys = new NeeoDeviceKeys(brainUrl, clientBuilder);

        request = new AtomicReference<>(new HttpRequest(clientBuilder));

        this.systemInfo = getSystemInfo(ipAddress, clientBuilder);

        String name = brainId;
        try (HttpRequest request = new HttpRequest(clientBuilder)) {
            logger.debug("Getting existing device mappings from {}{}", brainUrl, NeeoConstants.PROJECTS_HOME);
            final HttpResponse resp = request.sendGetCommand(brainUrl + NeeoConstants.PROJECTS_HOME);
            if (resp.getHttpCode() != HttpStatus.OK_200) {
                throw resp.createException();
            }

            final JsonObject root = JsonParser.parseString(resp.getContent()).getAsJsonObject();
            for (Map.Entry<String, JsonElement> room : root.getAsJsonObject("rooms").entrySet()) {
                final JsonObject roomObj = (JsonObject) room.getValue();

                if (roomObj.get("hasController").getAsBoolean()) {
                    name = roomObj.get("name").getAsString();
                    break;
                }
            }
        }
        this.brainName = name;

        final Object statusCheck = context.getComponentContext().getProperties()
                .get(NeeoConstants.CFG_CHECKSTATUSINTERVAL);
        int checkStatus = 10;
        if (statusCheck != null) {
            try {
                checkStatus = Integer.parseInt(statusCheck.toString());
            } catch (NumberFormatException e) {
                logger.debug("{} was not a valid integer, defaulting to 10: {}", NeeoConstants.CFG_CHECKSTATUSINTERVAL,
                        statusCheck);
            }
        }
        checkStatusInterval = checkStatus;

        // Get the port the service is listening on
        final int port = HttpServiceUtil.getHttpServicePort(context.getComponentContext().getBundleContext());

        final String primaryAddress = context.getNetworkAddressService().getPrimaryIpv4HostAddress();
        if (primaryAddress == null) {
            throw new IOException(
                    "Unable to create a callback URL because there is no primary address specified (please set the primary address in the configuration)");
        }

        callbackUrl = new URL("http://" + primaryAddress + ":"
                + (port == -1 ? NeeoConstants.DEFAULT_OPENHAB_PORT : port) + NeeoUtil.getServletUrl(brainId));
    }

    /**
     * Returns the brain's system information
     *
     * @return a non-null system information
     */
    public NeeoSystemInfo getSystemInfo() {
        return this.systemInfo;
    }

    /**
     * Helper method to retrieve the {@link NeeoSystemInfo} from the given ip address
     *
     * @param ipAddress the non-empty ip address
     * @return the non-null {@link NeeoSystemInfo} for the address
     * @throws IOException Signals that an I/O exception has occurred or the URL is not a brain
     */
    public static NeeoSystemInfo getSystemInfo(String ipAddress, ClientBuilder clientBuilder) throws IOException {
        NeeoUtil.requireNotEmpty(ipAddress, "ipAddress cannot be empty");
        final String sysInfo = NeeoConstants.PROTOCOL + (ipAddress.startsWith("/") ? ipAddress.substring(1) : ipAddress)
                + ":" + NeeoConstants.DEFAULT_BRAIN_PORT + NeeoConstants.SYSTEMINFO;

        try (HttpRequest req = new HttpRequest(clientBuilder)) {
            final HttpResponse res = req.sendGetCommand(sysInfo);
            if (res.getHttpCode() == HttpStatus.OK_200) {
                return Objects.requireNonNull(GSON.fromJson(res.getContent(), NeeoSystemInfo.class));
            } else {
                throw res.createException();
            }
        }
    }

    /**
     * Returns the name of the brain
     *
     * @return a non-null, non-empty brain name
     */
    public String getBrainName() {
        return brainName;
    }

    /**
     * Helper method to blink the LED on the given brain IP address
     *
     * @throws IOException Signals that an I/O exception has occurred or the URL is not a brain
     */
    public void blinkLed() throws IOException {
        final String identBrain = NeeoConstants.PROTOCOL
                + (brainIpAddress.startsWith("/") ? brainIpAddress.substring(1) : brainIpAddress) + ":"
                + NeeoConstants.DEFAULT_BRAIN_PORT + NeeoConstants.IDENTBRAIN;

        final HttpRequest rqst = request.get();
        final HttpResponse res = rqst.sendGetCommand(identBrain);
        if (res.getHttpCode() != HttpStatus.OK_200) {
            throw res.createException();
        }
    }

    /**
     * Helper method to get the log file from the brain, convert the ANSI escaped result to HTML and return it
     *
     * @return a non-empty string containing the log file from the brain
     *
     * @throws IOException Signals that an I/O exception has occurred or the URL is not a brain
     */
    public String getLog() throws IOException {
        final String logUrl = NeeoConstants.PROTOCOL
                + (brainIpAddress.startsWith("/") ? brainIpAddress.substring(1) : brainIpAddress) + ":"
                + NeeoConstants.DEFAULT_BRAIN_PORT + NeeoConstants.GETLOG;

        final HttpRequest rqst = request.get();
        final HttpResponse res = rqst.sendGetCommand(logUrl);
        if (res.getHttpCode() != HttpStatus.OK_200) {
            throw res.createException();
        }

        final StringBuilder bld = new StringBuilder(1000);
        bld.append("<pre><div><span>");

        final char[] resp = res.getContent().toCharArray();
        for (int x = 0; x < resp.length; x++) {
            final char ch = resp[x];
            final char nx = x + 1 == resp.length ? '-' : resp[x + 1];

            if ((ch == '\n' || ch == '\r')) {
                if (x + 1 < resp.length) {
                    bld.append("</span></div><div><span>");
                }
            } else if (ch == 27 && nx == '[') {
                bld.append("</span>");
                x++;
                String codes = "";
                while (x + 1 < resp.length && resp[++x] != 'm') {
                    codes += resp[x];
                }

                String style = "";
                for (String code : codes.split(";")) {
                    try {
                        int cint = Integer.parseInt(code);
                        if (cint == 0) {
                            style = "";
                        } else if (cint == 1) {
                            style += "font-weight:bold;";
                        } else if (cint == 4) {
                            style += "font-style:italic;";
                        } else if (cint >= 30 && cint <= 37) {
                            style += "color:" + ANSICOLORS[cint - 30];
                        } else if (cint >= 40 && cint <= 47) {
                            style += "background-color:" + ANSICOLORS[cint - 40];
                        }
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
                bld.append("<span style='" + style + "'>");
            } else {
                bld.append(ch);
            }
            if (x + 1 == resp.length) {
                bld.append("</span></div></pre>");
            }
        }
        return bld.toString();
    }

    /**
     * Start the API by scheduling the connection via {@link #scheduleConnect()}
     */
    public void start() {
        scheduleConnect(0);
    }

    /**
     * Restarts the API by closing and reopening the connection. Please note that this is currently an alias for
     * {@link #start()} (which does the same thing)
     */
    public void restart() {
        start();
    }

    /**
     * Checks to see if the API is currently connected or not
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return connected.get();
    }

    /**
     * Schedules a connection attempt in 5 seconds. Simply calls {@link #scheduleConnect(int)} with 5 as it's parameter
     */
    private void scheduleConnect() {
        scheduleConnect(5);
    }

    /**
     * Schedules a connection attempt (will cancel any prior connection attempt if active)
     *
     * @param scheduledTimeInSeconds the time to wait (in seconds) before scheduling a connect
     */
    private void scheduleConnect(int scheduledTimeInSeconds) {
        NeeoUtil.cancel(connect.getAndSet(scheduler.schedule(() -> {
            connect();
        }, scheduledTimeInSeconds, TimeUnit.SECONDS)));
    }

    /**
     * Schedules a check status attempt (will cancel any prior connection attempt if active)l
     */
    private void scheduleCheckStatus() {
        NeeoUtil.cancel(checkStatus.getAndSet(scheduler.scheduleWithFixedDelay(() -> {
            checkStatus();
        }, checkStatusInterval, checkStatusInterval, TimeUnit.SECONDS)));
    }

    /**
     * Checks the status of the brain via a quick socket connection. If the status is unavailable, we schedule a
     * connection attempt via {@link #scheduleConnect()} and end this task
     */
    private void checkStatus() {
        try {
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(brainIpAddress, NeeoConstants.DEFAULT_BRAIN_PORT),
                        NeeoConstants.CONNECTION_TIMEOUT);
            }
            logger.debug("Checking connectivity to {}:{} - successful", brainIpAddress,
                    NeeoConstants.DEFAULT_BRAIN_PORT);
        } catch (IOException ex) {
            logger.debug("Checking connectivity to {}:{} - unsuccessful - going offline: {}", brainIpAddress,
                    NeeoConstants.DEFAULT_BRAIN_PORT, ex.getMessage(), ex);
            setConnected(false);
            NeeoUtil.cancel(checkStatus.getAndSet(null));
            scheduleConnect();
        }
    }

    /**
     * Attempts to connect to the brain by first registering our API ({@link #registerApi()} and then refreshes our
     * device keys found on the brain.
     * Any IOException will be caught and another attempt will be scheduled
     */
    private void connect() {
        logger.debug("Starting connect for {} at {}", brainId, brainIpAddress);
        try {
            setConnected(false);

            NeeoUtil.close(request.getAndSet(new HttpRequest(clientBuilder)));

            NeeoUtil.checkInterrupt();
            registerApi();

            NeeoUtil.checkInterrupt();
            deviceKeys.refresh();

            NeeoUtil.checkInterrupt();
            setConnected(true);
            logger.debug("Connection successful for {} at {}", brainId, brainIpAddress);

            NeeoUtil.checkInterrupt();
            scheduleCheckStatus();
        } catch (IOException e) {
            logger.debug("Connection failed {} at {} (scheduling another connect): {} ", brainId, brainIpAddress,
                    e.getMessage(), e);
            scheduleConnect();
        } catch (InterruptedException e) {
            logger.debug("Connect was interrupted", e);
        }
    }

    /**
     * Register our API with the brain
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void registerApi() throws IOException {
        deregisterApi();

        final HttpRequest rqst = request.get();
        logger.debug("Registering {} for {}{} using callback {}", brainId, brainUrl, NeeoConstants.REGISTER_SDK_ADAPTER,
                callbackUrl);
        final String register = GSON.toJson(new NeeoAdapterRegistration(
                NeeoConstants.ADAPTER_NAME + "-" + InstanceUUID.get(), callbackUrl.toExternalForm()));
        final HttpResponse resp = rqst.sendPostJsonCommand(brainUrl + NeeoConstants.REGISTER_SDK_ADAPTER, register);
        if (resp.getHttpCode() != HttpStatus.OK_200) {
            throw resp.createException();
        }
    }

    /**
     * Get's the brain identifier for this API
     *
     * @return the brain id
     */
    public String getBrainId() {
        return brainId;
    }

    /**
     * Gets the brain URL for this API
     *
     * @return the brain url
     */
    public String getBrainUrl() {
        return brainUrl;
    }

    /**
     * Gets the {@link NeeoDeviceKeys}
     *
     * @return the device keys
     */
    public NeeoDeviceKeys getDeviceKeys() {
        return deviceKeys;
    }

    /**
     * Send a notification to the brain
     *
     * @param msg the possibly null, possibly empty notification to send
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void notify(String msg) throws IOException {
        if (isConnected()) {
            final HttpRequest rqst = request.get();
            logger.debug("Sending Notification to brain ({}): {}", brainId, msg);
            final HttpResponse resp = rqst.sendPostJsonCommand(brainUrl + NeeoConstants.NOTIFICATION, msg);
            if (resp.getHttpCode() != HttpStatus.OK_200) {
                throw resp.createException();
            }
        } else {
            logger.debug("Notification ignored - brain not connected");
        }
    }

    /**
     * Deregister our API with the brain.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void deregisterApi() throws IOException {
        final HttpRequest rqst = request.get();
        try {
            logger.debug("Deregistering {} for {}{} using callback {}", brainId, brainUrl,
                    NeeoConstants.UNREGISTER_SDK_ADAPTER, callbackUrl);
            final String deregister = GSON.toJson(new NeeoAdapterRegistration(
                    NeeoConstants.ADAPTER_NAME + "-" + InstanceUUID.get(), callbackUrl.toExternalForm()));
            final HttpResponse resp = rqst.sendPostJsonCommand(brainUrl + NeeoConstants.UNREGISTER_SDK_ADAPTER,
                    deregister);
            if (resp.getHttpCode() != HttpStatus.OK_200) {
                throw resp.createException();
            }
        } finally {
            NeeoUtil.cancel(checkStatus.getAndSet(null));
        }
    }

    /**
     * Executes a recipe for the given deviceKey
     *
     * @param deviceKey the non-empty device key
     * @param on true to start the recipe, false to stop
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void executeRecipe(String deviceKey, boolean on) throws IOException {
        NeeoUtil.requireNotEmpty(deviceKey, "deviceKey cannot be empty");

        final HttpRequest rqst = request.get();
        final HttpResponse resp = rqst.sendGetCommand(brainUrl + NeeoConstants.RECIPES);
        if (resp.getHttpCode() != HttpStatus.OK_200) {
            throw resp.createException();
        }

        for (NeeoRecipe recipe : GSON.fromJson(resp.getContent(), NeeoRecipe[].class)) {
            if (deviceKey.equalsIgnoreCase(recipe.getUid())) {
                final NeeoRecipeUrls urls = recipe.getUrls();
                final String url = urls == null ? null : (on ? urls.getSetPowerOn() : urls.getSetPowerOff());

                if (url != null && !url.isEmpty()) {
                    final HttpResponse cmdResp = rqst.sendGetCommand(url);
                    if (cmdResp.getHttpCode() != HttpStatus.OK_200) {
                        throw cmdResp.createException();
                    }
                }
                break;
            }
        }
    }

    /**
     * Returns the IP Address for the brain
     *
     * @return a non-null, non-empty, valid IP Address
     */
    public String getBrainIpAddress() {
        return brainIpAddress;
    }

    /**
     * Simply cancels any connection attempt and de-registers the API.
     */
    @Override
    public void close() {
        // kill our threads
        NeeoUtil.cancel(checkStatus.getAndSet(null));
        NeeoUtil.cancel(connect.getAndSet(null));

        try {
            deregisterApi();
        } catch (IOException e) {
            logger.debug("Exception while deregistring api during close - ignoring: {}", e.getMessage(), e);
        } finally {
            // Do this regardless if a runtime exception was thrown
            NeeoUtil.close(request.get());
            setConnected(false);
        }
    }

    /**
     * Adds a {@link PropertyChangeListener} for the given propertyChange
     *
     * @param propertyName a non-null, non-empty property name
     * @param listener a non-null listener to add
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        NeeoUtil.requireNotEmpty(propertyName, "propertyName must not be empty");
        Objects.requireNonNull(listener, "listener cannot be null");
        propertySupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Removes the {@link PropertyChangeListener} from property change notifications
     *
     * @param listener a non-null listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        Objects.requireNonNull(listener, "listener cannot be null");
        propertySupport.removePropertyChangeListener(listener);
    }

    /**
     * Helper method to set the connected status and fire off change listener
     *
     * @param connected true if connected, false otherwise
     */
    private void setConnected(boolean connected) {
        final boolean oldValue = this.connected.getAndSet(connected);

        if (oldValue != connected) {
            propertySupport.firePropertyChange(CONNECTED, oldValue, connected);
        }
    }
}
