#!/usr/bin/perl 

use lib '../';

$ret = `diff 1state.dot 1state.dot`;
if ($?) {
        print "ERROR 1 they should be equal: $ret \n";
        exit(1);
}
#print ".";

#$ret = `diff 2state.dot stoch.out.dot`;
#if ($?) {
#        print "ERROR 1 they should be equal: $ret \n";
#        exit(1);
#}
##print ".";
#
#$ret = `diff 2dep.dot stoch.out2.dot`;
#if ($?) {
#        print "ERROR 1 they should be equal: $ret \n";
#        exit(1);
#}
#print ".";

$ret = `diff 1state.dot 2state.dot`;
if (!$?) {
    print "ERROR 2 they should diff\n";
    print "$ret \n";
    exit(1);
}
#print ".";

# run dvd_stochastic_runner.pl to create sample files, check them against the
# already existing files. Info in the existing files can be found in README
print "Running tests\n";


# long function stochastic network should look like 3*.dot
print "Function Stochastic\n"; 
$ret = `perl ../dvd_stochastic_runner.pl 3 2 1 0 "stoch3" "gif" 1 0 0 1 1 0 file3.txt`;
if ($?) {
    print "Something wrong with runner";
    exit(1);
}
$ret = `diff 3state.dot stoch3.out.dot`;
if ($?) {
        print "ERROR 1 they should be equal: $ret \n";
        exit(1);
}
$ret = `diff 3dep.dot stoch3.out2.dot`;
if ($?) {
        print "ERROR 1 they should be equal: $ret \n";
        exit(1);
}
$ret = `diff 3state.dot stoch3.out2.dot`;
if (!$?) {
    print "ERROR 2 they should diff\n";
    print "$ret \n";
    exit(1);
}
`rm -rf stoch3*`;

print "Function Stochastic without probabilities in graph\n"; 
$ret = `perl ../dvd_stochastic_runner.pl 3 2 1 0 "stochnoprobs" "gif" 1 0 0 0 1 0 file3.txt`;
if ($?) {
    print "Something wrong with runner";
    exit(1);
}
$ret = `diff 6state.dot stochnoprobs.out.dot`;
if ($?) {
        print "ERROR 1 they should be equal: $ret \n";
        exit(1);
}
$ret = `diff 6dep.dot stochnoprobs.out2.dot`;
if ($?) {
        print "ERROR 1 they should be equal: $ret \n";
        exit(1);
}
$ret = `diff 6state.dot stochnoprobs.out2.dot`;
if (!$?) {
    print "ERROR 2 they should diff\n";
    print "$ret \n";
    exit(1);
}
`rm -rf stochnoprobs*`;
 
# function stochastic network should look like 2*.dot
print "Function Stochastic\n"; 
$ret = `perl ../dvd_stochastic_runner.pl 2 2 1 0 "stoch2" "gif" 1 0 0 1 1 0 file2.txt`;
if ($?) {
    print "Something wrong with runner";
    exit(1);
}
$ret = `diff 2state.dot stoch2.out.dot`;
if ($?) {
        print "ERROR 1 they should be equal: $ret \n";
        exit(1);
}
$ret = `diff 2dep.dot stoch2.out2.dot`;
if ($?) {
        print "ERROR 1 they should be equal: $ret \n";
        exit(1);
}
$ret = `diff 2state.dot stoch2.out2.dot`;
if (!$?) {
    print "ERROR 2 they should diff\n";
    print "$ret \n";
    exit(1);
}
`rm -rf stoch2*`;



# update stochastic network should look like 2*.dot
print "Update Stochastic\n"; 
$ret = `perl ../dvd_stochastic_runner.pl 2 2 1 1 upstoch "gif" 1 0 0 1 1 0 file2upstoch.txt`;
if ($?) {
    print "Something wrong with runner;"
}

$ret = `diff 2state.dot upstoch.out.dot`;
if ($?) {
        print "ERROR 1 they should be equal: $ret \n";
        exit(1);
}
$ret = `diff 2dep.dot upstoch.out2.dot`;
if ($?) {
        print "ERROR 1 they should be equal: $ret \n";
        exit(1);
}

$ret = `diff 2state.dot upstoch.out2.dot`;
if (!$?) {
    print "ERROR 2 they should diff\n";
    print "$ret \n";
    exit(1);
}
`rm -rf upstoch*`;


# Sequential update schedule 1_2
print "Sequential\n"; 
$ret = `perl ../dvd_stochastic_runner.pl 2 2 1 0 sequential "gif" 1 1 1_2 1 1 0 file4.txt`;
if ($?) {
    print "Something wrong with runner;"
}

$ret = `diff 4state.dot sequential.out.dot`;
if ($?) {
        print "ERROR 1 they should be equal: $ret \n";
        exit(1);
}
$ret = `diff 4dep.dot sequential.out2.dot`;
if ($?) {
        print "ERROR 1 they should be equal: $ret \n";
        exit(1);
}
$ret = `diff 4state.dot sequential.out2.dot`;
if (!$?) {
    print "ERROR 2 they should diff\n";
    print "$ret \n";
    exit(1);
}
`rm -rf sequential*`;

# deterministic 
print "Deterministic\n"; 
$ret = `perl ../dvd_stochastic_runner.pl 3 2 1 0 deterministic "gif" 1 0 0 1 1 0 file5.txt`;
if ($?) {
    print "Something wrong with runner;"
}

$ret = `diff 5state.dot deterministic.out.dot`;
if ($?) {
        print "ERROR 1 state they should be equal: $ret \n";
        exit(1);
}
$ret = `diff 5dep.dot deterministic.out2.dot`;
if ($?) {
        print "ERROR 1 they should be equal: $ret \n";
        exit(1);
}
$ret = `diff 5state.dot deterministic.out2.dot`;
if (!$?) { print "ERROR 2 they should diff\n";
    print "$ret \n";
    exit(1);
}
`rm -rf deterministic*`;


# single initial state 
print "initial state\n"; 
$ret = `perl ../dvd_stochastic_runner.pl 3 2 1 0 initial "gif" 1 0 0 1 0 1_0_1 file5.txt`;
if ($?) {
    print "Something wrong with runner;"
}

$ret = `diff 5graph.dot initial.graph.dot`;
if ($?) {
        print "ERROR 1 state they should be equal: $ret \n";
        exit(1);
}
$ret = `diff 5dep.dot initial.out2.dot`;
if ($?) {
        print "ERROR 1 they should be equal: $ret \n";
        exit(1);
}
$ret = `diff 5state.dot initial.out2.dot`;
if (!$?) { print "ERROR 2 they should diff\n";
    print "$ret \n";
    exit(1);
}
`rm -rf initial*`;

print "Everthing ok\n";
exit(0);
