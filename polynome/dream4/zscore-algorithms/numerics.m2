mean = (L) -> (sum L)//#L
stddev = (L) -> (
     m := mean L;
     var := (sum apply(L, a -> (a-m)^2))//(#L-1);
     sqrt(var)
     )
median = (L) -> if #Z % 2 == 1 then Z#((#Z-1)//2) else (Z#(#Z//2) + Z#(#Z//2-1))/2.0
quartile1 = (L) -> (m := medial L; L1 := select(L1, x -> x > m); median L1)
quartile3 = (L) -> (m := medial L; L1 := select(L1, x -> x < m); median L1)

spread = (Z) -> (
     --Z should be a list of numbers
     Z = sort Z;
     n := #Z;
     r := n % 4;
     k := n // 4;
     vals := if r === 0 then
	  {Z#0, Z#(k-1), (Z#(2*k-1) + Z#(2*k))/2.0, Z#(3*k), Z#(n-1)}
     else if r === 1 then
          {Z#0, (Z#(k-1)+Z#k)/2.0, Z#(2*k), (Z#(3*k)+Z#(3*k+1))/2.0, Z#(n-1)}
     else if r === 2 then
          {Z#0, Z#k, (Z#(2*k)+Z#(2*k+1))/2.0, Z#(3*k+1), Z#(n-1)}
     else if r === 3 then
          {Z#0, Z#k, Z#(2*k+1), Z#(3*k+2), Z#(n-1)}
     else error ("r = "|r);
     IQR := (vals#3-vals#1)*1.5;
     lo := # select(Z, x -> x < vals#1-IQR);
     hi := # select(Z, x -> x > vals#3+IQR);
     join(vals,{lo,hi,IQR,mean Z,stddev Z}))

showspread = (Zs,header) -> (
     -- Zs should be a list of values returned from spread.
     header = prepend("",header);
     Zs = transpose prepend({"min","Q1","median","Q3","max","lower outliers","upper outliers","IQR","mean","stddev"}, Zs);
     print netList(prepend(header,Zs), HorizontalSpace => 1, Boxes=>false);
     )
     

stripNull = (L) -> select(L, f -> f =!= null)
mat2m2 = (file) -> (
     F := lines get file;
     matrix apply(F, f -> stripNull toList value replace(///[ ]+///,",",f))
     )

discret = (H, mean'stddev) -> (
  apply(H#"timeseries", m -> (
	  -- discretize m via the z-score from inf_0..inf_9
	  E := entries transpose m;
	  transpose matrix toList apply(#E, r -> (
		    apply(E#r, x -> round((x-mean'stddev#r#0)/mean'stddev#r#1))))
	  )))

Zscores = (H, mean'stddev) -> (
  apply(H#"timeseries", m -> (
	  -- discretize m via the z-score from inf_0..inf_9
	  E := entries transpose m;
	  transpose matrix toList apply(#E, r -> (
		    apply(E#r, x -> ((x-mean'stddev#r#0)/mean'stddev#r#1))))
	  )))

getInfo = (H) -> (
     spreads := {};
     ans := toList apply(0..9, i -> (
	  vals := flatten apply(H#"timeseries", m -> drop(flatten entries m_{i}, 13));
	  mu := mean  vals;
	  sigma := stddev  vals;
	  spreads = append(spreads, spread vals);
	  {mu,sigma}));
     (ans, spreads)
     )
end

restart
load"dream4.m2"
load "numerics.m2"
-- read in the data from 10_1:
H = readDream4(insilicoNames#"10-1")
(MU,Zs) = getInfo H
DH = discret(H, MU)
ZH = Zscores(H, MU)
showspread(Zs,{})

perturbChanges = apply(ZH, m -> matrix for i from 0 to numColumns m - 1 list {m_(1,i) - m_(0,i), m_(11,i) - m_(10,i)})
apply(perturbChanges, m -> toList select(1..10, i -> (a := m_(i-1,0); b := m_(i-1,1); abs(a) >= 2.5 or abs(b) >= 2.5)))
apply(perturbChanges, m -> toList select(1..10, i -> (a := m_(i-1,0); b := m_(i-1,1); abs(a) >= 2.0 or abs(b) >= 2.0)))
apply(perturbChanges, m -> toList select(1..10, i -> (a := m_(i-1,0); b := m_(i-1,1); abs(a) >= 2.0 and abs(b) >= 2.0)))

-- 10_1:
--  perturb #1: gene 1, maybe 4 appear to be the objects of the perturbation
--  perturb #2: gene 7
--  perturb #3: genes 1,2,8
--  perturn #4: genes 7,8,9, maybe 3,6
--  perturn #5: genes 9, maybe 7


