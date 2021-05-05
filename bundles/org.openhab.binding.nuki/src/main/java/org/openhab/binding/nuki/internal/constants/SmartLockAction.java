package org.openhab.binding.nuki.internal.constants;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Enumeration of all lock actions Nuki Smart Lock accepts
 *
 * @author Jan Vybíral - Initial contribution
 */
public enum SmartLockAction {
    UNLOCK(1),
    LOCK(2),
    UNLATCH(3),
    LOCK_N_GO(4),
    LOCK_N_GO_WITH_UNLATCH(5);

    private final int action;

    SmartLockAction(int action) {
        this.action = action;
    }

    @Nullable
    public static SmartLockAction fromAction(int action) {
        for (SmartLockAction value : values()) {
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
