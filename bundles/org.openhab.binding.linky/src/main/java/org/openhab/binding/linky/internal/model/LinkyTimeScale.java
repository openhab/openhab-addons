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
package org.openhab.binding.linky.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link LinkyTimeScale} enumerates all possible time scale
 * for API queries
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public enum LinkyTimeScale {
    HOURLY("urlCdcHeure"),
    DAILY("urlCdcJour"),
    MONTHLY("urlCdcMois"),
    YEARLY("urlCdcAn");

    private String id;

    private LinkyTimeScale(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
