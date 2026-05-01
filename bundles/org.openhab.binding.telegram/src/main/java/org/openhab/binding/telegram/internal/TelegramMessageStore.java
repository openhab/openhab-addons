/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 */

package org.openhab.binding.telegram.internal;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persistent store for Telegram message IDs and callback IDs.
 *
 * <p>
 * openHAB's {@link StorageService} backs this class, so all entries survive
 * a restart of openHAB. The two logical maps are:
 * <ul>
 * <li><b>messageId store</b> – maps (chatId, replyId) → Telegram message ID.
 * Required to edit or delete an inline-keyboard message after a restart.</li>
 * <li><b>callbackId store</b> – maps (chatId, replyId) → Telegram callback
 * query ID. Required to answer (acknowledge) a button press after a restart.
 * Note: Telegram callback IDs expire after ~10 min, so entries in this store
 * are automatically removed once they have been used.</li>
 * </ul>
 *
 * <p>
 * Keys are composite strings {@code "<chatId>:<replyId>"} to allow a flat
 * key-value store without nested structures.
 *
 * @author Christoph Pfarrherr - Initial contribution
 */
@NonNullByDefault
public class TelegramMessageStore {

    /** Storage name for the replyId → message-ID mapping. */
    static final String MESSAGE_ID_STORAGE_NAME = "telegram.replyIdToMessageId";

    /** Storage name for the replyId → callback-ID mapping. */
    static final String CALLBACK_ID_STORAGE_NAME = "telegram.replyIdToCallbackId";

    private final Logger logger = LoggerFactory.getLogger(TelegramMessageStore.class);

    // Both maps use String values to avoid Gson round-trip issues with Integer → Double.
    private final Storage<String> messageIdStorage;
    private final Storage<String> callbackIdStorage;

    /**
     * Creates a new store backed by the given {@link StorageService}.
     *
     * @param storageService the openHAB storage service (OSGi injected)
     */
    public TelegramMessageStore(StorageService storageService) {
        this.messageIdStorage = storageService.getStorage(MESSAGE_ID_STORAGE_NAME, String.class.getClassLoader());
        this.callbackIdStorage = storageService.getStorage(CALLBACK_ID_STORAGE_NAME, String.class.getClassLoader());
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Builds the composite storage key.
     *
     * @param chatId Telegram chat ID
     * @param replyId application-level reply identifier (must not contain ':')
     * @return composite key {@code "<chatId>:<replyId>"}
     */
    static String buildKey(Long chatId, String replyId) {
        return chatId + ":" + replyId;
    }

    // -------------------------------------------------------------------------
    // Message-ID operations
    // -------------------------------------------------------------------------

    /**
     * Persists the Telegram {@code messageId} for the given (chatId, replyId) pair.
     *
     * @param chatId Telegram chat ID
     * @param replyId application-level reply identifier
     * @param messageId Telegram message ID returned by the send API
     */
    public void putMessageId(Long chatId, String replyId, Integer messageId) {
        String key = buildKey(chatId, replyId);
        logger.debug("Persisting messageId {} for key '{}'", messageId, key);
        messageIdStorage.put(key, messageId.toString());
    }

    /**
     * Retrieves and <b>removes</b> the stored Telegram message ID for
     * (chatId, replyId). Returns {@code null} if no entry exists.
     *
     * @param chatId Telegram chat ID
     * @param replyId application-level reply identifier
     * @return the Telegram message ID, or {@code null}
     */
    public @Nullable Integer removeMessageId(Long chatId, String replyId) {
        String key = buildKey(chatId, replyId);
        String value = messageIdStorage.remove(key);
        return parseIntOrNull(value, key, "messageId");
    }

    /**
     * Retrieves the stored Telegram message ID for (chatId, replyId) without
     * removing it. Returns {@code null} if no entry exists.
     *
     * @param chatId Telegram chat ID
     * @param replyId application-level reply identifier
     * @return the Telegram message ID, or {@code null}
     */
    public @Nullable Integer getMessageId(Long chatId, String replyId) {
        String value = messageIdStorage.get(buildKey(chatId, replyId));
        return parseIntOrNull(value, buildKey(chatId, replyId), "messageId");
    }

    /**
     * Returns all keys currently stored in the message-ID store.
     * Useful for diagnostics and testing.
     *
     * @return collection of composite keys
     */
    public Collection<String> allMessageIdKeys() {
        return List.copyOf(messageIdStorage.getKeys());
    }

    // -------------------------------------------------------------------------
    // Callback-ID operations
    // -------------------------------------------------------------------------

    /**
     * Persists the Telegram {@code callbackId} for the given (chatId, replyId) pair.
     *
     * @param chatId Telegram chat ID
     * @param replyId application-level reply identifier
     * @param callbackId Telegram callback query ID
     */
    public void putCallbackId(Long chatId, String replyId, String callbackId) {
        String key = buildKey(chatId, replyId);
        logger.debug("Persisting callbackId '{}' for key '{}'", callbackId, key);
        callbackIdStorage.put(key, callbackId);
    }

    /**
     * Retrieves the stored Telegram callback ID for (chatId, replyId).
     * The entry is <b>not</b> removed; call {@link #removeCallbackId} after use.
     *
     * @param chatId Telegram chat ID
     * @param replyId application-level reply identifier
     * @return the Telegram callback ID, or {@code null}
     */
    public @Nullable String getCallbackId(Long chatId, String replyId) {
        return callbackIdStorage.get(buildKey(chatId, replyId));
    }

    /**
     * Removes the stored Telegram callback ID for (chatId, replyId).
     *
     * @param chatId Telegram chat ID
     * @param replyId application-level reply identifier
     */
    public void removeCallbackId(Long chatId, String replyId) {
        String key = buildKey(chatId, replyId);
        logger.debug("Removing callbackId for key '{}'", key);
        callbackIdStorage.remove(key);
    }

    // -------------------------------------------------------------------------
    // Private utilities
    // -------------------------------------------------------------------------

    private @Nullable Integer parseIntOrNull(@Nullable String value, String key, String fieldName) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            logger.warn("Stored {} '{}' for key '{}' is not a valid integer – ignoring entry", fieldName, value, key);
            return null;
        }
    }
}
