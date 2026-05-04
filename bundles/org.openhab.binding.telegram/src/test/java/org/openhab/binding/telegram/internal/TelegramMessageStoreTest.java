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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * Unit tests for {@link TelegramMessageStore}.
 *
 * <p>
 * No OSGi runtime and no Mockito required. The {@link StorageService} is
 * replaced by an anonymous in-memory implementation backed by {@link HashMap}s,
 * one per storage namespace. This gives full round-trip coverage without any
 * container dependency.
 */
@NonNullByDefault
class TelegramMessageStoreTest {

    // -----------------------------------------------------------------------
    // Fixtures
    // -----------------------------------------------------------------------

    private static final ThingTypeUID THING_TYPE_UID = new ThingTypeUID("telegram", "telegramBot");
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_UID, "bot1");
    private static final ThingUID THING_UID_2 = new ThingUID(THING_TYPE_UID, "bot2");

    private static final Long CHAT_ID = 123456789L;
    private static final Long CHAT_ID_2 = 987654321L;
    private static final String REPLY_ID = "myReply";
    private static final String REPLY_ID_2 = "otherReply";
    private static final int MESSAGE_ID = 42;
    private static final String CALLBACK_ID = "abc-callback-xyz";

    /**
     * In-memory backing: namespace → (key → value).
     * Shared across store instances to simulate a real persistent backend.
     */
    private final Map<String, Map<String, @Nullable String>> namespaceMap = new HashMap<>();

    @NonNullByDefault({})
    private TelegramMessageStore store;

    // -----------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        namespaceMap.clear();
        store = new TelegramMessageStore(createStorageService(), THING_UID);
    }

    // -----------------------------------------------------------------------
    // Storage namespace naming
    // -----------------------------------------------------------------------

    @Test
    void messageIdStorageNameContainsThingUID() {
        String name = TelegramMessageStore.messageIdStorageName(THING_UID);
        assertTrue(name.contains(THING_UID.getAsString()),
                "Storage name must contain the Thing UID to prevent cross-bot collisions");
        assertTrue(name.startsWith(TelegramMessageStore.STORAGE_NAME_PREFIX));
        assertTrue(name.endsWith(TelegramMessageStore.MESSAGE_ID_STORAGE_SUFFIX));
    }

    @Test
    void callbackIdStorageNameContainsThingUID() {
        String name = TelegramMessageStore.callbackIdStorageName(THING_UID);
        assertTrue(name.contains(THING_UID.getAsString()));
        assertTrue(name.startsWith(TelegramMessageStore.STORAGE_NAME_PREFIX));
        assertTrue(name.endsWith(TelegramMessageStore.CALLBACK_ID_STORAGE_SUFFIX));
    }

    @Test
    void storageNamesDifferBetweenThings() {
        assertNotEquals(TelegramMessageStore.messageIdStorageName(THING_UID),
                TelegramMessageStore.messageIdStorageName(THING_UID_2),
                "Two different Things must use different storage namespaces");
        assertNotEquals(TelegramMessageStore.callbackIdStorageName(THING_UID),
                TelegramMessageStore.callbackIdStorageName(THING_UID_2));
    }

    // -----------------------------------------------------------------------
    // Cross-Thing isolation (the critical Copilot finding)
    // -----------------------------------------------------------------------

    @Test
    void messageIdIsolatedBetweenThingsSameChatIdAndReplyId() {
        // Two bots – same chatId, same replyId, but different Things
        TelegramMessageStore store2 = new TelegramMessageStore(createStorageService(), THING_UID_2);

        store.putMessageId(CHAT_ID, REPLY_ID, MESSAGE_ID);
        store2.putMessageId(CHAT_ID, REPLY_ID, 999);

        // Each store must only see its own value
        assertEquals(MESSAGE_ID, store.getMessageId(CHAT_ID, REPLY_ID));
        assertEquals(999, store2.getMessageId(CHAT_ID, REPLY_ID));
    }

    @Test
    void callbackIdIsolatedBetweenThingsSameChatIdAndReplyId() {
        TelegramMessageStore store2 = new TelegramMessageStore(createStorageService(), THING_UID_2);

        store.putCallbackId(CHAT_ID, REPLY_ID, CALLBACK_ID);
        store2.putCallbackId(CHAT_ID, REPLY_ID, "other-callback");

        assertEquals(CALLBACK_ID, store.getCallbackId(CHAT_ID, REPLY_ID));
        assertEquals("other-callback", store2.getCallbackId(CHAT_ID, REPLY_ID));
    }

    @Test
    void removeMessageIdOnOneThingDoesNotAffectOtherThing() {
        TelegramMessageStore store2 = new TelegramMessageStore(createStorageService(), THING_UID_2);

        store.putMessageId(CHAT_ID, REPLY_ID, MESSAGE_ID);
        store2.putMessageId(CHAT_ID, REPLY_ID, 999);

        store.removeMessageId(CHAT_ID, REPLY_ID);

        assertNull(store.getMessageId(CHAT_ID, REPLY_ID));
        assertEquals(999, store2.getMessageId(CHAT_ID, REPLY_ID));
    }

    // -----------------------------------------------------------------------
    // buildKey
    // -----------------------------------------------------------------------

    @Test
    void buildKeyCombinesChatIdAndReplyIdWithColon() {
        assertEquals(CHAT_ID + ":" + REPLY_ID, TelegramMessageStore.buildKey(CHAT_ID, REPLY_ID));
    }

    @Test
    void buildKeyDifferentChatIdProducesDifferentKey() {
        assertNotEquals(TelegramMessageStore.buildKey(CHAT_ID, REPLY_ID),
                TelegramMessageStore.buildKey(CHAT_ID_2, REPLY_ID));
    }

    @Test
    void buildKeyDifferentReplyIdProducesDifferentKey() {
        assertNotEquals(TelegramMessageStore.buildKey(CHAT_ID, REPLY_ID),
                TelegramMessageStore.buildKey(CHAT_ID, REPLY_ID_2));
    }

    // -----------------------------------------------------------------------
    // Message-ID – put / get
    // -----------------------------------------------------------------------

    @Test
    void putMessageIdStoresValueInBackingMap() {
        store.putMessageId(CHAT_ID, REPLY_ID, MESSAGE_ID);
        String namespace = TelegramMessageStore.messageIdStorageName(THING_UID);
        assertEquals(String.valueOf(MESSAGE_ID),
                namespaceMap.getOrDefault(namespace, Map.of()).get(TelegramMessageStore.buildKey(CHAT_ID, REPLY_ID)));
    }

    @Test
    void getMessageIdReturnsStoredValue() {
        store.putMessageId(CHAT_ID, REPLY_ID, MESSAGE_ID);
        assertEquals(MESSAGE_ID, store.getMessageId(CHAT_ID, REPLY_ID));
    }

    @Test
    void getMessageIdReturnsNullWhenAbsent() {
        assertNull(store.getMessageId(CHAT_ID, REPLY_ID));
    }

    @Test
    void getMessageIdDoesNotRemoveEntry() {
        store.putMessageId(CHAT_ID, REPLY_ID, MESSAGE_ID);
        store.getMessageId(CHAT_ID, REPLY_ID);
        assertEquals(MESSAGE_ID, store.getMessageId(CHAT_ID, REPLY_ID));
    }

    @Test
    void putMessageIdOverwritesPreviousValue() {
        store.putMessageId(CHAT_ID, REPLY_ID, MESSAGE_ID);
        store.putMessageId(CHAT_ID, REPLY_ID, 99);
        assertEquals(99, store.getMessageId(CHAT_ID, REPLY_ID));
    }

    @Test
    void putMessageIdIsolatesByChatId() {
        store.putMessageId(CHAT_ID, REPLY_ID, MESSAGE_ID);
        store.putMessageId(CHAT_ID_2, REPLY_ID, 77);
        assertEquals(MESSAGE_ID, store.getMessageId(CHAT_ID, REPLY_ID));
        assertEquals(77, store.getMessageId(CHAT_ID_2, REPLY_ID));
    }

    @Test
    void putMessageIdIsolatesByReplyId() {
        store.putMessageId(CHAT_ID, REPLY_ID, MESSAGE_ID);
        store.putMessageId(CHAT_ID, REPLY_ID_2, 55);
        assertEquals(MESSAGE_ID, store.getMessageId(CHAT_ID, REPLY_ID));
        assertEquals(55, store.getMessageId(CHAT_ID, REPLY_ID_2));
    }

    // -----------------------------------------------------------------------
    // Message-ID – removeMessageId
    // -----------------------------------------------------------------------

    @Test
    void removeMessageIdReturnsValueAndRemovesEntry() {
        store.putMessageId(CHAT_ID, REPLY_ID, MESSAGE_ID);
        assertEquals(MESSAGE_ID, store.removeMessageId(CHAT_ID, REPLY_ID));
        assertNull(store.getMessageId(CHAT_ID, REPLY_ID));
    }

    @Test
    void removeMessageIdReturnsNullWhenAbsent() {
        assertNull(store.removeMessageId(CHAT_ID, REPLY_ID));
    }

    @Test
    void removeMessageIdDoesNotAffectOtherEntries() {
        store.putMessageId(CHAT_ID, REPLY_ID, MESSAGE_ID);
        store.putMessageId(CHAT_ID, REPLY_ID_2, 77);
        store.removeMessageId(CHAT_ID, REPLY_ID);
        assertNull(store.getMessageId(CHAT_ID, REPLY_ID));
        assertEquals(77, store.getMessageId(CHAT_ID, REPLY_ID_2));
    }

    @Test
    void removeMessageIdIdempotentOnSecondCall() {
        store.putMessageId(CHAT_ID, REPLY_ID, MESSAGE_ID);
        store.removeMessageId(CHAT_ID, REPLY_ID);
        assertNull(store.removeMessageId(CHAT_ID, REPLY_ID));
    }

    // -----------------------------------------------------------------------
    // Message-ID – allMessageIdKeys
    // -----------------------------------------------------------------------

    @Test
    void allMessageIdKeysEmptyWhenNothingStored() {
        assertTrue(store.allMessageIdKeys().isEmpty());
    }

    @Test
    void allMessageIdKeysListsAllStoredKeys() {
        store.putMessageId(CHAT_ID, REPLY_ID, MESSAGE_ID);
        store.putMessageId(CHAT_ID_2, REPLY_ID_2, 55);

        Collection<String> keys = store.allMessageIdKeys();
        assertTrue(keys.contains(TelegramMessageStore.buildKey(CHAT_ID, REPLY_ID)));
        assertTrue(keys.contains(TelegramMessageStore.buildKey(CHAT_ID_2, REPLY_ID_2)));
        assertEquals(2, keys.size());
    }

    @Test
    void allMessageIdKeysDoesNotContainRemovedKey() {
        store.putMessageId(CHAT_ID, REPLY_ID, MESSAGE_ID);
        store.removeMessageId(CHAT_ID, REPLY_ID);
        assertFalse(store.allMessageIdKeys().contains(TelegramMessageStore.buildKey(CHAT_ID, REPLY_ID)));
    }

    // -----------------------------------------------------------------------
    // Callback-ID – put / get
    // -----------------------------------------------------------------------

    @Test
    void putCallbackIdStoresValueInBackingMap() {
        store.putCallbackId(CHAT_ID, REPLY_ID, CALLBACK_ID);
        String namespace = TelegramMessageStore.callbackIdStorageName(THING_UID);
        assertEquals(CALLBACK_ID,
                namespaceMap.getOrDefault(namespace, Map.of()).get(TelegramMessageStore.buildKey(CHAT_ID, REPLY_ID)));
    }

    @Test
    void getCallbackIdReturnsStoredValue() {
        store.putCallbackId(CHAT_ID, REPLY_ID, CALLBACK_ID);
        assertEquals(CALLBACK_ID, store.getCallbackId(CHAT_ID, REPLY_ID));
    }

    @Test
    void getCallbackIdReturnsNullWhenAbsent() {
        assertNull(store.getCallbackId(CHAT_ID, REPLY_ID));
    }

    @Test
    void getCallbackIdDoesNotRemoveEntry() {
        store.putCallbackId(CHAT_ID, REPLY_ID, CALLBACK_ID);
        store.getCallbackId(CHAT_ID, REPLY_ID);
        assertEquals(CALLBACK_ID, store.getCallbackId(CHAT_ID, REPLY_ID));
    }

    @Test
    void putCallbackIdOverwritesPreviousValue() {
        store.putCallbackId(CHAT_ID, REPLY_ID, CALLBACK_ID);
        store.putCallbackId(CHAT_ID, REPLY_ID, "new-callback");
        assertEquals("new-callback", store.getCallbackId(CHAT_ID, REPLY_ID));
    }

    @Test
    void putCallbackIdIsolatesByChatId() {
        store.putCallbackId(CHAT_ID, REPLY_ID, CALLBACK_ID);
        store.putCallbackId(CHAT_ID_2, REPLY_ID, "other-callback");
        assertEquals(CALLBACK_ID, store.getCallbackId(CHAT_ID, REPLY_ID));
        assertEquals("other-callback", store.getCallbackId(CHAT_ID_2, REPLY_ID));
    }

    // -----------------------------------------------------------------------
    // Callback-ID – removeCallbackId
    // -----------------------------------------------------------------------

    @Test
    void removeCallbackIdRemovesEntry() {
        store.putCallbackId(CHAT_ID, REPLY_ID, CALLBACK_ID);
        store.removeCallbackId(CHAT_ID, REPLY_ID);
        assertNull(store.getCallbackId(CHAT_ID, REPLY_ID));
    }

    @Test
    void removeCallbackIdDoesNotThrowWhenAbsent() {
        assertDoesNotThrow(() -> store.removeCallbackId(CHAT_ID, REPLY_ID));
    }

    @Test
    void removeCallbackIdDoesNotAffectOtherEntries() {
        store.putCallbackId(CHAT_ID, REPLY_ID, CALLBACK_ID);
        store.putCallbackId(CHAT_ID, REPLY_ID_2, "other");
        store.removeCallbackId(CHAT_ID, REPLY_ID);
        assertEquals("other", store.getCallbackId(CHAT_ID, REPLY_ID_2));
    }

    // -----------------------------------------------------------------------
    // Cross-store isolation (messageId vs callbackId)
    // -----------------------------------------------------------------------

    @Test
    void messageIdAndCallbackIdStoragesAreIndependent() {
        store.putMessageId(CHAT_ID, REPLY_ID, MESSAGE_ID);
        store.putCallbackId(CHAT_ID, REPLY_ID, CALLBACK_ID);

        store.removeMessageId(CHAT_ID, REPLY_ID);
        assertEquals(CALLBACK_ID, store.getCallbackId(CHAT_ID, REPLY_ID));

        store.removeCallbackId(CHAT_ID, REPLY_ID);
        assertNull(store.getMessageId(CHAT_ID, REPLY_ID));
    }

    // -----------------------------------------------------------------------
    // Robustness: corrupt stored value
    // -----------------------------------------------------------------------

    @Test
    void getMessageIdReturnsNullForCorruptValue() {
        Map<String, @Nullable String> map = Objects.requireNonNull(namespaceMap
                .computeIfAbsent(TelegramMessageStore.messageIdStorageName(THING_UID), k -> new HashMap<>()));
        map.put(TelegramMessageStore.buildKey(CHAT_ID, REPLY_ID), "NOT_A_NUMBER");
        assertNull(store.getMessageId(CHAT_ID, REPLY_ID));
    }

    @Test
    void removeMessageIdReturnsNullForCorruptValue() {
        Map<String, @Nullable String> map = Objects.requireNonNull(namespaceMap
                .computeIfAbsent(TelegramMessageStore.messageIdStorageName(THING_UID), k -> new HashMap<>()));
        map.put(TelegramMessageStore.buildKey(CHAT_ID, REPLY_ID), "NOT_A_NUMBER");
        assertNull(store.removeMessageId(CHAT_ID, REPLY_ID));
    }

    // -----------------------------------------------------------------------
    // Persistence simulation: new store instance, same backing maps
    // -----------------------------------------------------------------------

    @Test
    void messageIdSurvivesStoreReinit() {
        store.putMessageId(CHAT_ID, REPLY_ID, MESSAGE_ID);
        TelegramMessageStore reloaded = new TelegramMessageStore(createStorageService(), THING_UID);
        assertEquals(MESSAGE_ID, reloaded.getMessageId(CHAT_ID, REPLY_ID));
    }

    @Test
    void callbackIdSurvivesStoreReinit() {
        store.putCallbackId(CHAT_ID, REPLY_ID, CALLBACK_ID);
        TelegramMessageStore reloaded = new TelegramMessageStore(createStorageService(), THING_UID);
        assertEquals(CALLBACK_ID, reloaded.getCallbackId(CHAT_ID, REPLY_ID));
    }

    @Test
    void removedEntryIsAbsentAfterStoreReinit() {
        store.putMessageId(CHAT_ID, REPLY_ID, MESSAGE_ID);
        store.removeMessageId(CHAT_ID, REPLY_ID);
        TelegramMessageStore reloaded = new TelegramMessageStore(createStorageService(), THING_UID);
        assertNull(reloaded.getMessageId(CHAT_ID, REPLY_ID));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Creates a {@link StorageService} backed by the per-test {@link #namespaceMap}.
     * Multiple {@link TelegramMessageStore} instances in the same test share the
     * same physical maps, simulating persistence across "restarts".
     */
    private StorageService createStorageService() {
        Map<String, Map<String, @Nullable String>> backing = namespaceMap;
        return new StorageService() {
            @Override
            public <T> Storage<T> getStorage(String name) {
                return getStorage(name, null);
            }

            @SuppressWarnings({ "unchecked", "null" })
            @Override
            public <T> Storage<T> getStorage(String name, @Nullable ClassLoader classLoader) {
                Map<String, @Nullable String> ns = Objects
                        .requireNonNull(backing.computeIfAbsent(name, k -> new HashMap<>()));
                return (Storage<T>) mapBackedStorage(ns);
            }
        };
    }

    private static Storage<String> mapBackedStorage(Map<String, @Nullable String> backing) {
        return new Storage<>() {
            @Override
            public @Nullable String put(String key, @Nullable String value) {
                return backing.put(key, value);
            }

            @Override
            public @Nullable String remove(String key) {
                return backing.remove(key);
            }

            @Override
            public boolean containsKey(String key) {
                return backing.containsKey(key);
            }

            @Override
            public @Nullable String get(String key) {
                return backing.get(key);
            }

            @Override
            public Collection<String> getKeys() {
                return backing.keySet();
            }

            @Override
            public Collection<@Nullable String> getValues() {
                return backing.values();
            }
        };
    }
}
