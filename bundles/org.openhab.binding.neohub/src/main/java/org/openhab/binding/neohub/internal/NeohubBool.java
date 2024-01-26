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
package org.openhab.binding.neohub.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A custom class that wraps a boolean; we need this because newer versions of
 * NeoHub have broken JSON for some boolean values so we can't use the standard
 * deserializer, and thus have to use a custom one
 * 
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
@NonNullByDefault
public class NeohubBool {
    public boolean value;

    public NeohubBool(boolean value) {
        this.value = value;
    }
}
