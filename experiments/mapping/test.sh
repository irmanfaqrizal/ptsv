model=$1

# cd ../../ptsv-app
# mvn clean package
# cp target/ptsv.jar ../experiments/mapping/ptsv.jar
# cd ../experiments/mapping
java -cp ptsv.jar com.ptsv.app.App mapping $model