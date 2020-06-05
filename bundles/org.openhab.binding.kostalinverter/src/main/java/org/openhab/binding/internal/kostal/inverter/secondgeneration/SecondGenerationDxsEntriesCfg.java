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

    // public static String[] getDxsEntriesCfg() {
    // List<String> dxsEntriesList = new ArrayList<>(23);
    public static List<String> getDxsEntriesCfg() {
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
        // String[] dxsEntries = new String[23];

        // dxsEntriesList.toArray(dxsEntries);

        // return dxsEntries;
        return dxsEntriesList;
    }

    // public static String[] getDxsEntriesCfgExt() {
    // List<String> dxsEntriesListExt = new ArrayList<>(23);
    public static List<String> getDxsEntriesCfgExt() {
        List<String> dxsEntriesListExt = new ArrayList<>(23);
        dxsEntriesListExt.add("83886336");
        dxsEntriesListExt.add("83886592");
        dxsEntriesListExt.add("83886848");
        dxsEntriesListExt.add("83887106");
        dxsEntriesListExt.add("83887362");
        dxsEntriesListExt.add("83887618");
        dxsEntriesListExt.add("67110400");
        dxsEntriesListExt.add("67110656");
        dxsEntriesListExt.add("251659010");
        dxsEntriesListExt.add("251659266");
        dxsEntriesListExt.add("251659278");
        dxsEntriesListExt.add("251659279");
        dxsEntriesListExt.add("251659009");
        dxsEntriesListExt.add("251659265");
        dxsEntriesListExt.add("251658496");
        dxsEntriesListExt.add("33556238");
        dxsEntriesListExt.add("33556230");
        dxsEntriesListExt.add("33556228");
        dxsEntriesListExt.add("33556227");
        dxsEntriesListExt.add("150995968");
        dxsEntriesListExt.add("184549632");
        dxsEntriesListExt.add("251659280");
        dxsEntriesListExt.add("251659281");

        // Create an dxsEntriesExt array
        // String[] dxsEntriesExt = new String[23];

        // dxsEntriesListExt.toArray(dxsEntriesExt);

        // return dxsEntriesExt;
        return dxsEntriesListExt;
    }

    // public static String[] getDxsEntriesCfgExtExt() {
    // List<String> dxsEntriesListExtExt = new ArrayList<>(3);
    public static List<String> getDxsEntriesCfgExtExt() {
        List<String> dxsEntriesListExtExt = new ArrayList<>(23);
        dxsEntriesListExtExt.add("33556226");
        dxsEntriesListExtExt.add("33556229");
        dxsEntriesListExtExt.add("83888128");
        // Create an dxsEntriesExt array
        // String[] dxsEntriesExtExt = new String[3];

        // dxsEntriesListExtExt.toArray(dxsEntriesExtExt);

        // return dxsEntriesExtExt;
        return dxsEntriesListExtExt;
    }

    // public static String[] getDxsEntriesConfiguration() {
    // List<String> dxsEntriesConfigurationList = new ArrayList<>(24);
    public static List<String> getDxsEntriesConfiguration() {
        List<String> dxsEntriesConfigurationList = new ArrayList<>(24);
        dxsEntriesConfigurationList.add("ChargeTimeEnd");
        dxsEntriesConfigurationList.add("33556236");
        dxsEntriesConfigurationList.add("BatteryType");
        dxsEntriesConfigurationList.add("33556252");
        dxsEntriesConfigurationList.add("BatteryUsageConsumption");
        dxsEntriesConfigurationList.add("33556249");
        dxsEntriesConfigurationList.add("BatteryUsageStrategy");
        dxsEntriesConfigurationList.add("83888896");
        dxsEntriesConfigurationList.add("SmartBatteryControl");
        dxsEntriesConfigurationList.add("33556484");
        dxsEntriesConfigurationList.add("SmartBatteryControl_Text");
        dxsEntriesConfigurationList.add("33556484");
        dxsEntriesConfigurationList.add("BatteryChargeTimeFrom");
        dxsEntriesConfigurationList.add("33556239");
        dxsEntriesConfigurationList.add("BatteryChargeTimeTo");
        dxsEntriesConfigurationList.add("33556240");
        dxsEntriesConfigurationList.add("MaxDepthOfDischarge");
        dxsEntriesConfigurationList.add("33556247");
        dxsEntriesConfigurationList.add("ShadowManagement");
        dxsEntriesConfigurationList.add("33556483");
        dxsEntriesConfigurationList.add("ExternalModuleControl");
        dxsEntriesConfigurationList.add("33556482");
        dxsEntriesConfigurationList.add("InverterName");
        dxsEntriesConfigurationList.add("16777984");

        // Create an dxsEntries array
        // String[] dxsEntriesConfiguration = new String[24];

        // return dxsEntriesConfiguration;
        return dxsEntriesConfigurationList;

    }

}
