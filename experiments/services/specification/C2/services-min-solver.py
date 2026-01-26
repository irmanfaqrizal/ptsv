import sympy as sp
import sys
v6, v7, v8, v9, v10, v21, v11, v14, v13, v2, v18, v3, v17, v4, v5, v50, v30, v52, v51, v54, v53, v34, v56, v33, v55, v36, v58, v35, v57, v38, v37, v59, v39, v61, v60, v41, v63, v43, v65, v42, v64, v45, v44, v47, v46, v68, v49, v48, v29, v28, v66, v16, v32, v23, v22, v27, v26 = sp.symbols('v6 v7 v8 v9 v10 v21 v11 v14 v13 v2 v18 v3 v17 v4 v5 v50 v30 v52 v51 v54 v53 v34 v56 v33 v55 v36 v58 v35 v57 v38 v37 v59 v39 v61 v60 v41 v63 v43 v65 v42 v64 v45 v44 v47 v46 v68 v49 v48 v29 v28 v66 v16 v32 v23 v22 v27 v26', real=True)
vars_all = [v6, v7, v8, v9, v10, v21, v11, v14, v13, v2, v18, v3, v17, v4, v5, v50, v30, v52, v51, v54, v53, v34, v56, v33, v55, v36, v58, v35, v57, v38, v37, v59, v39, v61, v60, v41, v63, v43, v65, v42, v64, v45, v44, v47, v46, v68, v49, v48, v29, v28, v66, v16, v32, v23, v22, v27, v26]
eqs = [
	sp.Eq(v17 + v18, 1),
	sp.Eq(v7 + v8, 1),
	sp.Eq(v29*v38 + v28, sp.Rational(1, 7)),
	sp.Eq(v46/v47, sp.Rational(14, 9)),
	sp.Eq(v35/v36, sp.Rational(81, 14)),
	sp.Eq(v35/v37, sp.Rational(9, 14)),
	sp.Eq(v44/v45, sp.Rational(14, 81)),
	sp.Eq(v52, 1),
	sp.Eq(v50 + v51, 1),
	sp.Eq(v54, sp.Rational(5, 9)),
	sp.Eq(v30*v35*v41*v49 + v30*v37 + v28*v33*v42 + v30*v36*v46 + v30*v36*v47*v49 + v29*v38*v43*v49 + v29*v39*v47*v49 + v28*v34*v43*v49 + v29*v39*v46 + v30*v35*v42 + v28*v33*v41*v49, sp.Rational(1, 1)),
	sp.Eq(v11, 1),
	sp.Eq(v13 + v14, 1),
	sp.Eq(v56 + v57, 1),
	sp.Eq(v29*v39*v47 + v30*v36*v47 + v30*v37*v44*v51 + v30*v36*v46*v51 + v29*v39*v46*v51 + v30*v37*v45 + v30*v35, sp.Rational(9, 14)),
	sp.Eq(v16, 1),
	sp.Eq(v28 + v29 + v30, 1),
	sp.Eq(v3*v8*v11 + v4*v5*v11 + v3*v7, sp.Rational(1, 9)),
	sp.Eq(v3*v8 + v3*v7*v13 + v2*v10*v13, sp.Rational(9, 14)),
	sp.Eq(v35 + v36 + v37, 1),
	sp.Eq(v3*v7*v14*v18 + v2*v10*v14*v18, sp.Rational(1, 14)),
	sp.Eq(v30*v36*v46*v50*v55 + v30*v36*v47*v49*v52*v57 + v30*v35*v42*v48*v52*v57 + v29*v39*v46*v51*v52*v57 + v28*v33*v42*v48*v52*v57 + v29*v39*v47*v49*v52*v57 + v30*v37*v45*v48*v52*v57 + v30*v36*v46*v50*v53*v57 + v28*v34*v43*v49*v52*v57 + v30*v35*v41*v49*v52*v57 + v30*v37*v44*v51*v52*v57 + v28*v33*v41*v49*v52*v57 + v30*v36*v46*v51*v52*v57 + v29*v39*v46*v50*v55 + v30*v37*v44*v50*v53*v57 + v30*v37*v44*v50*v55 + v29*v38*v43*v49*v52*v57 + v29*v39*v46*v50*v53*v57, sp.Rational(1, 6)),
	sp.Eq(v2 + v4*v6, sp.Rational(8, 9)),
	sp.Eq(v38 + v39, 1),
	sp.Eq(v21, 1),
	sp.Eq(v65, 1),
	sp.Eq(v28*v34*v43*v49*v52*v56*v63 + v29*v38*v43*v49*v52*v56*v63 + v28*v33*v41*v49*v52*v56*v63 + v30*v35*v41*v49*v52*v56*v63 + v29*v39*v46*v50*v54*v61 + v30*v37*v45*v48*v52*v56*v63 + v29*v39*v46*v50*v54*v60*v63 + v28*v33*v42*v48*v52*v56*v63 + v30*v36*v46*v50*v54*v61 + v30*v36*v46*v50*v54*v60*v63 + v30*v37*v44*v50*v54*v61 + v30*v37*v44*v51*v52*v56*v63 + v30*v37*v44*v50*v53*v56*v63 + v30*v36*v46*v51*v52*v56*v63 + v29*v39*v46*v51*v52*v56*v63 + v29*v39*v47*v49*v52*v56*v63 + v30*v35*v42*v48*v52*v56*v63 + v29*v39*v46*v50*v53*v56*v63 + v30*v36*v46*v50*v53*v56*v63 + v30*v36*v47*v49*v52*v56*v63 + v30*v37*v44*v50*v54*v60*v63, sp.Rational(2, 3)),
	sp.Eq(v23, sp.Rational(1, 6)),
	sp.Eq(v66, 1),
	sp.Eq(v68, 1),
	sp.Eq(v30*v36*v46*v50*v54*v60 + v29*v39*v46*v50*v54*v61*v65 + v30*v36*v46*v50*v55*v58*v65 + v30*v37*v44*v50*v55*v58*v65 + v29*v39*v46*v50*v54*v60 + v29*v39*v46*v50*v55*v58*v65 + v30*v36*v46*v50*v54*v61*v65 + v30*v37*v44*v50*v54*v61*v65 + v30*v37*v44*v50*v54*v60, sp.Rational(1, 7)),
	sp.Eq(v46 + v47, 1),
	sp.Eq(v2*v9 + v4, sp.Rational(1, 7)),
	sp.Eq(v22*v26, sp.Rational(2, 3)),
	sp.Eq(v29*v39*v46*v50*v53*v56*v64*v68 + v30*v37*v44*v50*v54*v60*v64*v68 + v30*v37*v44*v51*v52*v56*v64*v68 + v28*v33*v42*v48*v52*v56*v64*v68 + v29*v38*v43*v49*v52*v56*v64*v68 + v28*v34*v43*v49*v52*v56*v64*v68 + v30*v36*v46*v50*v53*v56*v64*v68 + v30*v35*v41*v49*v52*v56*v64*v68 + v30*v36*v47*v49*v52*v56*v64*v68 + v28*v33*v41*v49*v52*v56*v64*v68 + v29*v39*v46*v50*v54*v60*v64*v68 + v29*v39*v47*v49*v52*v56*v64*v68 + v30*v36*v46*v50*v54*v60*v64*v68 + v30*v37*v44*v50*v53*v56*v64*v68 + v30*v35*v42*v48*v52*v56*v64*v68 + v30*v36*v46*v51*v52*v56*v64*v68 + v30*v37*v45*v48*v52*v56*v64*v68 + v29*v39*v46*v51*v52*v56*v64*v68, sp.Rational(1, 6)),
	sp.Eq(v7/v8, sp.Rational(14, 81)),
	sp.Eq(v44 + v45, 1),
	sp.Eq(v28*v34 + v29, sp.Rational(8, 9)),
	sp.Eq(v58 + v59, 1),
	sp.Eq(v60/v61, sp.Rational(3, 14)),
	sp.Eq(v30, sp.Rational(2, 21)),
	sp.Eq(v9 + v10, 1),
	sp.Eq(v32, 1),
	sp.Eq(v33 + v34, 1),
	sp.Eq(v28*v33*v42*v48 + v28*v33*v41 + v30*v35*v42*v48 + v30*v37*v45*v48 + v30*v36 + v30*v35*v41 + v30*v37*v44, sp.Rational(1, 9)),
	sp.Eq(v22 + v23, 1),
	sp.Eq(v2 + v3 + v4, 1),
	sp.Eq(v63 + v64, 1),
	sp.Eq(v30*v36*v46*v50*v55*v59 + v30*v36*v46*v50*v53 + v30*v37*v44*v50*v55*v59 + v30*v37*v44*v50*v53 + v29*v39*v46*v50*v53 + v29*v39*v46*v50*v55*v59, sp.Rational(1, 14)),
	sp.Eq(v2/v4, sp.Rational(56, 9)),
	sp.Eq(v3*v7*v14*v17*v21 + v2*v10*v14*v17*v21, sp.Rational(1, 7)),
	sp.Eq(v22*v27*v32, sp.Rational(1, 6)),
	sp.Eq(v53/v55, sp.Rational(3, 7)),
	sp.Eq(v41 + v42, 1),
	sp.Eq(v5 + v6, 1),
	sp.Eq(v60 + v61, 1),
	sp.Eq(v43, 1),
	sp.Eq(v28/v29, sp.Rational(9, 56)),
	sp.Eq(v49, 1),
	sp.Eq(v48, 1),
	sp.Eq(v41/v42, sp.Rational(1, 9)),
	sp.Eq(v3, sp.Rational(2, 21)),
	sp.Eq(v53 + v54 + v55, 1),
	sp.Eq(v26 + v27, 1)
]
sol = sp.solve(eqs, vars_all, dict=True)
print("Solution:")
for s in sol:
	print(s)
text_file = open(sys.argv[1] + "-sympy.txt", "w")
for s in sol:
    for key, value in s.items():
        text_file.write(str(key) + ":" + str(value) + "\n")
text_file.close()