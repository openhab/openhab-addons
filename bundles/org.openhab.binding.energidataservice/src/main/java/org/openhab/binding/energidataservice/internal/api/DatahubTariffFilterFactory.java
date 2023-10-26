/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.energidataservice.internal.api;

import static org.openhab.binding.energidataservice.internal.EnergiDataServiceBindingConstants.*;

import java.time.LocalDate;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Factory for creating a {@link DatahubTariffFilter} for a specific Grid Company GLN.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class DatahubTariffFilterFactory {

    private static final String GLN_AAL_ELNET = "5790001095451";
    private static final String GLN_CERIUS = "5790000705184";
    private static final String GLN_DINEL = "5790000610099";
    private static final String GLN_ELEKTRUS = "5790000836239";
    private static final String GLN_ELINORD = "5790001095277";
    private static final String GLN_ELNET_MIDT = "5790001100520";
    private static final String GLN_ELNET_KONGERSLEV = "5790002502699";
    private static final String GLN_FLOW_ELNET = "5790000392551";
    private static final String GLN_HAMMEL_ELFORSYNING_NET = "5790001090166";
    private static final String GLN_HURUP_ELVAERK_NET = "5790000610839";
    private static final String GLN_IKAST_E1_NET = "5790000682102";
    private static final String GLN_KONSTANT = "5790000704842";
    private static final String GLN_L_NET = "5790001090111";
    private static final String GLN_MIDTFYNS_ELFORSYNING = "5790001089023";
    private static final String GLN_N1 = "5790001089030";
    private static final String GLN_NETSELSKABET_ELVAERK = "5790000681075";
    private static final String GLN_NKE_ELNET = "5790001088231";
    private static final String GLN_NORD_ENERGI_NET = "5790000610877";
    private static final String GLN_NORDVESTJYSK_ELFORSYNING_NOE_NET = "5790000395620";
    private static final String GLN_RADIUS = "5790000705689";
    private static final String GLN_RAH_NET = "5790000681327";
    private static final String GLN_RAVDEX = "5790000836727";
    private static final String GLN_SUNDS_NET = "5790001095444";
    private static final String GLN_TARM_ELVAERK_NET = "5790000706419";
    private static final String GLN_TREFOR_EL_NET = "5790000392261";
    private static final String GLN_TREFOR_EL_NET_OEST = "5790000706686";
    private static final String GLN_VEKSEL = "5790001088217";
    private static final String GLN_VORES_ELNET = "5790000610976";
    private static final String GLN_ZEANET = "5790001089375";

    private static final String NOTE_NET_TARIFF = "Nettarif";
    private static final String NOTE_NET_TARIFF_C = NOTE_NET_TARIFF + " C";
    private static final String NOTE_NET_TARIFF_C_HOUR = NOTE_NET_TARIFF_C + " time";
    private static final String NOTE_NET_TARIFF_C_FLEX = NOTE_NET_TARIFF_C + " Flex";
    private static final String NOTE_NET_TARIFF_C_FLEX_HOUR = NOTE_NET_TARIFF_C_FLEX + " - time";
    private static final String NOTE_SYSTEM_TARIFF = "Systemtarif";
    private static final String NOTE_ELECTRICITY_TAX = "Elafgift";
    private static final String NOTE_REDUCED_ELECTRICITY_TAX = "Reduceret elafgift";
    private static final String NOTE_TRANSMISSION_NET_TARIFF = "Transmissions nettarif";

    public static final LocalDate N1_CUTOFF_DATE = LocalDate.of(2023, 1, 1);
    public static final LocalDate RADIUS_CUTOFF_DATE = LocalDate.of(2023, 1, 1);
    public static final LocalDate KONSTANT_CUTOFF_DATE = LocalDate.of(2023, 2, 1);

    public static DatahubTariffFilter getNetTariffByGLN(String globalLocationNumber) {
        switch (globalLocationNumber) {
            case GLN_AAL_ELNET:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("AAL-NT-05"), ChargeTypeCode.of("AAL-NTR05")),
                        Set.of(NOTE_NET_TARIFF_C_HOUR), DateQueryParameter.of(DateQueryParameterType.START_OF_DAY));
            case GLN_CERIUS:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("30TR_C_ET")), Set.of(NOTE_NET_TARIFF_C_HOUR));
            case GLN_DINEL:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("TCL>100_02"), ChargeTypeCode.of("TCL<100_52")),
                        Set.of(NOTE_NET_TARIFF_C_HOUR));
            case GLN_ELEKTRUS:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("6000091")), Set.of(NOTE_NET_TARIFF_C_HOUR),
                        DateQueryParameter.of(DateQueryParameterType.START_OF_DAY));
            case GLN_ELINORD:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("43300")),
                        Set.of("Transportbetaling, eget net C"));
            case GLN_ELNET_MIDT:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("T3001")), Set.of(NOTE_NET_TARIFF_C),
                        DateQueryParameter.of(DateQueryParameterType.START_OF_DAY));
            case GLN_ELNET_KONGERSLEV:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("K_22100")), Set.of(NOTE_NET_TARIFF_C));
            case GLN_FLOW_ELNET:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("FE2 NT-01")), Set.of(NOTE_NET_TARIFF_C_HOUR),
                        DateQueryParameter.of(DateQueryParameterType.START_OF_DAY));
            case GLN_HAMMEL_ELFORSYNING_NET:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("50001")), Set.of("Overliggende net"),
                        DateQueryParameter.of(DateQueryParameterType.START_OF_DAY));
            case GLN_HURUP_ELVAERK_NET:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("HEV-NT-01")), Set.of(NOTE_NET_TARIFF));
            case GLN_IKAST_E1_NET:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("IEV-NT-11")),
                        Set.of(NOTE_NET_TARIFF_C_HOUR, "Transport - Overordnet net"));
            case GLN_KONSTANT:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("151-NT01T"), ChargeTypeCode.of("151-NRA04T")),
                        Set.of(), DateQueryParameter.of(KONSTANT_CUTOFF_DATE));
            case GLN_L_NET:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("4010")), Set.of(NOTE_NET_TARIFF_C_HOUR));
            case GLN_MIDTFYNS_ELFORSYNING:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("TNT15000")), Set.of(NOTE_NET_TARIFF_C_FLEX),
                        DateQueryParameter.of(DateQueryParameterType.START_OF_DAY));
            case GLN_N1:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("CD"), ChargeTypeCode.of("CD R")), Set.of(),
                        DateQueryParameter.of(N1_CUTOFF_DATE));
            case GLN_NETSELSKABET_ELVAERK:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("0NCFF")), Set.of(NOTE_NET_TARIFF_C + " Flex"),
                        DateQueryParameter.of(DateQueryParameterType.START_OF_DAY));
            case GLN_NKE_ELNET:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("94TR_C_ET")), Set.of(NOTE_NET_TARIFF_C_HOUR),
                        DateQueryParameter.of(DateQueryParameterType.START_OF_DAY));
            case GLN_NORD_ENERGI_NET:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("TAC")), Set.of(NOTE_NET_TARIFF_C),
                        DateQueryParameter.of(DateQueryParameterType.START_OF_DAY));
            case GLN_NORDVESTJYSK_ELFORSYNING_NOE_NET:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("Net C")), Set.of(NOTE_NET_TARIFF_C));
            case GLN_RADIUS:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("DT_C_01")), Set.of(NOTE_NET_TARIFF_C_HOUR),
                        DateQueryParameter.of(RADIUS_CUTOFF_DATE));
            case GLN_RAH_NET:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("RAH-C")), Set.of(NOTE_NET_TARIFF_C_HOUR),
                        DateQueryParameter.of(DateQueryParameterType.START_OF_DAY));
            case GLN_RAVDEX:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("NT-C")), Set.of(NOTE_NET_TARIFF_C_HOUR),
                        DateQueryParameter.of(DateQueryParameterType.START_OF_DAY));
            case GLN_SUNDS_NET:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("SEF-NT-05"), ChargeTypeCode.of("SEF-NT-05R")),
                        Set.of(NOTE_NET_TARIFF_C_FLEX_HOUR),
                        DateQueryParameter.of(DateQueryParameterType.START_OF_DAY));
            case GLN_TARM_ELVAERK_NET:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("TEV-NT-01"), ChargeTypeCode.of("TEV-NT-01R")),
                        Set.of(NOTE_NET_TARIFF_C));
            case GLN_TREFOR_EL_NET:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("C")), Set.of(NOTE_NET_TARIFF_C_HOUR),
                        DateQueryParameter.of(DateQueryParameterType.START_OF_DAY));
            case GLN_TREFOR_EL_NET_OEST:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("46")), Set.of(NOTE_NET_TARIFF_C_HOUR),
                        DateQueryParameter.of(DateQueryParameterType.START_OF_DAY));
            case GLN_VEKSEL:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("NT-10")),
                        Set.of(NOTE_NET_TARIFF_C_HOUR + "  NT-10"));
            case GLN_VORES_ELNET:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("TNT1009")), Set.of(NOTE_NET_TARIFF_C_HOUR),
                        DateQueryParameter.of(DateQueryParameterType.START_OF_DAY));
            case GLN_ZEANET:
                return new DatahubTariffFilter(Set.of(ChargeTypeCode.of("43110")), Set.of(NOTE_NET_TARIFF_C_HOUR),
                        DateQueryParameter.of(DateQueryParameterType.START_OF_DAY));
            default:
                return new DatahubTariffFilter(Set.of(), Set.of(NOTE_NET_TARIFF_C),
                        DateQueryParameter.of(DateQueryParameterType.START_OF_YEAR));
        }
    }

    public static DatahubTariffFilter getSystemTariff() {
        return new DatahubTariffFilter(Set.of(), Set.of(NOTE_SYSTEM_TARIFF),
                DateQueryParameter.of(ENERGINET_CUTOFF_DATE));
    }

    public static DatahubTariffFilter getElectricityTax() {
        return new DatahubTariffFilter(Set.of(), Set.of(NOTE_ELECTRICITY_TAX),
                DateQueryParameter.of(ENERGINET_CUTOFF_DATE));
    }

    public static DatahubTariffFilter getReducedElectricityTax() {
        return new DatahubTariffFilter(Set.of(), Set.of(NOTE_REDUCED_ELECTRICITY_TAX),
                DateQueryParameter.of(LocalDate.of(2021, 2, 1)));
    }

    public static DatahubTariffFilter getTransmissionNetTariff() {
        return new DatahubTariffFilter(Set.of(), Set.of(NOTE_TRANSMISSION_NET_TARIFF),
                DateQueryParameter.of(ENERGINET_CUTOFF_DATE));
    }
}
