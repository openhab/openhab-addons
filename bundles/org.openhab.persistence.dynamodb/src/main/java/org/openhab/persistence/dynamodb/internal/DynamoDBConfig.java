/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.persistence.dynamodb.internal;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfilesConfigFile;
import com.amazonaws.regions.Regions;

/**
 * Configuration for DynamoDB connections
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class DynamoDBConfig {
    public static final String DEFAULT_TABLE_PREFIX = "openhab-";
    public static final boolean DEFAULT_CREATE_TABLE_ON_DEMAND = true;
    public static final long DEFAULT_READ_CAPACITY_UNITS = 1;
    public static final long DEFAULT_WRITE_CAPACITY_UNITS = 1;
    public static final long DEFAULT_BUFFER_COMMIT_INTERVAL_MILLIS = 1000;
    public static final int DEFAULT_BUFFER_SIZE = 1000;

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBConfig.class);

    private String tablePrefix = DEFAULT_TABLE_PREFIX;
    private Regions region;
    private AWSCredentials credentials;
    private boolean createTable = DEFAULT_CREATE_TABLE_ON_DEMAND;
    private long readCapacityUnits = DEFAULT_READ_CAPACITY_UNITS;
    private long writeCapacityUnits = DEFAULT_WRITE_CAPACITY_UNITS;
    private long bufferCommitIntervalMillis = DEFAULT_BUFFER_COMMIT_INTERVAL_MILLIS;
    private int bufferSize = DEFAULT_BUFFER_SIZE;

    /**
     *
     * @param config persistence service configuration
     * @return DynamoDB configuration. Returns null in case of configuration errors
     */
    public static @Nullable DynamoDBConfig fromConfig(Map<String, Object> config) {
        try {
            String regionName = (String) config.get("region");
            if (regionName == null) {
                return null;
            }
            final Regions region;
            try {
                region = Regions.fromName(regionName);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Specify valid AWS region to use, got {}. Valid values include: {}", regionName, Arrays
                        .asList(Regions.values()).stream().map(r -> r.getName()).collect(Collectors.joining(",")));
                return null;
            }

            AWSCredentials credentials;
            String accessKey = (String) config.get("accessKey");
            String secretKey = (String) config.get("secretKey");
            if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
                LOGGER.debug("accessKey and secretKey specified. Using those.");
                credentials = new BasicAWSCredentials(accessKey, secretKey);
            } else {
                LOGGER.debug("accessKey and/or secretKey blank. Checking profilesConfigFile and profile.");
                String profilesConfigFile = (String) config.get("profilesConfigFile");
                String profile = (String) config.get("profile");
                if (profilesConfigFile == null || profilesConfigFile.isBlank() || profile == null
                        || profile.isBlank()) {
                    LOGGER.error("Specify either 1) accessKey and secretKey; or 2) profilesConfigFile and "
                            + "profile for providing AWS credentials");
                    return null;
                }
                credentials = new ProfilesConfigFile(profilesConfigFile).getCredentials(profile);
            }

            String table = (String) config.get("tablePrefix");
            if (table == null || table.isBlank()) {
                LOGGER.debug("Using default table name {}", DEFAULT_TABLE_PREFIX);
                table = DEFAULT_TABLE_PREFIX;
            }

            final boolean createTable;
            String createTableParam = (String) config.get("createTable");
            if (createTableParam == null || createTableParam.isBlank()) {
                LOGGER.debug("Creating table on demand: {}", DEFAULT_CREATE_TABLE_ON_DEMAND);
                createTable = DEFAULT_CREATE_TABLE_ON_DEMAND;
            } else {
                createTable = Boolean.parseBoolean(createTableParam);
            }

            final long readCapacityUnits;
            String readCapacityUnitsParam = (String) config.get("readCapacityUnits");
            if (readCapacityUnitsParam == null || readCapacityUnitsParam.isBlank()) {
                LOGGER.debug("Read capacity units: {}", DEFAULT_READ_CAPACITY_UNITS);
                readCapacityUnits = DEFAULT_READ_CAPACITY_UNITS;
            } else {
                readCapacityUnits = Long.parseLong(readCapacityUnitsParam);
            }

            final long writeCapacityUnits;
            String writeCapacityUnitsParam = (String) config.get("writeCapacityUnits");
            if (writeCapacityUnitsParam == null || writeCapacityUnitsParam.isBlank()) {
                LOGGER.debug("Write capacity units: {}", DEFAULT_WRITE_CAPACITY_UNITS);
                writeCapacityUnits = DEFAULT_WRITE_CAPACITY_UNITS;
            } else {
                writeCapacityUnits = Long.parseLong(writeCapacityUnitsParam);
            }

            final long bufferCommitIntervalMillis;
            String bufferCommitIntervalMillisParam = (String) config.get("bufferCommitIntervalMillis");
            if (bufferCommitIntervalMillisParam == null || bufferCommitIntervalMillisParam.isBlank()) {
                LOGGER.debug("Buffer commit interval millis: {}", DEFAULT_BUFFER_COMMIT_INTERVAL_MILLIS);
                bufferCommitIntervalMillis = DEFAULT_BUFFER_COMMIT_INTERVAL_MILLIS;
            } else {
                bufferCommitIntervalMillis = Long.parseLong(bufferCommitIntervalMillisParam);
            }

            final int bufferSize;
            String bufferSizeParam = (String) config.get("bufferSize");
            if (bufferSizeParam == null || bufferSizeParam.isBlank()) {
                LOGGER.debug("Buffer size: {}", DEFAULT_BUFFER_SIZE);
                bufferSize = DEFAULT_BUFFER_SIZE;
            } else {
                bufferSize = Integer.parseInt(bufferSizeParam);
            }

            return new DynamoDBConfig(region, credentials, table, createTable, readCapacityUnits, writeCapacityUnits,
                    bufferCommitIntervalMillis, bufferSize);
        } catch (Exception e) {
            LOGGER.error("Error with configuration", e);
            return null;
        }
    }

    public DynamoDBConfig(Regions region, AWSCredentials credentials, String table, boolean createTable,
            long readCapacityUnits, long writeCapacityUnits, long bufferCommitIntervalMillis, int bufferSize) {
        this.region = region;
        this.credentials = credentials;
        this.tablePrefix = table;
        this.createTable = createTable;
        this.readCapacityUnits = readCapacityUnits;
        this.writeCapacityUnits = writeCapacityUnits;
        this.bufferCommitIntervalMillis = bufferCommitIntervalMillis;
        this.bufferSize = bufferSize;
    }

    public AWSCredentials getCredentials() {
        return credentials;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public Regions getRegion() {
        return region;
    }

    public boolean isCreateTable() {
        return createTable;
    }

    public long getReadCapacityUnits() {
        return readCapacityUnits;
    }

    public long getWriteCapacityUnits() {
        return writeCapacityUnits;
    }

    public long getBufferCommitIntervalMillis() {
        return bufferCommitIntervalMillis;
    }

    public int getBufferSize() {
        return bufferSize;
    }
}
