describe('module PaperUI.services.repositories', function() {
    beforeEach(function() {
        module('PaperUI.services.repositories');
    });

    describe('Repository base class', function() {
        var rootScope;
        var repository;
        var entityService
        beforeEach(inject(function($q, $rootScope, $timeout, Repository) {
            rootScope = $rootScope;
            $rootScope.data = {};

            entityService = {
                getAll : function() {
                }
            };
            spyOn(entityService, 'getAll').and.callFake(function(callback) {
                callback([ 'model1', 'model2', 'model3' ]);
            });

            $rootScope.data.entities = [];
            repository = new Repository.create($q, $rootScope, entityService, 'entities', true);
        }));

        it('should refresh all entites', function(done) {
            repository.getAll().then(function(entities) {
                expect(entityService.getAll).toHaveBeenCalled();
                expect(entities.length).toEqual(3);
                done();
            });
            rootScope.$apply();
        });

        it('should get one entity', function(done) {
            var condition = function(element) {
                return element === 'model3';
            }

            repository.getOne(condition).then(function(entity) {
                expect(entityService.getAll).toHaveBeenCalled();
                expect(entity).toEqual('model3');
                done();
            });
            rootScope.$apply();
        });
    });
});