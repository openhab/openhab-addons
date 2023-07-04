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
package org.openhab.binding.toyota.internal.dto;

import java.util.ArrayList;

public class CustomerProfile {
    public static String UUID = "uuid";
    public static String MY_TOYOTA_ID = "myToyotaId";

    public ArrayList<Address> addresses;
    public String username;
    public String email;
    public String firstName;
    public String lastName;
    public String languageCode;
    public String countryCode;
    public String title;
    public String uuid;
    public CommPref commPref;
    public String myToyotaId;
    public boolean active;
    public boolean personalDataTreatment;
    public boolean personalDataTransfer;
    public boolean personalDataSurvey;
}
