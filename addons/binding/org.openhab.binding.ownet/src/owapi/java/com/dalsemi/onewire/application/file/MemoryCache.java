
/*---------------------------------------------------------------------------
 * Copyright (C) 2001 Dallas Semiconductor Corporation, All Rights Reserved.
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

package com.dalsemi.onewire.application.file;

// imports
import java.util.Vector;
import java.util.Enumeration;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.PagedMemoryBank;
import com.dalsemi.onewire.container.MemoryBank;
import com.dalsemi.onewire.container.OTPMemoryBank;
import com.dalsemi.onewire.utils.CRC16;
import com.dalsemi.onewire.utils.Bit;


/**
 * Class to provide read/write cache services to a 1-Wire memory
 * device.  Writes are only performed when this classes <code>sync()</code>
 * method is called.  Provides page bitmap services for OTP devices.
 *
 * <p>Objectives:
 * <ul>
 * <li> Cache read/written pages
 * <li> write only on sync()
 * <li> write order is oldest to newest.
 * <li> Collect redirection information when appropriate
 * </ul>
 *
 * @author  DS
 * @version 0.01, 1 June 2001
 * @see     com.dalsemi.onewire.application.file.OWFile
 * @see     com.dalsemi.onewire.application.file.OWFileDescriptor
 * @see     com.dalsemi.onewire.application.file.OWFileInputStream
 * @see     com.dalsemi.onewire.application.file.OWFileOutputStream
 */
class MemoryCache
{

   //--------
   //-------- Static Final Variables
   //--------

   /** cache pageState's */
   private static final int NOT_READ      = 0;
   private static final int READ_CRC      = 1;
   private static final int READ_NO_CRC   = 2;
   private static final int VERIFY        = 3;
   private static final int REDIRECT      = 4;
   private static final int WRITE         = 5;

   /** Flag to indicate the writeLog entry is empty */
   private static final int EMPTY         = -1;

   /** Field NONE - flag to indicate last page read is not known */
   private static final int NONE = -100;

   /** Field USED - flag to indicate page bitmap file used */
   private static final int USED = 0;

   /** Field NOT_USED - flag to indicated page bitmap file un-used */
   private static final int NOT_USED = 1;

   /** Enable/disable debug messages */
   private static final boolean doDebugMessages = false;

   //--------
   //-------- Variables
   //--------

   /** Field owd - 1-Wire container that containes this memory to cache */
   private OneWireContainer[] owd;

   /** Field cache -  2 dimentional array to contain the cache */
   private byte[][] cache;

   /** Field len - array of lengths of packets found */
   private int[] len;

   /** Field pageState - array of flags to indicate the page has been changed */
   private int[] pageState;

   /** Field banks - vector of memory banks that contain the Filesystem */
   private Vector banks;

   /** Field totalPages - total pages in this Filesystem */
   private int totalPages;

   /** Field lastPageRead - last page read by this cache */
   private int lastPageRead;

   /** Field maxPacketDataLength - maximum data length on a page */
   private int maxPacketDataLength;

   /** Field bankPages - array of the number of pages in vector of memory banks */
   private int[] bankPages;

   /** Field startPages - array of the number of start pages for device list */
   private int[] startPages;

   /** Field writeLog - array to track the order of pages written to the cache */
   private int[] writeLog;

   /** Field tempExtra - temporary buffer used to to read the extra information from a page read */
   private byte[] tempExtra;

   /** Field tempPage - temporary buffer the size of a page */
   private byte[] tempPage;

   /** Field redirect - array of redirection bytes */
   private int[] redirect;

   /** Field owners - vector of classes that are using this cache */
   private Vector owners;

   /** Field openedToWrite - vector of files that have been opened to write on this filesystem */
   private Vector openedToWrite;

   /** Field canRedirect - flag to indicate page redirection information must be gathered */
   private boolean canRedirect;

   /** Field pbmBank - memory bank used for the page bitmap */
   private OTPMemoryBank pbmBank;

   /** Field pbmByteOffset - byte offset into page bitmap buffer */
   private int pbmByteOffset;

   /** Field pbmBitOffset - bit offset into page bitmap buffer  */
   private int pbmBitOffset;

   /** Field pbmCache - buffer to cache the page bitmap */
   private byte[] pbmCache;

   /** Field pbmCacheModified - modifified version of the page bitmap */
   private byte[] pbmCacheModified;

   /** Field pbmRead - flag indicating that the page bitmap has been read */
   private boolean pbmRead;

   /** Field lastFreePage - last free page found in the page bitmap */
   private int lastFreePage;

   /** Field lastDevice - last device read/written */
   private int lastDevice;

   /** Field autoOverdrive - flag to indicate if we need to do auto-ovedrive */
   private boolean autoOverdrive;

   //--------
   //-------- Constructor
   //--------

   /**
    * Construct a new memory cache for provided 1-wire container device.
    *
    * @param device 1-Wire container
    */
   public MemoryCache(OneWireContainer device)
   {
      OneWireContainer[] devices = new OneWireContainer[1];
      devices[0] = device;

      init(devices);
   }

