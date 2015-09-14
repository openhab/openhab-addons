
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

package com.dalsemi.onewire.adapter;

// imports
import java.util.Vector;
import java.util.Enumeration;
import com.dalsemi.onewire.adapter.UAdapterState;
import com.dalsemi.onewire.adapter.RawSendPacket;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.utils.Address;
import com.dalsemi.onewire.OneWireAccessProvider;


/** UPacketBuilder contains the methods to build a communication packet
 *  to the DS2480 based serial adapter.
 *
 *  @version    0.00, 28 Aug 2000
 *  @author     DS
 */
class UPacketBuilder
{

   //--------
   //-------- Finals
   //--------
   //-------- Misc

   /** Byte operation                                     */
   public static final int OPERATION_BYTE = 0;

   /** Byte operation                                     */
   public static final int OPERATION_SEARCH = 1;

   /** Max bytes to stream at once  */
   public static final char MAX_BYTES_STREAMED = 64;

   //-------- DS9097U function commands

   /** DS9097U funciton command, single bit               */
   public static final char FUNCTION_BIT = 0x81;

   /** DS9097U funciton command, turn search mode on      */
   public static final char FUNCTION_SEARCHON = 0xB1;

   /** DS9097U funciton command, turn search mode off     */
   public static final char FUNCTION_SEARCHOFF = 0xA1;

   /** DS9097U funciton command, OneWire reset            */
   public static final char FUNCTION_RESET = 0xC1;

   /** DS9097U funciton command, 5V pulse imediate        */
   public static final char FUNCTION_5VPULSE_NOW = 0xED;

   /** DS9097U funciton command, 12V pulse imediate        */
   public static final char FUNCTION_12VPULSE_NOW = 0xFD;

   /** DS9097U funciton command, 5V pulse after next byte */
   public static final char FUNCTION_5VPULSE_ARM = 0xEF;

   /** DS9097U funciton command to stop an ongoing pulse  */
   public static final char FUNCTION_STOP_PULSE = 0xF1;

   //-------- DS9097U bit polarity settings for doing bit operations

   /** DS9097U bit polarity one for function FUNCTION_BIT   */
   public static final char BIT_ONE = 0x10;

   /** DS9097U bit polarity zero  for function FUNCTION_BIT */
   public static final char BIT_ZERO = 0x00;

   //-------- DS9097U 5V priming values

   /** DS9097U 5V prime on for function FUNCTION_BIT    */
   public static final char PRIME5V_TRUE = 0x02;

   /** DS9097U 5V prime off for function FUNCTION_BIT   */
   public static final char PRIME5V_FALSE = 0x00;

   //-------- DS9097U command masks

   /** DS9097U mask to read or write a configuration parameter   */
   public static final char CONFIG_MASK = 0x01;

   /** DS9097U mask to read the OneWire reset response byte */
   public static final char RESPONSE_RESET_MASK = 0x03;

   //-------- DS9097U reset results

   /** DS9097U  OneWire reset result = shorted */
   public static final char RESPONSE_RESET_SHORT = 0x00;

   /** DS9097U  OneWire reset result = presence */
   public static final char RESPONSE_RESET_PRESENCE = 0x01;

   /** DS9097U  OneWire reset result = alarm */
   public static final char RESPONSE_RESET_ALARM = 0x02;

   /** DS9097U  OneWire reset result = no presence */
   public static final char RESPONSE_RESET_NOPRESENCE = 0x03;

   //-------- DS9097U bit interpretation

   /** DS9097U mask to read bit operation result   */
   public static final char RESPONSE_BIT_MASK = 0x03;

   /** DS9097U read bit operation 1 */
   public static final char RESPONSE_BIT_ONE = 0x03;

   /** DS9097U read bit operation 0 */
   public static final char RESPONSE_BIT_ZERO = 0x00;

   /** Enable/disable debug messages                   */
   public static boolean doDebugMessages = false;

   //--------
   //-------- Variables
   //--------

   /**
    * The current state of the U brick, passed into constructor.
    */
   private UAdapterState uState;

   /**
    * The current current count for the number of return bytes from
    * the packet being created.
    */
   protected int totalReturnLength;

   /**
    * Current raw send packet before it is added to the packetsVector
    */
   protected RawSendPacket packet;

   /**
    * Vector of raw send packets
    */
   protected Vector packetsVector;

