
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

package com.dalsemi.onewire.utils;

/**
 * Utilities for bit operations on an array.
 *
 * @version    0.00, 27 August 2000
 * @author     DS
 */
public class Bit
{

   /**
    * Write the bit state in a byte array.
    *
    * @param state new state of the bit 1, 0
    * @param index bit index into byte array
    * @param offset byte offset into byte array to start
    * @param buf byte array to manipulate
    */
   public static void arrayWriteBit (int state, int index, int offset,
                                     byte[] buf)
   {
      int nbyt = (index >>> 3);
      int nbit = index - (nbyt << 3);

      if (state == 1)
         buf [nbyt + offset] |= (0x01 << nbit);
      else
         buf [nbyt + offset] &= ~(0x01 << nbit);
   }

   /**
    * Read a bit state in a byte array.
    *
    * @param index bit index into byte array
    * @param offset byte offset into byte array to start
    * @param buf byte array to read from
    *
    * @return bit state 1 or 0
    */
   public static int arrayReadBit (int index, int offset, byte[] buf)
   {
      int nbyt = (index >>> 3);
      int nbit = index - (nbyt << 3);

      return ((buf [nbyt + offset] >>> nbit) & 0x01);
   }
}
