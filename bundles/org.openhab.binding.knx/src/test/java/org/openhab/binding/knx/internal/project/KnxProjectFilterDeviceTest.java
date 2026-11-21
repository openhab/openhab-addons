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
package org.openhab.binding.knx.internal.project;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests which devices {@link KnxProject#isFilterDevice(KnxDevice)} takes for filter devices.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxProjectFilterDeviceTest {

    private static final String PRODUCT_ID = "M-00C8_H-F-1_P-F";

    private static KnxDevice device(String name, @Nullable String description, @Nullable String productRefId) {
        return new KnxDevice("P-1_DI-1", new KnxIndividualAddress(15, 15, 1), name, description, productRefId, null,
                null, List.of(), List.of(), List.of());
    }

    private static KnxProject project(KnxDevice device, List<KnxProduct> products) {
        KnxProjectInfo info = new KnxProjectInfo("P-1", "Made up", KnxGroupAddressStyle.THREE_LEVEL, null, null, null,
                null);
        return new KnxProject(info, "Installation", List.of(), List.of(), new KnxTopology(List.of(), List.of(device)),
                List.of(), 0, products, List.of());
    }

    private static boolean filter(@Nullable String text, @Nullable String orderNumber) {
        KnxDevice device = device("Made-up device", null, PRODUCT_ID);
        return project(device, List.of(new KnxProduct(PRODUCT_ID, text, orderNumber))).isFilterDevice(device);
    }

    @ParameterizedTest
    @ValueSource(strings = { "dummy", "Dummy", " DUMMY ", "\tdummy\n" })
    void theOrderNumberAloneMakesAFilterDevice(String orderNumber) {
        assertTrue(filter("Made-up line filler", orderNumber));
        assertTrue(filter(null, orderNumber));
    }

    @ParameterizedTest
    @ValueSource(strings = { "dummy-2", "XDUMMY", "", "MU 1.1", "dummy 2" })
    void anotherOrderNumberDoesNot(String orderNumber) {
        assertFalse(filter("Made-up actuator", orderNumber));
        assertFalse(filter(null, orderNumber));
    }

    @ParameterizedTest
    @ValueSource(strings = { "Dummy", "KNX Dummy", "dummy (filter)", "Dummy-1", "(DUMMY)", "Made-up dummy 2", "2dummy",
            "Dummy2", "dummy2 filter", "Adummy, dummy" })
    void theWordInTheProductTextMakesAFilterDevice(String text) {
        assertTrue(filter(text, "MU 1.1"));
        assertTrue(filter(text, null));
    }

    @ParameterizedTest
    @ValueSource(strings = { "Dummyload", "Adummy", "Made-up actuator", "dumm", "" })
    void theWordInsideAnotherWordDoesNot(String text) {
        assertFalse(filter(text, "MU 1.1"));
        assertFalse(filter(text, null));
    }

    @Test
    void aProductWithoutTextAndOrderNumberIsNoFilterDevice() {
        assertFalse(filter(null, null));
    }

    @Test
    void theNameAndTheDescriptionTheInstallerTypedAreNotLookedAt() {
        KnxDevice device = device("Dummy", "dummy", PRODUCT_ID);

        assertFalse(project(device, List.of(new KnxProduct(PRODUCT_ID, "Made-up actuator", "MU 1.1")))
                .isFilterDevice(device));
    }

    @Test
    void aDeviceWithoutProductIsNoFilterDevice() {
        KnxDevice device = device("Dummy", "dummy", null);

        assertFalse(project(device, List.of(new KnxProduct(PRODUCT_ID, "Dummy", "dummy"))).isFilterDevice(device));
    }

    @Test
    void aProductTheManufacturerFilesDoNotHoldIsNoFilterDevice() {
        KnxDevice device = device("Dummy", "dummy", "M-00C8_H-F-1_P-OTHER");

        assertFalse(project(device, List.of(new KnxProduct(PRODUCT_ID, "Dummy", "dummy"))).isFilterDevice(device));
        assertFalse(project(device, List.of()).isFilterDevice(device));
    }
}
