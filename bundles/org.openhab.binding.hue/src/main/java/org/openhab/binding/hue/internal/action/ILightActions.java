/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.Command;

/**
 * The {@link ILightActions} defines the interface for all thing actions supported by the binding.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public interface ILightActions {

    public void fadingLightCommand(@Nullable String channel, @Nullable Command command, @Nullable DecimalType fadeTime);
}
