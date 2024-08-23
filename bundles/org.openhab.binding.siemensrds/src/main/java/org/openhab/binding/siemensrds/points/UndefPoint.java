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
package org.openhab.binding.siemensrds.points;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * private class a data point where "value" is unknown
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class UndefPoint extends BasePoint {

    @Override
    public State getState() {
        return UnDefType.UNDEF;
    }

    @Override
    public int asInt() {
        return UNDEFINED_VALUE;
    }

    @Override
    public void refreshValueFrom(BasePoint from) {
        // do nothing
    }
}
