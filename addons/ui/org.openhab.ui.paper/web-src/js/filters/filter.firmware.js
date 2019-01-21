var firmwareFilterModule = angular.module('PaperUI.filters.firmware', []);

firmwareFilterModule.constant('FIRMWARE_STATUS', {
    UNKNOWN : 'UNKNOWN',
    UP_TO_DATE : 'UP_TO_DATE',
    UPDATE_AVAILABLE : 'UPDATE_AVAILABLE',
    UPDATE_EXECUTABLE : 'UPDATE_EXECUTABLE'
});

firmwareFilterModule.constant('UPDATE_STEP', {
    DOWNLOADING : 'DOWNLOADING',
    WAITING : 'WAITING',
    TRANSFERRING : 'TRANSFERRING',
    UPDATING : 'UPDATING',
    REBOOTING : 'REBOOTING'
});

firmwareFilterModule.filter('firmwareStatusFormat', [ 'FIRMWARE_STATUS', function(FIRMWARE_STATUS) {
    return function(firmwareStatus) {
        switch (firmwareStatus) {
            case FIRMWARE_STATUS.UNKNOWN:
                return 'Unknown';
            case FIRMWARE_STATUS.UP_TO_DATE:
                return 'Up to date';
            case FIRMWARE_STATUS.UPDATE_AVAILABLE:
                return 'Update available';
            case FIRMWARE_STATUS.UPDATE_EXECUTABLE:
                return 'Ready to install';
        }
    };
} ]).filter('firmwareStatusClass', [ 'FIRMWARE_STATUS', function(FIRMWARE_STATUS) {
    return function(firmwareStatus) {
        switch (firmwareStatus) {
            case FIRMWARE_STATUS.UNKNOWN:
                return 'grey';
            case FIRMWARE_STATUS.UP_TO_DATE:
                return 'online';
            case FIRMWARE_STATUS.UPDATE_AVAILABLE:
                return 'orange';
            case FIRMWARE_STATUS.UPDATE_EXECUTABLE:
                return 'blue';
        }
    };
} ]).filter('firmwareUpdateStep', [ 'UPDATE_STEP', function(UPDATE_STEP) {
    return function(updateStep) {
        switch (updateStep) {
            case UPDATE_STEP.DOWNLOADING:
                return 'Downloading';
            case UPDATE_STEP.WAITING:
                return 'Waiting';
            case UPDATE_STEP.TRANSFERRING:
                return 'Transfering';
            case UPDATE_STEP.UPDATING:
                return 'Updating';
            case UPDATE_STEP.REBOOTING:
                return 'Rebooting';
        }
    }
} ]);
