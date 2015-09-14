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

import com.dalsemi.onewire.*;
import com.dalsemi.onewire.adapter.*;
import com.dalsemi.onewire.utils.*;

/**
 * <P>This class implements an account debit application for unsigned SHA
 * Transactions.  Account data is stored on user iButtons with no digital
 * signature (unlike <code>SHADebit</code> which signs the data with a
 * coprocessor iButton).</P>
 *
 * <P>You may be wondering, "Why use a SHA transaction interface for
 * unsigned account data?"  The answer is for increasing transaction times
 * with the DS1961S user iButton.  All data writes to the DS1961S require
 * knowledge of the iButton's unique secret, which prevents tampering with
 * the account data.  Since a data signature on account data is designed
 * for the same purpose, it is removed from this transaction to remove
 * redundancy and achieve better performance with the DS1961S.</P>
 *
 * <P>Due to the nature of the DS1961S/DS2432 (i.e. it's designed as
 * a battery-less EEPROM device for 'touch' environments), it is possible
 * that account data could be corrupted if the writing of the data is
 * interrupted. In fact, it is also possible that if the writing of a single
 * bit is interrupted, that bit could become borderline (i.e. right on
 * the line of being a 1 or a 0, such that the 1-Wire I/O logic reads it as
 * a 1 and the SHA engine reads it as a 0) and could be permanently corrupted.
 * As a workaround to problems of this nature, a double-write scheme is used
 * where the length indicates which record is the actual account balance.<P>
 *
 * <P>The account file consists of the following:
 *  <UL>
 *    <LI> 1 byte: Length of the account file</LI>
 *    <LI> 1 byte: Account data type (dynamic or static)</LI>
 *    <LI> 2 bytes: Account Money Conversion Factor</LI>
 *    <LI> 4 bytes: Don't Care</LI>
 *    <LI> <B>Record A</B>
 *    <LI> 3 bytes: Account Balance</LI>
 *    <LI> 2 bytes: Transaction ID</LI>
 *    <LI> 1 byte: File continuation pointer</LI>
 *    <LI> 2 bytes: CRC16 of 14 bytes seeded with the page number</LI>
 *    <LI> <B>Record B</B>
 *    <LI> 3 bytes: Account Balance</LI>
 *    <LI> 2 bytes: Transaction ID</LI>
 *    <LI> 1 byte: File continuation pointer</LI>
 *    <LI> 2 bytes: CRC16 of 22 bytes seeded with the page number</LI>
 *    <LI> <B>24 bytes Total</B></LI>
 *  </UL></P>
 *
 * <P>If the length of the account file is 13, then the current record is
 * Record A.  If the length of the account file is 21, then the current record
 * is Record B.  When updating the button, first the other record is updated
 * (i.e. if Record A is current, then update Record B with the new balance) and
 * write the new data for that record to the button.  After verifying that
 * the data was written correctly, the file pointer is the updated to point to
 * the new record.  If data corruption occurs at any point in the transaction
 * (and the user doesn't hang around long enough to allow the transaction
 * system a chance to fix it), the next time the user performs a transaction
 * it is possible to discern what the last known good value is.<P>
 *
 * <P>A typical use case for this class might be as follows:
 * <pre>
 *   OneWireContainer18 coprOWC18 = new OneWireContainer18(adapter,address);
 *
 *   //COPR.0 is the filename for coprocessor service data
 *   SHAiButtonCopr copr = new SHAiButtonCopr(coprOWC18,"COPR.0");
 *
 *   //Initial amount for new users is $100, and debit amount is 50 cents
 *   SHATransaction trans = new SHADebitUnsigned(copr, 10000, 50);
 *
 *   OneWireContainer33 owc33 = new OneWireContainer33(adapter, userAddress);
 *
 *   //The following constructor erases all transaction data from the user and
 *   //installs the system authentication secret on the user iButton.
 *   //The second instance of coprocessor is used for write-authorization.  If you're
 *   //not using the system coprocessor for data signing, it can be re-used for this
 *   //purpose.
 *   SHAiButtonUser user = new SHAiButtonUser33(copr, copr, owc33, true, authSecret);
 *
 *   //creates account data on iButton
 *   if(trans.setupTransactionData(user))
 *      System.out.println("Account data installed successfully");
 *   else
 *      System.out.println("Account data installation failed");
 *
 *   //... ... ... ... ... ... ... ... ... ... ... ... ... ... ... ...
 *
 *   //verifies authentication response of user iButton from "challenge"
 *   if(trans.verifyUser(user))
 *   {
 *      System.out.println("User Verified Successfully");
 *
 *      //Checks to see that account balance is greater than zero
 *      if(trans.verifyTransactionData(user))
 *      {
 *         System.out.println("Account Data Verified Successfully");
 *
 *         //performs the debit and writes the new account balance
 *         if(trans.executeTransaction(user))
 *         {
 *            System.out.println("Account debited.");
 *            System.out.println("New Balance: " +
 *               trans.getParameter(SHADebit.USER_BALANCE));
 *         }
 *      }
 *   }
 *
 *   //... ... ... ... ... ... ... ... ... ... ... ... ... ... ... ...
 *
 *   if(trans.getLastError()!=0)
 *   {
 *      System.err.println("Error code: " + trans.getLastError());
 *   }
 * </pre></P>
 *
 * <p>This class makes use of several performance enhancements for TINI.
 * For instance, most methods are <code>synchronized</code> to access instance
 * variable byte arrays rather than creating new byte arrays every time a
 * transaction is performed.  This could hurt performance in multi-threaded
 * applications, but the usefulness of having several threads contending
 * to talk to a single iButton is questionable since the methods in
 * <code>com.dalsemi.onewire.adapter.DSPortAdapter</code>
 * <code>beginExclusive(boolean)</code> and <code>endExclusive()</code>
 * should be used.</p>
 *
 * @see SHATransaction
 * @see SHAiButtonCopr
 * @see SHAiButtonUser
 *
 * @version 1.00
 * @author  SKH
 */
