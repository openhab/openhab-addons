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
package org.openhab.binding.amazonechocontrol.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.THING_TYPE_ECHO;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

/**
 * Tests for {@link AmazonEchoDiscovery}.
 *
 * @author Lee Ballard - Initial contribution
 */
class AmazonEchoDiscoveryTest {

    @ParameterizedTest
    @CsvSource(value = { //
            "40B4CD10B295, 40:b4:cd:10:b2:95", //
            "40:B4:CD:10:B2:95, 40:b4:cd:10:b2:95", //
            "40-B4-CD-10-B2-95, 40:b4:cd:10:b2:95" //
    })
    void testMacAddressIsAddedToDiscoveryProperties(String input, String expected) {
        DiscoveryResultBuilder builder = newDiscoveryResultBuilder();

        AmazonEchoDiscovery.addMacAddressProperty(builder, input);

        assertEquals(expected, builder.build().getProperties().get(Thing.PROPERTY_MAC_ADDRESS));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", "invalid", "000000000000" })
    void testInvalidMacAddressIsNotAddedToDiscoveryProperties(String input) {
        DiscoveryResultBuilder builder = newDiscoveryResultBuilder();

        AmazonEchoDiscovery.addMacAddressProperty(builder, input);

        assertFalse(builder.build().getProperties().containsKey(Thing.PROPERTY_MAC_ADDRESS));
    }

    private DiscoveryResultBuilder newDiscoveryResultBuilder() {
        return DiscoveryResultBuilder.create(new ThingUID(THING_TYPE_ECHO, "account", "echo")).withLabel("Echo");
    }
}
