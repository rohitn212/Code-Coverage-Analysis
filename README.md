# README #

### What is this repository for? ###

A code coverage analysis program that I developed for my cloud computing class at UIC. Initially, I had to choose an open sourced repository on github to work with, for which I ended up choosing https://github.com/toby1984/boolean-algebra. I had to write JUnit tests to further maximise coverage and then generate code coverage reports of said JUnit tests using Jacoco. Used an XML parser to parse the code coverage reports and produce an intermediate report. The intermediate report is then fed into Apache Hadoop MapReduce which produces a report containing the lines of code covered by the tests as the key and the tests that cover them as values (sorted in order of how many lines each test covers). Deployed the program in AWS S3 and AWS EMR to run it on the cloud.

### How do I get set up? ###

Download repo, navigate into boolean-algebra and execute following commands:

Gradle command to create xmls for tests using Jacoco : 
gradle test --tests gradle test --tests de.codesourcery.booleanalgebra.ExpressionContextTest.removeNullTest jacocoTestReport
gradle test --tests gradle test --tests de.codesourcery.booleanalgebra.ExpressionContextTest.lookupNullTest jacocoTestReport
gradle test --tests gradle test --tests de.codesourcery.booleanalgebra.ExpressionContextTest.setNullTest jacocoTestReport

After each command, move the xml file from build/reports/jacoco/test to a folder called "xml" in the root folder(boolean-algebra)
Then run the ParseXML.java file in src/main/java, it will automatically read in the xml files from a "xml" folder in the root folder.
It should produce a textfile called MapInput.txt in the root folder, which you should put in a folder called MapInput within the root
folder itself

Gradle command to create jar file - ./gradlew jar

Hadoop command - hadoop jar build/libs/boolean-algebra-0.6.2-SNAPSHOT.jar MapInput MapOutput
