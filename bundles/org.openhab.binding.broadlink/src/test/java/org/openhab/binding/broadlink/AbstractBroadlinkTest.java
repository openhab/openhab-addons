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
package org.openhab.binding.broadlink;

//import static org.openhab.binding.broadlink.internal.BroadlinkBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openhab.core.storage.Storage;
import org.openhab.core.test.storage.VolatileStorageService;

/**
 * Abstract superclass for all Broadlink unit tests;
 * ensures that the mapping file will be found
 * in a testing context
 * 
 * @author John Marshall - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractBroadlinkTest {
    protected static VolatileStorageService storageService = new VolatileStorageService();
    protected static Storage<String> irStorage = storageService.getStorage(
            org.openhab.binding.broadlink.internal.BroadlinkBindingConstants.IR_MAP_NAME,
            String.class.getClassLoader());
    protected static Storage<String> rfStorage = storageService.getStorage(
            org.openhab.binding.broadlink.internal.BroadlinkBindingConstants.RF_MAP_NAME,
            String.class.getClassLoader());

    @BeforeEach
    public void setUp() throws Exception {
        for (String s : irStorage.getKeys()) {
            irStorage.remove(s);
        }
        for (String s : rfStorage.getKeys()) {
            rfStorage.remove(s);
        }
        irStorage.put("IR_TEST_COMMAND_ON", "00112233");
        irStorage.put("IR_TEST_COMMAND_OFF", "33221100");
        rfStorage.put("RF_TEST_COMMAND_ON", "00112233");
        rfStorage.put("RF_TEST_COMMAND_OFF", "33221100");
    }

    @BeforeAll
    public static void beforeClass() {
    }

    @AfterAll
    public static void afterClass() {
    }
}
