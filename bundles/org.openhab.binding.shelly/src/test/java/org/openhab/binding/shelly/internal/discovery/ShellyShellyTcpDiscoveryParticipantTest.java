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
package org.openhab.binding.shelly.internal.discovery;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;

/**
 * Tests for {@link ShellyShellyTcpDiscoveryParticipant}.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyShellyTcpDiscoveryParticipantTest {
    private static final String DEVICE_ID = "000000000000";

    @Test
    void serviceTypeIsShellyTcp() {
        assertThat(createShellyTcpParticipant().getServiceType(), is("_shelly._tcp.local."));
    }

    @Test
    void serviceTypeDiffersFromTheHttpParticipant() {
        assertThat(createShellyTcpParticipant().getServiceType(),
                is(not(equalTo(createHttpParticipant().getServiceType()))));
    }

    @Test
    void supportedThingTypesAreInheritedFromTheHttpParticipant() {
        assertThat(createShellyTcpParticipant().getSupportedThingTypeUIDs(),
                is(equalTo(createHttpParticipant().getSupportedThingTypeUIDs())));
    }

    @Test
    void thingUidIsResolvedFromTheServiceName() {
        ThingUID actual = Objects
                .requireNonNull(createShellyTcpParticipant().getThingUID(serviceInfo("shellypresenceg4-" + DEVICE_ID)));

        assertThat(actual, is(equalTo(new ThingUID(THING_TYPE_SHELLYPLUSPRESENCE, DEVICE_ID))));
    }

    @Test
    void thingUidIsNullForANonShellyServiceName() {
        assertThat(createShellyTcpParticipant().getThingUID(serviceInfo("my-shelly")), is(nullValue()));
    }

    @Test
    void resultIsNullForANonShellyServiceName() {
        assertThat(createShellyTcpParticipant().createResult(serviceInfo("my-shelly")), is(nullValue()));
    }

    @Test
    void resultIsNullWhenTheServiceHasNoAddress() {
        assertThat(createShellyTcpParticipant().createResult(serviceInfo("shellypresenceg4-" + DEVICE_ID)),
                is(nullValue()));
    }

    private static ServiceInfo serviceInfo(String serviceName) {
        ServiceInfo service = mock(ServiceInfo.class);
        when(service.getName()).thenReturn(serviceName);
        return service;
    }

    private static ShellyShellyTcpDiscoveryParticipant createShellyTcpParticipant() {
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        when(httpClientFactory.getCommonHttpClient()).thenReturn(mock(HttpClient.class));
        Dictionary<String, Object> properties = new Hashtable<>();
        ComponentContext componentContext = mock(ComponentContext.class);
        when(componentContext.getProperties()).thenReturn(properties);

        return new ShellyShellyTcpDiscoveryParticipant(mock(ConfigurationAdmin.class), httpClientFactory,
                mock(LocaleProvider.class), mock(ShellyTranslationProvider.class), mock(ShellyThingTable.class),
                mock(NetworkAddressService.class), componentContext);
    }

    private static ShellyMDNSDiscoveryParticipant createHttpParticipant() {
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        when(httpClientFactory.getCommonHttpClient()).thenReturn(mock(HttpClient.class));
        Dictionary<String, Object> properties = new Hashtable<>();
        ComponentContext componentContext = mock(ComponentContext.class);
        when(componentContext.getProperties()).thenReturn(properties);

        return new ShellyMDNSDiscoveryParticipant(mock(ConfigurationAdmin.class), httpClientFactory,
                mock(LocaleProvider.class), mock(ShellyTranslationProvider.class), mock(ShellyThingTable.class),
                mock(NetworkAddressService.class), componentContext);
    }
}
