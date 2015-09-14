
/*---------------------------------------------------------------------------
 * Copyright (C) 1999 - 2001 Dallas Semiconductor Corporation, All Rights Reserved.
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

import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.utils.Bit;


/**
 * <P> 1-Wire container for Real-Time Clock with Interrupt, DS2417.  The DS2417 is similar
 * to the DS2415 with the addition of a hardware interrupt pin.  This container 
 * encapsulates the functionality of the iButton family type <B>27</B> (hex)</P>
 * 
 * <H3> Features </H3> 
 * <UL>
 *   <LI> Real-Time Clock with fully compatible 1-Wire MicroLAN interface
 *   <LI> Programmable interrupt output for system wakeup
 *   <LI> Uses the same binary time/date representation as the DS2404 
 *        but with 1 second resolution
 *   <LI> Clock accuracy ± 2 minutes per month at 25&#176
 *   <LI> Operating temperature range from -40&#176C to
 *        +85&#176C
 *   <LI> Low power, 200 nA typical with oscillator running
 * </UL>
 * 
 * <H3> Clock </H3> 
 *
 * <P>The clock methods can be organized into the following categories.  Note that methods
 *    that are implemented for the {@link com.dalsemi.onewire.container.ClockContainer ClockContainer} 
 *    interface are marked with (*): </P>
 * <UL>
 *   <LI> <B> Information </B>
 *     <UL>
 *       <LI> {@link #hasClockAlarm() hasClockAlarm} *
 *       <LI> {@link #canDisableClock() canDisableClock} *
 *       <LI> {@link #getClockResolution() getClockResolution} *
 *     </UL>
 *   <LI> <B> Read </B>
 *     <UL>
 *       <LI> <I> Clock </I>
 *         <UL>
 *           <LI> {@link #getClock(byte[]) getClock} *
 *           <LI> {@link #getClockAlarm(byte[]) getClockAlarm} *
 *           <LI> {@link #isClockAlarming(byte[]) isClockAlarming} *
 *           <LI> {@link #isClockAlarmEnabled(byte[]) isClockAlarmEnabled} *
 *           <LI> {@link #isClockRunning(byte[]) isClockRunning} *
 *         </UL>
 *       <LI> <I> Misc </I>
 *         <UL>
 *           <LI> {@link #readDevice() readDevice} *
 *         </UL>
 *     </UL>
 *   <LI> <B> Write </B>
 *     <UL>
 *       <LI> <I> Clock </I>
 *         <UL>
 *           <LI> {@link #setClock(long, byte[]) setClock} *
 *           <LI> {@link #setClockAlarm(long, byte[]) setClockAlarm} *
 *           <LI> {@link #setClockRunEnable(boolean, byte[]) setClockRunEnable} *
 *           <LI> {@link #setClockAlarmEnable(boolean, byte[]) setClockAlarmEnable} *
 *         </UL>
 *       <LI> <I> Misc </I>
 *         <UL>
 *           <LI> {@link #writeDevice(byte[]) writeDevice} *
 *         </UL>
 *     </UL>
 *  </UL>
 *
 * <H3> Usage </H3> 
 * 
 * <DL> 
 * <DD> See the usage example in 
 * {@link com.dalsemi.onewire.container.ClockContainer ClockContainer}
 * for clock specific operations.
 * </DL>
 *
 * <H3> DataSheet </H3> 
 * <DL>
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS2417.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS2417.pdf</A>
 * </DL>
 * 
 * @see com.dalsemi.onewire.container.ClockContainer
 * 
 * @version    1.10, 26 September 2001
 * @author     BA
 */
