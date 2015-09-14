
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
import com.dalsemi.onewire.*;
import com.dalsemi.onewire.utils.*;
import com.dalsemi.onewire.adapter.*;


//----------------------------------------------------------------------------

/**
 * <P> 1-Wire container for temperature iButton which measures temperatures
 * from -55&#176C to +125&#176C, DS18B20.  This container encapsulates the
 * functionality of the iButton family type <B>28</B> (hex)</P>
 *
 * <H3> Features </H3>
 * <UL>
 *   <LI> Measures temperatures from -55&#176C to +125&#176C. Fahrenheit
 *        equivalent is -67&#176F to +257&#176F
 *   <LI> Power supply range is 3.0V to 5.5V
 *   <LI> Zero standby power
 *   <LI> +/- 0.5&#176C accuracy from -10&#176C to +85&#176C
 *   <LI> Thermometer resolution programmable from 9 to 12 bits
 *   <LI> Converts 12-bit temperature to digital word in 750 ms (max.)
 *   <LI> User-definable, nonvolatile temperature alarm settings
 *   <LI> Alarm search command identifies and addresses devices whose temperature is
 *        outside of programmed limits (temperature alarm condition)
 * </UL>
 *
 * <H3> Usage </H3>
 *
 * <DL>
 * <DD> See the usage example in
 * {@link com.dalsemi.onewire.container.TemperatureContainer TemperatureContainer}
 * for temperature specific operations.
 * </DL>
 *
 * <H3> DataSheet </H3>
 * <DL>
 * <DD><A HREF="http://pdfserv.maxim-ic.com/arpdf/DS18B20.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS18B20.pdf</A>
 * </DL>
 *
 * @see com.dalsemi.onewire.container.TemperatureContainer
 *
 * @version    1.00, 15 September 2000
 * @author     BH
 */
