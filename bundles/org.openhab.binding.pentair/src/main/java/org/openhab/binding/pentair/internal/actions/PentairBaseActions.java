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
package org.openhab.binding.pentair.internal.actions;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link PentairBaseActions } Abstract class for all Pentair actions classes
 *
 * @author Jeff James - Initial contribution
 *
 */
@NonNullByDefault
public class PentairBaseActions {

    @Nullable
    private PentairWriter writer;
    protected int id;

    public void initialize(PentairWriter writer, int id) {
        this.writer = writer;
        this.id = id;
    }

    public PentairWriter getWriter() {
        return Objects.requireNonNull(writer);
    }
}
