/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
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
 * @author Alexander Krasnogolowy - using Telegram library from pengrad
 */
@NonNullByDefault
public class TelegramHandler extends BaseThingHandler {

    @NonNullByDefault
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

    private final List<Long> chatIds = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(TelegramHandler.class);
    private @Nullable ScheduledFuture<?> thingOnlineStatusJob;

    // Keep track of the callback id created by Telegram. This must be sent back in
    // the answerCallbackQuery
    // to stop the progress bar in the Telegram client
    private final Map<ReplyKey, String> replyIdToCallbackId = new HashMap<>();
    // Keep track of message id sent with reply markup because we want to remove the
    // markup after the user provided an
    // answer and need the id of the original message
    private final Map<ReplyKey, Integer> replyIdToMessageId = new HashMap<>();

    private @Nullable TelegramBot bot;
    private @Nullable OkHttpClient botLibClient;
    private @Nullable HttpClient downloadDataClient;
    private @Nullable ParseMode parseMode;

    public TelegramHandler(Thing thing, @Nullable HttpClient httpClient) {
        super(thing);
        downloadDataClient = httpClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands to handle
    }

    @Override
    public void initialize() {
        TelegramConfiguration config = getConfigAs(TelegramConfiguration.class);

        String botToken = config.getBotToken();
        chatIds.clear();
        for (String chatIdStr : config.getChatIds()) {
            try {
                chatIds.add(Long.valueOf(chatIdStr));
            } catch (NumberFormatException e) {
                logger.warn("The chat id {} is not a number and will be ignored", chatIdStr);
            }
        }
        if (config.getParseMode() != null) {
            try {
                parseMode = ParseMode.valueOf(config.getParseMode());
            } catch (IllegalArgumentException e) {
                logger.warn("parseMode is invalid and will be ignored. Only Markdown or HTML are allowed values");
            }
        }

        OkHttpClient.Builder prepareConnection = new OkHttpClient.Builder().connectTimeout(75, TimeUnit.SECONDS)
                .readTimeout(75, TimeUnit.SECONDS);

        String proxyHost = config.getProxyHost();
        Integer proxyPort = config.getProxyPort();
        String proxyType = config.getProxyType();

        if (proxyHost != null && proxyPort != null) {
            InetSocketAddress proxyAddr = new InetSocketAddress(proxyHost, proxyPort);

            Proxy.Type proxyTypeParam = Proxy.Type.SOCKS;

            if ("HTTP".equals(proxyType)) {
                proxyTypeParam = Proxy.Type.HTTP;
            }

            Proxy proxy = new Proxy(proxyTypeParam, proxyAddr);

            logger.debug("{} Proxy {}:{} is used for telegram ", proxyTypeParam, proxyHost, proxyPort);
            prepareConnection.proxy(proxy);
        }

        botLibClient = prepareConnection.build();
        updateStatus(ThingStatus.UNKNOWN);
        delayThingOnlineStatus();
        TelegramBot localBot = bot = new TelegramBot.Builder(botToken).okHttpClient(botLibClient).build();
        localBot.setUpdatesListener(updates -> {
            cancelThingOnlineStatusJob();
            updateStatus(ThingStatus.ONLINE);
            for (Update update : updates) {
                String lastMessageText = null;
                Integer lastMessageDate = null;
                String lastMessageFirstName = null;
                String lastMessageLastName = null;
                String lastMessageUsername = null;
                Long chatId = null;
                String replyId = null;
                if (update.message() != null && update.message().text() != null) {
                    Message message = update.message();
                    chatId = message.chat().id();
                    if (!chatIds.contains(chatId)) {
                        logger.warn(
                                "Ignored message from unknown chat id {}. If you know the sender of that chat, add it to the list of chat ids in the thing configuration to authorize it",
                                chatId);
                        continue; // this is very important regarding security to avoid commands from an unknown
                                  // chat
                    }

                    lastMessageText = message.text();
                    lastMessageDate = message.date();
                    lastMessageFirstName = message.from().firstName();
                    lastMessageLastName = message.from().lastName();
                    lastMessageUsername = message.from().username();
                } else if (update.callbackQuery() != null && update.callbackQuery().message() != null
                        && update.callbackQuery().message().text() != null) {
                    String[] callbackData = update.callbackQuery().data().split(" ", 2);

                    if (callbackData.length == 2) {
                        replyId = callbackData[0];
                        lastMessageText = callbackData[1];
                        lastMessageDate = update.callbackQuery().message().date();
                        lastMessageFirstName = update.callbackQuery().from().firstName();
                        lastMessageLastName = update.callbackQuery().from().lastName();
                        lastMessageUsername = update.callbackQuery().from().username();
                        chatId = update.callbackQuery().message().chat().id();
                        replyIdToCallbackId.put(new ReplyKey(chatId, replyId), update.callbackQuery().id());
                        logger.debug("Received callbackId {} for chatId {} and replyId {}", update.callbackQuery().id(),
                                chatId, replyId);
                    } else {
                        logger.warn(
                                "The received callback query {} has not the right format (must be seperated by spaces)",
                                update.callbackQuery().data());
                    }
                }
                updateChannel(LASTMESSAGETEXT,
                        lastMessageText != null ? new StringType(lastMessageText) : UnDefType.NULL);
                updateChannel(LASTMESSAGEDATE,
                        lastMessageDate != null
                                ? new DateTimeType(ZonedDateTime
                                        .ofInstant(Instant.ofEpochSecond(lastMessageDate.intValue()), ZoneOffset.UTC))
                                : UnDefType.NULL);
                updateChannel(LASTMESSAGENAME, (lastMessageFirstName != null || lastMessageLastName != null)
                        ? new StringType((lastMessageFirstName != null ? lastMessageFirstName + " " : "")
                                + (lastMessageLastName != null ? lastMessageLastName : ""))
                        : UnDefType.NULL);
                updateChannel(LASTMESSAGEUSERNAME,
                        lastMessageUsername != null ? new StringType(lastMessageUsername) : UnDefType.NULL);
                updateChannel(CHATID, chatId != null ? new StringType(chatId.toString()) : UnDefType.NULL);
                updateChannel(REPLYID, replyId != null ? new StringType(replyId) : UnDefType.NULL);
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, exception -> {
            if (exception != null) {
                if (exception.response() != null) {
                    BaseResponse localResponse = exception.response();
                    if (localResponse.errorCode() == 401) { // unauthorized
                        cancelThingOnlineStatusJob();
                        localBot.removeGetUpdatesListener();
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                "Unauthorized attempt to connect to the Telegram server, please check if the bot token is valid");
                        return;
                    }
                }
                if (exception.getCause() != null) { // cause is only non-null in case of an IOException
                    cancelThingOnlineStatusJob();
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, exception.getMessage());
                    delayThingOnlineStatus();
                    return;
                }
                logger.warn("Telegram exception: {}", exception.getMessage());
            }
        });
    }

    private synchronized void delayThingOnlineStatus() {
        thingOnlineStatusJob = scheduler.schedule(() -> {
            // if no error was returned within 10s, we assume the initialization went well
            updateStatus(ThingStatus.ONLINE);
        }, 10, TimeUnit.SECONDS);
    }

    private synchronized void cancelThingOnlineStatusJob() {
        if (thingOnlineStatusJob != null) {
            thingOnlineStatusJob.cancel(true);
            thingOnlineStatusJob = null;
        }
    }

    @Override
    public void dispose() {
        logger.debug("Trying to dispose Telegram client");
        cancelThingOnlineStatusJob();
        OkHttpClient localClient = botLibClient;
        TelegramBot localBot = bot;
        if (localClient != null && localBot != null) {
            localBot.removeGetUpdatesListener();
            localClient.dispatcher().executorService().shutdown();
            localClient.connectionPool().evictAll();
            logger.debug("Telegram client closed");
        }
        super.dispose();
    }

    public void updateChannel(String channelName, State state) {
        updateState(new ChannelUID(getThing().getUID(), channelName), state);
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

    @Nullable
    public HttpClient getClient() {
        return downloadDataClient;
    }

}
