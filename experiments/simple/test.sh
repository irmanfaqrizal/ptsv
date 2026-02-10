# cd ../../ptsv-app
# mvn clean package
# cp target/ptsv.jar ../experiments/simple/ptsv.jar
cd ../experiments/simple
java -cp ptsv.jar com.ptsv.app.App simple.if

#### Uncomment below to use trace injection
# java -cp ptsv.jar com.ptsv.app.App simple.if -trace traces
