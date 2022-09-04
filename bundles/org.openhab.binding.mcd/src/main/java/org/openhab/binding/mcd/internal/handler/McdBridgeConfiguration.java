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
package org.openhab.binding.mcd.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link McdBridgeConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Simon Dengler - Initial contribution
 */
@NonNullByDefault
public class McdBridgeConfiguration {

    private @Nullable String userEmail;
    private @Nullable String userPassword;

    /**
     * Return user email as string
     * 
     * @return User email as string
     */
    @Nullable
    String getUserEmail() {
        return userEmail;
    }

    /**
     * Return user password as string
     * 
     * @return password as string
     */
    @Nullable
    String getUserPassword() {
        return userPassword;
    }
}
