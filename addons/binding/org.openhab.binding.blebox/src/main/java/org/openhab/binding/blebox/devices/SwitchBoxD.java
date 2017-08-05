package org.openhab.binding.blebox.devices;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.blebox.BleboxBindingConstants;

public class SwitchBoxD extends SwitchBox {
    public static final String ResponseRoot = "relays";

    public SwitchBoxD(String ipAddress) {
        super(BleboxBindingConstants.SWITCHBOXD, ipAddress);
    }

    public void SetSwitchState(int switchIndex, OnOffType onOff) {
        String url = SetUrl + switchIndex + "/" + (onOff.equals(OnOffType.ON) ? "1" : "0");

        GetJson(url, StateResponse.class, ResponseRoot);
    }

    public OnOffType[] GetSwitchesState() {
        Relay[] response = GetJsonArray(StateUrl, Relay[].class, ResponseRoot);

        OnOffType[] result = new OnOffType[2];

        if (response != null && response.length == 2) {
            result[0] = response[0].state > 0 ? OnOffType.ON : OnOffType.OFF;
            result[1] = response[1].state > 0 ? OnOffType.ON : OnOffType.OFF;

            return result;
        }

        return null;
    }

}
