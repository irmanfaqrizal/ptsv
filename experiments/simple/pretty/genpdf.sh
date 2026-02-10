# dot -Tpdf simple-A-min.dot > simple-A-min.pdf
# dot -Tpdf simple-B-min.dot > simple-B-min.pdf
# dot -Tpdf simple-min.dot > simple-min.pdf
# dot -Tpdf simple-min-pts.dot > simple-min-pts.pdf
dot -Tpdf simple-min-pts-fractional.dot > simple-min-pts-fractional.pdf

# graphviz2drawio simple-A-min.dot
# graphviz2drawio simple-B-min.dot
# graphviz2drawio simple-min.dot
# graphviz2drawio simple-min-pts.dot
graphviz2drawio simple-min-pts-fractional.dot