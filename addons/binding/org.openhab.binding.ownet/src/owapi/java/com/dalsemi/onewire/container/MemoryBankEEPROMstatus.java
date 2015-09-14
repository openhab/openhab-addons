
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
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.utils.*;
import com.dalsemi.onewire.container.OneWireContainer;


/**
 * Memory bank class for the EEPROM section of iButtons and 1-Wire devices on the DS2408.
 *
 *  @version    0.00, 28 Aug 2000
 *  @author     DS
 */
class MemoryBankEEPROMstatus
   implements MemoryBank
{

   //--------
   //-------- Static Final Variables
   //--------

   /**
     * Read Memory Command
     */
   public static final byte READ_MEMORY_COMMAND = ( byte ) 0xF0;

   /**
     * Write Scratchpad Command
     */
   public static final byte WRITE_SCRATCHPAD_COMMAND = ( byte ) 0x0F;

   /**
     * Read Scratchpad Command
     */
   public static final byte READ_SCRATCHPAD_COMMAND = ( byte ) 0xAA;

   /**
     * Copy Scratchpad Command
     */
   public static final byte COPY_SCRATCHPAD_COMMAND = ( byte ) 0x55;

   /**
    * Channel acces write to change the property of the channel
    */
   public static final byte CHANNEL_ACCESS_WRITE = ( byte ) 0x5A;


   //--------
   //-------- Variables
   //--------

   /**
    * Reference to the OneWireContainer this bank resides on.
    */
   protected OneWireContainer ib;

   /**
    * block of 0xFF's used for faster read pre-fill of 1-Wire blocks
    */
   protected byte[] ffBlock;

   /**
    * Flag to indicate that speed needs to be set
    */
   protected boolean doSetSpeed;

   //--------
   //-------- Protected Variables for MemoryBank implementation
   //--------

   /**
    * Size of memory bank in bytes
    */
   protected int size;

   /**
    * Memory bank descriptions
    */
   protected String bankDescription;

   /**
    * Memory bank usage flags
    */
   protected boolean generalPurposeMemory;

   /**
    * Flag if memory bank is read/write
    */
   protected boolean readWrite;

   /**
    * Flag if memory bank is write once (EPROM)
    */
   protected boolean writeOnce;

   /**
    * Flag if memory bank is read only
    */
   protected boolean readOnly;

   /**
    * Flag if memory bank is non volatile
    * (will not erase when power removed)
    */
   protected boolean nonVolatile;

   /**
    * Flag if memory bank needs program Pulse to write
    */
   protected boolean programPulse;

   /**
    * Flag if memory bank needs power delivery to write
    */
   protected boolean powerDelivery;

   /**
    * Starting physical address in memory bank.  Needed for different
    * types of memory in the same logical memory bank.  This can be
    * used to seperate them into two virtual memory banks.  Example:
    * DS2406 status page has mixed EPROM and Volatile RAM.
    */
   protected int startPhysicalAddress;

   /**
    * Flag if read back verification is enabled in 'write()'.
    */
   protected boolean writeVerification;

   //--------
   //-------- Protected Variables for PagedMemoryBank implementation
   //--------

   /**
    * Number of pages in memory bank
    */
   protected int numberPages;

   /**
    *  page length in memory bank
    */
   protected int pageLength;

   /**
    * Max data length in page packet in memory bank
    */
   protected int maxPacketDataLength;

   /**
    * Flag if memory bank has page auto-CRC generation
    */
   protected boolean pageAutoCRC;


   //--------
   //-------- Constructor
   //--------

   /**
    * Memory bank contstuctor.  Requires reference to the OneWireContainer
    * this memory bank resides on.  Requires reference to memory banks used
    * in OTP operations.
    */
   public MemoryBankEEPROMstatus (OneWireContainer ibutton)
   {

      // keep reference to ibutton where memory bank is
      ib = ibutton;

      // initialize attributes of this memory bank - DEFAULT: Main memory DS1985 w/o lock stuff
      generalPurposeMemory = false;
      bankDescription      = "Main Memory";
      numberPages          = 1;
      readWrite            = false;
      writeOnce            = false;
      readOnly             = false;
      nonVolatile          = true;
      pageAutoCRC          = true;
      programPulse         = false;
      powerDelivery        = false;
      writeVerification    = false;
      startPhysicalAddress = 0;
      doSetSpeed           = true;

      // create the ffblock (used for faster 0xFF fills)
      ffBlock = new byte [20];

      for (int i = 0; i < 20; i++)
         ffBlock [i] = ( byte ) 0xFF;
   }

   //--------
   //-------- MemoryBank query methods
   //--------

   /**
    * Query to see get a string description of the current memory bank.
    *
    * @return  String containing the memory bank description
    */
   public String getBankDescription ()
   {
      return bankDescription;
   }

   /**
    * Query to see if the current memory bank is general purpose
    * user memory.  If it is NOT then it is Memory-Mapped and writing
    * values to this memory will affect the behavior of the 1-Wire
    * device.
    *
    * @return  'true' if current memory bank is general purpose
    */
   public boolean isGeneralPurposeMemory ()
   {
      return generalPurposeMemory;
   }

   /**
    * Query to see if current memory bank is read/write.
    *
    * @return  'true' if current memory bank is read/write
    */
   public boolean isReadWrite ()
   {
      return readWrite;
   }

   /**
    * Query to see if current memory bank is write write once such
    * as with EPROM technology.
    *
    * @return  'true' if current memory bank can only be written once
    */
   public boolean isWriteOnce ()
   {
      return writeOnce;
   }

   /**
    * Query to see if current memory bank is read only.
    *
    * @return  'true' if current memory bank can only be read
    */
   public boolean isReadOnly ()
   {
      return readOnly;
   }

   /**
    * Query to see if current memory bank non-volatile.  Memory is
    * non-volatile if it retains its contents even when removed from
    * the 1-Wire network.
    *
    * @return  'true' if current memory bank non volatile.
    */
   public boolean isNonVolatile ()
   {
      return nonVolatile;
   }

   /**
    * Query to see if current memory bank pages need the adapter to
    * have a 'ProgramPulse' in order to write to the memory.
    *
    * @return  'true' if writing to the current memory bank pages
    *                 requires a 'ProgramPulse'.
    */
   public boolean needsProgramPulse ()
   {
      return programPulse;
   }

   /**
    * Query to see if current memory bank pages need the adapter to
    * have a 'PowerDelivery' feature in order to write to the memory.
    *
    * @return  'true' if writing to the current memory bank pages
    *                 requires 'PowerDelivery'.
    */
   public boolean needsPowerDelivery ()
   {
      return powerDelivery;
   }

   /**
    * Query to get the starting physical address of this bank.  Physical
    * banks are sometimes sub-divided into logical banks due to changes
    * in attributes.
    *
    * @return  physical starting address of this logical bank.
    */
   public int getStartPhysicalAddress ()
   {
      return startPhysicalAddress;
   }

   /**
    * Query to get the memory bank size in bytes.
    *
    * @return  memory bank size in bytes.
    */
   public int getSize ()
   {
      return size;
   }

   //--------
   //-------- PagedMemoryBank query methods
   //--------

   /**
    * Query to get the number of pages in current memory bank.
    *
    * @return  number of pages in current memory bank
    */
   public int getNumberPages ()
   {
      return numberPages;
   }

   /**
    * Query to get  page length in bytes in current memory bank.
    *
    * @return   page length in bytes in current memory bank
    */
   public int getPageLength ()
   {
      return pageLength;
   }

   /**
    * Query to get Maximum data page length in bytes for a packet
    * read or written in the current memory bank.  See the 'ReadPagePacket()'
    * and 'WritePagePacket()' methods.  This method is only usefull
    * if the current memory bank is general purpose memory.
    *
    * @return  max packet page length in bytes in current memory bank
    */
   public int getMaxPacketDataLength ()
   {
      return maxPacketDataLength;
   }

   /**
    * Query to see if current memory bank pages can be read with
    * the contents being verified by a device generated CRC.
    * This is used to see if the 'ReadPageCRC()' can be used.
    *
    * @return  'true' if current memory bank can be read with self
    *          generated CRC.
    */
   public boolean hasPageAutoCRC ()
   {
      return pageAutoCRC;
   }

   /**
    * Checks to see if this memory bank's pages deliver extra
    * information outside of the normal data space,  when read.  Examples
    * of this may be a redirection byte, counter, tamper protection
    * bytes, or SHA-1 result.  If this method returns true then the
    * methods with an 'extraInfo' parameter can be used:
    * {@link #readPage(int,boolean,byte[],int,byte[]) readPage},
    * {@link #readPageCRC(int,boolean,byte[],int,byte[]) readPageCRC}, and
    * {@link #readPagePacket(int,boolean,byte[],int,byte[]) readPagePacket}.
    *
    * @return  <CODE> true </CODE> if reading the this memory bank's
    *                 pages provides extra information
    *
    * @see #readPage(int,boolean,byte[],int,byte[]) readPage(extra)
    * @see #readPageCRC(int,boolean,byte[],int,byte[]) readPageCRC(extra)
    * @see #readPagePacket(int,boolean,byte[],int,byte[]) readPagePacket(extra)
    * @since 1-Wire API 0.01
    */
   public boolean hasExtraInfo ()
   {
      return false;
   }

   /**
    * Query to get the length in bytes of extra information that
    * is read when read a page in the current memory bank.  See
    * 'hasExtraInfo()'.
    *
    * @return  number of bytes in Extra Information read when reading
    *          pages in the current memory bank.
    */
   public int getExtraInfoLength ()
   {
      return 0;
   }

   /**
    * Query to get a string description of what is contained in
    * the Extra Informationed return when reading pages in the current
    * memory bank.  See 'hasExtraInfo()'.
    *
    * @return string describing extra information.
    */
   public String getExtraInfoDescription ()
   {
      return null;
   }

   /**
    * Set the write verification for the 'write()' method.
    *
    * @param  doReadVerf   true (default) verify write in 'write'
    *                      false, don't verify write (used on
    *                      Write-Once bit manipulation)
    */
   public void setWriteVerification (boolean doReadVerf)
   {
      writeVerification = doReadVerf;
   }

   //--------
   //-------- MemoryBank I/O methods
   //--------

   /**
    * Read  memory in the current bank with no CRC checking (device or
    * data). The resulting data from this API may or may not be what is on
    * the 1-Wire device.  It is recommends that the data contain some kind
    * of checking (CRC) like in the readPagePacket() method or have
    * the 1-Wire device provide the CRC as in readPageCRC().  readPageCRC()
    * however is not supported on all memory types, see 'hasPageAutoCRC()'.
    * If neither is an option then this method could be called more
    * then once to at least verify that the same thing is read consistantly.
    *
    * @param  startAddr     starting physical address
    * @param  readContinue  if 'true' then device read is continued without
    *                       re-selecting.  This can only be used if the new
    *                       read() continious where the last one led off
    *                       and it is inside a 'beginExclusive/endExclusive'
    *                       block.
    * @param  readBuf       byte array to place read data into
    * @param  offset        offset into readBuf to place data
    * @param  len           length in bytes to read
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void read (int startAddr, boolean readContinue, byte[] readBuf,
                     int offset, int len)
      throws OneWireIOException, OneWireException
   {
      byte[] buff = new byte[20];
      int addr = startPhysicalAddress + startAddr;

      System.arraycopy(ffBlock,0,buff,0,20);

      // check if read exceeds memory
      if ((startAddr + len) > size)
         throw new OneWireException("Read exceeds memory bank end");

      // attempt to put device at max desired speed
      // attempt to put device at max desired speed
      if (!readContinue)
      {
         checkSpeed();

         // select the device
         if (ib.adapter.select(ib.address))
         {
            buff[0] = READ_MEMORY_COMMAND;

            // address 1
            buff[1] = ( byte ) (addr & 0xFF);
            // address 2
            buff[2] = ( byte ) (((addr & 0xFFFF) >>> 8) & 0xFF);

            ib.adapter.dataBlock(buff, 0, len+3);

            // extract the data
            System.arraycopy(buff, 3, readBuf, offset, len);
         }
         else
            throw new OneWireIOException("Device select failed");
      }
      else
      {
         ib.adapter.dataBlock(buff, 0, len);

         // extract the data
         System.arraycopy(buff, 0, readBuf, offset, len);
      }
   }

   /**
    * Write  memory in the current bank.  It is recommended that
    * when writing  data that some structure in the data is created
    * to provide error free reading back with read().  Or the
    * method 'writePagePacket()' could be used which automatically
    * wraps the data in a length and CRC.
    *
    * When using on Write-Once devices care must be taken to write into
    * into empty space.  If write() is used to write over an unlocked
    * page on a Write-Once device it will fail.  If write verification
    * is turned off with the method 'setWriteVerification(false)' then
    * the result will be an 'AND' of the existing data and the new data.
    *
    * @param  startAddr     starting address
    * @param  writeBuf      byte array containing data to write
    * @param  offset        offset into writeBuf to get data
    * @param  len           length in bytes to write
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void write (int startAddr, byte[] writeBuf, int offset, int len)
      throws OneWireIOException, OneWireException
   {
      int i;
      byte[] es_data    = new byte[3];
      byte[] scratchpad = new byte[8];

      // return if nothing to do
      if (len == 0)
         return;

      // attempt to put device at speed
      checkSpeed();

      // check if write exceeds memory
      if ((startAddr + len) > size)
         throw new OneWireException("Write exceeds memory bank end");

      // check if trying to write read only bank
      if (isReadOnly() && (((startPhysicalAddress+startAddr) != 137) && (len != 1)))
         throw new OneWireException("Trying to write read-only memory bank");

      if(((startPhysicalAddress+startAddr) == 137) && (len == 1))
      {
         ib.adapter.select(ib.address);

         byte[] buffer = new byte[5];

         buffer[0] = CHANNEL_ACCESS_WRITE;
         buffer[1] = writeBuf[offset];
         buffer[2] = (byte) ~writeBuf[offset];
         System.arraycopy(ffBlock,0,buffer,3,2);

         ib.adapter.dataBlock(buffer, 0, 5);

         if(buffer[3] != (byte) 0x00AA)
         {
            throw new OneWireIOException("Failure to change DS2408 latch state: buffer=" + Convert.toHexString(buffer));
         }
      }
      else if(((startPhysicalAddress+startAddr) > 138) &&
              ((startPhysicalAddress+startAddr+len) < 143))
      {
         ib.adapter.select(ib.address);

         byte[] buffer = new byte[6];

         buffer[0] = (byte) 0xCC;
         buffer[1] = (byte) ((startAddr+startPhysicalAddress) & 0xFF);
         buffer[2] = (byte) ((((startAddr+startPhysicalAddress) & 0xFFFF) >>> 8) & 0xFF);

         System.arraycopy(writeBuf,offset,buffer,3,len);

         ib.adapter.dataBlock(buffer,0,len+3);
      }
      else if(((startPhysicalAddress+startAddr) > 127) &&
              ((startPhysicalAddress+startAddr+len) < 130))
      {
         byte[] buffer = new byte[8];
         int addr = 128;
         byte[] buff = new byte[11];

         System.arraycopy(ffBlock,0,buff,0,11);

         ib.adapter.select(ib.address);

         buff[0] = READ_MEMORY_COMMAND;

         // address 1
         buff[1] = ( byte ) (addr & 0xFF);
         // address 2
         buff[2] = ( byte ) (((addr & 0xFFFF) >>> 8) & 0xFF);

         ib.adapter.dataBlock(buff, 0, 11);

         // extract the data
         System.arraycopy(buff, 3, buffer, 0, 8);

         System.arraycopy(writeBuf,offset,buffer,0,len);

         // write the page of data to scratchpad
         if(!writeScratchpad(startPhysicalAddress+startAddr,buffer,0,8))
            throw new OneWireIOException("Invalid CRC16 in write");

         if(!readScratchpad(scratchpad,0,8,es_data))
            throw new OneWireIOException("Read scratchpad was not successful.");

         if((es_data[2] & 0x20) == 0x20)
         {
            throw new OneWireIOException("The write scratchpad command was not completed.");
         }
         else
         {
            for(i=0;i<8;i++)
               if(scratchpad[i] != buffer[i])
               {
                  throw new
                     OneWireIOException("The read back of the data in the scratch pad did " +
                                        "not match.");
               }
         }

         // Copy data from scratchpad into memory
         copyScratchpad(es_data);
      }
      else
         throw new OneWireIOException("Trying to write read-only memory.");
   }

   //--------
   //-------- checkSpeed methods
   //--------

   /**
    * Check the device speed if has not been done before or if
    * an error was detected.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void checkSpeed ()
      throws OneWireIOException, OneWireException
   {
      synchronized (this)
      {

         // only check the speed
         if (doSetSpeed)
         {

            // attempt to set the correct speed and verify device present
            ib.doSpeed();

            // no execptions so clear flag
            doSetSpeed = false;
         }
      }
   }

   /**
    * Set the flag to indicate the next 'checkSpeed()' will force
    * a speed set and verify 'doSpeed()'.
    */
   public void forceVerify ()
   {
      synchronized (this)
      {
         doSetSpeed = true;
      }
   }

   /**
    * Write to the Scratch Pad, which is a max of 8 bytes...  Note that if
    * less than 8 bytes are written, the ending offset will still report
    * that a full eight bytes are on the buffer.  This means that all 8 bytes
    * of the data in the scratchpad will be copied, not just the bytes user
    * wrote into it.
    *
    * @param  addr          the address to write the data to
    * @param  out_buf       byte array to write into scratch pad
    * @param  offset        offset into out_buf to write the data
    * @param  len           length of the write data
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public boolean writeScratchpad(int addr, byte[] out_buf, int offset, int len)
      throws OneWireIOException, OneWireException
   {
      byte[] send_block = new byte [14];

      // protect send buffer
      // since the scratchpad is only eight bytes, there is no reason to write
      // more than eight bytes..  and we can optimize our send buffer's size.
      if(len>8)
         len = 8;

      // access the device
      if (ib.adapter.select(ib.getAddress()))
      {
         int cnt = 0;
         // set data block up
         // start by sending the write scratchpad command
         send_block [cnt++] = WRITE_SCRATCHPAD_COMMAND;
         // followed by the target address
         send_block [cnt++] = ( byte ) (addr & 0x00FF);
         send_block [cnt++] = ( byte ) (((addr & 0x00FFFF) >>> 8) & 0x00FF);

         // followed by the data to write to the scratchpad
         System.arraycopy(out_buf,offset,send_block,3,len);
         cnt += len;

         // followed by two bytes for reading CRC16 value
         send_block [cnt++] = ( byte ) 0x00FF;
         send_block [cnt++] = ( byte ) 0x00FF;

         // send the data
         ib.adapter.dataBlock(send_block, 0, cnt);

         // verify the CRC is correct
//         if (CRC16.compute(send_block, 0, cnt) != 0x0000B001)
//            throw new OneWireIOException("Invalid CRC16 in Writing Scratch Pad");
      }
      else
         throw new OneWireIOException("Device select failed.");

      return true;
   }

   /**
    * Copy all 8 bytes of the Sratch Pad to a certain address in memory.
    *
    * @param addr the address to copy the data to
    * @param auth byte[] containing write authorization
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public synchronized void copyScratchpad(byte[] es_data)
      throws OneWireIOException, OneWireException
   {
      byte[] send_block = new byte[4];

      // access the device
      if (ib.adapter.select(ib.getAddress()))
      {
         // ending address with data status
         send_block[3] = es_data[2];//ES;

         // address 2
         send_block[2] = es_data[1];//TA2

         // address 1
         send_block[1] = es_data[0];//TA1;

         // Copy command
         send_block[0] = COPY_SCRATCHPAD_COMMAND;

         // send copy scratchpad command
         ib.adapter.dataBlock(send_block, 0, 3);

         // provide strong pull-up for copy
         ib.adapter.setPowerDuration(DSPortAdapter.DELIVERY_INFINITE);
         ib.adapter.startPowerDelivery(DSPortAdapter.CONDITION_AFTER_BYTE);
         ib.adapter.putByte(send_block[3]);

         // pause before checking result
         try
         {
            Thread.sleep(12);
         }
         catch (InterruptedException e){}

         ib.adapter.setPowerNormal();

         // get result
         byte test = ( byte ) ib.adapter.getByte();

         if(test == ( byte ) 0x00FF)
         {
            throw new OneWireIOException("The scratchpad did not get copied to memory.");
         }
      }
      else
         throw new OneWireIOException("Device select failed.");
   }

   /**
    * Read from the Scratch Pad, which is a max of 8 bytes.
    *
    * @param  readBuf       byte array to place read data into
    *                       length of array is always pageLength.
    * @param  offset        offset into readBuf to pug data
    * @param  len           length in bytes to read
    * @param  extraInfo     byte array to put extra info read into
    *                       (TA1, TA2, e/s byte)
    *                       Can be 'null' if extra info is not needed.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public boolean readScratchpad (byte[] readBuf, int offset, int len,
                                  byte[] es_data)
      throws OneWireIOException, OneWireException
   {
      // select the device
      if (!ib.adapter.select(ib.address))
      {
         forceVerify();
         throw new OneWireIOException("Device select failed");
      }

      // build first block
      byte[] raw_buf = new byte [14];
      raw_buf [0] = READ_SCRATCHPAD_COMMAND;
      System.arraycopy(ffBlock, 0, raw_buf, 1, 13);

      // do data block for TA1, TA2, and E/S
      // followed by 8 bytes of data and 2 bytes of crc
      ib.adapter.dataBlock(raw_buf, 0, 14);

      // verify CRC16 is correct
      if (CRC16.compute(raw_buf, 0, 14) == 0x0000B001)
      {
         // optionally extract the extra info
         if (es_data != null)
            System.arraycopy(raw_buf, 1, es_data, 0, 3);

         System.arraycopy(raw_buf, 4, readBuf, offset, len);
         // success
         return true;
      }
      else
         throw new OneWireException("Error due to CRC.");
   }

}
