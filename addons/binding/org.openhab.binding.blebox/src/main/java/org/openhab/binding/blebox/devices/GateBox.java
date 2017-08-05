package org.openhab.binding.blebox.devices;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.blebox.BleboxBindingConstants;

public class GateBox extends BaseDevice {

    public static final float MAX_CHANNEL_VALUE = 2.55f;

    public static final String SetUrl = "/s/p";
    public static final String StateUrl = "/api/gate/state";

    public GateBox(String ipAddress) {
        super(BleboxBindingConstants.GATEBOX, ipAddress);
    }

    public GateBox(String itemType, String ipAddress) {
        super(itemType, ipAddress);
    }

    public class StateResponse extends BaseResponse {

        public String currentPos;
        public String desiredPos;

        @Override
        public String getRootElement() {
            return null;
        }

        public PercentType GetPosition() {
            return PercentType.valueOf(currentPos);
        }

    }

    public class SetRequest extends BaseRequest {

        public String desiredPos;

        @Override
        public String getRootElement() {
            return "gate";
        }

    }

    public StateResponse GetStatus() {
        StateResponse response = GetJson(StateUrl, StateResponse.class, null);

        return response;
    }

    public void SetPosition(PercentType p) {
        // if (LastKnownColor == null) {
        // LastKnownColor = HSBType.BLACK;
        // }
        //
        String url = SetUrl;

        GetJson(url, StateResponse.class, "gate");

    }

    public void SetState(OnOffType p) {
        if (p == OnOffType.ON) {
            SetPosition(PercentType.HUNDRED);
        } else {
            SetPosition(PercentType.ZERO);
        }
    }

}
