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
package org.openhab.transform.vat.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.transform.TransformationService;

/**
 * The {@link VATTransformationConstants} class defines constants
 * used across the whole profile.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class VATTransformationConstants {

    public static final ProfileTypeUID PROFILE_TYPE_UID = new ProfileTypeUID(
            TransformationService.TRANSFORM_PROFILE_SCOPE, "VAT");

    public static final Map<String, String> RATES = Map.ofEntries(
            // European Union countries
            Map.entry("AT", "20"), // Austria
            Map.entry("BE", "21"), // Belgium
            Map.entry("BG", "20"), // Bulgaria
            Map.entry("HR", "25"), // Croatia
            Map.entry("CY", "19"), // Cyprus
            Map.entry("CZ", "21"), // Czech Republic
            Map.entry("DK", "25"), // Denmark
            Map.entry("EE", "20"), // Estonia
            Map.entry("FI", "24"), // Finland
            Map.entry("FR", "20"), // France
            Map.entry("DE", "19"), // Germany
            Map.entry("GR", "24"), // Greece
            Map.entry("HU", "27"), // Hungary
            Map.entry("IE", "23"), // Ireland
            Map.entry("IT", "22"), // Italy
            Map.entry("LV", "21"), // Latvia
            Map.entry("LT", "21"), // Lithuania
            Map.entry("LU", "17"), // Luxembourg
            Map.entry("MT", "18"), // Malta
            Map.entry("NL", "21"), // Netherlands
            Map.entry("PL", "23"), // Poland
            Map.entry("PT", "23"), // Portugal
            Map.entry("RO", "19"), // Romania
            Map.entry("SK", "20"), // Slovakia
            Map.entry("SI", "22"), // Slovenia
            Map.entry("ES", "21"), // Spain
            Map.entry("SE", "25"), // Sweden

            // Non-European Union countries
            Map.entry("AL", "20"), // Albania
            Map.entry("DZ", "19"), // Algeria
            Map.entry("AD", "4.5"), // Andorra
            Map.entry("AO", "14"), // Angola
            Map.entry("AG", "15"), // Antigua and Barbuda
            Map.entry("AR", "21"), // Argentina
            Map.entry("AM", "20"), // Armenia
            Map.entry("AU", "10"), // Australia
            Map.entry("AZ", "18"), // Azerbaijan
            Map.entry("BS", "12"), // Bahamas
            Map.entry("BH", "10"), // Bahrain
            Map.entry("BD", "15"), // Bangladesh
            Map.entry("BB", "17.5"), // Barbados
            Map.entry("BY", "20"), // Belarus
            Map.entry("BZ", "12.5"), // Belize
            Map.entry("BJ", "18"), // Benin
            Map.entry("BO", "13"), // Bolivia
            Map.entry("BA", "17"), // Bosnia and Herzegovina
            Map.entry("BW", "12"), // Botswana
            Map.entry("BR", "20"), // Brazil
            Map.entry("BF", "18"), // Burkina Faso
            Map.entry("BI", "18"), // Burundi
            Map.entry("KH", "10"), // Cambodia
            Map.entry("CM", "19.25"), // Cameroon
            Map.entry("CA", "5"), // Canada
            Map.entry("CV", "15"), // Cape Verde
            Map.entry("CF", "19"), // Central African Republic
            Map.entry("TD", "18"), // Chad
            Map.entry("CL", "19"), // Chile
            Map.entry("CN", "13"), // China
            Map.entry("CO", "19"), // Colombia
            Map.entry("CR", "13"), // Costa Rica
            Map.entry("CD", "16"), // Democratic Republic of the Congo
            Map.entry("DM", "15"), // Dominica
            Map.entry("DO", "18"), // Dominican Republic
            Map.entry("EC", "12"), // Ecuador
            Map.entry("EG", "14"), // Egypt
            Map.entry("SV", "13"), // El Salvador
            Map.entry("GQ", "15"), // Equatorial Guinea
            Map.entry("ET", "15"), // Ethiopia
            Map.entry("FO", "25"), // Faroe Islands
            Map.entry("FJ", "15"), // Fiji
            Map.entry("GA", "18"), // Gabon
            Map.entry("GM", "15"), // Gambia
            Map.entry("GE", "18"), // Georgia
            Map.entry("GH", "15"), // Ghana
            Map.entry("GD", "15"), // Grenada
            Map.entry("GT", "12"), // Guatemala
            Map.entry("GN", "18"), // Guinea
            Map.entry("GW", "15"), // Guinea-Bissau
            Map.entry("GY", "16"), // Guyana
            Map.entry("HT", "10"), // Haiti
            Map.entry("HN", "15"), // Honduras
            Map.entry("IS", "24"), // Iceland
            Map.entry("IN", "5.5"), // India
            Map.entry("ID", "11"), // Indonesia
            Map.entry("IR", "9"), // Iran
            Map.entry("IM", "20"), // Isle of Man
            Map.entry("IL", "17"), // Israel
            Map.entry("CI", "18"), // Ivory Coast
            Map.entry("JM", "12.5"), // Jamaica
            Map.entry("JP", "10"), // Japan
            Map.entry("JE", "5"), // Jersey
            Map.entry("JO", "16"), // Jordan
            Map.entry("KZ", "12"), // Kazakhstan
            Map.entry("KE", "16"), // Kenya
            Map.entry("KG", "20"), // Kyrgyzstan
            Map.entry("LA", "10"), // Laos
            Map.entry("LB", "11"), // Lebanon
            Map.entry("LS", "14"), // Lesotho
            Map.entry("LI", "7.7"), // Liechtenstein
            Map.entry("MG", "20"), // Madagascar
            Map.entry("MW", "16.5"), // Malawi
            Map.entry("MY", "6"), // Malaysia
            Map.entry("MV", "6"), // Maldives
            Map.entry("ML", "18"), // Mali
            Map.entry("MR", "14"), // Mauritania
            Map.entry("MU", "15"), // Mauritius
            Map.entry("MX", "16"), // Mexico
            Map.entry("MD", "20"), // Moldova
            Map.entry("MC", "19.6"), // Monaco
            Map.entry("MN", "10"), // Mongolia
            Map.entry("ME", "21"), // Montenegro
            Map.entry("MA", "20"), // Morocco
            Map.entry("MZ", "17"), // Mozambique
            Map.entry("NA", "15"), // Namibia
            Map.entry("NP", "13"), // Nepal
            Map.entry("NZ", "15"), // New Zealand
            Map.entry("NI", "15"), // Nicaragua
            Map.entry("NE", "19"), // Niger
            Map.entry("NG", "7.5"), // Nigeria
            Map.entry("NU", "5"), // Niue
            Map.entry("MK", "18"), // North Macedonia
            Map.entry("NO", "25"), // Norway
            Map.entry("PK", "17"), // Pakistan
            Map.entry("PW", "10"), // Palau
            Map.entry("PS", "16"), // Palestine
            Map.entry("PA", "7"), // Panama
            Map.entry("PG", "10"), // Papua New Guinea
            Map.entry("PY", "10"), // Paraguay
            Map.entry("PE", "18"), // Peru
            Map.entry("PH", "12"), // Philippines
            Map.entry("CG", "16"), // Republic of Congo
            Map.entry("RU", "20"), // Russia
            Map.entry("RW", "18"), // Rwanda
            Map.entry("KN", "17"), // Saint Kitts and Nevis
            Map.entry("VC", "15"), // Saint Vincent and the Grenadines
            Map.entry("WS", "15"), // Samoa
            Map.entry("SA", "15"), // Saudi Arabia
            Map.entry("SN", "18"), // Senegal
            Map.entry("RS", "20"), // Serbia
            Map.entry("SC", "15"), // Seychelles
            Map.entry("SL", "15"), // Sierra Leone
            Map.entry("SG", "8"), // Singapore
            Map.entry("ZA", "15"), // South Africa
            Map.entry("KR", "10"), // South Korea
            Map.entry("LK", "12"), // Sri Lanka
            Map.entry("SD", "17"), // Sudan
            Map.entry("CH", "7.7"), // Switzerland
            Map.entry("TW", "5"), // Taiwan
            Map.entry("TJ", "20"), // Tajikistan
            Map.entry("TZ", "18"), // Tanzania
            Map.entry("TH", "10"), // Thailand
            Map.entry("TG", "18"), // Togo
            Map.entry("TO", "15"), // Tonga
            Map.entry("TT", "12.5"), // Trinidad and Tobago
            Map.entry("TN", "18"), // Tunisia
            Map.entry("TR", "18"), // Turkey
            Map.entry("TM", "15"), // Turkmenistan
            Map.entry("UG", "18"), // Uganda
            Map.entry("UA", "20"), // Ukraine
            Map.entry("AE", "5"), // United Arab Emirates
            Map.entry("GB", "20"), // United Kingdom
            Map.entry("UY", ""), // Uruguay
            Map.entry("UZ", "12"), // Uzbekistan
            Map.entry("VU", "13"), // Vanuatu
            Map.entry("VN", "10"), // Vietnam
            Map.entry("VE", "12"), // Venezuela
            Map.entry("ZM", "16"), // Zambia
            Map.entry("ZW", "15") // Zimbabwe
    );
}
