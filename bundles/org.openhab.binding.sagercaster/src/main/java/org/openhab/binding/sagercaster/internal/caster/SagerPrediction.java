/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sagercaster.internal.caster;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This record holds the result of the SagerCaster algorithm
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public record SagerPrediction(String sagerCode) {

    public String getForecast() {
        return Character.toString(sagerCode.charAt(0));
    }

    public String getWindVelocity() {
        return Character.toString(sagerCode.charAt(1));
    }

    public String getWindDirection() {
        return Character.toString(sagerCode.charAt(2));
    }

    public @Nullable String getWindDirection2() {
        return sagerCode.length() > 3 ? Character.toString(sagerCode.charAt(3)) : null;
    }
}
