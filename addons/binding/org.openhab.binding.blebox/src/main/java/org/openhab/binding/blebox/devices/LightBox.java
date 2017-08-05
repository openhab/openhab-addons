package org.openhab.binding.blebox.devices;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.blebox.BleboxBindingConstants;

public class LightBox extends BaseDevice {

    public static final float MAX_CHANNEL_VALUE = 2.55f;

    public static final String SetUrl = "/api/rgbw/set";
    public static final String StateUrl = "/api/rgbw/state";

    public HSBType LastKnownColor = null;
    public PercentType LastKnownBrigthness = null;

    public LightBox(String ipAddress) {
        super(BleboxBindingConstants.WLIGHTBOX, ipAddress);
    }

    public LightBox(String itemType, String ipAddress) {
        super(itemType, ipAddress);
    }

    public class StateResponse extends BaseResponse {
        public static final String RootElement = "rgbw";

        public String currentColor;
        public String desiredColor;

        public int effectID;

        @Override
        public String getRootElement() {
            return RootElement;
        }

        public HSBType GetColor() {
            return FromHexToHSB(currentColor);
        }

        public PercentType GetWhiteBrightness() {
            return FromHexToPercentType(currentColor.substring(6, 8));
        }
    }

    public class SetRequest extends BaseRequest {
        public static final String RootElement = "rgbw";

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

    public void SetColor(HSBType hsb) {
        if (LastKnownBrigthness == null) {
            LastKnownBrigthness = PercentType.ZERO;
        }

        SetRequest request = new SetRequest();
        request.desiredColor = FromHSBToHex(hsb) + PercentToHex(LastKnownBrigthness);

        StateResponse response = PostJson(request, SetUrl, StateResponse.class, StateResponse.RootElement);

        LastKnownColor = hsb;
    }

    public void SetWhiteBrightness(PercentType p) {
        if (LastKnownColor == null) {
            LastKnownColor = HSBType.BLACK;
        }

        SetRequest request = new SetRequest();
        request.desiredColor = FromHSBToHex(LastKnownColor) + PercentToHex(p);

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

    public static String FromHSBToHex(HSBType hsb) {
        int r = Math.round(hsb.getRed().intValue() * MAX_CHANNEL_VALUE);
        int g = Math.round(hsb.getGreen().intValue() * MAX_CHANNEL_VALUE);
        int b = Math.round(hsb.getBlue().intValue() * MAX_CHANNEL_VALUE);

        String toRet = String.format("%02X", r) + String.format("%02X", g) + String.format("%02X", b);

        return toRet;
    }

    public static HSBType FromHexToHSB(String color) {

        int r = Integer.parseInt(color.substring(0, 2), 16);
        int g = Integer.parseInt(color.substring(2, 4), 16);
        int b = Integer.parseInt(color.substring(4, 6), 16);
        int w = Integer.parseInt(color.substring(4, 6), 16);

        return HSBType.fromRGB(r, g, b);
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
