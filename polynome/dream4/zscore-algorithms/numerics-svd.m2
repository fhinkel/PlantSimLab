-- This file is mes-svd.m2 cleaned up
-- in ~/local/dream4/dream3-code-data/

-- Create the Q matrix from a list of points and a list of monomials
-- Several entities are stored in the cache table of Q.

needsPackage "PolynomialDynamicalSystems"
needs "numerics.m2"

debug Core

PointTransformMatrix = new Type of MutableHashTable

addMonomial = method()
addMonomial(PointTransformMatrix, RingElement) := (Q, monom) -> (
     -- adds monom if it is not already there
     -- returns the column index of that monomial
     if not Q.QH#?monom then (
     	  s := (support monom)#0;
     	  col1 := Q.QH#s;
	  R := ring monom;
	  e2 := first exponents monom - first exponents s;
	  monom2 := R_e2;  -- this is monom//s, but that is currently not defined over RR
     	  col2 := addMonomial(Q,monom2);
     	  nrows := numRows Q.Q;
     	  i := Q.QH#monom = Q.nextval;
	  Q.nextval = Q.nextval + 1;	  
     	  for j from 0 to nrows-1 do Q.Q_(j,i) = Q.Q_(j,col1) * Q.Q_(j,col2);
     	  );
     Q.QH#monom
     )

makeQ = method()
makeQ(Matrix, Matrix, ZZ) := (pts,monoms,sz) -> (
     -- The matrix is created over 'ring pts'
     -- The result is a mutable matrix
     R := ring monoms;
     result := new PointTransformMatrix;
     nrows := numgens target pts;
     ncols := numgens source pts;
     Q1 := matrix toList(nrows:{1.0});
     Q2 := pts;
     Q3 := map(target Q2, RR^(sz-1-(numgens source Q2)), 0);
     result.Q = mutableMatrix(Q1|Q2|Q3);
     result.QH = new MutableHashTable;
     result.QH#(1_R) = 0;
     for i from 1 to ncols do result.QH#(R_(i-1)) = i;
     result.nextval = ncols+1;
     apply(flatten entries sort monoms, m -> addMonomial(result,m));
     result
     )

