describe('module PaperUI.controllers.configuration', function() {
    beforeEach(function() {
        module('PaperUI');
    });
    describe('tests for ServicesController', function() {
        var ServicesController, scope, injector;
        beforeEach(inject(function($injector, $rootScope, $controller, $mdDialog) {
            scope = $rootScope.$new();
            mdDialog = $mdDialog;
            injector = $injector;
            $controller('BodyController', {
                '$scope' : scope
            });
            ServicesController = $controller('ServicesController', {
                '$scope' : scope
            });
        }));
        it('should require ServicesController', function() {
            expect(ServicesController).toBeDefined();
        });
        it('should refresh services', function() {
            var serviceConfigService = injector.get('serviceConfigService');
            spyOn(serviceConfigService, 'getAll');
            scope.refresh();
            expect(serviceConfigService.getAll).toHaveBeenCalled();
        });
        it('should open configure dialog', function() {
            spyOn(mdDialog, 'show');
            scope.configure(0);
            expect(mdDialog.show).toHaveBeenCalled();
        });
    });
});