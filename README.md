multi-terminal
==============

multi-terminal is a command-line tool for managing the IRMA 
card. It uses the Idemix library together with SCUBA and different libraries
from https://github.com/credentials/.

## Installation
```
git submodule init
git submodule update
cd irma_configuration
git checkout demo
cd ..
```
Generate the following jars: credentials_api.dev.jar, credentials_idemix.dev.jar,  
idemix_library.dev.jar, idemix_terminal.dev.jar and scuba.dev.jar
via the instructions available at http://credentials.github.io/

Create the lib directory (mkdir lib) and move the jars to ./lib. Then, install them using maven:
```
mvn install:install-file -Dfile=lib/credentials_api.dev.jar -DgroupId=org.irmacard.credentials -DartifactId=credentials -Dversion=1.0 -Dpackaging=org.irmacard.credentials
mvn install:install-file -Dfile=lib/credentials_idemix.dev.jar -DgroupId=org.irmacard.credentials.idemix -DartifactId=credentials-idemix -Dversion=1.0 -Dpackaging=org.irmacard.credentials.idemix
mvn install:install-file -Dfile=lib/idemix_library.dev.jar -DgroupId=com.ibm.zurich -DartifactId=ibm-idemix -Dversion=1.0 -Dpackaging=com.ibm.zurich
mvn install:install-file -Dfile=lib/idemix_terminal.dev.jar -DgroupId=org.irmacard.idemix -DartifactId=idemix-terminal -Dversion=1.0 -Dpackaging=org.irmacard.idemix
mvn install:install-file -Dfile=lib/scuba.dev.jar -DgroupId=net.sourceforge.scuba -DartifactId=scuba -Dversion=1.0 -Dpackaging=net.sourceforge.scuba
```
Finally, compile and generate a jar file:
```
mvn clean install
```
## Use
```
test@test1:~$ java -jar target/multi_terminal-1.0-SNAPSHOT-jar-with-dependencies.jar -h
usage: IRMA Terminal
 -h,--help                         print this message and exit
 -i,--info-card                    get information about IRMA card
 -ir,--issue-root-cred <arg>       issue root cred - requires cred pin
 -is,--issue-student-cred <arg>    issue student cred - requires cred pin
 -l,--log <arg>                    get log entries - requires admin pin
 -qa,--query-admin-pin             query admin pin
 -qc,--query-cred-pin              query credential pin
 -rr,--remove-root-cred <arg>      remove root cred - requires admin pin
 -rs,--remove-student-cred <arg>   remove student cred - requires admin
                                   pin
 -uap <old-pin new-pin>            update admin pin (6-digit)
 -ucp <admin-pin new-cred-pin>     update cred pin (4-digit)
 -vap,--verify-card-pin <arg>      verify admin pin status (6-digit)
 -vcp,--verify-cred-pin <arg>      verify cred pin status (4-digit)
 -vr,--verify-root-cred            verify root cred - all
 -vrds,--verify-root-cred-ds       verify root cred - all with DS
 -vrn,--verity-root-cred-none      verify root cred - none
 -vs,--verify-student-cred         verify student credential - all
 -vsn,--verity-student-cred-none   verify student cred - none
```