makeFunction = method()
makeFunction(TimeSeriesData, ZZ) := (TS,i) -> (
     -- i is from 1 to num columns of the matrices (but don't use this for perturbation columns)
     -- returns a pair: all of the input points (one row each), except the last in each time series
     --                 all of the output values, as one column vector.
     apply(TS.WildType, m -> {submatrix(m, 0..numRows m-2,), submatrix(m, 1..numRows m - 1, {i-1})})
     )

maxRelativeError = (v1,v2, tolerance) -> (
     M := apply(#v1, i -> (a := abs(v2#i-v1#i);
	       if a < tolerance then 0.0 else a/v2#i));
     m := max M;
     (m, positions(M, x -> x >= m))
     )

len = (u) -> (
     u = flatten entries u;
     x := 0.0;
     scan(u, u0 -> x = x + u0*u0);
     sqrt(x)
     )

svdFcn = (Q,T,monoms) -> (
     -- Input: Q: PointTransformMatrix, usually created with makeQ
     --   T: a FunctionData
     --   monoms: a one row matrix of monomials
     -- Output: (error, maxerror, soln, fcnvals, Q1)
     --   soln is a matrix, coefficients of monomials
     --     such that: values of the resulting fcn at the keys of T are closest
     --     in l2 norm to actual values of T.
     --   error: is the l2norm of the resulting approx to the actual
     --   maxerror: max relative error: how far off, as a fraction of actual vals is the computed val?
     --   fcnvals: is the approx values, actual values next to each other
     --   Q1: is the submatrix of the (values of each monom at each of the points: keys T)
     pts := keys T;
     vals := transpose matrix {apply(pts, x -> T#x)};
     cols := apply(flatten entries monoms, m -> addMonomial(Q,m));
     << "cols = " << cols << endl;
     x := mutableMatrix(RR,#cols,1);
     Q1 := map(RR_53,rawSubmatrix(raw Q.Q,0..#pts-1,cols));
     rawLeastSquares(raw Q1,raw mutableMatrix vals, raw x,false);
     x = matrix x;
     F := (monoms * x)_(0,0);
     v1 := (matrix Q1)*x;
     v2 := vals;
     (len(v1-v2), (maxRelativeError(flatten entries v1,flatten entries v2,.000001))#0, x, new MutableList from entries(v1|v2), Q1)
     )

svdFcnRelative = (Q,T,monoms) -> (
     tolerance := .0000001;
     pts := keys T;
     vals0 := apply(pts, x -> T#x);
     cols := apply(flatten entries monoms, m -> addMonomial(Q,m));
     Q1 := map(RR_53,rawSubmatrix(raw Q.Q,0..#pts-1,cols));
     Qorig := matrix Q1;
     valsOrig := transpose matrix{vals0};
     vals1 := for i from 0 to #vals0-1 list (
	  x := vals0#i;
	  if x < tolerance then (
	       rowMult(Q1,i,0.0);
	       0.0
	       )
	  else (
	       rowMult(Q1,i,1.0/x);
	       1.0
	       )
	  );
     vals := transpose matrix {vals1};
     x := mutableMatrix(RR,#cols,1);
     rawLeastSquares(raw Q1,raw mutableMatrix vals, raw x,false);
     x = matrix x;
     F := (monoms * x)_(0,0);
     v1 := Qorig*x;
     v2 := valsOrig;
     (len(v1-v2), (maxRelativeError(flatten entries v1,flatten entries v2,.000001))#0, x, new MutableList from entries(v1|v2), Q1)
     )

svdF = method()
svdF(List, List) := (Fs, monoms) -> (
     -- input: Fs is a list of matrices {pts,vals}
     --        monoms is a list of monomials in a ring R, with
     --          the number of vars same as the number of cols in each matrix 'pts'
     -- output:
     --   a. the sum of squares error (sqrt of (squares of differences between vals and valsApprox))
     --   b. the maximum error in any entry.
     --   c. a list of vectors valsApprox
     --   d. solution: column vector x s.t. the function is monoms * x
     --   e. matrix Q1, used to compute x.
     --
     -- Step 1: make the Q1 matrix
     R := ring first monoms;
     pts := matrix apply(Fs, m -> {first m});
     vals := matrix apply(Fs, m -> {last m});
     Q := makeQ(pts, vars R, 1000);
     cols := apply(monoms, m -> addMonomial(Q,m));
     Q1 := map(RR_53,rawSubmatrix(raw Q.Q,0..numRows pts - 1,cols));
     -- Step 2: solve it via least squares
     x := mutableMatrix(RR,#cols,1);
     Qorig := matrix Q1;
     rawLeastSquares(raw Q1,raw mutableMatrix vals, raw x,false);
     x = matrix x;
     F := (matrix {monoms} * x)_(0,0);
     valsApprox := Qorig * x;
     -- now we need to split valsApprox to match Fs/last
     firstval := 0;
     vals1 := apply(Fs, m -> (
	       n := numRows m#1; 
	       result := submatrix(valsApprox, firstval .. firstval + n - 1, {0}); 
	       firstval = firstval + n; 
	       result));
     -- Step 3: compute the mentioned errors
     (len(vals-valsApprox), 
	  first maxRelativeError(flatten entries valsApprox, flatten entries vals, .000001), 
	  for i from 0 to #vals-1 list {vals1_i, last Fs_i},
	  F)
     )

squarefrees = (L, deg) -> apply(subsets(L, deg), product)
     -- return the list of all squarefree monomials in L of the given degree

doSubsets = (Q,T,pow,sz,varlist,base) -> (
     -- T is a FunctionData
     -- sz is the size of the set of all subsets to use
     -- varlist is a list of variables
     -- base is an ideal with the monomials to use for every one
     --  (usually 1_R, xi).
     minlen := infinity;
     minset := null;
     minmaxerr := infinity;
     minseterr := null;
     R := ring base;
     S := subsets(varlist, sz);
     ndone := 0;
     scan(S, x -> (
	       J := gens ((base + ideal x)^pow);
	       (len0,maxerr,f,vals) := svdFcnRelative(Q,T,J);
	       if len0 < minlen then (
		    << "minlen = " << len0 << " subset " << x << endl;
		    << "  poly " << net f <<endl;
		    minlen = len0;
		    minset = x;
		    );
	       if maxerr < minmaxerr then (
		    << "minerr = " << maxerr << " subset " << x << endl;
		    << "  poly " << net f <<endl;
		    minmaxerr = maxerr;
		    minseterr = x;
		    );
	       ndone = ndone + 1;
	       if ndone % 100 == 0 then
	         << "done " << ndone << endl;
	       ));
     (minlen,minset)
     )

doSubsetsKeep = (Q,T,pow,sz,varlist,base,thresh) -> (
     -- T is a FunctioData
     -- sz is the set of all subsets to use
     -- varlist is a list of variables
     -- base is an ideal with the monomials to use for every one
     --  (usually 1_R, xi).
     -- thresh: keep in a list all sets with RMS <= thresh
     result := {};
     minlen := infinity;
     minset := null;
     minmaxerr := infinity;
     minseterr := null;
     R := ring base;
     S := subsets(varlist, sz);
     ndone := 0;
     scan(S, x -> (
	       J := gens ((base + ideal x)^pow);
	       (len0,maxerr,f,vals) := svdFcnRelative(Q,T, J);
	       if maxerr < thresh then (
		    result = append(result,{maxerr,len0,x,toString((J * f)_(0,0)), vals});
		    );
	       if len0 < minlen then (
		    << "minlen = " << len0 << " subset " << x << endl;
		    << "  poly " << net f <<endl;
		    minlen = len0;
		    minset = x;
		    );
	       if maxerr < minmaxerr then (
		    << "minerr = " << maxerr << " subset " << x << endl;
		    << "  poly " << net f <<endl;
		    minmaxerr = maxerr;
		    minseterr = x;
		    );
	       ndone = ndone + 1;
	       if ndone % 100 == 0 then
	         << "done " << ndone << endl;
	       ));
     rsort result
     )

createDreamTimeSeries = (H) -> (
     -- H is the result of reading the time series data
     -- result: FunctionData which has the k perturbations (if k different time series)
     --   added in
     --
     perturbo := (k,p) -> (
     	  -- create a 21 by k matrix.  The p-th column has 1's in spots 0..9.
	  M := mutableMatrix(RR,21,k);
	  for i from 0 to 9 do M_(i,p) = 1.0;
	  matrix M
	  );
     -- add in the perturbations:
     k := #H#"timeseries";
     mats := toList apply(k, p -> (
	       m := H#"timeseries"#p;
	       m | perturbo(k,p)
	       ));
     new TimeSeriesData from hashTable {WildType => mats}
     )

createZDreamTimeSeries = (H) -> (
     -- H is the result of reading the time series data
     -- result: FunctionData which has the k perturbations (if k different time series)
     --   added in
     --
     (MU,Zs) = getInfo H;
     ZH = Zscores(H, MU);
     perturbo := (k,p) -> (
     	  -- create a 21 by k matrix.  The p-th column has 1's in spots 0..9.
	  M := mutableMatrix(RR,21,k);
	  for i from 0 to 9 do M_(i,p) = 1.0;
	  matrix M
	  );
     -- add in the perturbations:
     k := #ZH;
     mats := toList apply(k, p -> (
	       m := ZH#p;
	       m | perturbo(k,p)
	       ));
     new TimeSeriesData from hashTable {WildType => mats}
     )

toSage = (v1v2) -> (
     --v1v2 should be a list of two one column matrices.
     v1 := flatten entries v1v2#0;
     v2 := flatten entries v1v2#1;
     a1 := new Array from for i from 0 to #v1-1 list (i,v1#i);
     a2 := new Array from for i from 0 to #v2-1 list (i,v2#i);
     "v1 = " | toString a1 | "\nv2 = " | toString a2 | "\nline(v1,rgbcolor=(1,0,0)) + line(v2,rgbcolor=(0,0,1))\n"
     )
end
restart
load "numerics-svd.m2"
R = RR[a,b,c]
pts = matrix{{2.1,2.3,2.4},{1.1,1.3,1.7},{1.1,1.2,1.4},{1.5,6.7,6.9}}
Q = makeQ(matrix pts, matrix"a,b,c", 10)
peek oo

T = hashTable {{2.1,2.3,2.4} => 1.3, {1.1,1.3,1.7} => 2.7, {1.1,1.2,1.4} => 3.3, {1.5,6.7,6.9} => 1.0}
svdFcn(Q,T, matrix"1,a,b,c,a2,ab,ac,b2,bc,b2")

-- Let's try it on the data from 10-1:
restart
load "numerics-svd.m2"
load "dream4.m2"
H = readDream4(insilicoNames#"10-1")
ZH = createZDreamTimeSeries H  -- this contains the 5 columns of perturbation data, and entries in first 10 cols are z-scores
F5 = makeFunction(ZH,5)
pts5 = matrix(F5/(x -> {first x}))
vals5 = matrix(F5/(x -> {last x}))

R = RR[makeVars 15]
svdF(F5, {1_R, x1, x2, x3, x4})


(MU,Zs) = getInfo H
TS = new TimeSeriesData from {WildType => H#"timeseries"}
Fs = apply(1..10, i -> functionData(TS,i))
R = RR[makeVars 10]
Q = makeQ(matrix keys Fs#0, matrix{{x1,x2,x3,x4,x5,x6,x7,x8,x9,x10}}, 21)
svdFcn(Q,Fs#0,matrix{{1_R,x1,x2,x3,x4,x5,x6,x7,x8,x9,x10}} )
doSubsets(Q,Fs#0,2,3,{x1,x2,x3,x4,x5},ideal(0_R))


svdFcn(Q,Fs#0, (gens (ideal(1_R,x1,x7,x8))^2))
(gens (ideal(1_R,x1,x7,x8))^2)
Q = makeQ(matrix keys Fs#0, (gens (ideal(x1,x7,x8))^2), 15)

-- plan now: 
-- (1) create the correct function data sets.
--   this should include: "delta" perturbation functions: [1,1,...,1,0,...,0]
-- (2) then make Q matrix
-- (3) then pick out the correct part, and solve

Fcn = functionData(createDreamTimeSeries(H),1)
R = RR[makeVars 15]
Q = makeQ(matrix keys Fcn, vars R, 500)
-- we will do the following with many different sets of monomials
-- what do the return values really mean?
svdFcn(Q,Fcn,matrix{{1_R,x1,x2,x3,x4,x11}})
svdFcn(Q,Fcn,gens (ideal(1_R,x1,x2,x3,x4,x11))^2)
svdFcnRelative(Q,Fcn,matrix{{1_R,x1,x2,x3,x4,x11}})


sq = squarefrees({1_R,x1,x2,x3,x11},2)
svdFcn(Q,Fcn,matrix{sq})

-- o17 is the function created by svdFcn
apply((createDreamTimeSeries H)#WildType, m -> matrix{apply(entries m , v -> sub(o17,matrix{v}))})

----------------------------------------------
-- Let's try svdF on the data from 10-1:
restart
load "numerics-svd.m2"
load "dream4.m2"
H = readDream4(insilicoNames#"10-1")
ZH = createZDreamTimeSeries H  -- this contains the 5 columns of perturbation data, and entries in first 10 cols are z-scores

F5 = makeFunction(ZH,5)
pts5 = matrix(F5/(x -> {first x}))
vals5 = matrix(F5/(x -> {last x}))
R = RR[makeVars 15]
svdF(F5, {1_R, x1, x2, x3, x4})
for i from 0 to 4 do print toSage(oo_2_i)
try1 = sort apply(subsets(prepend(1_R, gens R), 3), vs -> (
	  (err1, err2, vals, xfcn) =  svdF(F5,vs);
	  << vs << ": " << err1 << endl;
	  {err1, err2, vs}
	  ))

try2 = sort apply(subsets({x1,x2,x3,x4,x6,x7,x8,x9,x10}, 3), vs -> (
	  vs1 = join({1_R,x5,x11,x15},vs);
	  vs1 = squarefrees(vs1,2);
	  vs2 := apply(vs, x -> x^2);
	  vs3 := apply(vs, x -> x^3);
	  vs = join(vs1,vs2,vs3);
	  vs = join(vs1,vs2);
	  (err1, err2, vals, xfcn) =  svdF(F5,vs);
	  << vs << ": " << err1 << endl;
	  {err1, err2, vs}
	  ))
try2/first//min
try2/(x -> x#1)//min
netList try2

try3 = sort apply(subsets({x1,x2,x3,x4,x6,x7,x8,x9,x10}, 2), vs -> (
	  vs1 := join({1_R,x5},vs);
	  vss := (((ideal vs1)^7) * ideal(1_R,x11))_*;
	  --vs1 := join({1_R,x5,x11},vs);
	  --vs1 = squarefrees(vs1,2);
	  --vs2 := apply(vs, x -> x^2);
	  --vs3 := apply(vs, x -> x^3);
	  --vs4 := apply(vs, x -> x^4);
	  --vss = join(vs1,vs2,vs3,vs4);
	  (err1, err2, vals, xfcn) =  svdF(F5,vss);
	  << vs << ": " << err1 << endl;
	  {err1, err2, vs}
	  ))
try3/first//min
try3/(x -> x#1)//min
netList try3

select(try2, x -> first x <= 3.5)
select(try2, x -> x#1 <= 2.0)
svdF(F5,{x1,x3,x9,x1^2,x3^2,x9^2})
ideal(x1,x3,x9,x1^2,x3^2,x9^2) * ideal(1_R,x5,x11)
svdF(F5,oo_*)

-- attempt to cluster points?  maybe we can do minsets that way?  9-24-09
apply(subsets(keys subFunctionData(functionData(ZH,2),{1,2,3}), 2), x -> len matrix{x#1-x#0})