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
package org.openhab.persistence.jdbc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link JdbcPersistenceServiceConstants} class defines common constants, which are
 * used across the whole persistence service.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class JdbcPersistenceServiceConstants {

    public static final String SERVICE_ID = "jdbc";
    public static final String SERVICE_LABEL = "JDBC";
    public static final String CONFIG_URI = "persistence:jdbc";
}
