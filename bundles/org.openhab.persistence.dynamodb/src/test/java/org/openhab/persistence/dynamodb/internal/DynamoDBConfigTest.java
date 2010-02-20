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

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.amazonaws.regions.Regions;

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
        assertNull(DynamoDBConfig.fromConfig(Collections.singletonMap("region", "foobie")));
    }

    @Test
    public void testRegionOnly() throws Exception {
        assertNull(DynamoDBConfig.fromConfig(Collections.singletonMap("region", "eu-west-1")));
    }

    @Test
    public void testRegionWithAccessKeys() throws Exception {
        DynamoDBConfig fromConfig = DynamoDBConfig
                .fromConfig(mapFrom("region", "eu-west-1", "accessKey", "access1", "secretKey", "secret1"));
        assertEquals(Regions.EU_WEST_1, fromConfig.getRegion());
        assertEquals("access1", fromConfig.getCredentials().getAWSAccessKeyId());
        assertEquals("secret1", fromConfig.getCredentials().getAWSSecretKey());
        assertEquals("openhab-", fromConfig.getTablePrefix());
        assertEquals(true, fromConfig.isCreateTable());
        assertEquals(1, fromConfig.getReadCapacityUnits());
        assertEquals(1, fromConfig.getWriteCapacityUnits());
        assertEquals(1000L, fromConfig.getBufferCommitIntervalMillis());
        assertEquals(1000, fromConfig.getBufferSize());
    }

    @Test
    public void testRegionWithProfilesConfigFile() throws Exception {
        Path credsFile = Files.createFile(Paths.get(folder.getPath(), "creds"));
        Files.write(
                credsFile, ("[fooprofile]\n" + "aws_access_key_id=testAccessKey\n"
                        + "aws_secret_access_key=testSecretKey\n" + "aws_session_token=testSessionToken\n").getBytes(),
                StandardOpenOption.TRUNCATE_EXISTING);

        DynamoDBConfig fromConfig = DynamoDBConfig.fromConfig(mapFrom("region", "eu-west-1", "profilesConfigFile",
                credsFile.toAbsolutePath().toString(), "profile", "fooprofile"));
        assertEquals(Regions.EU_WEST_1, fromConfig.getRegion());
        assertEquals("openhab-", fromConfig.getTablePrefix());
        assertEquals(true, fromConfig.isCreateTable());
        assertEquals(1, fromConfig.getReadCapacityUnits());
        assertEquals(1, fromConfig.getWriteCapacityUnits());
        assertEquals(1000L, fromConfig.getBufferCommitIntervalMillis());
        assertEquals(1000, fromConfig.getBufferSize());
    }

    @Test
    public void testEmptyConfiguration() throws Exception {
        assertNull(DynamoDBConfig.fromConfig(mapFrom()));
    }

    @Test
    public void testRegionWithInvalidProfilesConfigFile() throws Exception {
        Path credsFile = Files.createFile(Paths.get(folder.getPath(), "creds"));
        Files.write(credsFile,
                ("[fooprofile]\n" + "aws_access_key_idINVALIDKEY=testAccessKey\n"
                        + "aws_secret_access_key=testSecretKey\n" + "aws_session_token=testSessionToken\n").getBytes(),
                StandardOpenOption.TRUNCATE_EXISTING);

        assertNull(DynamoDBConfig.fromConfig(mapFrom("region", "eu-west-1", "profilesConfigFile",
                credsFile.toFile().getAbsolutePath(), "profile", "fooprofile")));
    }

    @Test
    public void testRegionWithProfilesConfigFileMissingProfile() throws Exception {
        Path credsFile = Files.createFile(Paths.get(folder.getPath(), "creds"));
        Files.write(
                credsFile, ("[fooprofile]\n" + "aws_access_key_id=testAccessKey\n"
                        + "aws_secret_access_key=testSecretKey\n" + "aws_session_token=testSessionToken\n").getBytes(),
                StandardOpenOption.TRUNCATE_EXISTING);

        assertNull(DynamoDBConfig.fromConfig(
                mapFrom("region", "eu-west-1", "profilesConfigFile", credsFile.toAbsolutePath().toString())));
    }

    @Test
    public void testRegionWithAccessKeysWithPrefix() throws Exception {
        DynamoDBConfig fromConfig = DynamoDBConfig.fromConfig(mapFrom("region", "eu-west-1", "accessKey", "access1",
                "secretKey", "secret1", "tablePrefix", "foobie-"));
        assertEquals(Regions.EU_WEST_1, fromConfig.getRegion());
        assertEquals("access1", fromConfig.getCredentials().getAWSAccessKeyId());
        assertEquals("secret1", fromConfig.getCredentials().getAWSSecretKey());
        assertEquals("foobie-", fromConfig.getTablePrefix());
        assertEquals(true, fromConfig.isCreateTable());
        assertEquals(1, fromConfig.getReadCapacityUnits());
        assertEquals(1, fromConfig.getWriteCapacityUnits());
        assertEquals(1000L, fromConfig.getBufferCommitIntervalMillis());
        assertEquals(1000, fromConfig.getBufferSize());
    }

    @Test
    public void testRegionWithAccessKeysWithPrefixWithCreateTable() throws Exception {
        DynamoDBConfig fromConfig = DynamoDBConfig.fromConfig(
                mapFrom("region", "eu-west-1", "accessKey", "access1", "secretKey", "secret1", "createTable", "false"));
        assertEquals(Regions.EU_WEST_1, fromConfig.getRegion());
        assertEquals("access1", fromConfig.getCredentials().getAWSAccessKeyId());
        assertEquals("secret1", fromConfig.getCredentials().getAWSSecretKey());
        assertEquals("openhab-", fromConfig.getTablePrefix());
        assertEquals(false, fromConfig.isCreateTable());
        assertEquals(1, fromConfig.getReadCapacityUnits());
        assertEquals(1, fromConfig.getWriteCapacityUnits());
        assertEquals(1000L, fromConfig.getBufferCommitIntervalMillis());
        assertEquals(1000, fromConfig.getBufferSize());
    }

    @Test
    public void testRegionWithAccessKeysWithPrefixWithReadCapacityUnits() throws Exception {
        DynamoDBConfig fromConfig = DynamoDBConfig.fromConfig(mapFrom("region", "eu-west-1", "accessKey", "access1",
                "secretKey", "secret1", "readCapacityUnits", "5"));
        assertEquals(Regions.EU_WEST_1, fromConfig.getRegion());
        assertEquals("access1", fromConfig.getCredentials().getAWSAccessKeyId());
        assertEquals("secret1", fromConfig.getCredentials().getAWSSecretKey());
        assertEquals("openhab-", fromConfig.getTablePrefix());
        assertEquals(true, fromConfig.isCreateTable());
        assertEquals(5, fromConfig.getReadCapacityUnits());
        assertEquals(1, fromConfig.getWriteCapacityUnits());
        assertEquals(1000L, fromConfig.getBufferCommitIntervalMillis());
        assertEquals(1000, fromConfig.getBufferSize());
    }

    @Test
    public void testRegionWithAccessKeysWithPrefixWithWriteCapacityUnits() throws Exception {
        DynamoDBConfig fromConfig = DynamoDBConfig.fromConfig(mapFrom("region", "eu-west-1", "accessKey", "access1",
                "secretKey", "secret1", "writeCapacityUnits", "5"));
        assertEquals(Regions.EU_WEST_1, fromConfig.getRegion());
        assertEquals("access1", fromConfig.getCredentials().getAWSAccessKeyId());
        assertEquals("secret1", fromConfig.getCredentials().getAWSSecretKey());
        assertEquals("openhab-", fromConfig.getTablePrefix());
        assertEquals(true, fromConfig.isCreateTable());
        assertEquals(1, fromConfig.getReadCapacityUnits());
        assertEquals(5, fromConfig.getWriteCapacityUnits());
        assertEquals(1000L, fromConfig.getBufferCommitIntervalMillis());
        assertEquals(1000, fromConfig.getBufferSize());
    }

    @Test
    public void testRegionWithAccessKeysWithPrefixWithReadWriteCapacityUnits() throws Exception {
        DynamoDBConfig fromConfig = DynamoDBConfig.fromConfig(mapFrom("region", "eu-west-1", "accessKey", "access1",
                "secretKey", "secret1", "readCapacityUnits", "3", "writeCapacityUnits", "5"));
        assertEquals(Regions.EU_WEST_1, fromConfig.getRegion());
        assertEquals("access1", fromConfig.getCredentials().getAWSAccessKeyId());
        assertEquals("secret1", fromConfig.getCredentials().getAWSSecretKey());
        assertEquals("openhab-", fromConfig.getTablePrefix());
        assertEquals(true, fromConfig.isCreateTable());
        assertEquals(3, fromConfig.getReadCapacityUnits());
        assertEquals(5, fromConfig.getWriteCapacityUnits());
        assertEquals(1000L, fromConfig.getBufferCommitIntervalMillis());
        assertEquals(1000, fromConfig.getBufferSize());
    }

    @Test
    public void testRegionWithAccessKeysWithPrefixWithReadWriteCapacityUnitsWithBufferSettings() throws Exception {
        DynamoDBConfig fromConfig = DynamoDBConfig.fromConfig(
                mapFrom("region", "eu-west-1", "accessKey", "access1", "secretKey", "secret1", "readCapacityUnits", "3",
                        "writeCapacityUnits", "5", "bufferCommitIntervalMillis", "501", "bufferSize", "112"));
        assertEquals(Regions.EU_WEST_1, fromConfig.getRegion());
        assertEquals("access1", fromConfig.getCredentials().getAWSAccessKeyId());
        assertEquals("secret1", fromConfig.getCredentials().getAWSSecretKey());
        assertEquals("openhab-", fromConfig.getTablePrefix());
        assertEquals(true, fromConfig.isCreateTable());
        assertEquals(3, fromConfig.getReadCapacityUnits());
        assertEquals(5, fromConfig.getWriteCapacityUnits());
        assertEquals(501L, fromConfig.getBufferCommitIntervalMillis());
        assertEquals(112, fromConfig.getBufferSize());
    }
}
