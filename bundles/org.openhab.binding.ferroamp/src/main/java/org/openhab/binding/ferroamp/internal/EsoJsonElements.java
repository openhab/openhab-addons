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
 * The {@link EsoJsonElements} is responsible for all parameters related to ESO
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
        jsonElementsEso.add("id");
        jsonElementsEso.add("ubat");
        jsonElementsEso.add("ibat");
        jsonElementsEso.add("wbatprod");
        jsonElementsEso.add("wbatcons");
        jsonElementsEso.add("soc");
        jsonElementsEso.add("relaystatus");
        jsonElementsEso.add("temp");
        jsonElementsEso.add("faultcode");
        jsonElementsEso.add("udc");
        jsonElementsEso.add("ts");
        return jsonElementsEso;
    }
}
