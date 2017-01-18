package org.openhab.binding.bosesoundtouch.internal;

/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.State;

/**
 * The {@link BoseSoundTouchHandlerParent} class defines all relevant callbacks for
 * the XML message handlers
 *
 * @author Christian Niessner - Initial contribution
 * @author Thomas Traunbauer
 */

public abstract class BoseSoundTouchHandlerParent extends BaseThingHandler {

    public BoseSoundTouchHandlerParent(Thing thing) {
        super(thing);
    }

    @Override // just overwrite to give XML handlers access.
    protected void updateState(ChannelUID channelUID, State state) {
        super.updateState(channelUID, state);
    }
}
