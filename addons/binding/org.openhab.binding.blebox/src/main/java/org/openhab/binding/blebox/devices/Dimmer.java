package org.openhab.binding.blebox.devices;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.openhab.binding.blebox.BleboxBindingConstants;

public class Dimmer extends BaseDevice {

    public static final float MaxBrightness = 255;

    public static final String SetUrl = "/api/dimmer/set";
    public static final String StateUrl = "/api/dimmer/state";

    public Dimmer(String ipAddress) {
        super(BleboxBindingConstants.DIMMERBOX, ipAddress);
    }

    public class SetRequest extends BaseRequest {
        public static final String RootElement = "dimmer";

        public Integer loadType;
        public Integer desiredBrightness;
        public Boolean overloaded;
        public Boolean overheated;

        public SetRequest setBrightness(Integer value) {
            desiredBrightness = value;

            return this;
        }

        @Override
        public String getRootElement() {
            return RootElement;
        }

    }

    public class StateResponse extends BaseResponse {
        public static final String RootElement = "dimmer";

        public Integer loadType;
        public Integer currentBrightness;
        public Integer desiredBrightness;
        public Integer temperature;
        public Boolean overloaded;
        public Boolean overheated;

        @Override
        public String getRootElement() {
            return RootElement;
        }

    }

    public void SetBrightness(Integer value) {
        SetRequest request = new SetRequest().setBrightness(value);

        StateResponse response = PostJson(request, SetUrl, StateResponse.class, StateResponse.RootElement);

    }

    public void SetBrightness(PercentType percent) {
        int value = Math.round((255 * percent.floatValue()) / 100);

        SetBrightness(value);
    }

    public PercentType GetBrightness() {
        StateResponse response = GetJson(StateUrl, StateResponse.class, StateResponse.RootElement);

        if (response != null) {

            float percent = response.currentBrightness / MaxBrightness;

            return new PercentType(Math.round(percent * 100));

        } else {
            return null;
        }
    }
}
