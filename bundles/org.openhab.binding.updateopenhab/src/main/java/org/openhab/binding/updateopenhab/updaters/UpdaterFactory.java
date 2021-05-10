/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.updateopenhab.updaters;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.updateopenhab.internal.OperatingSystem;
import org.openhab.binding.updateopenhab.internal.PackageManager;
import org.openhab.binding.updateopenhab.internal.TargetVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UpdaterFactory} creates a xxxUpdater class based on the Operating System (and its respective package
 * manager if the Operating System is Linux).
 *
 * @author AndrewFG - initial contribution
 */
@NonNullByDefault
public class UpdaterFactory {
    // logger must be static because the class needs to log within static methods
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdaterFactory.class);

    private static final String PROPERTY_FILE = "/etc/os-release";
    private static final String[] PROPERTY_KEYS = new String[] { "ID_LIKE", "ID" };

    /**
     * Reads the properties in the '/etc/os-release' file and returns the package manager based on the 'ID_LIKE' or 'ID'
     * properties.
     *
     * @return
     */
    private static PackageManager getLinuxPackageManager() {
        try (InputStream stream = new FileInputStream(PROPERTY_FILE)) {
            Properties properties = new Properties();
            properties.load(stream);
            for (String propertyKey : PROPERTY_KEYS) {
                if (properties.containsKey(propertyKey)) {
                    if (properties.getProperty(propertyKey).contains("debian")) {
                        return PackageManager.DEBIAN_PACKAGE_MANAGER;
                    }
                    if (properties.getProperty(propertyKey).contains("fedora")) {
                        return PackageManager.REDHAT_PACKAGE_MANAGER;
                    }
                    if (properties.getProperty(propertyKey).contains("gentoo")) {
                        return PackageManager.GENTOO_PACKAGE_MANAGER;
                    }
                }
            }
            LOGGER.debug("Property values ID_LIKE or ID not found");
        } catch (IOException e) {
            LOGGER.debug("Errror reading property file: {}, '{}'", PROPERTY_FILE, e.getMessage());
        }
        return PackageManager.UNKNOWN_PACKAGE_MANAGER;
    }

    /**
     * Static method that returns an instance of an xxxUpdater class based on the Operating System (and its respective
     * package manager).
     *
     * @param targetVersion target version type to upgrade to (STABLE, MILESTONE, SNAPSHOT)
     * @param password system 'sudo' password on Linux systems
     * @param sleepTime number of seconds that scripts shall sleep while OpenHAB shuts down
     * @return an instance of the respective xxxUpdater class
     */
    public static @Nullable BaseUpdater newUpdater(TargetVersion targetVersion, String password, int sleepTime) {
        switch (OperatingSystem.getOperatingSystemVersion()) {
            case WINDOWS:
                return new WindowsUpdater().setTargetVersion(targetVersion).setPassword(password)
                        .setSleepTime(sleepTime);
            case MAC:
                return new MacUpdater().setTargetVersion(targetVersion).setPassword(password).setSleepTime(sleepTime);
            case UNIX:
                switch (getLinuxPackageManager()) {
                    case DEBIAN_PACKAGE_MANAGER:
                        return new DebianUpdater().setTargetVersion(targetVersion).setPassword(password)
                                .setSleepTime(sleepTime);
                    case REDHAT_PACKAGE_MANAGER:
                        // TODO
                        // return new RedHatUpdater().setTargetVersion(targetVersion).setPassword(password)
                        // .setSleepTime(sleepTime);
                    case GENTOO_PACKAGE_MANAGER:
                        // TODO
                        // return new GentooUpdater().setTargetVersion(targetVersion).setPassword(password)
                        // .setSleepTime(sleepTime);
                    default:
                }
            default:
        }
        return null;
    }
}