   /**
    * Construct a new memory cache for provided 1-wire container device.
    *
    * @param device 1-Wire container
    */
   public MemoryCache(OneWireContainer[] devices)
   {
      init(devices);
   }

   /**
    * Initializes this memory cache for provided 1-wire container device(s).
    *
    * @param devices 1-Wire container(s)
    */
   private void init(OneWireContainer[] devices)
   {
      owd = devices;
      int mem_size=0;

      PagedMemoryBank pmb = null;

      banks         = new Vector(1);
      owners        = new Vector(1);
      openedToWrite = new Vector(1);
      startPages    = new int[owd.length];
      lastDevice    = 0;

      // check to see if adapter supports overdrive
      try
      {
         autoOverdrive = devices[0].getAdapter().canOverdrive();
      }
      catch (OneWireException e)
      {
         autoOverdrive = false;
      }

      //\\//\\//\\//\\//\\//\\//\\//
      if (doDebugMessages)
         System.out.println("___Constructor MemoryCache: "
                            + devices[0].getAddressAsString() + " num " + devices.length);

      // loop through all of the devices in the array
      totalPages = 0;
      for (int dev=0; dev < owd.length; dev++)
      {
         // check to make sure each device can do Overdrive
         if (owd[dev].getMaxSpeed() != DSPortAdapter.SPEED_OVERDRIVE)
            autoOverdrive = false;

         // record the start page offset for each device
         startPages[dev] = totalPages;

         // enumerate through the memory banks and collect the
         // general purpose banks in a vector
         for (Enumeration bank_enum = owd[dev].getMemoryBanks();
                 bank_enum.hasMoreElements(); )
         {
            // get the next memory bank
            MemoryBank mb = (MemoryBank) bank_enum.nextElement();

            // look for pbm memory bank (used in file structure)
            if (mb.isWriteOnce() && !mb.isGeneralPurposeMemory()
                    && mb.isNonVolatile() && (mb instanceof OTPMemoryBank))
            {
               // if more then 1 device with a OTP then error
               if (owd.length > 1)
               {
                  totalPages = 0;
                  return;
               }

               // If only 128 bytes then have DS2502 or DS2406 which have bitmap included
               // in the only status page.  All other EPROM devices have a special memory
               // bank that has 'Bitmap' in the title.
               if ((mem_size == 128)
                       || (mb.getBankDescription().indexOf("Bitmap") != -1))
               {
                  pbmBank = (OTPMemoryBank) mb;

                  if (mem_size == 128)
                     pbmBitOffset = 4;

                  pbmByteOffset  = 0;
                  canRedirect = true;

                  //\\//\\//\\//\\//\\//\\//\\//
                  if (doDebugMessages)
                     System.out.println("_Paged BitMap MemoryBank: "
                                        + mb.getBankDescription()
                                        + " with bit offset " + pbmBitOffset);
               }
            }

            // check regular memory bank
            if (!mb.isGeneralPurposeMemory() ||!mb.isNonVolatile()
                    ||!(mb instanceof PagedMemoryBank))
               continue;

            //\\//\\//\\//\\//\\//\\//\\//
            if (doDebugMessages)
               System.out.println("_Using MemoryBank: "
                                  + mb.getBankDescription());

            banks.addElement(mb);
            mem_size   += mb.getSize();
            totalPages += ((PagedMemoryBank)mb).getNumberPages();
         }
      }

      // count total bankPages
      bankPages  = new int [banks.size()];
      totalPages = 0;

      for (int b = 0; b < banks.size(); b++)
      {
         pmb           = (PagedMemoryBank) banks.elementAt(b);
         bankPages [b] = pmb.getNumberPages();
         totalPages    += bankPages [b];
      }

      // create the cache
      len                 = new int [totalPages];
      pageState           = new int [totalPages];
      writeLog            = new int [totalPages];
      redirect            = new int [totalPages];
      if (pmb != null)
      {
         maxPacketDataLength = pmb.getMaxPacketDataLength();
         cache               = new byte [totalPages][pmb.getPageLength()];
         tempPage            = new byte [pmb.getPageLength()];
      }

      // initialize some of the flag arrays
      for (int p = 0; p < totalPages; p++)
      {
         pageState [p]  = NOT_READ;
         len [p]      = 0;
         writeLog [p] = EMPTY;
      }

      // if getting redirection information, create necessarey arrays
      if (canRedirect)
      {
         tempExtra        = new byte [pmb.getExtraInfoLength()];
         pbmCache         = new byte [pbmBank.getSize()];
         pbmCacheModified = new byte [pbmBank.getSize()];
         pbmRead          = false;
      }
      else
         pbmRead = true;

      //\\//\\//\\//\\//\\//\\//\\//
      if (doDebugMessages)
         System.out.println("_Total Pages: " + totalPages
                            + ", get Redirection = " + canRedirect);
   }

