Simple thread pool that do it's best to run a job X milliseconds after submitting. Of course it is not guaranteed as pool may be overloaded or jobs may take huge amount of time.

One needs at least java 10 to run the code (local type inference used). However maven is set up to use version 11 (you may want to change it).

To run tests:
mvn -e test

For example of usage see:
ThreadPoolDemo.java
