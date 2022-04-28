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
package org.openhab.binding.pulseaudio.internal.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * All informations needed to precisely identify a device
 *
 * @author Gwendal Roulleau - Initial contribution
 *
 */
@NonNullByDefault
public class DeviceIdentifier {

    private String nameOrDescription;
    private List<Pattern> additionalFilters = new ArrayList<>();

    public DeviceIdentifier(String nameOrDescription, @Nullable String additionalFilters)
            throws PatternSyntaxException {
        super();
        this.nameOrDescription = nameOrDescription;
        if (additionalFilters != null && !additionalFilters.isEmpty()) {
            Arrays.asList(additionalFilters.split("###")).stream()
                    .forEach(ad -> this.additionalFilters.add(Pattern.compile(ad)));
        }
    }

    public String getNameOrDescription() {
        return nameOrDescription;
    }

    public List<Pattern> getAdditionalFilters() {
        return additionalFilters;
    }

    @Override
    public String toString() {
        List<Pattern> additionalFiltersFinal = additionalFilters;
        String additionalPatternToString = additionalFiltersFinal.stream().map(Pattern::pattern)
                .collect(Collectors.joining("###"));
        return "DeviceIdentifier [nameOrDescription=" + nameOrDescription + ", additionalFilter="
                + additionalPatternToString + "]";
    }
}