   /**
    * Gets the number of pages in this cache
    *
    * @return number of pages in the cache
    */
   public int getNumberPages()
   {
      return totalPages;
   }

   /**
    * Gets the number of pages in the specified bank number
    *
    * @param bankNum bank number to retrieve number of pages
    *
    * @return number of pages in the bank
    */
   public int getNumberPagesInBank(int bankNum)
   {
      if (totalPages > 0)
         return bankPages[bankNum];
      else
         return 0;
   }

   /**
    * Gets the page number of the first page on the specified
    * device.  If the device number is not valid then return 0.
    *
    * @param deviceNum device number to retrieve page offset
    *
    * @return page number of first page on device
    */
   public int getPageOffsetForDevice(int deviceNum)
   {
      return startPages[deviceNum];
   }

   /**
    * Gets the maximum number of bytes for data in each page.
    *
    * @return max number of data bytes per page
    */
   public int getMaxPacketDataLength()
   {
      return maxPacketDataLength;
   }

   /**
    * Check if this memory device is write-once.  If this is true then
    * the page bitmap facilities in this class will be used.
    *
    * @return true if this device is write-once
    */
   public boolean isWriteOnce()
   {
      return canRedirect;
   }

   /**
    * Read a page packet.  If the page is available in the cache
    * then return that data.
    *
    * @param page  page to read
    * @param readBuf buffer to place the data in
    * @param offset offset into the read buffer
    *
    * @return the number byte in the packet
    *
    * @throws OneWireException when the adapter is not setup properly
    * @throws OneWireIOException when an 1-Wire IO error occures
    */
   public int readPagePacket(int page, byte[] readBuf, int offset)
      throws OneWireIOException, OneWireException
   {

      //\\//\\//\\//\\//\\//\\//\\//
      if (doDebugMessages)
         System.out.println("___readPagePacket (" + page + ") ");

      // check if have a cache (any memory banks)
      if (totalPages == 0)
         throw new OneWireException("1-Wire Filesystem does not have memory");

      // check if out of range
      if (page >= totalPages)
         throw new OneWireException("Page requested is not in memory space");

      // check if doing autoOverdrive (greatly improves multi-device cache speed)
      if (autoOverdrive)
      {
         autoOverdrive = false;
         DSPortAdapter adapter = owd[0].getAdapter();
         adapter.setSpeed(DSPortAdapter.SPEED_REGULAR);
         adapter.reset();
         adapter.putByte(( byte ) 0x3C);
         adapter.setSpeed(DSPortAdapter.SPEED_OVERDRIVE);
      }

      // check if need to read the page bitmap for the first time
      if (!pbmRead)
         readPageBitMap();

      // page NOT cached (maybe redirected)
      if ((pageState[page] == NOT_READ) || (pageState[page] == READ_NO_CRC) ||
          (redirect [page] != 0))
      {

         //\\//\\//\\//\\//\\//\\//\\//
         if (doDebugMessages)
            System.out.println("_Not in cache or redirected, length="
                               + len [page] + " redirect=" + redirect [page]);

         // page not cached, so read it
         int local_page = getLocalPage(page);
         PagedMemoryBank pmb = getMemoryBankForPage(page);
         int local_device_page = page - startPages[getDeviceIndex(page)];

         //\\//\\//\\//\\//\\//\\//\\//
         if (doDebugMessages)
            System.out.println("_Look in MemoryBank "
                               + pmb.getBankDescription());

         if (canRedirect)
         {
            // don't use multi-bank page reference (would not work with redirect)

            // loop while page is redirected
            int loopcnt = 0;
            for (;;)
            {
               // check for redirection
               if (redirect [page] == 0)
               {
                  // check if already in cache
                  if ((pageState[page] == READ_CRC) || (pageState[page] == VERIFY) ||
                      (pageState[page] == WRITE))
                     break;

                  // read the page with device generated CRC
                  if (pmb.hasExtraInfo())
                  {
                     pmb.readPageCRC(page, (lastPageRead == (page - 1)),
                                     cache [page], 0, tempExtra);

                     // set the last page read
                     lastPageRead = page;

                     // get the redirection byte
                     redirect [page] = ~tempExtra [0] & 0x00FF;
                  }
                  // OTP device that does not give redirect as extra info (DS1982/DS2502)
                  else
                  {
                     pmb.readPageCRC(page, (lastPageRead == (page - 1)),
                                     cache [page], 0);

                     // get the redirection
                     redirect[page] = (byte)(((OTPMemoryBank)pmb).getRedirectedPage(page));

                     // last page can't be used due to redirect read
                     lastPageRead = NONE;
                  }

                  // set the page state
                  pageState[page] = READ_NO_CRC;

                  //\\//\\//\\//\\//\\//\\//\\//
                  if (doDebugMessages)
                     System.out.println("_Page: " + page + "->"
                                        + redirect [page] + " local " + local_page
                                        + " with packet length byte "
                                        + (cache[page][0] & 0x00FF));

                  // not redirected so look at packet
                  if (redirect [page] == 0)
                  {
                     // check if length is realistic
                     if ((cache [page][0] & 0x00FF) > maxPacketDataLength)
                        throw new OneWireIOException(
                           "Invalid length in packet");

                     // verify the CRC is correct
                     if (CRC16.compute(cache [page], 0, cache [page][0]
                                       + 3, page) == 0x0000B001)
                     {
                        // get the length
                        len [page] = cache [page][0];

                        // set the page state
                        pageState[page] = READ_CRC;

                        break;
                     }
                     else
                        throw new OneWireIOException(
                           "Invalid CRC16 in packet read " + page);
                  }
               }
               else
                  page = redirect [page];

               // check for looping redirection
               if (loopcnt++ > totalPages)
                  throw new OneWireIOException(
                     "Circular redirection of pages");
            }

            //\\//\\//\\//\\//\\//\\//\\//
            if (doDebugMessages)
            {
               System.out.print("_Data found (" + len [page] + "):");
               debugDump(cache [page], 1, len [page]);
            }

            // get copy of data for caller
            if (readBuf != null)
               System.arraycopy(cache [page], 1, readBuf, offset,
                                len [page]);

            return len [page];
         }
         // not an EPROM
         else
         {
            // loop if get a crc error in packet data until get same data twice
            for (;;)
            {
               pmb.readPage(local_page, (lastPageRead == (page - 1)),
                            tempPage, 0);

               //\\//\\//\\//\\//\\//\\//\\//
               if (doDebugMessages)
                  System.out.println("_Page: " + page
                                     + " translates to "
                                     + local_page + " or device page " +
                                     local_device_page);

               // set the last page read
               lastPageRead = page;

               // verify length is realistic
               if ((tempPage [0] & 0x00FF) <= maxPacketDataLength)
               {

                  // verify the CRC is correct
                  if (CRC16.compute(tempPage, 0, tempPage [0]
                                    + 3, local_device_page) == 0x0000B001)
                  {

                     // valid data so put into cache
                     System.arraycopy(tempPage, 0, cache [page], 0,
                                      tempPage.length);

                     // get the length
                     len [page] = tempPage [0];

                     // set the page state
                     pageState[page] = READ_CRC;

                     break;
                  }
               }

               //\\//\\//\\//\\//\\//\\//\\//
               if (doDebugMessages)
               {
                  System.out.print("_Invalid CRC, raw: ");
                  debugDump(tempPage, 0, tempPage.length);
               }

               // must have been invalid packet
               // compare with data currently in the cache
               boolean same_data = true;

               for (int i = 0; i < tempPage.length; i++)
               {
                  if ((tempPage [i] & 0x00FF)
                          != (cache [page][i] & 0x00FF))
                  {

                     //\\//\\//\\//\\//\\//\\//\\//
                     if (doDebugMessages)
                        System.out.println("_Differenet at position="
                                           + i);

                     same_data = false;

                     break;
                  }
               }

               // if the same then throw the exception, else loop again
               if (same_data)
               {
                  // set the page state
                  pageState[page] = READ_NO_CRC;

                  throw new OneWireIOException(
                     "Invalid CRC16 in packet read");
               }
               else
                  System.arraycopy(tempPage, 0, cache [page], 0,
                                   tempPage.length);
            }
         }

         //\\//\\//\\//\\//\\//\\//\\//
         if (doDebugMessages)
         {
            System.out.print("_Data found (" + len [page] + "):");
            debugDump(cache [page], 1, len [page]);
         }

         // get copy of data for caller
         if (readBuf != null)
            System.arraycopy(cache [page], 1, readBuf, offset,
                             len [page]);

         return len [page];
      }
      // page IS cached (READ_CRC, VERIFY, WRITE)
      else
      {
         //\\//\\//\\//\\//\\//\\//\\//
         if (doDebugMessages)
         {
            System.out.print("_In cache (" + len [page] + "):");
            debugDump(cache [page], 1, len [page]);
         }

         // get from cache
         if (readBuf != null)
            System.arraycopy(cache [page], 1, readBuf, offset, len [page]);

         return len [page];
      }
   }

