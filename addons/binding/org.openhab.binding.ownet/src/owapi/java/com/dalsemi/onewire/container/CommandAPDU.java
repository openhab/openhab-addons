
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

// CommandAPDU.java
package com.dalsemi.onewire.container;

/**
 * A <code>CommandAPDU</code> represents an ISO 7816-4 specified
 * Application Protocol Data Unit (APDU) sent to a
 * smart card. A response from the smart card is in turn represented
 * by a <code>ResponseAPDU</code>.<BR><BR>
 *
 * According to ISO 7816-4, a <code>CommandAPDU</code> has the following
 * format: <pre>
 *                  HEADER         |           BODY
 *         CLA    INS    P1    P2  |  [LC]    [DATA]    [LE]</pre>
 * where
 * <ul>
 * <li><code>CLA</code>  is the class byte
 * <li><code>INS</code>  is the instruction byte
 * <li><code>P1</code>   is the first parameter byte
 * <li><code>P2</code>   is the second parameter byte
 * <li><code>LC</code>   is the number of bytes present in the data block
 * <li><code>DATA</code> is an byte array of data to be sent
 * <li><code>LE</code>   is the maximum number of bytes expected in the <code>ResponseAPDU</code>
 * <li><code>[ ]</code>  denotes optional fields
 * </ul>
 *
 * <H3> Usage </H3> 
 * <OL>
 * <LI> 
 *    <code><pre>
 *   byte[] buffer = {(byte)0x90, (byte)0x00, (byte)0x00, (byte)0x00, 
 *                    (byte)0x01, (byte)0x02, (byte)0x03};
 *   CommandAPDU capdu = new CommandAPDU(buffer); </pre></code>
 * <LI>
 *   <code><pre>
 *   CommandAPDU capdu = new CommandAPDU((byte)0x90, (byte)0x00, (byte)0x00, (byte)0x00, 
 *                                       (byte)0x01, (byte)0x02, (byte)0x03);</pre></code>
 * </OL>
 * 
 * <H3> Additonal information </H3> 
 * <DL>
 * <DD><A HREF="http://www.opencard.org"> http://www.opencard.org</A>
 * </DL>
 * 
 * @see com.dalsemi.onewire.container.ResponseAPDU
 * @see com.dalsemi.onewire.container.OneWireContainer16
 * 
 * @version    0.00, 28 Aug 2000
 * @author     YL
 *
 */
public class CommandAPDU
{

   /** Index for addressing <code>CLA</code> in this <code>CommandAPDU</code>
       <code>apduBuffer</code>. */
   public final static int CLA = 0;

   /** Index for addressing <code>INS</code> in this <code>CommandAPDU</code>
       <code>apduBuffer</code>. */
   public final static int INS = 1;

   /** Index for addressing <code>P1</code>  in this <code>CommandAPDU</code>
       <code>apduBuffer</code>. */
   public final static int P1 = 2;

   /** Index for addressing <code>P2</code>  in this <code>CommandAPDU</code>
       <code>apduBuffer</code>. */
   public final static int P2 = 3;

   /** Index for addressing <code>LC</code> in this <code>CommandAPDU</code>   
       <code>apduBuffer</code>. */
   public final static int LC = 4;

   /** Byte array containing the entire <code>CommandAPDU</code>. */
   protected byte[] apduBuffer = null;

   /** Length of this <code>CommandAPDU</code> currently in the 
       <code>apduBuffer</code>. */
   protected int apduLength;

   /**
    * Constructs a new ISO 7816-4 <code>CommandAPDU</code>.
    *
    * @param     buffer  the entire <code>CommandAPDU</code> as a byte array
    */
   public CommandAPDU (byte[] buffer)
   {
      apduLength = buffer.length;
      apduBuffer = new byte [apduLength];

      System.arraycopy(buffer, 0, apduBuffer, 0, apduLength);
   }   // CommandAPDU

   /**
    * Constructs a new ISO 7816-4 CASE 1 <code>CommandAPDU</code>.
    *
    * @param     cla  <code>CLA</code> byte
    * @param     ins  <code>INS</code> byte
    * @param     p1   parameter byte <code>P1</code>
    * @param     p2   parameter byte <code>P2</code>
    */
   public CommandAPDU (byte cla, byte ins, byte p1, byte p2)
   {
      this(cla, ins, p1, p2, null, -1);
   }   // CommandAPDU

