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
package org.openhab.binding.insteon.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.driver.Driver;
import org.openhab.binding.insteon.internal.handler.InsteonSceneHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that represents Insteon scene
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class InsteonScene {
    public static final int GROUP_MIN = 1;
    public static final int GROUP_MAX = 255;

    private final Logger logger = LoggerFactory.getLogger(InsteonScene.class);

    private int group;
    private Driver driver;
    private @Nullable InsteonSceneHandler handler;
    private boolean modemDBEntry = false;

    public InsteonScene(Driver driver, int group) {
        this.driver = driver;
        this.group = group;
    }

    public int getGroup() {
        return group;
    }

    public Driver getDriver() {
        return driver;
    }

    public @Nullable InsteonSceneHandler getHandler() {
        return handler;
    }

    public boolean hasModemDBEntry() {
        return modemDBEntry;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public void setHandler(InsteonSceneHandler handler) {
        this.handler = handler;
    }

    public void setHasModemDBEntry(boolean modemDBEntry) {
        this.modemDBEntry = modemDBEntry;
    }

    /**
     * Initializes this scene
     */
    public void initialize() {
        if (!driver.isModemDBComplete()) {
            return;
        }

        if (driver.getModemDB().hasBroadcastGroup(group)) {
            if (!hasModemDBEntry()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("scene {} found in the modem database.", group);
                }
                setHasModemDBEntry(true);
            }
        } else {
            logger.warn("scene {} not found in the modem database.", group);
            setHasModemDBEntry(false);
        }
    }

    /**
     * Refreshes this scene
     */
    public void refresh() {
        initialize();

        InsteonSceneHandler handler = this.handler;
        if (handler != null) {
            handler.refresh();
        }
    }

    /**
     * Returns if scene group is valid
     *
     * @param group the scene group
     * @return true if group is an integer within supported range
     */
    public static boolean isValidGroup(String group) {
        try {
            return isValidGroup(Integer.parseInt(group));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns if scene group is valid
     *
     * @param group the scene group
     * @return true if group within supported range
     */
    public static boolean isValidGroup(int group) {
        return group >= GROUP_MIN && group <= GROUP_MAX;
    }

    /**
     * Factory method for creating a InsteonScene from a scene group and driver
     *
     * @param driver the scene driver
     * @param group the scene group
     * @return the newly created InsteonScene
     */
    public static InsteonScene makeScene(Driver driver, int group) {
        return new InsteonScene(driver, group);
    }
}
