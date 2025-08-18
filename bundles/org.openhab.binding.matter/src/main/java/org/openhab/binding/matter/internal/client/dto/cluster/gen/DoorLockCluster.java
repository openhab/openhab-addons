/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */

// AUTO-GENERATED, DO NOT EDIT!

package org.openhab.binding.matter.internal.client.dto.cluster.gen;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.matter.internal.client.dto.cluster.ClusterCommand;

/**
 * DoorLock
 *
 * @author Dan Cunningham - Initial contribution
 */
public class DoorLockCluster extends BaseCluster {

    public static final int CLUSTER_ID = 0x0101;
    public static final String CLUSTER_NAME = "DoorLock";
    public static final String CLUSTER_PREFIX = "doorLock";
    public static final String ATTRIBUTE_CLUSTER_REVISION = "clusterRevision";
    public static final String ATTRIBUTE_FEATURE_MAP = "featureMap";
    public static final String ATTRIBUTE_LOCK_STATE = "lockState";
    public static final String ATTRIBUTE_LOCK_TYPE = "lockType";
    public static final String ATTRIBUTE_ACTUATOR_ENABLED = "actuatorEnabled";
    public static final String ATTRIBUTE_DOOR_STATE = "doorState";
    public static final String ATTRIBUTE_DOOR_OPEN_EVENTS = "doorOpenEvents";
    public static final String ATTRIBUTE_DOOR_CLOSED_EVENTS = "doorClosedEvents";
    public static final String ATTRIBUTE_OPEN_PERIOD = "openPeriod";
    public static final String ATTRIBUTE_NUMBER_OF_TOTAL_USERS_SUPPORTED = "numberOfTotalUsersSupported";
    public static final String ATTRIBUTE_NUMBER_OF_PIN_USERS_SUPPORTED = "numberOfPinUsersSupported";
    public static final String ATTRIBUTE_NUMBER_OF_RFID_USERS_SUPPORTED = "numberOfRfidUsersSupported";
    public static final String ATTRIBUTE_NUMBER_OF_WEEK_DAY_SCHEDULES_SUPPORTED_PER_USER = "numberOfWeekDaySchedulesSupportedPerUser";
    public static final String ATTRIBUTE_NUMBER_OF_YEAR_DAY_SCHEDULES_SUPPORTED_PER_USER = "numberOfYearDaySchedulesSupportedPerUser";
    public static final String ATTRIBUTE_NUMBER_OF_HOLIDAY_SCHEDULES_SUPPORTED = "numberOfHolidaySchedulesSupported";
    public static final String ATTRIBUTE_MAX_PIN_CODE_LENGTH = "maxPinCodeLength";
    public static final String ATTRIBUTE_MIN_PIN_CODE_LENGTH = "minPinCodeLength";
    public static final String ATTRIBUTE_MAX_RFID_CODE_LENGTH = "maxRfidCodeLength";
    public static final String ATTRIBUTE_MIN_RFID_CODE_LENGTH = "minRfidCodeLength";
    public static final String ATTRIBUTE_CREDENTIAL_RULES_SUPPORT = "credentialRulesSupport";
    public static final String ATTRIBUTE_NUMBER_OF_CREDENTIALS_SUPPORTED_PER_USER = "numberOfCredentialsSupportedPerUser";
    public static final String ATTRIBUTE_LANGUAGE = "language";
    public static final String ATTRIBUTE_LED_SETTINGS = "ledSettings";
    public static final String ATTRIBUTE_AUTO_RELOCK_TIME = "autoRelockTime";
    public static final String ATTRIBUTE_SOUND_VOLUME = "soundVolume";
    public static final String ATTRIBUTE_OPERATING_MODE = "operatingMode";
    public static final String ATTRIBUTE_SUPPORTED_OPERATING_MODES = "supportedOperatingModes";
    public static final String ATTRIBUTE_DEFAULT_CONFIGURATION_REGISTER = "defaultConfigurationRegister";
    public static final String ATTRIBUTE_ENABLE_LOCAL_PROGRAMMING = "enableLocalProgramming";
    public static final String ATTRIBUTE_ENABLE_ONE_TOUCH_LOCKING = "enableOneTouchLocking";
    public static final String ATTRIBUTE_ENABLE_INSIDE_STATUS_LED = "enableInsideStatusLed";
    public static final String ATTRIBUTE_ENABLE_PRIVACY_MODE_BUTTON = "enablePrivacyModeButton";
    public static final String ATTRIBUTE_LOCAL_PROGRAMMING_FEATURES = "localProgrammingFeatures";
    public static final String ATTRIBUTE_WRONG_CODE_ENTRY_LIMIT = "wrongCodeEntryLimit";
    public static final String ATTRIBUTE_USER_CODE_TEMPORARY_DISABLE_TIME = "userCodeTemporaryDisableTime";
    public static final String ATTRIBUTE_SEND_PIN_OVER_THE_AIR = "sendPinOverTheAir";
    public static final String ATTRIBUTE_REQUIRE_PIN_FOR_REMOTE_OPERATION = "requirePinForRemoteOperation";
    public static final String ATTRIBUTE_EXPIRING_USER_TIMEOUT = "expiringUserTimeout";
    public static final String ATTRIBUTE_ALARM_MASK = "alarmMask";
    public static final String ATTRIBUTE_ALIRO_READER_VERIFICATION_KEY = "aliroReaderVerificationKey";
    public static final String ATTRIBUTE_ALIRO_READER_GROUP_IDENTIFIER = "aliroReaderGroupIdentifier";
    public static final String ATTRIBUTE_ALIRO_READER_GROUP_SUB_IDENTIFIER = "aliroReaderGroupSubIdentifier";
    public static final String ATTRIBUTE_ALIRO_EXPEDITED_TRANSACTION_SUPPORTED_PROTOCOL_VERSIONS = "aliroExpeditedTransactionSupportedProtocolVersions";
    public static final String ATTRIBUTE_ALIRO_GROUP_RESOLVING_KEY = "aliroGroupResolvingKey";
    public static final String ATTRIBUTE_ALIRO_SUPPORTED_BLEUWB_PROTOCOL_VERSIONS = "aliroSupportedBleuwbProtocolVersions";
    public static final String ATTRIBUTE_ALIRO_BLE_ADVERTISING_VERSION = "aliroBleAdvertisingVersion";
    public static final String ATTRIBUTE_NUMBER_OF_ALIRO_CREDENTIAL_ISSUER_KEYS_SUPPORTED = "numberOfAliroCredentialIssuerKeysSupported";
    public static final String ATTRIBUTE_NUMBER_OF_ALIRO_ENDPOINT_KEYS_SUPPORTED = "numberOfAliroEndpointKeysSupported";

