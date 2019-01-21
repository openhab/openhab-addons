describe('module PaperUI.filters.firmware', function() {

    beforeEach(function() {
        module('PaperUI.filters.firmware');
    });

    describe('filter firmwareStatusFormat', function() {
        var firmwareStatusFormat;
        var firmwareStatus;

        beforeEach(inject(function($filter, FIRMWARE_STATUS) {
            firmwareStatusFormat = $filter('firmwareStatusFormat');
            firmwareStatus = FIRMWARE_STATUS;
        }));

        it('should map FirmwareStatus to readable text', function() {
            expect(firmwareStatusFormat(firmwareStatus.UNKNOWN)).toBe('Unknown');
            expect(firmwareStatusFormat(firmwareStatus.UP_TO_DATE)).toBe('Up to date');
            expect(firmwareStatusFormat(firmwareStatus.UPDATE_AVAILABLE)).toBe('Update available');
            expect(firmwareStatusFormat(firmwareStatus.UPDATE_EXECUTABLE)).toBe('Ready to install');
        });
    });

    describe('filter firmwareStatusClass', function() {
        var firmwareStatusClass;
        var firmwareStatus;

        beforeEach(inject(function($filter, FIRMWARE_STATUS) {
            firmwareStatusClass = $filter('firmwareStatusClass');
            firmwareStatus = FIRMWARE_STATUS;
        }));

        it('should map FirmwareStatus to CSS class', function() {
            expect(firmwareStatusClass(firmwareStatus.UNKNOWN)).toBe('grey');
            expect(firmwareStatusClass(firmwareStatus.UP_TO_DATE)).toBe('online');
            expect(firmwareStatusClass(firmwareStatus.UPDATE_AVAILABLE)).toBe('orange');
            expect(firmwareStatusClass(firmwareStatus.UPDATE_EXECUTABLE)).toBe('blue');
        });
    });

    describe('filter firmwareUpdateStep', function() {
        var firmwareUpdateStep;
        var updateStep;

        beforeEach(inject(function($filter, UPDATE_STEP) {
            firmwareUpdateStep = $filter('firmwareUpdateStep');
            updateStep = UPDATE_STEP;
        }));

        it('should map firmware update step to readable text', function() {
            expect(firmwareUpdateStep(updateStep.DOWNLOADING)).toBe('Downloading');
            expect(firmwareUpdateStep(updateStep.WAITING)).toBe('Waiting');
            expect(firmwareUpdateStep(updateStep.TRANSFERRING)).toBe('Transfering');
            expect(firmwareUpdateStep(updateStep.UPDATING)).toBe('Updating');
            expect(firmwareUpdateStep(updateStep.REBOOTING)).toBe('Rebooting');
        });
    });
});