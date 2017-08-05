package org.openhab.binding.blebox.devices;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.blebox.BleboxBindingConstants;

public class SwitchBox extends BaseDevice {
    public static final String SetUrl = "/s/";
    public static final String StateUrl = "/api/relay/state";

    public SwitchBox(String ipAddress) {
        super(BleboxBindingConstants.SWITCHBOX, ipAddress);
    }

    public SwitchBox(String itemType, String ipAddress) {
        super(itemType, ipAddress);
    }

    public class StateResponse extends BaseResponse {

        public Relay[] relays;

        @Override
        public String getRootElement() {
            return null;
        }

    }

    public class Relay extends BaseResponse {
        public int relay;
        public int state;
        public int stateAfterRestart;

        @Override
        public String getRootElement() {
            return null;
        }
    }

    public void SetSwitchState(OnOffType onOff) {
        String url = SetUrl + (onOff.equals(OnOffType.ON) ? "1" : "0");

        GetJson(url, StateResponse.class, null);
    }

    public OnOffType GetSwitchState(int switchIndex) {
        Relay[] response = GetJsonArray(StateUrl, Relay[].class, null);

        if (response != null && response.length > switchIndex) {
            return response[switchIndex].state > 0 ? OnOffType.ON : OnOffType.OFF;
        }

        return null;
    }
}
