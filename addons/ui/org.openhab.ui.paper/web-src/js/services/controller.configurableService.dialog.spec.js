describe('module PaperUI.controllers.configurableServiceDialog', function() {
    beforeEach(function() {
        module('PaperUI.controllers.configurableServiceDialog');
    });

    describe('tests for ConfigurableServiceDialogController', function() {
        var ConfigurableServiceDialogController
        var scope;
        var restConfig;

        beforeEach(inject(function($injector, $rootScope, $controller) {
            scope = $rootScope.$new();
            $rootScope.data = {};

            ConfigurableServiceDialogController = $controller('ConfigurableServiceDialogController', {
                $scope : scope,
                serviceId : 'B',
                configDescriptionURI : 'CDURI',
                multiple : false
            });

            restConfig = $injector.get('restConfig');
            $httpBackend = $injector.get('$httpBackend');
            $httpBackend.when('GET', restConfig.restPath + "/config-descriptions/CDURI").respond({
                parameters : [ {
                    type : 'input',
                    name : 'PNAME'
                } ]
            });
            $httpBackend.when('GET', restConfig.restPath + "/services/B").respond({
                label : '1'
            });
            $httpBackend.when('GET', restConfig.restPath + "/services/B/config").respond({
                SNAME : '2'
            });
            $httpBackend.flush();
            serviceConfigService = $injector.get('serviceConfigService');
        }));
        it('should require ConfigurableServiceDialogController', function() {
            expect(ConfigurableServiceDialogController).toBeDefined();
        });
        it('should get service', function() {
            expect(scope.service.label).toEqual('1');
        });
        it('should get service configuration', function() {
            expect(scope.configuration.SNAME).toEqual(2);
        });

        it('should get service parameters', function() {
            expect(scope.parameters.length).toEqual(1);
        });

        it('should add service parameter', function() {
            scope.configArray = [];
            scope.addParameter();
            expect(scope.configArray.length).toEqual(1);
        });
        it('should save service configuration', function() {
            spyOn(serviceConfigService, 'updateConfig');
            scope.save();
            expect(serviceConfigService.updateConfig).toHaveBeenCalled();
        });

    });

});