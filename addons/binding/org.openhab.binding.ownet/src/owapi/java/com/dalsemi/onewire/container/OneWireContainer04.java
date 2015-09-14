
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
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.utils.Bit;
import com.dalsemi.onewire.utils.Convert;
import java.util.Vector;
import java.util.Enumeration;


/**
 * <P> 1-Wire container for 512 byte memory iButton Plus Time, DS1994 and 1-Wire Chip, DS2404.
 * This container encapsulates the functionality of the iButton family
 * type <B>04</B> (hex)</P>
 *
 * <P> This iButton is primarily used as a read/write portable memory device with real-time-clock,
 *  timer and experation features. </P>
 *
 * <H3> Features </H3>
 * <UL>
 *   <LI> 4096 bits (512 bytes) of read/write nonvolatile memory
 *   <LI> 256-bit (32-byte) scratchpad ensures integrity of data
 *        transfer
 *   <LI> Memory partitioned into 256-bit (32-byte) pages for
 *        packetizing data
 *   <LI> Data integrity assured with strict read/write
 *        protocols
 *   <LI> Contains real time clock/calendar in binary
 *        format
 *   <LI> Interval timer can automatically accumulate
 *        time when power is applied
 *   <LI> Programmable cycle counter can accumulate
 *        the number of system power-on/off cycles
 *   <LI> Programmable alarms can be set to generate
 *        interrupts for interval timer, real time clock,
 *        and/or cycle counter
 *   <LI> Write protect feature provides tamper-proof
 *        time data
 *   <LI> Programmable expiration date that will limit
 *        access to SRAM and timekeeping
 *   <LI> Clock accuracy is better than ±2 minute/
 *        month at 25&#176C
 *   <LI> Operating temperature range from -40&#176C to
 *        +70&#176C
 *   <LI> Over 10 years of data retention
 * </UL>
 *
 * <P> Appended to the clock page data retrieved with 'readDevice'
 * are 4 bytes that represent a bitmap
 * of changed bytes.  These bytes are used in the 'writeDevice' method
 * in conjuction with the 'set' methods to only write back the changed
 * clock register bytes.  The 'readDevice' method will clear any
 * pending alarms. </P>
 *
 * <P> WARNING: If write-protect alarm options have been set prior
 * to a call to 'writeDevice' then the operation is non-reversable. </P>
 *
 * <H3> Alternate Names </H3>
 * <UL>
 *   <LI> D2504
 *   <LI> D1427 (family type 84 hex)
 * </UL>
 *
 * <H3> Memory </H3>
 *
 * <P> The memory can be accessed through the objects that are returned
 * from the {@link #getMemoryBanks() getMemoryBanks} method. </P>
 *
 * The following is a list of the MemoryBank instances that are returned:
 *
 * <UL>
 *   <LI> <B> Scratchpad </B>
 *      <UL>
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 *                   {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 32 starting at physical address 0
 *         <LI> <I> Features</I> Read/Write not-general-purpose volatile
 *         <LI> <I> Pages</I> 1 pages of length 32 bytes
 *         <LI> <I> Extra information for each page</I>  Target address, offset, length 3
 *      </UL>
 *   <LI> <B> Main Memory </B>
 *      <UL>
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 512 starting at physical address 0
 *         <LI> <I> Features</I> Read/Write general-purpose non-volatile
 *         <LI> <I> Pages</I> 16 pages of length 32 bytes giving 29 bytes Packet data payload
 *      </UL>
 *   <LI> <B> Clock/alarm registers </B>
 *      <UL>
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 32 starting at physical address 512
 *         <LI> <I> Features</I> Read/Write not-general-purpose non-volatile
 *         <LI> <I> Pages</I> 1 pages of length 32 bytes
 *      </UL>
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
 *           <LI> {@link #isClockWriteProtected(byte[]) isClockWriteProtected}
 *         </UL>
 *       <LI> <I> Interval Timer </I>
 *         <UL>
 *           <LI> {@link #getIntervalTimer(byte[]) getIntervalTimer}
 *           <LI> {@link #getIntervalTimerAlarm(byte[]) getIntervalTimerAlarm}
 *           <LI> {@link #isIntervalTimerAlarming(byte[]) isIntervalTimerAlarming}
 *           <LI> {@link #isIntervalTimerAlarmEnabled(byte[]) isIntervalTimerAlarmEnabled}
 *           <LI> {@link #isIntervalTimerWriteProtected(byte[]) isIntervalTimerWriteProtected}
 *           <LI> {@link #isIntervalTimerAutomatic(byte[]) isIntervalTimerAutomatic}
 *           <LI> {@link #isIntervalTimerStopped(byte[]) isIntervalTimerStopped}
 *         </UL>
 *       <LI> <I> Power Cycle Counter </I>
 *         <UL>
 *           <LI> {@link #getCycleCounter(byte[]) getCycleCounter}
 *           <LI> {@link #getCycleCounterAlarm(byte[]) getCycleCounterAlarm}
 *           <LI> {@link #isCycleCounterAlarming(byte[]) isCycleCounterAlarming}
 *           <LI> {@link #isCycleCounterAlarmEnabled(byte[]) isCycleCounterAlarmEnabled}
 *           <LI> {@link #isCycleCounterWriteProtected(byte[]) isCycleCounterWriteProtected}
 *         </UL>
 *       <LI> <I> Misc </I>
 *         <UL>
 *           <LI> {@link #readDevice() readDevice} *
 *           <LI> {@link #isAutomaticDelayLong(byte[]) isAutomaticDelayLong}
 *           <LI> {@link #canReadAfterExpire(byte[]) canReadAfterExpire}
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
 *           <LI> {@link #writeProtectClock(byte[]) writeProtectClock}
 *         </UL>
 *       <LI> <I> Interval Timer </I>
 *         <UL>
 *           <LI> {@link #setIntervalTimer(long, byte[]) setIntervalTimer}
 *           <LI> {@link #setIntervalTimerAlarm(long, byte[]) setIntervalTimerAlarm}
 *           <LI> {@link #writeProtectIntervalTimer(byte[]) writeProtectIntervalTimer}
 *           <LI> {@link #setIntervalTimerAutomatic(boolean, byte[]) setIntervalTimerAutomatic}
 *           <LI> {@link #setIntervalTimerRunState(boolean, byte[]) setIntervalTimerRunState}
 *           <LI> {@link #setIntervalTimerAlarmEnable(boolean, byte[]) setIntervalTimerAlarmEnable}
 *         </UL>
 *       <LI> <I> Power Cycle Counter </I>
 *         <UL>
 *           <LI> {@link #setCycleCounter(long, byte[]) setCycleCounter}
 *           <LI> {@link #setCycleCounterAlarm(long, byte[]) setCycleCounterAlarm}
 *           <LI> {@link #writeProtectCycleCounter(byte[]) writeProtectCycleCounter}
 *           <LI> {@link #setCycleCounterAlarmEnable(boolean, byte[]) setCycleCounterAlarmEnable}
 *         </UL>
 *       <LI> <I> Misc </I>
 *         <UL>
 *           <LI> {@link #writeDevice(byte[]) writeDevice} *
 *           <LI> {@link #setReadAfterExpire(boolean, byte[]) setReadAfterExpire}
 *           <LI> {@link #setAutomaticDelayLong(boolean, byte[]) setAutomaticDelayLong}
 *         </UL>
 *     </UL>
 *  </UL>
 *
 * <H3> Usage </H3>
 *
 * <DL>
 * <DD> See the usage examples in
 * {@link com.dalsemi.onewire.container.ClockContainer ClockContainer}
 * for basic clock operations.
 * <DD> See the usage example in
 * {@link com.dalsemi.onewire.container.OneWireContainer OneWireContainer}
 * to enumerate the MemoryBanks.
 * <DD> See the usage examples in
 * {@link com.dalsemi.onewire.container.MemoryBank MemoryBank} and
 * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 * for bank specific operations.
 * </DL>
 *
 * <H3> DataSheets </H3>
 * <DL>
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS1992-DS1994.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS1992-DS1994.pdf</A>
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS2404.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS2404.pdf</A>
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS1427.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS1427.pdf</A>
 * </DL>
 *
 * @see com.dalsemi.onewire.container.MemoryBank
 * @see com.dalsemi.onewire.container.PagedMemoryBank
 * @see com.dalsemi.onewire.container.ClockContainer
 *
 * @version    0.00, 28 Aug 2000
 * @author     DS
 */
