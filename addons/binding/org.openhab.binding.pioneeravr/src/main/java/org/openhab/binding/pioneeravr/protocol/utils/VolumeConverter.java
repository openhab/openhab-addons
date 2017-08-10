/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.protocol.utils;

import java.text.DecimalFormat;

/**
 *
 * @author Antoine Besnard - Initial contribution
 */
public final class VolumeConverter {
    
    private static final String[] IP_CONTROL_VOLUME_FORMAT = { "000", "00", "00", "00" };
    private static final String[] IP_CONTROL_VOLUME_DEFAULT_VALUE = { "000", "00", "00", "00" };
    
    private static final double[] MAX_IP_CONTROL_VOLUME = { 184, 80, 80, 80 };
    private static final double[] MIN_DB_VOLUME = { 80, 80, 80, 80 };
    
    /**
     * Return the double value of the volume from the value received in the IpControl response.
     *
     * @param ipControlVolume
     * @return the volume in Db
     */
    public static double convertFromIpControlVolumeToDb(String ipControlVolume, int zone) {
        double ipControlVolumeInt = Double.parseDouble(ipControlVolume);
        return ((ipControlVolumeInt - 1d) / 2d) - MIN_DB_VOLUME[zone - 1];
    }
    
    /**
     * Return the string parameter to send to the AVR based on the given volume.
     *
     * @param volumeDb
     * @return the volume for IpControlRequest
     */
    public static String convertFromDbToIpControlVolume(double volumeDb, int zone) {
        double ipControlVolume = ((MIN_DB_VOLUME[zone - 1] + volumeDb) * 2d) + 1d;
        return formatIpControlVolume(ipControlVolume, zone);
    }
    
    /**
     * Return the String parameter to send to the AVR based on the given persentage of the max volume level.
     *
     * @param volumePercent
     * @return the volume for IpControlRequest
     */
    public static String convertFromPercentToIpControlVolume(double volumePercent, int zone) {
        double ipControlVolume = 1 + (volumePercent * MAX_IP_CONTROL_VOLUME[zone - 1] / 100);
        return formatIpControlVolume(ipControlVolume, zone);
    }
    
    /**
     * Return the percentage of the max volume levelfrom the value received in the IpControl response.
     *
     * @param ipControlVolume
     * @return the volume percentage
     */
    public static double convertFromIpControlVolumeToPercent(String ipControlVolume, int zone) {
        double ipControlVolumeInt = Double.parseDouble(ipControlVolume);
        return ((ipControlVolumeInt - 1d) * 100d) / MAX_IP_CONTROL_VOLUME[zone - 1];
    }
    
    /**
     * Format the given double value to an IpControl volume.
     *
     * @param ipControlVolume
     * @return
     */
    private static String formatIpControlVolume(double ipControlVolume, int zone) {
        DecimalFormat FORMATTER = new DecimalFormat(IP_CONTROL_VOLUME_FORMAT[zone - 1]);
        String result = IP_CONTROL_VOLUME_DEFAULT_VALUE[zone - 1];
        // DecimalFormat is not ThreadSafe
        synchronized (FORMATTER) {
            result = FORMATTER.format(Math.round(ipControlVolume));
        }
        return result;
    }
    
}
