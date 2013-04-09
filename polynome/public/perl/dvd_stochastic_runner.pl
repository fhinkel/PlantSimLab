#!/usr/bin/perl

## dvd_stochasitic_runner.pl
## Multi-function, stochastic DVD Processing
## This script takes a number of nodes, the number of states, and the filename for the input file
## Functions can be read from the input file with the possiblity for more than one function per node
## Authored by Jonte Craighead and Franziska Hinkelmann
#
# If this script is run, Stochastic is hard coded to 1, so we always get a
# probabilities in the graph (maybe this should be changed?)
#
# The all_trajectories_flag set means all possible arrows are drawn in one
# graph, without this option, one update is choosen at random for every
# variables - the program should always be run with the flag turned on, since
# this produces the phase space, the other option produces one possible
# trajectory for each state but not a simulation starting from one state
# 
# update_stochastic_flag set means that we treat the system as an update
# stochastic systems (the udpate schedule is random). This is simulated by
# using a function stochastic system where each function set has two members:
# the local update function and the identity. The probabilities are set such
# that (nodes-1) functions are delayed and only one function is updated. If
# the user gives a family of update functions for one node, an error is
# returned, because a function and update stochastic system is not
# allowed. 

use strict;

use lib './public/perl';

use DVDCore qw($Clientip $Function_data $Function_file &error_check @Output_array $Pwd &dvd_session &_log $Stochastic);
use Cwd;
use Getopt::Std;
getopts('vhd');
 
our ($opt_d, $opt_v, $opt_h);
my $DEBUG=$opt_d;
my $MINDEBUG=$opt_v;

if ($DEBUG) {
    $MINDEBUG = 1;
}

if ($DEBUG) {print getcwd;}
if ($MINDEBUG) {print "Using Minimum Debug output for dvd_stochastic_runner.pl<br>\n";}
if ($DEBUG) { print "<br>Number of arguments (should equal 12) " . $#ARGV .  "<br>"; }

die "Usage: dvd_stochasitic_runner.pl [-vdh] #nodes #states all_trajectories_flag update_stochastic_flag outputfilename statespace_format wiring_diagram_format show_wiring_diagram show_statespace update_sequential_flag update_schedule Probabilities_in_graph_flag trajectory_flag trajectory_value inputfile.txt \n\t-v minimal debug output \n\t-d debug output \n\t-h  this help\n" if ($opt_h || $#ARGV != 14);

my $n_nodes = $ARGV[0]; #number of variables
my $p_value = $ARGV[1]; #number of states
my $all_trajectories_flag= $ARGV[2];  # This flag set means all possible arrows are drawn in one
    # graph, without this option, one update is choosen at random for every
    # variables
my $update_stochastic_flag=$ARGV[3]; 	#if set, an update stochastic system is simulated using
    #random delays
my $file_prefix = $ARGV[4]; #outputfiles
my $statespace_format = $ARGV[5]; #graph format
my $wiring_diagram_format = $ARGV[6];
my $show_wiring_diagram = $ARGV[7]; #on if wiring diagram should be graphed
my $show_statespace = $ARGV[8]; #show_statespace 1 means create picture
my $update_sequential_flag = $ARGV[9]; #1 if sequential update, has to be set to 0 for random sequential updates (i.e., update_stochastic_flag == 1 )
my $update_schedule = $ARGV[10]; #update_schedule
my $Stochastic = $ARGV[11]; 	# if set to one, probabilities are included in graph of state space 
my $trajectory_flag = $ARGV[12]; # 1 if all trajectories, 0 for a single trajetory form intitial state trajectory_value
my $trajectory_value = $ARGV[13]; # initial state
my $stochastic_input_file = $ARGV[-1]; 

if ($DEBUG) {print "All trajectories flag, 1 if all trajectories, 0 for a single initial state: $trajectory_flag \n <br>"; }

if ($MINDEBUG) {print "Number of nodes $n_nodes <br>
    P_value :$p_value: <br>
    file_prefix :$file_prefix: <br>
    Statespace format :$statespace_format: <br>
    Wiring diagram format :$wiring_diagram_format:<br>
    Show Wiring diagram :$show_wiring_diagram:<br>
    Show Statespace :$show_statespace: <br>
    Functionfile :$stochastic_input_file: <br>
    Update sequential :$update_sequential_flag: <br>
    Update schedule :$update_schedule: <br>
    Update stochastic :$update_stochastic_flag: <br> 
    show_probabilities_state_space :$Stochastic: <br>";}

open(my $function_file, $stochastic_input_file);
_log("Attempted to read from '$stochastic_input_file'");

my $Pwd = getcwd();

my @response = dvd_session($n_nodes, $p_value, $file_prefix, 0, $update_sequential_flag, $update_schedule, $all_trajectories_flag, $show_statespace, $statespace_format, $show_wiring_diagram, $wiring_diagram_format, $trajectory_flag, $trajectory_value, $update_stochastic_flag, $Stochastic, $DEBUG, $function_file); 

if($response[0] == 1) { # a response code should always be returned by the main DVDCore functions
    _log($_) foreach(@Output_array);
    if ($trajectory_flag) {
        print "Number of components $Output_array[2]<br>";
        print "Number of fixed points $Output_array[3]<br>";
        print "$Output_array[5]<br>";
    }
} 
else {
    ### FBH TODO if show_statespace is false, DVD session returns a 0 as
    ### $response[0], what does that mean? should we return a 1 or should we check
    ### for == 0? state_space
    #print "Does this mean error?" .  $_ ."\n" foreach(@response);
}
