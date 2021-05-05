package org.openhab.binding.nuki.internal.constants;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Enumeration of all lock actions Nuki Opener accepts
 *
 * @author Jan Vybíral - Initial contribution
 */
public enum OpenerAction {
    ACTIVATE_RING_TO_OPEN(1),
    DEACTIVATE_RING_TO_OPEN(2),
    ELECTRIC_STRIKE_ACTUATION(3),
    ACTIVATE_CONTINUOUS_MODE(4),
    DEACTIVATE_CONTINUOUS_MODE(5);

    private final int action;

    OpenerAction(int action) {
        this.action = action;
    }

    @Nullable
    public static OpenerAction fromAction(int action) {
        for (OpenerAction value : values()) {
            if (value.action == action) {
                return value;
            }
        }
        return null;
    }

    public int getAction() {
        return action;
    }
}
