package org.openhab.binding.liqiudcheck.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

    public LiquidCheckProperties(String response) {
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        JsonObject payload = jsonObject.getAsJsonObject("payload");
        JsonObject device = payload.getAsJsonObject("device");
        firmware = device.get("firmware").getAsString();
        hardware = device.get("hardware").getAsString();
        name = device.get("name").getAsString();
        manufacturer = device.get("manufacturer").getAsString();
        uuid = device.get("uuid").getAsString();
        JsonObject security = device.getAsJsonObject("security");
        code = security.get("code").getAsString();
        JsonObject wifi = payload.getAsJsonObject("wifi");
        JsonObject station = wifi.getAsJsonObject("station");
        ip = station.get("ip").getAsString();
        mac = station.get("mac").getAsString();
        JsonObject accessPoint = wifi.getAsJsonObject("accessPoint");
        ssid = accessPoint.get("ssid").getAsString();
    }
}
