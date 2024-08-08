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
package org.openhab.binding.linky.internal.dto;

import org.eclipse.jetty.jaas.spi.UserInfo;

/**
 * The {@link UserInfo} holds informations about energy delivery point
 *
 * @author Laurent Arnal - Initial contribution
 */

public class Contracts {
    public String segment;
    public String subscribedPower;
    public String lastActivationDate;
    public String distributionTariff;
    public String offpeakHours;
    public String contractStatus;
    public String contractType;
    public String lastDistributionTariffChangeDate;
}
