--*********************
--File Name: Discretize.m2
--Author: Elena S. Dimitrova
--Original Date: 3/8/2009
--Descritpion: Booleanizes the input data based on our discretization method. ereadMat is a modification of readMat from the PolynomialDynamicalSystems package.
--Input: Number of variables; time series files
--Output: Files named "Bool-original filename.txt" with the booleanized data
--Usage:        discretize(dataFileList, n)
--   Ex:        discretize({"toy.txt"}, 3)
--   Returns:   nothing <"Bool-toy.txt" created>
--********************* 

-- Given a data file and a coefficient ring, ereadMat returns the (txn)-matrix of the data (t=time, n=vars). 

ereadMat := method(TypicalValue => Matrix)
ereadMat(String,InexactFieldFamily) := (filename,R) -> (
     ss := select(lines get filename, s -> length s > 0);
     matrix(R, apply(ss, s -> (t := separateRegexp(" +", s); 
                 t = apply(t,value);
                     select(t, x -> class x =!= Nothing))))
)

discretize = method()
discretize(List, List, ZZ) := (WT, WToutputfiles, n) -> (
    m:={};
    discrm:={};
    count:={};
    mat := apply(WT, s -> ereadMat(s,RR));
    
    apply(#mat, s -> (
        m = append(m, entries mat#s); 
        count=append(count, #(entries mat#s))
    ));
    
    m=transpose flatten m;
    
    for j from 0 to #m-1 do (
        dis:={}; 
        msort:=sort m#j;
        d:=0;
        for i from 1 to #msort-1 do (
            if d<abs(msort#(i)-msort#(i-1)) then (d=msort#(i-1)) 
        );
        apply(#msort, s->(
            if m#j#s <= d then dis=append(dis, 0) 
            else dis=append(dis, 1)
        ));
        discrm=append(discrm, dis);
    );
    c:=0;
    for f from 0 to #WT-1 do (
        file:=openOut WToutputfiles#f;
        for i from 0 to count#f-1 do (
            for j from 0  to #((transpose discrm)#(c+i))-1 do (
                file<<(transpose discrm)#(c+i)#j<<" ";
            );
            file << endl; 
        );
        c=c+count#f;
        file<<close;
    )
)


end
----------------------------------------------------- end of file---------------------------------------------------
