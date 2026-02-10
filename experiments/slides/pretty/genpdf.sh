name=slides-min-pts-fractional
dot -Tpdf $name.dot > $name.pdf
graphviz2drawio $name.dot
