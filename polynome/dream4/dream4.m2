-- This file reads in the raw data, as RR matrices
-- and contains some basic analysis routines (which might move at some point)

needs "incons.m2"
needs "remove-repeat.m2"
needsPackage "Markov"
needs "discretization.m2"

insilicoNames = hashTable {
  "10-1" => "challenge2-data/insilico_size10_1/insilico_size10_1_",
  "10-2" => "challenge2-data/insilico_size10_2/insilico_size10_2_",
  "10-3" => "challenge2-data/insilico_size10_3/insilico_size10_3_",
  "10-4" => "challenge2-data/insilico_size10_4/insilico_size10_4_",
  "10-5" => "challenge2-data/insilico_size10_5/insilico_size10_5_",

  "100-1" => "challenge2-data/insilico_size100_1/insilico_size100_1_",
  "100-2" => "challenge2-data/insilico_size100_2/insilico_size100_2_",
  "100-3" => "challenge2-data/insilico_size100_3/insilico_size100_3_",
  "100-4" => "challenge2-data/insilico_size100_4/insilico_size100_4_",
  "100-5" => "challenge2-data/insilico_size100_5/insilico_size100_5_"
}

insilicoName = insilicoNames#"10-1"
--files = {"wildtype", "timeseries", "knockouts"} -- add in "knockdowns", "multifactorial"?
files = {"wildtype", "timeseries", "knockouts", "knockdowns"}

