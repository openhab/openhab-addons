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
package org.openhab.binding.boschshc.internal.devices.smokedetector;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.AbstractSmokeDetectorHandler;
import org.openhab.core.thing.Thing;

/**
 * The smoke detector warns you in case of fire.
 *
 * @author Christian Oeing - Initial contribution
 * @author Gerd Zanker - AbstractSmokeDetectorHandler refactoring for reuse
 */
@NonNullByDefault
public class SmokeDetectorHandler extends AbstractSmokeDetectorHandler {

    public SmokeDetectorHandler(Thing thing) {
        super(thing);
    }
}
