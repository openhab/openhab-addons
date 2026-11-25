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
package org.openhab.binding.solaredge.internal.command;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Result;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solaredge.internal.config.PublicApiVersion;
import org.openhab.binding.solaredge.internal.config.SolarEdgeConfiguration;
import org.openhab.binding.solaredge.internal.connector.StatusUpdateListener;
import org.openhab.binding.solaredge.internal.handler.SolarEdgeHandler;

/**
 * Tests command completion after a connector generation changes.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class RequestGenerationTest {

    @Test
    public void ignoresOldV2Command() {
        SolarEdgeConfiguration config = new SolarEdgeConfiguration();
        config.setPublicApiVersion(PublicApiVersion.V2);
        verifyStaleCommand(newCommand(config, true));
    }

    @Test
    public void ignoresOldV1Command() {
        verifyStaleCommand(newCommand(new SolarEdgeConfiguration(), false));
    }

    @Test
    public void ignoresOldPrivateApiCommand() {
        SolarEdgeConfiguration config = new SolarEdgeConfiguration();
        config.setUsePrivateApi(true);
        verifyStaleCommand(newCommand(config, false));
    }

    @Test
    public void acceptsCurrentGenerationCompletion() {
        CommandAndListener commandAndListener = newCommand(new SolarEdgeConfiguration(), false);
        AtomicLong generation = new AtomicLong(2);
        assertTrue(commandAndListener.command().bindRequestGeneration(generation.get(), generation::get));
        commandAndListener.command().onComplete(mock(Result.class));
        verify(commandAndListener.listener()).update(any());
    }

    private void verifyStaleCommand(CommandAndListener commandAndListener) {
        AtomicLong generation = new AtomicLong(1);
        AbstractCommand oldCommand = commandAndListener.command();
        assertTrue(oldCommand.bindRequestGeneration(generation.get(), generation::get));
        generation.incrementAndGet();
        assertFalse(oldCommand.bindRequestGeneration(generation.get(), generation::get));
        HttpClient httpClient = mock(HttpClient.class);
        oldCommand.performAction(httpClient);
        oldCommand.onComplete(mock(Result.class));
        verifyNoInteractions(httpClient);
        verify(commandAndListener.listener(), never()).update(any());
    }

    private CommandAndListener newCommand(SolarEdgeConfiguration config, boolean v2) {
        SolarEdgeHandler handler = mock(SolarEdgeHandler.class);
        StatusUpdateListener listener = mock(StatusUpdateListener.class);
        when(handler.getConfiguration()).thenReturn(config);
        AbstractCommand command = config.isUsePrivateApi() ? new PrivateApiTokenCheck(handler, listener)
                : v2 ? new PublicApiV2KeyCheck(handler, listener) : new PublicApiKeyCheck(handler, listener);
        return new CommandAndListener(command, listener);
    }

    private record CommandAndListener(AbstractCommand command, StatusUpdateListener listener) {
    }
}