    public Integer clusterRevision; // 65533 ClusterRevision
    public FeatureMap featureMap; // 65532 FeatureMap
    /**
     * This attribute may be NULL if the lock hardware does not currently know the status of the locking mechanism. For
     * example, a lock may not know the LockState status after a power cycle until the first lock actuation is
     * completed.
     * The Not Fully Locked value is used by a lock to indicate that the state of the lock is somewhere between Locked
     * and Unlocked so it is only partially secured. For example, a deadbolt could be partially extended and not in a
     * dead latched state.
     */
    public LockStateEnum lockState; // 0 LockStateEnum R V
    /**
     * Indicates the type of door lock as defined in LockTypeEnum.
     */
    public LockTypeEnum lockType; // 1 LockTypeEnum R V
    /**
     * Indicates if the lock is currently able to (Enabled) or not able to (Disabled) process remote Lock, Unlock, or
     * Unlock with Timeout commands.
     */
    public Boolean actuatorEnabled; // 2 bool R V
    /**
     * Indicates the current door state as defined in DoorStateEnum.
     * Null only if an internal error prevents the retrieval of the current door state.
     */
    public DoorStateEnum doorState; // 3 DoorStateEnum R V
    /**
     * This attribute shall hold the number of door open events that have occurred since it was last zeroed.
     */
    public Integer doorOpenEvents; // 4 uint32 RW VM
    /**
     * This attribute shall hold the number of door closed events that have occurred since it was last zeroed.
     */
    public Integer doorClosedEvents; // 5 uint32 RW VM
    /**
     * This attribute shall hold the number of minutes the door has been open since the last time it transitioned from
     * closed to open.
     */
    public Integer openPeriod; // 6 uint16 RW VM
    /**
     * Indicates the number of total users supported by the lock.
     */
    public Integer numberOfTotalUsersSupported; // 17 uint16 R V
    /**
     * Indicates the number of PIN users supported.
     */
    public Integer numberOfPinUsersSupported; // 18 uint16 R V
    /**
     * Indicates the number of RFID users supported.
     */
    public Integer numberOfRfidUsersSupported; // 19 uint16 R V
    /**
     * Indicates the number of configurable week day schedule supported per user.
     */
    public Integer numberOfWeekDaySchedulesSupportedPerUser; // 20 uint8 R V
    /**
     * Indicates the number of configurable year day schedule supported per user.
     */
    public Integer numberOfYearDaySchedulesSupportedPerUser; // 21 uint8 R V
    /**
     * Indicates the number of holiday schedules supported for the entire door lock device.
     */
    public Integer numberOfHolidaySchedulesSupported; // 22 uint8 R V
    /**
     * Indicates the maximum length in bytes of a PIN Code on this device.
     */
    public Integer maxPinCodeLength; // 23 uint8 R V
    /**
     * Indicates the minimum length in bytes of a PIN Code on this device.
     */
    public Integer minPinCodeLength; // 24 uint8 R V
    /**
     * Indicates the maximum length in bytes of a RFID Code on this device. The value depends on the RFID code range
     * specified by the manufacturer, if media anti-collision identifiers (UID) are used as RFID code, a value of 20
     * (equals 10 Byte ISO 14443A UID) is recommended.
     */
    public Integer maxRfidCodeLength; // 25 uint8 R V
    /**
     * Indicates the minimum length in bytes of a RFID Code on this device. The value depends on the RFID code range
     * specified by the manufacturer, if media anti-collision identifiers (UID) are used as RFID code, a value of 8
     * (equals 4 Byte ISO 14443A UID) is recommended.
     */
    public Integer minRfidCodeLength; // 26 uint8 R V
    /**
     * This attribute shall contain a bitmap with the bits set for the values of CredentialRuleEnum supported on this
     * device.
     */
    public CredentialRulesBitmap credentialRulesSupport; // 27 CredentialRulesBitmap R V
    /**
     * Indicates the number of credentials that could be assigned for each user.
     * Depending on the value of NumberOfRFIDUsersSupported and NumberOfPINUsersSupported it may not be possible to
     * assign that number of credentials for a user.
     * For example, if the device supports only PIN and RFID credential types, NumberOfCredentialsSupportedPerUser is
     * set to 10, NumberOfPINUsersSupported is set to 5 and NumberOfRFIDUsersSupported is set to 3, it will not be
     * possible to actually assign 10 credentials for a user because maximum number of credentials in the database is 8.
     */
    public Integer numberOfCredentialsSupportedPerUser; // 28 uint8 R V
    /**
     * Indicates the language for the on-screen or audible user interface using a 2- byte language code from ISO-639-1.
     */
    public String language; // 33 string R[W] VM
    /**
     * Indicates the settings for the LED support, as defined by LEDSettingEnum.
     */
    public LEDSettingEnum ledSettings; // 34 LEDSettingEnum R[W] VM
    /**
     * Indicates the number of seconds to wait after unlocking a lock before it automatically locks again.
     * 0&#x3D;disabled. If set, unlock operations from any source will be timed. For one time unlock with timeout use
     * the specific command.
     */
    public Integer autoRelockTime; // 35 uint32 R[W] VM
    /**
     * Indicates the sound volume on a door lock as defined by SoundVolumeEnum.
     */
    public SoundVolumeEnum soundVolume; // 36 SoundVolumeEnum R[W] VM
    /**
     * This attribute shall indicate the current operating mode of the lock as defined in OperatingModeEnum.
     */
    public OperatingModeEnum operatingMode; // 37 OperatingModeEnum R[W] VM
    /**
     * This attribute shall contain a bitmap with all operating bits of the OperatingMode attribute supported by the
     * lock. All operating modes NOT supported by a lock shall be set to one. The value of the OperatingMode enumeration
     * defines the related bit to be set.
     */
    public OperatingModesBitmap supportedOperatingModes; // 38 OperatingModesBitmap R V
    /**
     * Indicates the default configurations as they are physically set on the device (example: hardware dip switch
     * setting, etc…) and represents the default setting for some of the attributes within this cluster (for example:
     * LED, Auto Lock, Sound Volume, and Operating Mode attributes).
     * This is a read-only attribute and is intended to allow clients to determine what changes may need to be made
     * without having to query all the included attributes. It may be beneficial for the clients to know what the
     * device’s original settings were in the event that the device needs to be restored to factory default settings.
     * If the Client device would like to query and modify the door lock server’s operating settings, it SHOULD send
     * read and write attribute requests to the specific attributes.
     * For example, the Sound Volume attribute default value is Silent Mode. However, it is possible that the current
     * Sound Volume is High Volume. Therefore, if the client wants to query/modify the current Sound Volume setting on
     * the server, the client SHOULD read/write to the Sound Volume attribute.
     */
    public ConfigurationRegisterBitmap defaultConfigurationRegister; // 39 ConfigurationRegisterBitmap R V
    /**
     * This attribute shall enable/disable local programming on the door lock of certain features (see
     * LocalProgrammingFeatures attribute). If this value is set to TRUE then local programming is enabled on the door
     * lock for all features. If it is set to FALSE then local programming is disabled on the door lock for those
     * features whose bit is set to 0 in the LocalProgrammingFeatures attribute. Local programming shall be enabled by
     * default.
     */
    public Boolean enableLocalProgramming; // 40 bool R[W] VA
    /**
     * This attribute shall enable/disable the ability to lock the door lock with a single touch on the door lock.
     */
    public Boolean enableOneTouchLocking; // 41 bool RW VM
    /**
     * This attribute shall enable/disable an inside LED that allows the user to see at a glance if the door is locked.
     */
    public Boolean enableInsideStatusLed; // 42 bool RW VM
    /**
     * This attribute shall enable/disable a button inside the door that is used to put the lock into privacy mode. When
     * the lock is in privacy mode it cannot be manipulated from the outside.
     */
    public Boolean enablePrivacyModeButton; // 43 bool RW VM
    /**
     * Indicates the local programming features that will be disabled when EnableLocalProgramming attribute is set to
     * False. If a door lock doesn’t support disabling one aspect of local programming it shall return CONSTRAINT_ERROR
     * during a write operation of this attribute. If the EnableLocalProgramming attribute is set to True then all local
     * programming features shall be enabled regardless of the bits set to 0 in this attribute.
     * The features that can be disabled from local programming are defined in LocalProgrammingFeaturesBitmap.
     */
    public LocalProgrammingFeaturesBitmap localProgrammingFeatures; // 44 LocalProgrammingFeaturesBitmap R[W] VA
    /**
     * Indicates the number of incorrect Pin codes or RFID presentment attempts a user is allowed to enter before the
     * lock will enter a lockout state. The value of this attribute is compared to all failing forms of credential
     * presentation, including Pin codes used in an Unlock Command when RequirePINforRemoteOperation is set to true.
     * Valid range is 1-255 incorrect attempts. The lockout state will be for the duration of
     * UserCodeTemporaryDisableTime. If the attribute accepts writes and an attempt to write the value 0 is made, the
     * device shall respond with CONSTRAINT_ERROR.
     * The lock may reset the counter used to track incorrect credential presentations as required by internal logic,
     * environmental events, or other reasons. The lock shall reset the counter if a valid credential is presented.
     */
    public Integer wrongCodeEntryLimit; // 48 uint8 R[W] VA
    /**
     * Indicates the number of seconds that the lock shuts down following wrong code entry. Valid range is 1-255
     * seconds. Device can shut down to lock user out for specified amount of time. (Makes it difficult to try and guess
     * a PIN for the device.) If the attribute accepts writes and an attempt to write the attribute to 0 is made, the
     * device shall respond with CONSTRAINT_ERROR.
     */
    public Integer userCodeTemporaryDisableTime; // 49 uint8 R[W] VA
    /**
     * Indicates the door locks ability to send PINs over the air. If the attribute is True it is ok for the door lock
     * server to send PINs over the air. This attribute determines the behavior of the server’s TX operation. If it is
     * false, then it is not ok for the device to send PIN in any messages over the air.
     * The PIN field within any door lock cluster message shall keep the first octet unchanged and masks the actual code
     * by replacing with 0xFF. For example (PIN &quot;1234&quot; ): If the attribute value is True, 0x04 0x31 0x32 0x33
     * 0x34 shall be used in the PIN field in any door lock cluster message payload. If the attribute value is False,
     * 0x04 0xFF 0xFF 0xFF 0xFF shall be used.
     */
    public Boolean sendPinOverTheAir; // 50 bool R[W] VA
    /**
     * Indicates if the door lock requires an optional PIN. If this attribute is set to True, the door lock server
     * requires that an optional PINs be included in the payload of remote lock operation events like Lock, Unlock,
     * Unlock with Timeout and Toggle in order to function.
     */
    public Boolean requirePinForRemoteOperation; // 51 bool R[W] VA
    /**
     * Indicates the number of minutes a PIN, RFID, Fingerprint, or other credential associated with a user of type
     * ExpiringUser shall remain valid after its first use before expiring. When the credential expires the UserStatus
     * for the corresponding user record shall be set to OccupiedDisabled.
     */
    public Integer expiringUserTimeout; // 53 uint16 R[W] VA
    /**
     * This attribute is only supported if the Alarms cluster is on the same endpoint. The alarm mask is used to turn
     * on/off alarms for particular functions. Alarms for an alarm group are enabled if the associated alarm mask bit is
     * set. Each bit represents a group of alarms. Entire alarm groups can be turned on or off by setting or clearing
     * the associated bit in the alarm mask.
     * This mask DOES NOT apply to the Events mechanism of this cluster.
     */
    public AlarmMaskBitmap alarmMask; // 64 AlarmMaskBitmap RW VA
    /**
     * Indicates the verification key component of the Reader’s key pair as defined in [Aliro]. The value, if not null,
     * shall be an uncompressed elliptic curve public key as defined in section 2.3.3 of SEC 1.
     * Null if no Reader key pair has been configured on the lock. See SetAliroReaderConfig.
     */
    public OctetString aliroReaderVerificationKey; // 128 octstr R A
    /**
     * Indicates the reader_group_identifier as defined in [Aliro].
     * Null if no reader_group_identifier has been configured on the lock. See SetAliroReaderConfig.
     */
    public OctetString aliroReaderGroupIdentifier; // 129 octstr R A
    /**
     * Indicates the reader_group_sub_identifier as defined in [Aliro].
     */
    public OctetString aliroReaderGroupSubIdentifier; // 130 octstr R A
    /**
     * Indicates the list of protocol versions supported for expedited transactions as defined in [Aliro].
     */
    public List<OctetString> aliroExpeditedTransactionSupportedProtocolVersions; // 131 list R A
    /**
     * Indicates the Group Resolving Key as defined in [Aliro].
     * Null if no group resolving key has been configured on the lock. See SetAliroReaderConfig.
     */
    public OctetString aliroGroupResolvingKey; // 132 octstr R A
    /**
     * Indicates the list of protocol versions supported for the Bluetooth LE + UWB Access Control Flow as defined in
     * [Aliro].
     */
    public List<OctetString> aliroSupportedBleuwbProtocolVersions; // 133 list R A
    /**
     * Indicates the version of the Bluetooth LE advertisement as defined in [Aliro].
     */
    public Integer aliroBleAdvertisingVersion; // 134 uint8 R A
    /**
     * Indicates the maximum number of AliroCredentialIssuerKey credentials that can be stored on the lock.
     */
    public Integer numberOfAliroCredentialIssuerKeysSupported; // 135 uint16 R V
    /**
     * Indicates the maximum number of endpoint key credentials that can be stored on the lock. This limit applies to
     * the sum of the number of AliroEvictableEndpointKey credentials and the number of AliroNonEvictableEndpointKey
     * credentials.
     * &gt; [!NOTE]
     * &gt; The credential indices used for these two credential types are independent of each other, similar to all
     * other credential types. As long as NumberOfAliroEndpointKeysSupported is at least 2 a client could add a
     * credential of type AliroEvictableEndpointKey at any index from 1 to NumberOfAliroEndpointKeysSupported and also
     * add a credential of type AliroNonEvictableEndpointKey at the same index, and both credentials would exist on the
     * server.
     */
    public Integer numberOfAliroEndpointKeysSupported; // 136 uint16 R V

