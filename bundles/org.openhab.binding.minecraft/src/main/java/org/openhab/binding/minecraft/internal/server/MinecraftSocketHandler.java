/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.minecraft.internal.server;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.minecraft.internal.message.OHMessage;
import org.openhab.binding.minecraft.internal.message.data.PlayerData;
import org.openhab.binding.minecraft.internal.message.data.ServerData;
import org.openhab.binding.minecraft.internal.message.data.SignData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.firebase.tubesock.WebSocketEventHandler;
import com.firebase.tubesock.WebSocketException;
import com.firebase.tubesock.WebSocketMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Handles sending and receiving messages from Minecraft server.
 *
 * @author Mattias Markehed - Initial contribution
 */
public class MinecraftSocketHandler implements WebSocketEventHandler {

    private Logger logger = LoggerFactory.getLogger(MinecraftSocketHandler.class);

    private BehaviorSubject<ServerData> serverRx = BehaviorSubject.create();
    private BehaviorSubject<List<SignData>> signsRx = BehaviorSubject.create();
    private BehaviorSubject<List<PlayerData>> playersRx = BehaviorSubject.create();

    private Gson gson = new GsonBuilder().create();

    @Override
    public void onClose() {
        logger.info("Connection to minecraft server closed");
    }

    @Override
    public void onError(WebSocketException e) {
        logger.error("Server error {}", e.getMessage(), e);
    }

    @Override
    public void onOpen() {
        logger.info("Connection to minecraft server opened");
    }

    @Override
    public void onLogMessage(String s) {
        logger.info("Log message: {}", s);
    }

    @Override
    public void onMessage(WebSocketMessage message) {
        String msg = message.getText();

        if (msg != null) {
            OHMessage ohMessage = gson.fromJson(msg, OHMessage.class);
            int messageType = ohMessage.getMessageType();
            if (OHMessage.MESSAGE_TYPE_SERVERS == messageType) {
                ServerData serverData = gson.fromJson(ohMessage.getMessage(), ServerData.class);
                serverRx.onNext(serverData);
            } else if (OHMessage.MESSAGE_TYPE_PLAYERS == messageType) {
                List<PlayerData> playerData = gson.fromJson(ohMessage.getMessage(),
                        new TypeToken<ArrayList<PlayerData>>() {
                        }.getType());
                playersRx.onNext(playerData);
            } else if (OHMessage.MESSAGE_TYPE_SIGNS == messageType) {
                List<SignData> signsData = gson.fromJson(ohMessage.getMessage(), new TypeToken<ArrayList<SignData>>() {
                }.getType());
                signsRx.onNext(signsData);
            }
        }
    }

    /**
     * Get observable emitting server items.
     *
     * @return observable emitting server items.
     */
    public Observable<ServerData> getServerRx() {
        return serverRx.asObservable();
    }

    /**
     * Get observable emitting sign items.
     *
     * @return observable emitting sign items.
     */
    public Observable<List<SignData>> getSignsRx() {
        return signsRx.asObservable();
    }

    /**
     * Get observable emitting player items.
     *
     * @return observable emitting player items.
     */
    public Observable<List<PlayerData>> getPlayersRx() {
        return playersRx.asObservable();
    }
}
