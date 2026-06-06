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
package org.openhab.persistence.rrd4j.internal.console;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.core.io.console.Console;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.persistence.PersistenceServiceRegistry;
import org.openhab.core.persistence.registry.PersistenceServiceConfigurationRegistry;
import org.openhab.persistence.rrd4j.internal.RRD4jPersistenceService;

/**
 * Tests for {@link RRD4jCommandExtension}.
 * 
 * @author Copilot - Initial contribution
 * @author Holger Friedrich - refactoring and additional tests
 */
@ExtendWith(MockitoExtension.class)
class RRD4jCommandExtensionTest {

    @Mock
    private PersistenceServiceRegistry persistenceServiceRegistry;

    @Mock
    private ItemRegistry itemRegistry;

    @Mock
    private PersistenceServiceConfigurationRegistry persistenceServiceConfigurationRegistry;

    @Mock
    private Console console;

    @Mock
    private RRD4jPersistenceService persistenceService;

    @Test
    void listCommandPrintsFilesWhenServiceAvailable() {
        when(persistenceServiceRegistry.getAll()).thenReturn(Set.<PersistenceService> of(persistenceService));
        when(persistenceService.getRrdFiles()).thenReturn(List.of("one.rrd", "two.rrd"));

        RRD4jCommandExtension extension = new RRD4jCommandExtension(persistenceServiceRegistry, itemRegistry,
                persistenceServiceConfigurationRegistry);

        extension.execute(new String[] { "list" }, console);

        InOrder order = inOrder(console);
        order.verify(console).println("Existing RRD files...");
        order.verify(console).println("  - one.rrd");
        order.verify(console).println("  - two.rrd");
        order.verify(console).println("2 files found.");
    }

    @Test
    void missingServicePrintsWarning() {
        when(persistenceServiceRegistry.getAll()).thenReturn(Set.of());

        RRD4jCommandExtension extension = new RRD4jCommandExtension(persistenceServiceRegistry, itemRegistry,
                persistenceServiceConfigurationRegistry);

        extension.execute(new String[] { "list" }, console);

        verify(console).println("No RRD4j persistence service installed:");
    }
}
