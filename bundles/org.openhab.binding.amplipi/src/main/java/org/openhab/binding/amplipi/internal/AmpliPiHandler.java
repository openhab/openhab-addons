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
package org.openhab.binding.amplipi.internal;

import static org.openhab.binding.amplipi.internal.AmpliPiBindingConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.amplipi.internal.audio.PAAudioSink;
import org.openhab.binding.amplipi.internal.discovery.AmpliPiZoneAndGroupDiscoveryService;
import org.openhab.binding.amplipi.internal.model.Announcement;
import org.openhab.binding.amplipi.internal.model.Preset;
import org.openhab.binding.amplipi.internal.model.SourceUpdate;
import org.openhab.binding.amplipi.internal.model.Status;
import org.openhab.binding.amplipi.internal.model.Stream;
import org.openhab.core.audio.AudioHTTPServer;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
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

import com.google.gson.Gson;

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

    private final HttpClient httpClient;
    private AudioHTTPServer audioHTTPServer;
    private final Gson gson;
    private @Nullable String callbackUrl;

    private String url = "http://amplipi";
    private List<Preset> presets = List.of();
    private List<Stream> streams = List.of();
    private List<AmpliPiStatusChangeListener> changeListeners = new ArrayList<>();

    private @Nullable ScheduledFuture<?> refreshJob;

    public AmpliPiHandler(Thing thing, HttpClient httpClient, AudioHTTPServer audioHTTPServer,
            @Nullable String callbackUrl) {
        super((Bridge) thing);
        this.httpClient = httpClient;
        this.audioHTTPServer = audioHTTPServer;
        this.callbackUrl = callbackUrl;
        this.gson = new Gson();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        AmpliPiConfiguration config = getConfigAs(AmpliPiConfiguration.class);
        url = "http://" + config.hostname;

        if (CHANNEL_PRESET.equals(channelUID.getId())) {
            if (command instanceof RefreshType) {
                updateState(channelUID, UnDefType.NULL);
            } else if (command instanceof DecimalType decimalCommand) {
                try {
                    ContentResponse response = this.httpClient
                            .newRequest(url + "/api/presets/" + decimalCommand.intValue() + "/load")
                            .method(HttpMethod.POST).timeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS).send();
                    if (response.getStatus() != HttpStatus.OK_200) {
                        logger.error("AmpliPi API returned HTTP status {}.", response.getStatus());
                        logger.debug("Content: {}", response.getContentAsString());
                    }
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "AmpliPi request failed: " + e.getMessage());
                }
            }
        } else if (channelUID.getId().startsWith(CHANNEL_INPUT)) {
            if (command instanceof StringType stringCommand) {
                int source = Integer.valueOf(channelUID.getId().substring(CHANNEL_INPUT.length())) - 1;
                SourceUpdate update = new SourceUpdate();
                update.setInput(stringCommand.toString());
                try {
                    StringContentProvider contentProvider = new StringContentProvider(gson.toJson(update));
                    ContentResponse response = this.httpClient.newRequest(url + "/api/sources/" + source)
                            .method(HttpMethod.PATCH).content(contentProvider, "application/json")
                            .timeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS).send();
                    if (response.getStatus() != HttpStatus.OK_200) {
                        logger.error("AmpliPi API returned HTTP status {}.", response.getStatus());
                        logger.debug("Content: {}", response.getContentAsString());
                    }
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "AmpliPi request failed: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void initialize() {
        AmpliPiConfiguration config = getConfigAs(AmpliPiConfiguration.class);
        url = "http://" + config.hostname;

        updateStatus(ThingStatus.UNKNOWN);

        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                ContentResponse response = this.httpClient.newRequest(url + "/api")
                        .timeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS).send();
                if (response.getStatus() == HttpStatus.OK_200) {
                    Status currentStatus = this.gson.fromJson(response.getContentAsString(), Status.class);
                    if (currentStatus != null) {
                        updateStatus(ThingStatus.ONLINE);
                        setProperties(currentStatus);
                        setInputs(currentStatus);
                        presets = currentStatus.getPresets();
                        streams = currentStatus.getStreams();
                        changeListeners.forEach(l -> l.receive(currentStatus));
                    } else {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "No valid response from AmpliPi API.");
                        logger.debug("Received response: {}", response.getContentAsString());
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "AmpliPi API returned HTTP status " + response.getStatus() + ".");
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "AmpliPi request failed: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Unexpected error occurred: {}", e.getMessage());
            }
        }, 0, config.refreshInterval, TimeUnit.SECONDS);
    }

    private void setProperties(Status currentStatus) {
        String version = currentStatus.getInfo().getVersion();
        Map<String, String> props = editProperties();
        props.put(Thing.PROPERTY_FIRMWARE_VERSION, version);
        updateProperties(props);
    }

    private void setInputs(Status currentStatus) {
        currentStatus.getSources().forEach(source -> {
            updateState(CHANNEL_INPUT + (source.getId() + 1), new StringType(source.getInput()));
        });
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
        return Set.of(PresetCommandOptionProvider.class, InputStateOptionProvider.class,
                AmpliPiZoneAndGroupDiscoveryService.class, PAAudioSink.class);
    }

    public List<Preset> getPresets() {
        return presets;
    }

    public List<Stream> getStreams() {
        return streams;
    }

    public String getUrl() {
        return url;
    }

    public AudioHTTPServer getAudioHTTPServer() {
        return audioHTTPServer;
    }

    public void addStatusChangeListener(AmpliPiStatusChangeListener listener) {
        changeListeners.add(listener);
    }

    public void removeStatusChangeListener(AmpliPiStatusChangeListener listener) {
        changeListeners.remove(listener);
    }

    public void playPA(String audioUrl, @Nullable PercentType volume) {
        Announcement announcement = new Announcement();
        announcement.setMedia(audioUrl);
        if (volume != null) {
            announcement.setVol(AmpliPiUtils.percentTypeToVolume(volume));
        }
        String url = getUrl() + "/api/announce";
        StringContentProvider contentProvider = new StringContentProvider(gson.toJson(announcement));
        try {
            ContentResponse response = httpClient.newRequest(url).method(HttpMethod.POST)
                    .content(contentProvider, "application/json").send();
            if (response.getStatus() != HttpStatus.OK_200) {
                logger.error("AmpliPi API returned HTTP status {}.", response.getStatus());
                logger.debug("Content: {}", response.getContentAsString());
            } else {
                logger.debug("PA request sent successfully.");
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "AmpliPi request failed: " + e.getMessage());
        }
    }

    public @Nullable String getCallbackUrl() {
        return callbackUrl;
    }
}
