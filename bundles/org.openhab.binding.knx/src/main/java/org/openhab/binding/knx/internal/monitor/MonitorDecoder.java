/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.monitor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.dpt.DPTUtil;
import org.openhab.binding.knx.internal.dpt.ValueDecoder;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.Type;

import io.calimero.KNXException;
import io.calimero.dptxlator.DPTXlator;
import io.calimero.dptxlator.DPTXlatorUtf8;
import io.calimero.dptxlator.TranslatorTypes;

/**
 * Decodes a payload for the group monitor the way a channel does, without the log lines the binding's decoder
 * writes when a payload does not fit its data type: the payload is checked against the library's translator first,
 * and only a payload that fits goes to the decoder.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public final class MonitorDecoder {

    private MonitorDecoder() {
    }

    /**
     * @param dpt the datapoint type, e.g. 9.001
     * @param asdu the plain value bytes, as a channel receives them
     * @param preferredType the openHAB type the channel prefers
     * @return the value, or null if the payload does not fit the type or the binding cannot decode it
     */
    public static @Nullable Type decode(String dpt, byte[] asdu, Class<? extends Type> preferredType) {
        if (!fits(dpt, asdu)) {
            return null;
        }
        return ValueDecoder.decode(dpt, asdu, preferredType);
    }

    /** {@return true if the binding has a decoding for the type; asked without the WARN DPTUtil writes for bad text} */
    public static boolean isSupported(String dpt) {
        return DPTUtil.DPT_PATTERN.matcher(dpt).matches() && !DPTUtil.getAllowedTypes(dpt).isEmpty();
    }

    /** {@return true if the library translates the payload as exactly one value of the type} */
    static boolean fits(String dpt, byte[] asdu) {
        if (asdu.length == 0 || !isSupported(dpt)) {
            return false;
        }
        try {
            DPTXlator translator = TranslatorTypes.createTranslator(0, DPTUtil.NORMALIZED_DPT.getOrDefault(dpt, dpt));
            if (!(translator instanceof DPTXlatorUtf8) && asdu.length != Math.max(1, translator.getTypeSize())) {
                return false;
            }
            translator.setData(asdu);
            translator.getValue();
            return true;
        } catch (KNXException | RuntimeException e) {
            return false;
        }
    }

    /** {@return the value as the console shows it, a quantity split into number and unit} */
    public static ValueText text(Type value) {
        if (value instanceof QuantityType<?> quantity) {
            return new ValueText(quantity.toBigDecimal().toPlainString(), quantity.getUnit().toString());
        }
        return new ValueText(value.toString(), null);
    }
}
