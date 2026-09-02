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
package org.openhab.transform.math.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.transform.TransformationService;
import org.osgi.service.component.annotations.Component;

/**
 * This {@link TransformationService} multiplies the input by a given value.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
@Component(service = { TransformationService.class }, property = { "openhab.transform=MULTIPLY" })
public class MultiplyTransformationService extends AbstractMathTransformationService {

    @Override
    QuantityType<?> performCalculation(QuantityType<?> source, QuantityType<?> value) {
        return source.multiply(value);
    }
}
