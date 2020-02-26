/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.goecharger.internal.api;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link GoEStatusResponse} class represents a json response from the
 * charger.
 *
 * @author Samuel Brucksch - Initial contribution
 */
public class GoEStatusResponse {
  @SerializedName("version")
  private String version;

  @SerializedName("car")
  private Integer pwmSignal;

  @SerializedName("amp")
  private Integer maxChargeAmps;

  @SerializedName("nrg")
  private Integer[] energy;

  @SerializedName("err")
  private String errorCode;

  @SerializedName("alw")
  private Integer allowCharging;

  @SerializedName("stp")
  private boolean automaticStop;

  @SerializedName("cbl")
  private Integer cableEncoding;

  @SerializedName("pha")
  private Integer phases;

  @SerializedName("tmp")
  private Integer temperature;

  @SerializedName("dws")
  private Double sessionChargeConsumption;

  @SerializedName("dwo")
  private Double sessionChargeConsumptionLimit;

  @SerializedName("eto")
  private Double totalChargeConsumption;

  @SerializedName("fmw")
  private String firmware;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Integer getPwmSignal() {
    return pwmSignal;
  }

  public void setPwmSignal(Integer pwmSignal) {
    this.pwmSignal = pwmSignal;
  }

  public Integer getMaxChargeAmps() {
    return maxChargeAmps;
  }

  public void setMaxChargeAmps(Integer maxChargeAmps) {
    this.maxChargeAmps = maxChargeAmps;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(Integer errorCode) {
    switch (errorCode) {
      case 0:
        this.errorCode = "NONE"; // TODO evaluate
        break;
      case 1:
        this.errorCode = "RCCB";
        break;
      case 3:
        this.errorCode = "PHASE";
        break;
      case 8:
        this.errorCode = "NO_GROUND";
        break;
      case 10:
        this.errorCode = "INTERNAL";
        break;
      default:
        this.errorCode = "NONE"; // TODO evaluate
        break;
    }
  }

  public Integer getAllowCharging() {
    return allowCharging;
  }

  public void setAllowCharging(Integer allowCharging) {
    this.allowCharging = allowCharging;
  }

  public boolean getAutomaticStop() {
    return automaticStop;
  }

  public void setAutomaticStop(Integer automaticStop) {
    this.automaticStop = automaticStop == 2;
  }

  public Integer getCableEncoding() {
    return cableEncoding;
  }

  public void setCableEncoding(Integer cableEncoding) {
    this.cableEncoding = cableEncoding;
  }

  public Integer getPhases() {
    return phases;
  }

  public void setPhases(Integer phases) {
    this.phases = phases;
  }

  public Integer getTemperature() {
    return temperature;
  }

  public void setTemperature(Integer temperature) {
    this.temperature = temperature;
  }

  public Double getSessionChargeConsumption() {
    return sessionChargeConsumption;
  }

  public void setSessionChargeConsumption(Long sessionChargeConsumption) {
    this.sessionChargeConsumption = sessionChargeConsumption/360000d;
  }

  public Double getSessionChargeConsumptionLimit() {
    return sessionChargeConsumptionLimit;
  }

  public void setSessionChargeConsumptionLimit(Integer sessionChargeConsumptionLimit) {
    this.sessionChargeConsumptionLimit = sessionChargeConsumptionLimit/10d;
  }

  public Double getTotalChargeConsumption() {
    return totalChargeConsumption;
  }

  public void setTotalChargeConsumption(Long totalChargeConsumption) {
    this.totalChargeConsumption = totalChargeConsumption/10d;
  }

  public String getFirmware() {
    return firmware;
  }

  public void setFirmware(String firmware) {
    this.firmware = firmware;
  }

  public Integer[] getEnergy() {
    return energy;
  }

  public void setEnergy(Integer[] energy) {
    this.energy = energy;

    if (Math.floor(getPhases() / 8) == 1 && this.energy[3] > this.energy[0]) {
      this.energy[0] = this.energy[3];
      this.energy[7] = this.energy[10];
      this.energy[12] = this.energy[15];
    }
  }
}