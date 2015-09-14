
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
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.adapter.*;


/**
 * <P>1-Wire&#174 container that encapsulates the functionality of the 1-Wire
 * family type <B>02</B> (hex), Dallas Semiconductor part number: <B>DS1991,
 * MultiKey</B>.</P>
 *
 * <P> This iButton is primarily used as a minimal security read/write portable memory device. </P>
 *
 * <H2> Features </H2>
 * <UL>
 *   <LI> Three 384 bit (48 bytes) password protected memory blocks
 *   <LI> 64 bit (8 byte) password per memory block
 *   <LI> 64 bit (8 byte) identification per memory block
 *   <LI> Data integrity assured with strict read/write
 *        protocols
 *   <LI> Operating temperature range from -40&#176C to
 *        +70&#176C
 *   <LI> Over 10 years of data retention
 * </UL>
 *
 * <H2> Memory </H2>
 *
 * <P> All memory is accessed through read/write routines, no <code>MemoryBank</code> classes are used.</P>
 *
 * <H2> Alternate Names </H2>
 * <UL>
 *   <LI> DS1425 (Family 82)
 * </UL>
 *
 * <H2> DataSheets </H2>
 *
 *   <A HREF="http://pdfserv.maxim-ic.com/arpdf/DS1991.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS1991.pdf</A><br>
 *   <A HREF="http://pdfserv.maxim-ic.com/arpdf/DS1425.pdf"> http://pdfserv.maxim-ic.com/arpdf/DS1425.pdf</A>
 *
 *
 *  @version    0.00, 28 Aug 2000
 *  @author     DS,GH
 */
