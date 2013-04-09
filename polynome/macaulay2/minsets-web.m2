needs "PolynomialDynamicalSystems.m2"

minsetsWD = method();
minsetsWD(List,String,ZZ,ZZ) := (WT, outfile, p, n) -> (
    k := ZZ/p;
    R := k[makeVars n];
    TS := readTSData(WT,{},k);
    MS := apply(gens R, x->last minSetScoring(TS,x));
    
    file := openOut outfile;
    file << "digraph { \n";
    
    L := apply(MS, p->(keys first last p => first p))/toString;
    L = apply(L, s->replace("=>","->",s));
    L = apply(L, s->replace(",",";",s));
    L = apply(L, s->replace("=>","->",s));
    
    apply(L, l->(file << l << ";" << endl));
    file << "}" << endl << close;
)

minsetsWD(String,String,ZZ,ZZ) := (infile, outfile, p, n) -> (
    k := ZZ/p;
    R := k[makeVars n];
    TS := readTSData(infile,k);
    MS := apply(gens R, x->last minSetScoring(TS,x));
    
    file := openOut outfile;
    file << "digraph { \n";
    
    L := apply(MS, p->(keys first last p => first p))/toString;
    L = apply(L, s->replace("=>","->",s));
    L = apply(L, s->replace(",",";",s));
    L = apply(L, s->replace("=>","->",s));
    
    apply(L, l->(file << l << ";" << endl));
    file << "}" << endl << close;
)


minsetsPDS = method();
minsetsPDS(List,String,ZZ,ZZ) := (WT, outfile, p, n) -> (
    k := ZZ/p;
    R := k[makeVars n];
    TS := readTSData(WT,{},k);
    MS := apply(gens R,x->keys first last last minSetScoring(TS,x));
    FD := apply(n, i->functionData(TS,i+1));
    FS := apply(n, i->findFunction(FD_i,MS_i)); 
    
    file := openOut outfile;
    apply(n, i->(file << "f" << i+1 << " = " << toString FS_i << endl));
    file << endl << close;
)

minsetsPDS(String,String,ZZ,ZZ) := (infile, outfile, p, n) -> (
    k := ZZ/p;
    R := k[makeVars n];
    TS = readTSData(infile,k);
    MS = apply(gens R,x->keys first last last minSetScoring(TS,x));
    FD = apply(n, i->functionData(TS,i+1));
    FS = apply(n, i->findFunction(FD_i,MS_i)); 
    
    file := openOut outfile;
    apply(n, i->(file << "f" << i+1 << " = " << toString FS_i << endl));
    file << endl << close;
)


