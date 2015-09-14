
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

import com.dalsemi.onewire.utils.Convert;
import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import java.util.*;

/**
 * <P> 1-Wire&#174 container for a Thermochron iButton, DS1921.
 * This container encapsulates the functionality of the 1-Wire family type <B>21</B> (hex).
 * </P>
 *
 * <H3> Features </H3>
 * <UL>
 *   <LI> Logs up to 2048 consecutive temperature measurements in nonvolatile, read-only memory
 *   <li> Real-Time clock with programmable alarm
 *   <LI> Programmable high and low temperature alarms
 *   <li> Alarm violation times and durations recorded in nonvolatile, read-only memory
 *   <li> Automatically 'wakes up' and logs temperature at user-programmable intervals
 *   <li> 4096 bits of general-purpose read/write nonvolatile memory
 *   <li> 256-bit scratchpad ensures integrity of data transfer
 *   <li> On-chip 16-bit CRC generator to verify read operations
 *   <li> Long-term histogram with 2&#176 C resolution
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
 *   <LI> <B> Scratchpad with CRC </B>
 *      <UL>
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 32 starting at physical address 0
 *         <LI> <I> Features</I> Read/Write not-general-purpose volatile
 *         <LI> <I> Pages</I> 1 page of length 32 bytes
 *         <LI> <I> Page Features </I> page-device-CRC
 *         <li> <i> Extra information for each page</i>  Target address, offset, length 3
 *      </UL>
 *   <LI> <B> Main Memory </B>
 *      <UL>
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 512 starting at physical address 0
 *         <LI> <I> Features</I> Read/Write general-purpose non-volatile
 *         <LI> <I> Pages</I> 16 pages of length 32 bytes giving 29 bytes Packet data payload
 *         <LI> <I> Page Features </I> page-device-CRC
 *      </UL>
 *   <LI> <B> Register control </B>
 *      <UL>
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 32 starting at physical address 512
 *         <LI> <I> Features</I> Read/Write not-general-purpose non-volatile
 *         <LI> <I> Pages</I> 1 pages of length 32 bytes
 *         <LI> <I> Page Features </I> page-device-CRC
 *      </UL>
 *   <LI> <B> Alarm time stamps </B>
 *      <UL>
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 96 starting at physical address 544
 *         <LI> <I> Features</I> Read-only not-general-purpose non-volatile
 *         <LI> <I> Pages</I> 3 pages of length 32 bytes
 *         <LI> <I> Page Features </I> page-device-CRC
 *      </UL>
 *   <LI> <B> Temperature histogram </B>
 *      <UL>
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 128 starting at physical address 2048
 *         <LI> <I> Features</I> Read-only not-general-purpose non-volatile
 *         <LI> <I> Pages</I> 4 pages of length 32 bytes
 *         <LI> <I> Page Features </I> page-device-CRC
 *      </UL>
 *   <LI> <B> Temperature log </B>
 *      <UL>
 *         <LI> <I> Implements </I> {@link com.dalsemi.onewire.container.MemoryBank MemoryBank},
 *                  {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 *         <LI> <I> Size </I> 2048 starting at physical address 4096
 *         <LI> <I> Features</I> Read-only not-general-purpose non-volatile
 *         <LI> <I> Pages</I> 64 pages of length 32 bytes
 *         <LI> <I> Page Features </I> page-device-CRC
 *      </UL>
 * </UL>
 *
 * <H3> Usage </H3>
 *
 * <p>The code below starts a mission with the following characteristics:
 * <ul>
 *     <li>Rollover flag enabled.  This means if more than 2048 samples are
 *         taken, the newer samples overwrite the oldest samples in the temperature
 *         log.</li>
 *     <li>High alarm of 28.0&#176 and a low alarm of 23.0&#176 C.  If the alarm is violated,
 *         the Temperature Alarm log will record when and for how long the violation occurred.</li>
 *     <li>The clock alarm enabled to Mondays at 12:30:45 pm.</li>
 *     <li>Sets the Thermocron's Real-Time Clock to the host system's clock.</li>
 *     <li>The mission will start in 2 minutes.</li>
 *     <li>A sample rate of 1 minute.</li>
 * </ul>
 * This code also ensures that the Thermocron's clock is set to run, and that the
 * clock alarm is enabled.</p>
 * <pre><code>
 *       // "ID" is a byte array of size 8 with an address of a part we
 *       // have already found with family code 12 hex
 *       // "access" is a DSPortAdapter
 *       OneWireContainer21 ds1921 = (OneWireContainer21) access.getDeviceContainer(ID);
 *       ds1921.setupContainer(access,ID);
 *       //  ds1921 previously setup as a OneWireContainer21
 *       ds1921.clearMemory();
 *       //  read the current state of the device
 *       byte[] state = ds1921.readDevice();
 *       //  enable rollover
 *       ds1921.setFlag(ds1921.CONTROL_REGISTER, ds1921.ROLLOVER_ENABLE_FLAG, true, state);
 *       //  set the high temperature alarm to 28 C
 *       ds1921.setTemperatureAlarm(ds1921.ALARM_HIGH, 28.0, state);
 *       //  set the low temperature alarm to 23 C
 *       ds1921.setTemperatureAlarm(ds1921.ALARM_LOW, 23.0, state);
 *       //  set the clock alarm to occur weekly, Mondays at 12:30:45 pm
 *       ds1921.setClockAlarm(12, 30, 45, 2, ds1921.ONCE_PER_WEEK, state);
 *       //  set the real time clock to the system's current clock
 *       ds1921.setClock(System.currentTimeMillis(), state);
 *       //  set the mission to start in 2 minutes
 *       ds1921.setMissionStartDelay(2,state);
 *       //  make sure the clock is set to run
 *       ds1921.setClockRunEnable(true, state);
 *       //  make sure the clock alarm is enabled
 *       ds1921.setClockAlarmEnable(true, state);
 *       //  write all that information out
 *       ds1921.writeDevice(state);
 *       //  now enable the mission with a sample rate of 1 minute
 *       ds1921.enableMission(1);
 * </code></pre>
 *
 * <p>The following code processes the temperature log:</p>
 * <code><pre>
 *       byte[] state = ds1921.readDevice();
 *       byte[] log = ds1921.getTemperatureLog(state);
 *       Calendar time_stamp = ds1921.getMissionTimeStamp(state);
 *       long time = time_stamp.getTime().getTime() + ds1921.getFirstLogOffset(state);
 *       int sample_rate = ds1921.getSampleRate(state);
 *
 *       System.out.println("TEMPERATURE LOG");
 *
 *       for (int i=0;i &lt; log.length;i++)
 *       {
 *           System.out.println("- Temperature recorded at  : "+(new Date(time)));
 *           System.out.println("-                     was  : "+ds1921.decodeTemperature(log[i])+" C");
 *           time += sample_rate * 60 * 1000;
 *       }
 * </pre></code>
 *
 * <p>The following code processes the alarm histories:</p>
 * <code><pre>
 *       byte[] high_history = ds1921.getAlarmHistory(ds1921.TEMPERATURE_HIGH_ALARM);
 *       byte[] low_history = ds1921.getAlarmHistory(ds1921.TEMPERATURE_LOW_ALARM);
 *       int sample_rate = ds1921.getSampleRate(state);
 *       int start_offset, violation_count;
 *       System.out.println("ALARM HISTORY");
 *       if (low_history.length==0)
 *       {
 *           System.out.println("- No violations against the low temperature alarm.");
 *           System.out.println("-");
 *       }
 *       for (int i=0;i &lt; low_history.length/4; i++)
 *       {
 *           start_offset = (low_history [i * 4] & 0x0ff)
 *                     | ((low_history [i * 4 + 1] &lt;&lt; 8) & 0x0ff00)
 *                     | ((low_history [i * 4 + 2] &lt;&lt; 16) & 0x0ff0000);
 *           violation_count = 0x0ff & low_history[i*4+3];
 *           System.out.println("- Low alarm started at     : "+(start_offset * sample_rate));
 *           System.out.println("-                          : Lasted "+(violation_count * sample_rate)+" minutes");
 *       }
 *       if (high_history.length==0)
 *       {
 *           System.out.println("- No violations against the high temperature alarm.");
 *           System.out.println("-");
 *       }
 *       for (int i=0;i &lt; high_history.length/4; i++)
 *       {
 *           start_offset = (high_history [i * 4] & 0x0ff)
 *                     | ((high_history [i * 4 + 1] &lt;&lt; 8) & 0x0ff00)
 *                     | ((high_history [i * 4 + 2] &lt;&lt; 16) & 0x0ff0000);
 *           violation_count = 0x0ff & high_history[i*4+3];
 *           System.out.println("- High alarm started at    : "+(start_offset * sample_rate));
 *           System.out.println("-                          : Lasted "+(violation_count * sample_rate)+" minutes");
 *       }
 * </pre></code>
 *
 * <p>The following code processes the temperature histogram:</p>
 * <code><pre>
 *       double resolution = ds1921.getTemperatureResolution();
 *       double histBinWidth = ds1921.getHistogramBinWidth();
 *       double start = ds1921.getHistogramLowTemperature();
 *       System.out.println("TEMPERATURE HISTOGRAM");
 *       for (int i=0;i &lt; histogram.length;i++)
 *       {
 *          System.out.println("- Histogram entry          : "
 *                             + histogram [i] + " at temperature "
 *                             + start + " to "
 *                             + ( start + (histBinWidth - resolution)) + " C");
 *          start += histBinWidth;
 *       }
 * </pre></code>
 *
 * <p>Also see the usage examples in the {@link com.dalsemi.onewire.container.TemperatureContainer TemperatureContainer}
 * and {@link com.dalsemi.onewire.container.ClockContainer ClockContainer}
 * interfaces.</p>
 *
 * For examples regarding memory operations,
 * <uL>
 * <li> See the usage example in
 * {@link com.dalsemi.onewire.container.OneWireContainer OneWireContainer}
 * to enumerate the MemoryBanks.
 * <li> See the usage examples in
 * {@link com.dalsemi.onewire.container.MemoryBank MemoryBank} and
 * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 * for bank specific operations.
 * </uL>
 *
 * <H3> DataSheet </H3>
 * <DL>
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS1921L-F5X.pdf">http://pdfserv.maxim-ic.com/arpdf/DS1921L-F5X.pdf</A>
 * </DL>
 *
 * Also visit <a href="http://www.ibutton.com/ibuttons/thermochron.html">
 * http://www.ibutton.com/ibuttons/thermochron.html</a> for links to more
 * sources on the DS1921 Thermocron.
 *
 * @see com.dalsemi.onewire.container.OneWireSensor
 * @see com.dalsemi.onewire.container.SwitchContainer
 * @see com.dalsemi.onewire.container.TemperatureContainer
 *
 * @version    0.00, 28 Aug 2000
 * @author     COlmstea, KLA
 *
 */
