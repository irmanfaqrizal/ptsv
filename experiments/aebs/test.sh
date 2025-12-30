name=aebs
cd ../../ptsv-app
mvn clean package
cp target/ptsv.jar ../experiments/$name/ptsv.jar
cd ../experiments/$name
# java -cp ptsv.jar com.ptsv.app.App mapping $name.if
java -cp ptsv.jar com.ptsv.app.App $name.if
# java -cp ptsv.jar com.ptsv.app.App simple.if traces