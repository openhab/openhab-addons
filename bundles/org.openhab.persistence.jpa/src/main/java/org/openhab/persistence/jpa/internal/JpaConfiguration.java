/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.persistence.jpa.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The configuration required for Jpa binding.
 *
 * @author Manfred Bergmann - Initial contribution
 * @author Kai Kreuzer - migrated to 3.x
 *
 */
@NonNullByDefault
public class JpaConfiguration {
    private final Logger logger = LoggerFactory.getLogger(JpaConfiguration.class);

    private static final String CFG_CONNECTION_URL = "url";
    private static final String CFG_DRIVER_CLASS = "driver";
    private static final String CFG_USERNAME = "user";
    private static final String CFG_PASSWORD = "password";
    private static final String CFG_SYNCMAPPING = "syncmappings";

    public final String dbConnectionUrl;
    public final String dbDriverClass;
    public final String dbUserName;
    public final String dbPassword;
    public final String dbSyncMapping;

    public JpaConfiguration(final Map<String, @Nullable Object> properties) throws IllegalArgumentException {
        logger.debug("Creating JPA config...");

        String param = (String) properties.get(CFG_CONNECTION_URL);
        logger.debug("url: {}", param);
        if (param == null) {
            throw new IllegalArgumentException("Connection URL is required in JPA configuration!");
        } else if (param.isBlank()) {
            throw new IllegalArgumentException("Empty connection URL in JPA configuration!");
        }
        dbConnectionUrl = param;

        param = (String) properties.get(CFG_DRIVER_CLASS);
        logger.debug("driver: {}", param);
        if (param == null) {
            throw new IllegalArgumentException("Driver class is required in JPA configuration!");
        } else if (param.isBlank()) {
            throw new IllegalArgumentException("Empty driver class in JPA configuration!");
        }
        dbDriverClass = param;

        param = (String) properties.get(CFG_USERNAME);
        if (param == null) {
            logger.info("{} was not specified in JPA configuration!", CFG_USERNAME);
        }
        dbUserName = param == null ? "" : param;

        param = (String) properties.get(CFG_PASSWORD);
        if (param == null) {
            logger.info("{} was not specified in JPA configuration!", CFG_PASSWORD);
        }
        dbPassword = param == null ? "" : param;

        param = (String) properties.get(CFG_SYNCMAPPING);
        if (param == null) {
            logger.debug("{} was not specified in JPA configuration!", CFG_SYNCMAPPING);
        }
        dbSyncMapping = param == null ? "" : param;

        logger.debug("Creating JPA config... done");
    }
}
