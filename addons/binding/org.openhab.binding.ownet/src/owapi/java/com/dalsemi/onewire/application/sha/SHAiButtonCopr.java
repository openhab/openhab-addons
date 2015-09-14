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

import java.util.*;
import java.io.*;

import com.dalsemi.onewire.container.OneWireContainer18;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.utils.IOHelper;
import com.dalsemi.onewire.application.file.OWFile;
import com.dalsemi.onewire.application.file.OWFileOutputStream;
import com.dalsemi.onewire.application.file.OWFileInputStream;
import com.dalsemi.onewire.application.file.OWFileNotFoundException;

/**
 * <P>Class for holding instances of SHA iButton Coprocessors involved in SHA
 * Transactions.  The Coprocessor is used for digitally signing transaction
 * data as well as generating random challenges for users and verifying
 * their response.</P>
 *
 * <p>A DS1963S SHA iButton can be a <code>SHAiButtonCopr</code> or a
 * <code>SHAiButtonUser</code>.  A Coprocessor iButton verifiessignatures
 * and signs data for User iButtons.  A Coprocessor might be located
 * inside a vending machine, where a person would bring their User iButton.  When
 * the User iButton is pressed to the Blue Dot to perform a transaction, the Coprocessor
 * would first verify that this button belongs to the system, i.e. that it knows the same
 * authentication secret (example: a Visa terminal making sure the iButton had a Visa
 * account installed). Then the Coprocessor would verify the signed data, probably money,
 * to make sure it was valid.  If someone tried to overwrite the money file, even with a
 * previously valid money file (an attempt to 'restore' a previous amount of money), the
 * signed file would be invalid because the signature includes the write cycle counter,
 * which is incremented every time a page is written to.  The write cycle counter is
 * read-only and does not roll over, so the previous amount of money could not be restored
 * by rolling the write counter. The Coprocessor verifies the money, then signs a new data
 * file that contains the new amount of money. </p>
 *
 * <p>There are two secrets involved with the transaction process.  The first secret is
 * the authentication secret.  It is used to validate a User iButton to a system.  The Coprocessor
 * iButton has the system authentication secret installed.  On User iButtons, the
 * system authentication secret is merged with binding data and the unique address
 * of the User iButton to create a unique device authentication secret.  The second secret
 * is a signing secret.  This secret only exists on the Coprocessor iButton, and is used
 * to sign and verify transaction data (i.e. money).  These secrets are inaccessible outside the
 * iButton.  Once they are installed, they cannot be retrieved.</p>
 *
 * <p>This class makes use of several performance enhancements for TINI.
 * For instance, most methods are <code>synchronized</code> to access instance variable
 * byte arrays rather than creating new byte arrays every time a transaction
 * is performed.  This could hurt performance in multi-threaded
 * applications, but the usefulness of having several threads contending
 * to talk to a single iButton is questionable since the methods in
 * <code>com.dalsemi.onewire.adapter.DSPortAdapter</code>
 * <code>beginExclusive(boolean)</code> and <code>endExclusive()</code> should be used.</p>
 *
 * @see SHATransaction
 * @see SHAiButtonUser
 *
 * @version 1.00
 * @author  SKH
 */
public class SHAiButtonCopr
{
   /**
    * Turns on extra debugging output
    */
   static final boolean DEBUG = false;

   // ***********************************************************************
   // Constants for Error codes
   // ***********************************************************************
   public static final int NO_ERROR = 0;
   public static final int WRITE_DATA_PAGE_FAILED  = -1;
   public static final int WRITE_SCRATCHPAD_FAILED = -2;
   public static final int MATCH_SCRATCHPAD_FAILED = -3;
   public static final int ERASE_SCRATCHPAD_FAILED = -4;
   public static final int COPY_SCRATCHPAD_FAILED  = -5;
   public static final int SHA_FUNCTION_FAILED     = -6;
   public static final int BIND_SECRET_FAILED      = -7;
   // ***********************************************************************

   /**
    * Last error code raised
    */
   protected int lastError;

   /**
    * Reference to the OneWireContainer
    */
   protected OneWireContainer18 ibc = null;

   /**
    * Cache of 1-Wire Address
    */
   protected byte[] address = null;

   /**
    * Page used for generating user authentication secret.
    */
   protected int authPageNumber = -1;

   /**
    * Any auxilliary data stored on this coprocessor
    */
   protected String auxData;

   /**
    * 7 bytes of binding data for scratchpad to bind secret installation
    */
   protected byte[] bindCode = new byte [7];

   /**
    * 32 bytes of binding data to bind secret installation
    */
   protected byte[] bindData = new byte [32];

   /**
    * Specifies whether or not this coprocessor is compatible with
    * the DS1961S.  This entails the use of a specifically padded
    * authentication secret.
    */
   protected boolean DS1961Scompatible = false;

   /**
    * Code used to specify encryption type.
    */
   protected int encCode = -1;

   /**
    * Filename, including extension, for user's service file
    */
   protected byte[] filename = new byte[5];

   /**
    * 20 byte initial signature, used for signing user account data
    */
   protected byte[] initialSignature = new byte [20];

   /**
    * The Provider name of the coprocessor's service
    */
   protected String providerName;

   /**
    * 3 byte challenge, used for signing user account data
    */
   protected byte[] signingChallenge = new byte [3];

   /**
    * Page used for signing user account data.
    */
   protected int signPageNumber = 8;

   /**
    * Code used to specify encryption type.
    */
   protected int version = -1;

   /**
    * Page used for generating user's validation MAC.
    */
   protected int wspcPageNumber = -1;



   // ***********************************************************************
   // Constructors
   // ***********************************************************************

   /**
    * <p>No default construct for user apps.  Coprocessors, unlike users, are
    * immutable classes, so there is no <code>setiButton</code> for User
    * applications.</p>
    *
    * @see #SHAiButtonCopr(OneWireContainer18,String,boolean,int,int,int,int,int,byte,byte[],byte[],byte[],byte[],byte[],byte[],byte[],byte[],byte[])
    * @see #SHAiButtonCopr(OneWireContainer18,String)
    */
   protected SHAiButtonCopr()
   {;}

