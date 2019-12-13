It's just a toy example of external sort:
- it does not check real amount of memory jvm can consume
- it assumes that single line can fit into the memory
- it operates on string length insetad of estimating size of a String stored in jvm memory

One need at least java 10 version to run the code (local type inference used). However maven is set up to build it under java 11 (you may want to change it).

To run tests:
mvn -e test

For example of usage see:
ExternalSortDemo.java