toMatrices = (S,R,keepfirstcol) -> (
     -- S is a list of strings
     -- R is the ring of the resulting matrices
     -- keepfirstcol:Boolean, keep first col?
     -- returns a list of matrices.  Each blank line in the input separates
     -- the matrices
     S = drop(S,1);
     S = join({""},S,{""});
     blanklines := positions(S, s -> match("^[:blank:]*$",s));
     << "blank lines: " << blanklines << endl;
     S1 := for i from 1 to #blanklines-1 list S_{blanklines#(i-1)+1..blanklines#i-1};
     S1 = select(S1, s -> #s > 0);
     answer := apply(S1, s0 -> matrix(R, apply(s0, s -> (t := separateRegexp("[[:blank:]]+", s); 
                 t = apply(t,value);
                 select(t, x -> class x =!= Nothing)))));
     if keepfirstcol then answer else apply(answer,  m -> submatrix(m,1..numColumns m - 1))
     )

knockoutMatrix = (M, W, upthresh, downthresh) -> (
     N := #M;
     L := toList apply(N, i -> toList apply(N, j -> M#i#j/W#j));
     matrix toList apply(N, i -> (
	  toList apply(N, j -> if i == j 
	       then 0.0
	       else if L#i#j >= upthresh
	         then L#i#j
		 else if L#i#j <= downthresh
		   then L#i#j
		   else 0.0)))
     )

knockoutGraph = (M, W, upthresh, downthresh) -> (
     -- M is a n by n list of RR's, of knockout steady state values
     -- W is a length n list of RR's, of wildtype steady state values
     -- The directed dependency graph with the given thresholds.
     N := #M;
     L := toList apply(N, i -> toList apply(N, j -> M#i#j/W#j));
     L1 := toList apply(N, i -> (i+1) => 
	  toList apply(N, j -> if i == j 
	       then 0
	       else if L#i#j >= upthresh
	         then 1 
		 else if L#i#j <= downthresh
		   then -1 
		   else 0));
     H := hashTable apply(L1, kv -> kv#0 => apply(positions(kv#1, x -> x != 0), y -> y+1));
     makeGraph for i from 1 to N list (if H#?i then H#i else {})
     )

discretizeTimeSeries = (TS,wildtype,knockouts,nintervals) -> (
     -- TS: a list of M by N matrices.  Columns correspond to genes, 
     --   each row corresponds to a time
     -- wildtype: 1 by N matrix with the steady state values for each gene
     -- knockouts: N by N matrices,each row is considered a steady state
     N := numColumns TS#0; -- also number of columns for all matrices in list TS
     Info := "";
     Tsi := for i from 0 to N-1 list (
       << "---discretizing gene " << i+1 << " ----" << endl;
       Info = Info | "---discretizing gene " | i+1 | " ----" | "\n";
       LL := apply(TS, m -> flatten entries m_{i});
       wt := wildtype_(0,i);
       LL = append(LL, {wt,wt});
       ST := LL/first;
       KO := entries(knockouts_{i});
       LL1 := drop(flatten KO, {i,i}); -- select(flatten KO, x -> x > 0.0);
       LL1 = append(LL,LL1);
       KO = apply(KO, L -> append(L, L#-1));
       I := createDiscretization(LL1,min ST, max ST, nintervals);
       Ls := flatten LL1;
       Info = Info | "min, max values: " | toString min Ls | " " | toString max Ls | "\n";
       Info = Info | "min, max steady: " | toString min ST | " " | toString max ST | "\n";
       Info = Info | "interval boundaries: " | toString prepend(min Ls, I) | "\n";
       (discretize(LL,I), discretize(KO,I))
       );
     (Tsi/first, Tsi/last, Info)
     )

writeTimeSeriesData = (filename, T, heading, nperturb) -> (
     -- T is a list: T_0 .. T_(N-1) one for each gene
     -- each T_i is a list of the time courses for gene (i+1).
     -- heading is a string
     allzeros := (heading == "knockout");
     F := openOut filename;
     ngenes := #T;
     ntimeseries := #T#0; -- length of each list T#k
     for i from 0 to ntimeseries-1 do (
       F << "# " << heading << " " << i+1 << endl;
       nrows := #T#0#i;
       for j from 0 to nrows-1 do (
         for k from 0 to ngenes-1 do
	   F << T#k#i#j << " ";
	 if allzeros or j >= 10 then (
	   for m from 0 to nperturb-1 do
	     F << "0 ")
	 else (
	   for m from 0 to nperturb-1 do
	     F << if m == i then "1 " else "0 ");
	 F << endl;
	 );
       );
     close F;
     )

readDream4 = (insilicoName) -> (
     H := hashTable apply(files, f -> f => 
	     toMatrices(
	       lines get(insilicoName|f|".tsv"), 
	       RR, 
	       f =!= "timeseries"));
     hashTable apply(pairs H, (k,v) -> if #v == 1 then (k,v#0) else (k,v))
     )

-- main discretization function:
discretizeDream4 = (prefix, silicoName, p) -> (
     -- prefix: string, for all file names generated.
     -- silicoName: insilicoName of the data
     -- p: number of intervals in discretization
     -- steps:
     -- 1. read in data
     -- 2. discretize the data
     -- 3. write out the files (discretization info, time series, knockout file)
     H := readDream4 silicoName;
     (TS,KO,DiscInfo) := discretizeTimeSeries(H#"timeseries",H#"wildtype",H#"knockouts",p);
     (prefix|"disc-info") << DiscInfo << endl << close;
     writeTimeSeriesData(prefix|"time-series", TS, "time series",#H#"timeseries");
     writeTimeSeriesData(prefix|"knockouts", KO, "knockout", #H#"timeseries");
     )

knockoutGraphDream4 = (prefix, silicoName, downthreshold, upthreshold) -> (
     -- returns (filename, incidence matrix: with weights)
     H := readDream4 silicoName;
     E := entries H#"knockouts";
     W := first entries H#"wildtype";
     G := knockoutGraph(E,W,upthreshold,downthreshold);
     M := knockoutMatrix(E,W,upthreshold,downthreshold);
     dotname := prefix | "-down" | toString downthreshold | "-up" | toString upthreshold | ".dot";
     jpgname := prefix | "-down" | toString downthreshold | "-up" | toString upthreshold | ".jpg";
     displayGraph(dotname, jpgname, G);
     (dotname,jpgname,M)
     )
end

restart
load "dream4.m2"

discretizeDream4("challenge2-discretized/output-10-1-", insilicoNames#"10-1", 5)
discretizeDream4("challenge2-discretized/output-10-2-", insilicoNames#"10-2", 5)
discretizeDream4("challenge2-discretized/output-10-3-", insilicoNames#"10-3", 5)
discretizeDream4("challenge2-discretized/output-10-4-", insilicoNames#"10-4", 5)
discretizeDream4("challenge2-discretized/output-10-5-", insilicoNames#"10-5", 5)

discretizeDream4("challenge2-discretized/output-100-1-", insilicoNames#"100-1", 5)
discretizeDream4("challenge2-discretized/output-100-2-", insilicoNames#"100-2", 5)
discretizeDream4("challenge2-discretized/output-100-3-", insilicoNames#"100-3", 5)
discretizeDream4("challenge2-discretized/output-100-4-", insilicoNames#"100-4", 5)
discretizeDream4("challenge2-discretized/output-100-5-", insilicoNames#"100-5", 5)

removeRepeatedStates("challenge2-discretized/output-10-1-time-series", "challenge2-discretized/no-repeats-10-1-time-series")
makeConsistent("challenge2-discretized/no-repeats-10-1-time-series", "challenge2-discretized/consistent-output-10-1-time-series")

removeRepeatedStates("challenge2-discretized/output-10-2-time-series", "challenge2-discretized/no-repeats-10-2-time-series")
makeConsistent("challenge2-discretized/no-repeats-10-2-time-series", "challenge2-discretized/consistent-output-10-2-time-series")

removeRepeatedStates("challenge2-discretized/output-10-3-time-series", "challenge2-discretized/no-repeats-10-3-time-series")
makeConsistent("challenge2-discretized/no-repeats-10-3-time-series", "challenge2-discretized/consistent-output-10-3-time-series")

removeRepeatedStates("challenge2-discretized/output-10-4-time-series", "challenge2-discretized/no-repeats-10-4-time-series")
makeConsistent("challenge2-discretized/no-repeats-10-4-time-series", "challenge2-discretized/consistent-output-10-4-time-series")

removeRepeatedStates("challenge2-discretized/output-10-5-time-series", "challenge2-discretized/no-repeats-10-5-time-series")
makeConsistent("challenge2-discretized/no-repeats-10-5-time-series", "challenge2-discretized/consistent-output-10-5-time-series")

removeRepeatedStates("challenge2-discretized/output-100-1-time-series", "challenge2-discretized/no-repeats-100-1-time-series")
makeConsistent("challenge2-discretized/no-repeats-100-1-time-series", "challenge2-discretized/consistent-output-100-1-time-series")

removeRepeatedStates("challenge2-discretized/output-100-2-time-series", "challenge2-discretized/no-repeats-100-2-time-series")
makeConsistent("challenge2-discretized/no-repeats-100-2-time-series", "challenge2-discretized/consistent-output-100-2-time-series")

removeRepeatedStates("challenge2-discretized/output-100-3-time-series", "challenge2-discretized/no-repeats-100-3-time-series")
makeConsistent("challenge2-discretized/no-repeats-100-3-time-series", "challenge2-discretized/consistent-output-100-3-time-series")

removeRepeatedStates("challenge2-discretized/output-100-4-time-series", "challenge2-discretized/no-repeats-100-4-time-series")
makeConsistent("challenge2-discretized/no-repeats-100-4-time-series", "challenge2-discretized/consistent-output-100-4-time-series")

removeRepeatedStates("challenge2-discretized/output-100-5-time-series", "challenge2-discretized/no-repeats-100-5-time-series")
makeConsistent("challenge2-discretized/no-repeats-100-5-time-series", "challenge2-discretized/consistent-output-100-5-time-series")

-----------------------------------------------------------
knockoutGraphDream4("challenge2-graphs/output-10-1", insilicoNames#"10-1", .75, 1.5)


removeRepeatedStates("challenge2-discretized/output-10-1-time-series", "challenge2-discretized/no-repeats-10-1-time-series")
makeConsistent("challenge2-discretized/no-repeats-10-1-time-series", "challenge2-discretized/consistent-output-10-1-time-series")

H = readDream4(insilicoNames#"10-1")

H = hashTable apply(files, f -> f => toMatrices(lines get(insilicoName|f|".tsv"), RR, f =!= "timeseries"));
H = hashTable apply(pairs H, (k,v) -> if #v == 1 then (k,v#0) else (k,v))

(TS,KO) = discretizeTimeSeries(H#"timeseries",H#"wildtype",H#"knockouts",5);
writeTimeSeriesData(TS, "time series")
writeTimeSeriesData(KO, "knockout")


M1 = apply(H#"timeseries", m -> flatten entries m_{0})
D1 = discretize(M1, 5)

M1 = apply(H#"timeseries", m -> flatten entries m_{3})
D1 = discretize(M1, 5)

M2 = apply(H#"timeseries", m -> flatten entries m_{1})
D2 = discretize(M2, 5)
matrix oo

E = entries H#"knockouts"
E = entries H#"knockdowns"
W = first entries H#"wildtype"
G = knockoutGraph(E,W,1.5,.75)
knockoutMatrix(E,W,1.5,.75)
displayGraph G

L = matrix toList apply(10, i -> toList apply(10, j -> E#i#j/W#j))
E = entries H#"knockdowns"
L = matrix toList apply(10, i -> toList apply(10, j -> E#i#j/W#j))

