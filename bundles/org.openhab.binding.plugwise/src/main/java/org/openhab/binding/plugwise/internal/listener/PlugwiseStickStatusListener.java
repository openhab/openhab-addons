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
package org.openhab.binding.plugwise.internal.listener;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.plugwise.internal.handler.PlugwiseStickHandler;
import org.openhab.core.thing.ThingStatus;

/**
 * Interface for listeners of {@link PlugwiseStickHandler} thing status changes.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public interface PlugwiseStickStatusListener {

    public void stickStatusChanged(ThingStatus status);
}
