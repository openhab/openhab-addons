
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
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.OneWireException;


/**
 * <P> 1-Wire&#174 container for a Single Addressable Switch, DS2405.  This container
 * encapsulates the functionality of the 1-Wire family type <B>05</B> (hex)</P>
 *
 * <H3> Features </H3>
 * <UL>
 *   <LI> Open drain PIO pin controlled through 1-Wire communication
 *   <LI> Logic level sensing of the PIO pin can be sensed
 *   <LI> Operating temperature range from -40&#176C to
 *        +85&#176C
 *   <li> One channel with level sensing abilities
 *   <li> Does not support activity sensing or 'Smart On' capabilities
 * </UL>
 *
 * <H3> Memory </H3>
 *
 * <P> The DS2405 has no memory beyond its 64-bit registration number. </P>
 *
 *
 * <H3> Usage </H3>
 *
 * <p>The DS2405 supports level sensing, but not activity sensing.  The code below
 * reads the state of the DS2405, extracting the latch state and the sensed level
 * of the PIO pin, then in the loop it toggles the latch state.</p>
 *
 * <code><pre>
 *      // "ID" is a byte array of size 8 with an address of a part we
 *      // have already found with family code 05 hex
 *      // "access" is a DSPortAdapter
 *
 *      int i=0;
 *      OneWireContainer05 ds2405 = (OneWireContainer05) access.getDeviceContainer(ID);
 *      ds2405.setupContainer(access,ID);
 *
 *      byte[] state = ds2405.readDevice();
 *
 *      // I know that the 2405 only has one channel (one switch)
 *      // and it doesn't support 'Smart On'
 *
 *      boolean latch_state = ds2405.getLatchState(0,state);
 *      System.out.println("Current state of switch: "+latch_state);
 *      System.out.println("Current output level:    "+ds2405.getLevel(0,state));
 *      while (++i &lt; 100)
 *      {
 *          System.out.println("Toggling switch");
 *          ds2405.setLatchState(0,!latch_state,false,state);
 *          ds2405.writeDevice(state);
 *          state = ds2405.readDevice();
 *          latch_state = ds2405.getLatchState(0,state);
 *          System.out.println("Current state of switch: "+latch_state);
 *          System.out.println("Current output level:    "+ds2405.getLevel(0,state));
 *          Thread.sleep(500);
 *      }
 *
 * </pre></code>
 *
 * <p>Also see the usage example in the {@link com.dalsemi.onewire.container.SwitchContainer SwithContainer}
 * interface.</p>
 *
 * <H3> DataSheet </H3>
 * <DL>
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS2405.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS2405.pdf</A>
 * </DL>
 *
 * Also see the {@link com.dalsemi.onewire.container.OneWireContainer12 DS2406}, a dual addressable switch (OneWireContainer12).
 *
 * @see com.dalsemi.onewire.container.OneWireSensor
 * @see com.dalsemi.onewire.container.SwitchContainer
 * @see com.dalsemi.onewire.container.OneWireContainer12
 *
 *  @version    0.00, 28 Aug 2000
 *  @author     KLA,DSS
 */