    // Structs
    /**
     * The door lock server provides several alarms which can be sent when there is a critical state on the door lock.
     * The alarms available for the door lock server are listed in AlarmCodeEnum.
     */
    public static class DoorLockAlarm {
        /**
         * This field shall indicate the alarm code of the event that has happened.
         */
        public AlarmCodeEnum alarmCode; // AlarmCodeEnum

        public DoorLockAlarm(AlarmCodeEnum alarmCode) {
            this.alarmCode = alarmCode;
        }
    }

    /**
     * The door lock server sends out a DoorStateChange event when the door lock door state changes.
     */
    public static class DoorStateChange {
        /**
         * This field shall indicate the new door state for this door event.
         */
        public DoorStateEnum doorState; // DoorStateEnum

        public DoorStateChange(DoorStateEnum doorState) {
            this.doorState = doorState;
        }
    }

    /**
     * The door lock server sends out a LockOperation event when the event is triggered by the various lock operation
     * sources.
     * • If the door lock server supports the Unbolt Door command, it shall generate a LockOperation event with
     * LockOperationType set to Unlock after an Unbolt Door command succeeds.
     * • If the door lock server supports the Unbolting feature and an Unlock Door command is performed, it shall
     * generate a LockOperation event with LockOperationType set to Unlatch when the unlatched state is reached and a
     * LockOperation event with LockOperationType set to Unlock when the lock successfully completes the unlock → hold
     * latch → release latch and return to unlock state operation.
     * • If the command fails during holding or releasing the latch but after passing the unlocked state, the door lock
     * server shall generate a LockOperationError event with LockOperationType set to Unlatch and a LockOperation event
     * with LockOperationType set to Unlock.
     * ◦ If it fails before reaching the unlocked state, the door lock server shall generate only a LockOperationError
     * event with LockOperationType set to Unlock.
     * • Upon manual actuation, a door lock server that supports the Unbolting feature:
     * ◦ shall generate a LockOperation event of LockOperationType Unlatch when it is actuated from the outside.
     * ◦ may generate a LockOperation event of LockOperationType Unlatch when it is actuated from the inside.
     */
    public static class LockOperation {
        /**
         * This field shall indicate the type of the lock operation that was performed.
         */
        public LockOperationTypeEnum lockOperationType; // LockOperationTypeEnum
        /**
         * This field shall indicate the source of the lock operation that was performed.
         */
        public OperationSourceEnum operationSource; // OperationSourceEnum
        /**
         * This field shall indicate the UserIndex who performed the lock operation. This shall be null if there is no
         * user index that can be determined for the given operation source. This shall NOT be null if a user index can
         * be determined. In particular, this shall NOT be null if the operation was associated with a valid credential.
         */
        public Integer userIndex; // uint16
        /**
         * This field shall indicate the fabric index of the fabric that performed the lock operation. This shall be
         * null if there is no fabric that can be determined for the given operation source. This shall NOT be null if
         * the operation source is &quot;Remote&quot;.
         */
        public Integer fabricIndex; // fabric-idx
        /**
         * This field shall indicate the Node ID of the node that performed the lock operation. This shall be null if
         * there is no Node associated with the given operation source. This shall NOT be null if the operation source
         * is &quot;Remote&quot;.
         */
        public BigInteger sourceNode; // node-id
        /**
         * This field shall indicate the list of credentials used in performing the lock operation. This shall be null
         * if no credentials were involved.
         */
        public List<CredentialStruct> credentials; // list

        public LockOperation(LockOperationTypeEnum lockOperationType, OperationSourceEnum operationSource,
                Integer userIndex, Integer fabricIndex, BigInteger sourceNode, List<CredentialStruct> credentials) {
            this.lockOperationType = lockOperationType;
            this.operationSource = operationSource;
            this.userIndex = userIndex;
            this.fabricIndex = fabricIndex;
            this.sourceNode = sourceNode;
            this.credentials = credentials;
        }
    }

    /**
     * The door lock server sends out a LockOperationError event when a lock operation fails for various reasons.
     */
    public static class LockOperationError {
        /**
         * This field shall indicate the type of the lock operation that was performed.
         */
        public LockOperationTypeEnum lockOperationType; // LockOperationTypeEnum
        /**
         * This field shall indicate the source of the lock operation that was performed.
         */
        public OperationSourceEnum operationSource; // OperationSourceEnum
        /**
         * This field shall indicate the lock operation error triggered when the operation was performed.
         */
        public OperationErrorEnum operationError; // OperationErrorEnum
        /**
         * This field shall indicate the lock UserIndex who performed the lock operation. This shall be null if there is
         * no user id that can be determined for the given operation source.
         */
        public Integer userIndex; // uint16
        /**
         * This field shall indicate the fabric index of the fabric that performed the lock operation. This shall be
         * null if there is no fabric that can be determined for the given operation source. This shall NOT be null if
         * the operation source is &quot;Remote&quot;.
         */
        public Integer fabricIndex; // fabric-idx
        /**
         * This field shall indicate the Node ID of the node that performed the lock operation. This shall be null if
         * there is no Node associated with the given operation source. This shall NOT be null if the operation source
         * is &quot;Remote&quot;.
         */
        public BigInteger sourceNode; // node-id
        /**
         * This field shall indicate the list of credentials used in performing the lock operation. This shall be null
         * if no credentials were involved.
         */
        public List<CredentialStruct> credentials; // list

        public LockOperationError(LockOperationTypeEnum lockOperationType, OperationSourceEnum operationSource,
                OperationErrorEnum operationError, Integer userIndex, Integer fabricIndex, BigInteger sourceNode,
                List<CredentialStruct> credentials) {
            this.lockOperationType = lockOperationType;
            this.operationSource = operationSource;
            this.operationError = operationError;
            this.userIndex = userIndex;
            this.fabricIndex = fabricIndex;
            this.sourceNode = sourceNode;
            this.credentials = credentials;
        }
    }

