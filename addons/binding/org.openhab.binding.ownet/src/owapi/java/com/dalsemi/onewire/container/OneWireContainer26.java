
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
import com.dalsemi.onewire.utils.*;
import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import java.util.*;


/**
 *  <P>1-Wire&#174 container that encapsulates the functionality of the 1-Wire
 *  family type <B>26</B> (hex), Dallas Semiconductor part number: <B>DS2438,
 *  Smart Battery Monitor</B>.</P>
 *
 * <H2>Features</H2>
 * <UL>
 *   <LI>direct-to-digital temperature sensor
 *   <LI>A/D converters which measures the battery voltage and current
 *   <LI>integrated current accumulator which keeps a running
 *       total of all current going into and out of the battery
 *   <LI>elapsed time meter
 *   <LI>40 bytes of nonvolatile EEPROM memory for storage of important parameters
 *   <LI>Operating temperature range from -40&#176C to
 *        +85&#176Ci
 * </UL>
 *
 * <H2>Note</H2>
 *  <P>
 *  Sometimes the VAD input will report 10.23 V even if nothing is attached.
 *  This value is also the maximum voltage that part can report.
 *  </P>
 *

 * <H3> DataSheet </H3>
 * <DL>
 * <DD>http://pdfserv.maxim-ic.com/arpdf/DS2438.pdf (not active yet, Sep-06-2001)
 * <DD><A HREF="http://www.ibutton.com/weather/humidity.html">http://www.ibutton.com/weather/humidity.html</A>
 * </DL>
 *
 *  @version    0.00, 28 Aug 2000
 *  @author     COlmstea
 *
 */
