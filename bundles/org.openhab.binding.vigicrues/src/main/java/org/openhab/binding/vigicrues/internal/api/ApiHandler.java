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
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FutureResponseListener;
import org.eclipse.jetty.http.HttpStatus;
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
    private static final int TIMEOUT_S = 30;

    private final Logger logger = LoggerFactory.getLogger(ApiHandler.class);
    private final HttpClient httpClient;
    private final Gson gson;

    private final int TERRITOIRE_ENTITY = 5;
    private final int TRONCON_ENTITY = 8;
    private final int STATION_ENTITY = 7;

    private final static String BASE_URL = "https://www.vigicrues.gouv.fr/services/";
    private final static String INFO_URL = BASE_URL + "InfoVigiCru.geojson";
    private final static String TRONCON_URL = BASE_URL + "TronEntVigiCru.json?TypEntVigiCru=%s&CdEntVigiCru=%s";
    private final static String TERITOIRE_URL = BASE_URL + "TerEntVigiCru.json?TypEntVigiCru=%s&CdEntVigiCru=%s";
    private final static String STATION_URL = BASE_URL + "StaEntVigiCru.json?TypEntVigiCru=%s&CdEntVigiCru=%s";
    private final static String STATION_DETAILS_URL = BASE_URL + "station.json/index.php?CdStationHydro=%s";
    private final static String OBSERVATION_URL = BASE_URL
            + "observations.json/index.php?CdStationHydro=%s&FormatDate=iso&GrdSerie=%s";

    private final static String MEASURES_URL = BASE_URL
            + "https://public.opendatasoft.com/api/records/1.0/search/?dataset=vigicrues&sort=timestamp&q=%s";
    private static final String HUBEAU_URL = "https://hubeau.eaufrance.fr/api/v2/hydrometrie/referentiel/stations?format=json&size=2000";

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
        String jsonResponse = "";
        int retry = 0;
        try {
            while (retry < 3) {
                Request req = httpClient.newRequest(url).timeout(TIMEOUT_S, TimeUnit.SECONDS).method("GET");
                req.header("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/144.0.0.0 Safari/537.36");
                req.header("Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
                req.header("Accept-Encoding", "gzip, deflate, br, zstd");
                req.header("Accept-Language", "en-US,en;q=0.9,fr-FR;q=0.8,fr;q=0.7");

                FutureResponseListener listener = new FutureResponseListener(req, 50 * 1024 * 1024);
                req.send(listener);

                ContentResponse response = listener.get(TIMEOUT_S, TimeUnit.SECONDS);
                retry++;

                int status = response.getStatus();
                if (status == HttpStatus.OK_200) {
                    jsonResponse = response.getContentAsString();
                    if (jsonResponse.contains("Site en maintenance")) {
                        Thread.sleep(200);
                        continue;
                    }
                    return gson.fromJson(jsonResponse, responseType);
                }
            }
            return gson.fromJson(jsonResponse, responseType);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new VigiCruesException(e);
        } catch (JsonSyntaxException e) {
            throw new VigiCruesException(e);
        }
    }

    public InfoVigiCru getTronconStatus() throws VigiCruesException {
        return execute(INFO_URL, InfoVigiCru.class);
    }

    public TerEntVigiCru getTroncon(String stationId) throws VigiCruesException {
        return execute(TRONCON_URL.formatted(TRONCON_ENTITY, stationId), TerEntVigiCru.class);
    }

    public TerEntVigiCru getTerritoire(String stationId) throws VigiCruesException {
        return execute(TERITOIRE_URL.formatted(TERRITOIRE_ENTITY, stationId), TerEntVigiCru.class);
    }

    public StaEntVigiCruAnswer getTerritoireDetails(String stationId) throws VigiCruesException {
        return execute(STATION_URL.formatted(TERRITOIRE_ENTITY, stationId), StaEntVigiCruAnswer.class);
    }

    public StaEntVigiCruAnswer getStationFeeds(String stationId) throws VigiCruesException {
        return execute(STATION_URL.formatted(STATION_ENTITY, stationId), StaEntVigiCruAnswer.class);
    }

    public CdStationHydro getStationDetails(String stationId) throws VigiCruesException {
        return execute(STATION_DETAILS_URL.formatted(stationId), CdStationHydro.class);
    }

    public OpenDatasoftResponse getMeasures(String stationId) throws VigiCruesException {
        return execute(MEASURES_URL.formatted(stationId), OpenDatasoftResponse.class);
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
        return execute(OBSERVATION_URL.formatted(stationId, obsType), ObservationAnswer.class);
    }
}
