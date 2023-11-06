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
package org.openhab.binding.mielecloud.internal.webservice.api.json;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Immutable POJO representing the remote control capabilities of a device. Queried from the Miele REST API.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class RemoteEnable {
    @Nullable
    private Boolean fullRemoteControl;
    @Nullable
    private Boolean smartGrid;

    public Optional<Boolean> getFullRemoteControl() {
        return Optional.ofNullable(fullRemoteControl);
    }

    public Optional<Boolean> getSmartGrid() {
        return Optional.ofNullable(smartGrid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullRemoteControl, smartGrid);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RemoteEnable other = (RemoteEnable) obj;
        return Objects.equals(fullRemoteControl, other.fullRemoteControl) && Objects.equals(smartGrid, other.smartGrid);
    }

    @Override
    public String toString() {
        return "RemoteEnable [fullRemoteControl=" + fullRemoteControl + ", smartGrid=" + smartGrid + "]";
    }
}
