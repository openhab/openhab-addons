package org.openhab.binding.onecta.internal.handler;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.onecta.internal.OnectaIndoorUnitConstants.*;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.onecta.internal.service.DataTransportService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.UnDefType;

@ExtendWith(MockitoExtension.class)
public class OnectaIndoorUnitHandlerTest {

    private OnectaIndoorUnitHandler handler;

    @Mock
    private ThingHandlerCallback callbackMock;

    @Mock
    private Thing thingMock;

    @Mock
    private DataTransportService dataTransServiceMock;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.setProperties(Map.of("unitID", "ThisIsDummyID", "refreshDelay", "10"));
        when(thingMock.getConfiguration()).thenReturn(thingConfiguration);
        handler = new OnectaIndoorUnitHandler(thingMock);
        handler.setCallback(callbackMock);

        // add Mock dataTransServiceMock to handler
        Field privateDataTransServiceField = OnectaIndoorUnitHandler.class.getDeclaredField("dataTransService");
        privateDataTransServiceField.setAccessible(true);
        privateDataTransServiceField.set(handler, dataTransServiceMock);

        lenient().when(thingMock.getUID()).thenReturn(new ThingUID("onecta", "indoorUnit", "bridge"));
    }

    @AfterEach
    public void tearDown() {
        // Free any resources, like open database connections, files etc.
        handler.dispose();
    }

    @Test
    public void initializeShouldCallTheCallback() {
        // we expect the handler#initialize method to call the callbackMock during execution and
        // pass it the thingMock and a ThingStatusInfo object containing the ThingStatus of the thingMock.
        handler.initialize();
        verify(callbackMock).statusUpdated(eq(thingMock), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
    }

    @Test
    public void refreshDeviceNotAvailTest() {
        when(dataTransServiceMock.isAvailable()).thenReturn(false);
        handler.refreshDevice();
        verify(callbackMock).statusUpdated(eq(thingMock), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
    }

    @Test
    public void refreshDeviceOkTest() {

        when(dataTransServiceMock.isAvailable()).thenReturn(true);

        when(dataTransServiceMock.getModelInfo()).thenReturn("DaikinModel");
        when(dataTransServiceMock.getSoftwareVersion()).thenReturn("1-2-3");
        when(dataTransServiceMock.getEepromVerion()).thenReturn("2012351");
        when(dataTransServiceMock.getDryKeepSetting()).thenReturn("ON");
        when(dataTransServiceMock.getFanMotorRotationSpeed()).thenReturn(19.2);
        when(dataTransServiceMock.getDeltaD()).thenReturn(20.2);
        when(dataTransServiceMock.getHeatExchangerTemperature()).thenReturn(21.2);
        when(dataTransServiceMock.getSuctionTemperature()).thenReturn(22.2);

        handler.refreshDevice();

        verify(callbackMock, times(0)).statusUpdated(eq(thingMock),
                argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));

        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_MODELINFO),
                new StringType("DaikinModel"));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_SOFTWAREVERSION),
                new StringType("1-2-3"));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_EEPROMVERSION),
                new StringType("2012351"));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_ISKEEPDRY),
                OnOffType.from("ON"));

        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_FANSPEED),
                new DecimalType(19.2));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_DELTAD),
                new DecimalType(20.2));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_HEATEXCHANGETEMP),
                new DecimalType(21.2));
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_SUCTIONTEMP),
                new DecimalType(22.2));
    }

    @Test
    public void refreshDeviceUndefTest() {
        when(dataTransServiceMock.isAvailable()).thenReturn(true);

        lenient().when(dataTransServiceMock.getModelInfo()).thenThrow(new RuntimeException("Simulating exception"));
        lenient().when(dataTransServiceMock.getSoftwareVersion())
                .thenThrow(new RuntimeException("Simulating exception"));
        lenient().when(dataTransServiceMock.getEepromVerion()).thenThrow(new RuntimeException("Simulating exception"));
        lenient().when(dataTransServiceMock.getDryKeepSetting())
                .thenThrow(new RuntimeException("Simulating exception"));
        lenient().when(dataTransServiceMock.getFanMotorRotationSpeed())
                .thenThrow(new RuntimeException("Simulating exception"));
        lenient().when(dataTransServiceMock.getDeltaD()).thenThrow(new RuntimeException("Simulating exception"));
        lenient().when(dataTransServiceMock.getHeatExchangerTemperature())
                .thenThrow(new RuntimeException("Simulating exception"));
        lenient().when(dataTransServiceMock.getSuctionTemperature())
                .thenThrow(new RuntimeException("Simulating exception"));

        handler.refreshDevice();

        verify(callbackMock, times(0)).statusUpdated(eq(thingMock),
                argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));

        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_MODELINFO),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_SOFTWAREVERSION),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_EEPROMVERSION),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_ISKEEPDRY),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_FANSPEED),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_DELTAD),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_HEATEXCHANGETEMP),
                UnDefType.UNDEF);
        verify(callbackMock, times(1)).stateUpdated(new ChannelUID(thingMock.getUID(), CHANNEL_IDU_SUCTIONTEMP),
                UnDefType.UNDEF);
    }
}
