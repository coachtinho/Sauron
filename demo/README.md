# Sauron demonstration

## Get started

This is a Sauron application where 2 different clients: **eye** and **spotter** can send data to server.

## Instructions for installing

Make sure that the parent POM was installed first, and you are on the project's root folder.

```
mvn clean install -DskipTests
```

## Step 1: Run server

To compile and run using _exec_ plugin:

```
cd silo-server/
mvn compile exec:java
```

## Step 2: Run eye

Open a new shell and make sure you are on the project's root folder.

To populate server with data:

```
cd eye
mvn compile exec:java < ../demo/Data/input.txt
```

```
EyeApp
Received 5 arguments
arg[0] = localhost
arg[1] = 8080
arg[2] = Alameda
arg[3] = -25.284736
arg[4] = 30.621354
localhost:8080
Camera was sucessfully registered
Sucessfully reported 7 items
Sucessfully reported 6 items
Exiting...
```

CAM_NAME = camera name \
 LONGITUDE = longitude \
 LATITUDE = latitude

## Step 3: Run spotter

To run the spotter client on **LINUX**:

```
cd ../spotter/
mvn compile exec:java < ../demo/Data/spotter_input.txt
```

make sure you see the following output:
```
SpotterApp
Received 2 arguments
arg[0] = localhost
arg[1] = 8080
localhost:8080
Spot: shows information regarding observations of the objects with identifiers that match with id
   Usage: spotter objectType id
Trail: shows the path taken by the object with id
   Usage: trail objectType id
Ping: shows information regarding the state of server
   Usage: ping
Clear: cleans server state
   Usage: clear
Init: configures server
   Usage: init
Help: shows commands supported by application
   Usage: help
Exit: exits the application
   Usage: exit
Server initialized
person,5638246,2020-04-03T12:13:39,Alameda,-25,284736,30,621354       
car,20SD23,2020-04-03T12:13:36,Alameda,-25,284736,30,621354
Caught exception with code INVALID_ARGUMENT and description: INVALID_ARGUMENT: Car ID doesn't match rules
person,5111111,2020-04-03T12:13:39,Alameda,-25,284736,30,621354       
person,5111112,2020-04-03T12:13:36,Alameda,-25,284736,30,621354       
person,5112112,2020-04-03T12:13:36,Alameda,-25,284736,30,621354       
person,5638246,2020-04-03T12:13:39,Alameda,-25,284736,30,621354       
car,10SD21,2020-04-03T12:13:36,Alameda,-25,284736,30,621354
person,5111112,2020-04-03T12:13:36,Alameda,-25,284736,30,621354       
person,5112112,2020-04-03T12:13:36,Alameda,-25,284736,30,621354       
Exiting...
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

---
