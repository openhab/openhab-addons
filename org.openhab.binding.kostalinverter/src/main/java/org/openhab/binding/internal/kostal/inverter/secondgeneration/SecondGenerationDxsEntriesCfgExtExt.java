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
 * The {@link SecondGenerationDxsEntriesCfgExtExt} class defines dxsEntries, which are
 * used in the second generation part of the binding.
 *
 * @author Örjan Backsell - Initial contribution Piko1020, Piko New Generation
 */
public class SecondGenerationDxsEntriesCfgExtExt {

    public static String[] getDxsEntriesCfgExtExt(String dxsEntriesCfgFileExtExt) {
        List<String> dxsEntriesListExtExt = new ArrayList<>(3);
        dxsEntriesListExtExt.add("33556226");
        dxsEntriesListExtExt.add("33556229");
        dxsEntriesListExtExt.add("83888128");
        // Create an dxsEntriesExt array
        String[] dxsEntriesExtExt = new String[3];

        dxsEntriesListExtExt.toArray(dxsEntriesExtExt);

        return dxsEntriesExtExt;
    }
}
