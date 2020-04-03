# Sauron demonstration

## Get started

This is a Sauron application where 2 different clients: __eye__ and __spotter__ can send data to server.

## Instructions for installing

Make sure that the parent POM was installed first.

```
mvn clean install -DskipTests
```

## Step 1: Run server

To compile and run using _exec_ plugin:
```
cd silo-server
mvn compile exec:java
```

## Step 2: Run eye

To run using appassembler plugin on Linux:
```
./target/appassembler/bin/eye localhost 8080 CAM_NAME LONGITUDE LATITUDE < input.txt
```

To run using appassembler plugin on Windows:

```
target\appassembler\bin\eye arg0 arg1 arg2
```

To compile and run using _exec_ plugin:

```
cd eye
mvn compile exec:java
```

This can only be run once as it sets a predefined _camera name_.

## Step 3: Run spotter

To run the spotter client:

```
cd spotter/
mvn exec:java
```






### To run more than one instance
Run using appassembler plugin on Linux:

```
./target/appassembler/bin/eye arg0 arg1 arg2
```

To run using appassembler plugin on Windows:

```
target\appassembler\bin\eye arg0 arg1 arg2
```

## To configure the Maven project in Eclipse

'File', 'Import...', 'Maven'-'Existing Maven Projects'

'Select root directory' and 'Browse' to the project base folder.

Check that the desired POM is selected and 'Finish'.


----

