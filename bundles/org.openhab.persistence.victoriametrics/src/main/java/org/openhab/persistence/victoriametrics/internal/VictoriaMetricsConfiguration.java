/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
 * @author Michael Weger - Remove case conversion, add metric prefix and suffix
 */
@NonNullByDefault
public class VictoriaMetricsConfiguration {
    public static final String URL_PARAM = "url";
    public static final String TOKEN_PARAM = "token";
    public static final String USER_PARAM = "user";
    public static final String PASSWORD_PARAM = "password";
    public static final String METRIC_PREFIX_PARAM = "metricPrefix";
    public static final String METRIC_SUFFIX_PARAM = "metricSuffix";
    public static final String ADD_CATEGORY_TAG_PARAM = "addCategoryTag";
    public static final String ADD_LABEL_TAG_PARAM = "addLabelTag";
    public static final String ADD_TYPE_TAG_PARAM = "addTypeTag";
    public static final String ADD_UNIT_TAG_PARAM = "addUnitTag";
    private final Logger logger = LoggerFactory.getLogger(VictoriaMetricsConfiguration.class);
    private final String url;
    private final String user;
    private final String password;
    private final String token;
    private final String metricPrefix;
    private final String metricSuffix;
    private final boolean addCategoryTag;
    private final boolean addTypeTag;
    private final boolean addLabelTag;
    private final boolean addUnitTag;

    public VictoriaMetricsConfiguration(Map<String, Object> config) {
        url = ConfigParser.valueAsOrElse(config.get(URL_PARAM), String.class, "http://127.0.0.1:8428");
        user = ConfigParser.valueAsOrElse(config.get(USER_PARAM), String.class, "");
        password = ConfigParser.valueAsOrElse(config.get(PASSWORD_PARAM), String.class, "");
        token = ConfigParser.valueAsOrElse(config.get(TOKEN_PARAM), String.class, "");
        metricPrefix = ConfigParser.valueAsOrElse(config.get(METRIC_PREFIX_PARAM), String.class, "");
        metricSuffix = ConfigParser.valueAsOrElse(config.get(METRIC_SUFFIX_PARAM), String.class, "");
        addCategoryTag = ConfigParser.valueAsOrElse(config.get(ADD_CATEGORY_TAG_PARAM), Boolean.class, false);
        addLabelTag = ConfigParser.valueAsOrElse(config.get(ADD_LABEL_TAG_PARAM), Boolean.class, false);
        addTypeTag = ConfigParser.valueAsOrElse(config.get(ADD_TYPE_TAG_PARAM), Boolean.class, false);
        addUnitTag = ConfigParser.valueAsOrElse(config.get(ADD_UNIT_TAG_PARAM), Boolean.class, false);
    }

    public boolean isValid() {
        // VictoriaMetrics single-node does not require authentication, so credentials are optional
        boolean hasUrl = !url.isBlank();
        boolean hasValidCredentials = user.isBlank() || !password.isBlank();
        boolean hasValidPrefix = metricPrefix.isEmpty()
                || VictoriaMetricsMetricNameResolver.isValidMetricName(metricPrefix);
        boolean hasValidSuffix = VictoriaMetricsMetricNameResolver.isValidMetricNameSuffix(metricSuffix);

        if (hasUrl && hasValidCredentials && hasValidPrefix && hasValidSuffix) {
            return true;
        } else {
            String msg = "VictoriaMetrics configuration isn't valid. Addon won't work: ";
            StringJoiner reason = new StringJoiner(",");
            if (!hasUrl) {
                reason.add("No URL defined");
            }
            if (!hasValidCredentials) {
                reason.add("User defined but no password");
            }
            if (!hasValidPrefix) {
                reason.add("Metric prefix is not a valid metric name");
            }
            if (!hasValidSuffix) {
                reason.add("Metric suffix contains characters not allowed in metric names");
            }
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

    public String getMetricPrefix() {
        return metricPrefix;
    }

    public String getMetricSuffix() {
        return metricSuffix;
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

    @Override
    public String toString() {
        return "VictoriaMetricsConfiguration{url='" + url + "', user='" + user + "', password='" + password.length()
                + " chars', token='" + token.length() + " chars', metricPrefix='" + metricPrefix + "', metricSuffix='"
                + metricSuffix + "', addCategoryTag=" + addCategoryTag + ", addTypeTag=" + addTypeTag + ", addLabelTag="
                + addLabelTag + ", addUnitTag=" + addUnitTag + '}';
    }
}
