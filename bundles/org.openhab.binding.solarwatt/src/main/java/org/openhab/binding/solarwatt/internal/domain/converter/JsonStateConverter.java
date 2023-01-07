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
package org.openhab.binding.solarwatt.internal.domain.converter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;

import com.google.gson.JsonElement;

/**
 * Interface bundling all converters from JsonElement to openhab State.
 *
 * We do not implement the interface but only pass closures.
 *
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public interface JsonStateConverter {
    State convert(JsonElement jsonElement);
}
