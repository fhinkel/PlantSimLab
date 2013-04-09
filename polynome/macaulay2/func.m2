--*********************
--File Name: func.m2
--Author: Elena S. Dimitrova
--Original Date: 3/5/2009
--Descritpion: Generates file functs.txt that contains for each local polynomial all distinct normal forms and the corresponding proportion wrt all normal forms. Takes multiple time series data and generates 5*(number of variables) PDS models normalized wrt G. bases under monomial orderings defined by weight vectors that are randomly selected from the G. fan of the ideal of data points.
--Input: Field characteristic; number of variables; time series files
--Output: File functs.txt that contains for each local polynomial all distinct normal forms and the corresponding proportion wrt all normal forms.
--Usage: sgfan(dataFiles, p, n)
--********************* 

load  "PolynomialDynamicalSystems.m2"
load  "Points.m2"

sgfan = method();
sgfan(List, String, ZZ, ZZ) := (WT, outfile, pp, nn) -> (

    kk = ZZ/pp; --Field
    --WT={"transition-1.txt","transition-2.txt","transition-3.txt","transition-4.txt","transition-5.txt","transition-6.txt","transition-7.txt","transition-8.txt","transition-9.txt","transition-10.txt","transition-11.txt","transition-12.txt","transition-13.txt"}; --(MUST come as input)
    
    --TS is a hashtable of time series data WITH NO KO DATA
    TS = readTSData(WT, {}, kk);
    
    --FD is a list of hashtables, where each contains the input-vectors/output pairs for each node
    FD = apply(nn, II->functionData(TS, II+1));
    
    --IN is a matrix of input vectors
    IN = matrix keys first FD;
    
    allNFs={};
    functs={};
    allFuncts={};
    
    --Sample randomly from the G. fan
    setRandomSeed processID();
    apply(5*nn, J -> (
        permw= apply(nn, i->random 100*nn);    
        sm=0;
        apply(nn, i->(sm=sm+(permw#i)^2));
        
        while sm > (100*nn)^2 do (permw= apply(nn, i->random 100*nn); sm=0; apply(nn, i->(sm=sm+(permw#i)^2))  );

        --Rr is a polynomial ring in nn variables; can declare a term order here
        Rr = kk[vars(53..52+nn), Weights => permw];
        
        --SM is a list of standard monomials
        --LT is an ideal of leading terms
        --GB is a list of Grobner basis elements - THIS IS WHAT YOU WANT FOR GFAN
        (SM, LT, GB) = points(transpose IN, Rr);
        
        --F is a list of interpolating functions, one for each node 
        FF = apply(nn, II->findFunction(FD_II, gens Rr));
        use Rr;
        GG = {matrix{GB}};
        NF = apply(nn, II->apply(GG, gb->(FF_II)%gb));
        
        temp={};
        temp=apply(nn, II->append(temp,NF#II#0));
        allNFs=append(allNFs,flatten temp);
    ));
    
    allCounts={};
    FF={};
    apply(nn, JJ->(
        s={};
        apply(5*nn, II->(
            s=append(s, allNFs#II#JJ);
        ));

        --Count how many times a normal form is repeated for each local polynomial
        st={};
        apply(#s, i->(st=append(st,sub(s#i,ring s#0))));
        
        s=st;
        st=set st;
        st=toList st;
        fns={};
        c=0;
        apply(#st, i->(
            apply(#s, j->(if st#i==s#j then c=c+1)),
            fns=append(fns,{st#i,c});
            c=0;
        )); 
        FF=append(FF,fns);
    ));
    
    --For each local polynomial f_i, print f_i={all distinct normal forms and the corresponding proportion wrt all normal forms}
--    file = openOut "functs.txt";
--    file = openOut concatenate("functions-", first WT);
    file = openOut outfile; --concatenate(last separate("l-", first separate(".", first WT)), ".functionfile.txt");

    apply(nn, i->(
	if #(FF#i)==1 then (file << "f" << i+1 << " = " << toString first FF#i#0 << endl)
	else (
		file << "f" << i+1 << " = {" << endl; 
		apply(FF#i, q->(file << toString q#0 << "  #" << toString ((q#1)/(5.0*nn)) << endl)); 
		file << "}" << endl
	)
    ));
    file << close;
)
end
----------------------------------------------------- end of file---------------------------------------------------
