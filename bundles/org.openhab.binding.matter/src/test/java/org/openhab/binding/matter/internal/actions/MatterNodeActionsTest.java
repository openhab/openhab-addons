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
package org.openhab.binding.matter.internal.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.matter.internal.MatterBindingConstants;
import org.openhab.binding.matter.internal.client.MatterErrorCode;
import org.openhab.binding.matter.internal.client.MatterRequestException;
import org.openhab.binding.matter.internal.client.dto.PairingCodes;
import org.openhab.binding.matter.internal.controller.MatterControllerClient;
import org.openhab.binding.matter.internal.handler.NodeHandler;
import org.openhab.binding.matter.internal.util.TranslationService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;

/**
 * Tests for {@link MatterNodeActions}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MatterNodeActionsTest {

    private final LocaleProvider localeProvider = mock(LocaleProvider.class);
    // Returns the key followed by its arguments, separated by "|"
    private final TranslationService translationService = mock(TranslationService.class, invocation -> {
        if ("getLocaleProvider".equals(invocation.getMethod().getName())) {
            return localeProvider;
        }
        Object[] args = invocation.getArguments();
        StringBuilder text = new StringBuilder((String) args[0]);
        for (int i = 1; i < args.length; i++) {
            text.append('|').append(args[i]);
        }
        return text.toString();
    });
    private final NodeHandler handler = mock(NodeHandler.class);
    private final MatterControllerClient client = mock(MatterControllerClient.class);
    private final TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
    private final MatterNodeActions actions = new MatterNodeActions(translationService, timeZoneProvider);

    @BeforeEach
    public void setUp() {
        when(localeProvider.getLocale()).thenReturn(Locale.US);
        when(timeZoneProvider.getTimeZone()).thenReturn(ZoneId.of("UTC"));
        when(handler.getClient()).thenReturn(client);
        when(handler.getNodeId()).thenReturn(BigInteger.valueOf(42));
        actions.setThingHandler(handler);
    }

    @Test
    public void pairingCodeIncludesExpiry() {
        PairingCodes codes = new PairingCodes();
        codes.manualPairingCode = "34970112332";
        codes.qrPairingCode = "MT:Y.K9042C00KA0648G00";
        codes.commissioningTimeout = 900;
        when(client.enhancedCommissioningWindow(BigInteger.valueOf(42)))
                .thenReturn(CompletableFuture.completedFuture(codes));

        Map<String, Object> result = actions.generateNewPairingCode();

        assertEquals("34970112332", result.get("manualPairingCode"));
        assertEquals("MT:Y.K9042C00KA0648G00", result.get("qrPairingCode"));
        String validUntil = (String) Objects.requireNonNull(result.get("validUntil"));
        assertTrue(validUntil.startsWith(MatterBindingConstants.THING_ACTION_RESULT_PAIRING_CODE_VALID_UNTIL + "|"));
        assertTrue(validUntil.endsWith("|15"));
    }

    @Test
    public void pairingCodeFailureReturnsTranslatedError() {
        when(client.enhancedCommissioningWindow(BigInteger.valueOf(42))).thenReturn(CompletableFuture
                .failedFuture(new MatterRequestException("busy", MatterErrorCode.FABRIC_OPERATION_IN_PROGRESS, true)));

        Map<String, Object> result = actions.generateNewPairingCode();

        assertEquals(Map.of("result", MatterErrorCode.FABRIC_OPERATION_IN_PROGRESS.getTranslationKey()), result);
    }

    @Test
    public void pairingCodeWithoutClientReturnsNoHandler() {
        when(handler.getClient()).thenReturn(null);

        Map<String, Object> result = actions.generateNewPairingCode();

        assertEquals(Map.of("result", MatterBindingConstants.THING_ACTION_RESULT_NO_HANDLER), result);
    }
}
