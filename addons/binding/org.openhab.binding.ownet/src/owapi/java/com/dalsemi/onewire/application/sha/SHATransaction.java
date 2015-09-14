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

/**
 * <P>Abstract super class for all SHA Transactions.  Typical SHA transactions
 * might be something like an account debit application, a door access control
 * system, or a web-based authentication servlet.  The <code>SHATransaction</code>
 * super class contains the bare minimum functionality necessary for the steps
 * involved in most SHA transaction applications.</P>
 *
 * <P>The first characteristic of a SHA Transaction is that it is tied to an
 * immutable <code>SHAiButtonCopr</code>, used for data signing and recreating
 * user authentication responses.  The SHA Transaction guarantees an interface
 * for initializing account transaction data (<code>setupTransactionData</code>),
 * verifying that the transaction data has not been tampered with
 * (<code>verifyTransactionData</code>), performing the transaction and updating
 * the data (<code>executeTransaction</code>), and validating a user with a
 * challenge-response authentication protocol (<code>verifyUser</code>).</P>
 *
 * <P>In addition, all transactions are characterized by certain parameters (i.e.
 * how much to debit from the user or what level of access is being requested
 * from the system).  The interface for retrieving and setting these parameters is
 * provided through the generic <code>getParameter</code> and
 * <code>setParameter</code>.</P>
 *
 * @see SHADebit
 * @see SHADebitUnsigned
 *
 * @version 1.00
 * @author  SKH
 */
public abstract class SHATransaction
{
   /** Turns on extra debugging for all SHATransactions */
   static final boolean DEBUG = false;

   static final int MAX_RETRY_CNT = 65536;

   static final java.util.Random rand = new java.util.Random();

   // **************************************************************** //
   // Error Constants
   // **************************************************************** //
   public static final int NO_ERROR = 0;
   public static final int SHA_FUNCTION_FAILED = -1;
   public static final int MATCH_SCRATCHPAD_FAILED = -2;
   public static final int COPR_WRITE_DATAPAGE_FAILED = -3;
   public static final int COPR_WRITE_SCRATCHPAD_FAILED = -4;
   public static final int COPR_BIND_SECRET_FAILED = -5;
   public static final int COPR_COMPUTE_CHALLENGE_FAILED = -6;
   public static final int COPROCESSOR_FAILURE = -6;
   public static final int USER_READ_AUTH_FAILED = -7;
   public static final int USER_WRITE_DATA_FAILED = -8;
   public static final int USER_BAD_ACCOUNT_DATA = -9;
   public static final int USER_DATA_NOT_UPDATED = -10;
   // **************************************************************** //

   /** The last error that occurred during this transaction */
   protected int lastError;

   /** The coprocessor used to complete this transaction */
   protected SHAiButtonCopr copr;

   /**
    * <p>User applications should not instantiate this class without
    * an instance of a coprocessor.</p>
    */
   protected SHATransaction() {;}

   /**
    * <P>Creates a new SHATransaction, ensuring that reference to
    * the coprocessor is saved and the errors are cleared.</P>
    */
   protected SHATransaction(SHAiButtonCopr copr)
   {
      this.copr = copr;
      this.lastError = 0;
   }

   /**
    * <P>Returns the error code for the last error in the transaction
    * process.</P>
    */
   public int getLastError()
   {
      return this.lastError;
   }

   /**
    * <P>Returns the error code for the last error in the transaction
    * process.</P>
    */
   public int getLastCoprError()
   {
      return this.copr.getLastError();
   }

   /**
    * <P>Setups initial transaction data on SHAiButtonUser.  This step
    * creates the account data file, signs it with the coprocessor,
    * and writes it to the iButton.</P>
    */
   public abstract boolean setupTransactionData(SHAiButtonUser user)
            throws OneWireException, OneWireIOException;

   /**
    * <P>Verifies that SHAiButtonUser is a valid user of this service.
    * This step writes a three byte challenge to the SHAiButtonUser
    * before doing an authenticated read of the account data.  The
    * returned MAC is verified using the system authentication secret
    * on the coprocessor.  If the MAC matches that generated by the
    * coprocessor, this function returns true.</P>
    */
   public abstract boolean verifyUser(SHAiButtonUser user)
            throws OneWireException, OneWireIOException;

   /**
    * <P>Verifies account data is valid for this service.  The user's
    * account data is recreated on the coprocessor and signed using
    * the system signing secret.  If the recreated signature matches
    * the signature in the account data, the account data is valid.</P>
    */
   public abstract boolean verifyTransactionData(SHAiButtonUser user)
      throws OneWireException, OneWireIOException;

   /**
    * <P>Performs the transaction.  For any given transaction type,
    * this step would involve updating any necessary account data,
    * signing the account data using the coprocessor's system signing
    * secret, and writing the updated account data to the user
    * iButton</P>
    */
   public abstract boolean executeTransaction(SHAiButtonUser user,
                                              boolean verifySuccess)
      throws OneWireException, OneWireIOException;

   /**
    * <P>Sets a particular parameter for this transaction.  Parameters
    * are specified in the class documentation for the specific type of
    * transaction that is being peformed.</P>
    */
   public abstract boolean setParameter(int type, int param);

   /**
    * <P>Retrieves the value of a particular parameter for this
    * transaction.  Parameters are specified in the class documentation
    * for the specific type of transaction that is being peformed.</P>
    */
   public abstract int getParameter(int type);

   /**
    * <P>Resets the value of all parameters for this transaction.
    * Parameters are specified in the class documentation for the
    * specific type of transaction that is being peformed.</P>
    */
   public abstract void resetParameters();
}
