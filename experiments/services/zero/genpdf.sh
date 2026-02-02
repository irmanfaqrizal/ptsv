dot -Tpdf services-min-pts.dot > services-min-pts.pdf
dot -Tpdf services-min-simulation-pts.dot > services-min-simulation-pts.pdf
dot -Tpdf services-min-execution-pts.dot > services-min-execution-pts.pdf

graphviz2drawio services-min-pts.dot
graphviz2drawio services-min-simulation-pts.dot
graphviz2drawio services-min-execution-pts.dot
