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
package org.openhab.binding.neeo.internal.handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.neeo.internal.NeeoBrainApi;
import org.openhab.binding.neeo.internal.NeeoBrainConfig;
import org.openhab.binding.neeo.internal.NeeoConstants;
import org.openhab.binding.neeo.internal.NeeoUtil;
import org.openhab.binding.neeo.internal.models.NeeoAction;
import org.openhab.binding.neeo.internal.models.NeeoBrain;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * A subclass of {@link BaseBridgeHandler} is responsible for handling commands and discovery for a
 * {@link NeeoBrain}
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class NeeoBrainHandler extends BaseBridgeHandler {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoBrainHandler.class);

    /** The {@link HttpService} to register callbacks */
    private final HttpService httpService;

    /** The {@link NetworkAddressService} to use */
    private final NetworkAddressService networkAddressService;

    /** GSON implementation - only used to deserialize {@link NeeoAction} */
    private final Gson gson = new Gson();

    /** The port the HTTP service is listening on */
    private final int servicePort;

    /**
     * The initialization task (null until set by {@link #initializeTask()} and set back to null in {@link #dispose()}
     */
    private final AtomicReference<@Nullable Future<?>> initializationTask = new AtomicReference<>();

    /** The check status task (not-null when connecting, null otherwise) */
    private final AtomicReference<@Nullable Future<?>> checkStatus = new AtomicReference<>();

    /** The lock that protected multi-threaded access to the state variables */
    private final ReadWriteLock stateLock = new ReentrantReadWriteLock();

    /** The {@link NeeoBrainApi} (null until set by {@link #initializationTask}) */
    @Nullable
    private NeeoBrainApi neeoBrainApi;

    /** The path to the forward action servlet - will be null if not enabled */
    @Nullable
    private String servletPath;

    /** The servlet for forward actions - will be null if not enabled */
    @Nullable
    private NeeoForwardActionsServlet forwardActionServlet;

    /**
     * Instantiates a new neeo brain handler from the {@link Bridge}, service port, {@link HttpService} and
     * {@link NetworkAddressService}.
     *
     * @param bridge the non-null {@link Bridge}
     * @param servicePort the service port the http service is listening on
     * @param httpService the non-null {@link HttpService}
     * @param networkAddressService the non-null {@link NetworkAddressService}
     */
    NeeoBrainHandler(Bridge bridge, int servicePort, HttpService httpService,
            NetworkAddressService networkAddressService) {
        super(bridge);

        Objects.requireNonNull(bridge, "bridge cannot be null");
        Objects.requireNonNull(httpService, "httpService cannot be null");
        Objects.requireNonNull(networkAddressService, "networkAddressService cannot be null");

        this.servicePort = servicePort;
        this.httpService = httpService;
        this.networkAddressService = networkAddressService;
    }

    /**
     * Handles any {@Commands} sent - this bridge has no commands and does nothing
     *
     * @see
     *      org.eclipse.smarthome.core.thing.binding.ThingHandler#handleCommand(org.eclipse.smarthome.core.thing.ChannelUID,
     *      org.eclipse.smarthome.core.types.Command)
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Simply cancels any existing initialization tasks and schedules a new task
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#initialize()
     */
    @Override
    public void initialize() {
        NeeoUtil.cancel(initializationTask.getAndSet(scheduler.submit(() -> {
            initializeTask();
        })));
    }

    /**
     * Initializes the bridge by connecting to the configuration ip address and parsing the results. Properties will be
     * set and the thing will go online.
     */
    private void initializeTask() {
        final Lock writerLock = stateLock.writeLock();
        writerLock.lock();
        try {
            NeeoUtil.checkInterrupt();

            final NeeoBrainConfig config = getBrainConfig();
            final String ipAddress = config.getIpAddress();
            if (ipAddress == null || StringUtils.isEmpty(ipAddress)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Brain IP Address must be specified");
                return;
            }
            final NeeoBrainApi api = new NeeoBrainApi(ipAddress);
            final NeeoBrain brain = api.getBrain();
            final String brainId = getNeeoBrainId();

            NeeoUtil.checkInterrupt();
            neeoBrainApi = api;

            final Map<String, String> properties = new HashMap<>();
            addProperty(properties, "Name", brain.getName());
            addProperty(properties, "Version", brain.getVersion());
            addProperty(properties, "Label", brain.getLabel());
            addProperty(properties, "Is Configured", String.valueOf(brain.isConfigured()));
            addProperty(properties, "Key", brain.getKey());
            addProperty(properties, "AirKey", brain.getAirkey());
            addProperty(properties, "Last Change", String.valueOf(brain.getLastChange()));
            updateProperties(properties);

            if (config.isEnableForwardActions()) {
                NeeoUtil.checkInterrupt();

                forwardActionServlet = new NeeoForwardActionsServlet(scheduler,
                        new NeeoForwardActionsServlet.Callback() {
                            @Override
                            public void post(String json) {
                                triggerChannel(NeeoConstants.CHANNEL_BRAIN_FOWARDACTIONS, json);

                                final NeeoAction action = gson.fromJson(json, NeeoAction.class);

                                for (final Thing child : getThing().getThings()) {
                                    final ThingHandler th = child.getHandler();
                                    if (th instanceof NeeoRoomHandler) {
                                        ((NeeoRoomHandler) th).processAction(action);
                                    }
                                }
                            }

                        }, config.getForwardChain());

                NeeoUtil.checkInterrupt();
                try {
                    servletPath = NeeoConstants.WEBAPP_FORWARDACTIONS.replace("{brainid}", brainId);

                    httpService.registerServlet(servletPath, forwardActionServlet, new Hashtable<>(),
                            httpService.createDefaultHttpContext());

                    final URL callbackURL = createCallbackUrl(brainId, config);
                    if (callbackURL == null) {
                        logger.debug(
                                "Unable to create a callback URL because there is no primary address specified (please set the primary address in the configuration)");
                    } else {
                        final URL url = new URL(callbackURL, servletPath);
                        api.registerForwardActions(url);
                    }
                } catch (NamespaceException | ServletException e) {
                    logger.debug("Error registering forward actions to {}: {}", servletPath, e.getMessage(), e);
                }
            }

            NeeoUtil.checkInterrupt();
            updateStatus(ThingStatus.ONLINE);
            NeeoUtil.checkInterrupt();
            if (config.getCheckStatusInterval() > 0) {
                NeeoUtil.cancel(checkStatus.getAndSet(scheduler.scheduleWithFixedDelay(() -> {
                    try {
                        NeeoUtil.checkInterrupt();
                        checkStatus(ipAddress);
                    } catch (InterruptedException e) {
                        // do nothing - we were interrupted and should stop
                    }
                }, config.getCheckStatusInterval(), config.getCheckStatusInterval(), TimeUnit.SECONDS)));
            }
        } catch (IOException e) {
            logger.debug("Exception occurred connecting to brain: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Exception occurred connecting to brain: " + e.getMessage());
        } catch (InterruptedException e) {
            logger.debug("Initializtion was interrupted", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Initialization was interrupted");
        } finally {
            writerLock.unlock();
        }
    }

    /**
     * Helper method to add a property to the properties map if the value is not null
     *
     * @param properties a non-null properties map
     * @param key a non-null, non-empty key
     * @param value a possibly null, possibly empty key
     */
    private void addProperty(Map<String, String> properties, String key, @Nullable String value) {
        Objects.requireNonNull(properties, "properties cannot be null");
        NeeoUtil.requireNotEmpty(key, "key cannot be empty");
        if (value != null && StringUtils.isNotEmpty(value)) {
            properties.put(key, value);
        }
    }

    /**
     * Gets the {@link NeeoBrainApi} used by this bridge
     *
     * @return a possibly null {@link NeeoBrainApi}
     */
    @Nullable
    public NeeoBrainApi getNeeoBrainApi() {
        final Lock readerLock = stateLock.readLock();
        readerLock.lock();
        try {
            return neeoBrainApi;
        } finally {
            readerLock.unlock();
        }
    }

    /**
     * Gets the brain id used by this bridge
     *
     * @return a non-null, non-empty brain id
     */
    public String getNeeoBrainId() {
        return getThing().getUID().getId();
    }

    /**
     * Helper method to get the {@link NeeoBrainConfig}
     *
     * @return the {@link NeeoBrainConfig}
     */
    private NeeoBrainConfig getBrainConfig() {
        return getConfigAs(NeeoBrainConfig.class);
    }

    /**
     * Checks the status of the brain via a quick socket connection. If the status is unavailable and we are
     * {@link ThingStatus#ONLINE}, then we go {@link ThingStatus#OFFLINE}. If the status is available and we are
     * {@link ThingStatus#OFFLINE}, we go {@link ThingStatus#ONLINE}.
     *
     * @param ipAddress a non-null, non-empty IP address
     */
    private void checkStatus(String ipAddress) {
        NeeoUtil.requireNotEmpty(ipAddress, "ipAddress cannot be empty");

        try {
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(ipAddress, NeeoConstants.DEFAULT_BRAIN_PORT), 5000);
            }
            logger.debug("Checking connectivity to {}:{} - successful", ipAddress, NeeoConstants.DEFAULT_BRAIN_PORT);

            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (IOException e) {
            if (getThing().getStatus() == ThingStatus.ONLINE) {
                logger.debug("Checking connectivity to {}:{} - unsuccessful - going offline: {}", ipAddress,
                        NeeoConstants.DEFAULT_BRAIN_PORT, e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Exception occurred connecting to brain: " + e.getMessage());
            } else {
                logger.debug("Checking connectivity to {}:{} - unsuccessful - still offline", ipAddress,
                        NeeoConstants.DEFAULT_BRAIN_PORT);
            }
        }
    }

    /**
     * Disposes of the bridge by closing/removing the {@link #neeoBrainApi} and canceling/removing any pending
     * {@link #initializeTask()}
     */
    @Override
    public void dispose() {
        final Lock writerLock = stateLock.writeLock();
        writerLock.lock();
        try {
            final NeeoBrainApi api = neeoBrainApi;
            neeoBrainApi = null;

            NeeoUtil.cancel(initializationTask.getAndSet(null));
            NeeoUtil.cancel(checkStatus.getAndSet(null));

            if (forwardActionServlet != null) {
                forwardActionServlet = null;

                if (api != null) {
                    try {
                        api.deregisterForwardActions();
                    } catch (IOException e) {
                        logger.debug("IOException occurred deregistering the forward actions: {}", e.getMessage(), e);
                    }
                }

                if (servletPath != null) {
                    httpService.unregister(servletPath);
                    servletPath = null;
                }
            }

            NeeoUtil.close(api);
        } finally {
            writerLock.unlock();
        }
    }

    /**
     * Creates the URL the brain should callback. Note: if there is multiple interfaces, we try to prefer the one on the
     * same subnet as the brain
     *
     * @param brainId the non-null, non-empty brain identifier
     * @param config the non-null brain configuration
     * @return the callback URL
     * @throws MalformedURLException if the URL is malformed
     */
    @Nullable
    private URL createCallbackUrl(String brainId, NeeoBrainConfig config) throws MalformedURLException {
        NeeoUtil.requireNotEmpty(brainId, "brainId cannot be empty");
        Objects.requireNonNull(config, "config cannot be null");

        final String ipAddress = networkAddressService.getPrimaryIpv4HostAddress();
        if (ipAddress == null) {
            logger.debug("No network interface could be found.");
            return null;
        }

        return new URL("http://" + ipAddress + ":" + servicePort);
    }
}
