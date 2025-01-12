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
package org.openhab.binding.homewizard.internal.devices.plug_in_battery;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.openhab.binding.homewizard.internal.devices.p1_meter.HomeWizardP1MeterHandler;
import org.openhab.binding.homewizard.internal.devices.p1_meter.HomeWizardP1MeterHandlerMock;
import org.openhab.core.thing.Thing;

/**
 * The {@link HomeWizardP1MeterHandlerMock} is responsible for mocking {@link HomeWizardP1MeterHandler}
 *
 * @author Gearrel Welvaart - Initial contribution
 */
@NonNullByDefault
public class HomeWizardPlugInBatteryHandlerMock extends HomeWizardPlugInBatteryHandler {

    public HomeWizardPlugInBatteryHandlerMock(Thing thing) {
        super(thing);

        executorService = Mockito.mock(ScheduledExecutorService.class);
        doAnswer((InvocationOnMock invocation) -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(executorService).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class));
    }
}