   /**
    * Flag to send only 'bit' commands to the DS2480
    */
   protected boolean bitsOnly;

   //--------
   //-------- Constructors
   //--------

   /**
    * Constructs a new u packet builder.
    *
    * @param  startUState   the object that contains the U brick state
    *                        which is reference when creating packets
    */
   public UPacketBuilder (UAdapterState startUState)
   {

      // get a reference to the U state
      uState = startUState;

      // create the buffer for the data
      packet = new RawSendPacket();

      // create the vector
      packetsVector = new Vector();

      // restart the packet to initialize
      restart();

      // Default on SunOS to bit-banging
      bitsOnly = (System.getProperty("os.name").indexOf("SunOS") != -1);

      // check for a bits only property
      String bits = OneWireAccessProvider.getProperty("onewire.serial.forcebitsonly");
      if (bits != null)
      {
         if (bits.indexOf("true") != -1)
            bitsOnly = true;
         else if (bits.indexOf("false") != -1)
            bitsOnly = false;
      }
   }

   //--------
   //-------- Packet handling Methods
   //--------

   /**
    * Reset the packet builder to start a new one.
    */
   public void restart ()
   {

      // clear the vector list of packets
      packetsVector.removeAllElements();

      // truncate the packet to 0 length
      packet.buffer.setLength(0);

      packet.returnLength = 0;

      // reset the return cound
      totalReturnLength = 0;
   }

   /**
    * Take the current packet and place it into the vector.  This
    * indicates a place where we need to wait for the results from
    * DS9097U adapter.
    */
   public void newPacket ()
   {

      // add the packet
      packetsVector.addElement(packet);

      // get a new packet
      packet = new RawSendPacket();
   }

   /**
    * Retrieve enumeration of raw send packets
    *
    * @return  the enumeration of packets
    */
   public Enumeration getPackets ()
   {

      // put the last packet into the vector if it is non zero
      if (packet.buffer.length() > 0)
         newPacket();

      return packetsVector.elements();
   }

   //--------
   //-------- 1-Wire Network operation append methods
   //--------

   /** Add the command to reset the OneWire at the current speed.
    *
    *  @return the number offset in the return packet to get the
    *          result of this operation
    */
   public int oneWireReset ()
   {

      // set to command mode
      setToCommandMode();

      // append the reset command at the current speed
      packet.buffer.append(( char ) (FUNCTION_RESET | uState.uSpeedMode));

      // count this as a return
      totalReturnLength++;
      packet.returnLength++;

      // check if not streaming resets
      if (!uState.streamResets)
         newPacket();

      // check for 2480 wait on extra bytes packet
      if (uState.longAlarmCheck
              && ((uState.uSpeedMode == UAdapterState.USPEED_REGULAR)
                  || (uState.uSpeedMode == UAdapterState.USPEED_FLEX)))
         newPacket();

      return totalReturnLength - 1;
   }

   /**
    * Append data bytes (read/write) to the packet.
    *
    * @param  dataBytesValue  character array of data bytes
    *
    * @return the number offset in the return packet to get the
    *          result of this operation
    */
   public int dataBytes (char dataBytesValue [])
   {
      char byte_value;
      int i,j;

      // set to data mode
      if (!bitsOnly)
         setToDataMode();

      // provide debug output
      if (doDebugMessages)
         System.out.println("DEBUG: UPacketbuilder-dataBytes[] length "
                            + dataBytesValue.length);

      // record the current count location
      int ret_value = totalReturnLength;

      // check each byte to see if some need duplication
      for (i = 0; i < dataBytesValue.length; i++)
      {
         // convert the rest to OneWireIOExceptions
         if (bitsOnly)
         {
            // change byte to bits
            byte_value = dataBytesValue [i];
            for (j = 0; j < 8; j++)
            {
               dataBit(((byte_value & 0x01) == 0x01), false);
               byte_value >>>= 1;
            }
         }
         else
         {
            // append the data
            packet.buffer.append(dataBytesValue [i]);

            // provide debug output
            if (doDebugMessages)
               System.out.println(
                  "DEBUG: UPacketbuilder-dataBytes[] byte["
                  + Integer.toHexString(( int ) dataBytesValue [i] & 0x00FF)
                  + "]");

            // check for duplicates needed for special characters
            if ((( char ) (dataBytesValue [i] & 0x00FF) == UAdapterState.MODE_COMMAND)
                    || ((( char ) (dataBytesValue [i] & 0x00FF) == UAdapterState.MODE_SPECIAL)
                        && (uState.revision == UAdapterState.CHIP_VERSION1)))
            {
               // duplicate this data byte
               packet.buffer.append(dataBytesValue [i]);
            }

            // add to the return number of bytes
            totalReturnLength++;
            packet.returnLength++;

            // provide debug output
            if (doDebugMessages)
               System.out.println(
                  "DEBUG: UPacketbuilder-dataBytes[] returnlength "
                  + packet.returnLength + " bufferLength "
                  + packet.buffer.length());

            // check for packet too large or not streaming bytes
            if ((packet.buffer.length() > MAX_BYTES_STREAMED)
                    ||!uState.streamBytes)
               newPacket();
         }
      }

      return ret_value;
   }

