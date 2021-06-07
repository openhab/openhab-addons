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
package org.openhab.binding.netatmo.internal.api.dto;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Hashtable;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.ComponentContext;

/**
 * @author Gaël L'hopital - Initial contribution
 */
public class NAObjectTest {
    private static ApiBridge apiBridge;

    @BeforeAll
    public static void init() {
        ComponentContext componentContext = mock(ComponentContext.class);
        when(componentContext.getProperties()).thenReturn(new Hashtable<>());
        OAuthFactory oAuthFactory = mock(OAuthFactory.class);
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
        apiBridge = new ApiBridge(oAuthFactory, httpClientFactory, timeZoneProvider, componentContext);
    }

    @Test
    public void testNAObject() throws Exception {
        String naObject = "{id:\"5954e7f249c75f97428b7b23\",name:\"Your House\"}";
        NAObject object = apiBridge.deserialize(NAObject.class, naObject);
        assertEquals(object.getName(), "Your House");
        assertEquals(object.getId(), "5954e7f249c75f97428b7b23");
    }
}
