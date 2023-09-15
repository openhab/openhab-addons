package org.openhab.binding.hue.internal.dto.clip2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hue.internal.dto.clip2.enums.TamperStateType;

/**
 * DTO for CLIP 2 home security tamper switch.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class TamperReport extends BaseReport {

    private @NonNullByDefault({}) String state;

    public TamperStateType getTamperState() throws IllegalArgumentException {
        return TamperStateType.valueOf(state.toUpperCase());
    }
}
