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
package org.openhab.binding.meteoalerte.internal.handler;

import static org.openhab.binding.meteoalerte.internal.MeteoAlerteBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.meteoalerte.internal.MeteoAlertIconProvider;
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
    private static final int TIMEOUT_MS = 30000;
    private static final String URL = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=risques-meteorologiques-copy&"
            + "facet=etat_vent&facet=etat_pluie_inondation&facet=etat_orage&facet=etat_inondation&facet=etat_neige&facet=etat_canicule&"
            + "facet=etat_grand_froid&facet=etat_avalanches&refine.nom_dept=%s";

    private final Logger logger = LoggerFactory.getLogger(MeteoAlerteHandler.class);
    private final MeteoAlertIconProvider iconProvider;
    private final Gson gson;

    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private String queryUrl = "";

    public MeteoAlerteHandler(Thing thing, Gson gson, MeteoAlertIconProvider iconProvider) {
        super(thing);
        this.gson = gson;
        this.iconProvider = iconProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Météo Alerte handler.");

        MeteoAlerteConfiguration config = getConfigAs(MeteoAlerteConfiguration.class);
        logger.debug("config department = {}", config.department);
        logger.debug("config refresh = {}", config.refresh);

        updateStatus(ThingStatus.UNKNOWN);
        queryUrl = URL.formatted(config.department);
        refreshJob = Optional
                .of(scheduler.scheduleWithFixedDelay(this::updateAndPublish, 0, config.refresh, TimeUnit.MINUTES));
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Météo Alerte handler.");

        refreshJob.ifPresent(job -> job.cancel(true));
        refreshJob = Optional.empty();
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
            updateChannels(Objects.requireNonNull(gson.fromJson(response, ApiResponse.class)));
        } catch (MalformedURLException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Querying '%s' error : %s".formatted(queryUrl, e.getMessage()));
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
        apiResponse.getRecords().findFirst().ifPresent(record -> record.getResponseFieldDTO().ifPresent(fields -> {
            updateAlert(WIND, fields.getVent());
            updateAlert(RAIN, fields.getPluieInondation());
            updateAlert(STORM, fields.getOrage());
            updateAlert(FLOOD, fields.getInondation());
            updateAlert(SNOW, fields.getNeige());
            updateAlert(HEAT, fields.getCanicule());
            updateAlert(FREEZE, fields.getGrandFroid());
            updateAlert(AVALANCHE, fields.getAvalanches());
            updateAlert(WAVE, fields.getVagueSubmersion());
            updateState(COMMENT, StringType.valueOf(fields.getVigilanceComment()));
            fields.getDateInsert().ifPresent(date -> updateDate(OBSERVATION_TIME, date));
            fields.getDatePrevue().ifPresent(date -> updateDate(END_TIME, date));
        }));
    }

    private void updateAlert(String channelId, AlertLevel value) {
        State state = value != AlertLevel.UNKNOWN ? new DecimalType(value.ordinal()) : UnDefType.NULL;
        if (isLinked(channelId)) {
            updateState(channelId, state);
        }

        String channelIcon = channelId + "-icon";
        if (isLinked(channelIcon)) {
            InputStream icon = iconProvider.getIcon(channelId, state.toString());
            if (icon != null) {
                try {
                    State result = new RawType(icon.readAllBytes(), "image/svg+xml");
                    updateState(channelIcon, result);
                } catch (IOException e) {
                    logger.warn("Error getting icon for channel {} and value {} : {}", channelId, value,
                            e.getMessage());
                }
            } else {
                logger.warn("Null icon returned for channel {} and state {}", channelIcon, state);
            }
        }
    }

    private void updateDate(String channelId, ZonedDateTime zonedDateTime) {
        if (isLinked(channelId)) {
            updateState(channelId, new DateTimeType(zonedDateTime));
        }
    }
}
