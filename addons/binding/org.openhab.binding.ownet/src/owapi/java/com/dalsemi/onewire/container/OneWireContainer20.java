
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

import java.util.Vector;
import java.util.Enumeration;
import com.dalsemi.onewire.*;
import com.dalsemi.onewire.utils.*;
import com.dalsemi.onewire.adapter.*;


/**
 *<P>1-Wire&#174 container that encapsulates the functionality of the 1-Wire
 *family type <b>20</b> (hex), Dallas Semiconductor part number: <B> DS2450,
 * 1-Wire Quad A/D Converter</B>.</P>
 *
 *
 * <H3>Features</H3>
 * <UL>
 *   <LI>Four high-impedance inputs
 *   <LI>Programmable input range (2.56V, 5.12V),
 *       resolution (1 to 16 bits) and alarm thresholds
 *   <LI>5V, single supply operation
 *   <LI>Very low power, 2.5 mW active, 25 &#181W idle
 *   <LI>Unused analog inputs can serve as open
 *       drain digital outputs for closed-loop control
 *   <LI>Operating temperature range from -40&#176C to
 *        +85&#176C
 * </UL>
 *
 * <H3>Usage</H3>
 *
 * <P>Example device setup</P>
 * <PRE><CODE>
 *      byte[] state = owd.readDevice();
 *      owd.setResolution(OneWireContainer20.CHANNELA, 16, state);
 *      owd.setResolution(OneWireContainer20.CHANNELB, 8, state);
 *      owd.setRange(OneWireContainer20.CHANNELA, 5.12, state);
 *      owd.setRange(OneWireContainer20.CHANNELB, 2.56, state);
 *      owd.writeDevice();
 * </CODE></PRE>
 *
 * <P>Example device read</P>
 * <PRE><CODE>
 *      owd.doADConvert(OneWireContainer20.CHANNELA, state);
 *      owd.doADConvert(OneWireContainer20.CHANNELB, state);
 *      double chAVolatge = owd.getADVoltage(OneWireContainer20.CHANNELA, state);
 *      double chBVoltage = owd.getADVoltage(OneWireContainer20.CHANNELB, state);
 * </CODE></PRE>
 *
 * <H3>Note</H3>
 *
 * <P>When converting analog voltages to digital, the user of the device must
 * gaurantee that the voltage seen by the channel of the quad A/D does not exceed
 * the selected input range of the device.  If this happens, the device will default
 * to reading 0 volts.  There is NO way to know if the device is reading a higher than
 * specified voltage or NO voltage.</P>
 *
 * <H3> DataSheet </H3>
 *
 *   <A HREF="http://pdfserv.maxim-ic.com/arpdf/DS2450.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS2450.pdf</A>
 *
 * @version    0.00, 28 Aug 2000
 * @author     JK,DSS
 */
