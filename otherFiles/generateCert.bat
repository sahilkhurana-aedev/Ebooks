rem usage: createKey.bat <user> <password>
rem        createKey.bat somebody password123

rem generates several self signed keys <name>.cer, <name>.jks, and <name>.p12 .
rem Truststore is set with name truststore.jks and set password of System@123

rem  Certificate Attributes -> CN: CommonName, OU: Organiztional Unit, O: Organization, L: Locaility, S: State, C: CountryName

set NAME=%1%
set PASSWORD=%2%
set TRUST_STORE_PASSWORD=%2%		
set JAVA_CERT_PATH=C:\Program Files\Java\jdk1.8.0_191\jre\lib\security\cacerts
set SAN_IP=192.168.161.127


rem Get Count of Existing Certificates in JAVA_CERT_PATH
echo 'Get Count of existing list of certificates from keystore ' %JAVA_CERT_PATH%
keytool -list -keystore "%JAVA_CERT_PATH%" -storepass changeit

rem Generate JKS key
echo 'Creating JKS key for ' %NAME% ' using password ' %PASSWORD%
keytool -genkey -alias %NAME% -keyalg RSA -sigalg SHA256withRSA -dname "CN=%NAME%,OU=AE,O=AE,L=CHD,S=CHD,C=IN" -keypass %PASSWORD% -storepass %PASSWORD% -keystore %NAME%.jks -ext san=ip:%SAN_IP%
echo 'Done creating key for ' %NAME%

rem The JKS keystore uses a proprietary format. It is recommended to migrate to PKCS12 which is an industry standard format 
keytool -importkeystore -srckeystore  %NAME%.jks -destkeystore %NAME%.p12 -srcstoretype JKS -deststoretype PKCS12 -srcstorepass %PASSWORD% -deststorepass %PASSWORD% -srcalias %NAME% -destalias %NAME% -srckeypass %PASSWORD% -destkeypass %PASSWORD% -noprompt
echo 'Done creating key PKCS12 for ' %NAME%

rem Generate Certificate using above generated JKS key
echo 'Generating Certificate ' %NAME%.cer
keytool -export -keystore  %NAME%.jks  -alias %NAME% -storepass %PASSWORD% -file %NAME%.cer
echo 'Done Creating Certificate ' %NAME%.cer

rem Insert certificate into keystore in cacerts JAVA_CERT_PATH
keytool -import -keystore "%JAVA_CERT_PATH%" -alias %NAME% -file %NAME%.cer -storepass changeit -noprompt

rem Get Updated Count of Certificates in JAVA_CERT_PATH
echo 'Get Count of existing list of certificates from keystore ' %JAVA_CERT_PATH%
keytool -list -keystore "%JAVA_CERT_PATH%" -storepass changeit

rem Generate truststore with all available certificates
keytool -import -trustcacerts -file %NAME%.cer -alias %NAME% -keystore truststore.jks -storepass %TRUST_STORE_PASSWORD% -noprompt




