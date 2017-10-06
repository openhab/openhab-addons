package org.openhab.binding.blebox.devices;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.blebox.BleboxBindingConstants;

public class LightBoxS extends BaseDevice {

    public static final float MAX_CHANNEL_VALUE = 2.55f;

    public static final String SetUrl = "/api/light/set";
    public static final String StateUrl = "/api/light/state";

    public PercentType LastKnownBrigthness = null;

    public LightBoxS(String ipAddress) {
        super(BleboxBindingConstants.WLIGHTBOXS, ipAddress);
    }

    public class StateResponse extends BaseResponse {
        public static final String RootElement = "light";

        public String currentColor;
        public String desiredColor;

        @Override
        public String getRootElement() {
            return RootElement;
        }

        public PercentType GetWhiteBrightness() {
            return FromHexToPercentType(currentColor);
        }
    }

    public class SetRequest extends BaseRequest {
        public static final String RootElement = "light";

        public String desiredColor;

        @Override
        public String getRootElement() {
            return RootElement;
        }

    }

    public StateResponse GetStatus() {
        StateResponse response = GetJson(StateUrl, StateResponse.class, StateResponse.RootElement);

        return response;
    }

    public void SetWhiteBrightness(PercentType p) {

        SetRequest request = new SetRequest();
        request.desiredColor = PercentToHex(p);

        StateResponse response = PostJson(request, SetUrl, StateResponse.class, StateResponse.RootElement);

        LastKnownBrigthness = p;
    }

    public void SetWhiteBrightness(OnOffType p) {
        if (p == OnOffType.ON) {
            SetWhiteBrightness(PercentType.HUNDRED);
        } else {
            SetWhiteBrightness(PercentType.ZERO);
        }
    }

    public static String PercentToHex(PercentType percent) {
        int p = (int) (percent.doubleValue() * MAX_CHANNEL_VALUE);

        return String.format("%02X", p);
    }

    public static PercentType FromHexToPercentType(String hex) {

        int w = Integer.parseInt(hex, 16);

        return new PercentType(Math.round(w / MAX_CHANNEL_VALUE));
    }

}
