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
package org.openhab.binding.meteofrance.internal.handler;

import static org.openhab.binding.meteofrance.internal.MeteoFranceBindingConstants.REQUEST_TIMEOUT_MS;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.HttpMethod;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meteofrance.internal.MeteoFranceException;
import org.openhab.binding.meteofrance.internal.config.BridgeConfiguration;
import org.openhab.binding.meteofrance.internal.deserialization.MeteoFranceDeserializer;
import org.openhab.binding.meteofrance.internal.dto.Domain;
import org.openhab.binding.meteofrance.internal.dto.MeteoFrance;
import org.openhab.binding.meteofrance.internal.dto.MeteoFrance.DomainId;
import org.openhab.binding.meteofrance.internal.dto.MeteoFrance.Meta;
import org.openhab.binding.meteofrance.internal.dto.MeteoFrance.Period;
import org.openhab.binding.meteofrance.internal.dto.MeteoFrance.Product;
import org.openhab.binding.meteofrance.internal.dto.MeteoFrance.TextBlocItem;
import org.openhab.binding.meteofrance.internal.dto.MeteoFrance.VigilanceEnCours;
import org.openhab.binding.meteofrance.internal.dto.RainForecast;
import org.openhab.binding.meteofrance.internal.dto.Term;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.PointType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link MeteoFranceBridgeHandler} is the handler for Meteo France bridge and connects it
 * to the Meteo France API Portal
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class MeteoFranceBridgeHandler extends BaseBridgeHandler {
    private static final String PORTAIL_API_BASE_URL = "https://public-api.meteofrance.fr/public/DPVigilance/v1/%s/encours";
    private static final String TEXTE_VIGILANCE_URL = PORTAIL_API_BASE_URL.formatted("textesvigilance");
    private static final String CARTE_VIGILANCE_URL = PORTAIL_API_BASE_URL.formatted("cartevigilance");

    private static final String RAIN_FORECAST_BASE_URL = "https://rpcache-aa.meteofrance.com/internet2018client/2.0/nowcast/rain?lat=%.4f&lon=%.4f&token=__Wj7dVSTjV9YGu1guveLyDq0g7S7TfTjaHBTPTpO0kj8__";

    private static final long CACHE_EXPIRY = TimeUnit.MINUTES.toMillis(10);

    private final Logger logger = LoggerFactory.getLogger(MeteoFranceBridgeHandler.class);
    private final Properties header = new Properties();
    private final MeteoFranceDeserializer deserializer;

    private final ExpiringCache<VigilanceEnCours> vigilanceText;
    private final ExpiringCache<VigilanceEnCours> vigilanceMap;

    public MeteoFranceBridgeHandler(Bridge bridge, MeteoFranceDeserializer deserializer) {
        super(bridge);
        this.deserializer = deserializer;

        vigilanceText = new ExpiringCache<>(CACHE_EXPIRY, () -> this.getVigilanceEnCours(TEXTE_VIGILANCE_URL));
        vigilanceMap = new ExpiringCache<>(CACHE_EXPIRY, () -> this.getVigilanceEnCours(CARTE_VIGILANCE_URL));
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Meteo-France API bridge handler.");
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
        } catch (MeteoFranceException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Exception deserializing API answer: %s".formatted(e.getMessage()));
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        return null;
    }

    public @Nullable TextBlocItem requestTextData(Domain domain) {
        VigilanceEnCours local = vigilanceText.getValue();
        if (local != null) {
            Product product = local.product();
            if (product != null) {
                return product.getBlocItem(domain).get();
            }
        }
        return null;
    }

    public @Nullable DomainId requestMapData(Domain domain, Term term) {
        Period period = requestPeriod(term);
        return period != null ? period.timelaps().get(domain) : null;
    }

    public @Nullable Period requestPeriod(Term term) {
        VigilanceEnCours local = vigilanceMap.getValue();
        if (local != null) {
            Product product = local.product();
            if (product != null) {
                return product.getPeriod(term).get();
            }
        }
        return null;
    }

    public Optional<Meta> getMeta() {
        VigilanceEnCours local = vigilanceText.getValue();
        return Optional.ofNullable(local != null ? local.meta() : null);
    }

    public @Nullable RainForecast getRainForecast(PointType location) {
        String url = String.format(Locale.US, RAIN_FORECAST_BASE_URL, location.getLatitude().doubleValue(),
                location.getLongitude().doubleValue());
        try {
            logger.debug("Sending rain-forecast request to: {}", url);
            String answer = HttpUtil.executeUrl(HttpMethod.GET, url, REQUEST_TIMEOUT_MS);
            logger.debug("Received answer: {}", answer);

            return deserializer.deserialize(RainForecast.class, answer);
        } catch (MeteoFranceException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Exception deserializing API answer: %s".formatted(e.getMessage()));
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
        return null;
    }
}
