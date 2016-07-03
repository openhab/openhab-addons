/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol;

import org.openhab.binding.zwave.internal.protocol.event.ZWaveEvent;

/**
 * ZWave Event Listener interface. Classes that implement this interface need to be able to handle incoming ZWaveEvent
 * events.
 *
 * @author Chris Jackson
 * @author Brian Crosby
 */
public interface ZWaveEventListener {

    /**
     * Event handler method for incoming Z-Wave events.
     *
     * @param event the incoming Z-Wave event.
     */
    void ZWaveIncomingEvent(ZWaveEvent event);
}
