/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.tankerkoenig.internal.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.openhab.binding.tankerkoenig.internal.config.OpeningTimes;
import org.openhab.binding.tankerkoenig.internal.config.TankerkoenigDetailResult;
import org.openhab.binding.tankerkoenig.internal.serializer.CustomTankerkoenigDetailResultDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/***
 * Serivce class requesting detail data from tankerkoenig api and providing result objects
 *
 * @author Dennis Dollinger/Jürgen Baginski
 *
 */
public class TankerkoenigDetailService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final GsonBuilder GSON_BUILDER = new GsonBuilder()
            .registerTypeAdapter(TankerkoenigDetailResult.class, new CustomTankerkoenigDetailResultDeserializer());;
    private static final Gson GSON = GSON_BUILDER.create();

    public OpeningTimes getTankstellenDetailData(String apikey, String locationID, String userAgent) {
        TankerkoenigDetailResult detailresult = this.getTankerkoenigDetailResult(apikey, locationID, userAgent);
        if (detailresult.isOk()) {
            OpeningTimes openingtimes = new OpeningTimes(locationID, detailresult.iswholeDay(),
                    detailresult.getOpeningtimes());
            logger.debug("Found opening times for stationID: {}", locationID);
            return openingtimes;
        } else {
            // no valid response for detail data
            return null;
        }
    }

    private String getResponseDetailString(String apikey, String locationID, String userAgent) throws IOException {
        String urlbase = "https://creativecommons.tankerkoenig.de/json/detail.php?";
        String urlcomplete = urlbase + "id=" + locationID + "&apikey=" + apikey;
        try {
            URL url = new URL(urlcomplete);
            URLConnection connection = url.openConnection();
            logger.debug("UpdateTankstellenDetails URL: {}", urlcomplete);
            return IOUtils.toString(connection.getInputStream());
        } catch (MalformedURLException e) {
            logger.error("Error in getResponseDetailString: ", e);
            return null;
        }
    }

    private TankerkoenigDetailResult getTankerkoenigDetailResult(String apikey, String locationID, String userAgent) {

        String jsonData = "";
        try {
            jsonData = getResponseDetailString(apikey, locationID, userAgent);
            logger.debug("UpdateTankstellenDetails jsonData : {}", jsonData);
            return GSON.fromJson(jsonData, TankerkoenigDetailResult.class);
        } catch (IOException e) {
            logger.debug("UpdateTankstellenDetails IOException: ", e);
            return TankerkoenigDetailResult.emptyResult();
        }
    }
}
