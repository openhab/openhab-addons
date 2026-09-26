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
package org.openhab.binding.rachio.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openhab.binding.rachio.internal.RachioUtils.i18nText;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests Rachio utility helpers.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
class RachioUtilsTest {
    @Test
    void i18nTextWithoutArgumentsUsesCustomTextKey() {
        assertThat(i18nText("thing-status.rachio.bridge.schedule-initialization-failed"),
                is("@text/thing-status.rachio.bridge.schedule-initialization-failed"));
    }

    @Test
    void i18nTextSerializesArgumentsAsJsonStringArray() {
        assertThat(
                i18nText("thing-status.rachio.bridge.api-rate-limit-blocked", "quote\"slash\\newline\n", 42, "reset"),
                is("@text/thing-status.rachio.bridge.api-rate-limit-blocked "
                        + "[\"quote\\\"slash\\\\newline\\n\",\"42\",\"reset\"]"));
    }
}
