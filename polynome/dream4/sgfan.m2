<<<<<<< HEAD:dream4/sgfan.m2
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
     m:=100;
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

    allNFs := apply(opts.Limit, J -> time (
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
    )
     

end

restart
load "sgfan.m2"
randomWeightVector 10
--time sgfan(("challenge2-discretized/consistent-output-10-1-time-series",null),"outy",5,15,Limit=>15)
time sgfan(("challenge2-discretized/consistent-output-10-1-time-series","challenge2-discretized/output-10-1-knockouts"),"outy",5,15,Limit=>15)
time sgfan(("toy.txt",null),"outy",5,4,Limit=>10)

{16, 14, 90, 5, 67, 22, 38, 10, 211, 14}


time sgfan(("challenge2-discretized/consistent-output-10-1-time-series",null),"challenge2-discretized/sgfan-10-1-n50",5,15,Limit=>50)
load "../macaulay2/minsets-web.m2"     
time minsetsWD("challenge2-discretized/consistent-output-10-1-time-series", "outy", 5, 15)

loadPackage "PolynomialDynamicalSystems"
R = ZZ/5[makeVars 15]
TS = readTSData("challenge2-discretized/consistent-output-10-1-time-series", ZZ/5)
minSets(TS,7,R)
oo/print;
=======
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
     m:=250;
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
    S := kk[makeVars nvars, MonomialSize=>8];

    F := matrix {apply(nvars, i-> time findFunction(FD_i, gens S))};
    
    --Sample randomly from the G. fan
    setRandomSeed();
    setRandomSeed processID();

    allNFs := apply(opts.Limit, J -> time (
	if J % 100 == 0 then << J << "." << flush;
        wtvec := randomWeightVector nvars;
        print wtvec;
        --Rr is a polynomial ring in nn variables; can declare a term order here
        Rr := kk[gens S, Weights => wtvec, MonomialSize=>8];
        
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
    )
     

end

restart
load "sgfan.m2"
randomWeightVector 10
--time sgfan(("challenge2-discretized/consistent-output-10-1-time-series",null),"outy",5,15,Limit=>15)
time sgfan(("challenge2-discretized/consistent-output-10-1-time-series","challenge2-discretized/output-10-1-knockouts"),"outy",5,15,Limit=>15)
time sgfan(("toy.txt",null),"outy",5,4,Limit=>10)

{16, 14, 90, 5, 67, 22, 38, 10, 211, 14}


time sgfan(("challenge2-discretized/consistent-output-10-1-time-series",null),"challenge2-discretized/sgfan-10-1-n50",5,15,Limit=>50)
load "../macaulay2/minsets-web.m2"     
time minsetsWD("challenge2-discretized/consistent-output-10-1-time-series", "outy", 5, 15)

loadPackage "PolynomialDynamicalSystems"
R = ZZ/5[makeVars 15]
TS = readTSData("challenge2-discretized/consistent-output-10-1-time-series", ZZ/5)
minSets(TS,7,R)
oo/print;

>>>>>>> 25670d4a2dc94895ce028d990d31b07e6bba0ebc:dream4/sgfan.m2
