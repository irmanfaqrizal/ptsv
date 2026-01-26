name=services
confs=("C1" "C2")

cd ../../ptsv-app
mvn clean package

for conf in "${confs[@]}"
do
    cp target/ptsv.jar ../experiments/$name/specification/$conf/ptsv.jar
    cd ../experiments/$name/specification/$conf
    java -cp ptsv.jar com.ptsv.app.App $name.if
    cd ../../../../ptsv-app
done

cd ../experiments/$name/simulation
source myenv/bin/activate
python3 simulator.py
deactivate
for conf in "${confs[@]}"
do
    cp -TR results/$conf ../specification/$conf/simulation
    cd ../specification/$conf
    java -cp ptsv.jar com.ptsv.app.App $name.if simulation
    cd ../../simulation
done

cd ../execution
source myenv/bin/activate
python3 executor.py
deactivate
for conf in "${confs[@]}"
do
    cp -TR results/$conf ../specification/$conf/execution
    cd ../specification/$conf
    java -cp ptsv.jar com.ptsv.app.App $name.if execution
    cd ../../execution
done


