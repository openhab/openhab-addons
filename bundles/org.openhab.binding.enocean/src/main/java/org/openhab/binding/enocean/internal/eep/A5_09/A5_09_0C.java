/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
        String[] VOCIdsSuper = super.getVOCIdentifications();
        int elFromSuper = VOCIdsSuper.length - 2;
        String[] VOCIdsExtension = new String[] { "Naphthalene", "4-Phenylcyclohexene", "Limonene", "Trichloroethylene",
                "Isovaleric acid", "Indole", "Cadaverine", "Putrescine", "Caproic acid", "Ozone" };
        String[] VOCIdsExtended = new String[elFromSuper + VOCIdsExtension.length];

        System.arraycopy(VOCIdsSuper, 0, VOCIdsExtended, 0, elFromSuper);
        System.arraycopy(VOCIdsExtension, 0, VOCIdsExtended, elFromSuper, VOCIdsExtension.length);

        return VOCIdsExtended;
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
