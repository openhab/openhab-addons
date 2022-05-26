/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.networkupstools.internal.nut;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit test to test the {@link NutApi} using a mock Socket connection.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
public class NutApiTest {

    private @Mock Socket socket;
    private NutConnector connector;

    @BeforeEach
    public void setUp() throws IOException {
        connector = new NutConnector("localhost", 0, "test", "pwd") {
            @Override
            protected Socket newSocket() {
                return socket;
            }
        };
    }

    /**
     * Test if retrieving a list of variables is correctly done.
     */
    @Test
    public void testListVariables() throws IOException, NutException, URISyntaxException {
        final String expectedCommands = Files
                .readAllLines(Paths.get(getClass().getResource("var_list_commands.txt").toURI())).stream()
                .collect(Collectors.joining(System.lineSeparator()));
        final StringBuilder actualCommands = new StringBuilder();
        try (InputStream in = getClass().getResourceAsStream("var_list.txt"); OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                actualCommands.append((char) b);
            }
        }) {
            doReturn(in).when(socket).getInputStream();
            doReturn(out).when(socket).getOutputStream();

            final NutApi api = new NutApi(connector);
            final Map<@NonNull String, @NonNull String> variables = api.getVariables("ups1");

            assertThat("Should have variables", variables.size(), is(4));
            assertThat("Should read variable correctly", variables.get("output.voltage.nominal"), is("115"));
            assertThat("Should send commands correctly", actualCommands.toString(), is(expectedCommands));
        }
    }

    /**
     * Test if retrieving a single variable is correctly done.
     */
    @Test
    public void testGetVariable() throws IOException, NutException, URISyntaxException {
        final String expectedCommands = Files
                .readAllLines(Paths.get(getClass().getResource("var_get_commands.txt").toURI())).stream()
                .collect(Collectors.joining(System.lineSeparator()));
        final StringBuilder actualCommands = new StringBuilder();
        try (InputStream in = getClass().getResourceAsStream("var_get.txt"); OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                actualCommands.append((char) b);
            }
        }) {
            doReturn(in).when(socket).getInputStream();
            doReturn(out).when(socket).getOutputStream();

            final NutApi api = new NutApi(connector);
            final String variable = api.getVariable("ups1", "ups.status");
            assertThat("Should read ups.status variable correctly", variable, is("OL"));
            assertThat("Should send commands correctly", actualCommands.toString(), is(expectedCommands));
        }
    }
}