public class OneWireContainer05
   extends OneWireContainer
   implements SwitchContainer
{

   //--------
   //-------- Constructors
   //--------

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS2405.
    * Note that the method <code>setupContainer(com.dalsemi.onewire.adapter.DSPortAdapter,byte[])</code>
    * must be called to set the correct <code>DSPortAdapter</code> device address.
    *
    * @see com.dalsemi.onewire.container.OneWireContainer#setupContainer(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) setupContainer(DSPortAdapter,byte[])
    * @see #OneWireContainer05(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer05(DSPortAdapter,byte[])
    * @see #OneWireContainer05(com.dalsemi.onewire.adapter.DSPortAdapter,long) OneWireContainer05(DSPortAdapter,long)
    * @see #OneWireContainer05(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer05(DSPortAdapter,String)
    */
   public OneWireContainer05 ()
   {
      super();
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS2405.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this 1-Wire device
    * @param  newAddress        address of this DS2405
    *
    * @see #OneWireContainer05()
    * @see #OneWireContainer05(com.dalsemi.onewire.adapter.DSPortAdapter,long) OneWireContainer05(DSPortAdapter,long)
    * @see #OneWireContainer05(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer05(DSPortAdapter,String)
    */
   public OneWireContainer05 (DSPortAdapter sourceAdapter, byte[] newAddress)
   {
      super(sourceAdapter, newAddress);
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS2405.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this 1-Wire device
    * @param  newAddress        address of this DS2405
    *
    * @see #OneWireContainer05()
    * @see #OneWireContainer05(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer05(DSPortAdapter,byte[])
    * @see #OneWireContainer05(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer05(DSPortAdapter,String)
    */
   public OneWireContainer05 (DSPortAdapter sourceAdapter, long newAddress)
   {
      super(sourceAdapter, newAddress);
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS2405.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this 1-Wire device
    * @param  newAddress        address of this DS2405
    *
    * @see #OneWireContainer05()
    * @see #OneWireContainer05(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer05(DSPortAdapter,byte[])
    * @see #OneWireContainer05(com.dalsemi.onewire.adapter.DSPortAdapter,long) OneWireContainer05(DSPortAdapter,long)
    */
   public OneWireContainer05 (DSPortAdapter sourceAdapter, String newAddress)
   {
      super(sourceAdapter, newAddress);
   }

   //--------
   //-------- Methods
   //--------

   /**
    * Gets the Dallas Semiconductor part number of the iButton
    * or 1-Wire Device as a <code>java.lang.String</code>.
    * For example "DS1992".
    *
    * @return iButton or 1-Wire device name
    */
   public String getName ()
   {
      return "DS2405";
   }

   /**
    * Retrieves the alternate Dallas Semiconductor part numbers or names.
    * A 'family' of MicroLAN devices may have more than one part number
    * depending on packaging.  There can also be nicknames such as
    * "Crypto iButton".
    *
    * @return  the alternate names for this iButton or 1-Wire device
    */
   public String getAlternateNames ()
   {
      return "Addressable Switch";
   }

   /**
    * Gets a short description of the function of this iButton
    * or 1-Wire Device type.
    *
    * @return device description
    */
   public String getDescription ()
   {
      return "Addressable Switch with controlled open drain PIO "
             + "pin. PIO pin sink capability is greater than 4mA "
             + "at 0.4V.";
   }

   //--------
   //-------- Switch Feature methods
   //--------

   /**
    * Gets the number of channels supported by this switch.
    * Channel specific methods will use a channel number specified
    * by an integer from [0 to (<code>getNumberChannels(byte[])</code> - 1)].  Note that
    * all devices of the same family will not necessarily have the
    * same number of channels.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return the number of channels for this device
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    */
   public int getNumberChannels (byte[] state)
   {
      //we ignore the state, DS2405 can only have one channel
      return 1;
   }

   /**
    * Checks if the channels of this switch are 'high side'
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
   public boolean isHighSideSwitch ()
   {
      return false;
   }

   /**
    * Checks if the channels of this switch support
    * activity sensing.  If this method returns <code>true</code> then the
    * method <code>getSensedActivity(int,byte[])</code> can be used.
    *
    * @return <code>true</code> if channels support activity sensing
    *
    * @see #getSensedActivity(int,byte[])
    * @see #clearActivity()
    */
   public boolean hasActivitySensing ()
   {
      return false;
   }

   /**
    * Checks if the channels of this switch support
    * level sensing.  If this method returns <code>true</code> then the
    * method <code>getLevel(int,byte[])</code> can be used.
    *
    * @return <code>true</code> if channels support level sensing
    *
    * @see #getLevel(int,byte[])
    */
   public boolean hasLevelSensing ()
   {
      return true;
   }

   /**
    * Checks if the channels of this switch support
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
   public boolean hasSmartOn ()
   {
      return false;
   }

   /**
    * Checks if the channels of this switch require that only one
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
   public boolean onlySingleChannelOn ()
   {
      return true;
   }

   //--------
   //-------- Switch 'get' Methods
   //--------

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
   {
      return ((state [0] & 0x02) == 0x02);
   }

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
   public boolean getLatchState (int channel, byte[] state)
   {
      return ((state [0] & 0x01) == 0x01);
   }

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
      throws OneWireException
   {

      //i don't do this
      throw new OneWireException("Sense Activity not supported");
   }

   /**
    * Clears the activity latches the next time possible.  For
    * example, on a DS2406/07, this happens the next time the
    * status is read with <code>readDevice()</code>.
    *
    * @throws OneWireException if this device does not support activity sensing
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #getSensedActivity(int,byte[])
    */
   public void clearActivity ()
      throws OneWireException
   {

      //i don't do this
      throw new OneWireException("Sense Activity not supported");
   }

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
                              boolean doSmart, byte[] state)
   {
      if (latchState)
         state [0] = ( byte ) (state [0] | 0x01);
      else
         state [0] = ( byte ) (state [0] & 0xfe);
   }

   /**
    * Retrieves the 1-Wire device sensor state.  This state is
    * returned as a byte array.  Pass this byte array to the 'get'
    * and 'set' methods.  If the device state needs to be changed then call
    * the 'writeDevice' to finalize the changes.
    *
    * @return 1-Wire device sensor state
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public byte[] readDevice ()
      throws OneWireIOException, OneWireException
   {

      //first let's make sure we can talk to the part
      //speed is not critical with the 2405 so i'll just call doSpeed()
      doSpeed();

      //this ain't a hard part--it's only gonna take 1 byte
      byte[] state = new byte [1];

      //here's the 'bitmap'
      //bit 0 :   switch state (0 for conducting, 1 for non-conducting)
      //bit 1 :   sensed level (0 for low, 1 for high)
      state [0] = ( byte ) 0;

      if (isPresent())
      {
         if (isAlarming())
            state [0] = 1;
      }
      else
         throw new OneWireIOException("Device not present");

      if (isPresent())
      {

         // Byte after 'search' indicates level
         if (adapter.getByte() != 0)
            state [0] = ( byte ) (state [0] | 0x02);
      }
      else
         throw new OneWireIOException("Device not present");

      return state;
   }

   /**
    * Writes the 1-Wire device sensor state that
    * have been changed by 'set' methods.  Only the state registers that
    * changed are updated.  This is done by referencing a field information
    * appended to the state data.
    *
    * @param  state 1-Wire device sensor state
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public void writeDevice (byte[] state)
      throws OneWireIOException, OneWireException
   {
      doSpeed();

      boolean value   = ((state [0] & 0x01) == 0x01);
      boolean compare = isAlarming();

      // check to see if already in the correct state
      if (compare == value)
         return;

         // incorrect state so toggle
      else if (adapter.select(address))
      {

         // verify
         compare = isAlarming();

         if (compare == value)
            return;
      }

      throw new OneWireIOException("Failure to change DS2405 latch state");
   }
}