   /**
    * Write a page packet into the cache.
    *
    * @param page  page to write
    * @param writeBuf buffer container the data to write
    * @param offset offset into write buffer
    * @param buflen length of data to write
    */
   public void writePagePacket(int page, byte[] writeBuf, int offset,
                               int buflen)
      throws OneWireIOException, OneWireException
   {
      int log;

      //\\//\\//\\//\\//\\//\\//\\//
      if (doDebugMessages)
      {
         System.out.print("___writePagePacket on page " + page
                          + " with data (" + buflen + "): ");
         debugDump(writeBuf, offset, buflen);
      }

      // check if have a cache (any memory banks)
      if (totalPages == 0)
         throw new OneWireException("1-Wire Filesystem does not have memory");

      // check if need to read the page bitmap for the first time
      if (!pbmRead)
         readPageBitMap();

      // OTP device
      if (canRedirect)
      {
         // get reference to memory bank
         OTPMemoryBank otp = (OTPMemoryBank)getMemoryBankForPage(page);

         // check redirectoin if writing to a page that has not been read
         if ((redirect[page] == 0) && (pageState[page] == NOT_READ))
            redirect[page] = otp.getRedirectedPage(page);

         // check if page to write to is already redirected
         if (redirect[page] != 0)
         {
            // loop to find the end of the redirect chain
            int last_page = page, cnt = 0;
            lastPageRead = NONE;
            do
            {
               last_page = redirect[last_page];

               redirect[last_page] = otp.getRedirectedPage(last_page);

               if (cnt++ > totalPages)
                  throw new OneWireException("Error in Filesystem, circular redirection of pages");
            }
            while (redirect[last_page] != 0);

            //\\//\\//\\//\\//\\//\\//\\//
            if (doDebugMessages)
               System.out.print("___redirection chain ended on page " + last_page);

            // Use the last_page since it was not redirected
            System.arraycopy(writeBuf, offset, cache[last_page], 1, buflen);
            len [last_page]      = buflen;
            cache [last_page][0] = (byte) buflen;
            int crc = CRC16.compute(cache[last_page], 0, buflen + 1, last_page);
            cache[last_page][buflen+1] = ( byte ) (~crc & 0xFF);
            cache[last_page][buflen+2] = ( byte ) (((~crc & 0xFFFF) >>> 8) & 0xFF);

            // set pageState flag
            pageState [last_page] = VERIFY;

            // change page to last_page to be used in writeLog
            page = last_page;
         }
         else
         {
            // Use the page since it is not redirected
            System.arraycopy(writeBuf, offset, cache[page], 1, buflen);
            len [page]      = buflen;
            cache [page][0] = (byte) buflen;
            int crc = CRC16.compute(cache[page], 0, buflen + 1, page);
            cache[page][buflen+1] = ( byte ) (~crc & 0xFF);
            cache[page][buflen+2] = ( byte ) (((~crc & 0xFFFF) >>> 8) & 0xFF);

            // set pageState flag
            pageState [page] = VERIFY;
         }
      }
      // NON-OTP device
      else
      {
         // put in cache
         System.arraycopy(writeBuf, offset, cache [page], 1, buflen);

         len [page]      = buflen;
         cache [page][0] = (byte) buflen;

         // set pageState flag
         pageState [page] = WRITE;
      }

      // record write in log
      // search the write log until find 'page' or EMPTY
      for (log = 0; log < totalPages; log++)
      {
         if ((writeLog [log] == page) || (writeLog [log] == EMPTY))
            break;
      }

      // shift write log down 1 to 'log'
      for (; log > 0; log--)
         writeLog [log] = writeLog [log - 1];

      // add page at top
      writeLog [0] = page;
   }

