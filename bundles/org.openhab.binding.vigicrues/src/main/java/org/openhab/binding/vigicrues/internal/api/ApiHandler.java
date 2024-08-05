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
package org.openhab.binding.vigicrues.internal.api;

import static org.openhab.binding.vigicrues.internal.VigiCruesBindingConstants.BINDING_ID;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.vigicrues.internal.dto.hubeau.HubEauResponse;
import org.openhab.binding.vigicrues.internal.dto.opendatasoft.OpenDatasoftResponse;
import org.openhab.binding.vigicrues.internal.dto.vigicrues.CdStationHydro;
import org.openhab.binding.vigicrues.internal.dto.vigicrues.InfoVigiCru;
import org.openhab.binding.vigicrues.internal.dto.vigicrues.ObservationAnswer;
import org.openhab.binding.vigicrues.internal.dto.vigicrues.StaEntVigiCruAnswer;
import org.openhab.binding.vigicrues.internal.dto.vigicrues.TerEntVigiCru;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.TrustAllTrustManager;
import org.openhab.core.library.types.PointType;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link ApiHandler} is the responsible to call a given
 * url and transform the answer in the appropriate dto class
 *
 * @author Gaël L'hopital - Initial contribution
 */

@Component(service = ApiHandler.class)
@NonNullByDefault
public class ApiHandler {
    private static final String HUBEAU_URL = "https://hubeau.eaufrance.fr/api/v1/hydrometrie/referentiel/stations?format=json&size=2000";
    private static final int TIMEOUT_S = 30;

    private final Logger logger = LoggerFactory.getLogger(ApiHandler.class);
    private final HttpClient httpClient;
    private final Gson gson;

    @Activate
    public ApiHandler(@Reference TimeZoneProvider timeZoneProvider,
            final @Reference HttpClientFactory httpClientFactory) {
        SslContextFactory sslContextFactory = new SslContextFactory.Client();

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[] { TrustAllTrustManager.getInstance() }, null);
            sslContextFactory.setSslContext(sslContext);

            httpClient = httpClientFactory.createHttpClient(BINDING_ID, sslContextFactory);
            httpClient.setResponseBufferSize(20971520);
            httpClient.start();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Exception occurred while requesting the SSL encryption algorithm", e);
        } catch (KeyManagementException e) {
            throw new IllegalArgumentException("Exception occurred while initialising the SSL context.", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to start Jetty HttpClient.", e);
        }

        this.gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class,
                (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime
                        .parse(json.getAsJsonPrimitive().getAsString())
                        .withZoneSameInstant(timeZoneProvider.getTimeZone()))
                .create();
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.warn("Unable to stop Jetty HttpClient {}", e.getMessage());
        }
    }

    private <T> T execute(String url, Class<T> responseType) throws VigiCruesException {
        try {
            ContentResponse response = httpClient.newRequest(url).timeout(TIMEOUT_S, TimeUnit.SECONDS).method("GET")
                    .send();
            String jsonResponse = response.getContentAsString();
            return gson.fromJson(jsonResponse, responseType);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new VigiCruesException(e);
        } catch (JsonSyntaxException e) {
            return execute(url, responseType);
        }
    }

    public InfoVigiCru getTronconStatus(String tronconId) throws VigiCruesException {
        final String baseUrl = "https://www.vigicrues.gouv.fr/services/InfoVigiCru.geojson?TypEntVigiCru=8&CdEntVigiCru=%s";
        return execute(baseUrl.formatted(tronconId), InfoVigiCru.class);
    }

    public TerEntVigiCru getTroncon(String stationId) throws VigiCruesException {
        final String baseUrl = "https://www.vigicrues.gouv.fr/services/TronEntVigiCru.json?TypEntVigiCru=8&CdEntVigiCru=%s";
        return execute(baseUrl.formatted(stationId), TerEntVigiCru.class);
    }

    public TerEntVigiCru getTerritoire(String stationId) throws VigiCruesException {
        final String baseUrl = "https://www.vigicrues.gouv.fr/services/TerEntVigiCru.json?TypEntVigiCru=5&CdEntVigiCru=%s";
        return execute(baseUrl.formatted(stationId), TerEntVigiCru.class);
    }

    public StaEntVigiCruAnswer getStationFeeds(String stationId) throws VigiCruesException {
        final String baseUrl = "https://www.vigicrues.gouv.fr/services/StaEntVigiCru.json?TypEntVigiCru=7&CdEntVigiCru=%s";
        return execute(baseUrl.formatted(stationId), StaEntVigiCruAnswer.class);
    }

    public CdStationHydro getStationDetails(String stationId) throws VigiCruesException {
        final String baseUrl = "https://www.vigicrues.gouv.fr/services/station.json/index.php?CdStationHydro=%s";
        return execute(baseUrl.formatted(stationId), CdStationHydro.class);
    }

    public OpenDatasoftResponse getMeasures(String stationId) throws VigiCruesException {
        final String baseUrl = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=vigicrues&sort=timestamp&q=%s";
        return execute(baseUrl.formatted(stationId), OpenDatasoftResponse.class);
    }

    public HubEauResponse discoverStations(PointType location, int range) throws VigiCruesException {
        return execute(
                String.format(Locale.US, "%s&latitude=%.2f&longitude=%.2f&distance=%d", HUBEAU_URL,
                        location.getLatitude().floatValue(), location.getLongitude().floatValue(), range),
                HubEauResponse.class);
    }

    public HubEauResponse discoverStations(String stationId) throws VigiCruesException {
        return execute("%s&code_station=%s".formatted(HUBEAU_URL, stationId), HubEauResponse.class);
    }

    public ObservationAnswer getHeights(String stationId) throws VigiCruesException {
        return getObservations(stationId, "H");
    }

    public ObservationAnswer getFlows(String stationId) throws VigiCruesException {
        return getObservations(stationId, "Q");
    }

    private ObservationAnswer getObservations(String stationId, String obsType) throws VigiCruesException {
        final String baseUrl = "https://www.vigicrues.gouv.fr/services/observations.json/index.php?CdStationHydro=%s&FormatDate=iso&GrdSerie=%s";
        return execute(baseUrl.formatted(stationId, obsType), ObservationAnswer.class);
    }
}
