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
package org.openhab.binding.hydrawise.internal.api.local.dto;

import java.util.List;

/**
 * The {@link Features} class models an accounts features.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class Features {

    public List<PlanArray> planArray = null;

    public Object id;

    public String planType2;

    public String planType2Key;

    public Object sku;

    public String discount;

    public String cost;

    public String costUs;

    public String costAu;

    public String costEu;

    public String costCa;

    public String costUk;

    public String active;

    public String controllerQty;

    public String rainfall;

    public String smsQty;

    public String scheduledReports;

    public String emailAlerts;

    public String defineSensor;

    public String addUser;

    public String contractor;

    public Object description;

    public String sensorPack;

    public String filelimit;

    public String filetypeall;

    public String planType;

    public String pushNotification;

    public String weatherQty;

    public String weatherFreeQty;

    public String reportingDays;

    public String weatherHourlyUpdates;

    public String freeEnthusiastPlans;

    public String visible;

    public Object contractorPurchasable;

    public Integer boc;

    public Object expiry;

    public Object start;

    public String customerplanId;

    public Integer smsUsed;
}
