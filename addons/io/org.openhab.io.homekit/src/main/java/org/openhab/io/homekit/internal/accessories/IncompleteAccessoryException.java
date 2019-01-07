package org.openhab.io.homekit.internal.accessories;

import org.openhab.io.homekit.internal.HomekitCharacteristicType;

public class IncompleteAccessoryException extends Exception {
    final HomekitCharacteristicType missingType;

    public IncompleteAccessoryException(HomekitCharacteristicType missingType) {
        super(String.format("Missing accessory type %s", missingType.getTag()));
        this.missingType = missingType;
    }
}
