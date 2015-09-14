/*---------------------------------------------------------------------------
 * Copyright (C) 2002 Dallas Semiconductor Corporation, All Rights Reserved.
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
import com.dalsemi.onewire.utils.CRC16;
import com.dalsemi.onewire.container.OneWireContainer;


/**
 * Memory bank class for the Scratchpad section of NVRAM iButtons and
 * 1-Wire devices with password protected memory pages.
 *
 *  @version    1.00, 11 Aug 2002
 *  @author     SH
 */
public class MemoryBankScratchCRCPW
   extends MemoryBankScratchEx
{

   /**
    * The Password container to acces the 8 byte passwords
    */
   protected PasswordContainer ibPass = null;

   /**
    * Enable Provided Power for some Password checking.
    */
   public boolean enablePower = false;

   //--------
   //-------- Constructor
   //--------

   /**
    * Memory bank contstuctor.  Requires reference to the OneWireContainer
    * this memory bank resides on.
    */
   public MemoryBankScratchCRCPW (PasswordContainer ibutton)
   {
      super((OneWireContainer)ibutton);

      ibPass = ibutton;

      // initialize attributes of this memory bank - DEFAULT: DS1963L scratchapd
      bankDescription = "Scratchpad with CRC and Password";
      pageAutoCRC     = true;

      // default copy scratchpad command (from DS1922)
      COPY_SCRATCHPAD_COMMAND = ( byte ) 0x99;
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
      byte[] extraInfo = new byte [extraInfoLength];

      readPageCRC(page, readContinue, readBuf, offset, extraInfo);
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
      int blockLength = 0;
      int num_crc = 0;

      // select the device
      if (!ib.adapter.select(ib.address))
      {
         forceVerify();

         throw new OneWireIOException("Device select failed");
      }

      // build block
      if(enablePower)
      {
         if(len == pageLength)
            blockLength = extraInfoLength + pageLength + 3;
         else
            blockLength = len + extraInfoLength + 1;
      }
      else
         blockLength = extraInfoLength + pageLength + 3;

      byte[] raw_buf = new byte [blockLength];

      raw_buf [0] = READ_SCRATCHPAD_COMMAND;

      System.arraycopy(ffBlock, 0, raw_buf, 1, raw_buf.length - 1);

      // send block, command + (extra) + page data + CRC
      ib.adapter.dataBlock(raw_buf, 0, raw_buf.length);

      // get the starting offset to see when the crc will show up
      int addr = raw_buf [1];

      addr = (addr | ((raw_buf [2] << 8) & 0xFF00)) & 0xFFFF;

      if(enablePower && (len == 64))
         num_crc = pageLength + 3 - (addr & 0x003F) + extraInfoLength;
      else if(!enablePower)
         num_crc = pageLength + 3 - (addr & 0x001F) + extraInfoLength;

      // check crc of entire block
      if(len == pageLength)
      {
         if (CRC16.compute(raw_buf, 0, num_crc, 0) != 0x0000B001)
         {
            forceVerify();
            throw new OneWireIOException("Invalid CRC16 read from device");
         }
      }

      // optionally extract the extra info
      if (extraInfo != null)
         System.arraycopy(raw_buf, 1, extraInfo, 0, extraInfoLength);

      // extract the page data
      if(!enablePower)
         System.arraycopy(raw_buf, extraInfoLength + 1, readBuf, offset, len);
      else
         System.arraycopy(raw_buf, extraInfoLength + 1, readBuf, offset, len);
   }

   /**
    * Copy the scratchpad page to memory.
    *
    * @param  startAddr     starting address
    * @param  len           length in bytes that was written already
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void copyScratchpad (int startAddr, int len)
      throws OneWireIOException, OneWireException
   {
      if(!enablePower)
      {
         if(((startAddr + len) & 0x1F) != 0)
            throw new OneWireException(
               "CopyScratchpad failed: Ending Offset must go to end of page");
      }

      // select the device
      if (!ib.adapter.select(ib.address))
      {
         forceVerify();
         throw new OneWireIOException("Device select failed");
      }

      // build block to send (1 cmd, 3 data, 8 password, 4 verification)
      int raw_buf_length = 16;
      byte[] raw_buf = new byte [raw_buf_length];

      raw_buf [0] = COPY_SCRATCHPAD_COMMAND;
      raw_buf [1] = ( byte ) (startAddr & 0xFF);
      raw_buf [2] = ( byte ) (((startAddr & 0xFFFF) >>> 8) & 0xFF);
      if(enablePower)
         raw_buf [3] = (byte) ((startAddr + len - 1) & 0x3F);
      else
         raw_buf [3] = ( byte ) ((startAddr + len - 1) & 0x1F);

      if(ibPass.isContainerReadWritePasswordSet())
      {
         ibPass.getContainerReadWritePassword(raw_buf, 4);
      }

      System.arraycopy(ffBlock, 0, raw_buf, raw_buf_length-4, 4);

      // send block (check copy indication complete)
      if(enablePower)
      {
         ib.adapter.dataBlock(raw_buf, 0, (raw_buf_length - 5));

         ib.adapter.startPowerDelivery(DSPortAdapter.CONDITION_AFTER_BYTE);

         ib.adapter.putByte(raw_buf[11]);

         msWait(23);

         ib.adapter.setPowerNormal();

         raw_buf[12] = (byte) ib.adapter.getByte();

         if(((raw_buf[12] & (byte)0xF0) != (byte)0xA0) &&
            ((raw_buf[12] & (byte)0xF0) != (byte)0x50))
            throw new OneWireIOException("Copy scratchpad complete not found");
      }
      else
      {
         ib.adapter.dataBlock(raw_buf, 0, raw_buf_length);

         byte verifyByte = (byte)(raw_buf[raw_buf_length-1] & 0x0F);
         if ( verifyByte!=0x0A && verifyByte!=0x05 )
         {
            //forceVerify();
            if(verifyByte==0x0F)
               throw new OneWireIOException("Copy scratchpad failed - invalid password");
            else
               throw new OneWireIOException("Copy scratchpad complete not found");
         }
      }
   }

   /**
    * Write to the scratchpad page of memory a NVRAM device.
    *
    * @param  startAddr     starting address
    * @param  writeBuf      byte array containing data to write
    * @param  offset        offset into readBuf to place data
    * @param  len           length in bytes to write
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void writeScratchpad (int startAddr, byte[] writeBuf, int offset,
                                int len)
      throws OneWireIOException, OneWireException
   {
      if( (((startAddr + len) & 0x1F) != 0) && (!enablePower) )
         throw new OneWireException(
            "WriteScratchpad failed: Ending Offset must go to end of page");

      super.writeScratchpad(startAddr, writeBuf, offset, len);
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
