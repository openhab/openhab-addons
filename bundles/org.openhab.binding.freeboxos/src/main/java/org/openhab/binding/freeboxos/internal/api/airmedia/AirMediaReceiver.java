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
package org.openhab.binding.freeboxos.internal.api.airmedia;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AirMediaReceiver} is the Java class used to map the "AirMediaReceiver"
 * structure used by the available AirMedia receivers API
 * https://dev.freebox.fr/sdk/os/airmedia/#
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class AirMediaReceiver {
    private @NonNullByDefault({}) String name;
    private boolean passwordProtected;

    public String getName() {
        return name;
    }

    public boolean isPasswordProtected() {
        return passwordProtected;
    }
}
