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
package org.openhab.persistence.victoriametrics.internal;

import java.util.Map;
import java.util.StringJoiner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.ConfigParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains this addon configurable parameters
 *
 * @author Joan Pujol Espinar - Initial contribution
 * @author Franz - Initial VictoriaMetrics adaptation
 */
@NonNullByDefault
public class VictoriaMetricsConfiguration {
    public static final String URL_PARAM = "url";
    public static final String TOKEN_PARAM = "token";
    public static final String USER_PARAM = "user";
    public static final String PASSWORD_PARAM = "password";
    public static final String ADD_CATEGORY_TAG_PARAM = "addCategoryTag";
    public static final String ADD_LABEL_TAG_PARAM = "addLabelTag";
    public static final String ADD_TYPE_TAG_PARAM = "addTypeTag";
    public static final String ADD_UNIT_TAG_PARAM = "addUnitTag";
    public static final String CAMEL_TO_SNAKE_CASE_PARAM = "camelToSnakeCase";
    public static final String MEASUREMENT_PREFIX = "measurementPrefix";
    private final Logger logger = LoggerFactory.getLogger(VictoriaMetricsConfiguration.class);
    private final String url;
    private final String user;
    private final String password;
    private final String token;
    private final String measurementPrefix;
    private final boolean addCategoryTag;
    private final boolean addTypeTag;
    private final boolean addLabelTag;
    private final boolean addUnitTag;
    private final boolean camelToSnakeCase;

    public VictoriaMetricsConfiguration(Map<String, Object> config) {
        // Set VictoriaMetrics default port
        url = ConfigParser.valueAsOrElse(config.get(URL_PARAM), String.class, "http://127.0.0.1:8428");
        user = ConfigParser.valueAsOrElse(config.get(USER_PARAM), String.class, "openhab");
        password = ConfigParser.valueAsOrElse(config.get(PASSWORD_PARAM), String.class, "");
        token = ConfigParser.valueAsOrElse(config.get(TOKEN_PARAM), String.class, "");
        addCategoryTag = ConfigParser.valueAsOrElse(config.get(ADD_CATEGORY_TAG_PARAM), Boolean.class, false);
        addLabelTag = ConfigParser.valueAsOrElse(config.get(ADD_LABEL_TAG_PARAM), Boolean.class, false);
        addTypeTag = ConfigParser.valueAsOrElse(config.get(ADD_TYPE_TAG_PARAM), Boolean.class, false);
        addUnitTag = ConfigParser.valueAsOrElse(config.get(ADD_UNIT_TAG_PARAM), Boolean.class, false);
        camelToSnakeCase = ConfigParser.valueAsOrElse(config.get(CAMEL_TO_SNAKE_CASE_PARAM), Boolean.class, true);
        measurementPrefix = ConfigParser.valueAsOrElse(config.get(MEASUREMENT_PREFIX), String.class, "openhab_");
    }

    public boolean isValid() {
        // VM OSS: allow blank credentials
        boolean hasBasicCredentials = !user.isBlank();
        boolean hasPassword = !password.isBlank();
        boolean hasToken = !token.isBlank();
        boolean hasValidCredentials = (!hasBasicCredentials || hasPassword) || hasToken;
        if (hasValidCredentials) {
            return true;
        } else {
            String msg = "VictoriaMetrics configuration isn't valid. Addon won't work: ";
            StringJoiner reason = new StringJoiner(",");
            // We have only this reason right now
            reason.add("User defined but no password");
            logger.warn("{} {}", msg, reason);
            return false;
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public String getMeasurementPrefix() {
        return measurementPrefix;
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

    public boolean isAddUnitTag() {
        return addUnitTag;
    }

    public boolean isCamelToSnakeCase() {
        return camelToSnakeCase;
    }

    @Override
    public String toString() {
        return "VictoriaMetricsConfiguration{" + "url='" + url + "', " + "user='" + user + "', " + "password='"
                + password.length() + " chars'" + " , " + "token='" + token.length() + " chars'" + ", "
                + "measurementPrefix='" + measurementPrefix + ", " + "addCategoryTag=" + addCategoryTag + ", "
                + "addTypeTag=" + addTypeTag + ", " + "addLabelTag=" + addLabelTag + ", " + "camelToSnakeCase="
                + camelToSnakeCase + ", " + "addUnitTag=" + addUnitTag + '}';
    }
}
