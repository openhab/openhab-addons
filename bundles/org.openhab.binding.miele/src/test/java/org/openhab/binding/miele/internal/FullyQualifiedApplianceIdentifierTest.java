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
package org.openhab.binding.miele.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.test.java.JavaTest;

/**
 * This class provides test cases for {@link
 * org.openhab.binding.miele.internal.FullyQualifiedApplianceIdentifier}
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class FullyQualifiedApplianceIdentifierTest extends JavaTest {

    @Test
    public void getUidWhenConstructedFromUidReturnsUid() {
        var identifier = new FullyQualifiedApplianceIdentifier("hdm:ZigBee:0123456789abcdef#210");
        assertEquals("hdm:ZigBee:0123456789abcdef#210", identifier.getUid());
    }

    @Test
    public void getUidWhenConstructedFromApplianceIdAndProtocolReturnsUid() {
        var identifier = new FullyQualifiedApplianceIdentifier("0123456789abcdef#210", "hdm:LAN:");
        assertEquals("hdm:LAN:0123456789abcdef#210", identifier.getUid());
    }

    @Test
    public void getApplianceIdWhenConstructedFromUidReturnsApplianceId() {
        var identifier = new FullyQualifiedApplianceIdentifier("hdm:ZigBee:0123456789abcdef#210");
        assertEquals("0123456789abcdef#210", identifier.getApplianceId());
    }

    @Test
    public void getApplianceIdWhenConstructedFromApplianceIdAndProtocolReturnsApplianceId() {
        var identifier = new FullyQualifiedApplianceIdentifier("0123456789abcdef#210", "hdm:LAN:");
        assertEquals("0123456789abcdef#210", identifier.getApplianceId());
    }

    @Test
    public void getIdWhenConstructedFromUidReturnsProtocol() {
        var identifier = new FullyQualifiedApplianceIdentifier("hdm:ZigBee:0123456789abcdef#210");
        assertEquals("0123456789abcdef_210", identifier.getId());
    }

    @Test
    public void getIdWhenConstructedFromApplianceIdAndProtocolReturnsProtocol() {
        var identifier = new FullyQualifiedApplianceIdentifier("0123456789abcdef#210", "hdm:LAN:");
        assertEquals("0123456789abcdef_210", identifier.getId());
    }

    @Test
    public void getProtocolWhenConstructedFromUidReturnsProtocol() {
        var identifier = new FullyQualifiedApplianceIdentifier("hdm:ZigBee:0123456789abcdef#210");
        assertEquals("hdm:ZigBee:", identifier.getProtocol());
    }

    @Test
    public void getProtocolWhenConstructedFromApplianceIdAndProtocolReturnsProtocol() {
        var identifier = new FullyQualifiedApplianceIdentifier("0123456789abcdef#210", "hdm:LAN:");
        assertEquals("hdm:LAN:", identifier.getProtocol());
    }
}
