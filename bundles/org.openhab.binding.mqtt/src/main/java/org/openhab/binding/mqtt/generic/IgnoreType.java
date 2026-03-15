/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.generic;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.PrimitiveType;

/**
 * This is a State that is used internally by the binding to indicate that the
 * received message should simply be ignored.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public enum IgnoreType implements PrimitiveType {
    SENTINEL;

    @Override
    public String format(String pattern) {
        return pattern.formatted(this.toString());
    }

    @Override
    public String toString() {
        return toFullString();
    }

    @Override
    public String toFullString() {
        return super.toString();
    }
}
