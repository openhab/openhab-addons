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
package org.openhab.binding.pleos.internal.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Brand}.
 *
 * @author Stephan Sandmann - Initial contribution
 */
@NonNullByDefault
public class BrandTest {

    @Test
    public void testBrandEnum() {
        assertEquals("HYUNDAI", Brand.HYUNDAI.name());
        assertEquals("KIA", Brand.KIA.name());
        assertEquals("GENESIS", Brand.GENESIS.name());
        assertEquals("UNKNOWN", Brand.UNKNOWN.name());
    }
}