    /**
     * The door lock server sends out a LockUserChange event when a lock user, schedule, or credential change has
     * occurred.
     */
    public static class LockUserChange {
        /**
         * This field shall indicate the lock data type that was changed.
         */
        public LockDataTypeEnum lockDataType; // LockDataTypeEnum
        /**
         * This field shall indicate the data operation performed on the lock data type changed.
         */
        public DataOperationTypeEnum dataOperationType; // DataOperationTypeEnum
        /**
         * This field shall indicate the source of the user data change.
         */
        public OperationSourceEnum operationSource; // OperationSourceEnum
        /**
         * This field shall indicate the lock UserIndex associated with the change (if any). This shall be null if there
         * is no specific user associated with the data operation. This shall be 0xFFFE if all users are affected (e.g.
         * Clear Users).
         */
        public Integer userIndex; // uint16
        /**
         * This field shall indicate the fabric index of the fabric that performed the change (if any). This shall be
         * null if there is no fabric that can be determined to have caused the change. This shall NOT be null if the
         * operation source is &quot;Remote&quot;.
         */
        public Integer fabricIndex; // fabric-idx
        /**
         * This field shall indicate the Node ID that performed the change (if any). The Node ID of the node that
         * performed the change. This shall be null if there was no Node involved in the change. This shall NOT be null
         * if the operation source is &quot;Remote&quot;.
         */
        public BigInteger sourceNode; // node-id
        /**
         * This field shall indicate the index of the specific item that was changed (e.g. schedule, PIN, RFID, etc.) in
         * the list of items identified by LockDataType. This shall be null if the LockDataType does not correspond to a
         * list that can be indexed into (e.g. ProgrammingUser). This shall be 0xFFFE if all indices are affected (e.g.
         * ClearPINCode, ClearRFIDCode, ClearWeekDaySchedule, ClearYearDaySchedule, etc.).
         */
        public Integer dataIndex; // uint16

        public LockUserChange(LockDataTypeEnum lockDataType, DataOperationTypeEnum dataOperationType,
                OperationSourceEnum operationSource, Integer userIndex, Integer fabricIndex, BigInteger sourceNode,
                Integer dataIndex) {
            this.lockDataType = lockDataType;
            this.dataOperationType = dataOperationType;
            this.operationSource = operationSource;
            this.userIndex = userIndex;
            this.fabricIndex = fabricIndex;
            this.sourceNode = sourceNode;
            this.dataIndex = dataIndex;
        }
    }

    /**
     * This struct shall indicate the credential types and their corresponding indices (if any) for the event or user
     * record.
     */
    public static class CredentialStruct {
        /**
         * This field shall indicate the credential field used to authorize the lock operation.
         */
        public CredentialTypeEnum credentialType; // CredentialTypeEnum
        /**
         * This field shall indicate the index of the specific credential used to authorize the lock operation in the
         * list of credentials identified by CredentialType (e.g. PIN, RFID, etc.). This field shall be set to 0 if
         * CredentialType is ProgrammingPIN or does not correspond to a list that can be indexed into.
         */
        public Integer credentialIndex; // uint16

        public CredentialStruct(CredentialTypeEnum credentialType, Integer credentialIndex) {
            this.credentialType = credentialType;
            this.credentialIndex = credentialIndex;
        }
    }

    // Enums
    /**
     * This enumeration shall indicate the alarm type.
     */
    public enum AlarmCodeEnum implements MatterEnum {
        LOCK_JAMMED(0, "Lock Jammed"),
        LOCK_FACTORY_RESET(1, "Lock Factory Reset"),
        LOCK_RADIO_POWER_CYCLED(3, "Lock Radio Power Cycled"),
        WRONG_CODE_ENTRY_LIMIT(4, "Wrong Code Entry Limit"),
        FRONT_ESCEUTCHEON_REMOVED(5, "Front Esceutcheon Removed"),
        DOOR_FORCED_OPEN(6, "Door Forced Open"),
        DOOR_AJAR(7, "Door Ajar"),
        FORCED_USER(8, "Forced User");

        public final Integer value;
        public final String label;

        private AlarmCodeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * This enumeration shall indicate the credential rule that can be applied to a particular user.
     */
    public enum CredentialRuleEnum implements MatterEnum {
        SINGLE(0, "Single"),
        DUAL(1, "Dual"),
        TRI(2, "Tri");

        public final Integer value;
        public final String label;

        private CredentialRuleEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * This enumeration shall indicate the credential type.
     */
    public enum CredentialTypeEnum implements MatterEnum {
        PROGRAMMING_PIN(0, "Programming Pin"),
        PIN(1, "Pin"),
        RFID(2, "Rfid"),
        FINGERPRINT(3, "Fingerprint"),
        FINGER_VEIN(4, "Finger Vein"),
        FACE(5, "Face"),
        ALIRO_CREDENTIAL_ISSUER_KEY(6, "Aliro Credential Issuer Key"),
        ALIRO_EVICTABLE_ENDPOINT_KEY(7, "Aliro Evictable Endpoint Key"),
        ALIRO_NON_EVICTABLE_ENDPOINT_KEY(8, "Aliro Non Evictable Endpoint Key");

        public final Integer value;
        public final String label;

        private CredentialTypeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * This enumeration shall indicate the data operation performed.
     */
    public enum DataOperationTypeEnum implements MatterEnum {
        ADD(0, "Add"),
        CLEAR(1, "Clear"),
        MODIFY(2, "Modify");

        public final Integer value;
        public final String label;

        private DataOperationTypeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * This enumeration shall indicate the current door state.
     */
    public enum DoorStateEnum implements MatterEnum {
        DOOR_OPEN(0, "Door Open"),
        DOOR_CLOSED(1, "Door Closed"),
        DOOR_JAMMED(2, "Door Jammed"),
        DOOR_FORCED_OPEN(3, "Door Forced Open"),
        DOOR_UNSPECIFIED_ERROR(4, "Door Unspecified Error"),
        DOOR_AJAR(5, "Door Ajar");

        public final Integer value;
        public final String label;

        private DoorStateEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * This enumeration shall indicate the data type that is being or has changed.
     */
    public enum LockDataTypeEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        PROGRAMMING_CODE(1, "Programming Code"),
        USER_INDEX(2, "User Index"),
        WEEK_DAY_SCHEDULE(3, "Week Day Schedule"),
        YEAR_DAY_SCHEDULE(4, "Year Day Schedule"),
        HOLIDAY_SCHEDULE(5, "Holiday Schedule"),
        PIN(6, "Pin"),
        RFID(7, "Rfid"),
        FINGERPRINT(8, "Fingerprint"),
        FINGER_VEIN(9, "Finger Vein"),
        FACE(10, "Face"),
        ALIRO_CREDENTIAL_ISSUER_KEY(11, "Aliro Credential Issuer Key"),
        ALIRO_EVICTABLE_ENDPOINT_KEY(12, "Aliro Evictable Endpoint Key"),
        ALIRO_NON_EVICTABLE_ENDPOINT_KEY(13, "Aliro Non Evictable Endpoint Key");

        public final Integer value;
        public final String label;

        private LockDataTypeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * This enumeration shall indicate the type of Lock operation performed.
     */
    public enum LockOperationTypeEnum implements MatterEnum {
        LOCK(0, "Lock"),
        UNLOCK(1, "Unlock"),
        NON_ACCESS_USER_EVENT(2, "Non Access User Event"),
        FORCED_USER_EVENT(3, "Forced User Event"),
        UNLATCH(4, "Unlatch");

        public final Integer value;
        public final String label;

        private LockOperationTypeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * This enumeration shall indicate the error cause of the Lock/Unlock operation performed.
     */
    public enum OperationErrorEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        INVALID_CREDENTIAL(1, "Invalid Credential"),
        DISABLED_USER_DENIED(2, "Disabled User Denied"),
        RESTRICTED(3, "Restricted"),
        INSUFFICIENT_BATTERY(4, "Insufficient Battery");

        public final Integer value;
        public final String label;

        private OperationErrorEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * This enumeration shall indicate the lock operating mode.
     * The table below shows the operating mode and which interfaces are enabled, if supported, for each mode.
     * Interface Operational: Yes, No or N/A
     * &gt; [!NOTE]
     * &gt; For modes that disable the remote interface, the door lock shall respond to Lock, Unlock, Toggle, and Unlock
     * with Timeout commands with a response status Failure and not take the action requested by those commands. The
     * door lock shall NOT disable the radio or otherwise unbind or leave the network. It shall still respond to all
     * other commands and requests.
     */
    public enum OperatingModeEnum implements MatterEnum {
        NORMAL(0, "Normal"),
        VACATION(1, "Vacation"),
        PRIVACY(2, "Privacy"),
        NO_REMOTE_LOCK_UNLOCK(3, "No Remote Lock Unlock"),
        PASSAGE(4, "Passage");

        public final Integer value;
        public final String label;

        private OperatingModeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * This enumeration shall indicate the source of the Lock/Unlock or user change operation performed.
     */
    public enum OperationSourceEnum implements MatterEnum {
        UNSPECIFIED(0, "Unspecified"),
        MANUAL(1, "Manual"),
        PROPRIETARY_REMOTE(2, "Proprietary Remote"),
        KEYPAD(3, "Keypad"),
        AUTO(4, "Auto"),
        BUTTON(5, "Button"),
        SCHEDULE(6, "Schedule"),
        REMOTE(7, "Remote"),
        RFID(8, "Rfid"),
        BIOMETRIC(9, "Biometric"),
        ALIRO(10, "Aliro");

        public final Integer value;
        public final String label;

        private OperationSourceEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * This enumeration shall indicate what the status is for a specific user ID.
     */
    public enum UserStatusEnum implements MatterEnum {
        AVAILABLE(0, "Available"),
        OCCUPIED_ENABLED(1, "Occupied Enabled"),
        OCCUPIED_DISABLED(3, "Occupied Disabled");

        public final Integer value;
        public final String label;

