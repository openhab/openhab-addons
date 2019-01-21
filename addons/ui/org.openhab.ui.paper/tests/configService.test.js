describe('factory configService', function() {
    var configService;
    beforeEach(function() {
        module('PaperUI');
    });
    beforeEach(inject(function(_configService_) {
        configService = _configService_;
    }));
    it('should require configService', function() {
        expect(configService).toBeDefined();
    });
    describe('tests for Rendering model', function() {
        var $httpBackend;
        var restConfig;
        var itemRepository;
        var thingRepository;
        var $q;
        beforeEach(inject(function($injector, $rootScope, thingService) {
            $httpBackend = $injector.get('$httpBackend');
            restConfig = $injector.get('restConfig');
            itemRepository = $injector.get('itemRepository');
            thingRepository = $injector.get('thingRepository');
            $q = $injector.get('$q');
        }));
        it('should accept empty config parameters', function() {
            var params = configService.getRenderingModel();
            expect(params).toEqual([]);
        });
        it('should return the default group when no groups', function() {
            var inputParams = [ {
                type : 'none'
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params.length).toEqual(1);
            expect(params[0].groupName).toEqual("_default");
        });
        it('should return the default group when not found', function() {
            var inputParams = [ {
                type : 'text',
                groupName : 'custom'
            } ];
            var groups = [ {
                name : 'some'
            } ];
            var params = configService.getRenderingModel(inputParams, groups);
            expect(params[0].groupName).toEqual("_default");
        });
        it('should return the passed group', function() {
            var inputParams = [ {
                type : 'text',
                groupName : 'custom'
            } ];
            var groups = [ {
                name : 'custom'
            } ];
            var params = configService.getRenderingModel(inputParams, groups);
            expect(params[0].groupName).toEqual("custom");
        });
        it('should return text widget for invalid type param', function() {
            var inputParams = [ {
                type : 'none'
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("input");
            expect(params[0].parameters[0].inputType).toEqual("text");
        });
        it('should return dropdown widget for context ITEM', function() {
            var inputParams = [ {
                context : 'item'
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("select");
        });
        it('should return item options for context ITEM', function() {
            var inputParams = [ {
                context : 'item',
                filterCriteria : [{
                    name: 'type',
                    value: 'number'
                }]
            } ];
            
            var items = [{
                type : 'number',
                label : 'number item'
            }, {
                type : 'contact',
                label : 'contact item'
            }]
            
            var deferred = $q.defer();
            var prom = deferred.promise;

            spyOn(itemRepository, 'getAll').and.returnValue(prom);
            
            var params = configService.getRenderingModel(inputParams);
            prom.then(function() {
                expect(params[0].parameters[0].element).toEqual("select");
                expect(params[0].parameters[0].options.length).toEqual(1);
                expect(params[0].parameters[0].options[0]).toBeDefined();
                expect(params[0].parameters[0].options[0].label).toEqual('number item');
            })

            deferred.resolve(items);

        });
        it('should return specific thing options for context THING and filter criteria', function() {
            var inputParams = [ {
                context : 'thing',
                filterCriteria : [{
                    name: 'UID',
                    value: 'binding:thingType:thingId1'
                },{
                    name: 'UID',
                    value: 'binding:thingType:thingId3'
                }]
            } ];
            
            var things = [{
                label : 'Magic Thing 1',
                UID : 'binding:thingType:thingId1'
            }, {
                label : 'Magic Thing 2',
                UID : 'binding:thingType:thingId2'
            }, {
                label : 'Magic Thing 3',
                UID : 'binding:thingType:thingId3'
            }]
            
            var deferred = $q.defer();
            var prom = deferred.promise;

            spyOn(thingRepository, 'getAll').and.returnValue(prom);
            
            var params = configService.getRenderingModel(inputParams);
            prom.then(function() {
                expect(params[0].parameters[0].element).toEqual("select");
                expect(params[0].parameters[0].options.length).toEqual(2);
                expect(params[0].parameters[0].options[0]).toBeDefined();
                expect(params[0].parameters[0].options[0].label).toEqual('Magic Thing 1');
                expect(params[0].parameters[0].options[1]).toBeDefined();
                expect(params[0].parameters[0].options[1].label).toEqual('Magic Thing 3');
            })

            deferred.resolve(things);

        });
        it('should retain thing options for context THING and update label', function() {
            var inputParams = [ {
                    context : 'thing',
                    options : [ 
                        { value:'thingUID1', label:'Existing Thing 1' },
                        { value:'thingUID2' },
                        { value:'thingUID3' }
                    ]
            }];
            
            var things = [{
                label : 'Magic Thing 2',
                UID : 'thingUID2'
            }]
            
            var deferred = $q.defer();
            var prom = deferred.promise;
            
            spyOn(thingRepository, 'getAll').and.returnValue(prom);
            
            var params = configService.getRenderingModel(inputParams);
            prom.then(function() {
                expect(params[0].parameters[0].element).toEqual("select");
                expect(params[0].parameters[0].options.length).toEqual(2);
                expect(params[0].parameters[0].options[0]).toBeDefined();
                expect(params[0].parameters[0].options[0].label).toEqual('Existing Thing 1');
                expect(params[0].parameters[0].options[1]).toBeDefined();
                expect(params[0].parameters[0].options[1].label).toEqual('Magic Thing 2');
                expect(params[0].parameters[0].options[2]).toBeDefined();
                expect(params[0].parameters[0].options[2].label).toEqual('thingUID3');
            })
            
            deferred.resolve(things);
            
        });
        it('should return date widget for context DATE type=Text', function() {
            var inputParams = [ {
                context : 'date',
                type : 'text'
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("date");
        });
        it('should return date widget for context DATE type!=text', function() {
            var inputParams = [ {
                context : 'date',
                type : ''
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("input");
        });
        it('should return dropdown widget for context THING', function() {
            var inputParams = [ {
                context : 'thing'
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("select");
        });
        it('should call thingService when context=THING', function() {
            var inputParams = [ {
                context : 'thing'
            } ];
            $httpBackend.when('GET', restConfig.restPath + "/things").respond([ {
                name : "thing"
            } ]);
            var params = configService.getRenderingModel(inputParams);
            $httpBackend.flush();
            expect(params[0].parameters[0].options.length).toEqual(1);
        });
        it('should return input time widget for context TIME type=text', function() {
            var inputParams = [ {
                context : 'time',
                type : 'text'
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("input");
            expect(params[0].parameters[0].inputType).toEqual("time");
        });
        it('should return input time widget for context TIME type!=text', function() {
            var inputParams = [ {
                context : 'time',
                type : ''
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("input");
            expect(params[0].parameters[0].inputType).toBeUndefined();
        });
        it('should return color widget for context COLOR', function() {
            var inputParams = [ {
                context : 'color'
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("color");
            expect(params[0].parameters[0].input).toEqual("TEXT");
            expect(params[0].parameters[0].inputType).toEqual("color");
        });
        it('should return script widget for context SCRIPT', function() {
            var inputParams = [ {
                context : 'script'
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("textarea");
            expect(params[0].parameters[0].inputType).toEqual("text");
            expect(params[0].parameters[0].label).toBeDefined();
        });
        it('should return dayOfWeek widget for context DAYOFWEEK', function() {
            var inputParams = [ {
                context : 'dayOfWeek'
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("dayofweek");
            expect(params[0].parameters[0].inputType).toEqual("text");
        });
        it('should return password widget for context PASSWORD', function() {
            var inputParams = [ {
                context : 'password'
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("input");
            expect(params[0].parameters[0].inputType).toEqual("password");
        });
        it('should return text widget for INVALID context', function() {
            var inputParams = [ {
                context : 'none'
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("input");
            expect(params[0].parameters[0].inputType).toEqual("text");
        });
        it('should return dropdown widget for type TEXT with options no limit', function() {
            var inputParams = [ {
                type : 'text',
                options : [ 1, 2 ],
                limitToOptions : true,
                required : true
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("select");
            expect(params[0].parameters[0].options.length).toEqual(2);
        });
        it('should return multiselect widget for type TEXT with options and limit', function() {
            var inputParams = [ {
                type : 'text',
                options : [ 1, 2 ],
                limitToOptions : false,
                required : true
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("multiSelect");
            expect(params[0].parameters[0].options.length).toEqual(2);
        });
        it('should return text widget for type TEXT', function() {
            var inputParams = [ {
                type : 'text'
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("input");
            expect(params[0].parameters[0].inputType).toEqual("text");
        });
        it('should return dropdown widget for type INTEGER/DECIMAL with options', function() {
            var inputParams = [ {
                type : 'integer',
                options : [ {
                    value : "1"
                }, {
                    value : "2"
                } ],
                limitToOptions : true
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("select");
            expect(params[0].parameters[0].options[0].value).toEqual(1);
            inputParams.type = 'decimal';
            var paramsDecimal = configService.getRenderingModel(inputParams);
            expect(paramsDecimal[0].parameters[0].element).toEqual("select");
            expect(paramsDecimal[0].parameters[0].options[0].value).toEqual(1);
        });
        it('should return parse floats type DECIMAL with options', function() {
            var inputParams = [ {
                type : 'decimal',
                options : [ {
                    value : "1.1"
                }, {
                    value : "2.2"
                } ],
                limitToOptions : true
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("select");
            expect(params[0].parameters[0].options[0].value).toEqual(1.1);
            expect(params[0].parameters[0].options[1].value).toEqual(2.2);
        });
        it('should return number widget for type INTEGER/DECIMAL', function() {
            var inputParams = [ {
                type : 'integer'
            } ];
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("input");
            expect(params[0].parameters[0].inputType).toEqual("number");
            expect(params[0].parameters[0].pattern).toEqual("-?\\d+");

            inputParams[0].pattern = '[1-3]{4}';
            var params = configService.getRenderingModel(inputParams);
            expect(params[0].parameters[0].element).toEqual("input");
            expect(params[0].parameters[0].inputType).toEqual("number");
            expect(params[0].parameters[0].pattern).toEqual("[1-3]{4}");

            inputParams[0].type = 'decimal';
            inputParams[0].pattern = undefined;
            var paramsDecimal = configService.getRenderingModel(inputParams);
            expect(paramsDecimal[0].parameters[0].element).toEqual("input");
            expect(paramsDecimal[0].parameters[0].inputType).toEqual("number");
            expect(params[0].parameters[0].pattern).toBe(undefined);
        });
        it('should set defaults type DECIMAL', function() {
            var thing = {
                    configuration: {}
            }
            
            var thingType = {
                configParameters : [ {
                    name: 'test',
                    type : 'DECIMAL',
                    defaultValue : '1.1'
                } ]
            }

            configService.setDefaults(thing, thingType);
            expect(thing.configuration['test']).toEqual(1.1);
        });
        it('should set config values from type DECIMAL', function() {
            var originalConfiguration = {};
            
            var groups = [{
                parameters : [ {
                    name : 'test',
                    type : 'DECIMAL',
                    defaultValue : '1.1'
                } ]
            }]

            var config = configService.setConfigDefaults(originalConfiguration, groups, false);
            expect(config['test']).toEqual(1.1);
        });
        it('should rertieve rules and add options for context RULE', function() {
            var inputParams = [ {
                context : 'RULE'
            } ];

            $httpBackend.when('GET', restConfig.restPath + "/rules").respond([ {
                uid : 'automation:rule:1',
                name : 'rule1'
            }, {
                uid : 'automation:rule:2',
                name : 'rule2'
            } ]);
            var params = configService.getRenderingModel(inputParams);
            $httpBackend.flush();
            expect(params[0].parameters[0].options.length).toEqual(2);
            expect(params[0].parameters[0].options[0].value).toEqual('automation:rule:1');
            expect(params[0].parameters[0].options[1].label).toEqual('rule2');
        });
    });
    
    describe('test for replaceEmtpyValues method', function() {
        it('should replace empty strings and undefined values by null', function () {
            var emptyProperties = {
                    label : '',
                    value : undefined,
                    property : null
            }
            
            emptyProperties = configService.replaceEmptyValues(emptyProperties)
            expect(emptyProperties.label).toBe(null)
            expect(emptyProperties.value).toBe(null)
            expect(emptyProperties.property).toBe(null)
        })
    })

});