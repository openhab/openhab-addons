/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.myq.internal.dto;

/**
 * The {@link AccountDTO} entity from the MyQ API
 *
 * @author Dan Cunningham - Initial contribution
 */
public class AccountDTO {

    public UsersDTO users;
    public Boolean admin;
    public AccountInfoDTO account;
    public String analyticsId;
    public String userId;
    public String userName;
    public String email;
    public String firstName;
    public String lastName;
    public String cultureCode;
    public AddressDTO address;
    public TimeZoneDTO timeZone;
    public Boolean mailingListOptIn;
    public Boolean requestAccountLinkInfo;
    public String phone;
    public Boolean diagnosticDataOptIn;
}