        private UserStatusEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    /**
     * This enumeration shall indicate what the type is for a specific user ID.
     */
    public enum UserTypeEnum implements MatterEnum {
        UNRESTRICTED_USER(0, "Unrestricted User"),
        YEAR_DAY_SCHEDULE_USER(1, "Year Day Schedule User"),
        WEEK_DAY_SCHEDULE_USER(2, "Week Day Schedule User"),
        PROGRAMMING_USER(3, "Programming User"),
        NON_ACCESS_USER(4, "Non Access User"),
        FORCED_USER(5, "Forced User"),
        DISPOSABLE_USER(6, "Disposable User"),
        EXPIRING_USER(7, "Expiring User"),
        SCHEDULE_RESTRICTED_USER(8, "Schedule Restricted User"),
        REMOTE_ONLY_USER(9, "Remote Only User");

        public final Integer value;
        public final String label;

        private UserTypeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum LockStateEnum implements MatterEnum {
        NOT_FULLY_LOCKED(0, "Not Fully Locked"),
        LOCKED(1, "Locked"),
        UNLOCKED(2, "Unlocked"),
        UNLATCHED(3, "Unlatched");

        public final Integer value;
        public final String label;

        private LockStateEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum LockTypeEnum implements MatterEnum {
        DEAD_BOLT(0, "Dead Bolt"),
        MAGNETIC(1, "Magnetic"),
        OTHER(2, "Other"),
        MORTISE(3, "Mortise"),
        RIM(4, "Rim"),
        LATCH_BOLT(5, "Latch Bolt"),
        CYLINDRICAL_LOCK(6, "Cylindrical Lock"),
        TUBULAR_LOCK(7, "Tubular Lock"),
        INTERCONNECTED_LOCK(8, "Interconnected Lock"),
        DEAD_LATCH(9, "Dead Latch"),
        DOOR_FURNITURE(10, "Door Furniture"),
        EUROCYLINDER(11, "Eurocylinder");

        public final Integer value;
        public final String label;

        private LockTypeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum LEDSettingEnum implements MatterEnum {
        NO_LED_SIGNAL(0, "No Led Signal"),
        NO_LED_SIGNAL_ACCESS_ALLOWED(1, "No Led Signal Access Allowed"),
        LED_SIGNAL_ALL(2, "Led Signal All");

        public final Integer value;
        public final String label;

        private LEDSettingEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum SoundVolumeEnum implements MatterEnum {
        SILENT(0, "Silent"),
        LOW(1, "Low"),
        HIGH(2, "High"),
        MEDIUM(3, "Medium");

        public final Integer value;
        public final String label;

        private SoundVolumeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum EventTypeEnum implements MatterEnum {
        OPERATION(0, "Operation"),
        PROGRAMMING(1, "Programming"),
        ALARM(2, "Alarm");

        public final Integer value;
        public final String label;

        private EventTypeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    public enum StatusCodeEnum implements MatterEnum {
        DUPLICATE(2, "Duplicate"),
        OCCUPIED(3, "Occupied");

        public final Integer value;
        public final String label;

        private StatusCodeEnum(Integer value, String label) {
            this.value = value;
            this.label = label;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    // Bitmaps
    /**
     * This bitmap shall indicate the days of the week the Week Day schedule applies for.
     */
    public static class DaysMaskBitmap {
        public boolean sunday;
        public boolean monday;
        public boolean tuesday;
        public boolean wednesday;
        public boolean thursday;
        public boolean friday;
        public boolean saturday;

        public DaysMaskBitmap(boolean sunday, boolean monday, boolean tuesday, boolean wednesday, boolean thursday,
                boolean friday, boolean saturday) {
            this.sunday = sunday;
            this.monday = monday;
            this.tuesday = tuesday;
            this.wednesday = wednesday;
            this.thursday = thursday;
            this.friday = friday;
            this.saturday = saturday;
        }
    }

    public static class CredentialRulesBitmap {
        public boolean single;
        public boolean dual;
        public boolean tri;

        public CredentialRulesBitmap(boolean single, boolean dual, boolean tri) {
            this.single = single;
            this.dual = dual;
            this.tri = tri;
        }
    }

    public static class OperatingModesBitmap {
        public boolean normal;
        public boolean vacation;
        public boolean privacy;
        public boolean noRemoteLockUnlock;
        public boolean passage;
        public short alwaysSet;

        public OperatingModesBitmap(boolean normal, boolean vacation, boolean privacy, boolean noRemoteLockUnlock,
                boolean passage, short alwaysSet) {
            this.normal = normal;
            this.vacation = vacation;
            this.privacy = privacy;
            this.noRemoteLockUnlock = noRemoteLockUnlock;
            this.passage = passage;
            this.alwaysSet = alwaysSet;
        }
    }

    public static class ConfigurationRegisterBitmap {
        /**
         * The state of local programming functionality
         * This bit shall indicate the state related to local programming:
         * • 0 &#x3D; Local programming is disabled
         * • 1 &#x3D; Local programming is enabled
         */
        public boolean localProgramming;
        /**
         * The state of the keypad interface
         * This bit shall indicate the state related to keypad interface:
         * • 0 &#x3D; Keypad interface is disabled
         * • 1 &#x3D; Keypad interface is enabled
         */
        public boolean keypadInterface;
        /**
         * The state of the remote interface
         * This bit shall indicate the state related to remote interface:
         * • 0 &#x3D; Remote interface is disabled
         * • 1 &#x3D; Remote interface is enabled
         */
        public boolean remoteInterface;
        /**
         * Sound volume is set to Silent value
         * This bit shall indicate the state related to sound volume:
         * • 0 &#x3D; Sound volume value is 0 (Silent)
         * • 1 &#x3D; Sound volume value is equal to something other than 0
         */
        public boolean soundVolume;
        /**
         * Auto relock time it set to 0
         * This bit shall indicate the state related to auto relock time:
         * • 0 &#x3D; Auto relock time value is 0
         * • 1 &#x3D; Auto relock time value is equal to something other than 0
         */
        public boolean autoRelockTime;
        /**
         * LEDs is disabled
         * This bit shall indicate the state related to LED settings:
         * • 0 &#x3D; LED settings value is 0 (NoLEDSignal)
         * • 1 &#x3D; LED settings value is equal to something other than 0
         */
        public boolean ledSettings;

        public ConfigurationRegisterBitmap(boolean localProgramming, boolean keypadInterface, boolean remoteInterface,
                boolean soundVolume, boolean autoRelockTime, boolean ledSettings) {
            this.localProgramming = localProgramming;
            this.keypadInterface = keypadInterface;
            this.remoteInterface = remoteInterface;
            this.soundVolume = soundVolume;
            this.autoRelockTime = autoRelockTime;
            this.ledSettings = ledSettings;
        }
    }

    public static class LocalProgrammingFeaturesBitmap {
        /**
         * The state of the ability to add users, credentials or schedules on the device
         * This bit shall indicate whether the door lock is able to add Users/Credentials/Schedules locally:
         * • 0 &#x3D; This ability is disabled
         * • 1 &#x3D; This ability is enabled
         */
        public boolean addUsersCredentialsSchedules;
        /**
         * The state of the ability to modify users, credentials or schedules on the device
         * This bit shall indicate whether the door lock is able to modify Users/Credentials/Schedules locally:
         * • 0 &#x3D; This ability is disabled
         * • 1 &#x3D; This ability is enabled
         */
        public boolean modifyUsersCredentialsSchedules;
        /**
         * The state of the ability to clear users, credentials or schedules on the device
         * This bit shall indicate whether the door lock is able to clear Users/Credentials/Schedules locally:
         * • 0 &#x3D; This ability is disabled
         * • 1 &#x3D; This ability is enabled
         */
        public boolean clearUsersCredentialsSchedules;
        /**
         * The state of the ability to adjust settings on the device
         * This bit shall indicate whether the door lock is able to adjust lock settings locally:
         * • 0 &#x3D; This ability is disabled
         * • 1 &#x3D; This ability is enabled
         */
        public boolean adjustSettings;

        public LocalProgrammingFeaturesBitmap(boolean addUsersCredentialsSchedules,
                boolean modifyUsersCredentialsSchedules, boolean clearUsersCredentialsSchedules,
                boolean adjustSettings) {
            this.addUsersCredentialsSchedules = addUsersCredentialsSchedules;
            this.modifyUsersCredentialsSchedules = modifyUsersCredentialsSchedules;
            this.clearUsersCredentialsSchedules = clearUsersCredentialsSchedules;
            this.adjustSettings = adjustSettings;
        }
    }

    public static class AlarmMaskBitmap {
        public boolean lockJammed;
        public boolean lockFactoryReset;
        public boolean lockRadioPowerCycled;
        public boolean wrongCodeEntryLimit;
        public boolean frontEscutcheonRemoved;
        public boolean doorForcedOpen;

        public AlarmMaskBitmap(boolean lockJammed, boolean lockFactoryReset, boolean lockRadioPowerCycled,
                boolean wrongCodeEntryLimit, boolean frontEscutcheonRemoved, boolean doorForcedOpen) {
            this.lockJammed = lockJammed;
            this.lockFactoryReset = lockFactoryReset;
            this.lockRadioPowerCycled = lockRadioPowerCycled;
            this.wrongCodeEntryLimit = wrongCodeEntryLimit;
            this.frontEscutcheonRemoved = frontEscutcheonRemoved;
            this.doorForcedOpen = doorForcedOpen;
        }
    }

    public static class FeatureMap {
        /**
         * 
         * If the User Feature is also supported then any PIN Code stored in the lock shall be associated with a User.
         * A lock may support multiple credential types so if the User feature is supported the UserType, UserStatus and
         * Schedules are all associated with a User index and not directly with a PIN index. A User index may have
         * several credentials associated with it.
         */
        public boolean pinCredential;
        /**
         * 
         * If the User Feature is also supported then any RFID credential stored in the lock shall be associated with a
         * User.
         * A lock may support multiple credential types so if the User feature is supported the UserType, UserStatus and
         * Schedules are all associated with a User index and not directly with a RFID index. A User Index may have
         * several credentials associated with it.
         */
        public boolean rfidCredential;
        /**
         * 
         * Currently the cluster only defines the metadata format for notifications when a fingerprint/ finger vein
         * credential is used to access the lock and doesn’t describe how to create fingerprint/finger vein credentials.
         * If the Users feature is also supported then the User that a fingerprint/finger vein is associated with can
         * also have its UserType, UserStatus and Schedule modified.
         * A lock may support multiple credential types so if the User feature is supported the UserType, UserStatus and
         * Schedules are all associated with a User index and not directly with a Finger index. A User Index may have
         * several credentials associated with it.
         */
        public boolean fingerCredentials;
        /**
         * 
         * If the User feature is supported then Week Day Schedules are applied to a User and not a credential.
         * Week Day Schedules are used to restrict access to a specified time window on certain days of the week. The
         * schedule is repeated each week.
         * The lock may automatically adjust the UserType when a schedule is created or cleared.
         * Support for WeekDayAccessSchedules requires that the lock has the capability of keeping track of local time.
         */
        public boolean weekDayAccessSchedules;
        /**
         * 
         * If this feature is supported this indicates that the lock has the ability to determine the position of the
         * door which is separate from the state of the lock.
         */
        public boolean doorPositionSensor;
        /**
         * 
         * Currently the cluster only defines the metadata format for notifications when a face recognition, iris, or
         * retina credential is used to access the lock and doesn’t describe how to create face recognition, iris, or
         * retina credentials. If the Users feature is also supported then the User that a face recognition, iris, or
         * retina credential is associated with can also have its UserType, UserStatus and Schedule modified.
         * A lock may support multiple credential types so if the User feature is supported the UserType, UserStatus and
         * Schedules are all associated with a User and not directly with a credential.
         */
        public boolean faceCredentials;
        /**
         * 
         * If this feature is supported then the lock supports the ability to verify a credential provided in a
         * lock/unlock command. Currently the cluster only supports providing the PIN credential to the lock/unlock
         * commands. If this feature is supported then the PIN Credential feature shall also be supported.
         */
        public boolean credentialOverTheAirAccess;
        /**
         * 
         * If the User Feature is supported then a lock employs a User database. A User within the User database is used
         * to associate credentials and schedules to single user record within the lock. This also means the UserType
         * and UserStatus fields are associated with a User and not a credential.
         */
        public boolean user;
        /**
         * 
         * If the User feature is supported then Year Day Schedules are applied to a User and not a credential. Year Day
         * Schedules are used to restrict access to a specified date and time window.
         * The lock may automatically adjust the UserType when a schedule is created or cleared.
         * Support for YearDayAccessSchedules requires that the lock has the capability of keeping track of local time.
         */
        public boolean yearDayAccessSchedules;
        /**
         * 
         * This feature is used to setup Holiday Schedule in the lock device. A Holiday Schedule sets a start and stop
         * end date/time for the lock to use the specified operating mode set by the Holiday Schedule.
         * Support for HolidaySchedules requires that the lock has the capability of keeping track of local time.
         */
        public boolean holidaySchedules;
        /**
         * 
         * Locks that support this feature differentiate between unbolting and unlocking. The Unbolt Door command
         * retracts the bolt without pulling the latch. The Unlock Door command fully unlocks the door by retracting the
         * bolt and briefly pulling the latch. While the latch is pulled, the lock state changes to Unlatched. Locks
         * without unbolting support don’t differentiate between unbolting and unlocking and perform the same operation
         * for both commands.
         */
        public boolean unbolting;
        /**
         * 
         * Locks that support this feature implement the Aliro specification as defined in [Aliro] and support Matter as
         * a method for provisioning Aliro credentials.
         */
        public boolean aliroProvisioning;
        /**
         * 
         * Locks that support this feature implement the Bluetooth LE + UWB Access Control Flow as defined in [Aliro].
         */
        public boolean aliroBleuwb;

        public FeatureMap(boolean pinCredential, boolean rfidCredential, boolean fingerCredentials,
                boolean weekDayAccessSchedules, boolean doorPositionSensor, boolean faceCredentials,
                boolean credentialOverTheAirAccess, boolean user, boolean yearDayAccessSchedules,
                boolean holidaySchedules, boolean unbolting, boolean aliroProvisioning, boolean aliroBleuwb) {
            this.pinCredential = pinCredential;
            this.rfidCredential = rfidCredential;
            this.fingerCredentials = fingerCredentials;
            this.weekDayAccessSchedules = weekDayAccessSchedules;
            this.doorPositionSensor = doorPositionSensor;
            this.faceCredentials = faceCredentials;
            this.credentialOverTheAirAccess = credentialOverTheAirAccess;
            this.user = user;
            this.yearDayAccessSchedules = yearDayAccessSchedules;
            this.holidaySchedules = holidaySchedules;
            this.unbolting = unbolting;
            this.aliroProvisioning = aliroProvisioning;
            this.aliroBleuwb = aliroBleuwb;
        }
    }

    public DoorLockCluster(BigInteger nodeId, int endpointId) {
        super(nodeId, endpointId, 257, "DoorLock");
    }

    protected DoorLockCluster(BigInteger nodeId, int endpointId, int clusterId, String clusterName) {
        super(nodeId, endpointId, clusterId, clusterName);
    }

    // commands
    /**
     * This command causes the lock device to lock the door. This command includes an optional code for the lock. The
     * door lock may require a PIN depending on the value of the RequirePINForRemoteOperation attribute.
     */
    public static ClusterCommand lockDoor(OctetString pinCode) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (pinCode != null) {
            map.put("pinCode", pinCode);
        }
        return new ClusterCommand("lockDoor", map);
    }

    /**
     * This command causes the lock device to unlock the door. This command includes an optional code for the lock. The
     * door lock may require a code depending on the value of the RequirePINForRemoteOperation attribute.
     * &gt; [!NOTE]
     * &gt; If the attribute AutoRelockTime is supported the lock will transition to the locked state when the auto
     * relock time has expired.
     */
    public static ClusterCommand unlockDoor(OctetString pinCode) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (pinCode != null) {
            map.put("pinCode", pinCode);
        }
        return new ClusterCommand("unlockDoor", map);
    }

    public static ClusterCommand toggle() {
        return new ClusterCommand("toggle");
    }

    /**
     * This command causes the lock device to unlock the door with a timeout parameter. After the time in seconds
     * specified in the timeout field, the lock device will relock itself automatically. This timeout parameter is only
     * temporary for this message transition and overrides the default relock time as specified in the AutoRelockTime
     * attribute. If the door lock device is not capable of or does not want to support temporary Relock Timeout, it
     * SHOULD NOT support this optional command.
     */
    public static ClusterCommand unlockWithTimeout(Integer timeout, OctetString pinCode) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (timeout != null) {
            map.put("timeout", timeout);
        }
        if (pinCode != null) {
            map.put("pinCode", pinCode);
        }
        return new ClusterCommand("unlockWithTimeout", map);
    }

    /**
     * Set a PIN Code into the lock.
     * Return status is a global status code or a cluster-specific status code from the Status Codes table and shall be
     * one of the following values:
     */
    public static ClusterCommand setPinCode(Integer userId, UserStatusEnum userStatus, UserTypeEnum userType,
            OctetString pin) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (userId != null) {
            map.put("userId", userId);
        }
        if (userStatus != null) {
            map.put("userStatus", userStatus);
        }
        if (userType != null) {
            map.put("userType", userType);
        }
        if (pin != null) {
            map.put("pin", pin);
        }
        return new ClusterCommand("setPinCode", map);
    }

    /**
     * Retrieve a PIN Code.
     */
    public static ClusterCommand getPinCode(Integer userId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (userId != null) {
            map.put("userId", userId);
        }
        return new ClusterCommand("getPinCode", map);
    }

    /**
     * Clear a PIN code or all PIN codes.
     * For each PIN Code cleared whose user doesn’t have a RFID Code or other credential type, then corresponding user
     * record’s UserStatus value shall be set to Available, and UserType value shall be set to UnrestrictedUser and all
     * schedules shall be cleared.
     */
    public static ClusterCommand clearPinCode(Integer pinSlotIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (pinSlotIndex != null) {
            map.put("pinSlotIndex", pinSlotIndex);
        }
        return new ClusterCommand("clearPinCode", map);
    }

    /**
     * Clear out all PINs on the lock.
     * &gt; [!NOTE]
     * &gt; On the server, the clear all PIN codes command SHOULD have the same effect as the ClearPINCode command with
     * respect to the setting of user status, user type and schedules.
     */
    public static ClusterCommand clearAllPinCodes() {
        return new ClusterCommand("clearAllPinCodes");
    }

    /**
     * Set the status of a user ID.
     */
    public static ClusterCommand setUserStatus(Integer userId, UserStatusEnum userStatus) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (userId != null) {
            map.put("userId", userId);
        }
        if (userStatus != null) {
            map.put("userStatus", userStatus);
        }
        return new ClusterCommand("setUserStatus", map);
    }

    /**
     * Get the status of a user.
     */
    public static ClusterCommand getUserStatus(Integer userId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (userId != null) {
            map.put("userId", userId);
        }
        return new ClusterCommand("getUserStatus", map);
    }

    /**
     * Set a weekly repeating schedule for a specified user.
     * The associated UserType may be changed to ScheduleRestrictedUser by the lock when a Week Day schedule is set.
     * Return status shall be one of the following values:
     */
    public static ClusterCommand setWeekDaySchedule(Integer weekDayIndex, Integer userIndex, DaysMaskBitmap daysMask,
            Integer startHour, Integer startMinute, Integer endHour, Integer endMinute) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (weekDayIndex != null) {
            map.put("weekDayIndex", weekDayIndex);
        }
        if (userIndex != null) {
            map.put("userIndex", userIndex);
        }
        if (daysMask != null) {
            map.put("daysMask", daysMask);
        }
        if (startHour != null) {
            map.put("startHour", startHour);
        }
        if (startMinute != null) {
            map.put("startMinute", startMinute);
        }
        if (endHour != null) {
            map.put("endHour", endHour);
        }
        if (endMinute != null) {
            map.put("endMinute", endMinute);
        }
        return new ClusterCommand("setWeekDaySchedule", map);
    }

    /**
     * Retrieve the specific weekly schedule for the specific user.
     */
    public static ClusterCommand getWeekDaySchedule(Integer weekDayIndex, Integer userIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (weekDayIndex != null) {
            map.put("weekDayIndex", weekDayIndex);
        }
        if (userIndex != null) {
            map.put("userIndex", userIndex);
        }
        return new ClusterCommand("getWeekDaySchedule", map);
    }

    /**
     * Clear the specific weekly schedule or all weekly schedules for the specific user.
     * Return status shall be one of the following values:
     */
    public static ClusterCommand clearWeekDaySchedule(Integer weekDayIndex, Integer userIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (weekDayIndex != null) {
            map.put("weekDayIndex", weekDayIndex);
        }
        if (userIndex != null) {
            map.put("userIndex", userIndex);
        }
        return new ClusterCommand("clearWeekDaySchedule", map);
    }

    /**
     * Set a time-specific schedule ID for a specified user.
     * The associated UserType may be changed to ScheduleRestrictedUser by the lock when a Year Day schedule is set.
     * Return status shall be one of the following values:
     */
    public static ClusterCommand setYearDaySchedule(Integer yearDayIndex, Integer userIndex, Integer localStartTime,
            Integer localEndTime) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (yearDayIndex != null) {
            map.put("yearDayIndex", yearDayIndex);
        }
        if (userIndex != null) {
            map.put("userIndex", userIndex);
        }
        if (localStartTime != null) {
            map.put("localStartTime", localStartTime);
        }
        if (localEndTime != null) {
            map.put("localEndTime", localEndTime);
        }
        return new ClusterCommand("setYearDaySchedule", map);
    }