   /**
    * Flush the pages written back to the 1-Wire device.
    *
    * @throws OneWireException when the adapter is not setup properly
    * @throws OneWireIOException when an 1-Wire IO error occures
    */
   public void sync()
      throws OneWireIOException, OneWireException
   {
      int page, log;

      //\\//\\//\\//\\//\\//\\//\\//
      if (doDebugMessages)
         System.out.println("___sync");

      // check if have a cache (any memory banks)
      if (totalPages == 0)
         return;

      // loop until all jobs complete
      boolean jobs;
      do
      {
         jobs = false;

         // loop through write log and write the oldest pages first
         for (log = totalPages - 1; log >= 0; log--)
         {
            // check if this is a valid log entry
            if (writeLog [log] != EMPTY)
            {

               // this was not empty so there is a job
               jobs = true;

               // get page number to write
               page = writeLog [log];

               //\\//\\//\\//\\//\\//\\//\\//
               if (doDebugMessages)
                  System.out.println("_page " + page + " in log " + log + " is not empty, pageState: " + pageState[page]);

               // get the memory bank
               PagedMemoryBank pmb = (PagedMemoryBank)getMemoryBankForPage(page);

               // get the local page number
               int local_page = getLocalPage(page);

               // Verify operation (only in EPROM operations)
               if (pageState[page] == VERIFY)
               {
                  //\\//\\//\\//\\//\\//\\//\\//
                  if (doDebugMessages)
                     System.out.println("_verify page " + page);

                  // read the page with device generated CRC
                  pmb.readPageCRC(page, (lastPageRead == (page - 1)),
                                  tempPage, 0);

                  // set the last page read
                  lastPageRead = page;

                  //\\//\\//\\//\\//\\//\\//\\//
                  if (doDebugMessages)
                  {
                     System.out.print("_Desired data: ");
                     debugDump(cache [page], 0, cache[page].length);
                     System.out.print("_Current data: ");
                     debugDump(tempPage, 0, tempPage.length);
                     System.out.println("_len " + len[page]);
                  }

                  // check to see if the desired data can be written here
                  boolean do_redirect = false;
                  for (int i = 1; i < (len[page] + 2); i++)
                  {
                     if ((((tempPage[i] & 0x00FF) ^ (cache[page][i] & 0x00FF)) &
                           (~tempPage[i] & 0x00FF)) > 0)
                     {
                        // error, data already on device, must redirect
                        do_redirect = true;
                        break;
                     }
                  }

                  // need to redirect
                  if (do_redirect)
                  {
                     //\\//\\//\\//\\//\\//\\//\\//
                     if (doDebugMessages)
                        System.out.println("_page is occupied with conflicting data, must redirect");

                     // find a new page, set VERIFY job there
                     // get the next available page
                     int new_page = getFirstFreePage();
                     while (new_page == page)
                     {
                        System.out.println("_can't use this page " + page);
                        markPageUsed(new_page);
                        new_page = getNextFreePage();
                     }

                     // verify got a free page
                     if (new_page < 0)
                        throw new OneWireException("Redireciton required but out of space on 1-Wire device");

                     // mark page used
                     markPageUsed(new_page);

                     // put the data in the new page and setup the job
                     System.arraycopy(cache[page], 0, cache[new_page], 0, tempPage.length);
                     pageState[new_page] = VERIFY;
                     len[new_page] = len[page];

                     // add to write log
                     for (int i = 0; i < totalPages; i++)
                     {
                        if (writeLog[i] == EMPTY)
                        {
                           writeLog[i] = new_page;
                           break;
                        }
                     }

                     // set old page for redirect
                     pageState[page] = REDIRECT;
                     cache[page][0] = (byte)(new_page & 0xFF);
                  }
                  // verify passed
                  else
                     pageState[page] = WRITE;
               }

               // Redirect operation
               if (pageState[page] == REDIRECT)
               {
                  //\\//\\//\\//\\//\\//\\//\\//
                  if (doDebugMessages)
                     System.out.println("_redirecting page " + page + " to " + (cache[page][0] & 0x00FF));

                  // redirect the page (new page located in first byte of cache)
                  ((OTPMemoryBank)pmb).redirectPage(page, cache[page][0] & 0x00FF);

                  // clear the redirect job
                  pageState [page] = NOT_READ;
                  lastPageRead   = NONE;
                  writeLog [log] = EMPTY;
               }

               // Write operation
               if (pageState [page] == WRITE)
               {
                  //\\//\\//\\//\\//\\//\\//\\//
                  if (doDebugMessages)
                  {
                     System.out.print("_write page " + page + " with data ("
                                      + len [page] + "): ");
                     debugDump(cache [page], 1, len [page]);
                  }

                  // check for new device, make sure it is at the correct speed
                  int new_index = getDeviceIndex(page);
                  if (new_index != lastDevice)
                  {
                     //\\//\\//\\//\\//\\//\\//\\//
                     if (doDebugMessages)
                        System.out.print("(" + new_index + ")");

                     lastDevice = new_index;
                     owd[lastDevice].doSpeed();
                  }

                  // write the page
                  pmb.writePagePacket(local_page, cache[page], 1, len[page]);

                  // clear pageState flag
                  pageState [page] = READ_CRC;
                  lastPageRead   = NONE;
                  writeLog [log] = EMPTY;
               }
            }
         }
      }
      while (jobs);

      // write the bitmap of used pages for OTP device
      if (canRedirect)
      {
         // make a buffer that contains only then new '0' bits in the bitmap
         // required to not overprogram any bits
         int numBytes = totalPages / 8;
         if (numBytes == 0)
            numBytes = 1;
         boolean changed = false;
         byte[] temp_buf = new byte[numBytes];

         for (int i = 0; i < numBytes; i++)
         {
            temp_buf[i] = (byte)(~(pbmCache[i] ^ pbmCacheModified[i]) & 0x00FF);
            if ((byte)temp_buf[i] != (byte)0xFF)
               changed = true;
         }

         //\\//\\//\\//\\//\\//\\//\\//
         if (doDebugMessages)
         {
            System.out.print("_device bitmap: " );
            debugDump(pbmCache, 0, pbmCache.length);
            System.out.print("_modified bitmap: " );
            debugDump(pbmCacheModified, 0, pbmCacheModified.length);
            System.out.print("_page bitmap to write, changed: " + changed + "   ");
            debugDump(temp_buf, 0, temp_buf.length);
         }

         // write if changed
         if (changed)
         {
            //\\//\\//\\//\\//\\//\\//\\//
            if (doDebugMessages)
               System.out.println("_writing page bitmap");

            // turn off read-back verification
            pbmBank.setWriteVerification(false);

            // write buffer
            pbmBank.write(0, temp_buf, 0, numBytes);

            // readback to make sure that it matches pbmCacheModified
            pbmBank.read(0, false, temp_buf, 0, numBytes);
            for (int i = 0; i < numBytes; i++)
            {
               if ((temp_buf[i] & 0x00FF) != (pbmCacheModified[i] & 0x00FF))
                  throw new OneWireException("Readback verfication of page bitmap was not correct");
            }

            // put new value of bitmap pbmCache
            System.arraycopy(temp_buf, 0, pbmCache, 0, numBytes);
            System.arraycopy(temp_buf, 0, pbmCacheModified, 0, numBytes);
         }
      }
   }

