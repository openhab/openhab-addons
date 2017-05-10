package org.openhab.binding.robonect.handler;

import java.util.Calendar;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.robonect.RobonectBindingConstants;
import org.openhab.binding.robonect.RobonectClient;
import org.openhab.binding.robonect.model.MowerInfo;
import org.openhab.binding.robonect.model.MowerMode;
import org.openhab.binding.robonect.model.MowerStatus;
import org.openhab.binding.robonect.model.NextTimer;
import org.openhab.binding.robonect.model.Status;
import org.openhab.binding.robonect.model.Timer;
import org.openhab.binding.robonect.model.Wlan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RobonectHandlerTest {

    @Mock
    private RobonectHandler testObj;

    @Mock
    private Thing robonectThingMock;

    @Mock
    private RobonectClient robonectClientMock;

    @Mock
    private ThingHandlerCallback callbackMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        testObj = new RobonectHandler(robonectThingMock);
        testObj.setCallback(callbackMock);
        testObj.setRobonectClient(robonectClientMock);
    }

    @Test
    public void shouldUpdateNextTimerChannelWithDateTimeState() {
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);

        // given
        MowerInfo mowerInfo = createSuccessfulMowerInfoResponse();
        Timer timer = new Timer();
        timer.setStatus(Timer.TimerMode.ACTIVE);
        NextTimer nextTimer = new NextTimer();
        nextTimer.setDate("01.05.2017");
        nextTimer.setTime("19:00:00");
        nextTimer.setUnix("1493665200");
        timer.setNext(nextTimer);

        // when
        when(robonectClientMock.getMowerInfo()).thenReturn(mowerInfo);
        when(robonectThingMock.getUID()).thenReturn(new ThingUID("1:2:3"));

        testObj.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_TIMER_NEXT_TIMER),
            RefreshType.REFRESH);

        // then
        verify(callbackMock, times(1)).stateUpdated(
            eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_TIMER_NEXT_TIMER)),
            stateCaptor.capture());

        State value = stateCaptor.getValue();
        assertTrue(value instanceof DateTimeType);
        DateTimeType dateTimeType = (DateTimeType) value;
        assertEquals(1, dateTimeType.getCalendar().get(Calendar.DAY_OF_MONTH));

        assertEquals(2017, dateTimeType.getCalendar().get(Calendar.YEAR));
        // calendar january is 0
        assertEquals(4, dateTimeType.getCalendar().get(Calendar.MONTH));
        assertEquals(19, dateTimeType.getCalendar().get(Calendar.HOUR_OF_DAY));
        assertEquals(0, dateTimeType.getCalendar().get(Calendar.MINUTE));
        assertEquals(0, dateTimeType.getCalendar().get(Calendar.SECOND));
    }

    @Test
    public void shouldUpdateNumericStateOnMowerStatusRefresh() {
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);

        // given
        MowerInfo mowerInfo = createSuccessfulMowerInfoResponse();
        mowerInfo.getStatus().setStatus(MowerStatus.MOWING);

        // when
        when(robonectClientMock.getMowerInfo()).thenReturn(mowerInfo);
        when(robonectThingMock.getUID()).thenReturn(new ThingUID("1:2:3"));

        testObj.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS),
            RefreshType.REFRESH);

        // then
        verify(callbackMock, times(1)).stateUpdated(eq(
                new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS)),
            stateCaptor.capture());

        State value = stateCaptor.getValue();
        assertTrue(value instanceof DecimalType);
        DecimalType status = (DecimalType) value;

        assertEquals(MowerStatus.MOWING.getStatusCode(), status.intValue());

    }

    private MowerInfo createSuccessfulMowerInfoResponse() {
        MowerInfo mowerInfo = new MowerInfo();
        Timer timer = new Timer();
        timer.setStatus(Timer.TimerMode.ACTIVE);
        NextTimer nextTimer = new NextTimer();
        nextTimer.setDate("01.05.2017");
        nextTimer.setTime("19:00:00");
        nextTimer.setUnix("1493665200");
        timer.setNext(nextTimer);
        mowerInfo.setTimer(timer);
        Status status = new Status();
        status.setBattery(99);
        status.setDuration(55);
        status.setHours(22);
        status.setMode(MowerMode.AUTO);
        status.setStatus(MowerStatus.CHARGING);
        mowerInfo.setStatus(status);
        mowerInfo.setName("Mowy");
        Wlan wlan = new Wlan();
        wlan.setSignal(-88);
        mowerInfo.setWlan(wlan);
        mowerInfo.setSuccessful(true);
        return mowerInfo;
    }
}
