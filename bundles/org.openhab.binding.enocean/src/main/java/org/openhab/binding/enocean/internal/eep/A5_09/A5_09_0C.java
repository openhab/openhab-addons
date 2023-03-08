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
    protected String[] get_VOC_Ids() {
        String[] VOC_Ids_super = super.get_VOC_Ids();
        int elFromSuper = VOC_Ids_super.length - 2;
        String[] VOC_Ids_extension = new String[] { "Naphthalene", "4-Phenylcyclohexene", "Limonene",
                "Trichloroethylene", "Isovaleric acid", "Indole", "Cadaverine", "Putrescine", "Caproic acid", "Ozone" };
        String[] VOC_Ids_extended = new String[elFromSuper + VOC_Ids_extension.length];

        System.arraycopy(VOC_Ids_super, 0, VOC_Ids_extended, 0, elFromSuper);
        System.arraycopy(VOC_Ids_extension, 0, VOC_Ids_extended, elFromSuper, VOC_Ids_extension.length);

        return VOC_Ids_extended;
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {

        if (channelId.equals(CHANNEL_VOC)) {
            double scaledVOC = getUnscaledVOCValue() * getScalingFactor();
            if (getBit(getDB_0(), 2)) {
                return new QuantityType<>(scaledVOC, Units.MICROGRAM_PER_CUBICMETRE);
            }
            return new QuantityType<>(scaledVOC, Units.PARTS_PER_BILLION);
        }

        return super.convertToStateImpl(channelId, channelTypeId, getCurrentStateFunc, config);
    }
}
