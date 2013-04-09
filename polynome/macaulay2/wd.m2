--*********************
--File Name: wd.m2
--Author: Elena S. Dimitrova
--Original Date: 3/5/2009
--Descritpion: Generates file graph.txt that contains a wiring diagram in GraphViz format. Takes multiple time series data and generates 5*(number of variables) PDS models normalized wrt G. bases under monomial orderings defined by weight vectors that are randomly selected from the G. fan of the ideal of data points. The weight of an edge x_i->x_j corresponds to the proportion of the weight vectors that generated local polynomials f_j containing x_i.
--Input: Field characteristic; number of variables; time series files
--Output: File graph.dot that contains a wiring diagram in GraphViz format
--********************* 

load "PolynomialDynamicalSystems.m2"
load "Points.m2"

wd = method();
wd(List, String, ZZ, ZZ) := (WT, outfile, pp, nn) -> (
    kk = ZZ/pp; --Field
    
    --TS is a hashtable of time series data WITH NO KO DATA
    TS = readTSData(WT, {}, kk);
    
    --FD is a list of hashtables, where each contains the input-vectors/output pairs for each node
    FD = apply(nn, II->functionData(TS, II+1));
    
    --IN is a matrix of input vectors
    IN = matrix keys first FD;
    
    allNFs={};
    
    --Sample randomly from the G. fan
    setRandomSeed processID();
    apply(5*nn, J -> (
        m=100*nn;
        w={};
        w={random(0,m)};
        apply(nn-2, II->(w=append(w,random(0,m=m-w#II));
        ));
        w=append(w,m-w#(nn-2));
        
        prms=permutations w;
        pn=random (#prms);
        w=prms#pn;
        
        --Rr is a polynomial ring in nn variables; w declares the monomial ordering
        Rr = kk[vars(53..52+nn), Weights => w];
        
        --SM is a list of standard monomials
        --LT is an ideal of leading terms
        --GB is a list of Grobner basis elements
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
    apply(nn, JJ->(
        s={};
        apply(5*nn, II->(
        s=append(s,toString (allNFs#II#JJ));
        ));
        
        --For each local polynomial f_i, count occurrences of each variable x_j
        count=0;
        lst={};
        
        apply(nn, i->(apply(5*nn,j->(if #(select("x"|i+1,s#j))-#(select("x"|i+1|"[[:digit:]]+",s#j))>0 then count=count+1 ));
            lst=append(lst,count);
            count=0));
        allCounts=append(allCounts,lst);
    ));
    
    v=toList vars(53..52+nn);
    
    --Print the output in GraphViz format
--    file = openOut "graph.dot";
--    file = openOut concatenate("graph-",first WT,".dot");
    file = openOut outfile;
    file << "digraph {" << endl;
    apply(v, q->(file << toString q << endl));
    apply(nn, j->(apply(nn, i->(if allCounts#j#i != 0 then (file << v#i << "->" << v#j << " [label=\"" << (allCounts#j#i)/(5*nn*1.) << "\"];" << endl)))));
    file << "}";
    file << close;
)
end
----------------------------------------------------- end of file---------------------------------------------------
