package org.openhab.binding.bosesoundtouch.internal.items;

import org.openhab.binding.bosesoundtouch.types.RadioStationType;

public class Preset {
    public int pos;
    public ContentItem contentItem;

    public boolean posIsValid() {
        return (getPos() >= 1 && getPos() <= 6);
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public RadioStationType getRadioStation() {
        switch (getPos()) {
            case 1:
                return RadioStationType.PRESET_1;
            case 2:
                return RadioStationType.PRESET_2;
            case 3:
                return RadioStationType.PRESET_3;
            case 4:
                return RadioStationType.PRESET_4;
            case 5:
                return RadioStationType.PRESET_5;
            case 6:
                return RadioStationType.PRESET_6;
            default:
                return RadioStationType.UNKNOWN;
        }
    }

}
