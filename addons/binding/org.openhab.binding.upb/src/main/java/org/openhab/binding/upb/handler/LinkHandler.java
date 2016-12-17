/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.upb.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.upb.UPBBindingConstants;

/**
 * Handles commands sent to a {@link UPBBindingConstants#THING_TYPE_LINK}.
 *
 * @author Chris Van Orman
 * @since 2.0.0
 *
 */
public class LinkHandler extends UPBBaseHandler {

    private long duplicateTimeout;
    private State lastState;
    private long lastUpdate;

    /**
     * Instantiates a new {@link LinkHandler}.
     *
     * @param thing the thing that should be handled
     */
    public LinkHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        this.duplicateTimeout = ((Number) getConfig().get(UPBBindingConstants.DUPLICATE_TIMEOUT)).longValue();
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        if (state.equals(lastState) && (System.currentTimeMillis() - lastUpdate) < duplicateTimeout) {
            return;
        }

        this.lastState = state;
        this.lastUpdate = System.currentTimeMillis();

        super.updateState(channelUID, state);
    }

    @Override
    protected boolean isLink() {
        return true;
    }
}