    /**
     * Retrieve the specific year day schedule for the specific schedule and user indexes.
     */
    public static ClusterCommand getYearDaySchedule(Integer yearDayIndex, Integer userIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (yearDayIndex != null) {
            map.put("yearDayIndex", yearDayIndex);
        }
        if (userIndex != null) {
            map.put("userIndex", userIndex);
        }
        return new ClusterCommand("getYearDaySchedule", map);
    }

    /**
     * Clears the specific year day schedule or all year day schedules for the specific user.
     * Return status shall be one of the following values:
     */
    public static ClusterCommand clearYearDaySchedule(Integer yearDayIndex, Integer userIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (yearDayIndex != null) {
            map.put("yearDayIndex", yearDayIndex);
        }
        if (userIndex != null) {
            map.put("userIndex", userIndex);
        }
        return new ClusterCommand("clearYearDaySchedule", map);
    }

    /**
     * Set the holiday Schedule by specifying local start time and local end time with respect to any Lock Operating
     * Mode.
     * Return status shall be one of the following values:
     */
    public static ClusterCommand setHolidaySchedule(Integer holidayIndex, Integer localStartTime, Integer localEndTime,
            OperatingModeEnum operatingMode) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (holidayIndex != null) {
            map.put("holidayIndex", holidayIndex);
        }
        if (localStartTime != null) {
            map.put("localStartTime", localStartTime);
        }
        if (localEndTime != null) {
            map.put("localEndTime", localEndTime);
        }
        if (operatingMode != null) {
            map.put("operatingMode", operatingMode);
        }
        return new ClusterCommand("setHolidaySchedule", map);
    }

