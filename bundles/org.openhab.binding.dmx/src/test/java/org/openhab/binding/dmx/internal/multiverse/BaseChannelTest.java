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
package org.openhab.binding.dmx.internal.multiverse;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests cases for BaseChannel
 *
 * @author Jan N. Klug - Initial contribution
 */
public class BaseChannelTest {

    @Test
    public void creatingBaseChannelFromIntegers() {
        // overrange
        BaseDmxChannel channel = new BaseDmxChannel(0, 600);
        assertThat(channel.getChannelId(), is(BaseDmxChannel.MAX_CHANNEL_ID));

        // underrange
        channel = new BaseDmxChannel(0, -1);
        assertThat(channel.getChannelId(), is(BaseDmxChannel.MIN_CHANNEL_ID));

        // inrange & universe
        channel = new BaseDmxChannel(5, 100);
        assertThat(channel.getChannelId(), is(100));
        assertThat(channel.getUniverseId(), is(5));

        // set universe
        channel.setUniverseId(1);
        assertThat(channel.getUniverseId(), is(1));
    }

    @Test
    public void creatingBaseChannelfromBaseChannel() {
        BaseDmxChannel baseChannel = new BaseDmxChannel(5, 100);
        BaseDmxChannel copyChannel = new BaseDmxChannel(baseChannel);

        assertThat(copyChannel.getChannelId(), is(100));
        assertThat(copyChannel.getUniverseId(), is(5));
    }

    @Test
    public void comparingChannels() {
        BaseDmxChannel channel1 = new BaseDmxChannel(5, 100);
        BaseDmxChannel channel2 = new BaseDmxChannel(7, 140);

        assertThat(channel1.compareTo(channel2), is(-1));
        assertThat(channel2.compareTo(channel1), is(1));
        assertThat(channel1.compareTo(channel1), is(0));
    }

    @Test
    public void stringConversion() {
        // to string
        BaseDmxChannel baseChannel = new BaseDmxChannel(5, 100);
        assertThat(baseChannel.toString(), is(equalTo("5:100")));

        // single channel from string with universe
        String parseString = new String("2:100");
        List<BaseDmxChannel> channelList = BaseDmxChannel.fromString(parseString, 0);
        assertThat(channelList.size(), is(1));
        assertThat(channelList.get(0).toString(), is(equalTo("2:100")));

        // single channel from string without universe
        parseString = new String("100");
        channelList = BaseDmxChannel.fromString(parseString, 2);
        assertThat(channelList.size(), is(1));
        assertThat(channelList.get(0).toString(), is(equalTo("2:100")));

        // two channels with channel width
        parseString = new String("100/2");
        channelList = BaseDmxChannel.fromString(parseString, 2);
        assertThat(channelList.size(), is(2));
        assertThat(channelList.get(0).toString(), is(equalTo("2:100")));
        assertThat(channelList.get(1).toString(), is(equalTo("2:101")));

        // to channels with comma
        parseString = new String("100,102");
        channelList = BaseDmxChannel.fromString(parseString, 2);
        assertThat(channelList.size(), is(2));
        assertThat(channelList.get(0).toString(), is(equalTo("2:100")));
        assertThat(channelList.get(1).toString(), is(equalTo("2:102")));

        // complex string
        parseString = new String("257,100/3,426");
        channelList = BaseDmxChannel.fromString(parseString, 2);
        assertThat(channelList.size(), is(5));
        assertThat(channelList.get(0).toString(), is(equalTo("2:257")));
        assertThat(channelList.get(1).toString(), is(equalTo("2:100")));
        assertThat(channelList.get(2).toString(), is(equalTo("2:101")));
        assertThat(channelList.get(3).toString(), is(equalTo("2:102")));
        assertThat(channelList.get(4).toString(), is(equalTo("2:426")));
    }
}
