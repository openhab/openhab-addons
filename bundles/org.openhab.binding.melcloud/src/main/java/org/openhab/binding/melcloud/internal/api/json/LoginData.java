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
package org.openhab.binding.melcloud.internal.api.json;

import com.google.gson.annotations.Expose;

/**
 * The {@link LoginData} is responsible of JSON data For MELCloud API
 * LoginData for Login Request.
 * Generated with jsonschema2pojo
 *
 * @author Luca Calcaterra - Initial contribution
 */
public class LoginData {

    @Expose
    private String contextKey;

    @Expose
    private Integer client;

    @Expose
    private Integer terms;

    @Expose
    private Integer aL;

    @Expose
    private Integer mL;

    @Expose
    private Boolean cMI;

    @Expose
    private Boolean isStaff;

    @Expose
    private Boolean cUTF;

    @Expose
    private Boolean cAA;

    @Expose
    private Boolean receiveCountryNotifications;

    @Expose
    private Boolean receiveAllNotifications;

    @Expose
    private Boolean cACA;

    @Expose
    private Boolean cAGA;

    @Expose
    private Integer maximumDevices;

    @Expose
    private Boolean showDiagnostics;

    @Expose
    private Integer language;

    @Expose
    private Integer country;

    @Expose
    private Integer realClient;

    @Expose
    private String name;

    @Expose
    private Boolean useFahrenheit;

    @Expose
    private Integer duration;

    @Expose
    private String expiry;

    @Expose
    private Boolean cMSC;

    @Expose
    private Object partnerApplicationVersion;

    @Expose
    private Boolean emailSettingsReminderShown;

    @Expose
    private Integer emailUnitErrors;

    @Expose
    private Integer emailCommsErrors;

    @Expose
    private Boolean isImpersonated;

    @Expose
    private String languageCode;

    @Expose
    private String countryName;

    @Expose
    private String currencySymbol;

    @Expose
    private String supportEmailAddress;

    @Expose
    private String dateSeperator;

    @Expose
    private String timeSeperator;

    @Expose
    private String atwLogoFile;

    @Expose
    private Boolean dECCReport;

    @Expose
    private Boolean cSVReport1min;

    @Expose
    private Boolean hidePresetPanel;

    @Expose
    private Boolean emailSettingsReminderRequired;

    @Expose
    private Object termsText;

    @Expose
    private Boolean mapView;

    @Expose
    private Integer mapZoom;

    @Expose
    private Double mapLongitude;

    @Expose
    private Double mapLatitude;

    public String getContextKey() {
        return contextKey;
    }

    public void setContextKey(String contextKey) {
        this.contextKey = contextKey;
    }

    public Integer getClient() {
        return client;
    }

    public void setClient(Integer client) {
        this.client = client;
    }

    public Integer getTerms() {
        return terms;
    }

    public void setTerms(Integer terms) {
        this.terms = terms;
    }

    public Integer getAL() {
        return aL;
    }

    public void setAL(Integer aL) {
        this.aL = aL;
    }

    public Integer getML() {
        return mL;
    }

    public void setML(Integer mL) {
        this.mL = mL;
    }

    public Boolean getCMI() {
        return cMI;
    }

    public void setCMI(Boolean cMI) {
        this.cMI = cMI;
    }

    public Boolean getIsStaff() {
        return isStaff;
    }

    public void setIsStaff(Boolean isStaff) {
        this.isStaff = isStaff;
    }

    public Boolean getCUTF() {
        return cUTF;
    }

    public void setCUTF(Boolean cUTF) {
        this.cUTF = cUTF;
    }

    public Boolean getCAA() {
        return cAA;
    }

    public void setCAA(Boolean cAA) {
        this.cAA = cAA;
    }

    public Boolean getReceiveCountryNotifications() {
        return receiveCountryNotifications;
    }

    public void setReceiveCountryNotifications(Boolean receiveCountryNotifications) {
        this.receiveCountryNotifications = receiveCountryNotifications;
    }

    public Boolean getReceiveAllNotifications() {
        return receiveAllNotifications;
    }

    public void setReceiveAllNotifications(Boolean receiveAllNotifications) {
        this.receiveAllNotifications = receiveAllNotifications;
    }

    public Boolean getCACA() {
        return cACA;
    }

    public void setCACA(Boolean cACA) {
        this.cACA = cACA;
    }

    public Boolean getCAGA() {
        return cAGA;
    }

    public void setCAGA(Boolean cAGA) {
        this.cAGA = cAGA;
    }

    public Integer getMaximumDevices() {
        return maximumDevices;
    }

