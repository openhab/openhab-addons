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
package org.openhab.misc.addonsuggestionfinder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.addon.AddonInfo;
import org.openhab.core.addon.AddonInfoList;
import org.openhab.core.addon.AddonInfoListReader;
import org.openhab.core.addon.AddonInfoProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AddonSuggestionInfoProvider} provides a list of candidate suggested addons to be installed.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component(name = "addon-suggestion-info-provider", service = AddonInfoProvider.class)
public class AddonSuggestionInfoProvider implements AddonInfoProvider {

    private final Logger logger = LoggerFactory.getLogger(AddonSuggestionInfoProvider.class);
    private final Set<AddonInfo> candidateAddonInfos = new HashSet<>();

    @Activate
    public AddonSuggestionInfoProvider() {
        setCandidates(getResourceXml());
    }

    @Override
    public @Nullable AddonInfo getAddonInfo(@Nullable String uid, @Nullable Locale locale) {
        return candidateAddonInfos.stream().filter(a -> a.getUID().equals(uid)).findFirst().orElse(null);
    }

    @Override
    public Set<AddonInfo> getAddonInfos(@Nullable Locale locale) {
        return candidateAddonInfos;
    }

    private String getResourceXml() {
        ClassLoader loader = getClass().getClassLoader();
        if (loader != null) {
            InputStream stream = loader.getResourceAsStream("addons.xml");
            if (stream != null) {
                try {
                    return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                }
            }
        }
        throw new IllegalStateException("Error loading 'addons.xml' resource");
    }

    public void setCandidates(String xml) {
        candidateAddonInfos.clear();
        AddonInfoListReader reader = new AddonInfoListReader();
        try {
            AddonInfoList addonInfoList = reader.readFromXML(xml);
            if (addonInfoList != null) {
                candidateAddonInfos.addAll(addonInfoList.getAddons().stream().collect(Collectors.toSet()));
            }
        } catch (PatternSyntaxException e) {
            logger.warn("PatternSyntaxException: message:{}, description:{}, pattern:{}, index:{}", e.getMessage(),
                    e.getDescription(), e.getPattern(), e.getIndex(), e);
        }
    }
}
