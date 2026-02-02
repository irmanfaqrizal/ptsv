name="performance"
confs=("c1" "c2" "c3" "c4" "c5" "c6" "c7" "c8" "c9")

cd ../../ptsv-app
mvn clean package

for conf in "${confs[@]}"
do
    echo "!!!!!!!!!!!!!!! GENERATING $conf !!!!!!!!!!!!!!!"
    cp target/ptsv.jar ../experiments/$name/$conf/ptsv.jar
    cd ../experiments/$name/$conf
    java -cp ptsv.jar com.ptsv.app.App simple.if
    cd ../../../ptsv-app
done