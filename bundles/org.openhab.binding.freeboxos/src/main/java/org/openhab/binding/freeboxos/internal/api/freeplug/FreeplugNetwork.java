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
package org.openhab.binding.freeboxos.internal.api.freeplug;

import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link FreeplugNetwork} is the Java class used to map the "Freeplug" structure used by the available
 * Freeplug API
 *
 * https://dev.freebox.fr/sdk/os/freeplug/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FreeplugNetwork {
    private @Nullable String id;
    private List<Freeplug> members = List.of();

    public List<Freeplug> getMembers() {
        return members;
    }

    public String getId() {
        return Objects.requireNonNull(id).toLowerCase();
    }
}
