const assert = require('assert');
var proxyquire = require('proxyquire').noCallThru();

describe('Conditionals', function () {

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

    describe('Function Conditions', function () {
        it('Should pass when the function returns true', function () {

            const condition_conf = proxyquire('../fluent/condition-conf', {
                '../log': createLogMock().mock,
                '../items': itemMock()
              });

            let conf = new condition_conf.FunctionConditionConf(() => true);
            assert.strictEqual(conf.check(), true);
        });

        it('Should not pass when the function returns false', function () {

            const condition_conf = proxyquire('../fluent/condition-conf', {
                '../log': createLogMock().mock,
                '../items': itemMock()
              });

            let conf = new condition_conf.FunctionConditionConf(() => false);
            assert.strictEqual(conf.check(), false);
        });
    });

    describe('Item Conditions', function () {
        it('Should pass when the item state matches', function () {

            const condition_conf = proxyquire('../fluent/condition-conf', {
                '../log': createLogMock().mock,
                '../items': itemMock((name) => {
                    assert.strictEqual(name, 'myitem');
                      return {
                          state: "mystate"
                      }
                })
              });

            let conf = new condition_conf.ItemStateConditionConf('myitem');
            
            assert.strictEqual(conf.is('mystate').check(), true);
        });

        it('Should not pass when the item state doesnt matches', function () {
            
            const condition_conf = proxyquire('../fluent/condition-conf', {
                '../log': createLogMock().mock,
                '../items': itemMock((name) => {
                    assert.strictEqual(name, 'myitem');
                      return {
                          state: "mystate2"
                      }
                })
              });

            let conf = new condition_conf.ItemStateConditionConf('myitem');
            
            assert.strictEqual(conf.is('mystate').check(), false);
        });

        it('Should not pass when the item doesnt exist', function () {
            
            const condition_conf = proxyquire('../fluent/condition-conf', {
                '../log': createLogMock().mock,
                '../items': itemMock((name) => {
                    assert.strictEqual(name, 'myitem');
                    return undefined;
                })
              });

            let conf = new condition_conf.ItemStateConditionConf('myitem');
            
            assert.throws(() => conf.is('mystate').check(), {message: /myitem/});
        });

        it('Should not pass when the item is null', function () {

            const condition_conf = proxyquire('../fluent/condition-conf', {
                '../log': createLogMock().mock,
                '../items': itemMock((name) => {
                    assert.strictEqual(name, 'myitem');
                    return null;
                })
              });

            let conf = new condition_conf.ItemStateConditionConf('myitem');
            
            assert.throws(() => conf.is('mystate').check(), {message: /myitem/});
        });

    });
});
