cd ../../ptsv-app
mvn clean package
cp target/ptsv.jar ../experiments/simple/ptsv.jar
cd ../experiments/simple
java -cp ptsv.jar com.ptsv.app.App simple.if
java -cp ptsv.jar com.ptsv.app.App simple.if traces