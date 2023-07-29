import static org.hamcrest.CoreMatchers.*
import static org.hamcrest.MatcherAssert.assertThat

import groovy.xml.XmlSlurper

def xml = '''\
<root>
  <person>
    <name>Brigitte</name>
    <age>34</age>
    <pets>bird</pets>
    <pets>fish</pets>
  </person>
</root>
'''

def result = new XmlSlurper().parseText(xml)

assertThat(result.person.name, is("Brigitte"))
assertThat(result.person.age, is(34))
assertThat(result.person.pets.size(), is(2))
assertThat(result.person.pets[0], is("bird"))
assertThat(result.person.pets[1], is("fish"))
