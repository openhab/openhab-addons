const assert = require('assert');
var proxyquire = require('proxyquire').noCallThru();

describe('Triggers', function () {

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

    function triggersMock(lookup) {
        return x => lookup(x);
    }

    describe('Item Triggers', function () {
        it('Should create correct item trigger', function (done) {

            const trigger_conf = proxyquire('../fluent/trigger-conf', {
                '../log': createLogMock().mock,
                '../triggers': {
                    ItemStateChangeTrigger: (name, from, to) => {
                        assert.strictEqual(name, 'item1');
                        assert.strictEqual(from, undefined);
                        assert.strictEqual(to, 'state1');
                        done();
                    }
                }
            });

            new trigger_conf.ItemTriggerConfig('item1').changed().to('state1')._toOHTriggers();
        });

    });

});
