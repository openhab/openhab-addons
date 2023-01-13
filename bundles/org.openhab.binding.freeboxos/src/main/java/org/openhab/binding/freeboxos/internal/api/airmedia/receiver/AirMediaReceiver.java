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
package org.openhab.binding.freeboxos.internal.api.airmedia.receiver;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.MediaType;

/**
 * The {@link AirMediaReceiver} is the Java class used to map the "AirMediaReceiver" structure used by the available
 * AirMedia receivers API
 *
 * https://dev.freebox.fr/sdk/os/airmedia/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AirMediaReceiver {
    private @Nullable String name; // This name is the UPnP name of the host
    private boolean passwordProtected;
    private Map<MediaType, Boolean> capabilities = Map.of();

    public String getName() {
        return Objects.requireNonNull(name);
    }

    public boolean isPasswordProtected() {
        return passwordProtected;
    }

    public Map<MediaType, Boolean> getCapabilities() {
        return capabilities;
    }
}
