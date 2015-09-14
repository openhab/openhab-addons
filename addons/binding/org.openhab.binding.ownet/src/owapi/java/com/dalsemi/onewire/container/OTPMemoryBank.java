
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
 * <P> One-Time-Programmable (OTP) Memory bank interface for iButtons (or 1-Wire devices) 
 * with OTP features.  This interface extents the base functionality of
 * the super-interfaces {@link com.dalsemi.onewire.container.MemoryBank MemoryBank}
 * and {@link com.dalsemi.onewire.container.PagedMemoryBank PagedMemoryBank}
 * by providing One-Time-Programmable services. </P> 
 *
 * <P>The OTPMemoryBank methods can be organized into the following categories: </P>
 * <UL>
 *   <LI> <B> Information </B>
 *     <UL>
 *       <LI> {@link #canRedirectPage() canRedirectPage} 
 *       <LI> {@link #canLockPage() canLockPage} 
 *       <LI> {@link #canLockRedirectPage() canLockRedirectPage} 
 *     </UL>
 *   <LI> <B> Read Status </B>
 *     <UL>
 *       <LI> {@link #getRedirectedPage(int) getRedirectedPage} 
 *       <LI> {@link #isPageLocked(int) isPageLocked} 
 *       <LI> {@link #isRedirectPageLocked(int) isRedirectPageLocked} 
 *     </UL>
 *   <LI> <B> Write Status </B>
 *     <UL>
 *       <LI> {@link #redirectPage(int,int) redirectPage} 
 *       <LI> {@link #lockPage(int) lockPage} 
 *       <LI> {@link #lockRedirectPage(int) lockRedirectPage} 
 *     </UL>
 *  </UL>
 *
 * <H3> Usage </H3> 
 *  
 * <DL> 
 * <DD> <H4> Example 1</H4> 
 * Read the OTP status of page 0 in the OTPMemoryBank instance 'otp':
 * <PRE> <CODE>
 *  if (otp.canRedirectPage())
 *  {
 *     int new_page = getRedirectedPage(0);
 *     if (new_page != 0)
 *        System.out.println("Page 0 is redirected to " + new_page);
 *  }
 * 
 *  if (otp.canLockPage())
 *  {
 *     if (otp.isPageLocked(0))
 *        System.out.println("Page 0 is locked");
 *  }
 *
 *  if (otp.canLockRedirectPage())
 *  {
 *     if (otp.isRedirectPageLocked(0))
 *        System.out.println("Page 0 redirection is locked");
 *  }
 * </CODE> </PRE>
 *
 * <DD> <H4> Example 1</H4> 
 * Lock all of the pages in the OTPMemoryBank instance 'otp':
 * <PRE> <CODE>
 *  if (otp.canLockPage())
 *  {
 *     // loop to lock each page  
 *     for (int pg = 0; pg < otp.getNumberPages(); pg++)
 *     {
 *        otp.lockPage(pg);
 *     }
 *  }
 *  else
 *     System.out.println("OTPMemoryBank does not support page locking");
 * </CODE> </PRE>
 * </DL>
 *
 * @see com.dalsemi.onewire.container.MemoryBank
 * @see com.dalsemi.onewire.container.PagedMemoryBank
 * @see com.dalsemi.onewire.container.OneWireContainer09
 * @see com.dalsemi.onewire.container.OneWireContainer0B
 * @see com.dalsemi.onewire.container.OneWireContainer0F
 * @see com.dalsemi.onewire.container.OneWireContainer12
 * @see com.dalsemi.onewire.container.OneWireContainer13
 *
 * @version    0.01, 11 Dec 2000
 * @author     DS
 */
public interface OTPMemoryBank
   extends PagedMemoryBank
{

   //--------
   //-------- OTP Memory Bank feature methods
   //--------

   /**
    * Checks to see if this memory bank has pages that can be redirected
    * to a new page.  This is used in Write-Once memory
    * to provide a means to update.
    *
    * @return  <CODE> true </CODE> if this memory bank pages can be redirected
    *          to a new page
    *
    * @see #redirectPage(int,int) redirectPage
    * @see #getRedirectedPage(int) getRedirectedPage
    */
   public boolean canRedirectPage ();

   /**
    * Checks to see if this memory bank has pages that can be locked.  A
    * locked page would prevent any changes to it's contents.
    *
    * @return  <CODE> true </CODE> if this memory bank has pages that can be 
    *          locked
    *
    * @see #lockPage(int) lockPage
    * @see #isPageLocked(int) isPageLocked
    */
   public boolean canLockPage ();

   /**
    * Checks to see if this memory bank has pages that can be locked from
    * being redirected.  This would prevent a Write-Once memory from
    * being updated.
    *
    * @return  <CODE> true </CODE> if this memory bank has pages that can 
    *          be locked from being redirected to a new page

    * @see #lockRedirectPage(int) lockRedirectPage
    * @see #isRedirectPageLocked(int) isRedirectPageLocked
    */
   public boolean canLockRedirectPage ();

   //--------
   //-------- I/O methods
   //--------

   /**
    * Locks the specifed page in this memory bank.  Not supported
    * by all devices. 
    *
    * @param  page   number of page to lock
    *
    * @throws OneWireIOException on a 1-Wire communication error such as 
    *         no device present or a read back verification fails.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to 
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire 
    *         adapter.  It will also be thrown if the device needs 'program' voltage
    *         and the adapter used by this device does not support it.
    *
    * @see #isPageLocked(int) isPageLocked
    * @see #canLockPage() canLockPage
    * @see com.dalsemi.onewire.adapter.DSPortAdapter#canProgram() DSPortAdapter.canProgram()
    */
   public void lockPage (int page)
      throws OneWireIOException, OneWireException;

   /**
    * Checks to see if the specified page is locked.
    *
    * @param  page  page to check 
    *
    * @return  <CODE> true </CODE> if page is locked
    *
    * @throws OneWireIOException on a 1-Wire communication error such as 
    *         no device present or a CRC read from the device is incorrect.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to 
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire 
    *         adapter.  
    *
    * @see #lockPage(int) lockPage
    * @see #canLockPage() canLockPage
    */
   public boolean isPageLocked (int page)
      throws OneWireIOException, OneWireException;

   /**
    * Redirects the specifed page to a new page.
    * Not supported by all devices. 
    *
    * @param  page      number of page to redirect
    * @param  newPage   new page number to redirect to
    *
    * @throws OneWireIOException on a 1-Wire communication error such as 
    *         no device present or a CRC read from the device is incorrect.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to 
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire 
    *         adapter.  It will also be thrown if the device needs 'program' voltage
    *         and the adapter used by this device does not support it.
    *
    * @see #canRedirectPage() canRedirectPage
    * @see #getRedirectedPage(int) getRedirectedPage
    * @see com.dalsemi.onewire.adapter.DSPortAdapter#canProgram() DSPortAdapter.canProgram()
    */
   public void redirectPage (int page, int newPage)
      throws OneWireIOException, OneWireException;

   /**
    * Checks to see if the specified page is redirected.
    * Not supported by all devices. 
    *
    * @param  page  page to check for redirection
    *
    * @return  the new page number or 0 if not redirected
    *
    * @throws OneWireIOException on a 1-Wire communication error such as 
    *         no device present or a CRC read from the device is incorrect.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to 
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire 
    *         adapter.  
    *
    * @see #canRedirectPage() canRedirectPage
    * @see #redirectPage(int,int) redirectPage
    *
    * @deprecated  As of 1-Wire API 0.01, replaced by {@link #getRedirectedPage(int)}
    */
   public int isPageRedirected (int page)
      throws OneWireIOException, OneWireException;

   /**
    * Gets the page redirection of the specified page.
    * Not supported by all devices. 
    *
    * @param  page  page to check for redirection
    *
    * @return  the new page number or 0 if not redirected
    *
    * @throws OneWireIOException on a 1-Wire communication error such as 
    *         no device present or a CRC read from the device is incorrect.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to 
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire 
    *         adapter.  
    *
    * @see #canRedirectPage() canRedirectPage
    * @see #redirectPage(int,int) redirectPage
    * @since 1-Wire API 0.01
    */
   public int getRedirectedPage (int page)
      throws OneWireIOException, OneWireException;

   /**
    * Locks the redirection of the specifed page. 
    * Not supported by all devices.  
    *
    * @param  page  page to redirect
    *
    * @throws OneWireIOException on a 1-Wire communication error such as 
    *         no device present or a CRC read from the device is incorrect.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to 
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire 
    *         adapter.  It will also be thrown if the device needs 'program' voltage
    *         and the adapter used by this device does not support it.
    *
    * @see #canLockRedirectPage() canLockRedirectPage
    * @see #isRedirectPageLocked(int) isRedirectPageLocked
    * @see com.dalsemi.onewire.adapter.DSPortAdapter#canProgram() DSPortAdapter.canProgram()
    */
   public void lockRedirectPage (int page)
      throws OneWireIOException, OneWireException;

   /**
    * Checks to see if the specified page has redirection locked.
    * Not supported by all devices.  
    *
    * @param  page  page to check for locked redirection
    *
    * @return  <CODE> true </CODE> if redirection is locked for this page
    *
    * @throws OneWireIOException on a 1-Wire communication error such as 
    *         no device present or a CRC read from the device is incorrect.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to 
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire 
    *         adapter.  
    *
    * @see #canLockRedirectPage() canLockRedirectPage
    * @see #lockRedirectPage(int) lockRedirectPage
    */
   public boolean isRedirectPageLocked (int page)
      throws OneWireIOException, OneWireException;
}
