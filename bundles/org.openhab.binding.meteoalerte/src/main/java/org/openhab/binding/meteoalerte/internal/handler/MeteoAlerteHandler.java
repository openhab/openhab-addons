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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.meteoalerte.internal.MeteoAlerteConfiguration;
import org.openhab.binding.meteoalerte.internal.json.ApiResponse;
import org.osgi.framework.FrameworkUtil;
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
    private static final int TIMEOUT_MS = 30000;
    private static final List<String> ALERT_LEVELS = Arrays.asList("Vert", "Jaune", "Orange", "Rouge");
    private final Logger logger = LoggerFactory.getLogger(MeteoAlerteHandler.class);

    // Time zone provider representing time zone configured in openHAB configuration
    private final TimeZoneProvider timeZoneProvider;
    private final Gson gson;
    private @Nullable ScheduledFuture<?> refreshJob;
    private String queryUrl = "";

    public MeteoAlerteHandler(Thing thing, TimeZoneProvider timeZoneProvider, Gson gson) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
        this.gson = gson;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Météo Alerte handler.");

        MeteoAlerteConfiguration config = getConfigAs(MeteoAlerteConfiguration.class);
        logger.debug("config department = {}", config.department);
        logger.debug("config refresh = {}", config.refresh);

        updateStatus(ThingStatus.UNKNOWN);
        queryUrl = URL + config.department;
        refreshJob = scheduler.scheduleWithFixedDelay(this::updateAndPublish, 0, config.refresh, TimeUnit.MINUTES);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Météo Alerte handler.");
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        this.refreshJob = null;
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
            updateStatus(ThingStatus.ONLINE);
            ApiResponse apiResponse = gson.fromJson(response, ApiResponse.class);
            updateChannels(apiResponse);
        } catch (MalformedURLException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    String.format("Querying '%s' raised : %s", queryUrl, e.getMessage()));
        } catch (IOException e) {
            logger.warn("Error opening connection to Meteo Alerte webservice : {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Update the channel from the last Meteo Alerte data retrieved
     *
     * @param channelId the id identifying the channel to be updated
     */
    private void updateChannels(ApiResponse apiResponse) {
        Arrays.stream(apiResponse.getRecords()).findFirst()
                .ifPresent((record) -> record.getResponseFieldDTO().ifPresent(fields -> {
                    updateAlertString(WIND, fields.getVent());
                    updateAlertString(RAIN, fields.getPluieInondation());
                    updateAlertString(STORM, fields.getOrage());
                    updateAlertString(FLOOD, fields.getInondation());
                    updateAlertString(SNOW, fields.getNeige());
                    updateAlertString(HEAT, fields.getCanicule());
                    updateAlertString(FREEZE, fields.getGrandFroid());
                    updateAlertString(AVALANCHE, fields.getAvalanches());

                    fields.getDateInsert().ifPresent(date -> updateDate(OBSERVATION_TIME, date));
                    updateState(COMMENT, new StringType(fields.getVigilanceComment()));
                    updateIcon(WIND, fields.getVent());
                    updateIcon(RAIN, fields.getPluieInondation());
                    updateIcon(STORM, fields.getOrage());
                    updateIcon(FLOOD, fields.getInondation());
                    updateIcon(SNOW, fields.getNeige());
                    updateIcon(HEAT, fields.getCanicule());
                    updateIcon(FREEZE, fields.getGrandFroid());
                    updateIcon(AVALANCHE, fields.getAvalanches());
                }));
    }

    public void updateIcon(String channelId, String value) {
        String iconChannelId = channelId + "-icon";
        if (isLinked(iconChannelId)) {
            String pictoName = channelId + (!value.isEmpty() ? "_" + value.toLowerCase() : "");
            byte[] image = getImage("picto" + File.separator + pictoName + ".gif");
            if (image != null) {
                RawType picto = new RawType(image, "image/gif");
                updateState(iconChannelId, picto);
            }
        }
    }

    private byte @Nullable [] getImage(String iconPath) {
        URL url = FrameworkUtil.getBundle(getClass()).getResource(iconPath);
        logger.debug("Path to icon image resource is: {}", url);
        try (InputStream in = new BufferedInputStream(url.openStream())) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int next = in.read();
            while (next > -1) {
                bos.write(next);
                next = in.read();
            }
            bos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            logger.debug("I/O exception occurred getting image data: {}", e.getMessage(), e);
        }
        return null;
    }

    public void updateAlertString(String channelId, String value) {
        if (!value.isEmpty() && isLinked(channelId)) {
            int level = ALERT_LEVELS.indexOf(value);
            if (level != -1) {
                updateState(channelId, new StringType(Integer.toString(level)));
            } else {
                updateState(channelId, UnDefType.UNDEF);
                logger.warn("Value {} is not a valid alert level for channel {}", value, channelId);
            }
        }
    }

    public void updateDate(String channelId, ZonedDateTime zonedDateTime) {
        if (isLinked(channelId)) {
            ZonedDateTime localDateTime = zonedDateTime.withZoneSameInstant(timeZoneProvider.getTimeZone());
            updateState(channelId, new DateTimeType(localDateTime));
        }
    }

}
