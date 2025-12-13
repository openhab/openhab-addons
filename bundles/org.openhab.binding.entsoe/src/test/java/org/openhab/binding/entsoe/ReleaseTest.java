/*
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
package org.openhab.binding.entsoe;//

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.entsoe.internal.client.EntsoeClient;
import org.openhab.binding.entsoe.internal.client.EntsoeDocumentParser;
import org.openhab.binding.entsoe.internal.client.EntsoeRequest;
import org.openhab.binding.entsoe.internal.client.SpotPrice;
import org.openhab.binding.entsoe.internal.exception.EntsoeConfigurationException;
import org.openhab.binding.entsoe.internal.exception.EntsoeResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * {@link ReleaseTest} performs real queries against the ENTSOE web service. Shall be performed before releasing a new
 * version
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ReleaseTest {
    private static final String TOKEN = "VALID_TOKEN_NEEDED";
    private final Logger logger = LoggerFactory.getLogger(ReleaseTest.class);
    private static HttpClient httpClient = new HttpClient(new SslContextFactory.Client());
    private static String[][] provenEicCodes = { { "10Y1001A1001A39I", "EE Estonia" },
            { "10Y1001A1001A44P", "SE1 Swedish Elspot Area 1" }, { "10Y1001A1001A45N", "SE2 Swedish Elspot Area 2" },
            { "10Y1001A1001A46L", "SE3 Swedish Elspot Area 3" }, { "10Y1001A1001A47J", "SE4 Swedish Elspot Area 4" },
            { "10Y1001A1001A48H", "NO5 Norwegian Area Elspot Area 5" },
            { "10Y1001A1001A70O", "IT-CENTRE_NORTH Italy Centre-North" },
            { "10Y1001A1001A71M", "IT-CENTRE_SOUTH Italy Centre-South" },
            { "10Y1001A1001A73I", "IT-NORTH Italy North" }, { "10Y1001A1001A74G", "IT-SARDINIA Italy Sardinia" },
            { "10Y1001A1001A75E", "IT-SICILY Italy Sicily" }, { "10Y1001A1001A788", "IT-SOUTH Italy South" },
            { "10Y1001A1001A82H", "DE_LU Germany_Luxemburg" }, { "10Y1001A1001A885", "IT-SACO_AC Italy Saco_AC" },
            { "10Y1001A1001A893", "IT-SACODC Italy Saco_DC" }, { "10Y1001C--000182", "UA-IPS Ukraine IPS" },
            { "10Y1001C--00096J", "IT-CALABRIA Italy Calabria" }, { "10YAT-APG------L", "AT Austria" },
            { "10YBE----------2", "BE Belgium" }, { "10YCA-BULGARIA-R", "BG Bulgaria" },
            { "10YCH-SWISSGRIDZ", "CH Switzerland" }, { "10YCS-CG-TSO---S", "ME Montenegro" },
            { "10YCZ-CEPS-----N", "CZ Czech Republic" }, { "10YDK-1--------W", "DK1 Denmark DK1" },
            { "10YDK-2--------M", "DK2 Denmark DK2" }, { "10YES-REE------0", "ES Spain" },
            { "10YFI-1--------U", "FI Finland" }, { "10YFR-RTE------C", "FR France" },
            { "10YGR-HTSO-----Y", "GR Greece" }, { "10YHR-HEP------M", "HR Croatia" },
            { "10YHU-MAVIR----U", "HU Hungary" }, { "10YLT-1001A0008Q", "LT Lithuania" },
            { "10YLV-1001A00074", "LV Latvia" }, { "10YMK-MEPSO----8", "MK FYROM" },
            { "10YNL----------L", "NL Netherlands" }, { "10YNO-1--------2", "NO1 Norwegian Area Elspot Area 1" },
            { "10YNO-2--------T", "NO2 Norwegian Area Elspot Area 2" },
            { "10YNO-3--------J", "NO3 Norwegian Area Elspot Area 3" },
            { "10YNO-4--------9", "NO4 Norwegian Area Elspot Area 4" }, { "10YPL-AREA-----S", "PL Poland" },
            { "10YPT-REN------W", "PT Portugal" }, { "10YRO-TEL------P", "RO Romania" },
            { "10YSI-ELES-----O", "SI Slovenia" }, { "10YSK-SEPS-----K", "SK Slovak Republic" },
            { "50Y0JVU59B4JWQCU", "NO_NO2NSL Virtual Bidding Zone NO2NSL" } };

    public static void main(String[] args) {
        new ReleaseTest();
    }

    public ReleaseTest() {
        try {
            httpClient.start();
            String eicCode;
            String description;
            for (int i = 0; i < provenEicCodes.length; i++) {
                eicCode = provenEicCodes[i][0];
                description = provenEicCodes[i][1];
                this.examine(eicCode, description);
                Thread.sleep(5000);
            }
            httpClient.stop();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private void examine(String eicCode, String description)
            throws EntsoeResponseException, EntsoeConfigurationException, InterruptedException, SAXException,
            IOException, ParserConfigurationException {
        EntsoeClient entsoeClient = new EntsoeClient(httpClient);
        entsoeClient.setUserAgentSupplier(this::getUserAgent);
        EntsoeRequest entsoeRequest = new EntsoeRequest(TOKEN, eicCode, Instant.now().minus(0, ChronoUnit.DAYS),
                Instant.now().plus(2, ChronoUnit.DAYS));
        String response = entsoeClient.doGetRequest(entsoeRequest, 60);
        EntsoeDocumentParser parser = new EntsoeDocumentParser(response);
        if (parser.isValid()) {
            TreeMap<Instant, SpotPrice> priceMap = parser.getPriceMap(parser.getSequences().firstKey());
            logger.warn("{} : {} size {} First price={}", description, parser.getSequences(), priceMap.size(),
                    priceMap.firstEntry().getKey(), priceMap.firstEntry().getValue().getState());
        } else {
            logger.warn("{} : FAILURE REASON {}", description, parser.getFailureReason());
        }
    }

    public String getUserAgent() {
        return "openHAB/releaseTest";
    }
}
