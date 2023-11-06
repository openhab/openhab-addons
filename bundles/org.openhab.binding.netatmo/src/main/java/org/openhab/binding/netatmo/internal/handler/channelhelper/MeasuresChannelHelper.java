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
package org.openhab.binding.netatmo.internal.handler.channelhelper;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.State;

/**
 * The {@link MeasuresChannelHelper} handles extensible channels based on getMeasure endpoint.
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */

@NonNullByDefault
public class MeasuresChannelHelper extends ChannelHelper {
    private @Nullable Map<String, State> measures;

    public MeasuresChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }

    public void setMeasures(Map<String, State> measures) {
        this.measures = measures;
    }

    @Override
    protected @Nullable State internalGetOther(String channelId) {
        Map<String, State> localMeasures = measures;
        if (localMeasures != null) {
            return localMeasures.get(channelId);
        }
        throw new IllegalArgumentException("localMeasures should not be null, please file a bug report.");
    }
}
