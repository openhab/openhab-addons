/**
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
package org.openhab.binding.ferroamp.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EhubParameters1} is responsible for all parameters regarded to EHUB
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

@NonNullByDefault
public class SsoJsonElements {
    public String ssoJsonElements;

    public SsoJsonElements(String ssoJsonElements) {
        this.ssoJsonElements = ssoJsonElements;
    }

    public static List<String> getJsonElementsSso() {
        final List<String> jsonElementsSso = new ArrayList<>();
        jsonElementsSso.add(new String("id"));
        jsonElementsSso.add(new String("upv"));
        jsonElementsSso.add(new String("ipv"));
        jsonElementsSso.add(new String("wpv"));
        jsonElementsSso.add(new String("relaystatus"));
        jsonElementsSso.add(new String("temp"));
        jsonElementsSso.add(new String("faultcode"));
        jsonElementsSso.add(new String("udc"));
        jsonElementsSso.add(new String("ts"));
        return jsonElementsSso;
    }
}
