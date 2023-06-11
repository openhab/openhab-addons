/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.satel.internal.config.SatelThingConfig;
import org.openhab.binding.satel.internal.event.SatelEventListener;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.State;

/**
 * The {@link SatelThingHandler} is base thing handler class for all non-bridge things.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public abstract class SatelThingHandler extends BaseThingHandler implements SatelEventListener {

    private @Nullable SatelThingConfig thingConfig;
    private @Nullable SatelBridgeHandler bridgeHandler;

    public SatelThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        final SatelBridgeHandler bridgeHandler = this.bridgeHandler;
        if (bridgeHandler != null) {
            bridgeHandler.removeEventListener(this);
        }
    }

    @Override
    public void initialize() {
        thingConfig = getConfig().as(SatelThingConfig.class);

        final Bridge bridge = getBridge();
        if (bridge != null) {
            final ThingHandler handler = bridge.getHandler();
            if (handler != null && handler instanceof SatelBridgeHandler) {
                ((SatelBridgeHandler) handler).addEventListener(this);
                this.bridgeHandler = (SatelBridgeHandler) handler;
            }
            if (bridge.getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        }
    }

    protected SatelThingConfig getThingConfig() {
        final SatelThingConfig thingConfig = this.thingConfig;
        if (thingConfig != null) {
            return thingConfig;
        }
        throw new IllegalStateException("Thing handler is not initialized yet for thing " + getThing().getUID());
    }

    protected void withBridgeHandlerPresent(Consumer<SatelBridgeHandler> action) {
        final SatelBridgeHandler bridgeHandler = this.bridgeHandler;
        if (bridgeHandler != null) {
            action.accept(bridgeHandler);
        }
    }

    protected SatelBridgeHandler getBridgeHandler() {
        final SatelBridgeHandler bridgeHandler = this.bridgeHandler;
        if (bridgeHandler != null) {
            return bridgeHandler;
        }
        throw new IllegalStateException("Bridge handler is not set for thing " + getThing().getUID());
    }

    /**
     * Updates switch channel with given state.
     *
     * @param channelID channel ID
     * @param switchOn if <code>true</code> the channel is updated with ON state, with OFF state otherwise
     */
    protected void updateSwitch(String channelID, boolean switchOn) {
        ChannelUID channelUID = new ChannelUID(this.getThing().getUID(), channelID);
        updateSwitch(channelUID, switchOn);
    }

    /**
     * Updates switch channel with given state.
     *
     * @param channelUID channel UID
     * @param switchOn if <code>true</code> the channel is updated with ON state, with OFF state otherwise
     */
    protected void updateSwitch(ChannelUID channelUID, boolean switchOn) {
        State state = switchOn ? OnOffType.ON : OnOffType.OFF;
        updateState(channelUID, state);
    }

    /**
     * Creates bitset of given size with particular bits set to 1.
     *
     * @param size bitset size in bytes
     * @param ids bits to set, first bit is 1
     * @return bitset as array of bytes
     */
    protected byte[] getObjectBitset(int size, int... ids) {
        byte[] bitset = new byte[size];
        for (int id : ids) {
            int bitNbr = id - 1;
            bitset[bitNbr / 8] |= (byte) (1 << (bitNbr % 8));
        }
        return bitset;
    }
}
