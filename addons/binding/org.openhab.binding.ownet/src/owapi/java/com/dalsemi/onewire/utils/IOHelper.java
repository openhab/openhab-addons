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

import java.io.*;

/**
 * Generic IO routines.  Supports printing and reading arrays of bytes.
 * Also, using the setReader and setWriter methods, the source of the
 * bytes can come from any stream as well as the destination for
 * written bytes.  All routines are static and final and handle all
 * exceptional cases by returning a default value.
 * 
 * @version    0.02, 2 June 2001
 * @author     SH
 */
public final class IOHelper
{
   /** Do not instantiate this class */
   private IOHelper(){;}

   /*----------------------------------------------------------------*/
   /*   Reading Helper Methods                                       */
   /*----------------------------------------------------------------*/

   private static BufferedReader br = null;
   // default the buffered reader to read from STDIN
      static
      {
         try
         {
            br = new BufferedReader(new InputStreamReader(System.in));
         }
         catch(Exception e)
         {
            System.err.println("IOHelper: Catastrophic Failure!");
            System.exit(1);
         }
      }

   public static final synchronized void setReader(Reader r)
   {
      br = new BufferedReader(r);
   }
   
   public static final synchronized String readLine()
   {
      try
      {
         return br.readLine();
      }
      catch(IOException ioe)
      {
         return "";
      }
   }
   
   public static final synchronized byte[] readBytes(int count, int pad, boolean hex)
   {
      if(hex)
         return readBytesHex(count,pad);
      else
         return readBytesAsc(count,pad);
   }
   public static final synchronized byte[] readBytesHex(int count, int pad)
   {
      try
      {
         String s   = br.readLine();
         int    len = s.length() > count ? count
                                         : s.length();
         byte[] ret;

         if (count > 0)
            ret = new byte [count];
         else
            ret = new byte [s.length()];

         byte[] temp = parseHex(s, 0);

         if (count == 0)
            return temp;

         len = temp.length;

         System.arraycopy(temp, 0, ret, 0, len);

         for (; len < count; len++)
            ret [len] = ( byte ) pad;

         return ret;
      }
      catch (Exception e)
      {
         return new byte [count];
      }
   }
   public static final synchronized byte[] readBytesAsc(int count, int pad)
   {
      try
      {
         String s   = br.readLine();
         int    len = s.length() > count ? count
                                         : s.length();
         byte[] ret;

         if (count > 0)
            ret = new byte [count];
         else
            ret = new byte [s.length()];

         if (count == 0)
         {
            System.arraycopy(s.getBytes(), 0, ret, 0, s.length());

            return ret;
         }

         System.arraycopy(s.getBytes(), 0, ret, 0, len);

         for (; len < count; len++)
            ret [len] = ( byte ) pad;

         return ret;
      }
      catch (IOException e)
      {
         return new byte [count];
      }
   }
   
   private static final byte[] parseHex (String s, int size)
   {
      byte[] temp;
      int    index = 0;
      char[] x = s.toLowerCase().toCharArray();

      if (size > 0)
         temp = new byte [size];
      else
         temp = new byte [x.length];

      try
      {
         for(int i=0; i<x.length && index<temp.length; index++)
         {
            int digit = -1;
            
            while(i<x.length && digit==-1) 
               digit = Character.digit(x[i++], 16);
            if(digit!=-1)
               temp[index] = (byte) ((digit << 4) & 0xF0);
            
            digit = -1;
            
            while(i<x.length && digit==-1) 
               digit = Character.digit(x[i++], 16);
            if(digit!=-1)
              temp[index] |= (byte)(digit & 0x0F);
         }
      }
      catch(Exception e){;}

      byte[] t;
      
      if (size == 0 && temp.length!=index)
      {
         t = new byte [index];
         System.arraycopy(temp, 0, t, 0, t.length);
      }
      else
         t = temp;

      return t;
   }

   public static final synchronized int readInt()
   {
      return readInt(-1);
   }
   public static final synchronized int readInt(int def)
   {
      try
      {
         return Integer.parseInt(br.readLine());
      }
      catch(NumberFormatException nfe)
      {
         return def;
      }
      catch(IOException ioe)
      {
         return def;
      }
   }

   /*----------------------------------------------------------------*/
   /*   Writing Helper Methods                                       */
   /*----------------------------------------------------------------*/

