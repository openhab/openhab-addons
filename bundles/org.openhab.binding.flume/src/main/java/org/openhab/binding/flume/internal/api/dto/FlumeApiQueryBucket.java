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
package org.openhab.binding.flume.internal.api.dto;

import java.time.LocalDateTime;

/**
 * The {@link FlumeApiQueryBucket} dto for query water usage.
 *
 * @author Jeff James - Initial contribution
 */
public class FlumeApiQueryBucket {
    public LocalDateTime datetime; // "datetime": "2016-03-01 00:30:00"
    public float value; // "value": 2.7943592
}
