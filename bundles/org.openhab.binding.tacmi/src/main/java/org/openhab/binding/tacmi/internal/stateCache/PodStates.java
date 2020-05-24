/**
 * Copyright (c) 2015-2020 Contributors to the openHAB project
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
package org.openhab.binding.tacmi.internal.stateCache;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The {@link PodStates} class defines a state cache details required
 * for communication with the TA C.M.I.
 *
 * @author Christian Niessner - Initial contribution
 */
public class PodStates {

    public int podId;
    public Collection<PodState> entries = new ArrayList<>();
}