public class OneWireContainer20
   extends OneWireContainer
   implements ADContainer
{

   //--------
   //-------- Static Final Variables
   //--------

   /** Offset of BITMAP in array returned from read state */
   public static final int BITMAP_OFFSET = 24;

   /** Offset of ALARMS in array returned from read state */
   public static final int ALARM_OFFSET = 8;

   /** Offset of external power offset in array returned from read state */
   public static final int EXPOWER_OFFSET = 20;

   /** Channel A number */
   public static final int CHANNELA = 0;

   /** Channel B number */
   public static final int CHANNELB = 1;

   /** Channel C number */
   public static final int CHANNELC = 2;

   /** Channel D number */
   public static final int CHANNELD = 3;

   /** No preset value */
   public static final int NO_PRESET = 0;

   /** Preset value to zeros */
   public static final int PRESET_TO_ZEROS = 1;

   /** Preset value to ones */
   public static final int PRESET_TO_ONES = 2;

   /** Number of channels */
   public static final int NUM_CHANNELS = 4;

   /** DS2450 Convert command */
   private static final byte CONVERT_COMMAND = ( byte ) 0x3C;

   //--------
   //-------- Variables
   //--------

   /**
    * Voltage readout memory bank
    */
   private MemoryBankAD readout;

   /**
    * Control/Alarms/calibration memory banks vector
    */
   private Vector regs;

   //--------
   //-------- Constructors
   //--------

   /**
    * Default constructor
    */
   public OneWireContainer20 ()
   {
      super();

      // initialize the memory banks
      initMem();
   }

   /**
    * Creates a container with a provided adapter object
    * and the address of the 1-Wire device.
    *
    * @param  sourceAdapter     adapter required to communicate with
    * this device
    * @param  newAddress        address of this 1-Wire device
    */
   public OneWireContainer20 (DSPortAdapter sourceAdapter, byte[] newAddress)
   {
      super(sourceAdapter, newAddress);

      // initialize the memory banks
      initMem();
   }

   /**
    * Creates a container with a provided adapter object
    * and the address of the 1-Wire device.
    *
    * @param  sourceAdapter     adapter required to communicate with
    * this device
    * @param  newAddress        address of this 1-Wire device
    */
   public OneWireContainer20 (DSPortAdapter sourceAdapter, long newAddress)
   {
      super(sourceAdapter, newAddress);

      // initialize the memory banks
      initMem();
   }

   /**
    * Creates a container with a provided adapter object
    * and the address of the 1-Wire device.
    *
    * @param  sourceAdapter     adapter required to communicate with
    * this device
    * @param  newAddress        address of this 1-Wire device
    */
   public OneWireContainer20 (DSPortAdapter sourceAdapter, String newAddress)
   {
      super(sourceAdapter, newAddress);

      // initialize the memory banks
      initMem();
   }

   //--------
   //-------- Methods
   //--------

   /**
    * Gets the name of this 1-Wire device.
    *
    * @return representation of this 1-Wire device's name
    */
   public String getName ()
   {
      return "DS2450";
   }

   /**
    * Gets any other possible names for this 1-Wire device.
    *
    * @return representation of this 1-Wire device's other names
    */
   public String getAlternateNames ()
   {
      return "1-Wire Quad A/D Converter";
   }

   /**
    * Gets a brief description of the functionality
    * of this 1-Wire device.
    *
    * @return description of this 1-Wire device's functionality
    */
   public String getDescription ()
   {
      return "Four high-impedance inputs for measurement of analog "
             + "voltages.  User programable input range.  Very low "
             + "power.  Built-in multidrop controller.  Channels "
             + "not used as input can be configured as outputs "
             + "through the use of open drain digital outputs. "
             + "Capable of use of Overdrive for fast data transfer. "
             + "Uses on-chip 16-bit CRC-generator to guarantee good data.";
   }

   /**
    * Gets the maximum speed this 1-Wire device can communicate at.
    *
    * @return maximum speed of this One-Wire device
    */
   public int getMaxSpeed ()
   {
      return DSPortAdapter.SPEED_OVERDRIVE;
   }

   /**
    * Gets an enumeration of memory banks.
    *
    * @return enumeration of memory banks
    *
    * @see com.dalsemi.onewire.container.MemoryBank
    * @see com.dalsemi.onewire.container.PagedMemoryBank
    * @see com.dalsemi.onewire.container.OTPMemoryBank
    */
   public Enumeration getMemoryBanks ()
   {
      Vector bank_vector = new Vector(4);

      // readout
      bank_vector.addElement(readout);

      // control/alarms/calibration
      for (int i = 0; i < 3; i++)
         bank_vector.addElement(regs.elementAt(i));

      return bank_vector.elements();
   }

   //--------
   //-------- A/D Feature methods
   //--------

   /**
    * Queries to get the number of channels supported by this A/D.
    * Channel specific methods will use a channel number specified
    * by an integer from <CODE>[0 to (getNumberChannels() - 1)]</CODE>.
    *
    * @return the number of channels
    */
   public int getNumberADChannels ()
   {
      return NUM_CHANNELS;
   }

   /**
    * Queries to see if this A/D measuring device has high/low
    * alarms.
    *
    * @return <CODE>true</CODE> if it has high/low trips
    */
   public boolean hasADAlarms ()
   {
      return true;
   }

   /**
    * Queries to get an array of available ranges for the specified
    * A/D channel.
    *
    * @param channel channel in the range
    *                 <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    *
    * @return available ranges starting
    *         from the largest range to the smallest range
    */
   public double[] getADRanges (int channel)
   {
      double[] ranges = new double [2];

      ranges [0] = 5.12;
      ranges [1] = 2.56;

      return ranges;
   }

   /**
    * Queries to get an array of available resolutions based
    * on the specified range on the specified A/D channel.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param range specified range
    *
    * @return available resolutions
    */
   public double[] getADResolutions (int channel, double range)
   {
      double[] res = new double [16];

      for (int i = 0; i < 16; i++)
         res [i] = range / ( double ) (1 << (i + 1));

      return res;
   }

   /**
    * Queries to see if this A/D supports doing multiple voltage
    * conversions at the same time.
    *
    * @return <CODE>true</CODE> if can do multi-channel voltage reads
    */
   public boolean canADMultiChannelRead ()
   {
      return true;
   }

   //--------
   //-------- A/D IO Methods
   //--------

   /**
    * Retrieves the entire A/D control/status and alarm pages.
    * It reads this and verifies the data with the onboard CRC generator.
    * Use the byte array returned from this method with static
    * utility methods to extract the status, alarm and other register values.
    * Appended to the data is 2 bytes that represent a bitmap
    * of changed bytes.  These bytes are used in the <CODE>writeADRegisters()</CODE>
    * in conjuction with the 'set' methods to only write back the changed
    * register bytes.
    *
    * @return register page contents verified
    *  with onboard CRC
    *
    * @throws OneWireIOException Data was not read correctly
    * @throws OneWireException Could not find part
    */
   public byte[] readDevice ()
      throws OneWireIOException, OneWireException
   {
      byte[]       read_buf = new byte [27];
      MemoryBankAD mb;

      // read the banks, control/alarm/calibration
      for (int i = 0; i < 3; i++)
      {
         mb = ( MemoryBankAD ) regs.elementAt(i);

         mb.readPageCRC(0, (i != 0), read_buf, i * 8);
      }

      // zero out the bitmap
      read_buf [24] = 0;
      read_buf [25] = 0;
      read_buf [26] = 0;

      return read_buf;
   }

   /**
    * Writes the bytes in the provided A/D register pages that
    * have been changed by the 'set' methods.  It knows which state has
    * changed by looking at the bitmap fields appended to the
    * register data.  Any alarm flags will be automatically
    * cleared.  Only VCC powered indicator byte in physical location 0x1C
    * can be written in the calibration memory bank.
    *
    * @param  state register pages
    *
    * @throws OneWireIOException Data was not written correctly
    * @throws OneWireException Could not find part
    */
   public void writeDevice (byte[] state)
      throws OneWireIOException, OneWireException
   {
      int          start_offset, len, i, bank, index;
      boolean      got_block;
      MemoryBankAD mb;

      // Force a clear of the alarm flags
      for (i = 0; i < 4; i++)
      {

         // check if POR or alarm high/low flag present
         index = i * 2 + 1;

         if ((state [index] & ( byte ) 0xB0) != 0)
         {

            // clear the bits
            state [index] &= ( byte ) 0x0F;

            // set to write in bitmap
            Bit.arrayWriteBit(1, index, BITMAP_OFFSET, state);
         }
      }

      // only allow physical address 0x1C to be written in calibration bank
      state [BITMAP_OFFSET + 2] = ( byte ) (state [BITMAP_OFFSET + 2] & 0x10);

      // loop through the three memory banks collecting changes
      for (bank = 0; bank < 3; bank++)
      {
         start_offset = 0;
         len          = 0;
         got_block    = false;
         mb           = ( MemoryBankAD ) regs.elementAt(bank);

         // loop through each byte in the memory bank
         for (i = 0; i < 8; i++)
         {

            // check to see if this byte needs writing (skip control register for now)
            if (Bit.arrayReadBit(bank * 8 + i, BITMAP_OFFSET, state) == 1)
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
               if (i == 7)
                  mb.write(start_offset, state, bank * 8 + start_offset, len);
            }
            else if (got_block)
            {

               // done with this block so write it
               mb.write(start_offset, state, bank * 8 + start_offset, len);

               got_block = false;
            }
         }
      }

      // clear out the bitmap
      state [24] = 0;
      state [25] = 0;
      state [26] = 0;
   }

   /**
    * Reads the voltage values.  Must be used after a <CODE>doADConvert()</CODE>
    * method call.  Also must include the last valid state from the
    * <CODE>readDevice()</CODE> method and this A/D must support multi-channel
    * read <CODE>canMultiChannelRead()</CODE> if there are more then 1 channel.
    *
    * @param state current state of this device returned from
    *              <CODE>readDevice()</CODE>
    *
    * @return voltage values for all channels
    *
    * @throws OneWireIOException Data was not read correctly
    * @throws OneWireException Could not find part
    */
   public double[] getADVoltage (byte[] state)
      throws OneWireIOException, OneWireException
   {
      byte[]   read_buf = new byte [8];
      double[] ret_dbl  = new double [4];

      // get readout page
      readout.readPageCRC(0, false, read_buf, 0);

      // convert to array of doubles
      for (int ch = 0; ch < 4; ch++)
      {
         ret_dbl [ch] = interpretVoltage(Convert.toLong(read_buf, ch * 2, 2),
                                         getADRange(ch, state));
      }

      return ret_dbl;
   }

   /**
    * Reads a channels voltage value.  Must be used after a
    * <CODE>doADConvert()</CODE> method call.  Also must include
    * the last valid state from the <CODE>readDevice()</CODE> method.
    * Note, if more then one channel is to be read then it is more
    * efficient to use the <CODE>getADVoltage(byte[])</CODE> method that returns
    * all channel values.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param state current state of this
    *               device returned from <CODE>readDevice()</CODE>
    *
    * @return voltage value for the specified
    *                  channel
    *
    * @throws OneWireIOException Data was not read correctly
    * @throws OneWireException Could not find part
    * @throws IllegalArgumentException Invalid channel number passed
    */
   public double getADVoltage (int channel, byte[] state)
      throws OneWireIOException, OneWireException
   {

      // check for valid channel value
      if ((channel < 0) || (channel > 3))
         throw new IllegalArgumentException("Invalid channel number");

      // get readout page
      byte[] read_buf = new byte [8];

      readout.readPageCRC(0, false, read_buf, 0);

      return interpretVoltage(Convert.toLong(read_buf, channel * 2, 2),
                              getADRange(channel, state));
   }

   /**
    * Performs voltage conversion on specified channel.  The method
    * <CODE>getADVoltage()</CODE> can be used to read the result
    * of the conversion.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param state current state of this
    *               device returned from <CODE>readDevice()</CODE>
    *
    * @throws OneWireIOException Data was not written correctly
    * @throws OneWireException Could not find part
    */
   public void doADConvert (int channel, byte[] state)
      throws OneWireIOException, OneWireException
   {

      // call with set presets to 0
      doADConvert(channel, PRESET_TO_ZEROS, state);
   }

   /**
    * Performs voltage conversion on all specified channels.  The method
    * <CODE>getADVoltage()</CODE> can be used to read the result of the
    * conversion. This A/D must support multi-channel read
    * <CODE>canMultiChannelRead()</CODE> if there are more then 1 channel
    * is specified.
    *
    * @param doConvert which channels to perform conversion on.
    * @param state current state of this
    *               device returned from <CODE>readDevice()</CODE>
    *
    * @throws OneWireIOException Data was not written correctly
    * @throws OneWireException Could not find part
    */
   public void doADConvert (boolean[] doConvert, byte[] state)
      throws OneWireIOException, OneWireException
   {

      // call with set presets to 0
      int[] presets = new int [4];

      for (int i = 0; i < 4; i++)
         presets [i] = PRESET_TO_ZEROS;

      doADConvert(doConvert, presets, state);
   }

   /**
    * Performs voltage conversion on specified channel.  The method
    * <CODE>getADVoltage()</CODE> can be used to read the result
    * of the conversion.
    *
    * @param channel 0,1,2,3 representing the channels A,B,C,D
    * @param preset preset value:
    *           <CODE>NO_PRESET (0), PRESET_TO_ZEROS (1), and PRESET_TO_ONES (2)</CODE>
    * @param state state of this
    *               device returned from <CODE>readDevice()</CODE>
    *
    * @throws OneWireIOException Data could not be written correctly
    * @throws OneWireException Could not find part
    * @throws IllegalArgumentException Invalid channel number passed
    */
   public void doADConvert (int channel, int preset, byte[] state)
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {

      // check for valid channel value
      if ((channel < 0) || (channel > 3))
         throw new IllegalArgumentException("Invalid channel number");

      // perform the conversion (do fixed max conversion time)
      doADConvert(( byte ) (0x01 << channel), ( byte ) (preset << channel),
                  1440, state);
   }

   /**
    * Performs voltage conversion on all specified channels.
    * The method <CODE>getADVoltage()</CODE> can be used to read the result
    * of the conversion.
    *
    * @param doConvert which channels to perform conversion on
    * @param preset preset values
    *              <CODE>NO_PRESET (0), PRESET_TO_ZEROS (1), and PRESET_TO_ONES (2)</CODE>
    * @param state current state of this
    *              device returned from <CODE>readDevice()</CODE>
    *
    * @throws OneWireIOException Data could not be written correctly
    * @throws OneWireException Could not find part
    */
   public void doADConvert (boolean[] doConvert, int[] preset, byte[] state)
      throws OneWireIOException, OneWireException
   {
      byte input_select_mask = 0;
      byte read_out_control  = 0;
      int  time              = 160;   // Time required in micro Seconds to covert.

      // calculate the input mask, readout control, and conversion time
      for (int ch = 3; ch >= 0; ch--)
      {

         // input select
         input_select_mask <<= 1;

         if (doConvert [ch])
            input_select_mask |= 0x01;

         // readout control
         read_out_control <<= 2;

         if (preset [ch] == PRESET_TO_ZEROS)
            read_out_control |= 0x01;
         else if (preset [ch] == PRESET_TO_ONES)
            read_out_control |= 0x02;

         // conversion time
         time += (80 * getADResolution(ch, state));
      }

      // do the conversion
      doADConvert(input_select_mask, read_out_control, time, state);
   }

   //--------
   //-------- A/D 'get' Methods
   //--------

   /**
    * Extracts the alarm voltage value of the specified channel from the
    * provided state buffer.  The state buffer is retrieved from the
    * <CODE>readDevice()</CODE> method.
    *
    * @param channel channel in the range
    *                <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param alarmType desired alarm, <CODE>ALARM_HIGH (1) or ALARM_LOW (0)</CODE>
    * @param state current state of this
    *               device returned from <CODE>readDevice()</CODE>
    *
    * @return alarm value in volts
    *
    * @throws IllegalArgumentException Invalid channel number passed
    */
   public double getADAlarm (int channel, int alarmType, byte[] state)
   {

      // check for valid channel value
      if ((channel < 0) || (channel > 3))
         throw new IllegalArgumentException("Invalid channel number");

      // extract alarm value and convert to voltage
      long temp_long =
         ( long ) (state [ALARM_OFFSET + channel * 2 + alarmType] & 0x00FF)
         << 8;

      return interpretVoltage(temp_long, getADRange(channel, state));
   }

   /**
    * Extracts the alarm enable value of the specified channel from
    * the provided state buffer.  The state buffer is retrieved from
    * the <CODE>readDevice()</CODE> method.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param alarmType desired alarm, <CODE>ALARM_HIGH (1)
    *               or ALARM_LOW (0)</CODE>
    * @param state current state of the state
    *               returned from <CODE>readDevice()</CODE>
    *
    * @return <CODE>true</CODE> if specified alarm is enabled
    *
    * @throws IllegalArgumentException Invalid channel number passed
    */
   public boolean getADAlarmEnable (int channel, int alarmType, byte[] state)
   {

      // check for valid channel value
      if ((channel < 0) || (channel > 3))
         throw new IllegalArgumentException("Invalid channel number");

      return (Bit.arrayReadBit(2 + alarmType, channel * 2 + 1, state) == 1);
   }

   /**
    * Checks the alarm event value of the specified channel from the provided
    * state buffer.  The state buffer is retrieved from the
    * <CODE>readDevice()</CODE> method.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param alarmType desired alarm, <CODE>ALARM_HIGH (1)
    *               or ALARM_LOW (0)</CODE>
    * @param state current state of the state
    *               returned from <CODE>readDevice()</CODE>
    *
    * @return <CODE>true</CODE> if specified alarm occurred
    *
    * @throws IllegalArgumentException Invalid channel number passed
    */
   public boolean hasADAlarmed (int channel, int alarmType, byte[] state)
   {

      // check for valid channel value
      if ((channel < 0) || (channel > 3))
         throw new IllegalArgumentException("Invalid channel number");

      return (Bit.arrayReadBit(4 + alarmType, channel * 2 + 1, state) == 1);
   }

   /**
    * Extracts the conversion resolution of the specified channel from the
    * provided state buffer expressed in volts.  The state is retrieved from the
    * <CODE>readDevice()</CODE> method.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param state current state of the state
    *               returned from <CODE>readDevice()</CODE>
    *
    * @return resolution of channel in volts
    *
    * @throws IllegalArgumentException Invalid channel number passed
    */
   public double getADResolution (int channel, byte[] state)
   {

      // check for valid channel value
      if ((channel < 0) || (channel > 3))
         throw new IllegalArgumentException("Invalid channel number");

      int res = state [channel * 2] & 0x0F;

      // return resolution, if 0 then 16 bits
      if (res == 0)
         res = 16;

      return getADRange(channel, state) / ( double ) (1 << res);
   }

   /**
    * Extracts the input voltage range of the specified channel from
    * the provided state buffer.  The state buffer is retrieved from
    * the <CODE>readDevice()</CODE> method.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param state current state of the state
    *                  returned from <CODE>readDevice()</CODE>
    *
    * @return A/D input voltage range
    *
    * @throws IllegalArgumentException Invalid channel number passed
    */
   public double getADRange (int channel, byte[] state)
   {

      // check for valid channel value
      if ((channel < 0) || (channel > 3))
         throw new IllegalArgumentException("Invalid channel number");

      return (Bit.arrayReadBit(0, channel * 2 + 1, state) == 1) ? 5.12
                                                                : 2.56;
   }

   /**
    * Detects if the output is enabled for the specified channel from
    * the provided register buffer.  The register buffer is retrieved
    * from the <CODE>readDevice()</CODE> method.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param state current state of the device
    *                  returned from <CODE>readDevice()</CODE>
    *
    * @return <CODE>true</CODE> if output is enabled on specified channel
    *
    * @throws IllegalArgumentException Invalid channel number passed
    */
   public boolean isOutputEnabled (int channel, byte[] state)
      throws IllegalArgumentException
   {

      // check for valid channel value
      if ((channel < 0) || (channel > 3))
         throw new IllegalArgumentException("Invalid channel number");

      return (Bit.arrayReadBit(7, channel * 2, state) == 1);
   }

   /**
    * Detects if the output is enabled for the specified channel from
    * the provided register buffer.  The register buffer is retrieved
    * from the <CODE>readDevice()</CODE> method.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param state current state of the device
    *                  returned from <CODE>readDevice()</CODE>
    *
    * @return <CODE>false</CODE> if output is conducting to ground and
    *         <CODE>true</CODE> if not conducting
    *
    * @throws IllegalArgumentException Invalid channel number passed
    */
   public boolean getOutputState (int channel, byte[] state)
      throws IllegalArgumentException
   {

      // check for valid channel value
      if ((channel < 0) || (channel > 3))
         throw new IllegalArgumentException("Invalid channel number");

      return (Bit.arrayReadBit(6, channel * 2, state) == 1);
   }

   /**
    * Detects if this device has seen a Power-On-Reset (POR).  If this has
    * occured it may be necessary to set the state of the device to the
    * desired values.   The register buffer is retrieved from the
    * <CODE>readDevice()</CODE> method.
    *
    * @param state current state of the device
    *                  returned from <CODE>readDevice()</CODE>
    *
    * @return <CODE>false</CODE> if output is conducting to ground and
    *         <CODE>true</CODE> if not conducting
    */
   public boolean getDevicePOR (byte[] state)
   {
      return (Bit.arrayReadBit(7, 1, state) == 1);
   }

   /**
    * Extracts the state of the external power indicator from the provided
    * register buffer.  Use 'setPower' to set or clear the external power
    * indicator flag. The register buffer is retrieved from the
    * <CODE>readDevice()</CODE> method.
    *
    * @param state current state of the
    *               device returned from <CODE>readDevice()</CODE>
    *
    * @return <CODE>true</CODE> if set to external power operation
    */
   public boolean isPowerExternal (byte[] state)
   {
      return (state [EXPOWER_OFFSET] != 0);
   }

   //--------
   //-------- A/D 'set' Methods
   //--------

   /**
    * Sets the alarm voltage value of the specified channel in the
    * provided state buffer.  The state buffer is retrieved from the
    * <CODE>readDevice()</CODE> method. The method <CODE>writeDevice()</CODE>
    * must be called to finalize these changes to the device.  Note that
    * multiple 'set' methods can be called before one call to
    * <CODE>writeDevice()</CODE>.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param alarmType desired alarm, <CODE>ALARM_HIGH (1)
    *               or ALARM_LOW (0)</CODE>
    * @param alarm alarm value (will be reduced to 8 bit resolution)
    * @param state current state of this
    *               device returned from <CODE>readDevice()</CODE>
    *
    * @throws IllegalArgumentException Invalid channel number passed
    */
   public void setADAlarm (int channel, int alarmType, double alarm,
                           byte[] state)
   {

      // check for valid channel value
      if ((channel < 0) || (channel > 3))
         throw new IllegalArgumentException("Invalid channel number");

      int offset = ALARM_OFFSET + channel * 2 + alarmType;

      state [offset] =
         ( byte ) ((voltageToInt(alarm, getADRange(channel, state)) >>> 8)
                   & 0x00FF);

      // set bitmap field to indicate this register has changed
      Bit.arrayWriteBit(1, offset, BITMAP_OFFSET, state);
   }

   /**
    * Sets the alarm enable value of the specified channel in the
    * provided state buffer.  The state buffer is retrieved from the
    * <CODE>readDevice()</CODE> method. The method <CODE>writeDevice()</CODE>
    * must be called to finalize these changes to the device.  Note that
    * multiple 'set' methods can be called before one call to
    * <CODE>writeDevice()</CODE>.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param alarmType desired alarm, <CODE>ALARM_HIGH (1)
    *               or ALARM_LOW (0)</CODE>
    * @param alarmEnable alarm enable value
    * @param state current state of this
    *               device returned from <CODE>readDevice()</CODE>
    *
    * @throws IllegalArgumentException Invalid channel number passed
    */
   public void setADAlarmEnable (int channel, int alarmType,
                                 boolean alarmEnable, byte[] state)
   {

      // check for valid channel value
      if ((channel < 0) || (channel > 3))
         throw new IllegalArgumentException("Invalid channel number");

      // change alarm enable
      Bit.arrayWriteBit(((alarmEnable) ? 1
                                       : 0), 2 + alarmType, channel * 2 + 1,
                                             state);

      // set bitmap field to indicate this register has changed
      Bit.arrayWriteBit(1, channel * 2 + 1, BITMAP_OFFSET, state);
   }

   /**
    * Sets the conversion resolution value for the specified channel in
    * the provided state buffer.  The state buffer is retrieved from the
    * <CODE>readDevice()</CODE> method. The method <CODE>writeDevice()</CODE>
    * must be called to finalize these changes to the device.  Note that
    * multiple 'set' methods can be called before one call to
    * <CODE>writeDevice()</CODE>.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param resolution resolution to use in volts
    * @param state current state of this
    *               device returned from <CODE>readDevice()</CODE>
    *
    * @throws IllegalArgumentException Invalid channel number passed
    */
   public void setADResolution (int channel, double resolution, byte[] state)
   {

      // check for valid channel value
      if ((channel < 0) || (channel > 3))
         throw new IllegalArgumentException("Invalid channel number");

      // convert voltage resolution into bit resolution
      int div      = ( int ) (getADRange(channel, state) / resolution);
      int res_bits = 0;

      do
      {
         div >>>= 1;

         res_bits++;
      }
      while (div != 0);

      res_bits -= 1;

      if (res_bits == 16)
         res_bits = 0;

      // check for valid bit resolution
      if ((res_bits < 0) || (res_bits > 15))
         throw new IllegalArgumentException("Invalid resolution");

      // clear out the resolution
      state [channel * 2] &= ( byte ) 0xF0;

      // set the resolution
      state [channel * 2] |= ( byte ) ((res_bits == 16) ? 0
                                                        : res_bits);

      // set bitmap field to indicate this register has changed
      Bit.arrayWriteBit(1, channel * 2, BITMAP_OFFSET, state);
   }

   /**
    * Sets the input range for the specified channel in the provided state
    * buffer.  The state buffer is retrieved from the <CODE>readDevice()</CODE>
    * method. The method <CODE>writeDevice()</CODE> must be called to finalize
    * these changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <CODE>writeDevice()</CODE>.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param range max volt range, use
    *                getRanges() method to get available ranges
    * @param state current state of this
    *               device returned from <CODE>readDevice()</CODE>
    *
    * @throws IllegalArgumentException Invalid channel number passed
    */
   public void setADRange (int channel, double range, byte[] state)
   {

      // check for valid channel value
      if ((channel < 0) || (channel > 3))
         throw new IllegalArgumentException("Invalid channel number");

      // convert range into bit value
      int range_bit;

      if ((range > 5.00) & (range < 5.30))
         range_bit = 1;
      else if ((range > 2.40) & (range < 2.70))
         range_bit = 0;
      else
         throw new IllegalArgumentException("Invalid range");

      // change range bit
      Bit.arrayWriteBit(range_bit, 0, channel * 2 + 1, state);

      // set bitmap field to indicate this register has changed
      Bit.arrayWriteBit(1, channel * 2 + 1, BITMAP_OFFSET, state);
   }

   /**
    * Sets the output enable and state for the specified channel in the
    * provided register buffer.  The register buffer is retrieved from
    * the <CODE>readDevice()</CODE> method. The method <CODE>writeDevice()</CODE>
    * must be called to finalize these changes to the device.  Note that
    * multiple 'set' methods can be called before one call to
    * <CODE>writeDevice()</CODE>.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param outputEnable <CODE>true</CODE> if output is enabled
    * @param outputState <CODE>false</CODE> if output is conducting to
    *           ground and <CODE>true</CODE> if not conducting.  This
    *           parameter is not used if <CODE>outputEnable</CODE> is
    *           <CODE>false</CODE>
    * @param state current state of the
    *                device returned from <CODE>readDevice()</CODE>
    */
   public void setOutput (int channel, boolean outputEnable,
                          boolean outputState, byte[] state)
   {

      // check for valid channel value
      if ((channel < 0) || (channel > 3))
         throw new IllegalArgumentException("Invalid channel number");

      // output enable bit
      Bit.arrayWriteBit(((outputEnable) ? 1
                                        : 0), 7, channel * 2, state);

      // optionally set state
      if (outputEnable)
         Bit.arrayWriteBit(((outputState) ? 1
                                          : 0), 6, channel * 2, state);

      // set bitmap field to indicate this register has changed
      Bit.arrayWriteBit(1, channel * 2, BITMAP_OFFSET, state);
   }

   /**
    * Sets or clears the external power flag in the provided register buffer.
    * The register buffer is retrieved from the <CODE>readDevice()</CODE> method.
    * The method <CODE>writeDevice()</CODE> must be called to finalize these
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <CODE>writeDevice()</CODE>.
    *
    * @param external <CODE>true</CODE> if setting external power is used
    * @param state current state of this
    *               device returned from <CODE>readDevice()</CODE>
    */
   public void setPower (boolean external, byte[] state)
   {

      // sed the flag
      state [EXPOWER_OFFSET] = ( byte ) (external ? 0x40
                                                  : 0);

      // set bitmap field to indicate this register has changed
      Bit.arrayWriteBit(1, EXPOWER_OFFSET, BITMAP_OFFSET, state);
   }

   //--------
   //-------- Utility methods
   //--------

   /**
    * Converts a raw voltage long value for the DS2450 into a valid voltage.
    * Requires the max voltage value.
    *
    * @param rawVoltage raw voltage
    * @param range max voltage
    *
    * @return calculated voltage based on the range
    */
   public static double interpretVoltage (long rawVoltage, double range)
   {
      return ((( double ) rawVoltage / 65535.0) * range);
   }

   /**
    * Converts a voltage double value to the DS2450 specific int value.
    * Requires the max voltage value.
    *
    * @param voltage voltage
    * @param range max voltage
    *
    * @return the DS2450 voltage
    */
   public static int voltageToInt (double voltage, double range)
   {
      return ( int ) ((voltage * 65535.0) / range);
   }

   //--------
   //-------- Private methods
   //--------

   /**
    * Create the memory bank interface to read/write
    */
   private void initMem ()
   {

      // readout
      readout = new MemoryBankAD(this);

      // control
      regs = new Vector(3);

      MemoryBankAD temp_mb = new MemoryBankAD(this);

      temp_mb.bankDescription      = "A/D Control and Status";
      temp_mb.generalPurposeMemory = false;
      temp_mb.startPhysicalAddress = 8;
      temp_mb.readWrite            = true;
      temp_mb.readOnly             = false;

      regs.addElement(temp_mb);

      // Alarms
      temp_mb                      = new MemoryBankAD(this);
      temp_mb.bankDescription      = "A/D Alarm Settings";
      temp_mb.generalPurposeMemory = false;
      temp_mb.startPhysicalAddress = 16;
      temp_mb.readWrite            = true;
      temp_mb.readOnly             = false;

      regs.addElement(temp_mb);

      // calibration
      temp_mb                      = new MemoryBankAD(this);
      temp_mb.bankDescription      = "A/D Calibration";
      temp_mb.generalPurposeMemory = false;
      temp_mb.startPhysicalAddress = 24;
      temp_mb.readWrite            = true;
      temp_mb.readOnly             = false;

      regs.addElement(temp_mb);
   }

   /**
    * Performs voltage conversion on all specified channels.  The method
    * <CODE>getADVoltage()</CODE> can be used to read the result of the
    * conversion.
    *
    * @param inputSelectMask input select mask
    * @param readOutControl read out control
    * @param timeUs time in microseconds for conversion
    * @param state current state of this
    *                device returned from <CODE>readDevice()</CODE>
    *
    * @throws OneWireIOException Data was not written correctly
    * @throws OneWireException Could not find part
    * @throws IlleaglArgumentException Invalid channel number passed
    */
   private void doADConvert (byte inputSelectMask, byte readOutControl,
                             int timeUs, byte[] state)
      throws OneWireIOException, OneWireException
   {

      // check if no conversions
      if (inputSelectMask == 0)
      {
         throw new IllegalArgumentException(
            "No conversion will take place.  No channel selected.");
      }

      // Create the command block to be sent.
      byte[] raw_buf = new byte [5];

      raw_buf [0] = CONVERT_COMMAND;
      raw_buf [1] = inputSelectMask;
      raw_buf [2] = ( byte ) readOutControl;
      raw_buf [3] = ( byte ) 0xFF;
      raw_buf [4] = ( byte ) 0xFF;

      // calculate the CRC16 up to and including readOutControl
      int crc16 = CRC16.compute(raw_buf, 0, 3, 0);

      // Send command block.
      if (adapter.select(address))
      {
         if (isPowerExternal(state))
         {

            // good power so send the entire block (with both CRC)
            adapter.dataBlock(raw_buf, 0, 5);

            // Wait for complete of conversion
            try
            {
               Thread.sleep((timeUs / 1000) + 10);
            }
            catch (InterruptedException e){}
            ;

            // calculate the rest of the CRC16
            crc16 = CRC16.compute(raw_buf, 3, 2, crc16);
         }
         else
         {

            // parasite power so send the all but last byte
            adapter.dataBlock(raw_buf, 0, 4);

            // setup power delivery
            adapter.setPowerDuration(DSPortAdapter.DELIVERY_INFINITE);
            adapter.startPowerDelivery(DSPortAdapter.CONDITION_AFTER_BYTE);

            // get the final CRC byte and start strong power delivery
            raw_buf [4] = ( byte ) adapter.getByte();
            crc16       = CRC16.compute(raw_buf, 3, 2, crc16);

            // Wait for power delivery to complete the conversion
            try
            {
               Thread.sleep((timeUs / 1000) + 1);
            }
            catch (InterruptedException e){}
            ;

            // Turn power off.
            adapter.setPowerNormal();
         }
      }
      else
         throw new OneWireException("OneWireContainer20 - Device not found.");

      // check the CRC result
      if (crc16 != 0x0000B001)
         throw new OneWireIOException(
            "OneWireContainer20 - Failure during conversion - Bad CRC");

      // check if still busy
      if (adapter.getByte() == 0x00)
         throw new OneWireIOException("Conversion failed to complete.");
   }
}
