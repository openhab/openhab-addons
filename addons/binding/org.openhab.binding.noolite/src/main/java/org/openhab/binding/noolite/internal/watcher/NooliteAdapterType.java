package org.openhab.binding.noolite.internal.watcher;

import org.openhab.binding.noolite.internal.NooliteHandlerFactory;

/**
 * The {@link NooliteHandlerFactory} is NooliteAdapterType is enum for adapter types
 *
 * @author Petr Shatsillo - Initial contribution
 */
public enum NooliteAdapterType {
    NOOLIE_TX(0),
    NOOLITE_RX(1),
    NOOLITE_F_TX(2),
    NOOLITE_F_RX(3),
    NOOLITE_F_SERVICE(4),
    NOOLITE_F_UPDATE(5);

    private final int code;

    NooliteAdapterType(int i) {
        this.code = i;
    }

    public int getCode() {
        return code;
    }

    public static NooliteAdapterType getValue(byte value) {
        for (NooliteAdapterType e : NooliteAdapterType.values()) {
            if (e.getCode() == value) {
                return e;
            }
        }
        return null;
    }
}
