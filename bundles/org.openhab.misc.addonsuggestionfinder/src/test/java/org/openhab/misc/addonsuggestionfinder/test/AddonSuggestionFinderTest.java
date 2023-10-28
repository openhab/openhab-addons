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
package org.openhab.misc.addonsuggestionfinder.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.addon.AddonInfo;
import org.openhab.misc.addonsuggestionfinder.AddonSuggestionInfoProvider;

/**
 * JUnit test for {@link AddonSuggestionInfoProvider}.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class AddonSuggestionFinderTest {

    @Test
    void testAddonSuggestionInfoProvider() {
        AddonSuggestionInfoProvider provider = new AddonSuggestionInfoProvider();
        assertNotNull(provider);
        Set<AddonInfo> addonInfos = provider.getAddonInfos(Locale.US);
        assertNotNull(addonInfos);
        AddonInfo addonInfo = provider.getAddonInfo("flying-aardvarks", Locale.US);
        assertNull(addonInfo);
    }
}
