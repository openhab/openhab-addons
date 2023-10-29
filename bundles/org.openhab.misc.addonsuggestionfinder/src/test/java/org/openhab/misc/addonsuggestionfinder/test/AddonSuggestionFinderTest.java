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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.addon.AddonDiscoveryMethod;
import org.openhab.core.addon.AddonInfo;
import org.openhab.core.addon.AddonMatchProperty;
import org.openhab.misc.addonsuggestionfinder.AddonSuggestionInfoProvider;

/**
 * JUnit test for {@link AddonSuggestionInfoProvider}.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class AddonSuggestionFinderTest {

    private static final String DELIMITER = "\n\t";

    @Test
    void testAddonSuggestionInfoProvider() {
        AddonSuggestionInfoProvider provider = new AddonSuggestionInfoProvider();
        assertNotNull(provider);
        Set<AddonInfo> addonInfos = provider.getAddonInfos(Locale.US);
        assertNotNull(addonInfos);

        AddonInfo addonInfoBad = provider.getAddonInfo("flying-aardvarks", Locale.US);
        assertNull(addonInfoBad);

        /*
         * At build time we run a regular expression syntax check on all the add-on infos resp. discovery methods and
         * match properties in the bundle, in order to help developers identify regex syntax errors in their addon.xml
         * files.
         */
        List<String> patternErrors = new ArrayList<>();
        for (AddonInfo addonInfo : addonInfos) {
            for (AddonDiscoveryMethod discoveryMethod : addonInfo.getDiscoveryMethods()) {
                for (AddonMatchProperty matchProperty : discoveryMethod.getMatchProperties()) {
                    try {
                        matchProperty.getPattern();
                    } catch (PatternSyntaxException e) {
                        patternErrors.add(String.format(
                                "Regex syntax error in org.openhab.%s.%s addon.xml => %s in \"%s\" position %d",
                                addonInfo.getType(), addonInfo.getId(), e.getDescription(), e.getPattern(),
                                e.getIndex()));
                    }
                }
            }
        }
        if (!patternErrors.isEmpty()) {
            fail(DELIMITER + String.join(DELIMITER, patternErrors));
        }
    }
}
