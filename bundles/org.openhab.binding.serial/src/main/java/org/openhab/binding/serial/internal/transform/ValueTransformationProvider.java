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
package org.openhab.binding.serial.internal.transform;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ValueTransformationProvider} allows to retrieve a transformation service by name
 *
 * @author Jan N. Klug - Initial contribution
 * @author Mike Major - Copied from HTTP binding to provide consistent user experience
 */
@NonNullByDefault
public interface ValueTransformationProvider {

    /**
     *
     * @param pattern A transformation pattern, starting with the transformation service
     *            * name, followed by a colon and the transformation itself.
     * @return
     */
    ValueTransformation getValueTransformation(@Nullable String pattern);
}
