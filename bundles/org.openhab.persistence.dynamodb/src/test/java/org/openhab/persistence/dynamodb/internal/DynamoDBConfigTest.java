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
package org.openhab.persistence.dynamodb.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.regions.Region;

/**
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class DynamoDBConfigTest {

    private static Map<String, Object> mapFrom(String... args) {
        assert args.length % 2 == 0;
        Map<String, String> config = new HashMap<>();
        for (int i = 1; i < args.length; i++) {
            String key = args[i - 1];
            String val = args[i];
            config.put(key, val);
        }
        return Collections.unmodifiableMap(config);
    }

    public @TempDir @NonNullByDefault({}) File folder;

    @Test
    public void testEmpty() throws Exception {
        assertNull(DynamoDBConfig.fromConfig(new HashMap<>()));
    }

    @Test
    public void testInvalidRegion() throws Exception {
        assertNull(DynamoDBConfig.fromConfig(Map.of("region", "foobie")));
    }

    @Test
    public void testRegionOnly() throws Exception {
        assertNull(DynamoDBConfig.fromConfig(Map.of("region", "eu-west-1")));
    }

    @Test
    public void testRegionWithAccessKeys() throws Exception {
        DynamoDBConfig fromConfig = DynamoDBConfig
                .fromConfig(mapFrom("region", "eu-west-1", "accessKey", "access1", "secretKey", "secret1"));
        assert fromConfig != null;
        assertEquals(Region.EU_WEST_1, fromConfig.getRegion());
        assertEquals("access1", fromConfig.getCredentials().accessKeyId());
        assertEquals("secret1", fromConfig.getCredentials().secretAccessKey());
        assertEquals("openhab-", fromConfig.getTablePrefixLegacy());
        assertEquals(1, fromConfig.getReadCapacityUnits());
        assertEquals(1, fromConfig.getWriteCapacityUnits());
        assertEquals(Optional.empty(), fromConfig.getRetryPolicy().map(RetryPolicy::retryMode));
        assertEquals(ExpectedTableSchema.MAYBE_LEGACY, fromConfig.getTableRevision());
    }

    @SuppressWarnings("null")
    @Test
    public void testRegionWithProfilesConfigFile() throws Exception {
        Path credsFile = Files.createFile(Paths.get(folder.getPath(), "creds"));
        Files.write(credsFile, ("""
                [fooprofile]
                aws_access_key_id=testAccessKey
                aws_secret_access_key=testSecretKey
                aws_session_token=testSessionToken
                """).getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        DynamoDBConfig fromConfig = DynamoDBConfig.fromConfig(mapFrom("region", "eu-west-1", "profilesConfigFile",
                credsFile.toAbsolutePath().toString(), "profile", "fooprofile"));
        assertNotNull(fromConfig);
        assertEquals(Region.EU_WEST_1, fromConfig.getRegion());
        assertEquals("openhab-", fromConfig.getTablePrefixLegacy());
        assertEquals(1, fromConfig.getReadCapacityUnits());
        assertEquals(1, fromConfig.getWriteCapacityUnits());
        assertEquals(Optional.empty(), fromConfig.getRetryPolicy().map(RetryPolicy::retryMode));
        assertEquals(ExpectedTableSchema.MAYBE_LEGACY, fromConfig.getTableRevision());
    }

    @SuppressWarnings("null")
    @Test
    public void testProfilesConfigFileRetryMode() throws Exception {
        Path credsFile = Files.createFile(Paths.get(folder.getPath(), "creds"));
        Files.write(credsFile, ("""
                [fooprofile]
                aws_access_key_id=testAccessKey
                aws_secret_access_key=testSecretKey
                aws_session_token=testSessionToken
                retry_mode=legacy\
                """).getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        DynamoDBConfig fromConfig = DynamoDBConfig.fromConfig(mapFrom("region", "eu-west-1", "profilesConfigFile",
                credsFile.toAbsolutePath().toString(), "profile", "fooprofile"));
        assertNotNull(fromConfig);
        assertEquals(Region.EU_WEST_1, fromConfig.getRegion());
        assertEquals("openhab-", fromConfig.getTablePrefixLegacy());
        assertEquals(1, fromConfig.getReadCapacityUnits());
        assertEquals(1, fromConfig.getWriteCapacityUnits());
        assertEquals(Optional.of(RetryMode.LEGACY), fromConfig.getRetryPolicy().map(RetryPolicy::retryMode));
        assertEquals(ExpectedTableSchema.MAYBE_LEGACY, fromConfig.getTableRevision());
    }

    @Test
    public void testEmptyConfiguration() throws Exception {
        assertNull(DynamoDBConfig.fromConfig(mapFrom()));
    }

    @Test
    public void testRegionWithInvalidProfilesConfigFile() throws Exception {
        Path credsFile = Files.createFile(Paths.get(folder.getPath(), "creds"));
        Files.write(credsFile, ("""
                [fooprofile]
                aws_access_key_idINVALIDKEY=testAccessKey
                aws_secret_access_key=testSecretKey
                aws_session_token=testSessionToken
                """).getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        assertNull(DynamoDBConfig.fromConfig(mapFrom("region", "eu-west-1", "profilesConfigFile",
                credsFile.toFile().getAbsolutePath(), "profile", "fooprofile")));
    }

    @Test
    public void testRegionWithProfilesConfigFileMissingProfile() throws Exception {
        Path credsFile = Files.createFile(Paths.get(folder.getPath(), "creds"));
        Files.write(credsFile, ("""
                [fooprofile]
                aws_access_key_id=testAccessKey
                aws_secret_access_key=testSecretKey
                aws_session_token=testSessionToken
                """).getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

        assertNull(DynamoDBConfig.fromConfig(
                mapFrom("region", "eu-west-1", "profilesConfigFile", credsFile.toAbsolutePath().toString())));
    }

    @SuppressWarnings("null")
    @Test
    public void testRegionWithAccessKeysWithLegacyPrefix() throws Exception {
        DynamoDBConfig fromConfig = DynamoDBConfig.fromConfig(mapFrom("region", "eu-west-1", "accessKey", "access1",
                "secretKey", "secret1", "tablePrefix", "foobie-", "expireDays", "105"));
        assertEquals(Region.EU_WEST_1, fromConfig.getRegion());
        assertEquals("access1", fromConfig.getCredentials().accessKeyId());
        assertEquals("secret1", fromConfig.getCredentials().secretAccessKey());
        assertEquals("foobie-", fromConfig.getTablePrefixLegacy());
        assertEquals(1, fromConfig.getReadCapacityUnits());
        assertEquals(1, fromConfig.getWriteCapacityUnits());
        assertEquals(Optional.empty(), fromConfig.getRetryPolicy().map(RetryPolicy::retryMode));
        assertEquals(ExpectedTableSchema.LEGACY, fromConfig.getTableRevision());
        assertNull(fromConfig.getExpireDays()); // not supported with legacy
    }

    @SuppressWarnings("null")
    @Test
    public void testRegionWithAccessKeysWithTable() throws Exception {
        DynamoDBConfig fromConfig = DynamoDBConfig.fromConfig(mapFrom("region", "eu-west-1", "accessKey", "access1",
                "secretKey", "secret1", "table", "mytable", "expireDays", "105"));
        assertEquals(Region.EU_WEST_1, fromConfig.getRegion());
        assertEquals("access1", fromConfig.getCredentials().accessKeyId());
        assertEquals("secret1", fromConfig.getCredentials().secretAccessKey());
        assertEquals("mytable", fromConfig.getTable());
        assertEquals(1, fromConfig.getReadCapacityUnits());
        assertEquals(1, fromConfig.getWriteCapacityUnits());
        assertEquals(Optional.empty(), fromConfig.getRetryPolicy().map(RetryPolicy::retryMode));
        assertEquals(ExpectedTableSchema.NEW, fromConfig.getTableRevision());
        assertEquals(105, fromConfig.getExpireDays());
    }

    @SuppressWarnings("null")
    @Test
    public void testRegionWithAccessKeysWithoutPrefixWithReadCapacityUnits() throws Exception {
        DynamoDBConfig fromConfig = DynamoDBConfig.fromConfig(mapFrom("region", "eu-west-1", "accessKey", "access1",
                "secretKey", "secret1", "readCapacityUnits", "5", "expireDays", "105"));
        assertEquals(Region.EU_WEST_1, fromConfig.getRegion());
        assertEquals("access1", fromConfig.getCredentials().accessKeyId());
        assertEquals("secret1", fromConfig.getCredentials().secretAccessKey());
        assertEquals("openhab-", fromConfig.getTablePrefixLegacy());
        assertEquals(5, fromConfig.getReadCapacityUnits());
        assertEquals(1, fromConfig.getWriteCapacityUnits());
        assertEquals(Optional.empty(), fromConfig.getRetryPolicy().map(RetryPolicy::retryMode));
        assertEquals(ExpectedTableSchema.MAYBE_LEGACY, fromConfig.getTableRevision());
        assertEquals(105, fromConfig.getExpireDays());
    }

    @SuppressWarnings("null")
    @Test
    public void testRegionWithAccessKeysWithoutPrefixWithWriteCapacityUnits() throws Exception {
        DynamoDBConfig fromConfig = DynamoDBConfig.fromConfig(mapFrom("region", "eu-west-1", "accessKey", "access1",
                "secretKey", "secret1", "writeCapacityUnits", "5"));
        assertEquals(Region.EU_WEST_1, fromConfig.getRegion());
        assertEquals("access1", fromConfig.getCredentials().accessKeyId());
        assertEquals("secret1", fromConfig.getCredentials().secretAccessKey());
        assertEquals("openhab-", fromConfig.getTablePrefixLegacy());
        assertEquals(1, fromConfig.getReadCapacityUnits());
        assertEquals(5, fromConfig.getWriteCapacityUnits());
        assertEquals(Optional.empty(), fromConfig.getRetryPolicy().map(RetryPolicy::retryMode));
        assertEquals(ExpectedTableSchema.MAYBE_LEGACY, fromConfig.getTableRevision());
        assertNull(fromConfig.getExpireDays()); // default is null
    }

    @SuppressWarnings("null")
    @Test
    public void testRegionWithAccessKeysWithoutPrefixWithReadWriteCapacityUnits() throws Exception {
        DynamoDBConfig fromConfig = DynamoDBConfig.fromConfig(mapFrom("region", "eu-west-1", "accessKey", "access1",
                "secretKey", "secret1", "readCapacityUnits", "3", "writeCapacityUnits", "5", "expireDays", "105"));
        assertEquals(Region.EU_WEST_1, fromConfig.getRegion());
        assertEquals("access1", fromConfig.getCredentials().accessKeyId());
        assertEquals("secret1", fromConfig.getCredentials().secretAccessKey());
        assertEquals("openhab-", fromConfig.getTablePrefixLegacy());
        assertEquals(3, fromConfig.getReadCapacityUnits());
        assertEquals(5, fromConfig.getWriteCapacityUnits());
        assertEquals(Optional.empty(), fromConfig.getRetryPolicy().map(RetryPolicy::retryMode));
        assertEquals(ExpectedTableSchema.MAYBE_LEGACY, fromConfig.getTableRevision());
    }

    @SuppressWarnings("null")
    @Test
    public void testRegionWithAccessKeysWithPrefixWithReadWriteCapacityUnitsWithBufferSettings() throws Exception {
        DynamoDBConfig fromConfig = DynamoDBConfig.fromConfig(
                mapFrom("region", "eu-west-1", "accessKey", "access1", "secretKey", "secret1", "readCapacityUnits", "3",
                        "writeCapacityUnits", "5", "bufferCommitIntervalMillis", "501", "bufferSize", "112"));
        assertEquals(Region.EU_WEST_1, fromConfig.getRegion());
        assertEquals("access1", fromConfig.getCredentials().accessKeyId());
        assertEquals("secret1", fromConfig.getCredentials().secretAccessKey());
        assertEquals("openhab-", fromConfig.getTablePrefixLegacy());
        assertEquals(3, fromConfig.getReadCapacityUnits());
        assertEquals(5, fromConfig.getWriteCapacityUnits());
        assertEquals(Optional.empty(), fromConfig.getRetryPolicy().map(RetryPolicy::retryMode));
        assertEquals(ExpectedTableSchema.MAYBE_LEGACY, fromConfig.getTableRevision());
    }
}
