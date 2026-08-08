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
package org.openhab.binding.amplipi.internal;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.amplipi.internal.model.Status;
import org.openhab.binding.amplipi.internal.model.Zone;
import org.openhab.binding.amplipi.internal.model.ZoneUpdate;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link AmpliPiGroupHandler} is responsible for handling commands, which are
 * sent to one of the AmpliPi Groups.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class AmpliPiZoneHandler extends BaseThingHandler implements AmpliPiStatusChangeListener {

    private final Logger logger = LoggerFactory.getLogger(AmpliPiZoneHandler.class);

    private final HttpClient httpClient;
    private final Gson gson;

    private @Nullable AmpliPiHandler bridgeHandler;

    private @Nullable Zone zoneState;

    public AmpliPiZoneHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
        this.gson = new Gson();
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            bridgeHandler = (AmpliPiHandler) bridge.getHandler();
            if (bridgeHandler != null) {
                bridgeHandler.addStatusChangeListener(this);
            } else {
                throw new IllegalStateException("Bridge handler must not be null here!");
            }
            if (bridge.getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            throw new IllegalStateException("Bridge must not be null here!");
        }
    }

    private int getId(Thing thing) {
        return Integer.valueOf(thing.getConfiguration().get(AmpliPiBindingConstants.CFG_PARAM_ID).toString());
    }

    private int getVolumeDelta(Thing thing) {
        return Integer.valueOf(thing.getConfiguration().get(AmpliPiBindingConstants.CFG_PARAM_VOLUME_DELTA).toString());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            // do nothing - we just wait for the next automatic refresh
            return;
        }
        ZoneUpdate update = new ZoneUpdate();
        switch (channelUID.getId()) {
            case AmpliPiBindingConstants.CHANNEL_POWER:
                if (command instanceof OnOffType) {
                    update.setMute(command == OnOffType.OFF);
                }
                break;
            case AmpliPiBindingConstants.CHANNEL_MUTE:
                if (command instanceof OnOffType) {
                    update.setMute(command == OnOffType.ON);
                }
                break;
            case AmpliPiBindingConstants.CHANNEL_VOLUME:
                Zone state = zoneState;
                // vol_f only exists from AmpliPi 0.1.8 on. A device whose status does not report it
                // cannot understand it when PATCHing either, so writes have to use the dB field too.
                boolean supportsVolumeFraction = state != null && state.getVolF() != null;
                int volumeDelta = getVolumeDelta(thing);
                if (command instanceof PercentType percentCommand) {
                    if (supportsVolumeFraction) {
                        update.setVolF(AmpliPiUtils.percentTypeToVolumeFraction(percentCommand));
                    } else {
                        update.setVol(AmpliPiUtils.percentTypeToVolume(percentCommand));
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    if (supportsVolumeFraction) {
                        // Send the step itself and let AmpliPi apply it to its own current volume.
                        // The cached zone state can be stale when the volume was changed through the
                        // AmpliPi UI or another client since the last poll, and this also leaves the
                        // bounds and overflow handling to the device.
                        double step = volumeDelta / 100.0;
                        update.setVolDeltaF(IncreaseDecreaseType.INCREASE.equals(command) ? step : -step);
                    } else if (state != null && state.getVol() != null) {
                        // Older firmware: keep adjusting the dB value against the cached state. The
                        // configured step is a percentage, so scale it over the dB range.
                        int stepDb = Math.max(1, (int) Math.round(
                                volumeDelta / 100.0 * (AmpliPiUtils.MAX_VOLUME_DB - AmpliPiUtils.MIN_VOLUME_DB)));
                        int current = state.getVol();
                        int newVol = IncreaseDecreaseType.INCREASE.equals(command)
                                ? Math.min(current + stepDb, AmpliPiUtils.MAX_VOLUME_DB)
                                : Math.max(current - stepDb, AmpliPiUtils.MIN_VOLUME_DB);
                        state.setVol(newVol);
                        update.setVol(newVol);
                    }
                }
                break;
            case AmpliPiBindingConstants.CHANNEL_SOURCE:
                if (command instanceof DecimalType decimalCommand) {
                    update.setSourceId(decimalCommand.intValue());
                }
                break;
        }
        if (bridgeHandler != null) {
            String url = bridgeHandler.getUrl() + "/api/zones/" + getId(thing);
            StringContentProvider contentProvider = new StringContentProvider(gson.toJson(update));
            try {
                ContentResponse response = httpClient.newRequest(url).method(HttpMethod.PATCH)
                        .content(contentProvider, "application/json").send();
                if (response.getStatus() != HttpStatus.OK_200) {
                    logger.error("AmpliPi API returned HTTP status {}.", response.getStatus());
                    logger.debug("Content: {}", response.getContentAsString());
                } else {
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "AmpliPi request failed: " + e.getMessage());
            }
        }
    }

    @Override
    public void receive(Status status) {
        int id = getId(thing);
        Optional<Zone> zone = status.getZones().stream().filter(z -> z.getId().equals(id)).findFirst();
        zone.ifPresent(this::updateZoneState);
    }

    private void updateZoneState(Zone state) {
        this.zoneState = state;

        Boolean power = !zoneState.getMute();
        Boolean mute = zoneState.getMute();
        Integer sourceId = zoneState.getSourceId();

        updateState(AmpliPiBindingConstants.CHANNEL_POWER, OnOffType.from(power));
        updateState(AmpliPiBindingConstants.CHANNEL_MUTE, OnOffType.from(mute));
        // Prefer the firmware-provided volume fraction; fall back to the dB value
        // for older firmware that does not report vol_f.
        Double volF = zoneState.getVolF();
        PercentType volume = volF != null ? AmpliPiUtils.volumeFractionToPercentType(volF)
                : AmpliPiUtils.volumeToPercentType(zoneState.getVol());
        updateState(AmpliPiBindingConstants.CHANNEL_VOLUME, volume);
        updateState(AmpliPiBindingConstants.CHANNEL_SOURCE, new DecimalType(sourceId));
    }
}
