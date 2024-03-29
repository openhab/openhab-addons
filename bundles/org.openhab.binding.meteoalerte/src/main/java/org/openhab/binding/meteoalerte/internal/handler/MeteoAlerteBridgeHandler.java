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

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.HttpMethod;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meteoalerte.internal.config.BridgeConfiguration;
import org.openhab.binding.meteoalerte.internal.db.DepartmentDbService;
import org.openhab.binding.meteoalerte.internal.discovery.MeteoAlerteDiscoveryService;
import org.openhab.binding.meteoalerte.internal.dto.TextBlocItem;
import org.openhab.binding.meteoalerte.internal.dto.VigilanceEnCours;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link MeteoAlerteBridgeHandler} is the handler for OpenUV API and connects it
 * to the webservice.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class MeteoAlerteBridgeHandler extends BaseBridgeHandler {
    private static final String PORTAIL_API_BASE_URL = "https://public-api.meteofrance.fr/public/DPVigilance/v1";
    private static final String TEXTE_VIGILANCE_URL = PORTAIL_API_BASE_URL + "/textesvigilance/encours";
    private static final String CARTE_VIGILANCE_URL = PORTAIL_API_BASE_URL + "/cartevigilance/encours";
    private static final String VIGNETTE_URL = PORTAIL_API_BASE_URL + "/vignettenationale-J/encours";

    private static final int REQUEST_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(30);

    private final Logger logger = LoggerFactory.getLogger(MeteoAlerteBridgeHandler.class);
    private final Properties header = new Properties();
    private final LocationProvider locationProvider;
    private final DepartmentDbService dbService;
    private final Gson gson;
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private @Nullable VigilanceEnCours vigilanceEnCours;

    public MeteoAlerteBridgeHandler(Bridge bridge, Gson gson, LocationProvider locationProvider,
            DepartmentDbService dbService) {
        super(bridge);
        this.locationProvider = locationProvider;
        this.dbService = dbService;
        this.gson = gson;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Meteo-Alerte API bridge handler.");
        BridgeConfiguration config = getConfigAs(BridgeConfiguration.class);
        if (config.apikey.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-unknown-apikey");
            return;
        }
        header.put("apikey", config.apikey);
        header.put("accept", "*/*");
        updateStatus(ThingStatus.UNKNOWN);

        refreshJob = Optional.of(scheduler.scheduleWithFixedDelay(this::getVigilanceEnCours, config.refresh,
                config.refresh, TimeUnit.MINUTES));
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
        refreshJob = Optional.empty();
        header.clear();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            getVigilanceEnCours();
        } else {
            logger.debug("The bridge only handles Refresh command and not '{}'", command);
        }
    }

    private void getVigilanceEnCours() {
        logger.debug("Updating channels");

        String url = TEXTE_VIGILANCE_URL;
        try {
            String answer = HttpUtil.executeUrl(HttpMethod.GET, url, header, null, null, REQUEST_TIMEOUT_MS);
            vigilanceEnCours = gson.fromJson(answer, VigilanceEnCours.class);
            logger.debug("{}\n", answer);
            updateStatus(ThingStatus.ONLINE);
        } catch (IOException e) {
            logger.warn("Request timedout : {}", e.getMessage());
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MeteoAlerteDiscoveryService.class);
    }

    public LocationProvider getLocationProvider() {
        return locationProvider;
    }

    public DepartmentDbService getDbService() {
        return dbService;
    }

    public @Nullable TextBlocItem requestData(String department) {
        if (vigilanceEnCours == null) {
            getVigilanceEnCours();
        }
        return vigilanceEnCours.product.textBlocItems.stream().filter(bloc -> bloc.domain.name().equals(department))
                .findFirst().orElse(null);
    }
}
