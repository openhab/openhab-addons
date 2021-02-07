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
package org.openhab.binding.semsportal.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SEMSPortalConfiguration} class contains fields mapping thing
 * configuration parameters.
 *
 * @author Iwan Bron - Initial contribution
 */
@NonNullByDefault
public class SEMSPortalConfiguration {

    private static final String INVALID = "INVALID";
    /**
     * We need username and password of the SEMS portal to access the solar plant
     * data.
     *
     * In the first version, you need to provide the station ID as well. Later we
     * can discover it from the SEMS portal.
     */
    // Because of the strict compiler NonNull/Nullable checks and failing to see "if
    // != null" checks,
    // mark them nonnull and initialize with bogus values.
    public String username = INVALID;
    public String password = INVALID;
    public String station = INVALID;
    public int update = 5;

    public boolean isProperlyInitialized() {
        return !username.equals(INVALID) && !password.equals(INVALID) && !station.equals(INVALID);
    }
}
