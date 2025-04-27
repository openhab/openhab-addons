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
package org.openhab.binding.fenecon.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link AddressComponent}.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public class AddressComponentTest {

    @Test
    void testFixComponent() {
        String component = "component";

        AddressComponent result = new AddressComponent(component);

        assertEquals("component", result.component());
    }

    @Test
    void testVariableComponentChangedForBundleRegexRequest1() {
        String component = "charger0";

        AddressComponent result = new AddressComponent(component);

        assertEquals("charger.+", result.component());
    }

    @Test
    void testVariableComponentChangedForBundleRegexRequest2() {
        String component = "charger1";

        AddressComponent result = new AddressComponent(component);

        assertEquals("charger.+", result.component());
    }
}
