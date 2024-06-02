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
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Zhivka Dimova - Initial contribution
 */
@NonNullByDefault
public class A5_09_05 extends A5_09 {

    public A5_09_05(ERP1Message packet) {
        super(packet);
    }

    protected double[] ScaleMultiplier = new double[] { 0.01, 0.1, 1, 10 };

    protected String[] getVOCIdentifications() {
        return new String[] { "VOCT", "Formaldehyde", "Benzene", "Styrene", "Toluene", "Tetrachloroethylene", "Xylene",
                "n-Hexane", "n-Octane", "Cyclopentane", "Methanol", "Ethanol", "1-Pentanol", "Acetone",
                "ethylene Oxide", "Acetaldehyde ue", "Acetic Acid", "Propionine Acid", "Valeric Acid", "Butyric Acid",
                "Ammoniac", "Hidrogen Sulfide", "Dimethylsulfide", "2-Butanol", "2-Methylpropanol", "Diethyl ether",
                "VOC-Index", "Ozone" };
    }

    protected long getUnscaledVOCValue() {
        return getDBByOffsetSizeValue(0, 16);
    }

    protected double getScalingFactor() {
        int smid = Long.valueOf(getDBByOffsetSizeValue(30, 2)).intValue();
        if (smid < 0 || smid >= ScaleMultiplier.length) {
            logger.debug("Invalid value according to enocean specification for A5_09 Scale Multiplier {}", smid);
            return 1;
        }

        return ScaleMultiplier[smid];
    }

    protected String getVOCID() {
        int vocId = getDB1Value();
        String[] VOCIdentifications = getVOCIdentifications();
        if (vocId == 255) {
            return VOCIdentifications[VOCIdentifications.length - 1];
        } else if (vocId < 0 || vocId >= VOCIdentifications.length - 1) {
            logger.debug("Invalid value according to enocean specification for A5_09 VOC Identification {}", vocId);
            return "";
        }

        return VOCIdentifications[vocId];
    }

    @Override
    protected State convertToStateImpl(String channelId, String channelTypeId,
            Function<String, State> getCurrentStateFunc, Configuration config) {
        if (CHANNEL_VOC.equals(channelId)) {
            double scaledVOC = getUnscaledVOCValue() * getScalingFactor();
            return new QuantityType<>(scaledVOC, Units.PARTS_PER_BILLION);
        } else if (CHANNEL_VOC_ID.equals(channelId)) {
            return new StringType(getVOCID());
        }

        return UnDefType.UNDEF;
    }
}
