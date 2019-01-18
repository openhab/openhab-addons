;
(function() {
    'use strict';

    angular.module('PaperUI.services.repositories', [ 'PaperUI.services.rest' ]).factory('Repository', Repository);

    function Repository() {
        return {
            create : RepositoryImpl
        }
    }

    function RepositoryImpl($q, $rootScope, remoteService, dataType, staticData, getOneFunction, idParameterName, elmentId) {
        var self = this;

        this.dirty = false;
        this.initialFetch = false;
        this.staticData = staticData
        this.singleElements = getOneFunction ? {} : null;

        return {
            add : add,
            remove : remove,
            update : update,
            getAll : getAll,
            getOne : getOne,
            find : find,
            filter : filter,
            findByIndex : findByIndex,
            setDirty : setDirty
        };

        function setDirty() {
            self.dirty = true;
        }

        function getAll(callback, refresh) {
            if (typeof callback === 'boolean') {
                refresh = callback;
                callback = null;
            }
            var deferred = $q.defer();
            deferred.promise.then(function(res) {
                if (callback) {
                    return callback(res);
                } else {
                    return;
                }
            }, function(res) {
                return;
            }, function(res) {
                if (callback) {
                    return callback(res);
                } else {
                    return;
                }
            });
            if (self.staticData && self.initialFetch && !refresh && !self.dirty) {
                deferred.resolve($rootScope.data[dataType]);
            } else {
                remoteService.getAll(function(data) {
                    if (((data.length != $rootScope.data[dataType].length) || self.dirty || refresh || !self.initialFetch)) {
                        self.initialFetch = true;
                        $rootScope.data[dataType] = data;
                        self.dirty = false;
                    }

                    deferred.resolve(data);
                });
            }
            return deferred.promise;
        }

        function getOne(condition, callback, refresh) {
            var deferred = $q.defer();

            var element = find(condition);
            if (element != null && !self.dirty && !refresh) {
                resolveSingleElement(element).then(function(singleElement) {
                    if (callback) {
                        callback(singleElement);
                    }
                    deferred.resolve(singleElement);
                });
            } else {
                getAll(null, true).then(function(res) {
                    resolveSingleElement(find(condition)).then(function(singleElement) {
                        if (callback) {
                            callback(singleElement);
                        }
                        deferred.resolve(singleElement);
                    });
                }, function(res) {
                    if (callback) {
                        callback(null);
                    }
                    deferred.resolve(null);
                });
            }

            return deferred.promise;
        }

        function resolveSingleElement(element) {
            var deferred = $q.defer();

            if (!element) {
                return deferred.resolve(undefined);
            } else if (getOneFunction && self.singleElements[element.UID]) {
                deferred.resolve(self.singleElements[element.UID]);
            } else if (getOneFunction) {
                var parameter = {};
                parameter[idParameterName] = element[elmentId];
                getOneFunction(parameter, function(singleElement) {
                    self.singleElements[element.UID] = singleElement;
                    deferred.resolve(singleElement);
                })
            } else {
                deferred.resolve(element);
            }

            return deferred.promise;
        }

        function find(condition) {
            for (var i = 0; i < $rootScope.data[dataType].length; i++) {
                var element = $rootScope.data[dataType][i];
                if (condition(element)) {
                    return element;
                }
            }
            return null;
        }

        function findByIndex(condition) {
            for (var i = 0; i < $rootScope.data[dataType].length; i++) {
                var element = $rootScope.data[dataType][i];
                if (condition(element)) {
                    return i;
                }
            }
            return -1;
        }

        function add(element) {
            $rootScope.data[dataType].push(element);
        }

        function remove(element, index) {
            if (typeof (index) === 'undefined' && $rootScope.data[dataType].indexOf(element) !== -1) {
                $rootScope.data[dataType].splice($rootScope.data[dataType].indexOf(element), 1);
            } else if (typeof (index) !== 'undefined' && index !== -1) {
                $rootScope.data[dataType].splice(index, 1);
            }
        }

        function update(element) {
            var index = $rootScope.data[dataType].indexOf(element);
            $rootScope.data[dataType][index] = element;
        }

        function filter(condition, callback) {
            var result = [];
            $rootScope.data[dataType].forEach(function(element) {
                if (condition(element)) {
                    result.push(element);
                }
            });

            callback(result);
        }
    }

})();