/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.boschindego.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschindego.internal.exceptions.IndegoException;

/**
 * The {@link AuthorizationProvider} is responsible for providing
 * authorization headers needed for communicating with the Bosch Indego
 * cloud services.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public interface AuthorizationProvider {
    /**
     * Get HTTP authorization header for authenticating with Bosch Indego services.
     *
     * @return the header contents
     * @throws IndegoException if not authorized
     */
    String getAuthorizationHeader() throws IndegoException;
}
