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
package org.openhab.binding.homematic.internal.type.generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * The CcuMetadataExtractor loads some JavaScript files from the CCU and generates the device and datapoint
 * descriptions into the file generated-descriptions.properties.
 * Not all descriptions can be generated, because they are spread across many files and were partly assembled.
 * To add the missing descriptions or to override a generated, use the extra-descriptions.properties file.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class CcuMetadataExtractor {
    private static final String CCU_URL = "http://ccu";

    private static final String DEVICE_KEYS = "/webui/webui.js";
    private static final String DESCRIPTION_KEYS = "/webui/js/lang/{LANGUAGE}/translate.lang.stringtable.js";
    private static final String DESCRIPTION_DESCRIPTIONS = "/webui/js/lang/{LANGUAGE}/translate.lang.deviceDescription.js";

    private static final String MASTER_LANG_PATH = "/config/easymodes/MASTER_LANG/";
    private static final String[] MASTER_LANG_FILES = { "HEATINGTHERMOSTATE_2ND_GEN.js", "HM_ES_PMSw.js" };

    private static final String[] LANGUAGES = { "en", "de" };

    /**
     * Starts the extractor and generates the file.
     */
    public static void main(String[] args) {
        try {
            CcuMetadataExtractor dg = new CcuMetadataExtractor();
            dg.generate();

        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }

    /**
     * Loads the JavaScript file from the CCU and generates the properties file.
     */
    private void generate() throws IOException {
        Map<String, String> deviceKeys = loadDeviceKeys();

        for (String lang : LANGUAGES) {
            // load datapoint descriptions
            Map<String, String> langDescriptions = loadJsonLangDescriptionFile(CCU_URL + DESCRIPTION_KEYS, lang);
            for (String fileName : MASTER_LANG_FILES) {
                langDescriptions.putAll(loadJsonLangDescriptionFile(CCU_URL + MASTER_LANG_PATH + fileName, lang));
            }

            // load device descriptions
            Map<String, String> deviceDescriptions = loadJsonLangDescriptionFile(CCU_URL + DESCRIPTION_DESCRIPTIONS,
                    lang);

            String langIdent = ("en".equals(lang) ? "" : "_" + lang);
            File file = new File("./src/main/resources/homematic/generated-descriptions" + langIdent + ".properties");
            System.out.println("Writing file " + file.getAbsolutePath());
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));

            // write device descriptions
            bw.write("# -------------- generated descriptions " + new Date() + " --------------\n");
            bw.write("# DON'T CHANGE THIS FILE!\n\n");
            for (Entry<String, String> entry : deviceDescriptions.entrySet()) {
                bw.write(entry.getKey());
                bw.write("=");
                bw.write(entry.getValue());
                bw.write("\n");
            }

            bw.write("\n\n\n");

            // write datapoint descriptions
            for (Entry<String, String> entry : deviceKeys.entrySet()) {
                String description = langDescriptions.get(entry.getValue());
                if (description == null) {
                    System.out.println("WARNING: Can't find a translation for " + entry.getValue());
                } else {
                    bw.write(entry.getKey().toUpperCase());
                    bw.write("=");
                    bw.write(description);
                    bw.write("\n");
                }
            }

            bw.flush();
            bw.close();
        }
    }

    /**
     * Loads a JavaScript file and extract JSON description data.
     */
    private Map<String, String> loadJsonLangDescriptionFile(String url, String lang) throws IOException {
        final Map<String, String> descriptions = new TreeMap<>();
        String descriptionUrl = url.replace("{LANGUAGE}", lang);

        String startLine = "  \"" + lang + "\" : {";
        String endLine = "  },";

        new UrlLoader(descriptionUrl, startLine, endLine) {
            @Override
            public void line(String line) {
                String[] entry = handleStringTable(line);
                if (entry != null) {
                    descriptions.put(entry[0].trim(), unescape(entry[1].trim()));
                }
            }
        };
        return descriptions;
    }

    /**
     * Loads all description keys.
     */
    private Map<String, String> loadDeviceKeys() throws IOException {
        final Map<String, String> deviceKeys = new TreeMap<>();

        new UrlLoader(CCU_URL + DEVICE_KEYS) {

            @Override
            public void line(String line) {
                if (line.startsWith("elvST['")) {
                    line = line.replace("elvST['", "");
                    line = line.replace("'] = '", "=");
                    line = line.replace("';", "");
                    line = line.replace("$", "");
                    line = line.replace("{", "");
                    line = line.replace("}", "");

                    int count = 0;
                    for (int i = 0; i < line.length(); i++) {
                        if (line.charAt(i) == '=') {
                            count++;
                        }
                    }
                    if (count > 1) {
                        line = line.replaceFirst("=", "|");
                    }
                    String[] split = line.split("=", 2);
                    deviceKeys.put(split[0].trim(), split[1].trim());
                }
            }
        };
        return deviceKeys;
    }

    /**
     * Splits a JSON JavaScript entry.
     */
    private String[] handleStringTable(String line) {
        line = line.replace("    \"", "");
        line = line.replace("\",", "");
        line = line.replace("\"", "");

        String[] splitted = line.split(":", 2);
        return splitted.length != 2 ? null : splitted;
    }

    /**
     * Transforms a string for a Java property file.
     */
    private String unescape(String str) {
        str = str.replace("%FC", "ü");
        str = str.replace("%DC", "Ü");
        str = str.replace("%E4", "ä");
        str = str.replace("%C4", "Ä");
        str = str.replace("%F6", "ö");
        str = str.replace("%D6", "Ö");
        str = str.replace("%DF", "ß");
        str = str.replace("&nbsp;", "");
        str = str.replace("<br/>", " ");
        str = str.replace("&uuml;", "ü");
        str = str.replace("&auml;", "ä");
        str = str.replace("&ouml;", "ö");
        str = str.replace("&Uuml;", "Ü");
        str = str.replace("&Auml;", "Ä");
        str = str.replace("&Ouml;", "Ö");
        return str;
    }

    /**
     * Simple class to load a CCU resource.
     */
    private abstract class UrlLoader {
        public UrlLoader(String url) throws IOException {
            this(url, null, null);
        }

        public UrlLoader(String url, String startLine, String endLine) throws IOException {
            System.out.println("Loading file " + url);
            Boolean includeLine = null;
            BufferedReader br = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "UTF-8"));

            String line;
            while ((line = br.readLine()) != null) {
                if (startLine != null) {
                    if (line.equals(startLine)) {
                        includeLine = true;
                        continue;
                    } else if (line.equals(endLine) || includeLine == null) {
                        includeLine = false;
                    }
                }
                if ((includeLine == null || includeLine) && !line.isBlank()) {
                    line(line);
                }
            }
            br.close();
        }

        public abstract void line(String line);
    }
}
