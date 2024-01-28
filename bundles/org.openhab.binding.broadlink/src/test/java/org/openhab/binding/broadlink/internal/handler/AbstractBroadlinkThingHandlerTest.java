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
package org.openhab.binding.broadlink.internal.handler;

import static org.openhab.binding.broadlink.internal.BroadlinkBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.mockito.Mockito;
import org.openhab.binding.broadlink.AbstractBroadlinkTest;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
import org.openhab.binding.broadlink.internal.BroadlinkProtocol;
import org.openhab.binding.broadlink.internal.BroadlinkRemoteDynamicCommandDescriptionProvider;
import org.openhab.binding.broadlink.internal.socket.NetworkTrafficObserver;
import org.openhab.binding.broadlink.internal.socket.RetryableSocket;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.util.HexUtils;
import org.slf4j.LoggerFactory;

/**
 * Abstract thing handler test.
 * 
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractBroadlinkThingHandlerTest extends AbstractBroadlinkTest {

    protected Map<String, Object> properties = new HashMap<>();
    protected Configuration config = new Configuration();
    protected ThingImpl thing = new ThingImpl(BroadlinkBindingConstants.THING_TYPE_A1, "a1");

    protected RetryableSocket mockSocket = Mockito.mock(RetryableSocket.class);
    protected NetworkTrafficObserver trafficObserver = Mockito.mock(NetworkTrafficObserver.class);
    protected ThingHandlerCallback mockCallback = Mockito.mock(ThingHandlerCallback.class);
    protected BroadlinkRemoteDynamicCommandDescriptionProvider commandDescriptionProvider = Mockito
            .mock(BroadlinkRemoteDynamicCommandDescriptionProvider.class);

    protected void configureUnderlyingThing(ThingTypeUID thingTypeUID, String thingId) {
        properties = new HashMap<>();
        properties.put("authorizationKey", "097628343fe99e23765c1513accf8b02");
        properties.put(Thing.PROPERTY_MAC_ADDRESS, "AB:CD:AB:CD:AB:CD");
        properties.put("iv", "562e17996d093d28ddb3ba695a2e6f58");
        config = new Configuration(properties);

        thing = new ThingImpl(thingTypeUID, thingId);
        thing.setConfiguration(config);
        thing.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
    }

    protected void setMocksForTesting(BroadlinkBaseThingHandler handler) {
        handler.setSocket(mockSocket);
        handler.setNetworkTrafficObserver(trafficObserver);
        handler.setCallback(mockCallback);
        handler.initialize();
    }

    protected byte[] generateReceivedBroadlinkMessage(byte[] payload) {
        byte[] mac = { 0x11, 0x22, 0x11, 0x22, 0x11, 0x22 };
        byte[] devId = { 0x11, 0x22, 0x11, 0x22 };
        return BroadlinkProtocol.buildMessage((byte) 0x6a, payload, 99, mac, devId, HexUtils.hexToBytes(BROADLINK_IV),
                HexUtils.hexToBytes(BROADLINK_AUTH_KEY), 0x2714, LoggerFactory.getLogger(getClass()));
    }
}
