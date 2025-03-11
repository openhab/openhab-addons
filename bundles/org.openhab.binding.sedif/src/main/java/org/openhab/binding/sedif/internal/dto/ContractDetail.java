package org.openhab.binding.sedif.internal.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

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

    public @Nullable List<CompteInfo> compteInfo = new ArrayList<CompteInfo>();
    public @Nullable Contract contrat;
    public @Nullable Client contratClient;
    public @Nullable Client payeurClient;
    public boolean compteParticulier;
    public boolean multipleContrats;
    public float solde;
}
