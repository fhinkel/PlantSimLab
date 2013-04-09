-- Discretize values
--

inInterval = (a, intervals) -> (
     result := position(intervals, b -> a <= b);
     if result === null then #intervals-1 else result)
     
discretization = (L, intervals) -> (
     -- intervals#0, ... should be numbers in increasing size
     -- every number of L should be <= L#(#L-1).
     -- replace each number a of L with the smallest i such that a <= L#i
     apply(L, a -> inInterval(a, intervals))
     )

findIntervals = (minL, maxL, minSteady, maxSteady, nintervals) -> (
     -- return a list of length nintervals, such that minSteady and maxSteady
     -- are in the same interval, and the lengths are pretty close to each other
     if minL > maxL or minSteady > maxSteady or minL > minSteady or maxSteady > maxL 
     then error "incorrect ranges of numbers given";
     delta := (maxL - minL)/nintervals;
     epsilon := .01 * delta;
     diffSteady := maxSteady - minSteady;
     if diffSteady > 2. * delta then print "WARNING: the max and min values are very far away";
     I := for i from 1 to nintervals list (minL + i*delta);
     interval1 := inInterval(minSteady, I);
     interval2 := inInterval(maxSteady, I);
     if interval1 == interval2 then 
       I
     else (
       if diffSteady > delta then (
	    -- make one interval [minSteady,maxSteady]
	    -- somehow divide the other intervals into nintervals-1 such.
	    lowerThird := minSteady-minL;
	    upperThird := maxL-maxSteady;
	    approxDelta := (lowerThird + upperThird)/(nintervals-1);
	    nintervalsLow := round(lowerThird/approxDelta);
	    nintervalsUp := nintervals - 1 - nintervalsLow;
	    deltaLow := if nintervalsLow == 0 then 0 else lowerThird/nintervalsLow;
	    deltaUp := if nintervalsUp == 0 then 0 else upperThird/nintervalsUp;
	    if nintervalsLow == 0 then -- we need to distinguish when steady vals are in leftmost interval
	      join({maxSteady+epsilon},
		 for i from 1 to nintervalsUp list (maxSteady + i*deltaUp))
	    else if nintervalsUp == 0 then
	      join(for i from 1 to nintervalsLow-1 list (minL + i*deltaLow),
		   {minSteady-epsilon,maxL})
	    else
	      join(for i from 1 to nintervalsLow-1 list (minL + i*deltaLow),
		 {minSteady-epsilon,maxSteady+epsilon},
		 for i from 1 to nintervalsUp list (maxSteady + i*deltaUp))
	  )
       else (
	    -- difference is <= delta
       	    J := new MutableList from I;
	    center := I#interval1;
	    dist1 := center - minSteady;
	    dist2 := maxSteady - center;
	    J#interval1 = if dist1 <= dist2 then minSteady-epsilon else maxSteady+epsilon;
	    toList J
	    )
       )
     )

createDiscretization = (LL, minST, maxST, nintervals) -> (
     Ls := flatten LL;
     << "min, max values: " << min Ls << " " << max Ls << endl;
     << "min, max steady: " << minST << " " << maxST << endl;
     I := findIntervals(min Ls, max Ls, minST, maxST, nintervals);
     << "interval boundaries: " << prepend(min Ls, I) << endl;
     I
     )

discretize = (LL,I) -> (
     -- LL is a list of lists of real numbers
     -- I is a discretization interval list
     apply(LL, L -> discretization(L, I))
     )

end

restart
load "discretization.m2"

assertCompare = (input, output) ->
     assert(toString prepend(.1,findIntervals toSequence input) == toString output)

assertCompare({.1, .6, .2, .22, 5}, {.1, .199, .3, .4, .5, .6})
assertCompare({.1, .6, .19, .21, 5}, {.1, .211, .3, .4, .5, .6})
assertCompare({.1, .6, .18, .26, 5}, {.1, .179, .3, .4, .5, .6})
assertCompare({.1, .6, .19, .22, 5}, {.1, .189, .3, .4, .5, .6})
assertCompare({.1, .6, .1, .21, 5}, {.1, .211, .3075, .405, .5025, .6})
assertCompare({.1, .6, .19, .28, 5}, {.1, .189, .3, .4, .5, .6}) 
assertCompare({.1, .6, .11, .21, 5}, {.1, .211, .3, .4, .5, .6})
assertCompare({.1, .6, .11, .18, 5}, {.1, .2, .3, .4, .5, .6})
assertCompare({.1, .6, .11, .23, 5}, {.1, .231, .3225, .415, .5075, .6})
assertCompare({.1, .6, .19, .31, 5}, {.1, .189, .311, .406667, .503333, .6})
assertCompare({.1, .6, .35, .42, 5}, {.1, .2, .3, .421, .5, .6})
assertCompare({.1, .6, .14, .27, 5}, {.1, .271, .3525, .435, .5175, .6})
assertCompare({.1, .6, .16, .27, 5}, {.1, .159, .271, .38, .49, .6})
assert try assertCompare({.1, .6, .19, .41, 5}, null) else true
assert try assertCompare({.1, .6, .82, .82, 5}, null) else true


L = {.120242, .0520947, .0224399, .0237167, .00555356, 0, .0169824, .00520616, .0141109, .02028, .0103418, .0811161, .0978415, .101294, .115689, .126712, .106986, .137276, .128103, .124203, .122657, .125496, .10968, .105418, .104307, .112806, .0903438, .0811324, .11234, .0926337, .0891585, .084599, .115613, .110377, .147428, .14303, .152746, .136748, .104686, .120821, .130223, .147582, .127451, .0403419, .0168512, .0121112, .0170139, .00988514, .0096774, .00843776, .00526406, .0010271, .0086306, .0856262, .11665, .146374, .103617, .139833, .14006, .10917, .121363, .112722, .141186, .13277, .0891257, .108887, .128127, .142769, .137183, .120195, .115868, .133124, .13177, .124174, .119757, .114439, .130079, .114287, .124166, .135744, .166283, .138227, .122799, .107351, .129883, .113712, .111896, .130868, .128823, .121164, .106194, .123047, .13437, .123325, .129031, .14054, .147205, .105072, .137204, .139127, .129029, .122656, .099002, .10874, .116296}
discretize(L,5)

discretize(L,5)
discretization(L, oo)

-- for gene 1
LL = {{.666511, .325775, .177501, .183885, .0930693, .0653015, .150214, .0913323, .135856, .166701, .11701, .470882, .554509, .571774, .643746, .698862, .600229, .751684, .705819, .686314, .678587}, {.69278, .6137, .592392, .586836, .629331, .51702, .470964, .627, .52847, .511094, .488297, .643364, .617185, .802442, .780454, .829031, .749043, .588731, .669407, .716418, .803212}, {.702555, .267011, .149558, .125857, .150371, .114727, .113688, .10749, .0916218, .070437, .108454, .493432, .648552, .797172, .583389, .764467, .765601, .611151, .672118, .62891, .771232}, {.729151, .51093, .609737, .705936, .779146, .751218, .666278, .644644, .73092, .724151, .68617, .664088, .637497, .715696, .636738, .686133, .74402, .896715, .756436, .679297, .602057}, {.714716, .633862, .624781, .719644, .709418, .671123, .596274, .680538, .737151, .681927, .710457, .768001, .801327, .590661, .751322, .760936, .710449, .678581, .560312, .609001, .646779}}
findIntervals(0.0653015, .896715, .6020569, .8032117, 5)

discretize(LL,5)

