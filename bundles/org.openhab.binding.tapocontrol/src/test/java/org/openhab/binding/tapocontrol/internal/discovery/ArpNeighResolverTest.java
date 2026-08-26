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
package org.openhab.binding.tapocontrol.internal.discovery;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * @author Kai Kreuzer - Initial contribution
 */
class ArpNeighResolverTest {

    @Test
    void resolvesLinuxNeighborTableFormat() {
        ArpNeighResolver resolver = new ArpNeighResolver() {
            @Override
            protected List<String> readNeighborTable() {
                return List.of("192.168.1.52 dev eth0 lladdr 30:de:4b:50:21:54 REACHABLE",
                        "192.168.1.1 dev eth0 lladdr aa:bb:cc:dd:ee:ff STALE");
            }
        };
        assertEquals(Optional.of("192.168.1.52"), resolver.resolveMac("30-de-4b-50-21-54"));
        assertEquals(Optional.of("192.168.1.1"), resolver.resolveMac("AA:BB:CC:DD:EE:FF"));
        assertTrue(resolver.resolveMac("11:22:33:44:55:66").isEmpty());
    }

    @Test
    void resolvesMacOsArpFormat() {
        ArpNeighResolver resolver = new ArpNeighResolver() {
            @Override
            protected List<String> readNeighborTable() {
                return List.of("? (192.168.178.44) at 30-de-4b-50-21-54 on en0 ifscope [ethernet]");
            }
        };
        assertEquals(Optional.of("192.168.178.44"), resolver.resolveMac("30:de:4b:50:21:54"));
    }

    @Test
    void blankOrNullMacYieldsEmpty() {
        ArpNeighResolver resolver = new ArpNeighResolver() {
            @Override
            protected List<String> readNeighborTable() {
                return List.of("? (192.168.1.9) at 30:de:4b:00:00:01 on en0");
            }
        };
        assertTrue(resolver.resolveMac("").isEmpty());
        assertTrue(resolver.resolveMac("  ").isEmpty());
    }
}
