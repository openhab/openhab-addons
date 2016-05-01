package org.openhab.binding.bosesoundtouch.types;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.State;

public enum OperationModeType implements PrimitiveType,State,Command {
    OFFLINE,
    STANDBY,
    PRESET1,
    PRESET2,
    PRESET3,
    PRESET4,
    PRESET5,
    PRESET6,
    INTERNET_RADIO,
    BLUETOOTH,
    AUX,
    MEDIA,
    SPOTIFY,
    PANDORA,
    DEEZER,
    SIRIUSXM,
    OTHER,
    GROUPMEMBER;

    @Override
    public String format(String pattern) {
        return String.format(pattern, this.toString());
    }
}