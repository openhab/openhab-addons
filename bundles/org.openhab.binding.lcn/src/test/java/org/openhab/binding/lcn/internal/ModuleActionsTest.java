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
package org.openhab.binding.lcn.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnException;

/**
 * Test class for {@link LcnModuleActions}.
 *
 * @author Fabian Wolter - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class ModuleActionsTest {
    private LcnModuleActions a = new LcnModuleActions();
    private final LcnModuleHandler handler = mock(LcnModuleHandler.class);
    @Captor
    private @NonNullByDefault({}) ArgumentCaptor<byte[]> byteBufferCaptor;

    @BeforeEach
    public void setUp() {
        a = new LcnModuleActions();
        a.setThingHandler(handler);
    }

    private byte[] stringToByteBuffer(String string) {
        return string.getBytes(LcnDefs.LCN_ENCODING);
    }

    @Test
    public void testSendDynamicText1CharRow1() throws LcnException {
        a.sendDynamicText(1, "a");

        verify(handler).sendPck(stringToByteBuffer("GTDT11a\0\0\0\0\0\0\0\0\0\0\0"));
    }

    @Test
    public void testSendDynamicText1ChunkRow1() throws LcnException {
        a.sendDynamicText(1, "abcdfghijklm");

        verify(handler, times(2)).sendPck(byteBufferCaptor.capture());

        assertThat(byteBufferCaptor.getAllValues(), contains(stringToByteBuffer("GTDT11abcdfghijklm"),
                stringToByteBuffer("GTDT12 \0\0\0\0\0\0\0\0\0\0\0")));
    }

    @Test
    public void testSendDynamicText1Chunk1CharRow1() throws LcnException {
        a.sendDynamicText(1, "abcdfghijklmn");

        verify(handler, times(2)).sendPck(byteBufferCaptor.capture());

        assertThat(byteBufferCaptor.getAllValues(), contains(stringToByteBuffer("GTDT11abcdfghijklm"),
                stringToByteBuffer("GTDT12n\0\0\0\0\0\0\0\0\0\0\0")));
    }

    @Test
    public void testSendDynamicText5ChunksRow1() throws LcnException {
        a.sendDynamicText(1, "abcdfghijklmnopqrstuvwxyzabcdfghijklmnopqrstuvwxyzabcdfghijk");

        verify(handler, times(5)).sendPck(byteBufferCaptor.capture());

        assertThat(byteBufferCaptor.getAllValues(),
                containsInAnyOrder(stringToByteBuffer("GTDT11abcdfghijklm"), stringToByteBuffer("GTDT12nopqrstuvwxy"),
                        stringToByteBuffer("GTDT13zabcdfghijkl"), stringToByteBuffer("GTDT14mnopqrstuvwx"),
                        stringToByteBuffer("GTDT15yzabcdfghijk")));
    }

    @Test
    public void testSendDynamicText5Chunks1CharRow1Truncated() throws LcnException {
        a.sendDynamicText(1, "abcdfghijklmnopqrstuvwxyzabcdfghijklmnopqrstuvwxyzabcdfghijkl");

        verify(handler, times(5)).sendPck(byteBufferCaptor.capture());

        assertThat(byteBufferCaptor.getAllValues(),
                containsInAnyOrder(stringToByteBuffer("GTDT11abcdfghijklm"), stringToByteBuffer("GTDT12nopqrstuvwxy"),
                        stringToByteBuffer("GTDT13zabcdfghijkl"), stringToByteBuffer("GTDT14mnopqrstuvwx"),
                        stringToByteBuffer("GTDT15yzabcdfghijk")));
    }

    @Test
    public void testSendDynamicText5Chunks1UmlautRow1Truncated() throws LcnException {
        a.sendDynamicText(1, "äcdfghijklmnopqrstuvwxyzabcdfghijklmnopqrstuvwxyzabcdfghijkl");

        verify(handler, times(5)).sendPck(byteBufferCaptor.capture());

        assertThat(byteBufferCaptor.getAllValues(),
                containsInAnyOrder(stringToByteBuffer("GTDT11äcdfghijklm"), stringToByteBuffer("GTDT12nopqrstuvwxy"),
                        stringToByteBuffer("GTDT13zabcdfghijkl"), stringToByteBuffer("GTDT14mnopqrstuvwx"),
                        stringToByteBuffer("GTDT15yzabcdfghijk")));
    }

    @Test
    public void testSendDynamicTextRow4() throws LcnException {
        a.sendDynamicText(4, "abcdfghijklmn");

        verify(handler, times(2)).sendPck(byteBufferCaptor.capture());

        assertThat(byteBufferCaptor.getAllValues(), contains(stringToByteBuffer("GTDT41abcdfghijklm"),
                stringToByteBuffer("GTDT42n\0\0\0\0\0\0\0\0\0\0\0")));
    }

    @Test
    public void testSendDynamicTextSplitInCharacter() throws LcnException {
        a.sendDynamicText(4, "Test 123 öäüß");

        verify(handler, times(2)).sendPck(byteBufferCaptor.capture());

        String string1 = "GTDT41Test 123 ö";
        ByteBuffer chunk1 = ByteBuffer.allocate(stringToByteBuffer(string1).length + 1);
        chunk1.put(stringToByteBuffer(string1));
        chunk1.put((byte) -61); // first byte of ä

        ByteBuffer chunk2 = ByteBuffer.allocate(18);
        chunk2.put(stringToByteBuffer("GTDT42"));
        chunk2.put((byte) -92); // second byte of ä
        chunk2.put(stringToByteBuffer("üß\0\0\0\0\0\0"));

        assertThat(byteBufferCaptor.getAllValues(), contains(chunk1.array(), chunk2.array()));
    }

    @Test
    public void testSendKeysInvalidTable() throws LcnException {
        a.hitKey("E", 3, "MAKE");
        verify(handler, times(0)).sendPck(anyString());
    }

    @Test
    public void testSendKeysNullTable() throws LcnException {
        a.hitKey(null, 3, "MAKE");
        verify(handler, times(0)).sendPck(anyString());
    }

    @Test
    public void testSendKeysNullAction() throws LcnException {
        a.hitKey("D", 3, null);
        verify(handler, times(0)).sendPck(anyString());
    }

    @Test
    public void testSendKeysInvalidKey0() throws LcnException {
        a.hitKey("D", 0, "MAKE");
        verify(handler, times(0)).sendPck(anyString());
    }

    @Test
    public void testSendKeysInvalidKey9() throws LcnException {
        a.hitKey("D", 9, "MAKE");
        verify(handler, times(0)).sendPck(anyString());
    }

    @Test
    public void testSendKeysInvalidAction() throws LcnException {
        a.hitKey("D", 8, "invalid");
        verify(handler, times(0)).sendPck(anyString());
    }

    @Test
    public void testSendKeysA1Hit() throws LcnException {
        a.hitKey("a", 1, "HIT");

        verify(handler).sendPck("TSK--10000000");
    }

    @Test
    public void testSendKeysC8Hit() throws LcnException {
        a.hitKey("C", 8, "break");

        verify(handler).sendPck("TS--O00000001");
    }

    @Test
    public void testSendKeysD3Make() throws LcnException {
        a.hitKey("D", 3, "MAKE");

        verify(handler).sendPck("TS---L00100000");
    }

    @Test
    public void testBeepNull() throws LcnException {
        a.beep(null, null, null);

        verify(handler).sendPck("PIN1");
        verify(handler, times(1)).sendPck(anyString());
    }

    @Test
    public void testBeepSpecial() throws LcnException {
        a.beep(null, "S", 5);

        verify(handler).sendPck("PIS5");
        verify(handler, times(1)).sendPck(anyString());
    }

    @Test
    public void testBeepVolume() throws LcnException {
        a.beep(50d, "3", 5);

        verify(handler).sendPck("PIV050");
        verify(handler).sendPck("PI35");
        verify(handler, times(2)).sendPck(anyString());
    }

    @Test
    public void testBeepInvalidVolume() throws LcnException {
        a.beep(-1d, "3", 5);

        verify(handler, times(0)).sendPck(anyString());
    }

    @Test
    public void testBeepInvalidTonality() throws LcnException {
        a.beep(null, "X", 5);

        verify(handler).sendPck("PIN5");
        verify(handler, times(1)).sendPck(anyString());
    }
}
