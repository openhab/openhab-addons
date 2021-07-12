/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.amplipi.internal;

import static org.openhab.binding.amplipi.internal.AmpliPiBindingConstants.CHANNEL_PRESET;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.ResponseExceptionMapper;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amplipi.internal.api.GroupApi;
import org.openhab.binding.amplipi.internal.api.PresetApi;
import org.openhab.binding.amplipi.internal.api.StatusApi;
import org.openhab.binding.amplipi.internal.api.ZoneApi;
import org.openhab.binding.amplipi.internal.model.Preset;
import org.openhab.binding.amplipi.internal.model.Status;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

/**
 * The {@link AmpliPiHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class AmpliPiHandler extends BaseBridgeHandler {

    private static final int REQUEST_TIMEOUT = 5000;

    private final Logger logger = LoggerFactory.getLogger(AmpliPiHandler.class);

    private @Nullable StatusApi statusApi;
    private @Nullable PresetApi presetApi;
    private @Nullable ZoneApi zoneApi;
    private @Nullable GroupApi groupApi;

    private List<Preset> presets = List.of();
    private List<AmpliPiStatusChangeListener> changeListeners = new ArrayList<>();

    private @Nullable ScheduledFuture<?> refreshJob;

    public AmpliPiHandler(Thing thing) {
        super((Bridge) thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_PRESET.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                updateState(channelUID, UnDefType.NULL);
            } else if (command instanceof DecimalType) {
                DecimalType preset = (DecimalType) command;
                if (presetApi != null) {
                    try {
                        presetApi.loadPresetApiPresetsPidLoadPost(preset.intValue());
                    } catch (ProcessingException e) {
                        handleProcessingException(e);
                    }
                } else {
                    logger.error("Handler not correctly initialized!");
                }
            }
        }
    }

    @Override
    public void initialize() {
        AmpliPiConfiguration config = getConfigAs(AmpliPiConfiguration.class);

        JacksonJsonProvider provider = new JacksonJsonProvider();
        List<Object> providers = new ArrayList<>();
        providers.add(provider);
        providers.add((ResponseExceptionMapper<?>) (@Nullable Response r) -> new IOException());

        String url = "http://" + config.hostname;
        statusApi = JAXRSClientFactory.create(url, StatusApi.class, providers);
        presetApi = JAXRSClientFactory.create(url, PresetApi.class, providers);
        zoneApi = JAXRSClientFactory.create(url, ZoneApi.class, providers);
        groupApi = JAXRSClientFactory.create(url, GroupApi.class, providers);
        HTTPClientPolicy clientConfig = WebClient.getConfig(statusApi).getHttpConduit().getClient();
        clientConfig.setReceiveTimeout(REQUEST_TIMEOUT);
        clientConfig = WebClient.getConfig(presetApi).getHttpConduit().getClient();
        clientConfig.setReceiveTimeout(REQUEST_TIMEOUT);
        clientConfig = WebClient.getConfig(zoneApi).getHttpConduit().getClient();
        clientConfig.setReceiveTimeout(REQUEST_TIMEOUT);
        clientConfig = WebClient.getConfig(groupApi).getHttpConduit().getClient();
        clientConfig.setReceiveTimeout(REQUEST_TIMEOUT);

        updateStatus(ThingStatus.UNKNOWN);

        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                if (statusApi != null) {
                    Status currentStatus = statusApi.getStatusApiGet();
                    updateStatus(ThingStatus.ONLINE);
                    setProperties(currentStatus);
                    presets = currentStatus.getPresets();
                    changeListeners.forEach(l -> l.receive(currentStatus));
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "API Client is not initialized.");
                }
            } catch (ProcessingException e) {
                handleProcessingException(e);
            } catch (Exception e) {
                logger.error("Unexpected error occurred: {}", e.getMessage());
            }
        }, 0, config.refreshInterval, TimeUnit.SECONDS);
    }

    private void setProperties(Status currentStatus) {
        String version = currentStatus.getInfo().getVersion();
        Map<String, String> props = editProperties();
        props.put("firmwareVersion", version);
        updateProperties(props);
    }

    private void handleProcessingException(ProcessingException e) {
        Throwable cause = e.getCause();
        String msg = cause == null ? e.getMessage() : cause.getMessage();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, msg);
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(PresetCommandOptionProvider.class, AmpliPiZoneAndGroupDiscoveryService.class);
    }

    public List<Preset> getPresets() {
        return presets;
    }

    public void addStatusChangeListener(AmpliPiStatusChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeStatusChangeListener(AmpliPiStatusChangeListener listener) {
        changeListeners.remove(listener);
    }

    public @Nullable ZoneApi getZoneApi() {
        return zoneApi;
    }

    public @Nullable GroupApi getGroupApi() {
        return groupApi;
    }
}
