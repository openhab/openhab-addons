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
package org.openhab.binding.amazonechocontrol.internal.connection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openhab.binding.amazonechocontrol.internal.connection.Connection.renewalTime;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests that the access token and the session are renewed before their lifetime ends, not after.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class ConnectionTokenRenewalTest {

    private static final long NOW = 1_000_000L;

    @Test
    public void anAccessTokenValidForOneHourIsRenewedAfterFortyEightMinutes() {
        long oneHour = Duration.ofHours(1).toSeconds();

        assertThat(renewalTime(NOW, oneHour), is(NOW + Duration.ofMinutes(48).toMillis()));
    }

    @Test
    public void aSessionValidForFiveDaysIsRenewedAfterFourDays() {
        long fiveDays = Duration.ofDays(5).toSeconds();

        assertThat(renewalTime(NOW, fiveDays), is(NOW + Duration.ofDays(4).toMillis()));
    }
}
