name=services
cd ../../ptsv-app
mvn clean package
cp target/ptsv.jar ../experiments/$name/ptsv.jar
cd ../experiments/$name
java -cp ptsv.jar com.ptsv.app.App $name.if