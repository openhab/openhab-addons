
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
 * <p>Interface class for 1-Wire&#174 switch devices. This class
 * should be implemented for each switch type 1-Wire device.</p>
 *
 * <h3> Features </h3>
 *
 * <ul>
 *    <li>
 *        Supports activity sensing and clearing on devices with activity sensing
 *    </li>
 *    <li>
 *        Supports level sensing on devices with level sensing
 *    </li>
 *    <li>
 *        Supports switches with 'smart on' capabilities for 1-Wire branch searches
 *    </li>
 * </ul>
 *
 * <h3> Usage </h3>
 *
 * <p><code>SwitchContainer</code> extends <code>OneWireSensor</code>, so the general usage
 * model applies to any <code>SwitchContainer</code>:
 * <ol>
 *   <li> readDevice()  </li>
 *   <li> perform operations on the <code>SwitchContainer</code>  </li>
 *   <li> writeDevice(byte[]) </li>
 * </ol>
 *
 * <p>Consider this interaction with a <code>SwitchContainer</code> that toggles
 * all of the switches on the device:
 *
 * <pre><code>
 *     //switchcontainer is a com.dalsemi.onewire.container.SwitchContainer
 *     byte[] state = switchcontainer.readDevice();
 *     int number_of_switches = switchcontainer.getNumberChannels(state);
 *     System.out.println("This device has "+number_of_switches+" switches");
 *     for (int i=0; i &lt; number_of_switches; i++)
 *     {
 *          boolean switch_state = switchcontainer.getLatchState(i, state);
 *          System.out.println("Switch "+i+" is "+(switch_state ? "on" : "off"));
 *          switchcontainer.setLatchState(i,!switch_state,false,state);
 *     }
 *     switchcontainer.writeDevice(state);
 *
 * </code></pre>
 *
 * @see OneWireSensor
 * @see ClockContainer
 * @see TemperatureContainer
 * @see PotentiometerContainer
 * @see ADContainer
 *
 * @version    0.00, 27 August 2000
 * @author     DS, KLA
 */
