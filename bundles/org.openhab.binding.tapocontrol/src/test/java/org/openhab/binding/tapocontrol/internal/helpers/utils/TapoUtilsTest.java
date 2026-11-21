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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link TapoUtils}.
 *
 * @author Lee Ballard - Initial contribution
 * @author Christopher Smith - Add Kasa EP10, EP40 and KP405 models
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
}
