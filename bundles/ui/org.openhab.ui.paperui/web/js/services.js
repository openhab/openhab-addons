angular.module('SmartHomeManagerApp.services', []).config(function($httpProvider){
    var language = localStorage.getItem('language');
    if(language) {
        $httpProvider.defaults.headers.common['Accept-Language'] = language;
    }
    var interceptor = ['$rootScope', '$q', function (scope, $q) {

        function success(response) {
            return response;
        }

        function error(response) {
            scope.showErrorToast('An error occured: ' + response.status + ' (' + response.statusText + ')');
            return $q.reject(response);
        }

        return function (promise) {
            return promise.then(success, error);
        }

    }];
    $httpProvider.responseInterceptors.push(interceptor);
}).factory('itemService', function($resource) {
    return $resource('/rest/items', {}, {
        getAll : {
            method : 'GET',
            isArray: true,
            url: '/rest/items?recursive=true'
        },
        getByName : {
            method : 'GET',
            params : {
                bindingId : '@itemName'
            },
            url : '/rest/items/:itemName'
        },
        remove : {
            method : 'DELETE',
            params : {
                itemName : '@itemName'
            },
            url : '/rest/items/:itemName'
        },
        create : {
            method : 'PUT',
            params : {
                itemName : '@itemName'
            },
            url : '/rest/items/:itemName',
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        updateState : {
            method : 'PUT',
            params : {
                itemName : '@itemName'
            },
            url : '/rest/items/:itemName/state',
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        sendCommand : {
            method : 'POST',
            params : {
                itemName : '@itemName'
            },
            url : '/rest/items/:itemName',
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
            url : '/rest/items/:itemName/members/:memberItemName'
        },
        removeMember : {
            method : 'DELETE',
            params : {
                itemName : '@itemName',
                memberItemName : '@memberItemName'
            },
            url : '/rest/items/:itemName/members/:memberItemName'
        },
        addTag : {
            method : 'PUT',
            params : {
                itemName : '@itemName',
                tag : '@tag'
            },
            url : '/rest/items/:itemName/tags/:tag'
        },
        removeTag : {
            method : 'DELETE',
            params : {
                itemName : '@itemName',
                tag : '@tag'
            },
            url : '/rest/items/:itemName/tags/:tag'
        }
    });
}).factory('bindingService', function($resource) {
    return $resource('/rest/bindings', {}, {
        getAll : {
            method : 'GET',
            cache : true,
            isArray : true
        },
    });
}).factory('inboxService', function($resource) {
    return $resource('/rest/inbox', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        approve : {
            method : 'POST',
            params : {
                thingUID : '@thingUID'
            },
            url : '/rest/inbox/approve/:thingUID'
        },
        ignore : {
            method : 'POST',
            params : {
                thingUID : '@thingUID'
            },
            url : '/rest/inbox/ignore/:thingUID'
        },
        remove : {
            method : 'DELETE',
            params : {
                thingUID : '@thingUID'
            },
            url : '/rest/inbox/:thingUID'
        }
    })
}).factory('discoveryService', function($resource) {
    return $resource('/rest/discovery', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        scan : {
            method : 'POST',
            params : {
                bindingId : '@bindingId'
            },
            url : '/rest/discovery/scan/:bindingId'
        }
    });
}).factory('thingTypeService', function($resource) {
    return $resource('/rest/thing-types', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getByUid : {
            method : 'GET',
            params : {
                bindingId : '@thingTypeUID'
            },
            url : '/rest/thing-types/:thingTypeUID'
        }
    });
}).factory('linkService', function($resource) {
    return $resource('/rest/links', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        link : {
            method : 'PUT',
            params : {
                itemName : '@itemName',
                channelUID : '@channelUID'
            },
            url : '/rest/links/:itemName/:channelUID'
        },
        unlink : {
            method : 'DELETE',
            params : {
                itemName : '@itemName',
                channelUID : '@channelUID'
            },
            url : '/rest/links/:itemName/:channelUID'
        }
    });
}).factory('thingService', function($resource) {
    return $resource('/rest/things', {}, {
        getAll : {
            method : 'GET',
            isArray : true
        },
        getByUid : {
            method : 'GET',
            params : {
                bindingId : '@thingUID'
            },
            url : '/rest/things/:thingUID'
        },
        remove : {
            method : 'DELETE',
            params : {
                thingUID : '@thingUID'
            },
            url : '/rest/things/:thingUID'
        },
        add : {
            method : 'POST',
            url : '/rest/things',
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        update : {
            method : 'PUT',
            params : {
                thingUID : '@thingUID'
            },
            url : '/rest/things/:thingUID',
            headers : {
                'Content-Type' : 'application/json'
            }
        },
        link : {
            method : 'POST',
            params : {
                thingUID : '@thingUID',
                channelId : '@channelId'    
            },
            url : '/rest/things/:thingUID/channels/:channelId/link',
            headers : {
                'Content-Type' : 'text/plain'
            }
        },
        unlink : {
            method : 'DELETE',
            params : {
                thingUID : '@thingUID',
                channelId : '@channelId'    
            },
            url : '/rest/things/:thingUID/channels/:channelId/link',
        }
    });
});