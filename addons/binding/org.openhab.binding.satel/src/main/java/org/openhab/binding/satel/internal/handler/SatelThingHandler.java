/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.satel.internal.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.satel.internal.config.SatelThingConfig;
import org.openhab.binding.satel.internal.event.SatelEventListener;

/**
 * The {@link SatelThingHandler} is base thing handler class for all non-bridge things.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public abstract class SatelThingHandler extends BaseThingHandler implements SatelEventListener {

    protected SatelThingConfig thingConfig;
    protected SatelBridgeHandler bridgeHandler;

    public SatelThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        if (bridgeHandler != null) {
            bridgeHandler.removeEventListener(this);
        }
    }

    @Override
    public void initialize() {
        thingConfig = getConfig().as(SatelThingConfig.class);

        Bridge bridge = getBridge();
        if (bridge != null) {
            ThingHandler handler = bridge.getHandler();
            if (handler != null && handler instanceof SatelBridgeHandler) {
                bridgeHandler = (SatelBridgeHandler) handler;
                bridgeHandler.addEventListener(this);
            }
            if (bridge.getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    protected void updateSwitch(String channelID, boolean switchOn) {
        ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), channelID);
        updateSwitch(channelUID, switchOn);
    }

    protected void updateSwitch(ChannelUID channelUID, boolean switchOn) {
        State state = switchOn ? OnOffType.ON : OnOffType.OFF;
        updateState(channelUID, state);
    }

    protected byte[] getObjectBitset(int size, int... ids) {
        byte[] bitset = new byte[size];
        for (int id : ids) {
            int bitNbr = id - 1;
            bitset[bitNbr / 8] |= (byte) (1 << (bitNbr % 8));
        }
        return bitset;
    }

}
