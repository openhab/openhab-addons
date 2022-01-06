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
package org.openhab.binding.nanoleaf.internal.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents effect commands for select and write
 *
 * @author Martin Raepple - Initial contribution
 */
@NonNullByDefault
public class Effects {

    private @Nullable String select;
    private @Nullable List<String> effectsList = null;
    private @Nullable Write write;

    public @Nullable String getSelect() {
        return select;
    }

    public void setSelect(@Nullable String select) {
        this.select = select;
    }

    public @Nullable List<String> getEffectsList() {
        return effectsList;
    }

    public void setEffectsList(@Nullable List<String> effectsList) {
        this.effectsList = effectsList;
    }

    public @Nullable Write getWrite() {
        return write;
    }

    public void setWrite(@Nullable Write write) {
        this.write = write;
    }
}
