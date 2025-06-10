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
package org.openhab.binding.sedif.internal.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ContractDetail} holds Contract information
 *
 * @author Laurent Arnal - Initial contribution
 */
public class ContractDetail extends Value {
    public class CompteInfo {
        @SerializedName("ELMA")
        public String eLma;
        @SerializedName("ELEMB")
        public String eLmb;
        @SerializedName("ID_PDS")
        public String idPds;
        @SerializedName("NUM_COMPTEUR")
        public String numCompteur;
    }

    public class Client {
        public String billingCity;
        public String billingPostalCode;
        public String billingStreet;
        public String complementNom;
        public String email;
        public String firstName;
        public boolean gC;
        public String id;
        public String lastName;
        public String mobilePhone;
        public String name;
        public String salutation;
        @SerializedName("VCRM_ID")
        public String vCrmId;
        public boolean verrouillageFiche;
    }

    public List<CompteInfo> compteInfo = new ArrayList<CompteInfo>();
    public @Nullable Contract contrat;
    public @Nullable Client contratClient;
    public @Nullable Client payeurClient;
    public boolean compteParticulier;
    public boolean multipleContrats;
    public float solde;
}
