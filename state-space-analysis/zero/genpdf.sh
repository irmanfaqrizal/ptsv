name="aebs"

dot -Tpdf $name-min-pts.dot > $name-min-pts.pdf
dot -Tpdf $name-min-simulation-pts.dot > $name-min-simulation-pts.pdf
dot -Tpdf $name-min-execution-pts.dot > $name-min-execution-pts.pdf

graphviz2drawio $name-min-pts.dot
graphviz2drawio $name-min-simulation-pts.dot
graphviz2drawio $name-min-execution-pts.dot
