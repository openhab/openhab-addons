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
package org.openhab.persistence.jdbc.internal.utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.persistence.FilterCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class
 *
 * @author Helmut Lehmeyer - Initial contribution
 */
@NonNullByDefault
public class StringUtilsExt {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringUtilsExt.class);

    /**
     * Replaces multiple found words with the given Array contents
     *
     * @param str String for replacement
     * @param separate A String or Array to be replaced
     * @param separators Array will be merged to str
     * @return
     */
    public static String replaceArrayMerge(String str, String separate, Object[] separators) {
        String s = str;
        for (int i = 0; i < separators.length; i++) {
            s = s.replaceAll(separate, (String) separators[i]);
        }
        return s;
    }

    /**
     * @see #replaceArrayMerge(String str, String separate, Object[] separators)
     */
    public static String replaceArrayMerge(String str, String[] separate, String[] separators) {
        String s = str;
        for (int i = 0; i < separators.length; i++) {
            s = s.replaceAll(separate[i], separators[i]);
        }
        return s;
    }

    /**
     * @see #parseJdbcURL(String url, Properties def)
     */
    public static Properties parseJdbcURL(String url) {
        return parseJdbcURL(url, null);
    }

    /**
     * <b>JDBC-URI Examples:</b><br/>
     *
     * <pre>
     * {@code
     * jdbc:dbShortcut:c:/dev/databaseName<br/>
     * jdbc:dbShortcut:scheme:c:/dev/databaseName<br/>
     * jdbc:dbShortcut:scheme:c:\\dev\\databaseName<br/>
     * jdbc:dbShortcut:./databaseName<br/>
     * jdbc:dbShortcut:/databaseName<br/>
     * jdbc:dbShortcut:~/databaseName<br/>
     * jdbc:dbShortcut:/path/databaseName.db<br/>
     * jdbc:dbShortcut:./../../path/databaseName<br/>
     * jdbc:dbShortcut:scheme:./path/../path/databaseName;param1=true;<br/>
     * jdbc:dbShortcut://192.168.0.145:3306/databaseName?param1=false&param2=true
     * }
     * </pre>
     * <p>
     *
     * @param url JDBC-URI
     * @param def Predefined Properties Object
     * @return A merged Properties Object may contain:<br/>
     *         parseValid (mandatory)<br/>
     *         scheme<br/>
     *         serverPath<br/>
     *         dbShortcut<br/>
     *         databaseName<br/>
     *         portNumber<br/>
     *         serverName<br/>
     *         pathQuery<br/>
     */
    public static Properties parseJdbcURL(String url, @Nullable Properties def) {
        Properties props;
        if (def == null) {
            props = new Properties();
        } else {
            props = new Properties(def);
        }

        if (url.length() < 9) {
            return props;
        }

        // replace all \
        if (url.contains("\\")) {
            url = url.replaceAll("\\\\", "/");
        }

        // replace first ; with ?
        if (url.contains(";")) {
            // replace first ; with ?
            url = url.replaceFirst(";", "?");
            // replace other ; with &
            url = url.replace(";", "&");
        }

        if (url.split(":").length < 3 || url.indexOf("/") == -1) {
            LOGGER.error("parseJdbcURL: URI '{}' is not well formated, expected uri like 'jdbc:dbShortcut:/path'", url);
            props.put("parseValid", "false");
            return props;
        }

        String[] protAndDb = stringBeforeSubstr(url, ":", 1).split(":");
        if (!"jdbc".equals(protAndDb[0])) {
            LOGGER.error("parseJdbcURL: URI '{}' is not well formated, expected suffix 'jdbc' found '{}'", url,
                    protAndDb[0]);
            props.put("parseValid", "false");
            return props;
        }
        props.put("parseValid", "true");
        props.put("dbShortcut", protAndDb[1]);

        URI dbURI = null;
        try {
            dbURI = new URI(stringAfterSubstr(url, ":", 1).replaceFirst(" ", ""));
            if (dbURI.getScheme() != null) {
                props.put("scheme", dbURI.getScheme());
                dbURI = new URI(stringAfterSubstr(url, ":", 2).replaceFirst(" ", ""));
            }
        } catch (URISyntaxException e) {
            LOGGER.error("parseJdbcURL: URI '{}' is not well formated.", url, e);
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

        String pathURI = dbURI.getPath();
        if (pathURI != null) {
            String path = "";
            if ((pathURI.indexOf("/") >= 0) && (pathURI.indexOf("/") <= 1)) {
                if (stringAfterSubstr(pathURI, "/").contains("/")) {
                    path = stringBeforeLastSubstr(pathURI, "/") + "/";
                } else {
                    path = stringBeforeSubstr(pathURI, "/") + "/";
                }
            }
            String schemeURI = dbURI.getScheme();
            if (schemeURI != null && schemeURI.length() == 1) {
                path = schemeURI + ":" + path;
            }
            props.put("serverPath", path);
            props.put("databaseName", pathURI.contains("/") ? stringAfterLastSubstr(pathURI, "/") : pathURI);
        }
        if (dbURI.getPort() != -1) {
            props.put("portNumber", dbURI.getPort() + "");
        }
        if (dbURI.getHost() != null) {
            props.put("serverName", dbURI.getHost());
        }

        return props;
    }

    /**
     * Returns a String before the last occurrence of a substring
     */
    public static String stringBeforeLastSubstr(String s, String substr) {
        List<Integer> a = substrPos(s, substr);
        return s.substring(0, a.get(a.size() - 1));
    }

    /**
     * Returns a String after the last occurrence of a substring
     */
    public static String stringAfterLastSubstr(String s, String substr) {
        List<Integer> a = substrPos(s, substr);
        return s.substring(a.get(a.size() - 1) + 1);
    }

    /**
     * Returns a String after the first occurrence of a substring
     */
    public static String stringAfterSubstr(String s, String substr) {
        return s.substring(s.indexOf(substr) + 1);
    }

    /**
     * Returns a String after the n occurrence of a substring
     */
    public static String stringAfterSubstr(String s, String substr, int n) {
        return s.substring(substrPos(s, substr).get(n) + 1);
    }

    /**
     * Returns a String before the first occurrence of a substring
     */
    public static String stringBeforeSubstr(String s, String substr) {
        return s.substring(0, s.indexOf(substr));
    }

    /**
     * Returns a String before the n occurrence of a substring.
     */
    public static String stringBeforeSubstr(String s, String substr, int n) {
        return s.substring(0, substrPos(s, substr).get(n));
    }

    /**
     * Returns a list with indices of the occurrence of a substring.
     */
    public static List<Integer> substrPos(String s, String substr) {
        return substrPos(s, substr, true);
    }

    /**
     * Returns a list with indices of the occurrence of a substring.
     */
    public static List<Integer> substrPos(String s, String substr, boolean ignoreCase) {
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

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    public static String filterToString(FilterCriteria filter) {
        StringBuilder builder = new StringBuilder();
        builder.append("FilterCriteria [itemName=");
        builder.append(filter.getItemName());
        builder.append(", beginDate=");
        builder.append(filter.getBeginDate());
        builder.append(", endDate=");
        builder.append(filter.getEndDate());
        builder.append(", pageNumber=");
        builder.append(filter.getPageNumber());
        builder.append(", pageSize=");
        builder.append(filter.getPageSize());
        builder.append(", operator=");
        builder.append(filter.getOperator());
        builder.append(", ordering=");
        builder.append(filter.getOrdering());
        builder.append(", state=");
        builder.append(filter.getState());
        builder.append("]");
        return builder.toString();
    }
}
