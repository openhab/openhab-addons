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
package org.openhab.transform.vat.internal;

import static org.openhab.transform.vat.internal.VATTransformationConstants.*;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.UnDefType;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link TransformationService} adds VAT to the input according to configured country.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@Component(service = { TransformationService.class }, property = { "openhab.transform=VAT" })
public class VATTransformationService implements TransformationService {

    private final Logger logger = LoggerFactory.getLogger(VATTransformationService.class);

    @Override
    public @Nullable String transform(String valueString, String sourceString) throws TransformationException {
        QuantityType<?> source;
        try {
            source = new QuantityType<>(sourceString);
        } catch (IllegalArgumentException e) {
            if (UnDefType.NULL.toString().equals(sourceString) || UnDefType.UNDEF.toString().equals(sourceString)) {
                return sourceString;
            }
            logger.warn("Input value '{}' could not be converted to a valid number", sourceString);
            throw new TransformationException("VAT Transformation can only be used with numeric inputs", e);
        }
        BigDecimal value;
        try {
            value = new BigDecimal(valueString);
        } catch (NumberFormatException e) {
            String rate = RATES.get(valueString);
            if (rate == null) {
                logger.warn("Input value '{}' could not be converted to a valid number or country code", valueString);
                throw new TransformationException("VAT Transformation can only be used with numeric inputs", e);
            }
            value = new BigDecimal(rate);
        }

        return addVAT(source, value).toString();
    }

    private QuantityType<?> addVAT(QuantityType<?> source, BigDecimal percentage) {
        return source.multiply(percentage.divide(new BigDecimal("100")).add(BigDecimal.ONE));
    }
}
