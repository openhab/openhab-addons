package org.openhab.binding.hue.internal.dto.clip2;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * TODO THIS IS A PLACE HOLDER for PR #15552 (base class for a sensor report).
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class BaseReport {

    private @NonNullByDefault({}) @SerializedName(value = "changed", alternate = { "updated" }) Instant changed;

    public Instant getLastChanged() {
        return changed;
    }
}
