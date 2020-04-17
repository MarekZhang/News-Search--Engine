# Lucene-Search-App
A implementation of search Engine based on Apache Lucene and four sources news
 - CS7IS3 Assignment 2

## Running Environment

```
Linux 4.4.0
Maven 3.2.2
Java 1.8
gcc 5.4.0
Lucene 7.2.1
```

## Running App
- 1. Download this repo</br>
- 2. Build project
```shell
cd Lucene-Search-News-Engine/ir_artifact
```
```shell
mvn clean
```
```shell
mvn package
```
- 3. Run Index 
```shell
java -cp target/ir_artifact-1.0-SNAPSHOT.jar com.test.lucene.IndexCreate 
```
- 4. Run Search
```shell
java -cp target/ir_artifact-1.0-SNAPSHOT.jar com.test.lucene.IndexSearch -ExpQuery
```
```shell
java -cp target/ir_artifact-1.0-SNAPSHOT.jar com.test.lucene.IndexSearch -NormalQuery
```
- 5. Evaluation</br>
```shell
cd trec_eval
```
```shell
./trec_eval trec_eval_assignment2 results.out
```