   /**
    * Append data bytes (read/write) to the packet.
    *
    * @param  dataBytesValue  byte array of data bytes
    * @param  off   offset into the array of data to start
    * @param  len   length of data to send / receive starting at 'off'
    *
    * @return the number offset in the return packet to get the
    *          result of this operation
    */
   public int dataBytes (byte[] dataBytesValue, int off, int len)
   {
      char[] temp_ch = new char [len];

      for (int i = 0; i < len; i++)
         temp_ch [i] = ( char ) dataBytesValue [off + i];

      return dataBytes(temp_ch);
   }

   /**
    * Append a data byte (read/write) to the packet.
    *
    * @param  dataByteValue  data byte to append
    *
    * @return the number offset in the return packet to get the
    *          result of this operation
    */
   public int dataByte (char dataByteValue)
   {

      // contruct a temporary array of characters of lenght 1
      // to use the dataBytes method
      char[] temp_char_array = new char [1];

      temp_char_array [0] = dataByteValue;

      // provide debug output
      if (doDebugMessages)
         System.out.println(
            "DEBUG: UPacketbuilder-dataBytes ["
            + Integer.toHexString(( int ) dataByteValue & 0x00FF) + "]");

      return dataBytes(temp_char_array);
   }

   /**
    * Append a data byte (read/write) to the packet.  Do a strong pullup
    * when the byte is complete
    *
    * @param  dataByteValue  data byte to append
    *
    * @return the number offset in the return packet to get the
    *          result of this operation
    */
   public int primedDataByte (byte dataByteValue)
   {
      int offset, start_offset = 0;

      // create a primed data byte by using bits with last one primed
      for (int i = 0; i < 8; i++)
      {
         offset        = dataBit(((dataByteValue & 0x01) == 0x01), (i == 7));
         dataByteValue >>>= 1;

         // record the starting offset
         if (i == 0)
            start_offset = offset;
      }

      return start_offset;
   }

   /**
    * Append a data bit (read/write) to the packet.
    *
    * @param  dataBit   bit to append
    * @param  strong5V  true if want strong5V after bit
    *
    * @return the number offset in the return packet to get the
    *          result of this operation
    */
   public int dataBit (boolean dataBit, boolean strong5V)
   {

      // set to command mode
      setToCommandMode();

      // append the bit with polarity and strong5V options
      packet.buffer.append(( char ) (FUNCTION_BIT | uState.uSpeedMode
                                     | ((dataBit) ? BIT_ONE
                                                  : BIT_ZERO) | ((strong5V)
                                                                 ? PRIME5V_TRUE
                                                                 : PRIME5V_FALSE)));

      // add to the return number of bytes
      totalReturnLength++;
      packet.returnLength++;

      // check for packet too large or not streaming bits
      if ((packet.buffer.length() > MAX_BYTES_STREAMED) ||!uState.streamBits)
         newPacket();

      return (totalReturnLength - 1);
   }

