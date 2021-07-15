/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.threema.tests.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openhab.binding.threema.internal.ThreemaBindingConstants.THING_TYPE_BASIC;

import java.io.IOException;
import java.util.List;

import org.mockito.Mockito;
import org.openhab.binding.threema.internal.ThreemaBasicConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

import ch.threema.apitool.APIConnector;
import io.micrometer.core.lang.NonNull;

/**
 * @author Kai K. - Initial contribution
 */
@SuppressWarnings("null")
class Mocks {
    public static @NonNull ThreemaBasicConfiguration createThreemaConfiguration(String gatewayID, String secret,
            List<String> recipientIds) {
        ThreemaBasicConfiguration threemaConfig = mock(ThreemaBasicConfiguration.class);
        when(threemaConfig.getGatewayId()).thenReturn(gatewayID);
        when(threemaConfig.getSecret()).thenReturn(secret);
        when(threemaConfig.getRecipientIds()).thenReturn(recipientIds);
        return threemaConfig;
    }

    public static @NonNull Configuration createConfiguration(String gatewayID, String secret,
            List<String> recipientIds) {
        Configuration config = mock(Configuration.class);
        ThreemaBasicConfiguration threemaConfiguration = createThreemaConfiguration(gatewayID, secret, recipientIds);
        when(config.as(ThreemaBasicConfiguration.class)).thenReturn(threemaConfiguration);
        return config;
    }

    public static @NonNull Thing createThing(String gatewayID, String secret, List<String> recipientIds) {
        Thing thing = mock(Thing.class);
        Configuration configuration = createConfiguration(gatewayID, secret, recipientIds);
        when(thing.getConfiguration()).thenReturn(configuration);
        when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE_BASIC, "uid123"));
        return thing;
    }

    public static @NonNull APIConnector createApiConnector() throws IOException {
        APIConnector apiConnector = mock(APIConnector.class);
        when(apiConnector.lookupCredits()).thenReturn(1);
        when(apiConnector.sendTextMessageSimple(Mockito.anyString(), Mockito.anyString())).thenReturn("12343");
        return apiConnector;
    }

    private Mocks() {
    }
}
