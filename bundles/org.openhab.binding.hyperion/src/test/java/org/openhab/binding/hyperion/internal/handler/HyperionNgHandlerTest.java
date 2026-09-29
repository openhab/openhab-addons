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
package org.openhab.binding.hyperion.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link HyperionNgHandler#parseInstanceIndices(String)}.
 *
 * @author Ole Morten Rønning - Initial contribution
 */
@NonNullByDefault
public class HyperionNgHandlerTest {

    @Test
    public void parsesCommaSeparatedIndices() {
        assertEquals(List.of(0, 1), HyperionNgHandler.parseInstanceIndices("0,1"));
    }

    @Test
    public void trimsWhitespaceAroundTokens() {
        assertEquals(List.of(0, 1, 2), HyperionNgHandler.parseInstanceIndices(" 0 , 1 , 2 "));
    }

    @Test
    public void removesDuplicatesPreservingFirstOccurrenceOrder() {
        assertEquals(List.of(2, 0, 1), HyperionNgHandler.parseInstanceIndices("2,0,2,1,0"));
    }

    @Test
    public void ignoresNegativeValues() {
        assertEquals(List.of(0, 1), HyperionNgHandler.parseInstanceIndices("-1,0,1"));
    }

    @Test
    public void ignoresNonNumericTokens() {
        assertEquals(List.of(0, 3), HyperionNgHandler.parseInstanceIndices("0,x,,3"));
    }

    @Test
    public void returnsEmptyListForNullInput() {
        assertEquals(List.of(), HyperionNgHandler.parseInstanceIndices(null));
    }

    @Test
    public void returnsEmptyListForEmptyInput() {
        assertEquals(List.of(), HyperionNgHandler.parseInstanceIndices("   "));
    }
}
