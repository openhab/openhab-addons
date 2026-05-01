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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;

/**
 * Unit tests for {@link TelegramMessageStore}.
 *
 * <p>
 * No OSGi runtime or Mockito required. The {@link StorageService}
 * is replaced by a simple anonymous implementation that internally
 * uses {@link HashMap}s. This enables full round-trip coverage
 * without external dependencies.
 */
@NonNullByDefault
class TelegramMessageStoreTest {

    // -----------------------------------------------------------------------
    // Test-Fixtures
    // -----------------------------------------------------------------------

    private static final Long CHAT_ID = 123456789L;
    private static final Long CHAT_ID_2 = 987654321L;
    private static final String REPLY_ID = "myReply";
    private static final String REPLY_ID_2 = "otherReply";
    private static final int MESSAGE_ID = 42;
    private static final String CALLBACK_ID = "abc-callback-xyz";

    /** An in-memory map that simulates the persistent messageId store. */
    private final Map<String, @Nullable String> messageIdBackingMap = new HashMap<>();

    /** An in-memory map that simulates the persistent callbackId store. */
    private final Map<String, @Nullable String> callbackIdBackingMap = new HashMap<>();

    @NonNullByDefault({})
    private TelegramMessageStore store;

    // -----------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        messageIdBackingMap.clear();
        callbackIdBackingMap.clear();
        store = new TelegramMessageStore(createStorageService());
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
        assertEquals(String.valueOf(MESSAGE_ID),
                messageIdBackingMap.get(TelegramMessageStore.buildKey(CHAT_ID, REPLY_ID)));
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
        assertEquals(CALLBACK_ID, callbackIdBackingMap.get(TelegramMessageStore.buildKey(CHAT_ID, REPLY_ID)));
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
    // Cross-Store-Isolation
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
    // Robustness: corrupt value in the backing store
    // -----------------------------------------------------------------------

    @Test
    void getMessageIdReturnsNullForCorruptValue() {
        messageIdBackingMap.put(TelegramMessageStore.buildKey(CHAT_ID, REPLY_ID), "NOT_A_NUMBER");
        assertNull(store.getMessageId(CHAT_ID, REPLY_ID));
    }

    @Test
    void removeMessageIdReturnsNullForCorruptValue() {
        messageIdBackingMap.put(TelegramMessageStore.buildKey(CHAT_ID, REPLY_ID), "NOT_A_NUMBER");
        assertNull(store.removeMessageId(CHAT_ID, REPLY_ID));
    }

    // -----------------------------------------------------------------------
    // Persistency Simulation: new Store, same Backing-Maps
    // -----------------------------------------------------------------------

    @Test
    void messageIdSurvivesStoreReinit() {
        store.putMessageId(CHAT_ID, REPLY_ID, MESSAGE_ID);
        // Neustart: neuer TelegramMessageStore, selbe Maps
        assertEquals(MESSAGE_ID, new TelegramMessageStore(createStorageService()).getMessageId(CHAT_ID, REPLY_ID));
    }

    @Test
    void callbackIdSurvivesStoreReinit() {
        store.putCallbackId(CHAT_ID, REPLY_ID, CALLBACK_ID);
        assertEquals(CALLBACK_ID, new TelegramMessageStore(createStorageService()).getCallbackId(CHAT_ID, REPLY_ID));
    }

    @Test
    void removedEntryIsAbsentAfterStoreReinit() {
        store.putMessageId(CHAT_ID, REPLY_ID, MESSAGE_ID);
        store.removeMessageId(CHAT_ID, REPLY_ID);
        assertNull(new TelegramMessageStore(createStorageService()).getMessageId(CHAT_ID, REPLY_ID));
    }

    // -----------------------------------------------------------------------
    // Helper Methods
    // -----------------------------------------------------------------------

    /**
     * Creates a {@link StorageService}, which uses the two HashMap fields of this
     * test class as backing store, allowing multiple
     * {@link TelegramMessageStore} instances within the same test to access the same data
     * (persistency simulation without real disk).
     */
    private StorageService createStorageService() {
        // Fields as local variables for use in the anonymous class
        Map<String, @Nullable String> msgMap = messageIdBackingMap;
        Map<String, @Nullable String> cbMap = callbackIdBackingMap;

        return new StorageService() {
            @Override
            public <T> Storage<T> getStorage(String name) {
                return getStorage(name, null);
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> Storage<T> getStorage(String name, @Nullable ClassLoader classLoader) {
                Map<String, @Nullable String> backing = TelegramMessageStore.MESSAGE_ID_STORAGE_NAME.equals(name)
                        ? msgMap
                        : cbMap;
                return (Storage<T>) mapBackedStorage(backing);
            }
        };
    }

    /**
     * Creates a {@link Storage}-implementation, which operates directly on the
     * provided {@link Map}.
     */
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
