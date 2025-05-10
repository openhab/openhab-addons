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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fenecon.internal.FeneconBindingConstants;

/**
 * Test for {@link AddressComponentChannelUtil}.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public class AddressComponentChannelUtilTest {

    @Test
    void testCreateComponentRequests() {
        // ARRANGE
        List<Address> expectedSumList = List.of(new Address(FeneconBindingConstants.STATE_ADDRESS),
                new Address(FeneconBindingConstants.GRID_MODE_ADDRESS), new Address("system/Version"),
                new Address("battery/SoH"), new Address("battery/Current"));

        // ACT
        List<String> result = AddressComponentChannelUtil.createComponentRequests(expectedSumList);

        // ASSERT
        assertTrue(result.size() == 3);
        assertTrue(result.contains("_sum/(GridMode%7CState)"));
        assertTrue(result.contains("system/(Version)"));
        assertTrue(result.contains("battery/(Current%7CSoH)"));
    }

    @Test
    void testCreateComponentRequestsWithRegEx() {
        // ARRANGE
        List<Address> expectedSumList = List.of(new Address("system/Version"), new Address("battery0/SoH"),
                new Address("battery0/Current"), new Address("battery1/SoH"));

        // ACT
        List<String> result = AddressComponentChannelUtil.createComponentRequests(expectedSumList);

        // ASSERT
        assertTrue(result.size() == 2);
        assertTrue(result.contains("system/(Version)"));
        assertTrue(result.contains("battery.+/(Current%7CSoH)"));
    }

    @Test
    void testSplit() {
        // ARRANGE
        List<Address> expectedSumList = List.of(new Address(FeneconBindingConstants.STATE_ADDRESS),
                new Address(FeneconBindingConstants.GRID_MODE_ADDRESS),
                new Address(FeneconBindingConstants.CONSUMPTION_ACTIVE_POWER_ADDRESS));

        List<Address> expectedFantasyList = List.of(new Address("fantasy/Potter"));
        List<Address> expectedScyFiList = List.of(new Address("scify/Dune"), new Address("scify/Expanse"));

        List<Address> addresses = Stream.of(expectedSumList, expectedFantasyList, expectedScyFiList)
                .flatMap(Collection::stream).toList();

        // ACT
        Map<AddressComponent, Set<AddressChannel>> result = AddressComponentChannelUtil.split(addresses);

        // ASSERT
        assertTrue(result.getOrDefault(new Address(FeneconBindingConstants.STATE_ADDRESS).getComponent(), Set.of())
                .containsAll(expectedSumList.stream().map(Address::getChannel).toList()));

        assertTrue(result.getOrDefault(new AddressComponent("fantasy"), Set.of())
                .containsAll(expectedFantasyList.stream().map(Address::getChannel).toList()));

        assertTrue(result.getOrDefault(new AddressComponent("scify"), Set.of())
                .containsAll(expectedScyFiList.stream().map(Address::getChannel).toList()));
    }

    @Test
    void testCreateRequest() {
        // ARRANGE
        List<Address> expectedSumList = List.of(new Address(FeneconBindingConstants.STATE_ADDRESS),
                new Address(FeneconBindingConstants.GRID_MODE_ADDRESS));

        // ACT
        AddressComponent component = new AddressComponent("_sum");
        Map<AddressComponent, Set<AddressChannel>> split = AddressComponentChannelUtil.split(expectedSumList);
        Set<AddressChannel> sciFyChannels = split.getOrDefault(component, Set.of());
        String result = AddressComponentChannelUtil.createComponentRequest(component, sciFyChannels);

        // ASSERT
        assertEquals("_sum/(GridMode%7CState)", result);
    }
}
