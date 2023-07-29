import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.assertThat

import groovy.json.JsonSlurper

def json = '''\
{
  "person": {
    "name": "John",
    "age": 10,
    "pets": ["cat", "dog"]
  }
}
'''

def result = new JsonSlurper().parseText(json)

assertThat(result.person.name, is("John"))
assertThat(result.person.age, is(10))
assertThat(result.person.pets.size(), is(2))
assertThat(result.person.pets[0], is("cat"))
assertThat(result.person.pets[1], is("dog"))