   /**
    * Constructs a new ISO 7816-4 CASE 2 <code>CommandAPDU</code>.
    *
    * @param     cla  <code>CLA</code> byte
    * @param     ins  <code>INS</code> byte
    * @param     p1   parameter byte <code>P1</code>
    * @param     p2   parameter byte <code>P2</code>
    * @param     le   length of expected <code>ResponseAPDU</code>,
    *                 ranges from <code>-1</code> to 
    *                 <code>255</code>, where <code>-1</code> is no length
    *                 and <code>0</code> is the maximum length 
    *                 supported
    *
    * @see       ResponseAPDU
    */
   public CommandAPDU (byte cla, byte ins, byte p1, byte p2, int le)
   {
      this(cla, ins, p1, p2, null, le);
   }   // CommandAPDU

   /**
    * Constructs a new ISO 7816-4 CASE 3 <code>CommandAPDU</code>.
    *
    * @param     cla  <code>CLA</code> byte
    * @param     ins  <code>INS</code> byte
    * @param     p1   parameter byte <code>P1</code>
    * @param     p2   parameter byte <code>P2</code>
    * @param     data this <code>CommandAPDU</code> data as a byte array,
    *                 <code>LC</code> is derived from this data 
    *                 array length
    */
   public CommandAPDU (byte cla, byte ins, byte p1, byte p2, byte[] data)
   {
      this(cla, ins, p1, p2, data, -1);
   }   // CommandAPDU

   /**
    * Constructs a new ISO 7816-4 CASE 4 <code>CommandAPDU</code>.
    *
    * @param     cla  <code>CLA</code> byte
    * @param     ins  <code>INS</code> byte
    * @param     p1   parameter byte <code>P1</code>
    * @param     p2   parameter byte <code>P2</code>
    * @param     data <code>CommandAPDU</code> data as a byte array,
    *                 <code>LC</code> is derived from this data 
    *                 array length
    * @param     le   length of expected <code>ResponseAPDU</code>,
    *                 ranges from <code>-1</code> to 
    *                 <code>255</code>, where <code>-1</code> is no length
    *                 and <code>0</code> is the maximum length 
    *                 supported
    *
    * @see       ResponseAPDU
    */
   public CommandAPDU (byte cla, byte ins, byte p1, byte p2, byte[] data,
                       int le)
   {

      // KLA ... changed 7-18-02.  We always need that
      // length byte (LC) specified.  Otherwise if the IPR isn't
      // cleared out, then we might try to read a garbage
      // length and think we've gotten a screwy APDU on the
      // button.

      // all CommandAPDU has at least 5 bytes of header...
      // that's CLA, INS, P1, P2, and LC 

      apduLength = 5;

      if (data != null)
      {
         apduLength += data.length;   // add data length
      }

      if (le >= 0)
      {
         apduLength++;   // add one byte for LE
      }

      apduBuffer = new byte [apduLength];

      // fill CommandAPDU buffer body
      apduBuffer [CLA] = cla;
      apduBuffer [INS] = ins;
      apduBuffer [P1]  = p1;
      apduBuffer [P2]  = p2;

      if (data != null)
      {
         apduBuffer [LC] = ( byte ) data.length;

         System.arraycopy(data, 0, apduBuffer, LC + 1, data.length);
      }
      else
      {
         // fill in the LC byte anyhoo
         apduBuffer[LC] = (byte)0;
      }

      if (le >= 0)
         apduBuffer [apduLength - 1] = ( byte ) le;
   }   // CommandAPDU

   /**
    * Gets the <code>CLA</code> byte value.
    *
    * @return <code>CLA</code> byte of this <code>CommandAPDU</code>
    */
   public byte getCLA ()
   {
      return apduBuffer [CLA];
   }   // getCLA

   /**
    * Gets the <code>INS</code> byte value.
    *
    * @return <code>INS</code> byte of this <code>CommandAPDU</code>
    */
   public byte getINS ()
   {
      return apduBuffer [INS];
   }   // getINS

   /**
    * Gets the first parameter (<code>P1</code>) byte value.
    *
    * @return <code>P1</code> byte of this <code>CommandAPDU</code>
    */
   public byte getP1 ()
   {
      return apduBuffer [P1];
   }   //getP1

   /**
    * Gets the second parameter (<code>P2</code>) byte value.
    *
    * @return <code>P2</code> byte of this <code>CommandAPDU</code>
    */
   public byte getP2 ()
   {
      return apduBuffer [P2];
   }   // getP2

