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

  @SerializedName("ast")
  private Integer accessState;

  @SerializedName("amp")
  private Integer maxChargeAmps;

  @SerializedName("nrg")
  private Integer[] energy;

  @SerializedName("err")
  private Integer errorCode;

  @SerializedName("alw")
  private Integer allowCharging;

  @SerializedName("cbl")
  private Integer cableEncoding;

  @SerializedName("pha")
  private Integer phases;

  @SerializedName("tmp")
  private Integer temperature;

  @SerializedName("dws")
  private Long sessionChargeConsumption;

  @SerializedName("dwo")
  private Integer sessionChargeConsumptionLimit;

  @SerializedName("eto")
  private Long totalChargeConsumption;

  @SerializedName("fwv")
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

  public Integer getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(Integer errorCode) {
    this.errorCode = errorCode;
  }

  public Integer getAllowCharging() {
    return allowCharging;
  }

  public void setAllowCharging(Integer allowCharging) {
    this.allowCharging = allowCharging;
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

  public Long getSessionChargeConsumption() {
    return sessionChargeConsumption;
  }

  public void setSessionChargeConsumption(Long sessionChargeConsumption) {
    this.sessionChargeConsumption = sessionChargeConsumption;
  }

  public Integer getSessionChargeConsumptionLimit() {
    return sessionChargeConsumptionLimit;
  }

  public void setSessionChargeConsumptionLimit(Integer sessionChargeConsumptionLimit) {
    this.sessionChargeConsumptionLimit = sessionChargeConsumptionLimit;
  }

  public Long getTotalChargeConsumption() {
    return totalChargeConsumption;
  }

  public void setTotalChargeConsumption(Long totalChargeConsumption) {
    this.totalChargeConsumption = totalChargeConsumption;
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

  public Integer getAccessState() {
    return accessState;
  }

  public void setAccessState(Integer accessState) {
    this.accessState = accessState;
  }
}