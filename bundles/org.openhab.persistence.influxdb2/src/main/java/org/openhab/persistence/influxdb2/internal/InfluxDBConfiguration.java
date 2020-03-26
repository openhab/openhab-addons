/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.persistence.influxdb2.internal;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Contains this addon configurable parameters
 *
 * @author Joan Pujol Espinar - Initial contribution
 */
@NonNullByDefault
public class InfluxDBConfiguration {
    public static final String URL_PARAM = "url";
    public static final String TOKEN_PARAM = "token";
    public static final String ORGANIZATION_PARAM = "organization";
    public static final String BUCKET_PARAM = "bucket";
    public static final String REPLACE_UNDERSCORE_PARAM = "replaceUnderscore";
    public static final String ADD_CATEGORY_TAG_PARAM = "addCategoryTag";
    public static final String ADD_LABEL_TAG_PARAM = "addLabelTag";
    public static final String ADD_TYPE_TAG_PARAM = "addTypeTag";

    public static InfluxDBConfiguration NO_CONFIGURATION = new InfluxDBConfiguration(Collections.emptyMap());
    private String url;
    private String token;
    private String organization;
    private String bucket;

    private boolean replaceUnderscore;
    private boolean addCategoryTag;
    private boolean addTypeTag;
    private boolean addLabelTag;

    @SuppressWarnings("null")
    public InfluxDBConfiguration(Map<String, @Nullable Object> config) {
        url = (String) config.getOrDefault(URL_PARAM, "http://127.0.0.1:9999");
        token = (String) config.getOrDefault(TOKEN_PARAM, "");
        organization = (String) config.getOrDefault(ORGANIZATION_PARAM, "openhab");
        bucket = (String) config.getOrDefault(BUCKET_PARAM, "default");

        replaceUnderscore = getConfigBooleanValue(config, REPLACE_UNDERSCORE_PARAM, false);
        addCategoryTag = getConfigBooleanValue(config, ADD_CATEGORY_TAG_PARAM, false);
        addLabelTag = getConfigBooleanValue(config, ADD_LABEL_TAG_PARAM, false);
        addTypeTag = getConfigBooleanValue(config, ADD_TYPE_TAG_PARAM, false);
    }

    private static boolean getConfigBooleanValue(Map<String, @Nullable Object> config, String key,
            boolean defaultValue) {
        Object object = config.get(key);

        if (object instanceof Boolean) {
            return (Boolean) object;
        } else if (object != null) {
            return "true".equalsIgnoreCase((String) object);
        } else {
            return defaultValue;
        }
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(url) && StringUtils.isNotBlank(token) && StringUtils.isNotBlank(organization)
                && StringUtils.isNotBlank(bucket);
    }

    public String getUrl() {
        return url;
    }

    public String getToken() {
        return token;
    }

    public String getOrganization() {
        return organization;
    }

    public String getBucket() {
        return bucket;
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

    @Override
    public String toString() {
        return "Configuration{" + "url='" + url + '\'' + ", token='" + token + '\'' + ", organization='" + organization
                + '\'' + ", bucket='" + bucket + '\'' + ", replaceUnderscore=" + replaceUnderscore + ", addCategoryTag="
                + addCategoryTag + ", addTypeTag=" + addTypeTag + ", addLabelTag=" + addLabelTag + '}';
    }

    public int getTokenLength() {
        return token.length();
    }

    public char[] getTokenAsCharArray() {
        return token.toCharArray();
    }
}
