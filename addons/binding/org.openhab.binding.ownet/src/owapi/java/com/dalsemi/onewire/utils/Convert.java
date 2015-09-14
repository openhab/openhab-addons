
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
 * Utilities for conversion between miscellaneous datatypes.
 *
 * @version    1.00, 28 December 2001
 * @author     SH
 */
public class Convert
{
   /** returns hex character for each digit, 0-15 */
   private static final char[] lookup_hex = new char[]
      {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

   /**
    * Inner class for conversion exceptions
    *
    */
   public static class ConvertException extends Exception
   {
      public ConvertException(String message)
      {
         super(message);
      }
      public ConvertException()
      {
         super();
      }
   }
   
   /**
    * Not to be instantiated
    */
   private Convert()
   {;}

   // ----------------------------------------------------------------------
   // Temperature conversions
   // ----------------------------------------------------------------------

   // ??? does this optimization help out on TINI, where double-division is
   // ??? potentially slower?  If not, feel free to delete it.
   // ???
   /** cache the value of five divided by nine, which is irrational */
   private static final double FIVE_NINTHS = (5.0d / 9.0d);

   /**
    * Converts a temperature reading from Celsius to Fahrenheit.
    *
    * @param   celsiusTemperature temperature value in Celsius
    *
    * @return  the Fahrenheit conversion of the supplied temperature
    */
   public static final double toFahrenheit (double celsiusTemperature)
   {
      // (9/5)=1.8
      return celsiusTemperature*1.8d + 32.0d;
   }

   /**
    * Converts a temperature reading from Fahrenheit to Celsius.
    *
    * @param  fahrenheitTemperature temperature value in Fahrenheit
    *
    * @return  the Celsius conversion of the supplied temperature
    */
   public static final double toCelsius (double fahrenheitTemperature)
   {
      return (fahrenheitTemperature - 32.0d)*FIVE_NINTHS;
   }

   // ----------------------------------------------------------------------
   // Long <-> ByteArray conversions
   // ----------------------------------------------------------------------

   /**
    * This method constructs a long from a LSByte byte array of specified length.
    *
    * @param  byteArray byte array to convert to a long (LSByte first)
    * @param  offset byte offset into the array where to start to convert
    * @param  len number of bytes to use to convert to a long
    *
    * @returns value constructed from bytes
    */
   public static final long toLong (byte[] byteArray, int offset, int len)
   {
      long val = 0;

      len = Math.min(len, 8);

      // Concatanate the byte array into one variable.
      for (int i = (len - 1); i >= 0; i--)
      {
         val <<= 8;
         val |= (byteArray [offset + i] & 0x00FF);
      }

      return val;
   }

   /**
    * This method constructs a long from a LSByte byte array of specified length.
    * Uses 8 bytes starting at the first index.
    *
    * @param  byteArray byte array to convert to a long (LSByte first)
    *
    * @returns value constructed from bytes
    */
   public static final long toLong (byte[] byteArray)
   {
      return toLong(byteArray, 0, Math.min(8, byteArray.length));
   }

   /**
    * This method constructs a LSByte byte array with specified length from a long.
    *
    * @param  longVal the long value to convert to a byte array.
    * @param  byteArray LSByte first byte array, holds bytes from long
    * @param  offset byte offset into the array
    * @param  len number of bytes to get
    *
    * @returns value constructed from bytes
    */
   public static final void toByteArray(long longVal,
                                        byte[] byteArray, int offset, int len)
   {
      int max = offset + len;

      // Concatanate the byte array into one variable.
      for (int i = offset; i <max; i++)
      {
         byteArray[i] = (byte)longVal;
         longVal >>>= 8;
      }
   }

   /**
    * This method constructs a LSByte byte array with 8 bytes from a long.
    *
    * @param  longVal the long value to convert to a byte array.
    * @param  byteArray LSByte first byte array, holds bytes from long
    *
    */
   public static final void toByteArray(long longVal, byte[] byteArray)
   {
      toByteArray(longVal, byteArray, 0, 8);
   }

   /**
    * This method constructs a LSByte byte array with 8 bytes from a long.
    *
    * @param  longVal the long value to convert to a byte array.
    *
    * @returns value constructed from bytes
    */
   public static final byte[] toByteArray(long longVal)
   {
      byte[] byteArray = new byte[8];
      toByteArray(longVal, byteArray, 0, 8);
      return byteArray;
   }

   // ----------------------------------------------------------------------
   // Int <-> ByteArray conversions
   // ----------------------------------------------------------------------

   /**
    * This method constructs an int from a LSByte byte array of specified length.
    *
    * @param  byteArray byte array to convert to an int (LSByte first)
    * @param  offset byte offset into the array where to start to convert
    * @param  len number of bytes to use to convert to an int
    *
    * @returns value constructed from bytes
    */
   public static final int toInt (byte[] byteArray, int offset, int len)
   {
      int val = 0;

      len = Math.min(len, 4);

      // Concatanate the byte array into one variable.
      for (int i = (len - 1); i >= 0; i--)
      {
         val <<= 8;
         val |= (byteArray [offset + i] & 0x00FF);
      }

      return val;
   }

   /**
    * This method constructs an int from a LSByte byte array of specified length.
    * Uses 4 bytes starting at the first index.
    *
    * @param  byteArray byte array to convert to an int (LSByte first)
    *
    * @returns value constructed from bytes
    */
   public static final int toInt (byte[] byteArray)
   {
      return toInt(byteArray, 0, Math.min(4, byteArray.length));
   }

   /**
    * This method constructs a LSByte byte array with specified length from an int.
    *
    * @param  intVal the int value to convert to a byte array.
    * @param  byteArray LSByte first byte array, holds bytes from int
    * @param  offset byte offset into the array
    * @param  len number of bytes to get
    */
   public static final void toByteArray(int intVal,
                                        byte[] byteArray, int offset, int len)
   {
      int max = offset + len;

      // Concatanate the byte array into one variable.
      for (int i = offset; i <max; i++)
      {
         byteArray[i] = (byte)intVal;
         intVal >>>= 8;
      }
   }

   /**
    * This method constructs a LSByte byte array with 4 bytes from an int.
    *
    * @param  intVal the int value to convert to a byte array.
    * @param  byteArray LSByte first byte array, holds bytes from long
    *
    */
   public static final void toByteArray(int intVal, byte[] byteArray)
   {
      toByteArray(intVal, byteArray, 0, 4);
   }

   /**
    * This method constructs a LSByte byte array with 4 bytes from an int.
    *
    * @param  longVal the long value to convert to a byte array.
    *
    * @returns value constructed from bytes
    */
   public static final byte[] toByteArray(int intVal)
   {
      byte[] byteArray = new byte[4];
      toByteArray(intVal, byteArray, 0, 4);
      return byteArray;
   }

   // ----------------------------------------------------------------------
   // String <-> ByteArray conversions
   // ----------------------------------------------------------------------

   /**
    * <P>Converts a hex-encoded string into an array of bytes.</P>
    * <P>To illustrate the rules for parsing, the following String:<br>
    * "FF 0 1234 567"<br>
    * becomes:<br>
    * byte[]{0xFF,0x00,0x12,0x34,0x56,0x07}
    * </P>
    *
    * @param strData hex-encoded numerical string
    * @return byte[] the decoded bytes
    */
   public static final byte[] toByteArray(String strData)
      throws ConvertException
   {
      byte[] bDataTmp = new byte[strData.length()*2];
      int len = toByteArray(strData, bDataTmp, 0, bDataTmp.length);
      byte[] bData = new byte[len];
      System.arraycopy(bDataTmp, 0, bData, 0, len);
      return bData;
   }

   /**
    * <P>Converts a hex-encoded string into an array of bytes.</P>
    * <P>To illustrate the rules for parsing, the following String:<br>
    * "FF 0 1234 567"<br>
    * becomes:<br>
    * byte[]{0xFF,0x00,0x12,0x34,0x56,0x07}
    * </P>
    *
    * @param strData hex-encoded numerical string
    * @param bData byte[] which will hold the decoded bytes
    * @return The number of bytes converted
    */
   public static final int toByteArray(String strData, byte[] bData)
      throws ConvertException
   {
      return toByteArray(strData, bData, 0, bData.length);
   }

   /**
    * <P>Converts a hex-encoded string into an array of bytes.</P>
    * <P>To illustrate the rules for parsing, the following String:<br>
    * "FF 0 1234 567"<br>
    * becomes:<br>
    * byte[]{0xFF,0x00,0x12,0x34,0x56,0x07}
    * </P>
    *
    * @param strData hex-encoded numerical string
    * @param bData byte[] which will hold the decoded bytes
    * @param offset the offset into bData to start placing bytes
    * @param length the maximum number of bytes to convert
    * @return The number of bytes converted
    */
   public static final int toByteArray(String strData,
                                       byte[] bData, int offset, int length)
      throws ConvertException
   {
      int strIndex = 0, strLength = strData.length();
      int index = offset;
      int end = length + offset;
      char upper, lower;
      int uVal, lVal;

      while(index<end && strIndex<strLength)
      {
         lower = '0';
         do
         {
            upper = strData.charAt(strIndex++);
         }
         while(strIndex<strLength && Character.isWhitespace(upper));

         // still haven't reached the end of the string
         if(strIndex<strLength)
         {
            lower = strData.charAt(strIndex++);
            if(Character.isWhitespace(lower))
            {
               lower = upper;
               upper = '0';
            }
         }
         // passed the end of the string with only one character
         else if(!Character.isWhitespace(upper))
         {
            lower = upper;
            upper = '0';
         }
         // passed the end of string with no characters
         else
            continue;
         
         uVal = Character.digit(upper, 16);
         lVal = Character.digit(lower, 16);
         if(uVal!=-1 && lVal!=-1)
         {
            bData[index++] = (byte)(((uVal&0x0F) << 4) | (lVal&0x0F));
         }
         else
            throw new ConvertException(
               ("Bad character in input string: " + upper) + lower);
      }
      return (index-offset);
   }

   /**
    * Converts a byte array into a hex-encoded String, using the provided
    * delimeter.
    *
    * @param data The byte[] to convert to a hex-encoded string
    * @return Hex-encoded String
    */
   public static final String toHexString(byte[] data)
   {
      return toHexString(data, 0, data.length, "");
   }

   /**
    * Converts a byte array into a hex-encoded String, using the provided
    * delimeter.
    *
    * @param data The byte[] to convert to a hex-encoded string
    * @param offset the offset to start converting bytes
    * @param length the number of bytes to convert
    * @return Hex-encoded String
    */
   public static final String toHexString(byte[] data, int offset, int length)
   {
      return toHexString(data, offset, length, "");
   }


   /**
    * Converts a byte array into a hex-encoded String, using the provided
    * delimeter.
    *
    * @param data The byte[] to convert to a hex-encoded string
    * @param delimeter the delimeter to place between each byte of data
    * @return Hex-encoded String
    */
   public static final String toHexString(byte[] data, String delimeter)
   {
      return toHexString(data, 0, data.length, delimeter);
   }

   /**
    * Converts a byte array into a hex-encoded String, using the provided
    * delimeter.
    *
    * @param data The byte[] to convert to a hex-encoded string
    * @param offset the offset to start converting bytes
    * @param length the number of bytes to convert
    * @param delimeter the delimeter to place between each byte of data
    * @return Hex-encoded String
    */
   public static final String toHexString(byte[] data, int offset, int length,
                                          String delimeter)
   {
      StringBuffer value = new StringBuffer(length*(2+delimeter.length()));
      int max = length+offset;
      int lastDelim = max-1;
      for(int i=offset; i<max; i++)
      {
         byte bits = data[i];
         value.append(lookup_hex[(bits>>4)&0x0F]);
         value.append(lookup_hex[bits&0x0F]);
         if(i<lastDelim)
            value.append(delimeter);
      }
      return value.toString();
   }

   /**
    * <P>Converts a single byte into a hex-encoded string.</P>
    *
    * @param bValue the byte to encode
    * @return String Hex-encoded String
    */
   public static final String toHexString(byte bValue)
   {
      char[] hexValue = new char[2];
      hexValue[1] = lookup_hex[bValue&0x0F];
      hexValue[0] = lookup_hex[(bValue>>4)&0x0F];
      return new String(hexValue);
   }

   /**
    * Converts a char array into a hex-encoded String, using the provided
    * delimeter.
    *
    * @param data The char[] to convert to a hex-encoded string
    * @return Hex-encoded String
    */
   public static final String toHexString(char[] data)
   {
      return toHexString(data, 0, data.length, "");
   }

   /**
    * Converts a byte array into a hex-encoded String, using the provided
    * delimeter.
    *
    * @param data The char[] to convert to a hex-encoded string
    * @param offset the offset to start converting bytes
    * @param length the number of bytes to convert
    * @return Hex-encoded String
    */
   public static final String toHexString(char[] data, int offset, int length)
   {
      return toHexString(data, offset, length, "");
   }


   /**
    * Converts a char array into a hex-encoded String, using the provided
    * delimeter.
    *
    * @param data The char[] to convert to a hex-encoded string
    * @param delimeter the delimeter to place between each byte of data
    * @return Hex-encoded String
    */
   public static final String toHexString(char[] data, String delimeter)
   {
      return toHexString(data, 0, data.length, delimeter);
   }

   /**
    * Converts a char array into a hex-encoded String, using the provided
    * delimeter.
    *
    * @param data The char[] to convert to a hex-encoded string
    * @param offset the offset to start converting bytes
    * @param length the number of bytes to convert
    * @param delimeter the delimeter to place between each byte of data
    * @return Hex-encoded String
    */
   public static final String toHexString(char[] data, int offset, int length,
                                          String delimeter)
   {
      StringBuffer value = new StringBuffer(length*(2+delimeter.length()));
      int max = length+offset;
      int lastDelim = max-1;
      for(int i=offset; i<max; i++)
      {
         char bits = data[i];
         value.append(lookup_hex[(bits>>4)&0x0F]);
         value.append(lookup_hex[bits&0x0F]);
         if(i<lastDelim)
            value.append(delimeter);
      }
      return value.toString();
   }

   /**
    * <P>Converts a single character into a hex-encoded string.</P>
    *
    * @param bValue the byte to encode
    * @return String Hex-encoded String
    */
   public static final String toHexString(char bValue)
   {
      char[] hexValue = new char[2];
      hexValue[1] = lookup_hex[bValue&0x0F];
      hexValue[0] = lookup_hex[(bValue>>4)&0x0F];
      return new String(hexValue);
   }


   // ----------------------------------------------------------------------
   // String <-> Long conversions
   // ----------------------------------------------------------------------

   /**
    * <P>Converts a hex-encoded string (LSByte) into a long.</P>
    * <P>To illustrate the rules for parsing, the following String:<br>
    * "FF 0 1234 567 12 03"<br>
    * becomes:<br>
    * long 0x03120756341200ff
    * </P>
    *
    * @param strData hex-encoded numerical string
    * @return the decoded long
    */
   public static final long toLong(String strData)
      throws ConvertException
   {
      return toLong(toByteArray(strData));
   }

   /**
    * <P>Converts a long into a hex-encoded string (LSByte).</P>
    *
    * @param lValue the long integer to encode
    * @return String Hex-encoded String
    */
   public static final String toHexString(long lValue)
   {
      return toHexString(toByteArray(lValue),"");
   }

   // ----------------------------------------------------------------------
   // String <-> Int conversions
   // ----------------------------------------------------------------------

   /**
    * <P>Converts a hex-encoded string (LSByte) into an int.</P>
    * <P>To illustrate the rules for parsing, the following String:<br>
    * "FF 0 1234 567 12 03"<br>
    * becomes:<br>
    * long 0x03120756341200ff
    * </P>
    *
    * @param strData hex-encoded numerical string
    * @return the decoded int
    */
   public static final int toInt(String strData)
      throws ConvertException
   {
      return toInt(toByteArray(strData));
   }

   /**
    * <P>Converts an integer into a hex-encoded string (LSByte).</P>
    *
    * @param iValue the integer to encode
    * @return String Hex-encoded String
    */
   public static final String toHexString(int iValue)
   {
      return toHexString(toByteArray(iValue),"");
   }

   // ----------------------------------------------------------------------
   // Double conversions
   // ----------------------------------------------------------------------

   /** Field Double NEGATIVE_INFINITY */
   static final double d_POSITIVE_INFINITY = 1.0d / 0.0d;
   /** Field Double NEGATIVE_INFINITY */
   static final double d_NEGATIVE_INFINITY = -1.0d / 0.0d;

   /**
    * <P>Converts a double value into a string with the specified number of
    * digits after the decimal place.</P>
    *
    * @param dubbel the double value to convert to a string
    * @param nFrac the number of digits to display after the decimal point
    *
    * @return String representation of the double value with the specified
    *         number of digits after the decimal place.
    */
   public static final String toString(double dubbel, int nFrac)
   {
      // check for special case
      if(dubbel==d_POSITIVE_INFINITY)
         return "Infinity";
      else if(dubbel==d_NEGATIVE_INFINITY)
         return "-Infinity";
      else if(dubbel!=dubbel)
         return "NaN";

      // check for fast out (no fractional digits)
      if(nFrac<=0)
         // round the whole portion
         return Long.toString((long)(dubbel + 0.5d));

      // extract the non-fractional portion
      long dWhole = (long)dubbel;

      // figure out if it's positive or negative.  We need to remove
      // the sign from the fractional part
      double sign = (dWhole<0) ? -1d : 1d;

      // figure out how many places to shift fractional portion
      double shifter = 1;
      for(int i=0; i<nFrac; i++)
         shifter *= 10;

      // extract, unsign, shift, and round the fractional portion
      long dFrac = (long)((dubbel - dWhole)*sign*shifter + 0.5d);

      // convert the fractional portion to a string
      String fracString = Long.toString(dFrac);
      int fracLength = fracString.length();

      // ensure that rounding the fraction didn't carry into the whole portion
      if(fracLength>nFrac)
      {
         dWhole += 1;
         fracLength = 0;
      }

      // convert the whole portion to a string
      String wholeString = Long.toString(dWhole);
      int wholeLength = wholeString.length();

      // create the string buffer
      char[] dubbelChars = new char[wholeLength + 1 + nFrac];

      // append the non-fractional portion
      wholeString.getChars(0, wholeLength, dubbelChars, 0);

      // and the decimal place
      dubbelChars[wholeLength] = '.';

      // append any necessary leading zeroes
      int i = wholeLength + 1;
      int max = i + nFrac - fracLength;
      for(; i<max; i++)
         dubbelChars[i] = '0';

      // append the fractional portion
      if(fracLength>0)
         fracString.getChars(0, fracLength, dubbelChars, max);

      return new String(dubbelChars, 0, dubbelChars.length);
   }


   // ----------------------------------------------------------------------
   // Float conversions
   // ----------------------------------------------------------------------

   /** Field Float NEGATIVE_INFINITY */
   static final float f_POSITIVE_INFINITY = 1.0f / 0.0f;
   /** Field Float NEGATIVE_INFINITY */
   static final float f_NEGATIVE_INFINITY = -1.0f / 0.0f;

   /**
    * <P>Converts a float value into a string with the specified number of
    * digits after the decimal place.</P>
    * <P>Note: this function does not properly handle special case float
    * values such as Infinity and NaN.</P>
    *
    * @param flote the float value to convert to a string
    * @param nFrac the number of digits to display after the decimal point
    *
    * @return String representation of the float value with the specified
    *         number of digits after the decimal place.
    */
   public static final String toString(float flote, int nFrac)
   {
      // check for special case
      if(flote==f_POSITIVE_INFINITY)
         return "Infinity";
      else if(flote==f_NEGATIVE_INFINITY)
         return "-Infinity";
      else if(flote!=flote)
         return "NaN";

      // check for fast out (no fractional digits)
      if(nFrac<=0)
         // round the whole portion
         return Long.toString((long)(flote + 0.5f));

      // extract the non-fractional portion
      long fWhole = (long)flote;

      // figure out if it's positive or negative.  We need to remove
      // the sign from the fractional part
      float sign = (fWhole<0) ? -1f : 1f;

      // figure out how many places to shift fractional portion
      float shifter = 1;
      for(int i=0; i<nFrac; i++)
         shifter *= 10;

      // extract, shift, and round the fractional portion
      long fFrac = (long)((flote - fWhole)*sign*shifter + 0.5f);

      // convert the fractional portion to a string
      String fracString = Long.toString(fFrac);
      int fracLength = fracString.length();

      // ensure that rounding the fraction didn't carry into the whole portion
      if(fracLength>nFrac)
      {
         fWhole += 1;
         fracLength = 0;
      }

      // convert the whole portion to a string
      String wholeString = String.valueOf(fWhole);
      int wholeLength = wholeString.length();

      // create the string buffer
      char[] floteChars = new char[wholeLength + 1 + nFrac];

      // append the non-fractional portion
      wholeString.getChars(0, wholeLength, floteChars, 0);

      // and the decimal place
      floteChars[wholeLength] = '.';

      // append any necessary leading zeroes
      int i = wholeLength + 1;
      int max = i + nFrac - fracLength;
      for(; i<max; i++)
         floteChars[i] = '0';

      // append the fractional portion
      if(fracLength>0)
         fracString.getChars(0, fracLength, floteChars, max);

      return new String(floteChars, 0, floteChars.length);
   }
}
