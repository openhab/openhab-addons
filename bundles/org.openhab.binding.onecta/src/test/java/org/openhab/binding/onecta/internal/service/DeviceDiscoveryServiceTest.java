package org.openhab.binding.onecta.internal.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.onecta.internal.OnectaBridgeConstants.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.onecta.internal.api.OnectaConnectionClient;
import org.openhab.binding.onecta.internal.api.dto.units.Unit;
import org.openhab.binding.onecta.internal.api.dto.units.Units;
import org.openhab.binding.onecta.internal.exception.DaikinCommunicationException;
import org.openhab.binding.onecta.internal.handler.OnectaBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

/**
 *
 * @author Alexander Drent - Initial contribution
 *
 */

@ExtendWith(MockitoExtension.class)
public class DeviceDiscoveryServiceTest {

    private DeviceDiscoveryService deviceDiscoveryService;

    private String jsonString;
    private static JsonArray rawData = new JsonArray();
    private static Units onectaData = new Units();

    @Mock
    private OnectaBridgeHandler bridgeHandler;

    @Mock
    private Bridge bridgeMock;

    @Mock
    private OnectaConnectionClient onectaConnectionClientMock;

    @Mock
    private ThingUID thingUIDMock;

    @Mock
    private DiscoveryListener mockDiscoveryListener;

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException, IOException {

        jsonString = new String(Files.readAllBytes(Paths.get("src/test/resources/aircoUnits.json")),
                StandardCharsets.UTF_8);
        rawData = JsonParser.parseString(jsonString).getAsJsonArray();
        onectaData.getAll().clear();
        for (int i = 0; i < rawData.size(); i++) {
            onectaData.getAll()
                    .add(Objects.requireNonNull(new Gson().fromJson(rawData.get(i).getAsJsonObject(), Unit.class)));
        }

        deviceDiscoveryService = new DeviceDiscoveryService(bridgeHandler);
        when(bridgeHandler.getThing()).thenReturn(bridgeMock);
        when(bridgeHandler.getThing().getStatus()).thenReturn(ThingStatus.ONLINE);
        when(bridgeHandler.getThing().getUID()).thenReturn(thingUIDMock);
        when(bridgeHandler.getThing().getUID().getId()).thenReturn("Fiets");
        when(bridgeHandler.getThing().getUID().getBindingId()).thenReturn("onecta");

        Field privateField = DeviceDiscoveryService.class.getDeclaredField("onectaConnectionClient");
        privateField.setAccessible(true);
        privateField.set(deviceDiscoveryService, onectaConnectionClientMock);

        deviceDiscoveryService.addDiscoveryListener(mockDiscoveryListener);
    }

    @Test
    public void startScanTest() throws DaikinCommunicationException {

        doNothing().when(onectaConnectionClientMock).refreshUnitsData(any(Thing.class));
        when(onectaConnectionClientMock.getUnits()).thenReturn(onectaData);
        deviceDiscoveryService.startScan();

        verify(bridgeHandler.getThing()).setProperty("Discovered climateControl (Kantoor Jeanette)",
                "1ce8c13f-5271-4343-ac9f-a1b2c3d4e5f6");
        verify(bridgeHandler.getThing()).setProperty("Discovered climateControl (Slaapkamer J & A)",
                "5e41c4af-a5b8-4175-ac76-a1b2c3d4e5f6");
        verify(bridgeHandler.getThing()).setProperty("Discovered climateControl (Slaapkamer Tijn)",
                "6c51835b-95ea-4e6e-9619-a1b2c3d4e5f6");
        verify(bridgeHandler.getThing()).setProperty("Discovered climateControl (Woonkamer)",
                "80100dc5-a289-47c1-bbdb-a1b2c3d4e5f6");
        verify(bridgeHandler.getThing()).setProperty("Discovered climateControl (Slaapkamer Sophie)",
                "e8776702-47bd-4486-a6bd-a1b2c3d4e5f6");
        verify(bridgeHandler.getThing()).setProperty("Discovered climateControl (c9cd8376-a32d-423b-acff-a1b2c3d4e5f6)",
                "c9cd8376-a32d-423b-acff-a1b2c3d4e5f6");

        verify(bridgeHandler.getThing()).setProperty("Discovered gateway (Kantoor Jeanette)",
                "1ce8c13f-5271-4343-ac9f-a1b2c3d4e5f6");
        verify(bridgeHandler.getThing()).setProperty("Discovered gateway (Slaapkamer J & A)",
                "5e41c4af-a5b8-4175-ac76-a1b2c3d4e5f6");
        verify(bridgeHandler.getThing()).setProperty("Discovered gateway (Slaapkamer Tijn)",
                "6c51835b-95ea-4e6e-9619-a1b2c3d4e5f6");
        verify(bridgeHandler.getThing()).setProperty("Discovered gateway (Woonkamer)",
                "80100dc5-a289-47c1-bbdb-a1b2c3d4e5f6");
        verify(bridgeHandler.getThing()).setProperty("Discovered gateway (Slaapkamer Sophie)",
                "e8776702-47bd-4486-a6bd-a1b2c3d4e5f6");
        verify(bridgeHandler.getThing()).setProperty("Discovered gateway (c9cd8376-a32d-423b-acff-a1b2c3d4e5f6)",
                "c9cd8376-a32d-423b-acff-a1b2c3d4e5f6");

        verify(bridgeHandler.getThing()).setProperty(
                "Discovered domesticHotWaterTank (c9cd8376-a32d-423b-acff-a1b2c3d4e5f6)",
                "c9cd8376-a32d-423b-acff-a1b2c3d4e5f6");

        verify(bridgeHandler.getThing()).setProperty("Discovered indoorUnit (Kantoor Jeanette)",
                "1ce8c13f-5271-4343-ac9f-a1b2c3d4e5f6");
        verify(bridgeHandler.getThing()).setProperty("Discovered indoorUnit (Slaapkamer J & A)",
                "5e41c4af-a5b8-4175-ac76-a1b2c3d4e5f6");
        verify(bridgeHandler.getThing()).setProperty("Discovered indoorUnit (Slaapkamer Tijn)",
                "6c51835b-95ea-4e6e-9619-a1b2c3d4e5f6");
        verify(bridgeHandler.getThing()).setProperty("Discovered indoorUnit (Woonkamer)",
                "80100dc5-a289-47c1-bbdb-a1b2c3d4e5f6");
        verify(bridgeHandler.getThing()).setProperty("Discovered indoorUnit (Slaapkamer Sophie)",
                "e8776702-47bd-4486-a6bd-a1b2c3d4e5f6");

        verify(mockDiscoveryListener, times(6)).thingDiscovered(ArgumentMatchers.same(deviceDiscoveryService),
                ArgumentMatchers.argThat(arg -> arg.getThingTypeUID().equals(DEVICE_THING_TYPE)));
        verify(mockDiscoveryListener, times(6)).thingDiscovered(ArgumentMatchers.same(deviceDiscoveryService),
                ArgumentMatchers.argThat(arg -> arg.getThingTypeUID().equals(GATEWAY_THING_TYPE)));
        verify(mockDiscoveryListener, times(1)).thingDiscovered(ArgumentMatchers.same(deviceDiscoveryService),
                ArgumentMatchers.argThat(arg -> arg.getThingTypeUID().equals(WATERTANK_THING_TYPE)));
        verify(mockDiscoveryListener, times(5)).thingDiscovered(ArgumentMatchers.same(deviceDiscoveryService),
                ArgumentMatchers.argThat(arg -> arg.getThingTypeUID().equals(INDOORUNIT_THING_TYPE)));
    }
}
