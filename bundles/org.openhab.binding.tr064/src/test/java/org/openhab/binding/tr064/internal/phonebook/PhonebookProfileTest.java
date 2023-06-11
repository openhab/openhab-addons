/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.tr064.internal.phonebook;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.tr064.internal.Tr064BindingConstants.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.UIDUtils;

/**
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PhonebookProfileTest {

    private static final String INTERNAL_PHONE_NUMBER = "999";
    private static final String OTHER_PHONE_NUMBER = "555-456";
    private static final String JOHN_DOES_PHONE_NUMBER = "12345";
    private static final String JOHN_DOES_NAME = "John Doe";
    private static final ThingUID THING_UID = new ThingUID(BINDING_ID, THING_TYPE_FRITZBOX.getId(), "test");
    private static final String MY_PHONEBOOK = UIDUtils.encode(THING_UID.getAsString()) + ":MyPhonebook";

    @NonNullByDefault
    public static class ParameterSet {
        public final State state;
        public final State resultingState;
        public final @Nullable Object matchCount;
        public final @Nullable Object phoneNumberIndex;

        public ParameterSet(State state, State resultingState, @Nullable Object matchCount,
                @Nullable Object phoneNumberIndex) {
            this.state = state;
            this.resultingState = resultingState;
            this.matchCount = matchCount;
            this.phoneNumberIndex = phoneNumberIndex;
        }
    }

    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][] { //
                { new ParameterSet(UnDefType.UNDEF, UnDefType.UNDEF, null, null) }, //
                { new ParameterSet(new StringType(JOHN_DOES_PHONE_NUMBER), new StringType(JOHN_DOES_NAME), null,
                        null) }, //
                { new ParameterSet(new StringType(JOHN_DOES_PHONE_NUMBER), new StringType(JOHN_DOES_NAME),
                        BigDecimal.ONE, null) }, //
                { new ParameterSet(new StringType(JOHN_DOES_PHONE_NUMBER), new StringType(JOHN_DOES_NAME), "3", null) }, //
                { new ParameterSet(new StringListType(JOHN_DOES_PHONE_NUMBER, INTERNAL_PHONE_NUMBER),
                        new StringType(JOHN_DOES_NAME), null, null) }, //
                { new ParameterSet(new StringListType(JOHN_DOES_PHONE_NUMBER, INTERNAL_PHONE_NUMBER),
                        new StringType(JOHN_DOES_NAME), null, BigDecimal.ZERO) }, //
                { new ParameterSet(new StringListType(INTERNAL_PHONE_NUMBER, JOHN_DOES_PHONE_NUMBER),
                        new StringType(JOHN_DOES_NAME), null, BigDecimal.ONE) }, //
                { new ParameterSet(new StringType(OTHER_PHONE_NUMBER), new StringType(OTHER_PHONE_NUMBER), null,
                        null) }, //
                { new ParameterSet(new StringListType(OTHER_PHONE_NUMBER, INTERNAL_PHONE_NUMBER),
                        new StringType(OTHER_PHONE_NUMBER), null, null) }, //
                { new ParameterSet(new StringListType(OTHER_PHONE_NUMBER, INTERNAL_PHONE_NUMBER),
                        new StringType(OTHER_PHONE_NUMBER), null, BigDecimal.ZERO) }, //
                { new ParameterSet(new StringListType(INTERNAL_PHONE_NUMBER, OTHER_PHONE_NUMBER),
                        new StringType(OTHER_PHONE_NUMBER), null, BigDecimal.ONE) }, //
        });
    }

    private @Mock ProfileCallback mockCallback;
    private @Mock ProfileContext mockContext;
    private @Mock PhonebookProvider mockPhonebookProvider;

    @NonNullByDefault
    private final Phonebook phonebook = new Phonebook() {
        @Override
        public Optional<String> lookupNumber(String number, int matchCount) {
            switch (number) {
                case JOHN_DOES_PHONE_NUMBER:
                    return Optional.of(JOHN_DOES_NAME);
                default:
                    return Optional.empty();
            }
        }

        @Override
        public String getName() {
            return MY_PHONEBOOK;
        }
    };

    @BeforeEach
    public void setup() {
        when(mockPhonebookProvider.getPhonebookByName(any(String.class))).thenReturn(Optional.of(phonebook));
        when(mockPhonebookProvider.getPhonebooks()).thenReturn(Set.of(phonebook));
    }

    @ParameterizedTest
    @MethodSource("parameters")
    public void testPhonebookProfileResolvesPhoneNumber(ParameterSet parameterSet) {
        StateProfile profile = initProfile(MY_PHONEBOOK, parameterSet.matchCount, parameterSet.phoneNumberIndex);
        verifySendUpdate(profile, parameterSet.state, parameterSet.resultingState);
    }

    private StateProfile initProfile(Object phonebookName, @Nullable Object matchCount,
            @Nullable Object phoneNumberIndex) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(PhonebookProfile.PHONEBOOK_PARAM, phonebookName);
        if (matchCount != null) {
            properties.put(PhonebookProfile.MATCH_COUNT_PARAM, matchCount);
        }
        if (phoneNumberIndex != null) {
            properties.put(PhonebookProfile.PHONE_NUMBER_INDEX_PARAM, phoneNumberIndex);
        }
        when(mockContext.getConfiguration()).thenReturn(new Configuration(properties));
        return new PhonebookProfile(mockCallback, mockContext, Map.of(THING_UID, mockPhonebookProvider));
    }

    private void verifySendUpdate(StateProfile profile, State state, State expectedState) {
        reset(mockCallback);
        profile.onStateUpdateFromHandler(state);
        verify(mockCallback, times(1)).sendUpdate(eq(expectedState));
    }
}
