/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.io.homekit.internal.accessories;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.homekit.internal.HomekitCharacteristicType;

/**
 *
 * @author Tim Harper - Initial contribution
 */
@NonNullByDefault
public class IncompleteAccessoryException extends Exception {
    private static final long serialVersionUID = 8595808359805444177L;

    public IncompleteAccessoryException(HomekitCharacteristicType missingType) {
        super(String.format("Missing accessory type %s", missingType.getTag()));
    }

    public IncompleteAccessoryException(String message) {
        super(message);
    }
}
