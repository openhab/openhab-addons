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
package org.openhab.misc.addonsuggestionfinder.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.addon.AddonInfo;

/**
 * DTO for a list of {@link AddonInfo}.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class AddonInfoList {
    public @Nullable List<AddonInfo> addonInfos;

    public List<AddonInfo> getAddonInfos() {
        List<AddonInfo> addonInfos = this.addonInfos;
        return addonInfos != null ? addonInfos : List.of();
    }
}
