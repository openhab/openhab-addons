/*---------------------------------------------------------------------------
 * Copyright (C) 2003 Dallas Semiconductor Corporation, All Rights Reserved.
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
import com.dalsemi.onewire.utils.CRC16;
import com.dalsemi.onewire.utils.SHA;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.adapter.DSPortAdapter;

import com.dalsemi.onewire.debug.Debug;
import com.dalsemi.onewire.utils.Convert;

/**
 * Memory bank class for the Scratchpad section of SHA EEPROM iButtons and
 * 1-Wire devices with SHA write-protected memory pages.
 *
 *  @version    1.00, 11 Aug 2002
 *  @author     SH
 */
public class MemoryBankScratchSHAEE
   extends MemoryBankScratchEx
{
   /** turn on extra debugging output */
   private static final boolean DEBUG = false;


   /** Load First Secret */
   public static final byte LOAD_FIRST_SECRET = ( byte ) 0x5A;

   /** Compute next Secret command */
   public static final byte COMPUTE_NEXT_SECRET = ( byte ) 0x33;

   /** Refresh Scratchpad command */
   public static final byte REFRESH_SCRATCHPAD = ( byte ) 0xA3;

   /** cached byte[] for re-use in SHA debit applications, speeds up operation on TINI */
   private final byte[] MT_buffer = new byte [64];
   /** cached byte[] for re-use in SHA debit applications, speeds up operation on TINI */
   private final byte[] MAC_buffer = new byte[20];
   /** cached byte[] for re-use in SHA debit applications, speeds up operation on TINI */
   private final byte[] page_data_buffer = new byte [32];
   /** cached byte[] for re-use in SHA debit applications, speeds up operation on TINI */
   private final byte[] scratchpad_buffer = new byte [8];
   /** cached byte[] for re-use in SHA debit applications, speeds up operation on TINI */
   private final byte[] copy_scratchpad_buffer = new byte[4];
   /** cached byte[] for re-use in SHA debit applications, speeds up operation on TINI */
   private final byte[] read_scratchpad_buffer = new byte[8+3+3];

   /**
    * block of 0xFF's used for faster read pre-fill of 1-Wire blocks
    * Comes from OneWireContainer33 that this MemoryBank references.
    */
   protected static final byte[] ffBlock = OneWireContainer33.ffBlock;

   /**
    * block of 0x00's used for faster read pre-fill of 1-Wire blocks
    * Comes from OneWireContainer33 that this MemoryBank references.
    */
   protected static final byte[] zeroBlock = OneWireContainer33.zeroBlock;

   /**
    * The Password container to acces the 8 byte passwords
    */
   protected OneWireContainer33 owc33 = null;


   //--------
   //-------- Constructor
   //--------

   /**
    * Memory bank contstuctor.  Requires reference to the OneWireContainer
    * this memory bank resides on.
    */
   public MemoryBankScratchSHAEE (OneWireContainer33 ibutton)
   {
      super((OneWireContainer)ibutton);

      owc33 = ibutton;

      // initialize attributes of this memory bank - DEFAULT: DS1963L scratchapd
      bankDescription = "Scratchpad with CRC and 'Copy Scratchpad w/ SHA MAC'";
      pageAutoCRC = true;
      startPhysicalAddress = 0;
      size = 8;
      numberPages = 1;
      pageLength = 8;
      maxPacketDataLength = 8-3;
      extraInfo = true;
      extraInfoLength = 3;

      // COPY_SCRATCHPAD_WITH_MAC
      COPY_SCRATCHPAD_COMMAND = ( byte ) 0x55;
   }

   //--------
   //-------- PagedMemoryBank I/O methods
   //--------

   /**
    * Read a complete memory page with CRC verification provided by the
    * device.  Not supported by all devices.  See the method
    * 'hasPageAutoCRC()'.
    *
    * @param  page          page number to read
    * @param  readContinue  if 'true' then device read is continued without
    *                       re-selecting.  This can only be used if the new
    *                       readPagePacket() continious where the last one
    *                       stopped and it is inside a
    *                       'beginExclusive/endExclusive' block.
    * @param  readBuf       byte array to put data read. Must have at least
    *                       'getMaxPacketDataLength()' elements.
    * @param  offset        offset into readBuf to place data
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void readPageCRC (int page, boolean readContinue, byte[] readBuf,
                            int offset)
      throws OneWireIOException, OneWireException
   {
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         Debug.debug("-----------------------------------------------------------");
         Debug.debug("MemoryBankScratchSHAEE.readPageCRC(int, boolean, byte[], int) called");
         Debug.debug("  romID="+owc33.getAddressAsString());
         Debug.debug("  page="+page);
         Debug.debug("  readContinue="+readContinue);
         Debug.debug("  offset="+offset);
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      //byte[] extraInfo = new byte [extraInfoLength];

      readPageCRC(page, readContinue, readBuf, offset, null);

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
         Debug.debug("-----------------------------------------------------------");
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
   }

   /**
    * Read a complete memory page with CRC verification provided by the
    * device with extra information.  Not supported by all devices.
    * See the method 'hasPageAutoCRC()'.
    * See the method 'hasExtraInfo()' for a description of the optional
    * extra information.
    *
    * @param  page          page number to read
    * @param  readContinue  if 'true' then device read is continued without
    *                       re-selecting.  This can only be used if the new
    *                       readPagePacket() continious where the last one
    *                       stopped and it is inside a
    *                       'beginExclusive/endExclusive' block.
    * @param  readBuf       byte array to put data read. Must have at least
    *                       'getMaxPacketDataLength()' elements.
    * @param  offset        offset into readBuf to place data
    * @param  extraInfo     byte array to put extra info read into
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void readPageCRC (int page, boolean readContinue, byte[] readBuf,
                            int offset, byte[] extraInfo)
      throws OneWireIOException, OneWireException
   {
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         Debug.debug("-----------------------------------------------------------");
         Debug.debug("MemoryBankScratchSHAEE.readPageCRC(int, boolean, byte[], int, byte[]) called");
         Debug.debug("  romID=" + owc33.getAddressAsString());
         Debug.debug("  page=" + page);
         Debug.debug("  readContinue=" + readContinue);
         Debug.debug("  offset=" + offset);
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

      // only needs to be implemented if supported by hardware
      if (!pageAutoCRC)
         throw new OneWireException(
            "Read page with CRC not supported in this memory bank");

      // attempt to put device at max desired speed
      if (!readContinue)
         checkSpeed();

      // check if read exceeds memory
      if (page > numberPages)
         throw new OneWireException("Read exceeds memory bank end");

      // read the scratchpad
      readScratchpad(readBuf, offset, pageLength, extraInfo);

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
         Debug.debug("-----------------------------------------------------------");
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
   }

   //--------
   //-------- ScratchPad methods
   //--------

   /**
    * Read the scratchpad page of memory from a NVRAM device
    * This method reads and returns the entire scratchpad after the byte
    * offset regardless of the actual ending offset
    *
    * @param  readBuf       byte array to place read data into
    *                       length of array is always pageLength.
    * @param  offset        offset into readBuf to pug data
    * @param  len           length in bytes to read
    * @param  extraInfo     byte array to put extra info read into
    *                       (TA1, TA2, e/s byte)
    *                       length of array is always extraInfoLength.
    *                       Can be 'null' if extra info is not needed.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void readScratchpad (byte[] readBuf, int offset, int len,
                               byte[] extraInfo)
      throws OneWireIOException, OneWireException
   {
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         Debug.debug("-----------------------------------------------------------");
         Debug.debug("MemoryBankScratchSHAEE.readScratchpad(byte[], int, int, byte[]) called");
         Debug.debug("  romID=" + owc33.getAddressAsString());
         Debug.debug("  offset=" + offset);
         Debug.debug("  len=" + len);
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      synchronized(read_scratchpad_buffer)
      {
         int num_crc = 0;

         checkSpeed();

         // select the device
         if (!ib.adapter.select(ib.address))
         {
            forceVerify();

            throw new OneWireIOException("Device select failed");
         }

         // build block
         read_scratchpad_buffer[0] = READ_SCRATCHPAD_COMMAND;

         System.arraycopy(ffBlock, 0,
            read_scratchpad_buffer, 1, read_scratchpad_buffer.length - 1);

         // send block, command + (extra) + page data + CRC
         ib.adapter.dataBlock(read_scratchpad_buffer, 0, read_scratchpad_buffer.length);

         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
         if(DEBUG)
            Debug.debug("read_scratchpad_buffer", read_scratchpad_buffer);
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

         // get the starting offset to see when the crc will show up
         int addr = read_scratchpad_buffer[1];

         addr = (addr | ((read_scratchpad_buffer[2] << 8) & 0xFF00)) & 0xFFFF;

         num_crc = pageLength + 3 + extraInfoLength;

         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
         if(DEBUG)
            Debug.debug("num_crc=" + num_crc);
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

         // check crc of entire block
         if(len == pageLength)
         {
            if (CRC16.compute(read_scratchpad_buffer, 0, num_crc, 0) != 0x0000B001)
            {
               //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
               if(DEBUG)
                  Debug.debug("CRC16 Failed");
               //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
               forceVerify();

               throw new OneWireIOException("Invalid CRC16 read from device");
            }
         }

         // optionally extract the extra info
         if (extraInfo != null)
            System.arraycopy(read_scratchpad_buffer, 1,
               extraInfo, 0, extraInfoLength);

         // extract the page data
         System.arraycopy(read_scratchpad_buffer, extraInfoLength + 1,
            readBuf, offset, len);
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
         Debug.debug("-----------------------------------------------------------");
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
   }

   /**
    * Copy the scratchpad page to memory.
    *
    * @param  addr the address to copy the data to
    * @param  len length byte is ignored, must always be 8.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void copyScratchpad (int addr, int len)
      throws OneWireIOException, OneWireException
   {
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         Debug.debug("-----------------------------------------------------------");
         Debug.debug("MemoryBankScratchSHAEE.copyScratchpad(int, int) called");
         Debug.debug("  romID=" + owc33.getAddressAsString());
         Debug.debug("  addr=0x" + Convert.toHexString((byte)addr));
         Debug.debug("  len=" + len);
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      synchronized(scratchpad_buffer)
      {
         readScratchpad(scratchpad_buffer, 0, 8, null);
         copyScratchpad(addr, scratchpad_buffer, 0);
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
         Debug.debug("-----------------------------------------------------------");
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
   }

   /**
    * Copy the scratchpad page to memory.
    *
    * @param  addr the address to copy to
    * @param  scratchpad the scratchpad contents that will be copied
    * @param  offset the offset into scratchpad byte[] where scratchpad data begins
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void copyScratchpad (int addr, byte[] scratchpad, int offset)
      throws OneWireIOException, OneWireException
   {
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         Debug.debug("-----------------------------------------------------------");
         Debug.debug("MemoryBankScratchSHAEE.copyScratchpad(int, byte[], int) called");
         Debug.debug("  romID="+owc33.getAddressAsString());
         Debug.debug("  addr=0x" + Convert.toHexString((byte)addr));
         Debug.debug("  scratchpad", scratchpad, offset, 8);
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      synchronized(page_data_buffer)
      {
         readMemory(addr&0xE0, false, page_data_buffer, 0, 32);

         // readMemory clears the TA address set by write scratchpad, let's re-write it
         writeScratchpad(addr, scratchpad, offset, 8);

         copyScratchpad(addr, scratchpad, offset, page_data_buffer, 0);
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
         Debug.debug("-----------------------------------------------------------");
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
   }

   /**
    * Copy the scratchpad page to memory.
    *
    * @param  addr the address to copy to
    * @param  scratchpad the scratchpad contents that will be copied
    * @param  scratchpadOffset the offset into scratchpad byte[] where scratchpad data begins
    * @param  pageData the data on the page of memory to be written to
    * @param  pageDataOffset the offset into pageData byte[] where pageData begins
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void copyScratchpad (int addr,
                               byte[] scratchpad, int scratchpadOffset,
                               byte[] pageData, int pageDataOffset)
      throws OneWireIOException, OneWireException
   {
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         Debug.debug("-----------------------------------------------------------");
         Debug.debug("MemoryBankScratchSHAEE.copyScratchpad(int, byte[], int, byte[], int) called");
         Debug.debug("  romID=" + owc33.getAddressAsString());
         Debug.debug("  addr=0x" + Convert.toHexString((byte)addr));
         Debug.debug("  scratchpad", scratchpad, scratchpadOffset, 8);
         Debug.debug("  pageData", pageData, pageDataOffset, 32);
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      synchronized(MT_buffer)
      {
         // copy the secret into message block
         owc33.getContainerSecret(MT_buffer, 0);
         System.arraycopy(MT_buffer,4,MT_buffer,48,4);

         // copy the current page contents into the buffer
         System.arraycopy(pageData,pageDataOffset,MT_buffer,4,28);

         System.arraycopy(scratchpad,scratchpadOffset,MT_buffer,32,8);

         MT_buffer[40] = ( byte ) ((addr & 0x0E0) >>> 5);
         System.arraycopy(owc33.getAddress(),0,MT_buffer,41,7);
         System.arraycopy(ffBlock,0,MT_buffer,52,3);

         // put in the padding
         MT_buffer[55] = ( byte ) 0x80;
         System.arraycopy(zeroBlock,0,MT_buffer,56,6);
         MT_buffer[62] = ( byte ) 0x01;
         MT_buffer[63] = ( byte ) 0xB8;

         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
         if(DEBUG)
         {
            Debug.debug("MT_buffer", MT_buffer);
         }
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

         synchronized(MAC_buffer)
         {
            // do the SHA calculation to get MAC
            SHA.ComputeSHA(MT_buffer, MAC_buffer, 0);
            copyScratchpadWithMAC(addr, MAC_buffer, 0);
          }
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
         Debug.debug("-----------------------------------------------------------");
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
   }


   /**
    * Copy all 8 bytes of the Sratch Pad to a certain address in memory
    * using the provided authorization MAC
    *
    * @param addr the address to copy the data to
    * @param authMAC byte[] containing write authorization MAC
    * @param authOffset offset into authMAC where authorization MAC begins
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void copyScratchpadWithMAC(int addr, byte[] authMAC, int authOffset)
      throws OneWireIOException, OneWireException
   {
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         Debug.debug("-----------------------------------------------------------");
         Debug.debug("MemoryBankScratchSHAEE.copyScratchpadWithMAC(int, byte[], int) called");
         Debug.debug("  romID=" + owc33.getAddressAsString());
         Debug.debug("  addr=0x" + Convert.toHexString((byte)addr));
         Debug.debug("  authMAC", authMAC, authOffset, 20);
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      synchronized(copy_scratchpad_buffer)
      {
         byte[] send_block = copy_scratchpad_buffer;

         checkSpeed();

         // access the device
         if (ib.adapter.select(ib.getAddress()))
         {
            // ending address with data status
            send_block[3] = 0x5F;//ES - always 0x5F

            // address 2
            send_block[2] = (byte)((addr>>8) & 0x0FF);//TA2

            // address 1
            send_block[1] = (byte)((addr) & 0x0FF);//TA1;

            // Copy command
            send_block[0] = COPY_SCRATCHPAD_COMMAND;

            // send copy scratchpad command
            ib.adapter.dataBlock(send_block, 0, 4);

            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            if(DEBUG)
               Debug.debug("  send_block", send_block, 0, 4);
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

            // pause before sending appropriate MAC
            try
            {
               Thread.sleep(2);
            }
            catch (InterruptedException e){}

            // sending MAC
            ib.adapter.dataBlock(authMAC, authOffset, 19);

            // provide strong pull-up for copy
            ib.adapter.setPowerDuration(DSPortAdapter.DELIVERY_INFINITE);
            ib.adapter.startPowerDelivery(DSPortAdapter.CONDITION_AFTER_BYTE);
            ib.adapter.putByte(authMAC[authOffset + 19]);

            // pause before checking result
            try
            {
               Thread.sleep(12);
            }
            catch (InterruptedException e){}

            ib.adapter.setPowerNormal();

            // get result
            byte test = ( byte ) ib.adapter.getByte();

            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            if(DEBUG)
               Debug.debug("  result=0x" + Convert.toHexString((byte)test));
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

            if((test != ( byte ) 0xAA) && (test != ( byte ) 0x55))
            {
               if(test == ( byte ) 0xFF)
                  throw new OneWireException("That area of memory is write-protected.");
               else if(test == ( byte )0x00)
                  throw new OneWireIOException("Error due to not matching MAC.");
            }
         }
         else
            throw new OneWireIOException("Device select failed.");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
         Debug.debug("-----------------------------------------------------------");
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
   }

   /**
    * Write to the scratchpad page of memory a NVRAM device.
    *
    * @param  addr physical address to copy data to
    * @param  writeBuf byte array containing data to write
    * @param  offset offset into readBuf to place data
    * @param  len length in bytes to write
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void writeScratchpad (int addr, byte[] writeBuf, int offset,
                                int len)
      throws OneWireIOException, OneWireException
   {
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         Debug.debug("-----------------------------------------------------------");
         Debug.debug("MemoryBankScratchSHAEE.writeScratchpad(int, byte[], int, int) called");
         Debug.debug("  romID="+owc33.getAddressAsString());
         Debug.debug("  addr=0x" + Convert.toHexString((byte)addr));
         Debug.debug("  writeBuf" , writeBuf, offset, len);
         Debug.stackTrace();
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

      checkSpeed();

      super.writeScratchpad(addr, writeBuf, offset, len);

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         Debug.debug("MemoryBankScratchSHAEE.writeScratchpad(int, byte[], int, int) finished");
         Debug.debug("-----------------------------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
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
    * @param  addr          the address to write to
    * @param  writeBuf      byte array containing data to write
    * @param  offset        offset into writeBuf to get data
    * @param  len           length in bytes to write
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void write (int addr, byte[] writeBuf, int offset, int len)
      throws OneWireIOException, OneWireException
   {
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         Debug.debug("-----------------------------------------------------------");
         Debug.debug("MemoryBankScratchSHAEE.write(int, byte[], int, int) called");
         Debug.debug("  romID="+owc33.getAddressAsString());
         Debug.debug("  addr=0x" + Convert.toHexString((byte)addr));
         Debug.debug("  writeBuf", writeBuf, offset, len);
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

     writeScratchpad(addr, writeBuf, offset, len);

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
         Debug.debug("-----------------------------------------------------------");
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
   }

   /**
    *  Load First Secret for the DS2432.  Loads the specified data
    *  to the specified location.  If the address is data memory
    *  (instead of secret memory), this command must have been preceded by
    *  a Refresh Scratchpad command for it to be successful.
    *
    * @param addr the address to write the data to
    * @param data the data to 'load' with the Load First Secret command
    * @param offset the offset to use for reading the data byte[]
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void loadFirstSecret(int addr, byte[] data, int offset)
      throws OneWireIOException, OneWireException
   {
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         Debug.debug("-----------------------------------------------------------");
         Debug.debug("MemoryBankScratchSHAEE.loadFirstSecret(int, byte[], int) called");
         Debug.debug("  romID="+owc33.getAddressAsString());
         Debug.debug("  addr=0x" + Convert.toHexString(addr));
         Debug.debug("  data", data, offset, 8);
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

      writeScratchpad(addr, data, offset, 8);
      loadFirstSecret(addr);

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
         Debug.debug("-----------------------------------------------------------");
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
   }



   /**
    *  Load First Secret for the DS2432.  Loads current contents of the
    *  scratchpad to the specified location.  If the address is data memory
    *  (instead of secret memory), this command must have been preceded by
    *  a Refresh Scratchpad command for it to be successful.
    *
    * @param addr the address to write the data to
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void loadFirstSecret(int addr)
      throws OneWireIOException, OneWireException
   {
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         Debug.debug("-----------------------------------------------------------");
         Debug.debug("MemoryBankScratchSHAEE.loadFirstSecret(int) called");
         Debug.debug("  romID="+owc33.getAddressAsString());
         Debug.debug("  addr=0x" + Convert.toHexString((byte)addr));
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

      byte[] send_block = new byte[4];

      checkSpeed();

      // access the device
      if (ib.adapter.select(ib.getAddress()))
      {
         send_block [0] = LOAD_FIRST_SECRET;
         send_block [1] = ( byte ) (addr & 0x00FF);
         send_block [2] = ( byte ) ((addr >>> 8) & 0x00FF);
         send_block [3] = ( byte ) 0x5F;// Should be 0x5F,not ( byte ) ((addr + 7) & 0x01F);

         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
         if(DEBUG)
            Debug.debug("send_block", send_block, 0, 4);
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

          // now send the block
         ib.adapter.dataBlock(send_block,0,3);

         // provide strong pull-up for load
         ib.adapter.setPowerDuration(DSPortAdapter.DELIVERY_INFINITE);
         ib.adapter.startPowerDelivery(DSPortAdapter.CONDITION_AFTER_BYTE);
         ib.adapter.putByte(send_block[3]);

         try
         {
            Thread.sleep(20);
         }
         catch (InterruptedException e){}

         ib.adapter.setPowerNormal();

         byte test = ( byte ) ib.adapter.getByte();

         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
         if(DEBUG)
            Debug.debug("result=" + Convert.toHexString(test));
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

         if((test != (byte) 0xAA) && (test != (byte) 0x55))
            throw new OneWireException("Error due to invalid load.");

         // if data is loaded to secrets memory, lets read it so we can
         // set the container secret
         if(addr==0x080)
         {
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
            if(DEBUG)
               Debug.debug("reading scratchpad and setting container secret");
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

            byte[] secret = new byte[8];
            readScratchpad(secret, 0, 8, null);
            owc33.setContainerSecret(secret, 0);
         }
      }
      else
         throw new OneWireIOException("Device select failed.");
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
         Debug.debug("-----------------------------------------------------------");
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
   }

   /**
    * Computes the next secret.
    *
    * @param addr the physical address of the page to use for secret computation
    * @param partialsecret byte array containing next partial secret for writing to the scratchpad
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void computeNextSecret(int addr)
      throws OneWireIOException, OneWireException
   {
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         Debug.debug("-----------------------------------------------------------");
         Debug.debug("MemoryBankScratchSHAEE.computeNextSecret(int) called");
         Debug.debug("  romID="+owc33.getAddressAsString());
         Debug.debug("  addr=0x" + Convert.toHexString((byte)addr));
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

      byte[] send_block = new byte [3];
      byte[] scratch = new byte [8];
      byte[] next_secret = null;

      // check to see if secret is set
      if(owc33.isContainerSecretSet())
      {
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
         if(DEBUG)
            Debug.debug("Calculating next secret for container");
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

         byte[] memory     = new byte [32];
         byte[] secret     = new byte [8];
         byte[] MT         = new byte [64];

         readMemory(addr&0xE0, false, memory, 0, 32);

         owc33.getContainerSecret(secret, 0);

         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
         if(DEBUG)
            Debug.debug("currentSecret", secret, 0, 8);
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

         System.arraycopy(secret,0,MT,0,4);
         System.arraycopy(memory,0,MT,4,32);
         System.arraycopy(ffBlock,0,MT,36,4);
         readScratchpad(MT, 40, 8, null);
         MT[40] = ( byte ) (MT[40] & ( byte ) 0x3F);
         System.arraycopy(secret,4,MT,48,4);
         System.arraycopy(ffBlock,0,MT,52,3);

         // message padding
         MT[55] = ( byte ) 0x80;
         System.arraycopy(zeroBlock,0,MT,56,6);
         MT[62] = ( byte ) 0x01;
         MT[63] = ( byte ) 0xB8;

         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
         if(DEBUG)
            Debug.debug("MT", MT, 0, 64);
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

         int[] AtoE = new int [5];
         SHA.ComputeSHA(MT,AtoE);

         //copy E into secret
         for(int temp=AtoE[4],i=0;i<4;i++)
         {
            secret[i] = (byte)(temp & 0x0FF);
            temp >>= 8;
         }
         //copy D into secret
         for(int temp=AtoE[3],i=4;i<8;i++)
         {
            secret[i] = (byte)(temp & 0x0FF);
            temp >>= 8;
         }
         next_secret = secret;

         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
         if(DEBUG)
            Debug.debug("nextSecret", secret, 0, 8);
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      }

      checkSpeed();

      // access the device
      if (ib.adapter.select(ib.getAddress()))
      {
         // Next Secret command
         send_block[0] = COMPUTE_NEXT_SECRET;
         // address 1
         send_block[1] = ( byte ) (addr & 0xFF);
         // address 2
         send_block[2] = ( byte ) (((addr & 0xFFFF) >>> 8) & 0xFF);

         // now send the block
         ib.adapter.dataBlock(send_block,0,2);

         // provide strong pull-up for compute next secret
         ib.adapter.setPowerDuration(DSPortAdapter.DELIVERY_INFINITE);
         ib.adapter.startPowerDelivery(DSPortAdapter.CONDITION_AFTER_BYTE);
         ib.adapter.putByte(send_block[2]);

         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
         if(DEBUG)
            Debug.debug("sendblock ", send_block, 0, 3);
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

         try
         {
            Thread.sleep(14);
         }
         catch (InterruptedException e){}

         ib.adapter.setPowerNormal();

         readScratchpad(scratch,0,8,null);
         for(int i=0;i<8;i++)
         {
            if(scratch[i] != ( byte ) 0xAA)
            {
               throw new OneWireIOException("Next secret not calculated.");
            }
         }
         if(next_secret!=null)
         {
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            if(DEBUG)
               Debug.debug("setting container secret", next_secret, 0, 8);
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            owc33.setContainerSecret(next_secret, 0);
         }
      }
      else
         throw new OneWireIOException("Device select failed.");

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
         Debug.debug("-----------------------------------------------------------");
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
   }

   /**
    * Computes the next secret.
    *
    * @param addr the physical address of the page to use for secret computation
    * @param partialsecret byte array containing next partial secret for writing to the scratchpad
    * @param offset into partialsecret byte array to start reading
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void computeNextSecret(int addr, byte[] partialsecret, int offset)
      throws OneWireIOException, OneWireException
   {
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         Debug.debug("-----------------------------------------------------------");
         Debug.debug("MemoryBankScratchSHAEE.computeNextSecret(int, byte[], int) called");
         Debug.debug("  romID=" + owc33.getAddressAsString());
         Debug.debug("  addr=0x" + Convert.toHexString((byte)addr));
         Debug.debug("  partialsecret", partialsecret, offset, 8);
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

      byte[] send_block = new byte [3];
      byte[] scratch = new byte [8];
      byte[] next_secret = null;

      writeScratchpad(addr, partialsecret, 0, 8);

      // check to see if secret is set
      if(owc33.isContainerSecretSet())
      {
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
         if(DEBUG)
            Debug.debug("Calculating next secret for container");
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

         byte[] memory     = new byte [32];
         byte[] secret     = new byte [8];
         byte[] MT         = new byte [64];

         readMemory(addr&0xE0, false, memory, 0, 32);

         owc33.getContainerSecret(secret, 0);

         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
         if(DEBUG)
            Debug.debug("currentSecret", secret, 0, 8);
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

         System.arraycopy(secret,0,MT,0,4);
         System.arraycopy(memory,0,MT,4,32);
         System.arraycopy(ffBlock,0,MT,36,4);
         MT[40] = ( byte ) (partialsecret[0] & ( byte ) 0x3F);
         System.arraycopy(partialsecret,1,MT,41,7);
         System.arraycopy(secret,4,MT,48,4);
         System.arraycopy(ffBlock,0,MT,52,3);

         // message padding
         MT[55] = ( byte ) 0x80;
         System.arraycopy(zeroBlock,0,MT,56,6);
         MT[62] = ( byte ) 0x01;
         MT[63] = ( byte ) 0xB8;

         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
         if(DEBUG)
            Debug.debug("MT", MT, 0, 64);
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

         int[] AtoE = new int [5];
         SHA.ComputeSHA(MT,AtoE);

         //copy E into secret
         for(int temp=AtoE[4],i=0;i<4;i++)
         {
            secret[i] = (byte)(temp & 0x0FF);
            temp >>= 8;
         }
         //copy D into secret
         for(int temp=AtoE[3],i=4;i<8;i++)
         {
            secret[i] = (byte)(temp & 0x0FF);
            temp >>= 8;
         }
         next_secret = secret;

         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
         if(DEBUG)
            Debug.debug("nextSecret=", secret, 0, 8);
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      }

      // access the device
      if (ib.adapter.select(ib.getAddress()))
      {
         // Next Secret command
         send_block[0] = COMPUTE_NEXT_SECRET;
         // address 1
         send_block[1] = ( byte ) (addr & 0xFF);
         // address 2
         send_block[2] = ( byte ) (((addr & 0xFFFF) >>> 8) & 0xFF);

         // now send the block
         ib.adapter.dataBlock(send_block,0,2);

         // provide strong pull-up for compute next secret
         ib.adapter.setPowerDuration(DSPortAdapter.DELIVERY_INFINITE);
         ib.adapter.startPowerDelivery(DSPortAdapter.CONDITION_AFTER_BYTE);
         ib.adapter.putByte(send_block[2]);

         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
         if(DEBUG)
            Debug.debug("sendblock ", send_block, 0, 3);
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

         try
         {
            Thread.sleep(14);
         }
         catch (InterruptedException e){}

         ib.adapter.setPowerNormal();

         readScratchpad(scratch,0,8,null);
         for(int i=0;i<8;i++)
         {
            if(scratch[i] != ( byte ) 0xAA)
            {
               throw new OneWireIOException("Next secret not calculated.");
            }
         }

         if(next_secret!=null)
         {
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            if(DEBUG)
               Debug.debug("setting container secret", next_secret, 0, 8);
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            owc33.setContainerSecret(next_secret, 0);
         }
      }
      else
         throw new OneWireIOException("Device select failed.");

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
         Debug.debug("-----------------------------------------------------------");
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
   }


   /**
    * Refreshes the scratchpad for DS1961S.  Command has no effect on DS2432
    * devices.  After this command is executed, the data at the address
    * specified will be loaded into the scratchpad.  The Load First Secret
    * command can then be used to re-write the data back to the page, correcting
    * any weakly-programmed EEPROM bits.
    *
    * @param addr the address to load the data from into the scratchpad
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void refreshScratchpad(int addr)
      throws OneWireIOException, OneWireException
   {
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         Debug.debug("-----------------------------------------------------------");
         Debug.debug("MemoryBankScratchSHAEE.refreshScratchpad(int) called");
         Debug.debug("  romID=" + owc33.getAddressAsString());
         Debug.debug("  addr=0x" + Convert.toHexString((byte)addr));
         Debug.stackTrace();
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

      checkSpeed();

      // access the device
      if (ib.adapter.select(ib.getAddress()))
      {
         byte[] send_block = new byte[13];

         send_block [0] = REFRESH_SCRATCHPAD;
         send_block [1] = ( byte ) (addr & 0x00FF);
         send_block [2] = ( byte ) ((addr >>> 8) & 0x00FF);
         for(int i=3; i<11; i++)
            send_block[i] = (byte)0x00;
         send_block[11] = (byte)0xFF;
         send_block[12] = (byte)0xFF;

          // now send the block
         ib.adapter.dataBlock(send_block, 0, 13);

         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
         if(DEBUG)
            Debug.debug("send_block", send_block, 0, 13);
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

         if(CRC16.compute(send_block, 0, 13, 0) != 0x0B001)
         {
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            if(DEBUG)
               Debug.debug("   Refresh Scratchpad failed because of bad CRC16");
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
            throw new OneWireException("Bad CRC16 on Refresh Scratchpad");
         }
      }
      else
      {
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
         if(DEBUG)
            Debug.debug("   Refresh Scratchpad failed because there is no device");
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
         throw new OneWireIOException("Device select failed.");
      }

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
         Debug.debug("-----------------------------------------------------------");
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
   }

   /**
    * Reads actual memory (not scratchpad memory) with no CRC checking (device or
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
   private void readMemory (int startAddr, boolean readContinue, byte[] readBuf,
                            int offset, int len)
      throws OneWireIOException, OneWireException
   {
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         Debug.debug("-----------------------------------------------------------");
         Debug.debug("MemoryBankScratchSHAEE.readMemory(int, boolean, byte[], int, int) called");
         Debug.debug("  romID=" + owc33.getAddressAsString());
         Debug.debug("  startAddr=0x" + Convert.toHexString((byte)startAddr));
         Debug.debug("  readContinue=" + readContinue);
         Debug.debug("  offset=" + offset);
         Debug.debug("  len=" + len);
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

      // attempt to put device at max desired speed
      if (!readContinue)
         checkSpeed();

      // see if need to access the device
      if (!readContinue)
      {
         // select the device
         if (!ib.adapter.select(ib.getAddress()))
            throw new OneWireIOException("Device select failed.");

         // build start reading memory block
         readBuf [offset] = ( byte ) 0xF0; // READ MEMORY, no CRC, no MAC
         readBuf [offset+1] = ( byte ) (startAddr & 0xFF);
         readBuf [offset+2] = ( byte ) (((startAddr & 0xFFFF) >>> 8) & 0xFF);

         // do the first block for command, address
         ib.adapter.dataBlock(readBuf, offset, 3);
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
         if(DEBUG)
         {
            Debug.debug("  readBuf", readBuf, offset, 3);
         }
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      }

      // pre-fill readBuf with 0xFF
      int pgs   = len / 32;
      int extra = len % 32;

      for (int i = 0; i < pgs; i++)
         System.arraycopy(ffBlock, 0, readBuf, offset + i * 32,
                          32);
      if(extra>0)
         System.arraycopy(ffBlock, 0, readBuf, offset + pgs * 32, extra);

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      // send second block to read data, return result
      ib.adapter.dataBlock(readBuf, offset, len);

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         Debug.debug("  readBuf", readBuf, offset, len);
         Debug.debug("-----------------------------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
   }

   /**
    * helper method to pause for specified milliseconds
    */
   private static final void msWait(final long ms)
   {
      try
      {
         Thread.sleep(ms);
      }
      catch(InterruptedException ie)
      {;}
   }
}
