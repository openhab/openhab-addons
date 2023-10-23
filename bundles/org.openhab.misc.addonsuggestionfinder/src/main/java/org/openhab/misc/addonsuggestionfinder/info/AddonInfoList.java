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
package org.openhab.misc.addonsuggestionfinder.info;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.addon.AddonInfo;

/**
 * DTO containing a list of {@code AddonInfo}
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class AddonInfoList {
    protected @Nullable List<AddonInfo> addons;

    public List<AddonInfo> getAddons() {
        List<AddonInfo> addons = this.addons;
        return addons != null ? addons : List.of();
    }

    public AddonInfoList setAddons(@Nullable List<AddonInfo> addons) {
        this.addons = addons;
        return this;
    }
}
