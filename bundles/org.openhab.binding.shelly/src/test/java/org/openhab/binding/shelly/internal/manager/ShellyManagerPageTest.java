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
package org.openhab.binding.shelly.internal.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.openhab.core.thing.Thing.PROPERTY_FIRMWARE_VERSION;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.binding.shelly.internal.ShellyHandlerFactory;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * Tests for {@link ShellyManagerPage}.
 *
 * @author Holger Friedrich - Initial contribution
 */
@NonNullByDefault
public class ShellyManagerPageTest {

    private final ShellyManagerPage page = new ShellyManagerPage(mock(ConfigurationAdmin.class),
            mock(ShellyTranslationProvider.class), mock(HttpClient.class), "127.0.0.1", 8080,
            mock(ShellyHandlerFactory.class), new ShellyManagerCache<>(), new ShellyManagerCache<>());

    @ParameterizedTest
    @CsvSource(value = { "2.1.1/20261026, 2.1.1", "1.7.0-beta2/20250819, 1.7.0-beta2", "v1.14.0/20230913, v1.14.0",
            "1.2.3/, 1.2.3", "/20230913, ''", "/, ''", "2.1.1, 2.1.1" })
    public void firmwareVersionIsShownWithoutBuildDate(String firmwareVersion, String expected) {
        String html = page.fillAttributes("<td>${" + PROPERTY_FIRMWARE_VERSION + "}</td>",
                Map.of(PROPERTY_FIRMWARE_VERSION, firmwareVersion));

        assertThat(html, is("<td>" + expected + "</td>"));
    }
}
