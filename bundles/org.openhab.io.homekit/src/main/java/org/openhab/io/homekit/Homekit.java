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
package org.openhab.io.homekit;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;

import io.github.hapjava.accessories.HomekitAccessory;

/**
 * HomeKit integration API
 *
 * @author Andy Lintner - Initial contribution
 */
@NonNullByDefault
public interface Homekit {

    /**
     * Refreshes the saved authentication info from the underlying storage service. If you
     * make changes to the saved authentication info, call this.
     *
     * @throws IOException exception in case new auth info could not be published via mDNS
     */
    void refreshAuthInfo() throws IOException;

    /**
     * HomeKit requests normally require authentication via the pairing mechanism. Use this
     * method to bypass that check and enable unauthenticated requests. This can be useful
     * when debugging.
     *
     * @param allow boolean indicating whether or not to allow unauthenticated requests
     */
    void allowUnauthenticatedRequests(boolean allow);

    /**
     * returns list of HomeKit accessories registered on all bridge instances.
     */
    Collection<HomekitAccessory> getAccessories();

    /**
     * returns list of HomeKit accessories registered on a specific instance.
     */
    Collection<HomekitAccessory> getAccessories(int instance);

    /**
     * clear all pairings with HomeKit clients on all bridge instances.
     */
    void clearHomekitPairings();

    /**
     * clear all pairings with HomeKit clients for a specific instance.
     * 
     * @param instance the instance number (1-based)
     */
    void clearHomekitPairings(int instance);

    /**
     * Prune dummy accessories (accessories that no longer have associated items)
     * on all bridge instances.
     */
    void pruneDummyAccessories();

    /**
     * Prune dummy accessories (accessories that no longer have associated items)
     * for a specific instance
     *
     * @param instance the instance number (1-based)
     */
    void pruneDummyAccessories(int instance);

    /**
     * returns how many bridge instances there are
     */
    int getInstanceCount();
}
