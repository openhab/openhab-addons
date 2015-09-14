/*---------------------------------------------------------------------------
 * Copyright (C) 2001 Dallas Semiconductor Corporation, All Rights Reserved.
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
 * 1-Wire Humidity interface class for basic Humidity measuring
 * operations. This class should be implemented for each Humidity
 * type 1-Wire device.
 *
 *
 * <P>The HumidityContainer methods can be organized into the following categories: </P>
 * <UL>
 *   <LI> <B> Information </B>
 *     <UL>
 *       <LI> {@link #getHumidity                        getHumidity}
 *       <LI> {@link #getHumidityResolution              getHumidityResolution}
 *       <LI> {@link #getHumidityAlarm                   getHumidityAlarm}
 *       <LI> {@link #getHumidityAlarmResolution         getHumidityAlarmResolution}
 *       <LI> {@link #getHumidityResolution              getHumidityResolution}
 *       <LI> {@link #getHumidityResolutions             getHumidityResolutions}
 *       <LI> {@link #hasSelectableHumidityResolution    hasSelectableHumidityResolution}
 *       <LI> {@link #hasHumidityAlarms                  hasHumidityAlarms}
 *       <LI> {@link #isRelative                         isRelative}
 *     </UL>
 *   <LI> <B> Options </B>
 *     <UL>
 *       <LI> {@link #doHumidityConvert     doHumidityConvert}
 *       <LI> {@link #setHumidityAlarm      setHumidityAlarm}
 *       <LI> {@link #setHumidityResolution setHumidityResolution}
 *     </UL>
 *   <LI> <B> I/O </B>
 *     <UL>
 *       <LI> {@link #readDevice  readDevice}
 *       <LI> {@link #writeDevice writeDevice}
 *     </UL>
 *  </UL>
 *
 * <H3> Usage </H3>
 *
 * <DL>
 * <DD> <H4> Example</H4>
 * Gets humidity reading from a HumidityContainer instance '<code>hc</code>':
 * <PRE> <CODE>
 *   double lastHumidity;
 *
 *   // get the current resolution and other settings of the device (done only once)
 *   byte[] state = hc.readDevice();
 *
 *   // loop to read the humidity
 *   do 
 *   {
 *      // perform a humidity conversion
 *      hc.doHumidityConvert(state);
 *
 *      // read the result of the conversion
 *      state = hc.readDevice();
 *
 *      // extract the result out of state
 *      lastHumidity = hc.getHumidity(state);
 *      ...
 *
 *   }
 *   while (!done);
 * </CODE> </PRE>
 *
 * @see com.dalsemi.onewire.container.OneWireContainer28
 *
 * @version    0.00, 27 August 2001
 * @author     DS
 */