   private static PrintWriter pw = null;
   // default the print writer to write to STDOUT
      static
      {
         try
         {
            pw = new PrintWriter(new OutputStreamWriter(System.out));
         }
         catch(Exception e)
         {
            System.err.println("IOHelper: Catastrophic Failure!");
            System.exit(1);
         }
      }
   public static final synchronized void setWriter(Writer w)
   {
      pw = new PrintWriter(w);
   }
   
   public static final synchronized void writeBytesHex(String delim, byte[] b, int offset, int cnt)
   {
      int i = offset;
      for(; i<(offset+cnt); )
      {
         if( i!=offset && ((i-offset)&15)==0 )
            pw.println();
         pw.print(byteStr(b[i++]));
         pw.print(delim);
      }
      pw.println();
      pw.flush();
   }
   public static final synchronized void writeBytesHex(byte[] b, int offset, int cnt)
   {
      writeBytesHex(".", b, offset, cnt);
   }
   public static final synchronized void writeBytesHex(byte[] b)
   {
      writeBytesHex(".", b, 0, b.length);
   }
   
   /**
    * Writes a <code>byte[]</code> to the specified output stream.  This method
    * writes a combined hex and ascii representation where each line has
    * (at most) 16 bytes of data in hex followed by three spaces and the ascii
    * representation of those bytes.  To write out just the Hex representation,
    * use <code>writeBytesHex(byte[],int,int)</code>.
    * 
    * @param b the byte array to print out.
    * @param offset the starting location to begin printing
    * @param cnt the number of bytes to print.
    */
   public static final synchronized void writeBytes(String delim, byte[] b, int offset, int cnt)
   {
      int last,i;
      last = i = offset;
      for(; i<(offset+cnt); )
      {
         if( i!=offset && ((i-offset)&15)==0 )
         {
            pw.print("  ");
            for(; last<i; last++)
               pw.print((char)b[last]);
            pw.println();
         }
         pw.print(byteStr(b[i++]));
         pw.print(delim);
      }
      for(int k=i; ((k-offset)&15)!=0; k++)
      {
         pw.print("  ");
         pw.print(delim);
      }
      pw.print("  ");
      for(; last<i; last++)
         pw.print((char)b[last]);
      pw.println();
      pw.flush();
   }
   
   /**
    * Writes a <code>byte[]</code> to the specified output stream.  This method
    * writes a combined hex and ascii representation where each line has
    * (at most) 16 bytes of data in hex followed by three spaces and the ascii
    * representation of those bytes.  To write out just the Hex representation,
    * use <code>writeBytesHex(byte[],int,int)</code>.
    * 
    * @param b the byte array to print out.
    */
   public static final synchronized void writeBytes(byte[] b)
   {
      writeBytes(".", b, 0, b.length);
   }

   public static final synchronized void writeBytes(byte[] b, int offset, int cnt)
   {
      writeBytes(".", b, offset, cnt);
   }

   public static final synchronized void write(String s)
   {
      pw.print(s);
      pw.flush();
   }
   public static final synchronized void write(Object o)
   {
      pw.print(o);
      pw.flush();
   }
   public static final synchronized void write(boolean b)
   {
      pw.print(b);
      pw.flush();
   }
   public static final synchronized void write(int i)
   {
      pw.print(i);
      pw.flush();
   }
   
   
   public static final synchronized void writeLine()
   {
      pw.println();
      pw.flush();
   }
   public static final synchronized void writeLine(String s)
   {
      pw.println(s);
      pw.flush();
   }
   public static final synchronized void writeLine(Object o)
   {
      pw.println(o);
      pw.flush();
   }
   public static final synchronized void writeLine(boolean b)
   {
      pw.println(b);
      pw.flush();
   }
   public static final synchronized void writeLine(int i)
   {
      pw.println(i);
      pw.flush();
   }

   public static final synchronized void writeHex(byte b)
   {
      pw.print(byteStr(b));
      pw.flush();
   }
   public static final synchronized void writeHex(long l)
   {
      pw.print(Long.toHexString(l));
      pw.flush();
   }
   
   public static final synchronized void writeLineHex(byte b)
   {
      pw.println(byteStr(b));
      pw.flush();
   }
   public static final synchronized void writeLineHex(long l)
   {
      pw.println(Long.toHexString(l));
      pw.flush();
   }

   private static final char[] hex = "0123456789ABCDEF".toCharArray();
   private static final String byteStr(byte b)
   {
      return "" + hex[((b>>4)&0x0F)] + hex[(b&0x0F)];
   }
   
}
