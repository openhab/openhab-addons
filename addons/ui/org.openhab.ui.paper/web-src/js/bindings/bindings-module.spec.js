describe('module PaperUI.bindings', function() {
    beforeEach(function() {
        module('PaperUI.bindings');
    });
    describe('the BindingList directive', function() {
        var bindingsList;
        beforeEach(inject(function($compile, $rootScope) {
            bindingsList = $compile('<bindings-list />')($rootScope)
        }));

        it('should compile', function() {
            expect(bindingsList).toBeDefined();
        })
    });

    describe('the ConfigureBindingDialogController', function() {
        var configureBindingDialogController, scope, bindingService;
        var restConfig;
        beforeEach(inject(function($injector, $rootScope, $controller) {
            $rootScope.data = {};
            scope = $rootScope.$new();

            var bindingRepository = $injector.get('bindingRepository');
            restConfig = $injector.get('restConfig');
            $rootScope.data.bindings = [ {
                id : 'B'
            } ];
            configureBindingDialogController = $controller('ConfigureBindingDialogController', {
                '$scope' : scope,
                'binding' : {
                    'id' : 'B',
                    'configDescriptionURI' : 'CDURI'
                },
                'bindingRepository' : bindingRepository
            });
            $httpBackend = $injector.get('$httpBackend');
            $httpBackend.when('GET', restConfig.restPath + "/config-descriptions/CDURI").respond({
                parameters : [ {
                    type : 'input',
                    name : 'PNAME'
                } ]
            });
            $httpBackend.when('GET', restConfig.restPath + "/bindings/B/config").respond({
                PNAME : '1'
            });
            $httpBackend.flush();
            bindingService = $injector.get('bindingService');
        }));
        it('should require ConfigureBindingDialogController', function() {
            expect(configureBindingDialogController).toBeDefined();
        });
        it('should get binding parameters', function() {
            expect(scope.parameters.length).toEqual(1);
        });
        it('should get binding configuration', function() {
            expect(scope.configuration.PNAME).toEqual(1);
        });
        it('should add binding parameter', function() {
            scope.configArray = [];
            scope.addParameter();
            expect(scope.configArray.length).toEqual(1);
        });
        it('should save binding configuration', function() {
            spyOn(bindingService, 'updateConfig');
            scope.save();
            expect(bindingService.updateConfig).toHaveBeenCalled();
        });
    });
});