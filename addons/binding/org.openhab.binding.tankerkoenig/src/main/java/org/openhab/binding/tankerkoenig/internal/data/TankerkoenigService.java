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
import org.openhab.binding.tankerkoenig.internal.config.TankerkoenigListResult;
import org.openhab.binding.tankerkoenig.internal.serializer.CustomTankerkoenigListResultDeserializer;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/***
 * Serivce class requesting data from tankerkoenig api and providing result objects
 *
 * @author Dennis Dollinger
 *
 */
public class TankerkoenigService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final GsonBuilder GSON_BUILDER = new GsonBuilder().registerTypeAdapter(TankerkoenigListResult.class,
            new CustomTankerkoenigListResultDeserializer());;
    private static final Gson GSON = GSON_BUILDER.create();

    public TankerkoenigListResult getTankstellenListData(String apikey, String locationIDs) {
        return this.getTankerkoenigListResult(apikey, locationIDs);
    }

    private String getResponseString(String apikey, String locationIDs) throws IOException {

        String urlbase = "https://creativecommons.tankerkoenig.de/json/prices.php?";
        String urlcomplete = urlbase + "ids=" + locationIDs + "&apikey=" + apikey;
        try {
            String userAgent = "openHAB, Tankerkoenig-Binding Version ";
            Version version = FrameworkUtil.getBundle(this.getClass()).getVersion();
            userAgent = userAgent + version.toString();
            URL url = new URL(urlcomplete);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", userAgent);
            return IOUtils.toString(connection.getInputStream());
        } catch (MalformedURLException e) {
            logger.error("Error in getResponseString: {}", e.toString());
            return null;
        }
    }

    private TankerkoenigListResult getTankerkoenigListResult(String apikey, String locationIDs) {
        String jsonData = "";
        try {
            jsonData = getResponseString(apikey, locationIDs);
            return GSON.fromJson(jsonData, TankerkoenigListResult.class);
        } catch (IOException e) {
            logger.error("Error in getTankerkoenigListResult: {}", e);
            return TankerkoenigListResult.emptyResult();
        }
    }
}
