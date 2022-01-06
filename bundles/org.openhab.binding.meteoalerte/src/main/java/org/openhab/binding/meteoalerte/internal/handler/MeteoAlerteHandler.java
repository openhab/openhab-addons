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
package org.openhab.binding.meteoalerte.internal.handler;

import static org.openhab.binding.meteoalerte.internal.MeteoAlerteBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meteoalerte.internal.MeteoAlerteConfiguration;
import org.openhab.binding.meteoalerte.internal.json.ApiResponse;
import org.openhab.binding.meteoalerte.internal.json.ResponseFieldDTO.AlertLevel;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link MeteoAlerteHandler} is responsible for updating channels
 * and querying the API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MeteoAlerteHandler extends BaseThingHandler {
    private static final String URL = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=risques-meteorologiques-copy&"
            + "facet=etat_vent&facet=etat_pluie_inondation&facet=etat_orage&facet=etat_inondation&facet=etat_neige&facet=etat_canicule&"
            + "facet=etat_grand_froid&facet=etat_avalanches&refine.nom_dept=%s";
    private static final int TIMEOUT_MS = 30000;
    private static final String UNKNOWN_COLOR = "b3b3b3";
    private static final Map<AlertLevel, String> ALERT_COLORS = Map.ofEntries(Map.entry(AlertLevel.GREEN, "00ff00"),
            Map.entry(AlertLevel.YELLOW, "ffff00"), Map.entry(AlertLevel.ORANGE, "ff6600"),
            Map.entry(AlertLevel.RED, "ff0000"), Map.entry(AlertLevel.UNKNOWN, UNKNOWN_COLOR));

    private static final Map<AlertLevel, State> ALERT_LEVELS = Map.ofEntries(
            Map.entry(AlertLevel.GREEN, DecimalType.ZERO), Map.entry(AlertLevel.YELLOW, new DecimalType(1)),
            Map.entry(AlertLevel.ORANGE, new DecimalType(2)), Map.entry(AlertLevel.RED, new DecimalType(3)));

    private final Logger logger = LoggerFactory.getLogger(MeteoAlerteHandler.class);
    private final Gson gson;
    private @Nullable ScheduledFuture<?> refreshJob;
    private String queryUrl = "";

    public MeteoAlerteHandler(Thing thing, Gson gson) {
        super(thing);
        this.gson = gson;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Météo Alerte handler.");

        MeteoAlerteConfiguration config = getConfigAs(MeteoAlerteConfiguration.class);
        logger.debug("config department = {}", config.department);
        logger.debug("config refresh = {}", config.refresh);

        updateStatus(ThingStatus.UNKNOWN);
        queryUrl = String.format(URL, config.department);
        refreshJob = scheduler.scheduleWithFixedDelay(this::updateAndPublish, 0, config.refresh, TimeUnit.MINUTES);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Météo Alerte handler.");
        ScheduledFuture<?> localJob = refreshJob;
        if (localJob != null) {
            localJob.cancel(true);
        }
        refreshJob = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateAndPublish();
        }
    }

    private void updateAndPublish() {
        try {
            if (queryUrl.isEmpty()) {
                throw new MalformedURLException("queryUrl not initialized");
            }
            String response = HttpUtil.executeUrl("GET", queryUrl, TIMEOUT_MS);
            if (response == null) {
                throw new IOException("Empty response");
            }
            updateStatus(ThingStatus.ONLINE);
            ApiResponse apiResponse = gson.fromJson(response, ApiResponse.class);
            updateChannels(Objects.requireNonNull(apiResponse));
        } catch (MalformedURLException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    String.format("Querying '%s' raised : %s", queryUrl, e.getMessage()));
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Update the channel from the last Meteo Alerte data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     */
    private void updateChannels(ApiResponse apiResponse) {
        apiResponse.getRecords().findFirst().ifPresent((record) -> record.getResponseFieldDTO().ifPresent(fields -> {
            updateAlert(WIND, fields.getVent());
            updateAlert(RAIN, fields.getPluieInondation());
            updateAlert(STORM, fields.getOrage());
            updateAlert(FLOOD, fields.getInondation());
            updateAlert(SNOW, fields.getNeige());
            updateAlert(HEAT, fields.getCanicule());
            updateAlert(FREEZE, fields.getGrandFroid());
            updateAlert(AVALANCHE, fields.getAvalanches());
            updateAlert(WAVE, fields.getVagueSubmersion());
            updateState(COMMENT, new StringType(fields.getVigilanceComment()));
            fields.getDateInsert().ifPresent(date -> updateDate(OBSERVATION_TIME, date));
            fields.getDatePrevue().ifPresent(date -> updateDate(END_TIME, date));
        }));
    }

    private byte @Nullable [] getResource(String iconPath) {
        ClassLoader classLoader = MeteoAlerteHandler.class.getClassLoader();
        if (classLoader != null) {
            try (InputStream stream = classLoader.getResourceAsStream(iconPath)) {
                return stream != null ? stream.readAllBytes() : null;
            } catch (IOException e) {
                logger.warn("Unable to load ressource '{}' : {}", iconPath, e.getMessage());
            }
        }
        return null;
    }

    private void updateAlert(String channelId, AlertLevel value) {
        String channelIcon = channelId + "-icon";
        if (isLinked(channelId)) {
            updateState(channelId, ALERT_LEVELS.getOrDefault(value, UnDefType.UNDEF));
        }
        if (isLinked(channelIcon)) {
            State result = UnDefType.UNDEF;
            byte[] bytes = getResource(String.format("picto/%s.svg", channelId));
            if (bytes != null) {
                String resource = new String(bytes, StandardCharsets.UTF_8);
                resource = resource.replaceAll(UNKNOWN_COLOR, ALERT_COLORS.getOrDefault(value, UNKNOWN_COLOR));
                result = new RawType(resource.getBytes(StandardCharsets.UTF_8), "image/svg+xml");
            }
            updateState(channelIcon, result);
        }
    }

    private void updateDate(String channelId, ZonedDateTime zonedDateTime) {
        if (isLinked(channelId)) {
            updateState(channelId, new DateTimeType(zonedDateTime));
        }
    }
}
