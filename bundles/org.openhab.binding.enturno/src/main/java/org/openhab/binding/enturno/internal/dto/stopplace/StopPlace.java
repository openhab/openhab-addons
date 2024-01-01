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
package org.openhab.binding.enturno.internal.dto.stopplace;

import org.openhab.binding.enturno.internal.dto.estimated.EstimatedCalls;

/**
 * Generated Plain Old Java Objects class for {@link StopPlace} from JSON.
 *
 * @author Michal Kloc - Initial contribution
 */
public class StopPlace {
    public java.util.List<EstimatedCalls> estimatedCalls;

    public String name;

    public String id;

    public String transportMode;
}
