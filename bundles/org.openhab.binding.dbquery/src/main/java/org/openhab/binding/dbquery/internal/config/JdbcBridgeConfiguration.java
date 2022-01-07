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
package org.openhab.binding.dbquery.internal.config;

import java.util.StringJoiner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Contains fields mapping InfluxDB2 bridge configuration parameters.
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class JdbcBridgeConfiguration {
    private static final int DEFAULT_MAX_POOL_SIZE = 3;
    private static final int DEFAULT_MINIMUM_IDLE = 1;

    private String url;
    private String user;
    private String password;
    @Nullable
    private Integer maxPoolSize = null;
    @Nullable
    private Integer minimumIdle = null;

    public JdbcBridgeConfiguration(String url, String user, String password, @Nullable Integer maxPoolSize,
            @Nullable Integer minimumIdle) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.maxPoolSize = maxPoolSize;
        this.minimumIdle = minimumIdle;
    }

    public JdbcBridgeConfiguration() {
        // Used only when configuration is created by reflection using ConfigMapper
        url = user = password = "";
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

    public Integer getMaxPoolSize() {
        return maxPoolSize != null ? maxPoolSize : DEFAULT_MAX_POOL_SIZE;
    }

    public Integer getMinimumIdle() {
        return minimumIdle != null ? minimumIdle : DEFAULT_MINIMUM_IDLE;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", JdbcBridgeConfiguration.class.getSimpleName() + "[", "]").add("url='" + url + "'")
                .add("user='" + user + "'").add("password='" + password + "'").add("maxPoolSize=" + maxPoolSize)
                .add("minimumIdle=" + minimumIdle).toString();
    }
}
