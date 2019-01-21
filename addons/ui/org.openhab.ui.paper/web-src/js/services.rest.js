angular.module('PaperUI.services.rest', [ 'PaperUI.constants', 'ngResource' ]).config(function($httpProvider) {
    var accessToken = function getAccessToken() {
        return $('#authentication').data('access-token')
    }();
    if (accessToken != '{{ACCESS_TOKEN}}') {
        var authorizationHeader = function getAuthorizationHeader() {
            return 'Bearer ' + accessToken
        }();
        $httpProvider.defaults.headers.common['Authorization'] = authorizationHeader;
    }
}).factory('itemService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/items', {}, {
        getAll : {
            method : 'GET',
            isArray : true,
            url : restConfig.restPath + '/items?recursive=false'
        },
        getByName : {
            method : 'GET',
            params : {
                itemName : '@itemName'
            },
            url : restConfig.restPath + '/items/:itemName'
        },
        remove : {
            method : 'DELETE',
            params : {
                itemName : '@itemName'
            },
            url : restConfig.restPath + '/items/:itemName'
        },
        create : {
            method : 'PUT',
            params : {
                itemName : '@itemName'
            },
            url : restConfig.restPath + '/items/:itemName',
            transformResponse : function(response, headerGetter, status) {
                var response = angular.fromJson(response);
                if (status == 405) {
                    response.customMessage = "Item is not editable.";
                }
                return response;
            }
        },
        updateState : {
            method : 'PUT',
            params : {
                itemName : '@itemName'
            },
            url : restConfig.restPath + '/items/:itemName/state',
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        getItemState : {
            method : 'GET',
            params : {
                itemName : '@itemName'
            },
            url : restConfig.restPath + '/items/:itemName/state',
            transformResponse : function(data) {
                return data;
            },
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        sendCommand : {
            method : 'POST',
            params : {
                itemName : '@itemName'
            },
            url : restConfig.restPath + '/items/:itemName',
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        addMember : {
            method : 'PUT',
            params : {
                itemName : '@itemName',
                memberItemName : '@memberItemName'
            },
            url : restConfig.restPath + '/items/:itemName/members/:memberItemName'
        },
        removeMember : {
            method : 'DELETE',
            params : {
                itemName : '@itemName',
                memberItemName : '@memberItemName'
            },
            url : restConfig.restPath + '/items/:itemName/members/:memberItemName'
        },
        addTag : {
            method : 'PUT',
            params : {
                itemName : '@itemName',
                tag : '@tag'
            },
            url : restConfig.restPath + '/items/:itemName/tags/:tag'
        },
        removeTag : {
            method : 'DELETE',
            params : {
                itemName : '@itemName',
                tag : '@tag'
            },
            url : restConfig.restPath + '/items/:itemName/tags/:tag'
        },
        updateMetadata : {
            method : 'PUT',
            params : {
                itemName : '@itemName',
                namespace : '@namespace'
            },
            url : restConfig.restPath + '/items/:itemName/metadata/:namespace'
        }
    });
}).factory('bindingService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/bindings', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getConfigById : {
            method : 'GET',
            params : {
                id : '@id'
            },
            interceptor : {
                response : function(response) {
                    return response.data;
                }
            },
            url : restConfig.restPath + '/bindings/:id/config'
        },
        updateConfig : {
            method : 'PUT',
            headers : {
                'Content-Type' : 'application/json'
            },
            params : {
                id : '@id'
            },
            url : restConfig.restPath + '/bindings/:id/config'
        },
    });
}).factory('inboxService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/inbox', {}, {
        getAll : {
            method : 'GET',
            isArray : true,
            transformResponse : function(data) {
                var results = angular.fromJson(data);
                for (var i = 0; i < results.length; i++) {
                    results[i].bindingType = results[i].thingTypeUID.split(':')[0];
                }
                return results
            },
        },
        approve : {
            method : 'POST',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/inbox/:thingUID/approve',
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        ignore : {
            method : 'POST',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/inbox/:thingUID/ignore'
        },
        unignore : {
            method : 'POST',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/inbox/:thingUID/unignore'
        },
        remove : {
            method : 'DELETE',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/inbox/:thingUID'
        }
    })
}).factory('discoveryService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/discovery', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        scan : {
            method : 'POST',
            params : {
                bindingId : '@bindingId'
            },
            transformResponse : function(data) {
                return {
                    timeout : angular.fromJson(data)
                }
            },
            url : restConfig.restPath + '/discovery/bindings/:bindingId/scan'
        }
    });
}).factory('thingTypeService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/thing-types', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getByUid : {
            method : 'GET',
            params : {
                thingTypeUID : '@thingTypeUID'
            },
            url : restConfig.restPath + '/thing-types/:thingTypeUID'
        }
    });
}).factory('profileTypeService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/profile-types', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getByChannel : {
            method : 'GET',
            params : {
                channelTypeUID : '@channelTypeUID',
                itemType : '@itemType'
            },
            url : restConfig.restPath + '/profile-types',
            isArray : true
        }
    });
}).factory('linkService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/links', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getLink : {
            method : 'GET',
            params : {
                itemName : '@itemName',
                channelUID : '@channelUID'
            },
            url : restConfig.restPath + '/links/:itemName/:channelUID'
        },
        link : {
            method : 'PUT',
            params : {
                itemName : '@itemName',
                channelUID : '@channelUID'
            },
            url : restConfig.restPath + '/links/:itemName/:channelUID',
            transformResponse : function(response, headerGetter, status) {
                var response = {};
                if (status == 405) {
                    response.customMessage = "Link is not editable.";
                }
                return response;
            }
        },
        unlink : {
            method : 'DELETE',
            params : {
                itemName : '@itemName',
                channelUID : '@channelUID'
            },
            url : restConfig.restPath + '/links/:itemName/:channelUID'
        }
    });
}).factory('thingService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/things', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getByUid : {
            method : 'GET',
            params : {
                bindingId : '@thingUID'
            },
            url : restConfig.restPath + '/things/:thingUID'
        },
        remove : {
            method : 'DELETE',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/things/:thingUID'
        },
        add : {
            method : 'POST',
            url : restConfig.restPath + '/things',
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        update : {
            method : 'PUT',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/things/:thingUID',
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        updateConfig : {
            method : 'PUT',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/things/:thingUID/config',
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        getFirmwareStatus : {
            method : 'GET',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/things/:thingUID/firmware/status',
        },
        getFirmwares : {
            method : 'GET',
            isArray : true,
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/things/:thingUID/firmwares'
        },
        installFirmware : {
            method : 'PUT',
            params : {
                thingUID : '@thingUID',
                firmwareVersion : '@firmwareVersion'
            },
            url : restConfig.restPath + '/things/:thingUID/firmware/:firmwareVersion'
        },
        enable : {
            method : 'PUT',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/things/:thingUID/enable'
        }
    });
}).factory('serviceConfigService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/services', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getContexts : {
            method : 'GET',
            params : {
                id : '@id'
            },
            isArray : true,
            url : restConfig.restPath + '/services/:id/contexts'
        },
        getById : {
            method : 'GET',
            params : {
                id : '@id'
            },
            url : restConfig.restPath + '/services/:id'
        },
        getConfigById : {
            method : 'GET',
            params : {
                id : '@id'
            },
            interceptor : {
                response : function(response) {
                    return response.data;
                }
            },
            url : restConfig.restPath + '/services/:id/config'
        },
        updateConfig : {
            method : 'PUT',
            headers : {
                'Content-Type' : 'application/json'
            },
            params : {
                id : '@id'
            },
            url : restConfig.restPath + '/services/:id/config'
        },
        deleteConfig : {
            method : 'DELETE',
            params : {
                id : '@id'
            },
            url : restConfig.restPath + '/services/:id/config'
        },
    });
}).factory('configDescriptionService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/config-descriptions', {}, {
        getAll : {
            method : 'GET',
            isArray : true,
            params : {
                scheme : '@scheme'
            }
        },
        getByUri : {
            method : 'GET',
            params : {
                uri : '@uri'
            },
            transformResponse : function(response, headerGetter, status) {
                var response = angular.fromJson(response);
                if (status == 404) {
                    response.showError = false;
                }
                return response;
            },
            url : restConfig.restPath + '/config-descriptions/:uri'
        },
    });
}).factory('extensionService', function($resource, restConfig, $http) {
    var extensionService = $resource(restConfig.restPath + '/extensions', {}, {
        getAll : {
            method : 'GET',
            isArray : true,
            transformResponse : function(response, headerGetter, status) {
                if (status == 503) {
                    return {
                        showError : false
                    };
                }
                return angular.fromJson(response);
            }
        },
        getByUri : {
            method : 'GET',
            params : {
                uri : '@id'
            },
            url : restConfig.restPath + '/extensions/:id'
        },
        getAllTypes : {
            method : 'GET',
            isArray : true,
            url : restConfig.restPath + '/extensions/types'
        },
        install : {
            method : 'POST',
            params : {
                id : '@id'
            },
            url : restConfig.restPath + '/extensions/:id/install'
        },
        installFromURL : {
            method : 'POST',
            params : {
                url : '@url'
            },
            url : restConfig.restPath + '/extensions/url/:url/install'
        },
        uninstall : {
            method : 'POST',
            params : {
                id : '@id'
            },
            url : restConfig.restPath + '/extensions/:id/uninstall'
        }
    });

    var suppressUnavailableError = function(response, headersGetter, status) {
        return status != 503 ? response : {
            showError : false
        };
    }

    extensionService.isAvailable = function(callback) {
        $http.head(restConfig.restPath + '/extensions', {
            transformResponse : suppressUnavailableError
        }).then(function() {
            callback(true);
        }, function() {
            callback(false);
        });
    }

    return extensionService;
}).factory('ruleService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/rules', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getByUid : {
            method : 'GET',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID'
        },
        add : {
            method : 'POST',
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        remove : {
            method : 'DELETE',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID'
        },
        getModuleConfigParameter : {
            method : 'GET',
            params : {
                ruleUID : '@ruleUID'
            },
            transformResponse : function(data, headersGetter, status) {
                return {
                    content : data
                };
            },
            url : restConfig.restPath + '/rules/:ruleUID/actions/action/config/script'
        },
        setModuleConfigParameter : {
            method : 'PUT',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID/actions/action/config/script',
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        update : {
            method : 'PUT',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID',
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        setEnabled : {
            method : 'POST',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID/enable',
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        runRule : {
            method : 'POST',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID/runnow',
            headers : {
                'Content-Type' : 'text/plain'
            }
        }
    });
}).factory('moduleTypeService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/module-types', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getByType : {
            method : 'GET',
            params : {
                mtype : '@mtype'
            },
            url : restConfig.restPath + '/module-types?type=:mtype',
            isArray : true
        },
        getByUid : {
            method : 'GET',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID'
        },
        getModuleConfigByUid : {
            method : 'GET',
            params : {
                ruleUID : '@ruleUID',
                moduleCategory : '@moduleCategory',
                id : '@id'

            },
            url : restConfig.restPath + '/rules/:ruleUID/:moduleCategory/:id/config'
        },
        add : {
            method : 'POST',
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        remove : {
            method : 'DELETE',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID'
        },
        getModuleConfigParameter : {
            method : 'GET',
            params : {
                ruleUID : '@ruleUID'
            },
            transformResponse : function(data, headersGetter, status) {
                return {
                    content : data
                };
            },
            url : restConfig.restPath + '/rules/:ruleUID/actions/action/config/script'
        },
        setModuleConfigParameter : {
            method : 'PUT',
            params : {
                ruleUID : '@ruleUID'
            },
            url : restConfig.restPath + '/rules/:ruleUID/actions/action/config/script',
            headers : {
                'Content-Type' : 'text/plain'
            }
        }
    });
}).factory('channelTypeService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/channel-types', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getByUri : {
            method : 'GET',
            params : {
                channelTypeUID : '@channelTypeUID'
            },
            url : restConfig.restPath + '/channel-types/:channelTypeUID'
        },
        getLinkableItemTypes : {
            method : 'GET',
            isArray : true,
            params : {
                channelTypeUID : '@channelTypeUID'
            },
            url : restConfig.restPath + '/channel-types/:channelTypeUID/linkableItemTypes'
        },
    });
}).factory('templateService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/channel-types', {}, {
        getAll : {
            method : 'GET',
            url : restConfig.restPath + '/templates',
            isArray : true
        },
        getByUid : {
            method : 'GET',
            params : {
                templateUID : '@templateUID'
            },
            url : restConfig.restPath + '/templates/:templateUID'
        },
    });
}).factory('imageService', function(restConfig, $http) {
    return {
        getItemState : function(itemName) {
            var promise = $http.get(restConfig.restPath + "/items/" + itemName + "/state").then(function(response) {
                return response.data;
            });
            return promise;
        }
    }
}).factory('firmwareService', function($resource, restConfig) {
    return $resource(restConfig.restPath + '/firmware', {}, {
        getStatus : {
            method : 'GET',
            isArray : true
        },
        getByUid : {
            method : 'GET',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/:thingUID'
        },
        update : {
            method : 'GET',
            params : {
                thingUID : '@thingUID'
            },
            url : restConfig.restPath + '/update/:thingUID'
        }
    });
});
;