    /**
     * Get the holiday schedule for the specified index.
     */
    public static ClusterCommand getHolidaySchedule(Integer holidayIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (holidayIndex != null) {
            map.put("holidayIndex", holidayIndex);
        }
        return new ClusterCommand("getHolidaySchedule", map);
    }

    /**
     * Clears the holiday schedule or all holiday schedules.
     */
    public static ClusterCommand clearHolidaySchedule(Integer holidayIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (holidayIndex != null) {
            map.put("holidayIndex", holidayIndex);
        }
        return new ClusterCommand("clearHolidaySchedule", map);
    }

    /**
     * Set the user type for a specified user.
     * For user type value please refer to User Type Value.
     * Return status shall be one of the following values:
     */
    public static ClusterCommand setUserType(Integer userId, UserTypeEnum userType) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (userId != null) {
            map.put("userId", userId);
        }
        if (userType != null) {
            map.put("userType", userType);
        }
        return new ClusterCommand("setUserType", map);
    }

    /**
     * Retrieve the user type for a specific user.
     */
    public static ClusterCommand getUserType(Integer userId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (userId != null) {
            map.put("userId", userId);
        }
        return new ClusterCommand("getUserType", map);
    }

    /**
     * Set an ID for RFID access into the lock.
     * Return status is a global status code or a cluster-specific status code from the Status Codes table and shall be
     * one of the following values:
     */
    public static ClusterCommand setRfidCode(Integer userId, UserStatusEnum userStatus, UserTypeEnum userType,
            OctetString rfidCode) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (userId != null) {
            map.put("userId", userId);
        }
        if (userStatus != null) {
            map.put("userStatus", userStatus);
        }
        if (userType != null) {
            map.put("userType", userType);
        }
        if (rfidCode != null) {
            map.put("rfidCode", rfidCode);
        }
        return new ClusterCommand("setRfidCode", map);
    }

    /**
     * Retrieve an RFID code.
     */
    public static ClusterCommand getRfidCode(Integer userId) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (userId != null) {
            map.put("userId", userId);
        }
        return new ClusterCommand("getRfidCode", map);
    }

    /**
     * Clear an RFID code or all RFID codes.
     * For each RFID Code cleared whose user doesn’t have a PIN Code or other credential type, then the corresponding
     * user record’s UserStatus value shall be set to Available, and UserType value shall be set to UnrestrictedUser and
     * all schedules shall be cleared.
     */
    public static ClusterCommand clearRfidCode(Integer rfidSlotIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (rfidSlotIndex != null) {
            map.put("rfidSlotIndex", rfidSlotIndex);
        }
        return new ClusterCommand("clearRfidCode", map);
    }

    /**
     * Clear out all RFIDs on the lock. If you clear all RFID codes and this user didn’t have a PIN code, the user
     * status has to be set to &quot;0 Available&quot;, the user type has to be set to the default value, and all
     * schedules which are supported have to be set to the default values.
     */
    public static ClusterCommand clearAllRfidCodes() {
        return new ClusterCommand("clearAllRfidCodes");
    }

    /**
     * Set user into the lock.
     * Fields used for different use cases:
     * Return status is a global status code or a cluster-specific status code from the Status Codes table and shall be
     * one of the following values:
     * • SUCCESS, if setting User was successful.
     * • FAILURE, if some unexpected internal error occurred setting User.
     * • OCCUPIED, if OperationType is Add and UserIndex points to an occupied slot.
     * • INVALID_COMMAND, if one or more fields violate constraints or are invalid or if OperationType is Modify and
     * UserIndex points to an available slot.
     */
    public static ClusterCommand setUser(DataOperationTypeEnum operationType, Integer userIndex, String userName,
            Integer userUniqueId, UserStatusEnum userStatus, UserTypeEnum userType, CredentialRuleEnum credentialRule) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (operationType != null) {
            map.put("operationType", operationType);
        }
        if (userIndex != null) {
            map.put("userIndex", userIndex);
        }
        if (userName != null) {
            map.put("userName", userName);
        }
        if (userUniqueId != null) {
            map.put("userUniqueId", userUniqueId);
        }
        if (userStatus != null) {
            map.put("userStatus", userStatus);
        }
        if (userType != null) {
            map.put("userType", userType);
        }
        if (credentialRule != null) {
            map.put("credentialRule", credentialRule);
        }
        return new ClusterCommand("setUser", map);
    }

