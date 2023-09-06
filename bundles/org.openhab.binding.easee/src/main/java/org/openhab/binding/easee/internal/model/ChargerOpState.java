package org.openhab.binding.easee.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public enum ChargerOpState {
    OFFLINE(0),
    DISCONNECTED(1),
    WAITING(2),
    CHARGING(3),
    COMPLETED(4),
    ERROR(5),
    READY_TO_CHARGE(6),
    NOT_AUTHENTICATED(7),
    DEAUTHENTICATING(8),
    UNKNOWN_STATE(-1);

    private final int code;

    private ChargerOpState(int code) {
        this.code = code;
    }

    public boolean isAuthenticatedState() {
        switch (this) {
            case WAITING:
            case CHARGING:
            case COMPLETED:
            case ERROR:
            case READY_TO_CHARGE:
                return true;
            default:
                return false;
        }
    }

    public static ChargerOpState fromCode(String code) {
        return ChargerOpState.fromCode(Integer.parseInt(code));
    }

    public static ChargerOpState fromCode(int code) {
        for (ChargerOpState state : ChargerOpState.values()) {
            if (state.code == code) {
                return state;
            }
        }
        return UNKNOWN_STATE;
    }
}
