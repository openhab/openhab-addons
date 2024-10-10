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
package org.openhab.binding.ferroamp.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EhubParameters} is responsible for all parameters regarded to EHUB
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

@NonNullByDefault
public class EsoJsonElements {
    public String esoJsonElements;

    public EsoJsonElements(String esoJsonElements) {
        this.esoJsonElements = esoJsonElements;
    }

    public static List<String> getJsonElementsEso() {
        final List<String> jsonElementsEso = new ArrayList<>();
        jsonElementsEso.add(new String("id"));
        jsonElementsEso.add(new String("ubat"));
        jsonElementsEso.add(new String("ibat"));
        jsonElementsEso.add(new String("wbatprod"));
        jsonElementsEso.add(new String("wbatcons"));
        jsonElementsEso.add(new String("soc"));
        jsonElementsEso.add(new String("relaystatus"));
        jsonElementsEso.add(new String("temp"));
        jsonElementsEso.add(new String("faultcode"));
        jsonElementsEso.add(new String("udc"));
        jsonElementsEso.add(new String("ts"));

        return jsonElementsEso;
    }
}
