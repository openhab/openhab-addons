/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.hueemulation.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.ConfigConstants;

/**
 * Each Hue bridge has a unique ID. This class generates one if none could be loaded from
 * a persistence file located at userdata/hueemulation/udn.
 *
 * @author David Graeff - Initial contribution
 */
public class UDN {
    private static @Nullable String udn;

    private static final File UDN_FILE = new File(
            ConfigConstants.getUserDataFolder() + File.separator + "hueemulation" + File.separator + "udn");

    /**
     * Gets the unique UDN for this server, will generate and persist one if not found.
     *
     * @throws IOException
     */
    static synchronized String getUDN() throws IOException {
        String udnString = udn;
        if (udnString != null) {
            return udnString;
        }

        if (!UDN_FILE.exists()) {
            UDN_FILE.getParentFile().mkdirs();
        }

        try (FileInputStream fis = new FileInputStream(UDN_FILE)) {
            udnString = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8)).readLine();
        } catch (IOException e) {
        }

        if (udnString == null) {
            udnString = UUID.randomUUID().toString();
            try (FileOutputStream fos = new FileOutputStream(UDN_FILE)) {
                byte[] bytes = udnString.getBytes(StandardCharsets.UTF_8);
                fos.write(bytes, 0, bytes.length);
            }
        }

        udn = udnString;
        return udnString;
    }
}
