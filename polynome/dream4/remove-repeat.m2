--*********************
--File Name: remove-repeat.m2
--Author: Elena S. Dimitrova
--Original Date: 3/6/2009
--Descritpion: Merges consecutively repeated states (in the middle as well as at the end of a time series) into one. e2functionData is a modification of functionData from PolynomialDynamicalSystems package.
--Input: Field characteristic; number of variables; time series files
--Output: FD, a list of hashtables, where each contains the input-vectors/output pairs for each node; IN, a matrix of input vectors
-- EDITED BY BRANDY
--********************* 

needs "PolynomialDynamicalSystems.m2"

removeRepeatedStates = method()
removeRepeatedStates(String, String) := (WTfile, outfile) -> ( 

	--still not handling ko data
     	TS = readTSData(WTfile,ZZ);

	-- make the list of matrices
     	mats = TS.WildType;
     	scan(keys TS, x -> if class x === ZZ and x =!= v then mats = join(mats,TS#x));

	file = openOut outfile;
	scan(#mats, j -> (
	   m = mats_j;
           e = entries m;
           --Merge consecutively repeated states into one state
           ee = {e#0};
           for i from 0 to #e-3 do (
		if e#i!=e#(i+1) then ee=append(ee,e#(i+1))
	   );
	   --appending last element, regardless if it is repeated (fixed pts ok)
	   ee=append(ee,e#(-1)); 
	   file << "#TS" << toString (j+1) << endl;
	   apply(ee, state->(
		apply(state, data->file << data << " ");
		file << endl;
	   ));
	));

	file << close;
)
end
----------------------------------------------------- end of file---------------------------------------------------
