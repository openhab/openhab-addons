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
package org.openhab.binding.netatmo.internal.channelhelper;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class MeasuresChannelHelper extends AbstractChannelHelper {

    private @Nullable Map<String, State> measures;

    public MeasuresChannelHelper() {
        super(Set.of());
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing) {
        Map<String, State> localMeasures = measures;
        return localMeasures != null ? localMeasures.containsKey(channelId) ? localMeasures.get(channelId) : null
                : UnDefType.UNDEF;
    }

    public void setMeasures(Map<String, State> measures) {
        this.measures = measures;
    }
}
