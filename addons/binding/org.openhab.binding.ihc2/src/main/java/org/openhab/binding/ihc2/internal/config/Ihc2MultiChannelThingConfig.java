/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.internal.config;

/**
 * The {@link Ihc2MultiChannelThingConfig} holds the information of a multi channel thing.
 *
 *
 * @author Niels Peter Enemark - Initial contribution
 */
public class Ihc2MultiChannelThingConfig {

    // private String name;
    private int numberOfChannels;

    private String channel1ResourceId;
    private String channel1ResourceType;
    private boolean channel1Readonly;
    private int channel1PulseTime;

    private String channel2ResourceId;
    private String channel2ResourceType;
    private boolean channel2Readonly;
    private int channel2PulseTime;

    private String channel3ResourceId;
    private String channel3ResourceType;
    private boolean channel3Readonly;
    private int channel3PulseTime;

    private String channel4ResourceId;
    private String channel4ResourceType;
    private boolean channel4Readonly;
    private int channel4PulseTime;

    private String channel5ResourceId;
    private String channel5ResourceType;
    private boolean channel5Readonly;
    private int channel5PulseTime;

    private String channel6ResourceId;
    private String channel6ResourceType;
    private boolean channel6Readonly;
    private int channel6PulseTime;

    // public String getName() {
    // return name;
    // }
    //
    // public void setName(String name) {
    // this.name = name;
    // }

    public int getNumberOfChannels() {
        return numberOfChannels;
    }

    public void setNumberOfChannels(int numberOfChannels) {
        this.numberOfChannels = numberOfChannels;
    }

    public int getChannel1ResourceId() {
        return Integer.decode(channel1ResourceId);
    }

    public void setChannel1ResourceId(String channel1ResourceId) {
        this.channel1ResourceId = channel1ResourceId;
    }

    public String getChannel1ResourceType() {
        return channel1ResourceType;
    }

    public void setChannel1ResourceType(String channel1ResourceType) {
        this.channel1ResourceType = channel1ResourceType;
    }

    public boolean isChannel1Readonly() {
        return channel1Readonly;
    }

    public void setChannel1Readonly(boolean channel1Readonly) {
        this.channel1Readonly = channel1Readonly;
    }

    public int getChannel1PulseTime() {
        return channel1PulseTime;
    }

    public void setChannel1PulseTime(int channel1PulseTime) {
        this.channel1PulseTime = channel1PulseTime;
    }

    public int getChannel2ResourceId() {
        return Integer.decode(channel2ResourceId);
    }

    public void setChannel2ResourceId(String channel2ResourceId) {
        this.channel2ResourceId = channel2ResourceId;
    }

    public String getChannel2ResourceType() {
        return channel2ResourceType;
    }

    public void setChannel2ResourceType(String channel2ResourceType) {
        this.channel2ResourceType = channel2ResourceType;
    }

    public boolean isChannel2Readonly() {
        return channel2Readonly;
    }

    public void setChannel2Readonly(boolean channel2Readonly) {
        this.channel2Readonly = channel2Readonly;
    }

    public int getChannel2PulseTime() {
        return channel2PulseTime;
    }

    public void setChannel2PulseTime(int channel2PulseTime) {
        this.channel2PulseTime = channel2PulseTime;
    }

    public int getChannel3ResourceId() {
        return Integer.decode(channel3ResourceId);
    }

    public void setChannel3ResourceId(String channel3ResourceId) {
        this.channel3ResourceId = channel3ResourceId;
    }

    public String getChannel3ResourceType() {
        return channel3ResourceType;
    }

    public void setChannel3ResourceType(String channel3ResourceType) {
        this.channel3ResourceType = channel3ResourceType;
    }

    public boolean isChannel3Readonly() {
        return channel3Readonly;
    }

    public void setChannel3Readonly(boolean channel3Readonly) {
        this.channel3Readonly = channel3Readonly;
    }

    public int getChannel3PulseTime() {
        return channel3PulseTime;
    }

    public void setChannel3PulseTime(int channel3PulseTime) {
        this.channel3PulseTime = channel3PulseTime;
    }

    public int getChannel4ResourceId() {
        return Integer.decode(channel4ResourceId);
    }

    public void setChannel4ResourceId(String channel4ResourceId) {
        this.channel4ResourceId = channel4ResourceId;
    }

    public String getChannel4ResourceType() {
        return channel4ResourceType;
    }

    public void setChannel4ResourceType(String channel4ResourceType) {
        this.channel4ResourceType = channel4ResourceType;
    }

    public boolean isChannel4Readonly() {
        return channel4Readonly;
    }

    public void setChannel4Readonly(boolean channel4Readonly) {
        this.channel4Readonly = channel4Readonly;
    }

    public int getChannel4PulseTime() {
        return channel4PulseTime;
    }

    public void setChannel4PulseTime(int channel4PulseTime) {
        this.channel4PulseTime = channel4PulseTime;
    }

    public int getChannel5ResourceId() {
        return Integer.decode(channel5ResourceId);
    }

    public void setChannel5ResourceId(String channel5ResourceId) {
        this.channel5ResourceId = channel5ResourceId;
    }

    public String getChannel5ResourceType() {
        return channel5ResourceType;
    }

    public void setChannel5ResourceType(String channel5ResourceType) {
        this.channel5ResourceType = channel5ResourceType;
    }

    public boolean isChannel5Readonly() {
        return channel5Readonly;
    }

    public void setChannel5Readonly(boolean channel5Readonly) {
        this.channel5Readonly = channel5Readonly;
    }

    public int getChannel5PulseTime() {
        return channel5PulseTime;
    }

    public void setChannel5PulseTime(int channel5PulseTime) {
        this.channel5PulseTime = channel5PulseTime;
    }

    public int getChannel6ResourceId() {
        return Integer.decode(channel6ResourceId);
    }

    public void setChannel6ResourceId(String channel6ResourceId) {
        this.channel6ResourceId = channel6ResourceId;
    }

    public String getChannel6ResourceType() {
        return channel6ResourceType;
    }

    public void setChannel6ResourceType(String channel6ResourceType) {
        this.channel6ResourceType = channel6ResourceType;
    }

    public boolean isChannel6Readonly() {
        return channel6Readonly;
    }

    public void setChannel6Readonly(boolean channel6Readonly) {
        this.channel6Readonly = channel6Readonly;
    }

    public int getChannel6PulseTime() {
        return channel6PulseTime;
    }

    public void setChannel6PulseTime(int channel6PulseTime) {
        this.channel6PulseTime = channel6PulseTime;
    }
}
