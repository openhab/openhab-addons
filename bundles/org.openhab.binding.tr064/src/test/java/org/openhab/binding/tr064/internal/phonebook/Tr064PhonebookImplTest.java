/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * The {@link Tr064PhonebookImplTest} class implements test cases for the {@link Tr064PhonebookImpl} class
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@MockitoSettings(strictness = Strictness.WARN)
@ExtendWith(MockitoExtension.class)
public class Tr064PhonebookImplTest {
    @Mock
    private @NonNullByDefault({}) HttpClient httpClient;

    // key -> input, value -> output
    public static Collection<Map.Entry<String, String>> phoneNumbers() {
        return List.of( //
                Map.entry("**820", "**820"), //
                Map.entry("49200123456", "49200123456"), //
                Map.entry("+49-200-123456", "+49200123456"), //
                Map.entry("49 (200) 123456", "49200123456"), //
                Map.entry("+49 200/123456", "+49200123456"));
    }

    @ParameterizedTest
    @MethodSource("phoneNumbers")
    public void testNormalization(Map.Entry<String, String> input) {
        when(httpClient.newRequest((String) any())).thenThrow(new IllegalArgumentException("testing"));
        Tr064PhonebookImpl testPhonebook = new Tr064PhonebookImpl(httpClient, "", 0);
        assertEquals(input.getValue(), testPhonebook.normalizeNumber(input.getKey()));
    }

    @Test
    public void testLookup() {
        when(httpClient.newRequest((String) any())).thenThrow(new IllegalArgumentException("testing"));
        TestPhonebook testPhonebook = new TestPhonebook(httpClient, "", 0);
        testPhonebook.setPhonebook(Map.of("+491238007001", "foo", "+4933998005671", "bar"));

        Optional<String> result = testPhonebook.lookupNumber("01238007001", 0);
        assertEquals(Optional.empty(), result);

        result = testPhonebook.lookupNumber("01238007001", 10);
        assertEquals("foo", result.get());

        result = testPhonebook.lookupNumber("033998005671", -1);
        assertEquals("bar", result.get());
    }

    private static class TestPhonebook extends Tr064PhonebookImpl {
        public TestPhonebook(HttpClient httpClient, String phonebookUrl, int httpTimeout) {
            super(httpClient, phonebookUrl, httpTimeout);
        }

        public void setPhonebook(Map<String, String> phonebook) {
            this.phonebook = phonebook;
        }
    }
}
