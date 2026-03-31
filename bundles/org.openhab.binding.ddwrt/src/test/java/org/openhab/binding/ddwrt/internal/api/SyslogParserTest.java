/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ddwrt.internal.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ddwrt.internal.api.SyslogParser.SyslogEvent;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link SyslogParser}.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
class SyslogParserTest {

    private @NonNullByDefault({}) SyslogParser parser;

    @BeforeEach
    void setUp() {
        parser = new SyslogParser(LoggerFactory.getLogger(SyslogParserTest.class));
    }

    // ---- Standard syslog format ----

    @Test
    void testParseStandardDhcpAck() {
        String line = "Feb 21 16:21:46 gateway daemon.info dnsmasq-dhcp[25563]: DHCPACK(br0) 192.168.1.100 aa:bb:cc:dd:ee:ff MyPhone";
        SyslogEvent event = parser.parseLine(line, 2026, null);
        assertThat(event, is(notNullValue()));
        assertThat(event.hostname, is(equalTo("gateway")));
        assertThat(event.process, is(equalTo("dnsmasq-dhcp")));
        assertThat(event.pid, is(equalTo(25563)));
        assertThat(event.facility, is(equalTo("daemon")));
        assertThat(event.severity, is(equalTo("info")));
        assertThat(event.message, is(equalTo("DHCPACK(br0) 192.168.1.100 aa:bb:cc:dd:ee:ff MyPhone")));
    }

    @Test
    void testParseStandardAuthEvent() {
        String line = "Feb 21 16:08:56 gateway authpriv.notice dropbear[2663]: Password auth succeeded for 'root' from 10.0.0.5:54321";
        SyslogEvent event = parser.parseLine(line, 2026, null);
        assertThat(event, is(notNullValue()));
        assertThat(event.process, is(equalTo("dropbear")));
        assertThat(event.pid, is(equalTo(2663)));
        assertThat(event.facility, is(equalTo("authpriv")));
        assertThat(event.severity, is(equalTo("notice")));
    }

    @Test
    void testParseStandardWithoutFacilitySeverity() {
        String line = "Mar 15 10:30:00 router crond[1234]: job completed";
        SyslogEvent event = parser.parseLine(line, 2026, null);
        assertThat(event, is(notNullValue()));
        assertThat(event.hostname, is(equalTo("router")));
        assertThat(event.process, is(equalTo("crond")));
        assertThat(event.pid, is(equalTo(1234)));
        assertThat(event.facility, is(nullValue()));
        assertThat(event.severity, is(nullValue()));
    }

    // ---- Kernel messages ----

    @Test
    void testParseKernelMessage() {
        String line = "Mar 10 12:00:00 gateway kernel: some kernel message";
        SyslogEvent event = parser.parseLine(line, 2026, null);
        assertThat(event, is(notNullValue()));
        assertThat(event.process, is(equalTo("kernel")));
        assertThat(event.pid, is(nullValue()));
        assertThat(event.message, is(equalTo("some kernel message")));
    }

    @Test
    void testParseKernelFirewallLog() {
        String line = "Mar 10 12:00:00 gateway kernel: IPTABLES DROP IN=eth0 OUT= SRC=10.0.0.1 DST=192.168.1.1 PROTO=TCP SPT=12345 DPT=80 WINDOW=0";
        SyslogEvent event = parser.parseLine(line, 2026, null);
        assertThat(event, is(notNullValue()));
        assertThat(event.firewall, is(notNullValue()));
        assertThat(event.firewall.sourceIp, is(equalTo("10.0.0.1")));
        assertThat(event.firewall.destIp, is(equalTo("192.168.1.1")));
        assertThat(event.firewall.protocol, is(equalTo("TCP")));
        assertThat(event.firewall.sourcePort, is(equalTo(12345)));
        assertThat(event.firewall.destPort, is(equalTo(80)));
    }

    // ---- Wireless events ----

    @Test
    void testParseWirelessAssociation() {
        String line = "Mar 10 12:00:00 gateway kern.info kernel: wireless: STA aa:bb:cc:dd:ee:ff associated";
        SyslogEvent event = parser.parseLine(line, 2026, null);
        assertThat(event, is(notNullValue()));
        assertThat(event.isWirelessEvent, is(true));
    }

    @Test
    void testNonWirelessEventNotFlagged() {
        String line = "Mar 10 12:00:00 gateway daemon.info dnsmasq[123]: query from 10.0.0.1";
        SyslogEvent event = parser.parseLine(line, 2026, null);
        assertThat(event, is(notNullValue()));
        assertThat(event.isWirelessEvent, is(false));
    }

    // ---- ubus JSON format ----