   /**
    * Append a search to the packet.  Assume that any reset and search
    * command have already been appended.  This will add only the search
    * itself.
    *
    * @param  mState OneWire state
    *
    * @return the number offset in the return packet to get the
    *          result of this operation
    */
   public int search (OneWireState mState)
   {

      // set to command mode
      setToCommandMode();

      // search mode on
      packet.buffer.append(( char ) (FUNCTION_SEARCHON | uState.uSpeedMode));

      // set to data mode
      setToDataMode();

      // create the search sequence character array
      char[] search_sequence = new char [16];

      // get a copy of the current ID
      char[] id = new char [8];

      for (int i = 0; i < 8; i++)
         id [i] = ( char ) (mState.ID [i] & 0xFF);

      // clear the string
      for (int i = 0; i < 16; i++)
         search_sequence [i] = 0;

      // provide debug output
      if (doDebugMessages)
         System.out.println("DEBUG: UPacketbuilder-search ["
                            + Integer.toHexString(( int ) id.length) + "]");

      // only modify bits if not the first search
      if (mState.searchLastDiscrepancy != 0xFF)
      {

         // set the bits in the added buffer
         for (int i = 0; i < 64; i++)
         {

            // before last discrepancy (go direction based on ID)
            if (i < (mState.searchLastDiscrepancy - 1))
               bitWrite(search_sequence, (i * 2 + 1), bitRead(id, i));

               // at last discrepancy (go 1's direction)
            else if (i == (mState.searchLastDiscrepancy - 1))
               bitWrite(search_sequence, (i * 2 + 1), true);

            // after last discrepancy so leave zeros
         }
      }

      // remember this position
      int return_position = totalReturnLength;

      // add this sequence
      packet.buffer.append(search_sequence);

      // set to command mode
      setToCommandMode();

      // search mode off
      packet.buffer.append(( char ) (FUNCTION_SEARCHOFF | uState.uSpeedMode));

      // add to the return number of bytes
      totalReturnLength   += 16;
      packet.returnLength += 16;

      return return_position;
   }

   /**
    * Append a search off to set the current speed.
    */
   public void setSpeed ()
   {

      // set to command mode
      setToCommandMode();

      // search mode off and change speed
      packet.buffer.append(( char ) (FUNCTION_SEARCHOFF | uState.uSpeedMode));

      // no return byte
   }

   //--------
   //-------- U mode commands
   //--------

   /**
    * Set the U state to command mode.
    */
   public void setToCommandMode ()
   {
      if (!uState.inCommandMode)
      {

         // append the command to switch
         packet.buffer.append(UAdapterState.MODE_COMMAND);

         // switch the state
         uState.inCommandMode = true;
      }
   }

   /**
    * Set the U state to data mode.
    */
   public void setToDataMode ()
   {
      if (uState.inCommandMode)
      {

         // append the command to switch
         packet.buffer.append(UAdapterState.MODE_DATA);

         // switch the state
         uState.inCommandMode = false;
      }
   }

   /**
    * Append a get parameter to the packet.
    *
    * @param  parameter  parameter to get
    *
    * @return the number offset in the return packet to get the
    *          result of this operation
    */
   public int getParameter (int parameter)
   {

      // set to command mode
      setToCommandMode();

      // append paramter get
      packet.buffer.append(( char ) (CONFIG_MASK | parameter >> 3));

      // add to the return number of bytes
      totalReturnLength++;
      packet.returnLength++;

      // check for packet too large
      if (packet.buffer.length() > MAX_BYTES_STREAMED)
         newPacket();

      return (totalReturnLength - 1);
   }

   /**
    * Append a set parameter to the packet.
    *
    * @param  parameter       parameter to set
    * @param  parameterValue  parameter value
    *
    * @return the number offset in the return packet to get the
    *          result of this operation
    */
   public int setParameter (char parameter, char parameterValue)
   {

      // set to command mode
      setToCommandMode();

      // append the paramter set with value
      packet.buffer.append(( char ) ((CONFIG_MASK | parameter)
                                     | parameterValue));

      // add to the return number of bytes
      totalReturnLength++;
      packet.returnLength++;

      // check for packet too large
      if (packet.buffer.length() > MAX_BYTES_STREAMED)
         newPacket();

      return (totalReturnLength - 1);
   }

   /**
    * Append a send command to the packet.  This command does not
    * elicit a response byte.
    *
    * @param  command       command to send
    * @param expectResponse
    *
    * @return the number offset in the return packet to get the
    *          result of this operation (if there is one)
    */
   public int sendCommand (char command, boolean expectResponse)
   {

      // set to command mode
      setToCommandMode();

      // append the paramter set with value
      packet.buffer.append(command);

      // check for response
      if (expectResponse)
      {

         // add to the return number of bytes
         totalReturnLength++;
         packet.returnLength++;
      }

      // check for packet too large
      if (packet.buffer.length() > MAX_BYTES_STREAMED)
         newPacket();

      return (totalReturnLength - 1);
   }

