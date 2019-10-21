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

import static org.openhab.binding.telegram.internal.TelegramBindingConstants.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;

import okhttp3.OkHttpClient;

/**
 * The {@link TelegramHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jens Runge - Initial contribution
 */
@NonNullByDefault
public class TelegramHandler extends BaseThingHandler {

    private class ReplyKey {

        final Long chatId;
        final String replyId;

        public ReplyKey(Long chatId, String replyId) {
            this.chatId = chatId;
            this.replyId = replyId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(chatId, replyId);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ReplyKey other = (ReplyKey) obj;
            return Objects.equals(chatId, other.chatId) && Objects.equals(replyId, other.replyId);
        }
    }

    private final List<Long> chatIds = new ArrayList<Long>();
    private final Logger LOGGER = LoggerFactory.getLogger(TelegramHandler.class);

    // Keep track of the callback id created by Telegram. This must be sent back in the answerCallbackQuery
    // to stop the progress bar in the Telegram client
    private final Map<ReplyKey, String> replyIdToCallbackId = new HashMap<>();
    // Keep track of message id sent with reply markup because we want to remove the markup after the user provided an
    // answer and need the id of the original message
    private final Map<ReplyKey, Integer> replyIdToMessageId = new HashMap<>();

    @Nullable
    private TelegramBot bot;
    @Nullable
    private OkHttpClient client;

    @Nullable
    private ParseMode parseMode;

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

        String botToken = config.getBotToken();
        for (String chatIdStr : config.getChatIds()) {
            try {
                chatIds.add(Long.valueOf(chatIdStr));
            } catch (NumberFormatException e) {
                LOGGER.warn("The chat id {} is not a number and will be ignored", chatIdStr);
            }
        }
        if (config.getParseMode() != null) {
            try {
                parseMode = ParseMode.valueOf(config.getParseMode());
            } catch (IllegalArgumentException e) {
                LOGGER.warn("parseMode is invalid and will be ignored. Only Markdown or HTML are allowed values");
            }
        }

        OkHttpClient client = new OkHttpClient();
        updateStatus(ThingStatus.ONLINE);
        TelegramBot localBot = bot = new TelegramBot.Builder(botToken).okHttpClient(client).build();
        localBot.setUpdatesListener(new UpdatesListener() {

            @Override
            @NonNullByDefault({})
            public int process(List<Update> updates) {
                for (Update update : updates) {
                    if (update.message() != null && update.message().text() != null) {
                        Message message = update.message();
                        Long chatId = message.chat().id();
                        if (!chatIds.contains(chatId)) {
                            continue; // this is very important regarding security to avoid commands from an unknown
                                      // chat
                        }

                        String lastMessageText = message.text();
                        String lastMessageDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                                .format(new Date(message.date().longValue() * 1000));
                        String lastMessageName = message.from().firstName() + " " + message.from().lastName();
                        String lastMessageUsername = message.from().username();

                        updateChannel(LASTMESSAGETEXT, lastMessageText);
                        updateChannel(LASTMESSAGEDATE, lastMessageDate);
                        updateChannel(LASTMESSAGENAME, lastMessageName);
                        updateChannel(LASTMESSAGEUSERNAME, lastMessageUsername);
                        updateChannel(CHATID, chatId.toString());
                    } else if (update.callbackQuery() != null && update.callbackQuery().message() != null
                            && update.callbackQuery().message().text() != null) {
                        String[] callbackData = update.callbackQuery().data().split(" ", 2);

                        if (callbackData.length == 2) {
                            String replyId = callbackData[0];
                            String lastMessageText = callbackData[1];
                            String lastMessageDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                                    .format(System.currentTimeMillis());
                            String lastMessageName = update.callbackQuery().from().firstName() + " "
                                    + update.callbackQuery().from().lastName();
                            String lastMessageUsername = update.callbackQuery().message().from().username();
                            Long chatId = update.callbackQuery().message().chat().id();
                            replyIdToCallbackId.put(new ReplyKey(chatId, replyId), update.callbackQuery().id());
                            LOGGER.debug("Received callbackId {} for chatId {} and replyId {}",
                                    update.callbackQuery().id(), chatId, replyId);
                            updateChannel(LASTMESSAGETEXT, lastMessageText);
                            updateChannel(LASTMESSAGEDATE, lastMessageDate);
                            updateChannel(LASTMESSAGENAME, lastMessageName);
                            updateChannel(LASTMESSAGEUSERNAME, lastMessageUsername);
                            updateChannel(CHATID, chatId.toString());
                            updateChannel(REPLYID, replyId);
                        } else {
                            LOGGER.warn(
                                    "The received callback query {} has not the right format (must be seperated by spaces)",
                                    update.callbackQuery().data());
                        }
                    }
                }
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            }
        });
    }

    @Override
    public void dispose() {
        LOGGER.debug("Trying to dispose Telegram client");
        OkHttpClient localClient = client;
        if (localClient != null) {
            localClient.dispatcher().executorService().shutdown();
            localClient.connectionPool().evictAll();
            LOGGER.debug("Telegram client closed");
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

    public List<Long> getChatIds() {
        return chatIds;
    }

    public void addMessageId(Long chatId, String replyId, Integer messageId) {
        replyIdToMessageId.put(new ReplyKey(chatId, replyId), messageId);
    }

    @Nullable
    public String getCallbackId(Long chatId, String replyId) {
        return replyIdToCallbackId.get(new ReplyKey(chatId, replyId));
    }

    public Integer removeMessageId(Long chatId, String replyId) {
        return replyIdToMessageId.remove(new ReplyKey(chatId, replyId));
    }

    @Nullable
    public ParseMode getParseMode() {
        return parseMode;
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    public <T extends BaseRequest, R extends BaseResponse> R execute(BaseRequest<T, R> request) {
        TelegramBot localBot = bot;
        return localBot != null ? localBot.execute(request) : null;
    }

}
