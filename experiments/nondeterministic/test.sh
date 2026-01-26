model="nondeterministic"

cd ../../ptsv-app
mvn clean package
cp target/ptsv.jar ../experiments/$model/ptsv.jar
cd ../experiments/$model
java -cp ptsv.jar com.ptsv.app.App $model.if