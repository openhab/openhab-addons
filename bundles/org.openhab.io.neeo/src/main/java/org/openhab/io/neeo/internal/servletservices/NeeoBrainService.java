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
package org.openhab.io.neeo.internal.servletservices;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.events.ItemCommandEvent;
import org.openhab.core.items.events.ItemEventFactory;
import org.openhab.core.items.events.ItemStateChangedEvent;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.events.ChannelTriggeredEvent;
import org.openhab.core.thing.events.ThingEventFactory;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.io.neeo.internal.NeeoApi;
import org.openhab.io.neeo.internal.NeeoConstants;
import org.openhab.io.neeo.internal.NeeoDeviceKeys;
import org.openhab.io.neeo.internal.NeeoItemValueConverter;
import org.openhab.io.neeo.internal.NeeoUtil;
import org.openhab.io.neeo.internal.ServiceContext;
import org.openhab.io.neeo.internal.models.ButtonInfo;
import org.openhab.io.neeo.internal.models.NeeoButtonGroup;
import org.openhab.io.neeo.internal.models.NeeoCapabilityType;
import org.openhab.io.neeo.internal.models.NeeoDevice;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannel;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannelDirectory;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannelKind;
import org.openhab.io.neeo.internal.models.NeeoDirectoryRequest;
import org.openhab.io.neeo.internal.models.NeeoDirectoryRequestAction;
import org.openhab.io.neeo.internal.models.NeeoDirectoryResult;
import org.openhab.io.neeo.internal.models.NeeoItemValue;
import org.openhab.io.neeo.internal.models.NeeoNotification;
import org.openhab.io.neeo.internal.models.NeeoSensorNotification;
import org.openhab.io.neeo.internal.models.NeeoThingUID;
import org.openhab.io.neeo.internal.net.HttpRequest;
import org.openhab.io.neeo.internal.servletservices.models.PathInfo;
import org.openhab.io.neeo.internal.servletservices.models.ReturnStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The implementation of {@link ServletService} that will handle device callbacks from the Neeo Brain
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoBrainService extends DefaultServletService {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoBrainService.class);

    /** The gson used for communications */
    private final Gson gson = NeeoUtil.createGson();

    /** The NEEO API to use */
    private final NeeoApi api;

    /** The service context */
    private final ServiceContext context;

    /** The HTTP request */
    private final HttpRequest request;

    /** The scheduler to use to schedule recipe execution */
    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(NeeoConstants.THREAD_POOL_NAME);

    /** The {@link NeeoItemValueConverter} used to convert values with */
    private final NeeoItemValueConverter itemConverter;

    private final PropertyChangeListener listener = new PropertyChangeListener() {
        @Override
        public void propertyChange(@Nullable PropertyChangeEvent evt) {
            if (evt != null && (Boolean) evt.getNewValue()) {
                resendState();
            }
        }
    };

    /**
     * Constructs the service from the {@link NeeoApi} and {@link ServiceContext}
     *
     * @param api the non-null api
     * @param context the non-null context
     */
    public NeeoBrainService(NeeoApi api, ServiceContext context, ClientBuilder clientBuilder) {
        Objects.requireNonNull(api, "api cannot be null");
        Objects.requireNonNull(context, "context cannot be null");

        this.context = context;
        this.itemConverter = new NeeoItemValueConverter(context);
        this.api = api;
        this.api.addPropertyChangeListener(NeeoApi.CONNECTED, listener);
        scheduler.execute(() -> {
            resendState();
        });
        request = new HttpRequest(clientBuilder);
    }

    /**
     * Returns true if the path start with 'device' or ends with either 'subscribe' or 'unsubscribe'
     *
     * @see DefaultServletService#canHandleRoute(String[])
     */
    @Override
    public boolean canHandleRoute(String[] paths) {
        Objects.requireNonNull(paths, "paths cannot be null");

        if (paths.length == 0) {
            return false;
        }

        if ("device".equalsIgnoreCase(paths[0])) {
            return true;
        }

        final String lastPath = paths.length >= 2 ? paths[1] : null;
        return "subscribe".equalsIgnoreCase(lastPath) || "unsubscribe".equalsIgnoreCase(lastPath);
    }

    @Override
    public void handlePost(HttpServletRequest req, String[] paths, HttpServletResponse resp) throws IOException {
        Objects.requireNonNull(req, "req cannot be null");
        Objects.requireNonNull(paths, "paths cannot be null");
        Objects.requireNonNull(resp, "resp cannot be null");
        if (paths.length == 0) {
            throw new IllegalArgumentException("paths cannot be empty");
        }

        final boolean hasDeviceStart = "device".equalsIgnoreCase(paths[0]);

        if (hasDeviceStart) {
            final PathInfo pathInfo = new PathInfo(paths);

            if ("directory".equalsIgnoreCase(pathInfo.getComponentType())) {
                handleDirectory(req, resp, pathInfo);
            } else {
                logger.debug("Unknown/unhandled brain service device route (POST): {}", String.join("/", paths));

            }
        } else {
            logger.debug("Unknown/unhandled brain service route (POST): {}", String.join("/", paths));
        }
    }

    @Override
    public void handleGet(HttpServletRequest req, String[] paths, HttpServletResponse resp) throws IOException {
        Objects.requireNonNull(req, "req cannot be null");
        Objects.requireNonNull(paths, "paths cannot be null");
        Objects.requireNonNull(resp, "resp cannot be null");
        if (paths.length == 0) {
            throw new IllegalArgumentException("paths cannot be empty");
        }

        // Paths handled specially
        // 1. See PATHINFO for various /device/* keys (except for the next)
        // 2. New subscribe path: /device/{thingUID}/subscribe/default/{devicekey}
        // 3. New unsubscribe path: /device/{thingUID}/unsubscribe/default
        // 4. Old subscribe path: /{thingUID}/subscribe or unsubscribe/{deviceid}/{devicekey}
        // 4. Old unsubscribe path: /{thingUID}/subscribe or unsubscribe/{deviceid}

        final boolean hasDeviceStart = "device".equalsIgnoreCase(paths[0]);
        if (hasDeviceStart && (paths.length >= 3 && !"subscribe".equalsIgnoreCase(paths[2])
                && !"unsubscribe".equalsIgnoreCase(paths[2]))) {
            try {
                final PathInfo pathInfo = new PathInfo(paths);

                String actionValue = pathInfo.getActionValue();
                if (actionValue == null || actionValue.isEmpty()) {
                    handleGetValue(resp, pathInfo);
                } else {
                    handleSetValue(resp, pathInfo);
                }
            } catch (IllegalArgumentException e) {
                logger.debug("Bad path: {} - {}", String.join("", paths), e.getMessage(), e);
            }
        } else {
            int idx = hasDeviceStart ? 1 : 0;

            if (idx + 2 < paths.length) {
                final String adapterName = paths[idx++];
                final String action = paths[idx++].toLowerCase();
                idx++; // deviceId/default - not used

                switch (action) {
                    case "subscribe":
                        if (idx < paths.length) {
                            final String deviceKey = paths[idx++];
                            handleSubscribe(resp, adapterName, deviceKey);
                        } else {
                            logger.debug("No device key set for a subscribe action: {}", String.join("/", paths));
                        }
                        break;
                    case "unsubscribe":
                        handleUnsubscribe(resp, adapterName);
                        break;
                    default:
                        logger.debug("Unknown action: {}", action);
                }

            } else {
                logger.debug("Unknown/unhandled brain service route (GET): {}", String.join("/", paths));
            }
        }
    }

    /**
     * Handle set value from the path
     *
     * @param resp the non-null response to write the response to
     * @param pathInfo the non-null path information
     */
    private void handleSetValue(HttpServletResponse resp, PathInfo pathInfo) {
        Objects.requireNonNull(resp, "resp cannot be null");
        Objects.requireNonNull(pathInfo, "pathInfo cannot be null");

        logger.debug("handleSetValue {}", pathInfo);
        final NeeoDevice device = context.getDefinitions().getDevice(pathInfo.getThingUid());
        if (device != null) {
            final NeeoDeviceChannel channel = device.getChannel(pathInfo.getItemName(), pathInfo.getSubType(),
                    pathInfo.getChannelNbr());
            if (channel != null && channel.getKind() == NeeoDeviceChannelKind.TRIGGER) {
                String value = channel.getValue();
                final ChannelTriggeredEvent event = ThingEventFactory.createTriggerEvent(value == null ? "" : value,
                        new ChannelUID(device.getUid(), channel.getItemName()));
                logger.debug("Posting triggered event: {}", event);
                context.getEventPublisher().post(event);
            } else {
                try {
                    final Item item = context.getItemRegistry().getItem(pathInfo.getItemName());
                    final Command cmd = NeeoItemValueConverter.convert(item, pathInfo);
                    if (cmd != null) {
                        final ItemCommandEvent event = ItemEventFactory.createCommandEvent(item.getName(), cmd);
                        logger.debug("Posting item event: {}", event);
                        context.getEventPublisher().post(event);
                    } else {
                        logger.debug("Cannot set value - no command for path: {}", pathInfo);
                    }
                } catch (ItemNotFoundException e) {
                    logger.debug("Cannot set value - no linked items: {}", pathInfo);
                }
            }
        } else {
            logger.debug("Cannot set value - no device definition: {}", pathInfo);
        }
    }

    /**
     * Handle set value from the path
     *
     * @param resp the non-null response to write the response to
     * @param pathInfo the non-null path information
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void handleGetValue(HttpServletResponse resp, PathInfo pathInfo) throws IOException {
        Objects.requireNonNull(resp, "resp cannot be null");
        Objects.requireNonNull(pathInfo, "pathInfo cannot be null");

        NeeoItemValue niv = new NeeoItemValue("");

        try {
            final NeeoDevice device = context.getDefinitions().getDevice(pathInfo.getThingUid());
            if (device != null) {
                final NeeoDeviceChannel channel = device.getChannel(pathInfo.getItemName(), pathInfo.getSubType(),
                        pathInfo.getChannelNbr());
                if (channel != null && channel.getKind() == NeeoDeviceChannelKind.ITEM) {
                    try {
                        final Item item = context.getItemRegistry().getItem(pathInfo.getItemName());
                        niv = itemConverter.convert(channel, item.getState());
                    } catch (ItemNotFoundException e) {
                        logger.debug("Item '{}' not found to get a value ({})", pathInfo.getItemName(), pathInfo);
                    }
                } else {
                    logger.debug("Channel definition for '{}' not found to get a value ({})", pathInfo.getItemName(),
                            pathInfo);
                }
            } else {
                logger.debug("Device definition for '{}' not found to get a value ({})", pathInfo.getItemName(),
                        pathInfo);
            }

            NeeoUtil.write(resp, gson.toJson(niv));
        } finally {
            logger.debug("handleGetValue {}: {}", pathInfo, niv.getValue());
        }
    }

    /**
     * Handle unsubscribing from a device by removing all device keys for the related {@link ThingUID}
     *
     * @param resp the non-null response to write to
     * @param adapterName the non-empty adapter name
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void handleUnsubscribe(HttpServletResponse resp, String adapterName) throws IOException {
        Objects.requireNonNull(resp, "resp cannot be null");
        NeeoUtil.requireNotEmpty(adapterName, "adapterName cannot be empty");

        logger.debug("handleUnsubscribe {}", adapterName);

        try {
            final NeeoThingUID uid = new NeeoThingUID(adapterName);
            api.getDeviceKeys().remove(uid);
            NeeoUtil.write(resp, gson.toJson(ReturnStatus.SUCCESS));
        } catch (IllegalArgumentException e) {
            logger.debug("AdapterName {} is not a valid thinguid - ignoring", adapterName);
            NeeoUtil.write(resp, gson.toJson(new ReturnStatus("AdapterName not a valid ThingUID: " + adapterName)));
        }
    }

    /**
     * Handle subscribe to a device by adding the device key to the API for the related {@link ThingUID}
     *
     * @param resp the non-null response to write to
     * @param adapterName the non-empty adapter name
     * @param deviceKey the non-empty device key
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void handleSubscribe(HttpServletResponse resp, String adapterName, String deviceKey) throws IOException {
        Objects.requireNonNull(resp, "resp cannot be null");
        NeeoUtil.requireNotEmpty(adapterName, "adapterName cannot be empty");
        NeeoUtil.requireNotEmpty(deviceKey, "deviceKey cannot be empty");

        logger.debug("handleSubscribe {}/{}", adapterName, deviceKey);

        try {
            final NeeoThingUID uid = new NeeoThingUID(adapterName);
            api.getDeviceKeys().put(uid, deviceKey);
            NeeoUtil.write(resp, gson.toJson(ReturnStatus.SUCCESS));
        } catch (IllegalArgumentException e) {
            logger.debug("AdapterName {} is not a valid thinguid - ignoring", adapterName);
            NeeoUtil.write(resp, gson.toJson(new ReturnStatus("AdapterName not a valid ThingUID: " + adapterName)));
        }
    }

    /**
     * Handle a directory request
     *
     * @param req the non-null request to use
     * @param resp the non-null response to write to
     * @param pathInfo the non-null path information
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void handleDirectory(HttpServletRequest req, HttpServletResponse resp, PathInfo pathInfo)
            throws IOException {
        Objects.requireNonNull(req, "req cannot be null");
        Objects.requireNonNull(resp, "resp cannot be null");
        Objects.requireNonNull(pathInfo, "pathInfo cannot be null");

        logger.debug("handleDirectory {}", pathInfo);

        final NeeoDevice device = context.getDefinitions().getDevice(pathInfo.getThingUid());
        if (device != null) {
            final NeeoDeviceChannel channel = device.getChannel(pathInfo.getItemName(), pathInfo.getSubType(),
                    pathInfo.getChannelNbr());
            if ("action".equalsIgnoreCase(pathInfo.getActionValue())) {
                final NeeoDirectoryRequestAction discoveryAction = gson.fromJson(req.getReader(),
                        NeeoDirectoryRequestAction.class);

                try {
                    final Item item = context.getItemRegistry().getItem(pathInfo.getItemName());
                    final Command cmd = NeeoItemValueConverter.convert(item, pathInfo,
                            discoveryAction.getActionIdentifier());
                    if (cmd != null) {
                        final ItemCommandEvent event = ItemEventFactory.createCommandEvent(item.getName(), cmd);
                        logger.debug("Posting item event: {}", event);
                        context.getEventPublisher().post(event);
                    } else {
                        logger.debug("Cannot set value (directory) - no command for path: {}", pathInfo);
                    }
                } catch (ItemNotFoundException e) {
                    logger.debug("Cannot set value(directory)  - no linked items: {}", pathInfo);
                }

            } else {
                if (channel instanceof NeeoDeviceChannelDirectory directoryChannel) {
                    final NeeoDirectoryRequest discoveryRequest = gson.fromJson(req.getReader(),
                            NeeoDirectoryRequest.class);
                    NeeoUtil.write(resp, gson.toJson(new NeeoDirectoryResult(discoveryRequest, directoryChannel)));
                } else {
                    logger.debug("Channel definition for '{}' not found to directory set value ({})",
                            pathInfo.getItemName(), pathInfo);
                }
            }
        } else {
            logger.debug("Device definition for '{}' not found to directory set value ({})", pathInfo.getItemName(),
                    pathInfo);

        }
    }

    /**
     * Returns the {@link EventFilter} used by this service. The {@link EventFilter} will simply filter for those items
     * that have been bound
     *
     * @return a non-null {@link EventFilter}
     */
    @NonNull
    @Override
    public EventFilter getEventFilter() {
        return new EventFilter() {

            @Override
            public boolean apply(@Nullable Event event) {
                Objects.requireNonNull(event, "event cannot be null");

                final ItemStateChangedEvent ise = (ItemStateChangedEvent) event;
                final String itemName = ise.getItemName();

                final NeeoDeviceKeys keys = api.getDeviceKeys();
                final boolean isBound = context.getDefinitions().isBound(keys, itemName);
                logger.trace("Apply Event: {} --- {} --- {} = {}", event, itemName, isBound, keys);
                return isBound;
            }
        };
    }

    /**
     * Handles the event by notifying the NEEO brain of the new value. If the channel has been linked to the
     * {@link NeeoButtonGroup#POWERONOFF}, then the related recipe will be powered on/off (in addition to sending the
     * new value).
     *
     * @see DefaultServletService#handleEvent(Event)
     *
     */
    @Override
    public boolean handleEvent(Event event) {
        Objects.requireNonNull(event, "event cannot be null");

        final ItemStateChangedEvent ise = (ItemStateChangedEvent) event;
        final String itemName = ise.getItemName();

        logger.trace("handleEvent: {}", event);
        notifyState(itemName, ise.getItemState());

        return true;
    }

    /**
     * Helper function to send the current state of all bound channels
     */
    private void resendState() {
        for (final Entry<NeeoDevice, NeeoDeviceChannel> boundEntry : context.getDefinitions()
                .getBound(api.getDeviceKeys())) {

            final NeeoDevice device = boundEntry.getKey();
            final NeeoDeviceChannel channel = boundEntry.getValue();

            try {
                final State state = context.getItemRegistry().getItem(channel.getItemName()).getState();

                for (String deviceKey : api.getDeviceKeys().get(device.getUid())) {
                    sendNotification(channel, deviceKey, state);
                }
            } catch (ItemNotFoundException e) {
                logger.debug("Item not found {}", channel.getItemName());
            }
        }
    }

    /**
     * Helper function to send some state for an itemName to the brain
     *
     * @param itemName a non-null, non-empty item name
     * @param state a non-null state
     */
    private void notifyState(String itemName, State state) {
        NeeoUtil.requireNotEmpty(itemName, "itemName cannot be empty");
        Objects.requireNonNull(state, "state cannot be null");

        logger.trace("notifyState: {} --- {}", itemName, state);

        for (final Entry<NeeoDevice, NeeoDeviceChannel> boundEntry : context.getDefinitions()
                .getBound(api.getDeviceKeys(), itemName)) {
            final NeeoDevice device = boundEntry.getKey();
            final NeeoDeviceChannel channel = boundEntry.getValue();
            final NeeoThingUID uid = new NeeoThingUID(device.getUid());

            logger.trace("notifyState (device): {} --- {} ", uid, channel);
            for (String deviceKey : api.getDeviceKeys().get(uid)) {
                logger.trace("notifyState (key): {} --- {}", uid, deviceKey);

                if (state instanceof OnOffType) {
                    Boolean recipeState = null;
                    final String label = channel.getLabel();
                    if (NeeoButtonGroup.POWERONOFF.getText().equalsIgnoreCase(label)) {
                        recipeState = state == OnOffType.ON;
                    } else if (state == OnOffType.ON && ButtonInfo.POWERON.getLabel().equalsIgnoreCase(label)) {
                        recipeState = true;
                    } else if (state == OnOffType.OFF && ButtonInfo.POWEROFF.getLabel().equalsIgnoreCase(label)) {
                        recipeState = false;
                    }

                    if (recipeState != null) {
                        logger.trace("notifyState (executeRecipe): {} --- {} --- {}", uid, deviceKey, recipeState);
                        final boolean turnOn = recipeState;
                        scheduler.submit(() -> {
                            try {
                                api.executeRecipe(deviceKey, turnOn);
                            } catch (IOException e) {
                                logger.debug("Exception occurred while handling executing a recipe: {}", e.getMessage(),
                                        e);
                            }
                        });
                    }
                }

                sendNotification(channel, deviceKey, state);
            }
        }
    }

    /**
     * Helper method to send a notification
     *
     * @param channel a non-null channel
     * @param deviceKey a non-null, non-empty device id
     * @param state a non-null state
     */
    private void sendNotification(NeeoDeviceChannel channel, String deviceKey, State state) {
        Objects.requireNonNull(channel, "channel cannot be null");
        NeeoUtil.requireNotEmpty(deviceKey, "deviceKey cannot be empty");
        Objects.requireNonNull(state, "state cannot be null");

        scheduler.execute(() -> {
            final String uin = channel.getUniqueItemName();

            final NeeoItemValue niv = itemConverter.convert(channel, state);

            // Use sensor notification if we have a >= 0.50 firmware AND it's not a power sensor
            if (api.getSystemInfo().isFirmwareGreaterOrEqual(NeeoConstants.NEEO_FIRMWARE_0_51_1)
                    && channel.getType() != NeeoCapabilityType.SENSOR_POWER) {
                final NeeoSensorNotification notify = new NeeoSensorNotification(deviceKey, uin, niv.getValue());
                try {
                    api.notify(gson.toJson(notify));
                } catch (IOException e) {
                    logger.debug("Exception occurred while handling event: {}", e.getMessage(), e);
                }
            } else {
                final NeeoNotification notify = new NeeoNotification(deviceKey, uin, niv.getValue());
                try {
                    api.notify(gson.toJson(notify));
                } catch (IOException e) {
                    logger.debug("Exception occurred while handling event: {}", e.getMessage(), e);
                }
            }
        });
    }

    /**
     * Simply closes the {@link #request}
     *
     * @see DefaultServletService#close()
     */
    @Override
    public void close() {
        this.api.removePropertyChangeListener(listener);
        request.close();
    }
}
