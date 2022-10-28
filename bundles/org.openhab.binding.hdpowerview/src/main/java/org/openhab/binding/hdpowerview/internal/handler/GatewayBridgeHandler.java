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
package org.openhab.binding.hdpowerview.internal.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.hdpowerview.internal.GatewayWebTargets;
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.HDPowerViewTranslationProvider;
import org.openhab.binding.hdpowerview.internal.api.gen3.Scene;
import org.openhab.binding.hdpowerview.internal.api.gen3.Shade;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewHubConfiguration;
import org.openhab.binding.hdpowerview.internal.exceptions.HubProcessingException;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridge handler for an HD PowerView Generation 3 Gateway.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class GatewayBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(GatewayBridgeHandler.class);
    private final String channelTypeId = HDPowerViewBindingConstants.CHANNELTYPE_SCENE_ACTIVATE;
    private final String channelGroupId = HDPowerViewBindingConstants.CHANNEL_GROUP_SCENES;

    private final HttpClient httpClient;
    private final HDPowerViewTranslationProvider translationProvider;
    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;

    private @Nullable GatewayWebTargets webTargets;
    private @Nullable ScheduledFuture<?> refreshTask;

    private boolean scenesLoaded;
    private boolean propertiesLoaded;
    private boolean isDisposing;

    public GatewayBridgeHandler(Bridge bridge, HttpClient httpClient,
            HDPowerViewTranslationProvider translationProvider, ClientBuilder clientBuilder,
            SseEventSourceFactory eventSourceFactory) {
        super(bridge);
        this.httpClient = httpClient;
        this.translationProvider = translationProvider;
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
    }

    @Override
    public void dispose() {
        isDisposing = true;
        ScheduledFuture<?> future = this.refreshTask;
        if (future != null) {
            future.cancel(true);
        }
        this.refreshTask = null;

        GatewayWebTargets webTargets = this.webTargets;
        if (webTargets != null) {
            try {
                webTargets.close();
            } catch (IOException e) {
            }
            this.webTargets = null;
        }
    }

    /**
     * Refresh the state of all things. Normally the thing's position state is updated by SSE. However we must do a
     * refresh once on start up in order to get the initial state. Also the other properties (battery, signal strength
     * etc.) are not updated by SSE. Furthermore we need to do periodic refreshes just in case the SSE connection may
     * have been lost.
     */
    private void doRefresh() {
        try {
            getWebTargets().openSSE();
            refreshProperties();
            refreshShades();
            refreshScenes();
        } catch (IllegalStateException | HubProcessingException e) {
            logger.warn("doRefresh() exception:{}, message:{}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * Getter for the list of all child shade thing handlers.
     *
     * @return the list of shade handlers.
     * @throws IllegalStateException if the bridge is not properly initialized.
     */
    private List<ShadeThingHandler> getShadeThingHandlers() throws IllegalStateException {
        Bridge bridge = getBridge();
        if (bridge != null) {
            List<ShadeThingHandler> result = new ArrayList<>();
            bridge.getThings().stream().map(thing -> thing.getHandler()).forEach(handler -> {
                if (handler instanceof ShadeThingHandler) {
                    result.add((ShadeThingHandler) handler);
                }
            });
            return result;
        }
        throw new IllegalStateException("Bridge not initialized.");
    }

    /**
     * Getter for the webTargets.
     *
     * @return the webTargets.
     * @throws IllegalStateException if webTargets is not initialized.
     */
    public GatewayWebTargets getWebTargets() throws IllegalStateException {
        GatewayWebTargets webTargets = this.webTargets;
        if (webTargets != null) {
            return webTargets;
        }
        throw new IllegalStateException("WebTargets not initialized.");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            scheduler.submit(() -> doRefresh());
            return;
        }
        Channel channel = getThing().getChannel(channelUID.getId());
        if (channel == null) {
            return;
        }
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID == null) {
            return;
        }
        if (channelTypeId.equals(channelTypeUID.getId()) && OnOffType.ON == command) {
            try {
                getWebTargets().activateScene(Integer.parseInt(channelUID.getIdWithoutGroup()));
            } catch (HubProcessingException | IllegalStateException e) {
                logger.warn("handleCommand() exception:{}, message:{}", e.getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    @Override
    public void initialize() {
        HDPowerViewHubConfiguration config = getConfigAs(HDPowerViewHubConfiguration.class);
        String host = config.host;

        if (host == null || host.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.no-host-address");
            return;
        }

        webTargets = new GatewayWebTargets(this, httpClient, clientBuilder, eventSourceFactory, host);
        scenesLoaded = false;
        propertiesLoaded = false;
        isDisposing = false;

        /*
         * Normally the thing's position state is updated by SSE. However we must do a refresh once on start up in order
         * to get the initial state. Also the other properties (battery, signal strength etc.) are not updated by SSE.
         * Furthermore we need to do periodic refreshes just in case the SSE connection may have been lost. So we
         * schedule the refresh at the 'hardRefresh' interval.
         */
        ScheduledFuture<?> refreshTask = this.refreshTask;
        if (refreshTask != null) {
            refreshTask.cancel(false);
        }
        this.refreshTask = scheduler.scheduleWithFixedDelay(() -> doRefresh(), 10, config.hardRefresh,
                TimeUnit.MINUTES);

        updateStatus(ThingStatus.UNKNOWN);
    }

    /**
     * Method that is called when a scene changes state.
     *
     * @param scene the one that changed.
     */
    public void onSceneEvent(Scene scene) {
        // TODO perhaps we should trigger an OH core event here ??
    }

    /**
     * Method that is called when a shade changes state.
     *
     * @param shade the one that changed.
     */
    public void onShadeEvent(Shade shade) {
        try {
            for (ShadeThingHandler handler : getShadeThingHandlers()) {
                if (isDisposing || handler.notify(shade)) {
                    break;
                }
            }
        } catch (IllegalStateException e) {
            logger.warn("onShadeEvent() exception:{}, message:{}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private void refreshProperties() throws HubProcessingException, IllegalStateException {
        if (propertiesLoaded || isDisposing) {
            return;
        }
        thing.setProperties(getWebTargets().getInformation());
        propertiesLoaded = true;
    }

    /**
     * Create the dynamic list of scene channels.
     *
     * @throws HubProcessingException if the web target connection caused an error.
     * @throws IllegalStateException if this handler is in an illegal state.
     */
    private void refreshScenes() throws HubProcessingException, IllegalStateException {
        if (scenesLoaded || isDisposing) {
            return;
        }
        ChannelTypeUID typeUID = new ChannelTypeUID(channelTypeId);
        ChannelGroupUID groupUID = new ChannelGroupUID(thing.getUID(), channelGroupId);
        List<Channel> channels = new ArrayList<>();
        for (Scene scene : getWebTargets().getScenes()) {
            ChannelUID channelUID = new ChannelUID(groupUID, Integer.toString(scene.getId()));
            String name = scene.getName();
            String description = translationProvider.getText("dynamic-channel.scene-activate.description", name);
            channels.add(ChannelBuilder.create(channelUID, CoreItemFactory.SWITCH).withType(typeUID).withLabel(name)
                    .withDescription(description).withAutoUpdatePolicy(AutoUpdatePolicy.VETO).build());
        }
        updateThing(editThing().withChannels(channels).build());
        scenesLoaded = true;
    }

    /**
     * Get the full list of shades data and notify each of the thing handlers.
     *
     * @throws HubProcessingException if the web target connection caused an error.
     * @throws IllegalStateException if this handler is in an illegal state.
     */
    private void refreshShades() throws HubProcessingException, IllegalStateException {
        List<ShadeThingHandler> handlers = getShadeThingHandlers();
        for (Shade shade : getWebTargets().getShades()) {
            for (ShadeThingHandler handler : handlers) {
                if (isDisposing || handler.notify(shade)) {
                    break;
                }
            }
        }
    }
}
