/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import static org.hamcrest.MatcherAssert.assertThat;

import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;

/**
 * Generic test class for all WemoLight related tests that contains methods and constants used across the different test
 * classes
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Stefan Triller - Ported Tests from Groovy to Java
 */
public class GenericWemoLightOSGiTestParent extends GenericWemoOSGiTest {

    // Thing information
    protected static final ThingTypeUID THING_TYPE_UID = WemoBindingConstants.THING_TYPE_MZ100;
    protected static final ThingTypeUID BRIDGE_TYPE_UID = WemoBindingConstants.THING_TYPE_BRIDGE;
    protected static final String WEMO_BRIDGE_ID = BRIDGE_TYPE_UID.getId();
    protected static final String DEFAULT_TEST_CHANNEL = WemoBindingConstants.CHANNEL_STATE;
    protected static final String DEFAULT_TEST_CHANNEL_TYPE = "Switch";

    private static final String WEMO_LIGHT_ID = THING_TYPE_UID.getId();

    // UPnP service information
    protected static final String DEVICE_MODEL_NAME = WEMO_LIGHT_ID;
    protected static final String SERVICE_ID = "bridge";
    protected static final String SERVICE_NUMBER = "1";
    protected static final String SERVLET_URL = DEVICE_CONTROL_PATH + SERVICE_ID + SERVICE_NUMBER;

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
    protected Thing createThing(ThingTypeUID thingTypeUID, String channelID, String itemAcceptedType) {
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
