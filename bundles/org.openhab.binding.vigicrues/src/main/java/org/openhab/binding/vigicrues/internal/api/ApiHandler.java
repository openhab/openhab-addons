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
package org.openhab.binding.vigicrues.internal.api;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Locale;

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
@NonNullByDefault
public class ApiHandler {
    private static final int TIMEOUT_MS = 30000;
    private final Gson gson;

    public ApiHandler(TimeZoneProvider timeZoneProvider) {
        this.gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class,
                (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime
                        .parse(json.getAsJsonPrimitive().getAsString())
                        .withZoneSameInstant(timeZoneProvider.getTimeZone()))
                .create();
    }

    private String execute(String url) throws VigiCruesException {
        String jsonResponse = "";
        try {
            jsonResponse = HttpUtil.executeUrl("GET", url, TIMEOUT_MS);
            return jsonResponse;
        } catch (IOException e) {
            throw new VigiCruesException(e);
        }
    }

    public InfoVigiCru GetTronconStatus(String tronconId) throws VigiCruesException {
        final String BASE_URL = "https://www.vigicrues.gouv.fr/services/1/InfoVigiCru.jsonld/?TypEntVigiCru=8&CdEntVigiCru=%s";
        String response = execute(String.format(BASE_URL, tronconId));
        try {
            return gson.fromJson(response, InfoVigiCru.class);
        } catch (JsonSyntaxException e) {
            throw new VigiCruesException(e);
        }
    }

    public TronEntVigiCru GetTroncon(String stationId) throws VigiCruesException {
        final String BASE_URL = "https://www.vigicrues.gouv.fr/services/1/TronEntVigiCru.jsonld/?TypEntVigiCru=8&CdEntVigiCru=%s";
        String response = execute(String.format(BASE_URL, stationId));
        try {
            return gson.fromJson(response, TronEntVigiCru.class);
        } catch (JsonSyntaxException e) {
            throw new VigiCruesException(e);
        }
    }

    public TerEntVigiCru GetTerritoire(String stationId) throws VigiCruesException {
        final String BASE_URL = "https://www.vigicrues.gouv.fr/services/1/TerEntVigiCru.jsonld/?TypEntVigiCru=5&CdEntVigiCru=%s";
        String response = execute(String.format(BASE_URL, stationId));
        try {
            return gson.fromJson(response, TerEntVigiCru.class);
        } catch (JsonSyntaxException e) {
            throw new VigiCruesException(e);
        }
    }

    public CdStationHydro GetStationDetails(String stationId) throws VigiCruesException {
        final String BASE_URL = "https://www.vigicrues.gouv.fr/services/station.json/index.php?CdStationHydro=%s";
        String response = execute(String.format(BASE_URL, stationId));
        try {
            return gson.fromJson(response, CdStationHydro.class);
        } catch (JsonSyntaxException e) {
            throw new VigiCruesException(e);
        }
    }

    public OpenDatasoftResponse GetMeasures(String stationId) throws VigiCruesException {
        final String BASE_URL = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=vigicrues&sort=timestamp&q=%s";
        String response = execute(String.format(BASE_URL, stationId));
        try {
            return gson.fromJson(response, OpenDatasoftResponse.class);
        } catch (JsonSyntaxException e) {
            throw new VigiCruesException(e);
        }
    }

    public HubEauResponse DiscoverStations(PointType location, int range) throws VigiCruesException {
        final String BASE_URL = "https://hubeau.eaufrance.fr/api/v1/hydrometrie/referentiel/stations?format=json&size=2000";

        String response = execute(BASE_URL + String.format(Locale.US, "&latitude=%.2f&longitude=%.2f&distance=%d",
                location.getLatitude().floatValue(), location.getLongitude().floatValue(), range));
        try {
            return gson.fromJson(response, HubEauResponse.class);
        } catch (JsonSyntaxException e) {
            throw new VigiCruesException(e);
        }
    }

    public HubEauResponse DiscoverStations(String stationId) throws VigiCruesException {
        final String BASE_URL = "https://hubeau.eaufrance.fr/api/v1/hydrometrie/referentiel/stations?format=json&size=2000";
        String response = execute(BASE_URL + String.format("&code_station=%s", stationId));
        try {
            return gson.fromJson(response, HubEauResponse.class);
        } catch (JsonSyntaxException e) {
            throw new VigiCruesException(e);
        }
    }
}
