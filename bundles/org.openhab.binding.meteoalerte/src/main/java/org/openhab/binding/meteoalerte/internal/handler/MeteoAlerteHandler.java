/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.meteoalerte.internal.MeteoAlerteConfiguration;
import org.openhab.binding.meteoalerte.internal.json.ApiResponse;
import org.openhab.binding.meteoalerte.internal.json.Record;
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
            + "facet=etat_grand_froid&facet=etat_avalanches&refine.nom_dept=";
    private static final ArrayList<String> ALERT_LEVELS = new ArrayList<>(
            Arrays.asList("Vert", "Jaune", "Orange", "Rouge"));
    private final Logger logger = LoggerFactory.getLogger(MeteoAlerteHandler.class);
    private final Gson gson = new Gson();

    private @NonNullByDefault({}) ScheduledFuture<?> refreshJob;
    private @NonNullByDefault({}) String queryUrl;

    public MeteoAlerteHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Météo Alerte handler.");

        MeteoAlerteConfiguration config = getConfigAs(MeteoAlerteConfiguration.class);
        logger.debug("config department = {}", config.department);
        logger.debug("config refresh = {}", config.refresh);

        updateStatus(ThingStatus.UNKNOWN);
        queryUrl = URL + config.department;
        refreshJob = scheduler.scheduleWithFixedDelay(this::updateAndPublish, 0, config.refresh, TimeUnit.HOURS);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Météo Alerte handler.");

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateAndPublish();
        } else {
            logger.debug("The Air Quality binding is read-only and can not handle command {}", command);
        }
    }

    private void updateAndPublish() {
        try {
            ApiResponse apiResponse = getMeteoAlerteData();
            if (apiResponse != null) {
                updateChannels(apiResponse);
            }
        } catch (Exception e) {
            logger.error("Exception occurred during execution: {}", e.getMessage(), e);
        }
    }

    /**
     * Request new weather alert data to the webservice
     *
     * @return the api data object mapping the JSON response or null in case of error
     */
    private @Nullable ApiResponse getMeteoAlerteData() {
        try {
            URL url = new URL(queryUrl);
            try {
                URLConnection connection = url.openConnection();
                String response = IOUtils.toString(connection.getInputStream());
                ApiResponse result = gson.fromJson(response, ApiResponse.class);
                IOUtils.closeQuietly(connection.getInputStream());
                updateStatus(ThingStatus.ONLINE);
                return result;
            } catch (IOException e) {
                logger.warn("Error opening connection to Meteo Alerte webservice : {}", e);
            }
        } catch (MalformedURLException e) {
            logger.error("Malformed URL in Météo Alerte request : {}", queryUrl);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
        return null;
    }

    /**
     * Update the channel from the last Air Quality data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     */

    private void updateChannels(ApiResponse apiResponse) {
        Record record = apiResponse.getRecords()[0];
        if (record != null) {
            updateState(WIND, new StringType(Integer.toString(ALERT_LEVELS.indexOf(record.getFields().getEtatVent()))));
            updateState(RAIN, new StringType(
                    Integer.toString(ALERT_LEVELS.indexOf(record.getFields().getEtatPluieInondation()))));
            updateState(STORM,
                    new StringType(Integer.toString(ALERT_LEVELS.indexOf(record.getFields().getEtatOrage()))));
            updateState(FLOOD,
                    new StringType(Integer.toString(ALERT_LEVELS.indexOf(record.getFields().getEtatInondation()))));
            updateState(SNOW,
                    new StringType(Integer.toString(ALERT_LEVELS.indexOf(record.getFields().getEtatNeige()))));
            updateState(HEAT,
                    new StringType(Integer.toString(ALERT_LEVELS.indexOf(record.getFields().getEtatCanicule()))));
            updateState(FREEZE,
                    new StringType(Integer.toString(ALERT_LEVELS.indexOf(record.getFields().getEtatGrandFroid()))));
            updateState(AVALANCHE,
                    new StringType(Integer.toString(ALERT_LEVELS.indexOf(record.getFields().getEtatAvalanches()))));
            updateState(OBSERVATIONTIME, new StringType(record.getFields().getDateInsert()));
            updateState(COMMENT, new StringType(record.getFields().getVigilanceCommentaireTexte()));
        }
    }
}
