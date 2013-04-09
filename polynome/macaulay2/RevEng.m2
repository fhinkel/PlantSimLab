newPackage(
    "RevEng",
        Version => "0.4", 
        Date => "December 20, 2006",
        Authors => {
         {Name => "Brandy Stigler", Email => "bstigler@mbi.osu.edu", HomePage => "http://users.mbi.ohio-state.edu/bstigler"},
         {Name => "Mike Stillman", Email => "mike@math.cornell.edu", HomePage => "http://www.math.cornell.edu/~mike"}
         },
        Headline => "Utilities for reverse engineering polynomial dynamical systems",
        DebuggingMode => true
        )

needs "Points.m2"

export{makeVars,
       TimeSeriesData, 
       FunctionData, 
       readTSData,
       functionData,
       subFunctionData,
       minRep,
       minSets,
       findFunction,
       checkFunction,
       Ws,
       minSetScoring,
       minSetWeightedScoring,
       outDegrees,
       makeDotFile,
       Avoid,
       WildType,
       Executable,
       Display,
       Output,
       mesdual, -- not to be left here
       mesintersect -- not to be left here
      }

debug Core
mesintersect = (L) -> (
     -- L is a list of monomial ideals
    M := L#0;
    R := ring M;
    i := 1;
    << #L << " generators" << endl;
    while i < #L do (
     << "doing " << i << endl;
     M = newMonomialIdeal(R, rawIntersect(raw M, raw L#i));
     i = i+1;
     );
    M)

mesdual = method()

mesdual MonomialIdeal := (J) -> (if J == 0 
  then monomialIdeal 1_(ring J) 
  else if ideal J == 1 then monomialIdeal 0_(ring J)
  else mesintersect (monomialIdeal @@ support \ first entries generators J))

---------------------------------------------------------------------------------------------------
-- Declaration of new data types
---------------------------------------------------------------------------------------------------

---------------------------------------------------------------------------------------------------
-- TimeSeriesData: This hashtable stores time series data with values in a set k.
-- The data is (txn)-dimensional, where t=#timepoints-1, n=#variables.
-- keys   = time series labels
-- values = time series
--    key = Wildtype:    value = list of (txn)-matrices of wildtype time series
--    key = (i, file_i): value = list of (txn)-matrices of time series for ith knockout 

TimeSeriesData = new Type of HashTable
 
---------------------------------------------------------------------------------------------------
-- FunctionData: This hashtable defines a function f:k^n->k, where k is a finite field.
-- keys   = points in k^n (in domain of f)
-- values = points in k (in codomain of f)

FunctionData = new Type of HashTable


---------------------------------------------------------------------------------------------------
-- Utilities for working with polynomial dynamical systems
---------------------------------------------------------------------------------------------------

---------------------------------------------------------------------------------------------------
-- Given an integer n, makeVars returns a list of n variables of type xi.

makeVars = method(TypicalValue => List)
makeVars(ZZ) := (n) -> apply(1..n, i -> value ("x"|i))

---------------------------------------------------------------------------------------------------
-- Given an element of a polynomial ring, getVars returns the list of variables in the polynomial.

getVars = method(TypicalValue => List)
getVars(ZZ) := (n) -> ({})
getVars(RingElement) := (f) -> (
    -- standard form of the monomials of f, i.e., no coefficients
    SF := apply(terms f, m -> first keys standardForm m);
    Vars := {};
    select(SF, h->if keys h!={} then Vars=append(Vars, keys h));
    Vars = sort unique flatten Vars;
    apply(Vars, e->e+1)
)

---------------------------------------------------------------------------------------------------
-- Given a list of elements, see prints out each element on a single line, followed by a hard return.

see = method()
see(List) := (fs) -> scan(fs, (g -> (print g; print "")))


---------------------------------------------------------------------------------------------------
-- Utilities for data processing
---------------------------------------------------------------------------------------------------

---------------------------------------------------------------------------------------------------
-- Internal to "readTSData"
-- Given a data file and a coefficient ring, readMat returns the (txn)-matrix of the data (t=time, n=vars). 

toMat = (S,R) -> (
     -- S is a list of strings
     matrix(R, apply(S, s -> (t := separateRegexp(" +", s); 
                 t = apply(t,value);
                     select(t, x -> class x =!= Nothing)))))

readMat = method(TypicalValue => Matrix)
readMat(String,Ring) := (filename,R) -> (
     ss := select(lines get filename, s -> length s > 0);
     toMat(ss,R))

---------------------------------------------------------------------------------------------------
-- Given a list of wildtype and a list of knockout time series data files, as well as a coefficient ring,
-- readTSData returns a TimeSeriesData hashtable of the data.
-- Uses "readMat"

readTSData = method(TypicalValue => TimeSeriesData)
readTSData(List,List,Ring) := (wtfiles, knockouts, R) -> (
     -- wtfiles: list of file names for wild type data series
     -- knockouts: list of pairs (i,filename), where
     --  i is an integer with which node gets knocked out (first variable has index 1).
     --  filename is the corresponding time series data
     -- output: TimeSeriesData

     wtmats := apply(wtfiles, s -> readMat(s,R));
     H := new MutableHashTable;
     scan(knockouts, x -> (
           m := readMat(x#1,R);
           i := x#0;
           if H#?i then H#i = append(H#i,m)
           else H#i = {m}));
     H.WildType = wtmats;
     new TimeSeriesData from H
)

getMats = (S,R) -> (
     -- returns a triple: (typ,mat,S)
     -- where typ is WildType, or an integer
     --  mat is a matrix over R
     -- and S is the orgininal S with elements removed
     seps := positions(S, s -> match("##.*",s));
     apply(#seps - 1, i -> (
      a := seps#i; 
      b := seps#(i+1)-1;
      label := if match("## wildtype ##", S#a) then WildType
      else (
           x := regex("## +knockout +([[:digit:]]+) +##", S#a);
           if #x < 2 then "knockout ??" else value substring(x#1#0,x#1#1,S#a)
           );
      (label, toMat(S_(toList(a+1..b)),R))))
     )

readTSData(String,Ring) := (filename, R) -> (
     -- filename should contain lines:
     -- ## wildtype ## comment
     -- ## knockout <nn> ## comment
     -- a line of numbers
     -- blank lines (which are ignored)
     -- a TimeSeriesData is returned
     -- all lines of numbers must be the same length
     S := lines get filename;
     S = select(S, s -> length s > 0);
     L := getMats(S,R);
     H := new MutableHashTable;
     scan(L, x -> (
      (typ,mat) := x;
      if H#?typ then H#typ = append(H#typ, mat) else H#typ = {mat}));
     new TimeSeriesData from H
)

TimeSeriesData + TimeSeriesData := (A,B) -> merge(A,B,join)

---------------------------------------------------------------------------------------------------
-- Given time series data and an integer i, functionData returns the FunctionData hashtable for function i,
-- that is the input-output (vector-scalar) data pairs corresponding to node i, if consistent; 
-- else it returns an error statement.

functionData = method(TypicalValue => FunctionData)
functionData(TimeSeriesData, ZZ) := (tsdata,v) -> (
     H := new MutableHashTable;

     -- first make the list of matrices

     mats := {};
     scan(keys tsdata, x -> if class x =!= ZZ or x =!= v then mats = join(mats,tsdata#x));

     -- now make the hash table
     scan(mats, m -> (
           e := entries m;
           for j from 0 to #e-2 do (
            tj := e#j;
            val := e#(j+1)#(v-1);
            if H#?tj and H#tj != val then
              error ("function inconsistent: point " | 
                   toString tj| " has images "|toString H#tj|
                   " and "|toString val);           
            H#tj = val;
            )));
     new FunctionData from H
)

---------------------------------------------------------------------------------------------------
-- Given function data (data for a function) and a list L of integers between 1 and n(=dim pds), 
-- corresponding to a subset of the set of variables, 
-- subFuunctionData returns the function data projected to the variables in L, if consistent; 
-- else it returns an error statement.

subFunctionData = method(TypicalValue => FunctionData)
subFunctionData(FunctionData, List) := (fcndata,L) -> (
     H := new MutableHashTable;
     L = apply(L, j -> j-1);
     scan(keys fcndata, p -> (
           q := p_L;
           val := fcndata#p;
           if H#?q and H#q != val
           then error ("sub function inconsistent: point " | 
            toString q| " has images "|toString H#q|
            " and "|toString val);
           H#q = val;
           ));
     new FunctionData from H
)

---------------------------------------------------------------------------------------------------
-- Internal to getdiffs
-- Given 2 lists of points in k^n and a polynomial ring, getdiffs1 returns a monomial

getdiffs1 = method(TypicalValue => RingElement)
getdiffs1(List, List, Ring) := (p,q,R) -> ( 
     m := 1_R;
     scan(#p, i -> if p#i =!= q#i then m = m*R_i);
     m)

---------------------------------------------------------------------------------------------------
-- Internal to minRep; uses getdiffs1
-- Given 2 lists of lists of points in k^n, getdiffs returns a monomial ideal

getdiffs = method(TypicalValue => MonomialIdeal)
getdiffs(List, List, Ring) := (P,Q,R) -> ( 
     L := flatten apply(P, p -> apply(Q, q -> getdiffs1(p,q,R)));
     monomialIdeal L)

---------------------------------------------------------------------------------------------------
-- Previously called "sparseSets"

-- Given function data D for f_i, and a polynomial ring, minRep returns a monomial ideal.
-- Purpose of ideal: set of variables, one from each gen of the ideal, is the smallest #vars 
-- required for a consistent function; that is, the sets of vars needed for a minimal representation 
-- of the polynomial function defined by D.
-- If ideal is gen by m monomials, then sets have at most m elements

minRep = method(TypicalValue => MonomialIdeal)
minRep(FunctionData, Ring) := (fcndata,R) -> (
     Ps := apply(unique values fcndata, j -> select(keys fcndata, k -> fcndata#k === j));

     -- the next 2 commented lines were used for testing purposes
    -- print apply(Ps, a-> #a);
    -- time Ls := apply(subsets(Ps,2), x -> getdiffs(x#0,x#1,R));

     --the last 2 lines were replaced with
     print apply(Ps, a-> #a);
     Ls := apply(subsets(Ps,2), x -> getdiffs(x#0,x#1,R));

     sum Ls
)

displayMinRep = (fil,I) -> (
     -- show: tally of degree of min gens
     -- if any of size 1,2,3, display them
     g := flatten entries gens I;
     h := tally apply(g, f -> first degree f);
     fil << #g << " min rep elements " << h << endl;
     h1 := select(g, f -> first degree f == 1);
     h2 := select(g, f -> first degree f == 2);
     h3 := select(g, f -> first degree f == 3);
     h4 := select(g, f -> first degree f == 4);
     if #h1 > 0 then fil << "min rep length 1 = " << toString h1 << endl;
     if #h2 > 0 then fil << "min rep length 2 = " << toString h2 << endl;
     if #h3 > 0 then fil << "min rep length 3 = " << toString h3 << endl;
     if #h3 > 0 then fil << "min rep length 4 = " << toString h4 << endl;
     )

displayMinSets = (fil,J) -> (
     -- show: tally of degree of min gens
     -- if any of size 1,2,3, display them
     g := J;
     h := tally apply(g, f -> first degree f);
     fil << #g << " min set elements " << h << endl;
     hlo := min keys h;
     h1 := select(g, f -> first degree f == 1);
     h2 := select(g, f -> first degree f == 2);
     h3 := select(g, f -> first degree f == 3);
     if #h1 > 0 and #h1 < 10 then fil << "min sets length 1 = " << toString h1 << endl;
     if #h2 > 0 and #h2 < 10 then fil << "min sets length 2 = " << toString h2 << endl;
     if #h3 > 0 and #h3 < 10 then fil << "min sets length 3 = " << toString h3 << endl;
     if hlo > 3 and (true or h#hlo < 8) then (
          hhlo := select(g, f -> first degree f == hlo);
          fil << "min sets length " << hlo << " = " << netList hhlo << endl;
      );
     )

minSets = method(Options => {Output => null,
                         Avoid => null}
                 )
minSets(TimeSeriesData, ZZ, Ring) := opts -> (T, i, R) -> (
        d := functionData(T,i);
        I := minRep(d,R);
    if instance(I,ZZ) then I = monomialIdeal(0_R); -- because I can be the zero element
--    I = I + monomialIdeal(R_(i-1));
    if opts.Avoid =!= null then (
         J1= saturate(I,product flatten {opts.Avoid});
         globalI = J1;
         I = J1;
         );
    J := flatten entries gens mesdual I;
    if opts.Output =!= null
    then (
      fil := openOut opts.Output;
      displayMinRep(fil, I);
          displayMinSets(fil, J);
      close fil;
      );
    J /sort @@ support
    )

---------------------------------------------------------------------------------------------------
-- Uses subFunctionData
-- Given function data D for f_i and a list L of variables xi, i=1..n, (returned from minRep)
-- findFunction computes a polynomial in the vars in L that fit D.

findFunction = method(TypicalValue => RingElement, Options => {MonomialOrder=>null})
findFunction(FunctionData, List) := o -> (fcndata,L) -> (

-- need to let user specify a term order. may have to remove "monoid"
-- if L=={}, then perhaps default should be the whole ring;
-- in this case, perhaps "findFunction" should be redefined to accept only one input (FunctionData) 
-- need to check if the order of variables given matters.

     if #L === 0 then error "expected positive number of variables";
     R := ring L#0;
     Lindices := apply(L, x -> index x + 1);
     F := subFunctionData(fcndata,Lindices);
     S := (coefficientRing R)(monoid [L]);
     pts := transpose matrix keys F;
     vals := transpose matrix {values F};
     (A,stds) := pointsMat(pts,S);
     f := ((transpose stds) * (vals // A))_(0,0);
     substitute(f,R)
)

---------------------------------------------------------------------------------------------------
-- Given function data D for f_i and a polynomial g, check evaluates g on D and 
-- returns true if g fits D and false otherwise; in this case, it returns an error statement.
-- Used to check the results of findFunction

checkFunction = method(TypicalValue => Boolean)
checkFunction(FunctionData, RingElement) := (fcndata,f) -> (
     pts := transpose matrix keys fcndata;
     Fs := makeRingMaps(pts,ring f);
     k := keys fcndata;
     s := select(0..#k-1, i -> Fs#i(f) != fcndata#(k#i));
     sp := apply(s, i -> k#i);
     if #s === 0 then true
     else (print ("function FAILS on points "|toString sp); false)
)

--------------------------------------------------------------------------------------------------
-- The following functions implement the scoring of edges found in 
-- Jarrah, Laubenbacher, Stigler, and Stillman

Ws = method()
Ws List := (J) -> (
     -- J should be a list of subsets of variables
     -- returns (Z1, Ws1):
     --  Z1 is a hashtable whose keys are the sizes of the supports, 
     --    and the value is the list of those support sets of that size
     --  Ws1 is a hash table, whose keys are the sizes of the sets, and 
     --    the value Ws1#s is a tally of the number of occurences
     --    of each variable xi in the sets Z1#s.
     Z := partition(length, J);
     Ws := applyPairs(Z, (k,v) -> (k, tally flatten v));
     (Z, Ws)
     )

--the following are variable scoring functions with xi as input and score as output

S1 = (Z,Ws) -> (xi) -> (
     sum apply(keys Z, s -> (k := Ws#s; if k#?xi then 1.0 * k#xi / s / #Z#s else 0.0))
     )

S2 = (Z,Ws) -> (xi) -> (
     sum apply(keys Z, s -> (k := Ws#s; if k#?xi then 1.0 * k#xi / s else 0.0))
     )

S3 = (Z,Ws) -> (xi) -> (
     sum apply(keys Z, s -> (k := Ws#s; if k#?xi then 1.0 * k#xi else 0.0))
     )

--changed the T1 function to include a multiplier bio if YAP1 is a var
bio = 2.0;
logT1 = (S,F) -> (
    --sum apply(F, xi -> log S(xi))
    sum apply(F, xi -> if xi==(first gens ring xi) then log bio*S(xi) else log S(xi))
    -- S is either S1, S2, or S3 (or other scoring function)
)

T2 = (S,F) -> (sum apply(F, xi -> S xi)) / #F

Model = new Type of HashTable

minSetScoring = method(Options => {Output => null, Avoid => null})
minSetScoring(TimeSeriesData, RingElement) := opts -> (T, xi) -> (
    i := index xi;
    << "##### variable " << xi << " ################" << endl;
    if opts.Output =!= null then (
    fil := openOut opts.Output;
    fil << "##### variable " << xi << " ################" << endl;
    close fil;
    );
    if i === null then error "expected a ring variable";
    i = i+1;
    R := ring xi;
    J := minSets(T,i,R,Output => opts.Output, Avoid => opts.Avoid);
    -- now we determine the (S1,T1)-scores of 
    -- each variable and each set in J
    (Z1,Ws1) := Ws J;
    S := S1(Z1,Ws1);
    scores := apply(J, F -> logT1(S,F));
    sets := apply(J, F -> (F,logT1(S,F)));
    maxscore := max scores;
-- made next line return the first best score - for web apps
    Jbest := J _ (positions(scores, x -> x === maxscore));
--    Jbest := J _ (position(scores, x -> x === maxscore));
    result := (xi => apply(Jbest, F -> hashTable apply(F, x -> x => S x)));
    if opts.Output =!= null then (
        fil = openOut opts.Output;
        fil << net result << endl;
        close fil;
    );
-- changed next line to return the var-score function S: 3-31-08
--     (sets, result)
    (sets, S, result)
)

minSetScoring(List, RingElement) := opts -> (L, xi) -> (
    --L is a list of minsets for variable xi
    (Z1,Ws1) := Ws L;
    S := S1(Z1,Ws1);
    scores := apply(L, F -> logT1(S,F));
    sets := apply(L, F -> (F,logT1(S,F)));
    maxscore := max scores;
    Lbest := L _ (positions(scores, x -> x === maxscore));
    result := (xi => apply(Lbest, F -> hashTable apply(F, x -> x => S x)));
    if opts.Output =!= null then (
        fil = openOut opts.Output;
        fil << net result << endl;
        close fil;
    );
    (sets, S, result)    
)

findBestMinsets = (J, weights) -> (
     score := (F) -> (sum(weights_(apply(F,index)))/#F);
     Js := partition(length,J);
     minlen := min keys Js;
     bestminlenscore := max apply(Js#minlen, score);
     bestscore := max apply(J, score);
     Jbest := select(J, F -> (s := score F;
           s == bestscore or (#F === minlen) and s == bestminlenscore));
     reverse apply(sort apply(Jbest, F -> (score F,F)), x -> x#1))

minSetWeightedScoring = method(Options => {Output => null, Avoid => null})
minSetWeightedScoring(TimeSeriesData, RingElement, List) := opts -> (T, xi, weights) -> (
     i := index xi;
     weights = weights#i; -- only need the ones for this xi.
     << "##### variable " << xi << " ################" << endl;
     if opts.Output =!= null then (
      fil := openOut opts.Output;
      fil << "##### variable " << xi << " ################" << endl;
      if opts.Avoid =!= null then fil << "avoid List = " << opts.Avoid << endl;
      close fil;
      );
     if i === null then error "expected a ring variable";
     i = i+1;
     R := ring xi;
     J := minSets(T,i,R,Output => opts.Output, Avoid => opts.Avoid);
     if length J  == 0 then 
       if opts.Output =!= null then (
      fil = openOut opts.Output;
      fil << "the data is not consistent" << endl;
      close fil;
      return (xi => null);
      );
     Jbest := findBestMinsets(J,weights);
     result := (xi => apply(Jbest, F -> hashTable apply(F, x -> x => weights#(index x))));
     if opts.Output =!= null then (
      fil = openOut opts.Output;
      fil << netList join({xi}, pack(5,result#1)) << endl;
      close fil;
      );
     result
     )

minSetWeightedScoring(TimeSeriesData,Ring,List) := opts -> (T,R,W) ->
     new Model from apply(gens R, xi -> time minSetWeightedScoring(T,xi,W,Output=>opts.Output, Avoid=>opts.Avoid))

minSetScoring(TimeSeriesData,Ring) := opts -> (T,R) ->
     new Model from apply(gens R, xi -> time minSetScoring(T,xi,Output=>opts.Output))

outDegrees = method()
outDegrees Model := (M) -> 
     tally flatten apply(values M, HL -> unique flatten apply(HL, H -> keys H))

makeDotFile = method(Options => {
      FileName => "model.dot",
      Executable => "dot",
      Display => true
      })
makeDotFile Model := opts -> (M) -> (
     file := openOut opts.FileName;
     file << "digraph { " << endl << endl;
     --file << concatenate("A [shape=record, label=\"{", 
     --      opts.FileName, "|Choose one dotted edge, if it exists.}\"];") << endl << endl;
     apply(rsort keys M, xi -> (
           Mi := M#xi;
           if #Mi === 1 then (
            -- w have either 0 or 1 minsets
            if #Mi#0 === 0 then file << toString xi << ";" << endl
            else file << replace(",",";",toString(rsort keys first Mi)) << " -> " << xi << ";" << endl
            )
           else (
            -- We have dotted lines
            int := product apply(Mi, x -> set keys x);
            un := sum apply(Mi, x -> set keys x) - int;
            file << replace(",",";",toString(rsort toList int)) << " -> " << xi << ";" << endl;
            file << replace(",",";",toString(rsort toList un)) << " -> " << xi << "[style=dotted];" << endl         
            )
           ));
     file << "}" << endl << close;
     run(opts.Executable|" -Tpdf "|opts.FileName|" > "|opts.FileName|".pdf");
     if opts.Display then run("open "|opts.FileName|".pdf");
     )

support (TimeSeriesData, ZZ) := (T,i) -> sort unique values functionData(T,i)
     
---------------------------------------------------------------------------------------------------
--+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++--
---------------------------------------------------------------------------------------------------

beginDocumentation()

document { Key => PolynomialDynamicalSystems,
     Headline => "Utilities for polynomial dynamical systems",
     EM "PolynomialDynamicalSystems", " is a package for the algebraic 
     manipulation of polynomial dynamical systems.",
     PARA{},
     "The following example describes the basic usage of this package.",
     PARA{},
     "In this example, the file 'wt1.dat' contains 7 time series data points for 5 nodes.
     The format of this file is: each row contains the (integer) data levels for a single node,
     separated by white space (spaces, tabs, but all on the same line).  Each row should contain 
     the same number of data points.  The knockout files have the same format.  The only difference
     is that knockout's for the i th node won't be used to determine functions for that node.",
     PARA{},
     "First, we read the time series and knockout data using ", TO readTSData, ".  This produces
     a TimeSeriesData object, which is just a Macaulay2 hash table.",
     EXAMPLE {
      ///T = readTSData({"wt1.dat"}, {(1,"ko1.dat")}, ZZ/5)///,
      },
     "Suppose we wish to understand which nodes might affect node 2.  First, we determine
     what any such function must satisy.",
     EXAMPLE {
      "fT = functionData(T,2)"
      },
     "In this example, there are only seven constaints on the function.  Consequently, there are
     many functions which will satisfy these constraints.",
     PARA{},
     "Next, we create a monomial ideal which encodes all of the possible sparsity patterns
     of all such functions satisfying these constraints.",
     EXAMPLE {
      "R = ZZ/5[makeVars 7];",
      "I = minRep(fT,R)",
      },
     "The monomial ideal I has the property that there is a function involving 
     variables x_i1, ..., x_ir if and only if I is contained in the ideal (x_i1, ..., x_ir).
     For example, each generator of I is divisible by either x2 or x4, so there is a function 
     involving just x2 and x4 which satisfies the data.",
     PARA{},
     "In order to find all minimal such sets, we use the Macaulay2 built-in function ",
     TO minimalPrimes, ".  Each monomial generator of the result encodes a minimal set.",
     EXAMPLE {
      "minimalPrimes I"
      },
     "The first generator tells us that there is a function involving only x2 and x4.",
     EXAMPLE {
      "findFunction(fT,{x2,x4})"
      },
     "The second generator tells us that there is a function involving only x3 and x4.",
     EXAMPLE {
      "findFunction(fT,{x3,x4})"
      }
}

document {
    Key => (checkFunction, FunctionData, RingElement),
    Headline => "given function data D and a polynomial g, evaluates g on D and returns true if g fits D and false otherwise"
}


document {
    Key => (findFunction, FunctionData, List),
    Headline => "given function data D and a list L of variables xi, i=1..n, computes a polynomial in the variables in L that fit D"
}

document {
    Key => (functionData,TimeSeriesData, ZZ),
    Headline => "given time series data and an integer i, returns a hashtable of type FunctionData for function i, that is the input-output (vector-scalar) data pairs corresponding to node i, if consistent; else returns an error statement"
}

document {
    Key => (getVars, RingElement),
    Headline => "returns the variables in a given polynomial"
}

document {
    Key => (makeVars, ZZ),
    Headline => "given an integer n, returns a list of variables {x1..xn}"
}

document {
    Key => (minRep, FunctionData, Ring),
    Headline => "given function data D and a polynomial ring, returns a monomial ideal, where the set of variables, one from each generator of the ideal, is the smallest # variables required for a consistent function; that is, the sets of variables needed for a minimal representation of the polynomial function defined by D; to be used with primaryDecomposition"
}

document { 
     Key => {readTSData, (readTSData,List,List,Ring)},
     Headline => "read time series data from a set of wild and knockout files",
     Usage => "readTSData(WT,KO,kk)",
     Inputs => {
      "WT" => " a list of file names, containing wild type data",
      "KO" => {" a list (i, L), where i is the variable being knocked out, and
            L is a list of file names containing knock out data for variable i."},
      "kk" => "the coefficient ring (usually a finite field)"
      },
     Outputs => {
      TimeSeriesData => "a hash table"
      },
     Caveat => {},
     SeeAlso => {}
     }

document {
    Key => (subFunctionData,FunctionData, List),
    Headline => "given function data and a list L of integers between 1 and n(=dim pds), corresponding to a subset of the set of variables, returns the function data projected to the variables in L, if consistent; else it returns an error statement"
}

--       TimeSeriesData, 
--       FunctionData, 

end
-----------------------------------------------------------
-- The following code might be useful in the future:
-- it combines files into one.
-- read in file, then include all of the data from the files
outF = openOut "panddata"
L = lines get "pandapas-data/new-pandapas-data"
scan(L, s -> (
      filename := replace("##.+## +","",s);
      filename = "pandapas-data/"|filename|".st";
      if fileExists filename
      then (
           outF << s << endl;
           outF << get filename << endl;
           )
      else
        << "file " << filename << " doesn't exist" << endl))
close outF    
-----------------------------------------------------------     
document { 
     Key => (borel,Matrix),
     Headline => "",
     Usage => "",
     Inputs => {
      },
     Outputs => {
      },
     Consequences => {
      },     
     "description",
     EXAMPLE {
      },
     Caveat => {},
     SeeAlso => {}
     }

restart
loadPackage "RevEng"

P = 5
kk = ZZ/P
T = readTSData("panddata",kk)
N = numgens source T.WildType#0 -- recovers N
R = kk[makeVars 13]

T0 = new TimeSeriesData from select(pairs T, (k,v) -> k === WildType)
T4 = new TimeSeriesData from select(pairs T, (k,v) -> k === WildType or k === 4)
KO4= new TimeSeriesData from select(pairs T, (k,v) -> k === 4)
M = minSetScoring(KO4,R)
makeDotFile M

apply(1..10, i -> support(KO4,i))
