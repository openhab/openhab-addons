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
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API *
 */
@NonNullByDefault
public class SedifBindingConstants {

    public static final String BINDING_ID = "sedif";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SEDIF = new ThingTypeUID(BINDING_ID, "sedif");
    public static final ThingTypeUID THING_TYPE_WEB_SEDIF_BRIDGE = new ThingTypeUID(BINDING_ID, "sedif-web");

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Set.of(THING_TYPE_SEDIF,
            THING_TYPE_WEB_SEDIF_BRIDGE);

    public static final String SEDIF_BASE_GROUP = "sedif-base";
    public static final String SEDIF_CONTRAT_GROUP = "sedif-contrat";
    public static final String SEDIF_CONTRAT_CLIENT_GROUP = "sedif-contrat-client";
    public static final String SEDIF_CONTRAT_PAYER_GROUP = "sedif-contrat-payer";
    public static final String SEDIF_CONTRAT_METER_GROUP = "sedif-contrat-meter";

    public static final String CHANNEL_CONSUMPTION = "consumption";
    public static final String CHANNEL_PRIX_MOYEN_EAU = "prix-moyen-eau";

    public static final String CHANNEL_AUTORITE_ORGANISATRICE = "autorite-organisatrice";
    public static final String CHANNEL_DATE_SORTIE_EPT = "date-sortie-ept";
    public static final String CHANNEL_EFACTURE = "efacture";
    public static final String CHANNEL_ICL_ACTIVE = "icl-active";
    public static final String CHANNEL_PRELEVAUTO = "prelevauto";
    public static final String CHANNEL_NAME = "name";
    public static final String CHANNEL_STREET = "rue";
    public static final String CHANNEL_CP = "cp";
    public static final String CHANNEL_TOWN = "commune";
    public static final String CHANNEL_STATE = "statut";
    public static final String CHANNEL_ELMA = "elema";
    public static final String CHANNEL_ELMB = "elemb";
    public static final String CHANNEL_ID_PDS = "id-pds";
    public static final String CHANNEL_NUM_METER = "num-compteur";
    public static final String CHANNEL_CONTRAT_BILLING_CITY = "contrat-billing-city";
    public static final String CHANNEL_CONTRAT_BILLING_POSTAL_CODE = "contrat-billing-postal-code";
    public static final String CHANNEL_CONTRAT_BILLING_STREET = "contrat-billing-street";
    public static final String CHANNEL_CONTRAT_FIRST_NAME = "contrat-first-name";
    public static final String CHANNEL_CONTRAT_LAST_NAME = "contrat-last-name";
    public static final String CHANNEL_CONTRAT_NAME_SUP = "contrat-name-sup";
    public static final String CHANNEL_CONTRAT_EMAIL = "contrat-email";
    public static final String CHANNEL_CONTRAT_GC = "contrat-gc";
    public static final String CHANNEL_CONTRAT_MOBILE_PHONE = "contrat-mobile-phone";
    public static final String CHANNEL_CONTRAT_SALUTATION = "contrat-salutation";
    public static final String CHANNEL_CONTRAT_VEROUILLAGE_FICHE = "contrat-verrouillage-fiche";
    public static final String CHANNEL_PAYER_BILLING_CITY = "payer-billing-city";
    public static final String CHANNEL_PAYER_BILLING_POSTAL_CODE = "payer-billing-postal-code";
    public static final String CHANNEL_PAYER_BILLING_STREET = "payer-billing-street";
    public static final String CHANNEL_PAYER_FIRST_NAME = "payer-first-name";
    public static final String CHANNEL_PAYER_LAST_NAME = "payer-last-name";
    public static final String CHANNEL_PAYER_NAME_SUP = "payer-name-sup";
    public static final String CHANNEL_PAYER_EMAIL = "payer-email";
    public static final String CHANNEL_PAYER_GC = "payer-gc";
    public static final String CHANNEL_PAYER_MOBILE_PHONE = "payer-mobile-phone";
    public static final String CHANNEL_PAYER_SALUTATION = "payer-salutation";
    public static final String CHANNEL_PAYER_VEROUILLAGE_FICHE = "payer-verrouillage-fiche";
    public static final String CHANNEL_SOLDE = "solde";

}
