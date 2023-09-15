package org.openhab.binding.hue.internal.dto.clip2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hue.internal.dto.clip2.enums.ContactStateType;

/**
 * DTO for CLIP 2 home security alarm contact.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ContactReport extends BaseReport {

    private @NonNullByDefault({}) String state;

    public ContactStateType getContactState() throws IllegalArgumentException {
        return ContactStateType.valueOf(state.toUpperCase());
    }
}
