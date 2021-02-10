/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal;

import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;

public class DECT500DefaultHSBStateProvider implements DynamicStateDescriptionProvider {

    // possible to instrument the FritzAHAWebInterface?
    // currently broken
    // TODO: FIX.If.You.Can
    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {
        originalStateDescription = StateDescriptionFragmentBuilder.create().withOptions(List.of()).build()
                .toStateDescription();
        return originalStateDescription;
    }
}
