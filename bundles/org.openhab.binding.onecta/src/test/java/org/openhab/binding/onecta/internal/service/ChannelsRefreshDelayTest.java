package org.openhab.binding.onecta.internal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.onecta.internal.OnectaWaterTankConstants.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 *
 * @author Alexander Drent - Initial contribution
 *
 */

@ExtendWith(MockitoExtension.class)
public class ChannelsRefreshDelayTest {

    private ChannelsRefreshDelay channelsRefreshDelayMock;

    private List<ChannelsRefreshDelay.ChannelDelay> channelRefreshDelayMock = new ArrayList<>();

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        channelsRefreshDelayMock = new ChannelsRefreshDelay(2000L);

        Field privateChannelsRefreshDelayField = ChannelsRefreshDelay.class.getDeclaredField("channelRefreshDelay");
        privateChannelsRefreshDelayField.setAccessible(true);
        privateChannelsRefreshDelayField.set(channelsRefreshDelayMock, channelRefreshDelayMock);
    }

    @Test
    public void channelsRefreshDelayAddTest() {
        channelsRefreshDelayMock.add(CHANNEL_HWT_POWER);
        channelsRefreshDelayMock.add(CHANNEL_HWT_POWER);
        assertEquals(1, channelRefreshDelayMock.size());

        channelsRefreshDelayMock.add(CHANNEL_HWT_ERRORCODE);
        channelsRefreshDelayMock.add(CHANNEL_HWT_POWER);
        assertEquals(2, channelRefreshDelayMock.size());
    }

    @Test
    public void isDelayPassedTest() throws InterruptedException {
        channelsRefreshDelayMock.add(CHANNEL_HWT_POWER);
        channelsRefreshDelayMock.add(CHANNEL_HWT_POWER);
        channelsRefreshDelayMock.add(CHANNEL_HWT_ERRORCODE);
        assertEquals(false, channelsRefreshDelayMock.isDelayPassed(CHANNEL_HWT_POWER));
        Thread.sleep(1000);
        channelsRefreshDelayMock.add(CHANNEL_HWT_POWER);
        Thread.sleep(500);
        assertEquals(false, channelsRefreshDelayMock.isDelayPassed(CHANNEL_HWT_POWER));

        Thread.sleep(2000);
        assertEquals(true, channelsRefreshDelayMock.isDelayPassed(CHANNEL_HWT_POWER));
        assertEquals(1, channelRefreshDelayMock.size());
        assertEquals(true, channelsRefreshDelayMock.isDelayPassed(CHANNEL_HWT_ERRORCODE));
        assertEquals(0, channelRefreshDelayMock.size());
    }
}
