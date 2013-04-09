--*********************
--File Name: incons.m2
--Author: Elena S. Dimitrova
--Original Date: 3/10/2009
--Descritpion: Removes inconsistencies from input data by splitting time series at the point of inconsistency. ereadMat is a modification of readMat from the PolynomialDynamicalSystems package.
--Input: Number of variables; DISCRETIZED time series files
--Output: Files named "transition-i.txt", each containing a single transition, all consistent
--********************* 

load "PolynomialDynamicalSystems.m2"
load "Points.m2"
n = 15; --Number of variables (MUST come as nput)
--WT={"2_KO1.txt","2_KO2.txt","2_KO3.txt","2_KO4.txt","2_KO5.txt","2_KO6.txt","2_KO7.txt","2_KO8.txt","2_KO9.txt","2_KO10.txt","2_KO11.txt"};
--WT={"2_TS1.txt","2_TS2.txt","2_TS3.txt","2_TS4.txt","2_TS5.txt","2_TS6.txt"}; --(MUST come as input)


-- Given a data file and a coefficient ring, ereadMat returns the (txn)-matrix of the data (t=time, n=vars). 

ereadMat = method(TypicalValue => Matrix)
ereadMat(String,Ring) := (filename,R) -> (
     ss := select(lines get filename, s -> length s > 0);
     matrix(R, apply(ss, s -> (t := separateRegexp(" +", s); 
                 t = apply(t,value);
                     select(t, x -> class x =!= Nothing))))
)

transitions={}; --Contains every pair of transitions
m={};
trouble={};


apply(#WT, i-> (mt = apply({WT#i}, s -> ereadMat(s,ZZ)); --mt is a hash table of points from one input file
apply(#mt, s -> (m = append(m, entries mt#s))); m=flatten m; --m is a list of points from one input file
for j from 0 to #m-2 do ( transitions=append(transitions, {m#j, m#(j+1)}) );-- Record them as transitions
m={}; )  );

--Identify transitions that are inconsistent
select(transitions, i->(
t=select (transitions, j->(i#0==j#0 and i#1!=j#1));
if t != {} then trouble=append(trouble, t);
));
trouble=flatten trouble;

--Keep only the consistent transitions
consistent_transitions=set transitions-set trouble;
consistent_transitions=toList consistent_transitions;

fl="size10-2-Bool-ko.txt";
file=openOut fl;

for i from 0 to #consistent_transitions-1 do ( 
for j from 0 to n-1 do (file << consistent_transitions#i#0#j << " "; );
file << endl;
for j from 0 to n-1 do (file << consistent_transitions#i#1#j << " "; );
file << endl;
file << endl;);
file<<close;



end
----------------------------------------------------- end of file---------------------------------------------------