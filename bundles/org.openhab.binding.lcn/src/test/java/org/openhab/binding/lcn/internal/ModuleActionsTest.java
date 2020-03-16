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
package org.openhab.binding.lcn.internal;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.lcn.internal.common.LcnDefs;
import org.openhab.binding.lcn.internal.common.LcnException;

/**
 * Test class for {@link LcnModuleActions}.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
public class ModuleActionsTest {
    private LcnModuleActions a = new LcnModuleActions();
    private LcnModuleHandler handler = mock(LcnModuleHandler.class);
    @Captor
    private @NonNullByDefault({}) ArgumentCaptor<ByteBuffer> byteBufferCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        a = new LcnModuleActions();
        a.setThingHandler(handler);
    }

    private ByteBuffer stringToByteBuffer(String string) throws UnsupportedEncodingException {
        ByteBuffer bb = ByteBuffer.wrap(string.getBytes(LcnDefs.LCN_ENCODING));
        bb.position(bb.capacity());
        return bb;
    }

    @Test
    public void testSendDynamicText1CharRow1() throws LcnException, UnsupportedEncodingException {
        a.sendDynamicText(1, "a");

        verify(handler).sendPck(stringToByteBuffer("GTDT11a\0\0\0\0\0\0\0\0\0\0\0"));
    }

    @Test
    public void testSendDynamicText1ChunkRow1() throws LcnException, UnsupportedEncodingException {
        a.sendDynamicText(1, "abcdfghijklm");

        verify(handler).sendPck(stringToByteBuffer("GTDT11abcdfghijklm"));
    }

    @Test
    public void testSendDynamicText1Chunk1CharRow1() throws LcnException, UnsupportedEncodingException {
        a.sendDynamicText(1, "abcdfghijklmn");

        verify(handler, times(2)).sendPck(byteBufferCaptor.capture());

        assertThat(byteBufferCaptor.getAllValues(), contains(stringToByteBuffer("GTDT11abcdfghijklm"),
                stringToByteBuffer("GTDT12n\0\0\0\0\0\0\0\0\0\0\0")));
    }

    @Test
    public void testSendDynamicText5ChunksRow1() throws LcnException, UnsupportedEncodingException {
        a.sendDynamicText(1, "abcdfghijklmnopqrstuvwxyzabcdfghijklmnopqrstuvwxyzabcdfghijk");

        verify(handler, times(5)).sendPck(byteBufferCaptor.capture());

        assertThat(byteBufferCaptor.getAllValues(),
                containsInAnyOrder(stringToByteBuffer("GTDT11abcdfghijklm"), stringToByteBuffer("GTDT12nopqrstuvwxy"),
                        stringToByteBuffer("GTDT13zabcdfghijkl"), stringToByteBuffer("GTDT14mnopqrstuvwx"),
                        stringToByteBuffer("GTDT15yzabcdfghijk")));
    }

    @Test
    public void testSendDynamicText5Chunks1CharRow1Truncated() throws LcnException, UnsupportedEncodingException {
        a.sendDynamicText(1, "abcdfghijklmnopqrstuvwxyzabcdfghijklmnopqrstuvwxyzabcdfghijkl");

        verify(handler, times(5)).sendPck(byteBufferCaptor.capture());

        assertThat(byteBufferCaptor.getAllValues(),
                containsInAnyOrder(stringToByteBuffer("GTDT11abcdfghijklm"), stringToByteBuffer("GTDT12nopqrstuvwxy"),
                        stringToByteBuffer("GTDT13zabcdfghijkl"), stringToByteBuffer("GTDT14mnopqrstuvwx"),
                        stringToByteBuffer("GTDT15yzabcdfghijk")));
    }

    @Test
    public void testSendDynamicText5Chunks1UmlautRow1Truncated() throws LcnException, UnsupportedEncodingException {
        a.sendDynamicText(1, "äcdfghijklmnopqrstuvwxyzabcdfghijklmnopqrstuvwxyzabcdfghijkl");

        verify(handler, times(5)).sendPck(byteBufferCaptor.capture());

        assertThat(byteBufferCaptor.getAllValues(),
                containsInAnyOrder(stringToByteBuffer("GTDT11äcdfghijklm"), stringToByteBuffer("GTDT12nopqrstuvwxy"),
                        stringToByteBuffer("GTDT13zabcdfghijkl"), stringToByteBuffer("GTDT14mnopqrstuvwx"),
                        stringToByteBuffer("GTDT15yzabcdfghijk")));
    }

    @Test
    public void testSendDynamicTextRow4() throws LcnException, UnsupportedEncodingException {
        a.sendDynamicText(4, "abcdfghijklmn");

        verify(handler, times(2)).sendPck(byteBufferCaptor.capture());

        assertThat(byteBufferCaptor.getAllValues(), contains(stringToByteBuffer("GTDT41abcdfghijklm"),
                stringToByteBuffer("GTDT42n\0\0\0\0\0\0\0\0\0\0\0")));
    }

    @Test
    public void testSendDynamicTextSplitInCharacter() throws LcnException, UnsupportedEncodingException {
        a.sendDynamicText(4, "Test 123 öäüß");

        verify(handler, times(2)).sendPck(byteBufferCaptor.capture());

        assertThat(byteBufferCaptor.getAllValues(),
                contains(stringToByteBuffer("GTDT41Test 123 ö\303"), stringToByteBuffer("GTDT42\244üß\0\0\0\0\0\0")));
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
}
