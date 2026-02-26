# State-space analysis

This folder contains a tool for automating the state-space analysis.
In particular, the tool can extract some paths from a given source TPTS that end with specific probabilities, then use those paths to extract other paths with same sequences of actions from a target TPTS.
Graphviz is the only external tool required to run this tool.

The tool can be executed with the following parameters:

`````
java -cp statespace.jar statespace <source TPTS> <min, max probabilities> <max paths> <target TPTS>
`````

For example, in [experiments](experiments), our objective is to extract 5 paths in [tpts-execution](experiments/tpts-execution.aut) that end with 0 probability transitions and use those paths to extract other paths in [tpts-golden](experiments/tpts-golden.aut) that have the same sequences.
The following command can be executed to achieve the objective:

`````
cd experiments
java -cp statespace.jar statespace tpts-execution 0,0 5 tpts-golden
`````

The paths from both source and target TPTSs are stored in [results](experiments/results/).
The file indexes indicate the correspondence between the paths.
For instance, [tpts-execution-1.pdf](experiments/results/tpts-execution-1.pdf) is path ending with 0 probability from a source TPTS and [tpts-golden-1.pdf](experiments/results/tpts-golden-1.pdf) is the corresponding path from the target TPTS.