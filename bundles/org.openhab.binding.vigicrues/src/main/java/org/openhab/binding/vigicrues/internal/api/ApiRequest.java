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

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vigicrues.internal.dto.hubeau.HubEauResponse;
import org.openhab.binding.vigicrues.internal.dto.opendatasoft.OpenDatasoftResponse;
import org.openhab.binding.vigicrues.internal.dto.vigicrues.CdStationHydro;
import org.openhab.binding.vigicrues.internal.dto.vigicrues.InfoVigiCru;
import org.openhab.binding.vigicrues.internal.dto.vigicrues.TerEntVigiCru;
import org.openhab.binding.vigicrues.internal.dto.vigicrues.TronEntVigiCru;
import org.openhab.core.library.types.PointType;

/**
 * The {@link APIRequests} defines all the action classes that can be used
 * when interaction with Vigicrues API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ApiRequest {
    private final String url;
    private final Class<?> responseClass;

    public String getUrl() {
        return url;
    }

    public Class<?> getResponseClass() {
        return responseClass;
    }

    public ApiRequest(String baseUrl, String parameters, Class<?> responseClass) {
        this(baseUrl + parameters, responseClass);
    }

    public ApiRequest(String url, Class<?> responseClass) {
        this.url = url;
        this.responseClass = responseClass;
    }

    public static class DiscoverStations extends ApiRequest {
        private static String BASE_URL = "https://hubeau.eaufrance.fr/api/v1/hydrometrie/referentiel/stations?format=json&size=2000";

        public DiscoverStations(PointType location, int range) {
            super(BASE_URL,
                    String.format(Locale.US, "&latitude=%.2f&longitude=%.2f&distance=%d",
                            location.getLatitude().floatValue(), location.getLongitude().floatValue(), range),
                    HubEauResponse.class);
        }

        public DiscoverStations(String stationId) {
            super(BASE_URL, String.format("&code_station=%s", stationId), HubEauResponse.class);
        }
    }

    public static class GetMeasures extends ApiRequest {
        private static String BASE_URL = "https://public.opendatasoft.com/api/records/1.0/search/?dataset=vigicrues&sort=timestamp&q=%s";

        public GetMeasures(String stationId) {
            super(String.format(BASE_URL, stationId), OpenDatasoftResponse.class);
        }
    }

    public static class GetStationDetails extends ApiRequest {
        private static String BASE_URL = "https://www.vigicrues.gouv.fr/services/station.json/index.php?CdStationHydro=%s";

        public GetStationDetails(String stationId) {
            super(String.format(BASE_URL, stationId), CdStationHydro.class);
        }
    }

    public static class GetTerritoire extends ApiRequest {
        private static String BASE_URL = "https://www.vigicrues.gouv.fr/services/1/TerEntVigiCru.jsonld/?TypEntVigiCru=5&CdEntVigiCru=%s";

        public GetTerritoire(String stationId) {
            super(String.format(BASE_URL, stationId), TerEntVigiCru.class);
        }
    }

    public static class GetTroncon extends ApiRequest {
        private static String BASE_URL = "https://www.vigicrues.gouv.fr/services/1/TronEntVigiCru.jsonld/?TypEntVigiCru=8&CdEntVigiCru=%s";

        public GetTroncon(String stationId) {
            super(String.format(BASE_URL, stationId), TronEntVigiCru.class);
        }
    }

    public static class GetTronconStatus extends ApiRequest {
        private static String BASE_URL = "https://www.vigicrues.gouv.fr/services/1/InfoVigiCru.jsonld/?TypEntVigiCru=8&CdEntVigiCru=%s";

        public GetTronconStatus(String tronconId) {
            super(String.format(BASE_URL, tronconId), InfoVigiCru.class);
        }
    }
}