   //--------
   //-------- 1-Wire Network result interpretation methods
   //--------

   /**
    * Interpret the block of bytes
    *
    * @param dataByteResponse
    * @param responseOffset
    * @param result
    * @param offset
    * @param len
    */
   public void interpretDataBytes (char[] dataByteResponse, int responseOffset,
                                   byte[] result, int offset, int len)
   {
      char result_byte;
      int temp_offset, i, j;

      for (i = 0; i < len; i++)
      {
         // convert the rest to OneWireIOExceptions
         if (bitsOnly)
         {
            temp_offset = responseOffset + 8 * i;

            // provide debug output
            if (doDebugMessages)
               System.out.println("DEBUG: UPacketbuilder-interpretDataBytes[] responseOffset "
                            + responseOffset + " offset " + offset + " lenbuf " + dataByteResponse.length);

            // loop through and interpret each bit
            result_byte = 0;
            for (j = 0; j < 8; j++)
            {
               result_byte = (char)(result_byte >>> 1);

               if (interpretOneWireBit(dataByteResponse [temp_offset + j]))
                  result_byte |= 0x80;
            }

            result[offset + i] = (byte)(result_byte & 0xFF);
         }
         else
            result[offset + i] = (byte)dataByteResponse[responseOffset + i];
      }
   }

   /**
    * Interpret the reset response byte from a U adapter
    *
    * @param  resetResponse  reset response byte from U
    *
    * @return the number representing the result of a 1-Wire reset
    */
   public int interpretOneWireReset (char resetResponse)
   {

      // make sure the response byte structure is correct
      if ((resetResponse & 0xC0) == 0xC0)
      {

         // retrieve the chip version and program voltage state
         uState.revision                =
            ( char ) (UAdapterState.CHIP_VERSION_MASK & resetResponse);
         uState.programVoltageAvailable = ((UAdapterState.PROGRAM_VOLTAGE_MASK
                                            & resetResponse) != 0);

         // provide debug output
         if (doDebugMessages)
            System.out.println("DEBUG: UPacketbuilder-reset response "
                               + Integer.toHexString(( int ) resetResponse
                                                     & 0x00FF));

         // convert the response byte to the OneWire reset result
         switch (resetResponse & RESPONSE_RESET_MASK)
         {

            case RESPONSE_RESET_SHORT :
               return DSPortAdapter.RESET_SHORT;
            case RESPONSE_RESET_PRESENCE :

               // if in long alarm check, record this as a non alarm reset
               if (uState.longAlarmCheck)
               {

                  // check if can give up checking
                  if (uState.lastAlarmCount++ > UAdapterState.MAX_ALARM_COUNT)
                     uState.longAlarmCheck = false;
               }

               return DSPortAdapter.RESET_PRESENCE;
            case RESPONSE_RESET_ALARM :

               // alarm presense so go into DS2480 long alarm check mode
               uState.longAlarmCheck = true;
               uState.lastAlarmCount = 0;

               return DSPortAdapter.RESET_ALARM;
            case RESPONSE_RESET_NOPRESENCE :
            default :
               return DSPortAdapter.RESET_NOPRESENCE;
         }
      }
      else
         return DSPortAdapter.RESET_NOPRESENCE;
   }

   /**
    * Interpret the bit response byte from a U adapter
    *
    * @param  bitResponse  bit response byte from U
    *
    * @return boolean representing the result of a 1-Wire bit operation
    */
   public boolean interpretOneWireBit (char bitResponse)
   {

      // interpret the bit
      if ((bitResponse & RESPONSE_BIT_MASK) == RESPONSE_BIT_ONE)
         return true;
      else
         return false;
   }

