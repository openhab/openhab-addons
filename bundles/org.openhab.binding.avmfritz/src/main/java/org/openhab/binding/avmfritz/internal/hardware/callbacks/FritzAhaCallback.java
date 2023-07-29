/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal.hardware.callbacks;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for callbacks in asynchronous requests.
 *
 * @author Robert Bausdorf - Initial contribution
 */
@NonNullByDefault
public interface FritzAhaCallback {
    /**
     * Runs callback code after response completion.
     */
    void execute(int status, String response);

    /**
     * Get the URI path
     *
     * @return URI path as String
     */
    String getPath();

    /**
     * Get the query String
     *
     * @return Query string as String
     */
    String getArgs();
}
