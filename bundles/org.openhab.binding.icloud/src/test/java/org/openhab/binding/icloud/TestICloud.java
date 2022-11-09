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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import org.openhab.binding.icloud.internal.ICloudDeviceInformationParser;
import org.openhab.binding.icloud.internal.ICloudService;
import org.openhab.binding.icloud.internal.json.response.ICloudAccountDataResponse;
import org.openhab.core.storage.json.internal.JsonStorage;

/**
 *
 * TODO
 *
 * @author Simon Spielmann
 */
public class TestICloud {

    private final String E_MAIL = System.getProperty("icloud.test.email");

    private final String PW = System.getProperty("icloud.test.pw");

    private final String DEVICE_ID = System.getProperty("icloud.test.device");

    @Test
    public void testAuth() throws IOException, InterruptedException {

        File jsonStorageFile = new File(System.getProperty("user.home"), "openhab-3.json");
        System.out.println(jsonStorageFile.toString());

        JsonStorage<String> stateStorage = new JsonStorage<String>(jsonStorageFile, TestICloud.class.getClassLoader(),
                0, 1000, 1000, List.of());

        ICloudService service = new ICloudService(this.E_MAIL, this.PW, stateStorage);
        service.authenticate(false);
        if (service.requires2fa()) {
            System.out.print("Code: ");
            String code = new Scanner(System.in).nextLine();
            System.out.println(service.validate2faCode(code));
            if (!service.isTrustedSession()) {
                service.trustSession();
            }
            if (!service.isTrustedSession()) {
                System.err.println("Trust failed!!!");
            }

        }
        ICloudAccountDataResponse deviceInfo = new ICloudDeviceInformationParser()
                .parse(service.getDevices().refreshClient());
        stateStorage.flush();
    }
}
