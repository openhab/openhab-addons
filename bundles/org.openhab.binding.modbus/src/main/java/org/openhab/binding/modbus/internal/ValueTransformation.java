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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.osgi.framework.BundleContext;

/**
 * Interface for Transformation
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public interface ValueTransformation {

    String transform(BundleContext context, String value);

    boolean isIdentityTransform();

    /**
     * Transform state to another state using this transformation
     *
     * @param context
     * @param types types to used to parse the transformation result
     * @param command
     * @return Transformed command, or null if no transformation was possible
     */
    default @Nullable State transformState(BundleContext context, List<Class<? extends State>> types, State state) {
        // Note that even identity transformations go through the State -> String -> State steps. This does add some
        // overhead but takes care of DecimalType -> PercentType conversions, for example.
        final String stateAsString = state.toString();
        final String transformed = transform(context, stateAsString);
        return TypeParser.parseState(types, transformed);
    }
}
