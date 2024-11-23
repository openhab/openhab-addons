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
package org.openhab.binding.lifx.internal.fields;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.openhab.binding.lifx.internal.fields.MACAddress.BROADCAST_ADDRESS;
import static org.openhab.core.util.HexUtils.bytesToHex;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.util.HexUtils;

/**
 * Tests {@link MACAddress}.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class MACAddressTest {

    @Test
    public void broadcastAddress() {
        assertThat(BROADCAST_ADDRESS.getAsLabel(), is("000000000000"));
        assertThat(BROADCAST_ADDRESS.getHex(), is("00:00:00:00:00:00"));
        assertThat(bytesToHex(BROADCAST_ADDRESS.getBytes().array()), is("000000000000"));
    }

    @Test
    public void defaultConstructor() {
        MACAddress macAddress = new MACAddress();
        assertThat(macAddress.getAsLabel(), is("000000000000"));
        assertThat(macAddress.getHex(), is("00:00:00:00:00:00"));
    }

    @Test
    public void constructFromByteBuffer() {
        MACAddress macAddress = new MACAddress(ByteBuffer.wrap(HexUtils.hexToBytes("D073D5123456")));
        assertThat(macAddress.getAsLabel(), is("D073D5123456"));
        assertThat(macAddress.getHex(), is("D0:73:D5:12:34:56"));
        assertThat(bytesToHex(macAddress.getBytes().array()), is("D073D5123456"));
    }

    @Test
    public void constructFromString() {
        MACAddress macAddress = new MACAddress("D073D5ABCDEF");
        assertThat(macAddress.getAsLabel(), is("D073D5ABCDEF"));
        assertThat(macAddress.getHex(), is("D0:73:D5:AB:CD:EF"));
        assertThat(bytesToHex(macAddress.getBytes().array()), is("D073D5ABCDEF"));
    }

    @Test
    public void broadcastAddressComparison() {
        assertThat(BROADCAST_ADDRESS, is(BROADCAST_ADDRESS));
        assertThat(BROADCAST_ADDRESS.hashCode(), is(BROADCAST_ADDRESS.hashCode()));

        assertThat(BROADCAST_ADDRESS, is(new MACAddress()));
        assertThat(BROADCAST_ADDRESS.hashCode(), is(new MACAddress().hashCode()));

        assertThat(BROADCAST_ADDRESS, is(not(new MACAddress("D073D5ABCDEF"))));
        assertThat(BROADCAST_ADDRESS, is(not(new MACAddress("D073D5ABCDEF").hashCode())));
    }

    @Test
    public void macAddressComparison() {
        assertThat(new MACAddress("D073D5ABCDEF"), is(new MACAddress("D073D5ABCDEF")));
        assertThat(new MACAddress("D073D5ABCDEF").hashCode(), is(new MACAddress("D073D5ABCDEF").hashCode()));

        assertThat(new MACAddress("D073D5ABCDEF"), is(not(BROADCAST_ADDRESS)));
        assertThat(new MACAddress("D073D5ABCDEF").hashCode(), is(not(BROADCAST_ADDRESS.hashCode())));

        assertThat(new MACAddress("D073D5ABCDEF"), is(not(new MACAddress("D073D5123456"))));
        assertThat(new MACAddress("D073D5ABCDEF").hashCode(), is(not(new MACAddress("D073D5123456").hashCode())));
    }
}
