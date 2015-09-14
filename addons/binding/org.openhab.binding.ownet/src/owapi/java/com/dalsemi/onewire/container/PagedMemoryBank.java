
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
 * <P> Paged Memory bank interface for iButtons (or 1-Wire devices) with page
 * based memory.  This interface extents the base functionality of
 * the super-interface {@link com.dalsemi.onewire.container.MemoryBank MemoryBank}
 * by providing paged based services. </P> 
 *
 * <P> This interface has methods to read and write a packet structure
 * called the UDP (Universal Data Packet).  This structure has a length byte,
 * data, and an inverted CRC16.  See Dallas Semiconductor Application Note 114
 * for details: 
 * <A HREF="http://dbserv.maxim-ic.com/appnotes.cfm?appnote_number=114"> 
 * http://dbserv.maxim-ic.com/appnotes.cfm?appnote_number=114</A>
 * </P> 
 * 
 * <P>The MemoryBank methods can be organized into the following categories: </P>
 * <UL>
 *   <LI> <B> Information </B>
 *     <UL>
 *       <LI> {@link #getNumberPages() getNumberPages} 
 *       <LI> {@link #getPageLength() getPageLength} 
 *       <LI> {@link #getMaxPacketDataLength() getMaxPacketDataLength} 
 *       <LI> {@link #hasPageAutoCRC() hasPageAutoCRC} 
 *       <LI> {@link #hasExtraInfo() hasExtraInfo}
 *       <LI> {@link #getExtraInfoLength() getExtraInfoLength}
 *       <LI> {@link #getExtraInfoDescription() getExtraInfoDescription}
 *     </UL>
 *   <LI> <B> I/O </B>
 *     <UL>
 *       <LI> {@link #readPage(int,boolean,byte[],int) readPage}
 *       <LI> {@link #readPage(int,boolean,byte[],int,byte[]) readPage with extra info}
 *       <LI> {@link #readPageCRC(int,boolean,byte[],int) readPageCRC}
 *       <LI> {@link #readPageCRC(int,boolean,byte[],int,byte[]) readPageCRC with extra info}
 *       <LI> {@link #readPagePacket(int,boolean,byte[],int) readPagePacket}
 *       <LI> {@link #readPagePacket(int,boolean,byte[],int,byte[]) readPagePacket with extra info}
 *       <LI> {@link #writePagePacket(int,byte[],int,int) writePagePacket}
 *     </UL>
 *  </UL>
 *
 * <H3> Usage </H3> 
 *  
 * <DL> 
 * <DD> <H4> Example 1</H4> 
 * Display some features of PagedMemoryBank instance 'pmb': 
 * <PRE> <CODE>
 *  System.out.print("PagedMemoryBank has: " + pmb.getNumberPages() + " pages of length ");
 *  System.out.print(pbank.getPageLength() + " bytes ");
 *  if (bank.isGeneralPurposeMemory())
 *    System.out.print("giving " + pbank.getMaxPacketDataLength() + " bytes Packet data payload");
 *  System.out.println();
 *
 *  if (pbank.hasPageAutoCRC())
 *    System.out.print("PagedMemoryBank has device generated CRC");
 * </CODE> </PRE>
 *
 * <DD> <H4> Example 2</H4> 
 * Write a packet into the first page of a PagedMemoryBank instance 'pmb': 
 * <PRE> <CODE>
 *  byte[] write_buf = new byte[pmb.getMaxPacketDataLength()];
 *  for (int i = 0; i < write_buf.length; i++)
 *      write_buf[i] = (byte)0;
 * 
 *  mb.writePagePacket(0, write_buf, 0, write_buf.length);
 * </CODE> </PRE>
 *
 * <DD> <H4> Example 3</H4> 
 * Read all of the pages of a PagedMemoryBank instance 'pmb' with device CRC verification: 
 * <PRE> <CODE>
 *  byte[] read_buf = new byte[pmb.getPageLength()];
 *
 *  if (pmb.hasAutoCRC())
 *  {
 *     // loop to read each page with CRC 
 *     for (int pg = 0; pg < pmb.getNumberPages(); pg++)
 *     {
 *        // use 'readContinue' arguement to only access device on first page
 *        pmb.readPageCRC(pg, (pg == 0), read_buf, 0);
 *
 *        // do something with data in read_buf ... 
 *     }
 *  }
 *  else
 *     System.out.println("PagedMemoryBank does not support device generated CRC");
 * 
 * </CODE> </PRE>
 * </DL>
 * 
 * @see com.dalsemi.onewire.container.MemoryBank
 * @see com.dalsemi.onewire.container.OTPMemoryBank
 * @see com.dalsemi.onewire.container.OneWireContainer04
 * @see com.dalsemi.onewire.container.OneWireContainer06
 * @see com.dalsemi.onewire.container.OneWireContainer08
 * @see com.dalsemi.onewire.container.OneWireContainer09
 * @see com.dalsemi.onewire.container.OneWireContainer0A
 * @see com.dalsemi.onewire.container.OneWireContainer0B
 * @see com.dalsemi.onewire.container.OneWireContainer0C
 * @see com.dalsemi.onewire.container.OneWireContainer0F
 * @see com.dalsemi.onewire.container.OneWireContainer12
 * @see com.dalsemi.onewire.container.OneWireContainer13
 * @see com.dalsemi.onewire.container.OneWireContainer14
 * @see com.dalsemi.onewire.container.OneWireContainer18
 * @see com.dalsemi.onewire.container.OneWireContainer1A
 * @see com.dalsemi.onewire.container.OneWireContainer1D
 * @see com.dalsemi.onewire.container.OneWireContainer20
 * @see com.dalsemi.onewire.container.OneWireContainer21
 * @see com.dalsemi.onewire.container.OneWireContainer23
 *
 * @version    0.01, 11 Dec 2000
 * @author     DS
 */
public interface PagedMemoryBank
   extends MemoryBank
{

   //--------
   //-------- Paged Memory Bank Feature methods
   //--------

   /**
    * Gets the number of pages in this memory bank.
    * The page numbers are then always 0 to (getNumberPages() - 1).
    *
    * @return  number of pages in this memory bank
    */
   public int getNumberPages ();

   /**
    * Gets raw page length in bytes in this memory bank.
    *
    * @return   page length in bytes in this memory bank
    */
   public int getPageLength ();

   /**
    * Gets Maximum data page length in bytes for a packet
    * read or written in this memory bank.  See the 
    * {@link #readPagePacket(int,boolean,byte[],int) readPagePacket}
    * and 
    * {@link #writePagePacket(int,byte[],int,int) writePagePacket}
    * methods.  This method is only usefull
    * if this memory bank is general purpose memory.
    *
    * @return  max packet page length in bytes in this memory bank
    * 
    * @see #readPagePacket(int,boolean,byte[],int) readPagePacket
    * @see #readPagePacket(int,boolean,byte[],int,byte[]) readPagePacket(extra)
    * @see #writePagePacket(int,byte[],int,int) writePagePacket
    */
   public int getMaxPacketDataLength ();

   /**
    * Checks to see if this memory bank's pages can be read with
    * the contents being verified by a device generated CRC.
    * This is used to see if the 
    * {@link #readPageCRC(int,boolean,byte[],int) readPageCRC}
    * method can be used.
    *
    * @return  <CODE> true </CODE> if this memory bank can be 
    *          read with self generated CRC
    *
    * @see #readPageCRC(int,boolean,byte[],int) readPageCRC
    * @see #readPageCRC(int,boolean,byte[],int,byte[]) readPageCRC(extra)
    */
   public boolean hasPageAutoCRC ();

   /**
    * Checks to see if this memory bank's pages deliver extra 
    * information outside of the normal data space,  when read.  Examples
    * of this may be a redirection byte, counter, tamper protection
    * bytes, or SHA-1 result.  If this method returns true then the
    * methods with an 'extraInfo' parameter can be used:
    * {@link #readPage(int,boolean,byte[],int,byte[]) readPage},
    * {@link #readPageCRC(int,boolean,byte[],int,byte[]) readPageCRC}, and
    * {@link #readPagePacket(int,boolean,byte[],int,byte[]) readPagePacket}.
    *
    * @return  <CODE> true </CODE> if reading the this memory bank's 
    *                 pages provides extra information
    *
    * @see #readPage(int,boolean,byte[],int,byte[]) readPage(extra)
    * @see #readPageCRC(int,boolean,byte[],int,byte[]) readPageCRC(extra)
    * @see #readPagePacket(int,boolean,byte[],int,byte[]) readPagePacket(extra)
    *
    * @deprecated  As of 1-Wire API 0.01, replaced by {@link #hasExtraInfo()}
    */
   public boolean haveExtraInfo ();

   /**
    * Checks to see if this memory bank's pages deliver extra 
    * information outside of the normal data space,  when read.  Examples
    * of this may be a redirection byte, counter, tamper protection
    * bytes, or SHA-1 result.  If this method returns true then the
    * methods with an 'extraInfo' parameter can be used:
    * {@link #readPage(int,boolean,byte[],int,byte[]) readPage},
    * {@link #readPageCRC(int,boolean,byte[],int,byte[]) readPageCRC}, and
    * {@link #readPagePacket(int,boolean,byte[],int,byte[]) readPagePacket}.
    *
    * @return  <CODE> true </CODE> if reading the this memory bank's 
    *                 pages provides extra information
    *
    * @see #readPage(int,boolean,byte[],int,byte[]) readPage(extra)
    * @see #readPageCRC(int,boolean,byte[],int,byte[]) readPageCRC(extra)
    * @see #readPagePacket(int,boolean,byte[],int,byte[]) readPagePacket(extra)
    * @since 1-Wire API 0.01
    */
   public boolean hasExtraInfo ();

   /**
    * Gets the length in bytes of extra information that
    * is read when reading a page in this memory bank.  
    *
    * @return  number of bytes in Extra Information read when reading
    *          pages from this memory bank
    *
    * @see #hasExtraInfo() hasExtraInfo
    */
   public int getExtraInfoLength ();

   /**
    * Gets a string description of what is contained in
    * the Extra Information returned when reading pages in this
    * memory bank.  
    *
    * @return extra information description.
    *
    * @see #hasExtraInfo() hasExtraInfo
    */
   public String getExtraInfoDescription ();

   //--------
   //-------- I/O methods
   //--------

   /**
    * Reads a page in this memory bank with no
    * CRC checking (device or data). The resulting data from this API
    * may or may not be what is on the 1-Wire device.  It is recommends
    * that the data contain some kind of checking (CRC) like in the
    * {@link #readPagePacket(int,boolean,byte[],int) readPagePacket}
    * method or have the 1-Wire device provide the
    * CRC as in 
    * {@link #readPageCRC(int,boolean,byte[],int) readPageCRC}.
    * However device CRC generation is not
    * supported on all memory types, see 
    * {@link #hasPageAutoCRC() hasPageAutoCRC}.
    * If neither is an option then this method could be called more
    * then once to at least verify that the same data is read consistently.
    *
    * The readContinue parameter is used to eliminate the overhead in re-accessing
    * a part already being read from. For example, if pages 0 - 4 are to
    * be read, readContinue would be set to false for page 0 and would be set
    * to true for the next four calls.
    * <P> Note: Using readContinue = true  can only be used if the new
    *           read continues where the last one left off
    *           and it is inside a 'beginExclusive/endExclusive'
    *           block.
    *
    * @param  page          page number to read packet from
    * @param  readContinue  <CODE> true </CODE> then device read 
    *                       is continued without re-selecting  
    * @param  readBuf       location for data read 
    * @param  offset        offset into readBuf to place data
    *
    * @throws OneWireIOException on a 1-Wire communication error such as 
    *         no 1-Wire device present.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to 
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire 
    *         adapter
    */
   public void readPage (int page, boolean readContinue, byte[] readBuf,
                         int offset)
      throws OneWireIOException, OneWireException;

   /**
    * Reads a page in this memory bank with extra information with no
    * CRC checking (device or data). The resulting data from this API
    * may or may not be what is on the 1-Wire device.  It is recommends
    * that the data contain some kind of checking (CRC) like in the
    * {@link #readPagePacket(int,boolean,byte[],int,byte[]) readPagePacket}
    * method or have the 1-Wire device provide the
    * CRC as in 
    * {@link #readPageCRC(int,boolean,byte[],int,byte[]) readPageCRC}.
    * However device CRC generation is not
    * supported on all memory types, see 
    * {@link #hasPageAutoCRC() hasPageAutoCRC}.
    * If neither is an option then this method could be called more
    * then once to at least verify that the same data is read consistently.The
    * readContinue parameter is used to eliminate the overhead in re-accessing
    * a part already being read from. For example, if pages 0 - 4 are to
    * be read, readContinue would be set to false for page 0 and would be set
    * to true for the next four calls.
    *
    * <P> Note: Using readContinue = true  can only be used if the new
    *           read continues where the last one left off
    *           and it is inside a 'beginExclusive/endExclusive'
    *           block.
    *
    * @param  page          page number to read packet from
    * @param  readContinue  <CODE> true </CODE> then device read 
    *                       is continued without re-selecting
    * @param  readBuf       location for data read 
    * @param  offset        offset into readBuf to place data
    * @param  extraInfo     location for extra info read
    *
    * @throws OneWireIOException on a 1-Wire communication error such as 
    *         no 1-Wire device present.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to 
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire 
    *         adapter
    *
    * @see #hasExtraInfo() hasExtraInfo
    * @see #getExtraInfoLength() getExtraInfoLength 
    */
   public void readPage (int page, boolean readContinue, byte[] readBuf,
                         int offset, byte[] extraInfo)
      throws OneWireIOException, OneWireException;

   /**
    * Reads a Universal Data Packet.
    *
    * The Universal Data Packet always starts on page boundaries but
    * can end anywhere in the page.  The structure specifies the length of
    * data bytes not including the length byte and the CRC16 bytes.
    * There is one length byte. The CRC16 is first initialized to
    * the page number.  This provides a check to verify the page that
    * was intended is being read.  The CRC16 is then calculated over
    * the length and data bytes.  The CRC16 is then inverted and stored
    * low byte first followed by the high byte.  The structure is
    * used by this method to verify the data but only
    * the data payload is returned. The
    * readContinue parameter is used to eliminate the overhead in re-accessing
    * a part already being read from. For example, if pages 0 - 4 are to
    * be read, readContinue would be set to false for page 0 and would be set
    * to true for the next four calls.
    *
    * <P> See Dallas Semiconductor Application Note 114
    * for details: <A HREF="http://dbserv.maxim-ic.com/appnotes.cfm?appnote_number=114"> 
    * http://dbserv.maxim-ic.com/appnotes.cfm?appnote_number=114</A>
    *
    * <P> Note: Using readContinue = true  can only be used if the new
    *           read continues where the last one left off
    *           and it is inside a 'beginExclusive/endExclusive'
    *           block.
    *
    * @param  page          page number to read packet from
    * @param  readContinue  <CODE> true </CODE> true then device read 
    *                       is continued without re-selecting
    * @param  readBuf       location for data read
    * @param  offset        offset into readBuf to place data
    *
    * @return  number of data bytes read from the device and saved to
    *          readBuf at the provided offset
    *
    * @throws OneWireIOException on a 1-Wire communication error such as 
    *         an invalid CRC16 or length found in the packet.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to 
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    *         It could also be caused due to the device page not containing a 
    *         valid packet.
    * @throws OneWireException on a communication or setup error with the 1-Wire 
    *         adapter
    * 
    * @see #getMaxPacketDataLength() getMaxPacketDataLength 
    */
   public int readPagePacket (int page, boolean readContinue, byte[] readBuf,
                              int offset)
      throws OneWireIOException, OneWireException;

   /**
    * Reads a Universal Data Packet and extra information.  See the
    * method 
    * {@link #readPagePacket(int,boolean,byte[],int) readPagePacket}
    * for a description of the packet structure.  The
    * readContinue parameter is used to eliminate the overhead in re-accessing
    * a part already being read from. For example, if pages 0 - 4 are to
    * be read, readContinue would be set to false for page 0 and would be set
    * to true for the next four calls.
    *
    * <P> Note: Using readContinue = true  can only be used if the new
    *           read continues where the last one left off
    *           and it is inside a 'beginExclusive/endExclusive'
    *           block.
    *
    * @param  page          page number to read packet from
    * @param  readContinue  <CODE> true </CODE> then device read 
    *                       is continued without re-selecting
    * @param  readBuf       location for data read
    * @param  offset        offset into readBuf to place data
    * @param  extraInfo     location for extra info read
    *
    * @return  number of data bytes read from the device and written to
    *          readBuf at the offset.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as 
    *         an invalid CRC16 or length found in the packet.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to 
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    *         It could also be caused due to the device page not containing a 
    *         valid packet.
    * @throws OneWireException on a communication or setup error with the 1-Wire 
    *         adapter
    *
    * @see #hasExtraInfo() hasExtraInfo
    * @see #getExtraInfoLength() getExtraInfoLength
    * @see #getMaxPacketDataLength() getMaxPacketDataLength
    */
   public int readPagePacket (int page, boolean readContinue, byte[] readBuf,
                              int offset, byte[] extraInfo)
      throws OneWireIOException, OneWireException;

   /**
    * Writes a Universal Data Packet.  See the method 
    * {@link #readPagePacket(int,boolean,byte[],int) readPagePacket}
    * for a description of the packet structure.
    *
    * @param  page          page number to write packet to
    * @param  writeBuf      data to write
    * @param  offset        offset into writeBuf where data to write is
    * @param  len           number of bytes to write with a max of
    *                       {@link #getMaxPacketDataLength() getMaxPacketDataLength} 
    *                       elements
    *
    * @throws OneWireIOException on a 1-Wire communication error such as 
    *         read verification error on write.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to 
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    *         It could also be caused due to the device page being write protected.
    * @throws OneWireException on a communication or setup error with the 1-Wire 
    *         adapter
    *
    * @see #getMaxPacketDataLength() getMaxPacketDataLength 
    */
   public void writePagePacket (int page, byte[] writeBuf, int offset,
                                int len)
      throws OneWireIOException, OneWireException;

   /**
    * Reads a complete memory page with CRC verification provided by the
    * device.  Not supported by all devices.  The
    * readContinue parameter is used to eliminate the overhead in re-accessing
    * a part already being read from. For example, if pages 0 - 4 are to
    * be read, readContinue would be set to false for page 0 and would be set
    * to true for the next four calls.
    *
    * <P> Note: Using readContinue = true  can only be used if the new
    *           read continues where the last one left off
    *           and it is inside a 'beginExclusive/endExclusive'
    *           block.
    *
    * @param  page          page number to read
    * @param  readContinue  <CODE> true </CODE> true then device read 
    *                       is continued without re-selecting
    * @param  readBuf       location for data read
    * @param  offset        offset into readBuf to place data
    *
    * @throws OneWireIOException on a 1-Wire communication error such as 
    *         an invalid CRC read from device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to 
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire 
    *         adapter
    *
    * @see #hasPageAutoCRC() hasPageAutoCRC
    * @see #getPageLength() getPageLength
    */
   public void readPageCRC (int page, boolean readContinue, byte[] readBuf,
                            int offset)
      throws OneWireIOException, OneWireException;

   /**
    * Reads a complete memory page with CRC verification provided by the
    * device with extra information.  Not supported by all devices. The
    * readContinue parameter is used to eliminate the overhead in re-accessing
    * a part already being read from. For example, if pages 0 - 4 are to
    * be read, readContinue would be set to false for page 0 and would be set
    * to true for the next four calls.
    * 
    *
    * <P> Note: Using readContinue = true  can only be used if the new
    *           read continues where the last one left off
    *           and it is inside a 'beginExclusive/endExclusive'
    *           block.
    *
    * @param  page          page number to read
    * @param  readContinue  <CODE> true </CODE> true then device read 
    *                       is issued without continued without re-selecting
    * @param  readBuf       location for data read
    * @param  offset        offset into readBuf to place data
    * @param  extraInfo     location for extra info read
    *
    * @throws OneWireIOException on a 1-Wire communication error such as 
    *         an invalid CRC read from device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to 
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire 
    *         adapter
    *
    * @see #hasExtraInfo() hasExtraInfo
    * @see #getExtraInfoLength() getExtraInfoLength
    * @see #hasPageAutoCRC() hasPageAutoCRC
    * @see #getPageLength() getPageLength
    */
   public void readPageCRC (int page, boolean readContinue, byte[] readBuf,
                            int offset, byte[] extraInfo)
      throws OneWireIOException, OneWireException;
}
