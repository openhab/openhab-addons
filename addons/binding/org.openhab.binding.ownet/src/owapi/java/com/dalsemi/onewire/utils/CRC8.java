
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
 * CRC8 is a class to contain an implementation of the
 * Cyclic-Redundency-Check CRC8 for the iButton.  The CRC8 is used
 * in the 1-Wire Network address of all iButtons and 1-Wire
 * devices.
 * <p>
 * CRC8 is based on the polynomial = X^8 + X^5 + X^4 + 1.
 *
 * @version    0.00, 28 Aug 2000
 * @author     DS
 *
 */
public class CRC8
{

   //--------
   //-------- Variables
   //--------

   /**
    * CRC 8 lookup table
    */
   private static byte dscrc_table [];

   /*
    * Create the lookup table
    */
   static
   {

      //Translated from the assembly code in iButton Standards, page 129.
      dscrc_table = new byte [256];

      int acc;
      int crc;

      for (int i = 0; i < 256; i++)
      {
         acc = i;
         crc = 0;

         for (int j = 0; j < 8; j++)
         {
            if (((acc ^ crc) & 0x01) == 0x01)
            {
               crc = ((crc ^ 0x18) >> 1) | 0x80;
            }
            else
               crc = crc >> 1;

            acc = acc >> 1;
         }

         dscrc_table [i] = ( byte ) crc;
      }
   }

   //--------
   //-------- Constructor
   //--------

   /**
    * Private constructor to prevent instantiation.
    */
   private CRC8 ()
   {
   }

   //--------
   //-------- Methods
   //--------

   /**
    * Perform the CRC8 on the data element based on the provided seed.
    * <p>
    * CRC8 is based on the polynomial = X^8 + X^5 + X^4 + 1.
    *
    * @param   dataToCrc   data element on which to perform the CRC8
    * @param   seed        seed the CRC8 with this value
    * @return  CRC8 value
    */
   public static int compute (int dataToCRC, int seed)
   {
      return (dscrc_table [(seed ^ dataToCRC) & 0x0FF] & 0x0FF);
   }

   /**
    * Perform the CRC8 on the data element based on a zero seed.
    * <p>
    * CRC8 is based on the polynomial = X^8 + X^5 + X^4 + 1.
    *
    * @param   dataToCrc   data element on which to perform the CRC8
    * @return  CRC8 value
    */
   public static int compute (int dataToCRC)
   {
      return (dscrc_table [dataToCRC & 0x0FF] & 0x0FF);
   }

   /**
    * Perform the CRC8 on an array of data elements based on a
    * zero seed.
    * <p>
    * CRC8 is based on the polynomial = X^8 + X^5 + X^4 + 1.
    *
    * @param   dataToCrc   array of data elements on which to perform the CRC8
    * @return  CRC8 value
    */
   public static int compute (byte dataToCrc [])
   {
      return compute(dataToCrc, 0, dataToCrc.length);
   }

   /**
    * Perform the CRC8 on an array of data elements based on a
    * zero seed.
    * <p>
    * CRC8 is based on the polynomial = X^8 + X^5 + X^4 + 1.
    *
    * @param   dataToCrc   array of data elements on which to perform the CRC8
    * @param   off         offset into array
    * @param   len         length of data to crc
    * @return  CRC8 value
    */
   public static int compute (byte dataToCrc [], int off, int len)
   {
      return compute(dataToCrc, off, len, 0);
   }

   /**
    * Perform the CRC8 on an array of data elements based on the
    * provided seed.
    * <p>
    * CRC8 is based on the polynomial = X^8 + X^5 + X^4 + 1.
    *
    * @param   dataToCrc   array of data elements on which to perform the CRC8
    * @param   off         offset into array
    * @param   len         length of data to crc
    * @param   seed        seed to use for CRC8
    * @return  CRC8 value
    */
   public static int compute (byte dataToCrc [], int off, int len, int seed)
   {

      // loop to do the crc on each data element
      int CRC8 = seed;

      for (int i = 0; i < len; i++)
         CRC8 = dscrc_table [(CRC8 ^ dataToCrc [i + off]) & 0x0FF];

      return (CRC8 & 0x0FF);
   }

   /**
    * Perform the CRC8 on an array of data elements based on the
    * provided seed.
    * <p>
    * CRC8 is based on the polynomial = X^8 + X^5 + X^4 + 1.
    *
    * @param   dataToCrc   array of data elements on which to perform the CRC8
    * @param   seed        seed to use for CRC8
    * @return  CRC8 value
    */
   public static int compute (byte dataToCrc [], int seed)
   {
      return compute(dataToCrc, 0, dataToCrc.length, seed);
   }
}
