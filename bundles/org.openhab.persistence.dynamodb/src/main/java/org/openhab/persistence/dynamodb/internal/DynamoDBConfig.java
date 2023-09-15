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
package org.openhab.persistence.dynamodb.internal;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.awscore.retry.AwsRetryPolicy;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.profiles.ProfileFile;
import software.amazon.awssdk.profiles.ProfileFile.Type;
import software.amazon.awssdk.profiles.ProfileProperty;
import software.amazon.awssdk.regions.Region;

/**
 * Configuration for DynamoDB connections
 *
 * If table parameter is specified and is not blank, we use new table schema (ExpectedTableRevision.NEW).
 * If tablePrefix parameter is specified and is not blank, we use legacy table schema (ExpectedTableRevision.LEGACY).
 * Other cases conservatively set ExpectedTableRevision.MAYBE_LEGACY, detecting the right schema during runtime.
 *
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class DynamoDBConfig {
    public static final String DEFAULT_TABLE_PREFIX = "openhab-";
    public static final String DEFAULT_TABLE_NAME = "openhab";
    public static final long DEFAULT_READ_CAPACITY_UNITS = 1;
    public static final long DEFAULT_WRITE_CAPACITY_UNITS = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBConfig.class);

    private long readCapacityUnits;
    private long writeCapacityUnits;
    private Region region;
    private AwsCredentials credentials;
    private Optional<RetryPolicy> retryPolicy;
    private ExpectedTableSchema tableRevision;
    private String table;
    private String tablePrefixLegacy;
    private @Nullable Integer expireDays;

    /**
     *
     * @param config persistence service configuration
     * @return DynamoDB configuration. Returns null in case of configuration errors
     */
    public static @Nullable DynamoDBConfig fromConfig(Map<String, Object> config) {
        ExpectedTableSchema tableRevision;
        try {
            String regionName = (String) config.get("region");
            if (regionName == null) {
                return null;
            }
            final Region region;
            if (Region.regions().stream().noneMatch(r -> r.toString().equals(regionName))) {
                LOGGER.warn("Region {} is not matching known regions: {}. The region might not be supported.",
                        regionName, Region.regions().stream().map(r -> r.toString()).collect(Collectors.joining(", ")));
            }
            region = Region.of(regionName);

            Optional<RetryMode> retryMode = Optional.empty();
            AwsCredentials credentials;
            String accessKey = (String) config.get("accessKey");
            String secretKey = (String) config.get("secretKey");
            if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
                LOGGER.debug("accessKey and secretKey specified. Using those.");
                credentials = AwsBasicCredentials.create(accessKey, secretKey);
            } else {
                LOGGER.debug("accessKey and/or secretKey blank. Checking profilesConfigFile and profile.");
                String profilesConfigFile = (String) config.get("profilesConfigFile");
                String profile = (String) config.get("profile");
                if (profilesConfigFile == null || profilesConfigFile.isBlank() || profile == null
                        || profile.isBlank()) {
                    LOGGER.error("""
                            Specify either 1) accessKey and secretKey; or 2) profilesConfigFile and \
                            profile for providing AWS credentials\
                            """);
                    return null;
                }
                ProfileFile profileFile = ProfileFile.builder().content(Path.of(profilesConfigFile))
                        .type(Type.CREDENTIALS).build();
                credentials = ProfileCredentialsProvider.builder().profileFile(profileFile).profileName(profile).build()
                        .resolveCredentials();

                retryMode = profileFile.profile(profile).flatMap(p -> p.property(ProfileProperty.RETRY_MODE))
                        .flatMap(retry_mode -> {
                            for (RetryMode value : RetryMode.values()) {
                                if (retry_mode.equalsIgnoreCase(value.name())) {
                                    return Optional.of(value);
                                }
                            }
                            LOGGER.warn(
                                    "Unknown retry_mode '{}' in profile. Ignoring and using SDK default retry mode.",
                                    retry_mode);
                            return Optional.empty();

                        });
                LOGGER.debug("Retry mode {}", retryMode);
            }

            String table = (String) config.get("table");
            String tablePrefixLegacy;
            if (table == null || table.isBlank()) {
                // the new parameter 'table' has not been set. Check whether the legacy parameter 'tablePrefix' is set
                table = DEFAULT_TABLE_NAME;
                tablePrefixLegacy = (String) config.get("tablePrefix");
                if (tablePrefixLegacy == null || tablePrefixLegacy.isBlank()) {
                    LOGGER.debug("Using default table prefix {}", DEFAULT_TABLE_PREFIX);
                    // No explicit value has been specified for tablePrefix, user could be still using the legacy setup
                    tableRevision = ExpectedTableSchema.MAYBE_LEGACY;
                    tablePrefixLegacy = DEFAULT_TABLE_PREFIX;
                } else {
                    // Explicit value for tablePrefix, user certainly prefers LEGACY
                    tableRevision = ExpectedTableSchema.LEGACY;
                }
            } else {
                tableRevision = ExpectedTableSchema.NEW;
                tablePrefixLegacy = DEFAULT_TABLE_PREFIX;
            }

            final long readCapacityUnits;
            String readCapacityUnitsParam = (String) config.get("readCapacityUnits");
            if (readCapacityUnitsParam == null || readCapacityUnitsParam.isBlank()) {
                readCapacityUnits = DEFAULT_READ_CAPACITY_UNITS;
            } else {
                readCapacityUnits = Long.parseLong(readCapacityUnitsParam);
            }

            final long writeCapacityUnits;
            String writeCapacityUnitsParam = (String) config.get("writeCapacityUnits");
            if (writeCapacityUnitsParam == null || writeCapacityUnitsParam.isBlank()) {
                writeCapacityUnits = DEFAULT_WRITE_CAPACITY_UNITS;
            } else {
                writeCapacityUnits = Long.parseLong(writeCapacityUnitsParam);
            }

            final @Nullable Integer expireDays;
            String expireDaysString = (String) config.get("expireDays");
            if (expireDaysString == null || expireDaysString.isBlank()) {
                expireDays = null;
            } else {
                expireDays = Integer.parseInt(expireDaysString);
                if (expireDays <= 0) {
                    LOGGER.error("expireDays should be positive integer or null");
                    return null;
                }
            }

            switch (tableRevision) {
                case NEW:
                    LOGGER.debug("Using new DynamoDB table schema");
                    return DynamoDBConfig.newSchema(region, credentials, retryMode.map(AwsRetryPolicy::forRetryMode),
                            table, readCapacityUnits, writeCapacityUnits, expireDays);
                case LEGACY:
                    LOGGER.warn(
                            "Using legacy DynamoDB table schema. It is recommended to transition to new schema by defining 'table' parameter and not configuring 'tablePrefix'");
                    return DynamoDBConfig.legacySchema(region, credentials, retryMode.map(AwsRetryPolicy::forRetryMode),
                            tablePrefixLegacy, readCapacityUnits, writeCapacityUnits);
                case MAYBE_LEGACY:
                    LOGGER.debug(
                            "Unclear whether we should use new legacy DynamoDB table schema. It is recommended to explicitly define new 'table' parameter. The correct table schema will be detected at runtime.");
                    return DynamoDBConfig.maybeLegacySchema(region, credentials,
                            retryMode.map(AwsRetryPolicy::forRetryMode), table, tablePrefixLegacy, readCapacityUnits,
                            writeCapacityUnits, expireDays);
                default:
                    throw new IllegalStateException("Unhandled enum. Bug");
            }
        } catch (Exception e) {
            LOGGER.error("Error with configuration: {} {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    private static DynamoDBConfig newSchema(Region region, AwsCredentials credentials,
            Optional<RetryPolicy> retryPolicy, String table, long readCapacityUnits, long writeCapacityUnits,
            @Nullable Integer expireDays) {
        return new DynamoDBConfig(region, credentials, retryPolicy, table, "", ExpectedTableSchema.NEW,
                readCapacityUnits, writeCapacityUnits, expireDays);
    }

    private static DynamoDBConfig legacySchema(Region region, AwsCredentials credentials,
            Optional<RetryPolicy> retryPolicy, String tablePrefixLegacy, long readCapacityUnits,
            long writeCapacityUnits) {
        return new DynamoDBConfig(region, credentials, retryPolicy, "", tablePrefixLegacy, ExpectedTableSchema.LEGACY,
                readCapacityUnits, writeCapacityUnits, null);
    }

    private static DynamoDBConfig maybeLegacySchema(Region region, AwsCredentials credentials,
            Optional<RetryPolicy> retryPolicy, String table, String tablePrefixLegacy, long readCapacityUnits,
            long writeCapacityUnits, @Nullable Integer expireDays) {
        return new DynamoDBConfig(region, credentials, retryPolicy, table, tablePrefixLegacy,
                ExpectedTableSchema.MAYBE_LEGACY, readCapacityUnits, writeCapacityUnits, expireDays);
    }

    private DynamoDBConfig(Region region, AwsCredentials credentials, Optional<RetryPolicy> retryPolicy, String table,
            String tablePrefixLegacy, ExpectedTableSchema tableRevision, long readCapacityUnits,
            long writeCapacityUnits, @Nullable Integer expireDays) {
        this.region = region;
        this.credentials = credentials;
        this.retryPolicy = retryPolicy;
        this.table = table;
        this.tablePrefixLegacy = tablePrefixLegacy;
        this.tableRevision = tableRevision;
        this.readCapacityUnits = readCapacityUnits;
        this.writeCapacityUnits = writeCapacityUnits;
        this.expireDays = expireDays;
    }

    public AwsCredentials getCredentials() {
        return credentials;
    }

    public String getTablePrefixLegacy() {
        return tablePrefixLegacy;
    }

    public String getTable() {
        return table;
    }

    public ExpectedTableSchema getTableRevision() {
        return tableRevision;
    }

    public Region getRegion() {
        return region;
    }

    public long getReadCapacityUnits() {
        return readCapacityUnits;
    }

    public long getWriteCapacityUnits() {
        return writeCapacityUnits;
    }

    public Optional<RetryPolicy> getRetryPolicy() {
        return retryPolicy;
    }

    public @Nullable Integer getExpireDays() {
        return expireDays;
    }
}
