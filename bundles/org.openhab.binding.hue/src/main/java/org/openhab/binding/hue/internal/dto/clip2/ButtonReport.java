package org.openhab.binding.hue.internal.dto.clip2;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * DTO for CLIP 2 button report.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ButtonReport extends BaseReport {
    private @NonNullByDefault({}) String event;

    public String getEvent() {
        return event;
    }
}
