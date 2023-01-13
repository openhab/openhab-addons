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
package org.openhab.binding.freeboxos.internal.api.wifi.ap;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link WifiAp} is the Java class used to hold information regarding an Access Point
 *
 * https://dev.freebox.fr/sdk/os/switch/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WifiAp {
    private int id;
    private @Nullable String name;
    private @Nullable WifiApStatus status;

    public int getId() {
        return id;
    }

    public String getName() {
        return Objects.requireNonNull(name);
    }

    public WifiApStatus getStatus() {
        return Objects.requireNonNull(status);
    }

}