public class OneWireContainer27
   extends OneWireContainer24
   implements ClockContainer
{
   /** Passed to setInterruptInterval to set the interrupt interval to 1 second. */
   public       static  final   byte    INTERRUPT_INTERVAL_1     = (byte)0x00;
   /** Passed to setInterruptInterval to set the interrupt interval to 4 seconds. */
   public       static  final   byte    INTERRUPT_INTERVAL_4     = (byte)0x01;
   /** Passed to setInterruptInterval to set the interrupt interval to 32 seconds. */
   public       static  final   byte    INTERRUPT_INTERVAL_32    = (byte)0x02;
   /** Passed to setInterruptInterval to set the interrupt interval to 64 seconds. */
   public       static  final   byte    INTERRUPT_INTERVAL_64    = (byte)0x03;
   /** Passed to setInterruptInterval to set the interrupt interval to 2048 seconds. */
   public       static  final   byte    INTERRUPT_INTERVAL_2048  = (byte)0x04;
   /** Passed to setInterruptInterval to set the interrupt interval to 1 seconds. */
   public       static  final   byte    INTERRUPT_INTERVAL_4096  = (byte)0x05;
   /** Passed to setInterruptInterval to set the interrupt interval to 65536 seconds. */
   public       static  final   byte    INTERRUPT_INTERVAL_65536 = (byte)0x06;
   /** Passed to setInterruptInterval to set the interrupt interval to 131072 seconds. */
   public       static  final   byte    INTERRUPT_INTERVAL_131072= (byte)0x07;


   //--- Constructors
   /**
    * Create an empty container that is not complete until after a call
    * to <code>setupContainer</code>. <p>
    *
    * This is one of the methods to construct a container.  The others are
    * through creating a OneWireContainer with parameters.
    *
    * @see #setupContainer(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) super.setupContainer()
    */
   public OneWireContainer27 ()
   {
      super();
   }

   /**
    * Create a container with the provided adapter instance
    * and the address of the iButton or 1-Wire device.<p>
    *
    * This is one of the methods to construct a container.  The other is
    * through creating a OneWireContainer with NO parameters.
    *
    * @param  sourceAdapter     adapter instance used to communicate with
    * this iButton
    * @param  newAddress        {@link com.dalsemi.onewire.utils.Address Address}
    *                           of this 1-Wire device
    *
    * @see #OneWireContainer27() OneWireContainer27
    * @see com.dalsemi.onewire.utils.Address utils.Address
    */
   public OneWireContainer27 (DSPortAdapter sourceAdapter, byte[] newAddress)
   {
      super(sourceAdapter, newAddress);
   }

   /**
    * Create a container with the provided adapter instance
    * and the address of the iButton or 1-Wire device.<p>
    *
    * This is one of the methods to construct a container.  The other is
    * through creating a OneWireContainer with NO parameters.
    *
    * @param  sourceAdapter     adapter instance used to communicate with
    * this 1-Wire device
    * @param  newAddress        {@link com.dalsemi.onewire.utils.Address Address}
    *                            of this 1-Wire device
    *
    * @see #OneWireContainer27() OneWireContainer27
    * @see com.dalsemi.onewire.utils.Address utils.Address
    */
   public OneWireContainer27 (DSPortAdapter sourceAdapter, long newAddress)
   {
      super(sourceAdapter, newAddress);
   }

   /**
    * Create a container with the provided adapter instance
    * and the address of the iButton or 1-Wire device.<p>
    *
    * This is one of the methods to construct a container.  The other is
    * through creating a OneWireContainer with NO parameters.
    *
    * @param  sourceAdapter     adapter instance used to communicate with
    * this 1-Wire device
    * @param  newAddress        {@link com.dalsemi.onewire.utils.Address Address}
    *                            of this 1-Wire device
    *
    * @see #OneWireContainer27() OneWireContainer27
    * @see com.dalsemi.onewire.utils.Address utils.Address
    */
   public OneWireContainer27 (DSPortAdapter sourceAdapter, String newAddress)
   {
      super(sourceAdapter, newAddress);
   }

   //--------
   //-------- Methods
   //--------

   /**
    * Get the Dallas Semiconductor part number of the iButton
    * or 1-Wire Device as a string.  For example 'DS1992'.
    *
    * @return iButton or 1-Wire device name
    */
   public String getName ()
   {
      return "DS2417";
   }

   /**
    * Get the alternate Dallas Semiconductor part numbers or names.
    * A 'family' of 1-Wire Network devices may have more than one part number
    * depending on packaging.  There can also be nicknames such as
    * 'Crypto iButton'.
    *
    * @return 1-Wire device alternate names
    */
   public String getAlternateNames ()
   {
      return "1-Wire Time Chip with hardware interrupt";
   }

   /**
    * Get a short description of the function of this iButton
    * or 1-Wire Device type.
    *
    * @return device description
    */
   public String getDescription ()
   {
      return "Real time clock with interrupt implemented as a binary counter " +
             "that can be used to add functions such as " +
             "calendar, time and date stamp and logbook to any " +
             "type of electronic device or embedded application that " +
             "uses a microcontroller.";
   }

   //--------
   //-------- Clock Feature methods
   //--------

   //--------
   //-------- Clock 'get' Methods
   //--------

   /**
    * Returns the interval, in seconds, that the device will interrupt on.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    * @return number of seconds in between interrupts.
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #getClock(byte[])
    */
   public long getInterruptInterval (byte[] state)
   {
       long ret = 0;
       switch(((state[CONTROL_OFFSET] & 0x70) >>> 4))
       {
          default:
             break;
          case 0x00: ret = 1;
                     break;
          case 0x01: ret = 4;
                     break;
          case 0x02: ret = 32;
                     break;
          case 0x03: ret = 64;
                     break;
          case 0x04: ret = 2048;
                     break;
          case 0x05: ret = 4096;
                     break;
          case 0x06: ret = 65536;
                     break;
          case 0x07: ret = 131072;
                     break;
      }
      return ret;
   }


   /**
    *
    * Checks to see if interrupt mode is turned on. If so, pulses will be generated
    * at an interval selected by setInterruptInterval.
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return true if interrupts are enabled
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #canDisableClock()
    * @see #setClockRunEnable(boolean,byte[])
    */
   public boolean isInterruptEnabled (byte[] state)
   {
      return (Bit.arrayReadBit(7, CONTROL_OFFSET, state) == 1);
   }

   //--------
   //-------- Clock 'set' Methods
   //--------

   /**
    * Sets the interval at which interrupting will occur.  Note that this feature
    * must be enabled first using setInterruptEnable(true,state).
    *
    * The method <code>writeDevice(byte[])</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice(byte[])</code>.
    *
    * @param intervalValue One of the following variables should be passed, representing
    * different time intervals to interrupt at:
    *       <code>INTERRUPT_INTERVAL_1</code>    1 second
    *       <code>INTERRUPT_INTERVAL_4</code>    4 seconds
    *       <code>INTERRUPT_INTERVAL_32</code>    32 seconds
    *       <code>INTERRUPT_INTERVAL_32</code>    64 seconds
    *       <code>INTERRUPT_INTERVAL_64</code>    2048 seconds
    *       <code>INTERRUPT_INTERVAL_2048</code>    4096 seconds
    *       <code>INTERRUPT_INTERVAL_4096</code>    65536 seconds
    *       <code>INTERRUPT_INTERVAL_131072</code>    131072 seconds
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #getClock(byte[])
    */
   public void setInterruptInterval (byte intervalValue, byte[] state)
   {
       state[CONTROL_OFFSET] &= (byte)0x8F;
       state[CONTROL_OFFSET] |= (byte)(intervalValue << 4);
   }

   /**
    * Enables or disables hardware interrupting.  If enabled, the
    * device sends an interrupt at intervals defined by using the
    * setInterruptInterval function.
    * @param runEnable true to enable interval interrupts.
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #canDisableClock()
    * @see #isClockRunning(byte[])
    */
   public void setInterruptEnable (boolean iEnable, byte[] state)
   {
      Bit.arrayWriteBit(iEnable ? 1
                                  : 0, 7, CONTROL_OFFSET, state);
   }

}
