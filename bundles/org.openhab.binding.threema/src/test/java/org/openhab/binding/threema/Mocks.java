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
package org.openhab.binding.threema;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.mockito.Mockito;
import org.openhab.binding.threema.internal.ThreemaConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;

import ch.threema.apitool.APIConnector;

/**
 * @author Kai K. - Initial contribution
 */
@NonNullByDefault
class Mocks {
    public static ThreemaConfiguration createThreemaConfiguration(String gatewayID, String secret,
            List<String> recipientIds) {
        ThreemaConfiguration threemaConfig = mock(ThreemaConfiguration.class);
        when(threemaConfig.getGatewayId()).thenReturn(gatewayID);
        when(threemaConfig.getSecret()).thenReturn(secret);
        when(threemaConfig.getRecipientIds()).thenReturn(recipientIds);
        return threemaConfig;
    }

    public static Configuration createConfiguration(String gatewayID, String secret, List<String> recipientIds) {
        Configuration config = mock(Configuration.class);
        ThreemaConfiguration threemaConfiguration = createThreemaConfiguration(gatewayID, secret, recipientIds);
        when(config.as(ThreemaConfiguration.class)).thenReturn(threemaConfiguration);
        return config;
    }

    public static Thing createThing(String gatewayID, String secret, List<String> recipientIds) {
        Thing thing = mock(Thing.class);
        Configuration configuration = createConfiguration(gatewayID, secret, recipientIds);
        when(thing.getConfiguration()).thenReturn(configuration);
        return thing;
    }

    public static APIConnector createApiConnector() throws IOException {
        APIConnector apiConnector = mock(APIConnector.class);
        when(apiConnector.lookupCredits()).thenReturn(1);
        when(apiConnector.sendTextMessageSimple(Mockito.anyString(), Mockito.anyString())).thenReturn("12343");
        return apiConnector;
    }

    private Mocks() {
    }
}
