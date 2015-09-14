/*---------------------------------------------------------------------------
 * Copyright (C) 2002 Dallas Semiconductor Corporation, All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY,  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL DALLAS SEMICONDUCTOR BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * Except as contained in this notice, the name of Dallas Semiconductor
 * shall not be used except as stated in the Dallas Semiconductor
 * Branding Policy.
 *---------------------------------------------------------------------------
 */
package com.dalsemi.onewire.container;

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.OneWireIOException;

/**
 * <p>Interface class for 1-Wire&#174 devices that perform analog measuring
 * operations. This class should be implemented for each A/D
 * type 1-Wire device.</p>
 *
 * <h3> Features </h3>
 *
 * <ul>
 *    <li>
 *        Allows multi-channel voltage readings
 *    </li>
 *    <li>
 *        Supports A/D Alarm enabling on devices with A/D Alarms
 *    </li>
 *    <li>
 *        Supports selectable A/D ranges on devices with selectable ranges
 *    </li>
 *    <li>
 *        Supports selectable A/D resolutions on devices with selectable resolutions
 *    </li>
 * </ul>
 *
 * <h3> Usage </h3>
 *
 * <p><code>ADContainer</code> extends <code>OneWireSensor</code>, so the general usage
 * model applies to any <code>ADContainer</code>:
 * <ol>
 *   <li> readDevice()  </li>
 *   <li> perform operations on the <code>ADContainer</code>  </li>
 *   <li> writeDevice(byte[]) </li>
 * </ol>
 *
 * <p>Consider this interaction with an <code>ADContainer</code> that reads from all of its
 * A/D channels, then tries to set its high alarm on its first channel (channel 0):
 *
 * <pre><code>
 *     //adcontainer is a com.dalsemi.onewire.container.ADContainer
 *     byte[] state = adcontainer.readDevice();
 *     double[] voltages = new double[adcontainer.getNumberADChannels()];
 *     for (int i=0; i &lt; adcontainer.getNumberADChannels(); i++)
 *     {
 *          adcontainer.doADConvert(i, state);
 *          voltages[i] = adc.getADVoltage(i, state);
 *     }
 *     if (adcontainer.hasADAlarms())
 *     {
 *          double highalarm = adcontainer.getADAlarm(0, ADContainer.ALARM_HIGH, state);
 *          adcontainer.setADAlarm(0, ADContainer.ALARM_HIGH, highalarm + 1.0, state);
 *          adcontainer.writeDevice(state);
 *     }
 *
 * </code></pre>
 *
 * @version    1.00, 20 February 2002
 * @author     SH
 */
