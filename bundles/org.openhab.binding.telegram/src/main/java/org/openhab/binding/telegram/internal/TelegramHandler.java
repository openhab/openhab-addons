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
package org.openhab.binding.telegram.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.telegram.bot.TelegramActions;
import org.openhab.binding.telegram.bot.TelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;

/**
 * The {@link TelegramHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jens Runge - Initial contribution
 */
@NonNullByDefault
public class TelegramHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TelegramHandler.class);
    @Nullable
    private TelegramBot bot;
    @Nullable
    private BotSession session;

    public TelegramHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands to handle
    }

    @Override
    public void initialize() {
        TelegramConfiguration config = getConfigAs(TelegramConfiguration.class);

        updateStatus(ThingStatus.ONLINE);
        String botName = config.getBotUsername();
        String botToken = config.getBotToken();
        List<Long> chatIds = new ArrayList<>();
        for (String chatIdStr : config.getChatIds()) {
            try {
                chatIds.add(Long.parseLong(chatIdStr));
            } catch (NumberFormatException e) {
                logger.warn("The chat id {} is not a number and will be ignored", chatIdStr);
            }
        }

        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        try {
            bot = new TelegramBot(botToken, botName, chatIds, this);
            session = botsApi.registerBot(bot);
            updateStatus(ThingStatus.ONLINE);
        } catch (TelegramApiException e) {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void dispose() {
        if (session != null) {
            BotSession s = session;
            if (s.isRunning()) {
                s.stop();
            }
        }
    }

    public void updateChannel(String channelName, String stateString) {
        State messageState = new StringType(stateString);
        updateState(new ChannelUID(getThing().getUID(), channelName), messageState);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(TelegramActions.class);
    }

    @Nullable
    public TelegramBot getBot() {
        return bot;
    }

}
