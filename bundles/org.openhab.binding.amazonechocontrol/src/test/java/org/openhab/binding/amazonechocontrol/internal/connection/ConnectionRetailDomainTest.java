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
import static org.openhab.binding.amazonechocontrol.internal.connection.Connection.normalizeRetailDomain;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests that the retail domain comes out as the bare domain Amazon's hosts are built from, whatever shape the
 * endpoints and users/me answers deliver it in.
 *
 * @author Martin Littkovsky - Initial contribution
 */
@NonNullByDefault
public class ConnectionRetailDomainTest {

    @Test
    public void aBareDomainFromTheEndpointsAnswerIsKept() {
        assertThat(normalizeRetailDomain("amazon.de"), is("amazon.de"));
    }

    @Test
    public void theHostOfTheMarketplaceUrlLosesItsWwwPrefix() {
        assertThat(normalizeRetailDomain("https://www.amazon.de"), is("amazon.de"));
        assertThat(normalizeRetailDomain("https://www.amazon.com"), is("amazon.com"));
        assertThat(normalizeRetailDomain("https://www.amazon.co.jp/"), is("amazon.co.jp"));
    }

    @Test
    public void caseAndWhitespaceDoNotMatter() {
        assertThat(normalizeRetailDomain(" WWW.Amazon.DE "), is("amazon.de"));
    }

    @Test
    public void nothingUsableFallsBackToTheDefault() {
        assertThat(normalizeRetailDomain(null), is("amazon.com"));
        assertThat(normalizeRetailDomain("  "), is("amazon.com"));
    }
}
