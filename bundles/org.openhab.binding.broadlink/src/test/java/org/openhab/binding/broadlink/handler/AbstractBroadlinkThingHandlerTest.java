/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.broadlink.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.mockito.Mockito;
import org.openhab.binding.broadlink.internal.BroadlinkBindingConstants;
import org.openhab.binding.broadlink.internal.socket.NetworkTrafficObserver;
import org.openhab.binding.broadlink.internal.socket.RetryableSocket;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.internal.ThingImpl;

/**
 * Abstract thing handler test.
 * 
 * @author John Marshall/Cato Sognen - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractBroadlinkThingHandlerTest {

    protected Map<String, Object> properties = new HashMap<>();
    protected Configuration config = new Configuration();
    protected ThingImpl thing = new ThingImpl(BroadlinkBindingConstants.THING_TYPE_A1, "a1");
    protected RetryableSocket mockSocket = Mockito.mock(RetryableSocket.class);
    protected NetworkTrafficObserver trafficObserver = Mockito.mock(NetworkTrafficObserver.class);
    protected ThingHandlerCallback mockCallback = Mockito.mock(ThingHandlerCallback.class);

    protected void configureUnderlyingThing(ThingTypeUID thingTypeUID, String thingId) {
        properties = new HashMap<>();
        properties.put("authorizationKey", "097628343fe99e23765c1513accf8b02");
        properties.put("mac", "AB:CD:AB:CD:AB:CD");
        properties.put("iv", "562e17996d093d28ddb3ba695a2e6f58");
        config = new Configuration(properties);

        thing = new ThingImpl(thingTypeUID, thingId);
        thing.setConfiguration(config);
    }

    protected void setMocksForTesting(BroadlinkBaseThingHandler handler) {
        handler.setSocket(mockSocket);
        handler.setNetworkTrafficObserver(trafficObserver);
        handler.setCallback(mockCallback);
    }
}