   //--------
   //-------- Owner tracking methods
   //--------

   /**
    * Add an owner to this memory cache.  This will be tracked
    * for later cleanup.
    *
    * @param tobj owner of instance
    */
   public void addOwner(Object tobj)
   {

      //\\//\\//\\//\\//\\//\\//\\//
      if (doDebugMessages)
         System.out.println("___addOwner");

      if (owners.indexOf(tobj) == -1)
      {
         owners.addElement(tobj);
      }
   }

   /**
    * Remove the specified owner of this memory cache.
    *
    * @param tobj owner of instance
    */
   public void removeOwner(Object tobj)
   {

      //\\//\\//\\//\\//\\//\\//\\//
      if (doDebugMessages)
         System.out.println("___removeOwner");

      owners.removeElement(tobj);
   }

   /**
    * Check to see if there on no owners of this memory cache.
    *
    * @return true if not owners of this memory cache
    */
   public boolean noOwners()
   {

      //\\//\\//\\//\\//\\//\\//\\//
      if (doDebugMessages)
         System.out.println("___noOwners = " + owners.isEmpty());

      return owners.isEmpty();
   }

   //--------
   //-------- Write file tracking methods
   //--------

   /**
    * Remove the provided filePath from the list of files
    * currently opened to write.
    *
    * @param filePath file to remove from write list
    */
   public void removeWriteOpen(String filePath)
   {
      int index = openedToWrite.indexOf(filePath);

      if (index != -1)
         openedToWrite.removeElementAt(index);
   }

