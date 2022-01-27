/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.voice.googlestt.internal;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GoogleSTTLocale} is responsible for loading supported locales for the Google Cloud Speech-to-Text service.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class GoogleSTTLocale {
    private static final Set<Locale> SUPPORTED_LOCALES = new HashSet<>();
    private static final String GC_STT_DOC_LANGUAGES = "https://cloud.google.com/speech-to-text/docs/languages";
    private static final String LOCAL_COPY = "af-ZA,sq-AL,am-ET,ar-DZ,ar-BH,ar-EG,ar-IQ,ar-IL,ar-JO,ar-KW,ar-LB,ar-MA,ar-OM,ar-QA,ar-SA,ar-PS,ar-TN,ar-AE,ar-YE,hy-AM,az-AZ,eu-ES,bn-BD,bn-IN,bs-BA,bg-BG,my-MM,ca-ES,hr-HR,cs-CZ,da-DK,nl-BE,nl-NL,en-AU,en-CA,en-GH,en-HK,en-IN,en-IE,en-KE,en-NZ,en-NG,en-PK,en-PH,en-SG,en-ZA,en-TZ,en-GB,en-US,et-EE,fi-FI,fr-BE,fr-CA,fr-FR,fr-CH,gl-ES,ka-GE,de-AT,de-DE,de-CH,el-GR,gu-IN,he-IL,hi-IN,hu-HU,is-IS,id-ID,it-IT,it-CH,ja-JP,jv-ID,kn-IN,kk-KZ,km-KH,ko-KR,lo-LA,lv-LV,lt-LT,mk-MK,ms-MY,ml-IN,mr-IN,mn-MN,ne-NP,no-NO,fa-IR,pl-PL,pt-BR,pt-PT,ro-RO,ru-RU,sr-RS,si-LK,sk-SK,sl-SI,es-AR,es-BO,es-CL,es-CO,es-CR,es-DO,es-EC,es-SV,es-GT,es-HN,es-MX,es-NI,es-PA,es-PY,es-PE,es-PR,es-ES,es-US,es-UY,es-VE,su-ID,sw-KE,sw-TZ,sv-SE,ta-IN,ta-MY,ta-SG,ta-LK,te-IN,th-TH,tr-TR,uk-UA,ur-IN,ur-PK,uz-UZ,vi-VN,zu-ZA";

    public static Set<Locale> getSupportedLocales() {
        return SUPPORTED_LOCALES;
    }

    public static void loadLocales(boolean fromDoc) {
        Logger logger = LoggerFactory.getLogger(GoogleSTTLocale.class);
        if (!SUPPORTED_LOCALES.isEmpty()) {
            logger.debug("Languages already loaded");
            return;
        }
        if (!fromDoc) {
            logger.debug("Loading languages from local");
            loadLocalesFromLocal();
            return;
        }
        logger.debug("Loading languages from doc");
        try {
            URL url = new URL(GC_STT_DOC_LANGUAGES);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "text/html");
            int status = con.getResponseCode();
            if (status != 200) {
                logger.warn("Http error loading supported locales, code: {}", status);
                loadLocalesFromLocal();
                return;
            }
            String html = new String(con.getInputStream().readAllBytes());
            Pattern pattern = Pattern.compile("\\<td\\>(?<lang>[a-z]{2})\\-(?<country>[A-Z]{2})\\<\\/td\\>",
                    Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(html);
            Locale lastLocale = null;
            while (matcher.find()) {
                Locale locale = new Locale(matcher.group("lang"), matcher.group("country"));
                if (lastLocale == null || !lastLocale.equals(locale)) {
                    lastLocale = locale;
                    SUPPORTED_LOCALES.add(locale);
                    logger.debug("Locale added {}", locale.toLanguageTag());
                }
            }
        } catch (IOException e) {
            logger.warn("Error loading supported locales: {}", e.getMessage());
            loadLocalesFromLocal();
        }
    }

    private static void loadLocalesFromLocal() {
        Arrays.stream(LOCAL_COPY.split(",")).map((localeTag) -> {
            String[] localeTagParts = localeTag.split("-");
            return new Locale(localeTagParts[0], localeTagParts[1]);
        }).forEach(SUPPORTED_LOCALES::add);
    }
}
