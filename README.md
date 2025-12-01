
# PTSV

PTSV is a tool to compute discrete Timed Probabilistic Transition Systems (TPTSs). It takes as input a network of timed automata and, optionally, a set of execution traces. Without the traces, the tool will generate probabilities of the transition delays according to uniform distribution. However, this feature is still being developed and only works for specific timed automata.

## Prerequisites

- [Java](https://www.java.com/en/download/manual.jsp) must be installed to run PTSV.
- The tool executes bash scripts inside to run other tools below.
- The network of timed automata is specified using the language described by the [IF toolset](https://gricad-gitlab.univ-grenoble-alpes.fr/verimag/if/if-toolset). It is required to install the toolset because PTSV uses it to generate discrete Timed Labeled Transition Systems (TLTSs).
- The [CADP Toolbox](https://cadp.inria.fr/) also needs to be installed for applying reduction on the TLTSs (and later for verification purposes).
- It is also recommended to install [graphviz](https://graphviz.org/download/) for visualization (PTSV uses it to generate transition system PDF files).

## Running PTSV

- A JAR file is provided in the [experiments](experiments) folder.
- The command to run it is as follows:
`````
cd experiments
java -cp ptsv.jar com.ptsv.app.App <IF model> <traces folder>
`````
- In the folder,  [experiments.sh](experiments/experiments.sh) can be executed to run PTSV using the example model [simple.if](experiments/simple.if) and the set of [traces](experiments/traces).
`````
cd experiments
./experiments.sh
`````
- In this example, the main outputs are as follows:
	- [simple-min-pts.pdf](experiments/simple-min-pts.pdf) shows the TPTS of simple.if according to uniform distribution.
	- [simple-min-traces-pts.pdf](experiments/simple-min-traces-pts.pdf) shows the TPTS of simple.if computed using the set of [traces](experiments/traces).

## Building from source

- Maven is required to build from source.
- The folder [ptsv-app](ptsv-app) contains a maven project of the tool.
- To rebuild the JAR file, run the following command:
`````
cd ptsv-app
mvn clean package
`````
- The JAR file will appear in [ptsv-app/target](ptsv-app/target) as ptsv.jar.