public class OneWireContainer26
   extends OneWireContainer
   implements ADContainer, TemperatureContainer, ClockContainer, HumidityContainer
{

   /**
    * Memory commands.
    */
   private static final byte READ_SCRATCHPAD_COMMAND  = ( byte ) 0xBE;
   private static final byte RECALL_MEMORY_COMMAND    = ( byte ) 0xB8;
   private static final byte COPY_SCRATCHPAD_COMMAND  = ( byte ) 0x48;
   private static final byte WRITE_SCRATCHPAD_COMMAND = ( byte ) 0x4E;
   private static final byte CONVERT_TEMP_COMMAND     = ( byte ) 0x44;
   private static final byte CONVERT_VOLTAGE_COMMAND  = ( byte ) 0xB4;

   /**
    * Channel selector for the VDD input.  Meant to be used with
    * a battery.
    */
   public static final int CHANNEL_VDD = 0x00;

   /**
    * Channel selector for the VAD input.  This is the general purpose
    * A-D input.
    */
   public static final int CHANNEL_VAD = 0x01;

   /**
    * Channel selectro the the IAD input.  Measures voltage across
    * a resistor, Rsense, for calculating current.
    */
   public static final int CHANNEL_VSENSE = 0x02;

   /**
    * Flag to set/check the Current A/D Control bit with setFlag/getFlag. When
    * this bit is true, the current A/D and the ICA are enabled and
    * current measurements will be taken at the rate of 36.41 Hz.
    */
   public static final byte IAD_FLAG = 0x01;

   /**
    * Flag to set/check the Current Accumulator bit with setFlag/getFlag. When
    * this bit is true, both the total discharging and charging current are
    * integrated into seperate registers and can be used for determining
    * full/empty levels.  When this bit is zero the memory (page 7) can be used
    * as user memory.
    */
   public static final byte CA_FLAG = 0x02;

   /**
    * Flag to set/check the Current Accumulator Shadow Selector bit with
    * setFlag/getFlag.  When this bit is true the CCA/DCA registers used to
    * add up charging/discharging current are shadowed to EEPROM to protect
    * against loss of data if the battery pack becomes discharged.
    */
   public static final byte EE_FLAG = 0x04;

   /**
    * Flag to set/check the voltage A/D Input Select Bit with setFlag/getFlag
    * When this bit is true the battery input is (VDD) is selected as input for
    * the voltage A/D input. When false the general purpose A/D input (VAD) is
    * selected as the voltage A/D input.
    */
   public static final byte AD_FLAG = 0x08;

   /**
    * Flag to check whether or not a temperature conversion is in progress
    * using getFlag().
    */
   public static final byte TB_FLAG = 0x10;

   /**
    * Flag to check whether or not an operation is being performed on the
    * nonvolatile memory using getFlag.
    */
   public static final byte NVB_FLAG = 0x20;

   /**
    * Flag to check whether or not the A/D converter is busy using getFlag().
    */
   public static final byte ADB_FLAG = 0x40;

   /**
    * Holds the value of the sensor resistance.
    */
   private double Rsens = .05;

   /**
    * Flag to indicate need to check speed
    */
   private boolean    doSpeedEnable = true;

   //--------
   //-------- Constructors
   //--------

   /**
    * Default constructor
    */
   public OneWireContainer26 ()
   {
      super();
   }

   /**
    * Create a container with a provided adapter object
    * and the address of the 1-Wire device.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this 1-Wire device
    * @param  newAddress        address of this 1-Wire device
    */
   public OneWireContainer26 (DSPortAdapter sourceAdapter, byte[] newAddress)
   {
      super(sourceAdapter, newAddress);
   }

   /**
    * Create a container with a provided adapter object
    * and the address of the 1-Wire device.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this 1-Wire device
    * @param  newAddress        address of this 1-Wire device
    */
   public OneWireContainer26 (DSPortAdapter sourceAdapter, long newAddress)
   {
      super(sourceAdapter, newAddress);
   }

   /**
    * Create a container with a provided adapter object
    * and the address of the 1-Wire device.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this 1-Wire device
    * @param  newAddress        address of this 1-Wire device
    */
   public OneWireContainer26 (DSPortAdapter sourceAdapter, String newAddress)
   {
      super(sourceAdapter, newAddress);
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
      Vector bank_vector = new Vector(8);

      // Status
      bank_vector.addElement(new MemoryBankSBM(this));

      // Temp/Volt/Current
      MemoryBankSBM temp = new MemoryBankSBM(this);
      temp.bankDescription      = "Temperature/Voltage/Current";
      temp.generalPurposeMemory = false;
      temp.startPhysicalAddress = 1;
      temp.size                 = 6;
      temp.readWrite            = false;
      temp.readOnly             = true;
      temp.nonVolatile          = false;
      temp.powerDelivery        = false;
      bank_vector.addElement(temp);

      // Threshold
      temp = new MemoryBankSBM(this);
      temp.bankDescription      = "Threshold";
      temp.generalPurposeMemory = false;
      temp.startPhysicalAddress = 7;
      temp.size                 = 1;
      temp.readWrite            = true;
      temp.readOnly             = false;
      temp.nonVolatile          = true;
      temp.powerDelivery        = true;
      bank_vector.addElement(temp);

      // Elapsed Timer Meter
      temp = new MemoryBankSBM(this);
      temp.bankDescription      = "Elapsed Timer Meter";
      temp.generalPurposeMemory = false;
      temp.startPhysicalAddress = 8;
      temp.size                 = 5;
      temp.readWrite            = true;
      temp.readOnly             = false;
      temp.nonVolatile          = false;
      temp.powerDelivery        = true;
      bank_vector.addElement(temp);

      // Current Offset
      temp = new MemoryBankSBM(this);
      temp.bankDescription      = "Current Offset";
      temp.generalPurposeMemory = false;
      temp.startPhysicalAddress = 13;
      temp.size                 = 2;
      temp.readWrite            = true;
      temp.readOnly             = false;
      temp.nonVolatile          = true;
      temp.powerDelivery        = true;
      bank_vector.addElement(temp);

      // Disconnect / End of Charge
      temp = new MemoryBankSBM(this);
      temp.bankDescription      = "Disconnect / End of Charge";
      temp.generalPurposeMemory = false;
      temp.startPhysicalAddress = 16;
      temp.size                 = 8;
      temp.readWrite            = true;
      temp.readOnly             = false;
      temp.nonVolatile          = false;
      temp.powerDelivery        = true;
      bank_vector.addElement(temp);

      // User Main Memory
      temp = new MemoryBankSBM(this);
      temp.bankDescription      = "User Main Memory";
      temp.generalPurposeMemory = true;
      temp.startPhysicalAddress = 24;
      temp.size                 = 32;
      temp.readWrite            = true;
      temp.readOnly             = false;
      temp.nonVolatile          = true;
      temp.powerDelivery        = true;
      bank_vector.addElement(temp);

      // User Memory / CCA / DCA
      temp = new MemoryBankSBM(this);
      temp.bankDescription      = "User Memory / CCA / DCA";
      temp.generalPurposeMemory = false;
      temp.startPhysicalAddress = 56;
      temp.size                 = 8;
      temp.readWrite            = true;
      temp.readOnly             = false;
      temp.nonVolatile          = true;
      temp.powerDelivery        = true;
      bank_vector.addElement(temp);

      return bank_vector.elements();
   }

   /**
    *  Returns the Dallas Semiconductor part number of this 1-Wire device
    *  as a string.
    *
    *  @return representation of this 1-Wire device's name
    *
    */
   public String getName ()
   {
      return "DS2438";
   }

   /**
    *  Return the alternate Dallas Semiconductor part number or name.
    *  ie. Smart Battery Monitor
    *
    *  @return representation of the alternate name(s)
    */
   public String getAlternateNames ()
   {
      return "Smart Battery Monitor";
   }

   /**
    *  Return a short description of the function of this 1-Wire device type.
    *
    *  @return representation of the functional description
    */
   public String getDescription ()
   {
      return "1-Wire device that integrates the total current charging or "
             + "discharging through a battery and stores it in a register. "
             + "It also returns the temperature (accurate to 2 degrees celcius),"
             + " as well as the instantaneous current and voltage and also "
             + "provides 40 bytes of EEPROM storage.";
   }

   /**
    * Set the value of the sense resistor used to determine
    * battery current.  This value is used in the <CODE>getCurrent()</CODE> calculation.
    * See the DS2438 datasheet for more information on sensing battery
    * current.
    *
    * @param resistance Value of the sense resistor in Ohms.
    */
   public synchronized void setSenseResistor (double resistance)
   {
      Rsens = resistance;
   }

   /**
    * Get the value used for the sense resistor in the <CODE>getCurrent()</CODE>
    * calculations.
    *
    * @return currently stored value of the sense resistor in Ohms
    */
   public double getSenseResistor ()
   {
      return Rsens;
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
    * Reads the specified 8 byte page and returns the data in an array.
    *
    * @param page the page number to read
    *
    * @return  eight byte array that make up the page
    *
    * @throws OneWireIOException Error reading data
    * @throws OneWireException Could not find part
    * @throws IllegalArgumentException Bad parameters passed
    */
   public byte[] readPage (int page)
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {
      byte[] buffer = new byte [11];
      byte[] result = new byte [8];
      int    crc8;   // this device uses a crc 8

      /* check validity of parameter */
      if ((page < 0) || (page > 7))
         throw new IllegalArgumentException("OneWireContainer26-Page " + page
                                            + " is an invalid page.");

      /* perform the read/verification */
      if (doSpeedEnable)
         doSpeed();

      if (adapter.select(address))
      {

         /* recall memory to the scratchpad */
         buffer [0] = RECALL_MEMORY_COMMAND;
         buffer [1] = ( byte ) page;

         adapter.dataBlock(buffer, 0, 2);

         /* perform the read scratchpad */
         adapter.reset();
         adapter.select(address);

         buffer [0] = READ_SCRATCHPAD_COMMAND;
         buffer [1] = ( byte ) page;

         for (int i = 2; i < 11; i++)
            buffer [i] = ( byte ) 0x0ff;

         adapter.dataBlock(buffer, 0, 11);

         /* do the crc check */
         crc8 = CRC8.compute(buffer, 2, 9);

         if (crc8 != 0x0)
            throw new OneWireIOException(
               "OneWireContainer26-Bad CRC during read." + crc8);

         // copy the data into the result
         System.arraycopy(buffer, 2, result, 0, 8);
      }
      else
         throw new OneWireException("OneWireContainer26-device not found.");

      return result;
   }

   /**
    * Writes a page of memory to this device. Pages 3-6 are always
    * available for user storage and page 7 is available if the CA bit is set
    * to 0 (false) with <CODE>setFlag()</CODE>.
    *
    * @param page    the page number
    * @param source  data to be written to page
    * @param offset  offset with page to begin writting
    *
    * @throws OneWireIOException Error reading data
    * @throws OneWireException Could not find part
    * @throws IllegalArgumentException Bad parameters passed
    */
   public void writePage (int page, byte[] source, int offset)
      throws OneWireIOException, OneWireException
   {
      byte[] buffer = new byte [10];

      /* check parameter validity */
      if ((page < 0) || (page > 7))
         throw new IllegalArgumentException("OneWireContainer26-Page " + page
                                            + " is an invalid page.");

      if (source.length < 8)
         throw new IllegalArgumentException(
            "OneWireContainer26-Invalid data page passed to writePage.");

      if (doSpeedEnable)
         doSpeed();

      if (adapter.select(address))
      {

         // write the page to the scratchpad first
         buffer [0] = WRITE_SCRATCHPAD_COMMAND;
         buffer [1] = ( byte ) page;

         System.arraycopy(source, offset, buffer, 2, 8);
         adapter.dataBlock(buffer, 0, 10);

         // now copy that part of the scratchpad to memory
         adapter.reset();
         adapter.select(address);

         buffer [0] = COPY_SCRATCHPAD_COMMAND;
         buffer [1] = ( byte ) page;

         adapter.dataBlock(buffer, 0, 2);
      }
      else
         throw new OneWireException("OneWireContainer26-Device not found.");
   }

   /**
    * Checks the specified flag in the status/configuration register
    * and returns its status as a boolean.
    *
    * @param  flagToGet flag bitmask.
    * Acceptable parameters: IAD_FLAG, CA_FLAG, EE_FLAG, AD_FLAG, TB_FLAG,
    * NVB_FLAG, ADB_FLAG
    * (may be ORed with | to check the status of more than one).
    *
    * @return state of flag
    *
    * @throws OneWireIOException Error reading data
    * @throws OneWireException Could not find part
    * @throws IllegalArgumentException Bad parameters passed
    */
   public boolean getFlag (byte flagToGet)
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {
      byte[] data = readPage(0);

      if ((data [0] & flagToGet) != 0)
         return true;

      return false;
   }

   /**
    * Set one of the flags in the STATUS/CONFIGURATION register.
    *
    * @param bitmask of the flag to set
    * Acceptable parameters: IAD_FLAG, CA_FLAG, EE_FLAG, AD_FLAG, TB_FLAG,
    * NVB_FLAG, ADB_FLAG.
    *
    * @param flagValue value to set flag to
    *
    * @throws OneWireIOException Error writting data
    * @throws OneWireException Could not find part
    * @throws IllegalArgumentException Bad parameters passed
    */
   public void setFlag (byte flagToSet, boolean flagValue)
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {
      byte[] data = readPage(0);

      if (flagValue)
         data [0] = ( byte ) (data [0] | flagToSet);
      else
         data [0] = ( byte ) (data [0] & ~(flagToSet));

      writePage(0, data, 0);
   }

   /**
    * Get the instantaneous current. The IAD flag must be true!!
    * Remember to set the Sense resistor value using
    * <CODE>setSenseResitor(double)</CODE>.
    *
    *
    * @param state current state of device
    * @return current value in Amperes
    */
   public double getCurrent (byte[] state)
   {
      short rawCurrent = ( short ) ((state [6] << 8) | (state [5] & 0x0ff));

      return rawCurrent / (4096.0 * Rsens);
   }

   /**
    * Calculate the remaining capacity in mAH as outlined in the data sheet.
    *
    * @return battery capacity remaining in mAH
    *
    * @throws OneWireIOException Error reading data
    * @throws OneWireException Could not find part
    * @throws IllegalArgumentException Bad parameters passed
    */
   public double getRemainingCapacity ()
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {
      int ica = getICA();

      return (1000 * ica / (2048 * Rsens));
   }

   /**
    * Determines if the battery is charging and returns a boolean.
    *
    *
    * @param state current state of device
    *
    * @return true if battery is changing, false if battery is idle or discharging
    *
    * @throws OneWireIOException Error reading data
    * @throws OneWireException Could not find part
    * @throws IllegalArgumentException Bad parameters passed
    */
   public boolean isCharging (byte[] state)
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {

      // positive current (if the thing is hooked up right) is charging
      if (getCurrent(state) > 0)
         return true;

      return false;
   }

   /**
    * Calibrate the current ADC. Although the part is shipped calibrated,
    * calibrations should be done whenever possible for best results.
    * NOTE: You MUST force zero current through Rsens (the sensor resistor)
    * while calibrating.
    *
    * @throws OneWireIOException Error calibrating
    * @throws OneWireException Could not find part
    * @throws IllegalArgumentException Bad parameters passed
    */
   public void calibrateCurrentADC ()
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {
      byte[] data;
      byte   currentLSB, currentMSB;

      // grab the current IAD settings so that we dont change anything
      boolean IADvalue = getFlag(IAD_FLAG);

      // the IAD bit must be set to "0" to write to the Offset Register
      setFlag(IAD_FLAG, false);

      // write all zeroes to the offset register
      data     = readPage(1);
      data [5] = data [6] = 0;

      writePage(1, data, 0);

      // enable current measurements once again
      setFlag(IAD_FLAG, true);

      // read the Current Register value
      data       = readPage(0);
      currentLSB = data [5];
      currentMSB = data [6];

      // disable current measurements so that we can write to the offset reg
      setFlag(IAD_FLAG, false);

      // change the sign of the current register value and store it as the offset
      data     = readPage(1);
      data [5] = ( byte ) (~(currentLSB) + 1);
      data [6] = ( byte ) (~(currentMSB));

      writePage(1, data, 0);

      // eset the IAD settings back to normal
      setFlag(IAD_FLAG, IADvalue);
   }

   /**
    * Set the minimum current measurement magnitude for which the ICA/CCA/DCA
    * are incremented. This is important for applications where the current
    * may get very small for long periods of time. Small currents can be
    * inaccurate by a high percentage, which leads to very inaccurate
    * accumulations.
    *
    * @param threshold minimum number of bits a current measurement must have to be accumulated,
    * Only 0,2,4 and 8 are valid parameters
    *
    * @throws OneWireIOException Error setting the threshold
    * @throws OneWireException Could not find part
    * @throws IllegalArgumentException Bad parameters passed
    */
   public void setThreshold (byte thresholdValue)
      throws OneWireIOException, OneWireException
   {
      byte   thresholdReg;
      byte[] data;

      switch (thresholdValue)
      {

         case 0 :
            thresholdReg = 0;
            break;
         case 2 :
            thresholdReg = 64;
            break;
         case 4 :
            thresholdReg = ( byte ) 128;
            break;
         case 8 :
            thresholdReg = ( byte ) 192;
            break;
         default :
            throw new IllegalArgumentException(
               "OneWireContainer26-Threshold value must be 0,2,4, or 8.");
      }

      // first save their original IAD settings so we dont change anything
      boolean IADvalue = getFlag(IAD_FLAG);

      // current measurements must be off to write to the threshold register
      setFlag(IAD_FLAG, false);

      // write the threshold register
      data     = readPage(0);
      data [7] = thresholdReg;

      writePage(0, data, 0);

      // set the IAD back to the way the user had it
      setFlag(IAD_FLAG, IADvalue);
   }

   /**
    * Retrieves the current ICA value in mVHr.
    *
    * @return value in the ICA register
    *
    * @throws OneWireIOException Error reading data
    * @throws OneWireException Could not find part
    * @throws IllegalArgumentException Bad parameters passed
    */
   public int getICA ()
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {
      byte[] data = readPage(1);

      return ( int ) (data [4] & 0x000000ff);
   }

   /**
    * Retrieves the current CCA value in mVHr. This value is accumulated over
    * the lifetime of the part (until it is set to 0 or the CA flag is set
    * to false) and includes only charging current (positive).
    *
    * @return CCA value
    *
    * @throws OneWireIOException Error reading data
    * @throws OneWireException Could not find part
    * @throws IllegalArgumentException Bad parameters passed
    */
   public int getCCA ()
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {
      byte[] data = readPage(7);

      return ((data [5] << 8) & 0x0000ff00) | (data [4] & 0x000000ff);
   }

   /**
    * Retrieves the value of the DCA in mVHr. This value is accumulated over
    * the lifetime of the part (until explicitly set to 0 or if the CA flag
    * is set to false) and includes only discharging current (negative).
    *
    * @return DCA value
    *
    * @throws OneWireIOException Error reading data
    * @throws OneWireException Could not find part
    * @throws IllegalArgumentException Bad parameters passed
    */
   public int getDCA ()
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {
      byte[] data = readPage(7);

      return ((data [7] << 8) & 0x0000ff00) | (data [6] & 0x000000ff);
   }

   /**
    * Set the value of the ICA.
    *
    * @param icaValue  new ICA value
    *
    * @throws OneWireIOException Error writing data
    * @throws OneWireException Could not find part
    * @throws IllegalArgumentException Bad parameters passed
    */
   public void setICA (int icaValue)
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {
      byte[] data = readPage(1);

      data [4] = ( byte ) (icaValue & 0x000000ff);

      writePage(1, data, 0);
   }

   /**
    * Set the value of the CCA.
    *
    * @param ccaValue new CCA value
    *
    * @throws OneWireIOException Error writing data
    * @throws OneWireException Could not find part
    * @throws IllegalArgumentException Bad parameters passed
    */
   public void setCCA (int ccaValue)
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {
      byte[] data = readPage(7);

      data [4] = ( byte ) (ccaValue & 0x00ff);
      data [5] = ( byte ) ((ccaValue & 0xff00) >>> 8);

      writePage(7, data, 0);
   }

   /**
    * Set the value of the DCA.
    *
    * @param dcaValue new DCA value
    *
    * @throws OneWireIOException Error writing data
    * @throws OneWireException Could not find part
    * @throws IllegalArgumentException Bad parameters passed
    */
   public void setDCA (int dcaValue)
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {
      byte[] data = readPage(7);

      data [6] = ( byte ) (dcaValue & 0x00ff);
      data [7] = ( byte ) ((dcaValue & 0xff00) >>> 8);

      writePage(7, data, 0);
   }

   /**
    * This method extracts the Clock Value in milliseconds from the
    * state data retrieved from the <CODE>readDevice()</CODE> method.
    *
    * @param state device state
    *
    * @return time in milliseconds that have
    * occured since 1970
    */
   public long getDisconnectTime (byte[] state)
   {
      return getTime(state, 16) * 1000;
   }

   /**
    * This method extracts the Clock Value in milliseconds from the
    * state data retrieved from the <CODE>readDevice()</CODE> method.
    *
    * @param state device state
    *
    * @return time in milliseconds that have
    * occured since 1970
    */
   public long getEndOfChargeTime (byte[] state)
   {
      return getTime(state, 20) * 1000;
   }

   //actually could be called byteArrayToLong, only used in time functions
   private long getTime (byte[] state, int start)
   {
      long time = (state [start] & 0x0ff)
                  | ((state [start + 1] & 0x0ff) << 8)
                  | ((state [start + 2] & 0x0ff) << 16)
                  | ((state [start + 3] & 0x0ff) << 24);

      return time & 0x0ffffffff;
   }

   //////////////////////////////////////////////////////////////////////////////
   //
   //      INTERFACE METHODS!!!!!!!!
   //
   //////////////////////////////////////////////////////////////////////////////

   /**
     * Query to get the number of channels supported by this A/D.
     * Channel specific methods will use a channel number specified
     * by an integer from [0 to (getNumberChannels() - 1)].
     *
     * @return number of channels
     */
   public int getNumberADChannels ()
   {
      return 3;   //has VDD, VAD channel  (battery, gen purpose)
                  // and it has a Vsense channel for current sensing
   }

   /**
    * Query to see if this A/D measuring device has high/low
    * alarms.
    *
    * @return true if has high/low trips
    */
   public boolean hasADAlarms ()
   {
      return false;
   }

   /**
    * Query to get an array of available ranges for the specified
    * A/D channel.
    *
    * @param channel  channel in the range
    *                  [0 to (getNumberChannels() - 1)]
    *
    * @return available ranges
    */
   public double[] getADRanges (int channel)
   {
      double[] result = new double [1];

      if(channel==CHANNEL_VSENSE)
         result [0] = .250;
      else
         result [0] = 10.23;

      /* for VAD, not entirely true--this should be
         2 * VDD.  If you hook up VDD to the
         one-wire in series with a diode and then
         hang a .1 microF capacitor off the line to ground,
         you can get about 9.5 for the high end accurately
                       ----------------------------------
                       |             *****************  |
         One-Wire------- DIODE-------*VDD     ONEWIRE*---
                                 |   *               *
                                 |   *        GROUND *---
                                 C   *               *  |
                                 |   *    2438       *  |
                                gnd  *               *  |
                                 |   *****************  |
                                 |----------------------|

       */
      return result;
   }

   /**
    * Query to get an array of available resolutions based
    * on the specified range on the specified A/D channel.
    *
    * @param channel channel in the range
    *                  [0 to (getNumberChannels() - 1)]
    * @param range A/D range
    *
    * @return available resolutions
    */
   public double[] getADResolutions (int channel, double range)
   {
      double[] result = new double [1];

      if(channel == CHANNEL_VSENSE)
         result [0] = 0.2441;
      else
         result [0] = 0.01;   //10 mV

      return result;
   }

   /**
    * Query to see if this A/D supports doing multiple voltage
    * conversions at the same time.
    *
    * @return true if device can do multi-channel voltage reads
    */
   public boolean canADMultiChannelRead ()
   {
      return false;
   }

   //--------
   //-------- A/D IO Methods
   //--------

   /**
    * This method is used to perform voltage conversion on all specified
    * channels.  The method 'getVoltage()' can be used to read the result
    * of the conversion.
    *
    * @param channel channel in the range
    *                 <CODE> [0 to (getNumberChannels() - 1)]</CODE>
    * @param state  current state of the
    *               device returned from <CODE>readDevice()</CODE>
    *
    * @throws OneWireIOException Error writing data
    * @throws OneWireException Could not find part
    */
   public void doADConvert (int channel, byte[] state)
      throws OneWireIOException, OneWireException
   {
      if(channel == CHANNEL_VSENSE)
      {
         if((state[0]&IAD_FLAG) == 0)
         {
            // enable the current sense channel
            setFlag(IAD_FLAG, true);
            state[0] |= IAD_FLAG;
            try
            {
               // updates once every 27.6 milliseconds
               Thread.sleep(30);
            }
            catch (InterruptedException e){}
         }

         byte[] data = readPage(0);
         // update the state
         System.arraycopy(data, 5, state, 5, 2);
      }
      else
      {
         setFlag(AD_FLAG, channel == CHANNEL_VDD);

         // first perform the conversion
         if (doSpeedEnable)
            doSpeed();

         if (adapter.select(address))
         {
            adapter.putByte(CONVERT_VOLTAGE_COMMAND);

            try
            {
               Thread.sleep(4);
            }
            catch (InterruptedException e){}

            byte[] data = readPage(0);

            //let's update state with this info
            System.arraycopy(data, 0, state, 0, 8);

            // save off the voltage in our state's holdindg area
            state [24 + channel * 2]     = data [4];
            state [24 + channel * 2 + 1] = data [3];
         }
         else
            throw new OneWireException("OneWireContainer26-Device not found.");
      }
   }

   /**
    * This method is used to perform voltage conversion on all specified
    * channels.  The method <CODE>getVoltage()</CODE> can be used to read the result
    * of the conversion. This A/D must support multi-channel read
    * <CODE>canMultiChannelRead()</CODE> if there are more then 1 channel is specified.
    *
    * @param doConvert  channels
    *                    to perform conversion on
    * @param state  current state of the
    *               device returned from <CODE>readDevice()</CODE>
    *
    * @throws OneWireIOException Error writing data
    * @throws OneWireException Could not find part
    */
   public void doADConvert (boolean[] doConvert, byte[] state)
      throws OneWireIOException, OneWireException
   {
      throw new OneWireException("This device cannot do multi-channel reads");
   }

   /**
    * This method is used to read the voltage values.  Must
    * be used after a <CODE>doADConvert()</CODE> method call.  Also must
    * include the last valid state from the <CODE>readDevice()</CODE> method
    * and this A/D must support multi-channel read <CODE>canMultiChannelRead()</CODE>
    * if there are more then 1 channel.
    *
    * @param state current state of the
    *               device returned from <CODE>readDevice()</CODE>
    *
    * @return voltage values for all channels
    *
    * @throws OneWireIOException Error reading data
    * @throws OneWireException Could not find part
    */
   public double[] getADVoltage (byte[] state)
      throws OneWireIOException, OneWireException
   {
      throw new OneWireException("This device cannot do multi-channel reads");
   }

   /**
    * This method is used to read a channels voltage value.  Must
    * be used after a <CODE>doADConvert()</CODE> method call.  Also must
    * include the last valid state from the <CODE>readDevice()</CODE> method.
    * Note, if more then one channel is to be read then it is more
    * efficient to use the <CODE>getVoltage()</CODE> method that returns all
    * channel values.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param state current state of the
    *               device returned from <CODE>readDevice()</CODE>
    *
    * @return   voltage value for the specified
    *                  channel
    *
    * @throws OneWireIOException Error reading data
    * @throws OneWireException Could not find part
    */
   public double getADVoltage (int channel, byte[] state)
      throws OneWireIOException, OneWireException
   {
      double result = 0;

      if(channel == CHANNEL_VSENSE)
         result = ((state [6] << 8) | (state [5] & 0x0ff))/4096d;
      else
         result = (((state [24 + channel*2] << 8) & 0x00300) |
                    (state [24 + channel*2 + 1] & 0x0ff))
                   / 100.0d;

      return result;
   }

   //--------
   //-------- A/D 'get' Methods
   //--------

   /**
    * This method is used to extract the alarm voltage value of the
    * specified channel from the provided state buffer.  The
    * state buffer is retrieved from the <CODE>readDevice()</CODE> method.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param alarmType desired alarm, <CODE>ALARM_HIGH (1)
    *               or ALARM_LOW (0)</CODE>
    * @param state current state of the
    *               device returned from <CODE>readDevice()</CODE>
    *
    * @return alarm_value in volts
    *
    * @throws OneWireException Device does not support A/D alarms
    */
   public double getADAlarm (int channel, int alarmType, byte[] state)
      throws OneWireException
   {
      throw new OneWireException("This device does not have A/D alarms");
   }

   /**
    * This method is used to extract the alarm enable value of the
    * specified channel from the provided state buffer.  The state
    * buffer is retrieved from the <CODE>readDevice()</CODE> method.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param alarmType desired alarm, <CODE>ALARM_HIGH (1)
    *               or ALARM_LOW (0)</CODE>
    * @param state current state of the state
    *               returned from <CODE>readDevice()</CODE>
    *
    * @return true if specified alarm is enabled
    *
    * @throws OneWireException Device does not support A/D alarms
    */
   public boolean getADAlarmEnable (int channel, int alarmType, byte[] state)
      throws OneWireException
   {
      throw new OneWireException("This device does not have A/D alarms");
   }

   /**
    * This method is used to check the alarm event value of the
    * specified channel from the provided state buffer.  The
    * state buffer is retrieved from the <CODE>readDevice()</CODE> method.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param alarmType desired alarm, <CODE>ALARM_HIGH (1)
    *               or ALARM_LOW (0)</CODE>
    * @param state current state of the state
                    returned from <CODE>readDevice()</CODE>
    *
    * @return true if specified alarm occurred
    *
    * @throws OneWireException Device does not support A/D alarms
    */
   public boolean hasADAlarmed (int channel, int alarmType, byte[] state)
      throws OneWireException
   {
      throw new OneWireException("This device does not have A/D alarms");
   }

   /**
    * This method is used to extract the conversion resolution of the
    * specified channel from the provided state buffer expressed in
    * volts.  The state is retrieved from the
    * <CODE>readDevice()</CODE> method.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param state current state of the state
    *               returned from <CODE>readDevice()</CODE>
    *
    * @return resolution of channel in volts
    */
   public double getADResolution (int channel, byte[] state)
   {

      //this is easy, its always 0.01 V = 10 mV
      return 0.01;
   }

   /**
    * This method is used to extract the input voltage range of the
    * specified channel from the provided state buffer.  The state
    * buffer is retrieved from the <CODE>readDevice()</CODE> method.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param state current state of the state
    *                  returned from <CODE>readDevice()</CODE>
    *
    * @return input voltage range
    */
   public double getADRange (int channel, byte[] state)
   {
      if(channel==CHANNEL_VSENSE)
         return .250;
      else
         return 10.23;
   }

   //--------
   //-------- A/D 'set' Methods
   //--------

   /**
    * This method is used to set the alarm voltage value of the
    * specified channel in the provided state buffer.  The
    * state buffer is retrieved from the <CODE>readDevice()</CODE> method.
    * The method <CODE>writeDevice()</CODE> must be called to finalize these
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <CODE>writeDevice()</CODE>.
    *
    * @param channel  channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param alarmType desired alarm, <CODE>ALARM_HIGH (1)
    *               or ALARM_LOW (0)</CODE>
    * @param alarm  alarm value (will be reduced to 8 bit resolution)
    * @param state  current state of the
    *               device returned from <CODE>readDevice()</CODE>
    *
    * @throws OneWireException Device does not support A/D alarms
    */
   public void setADAlarm (int channel, int alarmType, double alarm,
                           byte[] state)
      throws OneWireException
   {
      throw new OneWireException("This device does not have A/D alarms");
   }

   /**
    * This method is used to set the alarm enable value of the
    * specified channel in the provided state buffer.  The
    * state buffer is retrieved from the <CODE>readDevice()</CODE> method.
    * The method <CODE>writeDevice()</CODE> must be called to finalize these
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <CODE>writeDevice()</CODE>.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param alarmType desired alarm, <CODE>ALARM_HIGH (1)
    *               or ALARM_LOW (0)</CODE>
    * @param alarmEnable alarm enable value
    * @param state current state of the
    *               device returned from <CODE>readDevice()</CODE>
    *
    * @throws OneWireException Device does not support A/D alarms
    */
   public void setADAlarmEnable (int channel, int alarmType,
                                 boolean alarmEnable, byte[] state)
      throws OneWireException
   {
      throw new OneWireException("This device does not have AD alarms");
   }

   /**
    * This method is used to set the conversion resolution value for the
    * specified channel in the provided state buffer.  The
    * state buffer is retrieved from the <CODE>readDevice()</CODE> method.
    * The method <CODE>writeDevice()</CODE> must be called to finalize these
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <CODE>writeDevice()</CODE>.
    *
    * @param channel  channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param resolution resolution to use in volts
    * @param state current state of the
    *               device returned from <CODE>readDevice()</CODE>
    */
   public void setADResolution (int channel, double resolution, byte[] state)
   {

      //but you can't select the resolution for this part!!!!
      //just make it an airball
   }

   /**
    * This method is used to set the input range for the
    * specified channel in the provided state buffer.  The
    * state buffer is retrieved from the <CODE>readDevice()</CODE> method.
    * The method <CODE>writeDevice()</CODE> must be called to finalize these
    * changes to the device.  Note that multiple 'set' methods can
    * be called before one call to <CODE>writeDevice()</CODE>.
    *
    * @param channel channel in the range
    *                  <CODE>[0 to (getNumberChannels() - 1)]</CODE>
    * @param range maximum volt range, use
    *                <CODE>getRanges()</CODE> method to get available ranges
    * @param state current state of the
    *               device returned from <CODE>readDevice()</CODE>
    */
   public void setADRange (int channel, double range, byte[] state)
   {

      //you can't change the ranges here without changing VDD!!!
      //just make this function call an airball
   }

   /**
    * This method retrieves the 1-Wire device sensor state.  This state is
    * returned as a byte array.  Pass this byte array to the static query
    * and set methods.  If the device state needs to be changed then call
    * the <CODE>writeDevice()</CODE> to finalize the one or more change.
    *
    * @return 1-Wire device's state
    *
    * @throws OneWireIOException Error reading data
    * @throws OneWireException Could not find part
    */
   public byte[] readDevice ()
      throws OneWireIOException, OneWireException
   {

      //should return the first three pages
      //and then 6 extra bytes, 2 for channel 1 voltage and
      //2 for channel 2 voltage
      byte[] state = new byte [28];

      for (int i = 0; i < 3; i++)
      {
         byte[] pg = readPage(i);

         System.arraycopy(pg, 0, state, i * 8, 8);
      }

      //the last four bytes are used this way...
      //the current voltage reading is kept in page 0,
      //but if a new voltage reading is asked for we move it to the
      //end so it can be there in case it is asked for again,
      //so we kind of weasel around this whole ADcontainer thing

      /* here's a little map
           byte[24] VDD high byte
           byte[25] VDD low byte
           byte[26] VAD high byte
           byte[27] VAD low byte
      */
      return state;
   }

   /**
    * This method write the 1-Wire device sensor state that
    * have been changed by the 'set' methods.  It knows which registers have
    * changed by looking at the bitmap fields appended to the state
    * data.
    *
    * @param  state device's state
    *
    * @throws OneWireIOException Error writting data
    * @throws OneWireException Could not find part
    */
   public void writeDevice (byte[] state)
      throws OneWireIOException, OneWireException
   {
      writePage(0, state, 0);
      writePage(1, state, 8);
   }

   //--------
   //-------- Temperature Feature methods
   //--------

   /**
    * Query to see if this temperature measuring device has high/low
    * trip alarms.
    *
    * @return true if has high/low trip alarms
    */
   public boolean hasTemperatureAlarms ()
   {
      return false;
   }

   /**
    * Query to see if this device has selectable resolution.
    *
    * @return true if device has selectable resolution
    */
   public boolean hasSelectableTemperatureResolution ()
   {
      return false;
   }

   /**
    * Query to get an array of available resolutions in degrees C.
    *
    * @return available resolutions in degrees C
    */
   public double[] getTemperatureResolutions ()
   {
      double[] result = new double [1];

      result [0] = 0.03125;

      return result;
   }

   /**
    * Query to get the high/low resolution in degrees C.
    *
    * @return high/low resolution resolution in degrees C
    *
    * @throws OneWireException Device does not have temperature alarms
    */
   public double getTemperatureAlarmResolution ()
      throws OneWireException
   {
      throw new OneWireException(
         "This device does not have temperature alarms");
   }

   /**
    * Query to get the maximum temperature in degrees C.
    *
    * @return maximum temperature in degrees C
    */
   public double getMaxTemperature ()
   {
      return 125.0;
   }

   /**
    * Query to get the minimum temperature in degrees C.
    *
    * @return minimum temperature in degrees C
    */
   public double getMinTemperature ()
   {
      return -55.0;
   }

   //--------
   //-------- Temperature I/O Methods
   //--------

   /**
    * Perform an temperature conversion.  Use this state information
    * to calculate the conversion time.
    *
    * @param state device state
    *
    * @throws OneWireIOException Error writting data
    * @throws OneWireException Could not find part
    */
   public void doTemperatureConvert (byte[] state)
      throws OneWireIOException, OneWireException
   {
      byte[] data;   // hold page

      if (doSpeedEnable)
         doSpeed();

      if (adapter.select(address))
      {

         // perform the temperature conversion
         adapter.putByte(CONVERT_TEMP_COMMAND);

         try
         {
            Thread.sleep(10);
         }
         catch (InterruptedException Ie){}

         data      = readPage(0);
         state [2] = data [2];
         state [1] = data [1];
      }
      else
         throw new OneWireException("OneWireContainer26-Device not found.");
   }

   //--------
   //-------- Temperature 'get' Methods
   //--------

   /**
    * This method extracts the Temperature Value in degrees C from the
    * state data retrieved from the <CODE>readDevice()</CODE> method.
    *
    * @param state device state
    *
    * @return temperature in degrees C from the last <CODE>doTemperature()</CODE>
    */
   public double getTemperature (byte[] state)
   {
      double temp = (( short ) ((state [2] << 8) | (state [1] & 0x0ff)) >> 3)
                    * 0.03125;

      return temp;
   }

   /**
    * This method extracts the specified Alarm value in degrees C from the
    * state data retrieved from the <CODE>readDevice()</CODE> method.
    *
    * @param alarmType alarm trip type <CODE>ALARM_HIGH (1)
    *               or ALARM_LOW (0)</CODE>
    * @param state device state
    *
    * @return alarm trip temperature in degrees C
    *
    * @throws OneWireException Device does not have temperature alarms
    */
   public double getTemperatureAlarm (int alarmType, byte[] state)
      throws OneWireException
   {
      throw new OneWireException(
         "This device does not have temperature alarms");
   }

   /**
    * This method extracts the current resolution in degrees C from the
    * state data retrieved from the <CODE>readDevice()</CODE> method.
    *
    * @param state device state
    *
    * @return temperature resolution in degrees C
    */
   public double getTemperatureResolution (byte[] state)
   {
      return 0.03125;
   }

   //--------
   //-------- Temperature 'set' Methods
   //--------

   /**
    * This method sets the alarm value in degrees C in the
    * provided state data.  Use the method <CODE>writeDevice()</CODE> with
    * this data to finalize the change to the device.
    *
    * @param alarmType alarm type <CODE>ALARM_HIGH (1)
    *               or ALARM_LOW (0)</CODE>
    * @param alarmValue trip value in degrees C
    * @param state device state
    *
    * @throws OneWireException Device does not have temperature alarms
    */
   public void setTemperatureAlarm (int alarmType, double alarmValue,
                                    byte[] state)
      throws OneWireException, OneWireIOException
   {
      throw new OneWireException(
         "This device does not have temperature alarms");
   }

   /**
    * This method sets the current resolution in degrees C in the
    * provided state data.   Use the method <CODE>writeDevice()</CODE> with
    * this data to finalize the change to the device.
    *
    * @param resolution temperature resolution in degrees C
    * @param state device state
    *
    * @throws OneWireIOException Error writting data
    * @throws OneWireException Could not find part
    */
   public void setTemperatureResolution (double resolution, byte[] state)
      throws OneWireException, OneWireIOException
   {

      //airball, only one resolution!!!
   }

   //--------
   //-------- Clock Feature methods
   //--------

   /**
    * Query to see if the clock has an alarm feature.
    *
    * @return true if real-time-clock has an alarm
    */
   public boolean hasClockAlarm ()
   {
      return false;
   }

   /**
    * Query to see if the clock can be disabled.  See
    * the methods <CODE>isClockRunning()</CODE> and <CODE>setClockRunEnable()</CODE>.
    *
    * @return true if the clock can be enabled and
    * disabled
    */
   public boolean canDisableClock ()
   {
      return false;
   }

   /**
    * Query to get the clock resolution in milliseconds
    *
    * @return clock resolution in milliseconds.
    */
   public long getClockResolution ()
   {
      return 1000;
   }

   //--------
   //-------- Clock 'get' Methods
   //--------

   /**
    * This method extracts the Clock Value in milliseconds from the
    * state data retrieved from the <CODE>readDevice()</CODE> method.
    *
    * @param state  device state
    *
    * @return time in milliseconds that have
    * occured since 1970
    */
   public long getClock (byte[] state)
   {
      return getTime(state, 8) * 1000;
   }

   /**
    * This method extracts the Clock Alarm Value from the provided
    * state data retrieved from the <CODE>readDevice()</CODE>
    * method.
    *
    * @param state device state
    *
    * @return time in milliseconds that have
    * the clock alarm is set to
    *
    * @throws OneWireException Device does not have clock alarm
    */
   public long getClockAlarm (byte[] state)
      throws OneWireException
   {
      throw new OneWireException("This device does not have a clock alarm!");
   }

   /**
    * This method checks if the Clock Alarm flag has been set
    * from the state data retrieved from the
    * <CODE>readDevice()</CODE> method.
    *
    * @param state device state
    *
    * @return true if clock is alarming
    */
   public boolean isClockAlarming (byte[] state)
   {
      return false;
   }

   /**
    * This method checks if the Clock Alarm is enabled
    * from the provided state data retrieved from the
    * <CODE>readDevice()</CODE> method.
    *
    * @param state device state
    *
    * @return true if clock alarm is enabled
    */
   public boolean isClockAlarmEnabled (byte[] state)
   {
      return false;
   }

   /**
    * This method checks if the device's oscilator is enabled.  The clock
    * will not increment if the clock is not enabled.
    * This value is read from the provided state data retrieved from the
    * <CODE>readDevice()</CODE> method.
    *
    * @param state device state
    *
    * @return true if clock is running
    */
   public boolean isClockRunning (byte[] state)
   {
      return true;
   }

   //--------
   //-------- Clock 'set' Methods
   //--------

   /**
    * This method sets the Clock time in the provided state data
    * Use the method <CODE>writeDevice()</CODE> with
    * this data to finalize the change to the device.
    *
    * @param time new clock setting in milliseconds
    * @param state device state
    */
   public void setClock (long time, byte[] state)
   {
      time       = time / 1000;   //convert to seconds
      state [8]  = ( byte ) time;
      state [9]  = ( byte ) (time >> 8);
      state [10] = ( byte ) (time >> 16);
      state [11] = ( byte ) (time >> 24);
   }

   /**
    * This method sets the Clock Alarm in the provided state
    * data.  Use the method <CODE>writeDevice()</CODE> with
    * this data to finalize the change to the device.
    *
    * @param time new clock setting in mlliseconds
    * @param state device state
    *
    * @throws OneWireException Device does not support clock alarm
    */
   public void setClockAlarm (long time, byte[] state)
      throws OneWireException
   {
      throw new OneWireException("This device does not have a clock alarm!");
   }

   /**
    * This method sets the oscillator enable to the specified
    * value. Use the method <CODE>writeDevice()</CODE> with this
    * data to finalize the change to the device.
    *
    * @param runEnable true to enable clock oscillator
    * @param state device state
    *
    * @throws OneWireException Device does not support disabled clock
    */
   public void setClockRunEnable (boolean runEnable, byte[] state)
      throws OneWireException
   {
      if (!runEnable)
         throw new OneWireException(
            "This device's clock cannot be disabled!");
   }

   /**
    * This method sets the Clock Alarm enable. Use the method
    * <CODE>writeDevice()</CODE> with this data to finalize the
    * change to the device.
    *
    * @param  alarmEnable - true to enable clock alarm
    * @param state device state
    *
    * @throws OneWireException Device does not support clock alarm
    */
   public void setClockAlarmEnable (boolean alarmEnable, byte[] state)
      throws OneWireException
   {
      throw new OneWireException("This device does not have a clock alarm!");
   }

   //--------
   //-------- Humidity Feature methods
   //--------

   /**
    * Checks to see if humidity value given is a 'relative' humidity value.
    *
    * @return <code>true</code> if this <code>HumidityContainer</code>
    *         provides a relative humidity reading
    *
    * @see    #getHumidityResolution
    * @see    #getHumidityResolutions
    * @see    #setHumidityResolution
    */
   public boolean isRelative()
   {
      return true;
   }

   /**
    * Checks to see if this Humidity measuring device has high/low
    * trip alarms.
    *
    * @return <code>true</code> if this <code>HumidityContainer</code>
    *         has high/low trip alarms
    *
    * @see    #getHumidityAlarm
    * @see    #setHumidityAlarm
    */
   public boolean hasHumidityAlarms()
   {
      return false;
   }

   /**
    * Checks to see if this device has selectable Humidity resolution.
    *
    * @return <code>true</code> if this <code>HumidityContainer</code>
    *         has selectable Humidity resolution
    *
    * @see    #getHumidityResolution
    * @see    #getHumidityResolutions
    * @see    #setHumidityResolution
    */
   public boolean hasSelectableHumidityResolution ()
   {
      return false;
   }

   /**
    * Get an array of available Humidity resolutions in percent humidity (0 to 100).
    *
    * @return byte array of available Humidity resolutions in percent with
    *         minimum resolution as the first element and maximum resolution
    *         as the last element.
    *
    * @see    #hasSelectableHumidityResolution
    * @see    #getHumidityResolution
    * @see    #setHumidityResolution
    */
   public double[] getHumidityResolutions ()
   {
      double[] result = new double [1];

      result [0] = 0.1;

      return result;
   }

   /**
    * Gets the Humidity alarm resolution in percent.
    *
    * @return Humidity alarm resolution in percent for this 1-wire device
    *
    * @throws OneWireException         Device does not support Humidity
    *                                  alarms
    *
    * @see    #hasHumidityAlarms
    * @see    #getHumidityAlarm
    * @see    #setHumidityAlarm
    *
    */
   public double getHumidityAlarmResolution ()
      throws OneWireException
   {
      throw new OneWireException("This device does not have a humidity alarm!");
   }

   //--------
   //-------- Humidity I/O Methods
   //--------

   /**
    * Performs a Humidity conversion.
    *
    * @param  state byte array with device state information
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public void doHumidityConvert (byte[] state)
      throws OneWireIOException, OneWireException
   {
      // do temp convert
      doTemperatureConvert(state);

      // do VDD for supply voltage
      doADConvert(CHANNEL_VDD,state);

      // do VAD for sensor voltage
      doADConvert(CHANNEL_VAD,state);
   }

   //--------
   //-------- Humidity 'get' Methods
   //--------

   /**
    * Gets the humidity expressed as a percent value (0.0 to 100.0) of humidity.
    *
    * @param  state byte array with device state information
    * @return humidity expressed as a percent
    *
    * @see    #hasSelectableHumidityResolution
    * @see    #getHumidityResolution
    * @see    #setHumidityResolution
    */
   public double getHumidity (byte[] state)
   {
      double temp=0,vdd=0,vad=0,rh=0;

      try
      {
         // read the temperature
         temp = getTemperature(state);

         // read the supply voltage
         vdd = getADVoltage(CHANNEL_VDD,state);

         // read the sample voltage
         vad = getADVoltage(CHANNEL_VAD,state);
      }
      catch (OneWireException e)
      {
         // know from this implementatin that this will never happen
         return 0.0;
      }

      // do calculation and check for out of range values
      if (vdd != 0)
         rh = (((vad/vdd) - 0.16)/0.0062)/(1.0546 - 0.00216 * temp);

      if (rh < 0.0)
         rh = 0.0;
      else if (rh > 100.0)
         rh = 100.0;

      return rh;
   }

   /**
    * Gets the current Humidity resolution in percent from the
    * <code>state</code> data retrieved from the <code>readDevice()</code>
    * method.
    *
    * @param  state byte array with device state information
    *
    * @return Humidity resolution in percent for this 1-wire device
    *
    * @see    #hasSelectableHumidityResolution
    * @see    #getHumidityResolutions
    * @see    #setHumidityResolution
    */
   public double getHumidityResolution (byte[] state)
   {
      return 0.1;
   }

   /**
    * Gets the specified Humidity alarm value in percent from the
    * <code>state</code> data retrieved from the
    * <code>readDevice()</code> method.
    *
    * @param  alarmType valid value: <code>ALARM_HIGH</code> or
    *                   <code>ALARM_LOW</code>
    * @param  state     byte array with device state information
    *
    * @return Humidity alarm trip values in percent for this 1-wire device
    *
    * @throws OneWireException         Device does not support Humidity
    *                                  alarms
    *
    * @see    #hasHumidityAlarms
    * @see    #setHumidityAlarm
    */
   public double getHumidityAlarm (int alarmType, byte[] state)
      throws OneWireException
   {
      throw new OneWireException("This device does not have a humidity alarm!");
   }

   //--------
   //-------- Humidity 'set' Methods
   //--------

   /**
    * Sets the Humidity alarm value in percent in the provided
    * <code>state</code> data.
    * Use the method <code>writeDevice()</code> with
    * this data to finalize the change to the device.
    *
    * @param  alarmType  valid value: <code>ALARM_HIGH</code> or
    *                    <code>ALARM_LOW</code>
    * @param  alarmValue alarm trip value in percent
    * @param  state      byte array with device state information
    *
    * @throws OneWireException         Device does not support Humidity
    *                                  alarms
    *
    * @see    #hasHumidityAlarms
    * @see    #getHumidityAlarm
    */
   public void setHumidityAlarm (int alarmType, double alarmValue,
                                    byte[] state)
      throws OneWireException
   {
      throw new OneWireException("This device does not have a humidity alarm!");
   }

   /**
    * Sets the current Humidity resolution in percent in the provided
    * <code>state</code> data.   Use the method <code>writeDevice()</code>
    * with this data to finalize the change to the device.
    *
    * @param  resolution Humidity resolution in percent
    * @param  state      byte array with device state information
    *
    * @throws OneWireException         Device does not support selectable
    *                                  Humidity resolution
    *
    * @see    #hasSelectableHumidityResolution
    * @see    #getHumidityResolution
    * @see    #getHumidityResolutions
    */
   public void setHumidityResolution (double resolution, byte[] state)
      throws OneWireException
   {
      throw new OneWireException("This device does not have selectable humidity resolution!");
   }
}
