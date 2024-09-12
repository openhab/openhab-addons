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
package org.openhab.binding.haassohnpelletstove.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Helper} is a Helper class to overcome Call by value for a Status Description.
 *
 *
 * @author Christian Feininger - Initial contribution
 */
@NonNullByDefault
public class Helper {

    private String statusDescription = "";

    /***
     * Gets the Status Description
     *
     * @return
     */
    public String getStatusDesription() {
        return statusDescription;
    }

    /***
     * Sets the Status Description
     *
     * @param status
     */
    public void setStatusDescription(@Nullable String status) {
        if (status != null) {
            statusDescription = statusDescription + "\n" + status;
        }
    }
}
