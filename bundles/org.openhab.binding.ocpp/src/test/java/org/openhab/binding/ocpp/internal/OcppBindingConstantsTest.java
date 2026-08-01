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
package org.openhab.binding.ocpp.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openhab.binding.ocpp.internal.OcppBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for the connector representation property.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class OcppBindingConstantsTest {

    @Test
    public void connectorRepresentationIsUniqueAcrossChargers() {
        // The framework matches a representation property on thing type and value alone, without
        // consulting the bridge, so connector 1 of two different chargers must not collide.
        assertThat(uniqueConnectorId("charx", 1), is(not(uniqueConnectorId("wallbox", 1))));
    }

    @Test
    public void connectorRepresentationIsUniqueWithinACharger() {
        assertThat(uniqueConnectorId("charx", 1), is(not(uniqueConnectorId("charx", 2))));
    }

    @Test
    public void connectorRepresentationIsStable() {
        assertThat(uniqueConnectorId("charx", 2), is(uniqueConnectorId("charx", 2)));
        assertThat(uniqueConnectorId("charx", 2), is("charx:2"));
    }
}
