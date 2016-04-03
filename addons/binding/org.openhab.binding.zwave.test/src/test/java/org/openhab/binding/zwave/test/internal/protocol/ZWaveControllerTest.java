/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.test.internal.protocol;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveEventListener;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

public class ZWaveControllerTest {

    @Test
    public void AddNotificationListener() {
        ZWaveController controller = new ZWaveController(null);

        ZWaveEventListener eventListener = Mockito.mock(ZWaveEventListener.class);
        controller.addEventListener(eventListener);
        ZWaveCommandClassValueEvent event = new ZWaveCommandClassValueEvent(0, 0, null, null);

        // Notify an event and make sure we see it
        controller.notifyEventListeners(event);
        Mockito.verify(eventListener, Mockito.times(1)).ZWaveIncomingEvent((ZWaveEvent) Matchers.anyObject());

        // Register the handler again - this should be filtered
        controller.addEventListener(eventListener);

        // Notify the event again - we should only see one notification
        controller.notifyEventListeners(event);
        Mockito.verify(eventListener, Mockito.times(2)).ZWaveIncomingEvent((ZWaveEvent) Matchers.anyObject());
    }
}
