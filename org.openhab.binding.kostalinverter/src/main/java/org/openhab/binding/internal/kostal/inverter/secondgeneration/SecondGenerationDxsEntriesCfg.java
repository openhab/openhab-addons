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
package org.openhab.binding.internal.kostal.inverter.secondgeneration;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@link SecondGenerationDxsEntriesCfg} class defines dxsEntries, which are
 * used in the second generation part of the binding.
 *
 * @author Örjan Backsell - Initial contribution Piko1020, Piko New Generation
 */
public class SecondGenerationDxsEntriesCfg {

    public static String[] getDxsEntriesCfg(String dxsEntriesCfgFile) {
        List<String> dxsEntriesList = new ArrayList<>(23);
        dxsEntriesList.add("67109120");
        dxsEntriesList.add("251658754");
        dxsEntriesList.add("251658753");
        dxsEntriesList.add("16780032");
        dxsEntriesList.add("67109378");
        dxsEntriesList.add("67109377");
        dxsEntriesList.add("67109379");
        dxsEntriesList.add("67109634");
        dxsEntriesList.add("67109633");
        dxsEntriesList.add("67109635");
        dxsEntriesList.add("67109890");
        dxsEntriesList.add("67109889");
        dxsEntriesList.add("67109891");
        dxsEntriesList.add("33556736");
        dxsEntriesList.add("33555202");
        dxsEntriesList.add("33555201");
        dxsEntriesList.add("33555203");
        dxsEntriesList.add("33555458");
        dxsEntriesList.add("33555457");
        dxsEntriesList.add("33555459");
        dxsEntriesList.add("33555714");
        dxsEntriesList.add("33555713");
        dxsEntriesList.add("33555715");

        // Create an dxsEntries array
        String[] dxsEntries = new String[23];

        dxsEntriesList.toArray(dxsEntries);

        return dxsEntries;
    }
}
