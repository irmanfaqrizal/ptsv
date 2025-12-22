cd ../../ptsv-app
mvn clean package
cp target/ptsv.jar ../experiments/simpler/ptsv.jar
cd ../experiments/simpler
java -cp ptsv.jar com.ptsv.app.App simpler.if
# java -cp ptsv.jar com.ptsv.app.App simple.if traces