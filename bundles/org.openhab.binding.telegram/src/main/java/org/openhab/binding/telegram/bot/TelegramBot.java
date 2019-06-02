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
package org.openhab.binding.telegram.bot;

import static org.openhab.binding.telegram.internal.TelegramBindingConstants.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.telegram.internal.TelegramHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * The class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jens Runge - Initial contribution
 */
@NonNullByDefault
public class TelegramBot extends TelegramLongPollingBot {
    private final String botToken, botUserName;
    private final List<Long> chatIds;
    private final TelegramHandler telegramHandler;
    private static final Logger LOGGER = LoggerFactory.getLogger(TelegramBot.class);
    // Keep track of the callback id created by Telegram. This must be sent back in the answerCallbackQuery
    // to stop the progress bar in the Telegram client
    // TODO: if selective chat ids are supported, replyId as a key is probably not sufficient
    private final Map<String, String> replyIdToCallbackId = new HashMap<>();
    // Keep track of message id sent with reply markup because we want to remove the markup after the user provided an
    // answer and need the id of the original message
    private final Map<String, Integer> replyIdToMessageId = new HashMap<>();

    public TelegramBot(String botToken, String botUserName, List<Long> chatIds, TelegramHandler telegramHandler) {
        super();
        this.botToken = botToken;
        this.botUserName = botUserName;
        this.telegramHandler = telegramHandler;
        this.chatIds = chatIds;
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public void onUpdateReceived(@Nullable Update update) {
        if (update != null) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                Message message = update.getMessage();
                if (!chatIds.contains(message.getChatId())) {
                    return; // this is very important regarding security to avoid commands from an unknown chat
                }

                String lastMessageText = message.getText();
                String lastMessageDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                        .format(new Date(message.getDate().longValue() * 1000));
                String lastMessageName = message.getFrom().getFirstName() + " " + message.getFrom().getLastName();
                String lastMessageUsername = message.getFrom().getUserName();

                telegramHandler.updateChannel(LASTMESSAGETEXT, lastMessageText);
                telegramHandler.updateChannel(LASTMESSAGEDATE, lastMessageDate);
                telegramHandler.updateChannel(LASTMESSAGENAME, lastMessageName);
                telegramHandler.updateChannel(LASTMESSAGEUSERNAME, lastMessageUsername);
            } else if (update.hasCallbackQuery() && update.getCallbackQuery().getMessage().hasText()) {
                String[] callbackData = update.getCallbackQuery().getData().split(" ", 2);

                if (callbackData.length == 2) {
                    String replyId = callbackData[0];
                    String lastMessageText = callbackData[1];
                    String lastMessageDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                            .format(System.currentTimeMillis());
                    String lastMessageName = update.getCallbackQuery().getFrom().getFirstName() + " "
                            + update.getCallbackQuery().getFrom().getLastName();
                    String lastMessageUsername = update.getCallbackQuery().getMessage().getFrom().getUserName();
                    replyIdToCallbackId.put(replyId, update.getCallbackQuery().getId());

                    telegramHandler.updateChannel(LASTMESSAGETEXT, lastMessageText);
                    telegramHandler.updateChannel(LASTMESSAGEDATE, lastMessageDate);
                    telegramHandler.updateChannel(LASTMESSAGENAME, lastMessageName);
                    telegramHandler.updateChannel(LASTMESSAGEUSERNAME, lastMessageUsername);
                    telegramHandler.updateChannel(REPLYID, replyId);
                } else {
                    LOGGER.warn("The received callback query {} has not the right format (must be seperated by spaces)",
                            update.getCallbackQuery().getData());
                }
            }
        }
    }

    public List<Long> getChatIds() {
        return chatIds;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Nullable
    public String getCallbackId(String replyId) {
        return replyIdToCallbackId.get(replyId);
    }

    public void addMessageId(String replyId, Integer messageId) {
        replyIdToMessageId.put(replyId, messageId);
    }

    public Integer removeMessageId(String replyId) {
        return replyIdToMessageId.remove(replyId);
    }

}