    public void setMaximumDevices(Integer maximumDevices) {
        this.maximumDevices = maximumDevices;
    }

    public Boolean getShowDiagnostics() {
        return showDiagnostics;
    }

    public void setShowDiagnostics(Boolean showDiagnostics) {
        this.showDiagnostics = showDiagnostics;
    }

    public Integer getLanguage() {
        return language;
    }

    public void setLanguage(Integer language) {
        this.language = language;
    }

    public Integer getCountry() {
        return country;
    }

    public void setCountry(Integer country) {
        this.country = country;
    }

    public Integer getRealClient() {
        return realClient;
    }

    public void setRealClient(Integer realClient) {
        this.realClient = realClient;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getUseFahrenheit() {
        return useFahrenheit;
    }

    public void setUseFahrenheit(Boolean useFahrenheit) {
        this.useFahrenheit = useFahrenheit;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public Boolean getCMSC() {
        return cMSC;
    }

    public void setCMSC(Boolean cMSC) {
        this.cMSC = cMSC;
    }

    public Object getPartnerApplicationVersion() {
        return partnerApplicationVersion;
    }

    public void setPartnerApplicationVersion(Object partnerApplicationVersion) {
        this.partnerApplicationVersion = partnerApplicationVersion;
    }

    public Boolean getEmailSettingsReminderShown() {
        return emailSettingsReminderShown;
    }

    public void setEmailSettingsReminderShown(Boolean emailSettingsReminderShown) {
        this.emailSettingsReminderShown = emailSettingsReminderShown;
    }

    public Integer getEmailUnitErrors() {
        return emailUnitErrors;
    }

    public void setEmailUnitErrors(Integer emailUnitErrors) {
        this.emailUnitErrors = emailUnitErrors;
    }

    public Integer getEmailCommsErrors() {
        return emailCommsErrors;
    }

    public void setEmailCommsErrors(Integer emailCommsErrors) {
        this.emailCommsErrors = emailCommsErrors;
    }

    public Boolean getIsImpersonated() {
        return isImpersonated;
    }

    public void setIsImpersonated(Boolean isImpersonated) {
        this.isImpersonated = isImpersonated;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getSupportEmailAddress() {
        return supportEmailAddress;
    }

    public void setSupportEmailAddress(String supportEmailAddress) {
        this.supportEmailAddress = supportEmailAddress;
    }

    public String getDateSeperator() {
        return dateSeperator;
    }

    public void setDateSeperator(String dateSeperator) {
        this.dateSeperator = dateSeperator;
    }

    public String getTimeSeperator() {
        return timeSeperator;
    }

    public void setTimeSeperator(String timeSeperator) {
        this.timeSeperator = timeSeperator;
    }

    public String getAtwLogoFile() {
        return atwLogoFile;
    }

    public void setAtwLogoFile(String atwLogoFile) {
        this.atwLogoFile = atwLogoFile;
    }

    public Boolean getDECCReport() {
        return dECCReport;
    }

    public void setDECCReport(Boolean dECCReport) {
        this.dECCReport = dECCReport;
    }

    public Boolean getCSVReport1min() {
        return cSVReport1min;
    }

    public void setCSVReport1min(Boolean cSVReport1min) {
        this.cSVReport1min = cSVReport1min;
    }

    public Boolean getHidePresetPanel() {
        return hidePresetPanel;
    }

    public void setHidePresetPanel(Boolean hidePresetPanel) {
        this.hidePresetPanel = hidePresetPanel;
    }

    public Boolean getEmailSettingsReminderRequired() {
        return emailSettingsReminderRequired;
    }

    public void setEmailSettingsReminderRequired(Boolean emailSettingsReminderRequired) {
        this.emailSettingsReminderRequired = emailSettingsReminderRequired;
    }

    public Object getTermsText() {
        return termsText;
    }

    public void setTermsText(Object termsText) {
        this.termsText = termsText;
    }

    public Boolean getMapView() {
        return mapView;
    }

    public void setMapView(Boolean mapView) {
        this.mapView = mapView;
    }

    public Integer getMapZoom() {
        return mapZoom;
    }

    public void setMapZoom(Integer mapZoom) {
        this.mapZoom = mapZoom;
    }

    public Double getMapLongitude() {
        return mapLongitude;
    }

    public void setMapLongitude(Double mapLongitude) {
        this.mapLongitude = mapLongitude;
    }

    public Double getMapLatitude() {
        return mapLatitude;
    }

    public void setMapLatitude(Double mapLatitude) {
        this.mapLatitude = mapLatitude;
    }
}
