/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.tankerkoenig.internal.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

/***
 * The {@link Tankerkoenig} class represents the basic model for the tankerkoenig binding
 *
 * @author Dennis Dollinger
 *
 */
public class Tankerkoenig {

    public Tankerkoenig(String apiKey, String locationID) {

        this.apiKey = apiKey;
        this.locationID = locationID;
    }

    /**
     *
     * @return Returns a validation result. Empty String is OK, otherwise the error message will be returned
     */
    public String validate() {

        String jsonData = "";

        try {
            jsonData = getResponseString(this.getApiKey(), this.getLocationID());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "Error. Binding was not able to reach tankerkoenig.de";
        }

        Gson gson = new Gson();
        TankerkoenigResult res = gson.fromJson(jsonData, TankerkoenigResult.class);

        if (res.getStatus().equals("error")) {
            return "Error while initializing. Tankerkoenig.de message: '" + res.getMessage() + "'";
        }

        return "";
    }

    /**
     * Updates the data by getting the infos from the tankerkoenig.de api
     */
    public void update() {
        String jsonData = "";

        try {
            jsonData = getResponseString(this.getApiKey(), this.getLocationID());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Gson gson = new Gson();
        TankerkoenigResult res = gson.fromJson(jsonData, TankerkoenigResult.class);

        this.setResult(res);

    }

    String response = "";

    private String getResponseString(String apikey, String locationID) throws IOException {
        String urlbase = "https://creativecommons.tankerkoenig.de/json/detail.php?";
        String urlcomplete = urlbase + "id=" + locationID + "&apikey=" + apikey;

        try {

            URL url = new URL(urlcomplete);
            URLConnection connection = url.openConnection();
            response = IOUtils.toString(connection.getInputStream());
            return response;
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private String apiKey;
    private String locationID;

    public String getApiKey() {
        return apiKey;
    }

    public String getLocationID() {
        return locationID;
    }

    private TankerkoenigResult result;

    public TankerkoenigResult getResult() {
        return result;
    }

    public void setResult(TankerkoenigResult result) {
        this.result = result;
    }

}
