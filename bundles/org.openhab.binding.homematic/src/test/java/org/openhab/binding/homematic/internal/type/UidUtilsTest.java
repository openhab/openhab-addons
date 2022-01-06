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
package org.openhab.binding.homematic.internal.type;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openhab.binding.homematic.test.util.BridgeHelper.createHomematicBridge;
import static org.openhab.binding.homematic.test.util.DimmerHelper.*;

import org.junit.jupiter.api.Test;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDatapointInfo;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Tests for {@link UidUtilsTest}.
 * 
 * @author Florian Stolte - Initial Contribution
 *
 */
public class UidUtilsTest {

    @Test
    public void testGeneratedThingTypeUIDHasExpectedFormat() {
        HmDevice hmDevice = createDimmerHmDevice();

        ThingTypeUID generatedThingTypeUID = UidUtils.generateThingTypeUID(hmDevice);

        assertThat(generatedThingTypeUID.getAsString(), is("homematic:HM-LC-Dim1-Pl3"));
    }

    @Test
    public void testGeneratedThingTypeUIDHasExpectedFormatForHomegear() {
        HmDevice hmDevice = createDimmerHmDevice("HOMEGEAR");

        ThingTypeUID generatedThingTypeUID = UidUtils.generateThingTypeUID(hmDevice);

        assertThat(generatedThingTypeUID.getAsString(), is("homematic:HG-HM-LC-Dim1-Pl3"));
    }

    @Test
    public void testGeneratedChannelTypeUIDHasExpectedFormat() {
        HmDatapoint hmDatapoint = createDimmerHmDatapoint();

        ChannelTypeUID channelTypeUID = UidUtils.generateChannelTypeUID(hmDatapoint);

        assertThat(channelTypeUID.getAsString(), is("homematic:HM-LC-Dim1-Pl3_1_DIMMER"));
    }

    @Test
    public void testGeneratedChannelUIDHasExpectedFormat() {
        HmDatapoint hmDatapoint = createDimmerHmDatapoint();
        ThingUID thingUID = createDimmerThingUID();

        ChannelUID channelUID = UidUtils.generateChannelUID(hmDatapoint, thingUID);

        assertThat(channelUID.getAsString(), is("homematic:HM-LC-Dim1-Pl3:ABC12345678:1#DIMMER"));
    }

    @Test
    public void testGeneratedChannelGroupTypeUIDHasExpectedFormat() {
        HmChannel hmChannel = createDimmerHmChannel();

        ChannelGroupTypeUID channelGroupTypeUID = UidUtils.generateChannelGroupTypeUID(hmChannel);

        assertThat(channelGroupTypeUID.getAsString(), is("homematic:HM-LC-Dim1-Pl3_1"));
    }

    @Test
    public void testGeneratedThingUIDHasExpectedFormat() {
        HmDevice hmDevice = createDimmerHmDevice();
        Bridge bridge = createHomematicBridge();

        ThingUID thingUID = UidUtils.generateThingUID(hmDevice, bridge);

        assertThat(thingUID.getAsString(), is("homematic:HM-LC-Dim1-Pl3:myBridge:ABC12345678"));
    }

    @Test
    public void testCreateHmDatapointInfoParsesChannelUIDCorrectly() {
        ChannelUID channelUid = new ChannelUID("homematic:HM-LC-Dim1-Pl3:myBridge:ABC12345678:1#DIMMER");

        HmDatapointInfo hmDatapointInfo = UidUtils.createHmDatapointInfo(channelUid);

        assertThat(hmDatapointInfo.getAddress(), is("ABC12345678"));
        assertThat(hmDatapointInfo.getChannel(), is(1));
        assertThat(hmDatapointInfo.getName(), is("DIMMER"));
    }

    @Test
    public void testHomematicAddressIsGeneratedFromThingID() {
        Thing thing = createDimmerThing();

        String generatedAddress = UidUtils.getHomematicAddress(thing);

        assertThat(generatedAddress, is("ABC12345678"));
    }
}
