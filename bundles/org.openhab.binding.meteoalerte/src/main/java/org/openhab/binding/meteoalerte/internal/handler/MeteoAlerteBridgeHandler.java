/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.net.ConnectException;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.HttpMethod;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meteoalerte.internal.MeteoAlerteException;
import org.openhab.binding.meteoalerte.internal.config.BridgeConfiguration;
import org.openhab.binding.meteoalerte.internal.db.DepartmentDbService;
import org.openhab.binding.meteoalerte.internal.deserialization.MeteoAlerteDeserializer;
import org.openhab.binding.meteoalerte.internal.discovery.MeteoAlerteDiscoveryService;
import org.openhab.binding.meteoalerte.internal.dto.Domain;
import org.openhab.binding.meteoalerte.internal.dto.MeteoFrance;
import org.openhab.binding.meteoalerte.internal.dto.MeteoFrance.DomainId;
import org.openhab.binding.meteoalerte.internal.dto.MeteoFrance.Meta;
import org.openhab.binding.meteoalerte.internal.dto.MeteoFrance.Period;
import org.openhab.binding.meteoalerte.internal.dto.MeteoFrance.TextBlocItem;
import org.openhab.binding.meteoalerte.internal.dto.MeteoFrance.VigilanceEnCours;
import org.openhab.binding.meteoalerte.internal.dto.Term;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MeteoAlerteBridgeHandler} is the handler for OpenUV API and connects it
 * to the webservice.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class MeteoAlerteBridgeHandler extends BaseBridgeHandler {
    private static final String PORTAIL_API_BASE_URL = "https://public-api.meteofrance.fr/public/DPVigilance/v1/%s/encours";
    private static final String TEXTE_VIGILANCE_URL = PORTAIL_API_BASE_URL.formatted("textesvigilance");
    private static final String CARTE_VIGILANCE_URL = PORTAIL_API_BASE_URL.formatted("cartevigilance");
    private static final int REQUEST_TIMEOUT_MS = (int) TimeUnit.SECONDS.toMillis(45);
    private static final long CACHE_EXPIRY = TimeUnit.MINUTES.toMillis(10);

    private final Logger logger = LoggerFactory.getLogger(MeteoAlerteBridgeHandler.class);
    private final Properties header = new Properties();
    private final LocationProvider locationProvider;
    private final DepartmentDbService dbService;
    private final MeteoAlerteDeserializer deserializer;

    private final ExpiringCache<VigilanceEnCours> vigilanceText;
    private final ExpiringCache<VigilanceEnCours> vigilanceMap;

    public MeteoAlerteBridgeHandler(Bridge bridge, MeteoAlerteDeserializer deserializer,
            LocationProvider locationProvider, DepartmentDbService dbService) {
        super(bridge);
        this.locationProvider = locationProvider;
        this.dbService = dbService;
        this.deserializer = deserializer;

        vigilanceText = new ExpiringCache<>(CACHE_EXPIRY, () -> this.getVigilanceEnCours(TEXTE_VIGILANCE_URL));
        vigilanceMap = new ExpiringCache<>(CACHE_EXPIRY, () -> this.getVigilanceEnCours(CARTE_VIGILANCE_URL));
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
        scheduler.execute(() -> vigilanceText.getValue());
    }

    @Override
    public void dispose() {
        header.clear();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // The bridge does not handle commands
    }

    private @Nullable VigilanceEnCours getVigilanceEnCours(String url) {
        try {
            String answer = HttpUtil.executeUrl(HttpMethod.GET, url, header, null, null, REQUEST_TIMEOUT_MS);
            logger.trace(answer);

            VigilanceEnCours vigilance = deserializer.deserialize(MeteoFrance.VigilanceEnCours.class, answer);
            if (vigilance.code() != 0) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, vigilance.message());
            } else {
                updateStatus(ThingStatus.ONLINE);
                return vigilance;
            }
        } catch (ConnectException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (MeteoAlerteException e) {
            logger.warn("Exception deserializing API answer: {}", e.getMessage());
        } catch (IOException e) {
            logger.warn("Request timedout: {}", e.getMessage());
        }
        return null;
    }

    public @Nullable TextBlocItem requestTextData(Domain domain) {
        VigilanceEnCours local = vigilanceText.getValue();
        return local != null ? local.getProduct().map(p -> p.getBlocItem(domain)).get().orElse(null) : null;
    }

    public @Nullable DomainId requestMapData(Domain domain, Term term) {
        Period period = requestPeriod(term);
        return period != null ? period.timelaps().get(domain) : null;
    }

    public @Nullable Period requestPeriod(Term term) {
        VigilanceEnCours local = vigilanceMap.getValue();
        return local != null ? local.getProduct().map(p -> p.getPeriod(term)).get().orElse(null) : null;
    }

    public Optional<Meta> getMeta() {
        VigilanceEnCours local = vigilanceText.getValue();
        return Optional.ofNullable(local != null ? local.meta() : null);
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
}
