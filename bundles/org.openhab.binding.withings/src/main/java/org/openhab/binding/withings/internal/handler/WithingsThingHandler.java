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
package org.openhab.binding.withings.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.withings.internal.api.WithingsDataModel;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public interface WithingsThingHandler {

    void updateData(WithingsDataModel model);
}
