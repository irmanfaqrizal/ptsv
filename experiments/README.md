# Experiments

- This folder contains a set of experiments, and each of them is designed for a specific purpose.

## Performance

- This experiment is designed to evaluate the performance of the tool.
- The evaluation is done by defining networks of DSTA with the same structure but with different numbers of automaton and delay lengths; a set of 9 configurations are created (from [c1](performance/c1/) to [c9](performance/c9/), with increasing complexity).
- The [script](performance/test.sh) in the folder can be executed to perform the evaluation, and the [log](performance/log.txt) file contains an output example from a previously executed evaluation.

## AEBS
- The objective of this experiment is to demonstrate an analysis method where TPTSs generated according to delay distributions are compared with the ones computed using trace injections; the experiment consists of (i) analysis of response time probabilities and (ii) state-space analysis.
- [Simpy](https://simpy.readthedocs.io/) must be installed to perform the simulations.
- A network of DSTA describing an AEBS application with three components is used for the experiment.
- Two configurations ([C1](aebs/specification/C1/aebs.if) and [C2](aebs/specification/C2/aebs.if)) are defined for the experiment.

#### Analysis of response time probabilities
- For this specific analysis, [PRISM](https://www.prismmodelchecker.org/) is required to compute steady-state probabilities; the tool executable must be set in an environment variable $prism, such that `echo $prism` returns `<path-to-installation>/bin/prism`.
- This analysis is done by performing model checking on the generated TPTSs; for this, the input specification is annotated with sequences of actions, in which we want to query the probabilities of time elapsing in between them (e.g., see line 75 in [C1](aebs/specification/C1/aebs.if)).
- The [script](aebs/test.sh) in the folder can be executed to perform the complete analysis (see [log](aebs/log.txt) for analysis steps), it will produce a graph shown in [result.pdf](aebs/result.pdf).

#### State-space analysis
- The script [checkzero.sh](aebs/checkzero.sh) can be executed to return sequences leading to 0 probabilities in simulated TPTS (according to configuration C1 in this example).
- This requires the simulated TPTS to be first generated (e.g., by running [test.sh](aebs/test.sh)).

## Simple
- This experiment contains a [simple](simple/simple.if) example of a DSTA network consisting of two automata with different periodicites and delays.
- The [PDF](simple/simple-min-pts.pdf) file shows the expected output when PTSV is executed with the example as input.
