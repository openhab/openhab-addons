/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lametrictime.internal.api.local.dto;

/**
 * Pojo for endpoints.
 *
 * @author Gregory Moyer - Initial contribution
 */
public class Endpoints {
    private String appsActionUrl;
    private String appsGetUrl;
    private String appsListUrl;
    private String appsSwitchNextUrl;
    private String appsSwitchPrevUrl;
    private String appsSwitchUrl;
    private String audioUrl;
    private String bluetoothUrl;
    private String concreteNotificationUrl;
    private String currentNotificationUrl;
    private String deviceUrl;
    private String displayUrl;
    private String notificationsUrl;
    private String widgetUpdateUrl;
    private String wifiUrl;

    public String getAppsActionUrl() {
        return appsActionUrl;
    }

    public void setAppsActionUrl(String appsActionUrl) {
        this.appsActionUrl = appsActionUrl;
    }

    public Endpoints withAppsActionUrl(String appsActionUrl) {
        this.appsActionUrl = appsActionUrl;
        return this;
    }

    public String getAppsGetUrl() {
        return appsGetUrl;
    }

    public void setAppsGetUrl(String appsGetUrl) {
        this.appsGetUrl = appsGetUrl;
    }

    public Endpoints withAppsGetUrl(String appsGetUrl) {
        this.appsGetUrl = appsGetUrl;
        return this;
    }

    public String getAppsListUrl() {
        return appsListUrl;
    }

    public void setAppsListUrl(String appsListUrl) {
        this.appsListUrl = appsListUrl;
    }

    public Endpoints withAppsListUrl(String appsListUrl) {
        this.appsListUrl = appsListUrl;
        return this;
    }

    public String getAppsSwitchNextUrl() {
        return appsSwitchNextUrl;
    }

    public void setAppsSwitchNextUrl(String appsSwitchNextUrl) {
        this.appsSwitchNextUrl = appsSwitchNextUrl;
    }

    public Endpoints withAppsSwitchNextUrl(String appsSwitchNextUrl) {
        this.appsSwitchNextUrl = appsSwitchNextUrl;
        return this;
    }

    public String getAppsSwitchPrevUrl() {
        return appsSwitchPrevUrl;
    }

    public void setAppsSwitchPrevUrl(String appsSwitchPrevUrl) {
        this.appsSwitchPrevUrl = appsSwitchPrevUrl;
    }

    public Endpoints withAppsSwitchPrevUrl(String appsSwitchPrevUrl) {
        this.appsSwitchPrevUrl = appsSwitchPrevUrl;
        return this;
    }

    public String getAppsSwitchUrl() {
        return appsSwitchUrl;
    }

    public void setAppsSwitchUrl(String appsSwitchUrl) {
        this.appsSwitchUrl = appsSwitchUrl;
    }

    public Endpoints withAppsSwitchUrl(String appsSwitchUrl) {
        this.appsSwitchUrl = appsSwitchUrl;
        return this;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public Endpoints withAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
        return this;
    }

    public String getBluetoothUrl() {
        return bluetoothUrl;
    }

    public void setBluetoothUrl(String bluetoothUrl) {
        this.bluetoothUrl = bluetoothUrl;
    }

    public Endpoints withBluetoothUrl(String bluetoothUrl) {
        this.bluetoothUrl = bluetoothUrl;
        return this;
    }

    public String getConcreteNotificationUrl() {
        return concreteNotificationUrl;
    }

    public void setConcreteNotificationUrl(String concreteNotificationUrl) {
        this.concreteNotificationUrl = concreteNotificationUrl;
    }

    public Endpoints withConcreteNotificationUrl(String concreteNotificationUrl) {
        this.concreteNotificationUrl = concreteNotificationUrl;
        return this;
    }

    public String getCurrentNotificationUrl() {
        return currentNotificationUrl;
    }

    public void setCurrentNotificationUrl(String currentNotificationUrl) {
        this.currentNotificationUrl = currentNotificationUrl;
    }

    public Endpoints withCurrentNotificationUrl(String currentNotificationUrl) {
        this.currentNotificationUrl = currentNotificationUrl;
        return this;
    }

    public String getDeviceUrl() {
        return deviceUrl;
    }

    public void setDeviceUrl(String deviceUrl) {
        this.deviceUrl = deviceUrl;
    }

    public Endpoints withDeviceUrl(String deviceUrl) {
        this.deviceUrl = deviceUrl;
        return this;
    }

    public String getDisplayUrl() {
        return displayUrl;
    }

    public void setDisplayUrl(String displayUrl) {
        this.displayUrl = displayUrl;
    }

    public Endpoints withDisplayUrl(String displayUrl) {
        this.displayUrl = displayUrl;
        return this;
    }

    public String getNotificationsUrl() {
        return notificationsUrl;
    }

    public void setNotificationsUrl(String notificationsUrl) {
        this.notificationsUrl = notificationsUrl;
    }

    public Endpoints withNotificationsUrl(String notificationsUrl) {
        this.notificationsUrl = notificationsUrl;
        return this;
    }

    public String getWidgetUpdateUrl() {
        return widgetUpdateUrl;
    }

    public void setWidgetUpdateUrl(String widgetUpdateUrl) {
        this.widgetUpdateUrl = widgetUpdateUrl;
    }

    public Endpoints withWidgetUpdateUrl(String widgetUpdateUrl) {
        this.widgetUpdateUrl = widgetUpdateUrl;
        return this;
    }

    public String getWifiUrl() {
        return wifiUrl;
    }

    public void setWifiUrl(String wifiUrl) {
        this.wifiUrl = wifiUrl;
    }

    public Endpoints withWifiUrl(String wifiUrl) {
        this.wifiUrl = wifiUrl;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Endpoints [appsActionUrl=");
        builder.append(appsActionUrl);
        builder.append(", appsGetUrl=");
        builder.append(appsGetUrl);
        builder.append(", appsListUrl=");
        builder.append(appsListUrl);
        builder.append(", appsSwitchNextUrl=");
        builder.append(appsSwitchNextUrl);
        builder.append(", appsSwitchPrevUrl=");
        builder.append(appsSwitchPrevUrl);
        builder.append(", appsSwitchUrl=");
        builder.append(appsSwitchUrl);
        builder.append(", audioUrl=");
        builder.append(audioUrl);
        builder.append(", bluetoothUrl=");
        builder.append(bluetoothUrl);
        builder.append(", concreteNotificationUrl=");
        builder.append(concreteNotificationUrl);
        builder.append(", currentNotificationUrl=");
        builder.append(currentNotificationUrl);
        builder.append(", deviceUrl=");
        builder.append(deviceUrl);
        builder.append(", displayUrl=");
        builder.append(displayUrl);
        builder.append(", notificationsUrl=");
        builder.append(notificationsUrl);
        builder.append(", widgetUpdateUrl=");
        builder.append(widgetUpdateUrl);
        builder.append(", wifiUrl=");
        builder.append(wifiUrl);
        builder.append("]");
        return builder.toString();
    }
}
