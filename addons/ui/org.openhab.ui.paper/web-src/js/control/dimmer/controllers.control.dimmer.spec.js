describe('component dimmer control', function() {
    beforeEach(function() {
        module('PaperUI.control');
    })

    var ctrl;
    var item;

    beforeEach(inject(function($rootScope, $componentController) {
        var scope = $rootScope.$new();

        item = {
            type : 'Switch',
            label : 'An/Aus',
            state : 50
        }

        var bindings = {
            item : item
        };

        ctrl = $componentController('dimmerControl', scope, bindings);
        ctrl.$onInit();
    }));

    it('should bind "item"', function() {
        expect(ctrl.item).toBeDefined();
    });

    it('should define "setSwitch"', function() {
        expect(ctrl.setSwitch).toBeDefined();
    });

    it('should initialize $ctrl.state.switchState', function() {
        expect(ctrl.state.switchState).toBe(true);
    });

    describe('sending commands to backend', function() {

        var controlItemService;
        var $timeout;

        beforeEach(inject(function(_$timeout_, $httpBackend, _controlItemService_) {
            $timeout = _$timeout_;
            controlItemService = _controlItemService_;

            spyOn(controlItemService, 'sendCommand');
            $httpBackend.expectPOST('/rest/items').respond(201, '');
        }))

        it('should trigger sendCommand(\'ON\') when switch toggels true', function() {
            ctrl.state.switchState = true;
            ctrl.setSwitch();

            expect(controlItemService.sendCommand).toHaveBeenCalledWith(item, 'ON');
        })

        it('should trigger sendCommand(\'OFF\') when switch toggels false', function() {
            ctrl.state.switchState = false;
            ctrl.setSwitch();

            expect(controlItemService.sendCommand).toHaveBeenCalledWith(item, 'OFF');
        })

        it('should trigger sendCommand(80) when brightness sets to 80', function() {
            ctrl.item.state = 80;
            ctrl.setBrightness();
            $timeout.flush();

            expect(controlItemService.sendCommand).toHaveBeenCalledWith(ctrl.item, 80);
        })

        it('should set switchState true for brightness > 0', function() {
            ctrl.item.state = 1;
            ctrl.setBrightness();
            $timeout.flush();

            expect(ctrl.state.switchState).toBe(true);
        })

        it('should set switchState false for brightness == 0', function() {
            ctrl.item.state = 0;
            ctrl.setBrightness();
            $timeout.flush();

            expect(ctrl.state.switchState).toBe(false);
        })

    })
})
