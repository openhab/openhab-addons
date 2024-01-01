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
package org.openhab.binding.boschshc.internal.services.communicationquality.dto;

import org.openhab.binding.boschshc.internal.services.dto.BoschSHCServiceState;

/**
 * State of the communication quality service.
 * <p>
 * Example JSON:
 * 
 * <pre>
 * {
 *   "@type": "communicationQualityState",
 *   "quality": "UNKNOWN"
 * }
 * </pre>
 * 
 * @author David Pace - Initial contribution
 *
 */
public class CommunicationQualityServiceState extends BoschSHCServiceState {

    public CommunicationQualityServiceState() {
        super("communicationQualityState");
    }

    public CommunicationQualityState quality;
}
