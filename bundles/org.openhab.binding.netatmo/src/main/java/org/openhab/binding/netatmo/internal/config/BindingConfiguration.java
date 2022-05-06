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
package org.openhab.binding.netatmo.internal.config;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;

/**
 * The {@link BindingConfiguration} is responsible for holding configuration of the binding itself.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class BindingConfiguration {
    public Set<FeatureArea> features = Set.of();
    public boolean readFriends = false;

    public void update(BindingConfiguration newConfig) {
        this.features = newConfig.features;
        this.readFriends = newConfig.readFriends;
    }
}
