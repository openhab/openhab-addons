/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.protocol.event;

import org.openhab.binding.zwave.internal.protocol.initialization.ZWaveNodeInitStage;

/**
 * ZWave Network initialization state event.
 *
 * @author Chris Jackson
 */
public class ZWaveInitializationStateEvent extends ZWaveEvent {
    private ZWaveNodeInitStage stage;

    /**
     * Constructor. Creates a new instance of the ZWaveInitializationStateEvent class.
     *
     * @param nodeId the nodeId of the event. Must be set to the controller node.
     */
    public ZWaveInitializationStateEvent(int nodeId, ZWaveNodeInitStage stage) {
        super(nodeId);
        this.stage = stage;
    }

    /**
     * Returns the current initialisation stage for the node
     *
     * @return node stage
     */
    public ZWaveNodeInitStage getStage() {
        return stage;
    }
}
