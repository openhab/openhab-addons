package org.openhab.binding.airquality.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.types.State;

@NonNullByDefault
public enum Category {
    GOOD(HSBType.fromRGB(0, 228, 0)),
    MODERATE(HSBType.fromRGB(255, 255, 0)),
    UNHEALTHY_FSG(HSBType.fromRGB(255, 126, 0)),
    UNHEALTHY(HSBType.fromRGB(255, 0, 0)),
    VERY_UNHEALTHY(HSBType.fromRGB(143, 63, 151)),
    HAZARDOUS(HSBType.fromRGB(126, 0, 35));

    private HSBType color;

    Category(HSBType color) {
        this.color = color;
    }

    public State getColor() {
        return color;
    }
}
