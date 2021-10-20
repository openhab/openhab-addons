const assert = require('assert');
var proxyquire = require('proxyquire').noCallThru();

describe('Operations', function () {

    const createLogMock = () => {
        let messages = [];
    
        return { 
            messages, 
            mock:function(name){
                return {
                    error: a => messages.push(a)
                }
            }
        };
    }

    function itemMock(nameToState) {
        return {
            getItem: function(name){
                return nameToState(name);
            }
        }
    }

    describe('Copy State Operation', function () {
        it('Should copy state via send when everything set up correctly', function (done) {

            const operation_conf = proxyquire('../fluent/operation-conf',  {
                '../log': createLogMock().mock,
                '../items': itemMock((name) => {
                    if(name == 'item1') {
                        return {
                            state: "test1"
                        }
                    } else if(name == 'item2') {
                        return {
                            sendCommand: (state) => {
                                assert.strictEqual(state, "test1");
                                done();
                            }
                        }
                    } else {
                        assert.fail("wrong item requested " + name);
                    }
                })
              });

            let conf = new operation_conf.CopyStateOperation(true);

            conf.fromItem('item1').toItem('item2')._run(); 
        });

        it('Should copy state via update when everything set up correctly', function (done) {

            const operation_conf = proxyquire('../fluent/operation-conf',  {
                '../log': createLogMock().mock,
                '../items': itemMock((name) => {
                    if(name == 'item1') {
                        return {
                            state: "test1"
                        }
                    } else if(name == 'item2') {
                        return {
                            postUpdate: (state) => {
                                assert.strictEqual(state, "test1");
                                done();
                            }
                        }
                    } else {
                        assert.fail("wrong item requested " + name);
                    }
                })
              });

            let conf = new operation_conf.CopyStateOperation(false);

            conf.fromItem('item1').toItem('item2')._run(); 
        });

        it('Should copy null state', function (done) {

            const operation_conf = proxyquire('../fluent/operation-conf',  {
                '../log': createLogMock().mock,
                '../items': itemMock((name) => {
                    if(name == 'item1') {
                        return {
                            state: null
                        }
                    } else if(name == 'item2') {
                        return {
                            sendCommand: (state) => {
                                assert.strictEqual(state, null);
                                done();
                            }
                        }
                    } else {
                        assert.fail("wrong item requested " + name);
                    }
                })
              });

            let conf = new operation_conf.CopyStateOperation(true);

            conf.fromItem('item1').toItem('item2')._run(); 
        });

        it('Should disallow omission of to item', function () {

            const operation_conf = proxyquire('../fluent/operation-conf',  {
                '../log': createLogMock().mock,
                '../items': itemMock((name) => {
                    if(name == 'item1') {
                        return {
                            state: "test1"
                        }
                    } else if(name == 'item2') {
                        return {
                            sendCommand: (state) => {
                                assert.strictEqual(state, "test1");
                            }
                        }
                    } else {
                        assert.fail("wrong item requested " + name);
                    }
                })
              });

            let conf = new operation_conf.CopyStateOperation(true);

            assert.throws(() => conf.fromItem('item1')._run(), {message: /[Tt]o/});
        });

        it('Should disallow omission of from item', function () {

            const operation_conf = proxyquire('../fluent/operation-conf',  {
                '../log': createLogMock().mock,
                '../items': itemMock((name) => {
                    if(name == 'item1') {
                        return {
                            state: "test1"
                        }
                    } else if(name == 'item2') {
                        return {
                            sendCommand: (state) => {
                                assert.strictEqual(state, "test1");
                            }
                        }
                    } else {
                        assert.fail("wrong item requested " + name);
                    }
                })
              });

            let conf = new operation_conf.CopyStateOperation(true);

            assert.throws(() => conf.toItem('item1')._run(), {message: /[Ff]rom/});
        });

        it('Should disallow omission of both items', function () {

            const operation_conf = proxyquire('../fluent/operation-conf',  {
                '../log': createLogMock().mock,
                '../items': itemMock((name) => {
                    if(name == 'item1') {
                        return {
                            state: "test1"
                        }
                    } else if(name == 'item2') {
                        return {
                            sendCommand: (state) => {
                                assert.strictEqual(state, "test1");
                            }
                        }
                    } else {
                        assert.fail("wrong item requested " + name);
                    }
                })
              });

            let conf = new operation_conf.CopyStateOperation(true);

            assert.throws(() => conf._run());
        });

        it('Should tell you if from item doesnt exist', function () {

            const operation_conf = proxyquire('../fluent/operation-conf',  {
                '../log': createLogMock().mock,
                '../items': itemMock((name) => {
                    if(name == 'item1') {
                        return undefined;
                    } else if(name == 'item2') {
                        return {}
                    } else {
                        assert.fail("wrong item requested " + name);
                    }
                })
              });

            let conf = new operation_conf.CopyStateOperation(true);

            assert.throws(() => conf.fromItem('item1').toItem('item2')._run(), {message: /item1/});
        });

        it('Should tell you if to item doesnt exist', function () {

            const operation_conf = proxyquire('../fluent/operation-conf',  {
                '../log': createLogMock().mock,
                '../items': itemMock((name) => {
                    if(name == 'item1') {
                        return {};
                    } else if(name == 'item2') {
                        return undefined
                    } else {
                        assert.fail("wrong item requested " + name);
                    }
                })
              });

            let conf = new operation_conf.CopyStateOperation(true);

            assert.throws(() => conf.fromItem('item1').toItem('item2')._run(), {message: /item2/});
        });

    });
});