public class OneWireContainer02
   extends OneWireContainer
{

   //--------
   //-------- Static Final Variables
   //--------

   /**
    * DS1991 Write Scratchpad Command
    */
   private static final byte WRITE_SCRATCHPAD_COMMAND = ( byte ) 0x96;

   /**
    * DS1991 Read Scratchpad Command
    */
   private static final byte READ_SCRATCHPAD_COMMAND = 0x69;

   /**
    * DS1991 Copy Scratchpad Command
    */
   private static final byte COPY_SCRATCHPAD_COMMAND = 0x3C;

   /**
    * DS1991 Write Password Command
    */
   private static final byte WRITE_PASSWORD_COMMAND = 0x5A;

   /**
    * DS1991 Write SubKey Command
    */
   private static final byte WRITE_SUBKEY_COMMAND = ( byte ) 0x99;

   /**
    * DS1991 Read SubKey Command
    */
   private static final byte READ_SUBKEY_COMMAND = 0x66;

   /**
    * DS1991 Block code commands
    */
   private static byte blockCodes [][] = null;

   //--------
   //-------- Variables
   //--------

   /**
    * General purpose buffer
    */
   private byte[] buffer = new byte [82];

   //--------
   //-------- Constructor
   //--------
   static
   {
      blockCodes = new byte [9][8];

      initBlockCodes(blockCodes);
   }

   /**
    * Create an empty container.  Must call <code>setupContainer</code> before
    * using this new container.<p>
    *
    * This is one of the methods to construct a container.  The others are
    * through creating a OneWireContainer with parameters.
    *
    * @see #OneWireContainer02(DSPortAdapter,byte[])
    * @see #OneWireContainer02(DSPortAdapter,long)
    * @see #OneWireContainer02(DSPortAdapter,String)
    * @see #setupContainer(DSPortAdapter,byte[])
    * @see #setupContainer(DSPortAdapter,long)
    * @see #setupContainer(DSPortAdapter,String)
    */
   public OneWireContainer02 ()
   {
      super();
   }

   /**
    * Create a container with a provided adapter object
    * and the address of the iButton or 1-Wire device.<p>
    *
    * This is one of the methods to construct a container.  The other is
    * through creating a OneWireContainer with NO parameters.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this iButton.
    * @param  newAddress        address of this 1-Wire device
    * @see #OneWireContainer02()
    * @see com.dalsemi.onewire.utils.Address
    */
   public OneWireContainer02 (DSPortAdapter sourceAdapter, byte[] newAddress)
   {
      super(sourceAdapter, newAddress);
   }

   /**
    * Create a container with a provided adapter object
    * and the address of the iButton or 1-Wire device.<p>
    *
    * This is one of the methods to construct a container.  The other is
    * through creating a OneWireContainer with NO parameters.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this iButton.
    * @param  newAddress        address of this 1-Wire device
    * @see #OneWireContainer02()
    * @see com.dalsemi.onewire.utils.Address
    */
   public OneWireContainer02 (DSPortAdapter sourceAdapter, long newAddress)
   {
      super(sourceAdapter, newAddress);
   }

   /**
    * Create a container with a provided adapter object
    * and the address of the iButton or 1-Wire device.<p>
    *
    * This is one of the methods to construct a container.  The other is
    * through creating a OneWireContainer with NO parameters.
    *
    * @param  sourceAdapter     adapter object required to communicate with
    * this iButton.
    * @param  newAddress        address of this 1-Wire device
    * @see #OneWireContainer02()
    * @see com.dalsemi.onewire.utils.Address
    */
   public OneWireContainer02 (DSPortAdapter sourceAdapter, String newAddress)
   {
      super(sourceAdapter, newAddress);
   }

   //--------
   //-------- Information methods
   //--------

   /**
    */
   public String getName ()
   {
      return "DS1991";
   }

   /**
    */
   public String getAlternateNames ()
   {
      return "DS1425";
   }

   /**
    */
   public String getDescription ()
   {
      return "2048 bits of nonvolatile read/write memory "
             + "organized as three secure keys of 384 bits each "
             + "and a 512 bit scratch pad. Each key has its own "
             + "64 bit password and 64 bit ID field.  Secure "
             + "memory cannot be deciphered without matching 64 "
             + "bit password.";
   }

   //--------
   //-------- I/O methods
   //--------

   /**
    * Writes the data to the scratchpad from the given address.
    *
    * @param  addr     address to begin writing.  Must be between
    * 0x00 and 0x3F.
    * @param  data     data to write.
    *
    *
    * @throws IllegalArgumentException If address is out of range, or data is to long for scratchpad
    * @throws OneWireIOException If device is not found on the 1-Wire network
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public void writeScratchpad (int addr, byte[] data)
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {

      //confirm that data will fit
      if (addr > 0x3F)
         throw new IllegalArgumentException(
            "Address out of range: 0x00 to 0x3F");

      int dataRoom = 0x3F - addr + 1;

      if (dataRoom < data.length)
         throw new IllegalArgumentException(
            "Data is too long for scratchpad.");

      buffer [0] = WRITE_SCRATCHPAD_COMMAND;
      buffer [1] = ( byte ) (addr | 0xC0);
      buffer [2] = ( byte ) (~buffer [1]);

      System.arraycopy(data, 0, buffer, 3, data.length);

      //send command block
      if (adapter.select(address))
      {
         adapter.dataBlock(buffer, 0, 3 + data.length);
      }
      else
      {

         //device must not have been present
         throw new OneWireIOException("MultiKey iButton "
                                      + this.getAddressAsString()
                                      + " not found on 1-Wire Network");
      }
   }

   /**
    * Reads the entire scratchpad.
    *
    * @return  <code>byte[]</code> containing the data from the scratchpad;
    * the array will have a length of 64.
    *
    * @throws OneWireIOException If device is not found on the 1-Wire network
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public byte[] readScratchpad ()
      throws OneWireIOException, OneWireException
   {
      buffer [0] = READ_SCRATCHPAD_COMMAND;
      buffer [1] = ( byte ) 0xC0;   //Starting address of scratchpad
      buffer [2] = 0x3F;

      for (int i = 3; i < 67; i++)
         buffer [i] = ( byte ) 0xFF;

      //send command block
      if (adapter.select(address))
      {
         adapter.dataBlock(buffer, 0, 67);

         byte[] retData = new byte [64];

         System.arraycopy(buffer, 3, retData, 0, 64);

         return retData;
      }
      else
      {

         //device must not have been present
         throw new OneWireIOException("MultiKey iButton "
                                      + this.getAddressAsString()
                                      + " not found on 1-Wire Network");
      }
   }

   /**
    * Writes the data from the scratchpad to the specified block or
    * blocks.  Note that the write will erase the data from the
    * scratchpad.
    *
    * @param  key          subkey being written
    * @param  passwd       password for the subkey being written
    * @param  blockNum     number of the block to be copied (see page 7 of the
    *                      DS1991 data sheet) block 0-7, or 8 to copy all 64 bytes.
    *
    *
    * @throws IllegalArgumentException If key is out of range (0 to 2), or password is not 8 characters, or if
    * blockNum is out of range (0 to 8)
    * @throws OneWireIOException If device is not found on the 1-Wire network
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public void copyScratchpad (int key, byte[] passwd, int blockNum)
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {

      //confirm that input is OK
      if ((key < 0) || (key > 2))
         throw new IllegalArgumentException("Key out of range: 0 to 2.");

      if (passwd.length != 8)
         throw new IllegalArgumentException(
            "Password must contain exactly 8 characters");

      if ((blockNum < 0) || (blockNum > 8))
         throw new IllegalArgumentException("Block id out of range: 0 to 8.");

      buffer [0] = COPY_SCRATCHPAD_COMMAND;
      buffer [1] = ( byte ) (key << 6);
      buffer [2] = ( byte ) (~buffer [1]);

      //set up block selector code
      System.arraycopy(blockCodes [blockNum], 0, buffer, 3, 8);

      //set up password
      System.arraycopy(passwd, 0, buffer, 11, 8);

      //send command block
      if (adapter.select(address))
      {
         adapter.dataBlock(buffer, 0, 19);
      }
      else
      {

         //device must not have been present
         throw new OneWireIOException("MultiKey iButton "
                                      + this.getAddressAsString()
                                      + " not found on 1-Wire Network");
      }
   }

   /**
    * Reads the subkey requested with the given key name and password.
    * Note that this method allows for reading from the subkey data
    * only which starts at address 0x10 within a key. It does not allow
    * reading from any earlier address within a key because the device
    * cannot be force to allow reading the password. This is why the
    * subkey number is or-ed with 0x10 in creating the address in bytes
    * 1 and 2 of the sendBlock.
    *
    * @param             key number indicating the key to be read: 0, 1, or 2
    * @param  passwd     password of destination subkey
    *
    * @return byte[] containing the data from the subkey;
    *        the array will have a length of 64, since it includes the key
    *        identifier, sent password, and 48 bytes of data.
    *
    *
    * @throws IllegalArgumentException If key is out of range (0 to 2), or password is not 8 characters, or if
    * data does not have a length of 64
    * @throws OneWireIOException If device is not found on the 1-Wire network
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public byte[] readSubkey (int key, byte[] passwd)
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {

      //create block to send back
      byte[] retData = new byte [64];

      readSubkey(retData, key, passwd);

      return retData;
   }

   /**
    * Reads the subkey requested with the given key name and password.
    * Note that this method allows for reading from the subkey data
    * only which starts at address 0x10 within a key. It does not allow
    * reading from any earlier address within a key because the device
    * cannot be force to allow reading the password. This is why the
    * subkey number is or-ed with 0x10 in creating the address in bytes
    * 1 and 2 of the sendBlock.
    *
    * @param  data       buffer of length 64 into which to write the data
    * @param  key        number indicating the key to be read: 0, 1, or 2
    * @param  passwd     password of destination subkey
    *
    *
    * @throws IllegalArgumentException If key is out of range (0 to 2), or password is not 8 characters, or if
    * data does not have a length of 64
    * @throws OneWireIOException If device is not found on the 1-Wire network
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public void readSubkey (byte[] data, int key, byte[] passwd)
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {

      //confirm key and passwd within legal parameters
      if (key > 0x03)
         throw new IllegalArgumentException("Key out of range: 0 to 2.");

      if (passwd.length != 8)
         throw new IllegalArgumentException(
            "Password must contain exactly 8 characters.");

      if (data.length != 64)
         throw new IllegalArgumentException("Data must be size 64.");

      buffer [0] = READ_SUBKEY_COMMAND;
      buffer [1] = ( byte ) ((key << 6) | 0x10);
      buffer [2] = ( byte ) (~buffer [1]);

      //prepare buffer to receive
      for (int i = 3; i < 67; i++)
         buffer [i] = ( byte ) 0xFF;

      //insert password data
      System.arraycopy(passwd, 0, buffer, 11, 8);

      //send command block
      if (adapter.select(address))
      {
         adapter.dataBlock(buffer, 0, 67);
         adapter.reset();

         //create block to send back
         System.arraycopy(buffer, 3, data, 0, 64);
      }
      else
      {

         //device must not have been present
         throw new OneWireIOException("MultiKey iButton "
                                      + this.getAddressAsString()
                                      + " not found on 1-Wire Network");
      }
   }

   /**
    * Writes a new identifier and password to the secure subkey iButton
    *
    * @param  key          number indicating the key to be read: 0, 1, or 2
    * @param  oldName      identifier of the key used to confirm the correct
    * key's password to be changed.  Must be exactly length 8.
    * @param  newName      identifier to be used for the key with the new
    * password.  Must be exactly length 8.
    * @param  newPasswd    new password for destination subkey.  Must be
    * exactly length 8.
    *
    *
    * @throws IllegalArgumentException If key value is out of range (0 to 2), or if newPasswd, newName, or oldName
    * are not 8 characters
    * @throws OneWireIOException If device is not found on the 1-Wire network
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public void writePassword (int key, byte[] oldName, byte[] newName,
                              byte[] newPasswd)
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {

      //confirm key names and passwd within legal parameters
      if (key > 0x03)
         throw new IllegalArgumentException(
            "Key value out of range: 0 to 2.");

      if (newPasswd.length != 8)
         throw new IllegalArgumentException(
            "Password must contain exactly 8 characters.");

      if (oldName.length != 8)
         throw new IllegalArgumentException(
            "Old name must contain exactly 8 characters.");

      if (newName.length != 8)
         throw new IllegalArgumentException(
            "New name must contain exactly 8 characters.");

      buffer [0] = WRITE_PASSWORD_COMMAND;
      buffer [1] = ( byte ) (key << 6);
      buffer [2] = ( byte ) (~buffer [1]);

      //prepare buffer to receive 8 bytes of the identifier
      for (int i = 3; i < 11; i++)
         buffer [i] = ( byte ) 0xFF;

      //prepare same subkey identifier for confirmation
      System.arraycopy(oldName, 0, buffer, 11, 8);

      //prepare new subkey identifier
      System.arraycopy(newName, 0, buffer, 19, 8);

      //prepare new password for writing
      System.arraycopy(newPasswd, 0, buffer, 27, 8);

      //send command block
      if (adapter.select(address))
      {
         adapter.dataBlock(buffer, 0, 35);
         adapter.reset();
      }
      else
      {

         //device must not have been present
         throw new OneWireIOException("MultiKey iButton "
                                      + this.getAddressAsString()
                                      + " not found on 1-Wire Network");
      }
   }

   /**
    * Writes new data to the secure subkey
    *
    * @param  key       number indicating the key to be written: 0, 1, or 2
    * @param  addr      address to start writing at ( 0x00 to 0x3F )
    * @param  passwd    passwird for the subkey
    * @param  data      data to be written
    *
    *
    * @throws IllegalArgumentException If key is out of range (0 to 2), or if address is out of range, or if passwd is
    * not 8 characters, or if data length is out of bounds
    * @throws OneWireIOException If device is not found on the 1-Wire network
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public void writeSubkey (int key, int addr, byte[] passwd, byte[] data)
      throws OneWireIOException, OneWireException, IllegalArgumentException
   {

      //confirm key names and passwd within legal parameters
      if (key > 0x03)
         throw new IllegalArgumentException("Key out of range: 0 to 2.");

      if ((addr < 0x00) || (addr > 0x3F))
         throw new IllegalArgumentException(
            "Address must be between 0x00 and 0x3F");

      if (passwd.length != 8)
         throw new IllegalArgumentException(
            "Password must contain exactly 8 characters.");

      if (data.length > (0x3F - addr + 1))
         throw new IllegalArgumentException("Data length out of bounds.");

      buffer [0] = WRITE_SUBKEY_COMMAND;
      buffer [1] = ( byte ) ((key << 6) | addr);
      buffer [2] = ( byte ) (~buffer [1]);

      //prepare buffer to receive 8 bytes of the identifier
      for (int i = 3; i < 11; i++)
         buffer [i] = ( byte ) 0xFF;

      //prepare same subkey identifier for confirmation
      System.arraycopy(passwd, 0, buffer, 11, 8);

      //prepare data to write
      System.arraycopy(data, 0, buffer, 19, data.length);

      //send command block
      if (adapter.select(address))
      {
         adapter.dataBlock(buffer, 0, 19 + data.length);
         adapter.reset();
      }
      else
      {

         //device must not have been present
         throw new OneWireIOException("MultiKey iButton "
                                      + this.getAddressAsString()
                                      + " not found on 1-Wire Network");
      }
   }

   /**
    * Sets up the block codes for the copy scratchpad command.
    *
    * @param codes a 2 dimensional array [9][8] to contain the
    * codes.
    */
   private static void initBlockCodes (byte[][] codes)
   {
      codes [8][0] = 0x56;            //ALL 64 bytes address 0x00 to 0x3F
      codes [8][1] = 0x56;
      codes [8][2] = 0x7F;
      codes [8][3] = 0x51;
      codes [8][4] = 0x57;
      codes [8][5] = 0x5D;
      codes [8][6] = 0x5A;
      codes [8][7] = 0x7F;
      codes [0][0] = ( byte ) 0x9A;   //identifier (block 0)
      codes [0][1] = ( byte ) 0x9A;
      codes [0][2] = ( byte ) 0xB3;
      codes [0][3] = ( byte ) 0x9D;
      codes [0][4] = 0x64;
      codes [0][5] = 0x6E;
      codes [0][6] = 0x69;
      codes [0][7] = 0x4C;
      codes [1][0] = ( byte ) 0x9A;   //password  (block 1)
      codes [1][1] = ( byte ) 0x9A;
      codes [1][2] = 0x4C;
      codes [1][3] = 0x62;
      codes [1][4] = ( byte ) 0x9B;
      codes [1][5] = ( byte ) 0x91;
      codes [1][6] = 0x69;
      codes [1][7] = 0x4C;
      codes [2][0] = ( byte ) 0x9A;   //address 0x10 to 0x17  (block 2)
      codes [2][1] = 0x65;
      codes [2][2] = ( byte ) 0xB3;
      codes [2][3] = 0x62;
      codes [2][4] = ( byte ) 0x9B;
      codes [2][5] = 0x6E;
      codes [2][6] = ( byte ) 0x96;
      codes [2][7] = 0x4C;
      codes [3][0] = 0x6A;            //address 0x18 to 0x1F  (block 3)
      codes [3][1] = 0x6A;
      codes [3][2] = 0x43;
      codes [3][3] = 0x6D;
      codes [3][4] = 0x6B;
      codes [3][5] = 0x61;
      codes [3][6] = 0x66;
      codes [3][7] = 0x43;
      codes [4][0] = ( byte ) 0x95;   //address 0x20 to 0x27  (block 4)
      codes [4][1] = ( byte ) 0x95;
      codes [4][2] = ( byte ) 0xBC;
      codes [4][3] = ( byte ) 0x92;
      codes [4][4] = ( byte ) 0x94;
      codes [4][5] = ( byte ) 0x9E;
      codes [4][6] = ( byte ) 0x99;
      codes [4][7] = ( byte ) 0xBC;
      codes [5][0] = 0x65;            //address 0x28 to 0x2F  (block 5)
      codes [5][1] = ( byte ) 0x9A;
      codes [5][2] = 0x4C;
      codes [5][3] = ( byte ) 0x9D;
      codes [5][4] = 0x64;
      codes [5][5] = ( byte ) 0x91;
      codes [5][6] = 0x69;
      codes [5][7] = ( byte ) 0xB3;
      codes [6][0] = 0x65;            //address 0x30 to 0x37  (block 6)
      codes [6][1] = 0x65;
      codes [6][2] = ( byte ) 0xB3;
      codes [6][3] = ( byte ) 0x9D;
      codes [6][4] = 0x64;
      codes [6][5] = 0x6E;
      codes [6][6] = ( byte ) 0x96;
      codes [6][7] = ( byte ) 0xB3;
      codes [7][0] = 0x65;            //address 0x38 to 0x3F  (block 7)
      codes [7][1] = 0x65;
      codes [7][2] = 0x4C;
      codes [7][3] = 0x62;
      codes [7][4] = ( byte ) 0x9B;
      codes [7][5] = ( byte ) 0x91;
      codes [7][6] = ( byte ) 0x96;
      codes [7][7] = ( byte ) 0xB3;
   }
}
