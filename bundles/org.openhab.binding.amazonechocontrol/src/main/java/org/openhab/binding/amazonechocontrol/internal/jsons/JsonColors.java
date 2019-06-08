package org.openhab.binding.amazonechocontrol.internal.jsons;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class JsonColors {
    public @Nullable String colorName;

    public JsonColors(String colorName) {
        this.colorName = colorName;
    }
}
