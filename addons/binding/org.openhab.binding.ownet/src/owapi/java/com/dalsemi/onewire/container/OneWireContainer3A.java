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
 * <P> 1-Wire&#174 container for a Single Addressable Switch, DS2413.  This container
 * encapsulates the functionality of the 1-Wire family type <B>3A</B> (hex)</P>
 *
 * <H3> Features </H3>
 * <UL>
 *   <LI> Eight channels of programmable I/O with open-drain outputs
 *   <LI> Logic level sensing of the PIO pin can be sensed
 *   <LI> Multiple DS2413's can be identified on a common 1-Wire bus and operated
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
public class OneWireContainer3A
   extends OneWireContainer
   implements SwitchContainer
{
   //--------
   //-------- Variables
   //--------

   /**
    * Status memory bank of the DS2413 for memory map registers
    */
   private MemoryBankEEPROMstatus map;

   /**
    * Status memory bank of the DS2413 for the conditional search
    */
   private MemoryBankEEPROMstatus search;

   /**
    * PIO Access read command
    */
   public static final byte PIO_ACCESS_READ = ( byte ) 0xF5;

   /**
    * PIO Access read command
    */
   public static final byte PIO_ACCESS_WRITE = ( byte ) 0x5A;

   /**
    * Used for 0xFF array
    */
   private byte[] FF = new byte [8];


   //--------
   //-------- Constructors
   //--------

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS2413.
    * Note that the method <code>setupContainer(com.dalsemi.onewire.adapter.DSPortAdapter,byte[])</code>
    * must be called to set the correct <code>DSPortAdapter</code> device address.
    *
    * @see com.dalsemi.onewire.container.OneWireContainer#setupContainer(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) setupContainer(DSPortAdapter,byte[])
    * @see #OneWireContainer3A(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer3A(DSPortAdapter,byte[])
    * @see #OneWireContainer3A(com.dalsemi.onewire.adapter.DSPortAdapter,long) OneWireContainer3A(DSPortAdapter,long)
    * @see #OneWireContainer3A(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer3A(DSPortAdapter,String)
    */
   public OneWireContainer3A ()
   {
      super();

      for(int i=0; i<FF.length; i++)
         FF[i] = (byte) 0x0FF;
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS2413.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this 1-Wire device
    * @param  newAddress        address of this DS2413
    *
    * @see #OneWireContainer3A()
    * @see #OneWireContainer3A(com.dalsemi.onewire.adapter.DSPortAdapter,long) OneWireContainer3A(DSPortAdapter,long)
    * @see #OneWireContainer3A(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer3A(DSPortAdapter,String)
    */
   public OneWireContainer3A (DSPortAdapter sourceAdapter, byte[] newAddress)
   {
      super(sourceAdapter, newAddress);

      for(int i=0; i<FF.length; i++)
         FF[i] = (byte) 0x0FF;
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS2413.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this 1-Wire device
    * @param  newAddress        address of this DS2413
    *
    * @see #OneWireContainer3A()
    * @see #OneWireContainer3A(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer3A(DSPortAdapter,byte[])
    * @see #OneWireContainer3A(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer3A(DSPortAdapter,String)
    */
   public OneWireContainer3A (DSPortAdapter sourceAdapter, long newAddress)
   {
      super(sourceAdapter, newAddress);

      for(int i=0; i<FF.length; i++)
         FF[i] = (byte) 0x0FF;
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS2413.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this 1-Wire device
    * @param  newAddress        address of this DS2413
    *
    * @see #OneWireContainer3A()
    * @see #OneWireContainer3A(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer3A(DSPortAdapter,byte[])
    * @see #OneWireContainer3A(com.dalsemi.onewire.adapter.DSPortAdapter,long) OneWireContainer3A(DSPortAdapter,long)
    */
   public OneWireContainer3A (DSPortAdapter sourceAdapter, String newAddress)
   {
      super(sourceAdapter, newAddress);

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
      return "DS2413";
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
      return "Dual Channel Switch";
   }

   /**
    * Gets a short description of the function of this iButton
    * or 1-Wire Device type.
    *
    * @return device description
    */
   public String getDescription ()
   {
      return "Dual Channel Addressable Switch";
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
      return 2;
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
      byte  level = (byte) (0x01 << (channel*2));
      return ((state[1] & level) == level);
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
      byte latch = (byte) (0x01 << ((channel*2)+1));
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
      return false;
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
      byte temp;

      state[0] = (byte) 0x00FC;

      if(getLatchState(0,state))
      {
         temp = (byte) 0x01;
         state[0] = (byte) (((byte) state[0]) | temp);
      }
      
      if(getLatchState(1,state))
      {
         temp = (byte) 0x02;
         state[0] = (byte) (((byte) state[0]) | temp);
      }

      if (latchState)
         state[0] = (byte) (state[0] | latch);
      else
         state[0] = (byte) (state[0] & ~latch);
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
      state[0] = (byte) set;
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
      byte[] buff = new byte [2];

      buff[0] = (byte) 0xF5;  // PIO Access Read Command
      buff[1] = (byte) 0xFF;  // Used to read the PIO Status Bit Assignment

      // select the device
      if (adapter.select(address))
      {
         adapter.dataBlock(buff, 0, 2);
      }
      else
         throw new OneWireIOException("Device select failed");

      return buff;
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

      return register;
   }

   /**
    * Writes the 1-Wire device sensor state that
    * have been changed by 'set' methods.  Only the state registers that
    * changed are updated.  This is done by referencing a field information
    * appended to the state data.
    *
    * @param  state 1-Wire device PIO access write (x x x x x x PIOB PIOA)
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public void writeDevice (byte state[])
      throws OneWireIOException, OneWireException
   {
      byte[] buff = new byte [5];

      buff[0] = (byte) 0x5A;      // PIO Access Write Command
      buff[1] = (byte) state[0];  // Channel write information
      buff[2] = (byte) ~state[0]; // Inverted write byte
      buff[3] = (byte) 0xFF;      // Confirmation Byte
      buff[4] = (byte) 0xFF;      // PIO Pin Status

      // select the device
      if (adapter.select(address))
      {
         adapter.dataBlock(buff, 0, 5);
      }
      else
         throw new OneWireIOException("Device select failed");

      if(buff[3] != (byte) 0x00AA)
      {
         throw new OneWireIOException("Failure to change latch state.");
      }
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
   }

}
