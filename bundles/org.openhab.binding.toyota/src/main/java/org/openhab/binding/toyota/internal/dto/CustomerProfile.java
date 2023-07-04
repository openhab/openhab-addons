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
