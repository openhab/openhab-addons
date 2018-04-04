/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tankerkoenig.internal.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.tankerkoenig.internal.config.TankerkoenigDetailResult;
import org.openhab.binding.tankerkoenig.internal.config.TankerkoenigListResult;
import org.openhab.binding.tankerkoenig.internal.serializer.CustomTankerkoenigDetailResultDeserializer;
import org.openhab.binding.tankerkoenig.internal.serializer.CustomTankerkoenigListResultDeserializer;
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
public class TankerkoenigService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final GsonBuilder gsonBuilder = new GsonBuilder().registerTypeAdapter(TankerkoenigListResult.class,
            new CustomTankerkoenigListResultDeserializer());
    private final Gson gson = gsonBuilder.create();
    private final GsonBuilder gsonBuilderDetail = new GsonBuilder().registerTypeAdapter(TankerkoenigDetailResult.class,
            new CustomTankerkoenigDetailResultDeserializer());;
    private final Gson gsonDetail = gsonBuilderDetail.create();
    private static final int REQUEST_TIMEOUT = 5000;

    public TankerkoenigListResult getStationListData(String apikey, String locationIDs, String userAgent) {
        return this.getTankerkoenigListResult(apikey, locationIDs, userAgent);
    }

    public TankerkoenigDetailResult getStationDetailData(String apikey, String locationID, String userAgent) {
        return this.getTankerkoenigDetailResult(apikey, locationID, userAgent);
    }

    private String getResponseString(String apiKey, String locationIDs, String userAgent, boolean detail)
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

    private TankerkoenigListResult getTankerkoenigListResult(String apikey, String locationIDs, String userAgent) {
        String jsonData = "";
        try {
            jsonData = getResponseString(apikey, locationIDs, userAgent, false);
            logger.debug("json-String: {}", jsonData);
            return gson.fromJson(jsonData, TankerkoenigListResult.class);
        } catch (IOException e) {
            logger.debug("Error in getTankerkoenigListResult: ", e);
            // the return of an empty result will force the status-update OFFLINE!
            return TankerkoenigListResult.emptyResult();
        }
    }

    private TankerkoenigDetailResult getTankerkoenigDetailResult(String apiKey, String locationID, String userAgent) {
        String jsonData = "";
        try {
            jsonData = getResponseString(apiKey, locationID, userAgent, true);
            logger.debug("getTankerkoenigDetailResult jsonData : {}", jsonData);
            return gsonDetail.fromJson(jsonData, TankerkoenigDetailResult.class);
        } catch (IOException e) {
            logger.debug("getTankerkoenigDetailResult IOException: ", e);
            // the return of an empty result will force the status-update OFFLINE!
            return TankerkoenigDetailResult.emptyResult();
        }
    }
}
