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
package org.openhab.binding.fmiweather.internal.discovery;

import java.math.BigDecimal;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.fmiweather.internal.client.Location;

/**
 * Cities of Finland
 *
 * Originally parsed from (not available any more)
 * https://opendata.fmi.fi/wfs?service=WFS&version=2.0.0&request=getFeature&storedquery_id=fmi::forecast::hirlam::surface::cities::multipointcoverage
 * 
 *
 * Using piece of code similar to below:
 *
 * <pre>
 * System.out.println(parseMultiPointCoverageXml(new String(
 *         Files.readAllBytes(getTestResource("forecast_hirlam_surface_cities_multipointcoverage_response.xml"))))
 *                 .getLocations()
 * </pre>
 *
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public final class CitiesOfFinland {

    public static final Set<Location> CITIES_OF_FINLAND = Set.of(
            new Location("Akaa", "61.16667,23.86667", new BigDecimal("61.16667"), new BigDecimal("23.86667")),
            new Location("Alajärvi", "63.00000,23.81667", new BigDecimal("63.00000"), new BigDecimal("23.81667")),
            new Location("Alavus", "62.58333,23.61667", new BigDecimal("62.58333"), new BigDecimal("23.61667")),
            new Location("Espoo", "60.20520,24.65220", new BigDecimal("60.20520"), new BigDecimal("24.65220")),
            new Location("Forssa", "60.81462,23.62146", new BigDecimal("60.81462"), new BigDecimal("23.62146")),
            new Location("Haapajärvi", "63.75000,25.33333", new BigDecimal("63.75000"), new BigDecimal("25.33333")),
            new Location("Haapavesi", "64.12507,25.34792", new BigDecimal("64.12507"), new BigDecimal("25.34792")),
            new Location("Hamina", "60.56974,27.19794", new BigDecimal("60.56974"), new BigDecimal("27.19794")),
            new Location("Hanko", "59.83333,22.95000", new BigDecimal("59.83333"), new BigDecimal("22.95000")),
            new Location("Harjavalta", "61.31667,22.13333", new BigDecimal("61.31667"), new BigDecimal("22.13333")),
            new Location("Heinola", "61.20564,26.03811", new BigDecimal("61.20564"), new BigDecimal("26.03811")),
            new Location("Helsinki", "60.16952,24.93545", new BigDecimal("60.16952"), new BigDecimal("24.93545")),
            new Location("Huittinen", "61.18333,22.70000", new BigDecimal("61.18333"), new BigDecimal("22.70000")),
            new Location("Hyvinkää", "60.63333,24.86667", new BigDecimal("60.63333"), new BigDecimal("24.86667")),
            new Location("Hämeenlinna", "60.99596,24.46434", new BigDecimal("60.99596"), new BigDecimal("24.46434")),
            new Location("Iisalmi", "63.55915,27.19067", new BigDecimal("63.55915"), new BigDecimal("27.19067")),
            new Location("Ikaalinen", "61.76951,23.06580", new BigDecimal("61.76951"), new BigDecimal("23.06580")),
            new Location("Imatra", "61.17185,28.75242", new BigDecimal("61.17185"), new BigDecimal("28.75242")),
            new Location("Jakobstad", "63.67486,22.70256", new BigDecimal("63.67486"), new BigDecimal("22.70256")),
            new Location("Joensuu", "62.60118,29.76316", new BigDecimal("62.60118"), new BigDecimal("29.76316")),
            new Location("Juankoski", "63.06667,28.35000", new BigDecimal("63.06667"), new BigDecimal("28.35000")),
            new Location("Jyvaskyla", "62.24147,25.72088", new BigDecimal("62.24147"), new BigDecimal("25.72088")),
            new Location("Jämsä", "61.86420,25.19002", new BigDecimal("61.86420"), new BigDecimal("25.19002")),
            new Location("Järvenpää", "60.47369,25.08992", new BigDecimal("60.47369"), new BigDecimal("25.08992")),
            new Location("Kaarina", "60.40724,22.36904", new BigDecimal("60.40724"), new BigDecimal("22.36904")),
            new Location("Kajaani", "64.22728,27.72846", new BigDecimal("64.22728"), new BigDecimal("27.72846")),
            new Location("Kalajoki", "64.25000,23.95000", new BigDecimal("64.25000"), new BigDecimal("23.95000")),
            new Location("Kankaanpää", "61.80000,22.41667", new BigDecimal("61.80000"), new BigDecimal("22.41667")),
            new Location("Kannus", "63.90000,23.90000", new BigDecimal("63.90000"), new BigDecimal("23.90000")),
            new Location("Karkkila", "60.53418,24.20977", new BigDecimal("60.53418"), new BigDecimal("24.20977")),
            new Location("Kaskinen", "62.38330,21.21670", new BigDecimal("62.38330"), new BigDecimal("21.21670")),
            new Location("Kauhajoki", "62.43333,22.18333", new BigDecimal("62.43333"), new BigDecimal("22.18333")),
            new Location("Kauhava", "63.10299,23.07129", new BigDecimal("63.10299"), new BigDecimal("23.07129")),
            new Location("Kauniainen", "60.21209,24.72756", new BigDecimal("60.21209"), new BigDecimal("24.72756")),
            new Location("Kemi", "65.75000,24.58333", new BigDecimal("65.75000"), new BigDecimal("24.58333")),
            new Location("Kemijärvi", "66.66667,27.41667", new BigDecimal("66.66667"), new BigDecimal("27.41667")),
            new Location("Kerava", "60.40338,25.10500", new BigDecimal("60.40338"), new BigDecimal("25.10500")),
            new Location("Keuruu", "62.26667,24.70000", new BigDecimal("62.26667"), new BigDecimal("24.70000")),
            new Location("Kitee", "62.10000,30.15000", new BigDecimal("62.10000"), new BigDecimal("30.15000")),
            new Location("Kiuruvesi", "63.65000,26.61667", new BigDecimal("63.65000"), new BigDecimal("26.61667")),
            new Location("Kokemäki", "61.25647,22.35643", new BigDecimal("61.25647"), new BigDecimal("22.35643")),
            new Location("Kokkola", "63.83847,23.13066", new BigDecimal("63.83847"), new BigDecimal("23.13066")),
            new Location("Kotka", "60.46667,26.91667", new BigDecimal("60.46667"), new BigDecimal("26.91667")),
            new Location("Kouvola", "60.86667,26.70000", new BigDecimal("60.86667"), new BigDecimal("26.70000")),
            new Location("Kristinestad", "62.27429,21.37596", new BigDecimal("62.27429"), new BigDecimal("21.37596")),
            new Location("Kuhmo", "64.13333,29.51667", new BigDecimal("64.13333"), new BigDecimal("29.51667")),
            new Location("Kuopio", "62.89238,27.67703", new BigDecimal("62.89238"), new BigDecimal("27.67703")),
            new Location("Kurikka", "62.61667,22.41667", new BigDecimal("62.61667"), new BigDecimal("22.41667")),
            new Location("Kuusamo", "65.96667,29.18333", new BigDecimal("65.96667"), new BigDecimal("29.18333")),
            new Location("Lahti", "60.98267,25.66151", new BigDecimal("60.98267"), new BigDecimal("25.66151")),
            new Location("Laitila", "60.87575,21.69765", new BigDecimal("60.87575"), new BigDecimal("21.69765")),
            new Location("Lappeenranta", "61.05871,28.18871", new BigDecimal("61.05871"), new BigDecimal("28.18871")),
            new Location("Lapua", "62.96927,23.00880", new BigDecimal("62.96927"), new BigDecimal("23.00880")),
            new Location("Lieksa", "63.31667,30.01667", new BigDecimal("63.31667"), new BigDecimal("30.01667")),
            new Location("Lohja", "60.24859,24.06534", new BigDecimal("60.24859"), new BigDecimal("24.06534")),
            new Location("Loimaa", "60.84972,23.05610", new BigDecimal("60.84972"), new BigDecimal("23.05610")),
            new Location("Loviisa", "60.45659,26.22505", new BigDecimal("60.45659"), new BigDecimal("26.22505")),
            new Location("Mariehamn", "60.09726,19.93481", new BigDecimal("60.09726"), new BigDecimal("19.93481")),
            new Location("Mikkeli", "61.68857,27.27227", new BigDecimal("61.68857"), new BigDecimal("27.27227")),
            new Location("Mänttä-Vilppula", "62.02966,24.60268", new BigDecimal("62.02966"),
                    new BigDecimal("24.60268")),
            new Location("Naantali", "60.46744,22.02428", new BigDecimal("60.46744"), new BigDecimal("22.02428")),
            new Location("Nilsiä", "63.20000,28.08333", new BigDecimal("63.20000"), new BigDecimal("28.08333")),
            new Location("Nivala", "63.91667,24.96667", new BigDecimal("63.91667"), new BigDecimal("24.96667")),
            new Location("Nokia", "61.46667,23.50000", new BigDecimal("61.46667"), new BigDecimal("23.50000")),
            new Location("Nurmes", "63.54205,29.13965", new BigDecimal("63.54205"), new BigDecimal("29.13965")),
            new Location("Nykarleby", "63.52277,22.53073", new BigDecimal("63.52277"), new BigDecimal("22.53073")),
            new Location("Närpes", "62.47283,21.33707", new BigDecimal("62.47283"), new BigDecimal("21.33707")),
            new Location("Orimattila", "60.80487,25.72964", new BigDecimal("60.80487"), new BigDecimal("25.72964")),
            new Location("Orivesi", "61.67766,24.35720", new BigDecimal("61.67766"), new BigDecimal("24.35720")),
            new Location("Oulainen", "64.26667,24.80000", new BigDecimal("64.26667"), new BigDecimal("24.80000")),
            new Location("Oulu", "65.01236,25.46816", new BigDecimal("65.01236"), new BigDecimal("25.46816")),
            new Location("Outokumpu", "62.72685,29.01592", new BigDecimal("62.72685"), new BigDecimal("29.01592")),
            new Location("Paimio", "60.45671,22.68694", new BigDecimal("60.45671"), new BigDecimal("22.68694")),
            new Location("Pargas", "60.00000,23.15000", new BigDecimal("60.00000"), new BigDecimal("23.15000")),
            new Location("Parkano", "62.01667,23.01667", new BigDecimal("62.01667"), new BigDecimal("23.01667")),
            new Location("Pieksämäki", "62.30000,27.13333", new BigDecimal("62.30000"), new BigDecimal("27.13333")),
            new Location("Pori", "61.48333,21.78333", new BigDecimal("61.48333"), new BigDecimal("21.78333")),
            new Location("Porvoo", "60.39233,25.66507", new BigDecimal("60.39233"), new BigDecimal("25.66507")),
            new Location("Pudasjärvi", "65.38333,26.91667", new BigDecimal("65.38333"), new BigDecimal("26.91667")),
            new Location("Pyhäjärvi", "63.66667,25.90000", new BigDecimal("63.66667"), new BigDecimal("25.90000")),
            new Location("Raahe", "64.68333,24.48333", new BigDecimal("64.68333"), new BigDecimal("24.48333")),
            new Location("Raisio", "60.48592,22.16895", new BigDecimal("60.48592"), new BigDecimal("22.16895")),
            new Location("Raseborg", "59.97735,23.43967", new BigDecimal("59.97735"), new BigDecimal("23.43967")),
            new Location("Rauma", "61.12724,21.51127", new BigDecimal("61.12724"), new BigDecimal("21.51127")),
            new Location("Riihimäki", "60.73769,24.77726", new BigDecimal("60.73769"), new BigDecimal("24.77726")),
            new Location("Rovaniemi", "66.50000,25.71667", new BigDecimal("66.50000"), new BigDecimal("25.71667")),
            new Location("Saarijärvi", "62.70486,25.25396", new BigDecimal("62.70486"), new BigDecimal("25.25396")),
            new Location("Salo", "60.38333,23.13333", new BigDecimal("60.38333"), new BigDecimal("23.13333")),
            new Location("Sastamala", "61.35021,22.91053", new BigDecimal("61.35021"), new BigDecimal("22.91053")),
            new Location("Savonlinna", "61.86990,28.87999", new BigDecimal("61.86990"), new BigDecimal("28.87999")),
            new Location("Seinäjoki", "62.79446,22.82822", new BigDecimal("62.79446"), new BigDecimal("22.82822")),
            new Location("Somero", "60.61667,23.53333", new BigDecimal("60.61667"), new BigDecimal("23.53333")),
            new Location("Suonenjoki", "62.61667,27.13333", new BigDecimal("62.61667"), new BigDecimal("27.13333")),
            new Location("Tampere", "61.49911,23.78712", new BigDecimal("61.49911"), new BigDecimal("23.78712")),
            new Location("Tornio", "65.84811,24.14662", new BigDecimal("65.84811"), new BigDecimal("24.14662")),
            new Location("Turku", "60.45148,22.26869", new BigDecimal("60.45148"), new BigDecimal("22.26869")),
            new Location("Ulvila", "61.42844,21.87103", new BigDecimal("61.42844"), new BigDecimal("21.87103")),
            new Location("Uusikaupunki", "60.80043,21.40841", new BigDecimal("60.80043"), new BigDecimal("21.40841")),
            new Location("Vaasa", "63.09600,21.61577", new BigDecimal("63.09600"), new BigDecimal("21.61577")),
            new Location("Valkeakoski", "61.26421,24.03122", new BigDecimal("61.26421"), new BigDecimal("24.03122")),
            new Location("Vantaa", "60.30000,24.85000", new BigDecimal("60.30000"), new BigDecimal("24.85000")),
            new Location("Varkaus", "62.31533,27.87300", new BigDecimal("62.31533"), new BigDecimal("27.87300")),
            new Location("Viitasaari", "63.06667,25.86667", new BigDecimal("63.06667"), new BigDecimal("25.86667")),
            new Location("Vilppula", "62.02121,24.50483", new BigDecimal("62.02121"), new BigDecimal("24.50483")),
            new Location("Virrat", "62.24759,23.78004", new BigDecimal("62.24759"), new BigDecimal("23.78004")),
            new Location("Ylivieska", "64.08333,24.55000", new BigDecimal("64.08333"), new BigDecimal("24.55000")),
            new Location("Ylöjärvi", "61.55632,23.59606", new BigDecimal("61.55632"), new BigDecimal("23.59606")),
            new Location("Ähtäri", "62.55403,24.06186", new BigDecimal("62.55403"), new BigDecimal("24.06186")),
            new Location("Äänekoski", "62.60000,25.73333", new BigDecimal("62.60000"), new BigDecimal("25.73333")));
}
