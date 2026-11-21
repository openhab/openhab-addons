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
package org.openhab.binding.knx.internal.factory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.knx.internal.KNXBindingConstants.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.i18n.KNXTranslationProvider;
import org.openhab.binding.knx.internal.monitor.SecurityState;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.monitor.TelegramRecord;
import org.openhab.binding.knx.internal.monitor.TelegramType;
import org.openhab.binding.knx.internal.project.ProjectTestFiles;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;

/**
 * Checks that the handler factory gives the monitor of each bridge the lookup of its stored project.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ProjectWiringTest {

    @TempDir
    @Nullable
    Path tempFolder;

    @AfterEach
    void tearDown() {
        KNXTranslationProvider.I18N.setProvider(null, null);
    }

    private static Bridge bridge(String type, ThingUID uid) {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getThingTypeUID()).thenReturn("ip".equals(type) ? THING_TYPE_IP_BRIDGE : THING_TYPE_SERIAL_BRIDGE);
        when(bridge.getUID()).thenReturn(uid);
        return bridge;
    }

    @ParameterizedTest
    @ValueSource(strings = { "ip", "serial" })
    void theMonitorOfEachBridgeTypeUsesTheStoredProject(String type) throws Exception {
        Path folder = Objects.requireNonNull(tempFolder);
        KnxProjectStore store = new KnxProjectStore(folder, Clock.systemUTC());
        ThingUID uid = new ThingUID("knx:" + type + ":made-up");
        KNXHandlerFactory factory = new KNXHandlerFactory(mock(NetworkAddressService.class), Map.of(),
                mock(TranslationProvider.class), mock(LocaleProvider.class), mock(SerialPortManager.class), store);

        ThingHandler handler = factory.createHandler(bridge(type, uid));
        assertNull(store.get(uid), "creating the handler does not import anything");
        store.importProject(uid, ProjectTestFiles.plain(), null, null);

        TelegramMonitor monitor = ((KNXBridgeBaseThingHandler) Objects.requireNonNull(handler)).getTelegramMonitor();
        monitor.received(new IndividualAddress(7, 7, 1), new GroupAddress(31, 7, 2), TelegramType.WRITE,
                new byte[] { 0x0c, 0x33 }, SecurityState.PLAIN);
        TelegramRecord record = monitor.records().get(0);
        assertEquals("Made-up temperature", record.destinationName());
        assertEquals("21.5", record.value());

        factory.unregisterHandler(bridge(type, uid));
        assertTrue(Files.exists(folder.resolve("project-knx_" + type + "_made-up.json")),
                "removing the bridge keeps its project");
    }
}