public class OneWireContainer28
   extends OneWireContainer
   implements TemperatureContainer
{

   //-------------------------------------------------------------------------
   //-------- Static Final Variables
   //-------------------------------------------------------------------------

   /** DS18B20 writes data to scratchpad command */
   public static final byte WRITE_SCRATCHPAD_COMMAND = ( byte ) 0x4E;

   /** DS18B20 reads data from scratchpad command */
   public static final byte READ_SCRATCHPAD_COMMAND = ( byte ) 0xBE;

   /** DS18B20 copys data from scratchpad to E-squared memory command */
   public static final byte COPY_SCRATCHPAD_COMMAND = ( byte ) 0x48;

   /** DS18B20 converts temperature command */
   public static final byte CONVERT_TEMPERATURE_COMMAND = ( byte ) 0x44;

   /** DS18B20 recalls E-squared memory command */
   public static final byte RECALL_E2MEMORY_COMMAND = ( byte ) 0xB8;

   /**
    * DS18B20 reads power supply command.  This command is used to determine
    * if external power is supplied.
    */
   public static final byte READ_POWER_SUPPLY_COMMAND = ( byte ) 0xB4;

   /** DS18B20 12-bit resolution constant for CONFIG byte  */
   public static final byte RESOLUTION_12_BIT = ( byte ) 0x7F;

   /** DS18B20 11-bit resolution constant for CONFIG byte  */
   public static final byte RESOLUTION_11_BIT = ( byte ) 0x5F;

   /** DS18B20 10-bit resolution constant for CONFIG byte  */
   public static final byte RESOLUTION_10_BIT = ( byte ) 0x3F;

   /** DS18B20 9-bit resolution constant for CONFIG byte   */
   public static final byte RESOLUTION_9_BIT = ( byte ) 0x1F;

   /**
    * Creates an empty <code>OneWireContainer28</code>.  Must call
    * <code>setupContainer()</code> before using this new container.<p>
    *
    * This is one of the methods to construct a <code>OneWireContainer28</code>.
    * The others are through creating a <code>OneWireContainer28</code> with
    * parameters.
    *
    * @see #OneWireContainer28(DSPortAdapter,byte[])
    * @see #OneWireContainer28(DSPortAdapter,long)
    * @see #OneWireContainer28(DSPortAdapter,String)
    */
   public OneWireContainer28 ()
   {
      super();
   }

   /**
    * Creates a <code>OneWireContainer28</code> with the provided adapter
    * object and the address of this One-Wire device.
    *
    * This is one of the methods to construct a <code>OneWireContainer28</code>.
    * The others are through creating a <code>OneWireContainer28</code> with
    * different parameters types.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    *                           this One-Wire device
    * @param  newAddress        address of this One-Wire device
    *
    * @see com.dalsemi.onewire.utils.Address
    * @see #OneWireContainer28()
    * @see #OneWireContainer28(DSPortAdapter,long)
    * @see #OneWireContainer28(DSPortAdapter,String)
    */
   public OneWireContainer28 (DSPortAdapter sourceAdapter, byte[] newAddress)
   {
      super(sourceAdapter, newAddress);
   }

   /**
    * Creates a <code>OneWireContainer28</code> with the provided adapter
    * object and the address of this One-Wire device.
    *
    * This is one of the methods to construct a <code>OneWireContainer28</code>.
    * The others are through creating a <code>OneWireContainer28</code> with
    * different parameters types.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    *                           this One-Wire device
    * @param  newAddress        address of this One-Wire device
    *
    * @see com.dalsemi.onewire.utils.Address
    * @see #OneWireContainer28()
    * @see #OneWireContainer28(DSPortAdapter,byte[])
    * @see #OneWireContainer28(DSPortAdapter,String)
    */
   public OneWireContainer28 (DSPortAdapter sourceAdapter, long newAddress)
   {
      super(sourceAdapter, newAddress);
   }

   /**
    * Creates a <code>OneWireContainer28</code> with the provided adapter
    * object and the address of this One-Wire device.
    *
    * This is one of the methods to construct a <code>OneWireContainer28</code>.
    * The others are through creating a <code>OneWireContainer28</code> with
    * different parameters types.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    *                           this One-Wire device
    * @param  newAddress        address of this One-Wire device
    *
    * @see com.dalsemi.onewire.utils.Address
    * @see #OneWireContainer28()
    * @see #OneWireContainer28(DSPortAdapter,byte[])
    * @see #OneWireContainer28(DSPortAdapter,long)
    */
   public OneWireContainer28 (DSPortAdapter sourceAdapter, String newAddress)
   {
      super(sourceAdapter, newAddress);
   }

   //--------
   //-------- Information methods
   //--------

   /**
    * Retrieves the Dallas Semiconductor part number of this
    * <code>OneWireContainer28</code> as a <code>String</code>.
    * For example 'DS18B20'.
    *
    * @return this <code>OneWireContainer28</code> name
    */
   public String getName ()
   {
      return "DS18B20";
   }

   /**
    * Retrieves the alternate Dallas Semiconductor part numbers or names.
    * A 'family' of 1-Wire Network devices may have more than one part number
    * depending on packaging.  There can also be nicknames such as
    * 'Crypto iButton'.
    *
    * @return this <code>OneWireContainer28</code> alternate names
    */
   public String getAlternateNames ()
   {
      return "DS1820B, DS18B20X";
   }

   /**
    * Retrieves a short description of the function of this
    * <code>OneWireContainer28</code> type.
    *
    * @return <code>OneWireContainer28</code> functional description
    */
   public String getDescription ()
   {
      return "Digital thermometer measures temperatures from "
             + "-55C to 125C in 0.75 seconds (max).  +/- 0.5C "
             + "accuracy between -10C and 85C. Thermometer "
             + "resolution is programmable at 9, 10, 11, and 12 bits. ";
   }

   //--------
   //-------- Temperature Feature methods
   //--------

   /**
    * Checks to see if this temperature measuring device has high/low
    * trip alarms.
    *
    * @return <code>true</code> if this <code>OneWireContainer28</code>
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
    * @return <code>true</code> if this <code>OneWireContainer28</code>
    *         has selectable temperature resolution
    *
    * @see    #getTemperatureResolution
    * @see    #getTemperatureResolutions
    * @see    #setTemperatureResolution
    */
   public boolean hasSelectableTemperatureResolution ()
   {
      return true;
   }

   /**
    * Gets an array of available temperature resolutions in Celsius.
    *
    * @return byte array of available temperature resolutions in Celsius for
    *         this <code>OneWireContainer28</code>. The minimum resolution is
    *         returned as the first element and maximum resolution as the last
    *         element.
    *
    * @see    #hasSelectableTemperatureResolution
    * @see    #getTemperatureResolution
    * @see    #setTemperatureResolution
    */
   public double[] getTemperatureResolutions ()
   {
      double[] resolutions = new double [4];

      resolutions [0] = ( double ) 0.5;      // 9-bit
      resolutions [1] = ( double ) 0.25;     // 10-bit
      resolutions [2] = ( double ) 0.125;    // 11-bit
      resolutions [3] = ( double ) 0.0625;   // 12-bit

      return resolutions;
   }

   /**
    * Gets the temperature alarm resolution in Celsius.
    *
    * @return temperature alarm resolution in Celsius for this
    *         <code>OneWireContainer28</code>
    *
    * @see    #hasTemperatureAlarms
    * @see    #getTemperatureAlarm
    * @see    #setTemperatureAlarm
    *
    */
   public double getTemperatureAlarmResolution ()
   {
      return 1.0;
   }

   /**
    * Gets the maximum temperature in Celsius.
    *
    * @return maximum temperature in Celsius for this
    *         <code>OneWireContainer28</code>
    *
    * @see    #getMinTemperature
    */
   public double getMaxTemperature ()
   {
      return 125.0;
   }

   /**
    * Gets the minimum temperature in Celsius.
    *
    * @return minimum temperature in Celsius for this
    *         <code>OneWireContainer28</code>
    *
    * @see    #getMaxTemperature
    */
   public double getMinTemperature ()
   {
      return -55.0;
   }

   //--------
   //-------- Temperature I/O Methods
   //--------

   /**
    * Performs a temperature conversion on <code>state</code> information.
    *
    * @param  state byte array with device state information
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from this <code>OneWireContainer28</code>.
    *         This could be caused by a physical interruption in the 1-Wire
    *         Network due to shorts or a newly arriving 1-Wire device issuing a
    *         'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see    #getTemperature
    */
   public void doTemperatureConvert (byte[] state)
      throws OneWireIOException, OneWireException
   {
      int msDelay = 750;   // in milliseconds

      // select the device
      if (adapter.select(address))
      {

         // Setup Power Delivery
         adapter.setPowerDuration(DSPortAdapter.DELIVERY_INFINITE);
         adapter.startPowerDelivery(DSPortAdapter.CONDITION_AFTER_BYTE);
         // send the convert temperature command
         adapter.putByte(CONVERT_TEMPERATURE_COMMAND);

         // calculate duration of delay according to resolution desired
         switch (state [4])
         {

            case RESOLUTION_9_BIT :
               msDelay = 94;
               break;
            case RESOLUTION_10_BIT :
               msDelay = 188;
               break;
            case RESOLUTION_11_BIT :
               msDelay = 375;
               break;
            case RESOLUTION_12_BIT :
               msDelay = 750;
               break;
            default :
               msDelay = 750;
         }   // switch

         // delay for specified amount of time
         try
         {
            Thread.sleep(msDelay);
         }
         catch (InterruptedException e){}

         // Turn power back to normal.
         adapter.setPowerNormal();

         // check to see if the temperature conversion is over
         if (adapter.getByte() != 0xFF)
            throw new OneWireIOException(
               "OneWireContainer28-temperature conversion not complete");
      }
      else
      {

         // device must not have been present
         throw new OneWireIOException(
            "OneWireContainer28-device not present");
      }
   }

   //--------
   //-------- Temperature 'get' Methods
   //--------

   /**
    * Gets the temperature value in Celsius from the <code>state</code>
    * data retrieved from the <code>readDevice()</code> method.
    *
    * @param  state byte array with device state information for this
    *         <code>OneWireContainer28</code>
    *
    * @return temperature in Celsius from the last
    *                     <code>doTemperatureConvert()</code>
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from this <code>OneWireContainer28</code>.
    *         This could be caused by a physical interruption in the 1-Wire
    *         Network due to shorts or a newly arriving 1-Wire device issuing a
    *         'presence pulse'.
    *
    * @see    #doTemperatureConvert
    */
   public double getTemperature (byte[] state)
      throws OneWireIOException
   {

      // Take these three steps:
      // 1)  Make an 11-bit integer number out of MSB and LSB of the first 2 bytes from scratchpad
      // 2)  Divide final number by 16 to retrieve the floating point number.
      // 3)  Afterwards, test for the following temperatures:
      //     0x07D0 = 125.0C
      //     0x0550 = 85.0C
      //     0x0191 = 25.0625C
      //     0x00A2 = 10.125C
      //     0x0008 = 0.5C
      //     0x0000 = 0.0C
      //     0xFFF8 = -0.5C
      //     0xFF5E = -10.125C
      //     0xFE6F = -25.0625C
      //     0xFC90 = -55.0C
      double theTemperature = ( double ) 0.0;
      int    inttemperature = state [1];   // inttemperature is automatically sign extended here.

      inttemperature = (inttemperature << 8) | (state [0] & 0xFF);   // this converts 2 bytes into integer
      theTemperature = ( double ) (( double ) inttemperature / ( double ) 16);   // converts integer to a double

      return (theTemperature);
   }

   /**
    * Gets the specified temperature alarm value in Celsius from the
    * <code>state</code> data retrieved from the  <code>readDevice()</code>
    * method.
    *
    * @param  alarmType valid value: <code>ALARM_HIGH</code> or
    *                   <code>ALARM_LOW</code>
    * @param  state     byte array with device state information
    *
    * @return temperature alarm trip values in Celsius for this
    *         <code>OneWireContainer28</code>
    *
    * @see    #hasTemperatureAlarms
    * @see    #setTemperatureAlarm
    */
   public double getTemperatureAlarm (int alarmType, byte[] state)
   {
      return ( double ) state [alarmType == ALARM_LOW ? 3
                                                      : 2];
   }

   /**
    * Gets the current temperature resolution in Celsius from the
    * <code>state</code> data retrieved from the <code>readDevice()</code>
    * method.
    *
    * @param  state byte array with device state information
    *
    * @return temperature resolution in Celsius for this
    *         <code>OneWireContainer28</code>
    *
    * @see    #RESOLUTION_9_BIT
    * @see    #RESOLUTION_10_BIT
    * @see    #RESOLUTION_11_BIT
    * @see    #RESOLUTION_12_BIT
    * @see    #hasSelectableTemperatureResolution
    * @see    #getTemperatureResolutions
    * @see    #setTemperatureResolution
    */
   public double getTemperatureResolution (byte[] state)
   {
      double tempres = ( double ) 0.0;

      // calculate temperature resolution according to configuration byte
      switch (state [4])
      {

         case RESOLUTION_9_BIT :
            tempres = ( double ) 0.5;
            break;
         case RESOLUTION_10_BIT :
            tempres = ( double ) 0.25;
            break;
         case RESOLUTION_11_BIT :
            tempres = ( double ) 0.125;
            break;
         case RESOLUTION_12_BIT :
            tempres = ( double ) 0.0625;
            break;
         default :
            tempres = ( double ) 0.0;
      }   // switch

      return tempres;
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
      throws OneWireException, OneWireIOException
   {
      if ((alarmType != ALARM_LOW) && (alarmType != ALARM_HIGH))
         throw new IllegalArgumentException("Invalid alarm type.");

      if (alarmValue > 125.0 || alarmValue < -55.0)
         throw new IllegalArgumentException(
            "Value for alarm not in accepted range.  Must be -55 C <-> +125 C.");

      state [(alarmType == ALARM_LOW) ? 3
                                      : 2] = ( byte ) alarmValue;
   }

   /**
    * Sets the current temperature resolution in Celsius in the provided
    * <code>state</code> data.   Use the method <code>writeDevice()</code>
    * with this data to finalize the change to the device.
    *
    * @param  resolution temperature resolution in Celsius. Valid values are
    *                    <code>RESOLUTION_9_BIT</code>,
    *                    <code>RESOLUTION_10_BIT</code>,
    *                    <code>RESOLUTION_11_BIT</code> and
    *                    <code>RESOLUTION_12_BIT</code>.
    * @param  state      byte array with device state information
    *
    * @see    #RESOLUTION_9_BIT
    * @see    #RESOLUTION_10_BIT
    * @see    #RESOLUTION_11_BIT
    * @see    #RESOLUTION_12_BIT
    * @see    #hasSelectableTemperatureResolution
    * @see    #getTemperatureResolution
    * @see    #getTemperatureResolutions
    */
   public void setTemperatureResolution (double resolution, byte[] state)
      throws OneWireException
   {
      byte configbyte = RESOLUTION_12_BIT;

      synchronized (this)
      {

         // calculate configbyte from given resolution
         if (resolution == 0.5)
            configbyte = RESOLUTION_9_BIT;

         if (resolution == 0.25)
            configbyte = RESOLUTION_10_BIT;

         if (resolution == 0.125)
            configbyte = RESOLUTION_11_BIT;

         if (resolution == 0.0625)
            configbyte = RESOLUTION_12_BIT;

         state [4] = configbyte;
      }
   }

   /**
    * Retrieves this <code>OneWireContainer28</code> state information.
    * The state information is returned as a byte array.  Pass this byte
    * array to the '<code>get</code>' and '<code>set</code>' methods.
    * If the device state needs to be changed, then call the
    * <code>writeDevice()</code> to finalize the changes.
    *
    * @return <code>OneWireContainer28</code> state information.
    * Device state looks like this:
    * <pre>
    *   0 : temperature LSB
    *   1 : temperature MSB
    *   2 : trip high
    *   3 : trip low
    *   4 : configuration register (for resolution)
    *   5 : reserved
    *   6 : reserved
    *   7 : reserved
    *   8 : an 8 bit CRC of the previous 8 bytes
    * </pre>
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from this <code>OneWireContainer28</code>.
    *         This could be caused by a physical interruption in the 1-Wire
    *         Network due to shorts or a newly arriving 1-Wire device issuing a
    *         'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see    #writeDevice
    */
   public byte[] readDevice ()
      throws OneWireIOException, OneWireException
   {

      byte[] data;

      data = recallE2();

      return data;
   }

   /**
    * Writes to this <code>OneWireContainer28</code> <code>state</code>
    * information that have been changed by '<code>set</code>' methods.
    * Only the state registers that changed are updated.  This is done
    * by referencing a field information appended to the state data.
    *
    * @param  state      byte array with device state information
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from this <code>OneWireContainer28</code>.
    *         This could be caused by a physical interruption in the 1-Wire
    *         Network due to shorts or a newly arriving 1-Wire device issuing a
    *         'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see    #readDevice
    */
   public void writeDevice (byte[] state)
      throws OneWireIOException, OneWireException
   {
      byte[] temp = new byte [3];

      temp [0] = state [2];
      temp [1] = state [3];
      temp [2] = state [4];

      // Write it to the Scratchpad.
      writeScratchpad(temp);

      // Place in memory.
      copyScratchpad();
   }

   //--------
   //-------- Custom Methods for this iButton Type
   //--------
   //-------------------------------------------------------------------------

   /**
    * Reads the Scratchpad of the DS18B20.
    *
    * @return 9-byte buffer representing the scratchpad
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from this <code>OneWireContainer28</code>.
    *         This could be caused by a physical interruption in the 1-Wire
    *         Network due to shorts or a newly arriving 1-Wire device issuing a
    *         'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public byte[] readScratchpad ()
      throws OneWireIOException, OneWireException
   {
      byte[] result_block;

      // select the device
      if (adapter.select(address))
      {

         // create a block to send that reads the scratchpad
         byte[] send_block = new byte [10];

         // read scratchpad command
         send_block [0] = ( byte ) READ_SCRATCHPAD_COMMAND;

         // now add the read bytes for data bytes and crc8
         for (int i = 1; i < 10; i++)
            send_block [i] = ( byte ) 0xFF;

         // send the block
         adapter.dataBlock(send_block, 0, send_block.length);

         // now, send_block contains the 9-byte Scratchpad plus READ_SCRATCHPAD_COMMAND byte
         // convert the block to a 9-byte array representing Scratchpad (get rid of first byte)
         result_block = new byte [9];

         for (int i = 0; i < 9; i++)
         {
            result_block [i] = send_block [i + 1];
         }

         // see if CRC8 is correct
         if (CRC8.compute(send_block, 1, 9) == 0)
            return (result_block);
         else
            throw new OneWireIOException(
               "OneWireContainer28-Error reading CRC8 from device.");
      }

      // device must not have been present
      throw new OneWireIOException(
         "OneWireContainer28-Device not found on 1-Wire Network");
   }

   //-------------------------------------------------------------------------

   /**
    * Writes to the Scratchpad of the DS18B20.
    *
    * @param data data to be written to the scratchpad.  First
    *             byte of data must be the temperature High Trip Point, the
    *             second byte must be the temperature Low Trip Point, and
    *             the third must be the Resolution (configuration register).
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from this <code>OneWireContainer28</code>.
    *         This could be caused by a physical interruption in the 1-Wire
    *         Network due to shorts or a newly arriving 1-Wire device issuing a
    *         'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    * @throws IllegalArgumentException when data is of invalid length
    */
   public void writeScratchpad (byte[] data)
      throws OneWireIOException, OneWireException
   {

      // setup buffer to write to scratchpad
      byte[] writeBuffer = new byte [4];

      writeBuffer [0] = WRITE_SCRATCHPAD_COMMAND;
      writeBuffer [1] = data [0];
      writeBuffer [2] = data [1];
      writeBuffer [3] = data [2];

      // send command block to device
      if (adapter.select(address))
      {
         adapter.dataBlock(writeBuffer, 0, writeBuffer.length);
      }
      else
      {

         // device must not have been present
         throw new OneWireIOException(
            "OneWireContainer28-Device not found on 1-Wire Network");
      }

      // double check by reading scratchpad
      byte[] readBuffer;

      readBuffer = readScratchpad();

      if ((readBuffer [2] != data [0]) || (readBuffer [3] != data [1])
              || (readBuffer [4] != data [2]))
      {

         // writing to scratchpad failed
         throw new OneWireIOException(
            "OneWireContainer28-Error writing to scratchpad");
      }

      return;
   }

   //-------------------------------------------------------------------------

   /**
    * Copies the Scratchpad to the E-squared memory of the DS18B20.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from this <code>OneWireContainer28</code>.
    *         This could be caused by a physical interruption in the 1-Wire
    *         Network due to shorts or a newly arriving 1-Wire device issuing a
    *         'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public void copyScratchpad ()
      throws OneWireIOException, OneWireException
   {

      // first, let's read the scratchpad to compare later.
      byte[] readfirstbuffer;

      readfirstbuffer = readScratchpad();

      // second, let's copy the scratchpad.
      if (adapter.select(address))
      {

         // apply the power delivery
         adapter.setPowerDuration(DSPortAdapter.DELIVERY_INFINITE);
         adapter.startPowerDelivery(DSPortAdapter.CONDITION_AFTER_BYTE);

         // send the convert temperature command
         adapter.putByte(COPY_SCRATCHPAD_COMMAND);

         // sleep for 10 milliseconds to allow copy to take place.
         try
         {
            Thread.sleep(10);
         }
         catch (InterruptedException e){}
         ;

         // Turn power back to normal.
         adapter.setPowerNormal();
      }
      else
      {

         // device must not have been present
         throw new OneWireIOException(
            "OneWireContainer28-Device not found on 1-Wire Network");
      }

      // third, let's read the scratchpad again with the recallE2 command and compare.
      byte[] readlastbuffer;

      readlastbuffer = recallE2();

      if ((readfirstbuffer [2] != readlastbuffer [2])
              || (readfirstbuffer [3] != readlastbuffer [3])
              || (readfirstbuffer [4] != readlastbuffer [4]))
      {

         // copying to scratchpad failed
         throw new OneWireIOException(
            "OneWireContainer28-Error copying scratchpad to E2 memory.");
      }
   }

   //-------------------------------------------------------------------------

   /**
    * Recalls the DS18B20 temperature trigger values (<code>ALARM_HIGH</code>
    * and <code>ALARM_LOW</code>) and the configuration register to the
    * scratchpad and reads the scratchpad.
    *
    * @return byte array representing data in the device's scratchpad.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from this <code>OneWireContainer28</code>.
    *         This could be caused by a physical interruption in the 1-Wire
    *         Network due to shorts or a newly arriving 1-Wire device issuing a
    *         'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public byte[] recallE2 ()
      throws OneWireIOException, OneWireException
   {
      byte[] ScratchBuff;

      // select the device
      if (adapter.select(address))
      {

         // send the Recall E-squared memory command
         adapter.putByte(RECALL_E2MEMORY_COMMAND);

         // read scratchpad
         ScratchBuff = readScratchpad();

         return (ScratchBuff);
      }

      // device must not have been present
      throw new OneWireIOException(
         "OneWireContainer28-Device not found on 1-Wire Network");
   }

   //-------------------------------------------------------------------------

   /**
    * Reads the way power is supplied to the DS18B20.
    *
    * @return <code>true</code> for external power, <BR>
    *         <code>false</code> for parasite power
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from this <code>OneWireContainer28</code>.
    *         This could be caused by a physical interruption in the 1-Wire
    *         Network due to shorts or a newly arriving 1-Wire device issuing a
    *         'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public boolean isExternalPowerSupplied ()
      throws OneWireIOException, OneWireException
   {
      int     intresult = 0;
      boolean result    = false;

      // select the device
      if (adapter.select(address))
      {

         // send the "Read Power Supply" memory command
         adapter.putByte(READ_POWER_SUPPLY_COMMAND);

         // read results
         intresult = adapter.getByte();
      }
      else
      {

         // device must not have been present
         throw new OneWireIOException(
            "OneWireContainer28-Device not found on 1-Wire Network");
      }

      if (intresult != 0x00)
         result = true;   // reads 0xFF for true and 0x00 for false

      return result;
   }

   //-------------------------------------------------------------------------

   /**
    * Converts a temperature reading from Celsius to Fahrenheit.
    *
    * @param   celsiusTemperature temperature value in Celsius
    *
    * @return  the Fahrenheit conversion of the supplied temperature
    *
    * @deprecated Replace with call to com.dalsemi.onewire.utils.Convert.toFahrenheit()
    *
    * @see com.dalsemi.onewire.utils.Convert#toFahrenheit(double)
    */
   public float convertToFahrenheit (float celsiusTemperature)
   {
      return (float)Convert.toFahrenheit(celsiusTemperature);
   }
}
