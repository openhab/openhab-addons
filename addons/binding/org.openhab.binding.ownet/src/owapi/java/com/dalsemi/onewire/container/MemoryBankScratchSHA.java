
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
import com.dalsemi.onewire.utils.CRC16;
import com.dalsemi.onewire.container.OneWireContainer;


/**
 * Memory bank class for the Scratchpad section of SHA iButtons and
 * 1-Wire devices.
 *
 *  @version    0.00, 28 Aug 2000
 *  @author     DS
 */
class MemoryBankScratchSHA
   extends MemoryBankScratchCRC
{

   //--------
   //-------- Static Final Variables
   //--------

   /**
    * Erase Scratchpad Command
    */
   public static final byte ERASE_SCRATCHPAD_COMMAND = ( byte ) 0xC3;

   //--------
   //-------- Constructor
   //--------

   /**
    * Memory bank contstuctor.  Requires reference to the OneWireContainer
    * this memory bank resides on.
    */
   public MemoryBankScratchSHA (OneWireContainer ibutton)
   {
      super(ibutton);

      // initialize attributes of this memory bank - DEFAULT: DS1963S scratchapd
      bankDescription = "Scratchpad with CRC and auto-hide";
   }

   //--------
   //-------- ScratchPad methods
   //--------

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
      boolean calcCRC = false;
      byte[]  raw_buf = new byte [37];

      // select the device
      if (!ib.adapter.select(ib.address))
      {
         forceVerify();

         throw new OneWireIOException("Device select failed");
      }

      // erase the scratchpad
      raw_buf [0] = ERASE_SCRATCHPAD_COMMAND;

      System.arraycopy(ffBlock, 0, raw_buf, 1, 8);
      ib.adapter.dataBlock(raw_buf, 0, 9);

      if ((( byte ) (raw_buf [8] & 0x0F0) != ( byte ) 0xA0)
              && (( byte ) (raw_buf [8] & 0x0F0) != ( byte ) 0x50))
      {
         forceVerify();

         throw new OneWireIOException("Erase scratchpad complete not found");
      }

      // select the device
      if (!ib.adapter.select(ib.address))
      {
         forceVerify();

         throw new OneWireIOException("Device select failed");
      }

      // build block to send
      raw_buf [0] = WRITE_SCRATCHPAD_COMMAND;
      raw_buf [1] = ( byte ) (startAddr & 0xFF);
      raw_buf [2] = ( byte ) (((startAddr & 0xFFFF) >>> 8) & 0xFF);

      System.arraycopy(writeBuf, offset, raw_buf, 3, len);

      // check if full page (can utilize CRC)
      if (((startAddr + len) % pageLength) == 0)
      {
         System.arraycopy(ffBlock, 0, raw_buf, len + 3, 2);

         calcCRC = true;
      }

      // send block, return result 
      ib.adapter.dataBlock(raw_buf, 0, len + 3 + ((calcCRC) ? 2
                                                            : 0));

      // check crc
      if (calcCRC)
      {
         if (CRC16.compute(raw_buf, 0, len + 5, 0) != 0x0000B001)
         {
            forceVerify();

            throw new OneWireIOException("Invalid CRC16 read from device");
         }
      }
   }
}
