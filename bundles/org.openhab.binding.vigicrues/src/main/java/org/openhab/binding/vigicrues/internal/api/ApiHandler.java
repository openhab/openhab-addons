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
package org.openhab.binding.vigicrues.internal.api;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Locale;

import javax.ws.rs.HttpMethod;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vigicrues.internal.dto.hubeau.HubEauResponse;
import org.openhab.binding.vigicrues.internal.dto.opendatasoft.OpenDatasoftResponse;
import org.openhab.binding.vigicrues.internal.dto.vigicrues.CdStationHydro;
import org.openhab.binding.vigicrues.internal.dto.vigicrues.InfoVigiCru;
import org.openhab.binding.vigicrues.internal.dto.vigicrues.TerEntVigiCru;
import org.openhab.binding.vigicrues.internal.dto.vigicrues.TronEntVigiCru;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.PointType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
    private static final int TIMEOUT_MS = 30000;

    private final Gson gson;

    @Activate
    public ApiHandler(@Reference TimeZoneProvider timeZoneProvider) {
        this.gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class,
                (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime
                        .parse(json.getAsJsonPrimitive().getAsString())
                        .withZoneSameInstant(timeZoneProvider.getTimeZone()))
                .create();
    }

    private <T> T execute(String url, Class<T> responseType) throws VigiCruesException {
        try {
            String jsonResponse = HttpUtil.executeUrl(HttpMethod.GET, url, TIMEOUT_MS);
            return gson.fromJson(jsonResponse, responseType);
        } catch (IOException | JsonSyntaxException e) {
            throw new VigiCruesException(e);
        }
    }

    public InfoVigiCru getTronconStatus(String tronconId) throws VigiCruesException {
        final String baseUrl = "https://www.vigicrues.gouv.fr/services/1/InfoVigiCru.jsonld/?TypEntVigiCru=8&CdEntVigiCru=%s";
        return execute(String.format(baseUrl, tronconId), InfoVigiCru.class);
    }

    public TronEntVigiCru getTroncon(String stationId) throws VigiCruesException {
        final String baseUrl = "https://www.vigicrues.gouv.fr/services/1/TronEntVigiCru.jsonld/?TypEntVigiCru=8&CdEntVigiCru=%s";
        return execute(String.format(baseUrl, stationId), TronEntVigiCru.class);
    }

    public TerEntVigiCru getTerritoire(String stationId) throws VigiCruesException {
        final String baseUrl = "https://www.vigicrues.gouv.fr/services/1/TerEntVigiCru.jsonld/?TypEntVigiCru=5&CdEntVigiCru=%s";
        return execute(String.format(baseUrl, stationId), TerEntVigiCru.class);
    }

    public CdStationHydro getStationDetails(String stationId) throws VigiCruesException {
        final String baseUrl = "https://www.vigicrues.gouv.fr/services/station.json/index.php?CdStationHydro=%s";
        return execute(String.format(baseUrl, stationId), CdStationHydro.class);
    }

    public OpenDatasoftResponse getMeasures(String stationId) throws VigiCruesException {
        final String baseUrl = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=vigicrues&sort=timestamp&q=%s";
        return execute(String.format(baseUrl, stationId), OpenDatasoftResponse.class);
    }

    public HubEauResponse discoverStations(PointType location, int range) throws VigiCruesException {
        return execute(
                String.format(Locale.US, "%s&latitude=%.2f&longitude=%.2f&distance=%d", HUBEAU_URL,
                        location.getLatitude().floatValue(), location.getLongitude().floatValue(), range),
                HubEauResponse.class);
    }

    public HubEauResponse discoverStations(String stationId) throws VigiCruesException {
        return execute(String.format("%s&code_station=%s", HUBEAU_URL, stationId), HubEauResponse.class);
    }
}
