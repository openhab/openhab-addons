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
import com.dalsemi.onewire.container.OneWireContainer33;
import com.dalsemi.onewire.container.OneWireContainer18;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.utils.IOHelper;

/**
 * <P>Class representing DS1961S (or DS2432), family-code 0x33, SHA iButtons as a user
 * token in SHA Transactions.</P>
 *
 * @see SHATransaction
 * @see SHAiButtonCopr
 * @see SHAiButtonUser18
 *
 * @version 1.00
 * @author  SKH
 */
public class SHAiButtonUser33
   extends SHAiButtonUser
{
   /**
    * For fast 0xFF fills of byte arrays
    */
   private static final byte[] ffBlock
      = new byte[] {
          (byte)0x0FF,(byte)0x0FF,(byte)0x0FF,(byte)0x0FF,
          (byte)0x0FF,(byte)0x0FF,(byte)0x0FF,(byte)0x0FF,
          (byte)0x0FF,(byte)0x0FF,(byte)0x0FF,(byte)0x0FF,
          (byte)0x0FF,(byte)0x0FF,(byte)0x0FF,(byte)0x0FF,
          (byte)0x0FF,(byte)0x0FF,(byte)0x0FF,(byte)0x0FF,
          (byte)0x0FF,(byte)0x0FF,(byte)0x0FF,(byte)0x0FF,
          (byte)0x0FF,(byte)0x0FF,(byte)0x0FF,(byte)0x0FF,
          (byte)0x0FF,(byte)0x0FF,(byte)0x0FF,(byte)0x0FF
       };

   /**
    * Reference to the OneWireContainer
    */
   protected OneWireContainer33 ibc33 = null;

   /**
    * Must maintain a reference to the coprocessor
    * for generating the COPY_SCRATCHPAD authentication.
    * This is what is referred to as the write-authorization coprocessor.
    */
   protected SHAiButtonCopr copr = null;

   /**
    * <P>No default constructor for user apps.  At bare minimum, you need
    * a reference to a <code>SHAiButtonCopr</code> for the transaction system
    * and a <code>SHAiButtonCopr</code> for generating the DS1961S write
    * authorization for the copy-scratchpad command.</P>
    *
    * <P>Note: These can be the same coprocessor if you're transaction system
    * is using unsigned transaction data.</P>
    *
    * @see #SHAiButtonUser33(SHAiButtonCopr,OneWireContainer33,boolean,byte[])
    * @see #SHAiButtonUser33(SHAiButtonCopr,SHAiButtonCopr,OneWireContainer33)
    * @see #SHAiButtonUser33(SHAiButtonCopr,SHAiButtonCopr)
    */
   protected SHAiButtonUser33()
   {;}

   /**
    * <P>Initialize a DS1961S as a fresh user iButton for a given SHA service.
    * This constructor not only creates the service file for the user iButton
    * using the TMEX file structure, but it also installs the master
    * authentication secret and binds it to the iButton (making it unique for
    * a particular button).  Optionally, the device can be formatted before
    * the service file is installed.</P>
    *
    * <P>Note: With this constructor, the master secret is installed and bound
    * to the iButton, so the final secret is none by the object.  For that
    * reason, a hardware coprocessor is not necessary for generating the
    * write-authorization MAC.</P>
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
    * @see #SHAiButtonUser33(SHAiButtonCopr,SHAiButtonCopr,OneWireContainer33)
    * @see #SHAiButtonUser33(SHAiButtonCopr,SHAiButtonCopr)
    */
   public SHAiButtonUser33(SHAiButtonCopr copr, OneWireContainer33 owc,
                         boolean formatDevice, byte[] authSecret)
      throws OneWireException, OneWireIOException
   {
      //setup service filename
      this(copr,copr);

      //hold container reference
      this.ibc33 = owc;

      //and address
      this.address = owc.getAddress();

      //clear out old secret first
      byte[] NullSecret = new byte[8];
      for(int i=0; i<8; i++)
         NullSecret[i] = 0x00;

      if(!this.ibc33.loadFirstSecret(NullSecret, 0))
         throw new OneWireException("Failed to null out device secret.");

      if(!owc.installMasterSecret(0, authSecret))
         throw new OneWireException("Install Master Secret failed");

      if(!createServiceFile(owc, strServiceFilename, formatDevice))
         throw new OneWireException("Failed to create service file.");

      //setup the fullBindCode with rest of info
      this.fullBindCode[4] = (byte)this.accountPageNumber;
      System.arraycopy(this.address,0,
                       this.fullBindCode,5,7);

      if(!owc.bindSecretToiButton(this.accountPageNumber, copr.getBindData()))
         throw new OneWireException("Bind Secret to iButton failed");

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------");
         IOHelper.writeLine("Initialized DS1961S User");
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
    * <P>Initialize a DS1961S as a fresh user iButton for a given SHA service.
    * This constructor not only creates the service file for the user iButton
    * using the TMEX file structure, but it also installs the master
    * authentication secret and binds it to the iButton (making it unique for
    * a particular button).  Optionally, the device can be formatted before
    * the service file is installed.</P>
    *
    * <P>Note: With this constructor, the master secret is installed and bound
    * to the iButton, so the final secret is none by the object.  For that
    * reason, a hardware coprocessor is not necessary for generating the
    * write-authorization MAC.</P>
    *
    * @param coprBindCode The Coprocessor Bind Code without the information.
    * @param fileName The file name from the Coprocessor.
    * @param fileNameExt The file extenstion from the Coprocessor
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
    * @see #SHAiButtonUser33(SHAiButtonCopr,SHAiButtonCopr,OneWireContainer33)
    * @see #SHAiButtonUser33(SHAiButtonCopr,SHAiButtonCopr)
    */
   public SHAiButtonUser33(byte[] coprBindCode, byte[] fileName,
                           int fileNameExt, OneWireContainer33 owc,
                           boolean formatDevice, byte[] authSecret)
      throws OneWireException, OneWireIOException
   {
      //create string representation of service filename
      this.strServiceFilename = new String(fileName) + "."
                                + (int)fileNameExt;

      //hold container reference
      this.ibc33 = owc;

      //and address
      this.address = owc.getAddress();

      //clear out old secret first
      byte[] NullSecret = new byte[8];
      for(int i=0; i<8; i++)
         NullSecret[i] = 0x00;

      if(!this.ibc33.loadFirstSecret(NullSecret, 0))
         throw new OneWireException("Failed to null out device secret.");

      if(!owc.installMasterSecret(0, authSecret))
         throw new OneWireException("Install Master Secret failed");

      if(!createServiceFile(owc, strServiceFilename, formatDevice))
         throw new OneWireException("Failed to create service file.");

      //setup the fullBindCode with rest of info
      this.fullBindCode[4] = (byte)this.accountPageNumber;
      System.arraycopy(this.address,0,
                       this.fullBindCode,5,7);

      if(!owc.bindSecretToiButton(this.accountPageNumber, copr.getBindData()))
         throw new OneWireException("Bind Secret to iButton failed");

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------");
         IOHelper.writeLine("Initialized DS1961S User");
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
    * <P>Note: The same coprocessor can be used for write-authorization as
    * authentication if you're transaction system is using unsigned transaction
    * data.</P>
    *
    * @param copr The SHAiButtonCopr to which the user object is tied.  This
    *        Coprocessor contains the necessary binding code and service
    *        filename, necessary for both locating a user and recreating his
    *        unique secret.
    * @param authCopr The SHAiButtonCopr used to generate the write-authorization
    *        MAC for the copy-scratchpad command of the DS1961S.
    * @param owc The DS1961S iButton that this object will refer to.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #SHAiButtonUser33(SHAiButtonCopr,OneWireContainer33,boolean,byte[])
    * @see #SHAiButtonUser33(SHAiButtonCopr,SHAiButtonCopr)
    */
   public SHAiButtonUser33(SHAiButtonCopr copr, SHAiButtonCopr authCopr,
                           OneWireContainer33 owc)
      throws OneWireException, OneWireIOException
   {
      //setup service filename
      this(copr,authCopr);

      //hold container reference and address
      if(!setiButton33(owc))
         throw new OneWireException("Invalid SHA user");
   }

   /**
    * <P>Creates a valid SHAiButtonUser object.  If the service file,
    * whose name is taken from the <code>SHAiButtonCopr</code>, is not
    * found on the user iButton, a OneWireException is thrown with the
    * message "Invalid SHA user".</P>
    *
    * <P>Note: The same coprocessor can be used for write-authorization as
    * authentication if you're transaction system is using unsigned transaction
    * data.</P>
    *
    * @param coprBindCode The Coprocessor Bind Code without the information.
    * @param fileName The file name from the Coprocessor.
    * @param fileNameExt The file extenstion from the Coprocessor
    * @param authCopr The SHAiButtonCopr used to generate the write-authorization
    *        MAC for the copy-scratchpad command of the DS1961S.
    * @param owc The DS1961S iButton that this object will refer to.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #SHAiButtonUser33(SHAiButtonCopr,OneWireContainer33,boolean,byte[])
    * @see #SHAiButtonUser33(SHAiButtonCopr,SHAiButtonCopr)
    */
   public SHAiButtonUser33(byte[] coprBindCode, byte[] fileName,
                           int fileNameExt, OneWireContainer33 owc)
      throws OneWireException, OneWireIOException
   {
      //make sure fullBindCode has appropriate ff padding
      System.arraycopy(ffBlock, 0, this.fullBindCode, 0, 15);

      //create string representation of service filename
      copr.getFilename(this.serviceFile,0);
      this.strServiceFilename = new String(fileName) + "."
                                + (int)fileNameExt;

      //hold container reference and address
      if(!setiButton33(owc))
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
    * <P>Note: The same coprocessor can be used for write-authorization as
    * authentication if you're transaction system is using unsigned transaction
    * data.</P>
    *
    * @param copr The SHAiButtonCopr to which the user object is tied.  This
    *        Coprocessor contains the necessary binding code and service
    *        filename, necessary for both locating a user and recreating his
    *        unique secret.
    * @param authCopr The SHAiButtonCopr used to generate the write-authorization
    *        MAC for the copy-scratchpad command of the DS1961S.
    *
    *
    * @see #SHAiButtonUser33(SHAiButtonCopr,OneWireContainer33,boolean,byte[])
    * @see #SHAiButtonUser33(SHAiButtonCopr,SHAiButtonCopr,OneWireContainer33)
    */
   public SHAiButtonUser33(SHAiButtonCopr copr, SHAiButtonCopr authCopr)
   {
      //hold a reference to the coprocessor
      this.copr = authCopr;

      //make sure fullBindCode has appropriate ff padding
      System.arraycopy(ffBlock, 0, this.fullBindCode, 0, 15);

      //create string representation of service filename
      copr.getFilename(this.serviceFile,0);
      this.strServiceFilename = new String(this.serviceFile) + "."
                                + (int)copr.getFilenameExt();
   }

   /**
    * <P>Modifies this SHA iButton so that it refers to another DS1961S
    * container.  This function only copies the reference to the
    * OneWireContainer, copes the reference to it's 1-Wire address, and
    * then asserts that the iButton contains a valid acccount info file
    * associated with the system.</P>
    *
    * @param owc The <code>OneWireContainer33</code> this object will refer to.
    *
    * @return <code>true</code> if a valid account service file exists on
    *         this <code>OneWireContainer33</code>.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    */
   public synchronized boolean setiButton33(OneWireContainer33 owc)
      throws OneWireException, OneWireIOException
   {
      //hold container reference
      this.ibc33 = owc;

      //and address
      this.address = owc.getAddress();

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
         IOHelper.writeLine("Loaded DS1961S User");
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
      if(this.ibc33==null)
         this.ibc33 = new OneWireContainer33();
      this.ibc33.setupContainer(adapter,address);
      return setiButton33(this.ibc33);
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
      if(this.ibc33==null)
         return false;

      this.ibc33.setupContainer(this.ibc33.getAdapter(),address);

      return setiButton33(this.ibc33);
   }

   /**
    * <P>Returns the value of the write cycle counter for the
    * page where the account data is stored.  If the write
    * cycle counter has ever been retrieved, this returns the
    * cached value.  Otherwise, this method reads the value
    * from the part.</P>
    *
    * <P>Since the DS1961S has no "write cycle counters", this function
    * always returns -1.</P>
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
      //DS1961S has no write cycle counters
      return -1;
   }

   /**
    * <P>Returns <code>true</code> if this buttons account data is stored
    * on a page that has a write cycle counter.</P>
    *
    * <P>Since the DS1961S has no "write cycle counters", this function
    * always returns false.</P>
    *
    * @return <code>true</code> if account page has write cycle counter.
    */
   public synchronized boolean hasWriteCycleCounter()
   {
      //DS1961S has no write cycle counters
      return false;
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
    *      ( 0x0000FF ), ( 0x0000FF ), ( 0x0000FF ), ( 0x0000FF ),
    *      (svcPageNum), (deviceAN+0), (deviceAN+1), (deviceAN+2),
    *      (deviceAN+3), (deviceAN+4), (deviceAN+5), (deviceAN+6),
    *      ( 0x0000FF ), ( 0x0000FF ), ( 0x0000FF )
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
      /*System.arraycopy(ffBlock, 0, fullBindCode, offset, 15);
      fullBindCode[offset+4] = (byte)((0x40)|(byte)this.accountPageNumber);
      System.arraycopy(this.address, 0, fullBindCode, offset+5, 7);*/
   }

   /**
    * <P>Returns a byte representing the appropriate authorization command
    * for the coprocessor to use to authenticate this user. For a DS1961S,
    * the authentication command is AUTH_HOST.</P>
    *
    * @return byte indicating appropriate command for authenticating user
    *
    */
   public byte getAuthorizationCommand()
   {
      return OneWireContainer18.AUTH_HOST;
   }

   private byte[] writeAccountData_copyAuth   = new byte[32];
   private byte[] writeAccountData_scratchpad = new byte[32];
   private byte[] writeAccountData_pageData   = new byte[32];
   /**
    * <P>Writes the account data to the SHAiButton.  First, this function
    * asserts that the account page number is known.  The account data is
    * copied from dataBuffer starting at the offset.  If there are less
    * than 32 bytes available to copy, this function only copies the bytes
    * that are available.</P>
    *
    * <P>Note that for the DS1961S user button, an authorization MAC must
    * be generated for the copy-scratchpad command.  Since the scratchpad
    * is only 8 bytes long, this must be done 4 times to write a page of
    * data.  So, this function only writes (in 8 byte blocks) the bytes
    * that have changed.</P>
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
      //local vars.
      OneWireContainer33 ibcL = this.ibc33;
      byte[] copyAuth      = this.writeAccountData_copyAuth;
      byte[] scratchpad    = this.writeAccountData_scratchpad;
      byte[] pageData      = this.writeAccountData_pageData;
      byte[] fullBindCode  = this.fullBindCode;
      SHAiButtonCopr coprL = this.copr;

      //make sure account info is properly setup
      if(!checkAccountPageInfo(ibcL))
         return false;

      //if the part is being initialized, the container class "knows"
      //the secret already.  no need for a coprocessor.
      if(ibcL.isContainerSecretSet())
      {
         //use container to write the data page, since it knows the secret
         ibcL.writeDataPage(this.accountPageNumber, dataBuffer);
         //update the data cache
         System.arraycopy(dataBuffer, offset, this.accountData, 0, 32);
      }
      else
      {
         //since the container's secret is not set, we have to use the
         //coprocessor for generating the copy scratchpad authorization.
         if(coprL==null)
         {
            throw new OneWireException("No Write Authorization Coprocessor Available!");
         }

         //copy the data cache into a working page;
         System.arraycopy(this.accountData, 0, pageData, 0, 32);

         //takes four write/copies to actually write the data page.
         for(int i=24; i>=0; i-=8)
         {
            int index = offset + i;
            //only perform any action if the data needs to be updated
            if(   (dataBuffer[index  ] != accountData[i  ]) ||
                  (dataBuffer[index+1] != accountData[i+1]) ||
                  (dataBuffer[index+2] != accountData[i+2]) ||
                  (dataBuffer[index+3] != accountData[i+3]) ||
                  (dataBuffer[index+4] != accountData[i+4]) ||
                  (dataBuffer[index+5] != accountData[i+5]) ||
                  (dataBuffer[index+6] != accountData[i+6]) ||
                  (dataBuffer[index+7] != accountData[i+7])     )
            {
               //format the working page for generating the
               //appropriate copy authorization mac
               pageData[28] = dataBuffer[index];
               pageData[29] = dataBuffer[index+1];
               pageData[30] = dataBuffer[index+2];
               pageData[31] = dataBuffer[index+3];

               //format the scratchpad for generating the
               //appropriate copy authorization mac
               scratchpad[8] = dataBuffer[index+4];
               scratchpad[9] = dataBuffer[index+5];
               scratchpad[10] = dataBuffer[index+6];
               scratchpad[11] = dataBuffer[index+7];

               //add in the page num and address
               System.arraycopy(this.fullBindCode, 4,
                                scratchpad, 12, 11);

               //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
               if(DEBUG)
               {
                  IOHelper.writeLine("------------------------------------------------------------");
                  IOHelper.writeLine("SHAiButtonUser33 - writeAccountData loop ");
                  IOHelper.writeLine("current account data state: ");
                  IOHelper.writeBytesHex(this.accountData);
                  IOHelper.writeLine("current byte block: " + i);
                  IOHelper.writeLine("New bytes for block: ");
                  IOHelper.writeBytesHex(dataBuffer,index,8);
                  IOHelper.writeLine("------------------------------------------------------------");
               }
               //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

               //get the copy authorization mac
               coprL.createDataSignatureAuth(pageData, scratchpad,
                                             copyAuth, 0, fullBindCode);

               //only need to pass it to coprocessor once
               fullBindCode = null;

               //write 8 bytes of data to the DS1961S scratchpad
               if(!ibcL.writeScratchpad(this.accountPageNumber, i,
                                        dataBuffer, index, 8))
                  //operation failed
                  return false;

               //copy scratchpad to page
               if(!ibcL.copyScratchpad(this.accountPageNumber, i,
                                        copyAuth, 0))
                  //operation failed
                  return false;

               //update cache of account data
               System.arraycopy(dataBuffer, index,
                                this.accountData, i, 8);

               //update our working copy of the account data
               System.arraycopy(this.accountData, 0,
                                pageData, 0, 32);
            }
         }
      }
      return true;
   }

   /**
    * <P>Reads the account data off the SHAiButton using a standard READ
    * command.  First, this function asserts that the account page number is
    * known as well as the length of the account file.  The 32 byte account
    * data page is copied into dataBuffer starting at the given offset.</P>
    *
    * @param dataBuffer the buffer to copy the account data into
    * @param offset the index into the buffer where copying should begin
    * @return whether or not the read was successful
    */
   public synchronized boolean readAccountData(byte[] dataBuffer, int offset)
      throws OneWireException, OneWireIOException
   {
      //init local vars
      OneWireContainer33 ibcL = this.ibc33;

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

      //copy from cache into output buffer
      System.arraycopy(this.accountData, 0, dataBuffer, offset, 32);

      //had to work, right?
      return true;
   }

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
    */
   public synchronized int readAccountData(byte[] chlg, int chlgStart,
                                           byte[] dataBuffer, int dataStart,
                                           byte[] mac, int macStart)
      throws OneWireException, OneWireIOException
   {
      //init local variables
      OneWireContainer33 ibcL = this.ibc33;

      //make sure account info is properly setup
      if(this.accountPageNumber<0)
      {
         //user not setup
         throw new OneWireException("SHAiButtonUser Not Properly Initialized");
      }

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         // for debug, lets use a constant challenge
         chlg[0] = 0x00;
         chlg[1] = 0x00;
         chlg[2] = 0x00;
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      //container keeps challenge as a member of the class
      ibcL.setChallenge(chlg, chlgStart);

      //performs the read authenticated page, to answer the challenge
      if(ibcL.readAuthenticatedPage(this.accountPageNumber,
                                    dataBuffer, dataStart,
                                    mac, macStart))
      {
         //copy from outputbuffer into cache
         System.arraycopy(dataBuffer, dataStart, this.accountData, 0, 32);

         //has no write cycle counter
         return -1;
      }
      else
      {
         throw new OneWireException("SHAiButtonUser ReadAuthenticatedPage Failed");
      }
   }

   /**
    * Refreshes eeprom SHA devices in case of weakly-programmed bits on
    * the account page.
    *
    * @return true if the refresh was successful
    */
   public boolean refreshDevice()
      throws OneWireException,OneWireIOException
   {
      return ibc33.refreshPage(this.accountPageNumber);
   }

}
