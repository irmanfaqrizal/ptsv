# cd ../../ptsv-app
# mvn clean package
# cp target/ptsv.jar ../experiments/example/ptsv.jar
# cd ../experiments/example
java -cp ptsv.jar com.ptsv.app.App simple.if
java -cp ptsv.jar com.ptsv.app.App simple.if traces