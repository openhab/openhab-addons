/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.eep.A5_09;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 *
 * @author Zhivka Dimova - Initial contribution
 */
@NonNullByDefault
public class A5_09_0C extends A5_09_05 {

    public A5_09_0C(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected String[] getVOCIdentifications() {
        String[] parentVOCIds = super.getVOCIdentifications();
        int parentVOCIdsLength = parentVOCIds.length - 2;
        String[] additionalVOCIds = new String[] { "Naphthalene", "4-Phenylcyclohexene", "Limonene",
                "Trichloroethylene", "Isovaleric acid", "Indole", "Cadaverine", "Putrescine", "Caproic acid", "Ozone" };
        String[] combinedVOCIds = new String[parentVOCIdsLength + additionalVOCIds.length];

        System.arraycopy(parentVOCIds, 0, combinedVOCIds, 0, parentVOCIdsLength);
        System.arraycopy(additionalVOCIds, 0, combinedVOCIds, parentVOCIdsLength, additionalVOCIds.length);

        return combinedVOCIds;
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        if (CHANNEL_VOC.equals(channelId)) {
            double scaledVOC = getUnscaledVOCValue() * getScalingFactor();
            if (getBit(getDB0(), 2)) {
                return new QuantityType<>(scaledVOC, Units.MICROGRAM_PER_CUBICMETRE);
            }
            return new QuantityType<>(scaledVOC, Units.PARTS_PER_BILLION);
        }

        return super.convertToStateImpl(channelId, channelTypeId, getCurrentStateFunc, config);
    }
}
