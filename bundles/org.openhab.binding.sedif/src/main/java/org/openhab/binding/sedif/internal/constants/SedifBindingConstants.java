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
    public static final ThingTypeUID THING_TYPE_SEDIF = new ThingTypeUID(BINDING_ID, "sedif");
    public static final ThingTypeUID THING_TYPE_WEB_SEDIF_BRIDGE = new ThingTypeUID(BINDING_ID, "sedif-web");

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Set.of(THING_TYPE_SEDIF,
            THING_TYPE_WEB_SEDIF_BRIDGE);

    public static final String SEDIF_LASTUPDATE_PROPS = "lastUpdate";

    public static final String SEDIF_BASE_GROUP = "base";

    public static final String SEDIF_DAILY_CONSUMPTION_GROUP = "daily-consumption";
    public static final String SEDIF_WEEKLY_CONSUMPTION_GROUP = "weekly-consumption";
    public static final String SEDIF_MONTHLY_CONSUMPTION_GROUP = "monthly-consumption";
    public static final String SEDIF_YEARLY_CONSUMPTION_GROUP = "yearly-consumption";

    public static final String SEDIF_CONTRAT_GROUP = "contract";
    public static final String SEDIF_CONTRAT_CLIENT_GROUP = "contract-customer";
    public static final String SEDIF_CONTRAT_PAYER_GROUP = "contract-payer";
    public static final String SEDIF_CONTRAT_METER_GROUP = "contract-meter";

    public static final String CHANNEL_CONSUMPTION = "consumption";

    public static final String CHANNEL_DAILY_YESTERDAY_CONSUMPTION = "yesterday";
    public static final String CHANNEL_DAILY_DAY_MINUS_2_CONSUMPTION = "day-2";
    public static final String CHANNEL_DAILY_DAY_MINUS_3_CONSUMPTION = "day-3";

    public static final String CHANNEL_WEEKLY_THIS_WEEK_CONSUMPTION = "thisWeek";
    public static final String CHANNEL_WEEKLY_LAST_WEEK_CONSUMPTION = "lastWeek";
    public static final String CHANNEL_WEEKLY_WEEK_MINUS_2_CONSUMPTION = "week-2";

    public static final String CHANNEL_MONTHLY_THIS_MONTH_CONSUMPTION = "thisMonth";
    public static final String CHANNEL_MONTHLY_LAST_MONTH_CONSUMPTION = "lastMonth";
    public static final String CHANNEL_MONTHLY_MONTH_MINUS_2_CONSUMPTION = "month-2";

    public static final String CHANNEL_YEARLY_THIS_YEAR_CONSUMPTION = "thisYear";
    public static final String CHANNEL_YEARLY_LAST_YEAR_CONSUMPTION = "lastYear";
    public static final String CHANNEL_YEARLY_YEAR_MINUS_2_CONSUMPTION = "year-2";

    public static final String CHANNEL_PRIX_MOYEN_EAU = "mean-water-price";

    public static final String THING_WATER_METER_PROPERTY_CONTRACT_ORGANIZING_AUTHORITY = "contractOrganizingAuthority";
    public static final String THING_WATER_METER_PROPERTY_CONTRACT_DATE_SORTIE_EPT = "contractDateSortieEpt";
    public static final String THING_WATER_METER_PROPERTY_CONTRACT_EINVOICE = "contractEInvoice";
    public static final String THING_WATER_METER_PROPERTY_CONTRACT_ICL_ACTIVE = "contractIclActive";
    public static final String THING_WATER_METER_PROPERTY_CONTRACT_DIRECT_DEBIT = "contractDirectDebit";
    public static final String THING_WATER_METER_PROPERTY_CONTRACT_NAME = "contractName";
    public static final String THING_WATER_METER_PROPERTY_CONTRACT_STREET = "contractStreet";
    public static final String THING_WATER_METER_PROPERTY_CONTRACT_POST_CODE = "contractPostCode";
    public static final String THING_WATER_METER_PROPERTY_CONTRACT_TOWN = "contractTown";
    public static final String THING_WATER_METER_PROPERTY_CONTRACT_STATE = "contractState";
    public static final String THING_WATER_METER_PROPERTY_CONTRACT_BALANCE = "balance";

    public static final String THING_WATER_METER_PROPERTY_ELMA = "meterElema";
    public static final String THING_WATER_METER_PROPERTY_ELMB = "meterElemb";
    public static final String THING_WATER_METER_PROPERTY_ID_PDS = "meterIdPds";
    public static final String THING_WATER_METER_PROPERTY_NUM_METER = "meterId";

    public static final String THING_WATER_METER_PROPERTY_CUSTOMER_BILLING_TOWN = "customerBillingTown";
    public static final String THING_WATER_METER_PROPERTY_CUSTOMER_BILLING_POST_CODE = "customerBillingPostCode";
    public static final String THING_WATER_METER_PROPERTY_CUSTOMER_BILLING_STREET = "customerBillingStreet";
    public static final String THING_WATER_METER_PROPERTY_CUSTOMER_FIRST_NAME = "customerFirstName";
    public static final String THING_WATER_METER_PROPERTY_CUSTOMER_LAST_NAME = "customerLastName";
    public static final String THING_WATER_METER_PROPERTY_CUSTOMER_NAME_SUP = "customernameComplement";
    public static final String THING_WATER_METER_PROPERTY_CUSTOMER_EMAIL = "customerEmail";
    public static final String THING_WATER_METER_PROPERTY_CUSTOMER_GC = "customerGc";
    public static final String THING_WATER_METER_PROPERTY_CUSTOMER_MOBILE_PHONE = "customerMobilePhone";
    public static final String THING_WATER_METER_PROPERTY_CUSTOMER_TITLE = "customerTitle";
    public static final String THING_WATER_METER_PROPERTY_CUSTOMER_LOCK = "customerCustomerLock";

    public static final String THING_WATER_METER_PROPERTY_PAYER_BILLING_CITY = "payerBillingTown";
    public static final String THING_WATER_METER_PROPERTY_PAYER_BILLING_POSTAL_CODE = "payerBillingPostCode";
    public static final String THING_WATER_METER_PROPERTY_PAYER_BILLING_STREET = "payerBillingStreet";
    public static final String THING_WATER_METER_PROPERTY_PAYER_FIRST_NAME = "payerFirstName";
    public static final String THING_WATER_METER_PROPERTY_PAYER_LAST_NAME = "payerLastName";
    public static final String THING_WATER_METER_PROPERTY_PAYER_NAME_SUP = "payernameComplement";
    public static final String THING_WATER_METER_PROPERTY_PAYER_EMAIL = "payerEmail";
    public static final String THING_WATER_METER_PROPERTY_PAYER_GC = "payerGc";
    public static final String THING_WATER_METER_PROPERTY_PAYER_MOBILE_PHONE = "payerMobilePhone";
    public static final String THING_WATER_METER_PROPERTY_PAYER_TITLE = "payerTitle";
    public static final String THING_WATER_METER_PROPERTY_PAYER_LOCK = "payerLock";
}
