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
package org.openhab.binding.sedif.internal.constants;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SedifBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SedifBindingConstants {

    public static final String BINDING_ID = "sedif";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_METER = new ThingTypeUID(BINDING_ID, "meter");
    public static final ThingTypeUID THING_TYPE_GATEWAY_BRIDGE = new ThingTypeUID(BINDING_ID, "gateway");

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Set.of(THING_TYPE_METER,
            THING_TYPE_GATEWAY_BRIDGE);

    public static final String SEDIF_LASTUPDATE_PROPS = "lastUpdate";

    public static final String GROUP_BASE = "base";
    public static final String GROUP_DAILY_CONSUMPTION = "daily-consumption";
    public static final String GROUP_WEEKLY_CONSUMPTION = "weekly-consumption";
    public static final String GROUP_MONTHLY_CONSUMPTION = "monthly-consumption";
    public static final String GROUP_YEARLY_CONSUMPTION = "yearly-consumption";

    public static final String CHANNEL_CONSUMPTION = "consumption";

    public static final String CHANNEL_DAILY_YESTERDAY_CONSUMPTION = "yesterday";
    public static final String CHANNEL_DAILY_DAY_MINUS_2_CONSUMPTION = "day-2";
    public static final String CHANNEL_DAILY_DAY_MINUS_3_CONSUMPTION = "day-3";

    public static final String CHANNEL_WEEKLY_THIS_WEEK_CONSUMPTION = "this-week";
    public static final String CHANNEL_WEEKLY_LAST_WEEK_CONSUMPTION = "last-week";
    public static final String CHANNEL_WEEKLY_WEEK_MINUS_2_CONSUMPTION = "week-2";

    public static final String CHANNEL_MONTHLY_THIS_MONTH_CONSUMPTION = "this-month";
    public static final String CHANNEL_MONTHLY_LAST_MONTH_CONSUMPTION = "last-month";
    public static final String CHANNEL_MONTHLY_MONTH_MINUS_2_CONSUMPTION = "month-2";

    public static final String CHANNEL_YEARLY_THIS_YEAR_CONSUMPTION = "this-year";
    public static final String CHANNEL_YEARLY_LAST_YEAR_CONSUMPTION = "last-year";
    public static final String CHANNEL_YEARLY_YEAR_MINUS_2_CONSUMPTION = "year-2";

    public static final String CHANNEL_MEAN_WATER_PRICE = "mean-water-price";

    public static final String PROPERTY_CONTRACT_ORGANIZING_AUTHORITY = "contractOrganizingAuthority";
    public static final String PROPERTY_CONTRACT_DATE_SORTIE_EPT = "contractDateSortieEpt";
    public static final String PROPERTY_CONTRACT_EINVOICE = "contractEInvoice";
    public static final String PROPERTY_CONTRACT_ICL_ACTIVE = "contractIclActive";
    public static final String PROPERTY_CONTRACT_DIRECT_DEBIT = "contractDirectDebit";
    public static final String PROPERTY_CONTRACT_NAME = "contractName";
    public static final String PROPERTY_CONTRACT_STREET = "contractStreet";
    public static final String PROPERTY_CONTRACT_POST_CODE = "contractPostCode";
    public static final String PROPERTY_CONTRACT_TOWN = "contractTown";
    public static final String PROPERTY_CONTRACT_STATE = "contractState";
    public static final String PROPERTY_CONTRACT_BALANCE = "balance";

    public static final String PROPERTY_ELMA = "meterElema";
    public static final String PROPERTY_ELMB = "meterElemb";
    public static final String PROPERTY_ID_PDS = "meterIdPds";
    public static final String PROPERTY_NUM_METER = "meterId";
    public static final String PROPERTY_CONTRACT_ID = "contractId";

    public static final String PROPERTY_CUSTOMER_BILLING_TOWN = "customerBillingTown";
    public static final String PROPERTY_CUSTOMER_BILLING_POST_CODE = "customerBillingPostCode";
    public static final String PROPERTY_CUSTOMER_BILLING_STREET = "customerBillingStreet";
    public static final String PROPERTY_CUSTOMER_FIRST_NAME = "customerFirstName";
    public static final String PROPERTY_CUSTOMER_LAST_NAME = "customerLastName";
    public static final String PROPERTY_CUSTOMER_NAME_SUP = "customernameComplement";
    public static final String PROPERTY_CUSTOMER_EMAIL = "customerEmail";
    public static final String PROPERTY_CUSTOMER_GC = "customerGc";
    public static final String PROPERTY_CUSTOMER_MOBILE_PHONE = "customerMobilePhone";
    public static final String PROPERTY_CUSTOMER_TITLE = "customerTitle";
    public static final String PROPERTY_CUSTOMER_LOCK = "customerCustomerLock";

    public static final String PROPERTY_PAYER_BILLING_CITY = "payerBillingTown";
    public static final String PROPERTY_PAYER_BILLING_POSTAL_CODE = "payerBillingPostCode";
    public static final String PROPERTY_PAYER_BILLING_STREET = "payerBillingStreet";
    public static final String PROPERTY_PAYER_FIRST_NAME = "payerFirstName";
    public static final String PROPERTY_PAYER_LAST_NAME = "payerLastName";
    public static final String PROPERTY_PAYER_NAME_SUP = "payernameComplement";
    public static final String PROPERTY_PAYER_EMAIL = "payerEmail";
    public static final String PROPERTY_PAYER_GC = "payerGc";
    public static final String PROPERTY_PAYER_MOBILE_PHONE = "payerMobilePhone";
    public static final String PROPERTY_PAYER_TITLE = "payerTitle";
    public static final String PROPERTY_PAYER_LOCK = "payerLock";

    public static final int REQUEST_TIMEOUT = 30;
}
