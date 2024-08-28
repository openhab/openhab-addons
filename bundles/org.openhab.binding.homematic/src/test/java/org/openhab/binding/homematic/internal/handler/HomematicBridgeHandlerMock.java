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
package org.openhab.binding.homematic.internal.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.openhab.binding.homematic.internal.type.HomematicTypeGenerator;
import org.openhab.core.thing.Bridge;

/**
 * The {@link HomematicBridgeHandlerMock} is responsible for mocking {@link HomematicBridgeHandler}
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public class HomematicBridgeHandlerMock extends HomematicBridgeHandler {

    public HomematicBridgeHandlerMock(@NonNull Bridge bridge, HomematicTypeGenerator typeGenerator, String ipv4Address,
            HttpClient httpClient) {
        super(bridge, typeGenerator, ipv4Address, httpClient);
        executorService = Mockito.mock(ScheduledExecutorService.class);
        doAnswer((InvocationOnMock invocation) -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(executorService).submit(any(Runnable.class));
    }
}
