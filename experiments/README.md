# Experiments

- This folder contains a set of experiments, and each of them is designed for a specific purpose.
<!-- - Here, we explain the experiment objectives, how to run them, and what are the expected results. -->

## Performance

- This experiment is designed to evaluate the performance of the tool.
- The evaluation is done by defining networks of DSTA with the same structure but with different numbers of automaton and delay lengths; a set of 9 configurations are created (from [c1](performance/c1/) to [c9](performance/c9/), with increasing complexity).
- The [script](performance/test.sh) in the folder can be executed to perform the evaluation, and the [log](performance/log.txt) file contains an output example from a previously executed evaluation.

## Services
- The objective of this experiment is to demonstrate an analysis method where TPTSs generated according to delay distributions are compared with the ones computed using trace injections; the experiment consists of (i) analysis of response time probabilities and (ii) state-space analysis.
- A network of DSTA describing an AEBS application with three components is used for the experiment.
- Two configurations ([C1](services/specification/C1/services.if) and [C2](services/specification/C2/services.if)) are defined for the experiment.

#### Analysis of response time probabilities
- For this specific analysis, [PRISM](https://www.prismmodelchecker.org/) is required to compute steady-state probabilities; the tool executable must be set in an environment variable $prism, such that `echo $prism` returns `<path-to-installation>/bin/prism`.
- This analysis is done by performing model checking on the generated TPTSs; for this, the input specification is annotated with sequences of actions, in which we want to query the probabilities of time elapsing in between them (e.g., see line 75 in [C1](services/specification/C1/services.if)).
- The [script](services/test.sh) in the folder can be executed to perform the complete analysis.
<!-- - The following sequence of operations explains how the analysis is performed 
    - The TPTSs according to the configurations (i.e., [C1](services/specification/C1/services.if) and [C2](services/specification/C2/services.if)) are first generated; this results in two <i>golden</i> TPTSs.
    - In addition, the tool also returns a DTMC for each configuration, which is systematically taken as input by PRISM for computing steady-state probabilities.
    -  -->

#### State-space analysis
- The script [checkzero.sh](services/checkzero.sh) can be executed to return sequences leading to 0 probabilities in simulated TPTS (according to configuration C1 in this example).
- This requires the simulated TPTS to be first generated (e.g., by running [test.sh](services/test.sh)).

## Simple
- This experiment contains a [simple](simple/simple.if) example of a DSTA network consisting of two automata with different periodicites and delays.
- The [PDF](simple/simple-min-pts.pdf) file shows the expected output when PTSV is executed with the example as input.
