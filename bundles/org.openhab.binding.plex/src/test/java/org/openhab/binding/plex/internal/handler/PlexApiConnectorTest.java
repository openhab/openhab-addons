/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.plex.internal.handler;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.openhab.binding.plex.internal.config.PlexServerConfiguration;

/**
 * Tests cases for {@link org.openhab.binding.plex.internal.handler.PlexApiConnector}.
 *
 * @author Brian Homeyer - Initial contribution
 * @author Aron Beurskens - Added test
 */
@NonNullByDefault
public class PlexApiConnectorTest {
    private @NonNullByDefault({}) @Mock ScheduledExecutorService scheduler;
    private @NonNullByDefault({}) @Mock HttpClient httpClient;

    private @NonNullByDefault({}) PlexApiConnector plexApiConnector;

    @BeforeEach
    public void setUp() {
        plexApiConnector = new PlexApiConnector(scheduler, httpClient);
    }

    /**
     * Test that the .hasToken check return the correct values.
     */
    @ParameterizedTest(name = "{index} => token={0}, result={1}")
    @MethodSource("tokenProvider")
    public void testHasToken(String token, Boolean result) {
        PlexServerConfiguration config = new PlexServerConfiguration();
        config.token = token;
        plexApiConnector.setParameters(config);
        assertThat(plexApiConnector.hasToken(), is(result));
    }

    private static Stream<Arguments> tokenProvider() {
        return Stream.of(Arguments.of("123", true), Arguments.of("   ", false),
                Arguments.of("fdsjkghdf-dsjfhs-dsafkshj", true), Arguments.of("", false));
    }
}
