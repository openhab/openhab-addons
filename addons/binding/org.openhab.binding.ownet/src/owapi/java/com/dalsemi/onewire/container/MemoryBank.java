
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
 * <P>Memory bank interface class for basic memory communication with
 * iButtons (or 1-Wire devices).  The method <CODE> getMemoryBanks </CODE>
 * in all 1-Wire Containers ({@link com.dalsemi.onewire.container.OneWireContainer OneWireContainer})
 * returns an Enumeration of this interface to be used to read or write it's
 * memory. If the 1-Wire device does not have memory or the memory is non-standard,
 * then this enumeration may be empty. 
 * A MemoryBank returned from this method may also implement the
 * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}, 
 * or {@link com.dalsemi.onewire.container.OTPMemoryBank OTPMemoryBank} interfaces, 
 * to provide additional functionality. </P> 
 * 
 * <P>The MemoryBank methods can be organized into the following categories: </P>
 * <UL>
 *   <LI> <B> Information </B>
 *     <UL>
 *       <LI> {@link #getBankDescription() getBankDescription} 
 *       <LI> {@link #getSize() getSize} 
 *       <LI> {@link #getStartPhysicalAddress() getStartPhysicalAddress} 
 *       <LI> {@link #isGeneralPurposeMemory() isGeneralPurposeMemory} 
 *       <LI> {@link #isReadWrite() isReadWrite}
 *       <LI> {@link #isWriteOnce() isWriteOnce}
 *       <LI> {@link #isReadOnly() isReadOnly}
 *       <LI> {@link #isNonVolatile() isNonVolatile}
 *       <LI> {@link #needsProgramPulse() needsProgramPulse}
 *       <LI> {@link #needsPowerDelivery() needsPowerDelivery}
 *     </UL>
 *   <LI> <B> Options </B>
 *     <UL>
 *       <LI> {@link #setWriteVerification(boolean) setWriteVerification}
 *     </UL>
 *   <LI> <B> I/O </B>
 *     <UL>
 *       <LI> {@link #read(int,boolean,byte[],int,int) read}
 *       <LI> {@link #write(int,byte[], int, int) write}
 *     </UL>
 *  </UL>
 *
 * <H3> Usage </H3> 
 *  
 * <DL> 
 * <DD> <H4> Example 1</H4> 
 * Display some features of MemoryBank instance 'mb': 
 * <PRE> <CODE>
 *  if (mb.isWriteOnce())
 *     System.out.println("MemoryBank is write-once");
 * 
 *  if (mb.needsProgramPulse())
 *     System.out.println("MemoryBank requires program-pulse to write");
 * </CODE> </PRE>
 * 
 * <DD> <H4> Example 2</H4> 
 * Write the entire contents of a MemoryBank instance 'mb' with zeros: 
 * <PRE> <CODE>
 *  byte[] write_buf = new byte[mb.getSize()];
 *  for (int i = 0; i < write_buf.length; i++)
 *      write_buf[i] = (byte)0;
 * 
 *  mb.write(0, write_buf, 0, write_buf.length);
 * </CODE> </PRE>
 *
 * <DD> <H4> Example 3</H4>
 * Read the entire contents of a MemoryBank instance 'mb': 
 * <PRE> <CODE>
 *  byte[] read_buf = new byte[mb.getSize()];
 *
 *  mb.read(0, false, read_buf, 0, read_buf.length);
 * </CODE> </PRE>
 * </DL>
 *
 * @see com.dalsemi.onewire.container.PagedMemoryBank
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
 * @version    0.00, 28 Aug 2000
 * @author     DS
 */
public interface MemoryBank
{

   //--------
   //-------- Memory Bank Feature methods
   //--------

   /**
    * Gets a string description of this memory bank.
    *
    * @return  the memory bank description
    */
   public String getBankDescription ();

   /**
    * Checks to see if this memory bank is general purpose
    * user memory.  If it is NOT then it may be Memory-Mapped and writing
    * values to this memory may affect the behavior of the 1-Wire
    * device.
    *
    * @return  <CODE> true </CODE> if this memory bank is general purpose
    */
   public boolean isGeneralPurposeMemory ();

   /**
    * Gets the size of this memory bank in bytes.
    *
    * @return  number of bytes in current memory bank
    */
   public int getSize ();

   /**
    * Checks to see if this memory bank is read/write.
    *
    * @return  <CODE> true </CODE> if this memory bank is read/write
    */
   public boolean isReadWrite ();

   /**
    * Checks to see if this memory bank is write once such
    * as with EPROM technology.
    *
    * @return  <CODE>  true </CODE> if this memory bank can only be written once
    */
   public boolean isWriteOnce ();

   /**
    * Checks to see if this memory bank is read only.
    *
    * @return  <CODE>  true </CODE> if this memory bank can only be read
    */
   public boolean isReadOnly ();

   /**
    * Checks to see if this memory bank is non-volatile.  Memory is
    * non-volatile if it retains its contents even when removed from
    * the 1-Wire network.
    *
    * @return  <CODE> true </CODE>  if this memory bank is non volatile
    */
   public boolean isNonVolatile ();

   /**
    * Checks to see if this  memory bank requires a 
    * 'ProgramPulse' in order to write.
    *
    * @return  <CODE> true </CODE>  if writing to this memory bank 
    *                 requires a 'ProgramPulse' from the 1-Wire Adapter.
    *
    * @see com.dalsemi.onewire.adapter.DSPortAdapter
    */
   public boolean needsProgramPulse ();

   /**
    * Checks to see if this memory bank requires 'PowerDelivery'
    * in order to write.
    *
    * @return  <CODE> true </CODE> if writing to this memory bank 
    *                 requires 'PowerDelivery' from the 1-Wire Adapter
    *
    * @see com.dalsemi.onewire.adapter.DSPortAdapter
    */
   public boolean needsPowerDelivery ();

   /**
    * Gets the starting physical address of this bank.  Physical
    * banks are sometimes sub-divided into logical banks due to changes
    * in attributes.  Note that this method is for information only.  The read
    * and write methods will automatically calculate the physical address
    * when writing to a logical memory bank.
    *
    * @return  physical starting address of this logical bank
    */
   public int getStartPhysicalAddress ();

   /**
    * Sets or clears write verification for the 
    * {@link #write(int,byte[],int,int) write} method.
    *
    * @param  doReadVerf   <CODE> true </CODE>  (default) 
    *                      verify write in 'write',
    *                      <CODE> false </CODE> don't verify write (used on
    *                      Write-Once bit manipulation)
    *
    * @see com.dalsemi.onewire.container.OTPMemoryBank
    */
   public void setWriteVerification (boolean doReadVerf);

   //--------
   //-------- I/O methods
   //--------

   /**
    * Reads memory in this bank with no CRC checking (device or
    * data). The resulting data from this API may or may not be what is on
    * the 1-Wire device.  It is recommended that the data contain some kind
    * of checking (CRC) like in the <CODE> readPagePacket </CODE> method in
    * the
    * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
    * interface.  Some 1-Wire devices provide thier own CRC as in 
    * <CODE> readPageCRC </CODE> also found in the 
    * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank} 
    * interface.  The <CODE> readPageCRC </CODE> method 
    * is not supported on all memory types, see <CODE> hasPageAutoCRC </CODE>
    * in the same interface.
    * If neither is an option then this method could be called more
    * then once to at least verify that the same data is read consistently.  The
    * readContinue parameter is used to eliminate the overhead in re-accessing
    * a part already being read from. For example, if pages 0 - 4 are to
    * be read, readContinue would be set to false for page 0 and would be set
    * to true for the next four calls.
    *
    * <P> Note: Using readContinue = true  can only be used if the new
    *           read continuous where the last one led off
    *           and it is inside a 'beginExclusive/endExclusive'
    *           block.
    *
    * @param  startAddr     starting address
    * @param  readContinue  <CODE> true </CODE> then device read is 
    *                       continued without re-selecting
    * @param  readBuf       location for data read
    * @param  offset        offset into readBuf to place data
    * @param  len           length in bytes to read
    *
    * @throws OneWireIOException on a 1-Wire communication error such as 
    *         no device present.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to 
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire 
    *         adapter
    */
   public void read (int startAddr, boolean readContinue, byte[] readBuf,
                     int offset, int len)
      throws OneWireIOException, OneWireException;

   /**
    * Writes memory in this bank. It is recommended that a structure with some
    * built in error checking is used to provide data integrity on read.
    * The method <CODE> writePagePacket </CODE> found in the 
    * {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank} 
    * interface, which automatically wraps the data in a length and CRC, could
    * be used for this purpose.
    *
    * <P> When using on Write-Once devices care must be taken to write into
    * into empty space.  If <CODE> write </CODE> is used to write over an unlocked
    * page on a Write-Once device it will fail.  If write verification
    * is turned off with the method 
    * {@link #setWriteVerification(boolean) setWriteVerification(false)} 
    * then the result will be an 'AND' of the existing data and the new data. 
    *
    * @param  startAddr     starting address
    * @param  writeBuf      data to write
    * @param  offset        offset into writeBuf to get data
    * @param  len           length in bytes to write
    *
    * @throws OneWireIOException on a 1-Wire communication error such as 
    *         no device present or a read back verification fails.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to 
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire 
    *         adapter
    */
   public void write (int startAddr, byte[] writeBuf, int offset, int len)
      throws OneWireIOException, OneWireException;
}