   /**
    * <P>Sets up this coprocessor object based on the provided parameters
    * and saves all of these parameters as the contents of the file
    * <code>coprFilename</code> stored on <code>owc</code>.  Then, the
    * system secret and authentication secret are installed on the
    * coprocessor button.</P>
    *
    * <P>For the proper format of the coprocessor data file, see the
    * document entitled "Implementing Secured D-Identification and E-Payment
    * Applications using SHA iButtons".  For the format of TMEX file
    * structures, see Application Note 114.</P>
    *
    * @param l_owc The DS1963S used as a coprocessor.
    * @param coprFilename The TMEX filename where coprocessor configuration
    *        data is stored.  Usually, "COPR.0".
    * @param l_formatDevice boolean indicating whether or not the TMEX
    *        filesystem of this device should be formatted before the
    *        coprocessor data file is stored.
    * @param l_signPageNumber page number used for signing user account data.
    *        (Should be page 8, but page 0 is acceptable if you don't need
    *        the TMEX directory structure)
    * @param l_authPageNumber page number used for recreating user secret.
    * @param l_wspcPageNumber page number used for storing user secret and
    *        recreating authentication MAC.
    * @param l_version version of the service provided by this coprocessor.
    * @param l_encCode refers to a type of encryption used for user account
    *        data stored on user buttons.
    * @param l_serviceFileExt the file extension used for the service file.
    *        (An extension of decimal 102 is reserved for Money files).
    * @param l_serviceFilename the 4-byte name of the user's account data
    *        file.
    * @param l_providerName the name of the provider of this service
    * @param l_bindData the binding data used to finalize secret installation
    *        on user buttons.
    * @param l_bindCode the binding code used to finalize secret installation
    *        on user buttons.
    * @param l_auxData any auxilliary or miscellaneous data to be stored on
    *        the coprocessor.
    * @param l_initialSignature the 20-byte initial MAC placed in user account
    *        data before generating actual MAC.
    * @param l_signingChlg the 3-byte challenge used for signing user
    *        account data.
    * @param l_signingSecret the system signing secret used by the
    *        service being installed on this coprocessor.
    * @param l_authSecret the system authentication secret used by the
    *        service being installed on this coprocessor.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #SHAiButtonCopr(OneWireContainer18,String)
    */
   public SHAiButtonCopr (OneWireContainer18 l_owc, String coprFilename,
                          boolean l_formatDevice, int l_signPageNumber,
                          int l_authPageNumber, int l_wspcPageNumber,
                          int l_version, int l_encCode, byte l_serviceFileExt,
                          byte[] l_serviceFilename, byte[] l_providerName,
                          byte[] l_bindData, byte[] l_bindCode, byte[] l_auxData,
                          byte[] l_initialSignature, byte[] l_signingChlg,
                          byte[] l_signingSecret, byte[] l_authSecret)
      throws OneWireException, OneWireIOException
   {
      //clear any errors
      this.lastError = SHAiButtonCopr.NO_ERROR;

      // Do some bounds checking on our array data.
      if(l_bindData.length!=32)
         throw new OneWireException("Invalid Binding Data");
      if(l_bindCode.length!=7)
         throw new OneWireException("Invalid Binding Code");
      if(l_signingChlg.length!=3)
         throw new OneWireException("Invalid Signing Challenge");
      if(l_serviceFilename.length<4)
         throw new OneWireException("Invalid Service Filename");
      if(l_signPageNumber!=0 && l_signPageNumber!=8)
         throw new OneWireException("Invalid Signing Page Number (must be 0 or 8)");

      //Check to see if this coprocessor's authentication secret
      //is appropriately padded to be used with a DS1961S
      this.DS1961Scompatible = ((l_authSecret.length%47)==0);
      int secretDiv = l_authSecret.length/47;
      for(int j=0; j<secretDiv && DS1961Scompatible; j++)
      {
         int offset = 47*j;
         for(int i=32; i<36 && this.DS1961Scompatible; i++)
            this.DS1961Scompatible
               = (l_authSecret[i + offset] == (byte)0x0FF);
         for(int i=44; i<47 && this.DS1961Scompatible; i++)
            this.DS1961Scompatible
               = (l_authSecret[i + offset] == (byte)0x0FF);
      }

      //get the current month, date, and year
      Calendar c  = Calendar.getInstance();
      int month   = c.get(Calendar.MONTH) + 1;
      int date    = c.get(Calendar.DATE);
      int year    = c.get(Calendar.YEAR) - 1900;
      byte yearMSB = (byte)((year >> 8) & 0x0FF);
      byte yearLSB = (byte)(year & 0x0FF);

      try
      {
         if(l_formatDevice)
         {
            //format if necessary
            OWFile f = new OWFile(l_owc,coprFilename);
            f.format();
            f.close();
         }
         //Create the service file
         OWFileOutputStream fos
            = new OWFileOutputStream(l_owc, coprFilename);
         fos.write(l_serviceFilename,0,4);
         fos.write(l_serviceFileExt);
         fos.write(l_signPageNumber);
         fos.write(l_authPageNumber);
         fos.write(l_wspcPageNumber);
         fos.write(l_version);
         fos.write(month);
         fos.write(date);
         fos.write(yearMSB);
         fos.write(yearLSB);
         fos.write(l_bindData);
         fos.write(l_bindCode);
         fos.write(l_signingChlg);
         fos.write((byte)l_providerName.length);
         fos.write((byte)l_initialSignature.length);
         fos.write((byte)l_auxData.length);
         fos.write(l_providerName,0,(byte)l_providerName.length);
         fos.write(l_initialSignature,0,(byte)l_initialSignature.length);
         fos.write(l_auxData,0,(byte)l_auxData.length);
         fos.write(l_encCode);
         fos.write(DS1961Scompatible?0x55:0x00);
         fos.flush();
         fos.close();
      }
      catch(Exception ioe)
      {
         ioe.printStackTrace();
         throw new OneWireException("Creating Service File failed!");
      }

      //Install the system signing secret, used to sign and validate all user data
      if(!l_owc.installMasterSecret(l_signPageNumber, l_signingSecret, l_signPageNumber&7))
         throw new OneWireException("Could not install signing secret");

      //Install the system authentication secret, used to authenticate users
      if(!l_owc.installMasterSecret(l_authPageNumber, l_authSecret, l_authPageNumber&7))
         throw new OneWireException("Could not install authentication secret");

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------");
         IOHelper.writeLine("Initialized Coprocessor");
         IOHelper.writeLine("address");
         IOHelper.writeBytesHex(l_owc.getAddress());
         IOHelper.writeLine("signPageNumber: " + l_signPageNumber);
         IOHelper.writeLine("authPageNumber: " + l_authPageNumber);
         IOHelper.writeLine("wspcPageNumber: " + l_wspcPageNumber);
         IOHelper.writeLine("serviceFilename");
         IOHelper.writeBytesHex(l_serviceFilename);
         IOHelper.writeLine("bindData");
         IOHelper.writeBytesHex(l_bindData);
         IOHelper.writeLine("bindCode");
         IOHelper.writeBytesHex(l_bindCode);
         IOHelper.writeLine("initialSignature");
         IOHelper.writeBytesHex(l_initialSignature);
         IOHelper.writeLine("signingChlg");
         IOHelper.writeBytesHex(l_signingChlg);
         IOHelper.writeLine("signingSecret");
         IOHelper.writeBytesHex(l_signingSecret);
         IOHelper.writeLine("authSecret");
         IOHelper.writeBytesHex(l_authSecret);
         IOHelper.writeLine("------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      //Call this method because it will read back the file.  Ensuring
      //there were no errors in writing the file in the first place.
      this.setiButton(l_owc, coprFilename);

   }

   /**
    * <P>Sets up this coprocessor object based on the contents of the file
    * <code>coprFilename</code> stored on <code>owc</code>. This sets
    * all the properties of the object as a consequence of what's in
    * the coprocessor file.</P>
    *
    * <P>For the proper format of the coprocessor data file, see the
    * document entitled "Implementing Secured D-Identification and E-Payment
    * Applications using SHA iButtons".  For the format of TMEX file
    * structures, see Application Note 114.
    *
    * @param owc The DS1963S used as a coprocessor
    * @param coprFilename The TMEX filename where coprocessor configuration
    *        data is stored.  Usually, "COPR.0".
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #SHAiButtonCopr(OneWireContainer18,String,boolean,int,int,int,int,int,byte,byte[],byte[],byte[],byte[],byte[],byte[],byte[],byte[],byte[])
    */
   public SHAiButtonCopr(OneWireContainer18 owc, String coprFilename)
      throws OneWireException, OneWireIOException
   {
      setiButton(owc, coprFilename);
   }



   // ***********************************************************************
   // Modifier Methods
   //  - setiButton is the only essential modifier.  It updates all
   //    data members based on contents of service file alone.
   // ***********************************************************************

   /**
    * <P>Sets up this coprocessor object based on the contents of the file
    * <code>coprFilename</code> stored on <code>owc</code>. This sets
    * all the properties of the object as a consequence of what's in
    * the coprocessor file.</P>
    *
    * <P>In essence, this is the classes only proper modifier.  All data
    * members should be set by this method alone.</P>
    *
    * <P>For the proper format of the coprocessor data file, see the
    * document entitled "Implementing Secured D-Identification and E-Payment
    * Applications using SHA iButtons".  For the format of TMEX file
    * structures, see Application Note 114.
    *
    * @param owc The DS1963S used as a coprocessor
    * @param coprFilename The TMEX filename where coprocessor configuration
    *        data is stored.  Usually, "COPR.0".
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    */
   private void setiButton(OneWireContainer18 owc, String coprFilename)
      throws OneWireException, OneWireIOException
   {
      //hold container reference
      this.ibc = owc;
      //and address
      this.address = owc.getAddress();

      OWFileInputStream fis = null;
      try
      {
         if(DEBUG)
            IOHelper.writeLine("opening file: " + coprFilename + " on token: " + owc);
         fis = new OWFileInputStream(owc, coprFilename);
      }
      catch(OWFileNotFoundException e)
      {
         if(DEBUG)
            e.printStackTrace();
         throw new OneWireIOException("Coprocessor service file Not found: " + e);
      }
      try
      {
         //configures this object from the info in the given stream
         fromStream(fis);
      }
      catch(IOException ioe)
      {
         if(DEBUG)
            ioe.printStackTrace();
         throw new OneWireException("Bad Data in Coproccessor Service File: " + ioe);
      }
      finally
      {
         try
         {
               fis.close();
         }
         catch(IOException ioe)
         { /*well, at least I tried!*/; }
      }

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------");
         IOHelper.writeLine("Loaded Coprocessor");
         IOHelper.writeLine("address");
         IOHelper.writeBytesHex(owc.getAddress());
         IOHelper.writeLine("signPageNumber: " + signPageNumber);
         IOHelper.writeLine("authPageNumber: " + authPageNumber);
         IOHelper.writeLine("wspcPageNumber: " + wspcPageNumber);
         IOHelper.writeLine("serviceFilename");
         IOHelper.writeBytesHex(filename);
         IOHelper.writeLine("bindData");
         IOHelper.writeBytesHex(bindData);
         IOHelper.writeLine("bindCode");
         IOHelper.writeBytesHex(bindCode);
         IOHelper.writeLine("initialSignature");
         IOHelper.writeBytesHex(initialSignature);
         IOHelper.writeLine("signingChallenge");
         IOHelper.writeBytesHex(signingChallenge);
         IOHelper.writeLine("------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
   }


   /*
   Do I even need modifiers.  The class should only be modified with
   "setibutton"
   // ***********************************************************************
   // Protected Modifiers
   //  - All modifiers of this class's data are protected.  The reason
   //    is that all data members are set based on information in the
   //    Coprocessor's service file.  These member's should never be
   //    directly updated by User applications, but rather as a result
   //    to changes in the service file.
   // ***********************************************************************
   protected void setSigningPageNumber(int pg)
   {
      this.signPageNumber = pg;
   }
   protected void setWorkspacePageNumber(int pg)
   {
      this.wspcPageNumber = pg;
   }
   protected void setAuthenticationPageNumber(int pg)
   {
      this.authPageNumber = pg;
   }
   protected void setFilename(byte[] l_filename, int offset)
   {
      int cnt = Math.min(l_filename.length - offset, 4);
      System.arraycopy(l_filename, offset, filename, 0, cnt);
   }
   protected void setFilenameExt(byte ext)
   {
      filename[4] = ext;
   }
   protected void setBindData(byte[] l_bindData, int offset)
   {
      int cnt = Math.min(l_bindData.length - offset, 32);
      System.arraycopy(l_bindData, offset, bindData, 0, cnt);
   }
   protected void setBindCode(byte[] l_bindCode, int offset)
   {
      int cnt = Math.min(l_bindCode.length - offset, 7);
      System.arraycopy(l_bindCode, offset, bindCode, 0, cnt);
   }
   protected synchronized void setSigningChallenge (byte[] chlg, int offset)
   {
      int cnt = (chlg.length > (3+offset)) ? 3 : (chlg.length - offset);
      System.arraycopy(chlg, offset, signingChallenge, 0, cnt);
   }
   protected synchronized void setInitialSignature (byte[] sig_ini, int offset)
   {
      int cnt = (sig_ini.length > (20+offset)) ? 20 : (sig_ini.length - offset);
      System.arraycopy(sig_ini, offset, initialSignature, 0, cnt);
   }*/

   // ***********************************************************************
   // Begin Accessor Methods
   // ***********************************************************************

   /**
    * <P>Returns the 8 byte address of the OneWireContainer this
    * SHAiButton refers to.</P>
    *
    * @return 8 byte array containing family code, address, and
    *         crc8 of the OneWire device.
    */
   public byte[] getAddress()
   {
      byte[] data = new byte[8];
      System.arraycopy(address,0,data,0,8);
      return data;
   }

   /**
    * <P>Copies the 8 byte address of the OneWireContainer into
    * the provided array starting at the given offset.</P>
    *
    * @param data array with at least 8 bytes after offset
    * @param offset the index at which copying starts
    */
   public void getAddress(byte[] data, int offset)
   {
      System.arraycopy(address, 0, data, offset, 8);
   }

   /**
    * <P>Copies the specified number of bytes from the address
    * of the OneWireContainer into the provided array starting
    * at the given offset.</P>
    *
    * @param data array with at least cnt bytes after offset
    * @param offset the index at which copying starts
    * @param cnt the number of bytes to copy
    */
   public void getAddress(byte[] data, int offset, int cnt)
   {
      System.arraycopy(address, 0, data, offset, cnt);
   }

   /**
    * <P>Returns the page number used by this coprocessor for storing
    * system authentication secret and recreating user's authentication
    * secret.  The authentication secret stays constant, while the new
    * secret is copied on to the secret corresponding to the workspace
    * page.</P>
    *
    * @return page number used for system authentication secret
    * @see OneWireContainer18#bindSecretToiButton(int,byte[],byte[],int)
    */
   public int getAuthenticationPageNumber()
   {
      return this.authPageNumber;
   }

   /**
    * <P>Returns a string representing the auxilliary data associated
    * with this coprocessor's service.</P>
    *
    * @return the auxilliary data of this coprocessor's service
    */
   public String getAuxilliaryData()
   {
      return auxData;
   }

   /**
    * <P>Returns 7 byte binding code for finalizing secret installation
    * on user buttons.  This is copied into the user's scratchpad,
    * along with 8 other bytes of binding data (see
    * <code>bindSecretToiButton</code>) for finalizing the secret
    * and making it unique to the button.</P>
    *
    * @return the binding code for user's scratchpad
    * @see OneWireContainer18#bindSecretToiButton(int,byte[],byte[],int)
    */
   public byte[] getBindCode()
   {
      byte[] data = new byte[7];
      System.arraycopy(bindCode, 0, data, 0, 7);
      return data;
   }

   /**
    * <P>Copies 7 byte binding code for finalizing secret installation
    * on user buttons.  This is copied into the user's scratchpad,
    * along with 8 other bytes of binding data (see
    * <code>bindSecretToiButton</code>) for finalizing the secret
    * and making it unique to the button.</P>
    *
    * @param data array for copying the binding code for user's
    *        scratchpad
    * @param offset the index at which to start copying.
    * @see OneWireContainer18#bindSecretToiButton(int,byte[],byte[],int)
    */
   public void getBindCode(byte[] data, int offset)
   {
      System.arraycopy(bindCode, 0, data, offset, 7);
   }

   /**
    * <P>Returns 32 byte binding data for finalizing secret installation
    * on user buttons.  This is copied into the user's account data
    * page (see <code>bindSecretToiButton</code>) for finalizing the
    * secret and making it unique to the button.</P>
    *
    * @return the binding data for user's data page
    * @see OneWireContainer18#bindSecretToiButton(int,byte[],byte[],int)
    */
   public byte[] getBindData()
   {
      byte[] data = new byte[32];
      System.arraycopy(bindData, 0, data, 0, 32);
      return data;
   }

   /**
    * <P>Copies 32 byte binding data for finalizing secret installation
    * on user buttons.  This is then copied into the user's account data
    * page (see <code>bindSecretToiButton</code>) for finalizing the
    * secret and making it unique to the button.</P>
    *
    * @param data array for copying the binding data for user's
    *        data page
    * @param offset the index at which to start copying.
    * @see OneWireContainer18#bindSecretToiButton(int,byte[],byte[],int)
    */
   public void getBindData(byte[] data, int offset)
   {
      System.arraycopy(bindData, 0, data, offset, 32);
   }

   /**
    * <P>Returns an integer representing the encryption code for
    * this coprocessor.  No handling of specific encryption codes
    * are in place with this API, but could be added easily at
    * the <code>SHATransaction</codE> layer.</P>
    *
    * @return an integer representing type of encryption for user data.
    */
   public int getEncryptionCode()
   {
      return encCode;
   }

   /**
    * <P>Copies the filename (used for storing account data on user
    * buttons) into the specified array starting at the specified
    * offset.</P>
    *
    * @param l_filename the array into which the filename will be
    *        copied.
    * @param offset the starting index for copying the filename.
    */
   public void getFilename(byte[] l_filename, int offset)
   {
      int cnt = Math.min(l_filename.length - offset, 4);
      System.arraycopy(filename, offset, l_filename, offset, cnt);
   }

   /**
    * <P>Returns the extension of the filename used for storing account
    * data on user buttons.  If the type of this service is an
    * e-cash application, the file extension will be decimal 102.</P>
    *
    * @return proper file extension for user's account data file.
    */
   public byte getFilenameExt()
   {
      return filename[4];
   }

   /**
    * <P>Returns the 20-byte initial signature used by this coprocessor
    * for signing account data.</P>
    *
    * @return 20-byte initial signature.
    */
   public byte[] getInitialSignature()
   {
      byte[] data = new byte[20];
      System.arraycopy(initialSignature, 0, data, 0, 20);
      return data;
   }

   /**
    * <P>Copies the 20-byte initial signature used by this coprocessor
    * for signing account data into the specified array starting at the
    * specified offset.</P>
    *
    * @param data arry for copying the 20-byte initial signature.
    * @param offset the index at which to start copying.
    */
   public void getInitialSignature(byte[] data, int offset)
   {
      System.arraycopy(initialSignature, 0, data, offset, 20);
   }

   /**
    * <P>Returns error code matching last error encountered by the
    * coprocessor.  An error code of zero implies NO_ERROR.</P>
    *
    * @return the error code, zero for none.
    */
   public int getLastError()
   {
      return lastError;
   }

   /**
    * <P>Returns a string representing the Provider name associated
    * with this coprocessor's service.</P>
    *
    * @return the name of the provider's service.
    */
   public String getProviderName()
   {
      return providerName;
   }

   /**
    * <P>Returns the 3-byte signing challenge used by this coprocessor
    * for data validation.</P>
    *
    * @return 3-byte challenge
    */
   public byte[] getSigningChallenge()
   {
      byte[] data = new byte[3];
      System.arraycopy(signingChallenge, 0, data, 0, 3);
      return data;
   }

   /**
    * <P>Copies the 3-byte signing challenge used by this coprocessor
    * for data validation into the specified array starting at
    * the specified offset.</P>
    *
    * @param data the array for copying the 3-byte challenge.
    * @param offset the index at which to start copying.
    */
   public void getSigningChallenge(byte[] data, int offset)
   {
      System.arraycopy(signingChallenge, 0, data, offset, 3);
   }

   /**
    * <P>Returns the page number used by this coprocessor for signing
    * account data.</P>
    *
    * @return page number used for signing
    */
   public int getSigningPageNumber()
   {
      return this.signPageNumber;
   }

   /**
    * <P>Returns the version number of this service.</P>
    *
    * @return version number for service.
    */
   public int getVersion()
   {
      return this.version;
   }

   /**
    * <P>Returns the page number used by this coprocessor for regenerating
    * the user authentication.  This page is the target page for
    * <code>bindSecretToiButton</code> when trying to reproduce a user's
    * authentication secret.</P>
    *
    * @return page number used for regenerating user authentication.
    * @see OneWireContainer18#bindSecretToiButton(int,byte[],byte[],int)
    */
   public int getWorkspacePageNumber()
   {
      return this.wspcPageNumber;
   }

   /**
    * <P>Returns a boolean indicating whether or not this coprocessor's
    * secret's were formatted for compatibility with the DS1961S.</P>
    *
    * @return <code>true</code> if this coprocessor can authenticate a
    *         DS1961S using it's system authentication secret.
    * @see #reformatFor1961S(byte[])
    */
   public boolean isDS1961Scompatible()
   {
      return DS1961Scompatible;
   }
   // ***********************************************************************
   // End Accessor Methods
   // ***********************************************************************



   // ***********************************************************************
   // Begin SHA iButton Methods
   // ***********************************************************************


   /**
    * <P>Given a 32-byte array for page data and a 32-byte array for
    * scratchpad content, this function will create a 20-byte signature
    * for the data based on SHA-1.  The format of the calculation of the
    * data signature is as follows: First 4-bytes of signing secret,
    * 32-bytes of accountData, 12 bytes of scratchpad data starting at
    * index 8, last 4-bytes of signing secret, 3 bytes of scratchpad data
    * starting at index 20, and the rest is padding as specified for
    * standard SHA-1.  This is all laid out, in detail, in the DS1963S
    * data sheet.</P>
    *
    * <P>The resulting 20-byte signature is copied into
    * <code>mac_buffer</code> starting at <code>macStart</code>.  If you're
    * updating a signature that already exists in the accountData array,
    * it is acceptable to call the method like so:
    * <code><pre>
    *   copr.createDataSignature(accountData, spad, accountData, 8);
    * </pre></code>
    * assuming that the signature starts at index 8 of the accountData
    * array.</p>
    *
    * @param accountData the 32-byte data page for which the signature is
    *        generated.
    * @param signScratchpad the 32-byte scratchpad contents for which the
    *        signature is generated.  This will contain parameters such
    *        as the user's write cycle counter for the page, the user's
    *        1-wire address, and the page number where account data is
    *        stored.
    * @param mac_buffer used to return the 20-byte signature generated
    *        by signing the page using the coprocessor's system signing
    *        secret.
    * @param macStart the offset into mac_buffer where copying should start.

    * @return <code>true</code> if successful, <code>false</code> if an error
    *         occurred  (use <code>getLastError()</code> for more
    *         information on the type of error)
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see OneWireContainer18#SHAFunction(byte,int)
    * @see #getLastError()
    */
   public boolean createDataSignature(byte[] accountData,
                                      byte[] signScratchpad,
                                      byte[] mac_buffer, int macStart)
      throws OneWireException, OneWireIOException
   {
      //clear any errors
      this.lastError = SHAiButtonCopr.NO_ERROR;

      //maintain local reference to container
      OneWireContainer18 ibcL = this.ibc;
      int addr = this.signPageNumber << 5;

      //now we are ready to make a signature
      if (!ibcL.writeDataPage(this.signPageNumber, accountData))
      {
         this.lastError = SHAiButtonCopr.WRITE_DATA_PAGE_FAILED;
         return false;
      }

      //allow resume access to coprocessor
      ibcL.useResume(true);

      //write the signing information to the scratchpad
      if (!ibcL.writeScratchPad(0, 0, signScratchpad, 0, 32))
      {
         this.lastError = SHAiButtonCopr.WRITE_SCRATCHPAD_FAILED;
         ibcL.useResume(false);
         return false;
      }

      //\\//\\//\\//\\//\\//\\//\\////\\////\\////\\////\\////\\////\\//
      if(DEBUG)
      {
         IOHelper.writeLine("-----------------------------------------------------------");
         IOHelper.writeLine("COPR DEBUG - createDataSignature");
         IOHelper.write("address: ");
         IOHelper.writeBytesHex(address, 0, 8);
         IOHelper.writeLine("speed: " + this.ibc.getAdapter().getSpeed());
         IOHelper.writeLine("-----------------------------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\////\\////\\////\\////\\////\\////\\//

      //sign that baby!
      if (ibcL.SHAFunction(OneWireContainer18.SIGN_DATA_PAGE, addr))
      {
         //get the MAC from the scratchpad
         ibcL.readScratchPad(signScratchpad, 0);

         //copy the MAC into the accountData page
         System.arraycopy(signScratchpad, 8, mac_buffer, macStart, 20);

         ibcL.useResume(false);
         return true;
      }
      else
         this.lastError = SHAiButtonCopr.SHA_FUNCTION_FAILED;

      ibcL.useResume(false);
      return false;
   }

   /**
    * <p>Creates a data signature, but instead of using the signing secret,
    * it uses the authentication secret, bound for a particular button.</p>
    *
    * <P>fullBindCode can be null if the secret is already bound and in
    * the signing page.</p>
    *
    * @param accountData the 32-byte data page for which the signature is
    *        generated.
    * @param signScratchpad the 32-byte scratchpad contents for which the
    *        signature is generated.  This will contain parameters such
    *        as the user's write cycle counter for the page, the user's
    *        1-wire address, and the page number where account data is
    *        stored.
    * @param mac_buffer used to return the 20-byte signature generated
    *        by signing the page using the coprocessor's system signing
    *        secret.
    * @param macStart the offset into mac_buffer where copying should start.
    * @param fullBindCode used to recreate the user iButton's unique secret

    * @return <code>true</code> if successful, <code>false</code> if an error
    *         occurred  (use <code>getLastError()</code> for more
    *         information on the type of error)
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see OneWireContainer18#SHAFunction(byte,int)
    * @see #createDataSignature(byte[],byte[],byte[],int)
    * @see #getLastError()
    */
   public boolean createDataSignatureAuth(byte[] accountData,
                                          byte[] signScratchpad,
                                          byte[] mac_buffer, int macStart,
                                          byte[] fullBindCode)
      throws OneWireException, OneWireIOException
   {
      //clear any errors
      this.lastError = SHAiButtonCopr.NO_ERROR;

      //maintain local reference to container
      OneWireContainer18 ibcL = this.ibc;
      int page = this.signPageNumber;
      int addr = page << 5;

      //\\//\\//\\//\\//\\//\\//\\////\\////\\////\\////\\////\\////\\//
      if(DEBUG)
      {
         IOHelper.writeLine("-----------------------------------------------------------");
         IOHelper.writeLine("COPR DEBUG - createDataSignatureAuth");
         IOHelper.writeLine("address:");
         IOHelper.writeBytesHex(address, 0, 8);
         IOHelper.writeLine("accountData");
         IOHelper.writeBytesHex(accountData);
         IOHelper.writeLine("signScratchpad");
         IOHelper.writeBytesHex(signScratchpad);
         IOHelper.writeLine("mac_buffer: ");
         IOHelper.writeBytesHex(mac_buffer,macStart,20);
         IOHelper.writeLine("fullBindCode: ");
         if(fullBindCode!=null)
            IOHelper.writeBytesHex(fullBindCode);
         else
            IOHelper.writeLine("null");
         IOHelper.writeLine("-----------------------------------------------------------");
      }

      //\\//\\//\\//\\//\\//\\//\\////\\////\\////\\////\\////\\////\\//
      if(fullBindCode!=null)
      {
         //recreate the user's secret on the coprocessor.
         if(!ibcL.bindSecretToiButton(this.authPageNumber,
                                      this.bindData, fullBindCode,
                                      page&7 ))
         {
            this.lastError = SHAiButtonCopr.BIND_SECRET_FAILED; //bind secret failed
            return false;
         }
      }

      //now we are ready to make a signature
      if (!ibcL.writeDataPage(this.signPageNumber, accountData))
      {
         this.lastError = SHAiButtonCopr.WRITE_DATA_PAGE_FAILED;
         return false;
      }

      //allow resume access to coprocessor
      ibcL.useResume(true);

      //write the signing information to the scratchpad
      if (!ibcL.writeScratchPad(0, 0, signScratchpad, 0, 32))
      {
         this.lastError = SHAiButtonCopr.WRITE_SCRATCHPAD_FAILED;
         ibcL.useResume(false);
         return false;
      }

      //sign that baby!
      if (ibcL.SHAFunction(OneWireContainer18.SIGN_DATA_PAGE, addr))
      {
         //get the MAC from the scratchpad
         ibcL.readScratchPad(signScratchpad, 0);

         //copy the MAC into the accountData page
         System.arraycopy(signScratchpad, 8, mac_buffer, macStart, 20);

         ibcL.useResume(false);
         return true;
      }
      else
         this.lastError = SHAiButtonCopr.SHA_FUNCTION_FAILED;

      ibcL.useResume(false);
      return false;
   }

   //prevent malloc'ing in the critical path
   private byte[] generateChallenge_scratchpad = new byte[32];

   /**
    * <p>Generates a 3 byte random challenge in the iButton, sufficient to be used
    * as a challenge to be answered by a User iButton.  The user answers the challenge
    * with an authenticated read of it's account data.</p>
    *
    * <p>The DS1963S will generate 20 bytes of pseudo random data, though only
    * 3 bytes are needed for the challenge.  Programs can add more 'randomness'
    * by selecting different bytes from the 20 bytes of random data using the
    * <code>offset</code> parameter.</p>
    *
    * <p>The random number generator is actually the DS1963S's SHA engine, which requires
    * page data to compute a hash.  Select a page number with the <code>page_number</code>
    * parameter.</p>
    *
    * @param offset offset into the 20 random bytes to draw random data from
    *        (must be in range 0-16)
    * @param ch buffer for the challenge to be returned (must be of length 3 or more)
    * @param start the starting index into array <code>ch</code> to begin copying
    *        the challenge bytes.
    *
    * @return <code>true</code> if successful, <code>false</code> if an error
    *         occurred  (use <code>getLastError()</code> for more
    *         information on the type of error)
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see SHAiButtonUser#readAccountData(byte[],int,byte[],int,byte[],int)
    * @see #getLastError()
    */
   public synchronized boolean generateChallenge (int offset, byte[] ch, int start)
      throws OneWireIOException, OneWireException
   {
      //clear any errors
      this.lastError = SHAiButtonCopr.NO_ERROR;

      //maintain local reference to container
      OneWireContainer18 ibcL = this.ibc;
      byte[] scratchpad       = this.generateChallenge_scratchpad;
      int    addr             = authPageNumber << 5;

      if (ibcL.eraseScratchPad(authPageNumber))
      {
         //ibcL.useResume(true);

         if (ibcL.SHAFunction(OneWireContainer18.COMPUTE_CHALLENGE, addr))
         {
            //get the mac from the scratchpad
            ibcL.readScratchPad(scratchpad, 0);

            //copy the requested 3 return bytes
            System.arraycopy(scratchpad, 8 + (offset % 17), ch, start, 3);

            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
            if(DEBUG)
            {
               IOHelper.writeLine("-----------------------------------------------------------");
               IOHelper.writeLine("COPR DEBUG");
               IOHelper.writeLine("address:");
               IOHelper.writeLine("speed: " + this.ibc.getAdapter().getSpeed());
               IOHelper.writeBytesHex(address, 0, 8);
               IOHelper.writeLine("Challenge:");
               IOHelper.writeBytesHex(ch, start, 3);
               ch[start] = (byte)0x01;
               ch[start+1] = (byte)0x02;
               ch[start+2] = (byte)0x03;
               IOHelper.writeBytesHex(ch, start, 3);
               IOHelper.writeLine("-----------------------------------------------------------");
            }
            //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
            ibcL.useResume(false);
            return true;
         }
         else
            this.lastError = SHAiButtonCopr.SHA_FUNCTION_FAILED;
      }
      else
         this.lastError = SHAiButtonCopr.ERASE_SCRATCHPAD_FAILED;

      ibcL.useResume(false);
      return false;
   }


   /**
    * <p>Determines if a <code>SHAiButtonUser</code> belongs to the system
    * defined by this Coprocessor iButton.See the usage example in this
    * class for initializing a Coprocessor iButton.</p>
    *
    * <p>The first step in user authentication is to recreate the user's
    * unique secret on the coprocessor button using
    * <code>bindSecretToiButton(int,byte[],byte[],int)</code>.  Then the
    * coprocessor signs the pageData to produce a MAC.  If the MAC matches
    * that produced by the user, the user belongs to the system.</p>
    *
    * <p>The TMEX formatted page with the user's account data is in the
    * 32-byte parameter <code>pageData</code>.  If the verification
    * is successful, the data data signature must still be verified with
    * the <code>verifySignature()</code> method.</p>
    *
    * <p>Failure of this method does not necessarily mean that
    * the User iButton does not belong to the system.  It is possible that
    * a communication disruption here could cause a CRC error that
    * would be indistinguishable from a failed authentication.  However,
    * repeated attempts should reveal whether it was truly a communication
    * problem or a User iButton that does not belong to the system.</p>
    *
    * @param fullBindCode 15-byte binding code used to recreate user iButtons
    *        unique secret in the coprocessor.
    * @param pageData 32-byte buffer containing the data page holding the user's
    *        account data.
    * @param scratchpad the 32-byte scratchpad contents for which the
    *        signature is generated.  This will contain parameters such
    *        as the user's write cycle counter for the page, the user's
    *        1-wire address, and the page number where account data is
    *        stored.
    * @param verify_mac the 20-byte buffer containing the user's authentication
    *        response to the coprocessor's challenge.
    *
    * @return <code>true</code> if the operation was successful and the user's
    *         MAC matches that generated by the coprocessor.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #generateChallenge(int,byte[],int)
    * @see #verifySignature(byte[],byte[],byte[])
    * @see OneWireContainer18#bindSecretToiButton(int,byte[],byte[],int)
    * @see OneWireContainer18#SHAFunction(byte,int)
    * @see OneWireContainer18#matchScratchPad(byte[])
    * @see #getLastError()
    */
   public boolean verifyAuthentication(byte[] fullBindCode,
                                       byte[] pageData,
                                       byte[] scratchpad,
                                       byte[] verify_mac,
                                       byte   authCmd)
      throws OneWireIOException, OneWireException
   {
      //clear any errors
      this.lastError = SHAiButtonCopr.NO_ERROR;

      //maintain local reference to container
      OneWireContainer18 ibcL = this.ibc;
      int addr = this.wspcPageNumber << 5;
      int wspc = this.wspcPageNumber;

      //recreate the user's secret on the coprocessor.
      if(!ibcL.bindSecretToiButton(this.authPageNumber,
                                   this.bindData, fullBindCode,
                                   wspc))
      {
         this.lastError = SHAiButtonCopr.BIND_SECRET_FAILED; //bind secret failed
         return false;
      }

      ibcL.useResume(true);

      //\\//\\//\\//\\//\\//\\//\\////\\////\\////\\////\\////\\////\\//
      if(DEBUG)
      {
         IOHelper.writeLine("-----------------------------------------------------------");
         IOHelper.writeLine("COPR DEBUG - verifyAuthentication");
         IOHelper.write("address: ");
         IOHelper.writeBytesHex(address, 0, 8);
         IOHelper.writeLine("speed: " + this.ibc.getAdapter().getSpeed());
         IOHelper.writeLine("pageData");
         IOHelper.writeBytesHex(pageData);
         IOHelper.writeLine("scratchpad");
         IOHelper.writeBytesHex(scratchpad);
         IOHelper.writeLine("authCmd: " + authCmd);
         IOHelper.writeLine("bindData: ");
         IOHelper.writeBytesHex(bindData);
         IOHelper.writeLine("fullBindCode: ");
         IOHelper.writeBytesHex(fullBindCode);
         IOHelper.writeLine("-----------------------------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\////\\////\\////\\////\\////\\////\\//

      //write the account data
      if(!ibcL.writeDataPage(wspc, pageData))
      {
         this.lastError = SHAiButtonCopr.WRITE_DATA_PAGE_FAILED;
         ibcL.useResume(false);
         return false;
      }

      //write the scratchapd data
      if(!ibcL.writeScratchPad(wspc, 0,
                               scratchpad, 0, 32))
      {
         this.lastError = SHAiButtonCopr.WRITE_SCRATCHPAD_FAILED;
         ibcL.useResume(false);
         return false;
      }

      //generate the MAC
      if(ibcL.SHAFunction(authCmd, addr))
      {
         if(ibcL.matchScratchPad(verify_mac))
         {
            ibcL.useResume(false);
            return true;
         }
         else
            this.lastError = SHAiButtonCopr.MATCH_SCRATCHPAD_FAILED;
      }
      else
         this.lastError = SHAiButtonCopr.SHA_FUNCTION_FAILED;

      ibcL.useResume(false);

      return false;
   }

   /**
    * <P>Verifies a User iButton's signed data on this Coprocessor iButton.
    * The Coprocessor must recreate the signature based on the data in the
    * file and the contents of the given scratchpad, and then match that
    * with the signature passed in verify_mac.</P>
    *
    * @param pageData the full 32 byte TMEX file from the User iButton
    *        (from <code>verifyAuthentication</code>) with the
    * @param scratchpad the 32-byte scratchpad contents for which the
    *        signature is generated.  This will contain parameters such
    *        as the user's write cycle counter for the page, the user's
    *        1-wire address, and the page number where account data is
    *        stored.
    * @param verify_mac the 20-byte buffer containing the signature the user
    *        had stored with the account data file.
    *
    * @return <code>true<code> if the data file is valid, <code>false</code>
    *         if an error occurred (use <code>getLastError()</code> for more
    *         information on the type of error)
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #verifyAuthentication(byte[],byte[],byte[],byte[],byte)
    * @see #getLastError()
    */
   public boolean verifySignature(byte[] pageData, byte[] scratchpad,
                                  byte[] verify_mac)
      throws OneWireIOException, OneWireException
   {
      //clear any errors
      this.lastError = SHAiButtonCopr.NO_ERROR;

      //maintain local reference to container
      OneWireContainer18 ibcL = this.ibc;
      int addr = this.signPageNumber << 5;

      //now we are ready to make a signature
      if (!ibcL.writeDataPage(this.signPageNumber, pageData))
      {
         this.lastError = SHAiButtonCopr.WRITE_DATA_PAGE_FAILED;
         ibcL.useResume(false);
         return false;
      }

      ibcL.useResume(true);
      if (!ibcL.writeScratchPad(0, 0, scratchpad, 0, 32))
      {
         this.lastError = SHAiButtonCopr.WRITE_SCRATCHPAD_FAILED;
         ibcL.useResume(false);
         return false;
      }

      //\\//\\//\\//\\//\\//\\//\\////\\////\\////\\////\\////\\////\\//
      if(DEBUG)
      {
         IOHelper.writeLine("-----------------------------------------------------------");
         IOHelper.writeLine("COPR DEBUG - verifySignature");
         IOHelper.write("address: ");
         IOHelper.writeBytesHex(address, 0, 8);
         IOHelper.writeLine("speed: " + this.ibc.getAdapter().getSpeed());
         IOHelper.writeLine("-----------------------------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\////\\////\\////\\////\\////\\////\\//

      //sign that baby!
      if (ibcL.SHAFunction(OneWireContainer18.VALIDATE_DATA_PAGE, addr))
      {
         if (ibcL.matchScratchPad(verify_mac))
         {
            ibcL.useResume(false);
            return true;
         }
         else
            this.lastError = SHAiButtonCopr.MATCH_SCRATCHPAD_FAILED;
      }
      else
         this.lastError = SHAiButtonCopr.SHA_FUNCTION_FAILED;

      ibcL.useResume(false);
      return false;
   }

   // ***********************************************************************
   // End SHA iButton Methods
   // ***********************************************************************


   /**
    * Returns a string representing the 1-Wire address of this SHAiButton.
    *
    * @return a string containing the 8-byte address of this 1-Wire device.
    */
   public String toString ()
   {
      return "COPR: "+this.ibc.getAddressAsString() +
             ", provider: " + this.providerName +
             ", version: " + this.version;
   }


   /**
    * Configuration helper.  Used also by Coprocessor VM
    */
   protected void fromStream(InputStream is)
      throws IOException
   {
      is.read(this.filename,0,5);

      this.signPageNumber = is.read();
      this.authPageNumber = is.read();
      this.wspcPageNumber = is.read();

      this.version = is.read();

      is.skip(4);//skip date info

      is.read(this.bindData,0,32);
      is.read(this.bindCode,0,7);
      is.read(this.signingChallenge,0,3);

      int namelen = is.read();
      int siglen = is.read();
      int auxlen = is.read();

      byte[] l_providerName = new byte[namelen];
      is.read(l_providerName);
      this.providerName = new String(l_providerName);

      int cnt = Math.min(this.initialSignature.length, siglen);
      is.read(this.initialSignature,0,cnt);

      byte[] l_auxData = new byte[auxlen];
      is.read(l_auxData);
      this.auxData = new String(l_auxData);

      this.encCode = is.read();
      this.DS1961Scompatible = (is.read()!=0);
   }

   /**
    * Configuration saving method. Used also by Coprocessor VM
    */
   protected void toStream(OutputStream os)
      throws IOException
   {
      //first part written is completely standard format
      os.write(this.filename,0,5);
      os.write(this.signPageNumber);
      os.write(this.authPageNumber);
      os.write(this.wspcPageNumber);
      os.write(this.version);

      //month, date, and year ignored
      os.write(1); os.write(1);
      os.write(0); os.write(100);

      os.write(this.bindData);
      os.write(this.bindCode);
      os.write(this.signingChallenge);

      byte[] l_providerName = this.providerName.getBytes();
      byte[] l_auxData = this.auxData.getBytes();
      os.write((byte)l_providerName.length);
      os.write((byte)this.initialSignature.length);
      os.write((byte)l_auxData.length);
      os.write(l_providerName,0,(byte)l_providerName.length);
      os.write(this.initialSignature,0,(byte)this.initialSignature.length);
      os.write(l_auxData,0,(byte)l_auxData.length);
      os.write(this.encCode);
      os.write(this.DS1961Scompatible?0x55:0x00);

      os.flush();
   }


   // ***********************************************************************
   // Begin Static Utility Methods
   // ***********************************************************************

   /**
    * <P>Static method that reformats the inputted authentication secret
    * so it is compatible with the DS1961S.  This means that for every
    * group of 47 bytes in the secret, bytes at indices 32-35 and indices
    * 44-46 are all set to 0xFF.  Check the format for secret generation
    * in the DS1961S data sheet to verify format of digest buffer.</P>
    *
    * <P>Note that if a coprocessor button uses this formatted secret,
    * this function should be called for all user buttons including the
    * DS1963S and DS1961S to ensure compatibility</P>
    *
    * @param auth_secret the authentication secret to be reformatted.
    *
    * @return a reformatted authentication secret, with the appropriate
    *         padding for DS1961S interaction.
    */
   public static byte[] reformatFor1961S(byte[] auth_secret)
   {
      int numPartials = (auth_secret.length/47) + 1;
      byte[] new_secret = new byte[47*numPartials];

      for(int i=0; i<numPartials; i++)
      {
         int cnt = Math.min(auth_secret.length - (i*47), 47);
         System.arraycopy(auth_secret, i*47, new_secret, i*47, cnt);
         new_secret[i*47 + 32] = (byte)0xFF;
         new_secret[i*47 + 33] = (byte)0xFF;
         new_secret[i*47 + 34] = (byte)0xFF;
         new_secret[i*47 + 35] = (byte)0xFF;
         new_secret[i*47 + 44] = (byte)0xFF;
         new_secret[i*47 + 45] = (byte)0xFF;
         new_secret[i*47 + 46] = (byte)0xFF;
      }
      return new_secret;
   }
}

