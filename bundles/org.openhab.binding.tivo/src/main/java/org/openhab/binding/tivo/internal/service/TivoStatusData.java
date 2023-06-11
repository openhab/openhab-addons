/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.tivo.internal.service;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * TivoStatusData class stores the data from the last status query from the TiVo and any other errors / status
 * codes.
 *
 * @author Jayson Kubilis (DigitalBytes) - Initial contribution
 * @author Andrew Black (AndyXMB) - minor updates, removal of unused functions.
 * @author Michael Lobstein - Updated for OH3
 */

@NonNullByDefault
public class TivoStatusData {
    private boolean cmdOk = false;
    private Date time = new Date();
    private int channelNum = -1;
    private int subChannelNum = -1;
    private boolean isRecording = false;
    private String msg = "NO STATUS QUERIED YET";
    private boolean pubToUI = true;
    private ConnectionStatus connectionStatus = ConnectionStatus.INIT;

    public TivoStatusData() {
    }

    /*
     * {@link TivoStatusData} class stores the data from the last status query from the TiVo and any other errors /
     * status codes.
     *
     * @param cmdOk boolean true = last command executed correctly, false = last command failed with error message
     * 
     * @param channelNum int = channel number, -1 indicates no channel received. Valid channel range 1-9999.
     * 
     * @param subChannelNum int = sub-channel number, -1 indicates no sub-channel received. Valid sub-channel range
     * 1-9999.
     * 
     * @param isRecording boolean true = indicates the current channel is recording
     * 
     * @param msg string status message from the TiVo socket
     * 
     * @param pubToUI boolean true = this status needs to be published to the UI / Thing, false = do not publish (or it
     * already has been)
     * 
     * @param connectionStatus ConnectionStatus enum UNKNOWN= test not run/default, OFFLINE = offline, STANDBY = TiVo is
     * in standby, ONLINE = Online
     *
     */
    public TivoStatusData(boolean cmdOk, int channelNum, int subChannelNum, boolean isRecording, String msg,
            boolean pubToUI, ConnectionStatus connectionStatus) {
        this.cmdOk = cmdOk;
        this.time = new Date();
        this.channelNum = channelNum;
        this.subChannelNum = subChannelNum;
        this.isRecording = isRecording;
        this.msg = msg;
        this.pubToUI = pubToUI;
        this.connectionStatus = connectionStatus;
    }

    public enum ConnectionStatus {
        INIT,
        UNKNOWN,
        OFFLINE,
        STANDBY,
        ONLINE;
    }

    /**
     * {@link TivoStatusData} class stores the data from the last status query from the TiVo and any other errors /
     * status codes.
     *
     * @param cmdOk boolean true = last command executed correctly, false = last command failed with error message
     * @param channelNum int = channel number, -1 indicates no channel received. Valid channel range 1-9999.
     * @param msg string status message from the TiVo socket
     * @param pubToUI boolean true = this status needs to be published to the UI, false = do not publish (or it
     *            already has been)
     * @param connectionStatus enum UNKNOWN= test not run/default, OFFLINE = offline, STANDBY = TiVo is in standby
     *            , ONLINE = Online
     */
    @Override
    public String toString() {
        return "TivoStatusData [cmdOk=" + cmdOk + ", time=" + time + ", channelNum=" + channelNum + ", subChannelNum="
                + subChannelNum + ", msg=" + msg + ", pubToUI=" + pubToUI + ", connectionStatus=" + connectionStatus
                + "]";
    }

    /**
     * {@link isCmdOK} indicates if the last command executed correctly.
     *
     * @return cmdOk boolean true = executed correctly, false = last command failed with error message
     */
    public boolean isCmdOk() {
        return cmdOk;
    }

    /**
     * {@link} sets the value indicating if the last command executed correctly.
     *
     * @param cmdOk boolean true = executed correctly, false = last command failed with error message
     */
    public void setCmdOk(boolean cmdOk) {
        this.cmdOk = cmdOk;
    }

    /**
     * {@link getChannelNum} gets the channel number, -1 indicates no channel received. Valid channel range 1-9999.
     *
     * @return the channel number
     */
    public int getChannelNum() {
        return channelNum;
    }

    /**
     * {@link setChannelNum} sets the channel number, -1 indicates no channel received. Valid channel range 1-9999.
     *
     * @param channelNum the new channel number
     */
    public void setChannelNum(int channelNum) {
        this.channelNum = channelNum;
    }

    /**
     * {@link getSubChannelNum} gets the sub channel number, -1 indicates no sub channel received. Valid channel range
     * 1-9999.
     *
     * @return the sub channel number
     */
    public int getSubChannelNum() {
        return subChannelNum;
    }

    /**
     * {@link setSubChannelNum} sets the sub channel number, -1 indicates no sub channel received. Valid channel range
     * 1-9999.
     *
     * @param subChannelNum the new sub channel number
     */
    public void setSubChannelNum(int subChannelNum) {
        this.subChannelNum = subChannelNum;
    }

    /**
     * {@link setRecording} set to true if current channel is recording
     *
     * @param isRecording true = current channel is recording
     */
    public void setRecording(boolean isRecording) {
        this.isRecording = isRecording;
    }

    /**
     * {@link getPubToUI} get status indicating if current channel is recording
     *
     * @return isRecording true = current channel is recording
     */
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * {@link getMsg} gets status message string
     *
     * @return msg string
     */
    public String getMsg() {
        return msg;
    }

    /**
     * {@link setPubToUI} set to true if this status needs to be published to the channel / UI / Thing, false = do not
     * publish (or it already has been).
     *
     * @param pubToUI true = publish status to the channel objects
     */
    public void setPubToUI(boolean pubToUI) {
        this.pubToUI = pubToUI;
    }

    /**
     * {@link getPubToUI} get status indicating that the event needs to be published to the channel / UI / Thing, false
     * = do not publish (or it already has been).
     *
     * @return pubToUI true = publish status to the channel objects
     */
    public boolean getPubToUI() {
        return pubToUI;
    }

    /**
     * {@link setConnectionStatus} indicates the state of the connection / connection tests. Drives online/offline state
     * of the
     * Thing and connection process.
     *
     * @param connectionStatus enum UNKNOWN= test not run/default, OFFLINE = offline, STANDBY = TiVo is in standby,
     *            ONLINE = Online
     */
    public void setConnectionStatus(ConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    /**
     * {@link getConnectionStatus} returns the state of the connection / connection tests. Drives online/offline state
     * of the
     * Thing and connection process.
     *
     * @return ConnectionStatus enum UNKNOWN= test not run/default, OFFLINE = offline, STANDBY = TiVo is in standby,
     *         ONLINE = Online
     */
    public ConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }
}
