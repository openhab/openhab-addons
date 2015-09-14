
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


/**
 *  Scratchpad interface for Memory banks that require it.
 *
 *  @version    0.00, 28 Aug 2000
 *  @author     DS
 */
interface ScratchPad
{

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
      throws OneWireIOException, OneWireException;

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
      throws OneWireIOException, OneWireException;

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
      throws OneWireIOException, OneWireException;

   /**
    * Query to get the length in bytes of extra information that
    * is read when read a page in the current memory bank.  See
    * 'hasExtraInfo()'.
    *
    * @return  number of bytes in Extra Information read when reading
    *          pages in the current memory bank.
    */
   public int getExtraInfoLength ();

   /**
    * Check the device speed if has not been done before or if
    * an error was detected.
    *
    * @throws OneWireIOException
    * @throws OneWireException
    */
   public void checkSpeed ()
      throws OneWireIOException, OneWireException;

   /**
    * Set the flag to indicate the next 'checkSpeed()' will force
    * a speed set and verify.
    */
   public void forceVerify ();
}
