package org.openhab.binding.bosesoundtouch.types;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.State;

public enum OperationModeType implements PrimitiveType, State, Command {
    OFFLINE,
    STANDBY,
    INTERNET_RADIO,
    BLUETOOTH,
    AUX,
    MEDIA,
    SPOTIFY,
    PANDORA,
    DEEZER,
    SIRIUSXM,
    STORED_MUSIC,
    GROUPMEMBER,
    OTHER;

    @Override
    public String format(String pattern) {
        return String.format(pattern, this.toString());
    }

    @Override
    public String toFullString() {
        return super.toString();
    }
}