/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.tankerkoenig.internal.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tankerkoenig.internal.dto.TankerkoenigDetailResult;
import org.openhab.binding.tankerkoenig.internal.dto.TankerkoenigListResult;
import org.openhab.binding.tankerkoenig.internal.serializer.CustomTankerkoenigDetailResultDeserializer;
import org.openhab.binding.tankerkoenig.internal.serializer.CustomTankerkoenigListResultDeserializer;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/***
 * Serivce class requesting data from tankerkoenig api and providing result objects
 *
 * @author Dennis Dollinger - Initial contribution
 * @author Juergen Baginski - Initial contribution
 */
@NonNullByDefault
public class TankerkoenigService {
    private final Logger logger = LoggerFactory.getLogger(TankerkoenigService.class);

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(TankerkoenigListResult.class, new CustomTankerkoenigListResultDeserializer()).create();
    private final Gson gsonDetail = new GsonBuilder()
            .registerTypeAdapter(TankerkoenigDetailResult.class, new CustomTankerkoenigDetailResultDeserializer())
            .create();
    private static final int REQUEST_TIMEOUT = 5000;

    public @Nullable TankerkoenigListResult getStationListData(String apikey, String locationIDs, String userAgent) {
        return getTankerkoenigListResult(apikey, locationIDs, userAgent);
    }

    public @Nullable TankerkoenigDetailResult getStationDetailData(String apikey, String locationID, String userAgent) {
        return getTankerkoenigDetailResult(apikey, locationID, userAgent);
    }

    private @Nullable String getResponseString(String apiKey, String locationIDs, String userAgent, boolean detail)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("https://creativecommons.tankerkoenig.de/json/");
        if (detail) {
            sb.append("detail.php?id=");
        } else {
            sb.append("prices.php?ids=");
        }
        sb.append(locationIDs);
        sb.append("&apikey=");
        sb.append(apiKey);
        String url = sb.toString();
        try {
            Properties urlHeader = new Properties();
            urlHeader.put("USER-AGENT", userAgent);
            return HttpUtil.executeUrl("GET", url, urlHeader, null, "", REQUEST_TIMEOUT);
        } catch (MalformedURLException e) {
            logger.debug("Error in getResponseString: ", e);
            return null;
        }
    }

    private @Nullable TankerkoenigListResult getTankerkoenigListResult(String apikey, String locationIDs,
            String userAgent) {
        try {
            String jsonData = getResponseString(apikey, locationIDs, userAgent, false);
            logger.debug("json-String: {}", jsonData);
            return gson.fromJson(jsonData, TankerkoenigListResult.class);
        } catch (IOException e) {
            logger.debug("Error in getTankerkoenigListResult: ", e);
            // the return of an empty result will force the status-update OFFLINE!
            return TankerkoenigListResult.emptyResult();
        }
    }

    private @Nullable TankerkoenigDetailResult getTankerkoenigDetailResult(String apiKey, String locationID,
            String userAgent) {
        try {
            String jsonData = getResponseString(apiKey, locationID, userAgent, true);
            logger.debug("getTankerkoenigDetailResult jsonData : {}", jsonData);
            return gsonDetail.fromJson(jsonData, TankerkoenigDetailResult.class);
        } catch (IOException e) {
            logger.debug("getTankerkoenigDetailResult IOException: ", e);
            // the return of an empty result will force the status-update OFFLINE!
            return TankerkoenigDetailResult.emptyResult();
        }
    }
}