public class SHADebitUnsigned
   extends SHATransaction
{
   /** Used for fast FF copy */
   private static final byte[] ffBlock
      = new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
                     (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };

   //---------
   // Type constants
   // used to update/retrieve parameters of transaction
   //---------
   /** Update the amount this transaction will debit */
   public static final int DEBIT_AMOUNT = 0;

   /** Update the amount for initial account balance */
   public static final int INITIAL_AMOUNT = 1;

   /** Retrieve the amount for user's current balance */
   public static final int USER_BALANCE = 2;

   /** indices for fields in user account file */
   /** header indices */
   private static final int I_FILE_LENGTH = 0;
   private static final int I_DATA_TYPE_CODE = 1;
   private static final int I_CONVERSION_FACTOR = 2; // 2 conversion bytes
   private static final int I_DONT_CARE = 4; // 4 don't care bytes
   /** Record A indices */
   private static final int I_BALANCE_A = 8;
   private static final int I_TRANSACTION_ID_A = 11;
   private static final int I_CONTINUATION_PTR_A = 13;
   private static final int I_FILE_CRC16_A = 14;
   /** Record B indices */
   private static final int I_BALANCE_B = 16;
   private static final int I_TRANSACTION_ID_B = 19;
   private static final int I_CONTINUATION_PTR_B = 21;
   private static final int I_FILE_CRC16_B = 22;

   /** Constants for record length */
   private static final int RECORD_A_LENGTH = 13;
   private static final int RECORD_B_LENGTH = 21;


   //---------
   // Member variables
   //---------
   /** Amount to debit from user */
   private int debitAmount;

   /** Amount to initialize new user with */
   private int initialAmount;

   /** Most recent user's account balance */
   private int userBalance;

   //---------
   // Constructors
   //---------
   /** User apps should never call this */
   protected SHADebitUnsigned() {;}

   /**
    * SHADebitUnsigned constructor.  <code>copr</code> is the SHAiButtonCopr
    * that is used to perform this transaction.  After saving a
    * reference to the SHA coprocessor, this constructor resets all
    * parameters for this type of transaction to their default values.
    *
    * @param copr The coprocessor used for authentication and data
    *             signing in this transaction.
    */
   public SHADebitUnsigned(SHAiButtonCopr copr)
   {
      super(copr);

      resetParameters();
   }

   /**
    * SHADebitUnsigned constructor.  <code>copr</code> is the SHAiButtonCopr
    * that is used to perform this transaction.  After saving a
    * reference to the SHA coprocessor, this constructor resets all
    * parameters for this type of transaction to their default values.
    *
    * @param copr The coprocessor used for authentication and data
    *             signing in this transaction.
    */
   public SHADebitUnsigned(SHAiButtonCopr copr,
                           int initialAmount, int debitAmount)
   {
      super(copr);

      this.initialAmount = initialAmount;
      this.debitAmount = debitAmount;
   }


   /**
    * <P>Setup account data on a fresh user iButton.  Prior to calling
    * setup transaction data, the authentication secret for the iButton
    * should already be setup and a directory entry (as well as at least
    * an empty placeholder file) should exist for the account data.  If
    * you constructed the SHAiButtonUser using
    * <code>SHAiButtonUser(SHAiButtonCopr,OneWireContainer18,boolean,byte[])</code>
    * the secret has been setup for you and you should know call this
    * function.  If you try to install the authentication secret after
    * creating the account data, you will destroy all account data on the
    * iButton.</P>
    *
    * <P>You can set the value of the intial account balance by calling
    * <code>transaction.setParameter(SHADebit.INITIAL_AMOUNT,10000)</code>
    * where the value of the units is in cents (i.e. 10000 = $100).</P>
    *
    * <P><B>Flow of action: </B>
    *   <ul>
    *     <li> Generate generic account page </li>
    *     <li> Insert the initial balance into account data </li>
    *     <li> Insert the (constant) digital signature </li>
    *     <li> Write account data page to iButton </li>
    *   </ul></P>
    *
    * @param user SHAiButtonUser upon which the transaction occurs.
    *
    * @return <code>true</code>if and only if the signature is
    * successfully created by the coprocessor AND the data is
    * successfully written to the user iButton.
    *
    * @see SHAiButtonUser#writeAccountData(byte[],int)
    * @see #getLastError()
    */
   public boolean setupTransactionData(SHAiButtonUser user)
      throws OneWireException, OneWireIOException
   {
      //clear any error
      lastError = NO_ERROR;

      // not in critical path, so malloc'ing is okay
      byte[] accountData = new byte[32];

      return writeTransactionData(user, 0, this.initialAmount, accountData);
   }

   //prevent malloc'ing in the critical path
   private byte[] verifyUser_fullBindCode = new byte[15];
   private byte[] verifyUser_scratchpad   = new byte[32];
   private byte[] verifyUser_accountData  = new byte[32];
   private byte[] verifyUser_mac          = new byte[20];
   private byte[] verifyUser_chlg         = new byte[3];
   /**
    * <P>Verifies user's authentication response.  User is "authenticated" if
    * and only if the digital signature generated the user iButton matches
    * the digital signature generated by the coprocessor after the user's
    * unique secret has been recreated on the coprocessor.</P>
    *
    * <P><B>Flow of action: </B>
    *   <ul>
    *     <li> Generate 3-byte "challenge" on coprocessor </li>
    *     <li> Write challenge to scratchpad of user </li>
    *     <li> Read account data page with signature </li>
    *     <li> Attempt to match user's signature with the coprocessor </li>
    *   </ul></P>
    *
    * @param user SHAiButtonUser upon which the transaction occurs.
    *
    * @see SHAiButtonCopr#generateChallenge(int,byte[],int)
    * @see SHAiButtonCopr#verifyAuthentication(byte[],byte[],byte[],byte[],byte)
    * @see SHAiButtonUser#readAccountData(byte[],int,byte[],int,byte[],int)
    * @see #getLastError()
    */
   public synchronized boolean verifyUser(SHAiButtonUser user)
      throws OneWireException, OneWireIOException
   {
      //clear any error
      this.lastError = SHATransaction.NO_ERROR;

      //local vars
      byte[] fullBindCode = this.verifyUser_fullBindCode;
      byte[] scratchpad   = this.verifyUser_scratchpad;
      byte[] accountData  = this.verifyUser_accountData;
      byte[] mac          = this.verifyUser_mac;
      byte[] chlg         = this.verifyUser_chlg;

      int wcc;

      //Generate random challenge. This must be done on the
      //coprocessor, otherwise flags aren't setup for VALIDATE_PAGE.
      if(!copr.generateChallenge(0, chlg, 0))
      {
         lastError = COPR_COMPUTE_CHALLENGE_FAILED;
         return false;
      }

      //have user answer the challenge
      wcc = user.readAccountData(chlg, 0, accountData, 0, mac, 0);

      if(wcc<0)
      {
         if(user.hasWriteCycleCounter())
         {
            // failed to read account data
            this.lastError = SHATransaction.USER_READ_AUTH_FAILED;
            return false;
         }
         System.arraycopy(ffBlock, 0, scratchpad, 8, 4);
      }
      else
      {
         //copy the write cycle counter into scratchpad
         Convert.toByteArray(wcc, scratchpad, 8, 4);
      }

      //get the user's fullBindCode, formatted for user device
      user.getFullBindCode(fullBindCode, 0);

      //get the user address and page num from fullBindCode
      System.arraycopy(fullBindCode, 4, scratchpad, 12, 8);

      //set the same challenge bytes
      System.arraycopy(chlg, 0, scratchpad, 20, 3);

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------");
         IOHelper.writeLine("Verifying user");
         IOHelper.writeLine("chlg");
         IOHelper.writeBytesHex(chlg);
         IOHelper.writeLine("accountData");
         IOHelper.writeBytesHex(accountData);
         IOHelper.writeLine("mac");
         IOHelper.writeBytesHex(mac);
         IOHelper.writeLine("wcc: " + user.getWriteCycleCounter());
         IOHelper.writeLine("fullBindCode");
         IOHelper.writeBytesHex(fullBindCode);
         IOHelper.writeLine("scratchpad");
         IOHelper.writeBytesHex(scratchpad);
         IOHelper.writeLine("------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

      if(!copr.verifyAuthentication(fullBindCode,
                                    accountData,
                                    scratchpad, mac,
                                    user.getAuthorizationCommand()))
      {
         this.lastError = SHATransaction.COPROCESSOR_FAILURE;
         return false;
      }

      return true;
   }

   //prevent malloc'ing in the critical path
   private byte[] verifyData_accountData  = new byte[32];
   /**
    * <P>Verifies user's account data.  Account data is "verified" if and
    * only if the account balance is greater than zero.  No digital
    * signature is checked by this transaction.</P>
    *
    * <P><B>Flow of action: </B>
    *   <ul>
    *     <li> Read the account data from user </li>
    *     <li> Extract account balance </li>
    *     <li> Debit money from balance </li>
    *     <li> Insert the new balance </li>
    *     <li> Write the account data to the user </li>
    *   </ul></P>
    *
    * <P>If previous steps have been executed, all "Read" commands on
    * the user are reading from cached data.</P>
    *
    * @param user SHAiButtonUser upon which the transaction occurs.
    *
    * @return <code>true</code> if and only if the account balance is
    * greater than zero.
    *
    * @see SHAiButtonUser#readAccountData(byte[],int)
    * @see #getLastError()
    */
   public synchronized boolean verifyTransactionData(SHAiButtonUser user)
      throws OneWireException, OneWireIOException
   {
      //clear any error
      this.lastError = NO_ERROR;

      byte[] accountData = this.verifyData_accountData;

      //if verifyUser was called, this is a read of cached data
      user.readAccountData(accountData,0);

      // verify the A-B data scheme is valid
      boolean validPtr = false, validA = false, validB = false;

      byte fileLength = accountData[I_FILE_LENGTH];
      int crc16 = CRC16.compute(accountData, 0, fileLength+3,
                                user.getAccountPageNumber());

      if(fileLength==RECORD_A_LENGTH || fileLength==RECORD_B_LENGTH)
         validPtr = true;

      // was the crc of the file correct?
      if(crc16==0xB001)
      {
         // if the header points to a valid record, we're done
         if(validPtr)
         {
            // nothing more to check for DS1961S/DS2432, since
            // it carries no signed data, we're finished
            return true;
         }
         // header points to neither record A nor B, but
         // crc is absolutely correct.  that can only mean
         // we're looking at something that is the wrong
         // size from what was expected, but apparently is
         // exactly what was meant to be written.  I'm done.
         this.lastError = USER_BAD_ACCOUNT_DATA;
         return false;
      }

      // restore the other header information
      accountData[I_DATA_TYPE_CODE] = 0x01;
      accountData[I_CONVERSION_FACTOR+0] = (byte)0x8B;
      accountData[I_CONVERSION_FACTOR+1] = (byte)0x48;
      // zero-out the don't care bytes
      accountData[I_DONT_CARE] = 0x00;
      accountData[I_DONT_CARE+1] = 0x00;
      accountData[I_DONT_CARE+2] = 0x00;
      accountData[I_DONT_CARE+3] = 0x00;

      // lets try Record A and check the crc
      accountData[I_FILE_LENGTH] = RECORD_A_LENGTH;
      crc16 = CRC16.compute(accountData, 0, RECORD_A_LENGTH+3,
                            user.getAccountPageNumber());
      if(crc16==0xB001)
         validA = true;

      // lets try Record B and check the crc
      accountData[I_FILE_LENGTH] = RECORD_B_LENGTH;
      crc16 = CRC16.compute(accountData, 0, RECORD_B_LENGTH+3,
                            user.getAccountPageNumber());
      if(crc16==0xB001)
         validB = true;

      if(validA && validB)
      {
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
         if(DEBUG)
            System.out.println("Both A and B are valid");
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
         // Both A & B are valid!  And we know that we can only
         // get here if the pointer or the header was not valid.
         // That means that B was the last updated one but the
         // header got hosed and the debit was not finished...
         // which means A is the last known good value, let's go with A.
         accountData[I_FILE_LENGTH] = RECORD_A_LENGTH;
      }
      else if(validA)
      {
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
         if(DEBUG)
            System.out.println("A is valid not B");
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
         // B is invalid, A is valid.  Means A is the last updated one,
         // but B is the last known good value.  The header was not updated
         // to point to A before debit was aborted.  Let's go with B
         accountData[I_FILE_LENGTH] = RECORD_B_LENGTH;
      }
      else if(validB)
      {
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
         if(DEBUG)
            System.out.println("B is valid not A - impossible");
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
         // A is invalid, B is valid. Should never ever happen.  Something
         // got completely hosed.  What should happen here?
         this.lastError = USER_BAD_ACCOUNT_DATA;
         return false;
      }
      else
      {
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
         if(DEBUG)
            System.out.println("Neither record has valid CRC");
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
         // neither record contains a valid CRC.  What should happen here?
         // probably got weak bit in ptr record, no telling which way to go
         this.lastError = USER_BAD_ACCOUNT_DATA;
         return false;
      }

      //get the user's balance from accountData
      int balance = -1;
      if(accountData[I_FILE_LENGTH]==RECORD_A_LENGTH )
         balance = Convert.toInt(accountData, I_BALANCE_A, 3);
      else if(accountData[I_FILE_LENGTH]==RECORD_B_LENGTH)
         balance = Convert.toInt(accountData, I_BALANCE_B, 3);

      if(balance<0)
      {
         this.lastError = USER_BAD_ACCOUNT_DATA;
         return false;
      }

      this.userBalance = balance;

      int cnt = MAX_RETRY_CNT;
      do
      {
         if(user.refreshDevice()
             && writeTransactionData(user, -1, balance, accountData))
            break;
      }
      while(--cnt>0);

      return false;
   }


   //prevent malloc'ing in critical path
   private byte[] executeTransaction_accountData = new byte[32];
   private byte[] executeTransaction_oldAcctData = new byte[32];
   private byte[] executeTransaction_newAcctData = new byte[32];
   /**
    * <P>Performs the unsigned debit, subtracting the debit amount from
    * the user's balance and storing the new, unsigned account data on the
    * user's iButton.  The debit amount can be set using
    * <code>transaction.setParameter(SHADebit.DEBIT_AMOUNT, 50)</code>,
    * where the value is in units of cents (i.e. for 1 dollar, use 100).</P>
    *
    * <P><B>Flow of action: </B>
    *   <ul>
    *     <li> Read the account data from user </li>
    *     <li> Extract account balance </li>
    *     <li> Assert balance greater than debit amount </li>
    *     <li> Debit money from balance </li>
    *     <li> Insert the new balance </li>
    *     <li> Write the account data to the user </li>
    *   </ul></P>
    *
    * <P>If previous steps have been executed, all "Read" commands on
    * the user are reading from cached data.</P>
    *
    * @param user SHAiButtonUser upon which the transaction occurs.
    *
    * @return <code>true</code> if and only if the user has enough in the
    * account balance to perform the requested debit AND the account data
    * has been written to the button.
    *
    * @see SHAiButtonUser#readAccountData(byte[],int)
    * @see SHAiButtonUser#writeAccountData(byte[],int)
    * @see #getLastError()
    */
   public synchronized boolean executeTransaction(SHAiButtonUser user,
                                                  boolean verifySuccess)
      throws OneWireException, OneWireIOException
   {
      //clear any error
      this.lastError = NO_ERROR;

      //init local vars
      //holds the working copy of account data
      byte[] accountData = this.executeTransaction_accountData;
      //holds the backup copy of account data before writing
      byte[] oldAcctData = this.executeTransaction_oldAcctData;
      //holds the account data read back for checking
      byte[] newAcctData = this.executeTransaction_newAcctData;
      //just make the transaction ID a random number, so it changes
      int    transID     = rand.nextInt();

      //if verifyUser was called, this is a read of cached data
      user.readAccountData(accountData,0);

      //before we update the account data array at all, let's make a backup copy
      System.arraycopy(accountData, 0, oldAcctData, 0, 32);

      //get the user's balance from accountData
      int balance = -1;
      if(accountData[I_FILE_LENGTH]==RECORD_A_LENGTH)
         balance = Convert.toInt(accountData, I_BALANCE_A, 3);
      else if(accountData[I_FILE_LENGTH]==RECORD_B_LENGTH)
         balance = Convert.toInt(accountData, I_BALANCE_B, 3);

      //if there are insufficient funds
      if(this.debitAmount>balance)
      {
         this.lastError = USER_BAD_ACCOUNT_DATA;
         return false;
      }

      //update the user's balance
      this.userBalance = (balance - this.debitAmount);

      // attempt to update the page
      boolean success = false;
      try
      {
         success = writeTransactionData(user, transID, this.userBalance, accountData);
      }
      catch(Exception e)
      { /* sink */ }

      //if write didn't succeeded or if we need to perform
      //a verification step anyways, let's double-check what
      //the user has on the button.
      if(verifySuccess || !success)
      {
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
         if(DEBUG)
         {
            System.out.println("attempting to re-write transaction data: ");
            System.out.print("cur Data: ");
            System.out.println(Convert.toHexString(accountData, 0, 32, " "));
            System.out.print("old data: ");
            System.out.println(Convert.toHexString(oldAcctData, 0, 32, " "));
         }
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
         boolean dataOK = false;
         int cnt = MAX_RETRY_CNT;
         do
         {
            try
            {
               // let's refresh the page
               user.refreshDevice();
               //calling verify user re-issues a challenge-response
               //and reloads the cached account data in the user object.
               if(verifyUser(user))
               {
                  //compare the user's account data against the working
                  //copy and the backup copy.
                  if(user.readAccountData(newAcctData,0))
                  {
                     //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
                     if(DEBUG)
                     {
                        System.out.print("new data: ");
                        System.out.println(Convert.toHexString(newAcctData, 0, 32, " "));
                     }
                     //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
                     boolean isOld = true;
                     boolean isCur = true;
                     for(int i=0; i<32 && (isOld||isCur); i++)
                     {
                        //match the backup
                        isOld = isOld && (newAcctData[i]==oldAcctData[i]);
                        //match the working copy
                        isCur = isCur && (newAcctData[i]==accountData[i]);
                     }
                     if(isOld)
                     {
                        //if it matches the backup copy, we didn't write anything
                        //and the data is still okay, but we didn't do a debit
                        dataOK = true;
                        success = false;
                     }
                     else if(isCur)
                     {
                        dataOK = true;
                        success = true;
                     }
                     else
                     {
                        int cnt2 = MAX_RETRY_CNT;
                        do
                        {
                           //iBUTTON DATA IS TOTALLY HOSED
                           //keep trying to get account data on the button
                           try
                           {
                              success = writeTransactionData(user,
                                                             transID,
                                                             this.userBalance,
                                                             accountData);
                           }
                           catch(OneWireIOException owioe)
                           {
                              if(cnt2==0)
                                 throw owioe;
                           }
                           catch(OneWireException owe)
                           {
                              if(cnt2==0)
                                 throw owe;
                           }
                        }
                        while(cnt2-->0 && !success);
                     }
                  }
               }
            }
            catch(OneWireIOException owioe)
            {
               if(cnt==0)
                  throw owioe;
            }
            catch(OneWireException owe)
            {
               if(cnt==0)
                  throw owe;
            }
         }
         while(!dataOK && cnt-->0);

         if(!dataOK)
         {
            //couldn't fix the data after 255 retries
            IOHelper.writeLine("Catastrophic Failure!");
            success = false;
         }
      }

      return success;
   }

   private byte[] writeTransactionData_scratchpad = new byte[32];
   /**
    * Does the writing of transaction data to the user button as well
    * as actually signing the data with the coprocessor.
    *
    * No need to synchronize wince the methods that call this
    * private method will be synchronized.
    */
   private final boolean writeTransactionData(SHAiButtonUser user,
                                              int transID, int balance,
                                              byte[] accountData)
      throws OneWireException, OneWireIOException
   {
      //init local vars
      int    acctPageNum  = user.getAccountPageNumber();

      // data type code - dynamic: 0x00, static: 0x01
      accountData[I_DATA_TYPE_CODE] = 0x01;

      // conversion factor - 2 data bytes
      accountData[I_CONVERSION_FACTOR+0] = (byte)0x8B;
      accountData[I_CONVERSION_FACTOR+1] = (byte)0x48;

      // zero-out the don't care bytes
      accountData[I_DONT_CARE] = 0x00;
      accountData[I_DONT_CARE+1] = 0x00;
      accountData[I_DONT_CARE+2] = 0x00;
      accountData[I_DONT_CARE+3] = 0x00;

      if(accountData[I_FILE_LENGTH]==RECORD_A_LENGTH)
      {
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
         if(DEBUG)
            System.out.println("Was A, now using B");
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

         // length of the TMEX file
         accountData[I_FILE_LENGTH] = (byte)RECORD_B_LENGTH;

         // account balance - 3 data bytes
         Convert.toByteArray(balance, accountData, I_BALANCE_B, 3);

         // transaction ID - 2 data bytes
         accountData[I_TRANSACTION_ID_B+0] = (byte)transID;
         accountData[I_TRANSACTION_ID_B+1] = (byte)(transID>>>8);

         // continuation pointer for TMEX file
         accountData[I_CONTINUATION_PTR_B] = 0x00;

         // clear out the crc16 - 2 data bytes
         accountData[I_FILE_CRC16_B+0] = 0x00;
         accountData[I_FILE_CRC16_B+1] = 0x00;

         // dump in the inverted CRC
         int crc = ~CRC16.compute(accountData, 0,
                                  accountData[I_FILE_LENGTH] + 1,
                                  acctPageNum);
         accountData[I_FILE_CRC16_B+0] = (byte)crc;
         accountData[I_FILE_CRC16_B+1] = (byte)(crc>>8);
      }
      else
      {
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
         if(DEBUG)
            System.out.println("Was B, now using A");
         //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
         // length of the TMEX file
         accountData[I_FILE_LENGTH] = (byte)RECORD_A_LENGTH;

         // account balance - 3 data bytes
         Convert.toByteArray(balance, accountData, I_BALANCE_A, 3);

         // transaction ID - 2 data bytes
         accountData[I_TRANSACTION_ID_A+0] = (byte)transID;
         accountData[I_TRANSACTION_ID_A+1] = (byte)(transID>>>8);

         // continuation pointer for TMEX file
         accountData[I_CONTINUATION_PTR_A] = 0x00;

         // clear out the crc16 - 2 data bytes
         accountData[I_FILE_CRC16_A+0] = 0x00;
         accountData[I_FILE_CRC16_A+1] = 0x00;

         // dump in the inverted CRC
         int crc = ~CRC16.compute(accountData, 0,
                                  accountData[I_FILE_LENGTH] + 1,
                                  acctPageNum);
         accountData[I_FILE_CRC16_A+0] = (byte)crc;
         accountData[I_FILE_CRC16_A+1] = (byte)(crc>>8);
      }

      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
      if(DEBUG)
      {
         IOHelper.writeLine("------------------------------------");
         IOHelper.writeLine("writing transaction data");
         IOHelper.writeLine("acctPageNum: " + acctPageNum);
         IOHelper.writeLine("accountData");
         IOHelper.writeBytesHex(accountData);
         IOHelper.writeLine("------------------------------------");
      }
      //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

      // write it to the button
      try
      {
         if(user.writeAccountData(accountData, 0))
            return true;
      }
      catch(OneWireException owe)
      {
         if(DEBUG)
            IOHelper.writeLine(owe);
      }

      this.lastError = SHATransaction.USER_WRITE_DATA_FAILED;
      return false;
   }

   /**
    * <P>Retrieves the value of a particular parameter for this
    * debit transaction.</P>
    *
    * <P><B>Valid Parameters</B>
    *   <UL>
    *     <LI><code>SHADebit.DEBIT_AMOUNT</code></LI>
    *     <LI><code>SHADebit.INITIAL_AMOUNT</code></LI>
    *     <LI><code>SHADebit.USER_BALANCE</code></LI>
    *   </UL>
    * </P>
    *
    * <P>Note that the value of <code>SHADebit.USER_BALANCE</code> will
    * be set after calling <code>verifyTransactionData(SHAiButtonUser)</code>
    * and  after calling <code>executeTransaction(SHAiButtonUser)</code>.</P>
    *
    * @return The value of the requested parameter.
    *
    * @throws IllegalArgumentException if an invalid parameter type
    *         is requested.
    */
   public synchronized int getParameter(int type)
   {
      switch(type)
      {
         case DEBIT_AMOUNT:
            return debitAmount;
         case INITIAL_AMOUNT:
            return initialAmount;
         case USER_BALANCE:
            return userBalance;
         default:
            return -1;
      }
   }

   /**
    * <P>Sets the value of a particular parameter for this
    * debit transaction.</P>
    *
    * <P><B>Valid Parameters</B>
    *   <UL>
    *     <LI><code>SHADebit.DEBIT_AMOUNT</code></LI>
    *     <LI><code>SHADebit.INITIAL_AMOUNT</code></LI>
    *   </UL>
    * </P>
    *
    * @param type Specifies the parameter type (<code>SHADebit.DEBIT_AMOUNT</code> or
    *        <code>SHADebit.INITIAL_AMOUNT</code>)
    * @return </code>true</code> if a valid parameter type was specified
    *         and the value of the parameter is positive.
    *
    * @throws IllegalArgumentException if an invalid parameter type
    *         is requested.
    */
   public synchronized boolean setParameter(int type, int param)
   {
      switch(type)
      {
         case DEBIT_AMOUNT:
            debitAmount = param;
            break;
         case INITIAL_AMOUNT:
            initialAmount = param;
            break;
         default:
            return false;
      }
      return true;
   }

   /**
    * <p>Resets all transaction parameters to default values</p>
    */
   public synchronized void resetParameters()
   {
      debitAmount = 50; //50 cents
      initialAmount = 90000; //100 dollars
      userBalance = 0; //0 dollars
   }
}