public interface SwitchContainer
   extends OneWireSensor
{

   //--------
   //-------- Switch Feature methods
   //--------

   /**
    * Checks to see if the channels of this switch are 'high side'
    * switches.  This indicates that when 'on' or <code>true</code>, the switch output is
    * connect to the 1-Wire data.  If this method returns  <code>false</code>
    * then when the switch is 'on' or <code>true</code>, the switch is connected
    * to ground.
    *
    * @return <code>true</code> if the switch is a 'high side' switch,
    *         <code>false</code> if the switch is a 'low side' switch
    *
    * @see #getLatchState(int,byte[])
    */
   public boolean isHighSideSwitch ();

   /**
    * Checks to see if the channels of this switch support
    * activity sensing.  If this method returns <code>true</code> then the
    * method <code>getSensedActivity(int,byte[])</code> can be used.
    *
    * @return <code>true</code> if channels support activity sensing
    *
    * @see #getSensedActivity(int,byte[])
    * @see #clearActivity()
    */
   public boolean hasActivitySensing ();

   /**
    * Checks to see if the channels of this switch support
    * level sensing.  If this method returns <code>true</code> then the
    * method <code>getLevel(int,byte[])</code> can be used.
    *
    * @return <code>true</code> if channels support level sensing
    *
    * @see #getLevel(int,byte[])
    */
   public boolean hasLevelSensing ();

   /**
    * Checks to see if the channels of this switch support
    * 'smart on'. Smart on is the ability to turn on a channel
    * such that only 1-Wire device on this channel are awake
    * and ready to do an operation.  This greatly reduces
    * the time to discover the device down a branch.
    * If this method returns <code>true</code> then the
    * method <code>setLatchState(int,boolean,boolean,byte[])</code>
    * can be used with the <code>doSmart</code> parameter <code>true</code>.
    *
    * @return <code>true</code> if channels support 'smart on'
    *
    * @see #setLatchState(int,boolean,boolean,byte[])
    */
   public boolean hasSmartOn ();

   /**
    * Checks to see if the channels of this switch require that only one
    * channel is on at any one time.  If this method returns <code>true</code> then the
    * method <code>setLatchState(int,boolean,boolean,byte[])</code>
    * will not only affect the state of the given
    * channel but may affect the state of the other channels as well
    * to insure that only one channel is on at a time.
    *
    * @return <code>true</code> if only one channel can be on at a time.
    *
    * @see #setLatchState(int,boolean,boolean,byte[])
    */
   public boolean onlySingleChannelOn ();

   //--------
   //-------- Switch 'get' Methods
   //--------

   /**
    * Gets the number of channels supported by this switch.
    * Channel specific methods will use a channel number specified
    * by an integer from [0 to (<code>getNumberChannels(byte[])</code> - 1)].  Note that
    * all devices of the same family will not necessarily have the
    * same number of channels.  The DS2406 comes in two packages--one that
    * has a single channel, and one that has two channels.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return the number of channels for this device
    */
   public int getNumberChannels (byte[] state);

   /**
    * Checks the sensed level on the indicated channel.
    * To avoid an exception, verify that this switch
    * has level sensing with the  <code>hasLevelSensing()</code>.
    * Level sensing means that the device can sense the logic
    * level on its PIO pin.
    *
    * @param channel channel to execute this operation, in the range [0 to (<code>getNumberChannels(byte[])</code> - 1)]
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return <code>true</code> if level sensed is 'high' and <code>false</code> if level sensed is 'low'
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #hasLevelSensing()
    */
   public boolean getLevel (int channel, byte[] state)
      throws OneWireException;

   /**
    * Checks the latch state of the indicated channel.
    *
    * @param channel channel to execute this operation, in the range [0 to (<code>getNumberChannels(byte[])</code> - 1)]
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return <code>true</code> if channel latch is 'on'
    * or conducting and <code>false</code> if channel latch is 'off' and not
    * conducting.  Note that the actual output when the latch is 'on'
    * is returned from the <code>isHighSideSwitch()</code> method.
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #isHighSideSwitch()
    * @see #setLatchState(int,boolean,boolean,byte[])
    */
   public boolean getLatchState (int channel, byte[] state);

   /**
    * Checks if the indicated channel has experienced activity.
    * This occurs when the level on the PIO pins changes.  To clear
    * the activity that is reported, call <code>clearActivity()</code>.
    * To avoid an exception, verify that this device supports activity
    * sensing by calling the method <code>hasActivitySensing()</code>.
    *
    * @param channel channel to execute this operation, in the range [0 to (<code>getNumberChannels(byte[])</code> - 1)]
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return <code>true</code> if activity was detected and <code>false</code> if no activity was detected
    *
    * @throws OneWireException if this device does not have activity sensing
    *
    * @see #hasActivitySensing()
    * @see #clearActivity()
    */
   public boolean getSensedActivity (int channel, byte[] state)
      throws OneWireException;

   //--------
   //-------- Switch 'set' Methods
   //--------

   /**
    * Sets the latch state of the indicated channel.
    * The method <code>writeDevice()</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice()</code>.
    *
    * @param channel channel to execute this operation, in the range [0 to (<code>getNumberChannels(byte[])</code> - 1)]
    * @param latchState <code>true</code> to set the channel latch 'on'
    *     (conducting) and <code>false</code> to set the channel latch 'off' (not
    *     conducting).  Note that the actual output when the latch is 'on'
    *     is returned from the <code>isHighSideSwitch()</code> method.
    * @param doSmart If latchState is 'on'/<code>true</code> then doSmart indicates
    *                  if a 'smart on' is to be done.  To avoid an exception
    *                  check the capabilities of this device using the
    *                  <code>hasSmartOn()</code> method.
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see #hasSmartOn()
    * @see #getLatchState(int,byte[])
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    */
   public void setLatchState (int channel, boolean latchState,
                              boolean doSmart, byte[] state);

   /**
    * <p>Clears the activity latches the next time possible.  For
    * example, on a DS2406/07, this happens the next time the
    * status is read with <code>readDevice()</code>.</p>
    *
    * <p>The activity latches will only be cleared once.  With the 
    * DS2406/07, this means that only the first call to <code>readDevice()</code>
    * will clear the activity latches.  Subsequent calls to <code>readDevice()</code>
    * will leave the activity latch states intact, unless this method has been invoked 
    * since the last call to <code>readDevice()</code>.</p>
    *
    * @throws OneWireException if this device does not support activity sensing
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #getSensedActivity(int,byte[])
    */
   public void clearActivity ()
      throws OneWireException;
}
