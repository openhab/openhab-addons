/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.draytonwiser.internal.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.openhab.binding.draytonwiser.internal.DraytonWiserBindingConstants;
import org.openhab.binding.draytonwiser.internal.api.DraytonWiserApi;
import org.openhab.binding.draytonwiser.internal.api.DraytonWiserApiException;
import org.openhab.binding.draytonwiser.internal.handler.HeatHubConfiguration;
import org.openhab.binding.draytonwiser.internal.handler.HeatHubHandler;
import org.openhab.binding.draytonwiser.internal.model.DomainDTO;
import org.openhab.binding.draytonwiser.internal.model.DraytonWiserDTO;

/**
 * Test class for {@link DraytonWiserDiscoveryService}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@RunWith(Parameterized.class)
public class DraytonWiserDiscoveryServiceTest {

    @Mock
    private HeatHubHandler bridgeHandler;
    @Mock
    private Bridge bridge;
    @Mock
    private HttpClient httpClient;
    @Mock
    private Request request;

    private DraytonWiserApi api;
    private final String jsonFile;
    private final int expectedResults;

    public DraytonWiserDiscoveryServiceTest(final String jsonFile, final int expectedResults) {
        this.jsonFile = jsonFile;
        this.expectedResults = expectedResults;
    }

    @Parameters(name = "{0}")
    public static List<Object[]> data() {
        return Arrays.asList(new Object[] { "../test1.json", 11 }, new Object[] { "../test2.json", 22 });
    }

    @Before
    public void before() {
        initMocks(this);
        api = new DraytonWiserApi(httpClient);
        api.setConfiguration(new HeatHubConfiguration());

        doReturn(request).when(httpClient).newRequest((String) any());
        doReturn(request).when(request).method((String) any());
        doReturn(request).when(request).header((String) any(), any());
        doReturn(request).when(request).content(any());
        doReturn(request).when(request).timeout(anyLong(), any());
        doReturn(bridge).when(bridgeHandler).getThing();
        doReturn(new ThingUID(DraytonWiserBindingConstants.THING_TYPE_BRIDGE, "1")).when(bridge).getUID();
    }

    @Test
    public void testDiscovery() throws IOException, URISyntaxException, InterruptedException, TimeoutException,
            ExecutionException, DraytonWiserApiException {
        final byte[] content = Files.readAllBytes(Paths.get(getClass().getResource(jsonFile).toURI()));
        final HttpResponse response = new HttpResponse(null, null);
        response.status(Response.SC_OK);
        doReturn(new HttpContentResponse(response, content, null, null)).when(request).send();
        final List<DiscoveryResult> discoveryResults = new ArrayList<>();
        final DraytonWiserDiscoveryService service = new DraytonWiserDiscoveryService() {
            @Override
            protected void thingDiscovered(final DiscoveryResult discoveryResult) {
                discoveryResults.add(discoveryResult);
            }
        };
        service.setThingHandler(bridgeHandler);
        final DomainDTO domain = api.getDomain();

        if (domain == null) {
            fail("DomainDTO object is null");
        } else {
            service.onRefresh(new DraytonWiserDTO(domain));
            assertThat(discoveryResults.size(), is(expectedResults));
        }
    }
}
