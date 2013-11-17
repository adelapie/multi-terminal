multi-terminal
==============

multi-terminal is a command-line tool for managing the IRMA 
card. It uses the Idemix library together with SCUBA and different libraries
from https://github.com/credentials/.

## Installation
```
git submodule init
git submodule update
```
Generate the following jars: credentials_api.dev.jar, credentials_idemix.dev.jar,  
idemix_library.dev.jar, idemix_terminal.dev.jar and scuba.dev.jar
via the instructions available at http://credentials.github.io/

Create the lib directory (mkdir lib) and move the jars to ./lib. Then, install them using maven:
```
mvn install:install-file -Dfile=credentials_api.dev.jar -DgroupId=org.irmacard.credentials -DartifactId=credentials -Dversion=1.0 -Dpackaging=org.irmacard.credentials
mvn install:install-file -Dfile=credentials_idemix.dev.jar -DgroupId=org.irmacard.credentials.idemix -DartifactId=credentials-idemix -Dversion=1.0 -Dpackaging=org.irmacard.credentials.idemix
mvn install:install-file -Dfile=idemix_library.dev.jar -DgroupId=com.ibm.zurich -DartifactId=ibm-idemix -Dversion=1.0 -Dpackaging=com.ibm.zurich
mvn install:install-file -Dfile=idemix_terminal.dev.jar -DgroupId=org.irmacard.idemix -DartifactId=idemix-terminal -Dversion=1.0 -Dpackaging=org.irmacard.idemix
mvn install:install-file -Dfile=scuba.dev.jar -DgroupId=net.sourceforge.scuba -DartifactId=scuba -Dversion=1.0 -Dpackaging=net.sourceforge.scuba
```
Finally, compile and generate a jar file:
```
mvn clean install
```
## Use
```
test@ss:~$ java -jar target/multi_terminal-1.0-SNAPSHOT-jar-with-dependencies.jar -h
usage: IRMA Terminal
 -h,--help                      print this message and exit
 -i,--info-card                 get information about IRMA card
 -l,--log <arg>                 get log entries (requires admin pin
                                (6-digit))
 -uap <old-pin new-pin>         update admin pin (6-digit)
 -vap,--verify-card-pin <arg>   verify admin pin status (6-digit)
 -vcp,--verify-cred-pin <arg>   verify credential pin status (4-digit)
```
