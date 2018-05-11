/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.handler;

import java.util.Calendar;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.robonect.RobonectBindingConstants;
import org.openhab.binding.robonect.internal.RobonectClient;
import org.openhab.binding.robonect.handler.RobonectHandler;
import org.openhab.binding.robonect.internal.model.ErrorEntry;
import org.openhab.binding.robonect.internal.model.ErrorList;
import org.openhab.binding.robonect.internal.model.MowerInfo;
import org.openhab.binding.robonect.internal.model.MowerMode;
import org.openhab.binding.robonect.internal.model.MowerStatus;
import org.openhab.binding.robonect.internal.model.NextTimer;
import org.openhab.binding.robonect.internal.model.Status;
import org.openhab.binding.robonect.internal.model.Timer;
import org.openhab.binding.robonect.internal.model.Wlan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The goal of this class is to test RobonectHandler in isolation.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class RobonectHandlerTest {
    
    private RobonectHandler subject;

    @Mock
    private Thing robonectThingMock;

    @Mock
    private RobonectClient robonectClientMock;

    @Mock
    private ThingHandlerCallback callbackMock;
    
    @Mock
    private HttpClient httpClientMock;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        subject = new RobonectHandler(robonectThingMock, httpClientMock);
        subject.setCallback(callbackMock);
        subject.setRobonectClient(robonectClientMock);
    }

    @Test
    public void shouldUpdateNextTimerChannelWithDateTimeState() throws InterruptedException {
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

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_TIMER_NEXT_TIMER),
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
    public void shouldUpdateErrorChannelsIfErrorStatusReturned() throws InterruptedException {
        ArgumentCaptor<State> errorCodeCaptor = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> errorMessageCaptor = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> errorDateCaptor = ArgumentCaptor.forClass(State.class);

        // given
        MowerInfo mowerInfo = createSuccessfulMowerInfoResponse();
        ErrorEntry error = new ErrorEntry();
        error.setDate("01.05.2017");
        error.setTime("19:00:00");
        error.setUnix("1493665200");
        error.setErrorCode(new Integer(22));
        error.setErrorMessage("Dummy Message");
        mowerInfo.getStatus().setStatus(MowerStatus.ERROR_STATUS);
        mowerInfo.setError(error);
        ErrorList errorList = new ErrorList();
        errorList.setSuccessful(true);

        // when
        when(robonectClientMock.getMowerInfo()).thenReturn(mowerInfo);
        when(robonectClientMock.errorList()).thenReturn(errorList);
        when(robonectThingMock.getUID()).thenReturn(new ThingUID("1:2:3"));

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS),
                              RefreshType.REFRESH);

        // then
        verify(callbackMock, times(1))
                .stateUpdated(eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_ERROR_CODE)),
                        errorCodeCaptor.capture());
        verify(callbackMock, times(1))
                .stateUpdated(eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_ERROR_MESSAGE)),
                        errorMessageCaptor.capture());
        verify(callbackMock, times(1))
                .stateUpdated(eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_ERROR_DATE)),
                        errorDateCaptor.capture());

        State errorDate = errorDateCaptor.getValue();
        assertTrue(errorDate instanceof DateTimeType);
        DateTimeType dateTimeType = (DateTimeType) errorDate;
        assertEquals(1, dateTimeType.getCalendar().get(Calendar.DAY_OF_MONTH));
        assertEquals(2017, dateTimeType.getCalendar().get(Calendar.YEAR));
        // calendar january is 0
        assertEquals(4, dateTimeType.getCalendar().get(Calendar.MONTH));
        assertEquals(19, dateTimeType.getCalendar().get(Calendar.HOUR_OF_DAY));
        assertEquals(0, dateTimeType.getCalendar().get(Calendar.MINUTE));
        assertEquals(0, dateTimeType.getCalendar().get(Calendar.SECOND));

        State errorMessage = errorMessageCaptor.getValue();
        assertTrue(errorMessage instanceof StringType);
        StringType msgStringType = (StringType) errorMessage;
        assertEquals("Dummy Message", msgStringType.toFullString());

        State errorCode = errorCodeCaptor.getValue();
        assertTrue(errorCode instanceof DecimalType);
        DecimalType codeDecimaltype = (DecimalType) errorCode;
        assertEquals(22, codeDecimaltype.intValue());
    }

    @Test
    public void shouldResetErrorStateIfNoErrorInStatusUpdate() throws InterruptedException {
        ArgumentCaptor<State> errorCodeCaptor = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> errorMessageCaptor = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> errorDateCaptor = ArgumentCaptor.forClass(State.class);

        // given
        MowerInfo mowerInfo = createSuccessfulMowerInfoResponse();
        mowerInfo.getStatus().setStatus(MowerStatus.MOWING);
        mowerInfo.setError(null);

        // when
        when(robonectClientMock.getMowerInfo()).thenReturn(mowerInfo);
        when(robonectThingMock.getUID()).thenReturn(new ThingUID("1:2:3"));

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS),
                              RefreshType.REFRESH);

        // then
        verify(callbackMock, times(1))
                .stateUpdated(eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_ERROR_CODE)),
                        errorCodeCaptor.capture());
        verify(callbackMock, times(1))
                .stateUpdated(eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_ERROR_MESSAGE)),
                        errorMessageCaptor.capture());
        verify(callbackMock, times(1))
                .stateUpdated(eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_ERROR_DATE)),
                        errorDateCaptor.capture());

        assertEquals(errorCodeCaptor.getValue(), UnDefType.UNDEF);
        assertEquals(errorMessageCaptor.getValue(), UnDefType.UNDEF);
        assertEquals(errorDateCaptor.getValue(), UnDefType.UNDEF);
    }

    @Test
    public void shouldUpdateNumericStateOnMowerStatusRefresh() throws InterruptedException {
        ArgumentCaptor<State> stateCaptor = ArgumentCaptor.forClass(State.class);

        // given
        MowerInfo mowerInfo = createSuccessfulMowerInfoResponse();
        mowerInfo.getStatus().setStatus(MowerStatus.MOWING);

        // when
        when(robonectClientMock.getMowerInfo()).thenReturn(mowerInfo);
        when(robonectThingMock.getUID()).thenReturn(new ThingUID("1:2:3"));

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS),
                              RefreshType.REFRESH);

        // then
        verify(callbackMock, times(1))
                .stateUpdated(eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS)),
                        stateCaptor.capture());

        State value = stateCaptor.getValue();
        assertTrue(value instanceof DecimalType);
        DecimalType status = (DecimalType) value;

        assertEquals(MowerStatus.MOWING.getStatusCode(), status.intValue());

    }

    @Test
    public void shouldUpdateAllChannels() {
        ArgumentCaptor<State> stateCaptorName = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> stateCaptorBattery = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> stateCaptorStatus = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> stateCaptorDuration = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> stateCaptorHours = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> stateCaptorMode = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> stateCaptorStarted = ArgumentCaptor.forClass(State.class);
        ArgumentCaptor<State> stateCaptorWlan = ArgumentCaptor.forClass(State.class);

        // given
        MowerInfo mowerInfo = createSuccessfulMowerInfoResponse();
        ErrorList errorList = new ErrorList();
        errorList.setSuccessful(true);
        
        // when
        when(robonectClientMock.getMowerInfo()).thenReturn(mowerInfo);
        when(robonectClientMock.errorList()).thenReturn(errorList);
        when(robonectThingMock.getUID()).thenReturn(new ThingUID("1:2:3"));

        subject.handleCommand(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS),
                              RefreshType.REFRESH);
        
        // then
        verify(callbackMock, times(1))
                .stateUpdated(eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_MOWER_NAME)),
                        stateCaptorName.capture());
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS_BATTERY)),
                stateCaptorBattery.capture());
        verify(callbackMock, times(1))
                .stateUpdated(eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS)),
                        stateCaptorStatus.capture());
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS_DURATION)),
                stateCaptorDuration.capture());
        verify(callbackMock, times(1))
                .stateUpdated(eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS_HOURS)),
                        stateCaptorHours.capture());
        verify(callbackMock, times(1))
                .stateUpdated(eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_STATUS_MODE)),
                        stateCaptorMode.capture());
        verify(callbackMock, times(1)).stateUpdated(
                eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_MOWER_START)),
                stateCaptorStarted.capture());
        verify(callbackMock, times(1))
                .stateUpdated(eq(new ChannelUID(new ThingUID("1:2:3"), RobonectBindingConstants.CHANNEL_WLAN_SIGNAL)),
                        stateCaptorWlan.capture());

        assertEquals("Mowy", stateCaptorName.getValue().toFullString());
        assertEquals(99, ((DecimalType)stateCaptorBattery.getValue()).intValue());
        assertEquals(4, ((DecimalType)stateCaptorStatus.getValue()).intValue());
        assertEquals(55, ((QuantityType)stateCaptorDuration.getValue()).intValue());
        assertEquals(22, ((QuantityType)stateCaptorHours.getValue()).intValue());
        assertEquals(MowerMode.AUTO.name(), stateCaptorMode.getValue().toFullString());
        assertEquals(OnOffType.ON, stateCaptorStarted.getValue());
        assertEquals(-88, ((DecimalType)stateCaptorWlan.getValue()).intValue());
        
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
        mowerInfo.getStatus().setStopped(false);
        return mowerInfo;
    }
}
