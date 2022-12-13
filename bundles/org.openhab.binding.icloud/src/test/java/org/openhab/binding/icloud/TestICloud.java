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
package org.openhab.binding.icloud;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openhab.binding.icloud.internal.ICloudApiResponseException;
import org.openhab.binding.icloud.internal.ICloudService;
import org.openhab.binding.icloud.internal.handler.dto.json.response.ICloudAccountDataResponse;
import org.openhab.binding.icloud.internal.utilities.JsonUtils;
import org.openhab.core.storage.json.internal.JsonStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * Class to test/experiment with iCloud api.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
public class TestICloud {

    private final String iCloudTestEmail;
    private final String iCloudTestPassword;

    private final Logger logger = LoggerFactory.getLogger(TestICloud.class);

    @BeforeEach
    private void setUp() {
        final Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (logger instanceof ch.qos.logback.classic.Logger) {
            ((ch.qos.logback.classic.Logger) logger).setLevel(ch.qos.logback.classic.Level.DEBUG);
        }
    }

    public TestICloud() {
        String sysPropMail = System.getProperty("icloud.test.email");
        String sysPropPW = System.getProperty("icloud.test.pw");
        iCloudTestEmail = sysPropMail != null ? sysPropMail : "notset";
        iCloudTestPassword = sysPropPW != null ? sysPropPW : "notset";
    }

    @Test
    @EnabledIfSystemProperty(named = "icloud.test.email", matches = ".*", disabledReason = "Only for manual execution.")
    public void testAuth() throws IOException, InterruptedException, ICloudApiResponseException, JsonSyntaxException {
        File jsonStorageFile = new File(System.getProperty("user.home"), "openhab.json");
        logger.info(jsonStorageFile.toString());

        JsonStorage<String> stateStorage = new JsonStorage<String>(jsonStorageFile, TestICloud.class.getClassLoader(),
                0, 1000, 1000, List.of());

        ICloudService service = new ICloudService(iCloudTestEmail, iCloudTestPassword, stateStorage);
        service.authenticate(false);
        if (service.requires2fa()) {
            PrintStream consoleOutput = System.out;
            if (consoleOutput != null) {
                consoleOutput.print("Code: ");
            }
            @SuppressWarnings("resource")
            Scanner in = new Scanner(System.in);
            String code = in.nextLine();
            assertTrue(service.validate2faCode(code));
            if (!service.isTrustedSession()) {
                service.trustSession();
            }
            if (!service.isTrustedSession()) {
                logger.info("Trust failed!!!");
            }
        }
        ICloudAccountDataResponse deviceInfo = JsonUtils.fromJson(service.getDevices().refreshClient(),
                ICloudAccountDataResponse.class);
        assertNotNull(deviceInfo);
        stateStorage.flush();
    }
}
