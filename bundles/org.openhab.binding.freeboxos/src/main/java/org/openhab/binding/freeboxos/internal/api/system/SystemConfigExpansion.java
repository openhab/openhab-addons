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
package org.openhab.binding.freeboxos.internal.api.system;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.ExpansionType;

/**
 * The {@link SystemConfigExpansion} is the Java class used to map the System configuration expansion elements
 * structure used by the system API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SystemConfigExpansion {
    private int slot;
    private boolean probeDone;
    private boolean present;
    private boolean supported;
    private @Nullable String bundle;
    private ExpansionType type = ExpansionType.UNKNOWN;

    public int getSlot() {
        return slot;
    }

    public boolean isProbeDone() {
        return probeDone;
    }

    public boolean isPresent() {
        return present;
    }

    public boolean isSupported() {
        return supported;
    }

    public String getBundle() {
        return Objects.requireNonNull(bundle);
    }

    public ExpansionType getType() {
        return type;
    }

}
