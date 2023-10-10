package org.openhab.binding.hue.internal.dto.clip2;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hue.internal.dto.clip2.enums.TamperStateType;

/**
 * DTO for CLIP 2 home security tamper switch.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class TamperReport {

    private @NonNullByDefault({}) Instant changed;
    private @NonNullByDefault({}) String state;

    public Instant getLastChanged() {
        return changed;
    }

    public TamperStateType getTamperState() throws IllegalArgumentException {
        return TamperStateType.valueOf(state.toUpperCase());
    }

    public TamperReport setLastChanged(Instant changed) {
        this.changed = changed;
        return this;
    }

    public TamperReport setTamperState(String state) {
        this.state = state;
        return this;
    }
}
