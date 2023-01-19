/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpContentResponse;
import org.eclipse.jetty.client.HttpResponse;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.draytonwiser.internal.DraytonWiserBindingConstants;
import org.openhab.binding.draytonwiser.internal.api.DraytonWiserApi;
import org.openhab.binding.draytonwiser.internal.api.DraytonWiserApiException;
import org.openhab.binding.draytonwiser.internal.handler.HeatHubConfiguration;
import org.openhab.binding.draytonwiser.internal.handler.HeatHubHandler;
import org.openhab.binding.draytonwiser.internal.model.DomainDTO;
import org.openhab.binding.draytonwiser.internal.model.DraytonWiserDTO;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

/**
 * Test class for {@link DraytonWiserDiscoveryService}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
public class DraytonWiserDiscoveryServiceTest {

    private @Mock HeatHubHandler bridgeHandler;
    private @Mock Bridge bridge;
    private @Mock HttpClient httpClient;
    private @Mock Request request;

    private DraytonWiserApi api;

    public static List<Object[]> data() {
        return Arrays.asList(new Object[] { "../test1.json", 11 }, new Object[] { "../test2.json", 22 });
    }

    @BeforeEach
    public void before() {
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

    @ParameterizedTest
    @MethodSource("data")
    public void testDiscovery(final String jsonFile, final int expectedResults) throws IOException, URISyntaxException,
            InterruptedException, TimeoutException, ExecutionException, DraytonWiserApiException {
        final byte[] content = Files.readAllBytes(Paths.get(getClass().getResource(jsonFile).toURI()));
        final HttpResponse response = new HttpResponse(null, null);
        response.status(HttpServletResponse.SC_OK);
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
