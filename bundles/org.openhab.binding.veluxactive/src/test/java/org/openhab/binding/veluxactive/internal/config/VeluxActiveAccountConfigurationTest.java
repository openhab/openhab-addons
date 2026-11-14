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
package org.openhab.binding.veluxactive.internal.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Verifies that account credentials do not leak through diagnostic output.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
class VeluxActiveAccountConfigurationTest {
    @Test
    void toStringRedactsPassword() {
        var configuration = new VeluxActiveAccountConfiguration("user@example.com", "secret-password", 60);

        assertThat(configuration.toString(), not(containsString("secret-password")));
        assertThat(configuration.toString(), containsString("password=[redacted]"));
    }
}
