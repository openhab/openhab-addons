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
package org.openhab.binding.upb.internal;

import static org.eclipse.jdt.annotation.DefaultLocation.*;

import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.upb.internal.UPBDevice.DeviceState;
import org.openhab.binding.upb.internal.handler.UPBThingHandler;
import org.openhab.binding.upb.internal.handler.VirtualThingHandler;
import org.openhab.binding.upb.internal.message.Command;
import org.openhab.binding.upb.internal.message.UPBMessage;
import org.openhab.binding.upb.internal.message.UPBMessage.Type;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller logic for UPB network communications.
 *
 * @author Marcus Better - Initial contribution
 *
 */
@NonNullByDefault({ PARAMETER, RETURN_TYPE, FIELD })
public class UPBController {
    private final Logger logger = LoggerFactory.getLogger(UPBController.class);

    // Maps of devices and things keyed by (networkId, unitId)
    private final ConcurrentHashMap<Integer, UPBDevice> devices = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, UPBThingHandler> things = new ConcurrentHashMap<>();

    public void incomingMessage(final UPBMessage msg) {
        if (msg.getType() != Type.MESSAGE_REPORT) {
            return;
        }

        final byte networkId = msg.getNetwork();
        final byte srcId = msg.getSource();
        final byte dstId = msg.getDestination();
        final Command cmd = msg.getCommand();
        logger.debug("received message, network={} src={} dst={} cmd={}", networkId & 0xff, srcId & 0xff, dstId & 0xff,
                cmd);
        if (!isValidId(srcId)) {
            return;
        }
        final int srcAddr = mkAddr(networkId, srcId);
        final UPBDevice src = devices.getOrDefault(srcAddr, new UPBDevice(networkId, srcId));
        src.setState(DeviceState.ALIVE);

        final UPBThingHandler thingHnd = things.get(srcAddr);
        if (thingHnd == null) {
            logger.debug("unknown source device {}", srcId & 0xff);
            return;
        }

        if (msg.getControlWord().isLink() || dstId == 0 || srcId == dstId) {
            thingHnd.onMessageReceived(msg);
        }

        // link messages are additionally sent to any virtual devices
        if (msg.getControlWord().isLink()) {
            things.values().stream().filter(hnd -> hnd instanceof VirtualThingHandler)
                    .forEach(hnd -> hnd.onMessageReceived(msg));
        }
    }

    private static boolean isValidId(final byte id) {
        return id != 0 && id != -1;
    }

    public @Nullable UPBDevice getDevice(final byte networkId, final byte unitId) {
        return devices.get(mkAddr(networkId, unitId));
    }

    public void deviceAdded(final ThingHandler childHandler, final Thing childThing) {
        if (childHandler instanceof UPBThingHandler) {
            final UPBThingHandler hnd = (UPBThingHandler) childHandler;
            things.put(mkAddr(hnd.getNetworkId(), hnd.getUnitId()), hnd);
        }
    }

    public void deviceRemoved(final ThingHandler childHandler, final Thing childThing) {
        if (childHandler instanceof UPBThingHandler) {
            final UPBThingHandler hnd = (UPBThingHandler) childHandler;
            things.remove(mkAddr(hnd.getNetworkId(), hnd.getUnitId()), hnd);
        }
    }

    // forms a device lookup key from a network and unit ID
    private static int mkAddr(final byte networkId, final byte srcId) {
        return (networkId & 0xff) << 8 | (srcId & 0xff);
    }
}