   /**
    * Interpret the search response and set the 1-Wire state accordingly.
    *
    * @param  bitResponse  bit response byte from U
    *
    * @param mState
    * @param searchResponse
    * @param responseOffset
    *
    * @return boolean return is true if a valid ID was found when
    *                 interpreting the search results
    */
   public boolean interpretSearch (OneWireState mState,
                                   char[] searchResponse, int responseOffset)
   {
      char[] temp_id = new char [8];

      // change byte offset to bit offset
      int bit_offset = responseOffset * 8;

      // set the temp Last Descrep to none
      int temp_last_descrepancy        = 0xFF;
      int temp_last_family_descrepancy = 0;

      // interpret the search response sequence
      for (int i = 0; i < 64; i++)
      {

         // get the SerialNum bit
         bitWrite(temp_id, i,
                  bitRead(searchResponse, (i * 2) + 1 + bit_offset));

         // check LastDiscrepancy
         if (bitRead(searchResponse, i * 2 + bit_offset)
                 &&!bitRead(searchResponse, i * 2 + 1 + bit_offset))
         {
            temp_last_descrepancy = i + 1;

            // check LastFamilyDiscrepancy
            if (i < 8)
               temp_last_family_descrepancy = i + 1;
         }
      }

      // check
      byte[] id = new byte [8];

      for (int i = 0; i < 8; i++)
         id [i] = ( byte ) temp_id [i];

      // check results
      if ((!Address.isValid(id)) || (temp_last_descrepancy == 63)
              || (temp_id [0] == 0))
         return false;

         // successful search
      else
      {

         // check for lastone
         if ((temp_last_descrepancy == mState.searchLastDiscrepancy)
                 || (temp_last_descrepancy == 0xFF))
            mState.searchLastDevice = true;

         // copy the ID number to the buffer
         for (int i = 0; i < 8; i++)
            mState.ID [i] = ( byte ) temp_id [i];

         // set the count
         mState.searchLastDiscrepancy       = temp_last_descrepancy;
         mState.searchFamilyLastDiscrepancy = temp_last_family_descrepancy;

         return true;
      }
   }

   /**
    * Interpret the data response byte from a primed byte operation
    *
    * @param primedDataResponse
    * @param responseOffset
    *
    * @return the byte representing the result of a 1-Wire data byte
    */
   public byte interpretPrimedByte (char[] primedDataResponse,
                                    int responseOffset)
   {
      char result_byte = 0;

      // loop through and interpret each bit
      for (int i = 0; i < 8; i++)
      {
         result_byte = (char)(result_byte >>> 1);

         if (interpretOneWireBit(primedDataResponse [responseOffset + i]))
            result_byte |= 0x80;
      }

      return (byte)(result_byte & 0xFF);
   }

   //--------
   //-------- Misc Utility methods
   //--------

   /**
    * Request the maximum rate to do an operation
    */
   public static int getDesiredBaud (int operation, int owSpeed, int maxBaud)
   {
      int baud = 9600;

      switch (operation)
      {

         case OPERATION_BYTE :
            if (owSpeed == DSPortAdapter.SPEED_OVERDRIVE)
               baud = 115200;
            else
               baud = 9600;
            break;
         case OPERATION_SEARCH :
            if (owSpeed == DSPortAdapter.SPEED_OVERDRIVE)
               baud = 57600;
            else
               baud = 9600;
            break;
      }

      if (baud > maxBaud)
         baud = maxBaud;

      return baud;
   }

   /**
    * Bit utility to read a bit in the provided array of chars.
    *
    * @param  bitBuffer array of chars where the bit to read is located
    * @param  address   bit location to read (LSBit of first Byte in bitBuffer
    *                    is postion 0)
    *
    * @return the boolean value of the bit position
    */
   public boolean bitRead (char[] bitBuffer, int address)
   {
      int byte_number, bit_number;

      byte_number = (address / 8);
      bit_number  = address - (byte_number * 8);

      return ((( char ) ((bitBuffer [byte_number] >> bit_number) & 0x01))
              == 0x01);
   }

   /**
    * Bit utility to write a bit in the provided array of chars.
    *
    * @param  bitBuffer array of chars where the bit to write is located
    * @param  address   bit location to write (LSBit of first Byte in bitBuffer
    *                    is postion 0)
    * @param  newBitState new bit state
    */
   public void bitWrite (char[] bitBuffer, int address, boolean newBitState)
   {
      int byte_number, bit_number;

      byte_number = (address / 8);
      bit_number  = address - (byte_number * 8);

      if (newBitState)
         bitBuffer [byte_number] |= ( char ) (0x01 << bit_number);
      else
         bitBuffer [byte_number] &= ( char ) (~(0x01 << bit_number));
   }
}