   /**
    * Check to see if the provided filePath is currently opened
    * to write.  Optionally add it to the list if it not already
    * there.
    *
    * @param filePath  file to check to see if opened to write
    * @param addToList true to add file to list if not present
    *
    * @return true if file was not in the opened to write list
    */
   public boolean isOpenedToWrite(String filePath, boolean addToList)
   {
      int index = openedToWrite.indexOf(filePath);

      if (index != -1)
         return true;
      else
      {
         if (addToList)
            openedToWrite.addElement(filePath);
         return false;
      }
   }

   //--------
   //-------- Page-Bitmap methods
   //--------

   /**
    * Check to see if this memory cache should handle the page bitmap.
    *
    * @return true if this memory cache should handle the page bitmap
    */
   public boolean handlePageBitmap()
   {
      return !(pbmBank == null);
   }

   /**
    * Mark the specified page as used in the page bitmap.
    *
    * @param page number to mark as used
    */
   public void markPageUsed(int page)
   {
      //\\//\\//\\//\\//\\//\\//\\//
      if (doDebugMessages)
         System.out.println("___markPageUsed " + page);

      // mark page used in cached bitmap of used pages
      Bit.arrayWriteBit(USED, pbmBitOffset + page, pbmByteOffset,
                        pbmCacheModified);
   }

   /**
    * free the specified page as being un-used in the page bitmap
    *
    * @param page number to mark as un-used
    *
    * @return true if the page as be been marked as un-used, false
    *      if the page is on an OTP device and cannot be freed
    */
   public boolean freePage(int page)
   {

      //\\//\\//\\//\\//\\//\\//\\//
      if (doDebugMessages)
         System.out.print("___freePage " + page);

      // only free pages that have been written to cache
      // but not flushed to device
      if (Bit.arrayReadBit(pbmBitOffset + page, pbmByteOffset, pbmCache)
              == NOT_USED)
      {
         Bit.arrayWriteBit(NOT_USED, pbmBitOffset + page, pbmByteOffset,
                           pbmCacheModified);

         //\\//\\//\\//\\//\\//\\//\\//
         if (doDebugMessages)
            System.out.println("_ was cached so really free now ");

         return true;
      }
      else
      {

         //\\//\\//\\//\\//\\//\\//\\//
         if (doDebugMessages)
            System.out.println("_ not cached so not free");

         return false;
      }
   }

   /**
    * Get the first free page from the page bitmap.
    *
    * @return first page number that is free to write
    */
   public int getFirstFreePage()
   {

      //\\//\\//\\//\\//\\//\\//\\//
      if (doDebugMessages)
         System.out.print("___getFirstFreePage ");

      lastFreePage = 0;

      return getNextFreePage();
   }

   /**
    * Get the next free page from the page bitmap.
    *
    * @return next page number that is free to write
    */
   public int getNextFreePage()
   {
      for (int pg = lastFreePage; pg < totalPages; pg++)
      {
         if (Bit.arrayReadBit(
                 pbmBitOffset
                 + pg, pbmByteOffset, pbmCacheModified) == NOT_USED)
         {

            //\\//\\//\\//\\//\\//\\//\\//
            if (doDebugMessages)
               System.out.println("___getNextFreePage " + pg);

            lastFreePage = pg + 1;

            return pg;
         }
      }

      //\\//\\//\\//\\//\\//\\//\\//
      if (doDebugMessages)
         System.out.println("___getNextFreePage, no free pages ");

      return -1;
   }

