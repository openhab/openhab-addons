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
import java.util.Vector;
import java.util.Enumeration;


/**
 * <P> 1-Wire&#174 container for a Single Addressable Switch, DS2408.  This container
 * encapsulates the functionality of the 1-Wire family type <B>29</B> (hex)</P>
 *
 * <H3> Features </H3>
 * <UL>
 *   <LI> Eight channels of programmable I/O with open-drain outputs
 *   <LI> Logic level sensing of the PIO pin can be sensed
 *   <LI> Multiple DS2408's can be identified on a common 1-Wire bus and operated
 *        independently.
 *   <LI> Supports 1-Wire Conditional Search command with response controlled by
 *        programmable PIO conditions
 *   <LI> Supports Overdrive mode which boosts communication speed up to 142k bits
 *        per second.
 * </UL>
 *
 * <H3> Usage </H3>
 *
 *
 * @see com.dalsemi.onewire.container.OneWireSensor
 * @see com.dalsemi.onewire.container.SwitchContainer
 * @see com.dalsemi.onewire.container.OneWireContainer
 *
 *  @version    1.00, 01 Jun 2002
 *  @author     JPE
 */
public class OneWireContainer29
   extends OneWireContainer
   implements SwitchContainer
{
   //--------
   //-------- Variables
   //--------

   /**
    * Status memory bank of the DS2408 for memory map registers
    */
   private MemoryBankEEPROMstatus map;

   /**
    * Status memory bank of the DS2408 for the conditional search
    */
   private MemoryBankEEPROMstatus search;

   /**
    * Reset the activity latches
    */
   public static final byte RESET_ACTIVITY_LATCHES = ( byte ) 0xC3;

   /**
    * Used for 0xFF array
    */
   private byte[] FF = new byte [8];


   //--------
   //-------- Constructors
   //--------

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS2408.
    * Note that the method <code>setupContainer(com.dalsemi.onewire.adapter.DSPortAdapter,byte[])</code>
    * must be called to set the correct <code>DSPortAdapter</code> device address.
    *
    * @see com.dalsemi.onewire.container.OneWireContainer#setupContainer(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) setupContainer(DSPortAdapter,byte[])
    * @see #OneWireContainer29(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer29(DSPortAdapter,byte[])
    * @see #OneWireContainer29(com.dalsemi.onewire.adapter.DSPortAdapter,long) OneWireContainer29(DSPortAdapter,long)
    * @see #OneWireContainer29(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer29(DSPortAdapter,String)
    */
   public OneWireContainer29 ()
   {
      super();

      initmem();

      for(int i=0; i<FF.length; i++)
         FF[i] = (byte) 0x0FF;
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS2408.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this 1-Wire device
    * @param  newAddress        address of this DS2408
    *
    * @see #OneWireContainer29()
    * @see #OneWireContainer29(com.dalsemi.onewire.adapter.DSPortAdapter,long) OneWireContainer29(DSPortAdapter,long)
    * @see #OneWireContainer29(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer29(DSPortAdapter,String)
    */
   public OneWireContainer29 (DSPortAdapter sourceAdapter, byte[] newAddress)
   {
      super(sourceAdapter, newAddress);

      initmem();

      for(int i=0; i<FF.length; i++)
         FF[i] = (byte) 0x0FF;
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS2408.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this 1-Wire device
    * @param  newAddress        address of this DS2408
    *
    * @see #OneWireContainer29()
    * @see #OneWireContainer29(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer29(DSPortAdapter,byte[])
    * @see #OneWireContainer29(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer29(DSPortAdapter,String)
    */
   public OneWireContainer29 (DSPortAdapter sourceAdapter, long newAddress)
   {
      super(sourceAdapter, newAddress);

      initmem();

      for(int i=0; i<FF.length; i++)
         FF[i] = (byte) 0x0FF;
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS2408.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this 1-Wire device
    * @param  newAddress        address of this DS2408
    *
    * @see #OneWireContainer29()
    * @see #OneWireContainer29(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer29(DSPortAdapter,byte[])
    * @see #OneWireContainer29(com.dalsemi.onewire.adapter.DSPortAdapter,long) OneWireContainer29(DSPortAdapter,long)
    */
   public OneWireContainer29 (DSPortAdapter sourceAdapter, String newAddress)
   {
      super(sourceAdapter, newAddress);

      initmem();

      for(int i=0; i<FF.length; i++)
         FF[i] = (byte) 0x0FF;
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
      return "DS2408";
   }

   /**
    * Gets an enumeration of memory bank instances that implement one or more
    * of the following interfaces:
    * {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
    * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank},
    * and {@link com.dalsemi.onewire.container.OTPMemoryBank OTPMemoryBank}.
    * @return <CODE>Enumeration</CODE> of memory banks
    */
   public Enumeration getMemoryBanks ()
   {
      Vector bank_vector = new Vector(5);

      bank_vector.addElement(map);
      bank_vector.addElement(search);

      return bank_vector.elements();
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
      return "8-Channel Addressable Switch";
   }

   /**
    * Gets a short description of the function of this iButton
    * or 1-Wire Device type.
    *
    * @return device description
    */
   public String getDescription ()
   {
      return "1-Wire 8-Channel Addressable Switch";
   }

   /**
    * Returns the maximum speed this iButton or 1-Wire device can
    * communicate at.
    *
    * @return maximum speed
    * @see DSPortAdapter#setSpeed
    */
   public int getMaxSpeed ()
   {
      return DSPortAdapter.SPEED_OVERDRIVE;
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
      // check the 88h byte bits 6 and 7
      // 00 - 4 channels
      // 01 - 5 channels
      // 10 - 8 channels
      // 11 - 16 channes, which hasn't been implemented yet
      return 8;
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
      return true;
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
      return false;
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
      byte  level = (byte) (0x01 << channel);
      return ((state[0] & level) == level);
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
      byte latch = (byte) (0x01 << channel);
      return ((state [1] & latch) == latch);
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
      byte activity = (byte) (0x01 << channel);
      return ((state[2] & activity) == activity);
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
      adapter.select(address);
      byte[] buffer = new byte[9];

      buffer[0] = RESET_ACTIVITY_LATCHES;
      System.arraycopy(FF,0,buffer,1,8);

      adapter.dataBlock(buffer, 0, 9);

      if((buffer[1] != (byte) 0xAA) && (buffer[1] != (byte) 0x55))
         throw new OneWireException("Sense Activity was not cleared.");
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
      byte latch = (byte) (0x01 << channel);

      if (latchState)
         state[1] = (byte) (state[1] | latch);
      else
         state[1] = (byte) (state[1] & ~latch);
   }

   /**
    * Sets the latch state for all of the channels.
    * The method <code>writeDevice()</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice()</code>.
    *
    * @param set the state to set all of the channels, in the range [0 to (<code>getNumberChannels(byte[])</code> - 1)]
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see #getLatchState(int,byte[])
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    */
   public void setLatchState (byte set, byte[] state)
   {
      state[1] = (byte) set;
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
      byte[] state = new byte [3];

      System.arraycopy(FF,0,state,0,3);
      map.read(0,false,state,0,3);

      return state;
   }

   /**
    * Retrieves the 1-Wire device register mask.  This register is
    * returned as a byte array.  Pass this byte array to the 'get'
    * and 'set' methods.  If the device register mask needs to be changed then call
    * the 'writeRegister' to finalize the changes.
    *
    * @return 1-Wire device register mask
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public byte[] readRegister ()
      throws OneWireIOException, OneWireException
   {
      byte[] register = new byte[3];

      search.read(0,false,register,0,3);

      return register;
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
      map.write(1,state,1,1);
   }

   /**
    * Writes the 1-Wire device register mask that
    * have been changed by 'set' methods.
    *
    * @param  register 1-Wire device sensor state
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public void writeRegister (byte[] register)
      throws OneWireIOException, OneWireException
   {
      search.write(0,register,0,3);
   }

   /**
    * Turns the Reset mode on/off.
    *
    * @param  set if 'TRUE' the reset mode will be set or 'FALSE' to turn it off.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public void setResetMode(byte[] register, boolean set)
      throws OneWireIOException, OneWireException
   {
      if(set && ((register[2] & 0x04) == 0x04))
      {
         register[2] = (byte) (register[2] & (byte) 0xFB);
      }
      else if((!set) && ((register[2] & (byte) 0x04) == (byte) 0x00))
      {
         register[2] = (byte) (register[2] | (byte) 0x04);
      }
   }

   /**
    * Retrieves the state of the VCC pin.  If the pin is powered 'TRUE' is
    * returned else 'FALSE' is returned if the pin is grounded.
    *
    * @return <code>true</code> if VCC is powered and <code>false</code> if it is
    *         grounded.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public boolean getVCC(byte[] register)
      throws OneWireIOException, OneWireException
   {
      if((register[2] & (byte) 0x80) == (byte) 0x80)
         return true;

      return false;
   }

   /**
    * Checks if the Power On Reset if on and if so clears it.
    *
    * @param register current register for conditional search, which
    *                 if returned from <code>readRegister()</code>
    */
   public void clearPowerOnReset(byte[] register)
   {
      if((register[2] & (byte) 0x08) == (byte) 0x08)
      {
         register[2] = (byte) ((byte) register[2] & (byte) 0xF7);
      }
   }

   /**
    * Checks if the 'or' Condition Search is set and if not sets it.
    *
    * @param register current register for conditional search, which
    *                 if returned from <code>readRegister()</code>
    */
   public void orConditionalSearch(byte[] register)
   {
      if((register[2] & (byte) 0x02) == (byte) 0x02)
      {
         register[2] = (byte) ((byte) register[2] & (byte) 0xFD);
      }
   }

   /**
    * Checks if the 'and' Conditional Search is set and if not sets it.
    *
    * @param register current register for conditional search, which
    *                 if returned from <code>readRegister()</code>
    */
   public void andConditionalSearch(byte[] register)
   {
      if((register[2] & (byte) 0x02) != (byte) 0x02)
      {
         register[2] = (byte) ((byte) register[2] | (byte) 0x02);
      }
   }

   /**
    * Checks if the 'PIO' Conditional Search is set for input and if not sets it.
    *
    * @param register current register for conditional search, which
    *                 if returned from <code>readRegister()</code>
    */
   public void pioConditionalSearch(byte[] register)
   {
      if((register[2] & (byte) 0x01) == (byte) 0x01)
      {
         register[2] = (byte) ((byte) register[2] & (byte) 0xFE);
      }
   }

   /**
    * Checks if the activity latches are set for Conditional Search and if not sets it.
    *
    * @param register current register for conditional search, which
    *                 if returned from <code>readRegister()</code>
    */
   public void activityConditionalSearch(byte[] register)
   {
      if((register[2] & (byte) 0x01) != (byte) 0x01)
      {
         register[2] = (byte) ((byte) register[2] | (byte) 0x01);
      }
   }

   /**
    * Sets the channel passed to the proper state depending on the set parameter for
    * responding to the Conditional Search.
    *
    * @param channel  current channel to set
    * @param set      whether to turn the channel on/off for Conditional Search
    * @param register current register for conditional search, which
    *                 if returned from <code>readRegister()</code>
    */
   public void setChannelMask(int channel, boolean set, byte[] register)
   {
      byte mask = (byte) (0x01 << channel);

      if(set)
         register[0] = (byte) ((byte) register[0] | (byte) mask);
      else
         register[0] = (byte) ((byte) register[0] & (byte) ~mask);
   }

   /**
    * Sets the channel passed to the proper state depending on the set parameter for
    * the correct polarity in the Conditional Search.
    *
    * @param channel  current channel to set
    * @param set      whether to turn the channel on/off for polarity
    *                 Conditional Search
    * @param register current register for conditional search, which
    *                 if returned from <code>readRegister()</code>
    */
   public void setChannelPolarity(int channel, boolean set, byte[] register)
   {
      byte polarity = (byte) (0x01 << channel);

      if(set)
         register[1] = (byte) ((byte) register[1] | (byte) polarity);
      else
         register[1] = (byte) ((byte) register[1] & (byte) ~polarity);
   }

   /**
    * Retrieves the information if the channel is masked for the Conditional Search.
    *
    * @param channel  current channel to set
    * @param register current register for conditional search, which
    *                 if returned from <code>readRegister()</code>
    *
    * @return <code>true</code> if the channel is masked and <code>false</code> other wise.
    */
   public boolean getChannelMask(int channel, byte[] register)
   {
      byte mask = (byte) (0x01 << channel);

      return ((register[0] & mask) == mask);
   }

   /**
    * Retrieves the polarity of the channel for the Conditional Search.
    *
    * @param channel  current channel to set
    * @param register current register for conditional search, which
    *                 if returned from <code>readRegister()</code>
    *
    * @return <code>true</code> if the channel is masked and <code>false</code> other wise.
    */
   public boolean getChannelPolarity(int channel, byte[] register)
   {
      byte polarity = (byte) (0x01 << channel);

      return ((register[1] & polarity) == polarity);
   }

   /**
    * Initialize the memory banks and data associated with each.
    */
   private void initmem()
   {
      // Memory map registers
      map = new MemoryBankEEPROMstatus(this);
      map.bankDescription      = "Memory mapped register of pin logic state, port output " +
                                 "latch logic state and activity latch logic state.";
      map.startPhysicalAddress = 136;
      map.size                 = 3;
      map.readOnly             = true;

      // Conditional Search
      search = new MemoryBankEEPROMstatus(this);
      search.bankDescription   = "Conditional search bit mask, polarity bit mask and " +
                                 "control register.";
      search.startPhysicalAddress = 139;
      search.size                 = 3;
      search.readWrite            = true;
   }
}