   /**
    * Gets the length of data field (<code>LC</code>).
    *
    * @return the number of bytes present in the data field of
    * this <code>CommandAPDU</code>, <code>0</code> 
    * indicates that there is no body
    */
   public int getLC ()
   {
      if (apduLength >= 6)
         return apduBuffer [LC];
      else
         return 0;
   }   // getLC

   /**
    * Gets the expected length of <code>ResponseAPDU</code> (<code>LE</code>).
    *
    * @return the maximum number of bytes expected in the data field
    * of the <code>ResponseAPDU</code> to this <code>CommandAPDU</code>, 
    * <code>-1</code> indicates that no value is specified
    *
    * @see       ResponseAPDU
    */
   public int getLE ()
   {
      if ((apduLength == 5) || (apduLength == (6 + getLC())))
         return apduBuffer [apduLength - 1];
      else
         return -1;
   }   // getLE

   /**
    * Gets this <code>CommandAPDU</code> <code>apduBuffer</code>.
    * This method allows user to manipulate the buffered <code>CommandAPDU</code>.
    *
    * @return  <code>apduBuffer</code> that holds the current <code>CommandAPDU</code>
    *
    * @see #getBytes
    *
    */
   final public byte[] getBuffer ()
   {
      return apduBuffer;
   }   // getBuffer

   /**
    * Gets the byte at the specified offset in the <code>apduBuffer</code>.
    * This method can only be used to access the <code>CommandAPDU</code>
    * currently stored.  It is not possible to read beyond the
    * end of the <code>CommandAPDU</code>.
    *
    * @param index   the offset in the <code>apduBuffer</code>
    *
    * @return        the value at the given offset,
    *                or <code>-1</code> if the offset is invalid
    *
    * @see #setByte
    * @see #getLength
    */
   final public byte getByte (int index)
   {
      if (index >= apduLength)
         return ( byte ) -1;   // read beyond end of CommandAPDU
      else
         return (apduBuffer [index]);
   }                           // getByte

   /**
    * Gets a byte array of the buffered <code>CommandAPDU</code>.
    * The byte array returned gets allocated with the exact size of the
    * buffered <code>CommandAPDU</code>. To get direct access to the 
    * internal <code>apduBuffer</code>, use <code>getBuffer()</code>.
    *
    * @return  the buffered <code>CommandAPDU</code> copied into a new array
    *
    * @see #getBuffer
    */
   final public byte[] getBytes ()
   {
      byte[] apdu = new byte [apduLength];

      System.arraycopy(apduBuffer, 0, apdu, 0, apduLength);

      return apdu;
   }   // getBytes

   /**
    * Gets the length of the buffered <code>CommandAPDU</code>.
    *
    * @return  the length of the <code>CommandAPDU</code> currently stored
    */
   final public int getLength ()
   {
      return apduLength;
   }   // getLength

   /**
    * Sets the byte value at the specified offset in the 
    * <code>apduBuffer</code>.
    * This method can only be used to modify a <code>CommandAPDU</code>
    * already stored. It is not possible to set bytes beyond
    * the end of the current <code>CommandAPDU</code>.
    *
    * @param index   the offset in the <code>apduBuffer</code>
    * @param value   the new byte value to store
    *
    * @see #getByte
    * @see #getLength
    *
    */
   final public void setByte (int index, byte value)
   {
      if (index < apduLength)
         apduBuffer [index] = value;
   }   // setByte

   /**
    * Gets a string representation of this <code>CommandAPDU</code>.
    *
    * @return a string describing this <code>CommandAPDU</code>
    */
   public String toString ()
   {
      String apduString = "";

      apduString += "CLA = " + Integer.toHexString(apduBuffer [CLA] & 0xFF);
      apduString += " INS = " + Integer.toHexString(apduBuffer [INS] & 0xFF);
      apduString += " P1 = " + Integer.toHexString(apduBuffer [P1] & 0xFF);
      apduString += " P2 = " + Integer.toHexString(apduBuffer [P2] & 0xFF);
      apduString += " LC = " + Integer.toHexString(getLC() & 0xFF);

      if (getLE() == -1)
         apduString += " LE = " + getLE();
      else
         apduString += " LE = " + Integer.toHexString(getLE() & 0xFF);

      if (apduLength > 5)
      {
         apduString += "\nDATA = ";

         for (int i = 5; i < getLC() + 5; i++)
         {
            if ((apduBuffer [i] & 0xFF) < 0x10)
               apduString += '0';

            apduString += Integer.toHexString(( int ) (apduBuffer [i] & 0xFF))
                          + " ";
         }
      }

      // make hex String representation of byte array
      return (apduString.toUpperCase());
   }   // toString
}
