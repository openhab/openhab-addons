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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
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
import org.openhab.binding.meteoalerte.internal.MeteoAlerteConfiguration;
import org.openhab.binding.meteoalerte.internal.json.ApiResponse;
import org.openhab.binding.meteoalerte.internal.json.Fields;
import org.openhab.binding.meteoalerte.internal.json.Record;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

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
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, (JsonDeserializer<ZonedDateTime>) (json, type,
                    jsonDeserializationContext) -> ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString()))
            .create();

    // Time zone provider representing time zone configured in openHAB config
    private final TimeZoneProvider timeZoneProvider;

    private @NonNullByDefault({}) ScheduledFuture<?> refreshJob;
    private @NonNullByDefault({}) String queryUrl;

    public MeteoAlerteHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
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
                IOUtils.closeQuietly(connection.getInputStream());
                updateStatus(ThingStatus.ONLINE);
                return gson.fromJson(response, ApiResponse.class);
            } catch (IOException e) {
                logger.warn("Error opening connection to Meteo Alerte webservice : {}", e.getMessage());
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
            Fields fields = record.getFields();

            updateAlertString(WIND, fields.getEtatVent());
            updateAlertString(RAIN, fields.getEtatPluieInondation());
            updateAlertString(STORM, fields.getEtatOrage());
            updateAlertString(FLOOD, fields.getEtatInondation());
            updateAlertString(SNOW, fields.getEtatNeige());
            updateAlertString(HEAT, fields.getEtatCanicule());
            updateAlertString(FREEZE, fields.getEtatGrandFroid());
            updateAlertString(AVALANCHE, fields.getEtatAvalanches());

            updateDate(OBSERVATIONTIME, fields.getDateInsert());
            updateState(COMMENT, new StringType(fields.getVigilanceCommentaireTexte()));
            updateIcon(WIND, fields.getEtatVent());
            updateIcon(RAIN, fields.getEtatPluieInondation());
            updateIcon(STORM, fields.getEtatOrage());
            updateIcon(FLOOD, fields.getEtatInondation());
            updateIcon(SNOW, fields.getEtatNeige());
            updateIcon(HEAT, fields.getEtatCanicule());
            updateIcon(FREEZE, fields.getEtatGrandFroid());
            updateIcon(AVALANCHE, fields.getEtatAvalanches());

        }
    }

    public void updateIcon(String channelId, @Nullable String value) {
        String iconChannelId = channelId + "-icon";
        if (isLinked(iconChannelId)) {
            String pictoName = channelId + (value != null ? "_" + value.toLowerCase() : "");
            byte[] image = getImage("picto" + File.separator + pictoName + ".gif");
            if (image != null) {
                RawType picto = new RawType(image, "image/gif");
                updateState(iconChannelId, picto);
            }
        }
    }

    private byte @Nullable [] getImage(String iconPath) {
        byte[] data = null;
        URL url = FrameworkUtil.getBundle(getClass()).getResource(iconPath);
        logger.trace("Path to icon image resource is: {}", url);
        if (url != null) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                InputStream is = url.openStream();
                BufferedImage image = ImageIO.read(is);
                ImageIO.write(image, "gif", out);
                out.flush();
                data = out.toByteArray();
            } catch (IOException e) {
                logger.debug("I/O exception occurred getting image data: {}", e.getMessage(), e);
            }
        }
        return data;
    }

    public void updateAlertString(String channelId, @Nullable String value) {
        if (value != null && isLinked(channelId)) {
            int level = ALERT_LEVELS.indexOf(value);
            updateState(channelId, new StringType(Integer.toString(level)));
        }
    }

    public void updateDate(String channelId, @Nullable ZonedDateTime zonedDateTime) {
        if (zonedDateTime != null && isLinked(channelId)) {
            ZonedDateTime localDateTime = zonedDateTime.withZoneSameInstant(timeZoneProvider.getTimeZone());
            updateState(channelId, new DateTimeType(localDateTime));
        }
    }

}
