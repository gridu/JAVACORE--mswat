The task is to find simple stats from csv input files:
- Find the current amount for each position, warehouse, product.
- Find max, min, avg amounts for each warehouse and product.

Spark setup is done with docker. Running following command will start spark cluster (sparm master and single worker)
```console
> docker-compose up -d
```
Before submitting app to the cluster you need to package it into a jar file.
```console
> sbt package
[info] Packaging /Users/mswat/JAVACORE--mswat/target/scala-2.12/javacore-mswat_2.12-0.1.jar ...
[info] Done packaging.
[success] Total time: 4 s
```
Note: you should build the app every time code changes in order to reflect changes in cluster behaviour.

Then you can submit the application to the cluster with command:
```console
> docker exec -it spark-master /spark/bin/spark-submit --class warehouse.Warehouse --master local[2] /scala_code/target/scala-2.12/javacore-mswat_2.12-0.1.jar
```

When you are done remeber to free the resources aka shutdown the cluster
```
> docker-compose down
```
