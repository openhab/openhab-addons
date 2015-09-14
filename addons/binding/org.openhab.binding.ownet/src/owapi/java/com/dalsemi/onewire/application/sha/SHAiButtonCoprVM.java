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

import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.OneWireContainer18;
import com.dalsemi.onewire.utils.Address;
import com.dalsemi.onewire.utils.IOHelper;
import com.dalsemi.onewire.utils.SHA;
import com.dalsemi.onewire.application.file.OWFileOutputStream;
import com.dalsemi.onewire.application.file.OWFileInputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;

/**
 * <P>Class for simulating an instance of a SHA iButton Coprocessor involved
 * in SHA Transactions.  The Coprocessor is used for digitally signing transaction
 * data as well as generating random challenges for users and verifying
 * their response.</P>
 *
 * <p>With this class, no DS1963S SHA iButton is necessary for the coprocessor in
 * SHA Transactions.  The simulated Coprocessor iButton verifies signatures
 * and signs data for User iButtons.</P>
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
 * @see SHAiButtonCopr
 *
 * @version 1.00
 * @author  SKH
 */
public class SHAiButtonCoprVM
   extends SHAiButtonCopr
{
   /**
    * 8 8-byte Secrets for this simulated SHAiButton
    */
   protected byte[][] secretPage = new byte[8][8];

   /**
    * 1-Wire Address for this simulated device
    */
   protected byte[] address = new byte[8];

   // ***********************************************************************
   // Transient Data Members
   // ***********************************************************************

   //Temporary 512-bit buffer used for digest computation
   private static final byte[] digestBuff = new byte[64];

   //used for compute first secret
   private static final byte[] NullSecret
            = new byte[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};

   //used for generate challenge and random RomID
   private static java.util.Random rand = new java.util.Random();

   // ***********************************************************************
   // Class Constructors
   // ***********************************************************************

   /**
    * <P>Sets up this simulated coprocessor based on the provided parameters.
    * Then, the system secret and authentication secret are installed on the
    * simulated coprocessor iButton.</P>
    *
    * <P>For the proper format of the coprocessor data file, see the
    * document entitled "Implementing Secured D-Identification and E-Payment
    * Applications using SHA iButtons".  For the format of TMEX file
    * structures, see Application Note 114.</P>
    *
    * @param RomID The address for the simulated coprocessor.
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
    * @see #SHAiButtonCoprVM(String)
    * @see #SHAiButtonCoprVM(String,byte[],byte[])
    * @see #SHAiButtonCoprVM(OneWireContainer,String)
    * @see #SHAiButtonCoprVM(OneWireContainer,String,byte[],byte[])
    * @see #SHAiButtonCoprVM(OneWireContainer18,String,byte[],byte[])
    */
   public SHAiButtonCoprVM(byte[] RomID, int l_signPageNumber,
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

      //set up all the appropriate members
      System.arraycopy(RomID,0,this.address,0,8);
      this.signPageNumber = l_signPageNumber;
      this.authPageNumber = l_authPageNumber;
      this.wspcPageNumber = l_wspcPageNumber;
      this.version = l_version;
      this.encCode = l_encCode;
      System.arraycopy(l_serviceFilename,0,this.filename,0,4);
      this.filename[4] = l_serviceFileExt;
      this.providerName = new String(l_providerName);
      System.arraycopy(l_bindData,0,this.bindData,0,32);
      System.arraycopy(l_bindCode,0,this.bindCode,0,7);
      this.auxData = new String(l_auxData);
      System.arraycopy(l_initialSignature,0,this.initialSignature,0,20);
      System.arraycopy(l_signingChlg,0,this.signingChallenge,0,3);

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

      //Install the system signing secret, used to sign and validate all user data
      if(!installMasterSecret(signPageNumber, l_signingSecret, signPageNumber&7))
         throw new OneWireIOException("failed to install system signing secret");

      //Install the system authentication secret, used to authenticate users
      if(!installMasterSecret(authPageNumber, l_authSecret, authPageNumber&7))
         throw new OneWireIOException("failed to install authentication secret");
   }

   /**
    * <p>Loads a simulated DS1963S coprocessor device from disk.  The given
    * file name is loaded to get all the parameters of the coprocessor.
    * It is assumed that the secrets were stored in the file when
    * the simulated coprocessor's data was saved to disk.</p>
    *
    * @param filename The filename of the simulated coprocessor's data file ("shaCopr.dat")
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #SHAiButtonCoprVM(String,byte[],byte[])
    * @see #SHAiButtonCoprVM(OneWireContainer,String)
    * @see #SHAiButtonCoprVM(OneWireContainer,String,byte[],byte[])
    * @see #SHAiButtonCoprVM(OneWireContainer18,String,byte[],byte[])
    * @see #SHAiButtonCoprVM(byte[],int,int,int,int,int,byte,byte[],byte[],byte[],byte[],byte[],byte[],byte[],byte[],byte[])
    *
    */
   public SHAiButtonCoprVM(String filename)
      throws OneWireException, OneWireIOException
   {
      if(!load(filename))
         throw new OneWireIOException("failed to load config info");
   }

   /**
    * <p>Loads a simulated DS1963S coprocessor device from disk.  The given
    * file name is loaded to get all the parameters of the coprocessor.
    * After it is loaded, the given secrets are installed.</p>
    *
    * @param filename The filename of the simulated coprocessor's data file ("shaCopr.dat")
    * @param sign_secret The system data signing secret.
    * @param auth_secret The system device authentication secret.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #SHAiButtonCoprVM(String)
    * @see #SHAiButtonCoprVM(OneWireContainer,String)
    * @see #SHAiButtonCoprVM(OneWireContainer,String,byte[],byte[])
    * @see #SHAiButtonCoprVM(OneWireContainer18,String,byte[],byte[])
    * @see #SHAiButtonCoprVM(byte[],int,int,int,int,int,byte,byte[],byte[],byte[],byte[],byte[],byte[],byte[],byte[],byte[])
    */
   public SHAiButtonCoprVM(String filename,
                           byte[] sign_secret, byte[] auth_secret)
      throws OneWireException, OneWireIOException
   {
      if(!load(filename))
         throw new OneWireIOException("failed to load config info");
      if(!installMasterSecret(signPageNumber, sign_secret, signPageNumber&7))
         throw new OneWireIOException("failed to install system signing secret");
      if(!installMasterSecret(authPageNumber, auth_secret, authPageNumber&7))
         throw new OneWireIOException("failed to install authentication secret");
   }

   /**
    * <p>Loads a simulated DS1963S coprocessor device from any 1-Wire memory device
    * supported by the 1-Wire File I/O API.  The given file name is loaded to get
    * all the parameters of the coprocessor.  It is assumed that the secrets were
    * stored in the file when the simulated coprocessor's data was saved to disk.</p>
    *
    * @param owc 1-Wire memory device with valid TMEX file system
    * @param filename The filename of the simulated coprocessor's data file ("shaCopr.dat")
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #SHAiButtonCoprVM(String)
    * @see #SHAiButtonCoprVM(String,byte[],byte[])
    * @see #SHAiButtonCoprVM(OneWireContainer,String,byte[],byte[])
    * @see #SHAiButtonCoprVM(OneWireContainer18,String,byte[],byte[])
    * @see #SHAiButtonCoprVM(byte[],int,int,int,int,int,byte,byte[],byte[],byte[],byte[],byte[],byte[],byte[],byte[],byte[])
    */
   public SHAiButtonCoprVM(OneWireContainer owc, String filename)
      throws OneWireException, OneWireIOException
   {
      if(!load(owc,filename))
         throw new OneWireIOException("failed to load config info");
   }

   /**
    * <p>Loads a simulated DS1963S coprocessor device from any 1-Wire
    * memory device supported by the 1-Wire File I/O API.  The given
    * file name is loaded to get all the parameters of the coprocessor.
    * After it is loaded, the given secrets are installed.</p>
    *
    * @param owc 1-Wire memory device with valid TMEX file system
    * @param filename The filename of the simulated coprocessor's data file ("shaCopr.dat")
    * @param sign_secret The system data signing secret.
    * @param auth_secret The system device authentication secret.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #SHAiButtonCoprVM(String)
    * @see #SHAiButtonCoprVM(String,byte[],byte[])
    * @see #SHAiButtonCoprVM(OneWireContainer,String)
    * @see #SHAiButtonCoprVM(OneWireContainer18,String,byte[],byte[])
    * @see #SHAiButtonCoprVM(byte[],int,int,int,int,int,byte,byte[],byte[],byte[],byte[],byte[],byte[],byte[],byte[],byte[])
    */
   public SHAiButtonCoprVM(OneWireContainer owc, String filename,
                           byte[] sign_secret, byte[] auth_secret)
      throws OneWireException, OneWireIOException
   {
      if(!load(owc,filename))
         throw new OneWireIOException("failed to load config info");
      if(!installMasterSecret(signPageNumber, sign_secret, signPageNumber&7))
         throw new OneWireIOException("failed to install system signing secret");
      if(!installMasterSecret(authPageNumber, auth_secret, authPageNumber&7))
         throw new OneWireIOException("failed to install authentication secret");
   }

   /**
    * <p>Simulates a specific DS1963S coprocessor device.  First, the given
    * TMEX file name is loaded of the container to get all the parameters of
    * the coprocessor.  Then (since secrets are not readable off the iButton,
    * they must be provided) the secrets are installed on the virtual
    * coprocessor.</p>
    *
    * @param owc The coprocessor button this VM will simulate.
    * @param filename The TMEX filename of the coprocessor service file ("COPR.0")
    * @param sign_secret The system data signing secret.
    * @param auth_secret The system device authentication secret.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @see #SHAiButtonCoprVM(String)
    * @see #SHAiButtonCoprVM(String,byte[],byte[])
    * @see #SHAiButtonCoprVM(OneWireContainer,String)
    * @see #SHAiButtonCoprVM(OneWireContainer,String,byte[],byte[])
    * @see #SHAiButtonCoprVM(byte[],int,int,int,int,int,byte,byte[],byte[],byte[],byte[],byte[],byte[],byte[],byte[],byte[])
    */
   public SHAiButtonCoprVM(OneWireContainer18 owc, String filename,
                           byte[] sign_secret, byte[] auth_secret)
      throws OneWireException, OneWireIOException
   {
      if(!load(owc,filename))
         throw new OneWireIOException("failed to load config info");
      if(!installMasterSecret(signPageNumber, sign_secret, signPageNumber&7))
         throw new OneWireIOException("failed to install system signing secret");
      if(!installMasterSecret(authPageNumber, auth_secret, authPageNumber&7))
         throw new OneWireIOException("failed to install authentication secret");
   }

   // ***********************************************************************
   // End Constructors
   // ***********************************************************************

   // ***********************************************************************
   // Save and Load methods for serializing all data
   // ***********************************************************************

   /**
    * <p>Saves simulated coprocessor configuration info to an (almost)
    * standard-format to a hard drive file.</p>
    *
    * @param filename The filename of the simulated coprocessor's data
    *        file ("shaCopr.dat")
    * @param saveSecretData If <code>true</true>, the raw secret information
    *        is also written to the file
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @return <code>true</code> if the info was successfully saved
    */
   public boolean save(String filename, boolean saveSecretData)
      throws OneWireException, OneWireIOException
   {
      try
      {
         //Create the configuration file
         FileOutputStream fos = new FileOutputStream(filename);

         //write the data out to the config file
         toStream(fos);

         //non-standard additions
         fos.write(address,0,8);
         for(int i=0; i<8; i++)
         {
            if(saveSecretData)
               fos.write(secretPage[i]);
            else
               fos.write(NullSecret);
         }
         fos.flush();
         fos.close();

         return true;
      }
      catch(Exception e)
      {
         return false;
      }
   }

   /**
    * <p>Saves simulated coprocessor configuration info to an (almost)
    * standard-format to a 1-Wire Memory Device's TMEX file.</p>
    *
    * @param owc 1-Wire Memory Device with valid TMEX file structure.
    * @param filename The TMEX filename of the simulated coprocessor's data
    *        file ("COPR.2")
    * @param saveSecretData If <code>true</true>, the raw secret information
    *        is also written to the file.
    *
    * @throws OneWireIOException on a 1-Wire communication error such as
    *         reading an incorrect CRC from a 1-Wire device.  This could be
    *         caused by a physical interruption in the 1-Wire Network due to
    *         shorts or a newly arriving 1-Wire device issuing a 'presence pulse'.
    * @throws OneWireException on a communication or setup error with the 1-Wire
    *         adapter
    *
    * @return <code>true</code> if the info was successfully saved
    */
   public boolean save(OneWireContainer owc, String filename,
                       boolean saveSecretData)
      throws OneWireException, OneWireIOException
   {
      try
      {
         //Create the configuration file
         OWFileOutputStream fos = new OWFileOutputStream(owc, filename);

         //write the data out
         toStream(fos);

         //non-standard additions
         fos.write(address,0,8);
         for(int i=0; i<8; i++)
         {
            if(saveSecretData)
               fos.write(secretPage[i]);
            else
               fos.write(NullSecret);
         }
         fos.flush();
         fos.close();

         return true;
      }
      catch(Exception ioe)
      {
         return false;
      }
   }

   /**
    * <p>Loads coprocessor configuration information from an (almost) standard
    * service file on hard drive. If secret information was saved, this routine
    * automatically loads it.</P>
    *
    * @param filename The filename of the simulated coprocessor's data
    *        file ("shaCopr.dat")
    *
    * @return <code>true</code> if the info was successfully loaded
    */
   public boolean load(String filename)
   {
      try
      {
         //open the file containing config info
         FileInputStream fis = new FileInputStream(filename);

         //load info from the file stream
         fromStream(fis);

         //non-standard file components
         if(fis.available()>0)
         {
            fis.read(this.address,0,8);
            for(int i=0; i<8 && fis.available()>0; i++)
            {
               fis.read(secretPage[i]);
            }
         }
         fis.close();
         return true;
      }
      catch(Exception e)
      {
         return false;
      }
   }

   /**
    * <p>Loads coprocessor configuration information from an (almost) standard
    * service TMEX file on 1-Wire memory device. If secret information was saved,
    * this routine automatically loads it.</P>
    *
    * @param owc 1-Wire memory device with valid TMEX file structure
    * @param filename The TMEX filename of the simulated coprocessor's data
    *        file ("COPR.2")
    *
    * @return <code>true</code> if the info was successfully loaded
    */
   public boolean load(OneWireContainer owc, String filename)
   {
      try
      {
         //open the file containing config info
         OWFileInputStream fis = new OWFileInputStream(owc,filename);

         //load info from the file stream
         fromStream(fis);

         //non-standard file components
         if(fis.available()>0)
         {
            fis.read(this.address,0,8);
            for(int i=0; i<8 && fis.available()>0; i++)
            {
               fis.read(secretPage[i]);
            }
         }
         fis.close();
         return true;
      }
      catch(Exception e)
      {
         return false;
      }
   }

   /**
    * <p>Loads coprocessor configuration information from a standard TMEX
    * service file on a DS1963S.</P>
    *
    * @param owc DS1963S set up as a valid coprocessor
    * @param filename The TMEX filename of the coprocessor's data
    *        file ("COPR.0")
    *
    * @return <code>true</code> if the info was successfully loaded
    */
   public boolean load(OneWireContainer18 owc, String filename)
   {
      try
      {
         //open the file containing config info
         OWFileInputStream fis = new OWFileInputStream(owc,filename);

         //load info from the file stream
         fromStream(fis);

         //non-standard components
         System.arraycopy(owc.getAddress(),0,this.address,0,8);

         fis.close();
         return true;
      }
      catch(Exception e)
      {
         e.printStackTrace();
         return false;
      }
   }
   // ***********************************************************************
   // End Save and Load methods
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
                                      byte[] mac_buffer,
                                      int    macStart)
      throws OneWireException, OneWireIOException
   {
      //clear any errors
      this.lastError = SHAiButtonCopr.NO_ERROR;

      if(SHAFunction(OneWireContainer18.SIGN_DATA_PAGE,
                      secretPage[signPageNumber&7],
                      accountData,
                      signScratchpad,
                      null,
                      signPageNumber,
                      -1))
      {
         System.arraycopy(signScratchpad, 8, mac_buffer, macStart, 20);
         return true;
      }

      this.lastError = SHAiButtonCopr.SHA_FUNCTION_FAILED;
      return false;
   }

   //prevent malloc'ing in the critical path
   private byte[] generateChallenge_chlg = new byte[20];

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

      SHAiButtonCoprVM.rand.nextBytes(this.generateChallenge_chlg);

      System.arraycopy(this.generateChallenge_chlg,offset, ch,start, 3);

      return true;
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
    * @see #bindSecretToiButton(int,byte[],byte[],int)
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
      int secretNum = this.wspcPageNumber&7;

      //set Workspace Secret
      bindSecretToiButton(authPageNumber,
                          this.bindData, fullBindCode,
                          secretNum);

      if(SHAFunction( authCmd,
                      secretPage[secretNum],
                      pageData,
                      scratchpad,
                      null,
                      wspcPageNumber,
                      -1))
      {
         for(int i=0; i<20; i++)
         {
            if( scratchpad[i+8]!=verify_mac[i] )
            {
               this.lastError = SHAiButtonCopr.MATCH_SCRATCHPAD_FAILED;
               return false;
            }
         }
         return true;
      }
      this.lastError = SHAiButtonCopr.SHA_FUNCTION_FAILED;
      return false;

   }

   /**
    * <p>Creates a data signature, but instead of using the signing secret,
    * it uses the authentication secret, bound for a particular button.</p>
    *
    * <P><code>fullBindCode</code> is ignored by the Coprocessor VM.  Instead
    * of binding the secret to the signing page, the coprocessor VM "cheats"
    * and lets you sign the workspace page, where (presumably) the secret is
    * already bound.</p>
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
    * @param fullBindCode ignored by simulated coprocessor

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

      if(SHAFunction(OneWireContainer18.SIGN_DATA_PAGE,
                      secretPage[wspcPageNumber&7],
                      accountData,
                      signScratchpad,
                      null,
                      signPageNumber,
                      -1))
      {
         System.arraycopy(signScratchpad, 8, mac_buffer, macStart, 20);
         return true;
      }

      this.lastError = SHAiButtonCopr.SHA_FUNCTION_FAILED;
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

      if(SHAFunction(OneWireContainer18.VALIDATE_DATA_PAGE,
                      this.secretPage[signPageNumber&7],
                      pageData,
                      scratchpad,
                      this.address,
                      signPageNumber,
                      -1))
      {
         for(int i=0; i<20; i++)
         {
            if( scratchpad[i+8]!=verify_mac[i] )
            {
               this.lastError = SHAiButtonCopr.MATCH_SCRATCHPAD_FAILED;
               return false;
            }
         }
         return true;
      }
      this.lastError = SHAiButtonCopr.SHA_FUNCTION_FAILED;
      return false;
   }

   private byte[] bindSecretToiButton_scratchpad = new byte[32];
   /**
    * <p>Binds an installed secret to this virtual DS1963S by using
    * well-known binding data and this DS1963S's (unique?)
    * address.  This makes the secret unique
    * for this iButton.  Coprocessor iButtons use this method
    * to recreate the iButton's secret to verify authentication.
    * Roving iButtons use this method to finalize their secret keys.</p>
    *
    * <p>Note that unlike in the <code>installMasterSecret()</code> method,
    * the page number does not need to be equivalent to the <code>secret_number</code>
    * modulo 8.  The new secret (installed secret + binding code) is generated
    * from this page but can be copied into another secret.  User iButtons should
    * bind to the same page the secret was installed on.  Coprocessor iButtons
    * must copy to a new secret to preserve the general system authentication
    * secret.</p>
    *
    * <p>The binding should be either 7 bytes long or 15 bytes long.
    * A 15-length <code>byte</code> array is unaltered and placed in the scratchpad
    * for the binding.  A 7-length <code>byte</code> array is combined with the page
    * number and DS1963S unique address and then placed in the scratchpad.
    * Coprocessors should use a pre-formatted 15-length <code>byte</code> array.
    * User iButtons should let the method format for them (i.e.
    * use the 7-length <code>byte</code> array option).</p>
    *
    * @param page the page number that has the master secret already installed
    * @param bind_data 32 bytes of binding data used to bind the iButton to the system
    * @param bind_code the 7-byte or 15-byte binding code
    * @param secret_number secret number to copy the resulting secret to
    *
    * @return <code>true</code> if successful
    *
    * @see #installMasterSecret(int,byte[],int)
    */
   public synchronized boolean bindSecretToiButton (int pageNum,
           byte[] bindData, byte[] bindCode, int secretNum)
   {
      //local vars
      byte[] scratchpad = this.bindSecretToiButton_scratchpad;

      //write the bind_code to the scratchpad
      if(bindCode.length==7)
      {
         System.arraycopy(bindCode,0,scratchpad,8,4);
         scratchpad[12] = (byte)pageNum;
         System.arraycopy(this.address,0,scratchpad,13,7);
         System.arraycopy(bindCode,4,scratchpad,20,3);
      }
      else
      {
         System.arraycopy(bindCode, 0, scratchpad, 8,
                          (bindCode.length>15 ? 15
                                              : bindCode.length));
      }

      //compute the MAC
      if(!SHAFunction(OneWireContainer18.COMPUTE_NEXT_SECRET,
                      secretPage[pageNum&7], bindData, scratchpad,
                      null, pageNum, 0))
         return false;

      //install the secret
      System.arraycopy(scratchpad,0,secretPage[secretNum&7],0,8);

      return true;
   }



   /**
    * <p>Installs a secret on this virtual DS1963S.  The secret is written in partial phrases
    * of 47 bytes (32 bytes to a memory page, 15 bytes to the scratchpad) and
    * is cumulative until the entire secret is processed.  Secrets are associated
    * with a page number.  See the datasheet for more information on this
    * association.</p>
    *
    * <p>In most cases, <code>page</code> should be equal to <code>secret_number</code>
    * or <code>secret_number+8</code>, based on the association of secrets and page numbers.
    * A secret is 8 bytes and there are 8 secrets.  These 8 secrets are associated with the
    * first 16 pages of memory.</p>
    *
    * <p>On TINI, this method will be slightly faster if the secret's length is divisible by 47.
    * However, since secret key generation is a part of initialization, it is probably
    * not necessary.</p>
    *
    * @param page the page number used to write the partial secrets to
    * @param secret the entire secret to be installed
    * @param secret_number the secret 'page' to use (0 - 7)
    *
    * @return <code>true</code> if successful
    *
    * @see #bindSecretToiButton(int,byte[],byte[],int)
    */
   public boolean installMasterSecret(int pageNum, byte[] secret, int secretNum)
   {
      //47 is a magic number here because every time a partial secret
      //is to be computed, 32 bytes goes in the page and 15 goes in
      //the scratchpad, so it's going to be easier in the computations
      //if i know the input buffer length is divisible by 47
      if (secret.length == 0)
         return false;

      byte[] input_secret      = null;
      int    secret_mod_length = secret.length % 47;

      if (secret_mod_length == 0)   //if the length of the secret is divisible by 47
         input_secret = secret;
      else
      {

         /* i figure in the case where secret is not divisible by 47
            it will be quicker to just create a new array once and
            copy the data in, rather than on every partial secret
            calculation do bounds checking */
         input_secret = new byte [secret.length + (47 - secret_mod_length)];

         System.arraycopy(secret, 0, input_secret, 0, secret.length);
      }

      //the current offset into the input_secret buffer
      secretNum = secretNum&7;
      int offset = 0;
      byte cmd = OneWireContainer18.COMPUTE_FIRST_SECRET;
      byte[] scratchpad = new byte[32];
      byte[] dataPage = new byte[32];
      while (offset < input_secret.length)
      {
         for(int i=0; i<32; i++)
            scratchpad[i] = (byte)0x0FF;

         System.arraycopy(input_secret,offset,dataPage,0,32);
         System.arraycopy(input_secret,offset+32,scratchpad,8,15);
         if(!SHAFunction(cmd, secretPage[pageNum&7], dataPage,
                         scratchpad, null, signPageNumber, 0))
         {
            return false;
         }

         //install the secret
         System.arraycopy(scratchpad,0,secretPage[secretNum],0,8);

         offset += 47;
         cmd = OneWireContainer18.COMPUTE_NEXT_SECRET;
      }

      return true;
   }
   /**
    * <p>Performs one of the DS1963S's cryptographic functions on this
    * virtual SHA iButton.  See the datasheet for more information on
    * these functions.</p>
    *
    * <p>Valid parameters for the <code>function</code> argument are:
    * <ul>
    *    <li> COMPUTE_FIRST_SECRET    </li>
    *    <li> COMPUTE_NEXT_SECRET     </li>
    *    <li> VALIDATE_DATA_PAGE      </li>
    *    <li> SIGN_DATA_PAGE          </li>
    *    <li> COMPUTE_CHALLENGE       </li>
    *    <li> AUTH_HOST               </li>
    * </ul></p>
    *
    * @param function the SHA function code
    * @param shaSecret the secret used in SHA caclulation
    * @param shaPage the 32-byte page used in SHA caculation
    * @param scratchpad the 32-byte scratchpad data used in SHA caculation.
    *        MAC is returned in this buffer starting at offset 8, unless
    *        the function is COMPUTE_FIRST_SECRET or COMPUTE_NEXT_SECRET,
    *        when the 4-byte parts E and D are repeated throughout the
    *        scratchpad, starting at offset zero.
    * @param romID 1-Wire address.  Only necessary for a
    *        READ_AUTHENTICATED_PAGE command and COMPUTE_CHALLENGE command.
    * @param pageNum the page number on which the shaPage resides.  only
    *        necessary for a READ_AUTHENTICATED_PAGE command and
    *        COMPUTE_CHALLENGE command.
    * @param writeCycleCounter the counter is only necessary for a
    *        READ_AUTHENTICATED_PAGE command and COMPUTE_CHALLENGE command.
    *
    * @return <code>true</code> if the function successfully completed,
    *         <code>false</code> if the operation failed or if invalid
    *          command.
    *
    */
   private synchronized boolean SHAFunction (byte function,
                 byte[] shaSecret, byte[] shaPage, byte[] scratchpad,
                 byte[] romID, int pageNum, int writeCycleCounter)
   {
      //offset for location in scratchpad to copy the MAC
      int offset = 8;

      //byte used for the M-X control bits
      //Since never matching, I assume M bit is never set...
      //but I'm not confident that won't change if more functionality
      //is added to this class.
      byte shaMX = 0x00;

      switch(function)
      {

      //Compute first secret, compute next secret, validate and sign data page
      case OneWireContainer18.COMPUTE_FIRST_SECRET:
         shaSecret = NullSecret;
      case OneWireContainer18.COMPUTE_NEXT_SECRET:
         //starts copying at location zero, for secret placement.
         //secret is repeated 4 times in scratchpad.
         offset = 0;
      case OneWireContainer18.VALIDATE_DATA_PAGE:
      case OneWireContainer18.SIGN_DATA_PAGE:
         //M-X-P byte
         scratchpad[12] = (byte)((scratchpad[12]&0x3F)|(shaMX&0xC0));
         break;

      //Authenticate host
      case OneWireContainer18.AUTH_HOST:
         //for authenticate host, X bit is set.
         shaMX |= 0x40;
         //M-X-P byte
         scratchpad[12] = (byte)((scratchpad[12]&0x3F)|(shaMX&0xC0));
         break;

      //compute challenge and read authenticated page
      case OneWireContainer18.COMPUTE_CHALLENGE:
         //for Compute_Challenge, X bit is set.
         shaMX |= 0x40;
      case OneWireContainer18.READ_AUTHENTICATED_PAGE:
         //place the write cycle counter into the scratchpad
         scratchpad[8] = (byte)(writeCycleCounter&0x0FF);
         scratchpad[9] = (byte)((writeCycleCounter>>>8)&0x0FF);
         scratchpad[10] = (byte)((writeCycleCounter>>>16)&0x0FF);
         scratchpad[11] = (byte)((writeCycleCounter>>>24)&0x0FF);

         //M-X-P byte
         scratchpad[12] = (byte)((pageNum&0x0F)|(shaMX&0xC0));

         //place the RomID into the scratchpad
         System.arraycopy(romID,0,scratchpad,13,7);
         break;


      //Bad function input, can't perform SHA.
      default:
         return false;
      }

      //Set up the 64 byte buffer for computing the digest.
      System.arraycopy(shaSecret,0,digestBuff,0,4);
      System.arraycopy(shaPage,0,digestBuff,4,32);
      System.arraycopy(scratchpad,8,digestBuff,36,12);
      System.arraycopy(shaSecret,4,digestBuff,48,4);
      System.arraycopy(scratchpad,20,digestBuff,52,3);

      //init. digest buffer padding
      digestBuff[55] = (byte)0x80;
      for(int i=56; i<62; i++)
         digestBuff[i] = (byte)0x00;
      digestBuff[62] = (byte)0x01;
      digestBuff[63] = (byte)0xB8;

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------------------------------");

         if(function==OneWireContainer18.VALIDATE_DATA_PAGE)
            IOHelper.writeLine("Validating data page");
         else if(function==OneWireContainer18.AUTH_HOST)
            IOHelper.writeLine("Authenticating Host");
         else if(function==OneWireContainer18.SIGN_DATA_PAGE)
            IOHelper.writeLine("Signing Data Page");
         else if(function==OneWireContainer18.COMPUTE_NEXT_SECRET)
            IOHelper.writeLine("Computing Next Secret");
         else if(function==OneWireContainer18.COMPUTE_FIRST_SECRET)
            IOHelper.writeLine("Computing FIRST Secret");
         else
            IOHelper.writeLine("SHA Function" + function);

         IOHelper.writeLine("pageNum: " + pageNum);
         IOHelper.writeLine("DigestBuffer: ");
         IOHelper.writeBytesHex(digestBuff);
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      //compute the MAC
      SHA.ComputeSHA(digestBuff,scratchpad,offset);

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
      if(DEBUG)
      {
         IOHelper.writeLine("SHA Result: ");
         IOHelper.writeBytesHex(scratchpad,offset,20);
         IOHelper.writeLine("------------------------------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

      //is this a secret computation?
      if(offset==0)
      {
         //Repeat E and D throughout scratchpad, just like hardware
         //not sure if this is necessary, maybe for NEXT_SECRET?
         System.arraycopy(scratchpad,0,scratchpad,8,8);
         System.arraycopy(scratchpad,0,scratchpad,16,8);
         System.arraycopy(scratchpad,0,scratchpad,24,8);
      }

      return true;
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
      return "COPRVM: "+ Address.toString(this.address) +
             ", provider: " + this.providerName +
             ", version: " + this.version;
   }
}