    @Test
    void testParseUbusJson() {
        String line = "{\"message\":{\"msg\":\"dnsmasq-dhcp[1234]: DHCPACK(br-lan) 192.168.1.50 aa:bb:cc:dd:ee:ff phone\",\"id\":1,\"priority\":30,\"source\":0,\"time\":1740000000000000}}";
        SyslogEvent event = parser.parseLine(line, 2026, null);
        assertThat(event, is(notNullValue()));
        assertThat(event.process, is(equalTo("dnsmasq-dhcp")));
        assertThat(event.pid, is(equalTo(1234)));
        assertThat(event.message, is(equalTo("DHCPACK(br-lan) 192.168.1.50 aa:bb:cc:dd:ee:ff phone")));
        // priority 30 = daemon.info (facility 3 << 3 | severity 6)
        assertThat(event.facility, is(equalTo("daemon")));
        assertThat(event.severity, is(equalTo("info")));
    }

    @Test
    void testParseUbusJsonWithoutPid() {
        String line = "{\"message\":{\"msg\":\"netifd: Interface 'wan' is now up\",\"id\":2,\"priority\":14,\"source\":0,\"time\":1740000000000000}}";
        SyslogEvent event = parser.parseLine(line, 2026, null);
        assertThat(event, is(notNullValue()));
        assertThat(event.process, is(equalTo("netifd")));
        assertThat(event.pid, is(nullValue()));
        assertThat(event.message, is(equalTo("Interface 'wan' is now up")));
    }

    // ---- Device-specific patterns ----

    @Test
    void testDevicePatternTakesPriority() {
        // A custom 7-group pattern that captures year (OpenWrt style)
        Pattern owrtPattern = Pattern.compile(
                "^([A-Za-z]{3}\\s+\\d{1,2}\\s+\\d{2}:\\d{2}:\\d{2})\\s+(\\d{4})\\s+(\\w+)\\.(\\w+)\\s+(\\S+?)(?:\\[(\\d+)\\])?:\\s*(.*)$");
        String line = "Mar 15 10:30:00 2026 daemon.info dnsmasq[100]: test message";
        SyslogEvent event = parser.parseLine(line, 2026, owrtPattern);
        assertThat(event, is(notNullValue()));
        assertThat(event.process, is(equalTo("dnsmasq")));
        assertThat(event.message, is(equalTo("test message")));
    }

    // ---- Edge cases ----

    @Test
    void testEmptyLineReturnsNull() {
        SyslogEvent event = parser.parseLine("", 2026, null);
        assertThat(event, is(nullValue()));
    }

    @Test
    void testUnparseableLineReturnsNull() {
        SyslogEvent event = parser.parseLine("this is not a syslog line", 2026, null);
        assertThat(event, is(nullValue()));
    }

    @Test
    void testAnsiEscapesStripped() {
        String line = "\u001B[32mMar 10 12:00:00 gateway daemon.info test[1]: hello world\u001B[0m";
        SyslogEvent event = parser.parseLine(line, 2026, null);
        assertThat(event, is(notNullValue()));
        assertThat(event.message, is(equalTo("hello world")));
    }

    @Test
    void testSingleDigitDayParsed() {
        String line = "Mar  4 09:15:30 router daemon.info sshd[500]: Accepted publickey for root";
        SyslogEvent event = parser.parseLine(line, 2026, null);
        assertThat(event, is(notNullValue()));
        assertThat(event.timestamp, is(notNullValue()));
        assertThat(event.process, is(equalTo("sshd")));
    }

    // ---- Priority mapping ----

    @Test
    void testUbusPriorityEmergency() {
        // priority 0 = kern.emerg
        String line = "{\"message\":{\"msg\":\"panic[1]: kernel panic\",\"id\":1,\"priority\":0,\"source\":0,\"time\":1740000000000000}}";
        SyslogEvent event = parser.parseLine(line, 2026, null);
        assertThat(event, is(notNullValue()));
        assertThat(event.severity, is(equalTo("emerg")));
        assertThat(event.facility, is(equalTo("kern")));
    }

    @Test
    void testUbusPriorityWarning() {
        // priority 28 = daemon.warning (facility 3 << 3 | severity 4)
        String line = "{\"message\":{\"msg\":\"dnsmasq[1]: warning message\",\"id\":1,\"priority\":28,\"source\":0,\"time\":1740000000000000}}";
        SyslogEvent event = parser.parseLine(line, 2026, null);
        assertThat(event, is(notNullValue()));
        assertThat(event.severity, is(equalTo("warning")));
        assertThat(event.facility, is(equalTo("daemon")));
    }

    @Test
    void testNoFirewallInfoForNormalMessage() {
        String line = "Mar 10 12:00:00 gateway daemon.info dnsmasq[123]: query from 10.0.0.1";
        SyslogEvent event = parser.parseLine(line, 2026, null);
        assertThat(event, is(notNullValue()));
        assertThat(event.firewall, is(nullValue()));
    }

    @Test
    void testProcessWithDotInName() {
        String line = "Mar 10 12:00:00 gateway daemon.info dnsmasq-dhcp.sub[999]: test";
        SyslogEvent event = parser.parseLine(line, 2026, null);
        assertThat(event, is(notNullValue()));
        assertThat(event.process, is(not(equalTo(""))));
    }
}
