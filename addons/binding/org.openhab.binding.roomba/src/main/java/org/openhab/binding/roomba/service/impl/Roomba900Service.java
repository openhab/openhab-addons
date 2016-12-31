/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.roomba.service.impl;

import static org.openhab.binding.roomba.RoombaBindingConstants.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.roomba.model.RoombaOperation;
import org.openhab.binding.roomba.model.RoombaPayload;
import org.openhab.binding.roomba.model.exception.RoombaCommunicationException;
import org.openhab.binding.roomba.service.RoombaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * An implementation of Roomba's features for the Roomba 900 series.
 *
 * @author Stephen Liang
 *
 */
public class Roomba900Service implements RoombaService {
    public HttpClient httpClient;
    private String roombaEndpoint;
    private String roombaPassword;
    private Gson gson;

    private Logger logger = LoggerFactory.getLogger(Roomba900Service.class);

    /**
     * Constructor
     *
     * @param roombaIpAddress - The IP Address for the Roomba
     * @param roombaPassword - The password for the Roomba
     * @throws Exception If an exception occurs while starting the http client
     */
    public Roomba900Service(String roombaIpAddress, String roombaPassword) throws Exception {
        startHttpClient();

        this.gson = new Gson();
        this.roombaPassword = roombaPassword;
        this.roombaEndpoint = String.format("https://%s/umi", roombaIpAddress);
    }

    /**
     * Starts the HTTP Client to connect to the Roomba
     *
     * @throws Exception If an exception occurs while starting the http client
     */
    private void startHttpClient() throws Exception {
        this.httpClient = new HttpClient(new SslContextFactory(true));
        this.httpClient.setConnectTimeout(ROOMBA_API_TIMEOUT);
        this.httpClient.start();
    }

    /**
     * Pings the Roomba to if it is ok or not
     *
     * @return True if the Roomba is ok, false otherwise.
     */
    @Override
    public boolean isOk() throws RoombaCommunicationException {
        RoombaPayload roombaPayload = new RoombaPayload("get", "mssn");

        callRoomba(roombaPayload);

        return true;
    }

    /**
     * Starts the Roomba's cleaning process
     */
    @Override
    public void start() throws RoombaCommunicationException {
        RoombaPayload roombaPayload = new RoombaPayload("set", "cmd", new RoombaOperation("start"));

        callRoomba(roombaPayload);
    }

    /**
     * Pauses the Roomba's cleaning process
     */
    @Override
    public void pause() throws RoombaCommunicationException {
        RoombaPayload roombaPayload = new RoombaPayload("set", "cmd", new RoombaOperation("pause"));

        callRoomba(roombaPayload);
    }

    /**
     * Stops the Roomba's cleaning process
     */
    @Override
    public void stop() throws RoombaCommunicationException {
        RoombaPayload roombaPayload = new RoombaPayload("set", "cmd", new RoombaOperation("stop"));

        callRoomba(roombaPayload);
    }

    /**
     * Resumes a previously paused Roomba's cleaning
     */
    @Override
    public void resume() throws RoombaCommunicationException {
        RoombaPayload roombaPayload = new RoombaPayload("set", "cmd", new RoombaOperation("resume"));

        callRoomba(roombaPayload);
    }

    /**
     * Tells the Roomba to return to the charging dock
     */
    @Override
    public void dock() throws RoombaCommunicationException {
        RoombaPayload roombaPayload = new RoombaPayload("set", "cmd", new RoombaOperation("dock"));

        callRoomba(roombaPayload);
    }

    /**
     * Calls a Roomba's API Endpoint given the payload provided
     *
     * @param roombaPayload The payload to send to the Roomba
     * @return The response from the Roomba
     * @throws RoombaCommunicationException If an error occurs while sending the call to the Roomba
     */
    private ContentResponse callRoomba(RoombaPayload roombaPayload) throws RoombaCommunicationException {
        String jsonPayload = gson.toJson(roombaPayload);
        String basicAuthentication = "Basic " + B64Code.encode("user:" + roombaPassword, StringUtil.__ISO_8859_1);

        logger.trace("Connecting to {}, with payload: {}", roombaEndpoint, jsonPayload);

        try {
            ContentResponse response = httpClient.newRequest(roombaEndpoint).method(HttpMethod.POST)
                    .content(new BytesContentProvider(jsonPayload.getBytes())).agent(ROOMBA_USER_AGENT)
                    .header(HttpHeader.AUTHORIZATION, basicAuthentication).send();

            logger.trace("response received: {}", response.getContentAsString());

            if (response.getStatus() != 200) {
                logger.warn("Received a bad status from the Roomba: {}", response.getStatus());
                throw new RoombaCommunicationException("Received bad error code from roomba: " + response.getStatus());
            }

            return response;
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new RoombaCommunicationException("Unable to communicate with roomba", e);
        }
    }

}
