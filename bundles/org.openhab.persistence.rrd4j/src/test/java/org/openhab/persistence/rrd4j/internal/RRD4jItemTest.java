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
package org.openhab.persistence.rrd4j.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 * Basic tests for {@link RRD4jItem}.
 * 
 * @author Copilot - Initial contribution
 * @author Holger Friedrich - refactoring and additional tests
 */
@NonNullByDefault
class RRD4jItemTest {

    @Test
    void constructorStoresValues() {
        String name = "TestItem";
        State state = OnOffType.ON;
        Instant instant = Instant.parse("2024-12-31T23:59:59Z");

        RRD4jItem item = new RRD4jItem(name, state, instant);

        assertEquals(name, item.getName());
        assertEquals(state, item.getState());
        assertEquals(instant, item.getInstant());
        assertEquals(instant.atZone(ZoneId.systemDefault()), item.getTimestamp());
    }
}
