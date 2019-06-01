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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingActionsScope;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.telegram.internal.TelegramHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Provides the actions for the Telegram API.
 *
 * @author Alexander Krasnogolowy - Initial contribution
 *
 */
@ThingActionsScope(name = "telegram")
@NonNullByDefault
public class TelegramActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(TelegramActions.class);
    private @Nullable TelegramHandler handler;

    @Nullable
    private TelegramBot getBot() {
        if (handler != null) {
            TelegramBot bot = handler.getBot();
            if (bot != null) {
                return bot;
            } else {
                logger.warn("Bot '{}' not defined; action skipped.", handler);
            }
        } else {
            logger.warn("Handler '{}' not defined; action skipped.", this);
        }
        return null;
    }

    @RuleAction(label = "Telegram answer", description = "Sends a Telegram answer via Telegram API")
    public boolean sendTelegramAnswer(@ActionInput(name = "replyId") @Nullable String replyId,
            @ActionInput(name = "message") @Nullable String message) {
        if (replyId == null) {
            logger.warn("ReplyId not defined; action skipped.");
            return false;
        }
        TelegramBot bot = getBot();
        if (bot != null) {
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery()
                    .setCallbackQueryId(bot.getCallbackId(replyId)); // we could directly set the text here, but this
                                                                     // doesn't result in a real message only in a
                                                                     // little popup or in an alert, so the only purpose
                                                                     // is to stop the progress bar on client side
            try {
                bot.execute(answerCallbackQuery);
                Integer messageId = bot.removeMessageId(replyId);
                if (messageId != null) {
                    EditMessageReplyMarkup editReplyMarkup = new EditMessageReplyMarkup().setReplyMarkup(null)
                            .setMessageId(messageId); // remove reply markup from old message

                    for (Long chatId : bot.getChatIds()) {
                        editReplyMarkup.setChatId(chatId);
                        bot.execute(editReplyMarkup);
                    }
                }
                return message != null ? sendTelegram(message) : true;
            } catch (TelegramApiException e) {
                logger.warn("Failed to send telegram: ", e);
            }
        }
        return false;
    }

    @RuleAction(label = "Telegram message", description = "Sends a Telegram via Telegram API")
    public boolean sendTelegram(@ActionInput(name = "message") @Nullable String message) {
        return sendTelegram(message, (String) null);
    }

    @RuleAction(label = "Telegram MEssage", description = "Sends a Telegram via Telegram API")
    public boolean sendTelegram(@ActionInput(name = "message") @Nullable String message,
            @ActionInput(name = "replyId") @Nullable String replyId,
            @ActionInput(name = "buttons") @Nullable String... buttons) {
        if (message == null) {
            logger.warn("Message not defined; action skipped.");
            return false;
        }

        TelegramBot bot = getBot();
        if (bot != null) {
            SendMessage sendMessage = new SendMessage().setText(message).setParseMode("Markdown");
            if (replyId != null) {
                if (!replyId.contains(" ")) {
                    if (buttons.length > 0) {
                        InlineKeyboardMarkup keyBoardMarkup = new InlineKeyboardMarkup();
                        List<List<InlineKeyboardButton>> keyboard2D = new ArrayList<>();
                        List<InlineKeyboardButton> keyboard = new ArrayList<>();
                        keyboard2D.add(keyboard);
                        for (String button : buttons) {
                            keyboard.add(
                                    new InlineKeyboardButton().setText(button).setCallbackData(replyId + " " + button));
                        }
                        keyBoardMarkup.setKeyboard(keyboard2D);
                        sendMessage.setReplyMarkup(keyBoardMarkup);
                    } else {
                        logger.warn(
                                "The replyId {} for message {} is given, but no buttons are defined. ReplyMarkup will be ignored.",
                                replyId, message);
                    }
                } else {
                    logger.warn("replyId {} must not contain spaces. ReplyMarkup will be ignored.", replyId);
                }
            }
            try {
                for (Long chatId : bot.getChatIds()) {
                    sendMessage.setChatId(chatId);
                    Message retMessage = bot.execute(sendMessage);
                    bot.addMessageId(replyId, retMessage.getMessageId());
                }
                return true; // TODO: evaluate response and check if true/false
            } catch (TelegramApiException e) {
                logger.warn("Failed to send telegram: ", e);
            }
        }
        return false;
    }

    @RuleAction(label = "Telegram message", description = "Sends a Telegram via Telegram API")
    public boolean sendTelegram(@ActionInput(name = "message") @Nullable String format,
            @ActionInput(name = "args") @Nullable Object... args) {
        return sendTelegram(String.format(format, args));
    }

    @RuleAction(label = "Telegram photo", description = "Sends a Picture via Telegram API")
    public boolean sendTelegramPhoto(@ActionInput(name = "photoURL") @Nullable String photoURL,
            @ActionInput(name = "caption") @Nullable String caption) {
        if (photoURL == null) {
            logger.warn("Photo URL not defined; unable to retrieve photo for sending.");
            return false;
        }
        TelegramBot bot = getBot();
        if (bot != null) {
            try {
                if (photoURL.toLowerCase().startsWith("http")) {
                    // load image from url
                    logger.debug("Photo URL provided.");
                    SendPhoto sendPhoto = new SendPhoto().setPhoto(photoURL).setCaption(caption);
                    for (Long chatId : bot.getChatIds()) {
                        sendPhoto.setChatId(chatId);
                        bot.execute(sendPhoto);
                    }
                    return true;
                } else if (photoURL.toLowerCase().startsWith("file")) {
                    // Load image from local file system
                    logger.debug("Read file from local file system: {}", photoURL);
                    try {
                        URL url = new URL(photoURL);
                        File f = Paths.get(url.getPath()).toFile();
                        SendPhoto sendPhoto = new SendPhoto().setPhoto(f).setCaption(caption);
                        for (Long chatId : bot.getChatIds()) {
                            sendPhoto = sendPhoto.setChatId(chatId);
                            bot.execute(sendPhoto);
                        }
                        return true;
                    } catch (MalformedURLException e) {
                        logger.warn("Malformed URL: {}", photoURL);
                    }
                } else {
                    // Load image from provided base64 image
                    logger.debug("Photo base64 provided; converting to binary.");
                    InputStream is;
                    try {
                        is = Base64.getDecoder().wrap(new ByteArrayInputStream(photoURL.getBytes("UTF-8")));
                        SendPhoto sendPhoto = new SendPhoto().setPhoto(caption, is);
                        for (Long chatId : bot.getChatIds()) {
                            sendPhoto.setChatId(chatId);
                            bot.execute(sendPhoto);
                        }
                        return true;
                    } catch (UnsupportedEncodingException e) {
                        logger.warn("Cannot parse data fetched from photo URL as an image. Error: {}", e.getMessage());
                    }
                }
            } catch (TelegramApiException e) {
                logger.warn("Failed to send telegram photo: ", e);
            }
        }
        return false;
    }

    // legacy delegate methods

    public static boolean sendTelegram(@Nullable ThingActions actions, @Nullable String format,
            @Nullable Object... args) {
        if (actions instanceof TelegramActions) {
            return ((TelegramActions) actions).sendTelegram(format, args);
        } else {
            throw new IllegalArgumentException("Instance is not an TelegramActions class.");
        }
    }

    public static boolean sendTelegram(@Nullable ThingActions actions, @Nullable String message,
            @Nullable String replyId, @Nullable String... buttons) {
        if (actions instanceof TelegramActions) {
            return ((TelegramActions) actions).sendTelegram(message, replyId, buttons);
        } else {
            throw new IllegalArgumentException("Instance is not an TelegramActions class.");
        }
    }

    public static boolean sendTelegramPhoto(@Nullable ThingActions actions, @Nullable String photoURL,
            @Nullable String caption) {
        if (actions instanceof TelegramActions) {
            return ((TelegramActions) actions).sendTelegramPhoto(photoURL, caption);
        } else {
            throw new IllegalArgumentException("Instance is not an TelegramActions class.");
        }
    }

    public static boolean sendTelegramAnswer(@Nullable ThingActions actions, @Nullable String replyId,
            @Nullable String message) {
        if (actions instanceof TelegramActions) {
            return ((TelegramActions) actions).sendTelegramAnswer(replyId, message);
        } else {
            throw new IllegalArgumentException("Instance is not an TelegramActions class.");
        }
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (TelegramHandler) handler;

    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}