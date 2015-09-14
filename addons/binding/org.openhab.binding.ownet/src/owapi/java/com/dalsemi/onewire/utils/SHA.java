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
 * Utility methods for performing SHA calculations.
 */
public class SHA
{

   // SHA constants
   private static final int[] KTN
      = new int[] { 0x5a827999, 0x6ed9eba1, 0x8f1bbcdc, 0xca62c1d6 };

   private static final int H0 = 0x67452301;
   private static final int H1 = 0xEFCDAB89;
   private static final int H2 = 0x98BADCFE;
   private static final int H3 = 0x10325476;
   private static final int H4 = 0xC3D2E1F0;

   // some local variables in the compute SHA function.
   // can't 'static final' methods with no locals be
   // inlined easier?  I think so, but I need to remember
   // to look into that.
   private static int word,i,j;
   private static int ShftTmp, Temp;

   private static final int[] MTword;
   private static final int[] H;
   static //initializer block
   {
      H = new int[5];
      MTword = new int[80];
   }

   private SHA()
   { /* you can't instantiate this class */ }

   /**
    * Does Dallas SHA, as specified in DS1963S datasheet.
    * result is in intel Endian format, starting with the
    * LSB of E to the MSB of E followed by the LSB of D.
    * result array should be at least 20 bytes long, after
    * the offset.
    *
    * @param MT The message block (padded if necessary).
    * @param result The byte[] into which the result will be copied.
    * @param offset The starting location in 'result' to start copying.
    */
   public synchronized static final byte[] ComputeSHA(byte[] MT, byte[] result, int offset)
   {
      ComputeSHA(MT,H);

      //split up the result into a byte array, LSB first
      for(i=0;i<5;i++)
      {
         word = H[4-i];
         j = (i<<2) + offset;
         result[j + 0] = (byte)((word) & 0x00FF);
         result[j + 1] = (byte)((word>>>8) & 0x00FF);
         result[j + 2] = (byte)((word>>>16) & 0x00FF);
         result[j + 3] = (byte)((word>>>24) & 0x00FF);
      }

      return result;
   }


   /**
    * Does Dallas SHA, as specified in DS1963S datasheet.
    * result is in intel Endian format, starting with the
    * LSB of E to the MSB of E followed by the LSB of D.
    *
    * @param MT The message block (padded if necessary).
    * @param ABCDE The result will be copied into this 5-int array.
    */
   public synchronized static final void ComputeSHA(byte[] MT, int[] ABCDE)
   {
      for(i=0;i<16;i++)
         MTword[i] = ((MT[i*4]&0x00FF) << 24) | ((MT[i*4+1]&0x00FF) << 16) |
                     ((MT[i*4+2]&0x00FF) << 8) | (MT[i*4+3]&0x00FF);

      for(i=16;i<80;i++)
      {
         ShftTmp = MTword[i-3] ^ MTword[i-8] ^ MTword[i-14] ^ MTword[i-16];
         MTword[i] = ((ShftTmp << 1) & 0xFFFFFFFE) |
                     ((ShftTmp >>> 31) & 0x00000001);
      }

      ABCDE[0] = H0; //A
      ABCDE[1] = H1; //B
      ABCDE[2] = H2; //C
      ABCDE[3] = H3; //D
      ABCDE[4] = H4; //E

      for(i=0;i<80;i++)
      {
         ShftTmp = ((ABCDE[0] << 5) & 0xFFFFFFE0) | ((ABCDE[0] >>> 27) & 0x0000001F);
         Temp = NLF(ABCDE[1],ABCDE[2],ABCDE[3],i) + ABCDE[4] + KTN[i/20] + MTword[i] + ShftTmp;
         ABCDE[4] = ABCDE[3];
         ABCDE[3] = ABCDE[2];
         ABCDE[2] = ((ABCDE[1] << 30) & 0xC0000000) | ((ABCDE[1] >>> 2) & 0x3FFFFFFF);
         ABCDE[1] = ABCDE[0];
         ABCDE[0] = Temp;
      }
   }

   // calculation used for SHA.
   // static final methods with no locals should definitely be inlined
   // by the compiler.
   private static final int NLF (int B, int C, int D, int n)
   {
      if(n<20)
         return ((B&C)|((~B)&D));
      else if(n<40)
         return (B^C^D);
      else if(n<60)
         return ((B&C)|(B&D)|(C&D));
      else
         return (B^C^D);
   }
}
