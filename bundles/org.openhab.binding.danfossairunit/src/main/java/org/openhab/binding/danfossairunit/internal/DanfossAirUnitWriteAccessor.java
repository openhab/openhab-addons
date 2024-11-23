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
package org.openhab.binding.danfossairunit.internal;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * The {@link DanfossAirUnitWriteAccessor} encapsulates access to an air unit value to be written.
 *
 * @author Robert Bach - Initial contribution
 */
@FunctionalInterface
@NonNullByDefault
public interface DanfossAirUnitWriteAccessor {
    State access(DanfossAirUnit hrv, Command command) throws IOException;
}
