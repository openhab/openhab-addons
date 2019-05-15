/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.openhab.io.homekit.internal.HomekitCharacteristicType;

/**
 *
 * @author Tim Harper - Initial contribution
 */
public class IncompleteAccessoryException extends Exception {
    private static final long serialVersionUID = 8595808359805444177L;
    final HomekitCharacteristicType missingType;

    public IncompleteAccessoryException(HomekitCharacteristicType missingType) {
        super(String.format("Missing accessory type %s", missingType.getTag()));
        this.missingType = missingType;
    }
}
