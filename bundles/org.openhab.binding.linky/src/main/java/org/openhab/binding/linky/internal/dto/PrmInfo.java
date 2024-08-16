/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
 * The {@link UserInfo} holds ids of existing Prms
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */

public class PrmInfo {

    public PrmInfo() {
        customerId = "";
        contractInfo = new Contracts();
        identityInfo = new IdentityInfo();
        addressInfo = new AddressInfo();
        contactInfo = new ContactInfo();
        usagePointInfo = new UsagePointDetails();
    }

    public String prmId;
    public String customerId;

    public Contracts contractInfo;
    public UsagePointDetails usagePointInfo;
    public ContactInfo contactInfo;
    public AddressInfo addressInfo;
    public IdentityInfo identityInfo;
}
