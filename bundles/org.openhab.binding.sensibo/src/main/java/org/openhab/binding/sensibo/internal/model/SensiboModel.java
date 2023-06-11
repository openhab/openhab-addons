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
package org.openhab.binding.sensibo.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link SensiboModel} represents the home structure as designed by the user in the Sensibo app.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
public class SensiboModel {
    private final long lastUpdated;
    private final List<SensiboSky> pods = new ArrayList<>();

    public SensiboModel(final long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void addPod(final SensiboSky pod) {
        pods.add(pod);
    }

    public List<SensiboSky> getPods() {
        return pods;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public Optional<SensiboSky> findSensiboSkyByMacAddress(final String macAddress) {
        final String macAddressWithoutColons = StringUtils.remove(macAddress, ':');
        return pods.stream().filter(pod -> macAddressWithoutColons.equals(pod.getMacAddress())).findFirst();
    }

    /**
     * @param macAddress
     * @param acState
     */
    public void updateAcState(String macAddress, AcState acState) {
        findSensiboSkyByMacAddress(macAddress).ifPresent(sky -> sky.updateAcState(acState));
    }
}
