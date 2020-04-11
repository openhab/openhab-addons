/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.smarther.internal.api.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Smarther exception indicating that a subscription for given plant already exists.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class SmartherSubscriptionAlreadyExistsException extends SmartherGatewayException {

    private static final long serialVersionUID = 5185321219105493105L;

    /**
     * Constructor
     *
     * @param message BTicino/Legrand API gateway error message
     */
    public SmartherSubscriptionAlreadyExistsException(String message) {
        super(message);
    }

}
