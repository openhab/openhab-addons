/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.openhab.binding.nest.NestBindingConstants;
import org.openhab.binding.nest.config.NestBridgeConfiguration;
import org.openhab.binding.nest.internal.data.AccessTokenData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Keeps track of the access token, refreshing it if needed.
 *
 * @author David Bennett - Initial contribution
 */
public class NestAccessToken {
    private Logger logger = LoggerFactory.getLogger(NestAccessToken.class);
    private NestBridgeConfiguration config;
    private String accessToken;
    private String folderName;
    private HttpClient httpClient;

    private static final String ACCESS_TOKEN_FILE_NAME = "accesstoken.txt";

    /**
     * Create the helper class for the nest access token. Also creates the folder
     * to put the access token data in if it does not already exist.
     *
     * @param config The configuration to use for the token
     */
    public NestAccessToken(NestBridgeConfiguration config, HttpClient httpClient) {
        this.config = config;
        this.folderName = ConfigConstants.getUserDataFolder() + "/" + NestBindingConstants.BINDING_ID;
        this.httpClient = httpClient;
        File folder = new File(folderName);
        if (!folder.exists()) {
            logger.debug("Creating directory {}", folderName);
            folder.mkdirs();
        }
    }

    /**
     * Get the current access token, refreshing if needed. Also reads it from the disk
     * if it is stored there.
     *
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws InterruptedException
     */
    public String getAccessToken() throws InterruptedException, TimeoutException, ExecutionException {
        if (config.accessToken == null) {
            // See if it is written to disk.
            File file = new File(this.folderName, ACCESS_TOKEN_FILE_NAME);
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
                    String line = reader.readLine();
                    reader.close();
                    if (line != null && line.length() > 0) {
                        this.accessToken = line;
                    }
                } catch (IOException e) {
                    logger.error("Error reading access token file {}", file, e);
                }
            }
            refreshAccessToken();
        } else {
            accessToken = config.accessToken;
        }
        return accessToken;
    }

    private void refreshAccessToken() throws InterruptedException, TimeoutException, ExecutionException {
        try {
            StringBuilder urlBuilder = new StringBuilder(NestBindingConstants.NEST_ACCESS_TOKEN_URL)
                    .append("?client_id=").append(config.clientId).append("&client_secret=").append(config.clientSecret)
                    .append("&code=").append(config.pincode).append("&grant_type=authorization_code");
            logger.debug("Result {}", urlBuilder.toString());
            Request request = httpClient.POST(urlBuilder.toString())
                    .header("Content-Type", "application/x-www-form-urlencoded").timeout(10, TimeUnit.SECONDS);
            ContentResponse response = request.send();
            logger.debug("Result {}", response.getContentAsString());
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            AccessTokenData data = gson.fromJson(response.getContentAsString(), AccessTokenData.class);
            if (data.getAccessToken() != null) {
                accessToken = data.getAccessToken();
                logger.debug("Access token {}", accessToken);
                logger.debug("Expiration Time {}", data.getExpiresIn());
                // Write the token to a file so we can reload it later.
                File file = new File(this.folderName, ACCESS_TOKEN_FILE_NAME);
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
                    writer.write(accessToken);
                    writer.close();
                } catch (IOException e) {
                    logger.error("Error reading access token file {}", file, e);
                }
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e1) {
            logger.error("Unable to get the nest access token ", e1);
            throw e1;
        }
    }
}
