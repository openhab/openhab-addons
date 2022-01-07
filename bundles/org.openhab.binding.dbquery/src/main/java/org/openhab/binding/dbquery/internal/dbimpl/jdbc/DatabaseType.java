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

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enum with supported database types and required information to support them
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public enum DatabaseType {
    DERBY("derby", "org.apache.derby.jdbc.EmbeddedDriver",
            "Derby: version [10.12,11) from http://mvnrepository.com/artifact/org.apache.derby/derby"),
    H2("h2", "org.h2.Driver", "H2: version [1.4,2) from http://mvnrepository.com/artifact/com.h2database/h2"),
    HSQLDB("hsqldb", "org.hsqldb.jdbcDriver",
            "HSQLDB: version [2.3,3) from http://mvnrepository.com/artifact/org.hsqldb/hsqldb"),
    MARIADB("mariadb", "org.mariadb.jdbc.Driver",
            "MariaDB: version [1.3,2) from http://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client"),
    MYSQL("mysql", "com.mysql.jdbc.Driver",
            "MySQL: version [8.0,9) from http://mvnrepository.com/artifact/mysql/mysql-connector-java"),
    POSTGRESQL("postgresql", "org.postgresql.Driver",
            "PostgreSQL: version  [42.2,43) from http://mvnrepository.com/artifact/org.postgresql/postgresql"),
    SQLITE("sqlite", "org.sqlite.JDBC",
            "SQLite: version [3.16,4) from http://mvnrepository.com/artifact/org.xerial/sqlite-jdbc"),
    UNKNOWN("unknown", "no driver", "no driver");

    private String serviceName;
    private String driver;
    private String driverDownloadURL;

    DatabaseType(String serviceName, String driver, String driverDownloadURL) {
        this.serviceName = serviceName;
        this.driver = driver;
        this.driverDownloadURL = driverDownloadURL;
    }

    public static DatabaseType fromServiceName(String serviceName) {
        return Arrays.stream(values()).filter(databaseType -> databaseType.serviceName.equals(serviceName)).findFirst()
                .orElse(UNKNOWN);
    }

    public boolean isValid() {
        return this != UNKNOWN;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDriver() {
        return driver;
    }

    public String getDriverDownloadURL() {
        return driverDownloadURL;
    }
}
