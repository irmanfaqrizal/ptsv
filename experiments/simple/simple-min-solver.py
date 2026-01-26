import sympy as sp
b2, b3, a2, b4, a3, b5, a5, a6, a7, a8, a9 = sp.symbols('b2 b3 a2 b4 a3 b5 a5 a6 a7 a8 a9', real=True)
vars_all = [b2, b3, a2, b4, a3, b5, a5, a6, a7, a8, a9]
eqs = [
	sp.Eq(a6/a7, sp.Rational(1, 1)),
	sp.Eq(a5*b3*a9 + a7*a2*a9 + a5*b2, sp.Rational(1, 2)),
	sp.Eq(a5 + a6 + a7, 1),
	sp.Eq(b4 + b5, 1),
	sp.Eq(a6*b5*a8 + a5*b2*a8 + a5*b3, sp.Rational(1, 2)),
	sp.Eq(a6 + a7*a3, sp.Rational(1, 2)),
	sp.Eq(b2/b3, sp.Rational(1, 1)),
	sp.Eq(a6*b4 + a7, sp.Rational(1, 2)),
	sp.Eq(a2 + a3, 1),
	sp.Eq(a5, sp.Rational(1, 4)),
	sp.Eq(a8, 1),
	sp.Eq(a9, 1),
	sp.Eq(b2 + b3, 1)
]
sol = sp.solve(eqs, vars_all, dict=True)
print("Solutions symboliques :")
for s in sol:
	print(s)