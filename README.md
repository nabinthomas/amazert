# amazert 


Notes: 
## amazeRT Router Management Service
### Creating Certificates and Keys for the server. 
openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -keyout key.pem -out cert.pem

### The directory strucure is as follows.
amazert/src/* - Contains all the source files and install scripts. 

Makefile - used to build and package the services running on the router

### Build steps

1. Sync the repository locally 
2. The router management service (everything installed on the router) in amazert dir in the root. 
3. Following commands can bne used to build 

cd amazert

make # Builds the package

make commit #commits the built package so that it can be checked in, and used by the Android App later

The package amazert/amazert.pkg will be packaged with the android app for installing during the initial setup
The App will copy this file to the OpenWRT router, extract, and run install.sh in it. The install.sh script can be customized to copy the other app files to required locations, do the initial setup, and start the amazeRT Management Service