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
package org.openhab.transform.regex.internal;

/**
 * @author Thomas.Eichstaedt-Engelen - Initial contribution
 */
public abstract class AbstractTransformationServiceTest {

    protected String source = """
            <?xml version="1.0"?><xml_api_reply version="1"><weather module_id="0"\
             tab_id="0" mobile_row="0" mobile_zipped="1" row="0" section="0" ><forecast_information>\
            <city data="Krefeld, North Rhine-Westphalia"/><postal_code data="Krefeld Germany"/>\
            <latitude_e6 data=""/><longitude_e6 data=""/><forecast_date data="2011-03-01"/>\
            <current_date_time data="2011-03-01 15:20:00 +0000"/><unit_system data="SI"/></forecast_information>\
            <current_conditions><condition data="Meistens bew�lkt"/><temp_f data="46"/><temp_c data="8"/>\
            <humidity data="Feuchtigkeit: 66 %"/><icon data="/ig/images/weather/mostly_cloudy.gif"/>\
            <wind_condition data="Wind: N mit 26 km/h"/></current_conditions><forecast_conditions><day_of_week data="Di."/>\
            <low data="-1"/><high data="6"/><icon data="/ig/images/weather/sunny.gif"/><condition data="Klar"/>\
            </forecast_conditions><forecast_conditions><day_of_week data="Mi."/><low data="-1"/><high data="8"/>\
            <icon data="/ig/images/weather/sunny.gif"/><condition data="Klar"/></forecast_conditions><forecast_conditions>\
            <day_of_week data="Do."/><low data="-1"/><high data="8"/><icon data="/ig/images/weather/sunny.gif"/>\
            <condition data="Klar"/></forecast_conditions><forecast_conditions><day_of_week data="Fr."/><low data="0"/>\
            <high data="8"/><icon data="/ig/images/weather/sunny.gif"/><condition data="Klar"/></forecast_conditions>\
            </weather></xml_api_reply>\
            """;
}
