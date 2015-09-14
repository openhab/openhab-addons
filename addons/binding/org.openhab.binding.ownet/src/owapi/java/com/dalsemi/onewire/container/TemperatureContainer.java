
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
 * 1-Wire temperature interface class for basic temperature measuring
 * operations. This class should be implemented for each temperature
 * type 1-Wire device.
 *
 *
 * <P>The TemperatureContainer methods can be organized into the following categories: </P>
 * <UL>
 *   <LI> <B> Information </B>
 *     <UL>
 *       <LI> {@link #getMaxTemperature                  getMaxTemperature}
 *       <LI> {@link #getMinTemperature                  getMinTemperature}
 *       <LI> {@link #getTemperature                     getTemperature}
 *       <LI> {@link #getTemperatureAlarm                getTemperatureAlarm}
 *       <LI> {@link #getTemperatureAlarmResolution      getTemperatureAlarmResolution}
 *       <LI> {@link #getTemperatureResolution           getTemperatureResolution}
 *       <LI> {@link #getTemperatureResolutions          getTemperatureResolutions}
 *       <LI> {@link #hasSelectableTemperatureResolution hasSelectableTemperatureResolution}
 *       <LI> {@link #hasTemperatureAlarms               hasTemperatureAlarms}
 *     </UL>
 *   <LI> <B> Options </B>
 *     <UL>
 *       <LI> {@link #doTemperatureConvert     doTemperatureConvert}
 *       <LI> {@link #setTemperatureAlarm      setTemperatureAlarm}
 *       <LI> {@link #setTemperatureResolution setTemperatureResolution}
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
 * <DD> <H4> Example 1</H4>
 * Display some features of TemperatureContainer instance '<code>tc</code>':
 * <PRE> <CODE>
 *   // Read High and Low Alarms
 *   if (!tc.hasTemperatureAlarms())
 *      System.out.println("Temperature alarms not supported");
 *   else
 *   {
 *      byte[] state     = tc.readDevice();
 *      double alarmLow  = tc.getTemperatureAlarm(TemperatureContainer.ALARM_LOW, state);
 *      double alarmHigh = tc.getTemperatureAlarm(TemperatureContainer.ALARM_HIGH, state);
 *      System.out.println("Alarm: High = " + alarmHigh + ", Low = " + alarmLow);
 *   }             }
 * </CODE> </PRE>
 *
 * <DD> <H4> Example 2</H4>
 * Gets temperature reading from a TemperatureContainer instance '<code>tc</code>':
 * <PRE> <CODE>
 *   double lastTemperature;
 *
 *   // get the current resolution and other settings of the device (done only once)
 *   byte[] state = tc.readDevice();
 *
 *   do // loop to read the temp
 *   {
 *      // perform a temperature conversion
 *      tc.doTemperatureConvert(state);
 *
 *      // read the result of the conversion
 *      state = tc.readDevice();
 *
 *      // extract the result out of state
 *      lastTemperature = tc.getTemperature(state);
 *      ...
 *
 *   }while (!done);
 * </CODE> </PRE>
 *
 * The reason the conversion and the reading are separated
 * is that one may want to do a conversion without reading
 * the result.  One could take advantage of the alarm features
 * of a device by setting a threshold and doing conversions
 * until the device is alarming.  For example:
 * <PRE> <CODE>
 *   // get the current resolution of the device
 *   byte [] state = tc.readDevice();
 *
 *   // set the trips
 *   tc.setTemperatureAlarm(TemperatureContainer.ALARM_HIGH, 50, state);
 *   tc.setTemperatureAlarm(TemperatureContainer.ALARM_LOW, 20, state);
 *   tc.writeDevice(state);
 *
 *   do // loop on conversions until an alarm occurs
 *   {
 *      tc.doTemperatureConvert(state);
 *   } while (!tc.isAlarming());
 *   </CODE> </PRE>
 *
 * <DD> <H4> Example 3</H4>
 * Sets the temperature resolution of a TemperatureContainer instance '<code>tc</code>':
 * <PRE> <CODE>
 *   byte[] state = tc.readDevice();
 *   if (tc.hasSelectableTemperatureResolution())
 *   {
 *      double[] resolution = tc.getTemperatureResolutions();
 *      tc.setTemperatureResolution(resolution [resolution.length - 1], state);
 *      tc.writeDevice(state);
 *   }
 * </CODE> </PRE>
 * </DL>
 *
 * @see com.dalsemi.onewire.container.OneWireContainer10
 * @see com.dalsemi.onewire.container.OneWireContainer21
 * @see com.dalsemi.onewire.container.OneWireContainer26
 * @see com.dalsemi.onewire.container.OneWireContainer28
 * @see com.dalsemi.onewire.container.OneWireContainer30
 *
 * @version    0.00, 27 August 2000
 * @author     DS
 */
public interface TemperatureContainer
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
   //-------- Temperature Feature methods
   //--------

   /**
    * Checks to see if this temperature measuring device has high/low
    * trip alarms.
    *
    * @return <code>true</code> if this <code>TemperatureContainer</code>
    *         has high/low trip alarms
    *
    * @see    #getTemperatureAlarm
    * @see    #setTemperatureAlarm
    */
   public boolean hasTemperatureAlarms ();

   /**
    * Checks to see if this device has selectable temperature resolution.
    *
    * @return <code>true</code> if this <code>TemperatureContainer</code>
    *         has selectable temperature resolution
    *
    * @see    #getTemperatureResolution
    * @see    #getTemperatureResolutions
    * @see    #setTemperatureResolution
    */
   public boolean hasSelectableTemperatureResolution ();

   /**
    * Get an array of available temperature resolutions in Celsius.
    *
    * @return byte array of available temperature resolutions in Celsius with
    *         minimum resolution as the first element and maximum resolution
    *         as the last element.
    *
    * @see    #hasSelectableTemperatureResolution
    * @see    #getTemperatureResolution
    * @see    #setTemperatureResolution
    */
   public double[] getTemperatureResolutions ();

   /**
    * Gets the temperature alarm resolution in Celsius.
    *
    * @return temperature alarm resolution in Celsius for this 1-wire device
    *
    * @throws OneWireException         Device does not support temperature
    *                                  alarms
    *
    * @see    #hasTemperatureAlarms
    * @see    #getTemperatureAlarm
    * @see    #setTemperatureAlarm
    *
    */
   public double getTemperatureAlarmResolution ()
      throws OneWireException;

   /**
    * Gets the maximum temperature in Celsius.
    *
    * @return maximum temperature in Celsius for this 1-wire device
    */
   public double getMaxTemperature ();

   /**
    * Gets the minimum temperature in Celsius.
    *
    * @return minimum temperature in Celsius for this 1-wire device
    */
   public double getMinTemperature ();

   //--------
   //-------- Temperature I/O Methods
   //--------

   /**
    * Performs a temperature conversion.
    *
    * @param  state byte array with device state information
    *
    * @throws OneWireException         Part could not be found [ fatal ]
    * @throws OneWireIOException       Data wasn't transferred properly [ recoverable ]
    */
   public void doTemperatureConvert (byte[] state)
      throws OneWireIOException, OneWireException;

   //--------
   //-------- Temperature 'get' Methods
   //--------

   /**
    * Gets the temperature value in Celsius from the <code>state</code>
    * data retrieved from the <code>readDevice()</code> method.
    *
    * @param  state byte array with device state information
    *
    * @return temperature in Celsius from the last
    *                     <code>doTemperatureConvert()</code>
    *
    * @throws OneWireIOException In the case of invalid temperature data
    */
   public double getTemperature (byte[] state)
      throws OneWireIOException;

   /**
    * Gets the specified temperature alarm value in Celsius from the
    * <code>state</code> data retrieved from the
    * <code>readDevice()</code> method.
    *
    * @param  alarmType valid value: <code>ALARM_HIGH</code> or
    *                   <code>ALARM_LOW</code>
    * @param  state     byte array with device state information
    *
    * @return temperature alarm trip values in Celsius for this 1-wire device
    *
    * @throws OneWireException         Device does not support temperature
    *                                  alarms
    *
    * @see    #hasTemperatureAlarms
    * @see    #setTemperatureAlarm
    */
   public double getTemperatureAlarm (int alarmType, byte[] state)
      throws OneWireException;

   /**
    * Gets the current temperature resolution in Celsius from the
    * <code>state</code> data retrieved from the <code>readDevice()</code>
    * method.
    *
    * @param  state byte array with device state information
    *
    * @return temperature resolution in Celsius for this 1-wire device
    *
    * @see    #hasSelectableTemperatureResolution
    * @see    #getTemperatureResolutions
    * @see    #setTemperatureResolution
    */
   public double getTemperatureResolution (byte[] state);

   //--------
   //-------- Temperature 'set' Methods
   //--------

   /**
    * Sets the temperature alarm value in Celsius in the provided
    * <code>state</code> data.
    * Use the method <code>writeDevice()</code> with
    * this data to finalize the change to the device.
    *
    * @param  alarmType  valid value: <code>ALARM_HIGH</code> or
    *                    <code>ALARM_LOW</code>
    * @param  alarmValue alarm trip value in Celsius
    * @param  state      byte array with device state information
    *
    * @throws OneWireException         Device does not support temperature
    *                                  alarms
    *
    * @see    #hasTemperatureAlarms
    * @see    #getTemperatureAlarm
    */
   public void setTemperatureAlarm (int alarmType, double alarmValue,
                                    byte[] state)
      throws OneWireException;

   /**
    * Sets the current temperature resolution in Celsius in the provided
    * <code>state</code> data.   Use the method <code>writeDevice()</code>
    * with this data to finalize the change to the device.
    *
    * @param  resolution temperature resolution in Celsius
    * @param  state      byte array with device state information
    *
    * @throws OneWireException         Device does not support selectable
    *                                  temperature resolution
    *
    * @see    #hasSelectableTemperatureResolution
    * @see    #getTemperatureResolution
    * @see    #getTemperatureResolutions
    */
   public void setTemperatureResolution (double resolution, byte[] state)
      throws OneWireException;
}
