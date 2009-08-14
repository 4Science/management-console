copy pom.xml pom.xml.bak

REM - Spring DM -
call pax-import-bundle -g org.springframework.osgi -a spring-osgi-core -v 1.2.0
call pax-import-bundle -g org.springframework.osgi -a spring-osgi-extender -v 1.2.0
call pax-import-bundle -g org.springframework.osgi -a spring-osgi-io -v 1.2.0

REM - DuraCloud -
call pax-import-bundle -g org.duracloud -a storageprovider -v 1.0.0 -- -DimportTransitive -DwidenScope
call pax-import-bundle -g org.duracloud -a javaclient -v 1.0.0 -- -DimportTransitive -DwidenScope

REM - Other -
call pax-import-bundle -g org.apache.activemq -a com.springsource.org.apache.activemq -v 5.2.0 -- -DimportTransitive -DwidenScope
call pax-import-bundle -g javax.activation -a com.springsource.javax.activation -v 1.1.1
call pax-import-bundle -g org.slf4j -a com.springsource.slf4j.log4j -v 1.5.0 -- -DimportTransitive -DwidenScope
call pax-import-bundle -g com.thoughtworks.xstream -a com.springsource.com.thoughtworks.xstream -v 1.3.0 -- -DimportTransitive -DwidenScope
call pax-import-bundle -g org.jdom -a com.springsource.org.jdom -v 1.0.0

copy pom.xml pom-run.xml
move pom.xml.bak pom.xml
