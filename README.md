# Description

![Screenshot](System%20diagram.png)

There are 3 applications:
- ZSMDP - a desktop application made in JavaFX. 
  - Users can see train schedule, report passing trains through the station and send messages to other users (in other stations). 
  - The messages are sent to the CSZMDP application via secure TCP socket. 
  - Users log in, log out and get train schedules via SOAP service from CSZMDP.
  - Announcements are sent to all other ZSMDP applications and CSZMDP via multicast socket. 
  - Reports are sent to AZSMDP via Java RMI. 
- CSZMDP - holds train schedules and is responsible for handling user credentials and logins. It offers a SOAP service.
  - Train schedules are kept in Redis. 
  - There is also a REST service for receiving train passing reports. 
- AZSMDP - an archival application responsible for receiving, storing and sending reports in PDF format.  
