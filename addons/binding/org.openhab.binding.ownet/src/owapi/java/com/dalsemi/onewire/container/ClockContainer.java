
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


/**
 * <p>Interface class for 1-Wire&#174 devices that contain Real-Time clocks.
 * This class should be implemented for each Clock type 1-Wire device.</p>
 *
 * <h3> Features </h3>
 *
 * <ul>
 *    <li>
 *        Supports clock alarm enabling and setting on devices with clock alarms
 *    </li>
 *    <li>
 *        Supports enabling and disabling the clock on devices that can disable their oscillators
 *    </li>
 * </ul>
 *
 * <h3> Usage </h3>
 *
 * <p><code>ClockContainer</code> extends <code>com.dalsemi.onewire.container.OneWireSensor</code>, so the general usage
 * model applies to any <code>ClockContainer</code>:
 * <ol>
 *   <li> readDevice()  </li>
 *   <li> perform operations on the <code>ClockContainer</code>  </li>
 *   <li> writeDevice(byte[]) </li>
 * </ol>
 *
 * <p>Consider this interaction with a <code>ClockContainer</code> that reads from the
 * Real-Time clock, then tries to set it to the system's current clock setting before
 * disabling the oscillator:
 *
 * <pre><code>
 *     //clockcontainer is a com.dalsemi.onewire.container.ClockContainer
 *     byte[] state = clockcontainer.readDevice();
 *     long current_time = clockcontainer.getClock(state);
 *     System.out.println("Current time is :"+(new Date(current_time)));
 *     
 *     long system_time = System.currentTimeMillis();
 *     clockcontainer.setClock(system_time,state);
 *     clockcontainer.writeDevice(state);
 *
 *     //now try to disable to clock oscillator
 *     if (clockcontainer.canDisableClock())
 *     {
 *          state = clockcontainer.readDevice();
 *          clockcontainer.setClockRunEnable(false,state);
 *          clockcontainer.writeDevice(state);
 *     }
 *
 * </code></pre>
 *
 * @see OneWireSensor
 * @see ADContainer
 * @see TemperatureContainer
 * @see PotentiometerContainer
 * @see SwitchContainer
 *
 * @version    0.00, 28 Aug 2000
 * @author     DS, KLA
 */
public interface ClockContainer
   extends OneWireSensor
{

   //--------
   //-------- Clock Feature methods
   //--------

   /**
    * Checks to see if the clock has an alarm feature.
    *
    * @return true if the Real-Time clock has an alarm
    *
    * @see #getClockAlarm(byte[])
    * @see #isClockAlarmEnabled(byte[])
    * @see #isClockAlarming(byte[])
    * @see #setClockAlarm(long,byte[])
    * @see #setClockAlarmEnable(boolean,byte[])
    */
   public boolean hasClockAlarm ();

   /**
    * Checks to see if the clock can be disabled.
    *
    * @return true if the clock can be enabled and disabled
    *
    * @see #isClockRunning(byte[])
    * @see #setClockRunEnable(boolean,byte[])
    */
   public boolean canDisableClock ();

   /**
    * Gets the clock resolution in milliseconds.
    *
    * @return the clock resolution in milliseconds
    */
   public long getClockResolution ();

   //--------
   //-------- Clock 'get' Methods
   //--------

   /**
    * Extracts the Real-Time clock value in milliseconds.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return the time represented in this clock in milliseconds since 1970
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #setClock(long,byte[])
    */
   public long getClock (byte[] state);

   /**
    * Extracts the clock alarm value for the Real-Time clock.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return the set value of the clock alarm in milliseconds since 1970
    *
    * @throws OneWireException if this device does not have clock alarms
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #hasClockAlarm()
    * @see #isClockAlarmEnabled(byte[])
    * @see #isClockAlarming(byte[])
    * @see #setClockAlarm(long,byte[])
    * @see #setClockAlarmEnable(boolean,byte[])
    */
   public long getClockAlarm (byte[] state)
      throws OneWireException;

   /**
    * Checks if the clock alarm flag has been set.
    * This will occur when the value of the Real-Time
    * clock equals the value of the clock alarm.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return true if the Real-Time clock is alarming
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #hasClockAlarm()
    * @see #isClockAlarmEnabled(byte[])
    * @see #getClockAlarm(byte[])
    * @see #setClockAlarm(long,byte[])
    * @see #setClockAlarmEnable(boolean,byte[])
    */
   public boolean isClockAlarming (byte[] state);

   /**
    * Checks if the clock alarm is enabled.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return true if clock alarm is enabled
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #hasClockAlarm()
    * @see #isClockAlarming(byte[])
    * @see #getClockAlarm(byte[])
    * @see #setClockAlarm(long,byte[])
    * @see #setClockAlarmEnable(boolean,byte[])
    */
   public boolean isClockAlarmEnabled (byte[] state);

   /**
    * Checks if the device's oscillator is enabled.  The clock
    * will not increment if the clock oscillator is not enabled.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return true if the clock is running
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #canDisableClock()
    * @see #setClockRunEnable(boolean,byte[])
    */
   public boolean isClockRunning (byte[] state);

   //--------
   //-------- Clock 'set' Methods
   //--------

   /**
    * Sets the Real-Time clock.
    * The method <code>writeDevice()</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice()</code>.
    *
    * @param time new value for the Real-Time clock, in milliseconds 
    * since January 1, 1970
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #getClock(byte[])
    */
   public void setClock (long time, byte[] state);

   /**
    * Sets the clock alarm.
    * The method <code>writeDevice()</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice()</code>.  Also note that
    * not all clock devices have alarms.  Check to see if this device has
    * alarms first by calling the <code>hasClockAlarm()</code> method.
    *
    * @param time - new value for the Real-Time clock alarm, in milliseconds 
    * since January 1, 1970
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @throws OneWireException if this device does not have clock alarms
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #hasClockAlarm()
    * @see #isClockAlarmEnabled(byte[])
    * @see #getClockAlarm(byte[])
    * @see #isClockAlarming(byte[])
    * @see #setClockAlarmEnable(boolean,byte[])
    */
   public void setClockAlarm (long time, byte[] state)
      throws OneWireException;

   /**
    * Enables or disables the oscillator, turning the clock 'on' and 'off'.
    * The method <code>writeDevice()</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice()</code>.  Also note that
    * not all clock devices can disable their oscillators.  Check to see if this device can
    * disable its oscillator first by calling the <code>canDisableClock()</code> method.
    *
    * @param runEnable true to enable the clock oscillator
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @throws OneWireException if the clock oscillator cannot be disabled
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #canDisableClock()
    * @see #isClockRunning(byte[])
    */
   public void setClockRunEnable (boolean runEnable, byte[] state)
      throws OneWireException;

   /**
    * Enables or disables the clock alarm. 
    * The method <code>writeDevice()</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice()</code>.  Also note that
    * not all clock devices have alarms.  Check to see if this device has
    * alarms first by calling the <code>hasClockAlarm()</code> method.
    *
    * @param alarmEnable true to enable the clock alarm
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @throws OneWireException if this device does not have clock alarms
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #hasClockAlarm()
    * @see #isClockAlarmEnabled(byte[])
    * @see #getClockAlarm(byte[])
    * @see #setClockAlarm(long,byte[])
    * @see #isClockAlarming(byte[])
    */
   public void setClockAlarmEnable (boolean alarmEnable, byte[] state)
      throws OneWireException;
}
