///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 25+
//DEPS io.github.awenzel67:aijsondb-java:0.0.3
import io.github.awenzel67.AIJsonDBC;
void main(String... args) {
    try {
        AIJsonDBC.loadData("../data/500 KB_V3.json","../data/employeeSchemaDescription_V3.json");
        com.google.gson.JsonElement result = AIJsonDBC.query("var result=data.employees.length;");
        System.out.println("Query result: " + result);
        // Javascript Expression containing an error.
        result = AIJsonDBC.query("var result=data.employees.length;");
        System.out.println("Query result: " + result);
    } catch (RuntimeException e) {
        System.err.println("Error loading data: " + e.getMessage());
    }
}
