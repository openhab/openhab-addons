/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.upnpcontrol.internal.handler;

import static org.eclipse.jdt.annotation.Checks.requireNonNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.jupnp.UpnpService;
import org.jupnp.model.message.discovery.OutgoingSearchRequest;
import org.jupnp.transport.Router;
import org.jupnp.transport.RouterException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.upnpcontrol.internal.UpnpDynamicCommandDescriptionProvider;
import org.openhab.binding.upnpcontrol.internal.UpnpDynamicStateDescriptionProvider;
import org.openhab.binding.upnpcontrol.internal.config.UpnpControlBindingConfiguration;
import org.openhab.binding.upnpcontrol.internal.config.UpnpControlConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.upnp.UpnpIOService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for {@link UpnpServerHandlerTest} and {@link UpnpRendererHandlerTest}.
 *
 * @author Mark Herwege - Initial contribution
 */
@SuppressWarnings({ "null" })
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class UpnpHandlerTest {

    private final Logger logger = LoggerFactory.getLogger(UpnpHandlerTest.class);

    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    protected @Nullable UpnpHandler handler;

    @Mock
    protected @Nullable Thing thing;

    @Mock
    protected @Nullable UpnpIOService upnpIOService;

    @Mock
    protected @Nullable UpnpService upnpService;

    @Mock
    protected @Nullable UpnpDynamicStateDescriptionProvider upnpStateDescriptionProvider;

    @Mock
    protected @Nullable UpnpDynamicCommandDescriptionProvider upnpCommandDescriptionProvider;

    protected UpnpControlBindingConfiguration configuration = new UpnpControlBindingConfiguration();

    @Mock
    protected @Nullable Configuration config;

    // Use temporary folder for favorites and playlists testing
    @TempDir
    public @Nullable Path tempFolder;

    @Mock
    @Nullable
    protected ScheduledExecutorService scheduler;

    @Mock
    protected @Nullable ThingHandlerCallback callback;

    public void setUp() {
        // don't test for multi-threading, so avoid using extra threads
        implementAsDirectExecutor(requireNonNull(scheduler));

        String path = tempFolder.toString();
        if (!(path.endsWith(File.separator) || path.endsWith("/"))) {
            path = path + File.separator;
        }
        configuration.path = path;

        // stub thing methods
        when(thing.getConfiguration()).thenReturn(requireNonNull(config));
        when(thing.getStatus()).thenReturn(ThingStatus.OFFLINE);

        // stub upnpIOService methods for initialize
        when(upnpIOService.isRegistered(any())).thenReturn(true);

        Map<String, String> result = new HashMap<>();
        result.put("ConnectionID", "0");
        result.put("AVTransportID", "0");
        result.put("RcsID", "0");
        when(upnpIOService.invokeAction(any(), eq("ConnectionManager"), eq("GetCurrentConnectionInfo"), anyMap()))
                .thenReturn(result);

        // stub config for initialize
        when(config.as(UpnpControlConfiguration.class)).thenReturn(new UpnpControlConfiguration());

        upnpService = mock(UpnpService.class);
        Router router = mock(Router.class);
        when(upnpService.getRouter()).thenReturn(router);
        try {
            doNothing().when(router).send(any(OutgoingSearchRequest.class));
        } catch (RouterException e) {
            // This will never happen in the test since doNothing doesn't trigger behavior
            throw new RuntimeException("Unexpected exception in test setup", e);
        }
    }

    protected void initHandler(UpnpHandler handler) {
        handler.setCallback(callback);
        handler.upnpScheduler = requireNonNull(scheduler);

        // No timeouts for responses, as we don't actually communicate with a UPnP device
        handler.config.responseTimeout = 0;

        doReturn("12345").when(handler).getUDN();
    }

    /**
     * Mock the {@link ScheduledExecutorService}, so all testing is done in the current thread. We do not test
     * request/response with a real media server, so do not need the executor to avoid long running processes.
     * As an exception, we will schedule one off futures with 500ms delay, as this is related to internal
     * synchronization
     * logic.
     *
     * @param executor
     */
    private void implementAsDirectExecutor(ScheduledExecutorService executor) {
        doAnswer(invocation -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(executor).submit(any(Runnable.class));
        doAnswer(invocation -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(executor).scheduleWithFixedDelay(any(Runnable.class), eq(0L), anyLong(), any(TimeUnit.class));
        doAnswer(invocation -> SCHEDULER.schedule((Runnable) invocation.getArguments()[0], 500, TimeUnit.MILLISECONDS))
                .when(executor).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    public void tearDown() {
        logger.info("-----------------------------------------------------------------------------------");
    }
}
