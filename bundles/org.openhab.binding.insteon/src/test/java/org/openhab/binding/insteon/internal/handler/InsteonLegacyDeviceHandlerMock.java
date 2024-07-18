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
package org.openhab.binding.insteon.internal.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.openhab.binding.insteon.internal.InsteonBindingLegacyConstants;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link HomeWizardHandlerMock} is responsible for mocking {@link HomeWizardHandler}
 * 
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class InsteonLegacyDeviceHandlerMock extends InsteonLegacyDeviceHandler {

    public InsteonLegacyDeviceHandlerMock(Thing thing) {
        super(thing);

        executorService = Mockito.mock(ScheduledExecutorService.class);
        doAnswer((InvocationOnMock invocation) -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(executorService).execute(any(Runnable.class));

        doAnswer((InvocationOnMock invocation) -> {
            return new ThingUID(InsteonBindingLegacyConstants.DEVICE_THING_TYPE, "insteon-test-device");
        }).when(this).getBridge();
    }
}
