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

/**
 * The {@link ContractDetail} holds Contract information
 *
 * @author Laurent Arnal - Initial contribution
 */
public class ContractDetail extends Value {
    public class CompteInfo {
        public String ELEMA;
        public String ELEMB;
        public String ID_PDS;
        public String NUM_COMPTEUR;
    }

    public class Client {
        public String BillingCity;
        public String BillingPostalCode;
        public String BillingStreet;
        public String ComplementNom;
        public String Email;
        public String FirstName;
        public boolean GC;
        public String Id;
        public String LastName;
        public String MobilePhone;
        public String Name;
        public String Salutation;
        public String VCRM_ID;
        public boolean VerrouillageFiche;
    }

    public List<CompteInfo> compteInfo = new ArrayList<CompteInfo>();
    public @Nullable Contract contrat;
    public @Nullable Client contratClient;
    public @Nullable Client payeurClient;
    public boolean compteParticulier;
    public boolean multipleContrats;
    public float solde;
}
