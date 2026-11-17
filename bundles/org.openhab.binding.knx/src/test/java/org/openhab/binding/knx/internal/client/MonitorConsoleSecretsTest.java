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
package org.openhab.binding.knx.internal.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.knx.internal.console.KNXCommandExtension;
import org.openhab.binding.knx.internal.factory.KNXHandlerFactory;
import org.openhab.binding.knx.internal.handler.GroupAddressListener;
import org.openhab.binding.knx.internal.handler.IPBridgeThingHandler;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.binding.knx.internal.security.KeyringTestData;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.console.Console;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;
import io.calimero.link.KNXNetworkLink;
import io.calimero.mgmt.TransportLayerImpl;

/**
 * Checks that the {@code monitor} console command prints no key, password or hash of one, for a bridge built as the
 * real one is: its configuration holds the secrets, the keyring is loaded and the monitor is filled by the client.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class MonitorConsoleSecretsTest {
    private static final IndividualAddress OPENHAB = new IndividualAddress(1, 2, 3);
    private static final IndividualAddress SENSOR = new IndividualAddress(1, 1, 42);
    private static final GroupAddress SECURE_GROUP = new GroupAddress(8, 0, 0);
    private static final GroupAddress NO_KEY_GROUP = new GroupAddress(9, 7, 7);
    private static final GroupAddress PLAIN_GROUP = new GroupAddress(1, 0, 0);
    private static final byte[] WRITE_ON = { 0x00, (byte) 0x81 };

    private static final String TUNNEL_PASSWORD = "made-up-tunnel-secret";
    private static final String BACKBONE_KEY = "0f1e2d3c4b5a69788796a5b4c3d2e1f0";

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final LinkedBlockingQueue<KNXNetworkLink> links = new LinkedBlockingQueue<>();
    private @Nullable MonitorTestClient client;

    @AfterEach
    void tearDown() {
        MonitorTestClient tmp = client;
        if (tmp != null) {
            tmp.dispose();
        }
        scheduler.shutdownNow();
    }

    private static byte[] securedBySensor() throws Exception {
        try (CustomSecureManagement sensor = new CustomSecureManagement(new TransportLayerImpl(new FeedLink(SENSOR)),
                MonitorTestClient.keyringSecurity())) {
            return sensor.secureGroupObject(SENSOR, SECURE_GROUP, WRITE_ON).orElseThrow();
        }
    }

    /** A monitor with a plain, a secured, a plain-on-secured and two not decrypted rows, all made by the client. */
    private TelegramMonitor filledMonitor() throws Exception {
        TelegramMonitor monitor = new TelegramMonitor();
        FeedLink link = new FeedLink(OPENHAB);
        links.add(link);
        MonitorTestClient c = new MonitorTestClient(scheduler, monitor, () -> links.remove());
        client = c;
        c.initialize();
        assertTrue(c.isConnected());
        GroupAddressListener thing = mock(GroupAddressListener.class);
        when(thing.listensTo(any())).thenReturn(true);
        c.registerGroupAddressListener(thing);

        byte[] secured = securedBySensor();
        byte[] changed = secured.clone();
        changed[changed.length - 1] ^= 0x55;
        link.receive(SENSOR, PLAIN_GROUP, WRITE_ON);
        link.receive(SENSOR, SECURE_GROUP, secured);
        link.receive(SENSOR, SECURE_GROUP, WRITE_ON);
        link.receive(SENSOR, SECURE_GROUP, changed);
        link.receive(SENSOR, NO_KEY_GROUP, secured);

        assertEquals(5, monitor.records().size());
        return monitor;
    }

    private static Bridge realBridge() {
        Configuration configuration = new Configuration(Map.of("keyringPassword", KeyringTestData.PASSWORD,
                "tunnelUserPassword", TUNNEL_PASSWORD, "routerBackboneKey", BACKBONE_KEY));
        return BridgeBuilder.create(new ThingTypeUID("knx", "ip"), new ThingUID("knx", "ip", "home"))
                .withLabel("Home KNX").withConfiguration(configuration).build();
    }

    private static List<String> run(KNXCommandExtension extension, Console console, String... args) {
        clearInvocations(console);
        extension.execute(args, console);
        ArgumentCaptor<String> lines = ArgumentCaptor.forClass(String.class);
        verify(console, atLeast(0)).println(lines.capture());
        return new ArrayList<>(lines.getAllValues());
    }

    @Test
    void outputHoldsNoPasswordKeyOrHash() throws Exception {
        TelegramMonitor monitor = filledMonitor();
        IPBridgeThingHandler handler = mock(IPBridgeThingHandler.class);
        when(handler.getThing()).thenReturn(realBridge());
        when(handler.getTelegramMonitor()).thenReturn(monitor);
        KNXHandlerFactory factory = mock(KNXHandlerFactory.class);
        when(factory.getBridges()).thenReturn(Set.of(handler));
        KNXCommandExtension extension = new KNXCommandExtension(factory,
                new KnxProjectStore(Path.of("target", "no-such-knx-folder"), Clock.systemUTC()),
                mock(LocaleProvider.class));
        Console console = mock(Console.class);

        List<String> lines = run(extension, console, "monitor");
        lines.addAll(run(extension, console, "monitor", "home", "type=write", "limit=100"));
        String output = String.join("\n", lines).toLowerCase(Locale.ROOT);

        assertTrue(output.contains("rows 5"), "the rows are printed: " + output);
        assertTrue(output.contains("secured"), output);
        assertFalse(output.contains(TUNNEL_PASSWORD), "the tunnel password is printed");
        assertFalse(output.contains(BACKBONE_KEY), "the backbone key is printed");
        assertFalse(output.contains(KeyringTestData.PASSWORD), "the keyring password is printed");

        String xml = KeyringTestData.fileContent("openhab6.knxkeys");
        Matcher secret = Pattern
                .compile("(?:Key|Password|Authentication|ToolKey|ManagementPassword|Signature)=\"([^\"]+)\"")
                .matcher(xml);
        int checked = 0;
        while (secret.find()) {
            assertFalse(output.contains(secret.group(1).toLowerCase(Locale.ROOT)), "a secret value is printed");
            byte[] bytes;
            try {
                bytes = Base64.getDecoder().decode(secret.group(1));
            } catch (IllegalArgumentException e) {
                continue;
            }
            if (bytes.length >= 8) {
                assertFalse(output.contains(HexFormat.of().formatHex(bytes)), "a secret value is printed as hex");
                assertFalse(output.contains(HexFormat.ofDelimiter(" ").formatHex(bytes)),
                        "a secret value is printed as hex");
            }
            checked++;
        }
        assertTrue(checked > 40, "the keyring file has many secret values, found " + checked);
    }
}
