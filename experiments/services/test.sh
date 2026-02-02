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

cd ..

mkdir -p verdicts

cp specification/C1/services-min-pts-verdicts-final.txt verdicts/Specification-C1.txt
cp specification/C2/services-min-pts-verdicts-final.txt verdicts/Specification-C2.txt

cp specification/C1/services-min-simulation-rem-pts-verdicts-final.txt verdicts/Simulation-C1.txt
cp specification/C2/services-min-simulation-rem-pts-verdicts-final.txt verdicts/Simulation-C2.txt

cp specification/C1/services-min-execution-rem-pts-verdicts-final.txt verdicts/Execution-C1.txt
cp specification/C2/services-min-execution-rem-pts-verdicts-final.txt verdicts/Execution-C2.txt

python3 graph.py