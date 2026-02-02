
# PTSV

- PTSV is a tool to compute discrete Timed Probabilistic Transition Systems (TPTSs).
- It takes as input a network of Discrete Stochastic Timed Automata and, optionally, a set of execution traces.
- The network of DSTA is described according to a subset of [IF specification](https://www-verimag.imag.fr/~async/IF/tutorials.html) language extended with discrete probabilistic distributions of the delays (see [Syntax](docs/syntax.md)).


## Prerequisites

- [Java](https://www.java.com/en/download/manual.jsp) must be installed to run PTSV.
- The tool executes bash scripts to run other tools below:
	- The [IF toolset](https://gricad-gitlab.univ-grenoble-alpes.fr/verimag/if/if-toolset) is required to generate discrete Timed Labeled Transition Systems (TLTSs).
	- [Python3](https://www.python.org/downloads/) with [SciPy](https://pypi.org/project/scipy/) framework must be installed for solving systems of equations.
	- The [CADP Toolbox](https://cadp.inria.fr/) needs to be installed for applying reduction on the TLTSs (and later for verification purposes).
	- [Graphviz](https://graphviz.org/download/) is used for visualization (i.e., to generate transition system PDF files).

## Running PTSV

- A JAR file, named ptsv.jar, is provided in the [target](ptsv-app/target) folder.
- The command to run it is as follows:
`````
cd ptsv-app/target
java -cp ptsv.jar com.ptsv.app.App <IF model> [<traces folder>]
`````
- The [experiments](experiments) folder contains a set of experiments for testing the tool (see their [description](experiments/README.md) for more detail).
- For instance, [test.sh](experiments/simple/test.sh) can be executed to run PTSV using the example model [simple.if](experiments/simple/simple.if).
`````
cd experiments/simple
./test.sh
`````
- The tool will return [simple-min-pts.pdf](experiments/simple/simple-min-pts.pdf), which shows the TPTS of [simple.if](experiments/simple/simple.if) according to the specified distribution.


## Building from source

- Maven is required to build from source.
- The folder [ptsv-app](ptsv-app) contains a maven project of the tool.
- To rebuild the JAR file, run the following command:
`````
cd ptsv-app
mvn clean package
`````
- The JAR file will appear in [target](ptsv-app/target) as ptsv.jar.