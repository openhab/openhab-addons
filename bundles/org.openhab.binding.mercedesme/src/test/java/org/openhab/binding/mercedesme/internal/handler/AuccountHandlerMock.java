package org.openhab.binding.mercedesme.internal.handler;

import static org.mockito.Mockito.mock;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jetty.client.HttpClient;
import org.json.JSONObject;
import org.openhab.binding.mercedesme.internal.discovery.MercedesMeDiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;

import com.daimler.mbcarkit.proto.Client.ClientMessage;

public class AuccountHandlerMock extends AccountHandler {
    JSONObject command;

    public AuccountHandlerMock() {
        super(mock(Bridge.class), mock(MercedesMeDiscoveryService.class), mock(HttpClient.class),
                mock(LocaleProvider.class), mock(StorageService.class));
    }

    @Override
    public void registerVin(@NonNull String vin, @NonNull VehicleHandler handler) {
    }

    @Override
    public void getVehicleCapabilities(String vin) {
    }

    @Override
    public void sendCommand(ClientMessage cm) {
        System.out.println(cm.getAllFields());
        command = ProtoConverter.clientMessage2Json(cm);
    }

    public JSONObject getCommand() {
        return command;
    }
}
