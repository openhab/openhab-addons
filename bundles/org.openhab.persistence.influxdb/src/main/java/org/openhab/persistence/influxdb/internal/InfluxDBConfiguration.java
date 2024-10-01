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
package org.openhab.persistence.influxdb.internal;

import java.util.Map;
import java.util.StringJoiner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.ConfigParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains this addon configurable parameters
 *
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
public class InfluxDBConfiguration {
    public static final String URL_PARAM = "url";
    public static final String TOKEN_PARAM = "token";
    public static final String USER_PARAM = "user";
    public static final String PASSWORD_PARAM = "password";
    public static final String DATABASE_PARAM = "db";
    public static final String RETENTION_POLICY_PARAM = "retentionPolicy";
    public static final String VERSION_PARAM = "version";
    public static final String REPLACE_UNDERSCORE_PARAM = "replaceUnderscore";
    public static final String ADD_CATEGORY_TAG_PARAM = "addCategoryTag";
    public static final String ADD_LABEL_TAG_PARAM = "addLabelTag";
    public static final String ADD_TYPE_TAG_PARAM = "addTypeTag";
    private final Logger logger = LoggerFactory.getLogger(InfluxDBConfiguration.class);
    private final String url;
    private final String user;
    private final String password;
    private final String token;
    private final String databaseName;
    private final String retentionPolicy;
    private final InfluxDBVersion version;
    private final boolean replaceUnderscore;
    private final boolean addCategoryTag;
    private final boolean addTypeTag;
    private final boolean addLabelTag;

    public InfluxDBConfiguration(Map<String, Object> config) {
        url = ConfigParser.valueAsOrElse(config.get(URL_PARAM), String.class, "http://127.0.0.1:8086");
        user = ConfigParser.valueAsOrElse(config.get(USER_PARAM), String.class, "openhab");
        password = ConfigParser.valueAsOrElse(config.get(PASSWORD_PARAM), String.class, "");
        token = ConfigParser.valueAsOrElse(config.get(TOKEN_PARAM), String.class, "");
        databaseName = ConfigParser.valueAsOrElse(config.get(DATABASE_PARAM), String.class, "openhab");
        retentionPolicy = ConfigParser.valueAsOrElse(config.get(RETENTION_POLICY_PARAM), String.class, "autogen");
        version = parseInfluxVersion((String) config.getOrDefault(VERSION_PARAM, InfluxDBVersion.V1.name()));
        replaceUnderscore = ConfigParser.valueAsOrElse(config.get(REPLACE_UNDERSCORE_PARAM), Boolean.class, false);
        addCategoryTag = ConfigParser.valueAsOrElse(config.get(ADD_CATEGORY_TAG_PARAM), Boolean.class, false);
        addLabelTag = ConfigParser.valueAsOrElse(config.get(ADD_LABEL_TAG_PARAM), Boolean.class, false);
        addTypeTag = ConfigParser.valueAsOrElse(config.get(ADD_TYPE_TAG_PARAM), Boolean.class, false);
    }

    private InfluxDBVersion parseInfluxVersion(@Nullable String value) {
        try {
            return value != null ? InfluxDBVersion.valueOf(value) : InfluxDBVersion.UNKNOWN;
        } catch (RuntimeException e) {
            logger.warn("Invalid version {}", value);
            return InfluxDBVersion.UNKNOWN;
        }
    }

    public boolean isValid() {
        boolean hasVersion = version != InfluxDBVersion.UNKNOWN;
        boolean hasCredentials = false;
        if (version == InfluxDBVersion.V1) {
            hasCredentials = !user.isBlank() && !password.isBlank();
        } else if (version == InfluxDBVersion.V2) {
            hasCredentials = !token.isBlank() || (!user.isBlank() && !password.isBlank());
        }
        boolean hasDatabase = !databaseName.isBlank();
        boolean hasRetentionPolicy = !retentionPolicy.isBlank();

        boolean valid = hasVersion && hasCredentials && hasDatabase && hasRetentionPolicy;
        if (valid) {
            return true;
        } else {
            String msg = "InfluxDB configuration isn't valid. Addon won't work: ";
            StringJoiner reason = new StringJoiner(",");
            if (!hasVersion) {
                reason.add("Unknown version");
            } else {
                if (!hasCredentials) {
                    reason.add("No credentials");
                }
                if (!hasDatabase) {
                    reason.add("No database name / organization defined");
                }
                if (!hasRetentionPolicy) {
                    reason.add("No retention policy / bucket defined");
                }
            }
            logger.warn("{} {}", msg, reason);
            return false;
        }
    }

    public String getUrl() {
        return url;
    }

    public String getToken() {
        return token;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getRetentionPolicy() {
        return retentionPolicy;
    }

    public boolean isReplaceUnderscore() {
        return replaceUnderscore;
    }

    public boolean isAddCategoryTag() {
        return addCategoryTag;
    }

    public boolean isAddTypeTag() {
        return addTypeTag;
    }

    public boolean isAddLabelTag() {
        return addLabelTag;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public InfluxDBVersion getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "InfluxDBConfiguration{url='" + url + "', user='" + user + "', password='" + password.length()
                + " chars', token='" + token.length() + " chars', databaseName='" + databaseName
                + "', retentionPolicy='" + retentionPolicy + "', version=" + version + ", replaceUnderscore="
                + replaceUnderscore + ", addCategoryTag=" + addCategoryTag + ", addTypeTag=" + addTypeTag
                + ", addLabelTag=" + addLabelTag + '}';
    }
}
