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
package org.openhab.binding.tapocontrol.internal.helpers.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openhab.binding.tapocontrol.internal.TapoControlHandlerFactory.GSON;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tapocontrol.internal.discovery.dto.TapoDiscoveryResult;

/**
 * Tests for {@link TapoUtils}.
 *
 * @author Lee Ballard - Initial contribution
 * @author Christopher Smith - Add Kasa EP10, EP40 and KP405 models and labels
 */
@NonNullByDefault
class TapoUtilsTest {

    @Test
    void preservesFiveCharacterKasaModel() {
        assertThat(TapoUtils.getDeviceModel("HS220(US)"), is("HS220"));
    }

    @Test
    void mapsKasaPlugModels() {
        assertThat(TapoUtils.getDeviceModel("EP10(US)"), is("EP10"));
        assertThat(TapoUtils.getDeviceModel("EP40(US)"), is("EP40"));
        assertThat(TapoUtils.getDeviceModel("KP405(US)"), is("KP405"));
    }

    @Test
    void doesNotMapMatterEp40mToEp40() {
        assertThat(TapoUtils.getDeviceModel("EP40M(US)"), is("EP40M"));
    }

    @Test
    void labelsKasaDevicesWithKasaVendor() {
        assertThat(TapoUtils.getDeviceLabel(discovered("KP405(US)", "Deck Edison")),
                is("Kasa KP405 Dimmer-Plug (Deck Edison)"));
        assertThat(TapoUtils.getDeviceLabel(discovered("EP10(US)", "")), is("Kasa EP10 SmartPlug"));
        assertThat(TapoUtils.getDeviceLabel(discovered("P110(EU)", "")), is("Tapo P110 SmartPlug"));
    }

    private static TapoDiscoveryResult discovered(String model, String alias) {
        return Objects.requireNonNull(GSON.fromJson("{\"device_model\":\"" + model + "\",\"alias\":\"" + alias + "\"}",
                TapoDiscoveryResult.class));
    }
}
