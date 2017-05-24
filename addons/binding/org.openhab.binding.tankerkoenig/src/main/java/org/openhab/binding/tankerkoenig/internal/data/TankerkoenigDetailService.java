/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
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
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
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
    private Gson gson;

    public OpeningTimes getTankstellenDetailData(String apikey, String locationID) {
        TankerkoenigDetailResult detailresult = this.getTankerkoenigDetailResult(apikey, locationID);
        OpeningTimes openingtimes = new OpeningTimes(locationID, detailresult.iswholeDay(),
                detailresult.getOpeningtimes());
        logger.debug("Found opening times for stationID: {}", locationID);
        return openingtimes;
    }

    private String getResponseDetailString(String apikey, String locationID) throws IOException {
        String urlbase = "https://creativecommons.tankerkoenig.de/json/detail.php?";
        String urlcomplete = urlbase + "id=" + locationID + "&apikey=" + apikey;
        String response = "";
        try {
            String userAgent = "OpenHAB, Tankerkoenig-Binding Version ";
            Version version = FrameworkUtil.getBundle(this.getClass()).getVersion();
            userAgent = userAgent + version.toString();
            URL url = new URL(urlcomplete);
            URLConnection connection = url.openConnection();
            logger.debug("UpdateTankstellenDetails URL: {}", urlcomplete);
            response = IOUtils.toString(connection.getInputStream());
            return response;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private TankerkoenigDetailResult getTankerkoenigDetailResult(String apikey, String locationID) {

        String jsonData = "";
        try {
            jsonData = getResponseDetailString(apikey, locationID);
        } catch (IOException e) {
            logger.debug("UpdateTankstellenDetails IOException: {}", e);
        }
        logger.debug("UpdateTankstellenDetails jsonData : {}", jsonData);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(TankerkoenigDetailResult.class,
                new CustomTankerkoenigDetailResultDeserializer());
        gson = gsonBuilder.create();
        TankerkoenigDetailResult res = gson.fromJson(jsonData, TankerkoenigDetailResult.class);
        return res;
    }
}