public interface MissionContainer
   extends ClockContainer
{
   /**
    * Indicates the high alarm.
    */
   public static final int ALARM_HIGH = 1;

   /**
    * Indicates the low alarm.
    */
   public static final int ALARM_LOW = 0;

   // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   // - Mission Start/Stop/Status
   // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

   /**
    * Begins a new mission on this missioning device.
    *
    * @param sampleRate indicates the sampling rate, in seconds, that
    *        this missioning device should log samples.
    * @param missionStartDelay indicates the amount of time, in seconds,
    *        that should pass before the mission begins.
    * @param rolloverEnabled if <code>false</code>, this device will stop
    *        recording new samples after the data log is full.  Otherwise,
    *        it will replace samples starting at the beginning.
    * @param syncClock if <code>true</code>, the real-time clock of this
    *        missioning device will be synchronized with the current time
    *        according to this <code>java.util.Date</code>.
    */
   void startNewMission(int sampleRate, int missionStartDelay,
                        boolean rolloverEnabled, boolean syncClock,
                        boolean[] channelEnabled)
      throws OneWireException, OneWireIOException;

   /**
    * Ends the currently running mission.
    */
   void stopMission()
      throws OneWireException, OneWireIOException;

   /**
    * Returns <code>true</code> if a mission is currently running.
    * @return <code>true</code> if a mission is currently running.
    */
   boolean isMissionRunning()
      throws OneWireException, OneWireIOException;

   /**
    * Returns <code>true</code> if a rollover is enabled.
    * @return <code>true</code> if a rollover is enabled.
    */
   boolean isMissionRolloverEnabled()
      throws OneWireException, OneWireIOException;

   /**
    * Returns <code>true</code> if a mission has rolled over.
    * @return <code>true</code> if a mission has rolled over.
    */
   boolean hasMissionRolloverOccurred()
      throws OneWireException, OneWireIOException;

   /**
    * Loads the results of the currently running mission.  Must be called
    * before all mission result/status methods.
    */
   void loadMissionResults()
      throws OneWireException, OneWireIOException;

   /**
    *
    */
   boolean isMissionLoaded();

   /**
    * Clears the mission results and erases the log memory from this
    * missioning device.
    */
   void clearMissionResults()
      throws OneWireException, OneWireIOException;

   /**
    * Gets the number of channels supported by this Missioning device.
    * Channel specific methods will use a channel number specified
    * by an integer from [0 to (<code>getNumberOfMissionChannels()</code> - 1)].
    *
    * @return the number of channels
    */
   int getNumberMissionChannels()
      throws OneWireException, OneWireIOException;

   /**
    * Enables/disables the specified mission channel, indicating whether or
    * not the channel's readings will be recorded in the mission log.
    *
    * @param channel the channel to enable/disable
    * @param enable if true, the channel is enabled
    */
   void setMissionChannelEnable(int channel, boolean enable)
      throws OneWireException, OneWireIOException;

   /**
    * Returns true if the specified mission channel is enabled, indicating
    * that the channel's readings will be recorded in the mission log.
    *
    * @param channel the channel to enable/disable
    * @param enable if true, the channel is enabled
    */
   boolean getMissionChannelEnable(int channel)
      throws OneWireException, OneWireIOException;

   /**
    * Returns a default friendly label for each channel supported by this
    * Missioning device.
    *
    * @param channel the mission channel, between <code>0</code> and
    *        <code>(getNumberOfMissionChannels()-1)</code>
    * @return friendly label for the specified channel
    */
   String getMissionLabel(int channel)
      throws OneWireException, OneWireIOException;

   // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   // - Mission Results
   // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

   /**
    * Returns the time, in milliseconds, that the mission began.
    *
    * @param channel the mission channel, between <code>0</code> and
    *        <code>(getNumberOfMissionChannels()-1)</code>
    * @return time, in milliseconds, that the mission began
    */
   long getMissionTimeStamp(int channel)
      throws OneWireException, OneWireIOException;

   /**
    * Returns the amount of time, in milliseconds, before the first sample
    * occurred.  If rollover disabled, or datalog didn't fill up, this
    * will be 0.
    *
    * @param channel the mission channel, between <code>0</code> and
    *        <code>(getNumberOfMissionChannels()-1)</code>
    * @return time, in milliseconds, before first sample occurred
    */
   long getFirstSampleOffset(int channel)
      throws OneWireException, OneWireIOException;

   /**
    * Returns the amount of time, in seconds, between samples taken
    * by this missioning device.
    *
    * @param channel the mission channel, between <code>0</code> and
    *        <code>(getNumberOfMissionChannels()-1)</code>
    * @return time, in seconds, between sampling
    */
   int getMissionSampleRate(int channel)
      throws OneWireException, OneWireIOException;

   /**
    * Returns the number of samples taken for the specified channel
    * during the current mission.
    *
    * @param channel the mission channel, between <code>0</code> and
    *        <code>(getNumberOfMissionChannels()-1)</code>
    * @return number of samples taken for the specified channel
    */
   int getMissionSampleCount(int channel)
      throws OneWireException, OneWireIOException;

   /**
    * Returns the total number of samples taken for the specified channel
    * during the current mission.  This number can be more than the actual
    * sample count if rollover is enabled and the log has been filled.
    *
    * @param channel the mission channel, between <code>0</code> and
    *        <code>(getNumberOfMissionChannels()-1)</code>
    * @return number of samples taken for the specified channel
    */
   public int getMissionSampleCountTotal(int channel)
      throws OneWireException, OneWireIOException;

   /**
    * Returns the value of each sample taken by the current mission.
    *
    * @param channel the mission channel, between <code>0</code> and
    *        <code>(getNumberOfMissionChannels()-1)</code>
    * @param sampleNum the sample number to return, between <code>0</code> and
    *        <code>(getMissionSampleCount(channel)-1)</code>
    * @return the value of the specified sample on the specified channel
    */
   double getMissionSample(int channel, int sampleNum)
      throws OneWireException, OneWireIOException;

   /**
    * Returns the sample as an integer value.  This value is not converted to
    * degrees Celsius for temperature or to percent RH for Humidity.  It is
    * simply the 8 or 16 bits of digital data written in the mission log for
    * this sample entry.  It is up to the user to mask off the unused bits
    * and convert this value to it's proper units.  This method is primarily
    * for users of the DS2422 who are using an input device which is not an
    * A-D or have an A-D wholly dissimilar to the one specified in the
    * datasheet.
    *
    * @param channel the mission channel, between <code>0</code> and
    *        <code>(getNumberOfMissionChannels()-1)</code>
    * @param sampleNum the sample number to return, between <code>0</code> and
    *        <code>(getMissionSampleCount(channel)-1)</code>
    * @return the sample as a whole integer
    */
   public int getMissionSampleAsInteger(int channel, int sampleNum)
      throws OneWireException, OneWireIOException;

   /**
    * Returns the time, in milliseconds, that each sample was taken by the
    * current mission.
    *
    * @param channel the mission channel, between <code>0</code> and
    *        <code>(getNumberOfMissionChannels()-1)</code>
    * @param sampleNum the sample number to return, between <code>0</code> and
    *        <code>(getMissionSampleCount(channel)-1)</code>
    * @return the sample's timestamp, in milliseconds
    */
   long getMissionSampleTimeStamp(int channel, int sampleNum)
      throws OneWireException, OneWireIOException;

   // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   // - Mission Resolution and Range
   // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

   /**
    * Returns all available resolutions for the specified mission channel.
    *
    * @param channel the mission channel, between <code>0</code> and
    *        <code>(getNumberOfMissionChannels()-1)</code>
    * @return all available resolutions for the specified mission channel.
    */
   double[] getMissionResolutions(int channel)
      throws OneWireException, OneWireIOException;

   /**
    * Returns the currently selected resolution for the specified
    * channel.
    *
    * @param channel the mission channel, between <code>0</code> and
    *        <code>(getNumberOfMissionChannels()-1)</code>
    * @return the currently selected resolution for the specified channel.
    */
   double getMissionResolution(int channel)
      throws OneWireException, OneWireIOException;

   /**
    * Sets the selected resolution for the specified channel.
    *
    * @param channel the mission channel, between <code>0</code> and
    *        <code>(getNumberOfMissionChannels()-1)</code>
    * @param resolution the new resolution for the specified channel.
    */
   void setMissionResolution(int channel, double resolution)
      throws OneWireException, OneWireIOException;

   // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   // - Mission Alarms
   // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

   /**
    * Indicates whether or not the specified channel of this missioning device
    * has mission alarm capabilities.
    *
    * @param channel the mission channel, between <code>0</code> and
    *        <code>(getNumberOfMissionChannels()-1)</code>
    * @return true if the device has mission alarms for the specified channel.
    */
   boolean hasMissionAlarms(int channel);

   /**
    * Returns true if the specified channel's alarm value of the specified
    * type has been triggered during the mission.
    *
    * @param channel the mission channel, between <code>0</code> and
    *        <code>(getNumberOfMissionChannels()-1)</code>
    * @param alarmType valid value: <code>ALARM_HIGH</code> or
    *                   <code>ALARM_LOW</code>
    * @return true if the alarm was triggered.
    */
   boolean hasMissionAlarmed(int channel, int alarmType)
      throws OneWireException, OneWireIOException;

   /**
    * Returns true if the alarm of the specified type has been enabled for
    * the specified channel.
    *
    * @param channel the mission channel, between <code>0</code> and
    *        <code>(getNumberOfMissionChannels()-1)</code>
    * @param  alarmType valid value: <code>ALARM_HIGH</code> or
    *                   <code>ALARM_LOW</code>
    * @return true if the alarm of the specified type has been enabled for
    *         the specified channel.
    */
   boolean getMissionAlarmEnable(int channel, int alarmType)
      throws OneWireException, OneWireIOException;

   /**
    * Enables/disables the alarm of the specified type for the specified channel
    *
    * @param channel the mission channel, between <code>0</code> and
    *        <code>(getNumberOfMissionChannels()-1)</code>
    * @param alarmType valid value: <code>ALARM_HIGH</code> or
    *                   <code>ALARM_LOW</code>
    * @param enable if true, alarm is enabled.
    */
   void setMissionAlarmEnable(int channel, int alarmType, boolean enable)
      throws OneWireException, OneWireIOException;

   /**
    * Returns the threshold value which will trigger the alarm of the
    * specified type on the specified channel.
    *
    * @param channel the mission channel, between <code>0</code> and
    *        <code>(getNumberOfMissionChannels()-1)</code>
    * @param alarmType valid value: <code>ALARM_HIGH</code> or
    *                   <code>ALARM_LOW</code>
    * @return the threshold value which will trigger the alarm
    */
   double getMissionAlarm(int channel, int alarmType)
      throws OneWireException, OneWireIOException;

   /**
    * Sets the threshold value which will trigger the alarm of the
    * specified type on the specified channel.
    *
    * @param channel the mission channel, between <code>0</code> and
    *        <code>(getNumberOfMissionChannels()-1)</code>
    * @param alarmType valid value: <code>ALARM_HIGH</code> or
    *                   <code>ALARM_LOW</code>
    * @param threshold the threshold value which will trigger the alarm
    */
   void setMissionAlarm(int channel, int alarmType, double threshold)
      throws OneWireException, OneWireIOException;
}