--*********************
--File Name: func.m2
--Author: Elena S. Dimitrova
--Original Date: 8/17/2009
--Descritpion: Generates file functs.txt that contains for each local polynomial all distinct normal forms and the corresponding proportion wrt all normal forms. Takes multiple time series data and generates 5*(number of variables) PDS models normalized wrt G. bases under monomial orderings defined by weight vectors that are randomly selected from the G. fan of the ideal of data points.
--Input: Field characteristic; number of variables; time series files
--Output: File functs.txt that contains for each local polynomial all distinct normal forms and the corresponding proportion wrt all normal forms.
--********************* 

load "PolynomialDynamicalSystems.m2"
load "Points.m2"

pp = 5; --Field characteristic (MUST come as input)
kk = ZZ/pp; --Field
nn = 10; --Number of variables (MUST come as input)


WT={"transition-1.txt",
"transition-2.txt",
"transition-3.txt",
"transition-4.txt",
"transition-5.txt",
"transition-6.txt",
"transition-7.txt",
"transition-8.txt",
"transition-9.txt",
"transition-10.txt",
"transition-11.txt",
"transition-12.txt",
"transition-13.txt",
"transition-14.txt",
"transition-15.txt",
"transition-16.txt",
"transition-17.txt",
"transition-18.txt",
"transition-19.txt",
"transition-20.txt",
"transition-21.txt",
"transition-22.txt",
"transition-23.txt",
"transition-24.txt",
"transition-25.txt",
"transition-26.txt",
"transition-27.txt",
"transition-28.txt",
"transition-29.txt",
"transition-30.txt",
"transition-31.txt",
"transition-32.txt",
"transition-33.txt",
"transition-34.txt",
"transition-35.txt",
"transition-36.txt",
"transition-37.txt",
"transition-38.txt",
"transition-39.txt",
"transition-40.txt",
"transition-41.txt",
"transition-42.txt",
"transition-43.txt",
"transition-44.txt",
"transition-45.txt",
"transition-46.txt",
"transition-47.txt",
"transition-48.txt",
"transition-49.txt",
"transition-50.txt",
"transition-51.txt",
"transition-52.txt",
"transition-53.txt",
"transition-54.txt",
"transition-55.txt",
"transition-56.txt",
"transition-57.txt",
"transition-58.txt",
"transition-59.txt",
"transition-60.txt",
"transition-61.txt",
"transition-62.txt",
"transition-63.txt",
"transition-64.txt",
"transition-65.txt",
"transition-66.txt",
"transition-67.txt",
"transition-68.txt",
"transition-69.txt",
"transition-70.txt",
"transition-71.txt",
"transition-72.txt",
"transition-73.txt",
"transition-74.txt",
"transition-75.txt",
"transition-76.txt",
"transition-77.txt",
"transition-78.txt",
"transition-79.txt",
"transition-80.txt",
"transition-81.txt",
"transition-82.txt",
"transition-83.txt",
"transition-84.txt",
"transition-85.txt",
"transition-86.txt",
"transition-87.txt",
"transition-88.txt",
"transition-89.txt",
"transition-90.txt"};

--TS is a hashtable of time series data WITH KO data
TS = readTSData(WT, {(1, "ko_1.txt"), (2, "ko_2.txt"), (3, "ko_3.txt"), (4, "ko_4.txt"), (5, "ko_5.txt"), (6, "ko_6.txt"), (7, "ko_7.txt"), (8, "ko_8.txt"), (9, "ko_9.txt"), (10, "ko_10.txt")}, kk);

--FD is a list of hashtables, where each contains the input-vectors/output pairs for each node
FD = apply(nn, II->functionData(TS, II+1));

--IN is a matrix of input vectors
IN = matrix keys first FD;

allNFs={};
functs={};
allFuncts={};

--Sample randomly from the G. fan
setRandomSeed processID();

--Choose a random radius in [0,1]
m=10*nn;
onept={}; 

--Get 10 normal numbers
for j from 1 to 5 do(
x=random 1.; y=random 1.;
onept=append(onept, sqrt(-2*log(x))*cos(2*pi*y));
onept=append(onept, sqrt(-2*log(x))*sin(2*pi*y));
);
--Get the 115th number
--t=random 2;
--if t==0 then onept=append(onept, sqrt(-2*log(x))*cos(2*pi*y)) else onept=append(onept, sqrt(-2*log(x))*sin(2*pi*y));

r=random 1.;
r=r^(1./nn);

q=0;
for j from 0 to nn-1 do (q=q+(onept#j)^2);
q=sqrt(q);
normpts={};
for j from 0 to nn-1 do (p=(onept#j)*r/q; normpts=append(normpts,p);
);


--Convert spherical to rectangular coordinates
rectcoords={};
apply(nn, i->(
j=1;
while abs(normpts#i)>j/m do j=j+1;
rectcoords=append(rectcoords, j-1); 
));

print rectcoords;

--Rr is a polynomial ring in nn variables; can declare a term order here
Rr = kk[vars(53..52+nn), Weights => rectcoords];

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

fl = "NF-"|J|"-5.txt";
file = openOut fl;
--file << "{";
apply(#allNFs, i->(file << toString allNFs#i; if i<#allNFs-1 then file << ","));
--file << "}";
file << close;
--));


end
----------------------------------------------------- end of file---------------------------------------------------