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
mvn exec:java
```

Make sure you see the following output:

```
SiloServerApp
Received 1 arguments
arg[0] = 8080
Server started
```

## Step 2: Run eye

Open a new shell and make sure you are on the project's root folder.

To populate server with data:

```
cd eye
mvn exec:java < ../demo/Data/input.txt
```

If you see this output then the server has been correctly initialized:

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

```
cd ../spotter/
mvn exec:java
```

Make Sure you see the following output:

```
SpotterApp
Received 2 arguments
arg[0] = localhost
arg[1] = 8080
localhost:8080
```

### Help

To access information about the supported commands write:

```
help
```

Make sure you see the following output:

```
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
```

### Init

Then, to add some default observations to the server, you can do the command init:

```
init
```

Make sure you see the following output:

```
Server initialized
```

### Spot

To get information about the most recent observation of the person with id 5638246, you can run the command:

```
spot person 5638246
```

Make sure you see the following output:

```
person,5638246,2020-04-03T12:13:39,Alameda,-25,284736,30,621354
```

Now with a car:

```
spot car 20SD23
```

Make sure you see the following output:

```
car,20SD23,2020-04-03T12:13:36,Alameda,-25,284736,30,621354
```

If you try with a person that doesn't exist:

```
spot person 1
```

Make sure there is no output.

If you try with an invalid car ID: (like _1_):

```
spot car 1
```

you should get the following error:

```
Caught exception with code INVALID_ARGUMENT and description: INVALID_ARGUMENT: Car ID doesn't match rules
```

To get the most recent observation of all the people whose id start with _5_:

```
spot person 5*
```

Make sure you see the following output:

```
person,5111111,2020-04-03T12:31:29,Alameda,-25.284736,30.621354
person,5111112,2020-04-03T12:31:26,Alameda,-25.284736,30.621354
person,5112112,2020-04-03T12:31:26,Alameda,-25.284736,30.621354
person,5638246,2020-04-03T12:31:29,Alameda,-25.284736,30.621354
```

To get the most recent observations of all the cars whose license plate start with _1_ and also ends with _1_:

```
spot car 1*1
```

Make sure you see the following output:

```
car,10SD21,2020-04-03T12:31:26,Alameda,-25.284736,30.621354
```

To get the most recent observations of all the people whose id ends with _2_

```
spot person *2
```

Make sure you see the following output:

```
person,5111112,2020-04-03T12:31:26,Alameda,-25.284736,30.621354
person,5112112,2020-04-03T12:31:26,Alameda,-25.284736,30.621354
```

### Trail

To see all the observations about a particular person:

```
trail person 5111111
```

Make sure you see the following output:

```
person,5111111,2020-04-03T12:55:18,Alameda,-25.284736,30.621354
person,5111111,2020-04-03T12:55:15,Alameda,-25.284736,30.621354
```

### Exiting the app

To clear the server state you can:

```
clear
```

Make sure you see the following output:

```
Server state cleared
```

And to exit the application:

```
exit
```

Make sure you see the following output:

```
Exiting...
```

and the application exits.

### Note

All the commands work for both people and cars, even tough some instances were not shown in this demonstration.
