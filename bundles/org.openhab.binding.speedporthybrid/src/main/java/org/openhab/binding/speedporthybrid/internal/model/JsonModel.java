/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.speedporthybrid.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents a node in the Speedport Hybrid JSON model.
 *
 * @author Henning Treu - initial contribution
 *
 */
@NonNullByDefault
public class JsonModel {

    public @Nullable String vartype;
    public @Nullable String varid;
    public @Nullable String varvalue;

    public boolean hasValue(String value) {
        return value.equals(varvalue);
    }

    public boolean isId(String varId) {
        return this.varid != null && this.varid.equals(varId);
    }
}
