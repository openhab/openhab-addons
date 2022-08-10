package org.openhab.binding.liquidcheck.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.liquidcheck.internal.json.Response;

@NonNullByDefault
public class LiquidCheckProperties {
    public final String firmware;
    public final String hardware;
    public final String name;
    public final String manufacturer;
    public final String uuid;
    public final String code;
    public final String ip;
    public final String mac;
    public final String ssid;

    public LiquidCheckProperties(Response response) {
        firmware = response.payload.device.firmware;
        hardware = response.payload.device.hardware;
        name = response.payload.device.name;
        manufacturer = response.payload.device.manufacturer;
        uuid = response.payload.device.uuid;
        code = response.payload.device.security.code;
        ip = response.payload.wifi.station.ip;
        mac = response.payload.wifi.station.mac;
        ssid = response.payload.wifi.accessPoint.ssid;
    }
}
