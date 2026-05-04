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
 */
package org.openhab.binding.telegram.internal;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persistent store for Telegram message IDs and callback IDs, scoped to a
 * single {@link org.openhab.core.thing.Thing}.
 *
 * <p>
 * openHAB's {@link StorageService} backs this class, so all entries survive
 * a restart of openHAB. Each {@link TelegramHandler} instance owns exactly one
 * {@code TelegramMessageStore}; the storage namespaces include the Thing UID so
 * that multiple Telegram Things (each with its own bot token) can never
 * interfere with each other even if they happen to share the same
 * {@code chatId}/{@code replyId} values.
 *
 * <p>
 * The two logical maps are:
 * <ul>
 * <li><b>messageId store</b> – maps (chatId, replyId) → Telegram message ID.
 * Required to edit or delete an inline-keyboard message after a restart.</li>
 * <li><b>callbackId store</b> – maps (chatId, replyId) → Telegram callback
 * query ID. Required to answer (acknowledge) a button press after a restart.
 * Telegram callback IDs expire after approximately 10 minutes, so entries in
 * this store are removed once they have been used.</li>
 * </ul>
 *
 * <p>
 * Within each store, keys are composite strings {@code "<chatId>:<replyId>"}
 * to allow a flat key-value layout without nested structures.
 *
 * <p>
 * Storage namespace pattern:
 *
 * <pre>
 *   telegram.&lt;thingUID&gt;.replyIdToMessageId
 *   telegram.&lt;thingUID&gt;.replyIdToCallbackId
 * </pre>
 *
 * @author Christoph Pfarrherr - Initial contribution
 */

@NonNullByDefault
public class TelegramMessageStore {

    /** Prefix shared by both storage namespace names. */
    static final String STORAGE_NAME_PREFIX = "telegram.";

    /** Suffix for the replyId → message-ID storage namespace. */
    static final String MESSAGE_ID_STORAGE_SUFFIX = ".replyIdToMessageId";

    /** Suffix for the replyId → callback-ID storage namespace. */
    static final String CALLBACK_ID_STORAGE_SUFFIX = ".replyIdToCallbackId";

    private final Logger logger = LoggerFactory.getLogger(TelegramMessageStore.class);

    // Both maps use String values to avoid Gson round-trip issues with Integer → Double.
    private final Storage<String> messageIdStorage;
    private final Storage<String> callbackIdStorage;

    /**
     * Creates a new store scoped to the given Thing.
     *
     * <p>
     * Using the {@link ThingUID} as part of the storage namespace guarantees
     * that two {@link TelegramHandler} instances with different bot tokens
     * never share storage entries, even when their chat IDs and reply IDs are
     * identical.
     *
     * @param storageService the openHAB storage service (injected by
     *            {@link TelegramHandlerFactory})
     * @param thingUID the UID of the Thing that owns this store; used
     *            to build the storage namespace
     */
    public TelegramMessageStore(StorageService storageService, ThingUID thingUID) {
        String uid = thingUID.getAsString();
        this.messageIdStorage = storageService.getStorage(STORAGE_NAME_PREFIX + uid + MESSAGE_ID_STORAGE_SUFFIX,
                String.class.getClassLoader());
        this.callbackIdStorage = storageService.getStorage(STORAGE_NAME_PREFIX + uid + CALLBACK_ID_STORAGE_SUFFIX,
                String.class.getClassLoader());
    }

    // -------------------------------------------------------------------------
    // Package-visible helpers (used by tests)
    // -------------------------------------------------------------------------

    /**
     * Returns the full message-ID storage namespace for the given Thing UID.
     *
     * @param thingUID the Thing UID
     * @return storage namespace string
     */
    static String messageIdStorageName(ThingUID thingUID) {
        return STORAGE_NAME_PREFIX + thingUID.getAsString() + MESSAGE_ID_STORAGE_SUFFIX;
    }

    /**
     * Returns the full callback-ID storage namespace for the given Thing UID.
     *
     * @param thingUID the Thing UID
     * @return storage namespace string
     */
    static String callbackIdStorageName(ThingUID thingUID) {
        return STORAGE_NAME_PREFIX + thingUID.getAsString() + CALLBACK_ID_STORAGE_SUFFIX;
    }

    /**
     * Builds the composite storage key from a chat ID and a reply ID.
     *
     * @param chatId Telegram chat ID
     * @param replyId application-level reply identifier (must not contain ':')
     * @return composite key {@code "<chatId>:<replyId>"}
     * @throws IllegalArgumentException if {@code replyId} contains ':'
     */
    static String buildKey(Long chatId, String replyId) {
        if (replyId.indexOf(':') >= 0) {
            throw new IllegalArgumentException("replyId must not contain ':': " + replyId);
        }
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
        return parseIntOrNull(messageIdStorage.remove(key), key, "messageId");
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
        String key = buildKey(chatId, replyId);
        return parseIntOrNull(messageIdStorage.get(key), key, "messageId");
    }

    /**
     * Returns all keys currently stored in the message-ID store.
     * Intended for diagnostics and testing.
     *
     * @return unmodifiable collection of composite keys
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
     * The entry is automatically removed after use in {@link TelegramActions#sendTelegramAnswer}.
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
