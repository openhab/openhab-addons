package org.openhab.binding.hue.internal.dto.clip2;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hue.internal.dto.clip2.enums.ContactStateType;

/**
 * DTO for CLIP 2 home security alarm contact.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ContactReport {

    private @NonNullByDefault({}) Instant changed;
    private @NonNullByDefault({}) String state;

    public ContactStateType getContactState() throws IllegalArgumentException {
        return ContactStateType.valueOf(state.toUpperCase());
    }

    public Instant getLastChanged() {
        return changed;
    }

    public ContactReport setLastChanged(Instant changed) {
        this.changed = changed;
        return this;
    }

    public ContactReport setContactState(String state) {
        this.state = state;
        return this;
    }
}
