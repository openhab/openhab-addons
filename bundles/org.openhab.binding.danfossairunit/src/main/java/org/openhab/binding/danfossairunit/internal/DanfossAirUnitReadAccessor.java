/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.danfossairunit.internal;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;

/**
 * The {@link DanfossAirUnitReadAccessor} encapsulates access to an air unit value to be read.
 *
 * @author Robert Bach - Initial contribution
 */
@FunctionalInterface
@NonNullByDefault
public interface DanfossAirUnitReadAccessor {
    State access(DanfossAirUnit hrv) throws IOException, UnexpectedResponseValueException;
}
