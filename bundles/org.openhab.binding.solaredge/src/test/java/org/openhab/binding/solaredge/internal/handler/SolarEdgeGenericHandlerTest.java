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
package org.openhab.binding.solaredge.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solaredge.internal.oauth.SolarEdgeOAuthException;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * Tests SolarEdge handler status decisions.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class SolarEdgeGenericHandlerTest {
    @Test
    public void distinguishesAuthorizationFromTransientOAuthFailures() {
        assertEquals(ThingStatusDetail.CONFIGURATION_PENDING, SolarEdgeGenericHandler
                .oauthFailureStatusDetail(new SolarEdgeOAuthException("Authorization required", true)));
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR,
                SolarEdgeGenericHandler.oauthFailureStatusDetail(new SolarEdgeOAuthException("Temporary failure")));
    }
}
