/*---------------------------------------------------------------------------
 * Copyright (C) 1999,2000 Dallas Semiconductor Corporation, All Rights Reserved.
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

// imports
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
 * @see OneWireSensor
 * @see ClockContainer
 * @see TemperatureContainer
 * @see PotentiometerContainer
 * @see SwitchContainer
 *
 * @version    0.00, 27 August 2000
 * @author     DS, KLA
 */
public interface ADContainer
   extends OneWireSensor
{

   //--------
   //-------- Static Final Variables
   //--------

   /**
    * Indicates the high AD alarm.
    */
   public static final int ALARM_HIGH = 1;

   /**
    * Indicates the low AD alarm.
    */
   public static final int ALARM_LOW = 0;

   //--------
   //-------- A/D Feature methods
   //--------

   /**
    * Gets the number of channels supported by this A/D.
    * Channel specific methods will use a channel number specified
    * by an integer from [0 to (<code>getNumberADChannels()</code> - 1)].
    *
    * @return the number of channels
    */
   public int getNumberADChannels ();

   /**
    * Checks to see if this A/D measuring device has high/low
    * alarms.
    *
    * @return true if this device has high/low trip alarms
    */
   public boolean hasADAlarms ();

   /**
    * Gets an array of available ranges for the specified
    * A/D channel.
    *
    * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
    *
    * @return array indicating the available ranges starting
    *         from the largest range to the smallest range
    *
    * @see #getNumberADChannels()
    */
   public double[] getADRanges (int channel);

   /**
    * Gets an array of available resolutions based
    * on the specified range on the specified A/D channel.
    *
    * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
    * @param range A/D range setting from the <code>getADRanges(int)</code> method
    *
    * @return array indicating the available resolutions on this 
    *         <code>channel</code> for this <code>range</code>
    *
    * @see #getNumberADChannels()
    * @see #getADRanges(int)
    */
   public double[] getADResolutions (int channel, double range);

   /**
    * Checks to see if this A/D supports doing multiple voltage
    * conversions at the same time.
    *
    * @return true if the device can do multi-channel voltage reads
    * 
    * @see #doADConvert(boolean[],byte[])
    */
   public boolean canADMultiChannelRead ();

   //--------
   //-------- A/D IO Methods
   //--------

   /**
    * Performs a voltage conversion on one specified channel.  
    * Use the method <code>getADVoltage(int,byte[])</code> to read 
    * the result of this conversion, using the same <code>channel</code>
    * argument as this method uses.
    *
    * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         no 1-Wire device present.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    *         This is usually a recoverable error.
    * @throws OneWireException on a communication or setup error with the
    *         1-Wire adapter.  This is usually a non-recoverable error.
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #getADVoltage(int,byte[])
    */
   public void doADConvert (int channel, byte[] state)
      throws OneWireIOException, OneWireException;

   /**
    * Performs voltage conversion on one or more specified
    * channels.  The method <code>getADVoltage(byte[])</code> can be used to read the result
    * of the conversion(s). This A/D must support multi-channel read,
    * reported by <code>canADMultiChannelRead()</code>, if more then 1 channel is specified.
    *
    * @param doConvert array of size <code>getNumberADChannels()</code> representing 
    *                  which channels should perform conversions
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         no 1-Wire device present.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    *         This is usually a recoverable error.
    * @throws OneWireException on a communication or setup error with the
    *         1-Wire adapter.  This is usually a non-recoverable error.
    * 
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #getADVoltage(byte[])
    * @see #canADMultiChannelRead()
    */
   public void doADConvert (boolean[] doConvert, byte[] state)
      throws OneWireIOException, OneWireException;

   /**
    * Reads the value of the voltages after a <code>doADConvert(boolean[],byte[])</code>
    * method call.  This A/D device must support multi-channel reading, reported by
    * <code>canADMultiChannelRead()</code>, if more than 1 channel conversion was attempted
    * by <code>doADConvert()</code>.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return array with the voltage values for all channels
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         no 1-Wire device present.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    *         This is usually a recoverable error.
    * @throws OneWireException on a communication or setup error with the
    *         1-Wire adapter.  This is usually a non-recoverable error.
    *
    * @see #doADConvert(boolean[],byte[])
    */
   public double[] getADVoltage (byte[] state)
      throws OneWireIOException, OneWireException;

   /**
    * Reads the value of the voltages after a <code>doADConvert(int,byte[])</code>
    * method call.  If more than one channel has been read it is more
    * efficient to use the <code>getADVoltage(byte[])</code> method that 
    * returns all channel voltage values.
    *
    * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return the voltage value for the specified channel
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         no 1-Wire device present.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    *         This is usually a recoverable error.
    * @throws OneWireException on a communication or setup error with the
    *         1-Wire adapter.  This is usually a non-recoverable error.
    *
    * @see #doADConvert(int,byte[])
    * @see #getADVoltage(byte[])
    */
   public double getADVoltage (int channel, byte[] state)
      throws OneWireIOException, OneWireException;

   //--------
   //-------- A/D 'get' Methods
   //--------

   /**
    * Reads the value of the specified A/D alarm on the specified channel.
    * Not all A/D devices have alarms.  Check to see if this device has
    * alarms first by calling the <code>hasADAlarms()</code> method.
    *
    * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
    * @param alarmType the desired alarm, <code>ALARM_HIGH</code> or <code>ALARM_LOW</code>
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return the alarm value in volts
    *
    * @throws OneWireException if this device does not have A/D alarms
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #hasADAlarms()
    */
   public double getADAlarm (int channel, int alarmType, byte[] state)
      throws OneWireException;

   /**
    * Checks to see if the specified alarm on the specified channel is enabled.
    * Not all A/D devices have alarms.  Check to see if this device has
    * alarms first by calling the <code>hasADAlarms()</code> method.
    *
    * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
    * @param alarmType the desired alarm, <code>ALARM_HIGH</code> or <code>ALARM_LOW</code>
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return true if specified alarm is enabled
    *
    * @throws OneWireException if this device does not have A/D alarms
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #hasADAlarms()
    */
   public boolean getADAlarmEnable (int channel, int alarmType, byte[] state)
      throws OneWireException;

   /**
    * Checks the state of the specified alarm on the specified channel.
    * Not all A/D devices have alarms.  Check to see if this device has
    * alarms first by calling the <code>hasADAlarms()</code> method.
    *
    * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
    * @param alarmType the desired alarm, <code>ALARM_HIGH</code> or <code>ALARM_LOW</code>
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return true if specified alarm occurred
    *
    * @throws OneWireException if this device does not have A/D alarms
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #hasADAlarms()
    * @see #getADAlarmEnable(int,int,byte[])
    * @see #setADAlarmEnable(int,int,boolean,byte[])
    */
   public boolean hasADAlarmed (int channel, int alarmType, byte[] state)
      throws OneWireException;

   /**
    * Returns the currently selected resolution for the specified
    * channel.  This device may not have selectable resolutions,
    * though this method will return a valid value.
    *
    * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return the current resolution of <code>channel</code> in volts
    *
    * @see #getADResolutions(int,double)
    * @see #setADResolution(int,double,byte[])
    */
   public double getADResolution (int channel, byte[] state);

   /**
    * Returns the currently selected range for the specified
    * channel.  This device may not have selectable ranges,
    * though this method will return a valid value.
    *
    * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return the input voltage range
    * 
    * @see #getADRanges(int)
    * @see #setADRange(int,double,byte[])
    */
   public double getADRange (int channel, byte[] state);

   //--------
   //-------- A/D 'set' Methods
   //--------

   /**
    * Sets the voltage value of the specified alarm on the specified channel.
    * The method <code>writeDevice()</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice()</code>.  Also note that
    * not all A/D devices have alarms.  Check to see if this device has
    * alarms first by calling the <code>hasADAlarms()</code> method.
    *
    * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
    * @param alarmType the desired alarm, <code>ALARM_HIGH</code> or <code>ALARM_LOW</code>
    * @param alarm new alarm value
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @throws OneWireException if this device does not have A/D alarms
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #hasADAlarms()
    * @see #getADAlarm(int,int,byte[])
    * @see #getADAlarmEnable(int,int,byte[])
    * @see #setADAlarmEnable(int,int,boolean,byte[])
    * @see #hasADAlarmed(int,int,byte[])
    */
   public void setADAlarm (int channel, int alarmType, double alarm,
                           byte[] state)
      throws OneWireException;

   /**
    * Enables or disables the specified alarm on the specified channel.
    * The method <code>writeDevice()</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice()</code>.  Also note that
    * not all A/D devices have alarms.  Check to see if this device has
    * alarms first by calling the <code>hasADAlarms()</code> method.
    *
    * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
    * @param alarmType the desired alarm, <code>ALARM_HIGH</code> or <code>ALARM_LOW</code>
    * @param alarmEnable true to enable the alarm, false to disable
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @throws OneWireException if this device does not have A/D alarms
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #hasADAlarms()
    * @see #getADAlarm(int,int,byte[])
    * @see #setADAlarm(int,int,double,byte[])
    * @see #getADAlarmEnable(int,int,byte[])
    * @see #hasADAlarmed(int,int,byte[])
    */
   public void setADAlarmEnable (int channel, int alarmType,
                                 boolean alarmEnable, byte[] state)
      throws OneWireException;

   /**
    * Sets the conversion resolution value for the specified channel.
    * The method <code>writeDevice()</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice()</code>.  Also note that
    * not all A/D devices have alarms.  Check to see if this device has
    * alarms first by calling the <code>hasADAlarms()</code> method.
    *
    * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
    * @param resolution one of the resolutions returned by <code>getADResolutions(int,double)</code>
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see #getADResolutions(int,double)
    * @see #getADResolution(int,byte[])
    *
    */
   public void setADResolution (int channel, double resolution, byte[] state);

   /**
    * Sets the input range for the specified channel.
    * The method <code>writeDevice()</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice()</code>.  Also note that
    * not all A/D devices have alarms.  Check to see if this device has
    * alarms first by calling the <code>hasADAlarms()</code> method.
    *
    * @param channel channel number in the range [0 to (<code>getNumberADChannels()</code> - 1)]
    * @param range one of the ranges returned by <code>getADRanges(int)</code>
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see #getADRanges(int)
    * @see #getADRange(int,byte[])
    */
   public void setADRange (int channel, double range, byte[] state);
}
