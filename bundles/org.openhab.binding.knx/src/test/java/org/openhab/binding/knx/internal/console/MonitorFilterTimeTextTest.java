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
package org.openhab.binding.knx.internal.console;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Path;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.knx.internal.factory.KNXHandlerFactory;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.console.Console;

/**
 * Checks that a time the console cannot read is still printed with the library's own words after the filter text
 * moved to the monitor package.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class MonitorFilterTimeTextTest {

    private static List<String> run(String... args) {
        KNXHandlerFactory factory = mock(KNXHandlerFactory.class);
        when(factory.getBridges()).thenReturn(Set.of());
        KNXCommandExtension extension = new KNXCommandExtension(factory,
                new KnxProjectStore(Path.of("target", "no-such-knx-folder"), Clock.systemUTC()),
                mock(LocaleProvider.class), Clock.fixed(Instant.parse("2026-03-01T10:00:00Z"), ZoneOffset.UTC),
                ZoneOffset.UTC);
        Console console = mock(Console.class);
        extension.execute(args, console);
        ArgumentCaptor<String> lines = ArgumentCaptor.forClass(String.class);
        verify(console, atLeast(0)).println(lines.capture());
        return lines.getAllValues();
    }

    @Test
    void aTimeThatCannotBeReadPrintsTheLibrarysMessage() {
        String time = assertThrows(DateTimeException.class, () -> LocalTime.parse("nonsense")).getMessage();
        assertEquals(List.of("Cannot read \"from=nonsense\": " + time), run("monitor", "from=nonsense"));
        String dateTime = assertThrows(DateTimeException.class, () -> LocalDateTime.parse("2026-13-01T00:00"))
                .getMessage();
        assertEquals(List.of("Cannot read \"to=2026-13-01T00:00\": " + dateTime),
                run("monitor", "to=2026-13-01T00:00"));
    }
}
