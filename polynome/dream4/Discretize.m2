--*********************
--File Name:	Discretize.m2
--Author: 	Elena S. Dimitrova
--Original Date:3/8/2009
--Edited by:	Brandy Stigler
--Edit Date:	8/11/09
--Description: 	Booleanizes the input data based on our discretization method. 
--Input: 	Number of variables; time series files; output file name(s)
--Output: 	File(s) with the booleanized data:
--		If a single file is input/output, then multiple time series are separated by hash marks (#)
--Usage:        3 ways to call "discretize"
--1:		discretize(data_file_list, num_nodes, outfile)
--2:		discretize(data_file_list, num_nodes, outfile_list)
--3:		discretize(infile, num_nodes, outfile)
--		***Last way is intended for Polynome***
--********************* 

needs "PolynomialDynamicalSystems.m2"

discretize = method()

discretize(List, ZZ, String) := (WT, n, filename) -> (
    m:={};
    discrm:={};
    count:={};
    mat := apply(WT, s -> readRealMat(s,RR));
    
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
    file:=openOut filename;
    for f from 0 to #WT-1 do (
	file << "#TS" << toString(f+1) << endl;
        for i from 0 to count#f-1 do (
            for j from 0  to #((transpose discrm)#(c+i))-1 do (
                file<<(transpose discrm)#(c+i)#j<<" ";
            );
            file << endl; 
        );
        c=c+count#f;
    );
    file<<close;
)

discretize(List, ZZ, List) := (WT, n, WToutputfiles) -> (
    m:={};
    discrm:={};
    count:={};
    mat := apply(WT, s -> readRealMat(s,RR));
    
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

discretize(String, ZZ, String) := (infile, n, outfile) -> (
    m:={};
    discrm:={};
    count:={};
    mat := flatten values readRealTSData(infile,RR);
    
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
    file:=openOut outfile;
    for f from 0 to #mat-1 do (
	file << "#TS" << toString(f+1) << endl;
        for i from 0 to count#f-1 do (
            for j from 0  to #((transpose discrm)#(c+i))-1 do (
                file<<(transpose discrm)#(c+i)#j<<" ";
            );
            file << endl; 
        );
        c=c+count#f;
    );
    file<<close;
)

end

load discretize({"insilico_size10_1_timeseries-1.txt","insilico_size10_1_timeseries-2.txt","insilico_size10_1_timeseries-3.txt","insilico_size10_1_timeseries-4.txt","insilico_size10_1_timeseries-5.txt","insilico_size10_1_wildtype.txt","insilico_size10_1_knockout-1.txt","insilico_size10_1_knockout-2.txt","insilico_size10_1_knockout-3.txt","insilico_size10_1_knockout-4.txt","insilico_size10_1_knockout-5.txt","insilico_size10_1_knockout-6.txt","insilico_size10_1_knockout-7.txt","insilico_size10_1_knockout-8.txt","insilico_size10_1_knockout-9.txt","insilico_size10_1_knockout-10.txt"}, 10, "size10-1-Bool.txt")
----------------------------------------------------- end of file---------------------------------------------------
