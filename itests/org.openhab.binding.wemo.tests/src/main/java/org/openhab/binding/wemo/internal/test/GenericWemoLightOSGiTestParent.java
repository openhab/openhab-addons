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
package org.openhab.binding.wemo.internal.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.binding.wemo.internal.handler.AbstractWemoHandler;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;

/**
 * Generic test class for all WemoLight related tests that contains methods and constants used across the different test
 * classes
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Stefan Triller - Ported Tests from Groovy to Java
 */
public class GenericWemoLightOSGiTestParent extends GenericWemoOSGiTest {

    // Thing information
    protected ThingTypeUID THING_TYPE_UID = WemoBindingConstants.THING_TYPE_MZ100;
    protected ThingTypeUID BRIDGE_TYPE_UID = WemoBindingConstants.THING_TYPE_BRIDGE;
    protected String WEMO_BRIDGE_ID = BRIDGE_TYPE_UID.getId();
    protected String DEFAULT_TEST_CHANNEL = WemoBindingConstants.CHANNEL_STATE;
    protected String DEFAULT_TEST_CHANNEL_TYPE = "Switch";

    private final String WEMO_LIGHT_ID = THING_TYPE_UID.getId();

    // UPnP service information
    protected String DEVICE_MODEL_NAME = WEMO_LIGHT_ID;
    protected String SERVICE_ID = "bridge";
    protected String SERVICE_NUMBER = "1";
    protected String SERVLET_URL = DEVICE_CONTROL_PATH + SERVICE_ID + SERVICE_NUMBER;

    private Bridge bridge;

    protected Bridge createBridge(ThingTypeUID bridgeTypeUID) {
        Configuration configuration = new Configuration();
        configuration.put(WemoBindingConstants.UDN, DEVICE_UDN);

        ThingUID bridgeUID = new ThingUID(bridgeTypeUID, WEMO_BRIDGE_ID);

        bridge = BridgeBuilder.create(bridgeTypeUID, bridgeUID).withConfiguration(configuration).build();

        managedThingProvider.add(bridge);
        return bridge;
    }

    @Override
    protected Thing createThing(ThingTypeUID thingTypeUID, String channelID, String itemAcceptedType,
            WemoHttpCall wemoHttpCaller) {
        Configuration configuration = new Configuration();
        configuration.put(WemoBindingConstants.DEVICE_ID, WEMO_LIGHT_ID);

        ThingUID thingUID = new ThingUID(thingTypeUID, TEST_THING_ID);

        ChannelUID channelUID = new ChannelUID(thingUID, channelID);
        Channel channel = ChannelBuilder.create(channelUID, itemAcceptedType).withType(DEFAULT_CHANNEL_TYPE_UID)
                .withKind(ChannelKind.STATE).withLabel("label").build();
        ThingUID bridgeUID = new ThingUID(BRIDGE_TYPE_UID, WEMO_BRIDGE_ID);

        thing = ThingBuilder.create(thingTypeUID, thingUID).withConfiguration(configuration).withChannel(channel)
                .withBridge(bridgeUID).build();

        managedThingProvider.add(thing);

        ThingHandler handler = thing.getHandler();
        if (handler != null) {
            AbstractWemoHandler h = (AbstractWemoHandler) handler;
            h.setWemoHttpCaller(wemoHttpCaller);
        }

        return thing;
    }

    protected void removeThing() {
        if (thing != null) {
            Thing removedThing = thingRegistry.remove(thing.getUID());
            assertThat(removedThing, is(notNullValue()));
        }

        waitForAssert(() -> {
            assertThat(thing.getStatus(), is(ThingStatus.UNINITIALIZED));
        });

        if (bridge != null) {
            Bridge bridgeThing = (Bridge) thingRegistry.remove(bridge.getUID());
            assertThat(bridgeThing, is(notNullValue()));
        }

        waitForAssert(() -> {
            assertThat(bridge.getStatus(), is(ThingStatus.UNINITIALIZED));
        });
    }
}
