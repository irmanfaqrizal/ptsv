ifModel=simple.if
traces=traces
cd ../ptsv-app
mvn clean package
cp target/ptsv.jar ../experiments/ptsv.jar
cd ../experiments
java -cp ptsv.jar com.ptsv.app.App $ifModel
java -cp ptsv.jar com.ptsv.app.App $ifModel $traces