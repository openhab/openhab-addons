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
package org.openhab.binding.modbus.internal;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.BundleContext;

/**
 * The {@link CascadedValueTransformationImpl} implements {@link SingleValueTransformation for a cascaded set of
 * transformations}
 *
 * @author Jan N. Klug - Initial contribution
 * @author Sami Salonen - Copied from HTTP binding to provide consistent user experience
 */
@NonNullByDefault
public class CascadedValueTransformationImpl implements ValueTransformation {
    private final List<SingleValueTransformation> transformations;

    public CascadedValueTransformationImpl(@Nullable String transformationString) {
        String transformationNonNull = transformationString == null ? "" : transformationString;
        List<SingleValueTransformation> localTransformations = Arrays.stream(transformationNonNull.split("∩"))
                .filter(s -> !s.isEmpty()).map(transformation -> new SingleValueTransformation(transformation))
                .collect(Collectors.toList());
        if (localTransformations.isEmpty()) {
            localTransformations = List.of(new SingleValueTransformation(transformationString));
        }
        transformations = localTransformations;
    }

    @Override
    public String transform(BundleContext context, String value) {
        String input = value;
        // process all transformations
        for (final ValueTransformation transformation : transformations) {
            input = transformation.transform(context, input);
        }
        return input;
    }

    @Override
    public boolean isIdentityTransform() {
        return transformations.stream().allMatch(SingleValueTransformation::isIdentityTransform);
    }

    @Override
    public String toString() {
        return "CascadedValueTransformationImpl("
                + transformations.stream().map(SingleValueTransformation::toString).collect(Collectors.joining(" ∩ "))
                + ")";
    }

    List<SingleValueTransformation> getTransformations() {
        return transformations;
    }
}