   /**
    * Get the total number of free pages in this Filesystem.
    *
    * @return number of pages free
    *
    * @throws OneWireException when an IO exception occurs
    */
   public int getNumberFreePages()
      throws OneWireException
   {
      // check if need to read the page bitmap for the first time
      if (!pbmRead)
      {
         // read the pbm
         pbmBank.read(0, false, pbmCache, 0, pbmCache.length);

         // make a copy of it
         System.arraycopy(pbmCache, 0, pbmCacheModified, 0, pbmCache.length);

         // mark as read
         pbmRead = true;

         //\\//\\//\\//\\//\\//\\//\\//
         if (doDebugMessages)
         {
            System.out.print("_Page bitmap read in getNumberFreePages: ");
            debugDump(pbmCache, 0, pbmCache.length);
         }
      }

      int free_pages = 0;
      for (int pg = 0; pg < totalPages; pg++)
      {
         if (Bit.arrayReadBit(
                 pbmBitOffset
                 + pg, pbmByteOffset, pbmCacheModified) == NOT_USED)
            free_pages++;
      }

      //\\//\\//\\//\\//\\//\\//\\//
      if (doDebugMessages)
         System.out.println("___getNumberFreePages = " + free_pages);

      return free_pages;
   }

   /**
    * Gets the page number used in the remote page bitmap in an OTP device.
    *
    * @return page number used in the directory for the remote page bitmap
    */
   public int getBitMapPageNumber()
   {
      return (pbmBank.getStartPhysicalAddress() / pbmBank.getPageLength());
   }

   /**
    * Get the number of pages used for the remote page bitmap in an
    * OTP device.
    *
    * @return number of pages used in page bitmap
    */
   public int getBitMapNumberOfPages()
   {
      return ((totalPages / 8) / pbmBank.getPageLength());
   }

   /**
    * Get's the memory bank object for the specified page.
    * This is significant if the Filesystem spans memory banks
    * on the same or different devices.
    */
   public PagedMemoryBank getMemoryBankForPage(int page)
   {
      int cnt=0;

      for (int bank_num = 0; bank_num < banks.size(); bank_num++)
      {
         // check if 'page' is in this memory bank
         if ((cnt + bankPages[bank_num]) > page)
            return (PagedMemoryBank) banks.elementAt(bank_num);

         cnt += bankPages[bank_num];
      }

      // page provided is not in this Filesystem
      return null;
   }

   /**
    * Get's the index into the array of Devices where this page
    * resides.
    * This is significant if the Filesystem spans memory banks
    * on the same or different devices.
    */
   private int getDeviceIndex(int page)
   {
      for (int dev_num = (startPages.length - 1); dev_num >= 0; dev_num--)
      {
         // check if 'page' is in this memory bank
         if (startPages[dev_num] < page)
            return dev_num;
      }

      // page provided is not in this Filesystem
      return 0;
   }

   /**
    * Get's the local page number on the memory bank object for
    * the specified page.
    * This is significant if the Filesystem spans memory banks
    * on the same or different devices.
    */
   public int getLocalPage(int page)
   {
      int cnt=0;

      for (int bank_num = 0; bank_num < banks.size(); bank_num++)
      {
         // check if 'page' is in this memory bank
         if ((cnt + bankPages[bank_num]) > page)
            return (page - cnt);

         cnt += bankPages[bank_num];
      }

      // page provided is not in this Filesystem
      return 0;
   }

   /**
    * Clears the lastPageRead global so that a readPage will
    * not try to continue where the last page left off.
    * This should be called anytime exclusive access to the
    * 1-Wire canot be guaranteed.
    */
   public void clearLastPageRead()
   {
      // last page can't be used due to redirect read
      lastPageRead = NONE;
   }

   /**
    * Read the page bitmap.
    *
    * @throws OneWireException when an IO exception occurs
    */
   private void readPageBitMap()
      throws OneWireException
   {
      // read the pbm
      pbmBank.read(0, false, pbmCache, 0, pbmCache.length);

      // make a copy of it
      System.arraycopy(pbmCache, 0, pbmCacheModified, 0, pbmCache.length);

      // mark as read
      pbmRead = true;

      //\\//\\//\\//\\//\\//\\//\\//
      if (doDebugMessages)
      {
         System.out.print("____Page bitmap read: ");
         debugDump(pbmCache, 0, pbmCache.length);
      }
   }


   //--------
   //-------- Misc Utility Methods
   //--------

   /**
    * Debug dump utility method
    *
    * @param buf buffer to dump
    * @param offset offset to start in the buffer
    * @param len length to dump
    */
   private void debugDump(byte[] buf, int offset, int len)
   {
      for (int i = offset; i < (offset + len); i++)
      {
         System.out.print(Integer.toHexString((int) buf [i] & 0x00FF) + " ");
      }

      System.out.println();
   }
}
