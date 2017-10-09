# README #

Undergrad
UID - 679882752
Email - rnambi2@uic.edu
First Name - Rohit
Last Name - Nambiar

### What is this repository for? ###

UIC CS 441 HW2

### How do I get set up? ###

youtube link - https://youtu.be/a-1pHVuhEds

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