public interface HumidityContainer
                 extends OneWireSensor
{
   //--------
   //-------- Static Final Variables
   //--------

   /** high temperature alarm */
   public static final int ALARM_HIGH = 1;

   /** low temperature alarm */
   public static final int ALARM_LOW = 0;

   //--------
   //-------- Humidity Feature methods
   //--------

   /**
    * Checks to see if humidity value given is a 'relative' humidity value.
    *
    * @return <code>true</code> if this <code>HumidityContainer</code>
    *         provides a relative humidity reading
    *
    * @see    #getHumidityResolution
    * @see    #getHumidityResolutions
    * @see    #setHumidityResolution
    */
   public boolean isRelative();

   /**
    * Checks to see if this Humidity measuring device has high/low
    * trip alarms.
    *
    * @return <code>true</code> if this <code>HumidityContainer</code>
    *         has high/low trip alarms
    *
    * @see    #getHumidityAlarm
    * @see    #setHumidityAlarm
    */
   public boolean hasHumidityAlarms ();

   /**
    * Checks to see if this device has selectable Humidity resolution.
    *
    * @return <code>true</code> if this <code>HumidityContainer</code>
    *         has selectable Humidity resolution
    *
    * @see    #getHumidityResolution
    * @see    #getHumidityResolutions
    * @see    #setHumidityResolution
    */
   public boolean hasSelectableHumidityResolution ();

   /**
    * Get an array of available Humidity resolutions in percent humidity (0 to 100).
    *
    * @return byte array of available Humidity resolutions in percent with
    *         minimum resolution as the first element and maximum resolution
    *         as the last element.
    *
    * @see    #hasSelectableHumidityResolution
    * @see    #getHumidityResolution
    * @see    #setHumidityResolution
    */
   public double[] getHumidityResolutions ();

   /**
    * Gets the Humidity alarm resolution in percent.
    *
    * @return Humidity alarm resolution in percent for this 1-wire device
    *
    * @throws OneWireException         Device does not support Humidity
    *                                  alarms
    *
    * @see    #hasHumidityAlarms
    * @see    #getHumidityAlarm
    * @see    #setHumidityAlarm
    *
    */
   public double getHumidityAlarmResolution ()
      throws OneWireException;

   //--------
   //-------- Humidity I/O Methods
   //--------

   /**
    * Performs a Humidity conversion.
    *
    * @param  state byte array with device state information
    *
    * @throws OneWireIOException on a 1-Wire communication error such as 
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to 
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire 
    *         adapter
    */
   public void doHumidityConvert (byte[] state)
      throws OneWireIOException, OneWireException;

   //--------
   //-------- Humidity 'get' Methods
   //--------

   /**
    * Gets the humidity expressed as a percent value (0.0 to 100.0) of humidity.
    *
    * @param  state byte array with device state information
    * @return humidity expressed as a percent
    *
    * @see    #hasSelectableHumidityResolution
    * @see    #getHumidityResolution
    * @see    #setHumidityResolution
    */
   public double getHumidity (byte[] state);

   /**
    * Gets the current Humidity resolution in percent from the
    * <code>state</code> data retrieved from the <code>readDevice()</code>
    * method.
    *
    * @param  state byte array with device state information
    *
    * @return Humidity resolution in percent for this 1-wire device
    *
    * @see    #hasSelectableHumidityResolution
    * @see    #getHumidityResolutions
    * @see    #setHumidityResolution
    */
   public double getHumidityResolution (byte[] state);

   /**
    * Gets the specified Humidity alarm value in percent from the
    * <code>state</code> data retrieved from the
    * <code>readDevice()</code> method.
    *
    * @param  alarmType valid value: <code>ALARM_HIGH</code> or
    *                   <code>ALARM_LOW</code>
    * @param  state     byte array with device state information
    *
    * @return Humidity alarm trip values in percent for this 1-wire device
    *
    * @throws OneWireException         Device does not support Humidity
    *                                  alarms
    *
    * @see    #hasHumidityAlarms
    * @see    #setHumidityAlarm
    */
   public double getHumidityAlarm (int alarmType, byte[] state)
      throws OneWireException;

   //--------
   //-------- Humidity 'set' Methods
   //--------

   /**
    * Sets the Humidity alarm value in percent in the provided
    * <code>state</code> data.
    * Use the method <code>writeDevice()</code> with
    * this data to finalize the change to the device.
    *
    * @param  alarmType  valid value: <code>ALARM_HIGH</code> or
    *                    <code>ALARM_LOW</code>
    * @param  alarmValue alarm trip value in percent
    * @param  state      byte array with device state information
    *
    * @throws OneWireException         Device does not support Humidity
    *                                  alarms
    *
    * @see    #hasHumidityAlarms
    * @see    #getHumidityAlarm
    */
   public void setHumidityAlarm (int alarmType, double alarmValue,
                                    byte[] state)
      throws OneWireException;

   /**
    * Sets the current Humidity resolution in percent in the provided
    * <code>state</code> data.   Use the method <code>writeDevice()</code>
    * with this data to finalize the change to the device.
    *
    * @param  resolution Humidity resolution in percent
    * @param  state      byte array with device state information
    *
    * @throws OneWireException         Device does not support selectable
    *                                  Humidity resolution
    *
    * @see    #hasSelectableHumidityResolution
    * @see    #getHumidityResolution
    * @see    #getHumidityResolutions
    */
   public void setHumidityResolution (double resolution, byte[] state)
      throws OneWireException;

}