    /**
     * Retrieve user.
     * An InvokeResponse command shall be sent with an appropriate error (e.g. FAILURE, INVALID_COMMAND, etc.) as needed
     * otherwise the GetUserResponse Command shall be sent implying a status of SUCCESS.
     */
    public static ClusterCommand getUser(Integer userIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (userIndex != null) {
            map.put("userIndex", userIndex);
        }
        return new ClusterCommand("getUser", map);
    }

    /**
     * Clears a user or all Users.
     * For each user to clear, all associated credentials (e.g. PIN, RFID, fingerprint, etc.) shall be cleared and the
     * user entry values shall be reset to their default values (e.g. UserStatus shall be Available, UserType shall be
     * UnrestrictedUser) and all associated schedules shall be cleared.
     * A LockUserChange event with the provided UserIndex shall be generated after successfully clearing users.
     */
    public static ClusterCommand clearUser(Integer userIndex) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (userIndex != null) {
            map.put("userIndex", userIndex);
        }
        return new ClusterCommand("clearUser", map);
    }

    /**
     * Set a credential (e.g. PIN, RFID, Fingerprint, etc.) into the lock for a new user, existing user, or
     * ProgrammingUser.
     * Fields used for different use cases:
     */
    public static ClusterCommand setCredential(DataOperationTypeEnum operationType, CredentialStruct credential,
            OctetString credentialData, Integer userIndex, UserStatusEnum userStatus, UserTypeEnum userType) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (operationType != null) {
            map.put("operationType", operationType);
        }
        if (credential != null) {
            map.put("credential", credential);
        }
        if (credentialData != null) {
            map.put("credentialData", credentialData);
        }
        if (userIndex != null) {
            map.put("userIndex", userIndex);
        }
        if (userStatus != null) {
            map.put("userStatus", userStatus);
        }
        if (userType != null) {
            map.put("userType", userType);
        }
        return new ClusterCommand("setCredential", map);
    }

    /**
     * Retrieve the status of a particular credential (e.g. PIN, RFID, Fingerprint, etc.) by index.
     * An InvokeResponse command shall be sent with an appropriate error (e.g. FAILURE, INVALID_COMMAND, etc.) as needed
     * otherwise the GetCredentialStatusResponse command shall be sent implying a status of SUCCESS.
     */
    public static ClusterCommand getCredentialStatus(CredentialStruct credential) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (credential != null) {
            map.put("credential", credential);
        }
        return new ClusterCommand("getCredentialStatus", map);
    }

    /**
     * Clear one, one type, or all credentials except ProgrammingPIN credential.
     * Fields used for different use cases:
     * For each credential cleared whose user doesn’t have another valid credential, the corresponding user record shall
     * be reset back to default values and its UserStatus value shall be set to Available and UserType value shall be
     * set to UnrestrictedUser and all schedules shall be cleared. In this case a LockUserChange event shall be
     * generated for the user being cleared.
     * Return status shall be one of the following values:
     */
    public static ClusterCommand clearCredential(CredentialStruct credential) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (credential != null) {
            map.put("credential", credential);
        }
        return new ClusterCommand("clearCredential", map);
    }

    /**
     * This command causes the lock device to unlock the door without pulling the latch. This command includes an
     * optional code for the lock. The door lock may require a code depending on the value of the
     * RequirePINForRemoteOperation attribute.
     * &gt; [!NOTE]
     * &gt; If the attribute AutoRelockTime is supported, the lock will transition to the locked state when the auto
     * relock time has expired.
     */
    public static ClusterCommand unboltDoor(OctetString pinCode) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (pinCode != null) {
            map.put("pinCode", pinCode);
        }
        return new ClusterCommand("unboltDoor", map);
    }

    /**
     * This command allows communicating an Aliro Reader configuration, as defined in [Aliro], to the lock.
     */
    public static ClusterCommand setAliroReaderConfig(OctetString signingKey, OctetString verificationKey,
            OctetString groupIdentifier, OctetString groupResolvingKey) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (signingKey != null) {
            map.put("signingKey", signingKey);
        }
        if (verificationKey != null) {
            map.put("verificationKey", verificationKey);
        }
        if (groupIdentifier != null) {
            map.put("groupIdentifier", groupIdentifier);
        }
        if (groupResolvingKey != null) {
            map.put("groupResolvingKey", groupResolvingKey);
        }
        return new ClusterCommand("setAliroReaderConfig", map);
    }

    /**
     * This command allows clearing an existing Aliro Reader configuration for the lock. Administrators shall NOT clear
     * an Aliro Reader configuration without explicit user permission.
     * &gt; [!NOTE]
     * &gt; Using this command will revoke the ability of all existing Aliro user devices that have the old verification
     * key to interact with the lock. This effect is not restricted to a single fabric or otherwise scoped in any way.
     */
    public static ClusterCommand clearAliroReaderConfig() {
        return new ClusterCommand("clearAliroReaderConfig");
    }

    @Override
    public @NonNull String toString() {
        String str = "";
        str += "clusterRevision : " + clusterRevision + "\n";
        str += "featureMap : " + featureMap + "\n";
        str += "lockState : " + lockState + "\n";
        str += "lockType : " + lockType + "\n";
        str += "actuatorEnabled : " + actuatorEnabled + "\n";
        str += "doorState : " + doorState + "\n";
        str += "doorOpenEvents : " + doorOpenEvents + "\n";
        str += "doorClosedEvents : " + doorClosedEvents + "\n";
        str += "openPeriod : " + openPeriod + "\n";
        str += "numberOfTotalUsersSupported : " + numberOfTotalUsersSupported + "\n";
        str += "numberOfPinUsersSupported : " + numberOfPinUsersSupported + "\n";
        str += "numberOfRfidUsersSupported : " + numberOfRfidUsersSupported + "\n";
        str += "numberOfWeekDaySchedulesSupportedPerUser : " + numberOfWeekDaySchedulesSupportedPerUser + "\n";
        str += "numberOfYearDaySchedulesSupportedPerUser : " + numberOfYearDaySchedulesSupportedPerUser + "\n";
        str += "numberOfHolidaySchedulesSupported : " + numberOfHolidaySchedulesSupported + "\n";
        str += "maxPinCodeLength : " + maxPinCodeLength + "\n";
        str += "minPinCodeLength : " + minPinCodeLength + "\n";
        str += "maxRfidCodeLength : " + maxRfidCodeLength + "\n";
        str += "minRfidCodeLength : " + minRfidCodeLength + "\n";
        str += "credentialRulesSupport : " + credentialRulesSupport + "\n";
        str += "numberOfCredentialsSupportedPerUser : " + numberOfCredentialsSupportedPerUser + "\n";
        str += "language : " + language + "\n";
        str += "ledSettings : " + ledSettings + "\n";
        str += "autoRelockTime : " + autoRelockTime + "\n";
        str += "soundVolume : " + soundVolume + "\n";
        str += "operatingMode : " + operatingMode + "\n";
        str += "supportedOperatingModes : " + supportedOperatingModes + "\n";
        str += "defaultConfigurationRegister : " + defaultConfigurationRegister + "\n";
        str += "enableLocalProgramming : " + enableLocalProgramming + "\n";
        str += "enableOneTouchLocking : " + enableOneTouchLocking + "\n";
        str += "enableInsideStatusLed : " + enableInsideStatusLed + "\n";
        str += "enablePrivacyModeButton : " + enablePrivacyModeButton + "\n";
        str += "localProgrammingFeatures : " + localProgrammingFeatures + "\n";
        str += "wrongCodeEntryLimit : " + wrongCodeEntryLimit + "\n";
        str += "userCodeTemporaryDisableTime : " + userCodeTemporaryDisableTime + "\n";
        str += "sendPinOverTheAir : " + sendPinOverTheAir + "\n";
        str += "requirePinForRemoteOperation : " + requirePinForRemoteOperation + "\n";
        str += "expiringUserTimeout : " + expiringUserTimeout + "\n";
        str += "alarmMask : " + alarmMask + "\n";
        str += "aliroReaderVerificationKey : " + aliroReaderVerificationKey + "\n";
        str += "aliroReaderGroupIdentifier : " + aliroReaderGroupIdentifier + "\n";
        str += "aliroReaderGroupSubIdentifier : " + aliroReaderGroupSubIdentifier + "\n";
        str += "aliroExpeditedTransactionSupportedProtocolVersions : "
                + aliroExpeditedTransactionSupportedProtocolVersions + "\n";
        str += "aliroGroupResolvingKey : " + aliroGroupResolvingKey + "\n";
        str += "aliroSupportedBleuwbProtocolVersions : " + aliroSupportedBleuwbProtocolVersions + "\n";
        str += "aliroBleAdvertisingVersion : " + aliroBleAdvertisingVersion + "\n";
        str += "numberOfAliroCredentialIssuerKeysSupported : " + numberOfAliroCredentialIssuerKeysSupported + "\n";
        str += "numberOfAliroEndpointKeysSupported : " + numberOfAliroEndpointKeysSupported + "\n";
        return str;
    }
}
