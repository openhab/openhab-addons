package org.openhab.binding.toyota.internal.dto;

import java.util.ArrayList;

public class CommPref {
    public boolean sms;
    public boolean tel;
    public boolean email;
    public boolean post;
    public ArrayList<Email> emails;
    public ArrayList<Phone> phones;
    public String language;
    public boolean personalDataTreatment;
    public boolean personalDataTransfer;
    public boolean personalDataSurvey;
}
