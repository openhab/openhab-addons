import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.MatcherAssert.assertThat

@Grab('org.apache.commons:commons-csv:1.10.0')
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVFormat

def rows = CSVParser.parse('room,temperature\nKitchen,21', CSVFormat.DEFAULT).records
assertThat(rows[1].get(0), is("Kitchen"))
