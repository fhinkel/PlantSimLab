--*********************
--File Name: ifConsistent.m2
--Author: Elena S. Dimitrova
--Original Date: 3/6/2009
--Descritpion: An awkward but working way to tell if a data set is consistent. efunctionData is a modification of functionData from PolynomialDynamicalSystems package
--Input: Field characteristic; number of variables; time series files
--Output: True (consistent) or False (inconsistent)
--Usage:	isConsistent(discreteDataFileList, p, n)
--   Ex:	isConsistent({"Bool-toy.txt","Bool-toy2.txt"}, 2, 3)
--   Returns:	false
--********************* 

needs "PolynomialDynamicalSystems.m2"

efunctionData := method(TypicalValue => eFunctionData)
efunctionData(TimeSeriesData, ZZ) := (tsdata,v) -> (
     H := new MutableHashTable;

     -- first make the list of matrices
     mats := tsdata.WildType;
     scan(keys tsdata, x -> if class x === ZZ and x =!= v then mats = join(mats,tsdata#x));

     -- now make the hash table
     c = 0;	
     scan(mats, m -> (
           e := entries m;
           for j from 0 to #e-2 do (
            tj := e#j;
            val := e#(j+1)#(v-1);
            if H#?tj and H#tj != val then (c=c+1; return c);
              --error ("function silly: point " | 
                  -- toString tj| " has images "|toString H#tj|
                  -- " and "|toString val);           
            H#tj = val;
            )));
    return c;
    -- new eFunctionData from H
)

isConsistent = method(TypicalValue => Boolean)
isConsistent(List, ZZ, ZZ) := (WT, p, n) -> (
    k := ZZ/p; --Field
    --TS is a hashtable of time series data WITH NO KO DATA
    TS := readTSData(WT, {}, k);
    
    --FD is a list of hashtables, where each contains the input-vectors/output --pairs for each node
    ct:=0;
    apply(n, i->(ct=ct+efunctionData(TS, i+1)));
--    if ct>0 then return false else return true;
    if ct>0 then exit 0 else exit 42;
)

end
----------------------------------------------------- end of file---------------------------------------------------
