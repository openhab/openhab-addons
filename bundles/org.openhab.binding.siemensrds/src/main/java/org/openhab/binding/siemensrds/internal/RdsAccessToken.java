/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.siemensrds.internal;

import static org.openhab.binding.siemensrds.internal.RdsBindingConstants.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName; 

/**
 * Interface to the Access Token for a particular User
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
class RdsAccessToken {

    protected static final Logger LOGGER = 
            LoggerFactory.getLogger(RdsAccessToken.class);

    @SerializedName("access_token")
    private String accessToken;
    @SerializedName(".expires")
    private String expires;
        
    private Date expDate = null;
    
    /*
     * private method:
     * execute the HTTP POST on the server
     */
    private static String httpGetTokenJson(String user, String password) { 
        String result = "";

        try {
            URL url = new URL(URL_TOKEN);
            
            HttpsURLConnection https = 
                    (HttpsURLConnection) url.openConnection();
                    
            https.setRequestMethod(HTTP_POST);
    
            https.setRequestProperty(HDR_USER_AGENT,
                                     VAL_USER_AGENT);
            
            https.setRequestProperty(HDR_ACCEPT, 
                                     VAL_ACCEPT);
            
            https.setRequestProperty(HDR_CONT_TYPE,
                                     VAL_CONT_PLAIN);
            
            https.setRequestProperty(HDR_SUB_KEY, 
                                     VAL_SUB_KEY);
                    
            String requestStr = 
                    String.format(TOKEN_REQ, user, password);  
    
            https.setDoOutput(true);
            DataOutputStream wr = 
                    new DataOutputStream(https.getOutputStream());
            wr.writeBytes(requestStr);
            wr.flush();
            wr.close();
    
            int responseCode = https.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { 
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(https.getInputStream(), "UTF8"));
                String inStr;
                StringBuffer response = new StringBuffer();
                while ((inStr = in.readLine()) != null) {
                    response.append(inStr);
                } 
                in.close();
                result  = response.toString();
            } else {
                LOGGER.debug("httpGetTokenJson: http error={}", responseCode);
              }
        } catch (Exception e) {
            LOGGER.debug("httpGetTokenJson: exception={}", e.getMessage());
        }

        return result;
    }

    
    /*
     * public method:
     * execute a POST on the cloud server, parse the JSON, 
     * and create a class that encapsulates the data
     */
    public static RdsAccessToken create(String user, String password) {
        RdsAccessToken result = null;
        String json = httpGetTokenJson(user, password);

        try {
            if (!json.equals("")) {
                Gson gson = new Gson();
                result = gson.fromJson(json, RdsAccessToken.class);
            }
        } catch (Exception e) {
            LOGGER.debug("create: exception={}", e.getMessage());
        }
        
        return result;
    }
    
    
    /*
     * public method:
     * return the access token
     */
    public String getToken() {
        return accessToken;
    }
    
    /*
     * public method:
     * check if the token has expired
     */
    public Boolean isExpired() {
       if (expDate == null) {
          try {
            expDate = 
              new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z").parse(expires);
          } catch (ParseException e) {
              LOGGER.debug("isExpired: exception={}", e.getMessage());

              Calendar calendar = Calendar.getInstance();
              calendar.setTime(new Date());
              calendar.add(Calendar.DAY_OF_YEAR, 1);
              expDate = calendar.getTime();
          }
       }
       return (expDate == null || expDate.before(new Date()));  
    }
    
}
