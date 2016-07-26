# Glookast FIMS API test tool

This project services as a test tool and example code on how to communicate to Glookast products with the FIMS API implemented. At the moment of writing only the FIMS Capture service is implemented and works with Gloobox v2.0.

### Version
1.0.0

### Requirements
- For successful compilation and running of this test tool Java SDK v7+ is required.
- To send jobs a Gloobox v2.0 is required. Work is in the planning to build a capture service simulator that can be used instead of a Gloobox.

### Usage

Change working directory of shell / command prompt to the project folder then run the following command to compile and run the tool:
```
$ ./gradlew run
```

On first usage a service needs to be configured:

1. Go to Tools -> Services
2. Click New
3. Fill out the name
4. Fill out the address and port number on which a Gloobox channel is reachable
5. Fill out the callback address. This is the public address of the machine on which the tool is running. (e.g. 192.168.0.1:6000). The Gloobox will use this ip address and port number to communicate updates on the jobs
6. Press OK
7. Close the window

Video formats, Audio formats, Container formats are retrieved from the Gloobox. As soon this is done you are able to create Capture jobs and Capture profiles and send jobs to the Gloobox capture service.

