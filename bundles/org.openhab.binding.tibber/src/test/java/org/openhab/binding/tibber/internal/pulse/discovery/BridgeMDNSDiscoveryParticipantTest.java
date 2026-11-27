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
package org.openhab.binding.tibber.internal.pulse.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.tibber.internal.TibberBindingConstants.TIBBER_THING_TYPE;
import static org.openhab.binding.tibber.internal.TibberPulseBindingConstants.*;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.read.ListAppender;

/**
 * Tests the scenarios of the {@code discovery} spec of the change {@code add-bridge-mdns-discovery} that the
 * participant interface can show: matching, the content of the result, identity, withdrawal, and the absence of
 * connections, name resolution, and sensitive data. What the framework does with a result (inbox entries, scans,
 * ignored state, things that exist) is not under test here. All hosts, labels, and addresses are synthetic.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class BridgeMDNSDiscoveryParticipantTest {

    private static final String INSTANCE = "Tibber Bridge Web Server";
    private static final String HOST = "Bridge01234567.local.";
    private static final ThingUID BRIDGE_UID = new ThingUID("tibber:pulse-bridge:bridge01234567");
    private static final String SECRET = "s3cret-Pw";

    private final BridgeMDNSDiscoveryParticipant participant = new BridgeMDNSDiscoveryParticipant();

    /** An unstubbed array getter of a mock returns null, which the participant must survive. */
    private static ServiceInfo info(@Nullable String name, @Nullable String server, Inet4Address... inet4) {
        ServiceInfo info = mock(ServiceInfo.class);
        when(info.getName()).thenReturn(name);
        when(info.getServer()).thenReturn(server);
        if (inet4.length > 0) {
            when(info.getInet4Addresses()).thenReturn(inet4);
        }
        return info;
    }

    private static ServiceInfo bridge(Inet4Address... inet4) {
        return info(INSTANCE, HOST, inet4);
    }

    private static Inet4Address v4(String dotted) {
        String[] parts = dotted.split("\\.");
        byte[] raw = new byte[4];
        for (int i = 0; i < 4; i++) {
            raw[i] = (byte) Integer.parseInt(parts[i]);
        }
        try {
            return (Inet4Address) InetAddress.getByAddress(raw);
        } catch (UnknownHostException e) {
            throw new AssertionError(e);
        }
    }

    private static Inet6Address v6() {
        byte[] raw = new byte[16];
        raw[0] = 0x20;
        raw[1] = 0x01;
        raw[2] = 0x0d;
        raw[3] = (byte) 0xb8;
        raw[15] = 0x01;
        try {
            return (Inet6Address) InetAddress.getByAddress(raw);
        } catch (UnknownHostException e) {
            throw new AssertionError(e);
        }
    }

    private DiscoveryResult result(ServiceInfo info) {
        DiscoveryResult result = participant.createResult(info);
        assertNotNull(result);
        return result;
    }

    private void assertIgnored(ServiceInfo info) {
        assertNull(participant.createResult(info));
        assertNull(participant.getThingUID(info));
    }

    // Bridge discovery over mDNS

    @Test
    void whenTheParticipantIsAskedForItsTypesThenItNamesTheHttpServiceAndTheBridgeThingType() {
        assertEquals("_http._tcp.local.", participant.getServiceType());
        assertEquals(Set.of(THING_TYPE_PULSE_BRIDGE), participant.getSupportedThingTypeUIDs());
        assertEquals(0, participant.getRemovalGracePeriodSeconds(bridge()));
    }

    // discovery: Discovery offers only Pulse thing types

    @Test
    void whenTheParticipantsSupportedThingTypesAreListedThenItIsExactlyThePulseBridgeOfTheTibberBinding() {
        assertEquals(Set.of(new ThingTypeUID("tibber", "pulse-bridge")), participant.getSupportedThingTypeUIDs());
    }

    @Test
    void whenTheBridgeIsAnnouncedThenNoEntryForTheCloudThingTypeIsCreated() {
        DiscoveryResult result = result(bridge(v4("192.0.2.20")));

        assertNotEquals(TIBBER_THING_TYPE, result.getThingTypeUID());
        assertFalse(participant.getSupportedThingTypeUIDs().contains(TIBBER_THING_TYPE));
        assertTrue(result.getThingUID().getAsString().startsWith("tibber:pulse-bridge:"));
    }

    @Test
    void whenTheBridgeIsAnnouncedThenAResultForABridgeThingIsCreated() {
        DiscoveryResult result = result(bridge(v4("192.0.2.20")));

        assertEquals(THING_TYPE_PULSE_BRIDGE, result.getThingTypeUID());
        assertEquals(BRIDGE_UID, result.getThingUID());
    }

    @Test
    void whenAnotherHttpDeviceIsAnnouncedThenNoResultIsCreated() {
        assertIgnored(info("Office Printer", "printer.local.", v4("192.0.2.30")));
    }

    @Test
    void whenTheInstanceNameIsAnotherOneWithABridgeLikeHostNameThenNoResultIsCreated() {
        assertIgnored(info("Pulse P1 Web Server", HOST, v4("192.0.2.20")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "printer.local.", "Bridge0123456.local.", "Bridge012345678.local.",
            "Bridge0123456g.local.", "Bridge01234567.example.", "Bridge01234567.local..", "xBridge01234567.local.",
            "Bridge01234567.local.x", "Bridge01234567", "Bridge 1234567.local.", "Bridge01234567\u0000.local.",
            "Bridge0123456\u0660.local." })
    void whenTheInstanceNameMatchesButTheHostNameHasAnotherFormThenNoResultIsCreatedAndNothingIsThrown(String host) {
        assertIgnored(info(INSTANCE, host, v4("192.0.2.20")));
    }

    @Test
    void whenTheServiceHasNoHostNameOrNoInstanceNameThenNoResultIsCreatedAndNothingIsThrown() {
        assertIgnored(info(INSTANCE, null, v4("192.0.2.20")));
        assertIgnored(info(INSTANCE, "", v4("192.0.2.20")));
        assertIgnored(info(null, HOST, v4("192.0.2.20")));
        assertIgnored(info("", HOST, v4("192.0.2.20")));
        assertIgnored(info(null, null));
        assertIgnored(mock(ServiceInfo.class));
    }

    @Test
    void whenTheInstanceNameHasAnotherSpellingThenNoResultIsCreated() {
        assertIgnored(info(INSTANCE + " ", HOST));
        assertIgnored(info(" " + INSTANCE, HOST));
        assertIgnored(info("Tibber Bridge Web Server (2)", HOST));
        assertIgnored(info(INSTANCE + "._http._tcp.local.", HOST));
    }

    @Test
    void whenTheCaseDiffersThenOneResultWithTheLowerCaseIdentityIsCreated() {
        ServiceInfo info = info("tibber bridge web server", "BRIDGE0123ABCD.local.", v4("192.0.2.20"));

        DiscoveryResult result = result(info);

        assertEquals(new ThingUID("tibber:pulse-bridge:bridge0123abcd"), result.getThingUID());
        assertEquals("bridge0123abcd", result.getProperties().get(PROPERTY_HOST_LABEL));
        assertEquals(result.getThingUID(), participant.getThingUID(info));
    }

    @Test
    void whenTheHostNameHasNoTrailingDotThenTheResultHasTheSameThingUid() {
        DiscoveryResult result = result(info(INSTANCE, "Bridge01234567.local", v4("192.0.2.20")));

        assertEquals(BRIDGE_UID, result.getThingUID());
    }

    @Test
    void whenTxtRecordsAndAPortAreSetThenTheyAreNeitherReadNorPartOfTheResult() {
        ServiceInfo info = bridge(v4("192.0.2.20"));
        when(info.getPort()).thenReturn(8080);
        when(info.getPropertyString(any())).thenReturn(SECRET);
        when(info.getPropertyNames()).thenReturn(java.util.Collections.enumeration(List.of("password", "txtvers")));
        when(info.getTextBytes()).thenReturn(SECRET.getBytes(StandardCharsets.UTF_8));

        DiscoveryResult result = result(info);

        assertEquals(Map.of(CONFIG_HOSTNAME, "192.0.2.20", PROPERTY_HOST_LABEL, "bridge01234567"),
                result.getProperties());
        assertFalse(result.toString().contains(SECRET));
        assertFalse(result.toString().contains("8080"));
        verify(info, never()).getPort();
        verify(info, never()).getPropertyString(any());
        verify(info, never()).getPropertyNames();
        verify(info, never()).getPropertyBytes(any());
        verify(info, never()).getTextBytes();
    }

    // Discovery result of a bridge

    @Test
    void whenTheResultIsCreatedThenItsContentIsExactlyAsSpecified() {
        DiscoveryResult result = result(bridge(v4("192.0.2.20")));

        assertEquals(THING_TYPE_PULSE_BRIDGE, result.getThingTypeUID());
        assertEquals(BRIDGE_UID, result.getThingUID());
        assertEquals("Tibber Pulse Bridge", result.getLabel());
        assertEquals(Map.of("hostname", "192.0.2.20", "hostLabel", "bridge01234567"), result.getProperties());
        assertEquals("hostLabel", result.getRepresentationProperty());
        assertFalse(result.getProperties().containsKey("password"));
    }

    @Test
    void whenTheRecordListsSeveralAddressesThenTheFirstUsableIpv4AddressIsOffered() {
        DiscoveryResult result = result(bridge(v4("0.0.0.0"), v4("192.0.2.20"), v4("192.0.2.21")));

        assertEquals("192.0.2.20", result.getProperties().get(CONFIG_HOSTNAME));
    }

    @Test
    void whenTheAddressArrayHasNullEntriesAndMulticastAddressesThenTheyAreSkipped() {
        Inet4Address[] addresses = new Inet4Address[4];
        addresses[1] = v4("224.0.0.251");
        addresses[2] = v4("0.0.0.0");
        addresses[3] = v4("192.0.2.21");

        DiscoveryResult result = result(bridge(addresses));

        assertEquals("192.0.2.21", result.getProperties().get(CONFIG_HOSTNAME));
    }

    @Test
    void whenAnAddressIsLoopbackOrLinkLocalThenItIsStillOffered() {
        // Only the any-local and the multicast addresses are skipped by the specification.
        DiscoveryResult result = result(bridge(v4("169.254.1.2")));

        assertEquals("169.254.1.2", result.getProperties().get(CONFIG_HOSTNAME));
    }

    @Test
    void whenTheRecordHasOnlyAnIpv6AddressThenTheHostNameWithoutTheTrailingDotIsOffered() {
        ServiceInfo info = bridge();
        when(info.getInet4Addresses()).thenReturn(new Inet4Address[0]);
        when(info.getInet6Addresses()).thenReturn(new Inet6Address[] { v6() });

        DiscoveryResult result = result(info);

        assertEquals("Bridge01234567.local", result.getProperties().get(CONFIG_HOSTNAME));
        assertEquals(BRIDGE_UID, result.getThingUID());
        assertEquals(result(bridge(v4("192.0.2.20"))).getThingUID(), result.getThingUID());
        assertTrue(result.getProperties().values().stream().noneMatch(v -> String.valueOf(v).contains(":")));
        verify(info, never()).getInet6Addresses();
    }

    @Test
    void whenTheRecordHasNoAddressThenTheHostNameWithoutTheTrailingDotIsOffered() {
        ServiceInfo withNullArray = bridge();
        ServiceInfo withEmptyArray = bridge();
        when(withEmptyArray.getInet4Addresses()).thenReturn(new Inet4Address[0]);
        ServiceInfo onlyAnyLocal = bridge(v4("0.0.0.0"));

        for (ServiceInfo info : List.of(withNullArray, withEmptyArray, onlyAnyLocal)) {
            assertEquals("Bridge01234567.local", result(info).getProperties().get(CONFIG_HOSTNAME));
        }
    }

    @Test
    void whenTheRecordHasNoAddressThenTheHostNameIsOfferedInTheFormAdvertised() {
        DiscoveryResult result = result(info("TIBBER BRIDGE WEB SERVER", "BRIDGE0123ABCD.LOCAL."));

        assertEquals("BRIDGE0123ABCD.LOCAL", result.getProperties().get(CONFIG_HOSTNAME));
        assertEquals("bridge0123abcd", result.getProperties().get(PROPERTY_HOST_LABEL));
    }

    // Stable identity across address changes, duplicate announcements

    @Test
    void whenTheAddressChangesThenTheThingUidStaysAndTheHostnameFollows() {
        DiscoveryResult before = result(bridge(v4("192.0.2.20")));

        DiscoveryResult after = result(bridge(v4("192.0.2.21")));

        assertEquals(before.getThingUID(), after.getThingUID());
        assertEquals("192.0.2.20", before.getProperties().get(CONFIG_HOSTNAME));
        assertEquals("192.0.2.21", after.getProperties().get(CONFIG_HOSTNAME));
        assertEquals(before.getProperties().get(PROPERTY_HOST_LABEL), after.getProperties().get(PROPERTY_HOST_LABEL));
    }

    @Test
    void whenALaterAnnouncementHasAnotherCaseThenThingUidAndHostLabelAreTheSame() {
        DiscoveryResult first = result(info(INSTANCE, "Bridge0123ABCD.local.", v4("192.0.2.20")));

        DiscoveryResult later = result(info(INSTANCE, "bridge0123abcd.local.", v4("192.0.2.20")));

        assertEquals(first.getThingUID(), later.getThingUID());
        assertEquals(first.getProperties().get(PROPERTY_HOST_LABEL), later.getProperties().get(PROPERTY_HOST_LABEL));
    }

    @Test
    void whenTwoBridgesAreAnnouncedThenTheirThingUidsDiffer() {
        DiscoveryResult first = result(bridge(v4("192.0.2.20")));
        DiscoveryResult second = result(info(INSTANCE, "Bridge89abcdef.local.", v4("192.0.2.21")));

        assertNotEquals(first.getThingUID(), second.getThingUID());
        assertEquals(new ThingUID("tibber:pulse-bridge:bridge89abcdef"), second.getThingUID());
        assertEquals("Tibber Pulse Bridge", second.getLabel());
    }

    @Test
    void whenTheSameServiceIsReportedOneHundredTimesThenEveryResultHasTheSameThingUid() {
        ServiceInfo info = bridge(v4("192.0.2.20"));
        Set<ThingUID> uids = new java.util.HashSet<>();

        for (int i = 0; i < 100; i++) {
            uids.add(result(info).getThingUID());
        }

        assertEquals(Set.of(BRIDGE_UID), uids);
    }

    @Test
    void whenTheBridgeIsAnnouncedOverIpv4AndOverIpv6ThenBothResultsHaveTheSameThingUidAndHostLabel() {
        ServiceInfo ipv4 = bridge(v4("192.0.2.20"));
        ServiceInfo ipv6 = bridge();
        when(ipv6.getInet6Addresses()).thenReturn(new Inet6Address[] { v6() });

        DiscoveryResult first = result(ipv4);
        DiscoveryResult second = result(ipv6);

        assertEquals(first.getThingUID(), second.getThingUID());
        assertEquals(first.getProperties().get(PROPERTY_HOST_LABEL), second.getProperties().get(PROPERTY_HOST_LABEL));
    }

    static Stream<Arguments> inputs() {
        return Stream.of(Arguments.of(INSTANCE, HOST), Arguments.of(INSTANCE, "Bridge01234567.local"),
                Arguments.of("tibber bridge web server", "BRIDGE01234567.LOCAL."),
                Arguments.of("Office Printer", "printer.local."), Arguments.of("Pulse P1 Web Server", HOST),
                Arguments.of(INSTANCE, "printer.local."), Arguments.of(INSTANCE, ""),
                Arguments.of(INSTANCE, INSTANCE + "._http._tcp.local."), Arguments.of("", ""),
                Arguments.of(INSTANCE, "Bridge0123456g.local."), Arguments.of(INSTANCE, "Bridge012345678.local."),
                Arguments.of(INSTANCE, "x".repeat(100_000)), Arguments.of("x".repeat(100_000), HOST));
    }

    @ParameterizedTest
    @MethodSource("inputs")
    void whenAnyInputIsGivenThenResultAndRemovalAgreeAndNothingIsThrown(String name, String server) {
        ServiceInfo info = info(name, server, v4("192.0.2.20"));

        DiscoveryResult result = participant.createResult(info);
        ThingUID uid = participant.getThingUID(info);

        assertEquals(result == null, uid == null);
        if (result != null) {
            assertNotNull(uid);
            assertEquals(result.getThingUID(), uid);
            assertEquals(result.getProperties().get(PROPERTY_HOST_LABEL), uid.getId());
        }
    }

    // Bridge already configured

    @Test
    void whenTheParticipantIsBuiltThenItNeedsNoRegistryAndKeepsNoState() {
        assertEquals(1, BridgeMDNSDiscoveryParticipant.class.getDeclaredConstructors().length);
        assertEquals(0, BridgeMDNSDiscoveryParticipant.class.getDeclaredConstructors()[0].getParameterCount());
        for (java.lang.reflect.Field field : BridgeMDNSDiscoveryParticipant.class.getDeclaredFields()) {
            if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                // the only instance field is the logger
                assertEquals(org.slf4j.Logger.class, field.getType(), field.getName());
            }
        }
        for (java.lang.reflect.Method method : BridgeMDNSDiscoveryParticipant.class.getDeclaredMethods()) {
            for (Class<?> parameter : method.getParameterTypes()) {
                assertFalse(parameter.getName().contains("ThingRegistry"), method.getName());
            }
        }
    }

    // Withdrawal of the service

    @Test
    void whenTheServiceExpiresWithHostNameAndNoAddressThenTheThingUidOfTheBridgeIsNamed() {
        assertEquals(BRIDGE_UID, participant.getThingUID(info(INSTANCE, HOST)));
        assertEquals(BRIDGE_UID, participant.getThingUID(info(INSTANCE, "BRIDGE01234567.local")));
    }

    @Test
    void whenTheRemovalRecordHasNoUsableHostNameThenNoThingUidIsNamedAndNothingIsThrown() {
        assertNull(participant.getThingUID(info(INSTANCE, "")));
        assertNull(participant.getThingUID(info(INSTANCE, null)));
        assertNull(participant.getThingUID(info(INSTANCE, INSTANCE + "._http._tcp.local.")));
        assertNull(participant.getThingUID(mock(ServiceInfo.class)));
    }

    @Test
    void whenAForeignServiceIsWithdrawnThenNoThingUidIsNamed() {
        assertNull(participant.getThingUID(info("Office Printer", "printer.local.")));
        assertNull(participant.getThingUID(info("Office Printer", HOST)));
    }

    // Bridge discovery opens no connection

    @Test
    void whenTheRecordIsReadThenOnlyNameServerAndIpv4AddressesAreAsked() {
        ServiceInfo info = bridge(v4("192.0.2.20"));

        result(info);
        participant.getThingUID(info);

        verify(info, atLeastOnce()).getName();
        verify(info, atLeastOnce()).getServer();
        verify(info, atLeastOnce()).getInet4Addresses();
        verifyNoMoreInteractions(info);
    }

    // QA probes: hostile and malformed records (task 4.2 to 4.5 of the change)

    @ParameterizedTest
    @ValueSource(strings = { "T\u0131bber Br\u0131dge Web Server", "Tibber Bridge Web \u017Ferver",
            "T\u0130bber Br\u0130dge Web Server", "Tibber Bridge Web Server\u0000", "Tibber\u00A0Bridge Web Server",
            "Tibber Bridge Web Server\n", "Tibber  Bridge Web Server", "\uFF34ibber Bridge Web Server" })
    void whenTheInstanceNameIsALookAlikeOrHasControlCharactersThenNoResultIsCreated(String name) {
        // String.equalsIgnoreCase folds the dotless i, the dotted capital I, and the long s onto ASCII letters.
        assertIgnored(info(name, HOST, v4("192.0.2.20")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "Br\u0131dge01234567.local.", "BR\u0130DGE01234567.local.", "Bridge01234567.local.\n",
            "Bridge01234567.local. ", " Bridge01234567.local.", "Bridge01234567.l\u00F6cal.",
            "Bridge01234567.LOCAL\u0000", "Bridge\uFF10\uFF11234567.local.", "Bridge0123456\u212A.local.",
            "\uFEFFBridge01234567.local." })
    void whenTheHostNameHasUnusualUnicodeOrControlCharactersThenNoResultIsCreated(String host) {
        assertIgnored(info(INSTANCE, host, v4("192.0.2.20")));
    }

    @Test
    void whenHundredsOfAddressesAreListedThenTheFirstUsableOneIsOfferedWithoutDelay() {
        Inet4Address[] addresses = new Inet4Address[600];
        for (int i = 0; i < addresses.length; i++) {
            addresses[i] = (i % 3 == 0) ? v4("0.0.0.0") : (i % 3 == 1) ? v4("224.0.0.251") : null;
        }
        addresses[addresses.length - 2] = v4("192.0.2.77");
        addresses[addresses.length - 1] = v4("192.0.2.78");
        ServiceInfo withUsable = bridge(addresses);
        Inet4Address[] unusable = new Inet4Address[600];
        java.util.Arrays.fill(unusable, v4("224.0.0.251"));
        ServiceInfo withoutUsable = bridge(unusable);

        assertEquals("192.0.2.77", result(withUsable).getProperties().get(CONFIG_HOSTNAME));
        assertEquals("Bridge01234567.local", result(withoutUsable).getProperties().get(CONFIG_HOSTNAME));
    }

    @Test
    void whenTheHostNameIsVeryLongOrTheNameHasMegabytesThenNothingIsAllocatedOrThrown() {
        assertIgnored(info(INSTANCE, "Bridge" + "0".repeat(2_000_000) + ".local.", v4("192.0.2.20")));
        assertIgnored(info("Tibber Bridge Web Server".repeat(100_000), HOST, v4("192.0.2.20")));
        assertIgnored(info(INSTANCE, "bridge01234567" + ".local".repeat(100_000), v4("192.0.2.20")));
    }

    @Test
    void whenManyRandomLabelsInAnyCaseAreAnnouncedThenEveryUidIsValidAndResultAndRemovalAgree() {
        Random random = new Random(20261119L);
        String hex = "0123456789abcdefABCDEF";
        for (int i = 0; i < 5_000; i++) {
            StringBuilder digits = new StringBuilder();
            for (int j = 0; j < 8; j++) {
                digits.append(hex.charAt(random.nextInt(hex.length())));
            }
            String prefix = random.nextBoolean() ? "Bridge" : random.nextBoolean() ? "BRIDGE" : "bridge";
            String suffix = random.nextBoolean() ? ".local." : random.nextBoolean() ? ".LOCAL" : ".Local.";
            ServiceInfo info = info(random.nextBoolean() ? INSTANCE : INSTANCE.toUpperCase(Locale.ROOT),
                    prefix + digits + suffix, v4("192.0.2.20"));

            DiscoveryResult result = result(info);
            ThingUID uid = participant.getThingUID(info);

            assertNotNull(uid);
            assertEquals(uid, result.getThingUID());
            String label = "bridge" + digits.toString().toLowerCase(Locale.ROOT);
            assertEquals("tibber:pulse-bridge:" + label, uid.getAsString());
            assertEquals(uid, new ThingUID(uid.getAsString()));
            assertTrue(uid.getId().matches("bridge[0-9a-f]{8}"));
            assertEquals(label, result.getProperties().get(PROPERTY_HOST_LABEL));
        }
    }

    @Test
    void whenTheParticipantIsUsedFromManyThreadsThenEveryResultIsCorrect() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<Boolean>> futures = new ArrayList<>();
            for (int t = 0; t < 8; t++) {
                String host = String.format("Bridge%08x.local.", t);
                futures.add(pool.submit(() -> {
                    for (int i = 0; i < 2_000; i++) {
                        ServiceInfo info = info(INSTANCE, host, v4("192.0.2.20"));
                        DiscoveryResult result = participant.createResult(info);
                        ThingUID uid = participant.getThingUID(info);
                        if (result == null || uid == null || !uid.equals(result.getThingUID()) || !uid.getId().equals(
                                host.substring(0, host.length() - ".local.".length()).toLowerCase(Locale.ROOT))) {
                            return false;
                        }
                    }
                    return true;
                }));
            }
            for (Future<Boolean> future : futures) {
                assertTrue(future.get(20, TimeUnit.SECONDS));
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void whenAnAddressIsOfferedThenItIsNeverAskedForItsHostName() {
        // getHostName and getCanonicalHostName of an address without a name start a reverse lookup.
        Inet4Address address = mock(Inet4Address.class);
        when(address.getHostAddress()).thenReturn("192.0.2.20");

        DiscoveryResult result = result(bridge(address));

        assertEquals("192.0.2.20", result.getProperties().get(CONFIG_HOSTNAME));
        verify(address, never()).getHostName();
        verify(address, never()).getCanonicalHostName();
    }

    @Test
    void whenTheCompiledClassIsInspectedThenItCallsNothingOutsideTheParseOfTheRecordAndFoldsCaseWithoutTheDefaultLocale()
            throws Exception {
        // A stronger form of the inspection above: every class the participant (and its record) refers to, and every
        // method it calls on the java.net types, must be on a short list. A call into a resolver, a socket, a file, a
        // thread, or an HTTP client would add a class or a method name that is not on it.
        Set<String> allowedClasses = Set.of("java/lang/Object", "java/lang/String", "java/lang/Record",
                "java/lang/invoke/StringConcatFactory", "java/lang/invoke/MethodHandles",
                "java/lang/invoke/MethodHandles$Lookup", "java/lang/runtime/ObjectMethods", "java/util/Locale",
                "java/util/Map", "java/util/Set", "java/util/regex/Matcher", "java/util/regex/Pattern",
                "java/net/Inet4Address", "[Ljava/net/Inet4Address;", "javax/jmdns/ServiceInfo", "org/slf4j/Logger",
                "org/slf4j/LoggerFactory", "org/eclipse/jdt/annotation/NonNullByDefault",
                "org/openhab/core/config/discovery/DiscoveryResult",
                "org/openhab/core/config/discovery/DiscoveryResultBuilder",
                "org/openhab/core/config/discovery/mdns/MDNSDiscoveryParticipant",
                "org/openhab/core/thing/ThingTypeUID", "org/openhab/core/thing/ThingUID",
                "org/openhab/binding/tibber/internal/TibberPulseBindingConstants",
                BridgeMDNSDiscoveryParticipant.class.getName().replace('.', '/'),
                BridgeMDNSDiscoveryParticipant.class.getName().replace('.', '/') + "$Bridge");
        Set<String> allowedNetCalls = Set.of("java/net/Inet4Address.isAnyLocalAddress()Z",
                "java/net/Inet4Address.isMulticastAddress()Z",
                "java/net/Inet4Address.getHostAddress()Ljava/lang/String;");
        for (String className : List.of(BridgeMDNSDiscoveryParticipant.class.getName(),
                BridgeMDNSDiscoveryParticipant.class.getName() + "$Bridge")) {
            Set<String> classes = new TreeSet<>();
            Set<String> calls = new TreeSet<>();
            readConstantPool(className, classes, calls);

            for (String referred : classes) {
                assertTrue(allowedClasses.contains(referred), "unexpected class in " + className + ": " + referred);
            }
            // Case folding must name its locale: the no-argument forms use the default locale (Turkish i).
            assertFalse(calls.contains("java/lang/String.toLowerCase()Ljava/lang/String;"), className);
            assertFalse(calls.contains("java/lang/String.toUpperCase()Ljava/lang/String;"), className);
            for (String call : calls) {
                if (call.startsWith("java/net/")) {
                    assertTrue(allowedNetCalls.contains(call), "unexpected call in " + className + ": " + call);
                }
            }
        }
    }

    /** Reads the class names and the method references (owner.name) from the constant pool of a compiled class. */
    private static void readConstantPool(String className, Set<String> classes, Set<String> calls) throws IOException {
        String resource = "/" + className.replace('.', '/') + ".class";
        try (InputStream in = BridgeMDNSDiscoveryParticipant.class.getResourceAsStream(resource)) {
            assertNotNull(in);
            DataInputStream data = new DataInputStream(new ByteArrayInputStream(in.readAllBytes()));
            data.readInt();
            data.readUnsignedShort();
            data.readUnsignedShort();
            int count = data.readUnsignedShort();
            String[] utf8 = new String[count];
            int[] classIndex = new int[count];
            int[] refOwner = new int[count];
            int[] refNameAndType = new int[count];
            int[] natName = new int[count];
            int[] natDescriptor = new int[count];
            for (int i = 1; i < count; i++) {
                int tag = data.readUnsignedByte();
                switch (tag) {
                    case 1 -> utf8[i] = data.readUTF();
                    case 3, 4 -> data.readInt();
                    case 5, 6 -> {
                        data.readLong();
                        i++;
                    }
                    case 7 -> classIndex[i] = data.readUnsignedShort();
                    case 8, 16, 19, 20 -> data.readUnsignedShort();
                    case 9, 10, 11 -> {
                        refOwner[i] = data.readUnsignedShort();
                        refNameAndType[i] = data.readUnsignedShort();
                    }
                    case 12 -> {
                        natName[i] = data.readUnsignedShort();
                        natDescriptor[i] = data.readUnsignedShort();
                    }
                    case 15 -> {
                        data.readUnsignedByte();
                        data.readUnsignedShort();
                    }
                    case 17, 18 -> data.readInt();
                    default -> throw new AssertionError("constant pool tag " + tag);
                }
            }
            for (int i = 1; i < count; i++) {
                if (classIndex[i] != 0) {
                    classes.add(utf8[classIndex[i]]);
                }
                if (refOwner[i] != 0) {
                    calls.add(utf8[classIndex[refOwner[i]]] + "." + utf8[natName[refNameAndType[i]]]
                            + utf8[natDescriptor[refNameAndType[i]]]);
                }
            }
        }
    }

    // Discovery exposes no sensitive data

    @Test
    void whenTheResultIsCreatedThenNoPropertyLabelOrThingUidContainsAPasswordOrATxtValue() {
        ServiceInfo info = bridge(v4("192.0.2.20"));
        when(info.getPropertyString(any())).thenReturn(SECRET);

        DiscoveryResult result = result(info);

        String all = result + result.getLabel() + result.getThingUID() + result.getProperties();
        assertFalse(all.contains(SECRET));
        assertFalse(all.toLowerCase(java.util.Locale.ROOT).contains("password"));
        assertEquals(Set.of(CONFIG_HOSTNAME, PROPERTY_HOST_LABEL), result.getProperties().keySet());
    }

    @Test
    void whenTheParticipantProcessesServicesAtAnyLevelThenNoAddressOrHostLabelIsLoggedAtInfoOrAbove() {
        assumeTrue(LoggerFactory.getILoggerFactory() instanceof LoggerContext, "needs logback");
        Logger logger = (Logger) LoggerFactory.getLogger("org.openhab.binding.tibber.internal.pulse");
        Level previous = logger.getLevel();
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.setLevel(Level.TRACE);
        logger.addAppender(appender);
        try {
            ServiceInfo match = bridge(v4("192.0.2.20"));
            ServiceInfo foreign = info("Office Printer", "printer.local.", v4("192.0.2.30"));
            ServiceInfo withoutHost = info(INSTANCE, "");

            participant.createResult(match);
            participant.getThingUID(match);
            participant.createResult(foreign);
            participant.getThingUID(foreign);
            participant.createResult(withoutHost);
            participant.getThingUID(withoutHost);

            assertFalse(appender.list.isEmpty(), "the capture works: the participant logs at debug");
            for (ILoggingEvent event : appender.list) {
                StringBuilder text = new StringBuilder(event.getFormattedMessage());
                for (IThrowableProxy proxy = event.getThrowableProxy(); proxy != null; proxy = proxy.getCause()) {
                    text.append(' ').append(proxy.getMessage());
                }
                String rendered = text.toString().toLowerCase(java.util.Locale.ROOT);
                assertFalse(event.getLevel().isGreaterOrEqual(Level.INFO), rendered);
                for (String secret : new String[] { "192.0.2.", "bridge01234567", "printer", "office", "password" }) {
                    assertFalse(rendered.contains(secret), secret + " in " + rendered);
                }
            }
        } finally {
            logger.detachAppender(appender);
            logger.setLevel(previous);
            appender.stop();
        }
    }
}
