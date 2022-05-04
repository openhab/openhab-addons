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
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Base64;

import org.apache.commons.io.IOUtils;
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

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;

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

    private boolean evaluateResponse(@Nullable BaseResponse response) {
        if (response != null && !response.isOk()) {
            logger.warn("Failed to send telegram message: {}", response.description());
            return false;
        }
        return true;
    }

    @RuleAction(label = "Telegram answer", description = "Sends a Telegram answer via Telegram API")
    public boolean sendTelegramAnswer(@ActionInput(name = "chatId") @Nullable Long chatId,
            @ActionInput(name = "replyId") @Nullable String replyId,
            @ActionInput(name = "message") @Nullable String message) {
        if (replyId == null) {
            logger.warn("ReplyId not defined; action skipped.");
            return false;
        }
        if (chatId == null) {
            logger.warn("chatId not defined; action skipped.");
            return false;
        }
        TelegramHandler localHandler = handler;
        if (localHandler != null) {
            String callbackId = localHandler.getCallbackId(chatId, replyId);
            if (callbackId != null) {
                AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(
                        localHandler.getCallbackId(chatId, replyId));
                logger.debug("AnswerCallbackQuery for chatId {} and replyId {} is the callbackId {}", chatId, replyId,
                        localHandler.getCallbackId(chatId, replyId));
                // we could directly set the text here, but this
                // doesn't result in a real message only in a
                // little popup or in an alert, so the only purpose
                // is to stop the progress bar on client side
                if (!evaluateResponse(localHandler.execute(answerCallbackQuery))) {
                    return false;
                }
            }
            Integer messageId = localHandler.removeMessageId(chatId, replyId);
            logger.debug("remove messageId {} for chatId {} and replyId {}", messageId, chatId, replyId);

            EditMessageReplyMarkup editReplyMarkup = new EditMessageReplyMarkup(chatId, messageId.intValue())
                    .replyMarkup(new InlineKeyboardMarkup(new InlineKeyboardButton[0]));// remove reply markup from
                                                                                        // old message
            if (!evaluateResponse(localHandler.execute(editReplyMarkup))) {
                return false;
            }
            return message != null ? sendTelegram(chatId, message) : true;

        }
        return false;
    }

    @RuleAction(label = "Telegram answer", description = "Sends a Telegram answer via Telegram API")
    public boolean sendTelegramAnswer(@ActionInput(name = "replyId") @Nullable String replyId,
            @ActionInput(name = "message") @Nullable String message) {
        TelegramHandler localHandler = handler;
        if (localHandler != null) {
            for (Long chatId : localHandler.getChatIds()) {
                if (!sendTelegramAnswer(chatId, replyId, message)) {
                    return false;
                }
            }
        }
        return true;
    }

    @RuleAction(label = "Telegram message", description = "Sends a Telegram via Telegram API")
    public boolean sendTelegram(@ActionInput(name = "chatId") @Nullable Long chatId,
            @ActionInput(name = "message") @Nullable String message) {
        return sendTelegramGeneral(chatId, message, (String) null);
    }

    @RuleAction(label = "Telegram message", description = "Sends a Telegram via Telegram API")
    public boolean sendTelegram(@ActionInput(name = "message") @Nullable String message) {
        TelegramHandler localHandler = handler;
        if (localHandler != null) {
            for (Long chatId : localHandler.getChatIds()) {
                if (!sendTelegram(chatId, message)) {
                    return false;
                }
            }
        }
        return true;
    }

    @RuleAction(label = "Telegram message", description = "Sends a Telegram via Telegram API")
    public boolean sendTelegramQuery(@ActionInput(name = "chatId") @Nullable Long chatId,
            @ActionInput(name = "message") @Nullable String message,
            @ActionInput(name = "replyId") @Nullable String replyId,
            @ActionInput(name = "buttons") @Nullable String... buttons) {
        return sendTelegramGeneral(chatId, message, replyId, buttons);
    }

    @RuleAction(label = "Telegram message", description = "Sends a Telegram via Telegram API")
    public boolean sendTelegramQuery(@ActionInput(name = "message") @Nullable String message,
            @ActionInput(name = "replyId") @Nullable String replyId,
            @ActionInput(name = "buttons") @Nullable String... buttons) {
        TelegramHandler localHandler = handler;
        if (localHandler != null) {
            for (Long chatId : localHandler.getChatIds()) {
                if (!sendTelegramQuery(chatId, message, replyId, buttons)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean sendTelegramGeneral(@ActionInput(name = "chatId") @Nullable Long chatId, @Nullable String message,
            @Nullable String replyId, @Nullable String... buttons) {
        if (message == null) {
            logger.warn("Message not defined; action skipped.");
            return false;
        }
        if (chatId == null) {
            logger.warn("chatId not defined; action skipped.");
            return false;
        }
        TelegramHandler localHandler = handler;
        if (localHandler != null) {
            SendMessage sendMessage = new SendMessage(chatId, message);
            if (localHandler.getParseMode() != null) {
                sendMessage.parseMode(localHandler.getParseMode());
            }
            if (replyId != null) {
                if (!replyId.contains(" ")) {
                    if (buttons.length > 0) {
                        InlineKeyboardButton[][] keyboard2D = new InlineKeyboardButton[1][];
                        InlineKeyboardButton[] keyboard = new InlineKeyboardButton[buttons.length];
                        keyboard2D[0] = keyboard;
                        for (int i = 0; i < buttons.length; i++) {
                            keyboard[i] = new InlineKeyboardButton(buttons[i]).callbackData(replyId + " " + buttons[i]);
                        }
                        InlineKeyboardMarkup keyBoardMarkup = new InlineKeyboardMarkup(keyboard2D);
                        sendMessage.replyMarkup(keyBoardMarkup);
                    } else {
                        logger.warn(
                                "The replyId {} for message {} is given, but no buttons are defined. ReplyMarkup will be ignored.",
                                replyId, message);
                    }
                } else {
                    logger.warn("replyId {} must not contain spaces. ReplyMarkup will be ignored.", replyId);
                }
            }
            SendResponse retMessage = localHandler.execute(sendMessage);
            if (!evaluateResponse(retMessage)) {
                return false;
            }
            if (replyId != null && retMessage != null) {
                logger.debug("Adding chatId {}, replyId {} and messageId {}", chatId, replyId,
                        retMessage.message().messageId());
                localHandler.addMessageId(chatId, replyId, retMessage.message().messageId());
            }
            return true;
        }
        return false;
    }

    @RuleAction(label = "Telegram message", description = "Sends a Telegram via Telegram API")
    public boolean sendTelegram(@ActionInput(name = "chatId") @Nullable Long chatId,
            @ActionInput(name = "message") @Nullable String format,
            @ActionInput(name = "args") @Nullable Object... args) {
        return sendTelegram(chatId, String.format(format, args));
    }

    @RuleAction(label = "Telegram message", description = "Sends a Telegram via Telegram API")
    public boolean sendTelegram(@ActionInput(name = "message") @Nullable String format,
            @ActionInput(name = "args") @Nullable Object... args) {
        TelegramHandler localHandler = handler;
        if (localHandler != null) {
            for (Long chatId : localHandler.getChatIds()) {
                if (!sendTelegram(chatId, format, args)) {
                    return false;
                }
            }
        }
        return true;
    }

    @RuleAction(label = "Telegram photo", description = "Sends a Picture via Telegram API")
    public boolean sendTelegramPhoto(@ActionInput(name = "chatId") @Nullable Long chatId,
            @ActionInput(name = "photoURL") @Nullable String photoURL,
            @ActionInput(name = "caption") @Nullable String caption) {
        if (photoURL == null) {
            logger.warn("Photo URL not defined; unable to retrieve photo for sending.");
            return false;
        }
        if (chatId == null) {
            logger.warn("chatId not defined; action skipped.");
            return false;
        }

        TelegramHandler localHandler = handler;
        if (localHandler != null) {
            final SendPhoto sendPhoto;

            if (photoURL.toLowerCase().startsWith("http")) {
                // load image from url
                logger.debug("Photo URL provided.");
                sendPhoto = new SendPhoto(chatId, photoURL);
            } else if (photoURL.toLowerCase().startsWith("file")) {
                // Load image from local file system
                logger.debug("Read file from local file system: {}", photoURL);
                try {
                    URL url = new URL(photoURL);
                    sendPhoto = new SendPhoto(chatId, Paths.get(url.getPath()).toFile());
                } catch (MalformedURLException e) {
                    logger.warn("Malformed URL: {}", photoURL);
                    return false;
                }
            } else {
                // Load image from provided base64 image
                logger.debug("Photo base64 provided; converting to binary.");
                try {
                    InputStream is = Base64.getDecoder().wrap(new ByteArrayInputStream(photoURL.getBytes("UTF-8")));
                    try {
                        byte[] photoBytes = IOUtils.toByteArray(is);
                        sendPhoto = new SendPhoto(chatId, photoBytes);
                    } catch (IOException e) {
                        logger.warn("Malformed base64 string: {}", e.getMessage());
                        return false;
                    }
                } catch (UnsupportedEncodingException e) {
                    logger.warn("Cannot parse data fetched from photo URL as an image. Error: {}", e.getMessage());
                    return false;
                }
            }
            sendPhoto.caption(caption);
            if (localHandler.getParseMode() != null) {
                sendPhoto.parseMode(localHandler.getParseMode());
            }
            return evaluateResponse(localHandler.execute(sendPhoto));
        }
        return false;
    }

    @RuleAction(label = "Telegram photo", description = "Sends a Picture via Telegram API")
    public boolean sendTelegramPhoto(@ActionInput(name = "photoURL") @Nullable String photoURL,
            @ActionInput(name = "caption") @Nullable String caption) {
        TelegramHandler localHandler = handler;
        if (localHandler != null) {
            for (Long chatId : localHandler.getChatIds()) {
                if (!sendTelegramPhoto(chatId, photoURL, caption)) {
                    return false;
                }
            }
        }
        return true;
    }

    // legacy delegate methods

    public static boolean sendTelegram(@Nullable ThingActions actions, @Nullable String format,
            @Nullable Object... args) {
        if (actions instanceof TelegramActions) {
            return ((TelegramActions) actions).sendTelegram(format, args);
        } else {
            throw new IllegalArgumentException("Instance is not a TelegramActions class.");
        }
    }

    public static boolean sendTelegramQuery(@Nullable ThingActions actions, @Nullable String message,
            @Nullable String replyId, @Nullable String... buttons) {
        if (actions instanceof TelegramActions) {
            return ((TelegramActions) actions).sendTelegramQuery(message, replyId, buttons);
        } else {
            throw new IllegalArgumentException("Instance is not a TelegramActions class.");
        }
    }

    public static boolean sendTelegramPhoto(@Nullable ThingActions actions, @Nullable String photoURL,
            @Nullable String caption) {
        if (actions instanceof TelegramActions) {
            return ((TelegramActions) actions).sendTelegramPhoto(photoURL, caption);
        } else {
            throw new IllegalArgumentException("Instance is not a TelegramActions class.");
        }
    }

    public static boolean sendTelegramAnswer(@Nullable ThingActions actions, @Nullable String replyId,
            @Nullable String message) {
        if (actions instanceof TelegramActions) {
            return ((TelegramActions) actions).sendTelegramAnswer(replyId, message);
        } else {
            throw new IllegalArgumentException("Instance is not a TelegramActions class.");
        }
    }

    public static boolean sendTelegram(@Nullable ThingActions actions, @Nullable Long chatId, @Nullable String format,
            @Nullable Object... args) {
        if (actions instanceof TelegramActions) {
            return ((TelegramActions) actions).sendTelegram(chatId, format, args);
        } else {
            throw new IllegalArgumentException("Instance is not a TelegramActions class.");
        }
    }

    public static boolean sendTelegramQuery(@Nullable ThingActions actions, @Nullable Long chatId,
            @Nullable String message, @Nullable String replyId, @Nullable String... buttons) {
        if (actions instanceof TelegramActions) {
            return ((TelegramActions) actions).sendTelegramQuery(chatId, message, replyId, buttons);
        } else {
            throw new IllegalArgumentException("Instance is not a TelegramActions class.");
        }
    }

    public static boolean sendTelegramPhoto(@Nullable ThingActions actions, @Nullable Long chatId,
            @Nullable String photoURL, @Nullable String caption) {
        if (actions instanceof TelegramActions) {
            return ((TelegramActions) actions).sendTelegramPhoto(chatId, photoURL, caption);
        } else {
            throw new IllegalArgumentException("Instance is not a TelegramActions class.");
        }
    }

    public static boolean sendTelegramAnswer(@Nullable ThingActions actions, @Nullable Long chatId,
            @Nullable String replyId, @Nullable String message) {
        if (actions instanceof TelegramActions) {
            return ((TelegramActions) actions).sendTelegramAnswer(chatId, replyId, message);
        } else {
            throw new IllegalArgumentException("Instance is not a TelegramActions class.");
        }
    }

    public static boolean sendTelegramAnswer(@Nullable ThingActions actions, @Nullable String chatId,
            @Nullable String replyId, @Nullable String message) {
        if (actions instanceof TelegramActions) {
            return ((TelegramActions) actions).sendTelegramAnswer(Long.valueOf(chatId), replyId, message);
        } else {
            throw new IllegalArgumentException("Instance is not a TelegramActions class.");
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