public class OneWireContainer21
   extends OneWireContainer
   implements TemperatureContainer, ClockContainer
{
   private static final byte FAMILY_CODE = (byte)0x21;
   private boolean    doSpeedEnable = true;

   /* privates!
    * Memory commands.
    */
   private static final byte WRITE_SCRATCHPAD_COMMAND = (byte)0x0F;
   private static final byte READ_SCRATCHPAD_COMMAND = (byte)0xAA;
   private static final byte COPY_SCRATCHPAD_COMMAND = (byte)0x55;
   private static final byte READ_MEMORY_CRC_COMMAND = (byte)0xA5;
   private static final byte CLEAR_MEMORY_COMMAND = (byte)0x3C;
   private static final byte CONVERT_TEMPERATURE_COMMAND = ( byte ) 0x44;

   // Scratchpad access memory bank
   private MemoryBankScratchCRC scratch;

   // Register control memory bank
   private MemoryBankNVCRC register;

   // Alarms memory bank
   private MemoryBankNVCRC alarm;

   // Histogram memory bank
   private MemoryBankNVCRC histogram;

   // Log memory bank
   private MemoryBankNVCRC log;

   // Buffer to hold the temperature log in
   private byte[] read_log_buffer = new byte [64 * 32];   //64 pages X 32 bytes per page

   // should we update the Real time clock?
   private boolean updatertc = false;

   // Maxim/Dallas Semiconductor Part number
   private String partNumber = "DS1921";

   // Temperature range low temperaturein degrees Celsius
   // calculated through 12-bit field of 1-Wire Net Address
   private double temperatureRangeLow = -40.0;
   private double temperatureRangeHigh = 85.0;

   // Temperature range width in degrees Celsius
   // calculated through 12-bit field of 1-Wire Net Address
   //private double temperatureRangeWidth = 125.0;

   // Temperature resolution in degrees Celsius
   // calculated through 12-bit field of 1-Wire Net Address
   private double temperatureResolution = 0.5;

   // The temperature range at which the device will operate.
   private double temperatureOperatingRangeLow = -40.0;
   private double temperatureOperatingRangeHigh = 85.0;

   // Is this 1-Wire device a DS1921HZ?
   private boolean isDS1921HZ = false;


   /////////////////////////////////////////////
   //
   //PUBLIC's
   //
   /////////////////////////////////////////////

   /**
    * Address of the status register. Used with the <code>getFlag</code>
    * and <code>setFlag</code> methods to set and
    * check flags indicating the Thermochron's status.
    *
    * @see #getFlag(int,byte,byte[])
    * @see #getFlag(int,byte)
    * @see #setFlag(int,byte,boolean)
    * @see #setFlag(int,byte,boolean,byte[])
    */
   public static final int STATUS_REGISTER = 0x214;

   /**
    * Address of the control register. Used with the <code>getFlag</code>
    * and <code>setFlag</code> methods to set and
    * check flags indicating the Thermochron's status.
    *
    * @see #getFlag(int,byte,byte[])
    * @see #getFlag(int,byte)
    * @see #setFlag(int,byte,boolean)
    * @see #setFlag(int,byte,boolean,byte[])
    */
   public static final int CONTROL_REGISTER = 0x20E;

   /**
    * Alarm frequency setting for the <code>setClockAlarm()</code> method.
    * If the DS1921 Thermocron alarm is enabled and is not alarming,
    * it will alarm on the next Real-Time Clock second.
    *
    * @see #setClockAlarm(int,int,int,int,int,byte[])
    */
   public static final byte ONCE_PER_SECOND = ( byte ) 0x1F;

   /**
    * Alarm frequency setting for the <code>setClockAlarm()</code> method.
    * If the DS1921 Thermocron alarm is enabled and is not alarming,
    * it will alarm the next time the Real-Time Clock's 'second' value is
    * equal to the Alarm Clock's 'second' value.
    *
    * @see #setClockAlarm(int,int,int,int,int,byte[])
    */
   public static final byte ONCE_PER_MINUTE = ( byte ) 0x17;

   /**
    * Alarm frequency setting for the <code>setClockAlarm()</code> method.
    * If the DS1921 Thermocron alarm is enabled and is not alarming,
    * it will alarm the next time the Real-Time Clock's 'second' and 'minute' values are
    * equal to the Alarm Clock's 'second' and 'minute' values.
    *
    * @see #setClockAlarm(int,int,int,int,int,byte[])
    */
   public static final byte ONCE_PER_HOUR = ( byte ) 0x13;

   /**
    * Alarm frequency setting for the <code>setClockAlarm()</code> method.
    * If the DS1921 Thermocron alarm is enabled and is not alarming,
    * it will alarm the next time the Real-Time Clock's 'second', 'minute', and 'hour' values are
    * equal to the Alarm Clock's 'second', 'minute', and 'hour' values.
    *
    * @see #setClockAlarm(int,int,int,int,int,byte[])
    */
   public static final byte ONCE_PER_DAY = ( byte ) 0x11;

   /**
    * Alarm frequency setting for the <code>setClockAlarm()</code> method.
    * If the DS1921 Thermocron alarm is enabled and is not alarming,
    * it will alarm the next time the Real-Time Clock's 'second', 'minute', 'hour', and 'day of week' values are
    * equal to the Alarm Clock's 'second', 'minute', 'hour', and 'day of week' values
    *
    * @see #setClockAlarm(int,int,int,int,int,byte[])
    */
   public static final byte ONCE_PER_WEEK = ( byte ) 0x10;

   /**
    * Low temperature alarm value for the methods <code>getAlarmStatus()</code>,
    * <code>getAlarmHistory()</code>, and <code>setTemperatureAlarm()</code>.
    *
    * @see #getAlarmStatus(byte,byte[])
    * @see #getAlarmHistory(byte)
    * @see #setTemperatureAlarm(int,double,byte[])
    */
   public static final byte TEMPERATURE_LOW_ALARM = 4;

   /**
    * High temperature alarm value for the methods <code>getAlarmStatus()</code>,
    * <code>getAlarmHistory()</code>, and <code>setTemperatureAlarm()</code>.
    *
    * @see #getAlarmStatus(byte,byte[])
    * @see #getAlarmHistory(byte)
    * @see #setTemperatureAlarm(int,double,byte[])
    */
   public static final byte TEMPERATURE_HIGH_ALARM = 2;

   /**
    * Clock alarm value for the methods <code>getAlarmStatus()</code>
    * and <code>isClockAlarming()</code>.
    *
    * @see #getAlarmStatus(byte,byte[])
    * @see #isClockAlarming(byte[])
    */
   public static final byte TIMER_ALARM = 1;

   /**
    * CONTROL REGISTER FLAG: When enabled, the device will respond to conditional
    * search command if a timer alarm has occurred.
    *
    * @see #getFlag(int,byte,byte[])
    * @see #getFlag(int,byte)
    * @see #setFlag(int,byte,boolean)
    * @see #setFlag(int,byte,boolean,byte[])
    */
   public static final byte TIMER_ALARM_SEARCH_FLAG = 1;

   /**
    * CONTROL REGISTER FLAG: When enabled, the device will respond to conditional
    * search command if the temperature has reached the high temperature threshold.
    *
    * @see #getFlag(int,byte,byte[])
    * @see #getFlag(int,byte)
    * @see #setFlag(int,byte,boolean)
    * @see #setFlag(int,byte,boolean,byte[])
    */
   public static final byte TEMP_HIGH_SEARCH_FLAG = 2;

   /**
    * CONTROL REGISTER FLAG: When enabled, the device will respond to conditional
    * search command if the temperature has reached the low temperature threshold.
    *
    * @see #getFlag(int,byte,byte[])
    * @see #getFlag(int,byte)
    * @see #setFlag(int,byte,boolean)
    * @see #setFlag(int,byte,boolean,byte[])
    */
   public static final byte TEMP_LOW_SEARCH_FLAG = 4;

   /**
    * CONTROL REGISTER FLAG: When enabled, the device will begin overwriting the earlier
    * temperature measurements when the temperature log memory becomes full.
    *
    * @see #getFlag(int,byte,byte[])
    * @see #getFlag(int,byte)
    * @see #setFlag(int,byte,boolean)
    * @see #setFlag(int,byte,boolean,byte[])
    */
   public static final byte ROLLOVER_ENABLE_FLAG = 8;

   /**
    * CONTROL REGISTER FLAG: When DISABLED, the mission will start as soon as the
    * sample rate is written.
    *
    * @see #getFlag(int,byte,byte[])
    * @see #getFlag(int,byte)
    * @see #setFlag(int,byte,boolean)
    * @see #setFlag(int,byte,boolean,byte[])
    */
   public static final byte MISSION_ENABLE_FLAG = 16;

   /**
    * CONTROL REGISTER FLAG: Must be enabled to allow a clear memory
    * function. Must be set immediately before the command is issued.
    *
    * @see #clearMemory()
    * @see #getFlag(int,byte,byte[])
    * @see #getFlag(int,byte)
    * @see #setFlag(int,byte,boolean)
    * @see #setFlag(int,byte,boolean,byte[])
    */
   public static final byte MEMORY_CLEAR_ENABLE_FLAG = 64;

   /**
    * CONTROL REGISTER FLAG: When DISABLED, the real time clock will start
    * working. Must be disabled for normal operation.
    *
    * @see #getFlag(int,byte,byte[])
    * @see #getFlag(int,byte)
    * @see #setFlag(int,byte,boolean)
    * @see #setFlag(int,byte,boolean,byte[])
    */
   public static final byte OSCILLATOR_ENABLE_FLAG = ( byte ) 128;

   /**
    * STATUS REGISTER FLAG: Will read back true when a clock alarm has occurred.
    *
    * @see #getFlag(int,byte,byte[])
    * @see #getFlag(int,byte)
    */
   public static final byte TIMER_ALARM_FLAG = 1;

   /**
    * STATUS REGISTER FLAG:  Will read back true when the temperature during a mission
    * reaches or exceeds the temperature high threshold.
    *
    * @see #getFlag(int,byte,byte[])
    * @see #getFlag(int,byte)
    */
   public static final byte TEMPERATURE_HIGH_FLAG = 2;

   /**
    * STATUS REGISTER FLAG: Will read back true when a temperature equal to or below
    * the low temperature threshold was detected on a mission.
    *
    * @see #getFlag(int,byte,byte[])
    * @see #getFlag(int,byte)
    */
   public static final byte TEMPERATURE_LOW_FLAG = 4;

   /**
    * STATUS REGISTER FLAG: Will read back true when a mission temperature conversion
    * is in progress
    *
    * @see #getFlag(int,byte,byte[])
    * @see #getFlag(int,byte)
    */
   public static final byte SAMPLE_IN_PROGRESS_FLAG = 16;

   /**
    * STATUS REGISTER FLAG: Will read back true when a mission is in progress.
    *
    * @see #getFlag(int,byte,byte[])
    * @see #getFlag(int,byte)
    */
   public static final byte MISSION_IN_PROGRESS_FLAG = 32;

   /**
    * STATUS REGISTER FLAG: Will read back true if the memory has been cleared.
    *
    * @see #getFlag(int,byte,byte[])
    * @see #getFlag(int,byte)
    */
   public static final byte MEMORY_CLEARED_FLAG = 64;

   /**
    * STATUS REGISTER FLAG: Will read back true if a temperature conversion
    * of any kind is in progress.
    *
    * @see #getFlag(int,byte,byte[])
    * @see #getFlag(int,byte)
    */
   public static final byte TEMP_CORE_BUSY_FLAG = ( byte ) 128;

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS1921 Thermocron iButton.
    * Note that the method <code>setupContainer(DSPortAdapter,byte[])</code>
    * must be called to set the correct <code>DSPortAdapter</code> device address.
    *
    * @see com.dalsemi.onewire.container.OneWireContainer#setupContainer(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) setupContainer(DSPortAdapter,byte[])
    * @see #OneWireContainer21(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer21(DSPortAdapter,byte[])
    * @see #OneWireContainer21(com.dalsemi.onewire.adapter.DSPortAdapter,long)   OneWireContainer21(DSPortAdapter,long)
    * @see #OneWireContainer21(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer21(DSPortAdapter,String)
    */
   public OneWireContainer21 ()
   {
      super();

      // initialize the memory banks
      initMem();
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS1921 Thermocron iButton.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this iButton
    * @param  newAddress        address of this DS1921
    *
    * @see #OneWireContainer21()
    * @see #OneWireContainer21(com.dalsemi.onewire.adapter.DSPortAdapter,long)   OneWireContainer21(DSPortAdapter,long)
    * @see #OneWireContainer21(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer21(DSPortAdapter,String)
    */
   public OneWireContainer21 (DSPortAdapter sourceAdapter, byte[] newAddress)
   {
      super(sourceAdapter, newAddress);

      // initialize the memory banks
      initMem();
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS1921 Thermocron iButton.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this iButton
    * @param  newAddress        address of this DS1921
    *
    * @see #OneWireContainer21()
    * @see #OneWireContainer21(com.dalsemi.onewire.adapter.DSPortAdapter,byte[]) OneWireContainer21(DSPortAdapter,byte[])
    * @see #OneWireContainer21(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer21(DSPortAdapter,String)
    */
   public OneWireContainer21 (DSPortAdapter sourceAdapter, long newAddress)
   {
      super(sourceAdapter, newAddress);

      // initialize the memory banks
      initMem();
   }

   /**
    * Creates a new <code>OneWireContainer</code> for communication with a DS1921 Thermocron iButton.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this iButton
    * @param  newAddress        address of this DS1921
    *
    * @see #OneWireContainer21()
    * @see #OneWireContainer21(com.dalsemi.onewire.adapter.DSPortAdapter,long) OneWireContainer21(DSPortAdapter,long)
    * @see #OneWireContainer21(com.dalsemi.onewire.adapter.DSPortAdapter,java.lang.String) OneWireContainer21(DSPortAdapter,String)
    */
   public OneWireContainer21 (DSPortAdapter sourceAdapter, String newAddress)
   {
      super(sourceAdapter, newAddress);

      // initialize the memory banks
      initMem();
   }

   /**
    * Provides this container with the adapter object used to access this device and
    * the address of the iButton or 1-Wire device.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    *                           this iButton
    * @param  newAddress        address of this 1-Wire device
    * @see com.dalsemi.onewire.utils.Address
    */
   public void setupContainer(DSPortAdapter sourceAdapter, byte[] newAddress)
   {
      super.setupContainer(sourceAdapter, newAddress);
      setThermochronVariables();
   }

   /**
    * Provides this container with the adapter object used to access this device and
    * the address of the iButton or 1-Wire device.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    *                           this iButton
    * @param  newAddress        address of this 1-Wire device
    * @see com.dalsemi.onewire.utils.Address
    */
   public void setupContainer(DSPortAdapter sourceAdapter, long newAddress)
   {
      super.setupContainer(sourceAdapter, newAddress);
      setThermochronVariables();
   }

   /**
    * Provides this container with the adapter object used to access this device and
    * the address of the iButton or 1-Wire device.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    *                           this iButton
    * @param  newAddress        address of this 1-Wire device
    * @see com.dalsemi.onewire.utils.Address
    */
   public void setupContainer(DSPortAdapter sourceAdapter, java.lang.String newAddress)
   {
      super.setupContainer(sourceAdapter, newAddress);
      setThermochronVariables();
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
      Vector bank_vector = new Vector(6);

      // scratchpad
      bank_vector.addElement(scratch);

      // NVRAM
      bank_vector.addElement(new MemoryBankNVCRC(this, scratch));

      // Register page
      bank_vector.addElement(register);

      // Alarm time stamps and duration
      bank_vector.addElement(alarm);

      // Histogram
      bank_vector.addElement(histogram);

      // Log
      bank_vector.addElement(log);

      return bank_vector.elements();
   }

   //--------
   //-------- Private
   //--------

   /**
    * Construct the memory banks used for I/O.
    */
   private void initMem ()
   {

      // scratchpad
      scratch = new MemoryBankScratchCRC(this);

      // Register
      register                      = new MemoryBankNVCRC(this, scratch);
      register.numberPages          = 1;
      register.size                 = 32;
      register.bankDescription      = "Register control";
      register.startPhysicalAddress = 0x200;
      register.generalPurposeMemory = false;

      // Alarm registers
      alarm                      = new MemoryBankNVCRC(this, scratch);
      alarm.numberPages          = 3;
      alarm.size                 = 96;
      alarm.bankDescription      = "Alarm time stamps";
      alarm.startPhysicalAddress = 544;
      alarm.generalPurposeMemory = false;
      alarm.readOnly             = true;
      alarm.readWrite            = false;

      // Histogram
      histogram                      = new MemoryBankNVCRC(this, scratch);
      histogram.numberPages          = 4;
      histogram.size                 = 128;
      histogram.bankDescription      = "Temperature Histogram";
      histogram.startPhysicalAddress = 2048;
      histogram.generalPurposeMemory = false;
      histogram.readOnly             = true;
      histogram.readWrite            = false;

      // Log
      log                      = new MemoryBankNVCRC(this, scratch);
      log.numberPages          = 64;
      log.size                 = 2048;
      log.bankDescription      = "Temperature log";
      log.startPhysicalAddress = 4096;
      log.generalPurposeMemory = false;
      log.readOnly             = true;
      log.readWrite            = false;
   }

   /**
    * Sets the following, calculated from the
    * 12-bit code of the 1-Wire Net Address:
    * (All temperatures set to degrees Celsius)
    * 1)  The part numbers:
    *     DS1921L-F50 = physical range -40 to +85,
    *                   operating range -40 to +85.
    *     DS1921L-F51 = physical range -40 to +85,
    *                   operating range -10 to +85.
    *     DS1921L-F52 = physical range -40 to +85,
    *                   operating range -20 to +85.
    *     DS1921L-F53 = physical range -40 to +85,
    *                   operating range -30 to +85.
    *
    *     DS1921H     = physical range 15 to 46,
    *                   operating range -40 to +85
    *     DS1921Z     = physical range -5 to 26,
    *                   operating range -40 to +85
    * 2)  Temperature Range low temperature.
    * 3)  Temperature Range width in degrees Celsius.
    * 4)  Temperature Resolution.
    * 5)  If a DS1921H or DS1921Z is detected.
    *
    */
   private void setThermochronVariables()
   {
      // Get Temperature Range code, which is the first 12 (MSB) bits of the
      // unique serial number (after the CRC).
      byte[] address = getAddress(); // retrieve 1-Wire net address to look at range code.
      int rangeCode = (((address[6]&0x0FF)<<4)|((address[5]&0x0FF)>>4));

      switch (rangeCode)
      {
         case 0x34C:
            partNumber = "DS1921L-F51";
            temperatureRangeLow = -40;
            temperatureRangeHigh = 85;
            temperatureResolution = 0.5;
            temperatureOperatingRangeLow = -10;
            temperatureOperatingRangeHigh = 85;
            isDS1921HZ = false;
            break;
         case 0x254:
            partNumber = "DS1921L-F52";
            temperatureRangeLow = -40;
            temperatureRangeHigh = 85;
            temperatureResolution = 0.5;
            temperatureOperatingRangeLow = -20;
            temperatureOperatingRangeHigh = 85;
            isDS1921HZ = false;
            break;
         case 0x15C:
            partNumber = "DS1921L-F53";
            temperatureRangeLow = -40;
            temperatureRangeHigh = 85;
            temperatureResolution = 0.5;
            temperatureOperatingRangeLow = -30;
            temperatureOperatingRangeHigh = 85;
            isDS1921HZ = false;
            break;
         case 0x4F2:
            partNumber = "DS1921H-F5";
            temperatureRangeLow = 15;
            temperatureRangeHigh = 46;
            temperatureResolution = 0.125;
            temperatureOperatingRangeLow = -40;
            temperatureOperatingRangeHigh = 85;
            isDS1921HZ = true;
            break;
         case 0x3B2:
            partNumber = "DS1921Z-F5";
            temperatureRangeLow = -5;
            temperatureRangeHigh = 26;
            temperatureResolution = 0.125;
            temperatureOperatingRangeLow = -40;
            temperatureOperatingRangeHigh = 85;
            isDS1921HZ = true;
            break;
         default:
            long lower36bits =
               (((long)address[5]&0x0F)<<32) |
               (((long)address[4]&0x0FF)<<24) |
               (((long)address[3]&0x0FF)<<16) |
               (((long)address[2]&0x0FF)<<8) |
               ((long)address[1]&0x0FF);
            if (lower36bits >= 0x100000)
               partNumber = "DS1921G-F5";
            else
               partNumber = "DS1921L-PROTO";

            temperatureRangeLow = -40;
            temperatureRangeHigh = 85;
            temperatureResolution = 0.5;
            temperatureOperatingRangeLow = -40;
            temperatureOperatingRangeHigh = 85;
            isDS1921HZ = false;
            break;

      }
      /*
      // Get Temperature Range code, which is the first 12 (MSB) bits of the
      // unique serial number (after the CRC).
      byte[] netAddress = getAddress(); // retrieve 1-Wire net address to look at range code.
      int rangeCode = (netAddress[6] & 0xFF); // And with 0xFF to get rid of sign extension
      rangeCode = rangeCode << 8;   // left shift 8 bits to put most significant byte in correct place
      rangeCode = rangeCode + (netAddress[5] & 0xFF);  // add the least significant byte to make integer.
      rangeCode = rangeCode >> 4;   // this is a 12-bit number, so get rid of extra 4 bits.

      // Detect what kind of part we have, a DS1921L-F5X or a DS1921H/Z
      int detectionInt = rangeCode & 0x03;  // get the last 2 bits to see what they are
      if (detectionInt > 0) isDS1921HZ = true; // if the last 2 bits > 0 then the part is a DS1921H or Z

      // Get temperature ranges as a result of the rangeCode and the type of device.
      if (isDS1921HZ)
      {
         // get the most significant 8 bits of the 12-bit rangeCode
         temperatureRangeLow = rangeCode >> 4;
         temperatureRangeLow = temperatureRangeLow - 64; // 1 degree increment with 0x000 = -64 degrees.

         // Resolution Code -- the last 2 bits of the 12-bit rangeCode number
         //
         // 0 = 0.5 degrees Celsius
         // 1 = 0.25
         // 2 = 0.125
         // 3 = 0.0625
         switch(rangeCode & 0x03) // gets the last 2 bits of the 12-bit rangeCode.
         {
            case 0:
               temperatureResolution = 0.5;
               break;
            case 1:
               temperatureResolution = 0.25;
               break;
            case 2:
               temperatureResolution = 0.125;
               break;
            case 3:
               temperatureResolution = 0.0625;
               break;
            default:
               temperatureResolution = 0.5;
         }

         // Range Modifier Code (range width)
         //
         // 0 = full range, 256 * resolution
         // 1 = reduced range, 2/3 of full range
         // 2 = reduced range, 1/2 of full range
         // 3 = reduced range, 1/3 of full range

         switch((rangeCode >> 2) & 0x03)
         {
            case 0:
               temperatureRangeWidth = (256 * temperatureResolution) - 1;
               break;
            case 1:
               temperatureRangeWidth = (256 * temperatureResolution * 2 / 3) - 1;
               break;
            case 2:
               temperatureRangeWidth = (256 * temperatureResolution / 2) - 1;
               break;
            case 3:
               temperatureRangeWidth = (256 * temperatureResolution / 3) - 1;
               break;
            default:
               temperatureRangeWidth = (256 * temperatureResolution) - 1;
         }
      }
      else
      {
         // get the most significant 5 bits of the 12-bit rangeCode number.
         temperatureRangeLow = rangeCode >> 7;
         temperatureRangeLow = (temperatureRangeLow * 5) - 40;  // 5 degree increment
         temperatureResolution = 0.5;  // for non DS1921H/Z parts, the resolution is the same.
         // if part has a range code, get the next 5 bits of the 12-bit number.
         if (rangeCode > 0) temperatureRangeWidth = ((rangeCode >> 2) & (0x1F)) * 5; // 5 degree increment
      }
      // set the part number based (currently) on low temperature.
      switch((int) temperatureRangeLow) // switches on the low temperature.
      {
         case 15:
            partNumber = "DS1921H-F5";
            break;
         case -5:
            partNumber = "DS1921Z-F5";
            break;
         case -10:
            partNumber = "DS1921L-F51";
            break;
         case -20:
            partNumber = "DS1921L-F52";
            break;
         case -30:
            partNumber = "DS1921L-F53";
            break;
         case -40:
            partNumber = "DS1921L-F50";
            break;
         default:
            partNumber = "DS1921";
      }*/
   }

   /**
    * Grab the date from one of the time registers.
    * returns int[] = {year, month, date}
    */
   private int[] getDate (int timeReg, byte[] state)
   {
      byte  upper, lower;
      int[] result = new int [3];

      timeReg = timeReg & 31;

      /* extract the day of the month */
      lower      = state [timeReg++];
      upper      = ( byte ) ((lower >>> 4) & 0x0f);
      lower      = ( byte ) (lower & 0x0f);
      result [2] = 10 * upper + lower;

      /* extract the month */
      lower = state [timeReg++];
      upper = ( byte ) ((lower >>> 4) & 0x0f);
      lower = ( byte ) (lower & 0x0f);

      // the upper bit contains the century, so subdivide upper
      byte century = ( byte ) ((upper >>> 3) & 0x01);

      upper      = ( byte ) (upper & 0x01);
      result [1] = lower + upper * 10;

      /* grab the year */
      result [0] = 1900 + century * 100;
      lower      = state [timeReg++];
      upper      = ( byte ) ((lower >>> 4) & 0x0f);
      lower      = ( byte ) (lower & 0x0f);
      result [0] += upper * 10 + lower;

      return result;
   }

   /**
    * Gets the time of day fields in 24-hour time from button
    * returns int[] = {seconds, minutes, hours}
    */
   private int[] getTime (int timeReg, byte[] state)
   {
      byte  upper, lower;
      int[] result = new int [3];

      timeReg = timeReg & 31;

      // NOTE: The MSbit is ANDed out (with the 0x07) because alarm clock
      //       registers have an extra bit to indicate alarm frequency

      /* First grab the seconds. Upper half holds the 10's of seconds       */
      lower      = state [timeReg++];
      upper      = ( byte ) ((lower >>> 4) & 0x07);
      lower      = ( byte ) (lower & 0x0f);
      result [0] = ( int ) lower + ( int ) upper * 10;

      /* now grab minutes. The upper half holds the 10s of minutes          */
      lower      = state [timeReg++];
      upper      = ( byte ) ((lower >>> 4) & 0x07);
      lower      = ( byte ) (lower & 0x0f);
      result [1] = ( int ) lower + ( int ) upper * 10;

      /* now grab the hours. The lower half is single hours again, but the
         upper half of the byte is determined by the 2nd bit - specifying
         12/24 hour time. */
      lower = state [timeReg++];
      upper = ( byte ) ((lower >>> 4) & 0x07);
      lower = ( byte ) (lower & 0x0f);

      int hours;

      // if the 2nd bit is 1, convert 12 hour time to 24 hour time.
      if ((upper >>> 2) != 0)
      {

         // extract the AM/PM byte (PM is indicated by a 1)
         byte PM = ( byte ) (((upper << 6) >>> 7) & 0x01);

         // isolate the 10s place
         upper = ( byte ) (upper & 0x01);
         hours = upper * 10 + PM * 12;
      }
      else
         hours = upper * 10;   // already in 24 hour format

      hours      += lower;
      result [2] = hours;

      return result;
   }

   /**
    * Set the time in the DS1921 time register format.
    */
   private void setTime (int timeReg, int hours, int minutes, int seconds,
                         boolean AMPM, byte[] state)
   {
      byte upper, lower;

      /* format in bytes and write seconds */
      upper                = ( byte ) (seconds / 10);
      upper                = ( byte ) ((upper << 4) & 0xf0);
      lower                = ( byte ) (seconds % 10);
      lower                = ( byte ) (lower & 0x0f);
      state [timeReg & 31] = ( byte ) (upper | lower);

      timeReg++;

      /* format in bytes and write minutes */
      upper                = ( byte ) (minutes / 10);
      upper                = ( byte ) ((upper << 4) & 0xf0);
      lower                = ( byte ) (minutes % 10);
      lower                = ( byte ) (lower & 0x0f);
      state [timeReg & 31] = ( byte ) (upper | lower);

      timeReg++;

      /* format in bytes and write hours/(12/24) bit */
      if (AMPM)
      {
         upper = ( byte ) 0x04;

         if (hours > 11)
            upper = ( byte ) (upper | 0x02);

         // this next function simply checks for a decade hour
         if (((hours % 12) == 0) || ((hours % 12) > 9))
            upper = ( byte ) (upper | 0x01);

         if (hours > 12)
            hours = hours - 12;

         if (hours == 0)
            lower = ( byte ) 0x02;
         else
            lower = ( byte ) ((hours % 10) & 0x0f);
      }
      else
      {
         upper = ( byte ) (hours / 10);
         lower = ( byte ) (hours % 10);
      }

      upper                = ( byte ) ((upper << 4) & 0xf0);
      lower                = ( byte ) (lower & 0x0f);
      state [timeReg & 31] = ( byte ) (upper | lower);

      timeReg++;
   }

   /**
    * Set the current date in the DS1921's real time clock.
    *
    * year - The year to set to, i.e. 2001.
    * month - The month to set to, i.e. 1 for January, 12 for December.
    * day - The day of month to set to, i.e. 1 to 31 in January, 1 to 30 in April.
    */
   private void setDate (int year, int month, int day, byte[] state)
   {
      byte upper, lower;

      /* write the day byte (the upper holds 10s of days, lower holds single days) */
      upper        = ( byte ) (day / 10);
      upper        = ( byte ) ((upper << 4) & 0xf0);
      lower        = ( byte ) (day % 10);
      lower        = ( byte ) (lower & 0x0f);
      state [0x04] = ( byte ) (upper | lower);

      /* write the month bit in the same manner, with the MSBit indicating
         the century (1 for 2000, 0 for 1900) */
      upper = ( byte ) (month / 10);
      upper = ( byte ) ((upper << 4) & 0xf0);
      lower = ( byte ) (month % 10);
      lower = ( byte ) (lower & 0x0f);

      if (year > 1999)
      {
         upper = ( byte ) (upper | 128);

         //go ahead and fix up the year too while i'm at it
         year = year - 2000;
      }
      else
         year = year - 1900;

      state [0x05] = ( byte ) (upper | lower);

      // now write the year
      upper        = ( byte ) (year / 10);
      upper        = ( byte ) ((upper << 4) & 0xf0);
      lower        = ( byte ) (year % 10);
      lower        = ( byte ) (lower & 0x0f);
      state [0x06] = ( byte ) (upper | lower);
   }

   //////////////////////////////////////////////////////////////
   //
   //   Public methods
   //
   //////////////////////////////////////////////////////////////


   /**
    * Returns the maximum speed this iButton device can
    * communicate at.
    *
    * @return maximum speed
    * @see DSPortAdapter#setSpeed
    */
   public int getMaxSpeed ()
   {
      return DSPortAdapter.SPEED_OVERDRIVE;
   }

   /**
    * Gets the Dallas Semiconductor part number of the iButton
    * or 1-Wire Device as a <code>java.lang.String</code>.
    * For example "DS1992".
    *
    * @return iButton or 1-Wire device name
    */
   public String getName ()
   {
      return partNumber;
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
      return "Thermochron";
   }

   /**
    * Gets a short description of the function of this iButton
    * or 1-Wire Device type.
    *
    * @return device description
    */
   public String getDescription ()
   {
      // put the DS1921's characteristics together in a string format.
      String characteristics = "";
      if (partNumber != "DS1921")
      {
         // get the physical range as a string
         String strPhysicalRange = Convert.toString(getPhysicalRangeLowTemperature(),1)
            + " to " + Convert.toString(getPhysicalRangeHighTemperature(),1)
            + " degrees Celsius.";
         // get the operating range as a string
         String strOperatingRange = Convert.toString(getOperatingRangeLowTemperature(),1)
            + " to " + Convert.toString(getOperatingRangeHighTemperature(),1)
            + " degrees Celsius.";
         characteristics
            = " The operating range for this device is:  " + strOperatingRange
            + " The physical range for this device is:  " + strPhysicalRange
            + " The resolution is " + Convert.toString(getTemperatureResolution(),3)
            + " degrees Celsius, and the histogram bin width is "
            + Convert.toString(getHistogramBinWidth(),3)
            + " degrees Celsius.";
      }
      String returnString
         = "Rugged, self-sufficient 1-Wire device that, once setup for "
         + "a mission, will measure the temperature and record the result in "
         + "a protected memory section. It stores up to 2048 temperature "
         + "measurements and will take measurements at a user-specified "
         + "rate. The thermochron also records the number of times the temperature "
         + "falls on a given degree range (temperature bin), and stores the "
         + "data in histogram format." + characteristics;

      return returnString;
   }

   /**
    * Directs the container to avoid the calls to doSpeed() in methods that communicate
    * with the Thermocron. To ensure that all parts can talk to the 1-Wire bus
    * at their desired speed, each method contains a call
    * to <code>doSpeed()</code>.  However, this is an expensive operation.
    * If a user manages the bus speed in an
    * application,  call this method with <code>doSpeedCheck</code>
    * as <code>false</code>.  The default behavior is
    * to call <code>doSpeed()</code>.
    *
    * @param doSpeedCheck <code>true</code> for <code>doSpeed()</code> to be called before every
    * 1-Wire bus access, <code>false</code> to skip this expensive call
    *
    * @see OneWireContainer#doSpeed()
    */
   public synchronized void setSpeedCheck (boolean doSpeedCheck)
   {
      doSpeedEnable = doSpeedCheck;
   }

   /**
    * This method returns the low temperature of
    * the thermochron's physical temperature range.
    * The physical range is the range of temperatures
    * that the thermochron can record.
    *
    * The following is a list of physical ranges in
    * degrees Celsius:
    *
    *     DS1921L-F5X = physical range -40 to +85
    *
    *     DS1921H     = physical range 15 to 46
    *
    *     DS1921Z     = physical range -5 to 26
    *
    * @return the physical range low temperature in degrees Celsius
    */
   public double getPhysicalRangeLowTemperature()
   {
      return temperatureRangeLow;
   }

   /**
    * This method returns the high temperature of
    * the thermochron's physical temperature range.
    * The physical range is the range of temperatures
    * that the thermochron can record.
    *
    * The following is a list of physical ranges in
    * degrees Celsius:
    *
    *     DS1921L-F5X = physical range -40 to +85
    *
    *     DS1921H     = physical range 15 to 46
    *
    *     DS1921Z     = physical range -5 to 26
    *
    * @return the physical range low temperature in degrees Celsius
    */
   public double getPhysicalRangeHighTemperature()
   {
      return temperatureRangeHigh;
   }

   /**
    * This method returns the low temperature of
    * the thermochron's operating temperature range.
    * The operating range is the range of temperatures
    * for which the thermochron can function properly.
    *
    * The following is a list of operating ranges in
    * degrees Celsius:
    *
    *     DS1921L-F50 = operating range -40 to +85.
    *     DS1921L-F51 = operating range -10 to +85.
    *     DS1921L-F52 = operating range -20 to +85.
    *     DS1921L-F53 = operating range -30 to +85.
    *
    *     DS1921H     = operating range -40 to +85
    *     DS1921Z     = operating range -40 to +85
    *
    * @return the operating range low temperature in degrees Celsius
    */
   public double getOperatingRangeLowTemperature()
   {
      return temperatureOperatingRangeLow;
   }

   /**
    * This method returns the high temperature of
    * the thermochron's operating temperature range.
    * The operating range is the range of temperatures
    * for which the thermochron can function properly.
    *
    * The following is a list of operating ranges in
    * degrees Celsius:
    *
    *     DS1921L-F50 = operating range -40 to +85.
    *     DS1921L-F51 = operating range -10 to +85.
    *     DS1921L-F52 = operating range -20 to +85.
    *     DS1921L-F53 = operating range -30 to +85.
    *
    *     DS1921H     = operating range -40 to +85
    *     DS1921Z     = operating range -40 to +85
    *
    * @return the operating range high temperature in degrees Celsius
    */
   public double getOperatingRangeHighTemperature()
   {
      return temperatureOperatingRangeHigh;
   }

   /**
    * Retrieves the resolution with which the thermochron takes
    * temperatures in degrees Celsius.
    *
    * @return the temperature resolution of this thermochron.
    */
   public double getTemperatureResolution()
   {
      return temperatureResolution;
   }

   /**
    * Retrieves the lowest temperature of the first histogram bin
    * in degrees Celsius.
    *
    * @return the lowest histogram bin temperature.
    */
   public double getHistogramLowTemperature()
   {
      double lowTemp = getPhysicalRangeLowTemperature(); // low temp of thermochrons other than H or Z
      if (isDS1921HZ) lowTemp = lowTemp - (getTemperatureResolution() * 4);
      return lowTemp;
   }

   /**
    * This method returns the width of a histogram bin in degrees
    * Celsius.
    *
    * @return the width of a histogram bin for this thermochron.
    */
   public double getHistogramBinWidth()
   {
      return (getTemperatureResolution() * 4); // 4 temperature readings per bin
   }

   /**
    * Converts a temperature from the DS1921 <code>byte</code> encoded
    * format to degrees Celsius.  The raw temperature readings are unsigned
    * <code>byte</code> values, representing a 2.0 degree accuracy.
    *
    * @param tempByte raw DS1921 temperature reading
    *
    * @return temperature in degrees Celsius
    *
    * @see #encodeTemperature(double)
    */
   public double decodeTemperature (byte tempByte)
   {
      // the formula for DS1921H/Z:
      // C = Tbyte * Tres + (Tlow - (4 * Tres))
      // where C is decimal degrees Celsius.
      // and Tbyte is the byte to be decoded.
      // and Tlow is the low temperature of temperature range.
      // and Tres is the resolution of the DS1921.

      double decodedTemperature = 0.0;
      if (isDS1921HZ)
      {
         decodedTemperature = ((tempByte & 0x00ff) * temperatureResolution);
         decodedTemperature = decodedTemperature + (temperatureRangeLow - (4 * temperatureResolution));
      }
      else
      {
         decodedTemperature = ((tempByte & 0x00ff) / 2.0) - 40.0;
      }
      return decodedTemperature;
   }

   /**
    * Converts a temperature in degrees Celsius to
    * a <code>byte</code> formatted for the DS1921.
    *
    * @param temperature the temperature (Celsius) to convert
    *
    * @return the temperature in raw DS1921 format
    *
    * @see #decodeTemperature(byte)
    */
   public byte encodeTemperature (double temperature)
   {
      // the formula for DS1921H/Z:
      // Tbyte = ((C - Tlow) / Tres) + 4;
      // where Tbyte is the byte to be encoded.
      // and C is decimal degrees Celsius
      // and Tlow is the low temperature of temperature range
      // and Tres is the resolution of the DS1921

      byte encodedTemperature = 0x00;
      if (isDS1921HZ)
      {
         double result = ((temperature - temperatureRangeLow) / temperatureResolution) + 4;
         encodedTemperature = (byte) ((int) result & 0x000000ff);
      }
      else
      {
         encodedTemperature = ( byte ) ((( int ) (2 * temperature) + 80) & 0x000000ff);
      }
      return encodedTemperature;
   }

   /**
    * Writes a byte of data into the DS1921's memory. Note that writing to
    * the register page while a mission is in progress ends that mission.
    * Also note that the preferred way to write a page is through the
    * <code>MemoryBank</code> objects returned from the <code>getMemoryBanks()</code>
    * method.
    *
    * @param memAddr the address for writing (in the range of 0x200-0x21F)
    * @param source the data <code>byte</code> to write
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #readByte(int)
    * @see #getMemoryBanks()
    */
   public void writeByte (int memAddr, byte source)
      throws OneWireIOException, OneWireException
   {

      // User should only need to write to the 32 byte register page
      byte[] buffer = new byte [5];

      // break the address into its bytes
      byte msbAddress = ( byte ) ((memAddr >>> 8) & 0x0ff);
      byte lsbAddress = ( byte ) (memAddr & 0x0ff);

      /* check for valid parameters */
      if ((msbAddress > 0x1F) || (msbAddress < 0))
         throw new IllegalArgumentException(
            "OneWireContainer21-Address for write out of range.");

      /* perform the write and verification */
      if (doSpeedEnable)
         doSpeed();

      if (adapter.select(address))
      {

         /* write to the scratchpad first */
         buffer [0] = WRITE_SCRATCHPAD_COMMAND;
         buffer [1] = lsbAddress;
         buffer [2] = msbAddress;
         buffer [3] = source;

         adapter.dataBlock(buffer, 0, 4);

         /* read it back for the verification bytes required to copy it to mem */
         adapter.select(address);

         buffer [0] = READ_SCRATCHPAD_COMMAND;

         for (int i = 1; i < 5; i++)
            buffer [i] = ( byte ) 0x0ff;

         adapter.dataBlock(buffer, 0, 5);

         // check to see if the data was written correctly
         if (buffer [4] != source)
            throw new OneWireIOException(
               "OneWireContainer21-Error writing data byte.");

         /* now perform the copy from the scratchpad to memory */
         adapter.select(address);

         buffer [0] = COPY_SCRATCHPAD_COMMAND;

         // keep buffer[1]-buffer[3] because they contain the verification bytes
         buffer [4] = ( byte ) 0xff;

         adapter.dataBlock(buffer, 0, 5);

         /* now check to see that the part sent a 01010101 indicating a success */
         if ((buffer [4] != ( byte ) 0xAA) && (buffer [4] != ( byte ) 0x55))
            throw new OneWireIOException(
               "OneWireContainer21-Error writing data byte.");
      }
      else
         throw new OneWireException("OneWireContainer21-Device not present.");
   }

   /**
    * Reads a single byte from the DS1921.  Note that the preferred manner
    * of reading from the DS1921 Thermocron is through the <code>readDevice()</code>
    * method or through the <code>MemoryBank</code> objects returned in the
    * <code>getMemoryBanks()</code> method.
    *
    * @param memAddr the address to read from  (in the range of 0x200-0x21F)
    *
    * @return the data byte read
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #writeByte(int,byte)
    * @see #readDevice()
    * @see #getMemoryBanks()
    */
   public byte readByte (int memAddr)
      throws OneWireIOException, OneWireException
   {
      byte[] buffer = new byte [4];

      // break the address up into bytes
      byte msbAddress = ( byte ) ((memAddr >> 8) & 0x000000ff);
      byte lsbAddress = ( byte ) (memAddr & 0x000000ff);

      /* check the validity of the address */
      if ((msbAddress > 0x1F) || (msbAddress < 0))
         throw new IllegalArgumentException(
            "OneWireContainer21-Address for read out of range.");

      /* read a user specified amount of memory and verify its validity */
      if (doSpeedEnable)
         doSpeed();

      if (adapter.select(address))
      {
         buffer [0] = READ_MEMORY_CRC_COMMAND;
         buffer [1] = lsbAddress;
         buffer [2] = msbAddress;
         buffer [3] = ( byte ) 0x0ff;

         adapter.dataBlock(buffer, 0, 4);

         return buffer [3];
      }
      else
         throw new OneWireException("OneWireContainer21-Device not present.");
   }

   /**
    * <p>Gets the status of the specified flag from the specified register.
    * This method actually communicates with the Thermocron.  To improve
    * performance if you intend to make multiple calls to this method,
    * first call <code>readDevice()</code> and use the
    * <code>getFlag(int, byte, byte[])</code> method instead.</p>
    *
    * <p>The DS1921 Thermocron has two sets of flags.  One set belongs
    * to the control register.  When reading from the control register,
    * valid values for <code>bitMask</code> are:</p>
    * <ul>
    *     <li><code> TIMER_ALARM_SEARCH_FLAG  </code></li>
    *     <li><code> TEMP_HIGH_SEARCH_FLAG    </code></li>
    *     <li><code> TEMP_LOW_SEARCH_FLAG     </code></li>
    *     <li><code> ROLLOVER_ENABLE_FLAG     </code></li>
    *     <li><code> MISSION_ENABLE_FLAG      </code></li>
    *     <li><code> MEMORY_CLEAR_ENABLE_FLAG </code></li>
    *     <li><code> OSCILLATOR_ENABLE_FLAG   </code></li>
    * </ul>
    * <p>When reading from the status register, valid values
    * for <code>bitMask</code> are:</p>
    * <ul>
    *     <li><code> TIMER_ALARM_FLAG         </code></li>
    *     <li><code> TEMPERATURE_HIGH_FLAG    </code></li>
    *     <li><code> TEMPERATURE_LOW_FLAG     </code></li>
    *     <li><code> SAMPLE_IN_PROGRESS_FLAG  </code></li>
    *     <li><code> MISSION_IN_PROGRESS_FLAG </code></li>
    *     <li><code> MEMORY_CLEARED_FLAG      </code></li>
    *     <li><code> TEMP_CORE_BUSY_FLAG      </code></li>
    * </ul>
    *
    * @param register address of register containing the flag (valid values
    * are <code>CONTROL_REGISTER</code> and <code>STATUS_REGISTER</code>)
    * @param bitMask the flag to read (see above for available options)
    *
    * @return the status of the flag, where <code>true</code>
    * signifies a "1" and <code>false</code> signifies a "0"
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #getFlag(int,byte,byte[])
    * @see #readDevice()
    * @see #setFlag(int,byte,boolean)
    * @see #TIMER_ALARM_SEARCH_FLAG
    * @see #TEMP_HIGH_SEARCH_FLAG
    * @see #TEMP_LOW_SEARCH_FLAG
    * @see #ROLLOVER_ENABLE_FLAG
    * @see #MISSION_ENABLE_FLAG
    * @see #MEMORY_CLEAR_ENABLE_FLAG
    * @see #OSCILLATOR_ENABLE_FLAG
    * @see #TIMER_ALARM_FLAG
    * @see #TEMPERATURE_HIGH_FLAG
    * @see #TEMPERATURE_LOW_FLAG
    * @see #SAMPLE_IN_PROGRESS_FLAG
    * @see #MISSION_IN_PROGRESS_FLAG
    * @see #MEMORY_CLEARED_FLAG
    * @see #TEMP_CORE_BUSY_FLAG
    *
    *
    */
   public boolean getFlag (int register, byte bitMask)
      throws OneWireIOException, OneWireException
   {
      return ((readByte(register) & bitMask) != 0);
   }

   /**
    * <p>Gets the status of the specified flag from the specified register.
    * This method is the preferred manner of reading the control and
    * status flags.</p>
    *
    * <p>For more information on valid values for the <code>bitMask</code>
    * parameter, see the {@link #getFlag(int,byte) getFlag(int,byte)} method.</p>
    *
    * @param register address of register containing the flag (valid values
    * are <code>CONTROL_REGISTER</code> and <code>STATUS_REGISTER</code>)
    * @param bitMask the flag to read (see {@link #getFlag(int,byte) getFlag(int,byte)}
    * for available options)
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return the status of the flag, where <code>true</code>
    * signifies a "1" and <code>false</code> signifies a "0"
    *
    * @see #getFlag(int,byte)
    * @see #readDevice()
    * @see #setFlag(int,byte,boolean,byte[])
    */
   public boolean getFlag (int register, byte bitMask, byte[] state)
   {
      return ((state [register & 31] & bitMask) != 0);
   }

   /**
    * <p>Sets the status of the specified flag in the specified register.
    * If a mission is in progress a <code>OneWireIOException</code> will be thrown
    * (one cannot write to the registers while a mission is commencing).  This method
    * actually communicates with the DS1921 Thermocron.  To improve
    * performance if you intend to make multiple calls to this method,
    * first call <code>readDevice()</code> and use the
    * <code>setFlag(int,byte,boolean,byte[])</code> method instead.</p>
    *
    * <p>For more information on valid values for the <code>bitMask</code>
    * parameter, see the {@link #getFlag(int,byte) getFlag(int,byte)} method.</p>
    *
    * @param register address of register containing the flag (valid values
    * are <code>CONTROL_REGISTER</code> and <code>STATUS_REGISTER</code>)
    * @param bitMask the flag to read (see {@link #getFlag(int,byte) getFlag(int,byte)}
    * for available options)
    * @param flagValue new value for the flag (<code>true</code> is logic "1")
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    *         In the case of the DS1921 Thermocron, this could also be due to a
    *         currently running mission.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #getFlag(int,byte)
    * @see #getFlag(int,byte,byte[])
    * @see #setFlag(int,byte,boolean,byte[])
    * @see #readDevice()
    */
   public void setFlag (int register, byte bitMask, boolean flagValue)
      throws OneWireIOException, OneWireException
   {

      // check for Mission in Progress flag
      if (getFlag(STATUS_REGISTER, MISSION_IN_PROGRESS_FLAG))
         throw new OneWireIOException(
            "OneWireContainer21-Cannot write to register while mission is in progress.");

      // read the current flag settings
      byte flags = readByte(register);

      if (flagValue)
         flags = ( byte ) (flags | bitMask);
      else
         flags = ( byte ) (flags & ~(bitMask));

      // write the regs back
      writeByte(register, flags);
   }

   /**
    * <p>Sets the status of the specified flag in the specified register.
    * If a mission is in progress a <code>OneWireIOException</code> will be thrown
    * (one cannot write to the registers while a mission is commencing).  This method
    * is the preferred manner of setting the DS1921 status and control flags.
    * The method <code>writeDevice()</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice()</code>.</p>
    *
    * <p>For more information on valid values for the <code>bitMask</code>
    * parameter, see the {@link #getFlag(int,byte) getFlag(int,byte)} method.</p>
    *
    * @param register address of register containing the flag (valid values
    * are <code>CONTROL_REGISTER</code> and <code>STATUS_REGISTER</code>)
    * @param bitMask the flag to read (see {@link #getFlag(int,byte) getFlag(int,byte)}
    * for available options)
    * @param flagValue new value for the flag (<code>true</code> is logic "1")
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see #getFlag(int,byte)
    * @see #getFlag(int,byte,byte[])
    * @see #setFlag(int,byte,boolean)
    * @see #readDevice()
    * @see #writeDevice(byte[])
    */
   public void setFlag (int register, byte bitMask, boolean flagValue,
                        byte[] state)
   {
      register = register & 31;

      byte flags = state [register];

      if (flagValue)
         flags = ( byte ) (flags | bitMask);
      else
         flags = ( byte ) (flags & ~(bitMask));

      // write the regs back
      state [register] = flags;
   }

   /**
    * <p>Begins this DS1921's mission. If a mission is
    * already in progress, this will throw a <code>OneWireIOException</code>.
    * The mission will wait the number of minutes specified by the
    * mission start delay (use <code>setMissionStartDelay()</code>)
    * before beginning.</p>
    *
    * <p>Note that this method actually communicates with the DS1921
    * Thermocron.  No call to <code>writeDevice()</code> is required to
    * finalize mission enabling.  However, some flags (such as the mission
    * start delay) may need to be set with a call to <code>writeDevice()</code>
    * before the mission is enabled.  See the usage section
    * above for an example of starting a mission.</p>
    *
    * @param sampleRate the number of minutes to wait in between temperature samples
    * (valid values are 1 to 255)
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    *         In the case of the DS1921 Thermocron, this could also be due to a
    *         currently running mission.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #disableMission()
    * @see #setMissionStartDelay(int,byte[])
    * @see #writeDevice(byte[])
    */
   public void enableMission (int sampleRate)
      throws OneWireIOException, OneWireException
   {
      /* check for valid parameters */
      if ((sampleRate > 255) || (sampleRate < 0))
         throw new IllegalArgumentException(
            "OneWireContainer21-Sample rate must be 255 minutes or less");

      if (getFlag(STATUS_REGISTER, MISSION_IN_PROGRESS_FLAG))
         throw new OneWireIOException(
            "OneWireContainer30-Unable to start mission (Mission already in Progress)");

      // read the current register status
      byte controlReg = readByte(CONTROL_REGISTER);

      // Set the enable mission byte to 0
      controlReg = ( byte ) (controlReg & 0xEF);

      writeByte(CONTROL_REGISTER, controlReg);

      // set the sample rate and let her rip
      writeByte(0x20D, ( byte ) (sampleRate & 0x000000ff));
   }

   /**
    * Ends this DS1921's running mission.  Note that this method
    * actually communicates with the DS1921 Thermocron.  No additional
    * call to <code>writeDevice(byte[])</code> is required.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #enableMission(int)
    */
   public void disableMission ()
      throws OneWireIOException, OneWireException
   {

      // first read the current register
      byte statusReg = readByte(STATUS_REGISTER);

      // Set the MIP bit to 0, regardless of whether a mission is commencing
      statusReg = ( byte ) (statusReg & 0xDF);   // set the MIP bit to 0;

      writeByte(STATUS_REGISTER, statusReg);
   }

   /**
    * <p>Sets the time to wait before starting the mission.
    * The DS1921 will sleep <code>missionStartDelay</code>
    * minutes after the mission is enabled with <code>enableMission(int)</code>,
    * then begin taking samples.  Only the least significant 16 bits of
    * <code>missionStartDelay</code> are relevant.</p>
    *
    * <p>The method <code>writeDevice()</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice()</code>.</p>
    *
    * @param missionStartDelay the time in minutes to delay the first sample
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see #readDevice()
    * @see #writeDevice(byte[])
    * @see #enableMission(int)
    */
   public void setMissionStartDelay (int missionStartDelay, byte[] state)
   {
      state [0x12] = ( byte ) (missionStartDelay);
      state [0x13] = ( byte ) (missionStartDelay >> 8);
   }

   /**
    * <p>Clears the memory of any previous mission.  The memory
    * must be cleared before setting up a new mission. If a
    * mission is in progress a <code>OneWireIOException</code> is thrown.</p>
    *
    * <p>The Clear Memory command clears the Thermocron's memory
    * at address 220h and higher.  It also clears the sample rate, mission
    * start delay, mission time stamp, and mission samples counter.</p>
    *
    * <p>Note that this method actually communicates with the DS1921 Thermocron.
    * No call to <code>writeDevice(byte[])</code> is necessary to finalize this
    * activity.</p>
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    *         In the case of the DS1921 Thermocron, this could also be due to a
    *         currently running mission.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #enableMission(int)
    * @see #writeDevice(byte[])
    */
   public void clearMemory ()
      throws OneWireIOException, OneWireException
   {
      // added 8/29/2001 by SH - delay necessary so that clock is
      // running before mission is enabled.
      // check to see if the Oscillator is enabled.
      byte[] state = readDevice();
      if( isClockRunning(state) )
      {
         // if the osciallator is not enabled, start it
         setClockRunEnable(true, state);
         writeDevice(state);
         // and give it the required time
         try
         {
            Thread.sleep(751);
         }
         catch(InterruptedException ie) {;}
      }

      // first set the MCLRE bit to 1 in the control register
      setFlag(CONTROL_REGISTER, MEMORY_CLEAR_ENABLE_FLAG, true);

      // now send the memory clear command and wait 5 milliseconds
      if (doSpeedEnable)
         doSpeed();

      adapter.reset();

      if (adapter.select(address))
      {
         adapter.putByte(CLEAR_MEMORY_COMMAND);

         try
         {
            Thread.sleep(5);
         }
         catch (Exception e)
         {
            //drain it
         }
      }
      else
         throw new OneWireException("OneWireContainer21-Device not found.");
   }

   /**
    * <p>Gets the clock alarm time settings.  The alarm settings used by the
    * Thermocron are Hour, Minute, Second, and Day of Week.  Note that not
    * all values in the returned <code>java.util.Calendar</code> object
    * are valid.  Only four values in the <code>Calendar</code> should
    * be used.  The field names for these values are:<pre><code>
    *      Calendar.DAY_OF_MONTH
    *      Calendar.HOUR_OF_DAY
    *      Calendar.MINUTE
    *      Calendar.SECOND</code></pre>
    * </p>
    * <p>The hour is reported in 24-hour format.  Use the method <code>getClockAlarm(byte[])</code>
    * to find out the next time an alarm event will occur.</p>
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return the alarm clock time and day of the week
    *
    * @see #setClockAlarm(int,int,int,int,int,byte[])
    * @see #readDevice()
    * @see #getClockAlarm(byte[])
    */
   public Calendar getAlarmTime (byte[] state)
   {

      // first get the time
      int[]    time   = getTime(0x207, state);
      Calendar result = Calendar.getInstance();
      result.set(Calendar.YEAR, 0);
      result.set(Calendar.MONTH, 0);
      result.set(Calendar.DATE, 0);
      result.set(Calendar.HOUR_OF_DAY, time[2]);
      result.set(Calendar.MINUTE, time[1]);
      result.set(Calendar.SECOND, time[0]);

      // Removed by SH - Not J2ME-compatible
      //Calendar result = new GregorianCalendar(0, 0, 0, time [2], time [1],
      //                                        time [0]);

      // now put the day of the week in there
      byte dayOfWeek = ( byte ) (state [0x0A] & 0x07);

      result.set(Calendar.DAY_OF_MONTH, dayOfWeek);

      return result;
   }

   /**
    * Set the DS1921's alarm clock.  Some of the parameters
    * might be unimportant depending on the alarm frequency setting.
    * For instance, if the alarm frequency setting is <code>ONCE_PER_MINUTE</code>,
    * then the <code>hour</code> argument is irrelevant.</p>
    *
    * <p>Valid values for <code>alarmFrequency</code> are:<pre><code>
    *    ONCE_PER_SECOND
    *    ONCE_PER_MINUTE
    *    ONCE_PER_HOUR
    *    ONCE_PER_DAY
    *    ONCE_PER_WEEK</code></pre>
    * </p>
    *
    * <p>The method <code>writeDevice()</code> must be called to finalize
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <code>writeDevice()</code>.</p>
    *
    * @param hours the hour of the day (0-23)
    * @param minutes the minute setting (0-59)
    * @param seconds the second setting (0-59)
    * @param day the day of the week (1-7, 1==Sunday)
    * @param alarmFrequency frequency that the alarm should occur at
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @see #readDevice()
    * @see #writeDevice(byte[])
    * @see #getClockAlarm(byte[])
    * @see #ONCE_PER_SECOND
    * @see #ONCE_PER_MINUTE
    * @see #ONCE_PER_HOUR
    * @see #ONCE_PER_DAY
    * @see #ONCE_PER_WEEK
    */
   public void setClockAlarm (int hours, int minutes, int seconds, int day,
                              int alarmFrequency, byte[] state)
   {
      setTime(0x207, hours, minutes, seconds, false, state);

      state [0x0a] = ( byte ) day;

      int number_0_msb = 0;   //how many of the MS, MM, MH, MD bytes have

      //0 as their ms bit???
      switch (alarmFrequency)
      {
         case ONCE_PER_SECOND :
            number_0_msb = 0;
            break;
         case ONCE_PER_MINUTE :
            number_0_msb = 1;
            break;
         case ONCE_PER_HOUR :
            number_0_msb = 2;
            break;
         case ONCE_PER_DAY :
            number_0_msb = 3;
            break;
         default:
         case ONCE_PER_WEEK :
            number_0_msb = 4;
            break;
      }

      for (int i = 0x07; i < 0x0b; i++)
      {
         if (number_0_msb > 0)
         {
            number_0_msb--;

            state [i] = ( byte ) (state [i] & 0x7f);   //make the leading bit 0
         }
         else
            state [i] = ( byte ) (state [i] | 0x80);   //make the laeding bit 1
      }
   }

   /**
    * Returns the rate at which the DS1921 takes temperature samples.
    * This rate is set when the mission is enabled (in the method
    * <code>enableMission(int)</code>.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return the time, in minutes, between temperature readings
    *
    * @see #enableMission(int)
    * @see #readDevice()
    */
   public int getSampleRate (byte[] state)
   {
      return ( int ) (0x0FF & state [0x0D]);
   }

   /**
    * Determines the number of samples taken on this mission.
    * Only the last 2048 samples appear in the Thermocron's log,
    * though all readings from the current mission are logged
    * in the histogram.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return the number of samples taken in the current mission
    *
    * @see #readDevice()
    * @see #getDeviceSamplesCounter(byte[])
    */
   public int getMissionSamplesCounter (byte[] state)
   {
      byte low    = state [0x1A];
      byte medium = state [0x1B];
      byte high   = state [0x1C];

      return (((high << 16) & 0x00ff0000) | ((medium << 8) & 0x0000ff00)
              | (low & 0x000000ff));
   }

   /**
    * <p>Determines the total number of samples taken by this Thermocron.
    * This includes samples taken in past missions.  It also includes
    * 'forced' readings.  A 'forced' reading refers to a reading taken
    * when the Thermocron does not have a running mission and is instructed
    * to read the current temperature.</p>
    *
    * <p>The DS1921 Thermocron is tested to last for 1 million temperature
    * readings.</p>
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return the total number of measurements taken by this Thermocron
    *
    * @see #readDevice()
    * @see #getMissionSamplesCounter(byte[])
    */
   public int getDeviceSamplesCounter (byte[] state)
   {
      byte low    = state [0x1D];
      byte medium = state [0x1E];
      byte high   = state [0x1F];

      return (((high << 16) & 0x00ff0000) | ((medium << 8) & 0x0000ff00)
              | (low & 0x000000ff));
   }

   /**
    * Returns the date and time that the last mission was
    * started.  The values in the <code>java.util.Calendar</code>
    * object are fully specified.  In other words, the year, month,
    * date, hour, minute, and second are all valid in the returned
    * object.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return the date and time that the last mission was started
    *
    * @see #readDevice()
    */
   public Calendar getMissionTimeStamp (byte[] state)
   {
      /* i know here that the mission time stamp does not start at address 214,
       * however--the mission time stamp starts with minutes, and i have
       * a method to read the seconds.  since i can ignore that in this case,
       * i can go ahead and 'fake' read the seconds
       */
      int[] time_result = getTime(0x214, state);
      int[] date_result = getDate(0x217, state);
      int   year        = date_result [0] % 100;

      // determine the century based on the number of samples taken
      int numberOfCounts         = getMissionSamplesCounter(state);
      int timeBetweenCounts      = getSampleRate(state);
      int yearsSinceMissionStart =
         ( int ) ((numberOfCounts * timeBetweenCounts) / (525600));

      // get a rough estimate of how long ago this was
      //result = getDateTime(state);
      int[] offset_result = getDate(0x204, state);
      int   result_year   = offset_result [0];

      // add the century based on this calculation
      //if ((result.get(Calendar.YEAR) - yearsSinceMissionStart) > 1999)
      if ((result_year - yearsSinceMissionStart) > 1999)
         year += 2000;
      else
         year += 1900;

      // protect against deviations that may cause gross errors
      //if (year > result.get(Calendar.YEAR))
      if (year > result_year)
         year -= 100;

      Calendar result = Calendar.getInstance();
      result.set(Calendar.YEAR, year);
      result.set(Calendar.MONTH, date_result[1] - 1);
      result.set(Calendar.DATE, date_result[2]);
      result.set(Calendar.HOUR_OF_DAY, time_result[2]);
      result.set(Calendar.MINUTE, time_result[1]);
      // SH - zeroed out the seconds (previously random)
      result.set(Calendar.SECOND, 0);

      return result;
      //removed by SH - not J2ME-compliant
      //new GregorianCalendar(year, date_result [1] - 1,
      //                             date_result [2], time_result [2],
      //                             time_result [1]);
   }

   /**
    * <p>Helps determine the times for values in a temperature log.  If rollover
    * is enabled, temperature log entries will over-write previous
    * entries once more than 2048 logs are written.  The returned value can be
    * added to the underlying millisecond value of <code>getMissionTimeStamp()</code>
    * to determine the time that the 'first' log entry actually occurred.</p>
    * <pre><code>
    *      //ds1921 is a OneWireContainer21
    *      byte[] state = ds1921.readDevice();
    *      Calendar c = ds1921.getMissionTimeStamp(state);
    *      //find the time for the first log entry
    *      long first_entry = c.getTime().getTime();
    *      first_entry += ds1921.getFirstLogOffset(state);
    *      . . .
    * </code></pre>
    *
    * <p>Be cautious of Java's Daylight Savings Time offsets when using this
    * function--if you use the <code>Date</code> or <code>Calendar</code>
    * class to print this out, Java may try to automatically format
    * the <code>java.lang.String</code> to handle Daylight Savings Time, resulting in offset
    * by 1 hour problems.</p>
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return milliseconds between the beginning of the mission
    *     and the time of the first log entry reported from
    *     <code>getTemperatureLog()</code>
    *
    * @see #readDevice()
    * @see #getMissionTimeStamp(byte[])
    * @see #getTemperatureLog(byte[])
    */
   public long getFirstLogOffset (byte[] state)
   {
      long counter = getMissionSamplesCounter(state);


      if ((counter < 2049)
              || (!getFlag(CONTROL_REGISTER, ROLLOVER_ENABLE_FLAG, state)))
         return 0;

      //else we need to figure out when the first sample occurred
      //since there are counter entries, the first entry is (counter - 2048)
      //so if we multiply that times milliseconds between entry,
      //we should be OK
      counter -= 2048;

      //rate is the rate in minutes, must multiply by 60 to be seconds,
      //then by 1000 to be milliseconds
      int rate = this.getSampleRate(state);

      counter = counter * rate * 1000 * 60;

      return counter;
   }

   /**
    * <p>Returns the log of temperature measurements.  Each <code>byte</code>
    * in the returned array is an independent sample.  Use the method
    * <code>decodeTemperature(byte)</code> to get the double value
    * of the encoded temperature.  See the DS1921 datasheet for more
    * on the data's encoding scheme.  The array's length equals the
    * number of measurements taken thus far.  The temperature log can
    * be read while a mission is still in progress. </p>
    *
    * <p>Note that although this method takes the device state as a parameter,
    * this method still must communicate directly with the Thermocron
    * to read the log.</p>
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return the DS1921's encoded temperature log
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #decodeTemperature(byte)
    * @see #readDevice()
    * @see #getFirstLogOffset(byte[])
    * @see #getMissionTimeStamp(byte[])
    */
   public synchronized byte[] getTemperatureLog (byte[] state)
      throws OneWireIOException, OneWireException
   {
      byte[] result;

      /* get the number of samples and the rate at which they were taken */
      int numberOfReadings = getMissionSamplesCounter(state);

      // used for rollover
      int offsetDepth = 0;

      /* this next line checks the rollover bit and whether a rollover occurred */
      if ((getFlag(CONTROL_REGISTER, ROLLOVER_ENABLE_FLAG, state))
              && (numberOfReadings > 2048))
      {

         // offsetDepth holds the number of new readings before we hit older ones
         offsetDepth = numberOfReadings % 2048;
      }

      // the max number of readings STORED is 2048
      if (numberOfReadings > 2048)
         numberOfReadings = 2048;

      result = new byte [numberOfReadings];

      int offset = 0;

      while (offset < numberOfReadings)
      {
         log.readPageCRC(offset >> 5, false, read_log_buffer, offset);

         offset += 32;
      }

      //put the bytes into the output array, but careful for the case
      //where we rolled over that we start in the right place!
      System.arraycopy(read_log_buffer, offsetDepth, result, 0,
                       numberOfReadings - offsetDepth);
      System.arraycopy(read_log_buffer, 0, result,
                       numberOfReadings - offsetDepth, offsetDepth);

      return result;
   }

   /**
    * <p>Returns an array of at most 64 counter bins holding the DS1921 histogram data
    * (63 bins for the DS1921L-F5X and 64 bins for the DS1921H or DS1921Z).  For the
    * temperature histogram, the DS1921 provides bins that each consist of a 16-bit,
    * non rolling-over binary counter that is incremented each time a temperature value
    * acquired during a mission falls into the range of the bin. The bin to be
    * updated is determined by cutting off the two least significant bits of the
    * binary temperature value.  For example, on a DS1921L-F5X, bin 0 will hold the
    * counter for temperatures ranging from -40 to -38.5 (Celsius) and lower. Bin 1
    * is associated with the range of -38 to 36.5 and so on.  The last bin, in this
    * case bin 62, holds temperature values of 84 degrees and higher.  Please see the
    * respective DS1921H or DS1921Z datasheets for their bin arrangements.  The
    * temperature histogram can be read while a mission is still in progress.</p>
    *
    * @return the 63 temperature counters
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public int[] getTemperatureHistogram ()
      throws OneWireIOException, OneWireException
   {
      int[]  result;
      if (isDS1921HZ)
         result = new int[64]; //  One more bin for the H or Z
      else
         result = new int [63];
      byte[] buffer = new byte [128];

      /* read the data first */
      int offset = 0;

      while (offset < 128)
      {
         histogram.readPageCRC(offset >> 5, false, buffer, offset);

         offset += 32;
      }

      int i = 0, j = 0;

      while (i < result.length)
      {

         // get the 2 byte counter values
         result [i] = (buffer [j] & 0x00ff)
                      | ((buffer [j + 1] << 8) & 0xff00);

         i++;

         j += 2;
      }

      return result;
   }

   /**
    * Returns <code>true</code> if the specified alarm has been
    * triggered.  Valid values for the <code>alarmBit</code>
    * parameter are:<code><pre>
    *     TEMPERATURE_LOW_ALARM
    *     TEMPERATURE_HIGH_ALARM
    *     TIMER_ALARM
    * </pre></code>
    *
    * @param alarmBit the alarm to check
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return <true> if the specified alarm has been triggered
    *
    * @see #TEMPERATURE_LOW_ALARM
    * @see #TEMPERATURE_HIGH_ALARM
    * @see #TIMER_ALARM
    * @see #readDevice()
    * @see #getAlarmHistory(byte)
    */
   public boolean getAlarmStatus (byte alarmBit, byte[] state)
   {
      return ((state [STATUS_REGISTER & 31] & alarmBit) != 0);
   }

   /**
    * <p>Returns an array containing the alarm log.
    * The DS1921 contains two separate alarm logs. One for the high temperature
    * alarm and one for the low temperature alarm.  Each log can store
    * up to 12 log entries and each log entry will record up to 255
    * consecutive alarm violations.</p>
    *
    * <p>The returned array is not altered from its representation
    * on the DS1921 Thermocron.  It is therefore up to the caller to
    * interpret the data.  The number of logs in this alarm history
    * is equal to the array length divided by 4, since each entry
    * is 4 bytes.  The first three bytes are the number of samples into the
    * mission that the alarm occurred.  The fourth byte is the number of
    * consecutive samples that violated the alarm.  To extract the starting
    * offset and number of violations from the array:</p>
    * <code><pre>
    *       byte[] data = ds1921.getAlarmHistory(OneWireContainer21.TEMPERATURE_HIGH_ALARM);
    *       int start_offset;
    *       int violation_count;
    *       . . .
    *       for (int i=0;i < data.length/4; i++)
    *       {
    *           start_offset = (data [i * 4] & 0x0ff)
    *                     | ((data [i * 4 + 1] << 8) & 0x0ff00)
    *                     | ((data [i * 4 + 2] << 16) & 0x0ff0000);
    *           violation_count = 0x0ff & data[i*4+3];
    *
    *           . . .
    *
    *           // note: you may find it useful to multiply start_offset
    *           //       by getSampleRate() in order to get the number of
    *           //       minutes into the mission that the violation occurred
    *           //       on.  You can do the same with violation_count
    *           //       to determine how long the violation lasted.
    *       }
    * </pre></code>
    *
    * <p>Acceptable values for the <code>alarmBit</code>
    * parameter are:<pre><code>
    *     TEMPERATURE_LOW_ALARM
    *     TEMPERATURE_HIGH_ALARM</code></pre>
    * </p>
    *
    * @param alarmBit the alarm log to get
    *
    * @return the time/duration of the alarm (see above for the structure of the array)
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #getAlarmStatus(byte,byte[])
    */
   public byte[] getAlarmHistory (byte alarmBit)
      throws OneWireIOException, OneWireException
   {
      int    counter   = 0;
      byte[] temp_data = new byte [96];
      int    offset    = 0;

      while (offset < 96)
      {
         alarm.readPageCRC(offset >> 5, false, temp_data, offset);

         offset += 32;
      }

      if (alarmBit == TEMPERATURE_LOW_ALARM)
         offset = 0;
      else
         offset = 48;

      /* check how many entries there are (each entry consists of 4 bytes)
         the fourth byte of each entry is the counter - check if its > 0 */
      /* but there can only be a maximum of 12 entries! */
      while (counter < 12
             && (counter * 4 + 3 + offset < temp_data.length)
             && (temp_data [counter * 4 + 3 + offset] != 0))
         counter++;

      byte[] data = new byte [counter << 2];

      System.arraycopy(temp_data, offset, data, 0, counter << 2);

      return data;
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
      byte[] buffer = new byte [32];

      //going to return the register page, 32 bytes
      register.readPageCRC(0, false, buffer, 0);

      return buffer;
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
      if (getFlag(STATUS_REGISTER, MISSION_IN_PROGRESS_FLAG))
         throw new OneWireIOException(
            "OneWireContainer21-Cannot write to registers while mission is in progress.");

      int start = updatertc ? 0
                            : 7;

      register.write(start, state, start, 20 - start);   //last 12 bytes are read only

      synchronized (this)
      {
         updatertc = false;
      }
   }

   ////////////////////////////////////////////////////////////////////////////////////////
   //
   //       Temperature Interface Functions
   //
   ////////////////////////////////////////////////////////////////////////////////////////

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
   public boolean hasTemperatureAlarms ()
   {
      return true;
   }

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
   public boolean hasSelectableTemperatureResolution ()
   {
      return false;
   }

   /**
    * Get an array of available temperature resolutions in Celsius.
    *
    * @return byte array of available temperature resolutions in Celsius with
    *         minimum resolution as the first element and maximum resolution
    *         as the last element
    *
    * @see    #hasSelectableTemperatureResolution
    * @see    #getTemperatureResolution
    * @see    #setTemperatureResolution
    */
   public double[] getTemperatureResolutions ()
   {
      double[] d = new double [1];

      d [0] = temperatureResolution;

      return d;
   }

   /**
    * Gets the temperature alarm resolution in Celsius.
    *
    * @return temperature alarm resolution in Celsius for this 1-wire device
    *
    * @see    #hasTemperatureAlarms
    * @see    #getTemperatureAlarm
    * @see    #setTemperatureAlarm
    *
    */
   public double getTemperatureAlarmResolution ()
   {
      return 1.5;
   }

   /**
    * Gets the maximum temperature in Celsius.
    *
    * @return maximum temperature in Celsius for this 1-wire device
    *
    * @see #getMinTemperature()
    */
   public double getMaxTemperature ()
   {
      return getOperatingRangeHighTemperature();
   }

   /**
    * Gets the minimum temperature in Celsius.
    *
    * @return minimum temperature in Celsius for this 1-wire device
    *
    * @see #getMaxTemperature()
    */
   public double getMinTemperature ()
   {
      return getOperatingRangeLowTemperature();
   }

   //--------
   //-------- Temperature I/O Methods
   //--------

   /**
    * Performs a temperature conversion.  Use the <code>state</code>
    * information to calculate the conversion time.
    *
    * @param  state byte array with device state information
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    *         In the case of the DS1921 Thermocron, this could also be due to a
    *         currently running mission.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public void doTemperatureConvert (byte[] state)
      throws OneWireIOException, OneWireException
   {

      /* check for mission in progress */
      if (getFlag(STATUS_REGISTER, MISSION_IN_PROGRESS_FLAG))
         throw new OneWireIOException("OneWireContainer21-Cant force "
                                      + "temperature read during a mission.");

      /* get the temperature*/
      if (doSpeedEnable)
         doSpeed();   //we aren't worried about how long this takes...we're sleeping for 750 ms!

      adapter.reset();

      if (adapter.select(address))
      {

         // perform the temperature conversion
         adapter.putByte(CONVERT_TEMPERATURE_COMMAND);

         try
         {
            Thread.sleep(750);
         }
         catch (InterruptedException e){}

         // grab the temperature
         state [0x11] = readByte(0x211);
      }
      else
         throw new OneWireException("OneWireContainer21-Device not found!");
   }

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
    */
   public double getTemperature (byte[] state)
   {
      return decodeTemperature(state [0x11]);
   }

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
    * @see    #hasTemperatureAlarms
    * @see    #setTemperatureAlarm
    */
   public double getTemperatureAlarm (int alarmType, byte[] state)
   {
      if ((alarmType == TEMPERATURE_HIGH_ALARM) || (alarmType == ALARM_HIGH))
         return decodeTemperature(state [0x0c]);
      else
         return decodeTemperature(state [0x0b]);
   }

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
   public double getTemperatureResolution (byte[] state)
   {
      return temperatureResolution;
   }

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
    * @see    #hasTemperatureAlarms
    * @see    #getTemperatureAlarm
    */
   public void setTemperatureAlarm (int alarmType, double alarmValue,
                                    byte[] state)
   {
      double histogramLow = getHistogramLowTemperature();
      double histogramHigh = getPhysicalRangeHighTemperature() + (getHistogramBinWidth() - getTemperatureResolution());
      byte alarm = encodeTemperature(alarmValue);

      // take special care of top and bottom of temperature ranges for the different
      // types of thermochrons.
      if (isDS1921HZ)
      {
         if (alarmValue < histogramLow)
            alarm = 0;

         if (alarmValue > histogramHigh)
            alarm = ( byte ) 0xFF;  // maximum value stand for the histogram high temperature
      }
      else
      {
         if (alarmValue < -40.0)
            alarm = 0;

         if (alarmValue > 85.0)
            alarm = ( byte ) 0xfa;   // maximum value stands for 85.0 C
      }

      if ((alarmType == TEMPERATURE_HIGH_ALARM) || (alarmType == ALARM_HIGH))
      {
         state [0x0c] = alarm;
      }
      else
      {
         state [0x0b] = alarm;
      }
   }

   /**
    * Sets the current temperature resolution in Celsius in the provided
    * <code>state</code> data.   Use the method <code>writeDevice()</code>
    * with this data to finalize the change to the device.
    *
    * @param  resolution temperature resolution in Celsius
    * @param  state      byte array with device state information
    *
    * @throws OneWireException if the device does not support
    * selectable temperature resolution
    *
    * @see    #hasSelectableTemperatureResolution
    * @see    #getTemperatureResolution
    * @see    #getTemperatureResolutions
    */
   public void setTemperatureResolution (double resolution, byte[] state)
      throws OneWireException
   {
      throw new OneWireException("Selectable Temperature Resolution Not Supported");
   }

   ////////////////////////////////////////////////////////////////////////////////////////
   //
   //       Clock Interface Functions
   //
   ////////////////////////////////////////////////////////////////////////////////////////

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
   public boolean hasClockAlarm ()
   {
      return true;
   }

   /**
    * Checks to see if the clock can be disabled.
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
    * Gets the clock resolution in milliseconds
    *
    * @return the clock resolution in milliseconds
    */
   public long getClockResolution ()
   {
      return 1000;
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

      /* grab the time (at location 200, date at 204) */
      int[] time = getTime(0x200, state);
      int[] date = getDate(0x204, state);

      //date[1] - 1 because Java months are 0 offset
      //date[0] - 1900 because Java years are from 1900
      //Date d = new Date(date[0]-1900, date[1]-1, date[2], time[2], time[1], time[0]);
      Calendar result = Calendar.getInstance();
      result.set(Calendar.YEAR, date[0]);
      result.set(Calendar.MONTH, date[1]-1);
      result.set(Calendar.DATE, date[2]);
      result.set(Calendar.HOUR_OF_DAY, time[2]);
      result.set(Calendar.MINUTE, time[1]);
      result.set(Calendar.SECOND, time[0]);

      //removed by SH - not J2ME-compliant
      //Calendar d = new GregorianCalendar(date [0], date [1] - 1, date [2],
      //                                   time [2], time [1], time [0]);

      return result.getTime().getTime();
   }

   /**
    * Extracts the clock alarm value for the Real-Time clock.  In the case
    * of the DS1921 Thermocron, this is the time that the next periodic
    * alarm event will occur.
    *
    * @param state current state of the device returned from <code>readDevice()</code>
    *
    * @return milliseconds since 1970 that the clock alarm is set to
    *
    * @see com.dalsemi.onewire.container.OneWireSensor#readDevice()
    * @see #hasClockAlarm()
    * @see #isClockAlarmEnabled(byte[])
    * @see #isClockAlarming(byte[])
    * @see #setClockAlarm(long,byte[])
    * @see #setClockAlarmEnable(boolean,byte[])
    */
   public long getClockAlarm (byte[] state)
   {

      //first get the normal real time clock
      int[] time = getTime(0x200, state);
      int[] date = getDate(0x204, state);

      //date[0] = year
      //date[1] = month
      //date[2] = date
      //time[2] = hour
      //time[1] = minute
      //time[0] = second
      //date[1] - 1 because Java does funky months from offset 0
      Calendar c = Calendar.getInstance();
      c.set(Calendar.YEAR, date[0]);
      c.set(Calendar.MONTH, date[1]-1);
      c.set(Calendar.DATE, date[2]);
      c.set(Calendar.HOUR_OF_DAY, time[2]);
      c.set(Calendar.MINUTE, time[1]);
      c.set(Calendar.SECOND, time[0]);

      //removed by SH - not J2ME-compliant
      //Calendar c = new GregorianCalendar(date [0], date [1] - 1, date [2],
      //                                   time [2], time [1], time [0]);

      //get the seconds into the day we are at
      int time_into_day = time [0] + 60 * time [1] + 60 * 60 * time [2];

      //now lets get the alarm specs
      int[] a_time = getTime(0x207, state);

      //get the seconds into the day the alarm is at
      int a_time_into_day = a_time [0] + 60 * a_time [1]
                            + 60 * 60 * a_time [2];

      // now put the day of the week in there
      byte dayOfWeek = ( byte ) (state [0x0A] & 0x07);

      if (dayOfWeek == 0)
         dayOfWeek++;

      byte MS = ( byte ) ((state [0x07] >>> 7) & 0x01);
      byte MM = ( byte ) ((state [0x08] >>> 7) & 0x01);
      byte MH = ( byte ) ((state [0x09] >>> 7) & 0x01);
      byte MD = ( byte ) ((state [0x0A] >>> 7) & 0x01);

      long temp_time = 0;
      int MILLIS_PER_DAY = 1000 * 60 * 60 * 24;

      switch (MS + MM + MH + MD)
      {
         case 4:                                       //ONCE_PER_SECOND
            c.add(Calendar.SECOND, 1);
            break;
         case 3:                                       //ONCE_PER_MINUTE
            if (!(a_time_into_day < time_into_day))     //alarm has occurred
               c.add(Calendar.MINUTE, 1);

            c.set(Calendar.SECOND, a_time [0]);
            break;
         case 2:                                       //ONCE_PER_HOUR
            if (!(a_time_into_day < time_into_day))     //alarm has occurred
               c.add(Calendar.HOUR_OF_DAY, 1);          //will occur again next hour

            c.set(Calendar.SECOND, a_time [0]);
            c.set(Calendar.MINUTE, a_time [1]);
            break;
         case 1:                                       //ONCE_PER_DAY
            c.set(Calendar.SECOND, a_time [0]);
            c.set(Calendar.MINUTE, a_time [1]);
            c.set(Calendar.HOUR_OF_DAY, a_time [2]);

            if ((a_time_into_day < time_into_day))      //alarm has occurred
               c.add(Calendar.DATE, 1);                        //will occur again tomorrow
            break;
         default:
         case 0:                                       //ONCE_PER_WEEK
            c.set(Calendar.SECOND, a_time [0]);
            c.set(Calendar.MINUTE, a_time [1]);

            // c.set(c.AM_PM, (a_time[2] > 11) ? c.PM : c.AM);
            c.set(Calendar.HOUR_OF_DAY, a_time [2]);

            /* oh no!!! TINI doesn't like calls to Calendar.roll() and Calendar.add()!
               we could be stuck here forever!

            if (dayOfWeek == c.get(c.DAY_OF_WEEK))
            {

               //has alarm already occurred today?
               if ((a_time_into_day < time_into_day))   //alarm has occurred
                  c.add(c.DATE, 7);                     //will occur again next week
            }
            else
            {

               //roll the day of the week until it matches
               while (dayOfWeek != c.get(c.DAY_OF_WEEK))
                  c.roll(c.DATE, true);
            }*/
            temp_time = c.getTime().getTime();

            if (dayOfWeek == c.get(Calendar.DAY_OF_WEEK))
            {

               //has alarm already occurred today?
               if ((a_time_into_day < time_into_day))   //alarm has occurred
                  temp_time += (7 * MILLIS_PER_DAY);    //will occur again next week
            }
            else
            {

               //roll the day of the week until it matches
               int cdayofweek = c.get(Calendar.DAY_OF_WEEK);

               while ((dayOfWeek % 7) != (cdayofweek++ % 7))
               {
                   temp_time += MILLIS_PER_DAY;
               }
                  //c.roll(c.DATE, true);
            }

            return temp_time;
      }

      return c.getTime().getTime();   //c->getTime returns Date, Date->getTime returns long
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
      return ((state [STATUS_REGISTER & 31] & TIMER_ALARM) != 0);
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
      return ((state [CONTROL_REGISTER & 31] & TIMER_ALARM_SEARCH_FLAG) != 0);
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

      //checks for equal to 0 since active low means clock is running
      return ((state [CONTROL_REGISTER & 31] & OSCILLATOR_ENABLE_FLAG) == 0);
   }

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
   public void setClock (long time, byte[] state)
   {
      Date     x = new Date(time);
      Calendar d = Calendar.getInstance();

      //removed by SH - not J2ME-compliant
      //Calendar d = new GregorianCalendar();

      d.setTime(x);
      setTime(0x200, d.get(Calendar.HOUR_OF_DAY), 
                     d.get(Calendar.MINUTE),
                     d.get(Calendar.SECOND),
                     false,
                     state);
      setDate(d.get(Calendar.YEAR), d.get(Calendar.MONTH) + 1, d.get(Calendar.DATE), state);

      synchronized (this)
      {
         updatertc = true;
      }
   }

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
      throws OneWireException
   {

      //can't do this because we need more info on the alarm
      throw new OneWireException(
         "Cannot set the DS1921 Clock Alarm through the Clock interface.");
   }

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
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #canDisableClock()
    * @see #isClockRunning(byte[])
    */
   public void setClockRunEnable (boolean runEnable, byte[] state)
   {

      // the oscillator enable is active low
      setFlag(CONTROL_REGISTER, OSCILLATOR_ENABLE_FLAG, !runEnable, state);
   }

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
    * @see com.dalsemi.onewire.container.OneWireSensor#writeDevice(byte[])
    * @see #hasClockAlarm()
    * @see #isClockAlarmEnabled(byte[])
    * @see #getClockAlarm(byte[])
    * @see #setClockAlarm(long,byte[])
    * @see #isClockAlarming(byte[])
    */
   public void setClockAlarmEnable (boolean alarmEnable, byte[] state)
   {
      setFlag(CONTROL_REGISTER, TIMER_ALARM_SEARCH_FLAG, alarmEnable, state);
   }
}
