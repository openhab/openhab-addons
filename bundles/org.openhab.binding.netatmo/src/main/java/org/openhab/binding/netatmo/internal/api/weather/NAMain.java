/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.api.weather;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.dto.NADevice;
import org.openhab.binding.netatmo.internal.api.dto.NAModule;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class NAMain extends NADevice<NAModule> {
    private boolean readOnly;
    private boolean favorite;

    /**
     * true when the device is a user favorite and not owned by them
     *
     * @return favorite
     **/
    public boolean isFavorite() {
        return favorite;
    }

    /**
     * true when the user was invited to (or has favorited) a station, false when the user owns it
     *
     * @return readOnly
     **/
    public boolean isReadOnly() {
        return readOnly;
    }
}
