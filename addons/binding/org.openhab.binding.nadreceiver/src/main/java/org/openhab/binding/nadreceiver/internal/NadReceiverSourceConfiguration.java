/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nadreceiver.internal;

/**
 * The {@link NadReceiverSourceConfiguration} is responsible for collecting configuration values of NAD receiver sources
 *
 * @author Marc Chételat - Initial contribution
 */
public class NadReceiverSourceConfiguration {
    private String number = null;
    private String analogAudioFormat = null; // =Off
    private Integer analogAudioInput = null; // =1
    private Integer analogGain = null; // (=0
    private String digitalAudioFormat = null; // =Optical
    private Integer digitalAudioInput = null; // =1
    private Boolean enabled = null; // =Yes
    private String name; // =SwisscomTV
    private Integer preset = null; // =0
    private Integer triggerOut = null; // =0
    private String videoFormat = null; // =HDMI
    private Integer videoInput = null; // =1

    public NadReceiverSourceConfiguration(String number, String name) {
        this.setNumber(number);
        this.name = name;
    }

    public NadReceiverSourceConfiguration(String number, Boolean enabled) {
        this.setNumber(number);
        this.enabled = enabled;
    }

    public String getAnalogAudioFormat() {
        return analogAudioFormat;
    }

    public void setAnalogAudioFormat(String analogAudioFormat) {
        this.analogAudioFormat = analogAudioFormat;
    }

    public Integer getAnalogAudioInput() {
        return analogAudioInput;
    }

    public void setAnalogAudioInput(Integer analogAudioInput) {
        this.analogAudioInput = analogAudioInput;
    }

    public Integer getAnalogGain() {
        return analogGain;
    }

    public void setAnalogGain(Integer analogGain) {
        this.analogGain = analogGain;
    }

    public String getDigitalAudioFormat() {
        return digitalAudioFormat;
    }

    public void setDigitalAudioFormat(String digitalAudioFormat) {
        this.digitalAudioFormat = digitalAudioFormat;
    }

    public Integer getDigitalAudioInput() {
        return digitalAudioInput;
    }

    public void setDigitalAudioInput(Integer digitalAudioInput) {
        this.digitalAudioInput = digitalAudioInput;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPreset() {
        return preset;
    }

    public void setPreset(Integer preset) {
        this.preset = preset;
    }

    public Integer getTriggerOut() {
        return triggerOut;
    }

    public void setTriggerOut(Integer triggerOut) {
        this.triggerOut = triggerOut;
    }

    public String getVideoFormat() {
        return videoFormat;
    }

    public void setVideoFormat(String videoFormat) {
        this.videoFormat = videoFormat;
    }

    public Integer getVideoInput() {
        return videoInput;
    }

    public void setVideoInput(Integer videoInput) {
        this.videoInput = videoInput;
    }

    public boolean isComplete() {
        if (name != null && enabled != null) {
            return true;
        } else {
            return false;
        }
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

}
