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
package org.openhab.binding.dbquery.internal.dbimpl.jdbc;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parse JDBC url and extracts properties
 *
 * @author Joan Pujol - Initial contribution. Created from JdbcConfiguration class from Helmut Lehmeyer in JDBC
 *         persistence addon
 */
@NonNullByDefault
public class ParseJDBCUrl {
    private final Logger logger = LoggerFactory.getLogger(ParseJDBCUrl.class);
    private final String url;
    private final Properties properties;
    private boolean correct = false;
    private @Nullable String errorMessage;

    public ParseJDBCUrl(String url) {
        this.url = url;
        this.properties = parse();
    }

    public boolean isCorrectlyParsed() {
        return correct;
    }

    public String getDbShorcut() {
        return (String) properties.getOrDefault("dbShortcut", "");
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    private Properties parse() {
        String parsingUrl = this.url;
        Properties props = new Properties();

        if (parsingUrl.length() < 9) {
            errorMessage = "Url too short";
            return props;
        }

        // replace all \
        if (parsingUrl.contains("\\")) {
            parsingUrl = parsingUrl.replace("\\", "/");
        }

        // replace first ; with ?
        if (parsingUrl.contains(";")) {
            // replace first ; with ?
            parsingUrl = parsingUrl.replaceFirst(";", "?");
            // replace other ; with &
            parsingUrl = parsingUrl.replace(";", "&");
        }

        if (parsingUrl.split(":").length < 3 || !parsingUrl.contains("/")) {
            errorMessage = MessageFormat
                    .format("URI {0} is not well formatted, expected uri like ''jdbc:dbShortcut:/path''", parsingUrl);
            return props;
        }

        String[] protAndDb = stringBeforeSubstr(parsingUrl, ":", 1).split(":");
        if (!"jdbc".equals(protAndDb[0])) {
            errorMessage = MessageFormat.format("URI {0} is not well formatted, expected suffix 'jdbc' found {1}",
                    parsingUrl, protAndDb[0]);
            return props;
        }
        props.put("dbShortcut", protAndDb[1]);

        URI dbURI = null;
        try {
            dbURI = new URI(stringAfterSubstr(parsingUrl, ":", 1).replaceFirst(" ", ""));
            if (dbURI.getScheme() != null) {
                props.put("scheme", dbURI.getScheme());
                dbURI = new URI(stringAfterSubstr(parsingUrl, ":", 2).replaceFirst(" ", ""));
            }
        } catch (URISyntaxException e) {
            logger.warn("URI not well formatted", e);
            errorMessage = MessageFormat.format("parseJdbcURL: URI {0} is not well formatted: {1}", parsingUrl,
                    e.getMessage());
            return props;
        }

        // Query-Parameters
        if (dbURI.getQuery() != null) {
            String[] q = dbURI.getQuery().split("&");
            for (int i = 0; i < q.length; i++) {
                String[] t = q[i].split("=");
                props.put(t[0], t[1]);
            }
            props.put("pathQuery", dbURI.getQuery());
        }

        String path = "";
        if (dbURI.getPath() != null) {
            String gp = dbURI.getPath();
            String st = "/";
            if (gp.indexOf("/") <= 1) {
                if (substrPos(gp, st).size() > 1) {
                    path = stringBeforeLastSubstr(gp, st) + st;
                } else {
                    path = stringBeforeSubstr(gp, st) + st;
                }
            }
            if (dbURI.getScheme() != null && dbURI.getScheme().length() == 1) {
                path = dbURI.getScheme() + ":" + path;
            }
            props.put("serverPath", path);
        }
        if (dbURI.getPath() != null) {
            props.put("databaseName", stringAfterLastSubstr(dbURI.getPath(), "/"));
        }
        if (dbURI.getPort() != -1) {
            props.put("portNumber", dbURI.getPort() + "");
        }
        if (dbURI.getHost() != null) {
            props.put("serverName", dbURI.getHost());
        }

        correct = true;
        return props;
    }

    /**
     * Returns a String before the last occurrence of a substring
     */
    private static String stringBeforeLastSubstr(String s, String substr) {
        List<Integer> a = substrPos(s, substr);
        return s.substring(0, a.get(a.size() - 1));
    }

    /**
     * Returns a String after the last occurrence of a substring
     */
    private static String stringAfterLastSubstr(String s, String substr) {
        List<Integer> a = substrPos(s, substr);
        return s.substring(a.get(a.size() - 1) + 1);
    }

    /**
     * Returns a String after the n occurrence of a substring
     */
    private static String stringAfterSubstr(String s, String substr, int n) {
        return s.substring(substrPos(s, substr).get(n) + 1);
    }

    /**
     * Returns a String before the first occurrence of a substring
     */
    private static String stringBeforeSubstr(String s, String substr) {
        return s.substring(0, s.indexOf(substr));
    }

    /**
     * Returns a String before the n occurrence of a substring.
     */
    private static String stringBeforeSubstr(String s, String substr, int n) {
        return s.substring(0, substrPos(s, substr).get(n));
    }

    /**
     * Returns a list with indices of the occurrence of a substring.
     */
    private static List<Integer> substrPos(String s, String substr) {
        return substrPos(s, substr, true);
    }

    /**
     * Returns a list with indices of the occurrence of a substring.
     */
    private static List<Integer> substrPos(String s, String substr, boolean ignoreCase) {
        int substrLength = substr.length();
        int strLength = s.length();
        List<Integer> arr = new ArrayList<>();

        for (int i = 0; i < strLength - substrLength + 1; i++) {
            if (s.regionMatches(ignoreCase, i, substr, 0, substrLength)) {
                arr.add(i);
            }
        }
        return arr;
    }
}
