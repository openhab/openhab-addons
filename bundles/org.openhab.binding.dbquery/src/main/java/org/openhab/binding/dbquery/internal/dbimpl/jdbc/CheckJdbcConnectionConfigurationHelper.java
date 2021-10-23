/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.config.JdbcBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks Jdbc bridge configuration
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
class CheckJdbcConnectionConfigurationHelper {
    private final Logger logger = LoggerFactory.getLogger(CheckJdbcConnectionConfigurationHelper.class);

    private final ParseJDBCUrl parseJDBCUrl;
    private @Nullable String errorMessage;
    private DatabaseType databaseType = DatabaseType.UNKNOWN;

    public CheckJdbcConnectionConfigurationHelper(JdbcBridgeConfiguration configuration) {
        this.parseJDBCUrl = new ParseJDBCUrl(configuration.getUrl());
    }

    public boolean checkSetupCorrectToConnect() {
        if (!parseJDBCUrl.isCorrectlyParsed()) {
            errorMessage = parseJDBCUrl.getErrorMessage();
            return false;
        }

        databaseType = DatabaseType.fromServiceName(parseJDBCUrl.getDbShorcut());
        if (databaseType == DatabaseType.UNKNOWN) {
            errorMessage = "Unknown database type, check addon documentation for supported databases";
            return false;
        }

        if (!checkIfDriverIsPresent()) {
            errorMessage = "Driver not found in classpath, ensure you have deployed in addons folder: "
                    + databaseType.getDriverDownloadURL();
            return false;
        }

        return true;
    }

    public @Nullable String getErrorMessage() {
        return errorMessage;
    }

    private boolean checkIfDriverIsPresent() {
        try {
            Class.forName(databaseType.getDriver());
            return true;
        } catch (ClassNotFoundException e) {
            logger.warn("Driver class can't be initialized {}", databaseType.getDriver(), e);
            return false;
        }
    }
}
