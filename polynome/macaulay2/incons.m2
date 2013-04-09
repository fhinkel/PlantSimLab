--*********************
--File Name: 	incons.m2
--Author: 	Elena S. Dimitrova
--Original Date:3/10/2009
--Edited By:	Brandy Stigler
--Edit Data:	8/11/09
--Description: 	Removes inconsistencies from input data by splitting time series at the point of inconsistency. 
--Input: 	Number of variables; DISCRETIZED time series files; output file name
--Output: 	File with each transition, viewed as a time series, separated by hash marks (#) 
--Usage:	2 ways to call "makeConsistent"
--1:		makeConsistent(data_file_list, num_nodes, outfile)
--2:		makeConsistent(infile, num_nodes, outfile)
--              ***Last way is intended for Polynome***
--********************* 

needs "PolynomialDynamicalSystems.m2"

makeConsistent = method()
makeConsistent(List, ZZ, String) := (WT, n, outfile) -> ( 

    transitions := {}; --Contains every pair of transitions
    m := {};
    trouble := {};
    
    apply(#WT, i-> (
        --mt is a hash table of points from one input file
        --m is a list of points from one input file
    
        mt = apply({WT#i}, s -> readMat(s,ZZ));     
        apply(#mt, s -> (m = append(m, entries mt#s))); 
        m = flatten m; 
        for j from 0 to #m-2 do 
        ( 
            transitions = append(transitions, {m#j, m#(j+1)}) 
        );-- Record them as transitions
        m = {}; 
    ));
    
    --Identify transitions that are inconsistent
    scan(transitions, i->(
        t := select (transitions, j->(i#0==j#0 and i#1!=j#1));
        if t != {} then trouble = append(trouble, t);
    ));
    trouble = flatten trouble;
    
    --Keep only the consistent transitions
    consistentTransitions = set transitions-set trouble;
    consistentTransitions = toList consistentTransitions;
    
    --Print each transitions in a single file
    file = openOut outfile;
    for i from 0 to #consistentTransitions-1 do 
    ( 
        file << "#TS" << toString(i+1) << endl;
        for j from 0 to n-1 do (file << consistentTransitions#i#0#j << " "; );
        file << endl;
        for j from 0 to n-1 do (file << consistentTransitions#i#1#j << " "; );
        file << endl;
    );
    file<<close;
)

makeConsistent(String, ZZ, String) := (infile, n, outfile) -> ( 

    transitions := {}; --Contains every pair of transitions
    trouble := {};
    
    mat := flatten values readTSData(infile,ZZ);
    apply(mat, l->(
        m=entries l;
        for j from 0 to #m-2 do 
        ( 
            transitions = append(transitions, {m#j, m#(j+1)}) 
        );-- Record them as transitions
    ));
    
    --Identify transitions that are inconsistent
    scan(transitions, i->(
        t := select (transitions, j->(i#0==j#0 and i#1!=j#1));
        if t != {} then trouble = append(trouble, t);
    ));
    trouble = flatten trouble;

    --Keep only the consistent transitions
    consistentTransitions = set transitions-set trouble;
    consistentTransitions = toList consistentTransitions;
    
    --Print each transitions in a single file
    file = openOut outfile;
    for i from 0 to #consistentTransitions-1 do 
    ( 
        file << "#TS" << toString(i+1) << endl;
        for j from 0 to n-1 do (file << consistentTransitions#i#0#j << " "; );
        file << endl;
        for j from 0 to n-1 do (file << consistentTransitions#i#1#j << " "; );
        file << endl;
    );
    file<<close;
)
end
----------------------------------------------------- end of file---------------------------------------------------
