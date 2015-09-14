/*---------------------------------------------------------------------------
 * Copyright (C) 1999-2001 Dallas Semiconductor Corporation, All Rights Reserved.
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

package com.dalsemi.onewire.application.sha;

import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.container.OneWireContainer18;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.utils.IOHelper;

/**
 * <P>Class representing DS1963S (or DS2421), family-code 0x18, SHA iButtons as a user
 * token in SHA Transactions.</P>
 *
 * @see SHATransaction
 * @see SHAiButtonCopr
 * @see SHAiButtonUser33
 *
 * @version 1.00
 * @author  SKH
 */
public class SHAiButtonUser18
   extends SHAiButtonUser
{
   /**
    * Reference to the OneWireContainer
    */
   protected OneWireContainer18 ibc = null;

   /**
    * <P>No default constructor for user apps.  At bare minimum, you need
    * a reference to a <code>SHAiButtonCopr</code> before you can construct a
    * <code>SHAiButtonUser</code>.</P>
    *
    * @see #SHAiButtonUser18(SHAiButtonCopr,OneWireContainer18,boolean,byte[])
    * @see #SHAiButtonUser18(SHAiButtonCopr,OneWireContainer18)
    * @see #SHAiButtonUser18(SHAiButtonCopr)
    */
   protected SHAiButtonUser18()
   {;}

   /**
    * <P>Initialize a DS1963S as a fresh user iButton for a given SHA service.
    * This constructor not only creates the service file for the user iButton
    * using the TMEX file structure, but it also installs the master
    * authentication secret and binds it to the iButton (making it unique for
    * a particular button).  Optionally, the device can be formatted before
    * the service file is installed.</P>
    *
    * @param copr The SHAiButtonCopr to which the user object is tied.  This
    *        Coprocessor contains the necessary binding code and service
    *        filename, necessary for both locating a user and recreating his
    *        unique secret.
    * @param owc The DS1963S iButton that this object will refer to.
    * @param formatDevice If <code>true</code>, the TMEX filesystem will be
    *        formatted before the account service file is created.
    * @param authSecret The master authentication secret for the systm.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #SHAiButtonUser18(SHAiButtonCopr,OneWireContainer18)
    * @see #SHAiButtonUser18(SHAiButtonCopr)
    */
   public SHAiButtonUser18(SHAiButtonCopr copr, OneWireContainer18 owc,
                           boolean formatDevice, byte[] authSecret)
      throws OneWireException, OneWireIOException
   {
      //setup service filename
      this(copr);

      //hold container reference
      this.ibc = owc;

      //and address
      this.address = owc.getAddress();

      if(!createServiceFile(owc, strServiceFilename, formatDevice))
         throw new OneWireException("Failed to create service file.");

      //save a copy of the binding code
      copr.getBindCode(this.fullBindCode,0);
      System.arraycopy(this.fullBindCode,4,
                       this.fullBindCode,12, 3);

      //setup the fullBindCode with rest of info
      this.fullBindCode[4] = (byte)this.accountPageNumber;
      System.arraycopy(this.address,0,
                       this.fullBindCode,5,7);

      if(!owc.installMasterSecret(accountPageNumber,
                                  authSecret,
                                  accountPageNumber&7))
         throw new OneWireException("Install Master Secret failed");

      //not in critical path, so getBindBlah() is okay.
      if(!owc.bindSecretToiButton(accountPageNumber,
                                  copr.getBindData(), this.fullBindCode,
                                  accountPageNumber&7))
         throw new OneWireException("Bind Secret to iButton failed");

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------");
         IOHelper.writeLine("Initialized User");
         IOHelper.writeLine("address");
         IOHelper.writeBytesHex(owc.getAddress());
         IOHelper.writeLine("serviceFilename: " + strServiceFilename);
         IOHelper.writeLine("accountPageNumber: " + accountPageNumber);
         IOHelper.writeLine("authSecret");
         IOHelper.writeBytesHex(authSecret);
         IOHelper.writeLine("bindData");
         IOHelper.writeBytesHex(copr.bindData);
         IOHelper.writeLine("bindCode");
         IOHelper.writeBytesHex(copr.bindCode);
         IOHelper.writeLine("------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
   }

   /**
    * <P>Initialize a DS1963S as a fresh user iButton for a given SHA service.
    * This constructor not only creates the service file for the user iButton
    * using the TMEX file structure, but it also installs the master
    * authentication secret and binds it to the iButton (making it unique for
    * a particular button).  Optionally, the device can be formatted before
    * the service file is installed.</P>
    *
    * @param coprBindData The Coprocessor Bind Data, used to create a unique
    *        secret for this user token.
    * @param coprBindCode The Coprocessor Bind Code without the user-specific
    *        information, used to create a unique secret for this user token.
    * @param fileName The file name for the account info.
    * @param fileNameExt The file extenstion for the account info
    * @param owc The DS1963S iButton that this object will refer to.
    * @param formatDevice If <code>true</code>, the TMEX filesystem will be
    *        formatted before the account service file is created.
    * @param authSecret The master authentication secret for the systm.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #SHAiButtonUser18(SHAiButtonCopr,OneWireContainer18)
    * @see #SHAiButtonUser18(SHAiButtonCopr)
    */
   public SHAiButtonUser18(byte[] coprBindData, byte[] coprBindCode,
                           byte[] fileName, int fileNameExt,
                           OneWireContainer18 owc,
                           boolean formatDevice, byte[] authSecret)
      throws OneWireException, OneWireIOException
   {

      //hold container reference
      this.ibc = owc;

      //and address
      this.address = owc.getAddress();

      System.arraycopy(fileName, 0, this.serviceFile, 0, 4);
      //create string representation of service filename
      this.strServiceFilename = new String(fileName) + "."
                                + (int)fileNameExt;

      if(!createServiceFile(owc, strServiceFilename, formatDevice))
         throw new OneWireException("Failed to create service file.");

      //save a copy of the binding code
      System.arraycopy(coprBindCode, 0, this.fullBindCode, 0, 7);
      System.arraycopy(this.fullBindCode,4,
                       this.fullBindCode,12, 3);

      //setup the fullBindCode with rest of info
      this.fullBindCode[4] = (byte)this.accountPageNumber;
      System.arraycopy(this.address,0,
                       this.fullBindCode,5,7);

      if(!owc.installMasterSecret(accountPageNumber,
                                  authSecret,
                                  accountPageNumber&7))
         throw new OneWireException("Install Master Secret failed");

      //not in critical path, so getBindBlah() is okay.
      if(!owc.bindSecretToiButton(accountPageNumber,
                                  coprBindData, this.fullBindCode,
                                  accountPageNumber&7))
         throw new OneWireException("Bind Secret to iButton failed");

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------");
         IOHelper.writeLine("Initialized User");
         IOHelper.writeLine("address");
         IOHelper.writeBytesHex(owc.getAddress());
         IOHelper.writeLine("serviceFilename: " + strServiceFilename);
         IOHelper.writeLine("accountPageNumber: " + accountPageNumber);
         IOHelper.writeLine("authSecret");
         IOHelper.writeBytesHex(authSecret);
         IOHelper.writeLine("bindCode");
         IOHelper.writeBytesHex(coprBindCode);
         IOHelper.writeLine("------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
   }

   /**
    * <P>Creates a valid SHAiButtonUser object.  If the service file,
    * whose name is taken from the <code>SHAiButtonCopr</code>, is not
    * found on the user iButton, a OneWireException is thrown with the
    * message "Invalid SHA user".</P>
    *
    * @param copr The SHAiButtonCopr to which the user object is tied.  This
    *        Coprocessor contains the necessary binding code and service
    *        filename, necessary for both locating a user and recreating his
    *        unique secret.
    * @param owc The DS1963S iButton that this object will refer to.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #SHAiButtonUser18(SHAiButtonCopr,OneWireContainer18,boolean,byte[])
    * @see #SHAiButtonUser18(SHAiButtonCopr)
    */
   public SHAiButtonUser18(SHAiButtonCopr copr, OneWireContainer18 owc)
      throws OneWireException, OneWireIOException
   {
      //setup service filename
      this(copr);

      //hold container reference and address
      if(!setiButton18(owc))
         throw new OneWireException("Invalid SHA user");
   }

   /**
    * <P>Creates a valid SHAiButtonUser object.  If the service file,
    * whose name is taken from the <code>SHAiButtonCopr</code>, is not
    * found on the user iButton, a OneWireException is thrown with the
    * message "Invalid SHA user".</P>
    *
    * @param coprBindCode The Coprocessor Bind Code without the user-specific
    *        information.
    * @param fileName The file name of the account file
    * @param fileNameExt The file extenstion of the account file
    * @param owc The DS1963S iButton that this object will refer to.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #SHAiButtonUser18(SHAiButtonCopr,OneWireContainer18,boolean,byte[])
    * @see #SHAiButtonUser18(SHAiButtonCopr)
    */
   public SHAiButtonUser18(byte[] coprBindCode,
                           byte[] fileName, int fileNameExt,
                           OneWireContainer18 owc)
      throws OneWireException, OneWireIOException
   {
      //save a copy of the binding code
      System.arraycopy(coprBindCode, 0, this.fullBindCode, 0, 7);
      System.arraycopy(this.fullBindCode,4,
                       this.fullBindCode,12, 3);


      //create string representation of service filename
      System.arraycopy(fileName, 0, this.serviceFile, 0, 4);
      this.strServiceFilename = new String(fileName) + "."
                                + (int)fileNameExt;

      //hold container reference and address
      if(!setiButton18(owc))
         throw new OneWireException("Invalid SHA user");
   }

   /**
    * <P>Creates a mostly unitialized SHAiButtonUser object.  This constructor
    * merely copies the coprocessors 7 byte binding code into a local cache
    * and stores the name of the account service file used for all user
    * iButtons.</P>
    *
    * <P>Since this constructor leaves data unitialized, you should be very
    * careful with the use of it.  It is expected that after calling this
    * constructor, the user will call <code>setiButton</code> to finish the
    * initialization process.  On memory-starved platforms, this should help
    * optimize memory usage.</P>
    *
    * @param copr The SHAiButtonCopr to which the user object is tied.  This
    *        Coprocessor contains the necessary binding code and service
    *        filename, necessary for both locating a user and recreating his
    *        unique secret.
    *
    * @see #SHAiButtonUser18(SHAiButtonCopr,OneWireContainer18,boolean,byte[])
    * @see #SHAiButtonUser18(SHAiButtonCopr)
    */
   public SHAiButtonUser18(SHAiButtonCopr copr)
   {
      //save a copy of the binding code
      copr.getBindCode(this.fullBindCode,0);
      System.arraycopy(this.fullBindCode,4,
                       this.fullBindCode,12, 3);


      //create string representation of service filename
      copr.getFilename(this.serviceFile,0);
      this.strServiceFilename = new String(this.serviceFile) + "."
                                + (int)copr.getFilenameExt();
   }

   // ***********************************************************************
   // Modifier Methods
   //  - setiButton is the only essential modifier.  It updates all
   //    data members based on consquences of the account file alone.
   // ***********************************************************************

   /**
    * <P>Modifies this SHA iButton so that it refers to another DS1963S
    * container.  This function only copies the reference to the
    * OneWireContainer, copes the reference to it's 1-Wire address, and
    * then asserts that the iButton contains a valid acccount info file
    * associated with the system.</P>
    *
    * @param owc The <code>OneWireContainer18</code> this object will refer to.
    *
    * @return <code>true</code> if a valid account service file exists on
    *         this <code>OneWireContainer18</code>.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public synchronized boolean setiButton18(OneWireContainer18 owc)
      throws OneWireException, OneWireIOException
   {
      //hold container reference
      this.ibc = owc;

      //and address
      this.address = owc.getAddress(); //getAddress() doesn't malloc!

      //clear account information
      this.accountPageNumber = -1;

      //make sure account info is properly setup
      if(!checkAccountPageInfo(owc))
         return false;

      //setup the fullBindCode with rest of info
      this.fullBindCode[4] = (byte)this.accountPageNumber;
      System.arraycopy(this.address,0,
                       this.fullBindCode,5,7);


      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------");
         IOHelper.writeLine("Loaded User");
         IOHelper.writeLine("address");
         IOHelper.writeBytesHex(owc.getAddress());
         IOHelper.writeLine("accountPageNumber: " + accountPageNumber);
         IOHelper.writeLine("serviceFilename: " + strServiceFilename);
         IOHelper.writeLine("------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      return true;
   }

   /**
    * <P>Modifies this SHA iButton so that it refers to another 1963S.
    * If this object already has an appropriate instance of OneWireContainer,
    * that instance is updated with the new address.</P>
    *
    * @param adapter The adapter that the device can be found on.
    * @param address The address of the 1-Wire device
    *
    * @return <code>true</code> if a valid account service file exists on
    *         this <code>OneWireContainer18</code>.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public synchronized boolean setiButtonUser(DSPortAdapter adapter,
                                              byte[] address)
      throws OneWireException, OneWireIOException
   {
      if(this.ibc==null)
         this.ibc = new OneWireContainer18();

      this.ibc.setupContainer(adapter,address);

      if(this.forceOverdrive)
         this.ibc.setSpeed(DSPortAdapter.SPEED_OVERDRIVE, false);

      return setiButton18(this.ibc);
   }

   /**
    * <P>Modifies this SHA iButton so that it refers to another device.
    * If this object does not already has an appropriate instance of
    * OneWireContainer, it returns false immediately, because there is
    * no adapter info available.  Otherwise, it reuses the same adapter.</P>
    *
    * @param address The address of the 1-Wire device
    *
    * @return <code>true</code> if a valid account service file exists on
    *         this <code>OneWireContainer18</code>.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public synchronized boolean setiButtonUser(byte[] address)
      throws OneWireException, OneWireIOException
   {
      if(this.ibc==null)
         return false;

      this.ibc.setupContainer(this.ibc.getAdapter(),address);

      if(this.forceOverdrive)
         this.ibc.setSpeed(DSPortAdapter.SPEED_OVERDRIVE, false);

      return setiButton18(this.ibc);
   }

   // ***********************************************************************
   // End Modifier Methods
   // ***********************************************************************


   /**
    * <P>Returns the value of the write cycle counter for the
    * page where the account data is stored.  If the write
    * cycle counter has ever been retrieved, this returns the
    * cached value.  Otherwise, this method reads the value
    * from the part.</P>
    *
    * @return the value of the write cycle counter for the
    *         account data page.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public synchronized int getWriteCycleCounter()
      throws OneWireException, OneWireIOException
   {
      if(this.writeCycleCounter<0)
      {
         //need to get the writeCycleCounter.
         int wcc = -1;

         //don't worry, this should not happen in the critical path.
         //so malloc-ing doesnt really matter
         byte[] wcc_buffer = new byte[32];

         //read the page, 19, containing the write-cycle counters
         this.ibc.readMemoryPage(19, wcc_buffer, 0);

         //get the offset into page for wcc
         int offset = (this.accountPageNumber & 7) << 2;

         //convert the four bytes, lsb first, into an int
         wcc = (wcc_buffer[offset+3]&0x0ff);
         wcc = (wcc << 8) | (wcc_buffer[offset+2]&0x0ff);
         wcc = (wcc << 8) | (wcc_buffer[offset+1]&0x0ff);
         wcc = (wcc << 8) | (wcc_buffer[offset]&0x0ff);

         this.writeCycleCounter = wcc;
      }
      return this.writeCycleCounter;
   }

   /**
    * Returns <code>true</code> if this buttons account data is stored
    * on a page that has a write cycle counter.
    *
    * @return <code>true</code> if account page has write cycle counter.
    */
   public synchronized boolean hasWriteCycleCounter()
   {
      return (this.accountPageNumber>7);
   }

   /**
    * <P>This function creates the full 15-byte binding data for the
    * coprocessor to use to recreate this user's secret on the copr's
    * workspace page.  This function is located in the SHAiButtonUser
    * class to support binding codes for user buttons who use alternate
    * techniques (such as the DS1961S) for secret computation.</P>
    *
    * <P>For the DS1963S user iButton, the format of the full bind code is
    * as follows:
    *   <PRE>
    *      (bindCode+0), (bindCode+1), (bindCode+2), (bindCode+3),
    *      (svcPageNum), (deviceAN+0), (deviceAN+1), (deviceAN+2),
    *      (deviceAN+3), (deviceAN+4), (deviceAN+5), (deviceAN+6),
    *      (bindCode+4), (bindCode+5), (bindCode+6)
    *   </PRE></P>
    *
    * @param bindCode the 7-byte binding code from coprocessor's service file
    * @param fullBindCode the 15-byte full binding code to to be copied into
    *                     the coprocessor's scratchpad.  There should be 15
    *                     bytes available starting from the offset.
    * @param offset the offset into fullBindCode where copying should begin.
    */
   public void getFullBindCode(byte[] l_fullBindCode, int offset)
   {
      System.arraycopy(this.fullBindCode,0,
                       l_fullBindCode,offset,
                       15);
   }


   /**
    * <P>Returns a byte representing the appropriate authorization command
    * for the coprocessor to use to authenticate this user. For a DS1963S,
    * the authentication command is VALIDATE_PAGE.</P>
    *
    * @return byte indicating appropriate command for authenticating user
    *
    */
   public byte getAuthorizationCommand()
   {
      return OneWireContainer18.VALIDATE_DATA_PAGE;
   }


   /**
    * <P>Writes the account data to the SHAiButton.  First, this function
    * asserts that the account page number is known.  The account data is
    * copied from dataBuffer starting at the offset.  If there are less
    * than 32 bytes available to copy, this function only copies the bytes
    * that are available.</P>
    *
    * @param dataBuffer the buffer to copy the account data from
    * @param offset the index into the buffer where copying should begin
    * @return whether or not the data write succeeded
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public synchronized boolean writeAccountData(byte[] dataBuffer, int offset)
      throws OneWireException, OneWireIOException
   {
      //init local vars
      OneWireContainer18 ibcL = this.ibc;

      //make sure account info is properly setup
      if(!checkAccountPageInfo(ibcL))
         return false;

      int numBytes = Math.min(32, dataBuffer.length-offset);
      System.arraycopy(dataBuffer, offset, this.accountData, 0, numBytes);
      if(ibcL.writeDataPage(this.accountPageNumber, this.accountData))
      {
         if(this.writeCycleCounter>=0)
            this.writeCycleCounter++;
         return true;
      }

      //if write failed, we don't know what the write cycle counter is
      this.writeCycleCounter = -1;
      //and this cache should be marked dirty
      this.accountData[0] = 0;
      return false;
   }

   /**
    * <P>Reads the account data off the SHAiButton using a standard READ
    * command.  First, this function asserts that the account page number is
    * known as well as the length of the account file.  The 32 byte account
    * data page is copied into dataBuffer starting at the given offset.</P>
    *
    * @param dataBuffer the buffer to copy the account data into
    * @param offset the index into the buffer where copying should begin
    *
    * @return whether or not the read was successful
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public synchronized boolean readAccountData(byte[] dataBuffer, int offset)
      throws OneWireException, OneWireIOException
   {
      //init local vars
      OneWireContainer18 ibcL = this.ibc;

      //make sure account info is properly setup
      if(!checkAccountPageInfo(ibcL))
      {
         return false;
      }

      //if the cache is empty
      if(this.accountData[0]==0)
      {
         //read directly into local cache
         ibcL.readMemoryPage(this.accountPageNumber, this.accountData, 0);
      }

      //copy cached data into user's buffer
      System.arraycopy(this.accountData, 0, dataBuffer, offset, 32);

      //had to work, right?
      return true;
   }

   //prevent malloc'ing in critical path
   private byte[] readAccountData_rawData    = new byte[42];
   private byte[] readAccountData_scratchpad = new byte[32];
   /**
    * <P>Reads the account data off the SHAiButton using a READ_AUTHENTICATE
    * command.  First, this function asserts that the account page number is
    * known as well as the length of the account file.  Then it copies the
    * 3 byte challenge to the scratchpad before sending the command for
    * READ_AUTHENTICATE.  The 32 byte account data page is copied into
    * dataBuffer starting at dataStart.</P>
    *
    * <P>In addition to the account data, this function also returns a
    * calculated MAC.  The MAC requires 20 bytes after the start index.
    * The return value is the write cycle counter value for the account
    * data page<p>
    *
    * @param chlg the buffer containing a 3-byte random challenge.
    * @param chlgStart the index into the buffer where the 3 byte
    *        challenge begins.
    * @param dataBuffer the buffer to copy the account data into
    * @param dataStart the index into the buffer where copying should begin
    * @param mac the buffer to copy the resulting Message Authentication Code
    * @param macStart the index into the mac buffer to start copying
    *
    * @return the value of the write cycle counter for the page
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public synchronized int readAccountData(byte[] chlg, int chlgStart,
                                           byte[] dataBuffer, int dataStart,
                                           byte[] mac, int macStart)
      throws OneWireException, OneWireIOException
   {
      //init local variables
      OneWireContainer18 ibcL = this.ibc;
      byte[] rawData    = this.readAccountData_rawData;
      byte[] scratchpad = this.readAccountData_scratchpad;

      //make sure account info is properly setup
      if(this.accountPageNumber<0)
      {
         //user not setup
         return -1;
      }

      //copy challenge into scratchpad buffer
      System.arraycopy(chlg, 0, scratchpad, 20, 3);

      if(ibcL.eraseScratchPad(this.accountPageNumber))
      {
         //send 1 byte RESUME, instead of 9 byte SELECT
         ibcL.useResume(true);

         //write the challenge to the device scratchpad
         if(ibcL.writeScratchPad(this.accountPageNumber, 0,
                                     scratchpad, 0, 32))
         {

            //reads 42 bytes = 32 bytes of page data
            //               +  4 bytes page counter
            //               +  4 bytes secret counter
            //               +  2 bytes CRC
            boolean readOK
               = ibcL.readAuthenticatedPage(this.accountPageNumber,
                                            rawData, 0);

            //read the scratchpad for mac
            int len = ibcL.readScratchPad(scratchpad, 0);

            //disable RESUME
            ibcL.useResume(false);

            if ((!readOK) || (len < 0))
            {
               //read authenticate failed
               return -1;
            }

            //get the value of the write cycle counter
            int wcc = (rawData[35]&0x0ff);
            wcc = (wcc << 8) | (rawData[34]&0x0ff);
            wcc = (wcc << 8) | (rawData[33]&0x0ff);
            wcc = (wcc << 8) | (rawData[32]&0x0ff);

            //put the accountData in our local cache
            System.arraycopy(rawData, 0, this.accountData, 0, 32);

            //put the account data into return buffer
            System.arraycopy(rawData, 0, dataBuffer, dataStart, 32);

            //copy the mac into the return buffer
            System.arraycopy(scratchpad, 8, mac, macStart, 20);

            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
            if(DEBUG)
            {
               IOHelper.writeLine("----------------------------------------------------------");
               IOHelper.writeLine("User's ReadAuthPage");
               IOHelper.write("address: ");
               IOHelper.writeBytesHex(this.address);
               IOHelper.writeLine("speed: " + this.ibc.getAdapter().getSpeed());
               IOHelper.writeLine("RawData: ");
               IOHelper.writeBytesHex(rawData);
               IOHelper.writeLine("mac: ");
               IOHelper.writeBytesHex(mac, macStart, 20);
               IOHelper.writeLine("wcc: " + wcc);
               IOHelper.writeLine("----------------------------------------------------------");
            }
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

            //cache the write cycle counter
            this.writeCycleCounter = wcc;

            return wcc;
         }
         //write scratchpad failed
         return -1;
      }
      //erase scratchpad failed
      return -1;
   }

}
