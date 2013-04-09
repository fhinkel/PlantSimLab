--*********************
--File Name: func.m2
--Author: Elena S. Dimitrova
--Original Date: 3/5/2009
--Descritpion: Generates file functs.txt that contains for each local polynomial all distinct normal forms and the corresponding proportion wrt all normal forms. Takes multiple time series data and generates 5*(number of variables) PDS models normalized wrt G. bases under monomial orderings defined by weight vectors that are randomly selected from the G. fan of the ideal of data points.
--Input: Field characteristic; number of variables; time series files
--Output: File functs.txt that contains for each local polynomial all distinct normal forms and the corresponding proportion wrt all normal forms.
--Usage: sgfan(dataFiles, p, n)
--********************* 

needsPackage "PolynomialDynamicalSystems"
needsPackage "Points"

randomWeightVector = (nn) -> (
     local permw;
     while (
        permw = apply(nn, i->random 100*nn);
        sm := sum apply(permw, x -> x^2);
	sm > (100*nn)^2) 
     do ();
     permw)

randomWeightVector = (nn) -> (
     --Choose a random radius in [0,1]
     --m=20*nn;
     m:=500;
     onept:={}; 

     --Get 10 normal numbers
     for j from 1 to ceiling(nn/2) do(
	  x:=random 1.; y:=random 1.;
	  onept=append(onept, sqrt(-2*log(x))*cos(2*pi*y));
	  onept=append(onept, sqrt(-2*log(x))*sin(2*pi*y));
	  );
     --Get the 11th number
     --t=random 2;
     --if t==0 then onept=append(onept, sqrt(-2*log(x))*cos(2*pi*y)) else onept=append(onept, --sqrt(-2*log(x))*sin(2*pi*y));

     r:=random 1.;
     r=r^(1./nn);

     q:=0;
     for j from 0 to nn-1 do (q=q+(onept#j)^2);
     q=sqrt(q);
     normpts := for j from 0 to nn-1 list ((onept#j)*r/q);

     --Convert spherical to rectangular coordinates
     apply(nn, i-> (
	       j:=1;
	       while abs(normpts#i)>j/m do j=j+1;
	       j-1))
     )

sgfan = method(Options => {Limit => 1200})
sgfan(Sequence, String, ZZ, ZZ) := opts -> (WTandKO, outfile, p, nvars) -> (
    -- WTandKO: (WT, KO)
    -- outfile:
    -- p: number of states (prime number)
    -- nvars: number of genes (columns of each matrix)
    -- niterations: how many GB's to sample
    (WT,KO) := WTandKO;
    kk := ZZ/p; --Field
    --TSW is a hashtable of WT time series data
    --TSK is a hashtable of KO time series data 
    --TS is a hashtable of ALL time series data 
    TSW := readTSData(WT, kk);
    TSK := readKOData(KO, kk);
    TS := TSW + TSK;

    --FD is a list of hashtables, where each contains the input-vectors/output pairs for each node
    FD := apply(nvars, i->functionData(TS, i+1));
        
    --IN is a matrix of input vectors
    IN := transpose matrix keys first FD;
    
    -- All the normal forms will be placed in this ring:
    S := kk[makeVars nvars];

    F := matrix {apply(nvars, i-> time findFunction(FD_i, gens S))};
    
    --Sample randomly from the G. fan
    --setRandomSeed();
    setRandomSeed processID();

    allNFs = apply(opts.Limit, J -> time (
	if J % 100 == 0 then << J << "." << flush;
        wtvec := randomWeightVector nvars;
        print wtvec;
        --Rr is a polynomial ring in nn variables; can declare a term order here
        Rr := kk[gens S, Weights => wtvec];
        
        --SM is a list of standard monomials
        --LT is an ideal of leading terms
        --GB is a list of Grobner basis elements - THIS IS WHAT YOU WANT FOR GFAN
        (SM, LT, GB) := points(IN, Rr);
     	GB = matrix{GB};
	forceGB GB;
        --F is a list of interpolating functions, one for each node 
	F1 := sub(F, Rr);
	NF := F1 % GB;
        flatten entries sub(NF,S)
        ));
    FF := allNFs//transpose/tally/pairs;
    
    file := openOut outfile;
    apply(nvars, i-> (
	if #(FF#i)==1 then (file << "f" << i+1 << " = " << toString first FF#i#0 << endl)
	else (
		file << "f" << i+1 << " = {" << endl; 
		apply(FF#i, q->(file << toString q#0 << "  # " << toString (0.0 + (q#1)/opts.Limit) << endl)); 
		file << "}" << endl
	)
    ));
    file << close;
--    )

--------------------------------------
-- The variable counting starts here
--------------------------------------

m=#allNFs;
nn=nvars;

allCounts={};
FF={};
apply(nn, JJ->(
s={};
apply(m, II->(
s=append(s,toString (allNFs#II#JJ));
));


--Count how many times a normal form is repeated for each local polynomial
ssort=rsort s;
fns={};
c=1;
el=toString ssort#0;
apply(#ssort-1, II->(
if el==toString ssort#(II+1) then c=c+1 else (fns=append(fns,{el,c}), el=toString ssort#(II+1), c=1); ));
fns=append(fns,{el,c});
FF=append(FF,fns);
));

allCounts={};
apply(nn, JJ->(
s={};
apply(m, II->(
s=append(s,toString (allNFs#II#JJ));
));

--For each local polynomial f_i, count occurrences of each variable x_j
count=0;
lst={};

apply(nn, i->(apply(m,j->(if #(select("x"|i+1,s#j))-#(select("x"|i+1|"[[:digit:]]+",s#j))>0 then count=count+1 ));
lst=append(lst,count);
count=0));
allCounts=append(allCounts,lst);
));

v=toList vars(53..52+nn);

file = openOut "bool-dependencies-2000-500-10-2.txt";
apply(nn, i->(apply(nn, j->(file << (allCounts#j#i)/2000. << " ";)); file << endl;));
file << close;
)

end
sgfan(("size10-2-Bool-ts.txt","size10-2-Bool-ko.txt"),"bool-10-2-m500-2000.txt",2,15,Limit=>2000)