public class OneWireContainer04
   extends OneWireContainer
   implements ClockContainer
{

   //--------
   //-------- Finals
   //--------

   /** Offset of BITMAP in array returned from read registers */
   protected static final int BITMAP_OFFSET = 32;

   /** Offset of status register from read registers */
   protected static final int STATUS_OFFSET = 0;

   /** Offset of control register from read registers */
   protected static final int CONTROL_OFFSET = 1;

   /** Offset of real-time-clock in array returned from read registers */
   protected static final int RTC_OFFSET = 2;

   /** Offset of inverval-counter in array returned from read registers */
   protected static final int INTERVAL_OFFSET = 7;

   /** Offset of counter in array returned from read registers */
   protected static final int COUNTER_OFFSET = 12;

   /** Offset of real-time-clock-alarm in array returned from read registers */
   protected static final int RTC_ALARM_OFFSET = 16;

   /** Offset of inverval-counter-alarm in array returned from read registers */
   protected static final int INTERVAL_ALARM_OFFSET = 21;

   /** Offset of counter-alarm in array returned from read registers */
   protected static final int COUNTER_ALARM_OFFSET = 26;

   //--------
   //-------- Variables
   //--------

   /**
    * Scratchpad access memory bank
    */
   private MemoryBankScratch scratch;

   /**
    * Clock memory access memory bank
    */
   private MemoryBankNV clock;

   //--------
   //-------- Constructors
   //--------

   /**
    * Create an empty container that is not complete until after a call
    * to <code>setupContainer</code>. <p>
    *
    * This is one of the methods to construct a container.  The others are
    * through creating a OneWireContainer with parameters.
    *
    * @see #setupContainer(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) super.setupContainer()
    */
   public OneWireContainer04 ()
   {
      super();

      // initialize the clock memory bank
      initClock();
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
    * @see #OneWireContainer04() OneWireContainer04
    * @see com.dalsemi.onewire.utils.Address utils.Address
    */
   public OneWireContainer04 (DSPortAdapter sourceAdapter, byte[] newAddress)
   {
      super(sourceAdapter, newAddress);

      // initialize the clock memory bank
      initClock();
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
    * @see #OneWireContainer04() OneWireContainer04
    * @see com.dalsemi.onewire.utils.Address utils.Address
    */
   public OneWireContainer04 (DSPortAdapter sourceAdapter, long newAddress)
   {
      super(sourceAdapter, newAddress);

      // initialize the clock memory bank
      initClock();
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
    * @see #OneWireContainer04() OneWireContainer04
    * @see com.dalsemi.onewire.utils.Address utils.Address
    */
   public OneWireContainer04 (DSPortAdapter sourceAdapter, String newAddress)
   {
      super(sourceAdapter, newAddress);

      // initialize the clock memory bank
      initClock();
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
      return "DS1994";
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
      return "DS2404, Time-in-a-can, DS1427";
   }

   /**
    * Get a short description of the function of this iButton
    * or 1-Wire Device type.
    *
    * @return device description
    */
   public String getDescription ()
   {
      return "4096 bit read/write nonvolatile memory partitioned "
             + "into sixteen pages of 256 bits each and a real "
             + "time clock/calendar in binary format.";
   }

   /**
    * Get an enumeration of memory bank instances that implement one or more
    * of the following interfaces:
    * {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
    * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank},
    * and {@link com.dalsemi.onewire.container.OTPMemoryBank OTPMemoryBank}.
    * @return <CODE>Enumeration</CODE> of memory banks
    */
   public Enumeration getMemoryBanks ()
   {
      Vector bank_vector = new Vector(3);

      // scratchpad
      bank_vector.addElement(scratch);

      // NVRAM
      bank_vector.addElement(new MemoryBankNV(this, scratch));

      // clock
      bank_vector.addElement(clock);

      return bank_vector.elements();
   }

   //--------
   //-------- Clock Feature methods
   //--------

   /**
    * Query to see if the clock has an alarm feature.
    *
    * @return true if the Real-Time clock has an alarm
    *
    * @see #getClockAlarm(byte[])
    * @see #isClockAlarmEnabled(byte[])
    * @see #isClockAlarming(byte[])
    * @see #setClockAlarm(long,byte[])
    * @see #setClockAlarmEnable(boolean,byte[])
    */
   public boolean hasClockAlarm ()
   {
      return true;
   }

   /**
    * Query to see if the clock can be disabled.
    *
    * @return true if the clock can be enabled and disabled
    *
    * @see #isClockRunning(byte[])
    * @see #setClockRunEnable(boolean,byte[])
    */
   public boolean canDisableClock ()
   {
      return true;
   }

   /**
    * Query to get the clock resolution in milliseconds
    *
    * @return the clock resolution in milliseconds
    */
   public long getClockResolution ()
   {
      return 4;
   }

   //--------
   //-------- Clock IO Methods
   //--------

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
      byte[][] read_buf = new byte [2][36];
      boolean  alarming;
      int      buf_num = 0, attempt = 0, i;

      // put zero's in the bitmap of changed bytes
      for (i = 32; i < 36; i++)
      {
         read_buf [0][i] = 0;
         read_buf [1][i] = 0;
      }

      // check if device alarming
      alarming = isAlarming();

      // loop up to 5 times to read clock register page
      do
      {

         // only read status byte once if device was alarming (will be cleared)
         if (alarming && (attempt != 0))
            clock.read(1, false, read_buf [buf_num], 1, 31);
         else
            clock.read(0, false, read_buf [buf_num], 0, 32);

         // compare if this is not the first read
         if (attempt++ != 0)
         {

            // loop to see if same
            for (i = 1; i < 32; i++)
            {
               if ((i != 2) && (i != 7))
               {
                  if (read_buf [0][i] != read_buf [1][i])
                     break;
               }
            }

            // check on compare, if ok then return most recent read_buf
            if (i == 32)
               return read_buf [buf_num];
         }

         // alternate buffer
         buf_num = (buf_num == 0) ? 1
                                  : 0;
      }
      while (attempt < 5);

      // failed to get a match
      throw new OneWireIOException("Failed to read the clock register page");
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
   public void writeDevice(byte[] state)
      throws OneWireIOException, OneWireException
   {
      int     start_offset = 0, len = 0, i;
      boolean got_block = false;

      // loop to collect changed bytes and write them in blocks
      for (i = 0; i < 32; i++)
      {

         // check to see if this byte needs writing (skip control register for now)
         if ((Bit.arrayReadBit(i, BITMAP_OFFSET, state) == 1) && (i != 1))
         {

            // check if already in a block
            if (got_block)
               len++;

               // new block
            else
            {
               got_block    = true;
               start_offset = i;
               len          = 1;
            }

            // check for last byte exception, write current block
            if (i == 31)
               clock.write(start_offset, state, start_offset, len);
         }
         else if (got_block)
         {

            // done with this block so write it
            clock.write(start_offset, state, start_offset, len);

            got_block = false;
         }
      }

      // check if need to write control register
      if (Bit.arrayReadBit(CONTROL_OFFSET, BITMAP_OFFSET, state) == 1)
      {

         // write normaly
         clock.write(CONTROL_OFFSET, state, CONTROL_OFFSET, 1);

         // check if any write-protect bits set
         if ((state [CONTROL_OFFSET] & 0x07) != 0)
         {

            // need to do a copy scratchpad 2 more times to become write-protected
            for (i = 0; i < 2; i++)
               scratch.writeScratchpad(clock.getStartPhysicalAddress()
                                       + CONTROL_OFFSET, state,
                                                         CONTROL_OFFSET, 1);
         }
      }

      // clear out the bitmap
      for (i = BITMAP_OFFSET; i < state.length; i++)
         state [i] = 0;
   }

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
   public long getClock (byte[] state)
   {
      return Convert.toLong(state, RTC_OFFSET, 5) * 1000 / 256;
   }

   /**
    * Extracts the clock alarm value for the Real-Time clock.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return milliseconds since 1970 that the clock alarm is set to
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
      throws OneWireException
   {
      return Convert.toLong(state, RTC_ALARM_OFFSET, 5) * 1000 / 256;
   }

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
   public boolean isClockAlarming (byte[] state)
   {
      return (Bit.arrayReadBit(0, STATUS_OFFSET, state) == 1);
   }

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
   public boolean isClockAlarmEnabled (byte[] state)
   {
      return (Bit.arrayReadBit(3, STATUS_OFFSET, state) == 0);
   }

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
   public boolean isClockRunning (byte[] state)
   {
      return (Bit.arrayReadBit(4, CONTROL_OFFSET, state) == 1);
   }

   //--------
   //-------- DS1994 Specific Clock 'get' Methods
   //--------

   /**
    * Get the Interval Timer Value in milliseconds.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return time in milliseconds
    * that have occured since the interval counter was started
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #setIntervalTimer(long,byte[])
    */
   public long getIntervalTimer (byte[] state)
   {
      return Convert.toLong(state, INTERVAL_OFFSET, 5) * 1000 / 256;
   }

   /**
    * Get the power cycle count value.  This is the total number of
    * power cycles that the DS1994 has seen since the counter was reset.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return power cycle count
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #setCycleCounter(long,byte[]) setCycleCounter
    */
   public long getCycleCounter (byte[] state)
   {
      return Convert.toLong(state, COUNTER_OFFSET, 4);
   }

   /**
    * Get the Interval Timer Alarm Value.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return time in milliseconds that have
    * the interval timer alarm is set to
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #setIntervalTimerAlarm(long,byte[]) setIntervalTimerAlarm
    */
   public long getIntervalTimerAlarm (byte[] state)
   {
      return Convert.toLong(state, INTERVAL_ALARM_OFFSET, 5) * 1000 / 256;
   }

   /**
    * Get the cycle count Alarm Value.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return total number of power cycles
    * that the DS1994 has to to see before the alarm will be triggered
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #setCycleCounterAlarm(long,byte[]) setCycleCounterAlarm
    */
   public long getCycleCounterAlarm (byte[] state)
   {
      return Convert.toLong(state, COUNTER_ALARM_OFFSET, 4);
   }

   /**
    * Check if the Interval Timer Alarm flag has been set.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return <CODE>true</CODE> if interval timer is alarming
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #isIntervalTimerAlarmEnabled(byte[]) isIntervalTimerAlarmEnabled
    * @see #setIntervalTimerAlarmEnable(boolean,byte[]) setIntervalTimerAlarmEnable
    */
   public boolean isIntervalTimerAlarming (byte[] state)
   {
      return (Bit.arrayReadBit(1, STATUS_OFFSET, state) == 1);
   }

   /**
    * Check if the Cycle Alarm flag has been set.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return <code>true</code> if cycle counter is alarming
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #isCycleCounterAlarmEnabled(byte[]) isCycleCounterAlarmEnabled
    * @see #setCycleCounterAlarmEnable(boolean,byte[]) setCycleCounterAlarmEnable
    */
   public boolean isCycleCounterAlarming (byte[] state)
   {
      return (Bit.arrayReadBit(2, STATUS_OFFSET, state) == 1);
   }

   /**
    * Check if the Interval Timer Alarm is enabled.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return <code>true</code> if interval timer alarm is enabled
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #isIntervalTimerAlarming(byte[]) isIntervalTimerAlarming
    * @see #setIntervalTimerAlarmEnable(boolean,byte[]) setIntervalTimerAlarmEnable
    */
   public boolean isIntervalTimerAlarmEnabled (byte[] state)
   {
      return (Bit.arrayReadBit(4, STATUS_OFFSET, state) == 0);
   }

   /**
    * Check if the Cycle Alarm is enabled.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return <code>true</code> true if cycle counter alarm is enabled
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #isCycleCounterAlarming(byte[]) isCycleCounterAlarming
    * @see #setCycleCounterAlarmEnable(boolean,byte[]) setCycleCounterAlarmEnable
    */
   public boolean isCycleCounterAlarmEnabled (byte[] state)
   {
      return (Bit.arrayReadBit(5, STATUS_OFFSET, state) == 0);
   }

   /**
    * Check if the Real-Time clock/Alarm is write protected.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return <code>true</code> if real time clock/alarm is write
    * protected
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #writeProtectClock(byte[]) writeProtectClock
    */
   public boolean isClockWriteProtected (byte[] state)
   {
      return (Bit.arrayReadBit(0, CONTROL_OFFSET, state) == 1);
   }

   /**
    * Check if the Interval Timer and Interval Timer Alarm
    * register is write protected.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return <code>true</code> if interval timer and interval timer alarm is
    * write protected
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #writeProtectIntervalTimer(byte[]) writeProtectIntervalTimer
    */
   public boolean isIntervalTimerWriteProtected (byte[] state)
   {
      return (Bit.arrayReadBit(1, CONTROL_OFFSET, state) == 1);
   }

   /**
    * Check if the Cycle Counter and Alarm is write protected.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return <code>true</code> if cycle counter/alarm is write
    * protected
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #writeProtectCycleCounter(byte[]) writeProtectCycleCounter
    */
   public boolean isCycleCounterWriteProtected (byte[] state)
   {
      return (Bit.arrayReadBit(2, CONTROL_OFFSET, state) == 1);
   }

   /**
    * Check if the device can be read after a write protected
    * alarm has occured.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return <code>true</code> if the device can be read after a
    * write protected alarm has occured
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #setReadAfterExpire(boolean, byte[]) setReadAfterExpire
    */
   public boolean canReadAfterExpire (byte[] state)
   {
      return (Bit.arrayReadBit(3, CONTROL_OFFSET, state) == 1);
   }

   /**
    * Checks if the Interval timer is automatic or manual.  If it is
    * automatic then the interval counter will increment while the devices I/O line
    * is high after the delay select period has elapsed (either 3.5 or 123 ms, see
    * the isAutomaticDelayLong() method).
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return <code>true</code> if the interval timer is set to automatic
    * mode
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #setIntervalTimerAutomatic(boolean, byte[]) setIntervalTimerAutomatic
    */
   public boolean isIntervalTimerAutomatic (byte[] state)
   {
      return (Bit.arrayReadBit(5, CONTROL_OFFSET, state) == 1);
   }

   /**
    * Check if the Interval timer is stopped.  This only has meaning
    * if the interval timer is in manual mode (not <CODE>isIntervalTimerAutomatic</CODE>).
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return <code>true</code> if the interval timer is stopped
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #isIntervalTimerAutomatic(byte[]) isIntervalTimerAutomatic
    * @see #setIntervalTimerAutomatic(boolean, byte[]) setIntervalTimerAutomatic
    * @see #setIntervalTimerRunState(boolean, byte[]) setIntervalTimerRunState
    */
   public boolean isIntervalTimerStopped (byte[] state)
   {
      return (Bit.arrayReadBit(6, CONTROL_OFFSET, state) == 1);
   }

   /**
    * Checks if the automatic delay for the Inteval Timer and the Cycle
    * counter is either 3.5ms (regular) or 123ms (long).
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return <code>true</code> if the automatic interval/cycle counter
    * delay is in the long (123ms) mode, else it is 3.5ms
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #setAutomaticDelayLong(boolean,byte[]) setAutomaticDelayLong
    */
   public boolean isAutomaticDelayLong (byte[] state)
   {
      return (Bit.arrayReadBit(7, CONTROL_OFFSET, state) == 1);
   }

   //--------
   //-------- Clock 'set' Methods
   //--------

   /**
    * Sets the Real-Time clock.
    * The method <code>writeDevice(byte[])</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice(byte[])</code>.
    *
    * @param time new value for the Real-Time clock, in milliseconds
    * since January 1, 1970
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #getClock(byte[])
    */
   public void setClock (long time, byte[] state)
   {
      Convert.toByteArray(time * 256 / 1000, state, RTC_OFFSET, 5);

      // set bitmap field to indicate these clock registers were changed
      for (int i = 0; i < 5; i++)
         Bit.arrayWriteBit(1, RTC_OFFSET + i, BITMAP_OFFSET, state);
   }

   /**
    * Sets the clock alarm.
    * The method <code>writeDevice(byte[])</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice(byte[])</code>.  Also note that
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
      throws OneWireException
   {
      Convert.toByteArray(time * 256 / 1000, state, RTC_ALARM_OFFSET, 5);

      // set bitmap field to indicate these clock registers were changed
      for (int i = 0; i < 5; i++)
         Bit.arrayWriteBit(1, RTC_ALARM_OFFSET + i, BITMAP_OFFSET, state);
   }

   /**
    * Enables or disables the oscillator, turning the clock 'on' and 'off'.
    * The method <code>writeDevice(byte[])</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice(byte[])</code>.  Also note that
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
      throws OneWireException
   {
      Bit.arrayWriteBit(runEnable ? 1
                                  : 0, 4, CONTROL_OFFSET, state);

      // set bitmap field to indicate this clock register has changed
      Bit.arrayWriteBit(1, CONTROL_OFFSET, BITMAP_OFFSET, state);
   }

   /**
    * Enables or disables the clock alarm.
    * The method <code>writeDevice(byte[])</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice(byte[])</code>.  Also note that
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
      throws OneWireException
   {
      Bit.arrayWriteBit(alarmEnable ? 0
                                    : 1, 3, STATUS_OFFSET, state);

      // set bitmap field to indicate this clock register has changed
      Bit.arrayWriteBit(1, STATUS_OFFSET, BITMAP_OFFSET, state);
   }

   //--------
   //-------- DS1994 Specific Clock 'set' Methods
   //--------

   /**
    * Sets the Interval Timer.
    * The method <code>writeDevice(byte[])</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice(byte[])</code>.
    *
    * @param time interval in milliseconds to set (truncated to 1/256th of second)
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #getIntervalTimer(byte[]) getIntervalTimer
    */
   public void setIntervalTimer (long time, byte[] state)
   {
      Convert.toByteArray(time * 256 / 1000, state, INTERVAL_OFFSET, 5);

      // set bitmap field to indicate these clock registers were changed
      for (int i = 0; i < 5; i++)
         Bit.arrayWriteBit(1, INTERVAL_OFFSET + i, BITMAP_OFFSET, state);
   }

   /**
    * Sets power Cycle Counter.
    * The method <code>writeDevice(byte[])</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice(byte[])</code>.
    *
    * @param cycles initialize cycle counter value
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #getCycleCounter(byte[]) getCycleCounter
    */
   public void setCycleCounter (long cycles, byte[] state)
   {
      Convert.toByteArray(cycles, state, COUNTER_OFFSET, 4);

      // set bitmap field to indicate these clock registers were changed
      for (int i = 0; i < 4; i++)
         Bit.arrayWriteBit(1, COUNTER_OFFSET + i, BITMAP_OFFSET, state);
   }

   /**
    * Sets the Interval Timer Alarm.
    * The method <code>writeDevice(byte[])</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice(byte[])</code>.
    *
    * @param time in milliseconds to set the inverval timer
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #getIntervalTimerAlarm(byte[]) getIntervalTimerAlarm
    */
   public void setIntervalTimerAlarm (long time, byte[] state)
   {
      Convert.toByteArray(time * 256 / 1000, state, INTERVAL_ALARM_OFFSET, 5);

      // set bitmap field to indicate these clock registers were changed
      for (int i = 0; i < 5; i++)
         Bit.arrayWriteBit(1, INTERVAL_ALARM_OFFSET + i, BITMAP_OFFSET,
                           state);
   }

   /**
    * Sets the power Cycle Count Alarm. This counter holds the number
    * of times the DS1994 must experience power cycles
    * before it generates an alarm.
    * The method <code>writeDevice(byte[])</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice(byte[])</code>.
    *
    * @param cycles power Cycle Count alarm
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #getCycleCounterAlarm(byte[]) getCycleCounterAlarm
    */
   public void setCycleCounterAlarm (long cycles, byte[] state)
   {
      Convert.toByteArray(cycles, state, COUNTER_ALARM_OFFSET, 4);

      // set bitmap field to indicate these clock registers were changed
      for (int i = 0; i < 4; i++)
         Bit.arrayWriteBit(1, COUNTER_ALARM_OFFSET + i, BITMAP_OFFSET, state);
   }

   /**
    * Sets the write protect options for the Real-Time
    * clock/Alarm.
    * The method <code>writeDevice(byte[])</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice(byte[])</code>.
    *
    * <P>WARNING: after calling this method and then
    * <CODE> writeDevice </CODE> the device will be permanently write
    * protected. </P> <BR>
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #isClockWriteProtected(byte[]) isClockWriteProtected
    */
   public void writeProtectClock (byte[] state)
   {
      Bit.arrayWriteBit(1, 0, CONTROL_OFFSET, state);

      // set bitmap field to indicate this clock register has changed
      Bit.arrayWriteBit(1, CONTROL_OFFSET, BITMAP_OFFSET, state);
   }

   /**
    * Sets the write protect options for Interval Timer and
    * Interval Timer Alarm register.
    * The method <code>writeDevice(byte[])</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice(byte[])</code>.
    *
    * <P>WARNING: after calling this method and then
    * <CODE> writeDevice </CODE> the device will be permanently write
    * protected. </P> <BR>
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #isIntervalTimerWriteProtected(byte[]) isIntervalTimerWriteProtected
    */
   public void writeProtectIntervalTimer (byte[] state)
   {
      Bit.arrayWriteBit(1, 1, CONTROL_OFFSET, state);

      // set bitmap field to indicate this clock register has changed
      Bit.arrayWriteBit(1, CONTROL_OFFSET, BITMAP_OFFSET, state);
   }

   /**
    * Sets the write protect options for the Cycle Counter
    * and Alarm register.
    * The method <code>writeDevice(byte[])</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice(byte[])</code>.
    *
    * <P>WARNING: after calling this method and then
    * <CODE> writeDevice </CODE> the device will be permanently write
    * protected. </P> <BR>
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #isCycleCounterWriteProtected(byte[]) isCycleCounterWriteProtected
    */
   public void writeProtectCycleCounter (byte[] state)
   {
      Bit.arrayWriteBit(1, 2, CONTROL_OFFSET, state);

      // set bitmap field to indicate this clock register has changed
      Bit.arrayWriteBit(1, CONTROL_OFFSET, BITMAP_OFFSET, state);
   }

   /**
    * Sets the read state of the device after a
    * write protected alarm has occured.
    * The method <code>writeDevice(byte[])</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice(byte[])</code>.
    *
    * @param  readAfter <CODE>true</CODE> to read device after it
    *                 expires from a write protected alarm event
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #canReadAfterExpire(byte[]) canReadAfterExpire
    */
   public void setReadAfterExpire (boolean readAfter, byte[] state)
   {
      Bit.arrayWriteBit(readAfter ? 1
                                  : 0, 3, CONTROL_OFFSET, state);

      // set bitmap field to indicate this clock register has changed
      Bit.arrayWriteBit(1, CONTROL_OFFSET, BITMAP_OFFSET, state);
   }

   /**
    * Sets the Interval timer to automatic or manual mode.
    * When in automatic mode, the interval counter will increment
    * while the devices I/O line is high after the delay select
    * period has elapsed (either 3.5 or 123 ms, see the
    * <CODE>isAutomaticDelayLong()</CODE> method).
    * The method <code>writeDevice(byte[])</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice(byte[])</code>.
    *
    * @param  autoTimer <CODE>true</CODE> for the interval timer to operate in
    *                 automatic mode
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #isIntervalTimerAutomatic(byte[]) isIntervalTimerAutomatic
    */
   public void setIntervalTimerAutomatic (boolean autoTimer, byte[] state)
   {
      Bit.arrayWriteBit(autoTimer ? 1
                                  : 0, 5, CONTROL_OFFSET, state);

      // set bitmap field to indicate this clock register has changed
      Bit.arrayWriteBit(1, CONTROL_OFFSET, BITMAP_OFFSET, state);
   }

   /**
    * Sets the Interval timer run/stop mode.  This only
    * has meaning if the interval timer is in manual mode
    * (not <CODE>isIntervalTimerAutomatic()</CODE>).
    * The method <code>writeDevice(byte[])</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice(byte[])</code>.
    *
    * @param runState <CODE>true</CODE> to set the interval timer to run
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #isIntervalTimerAutomatic(byte[]) isIntervalTimerAutomatic
    * @see #isIntervalTimerStopped(byte[]) isIntervalTimerStopped
    */
   public void setIntervalTimerRunState (boolean runState, byte[] state)
   {
      Bit.arrayWriteBit(runState ? 1
                                 : 0, 6, CONTROL_OFFSET, state);

      // set bitmap field to indicate this clock register has changed
      Bit.arrayWriteBit(1, CONTROL_OFFSET, BITMAP_OFFSET, state);
   }

   /**
    * Sets the automatic delay for the Inteval Timer and the Cycle
    * counter to either 123ms (long) or 3.5ms (regular).
    * The method <code>writeDevice(byte[])</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice(byte[])</code>.
    *
    * @param delayLong <CODE>true</CODE> to set the interval timer to
    *                 cycle counter to increment after 123ms or <CODE>false</CODE>
    *                 for 3.5ms
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #isAutomaticDelayLong(byte[]) isAutomaticDelayLong
    */
   public void setAutomaticDelayLong (boolean delayLong, byte[] state)
   {
      Bit.arrayWriteBit(delayLong ? 1
                                  : 0, 7, CONTROL_OFFSET, state);

      // set bitmap field to indicate this clock register has changed
      Bit.arrayWriteBit(1, CONTROL_OFFSET, BITMAP_OFFSET, state);
   }

   /**
    * Sets the Interval Timer Alarm enable.
    * The method <code>writeDevice(byte[])</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice(byte[])</code>.
    *
    * @param alarmEnable <CODE>true</CODE> to enable the interval timer alarm
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #isIntervalTimerAlarmEnabled(byte[]) isIntervalTimerAlarmEnabled
    */
   public void setIntervalTimerAlarmEnable (boolean alarmEnable, byte[] state)
   {
      Bit.arrayWriteBit(alarmEnable ? 0
                                    : 1, 4, STATUS_OFFSET, state);

      // set bitmap field to indicate this clock register has changed
      Bit.arrayWriteBit(1, STATUS_OFFSET, BITMAP_OFFSET, state);
   }

   /**
    * Sets the Cycle counter Alarm enable.
    * The method <code>writeDevice(byte[])</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice(byte[])</code>.
    *
    * @param  alarmEnable <CODE>true</CODE> to enable the cycle counter alarm
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #isCycleCounterAlarmEnabled(byte[]) isCycleCounterAlarmEnabled
    */
   public void setCycleCounterAlarmEnable (boolean alarmEnable, byte[] state)
   {
      Bit.arrayWriteBit(alarmEnable ? 0
                                    : 1, 5, STATUS_OFFSET, state);

      // set bitmap field to indicate this clock register has changed
      Bit.arrayWriteBit(1, STATUS_OFFSET, BITMAP_OFFSET, state);
   }

   //--------
   //-------- Private
   //--------

   /**
    * Create the memory bank interface to read/write the clock
    */
   private void initClock ()
   {

      // scratchpad
      scratch = new MemoryBankScratch(this);

      // clock
      clock                      = new MemoryBankNV(this, scratch);
      clock.numberPages          = 1;
      clock.startPhysicalAddress = 512;
      clock.size                 = 32;
      clock.generalPurposeMemory = false;
      clock.maxPacketDataLength  = 0;
      clock.bankDescription      = "Clock/alarm registers";
   }
}
