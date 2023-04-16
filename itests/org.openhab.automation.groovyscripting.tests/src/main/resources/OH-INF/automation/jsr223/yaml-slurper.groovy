import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.assertThat

import groovy.yaml.YamlSlurper

def yaml = '''\
person:
  name: "Itsuki"
  age: 78
  pets:
  - rabbit
  - snake
'''

def result = new YamlSlurper().parseText(yaml)

assertThat(result.person.name, is("Itsuki"))
assertThat(result.person.age, is(78))
assertThat(result.person.pets.size(), is(2))
assertThat(result.person.pets[0], is("rabbit"))
assertThat(result.person.pets[1], is("snake"))
