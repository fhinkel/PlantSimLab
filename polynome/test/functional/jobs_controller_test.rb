require 'test_helper'

## Tests are run in alphabetical order
class JobsControllerTest < ActionController::TestCase

  # called before every single test 
  def setup 
    # make sure files directory is there
    `touch public/perl/files/`
    @job = jobs(:one)
  end
  
  def teardown  
    unless @job.file_prefix.nil?
        prefix = "public/perl/" + @job.file_prefix
        `rm -r #{prefix}* 2> /dev/null`
    end
  end

   test "should create dummy job" do 
    assert_difference('Job.count') do 
        get :index 
    end
    #print jobs(:one).input_data 
    #print jobs(:one).to_s
    assert_response :success
   end
  
  # check prefix.done.js until var done = 1
  def wait_until_completed(prefix)  
    done_file = prefix + ".done.js"
    line = ""
    waiting = true
    while (waiting)
        File.open(done_file, 'r')  do |file| 
            line = file.gets 
        end

        assert line.match( /^var\sdone\s=\s/ ), message = "1st line in done.js did not match var"
        if line.match( /^var\sdone\s=\s1/ )
            #print "line did not match var done = 1"
            waiting = false
        elsif line.match( /^var\sdone\s=\s2/ )
           print "\nDone file matches file var = 2 (an error was printed to done
           file)\n"
           return false
        else
            sleep 1 
        end
    end
    true
  end

  def run_test_on_job( job, filename_list )
    JobsController.new.generate_output_of(job)
    @prefix = "public/perl/" + job.file_prefix

    wait_until_completed( @prefix )
    filename_list.each do |filename|  
        file = @prefix + filename
        assert  FileTest.exists?(file), "#{file} does not exist"
        number_of_lines = `wc -l < #{file}`
        number_of_lines = number_of_lines.chop.to_i
        assert( 1 <= number_of_lines )
        assert !FileTest.exists?("#{file}.dummy"), "#{file}.dummy does not exist"
    end
  end

  test "should upload input.txt" do
    run_test_on_job( @job, ".input.txt" )
  end
  
  test "should discretize data " do
    run_test_on_job( @job, ".discretized-input.txt" )
  end
  
  test "should generate wiring diagram" do
    @job.wiring_diagram = true
    run_test_on_job( @job, ".wiring-diagram." + @job.wiring_diagram_format )
  end
  
  test "should generate 10 node wiring diagram" do
    @job = jobs(:ten_node_network)
    @job.wiring_diagram = true
    run_test_on_job( @job, ".wiring-diagram." + @job.wiring_diagram_format )
  end
  
  test "should generate 40 node wiring diagram" do
    @job = jobs(:forty_node_network)
    @job.wiring_diagram = true
    run_test_on_job( @job, ".wiring-diagram." + @job.wiring_diagram_format )
  end
  
  test "should generate function file" do
    @job.show_functions = true
    run_test_on_job( @job, ".functionfile.txt" )
  end

  test "should generate state space" do
    @job.state_space = true
    run_test_on_job( @job, ".out." + @job.state_space_format )
  end
  
  test "should generate all files" do
    @job.show_discretized = true
    @job.wiring_diagram = true
    @job.show_functions = true
    @job.state_space = true
    run_test_on_job( @job, [ ".discretized-input.txt", ".wiring-diagram." +
    @job.wiring_diagram_format, ".functionfile.txt", ".out." + @job.state_space_format] )
  end
  
 test "should generate only wiring diagram for deterministic network" do 
    @job = jobs(:deterministic_wiring_diagram_only)
    run_test_on_job(@job, ".wiring-diagram." + @job.wiring_diagram_format )
 end

 test "should upload input.txt for deterministic network" do
    @job.is_deterministic = true
    run_test_on_job( @job, ".input.txt" )
  end
  
  test "should discretize data for deterministic network" do
    @job.is_deterministic = true 
    run_test_on_job( @job, ".discretized-input.txt" )
  end
  
  test "x should generate wiring diagram for deterministic network" do 
    @job.wiring_diagram = true
    @job.is_deterministic = true 
    run_test_on_job( @job, ".wiring-diagram." + @job.wiring_diagram_format )
  end
  
  test "should generate function file for deterministic network" do
    @job.show_functions = true
    @job.is_deterministic = true 
    run_test_on_job( @job, ".functionfile.txt" )
  end

  test "should generate function file with n lines for deterministic network" do
    @job.show_functions = true
    @job.is_deterministic = true 
    run_test_on_job( @job, ".functionfile.txt" )
    function_file = @prefix + ".functionfile.txt"
    number_of_functions = `wc -l < #{function_file}`
    assert_equal( (@job.nodes+1)*10, number_of_functions.chop.to_i )
  end

  test "should generate state space for deterministic network" do
    @job.state_space = true
    @job.is_deterministic = true 
    run_test_on_job( @job, ".out." + @job.state_space_format )
  end
  
  test "should generate all files for deterministic network" do
    @job.show_discretized = true
    @job.wiring_diagram = true
    @job.show_functions = true
    @job.state_space = true
    @job.is_deterministic = true 
    run_test_on_job( @job, [ ".discretized-input.txt", ".wiring-diagram." +
    @job.wiring_diagram_format, ".functionfile.txt", ".out." + @job.state_space_format] )
    assert wait_until_completed( @prefix ), "this should work without errors"
  end
  
  test "should generate all files for large deterministic network need to fix
  this bug" do
    @job = jobs(:six_node_network)
    @job.show_discretized = true
    @job.wiring_diagram = true
    @job.show_functions = true
    @job.state_space = true
    @job.is_deterministic = true
    run_test_on_job( @job, [ ".discretized-input.txt", ".wiring-diagram." +
    @job.wiring_diagram_format, ".functionfile.txt", ".out." + @job.state_space_format] )
    assert wait_until_completed( @prefix ), "this should work without errors"
  end

  test "should generate wiring diagram for large deterministic network" do
    @job = jobs(:six_node_network)
    @job.wiring_diagram = true
    @job.is_deterministic = true 
    run_test_on_job( @job, [ ".discretized-input.txt", ".wiring-diagram." +
    @job.wiring_diagram_format] )
    assert wait_until_completed( @prefix ), "this should work without errors"
  end

  test "should generate all files for large network" do
    @job = jobs(:six_node_network)
    @job.show_discretized = true
    @job.wiring_diagram = true
    @job.show_functions = true
    @job.state_space = true
    @job.is_deterministic = false 
    run_test_on_job( @job, [ ".discretized-input.txt", ".wiring-diagram." +
    @job.wiring_diagram_format, ".functionfile.txt", ".out." + @job.state_space_format] )
    assert wait_until_completed( @prefix ), "this should work without errors"
  end
  
 ############ update sequential ############ 
  
  test "should not allow stochastic model with random sequential update" do
    @job = jobs(:stochastic_with_sequential)
    JobsController.new.generate_output_of(@job)
    @prefix = "public/perl/" + @job.file_prefix
    assert !wait_until_completed( @prefix ), "generate should have returned an error"
  end

  test "should not allow stochastic model with sequential update" do
    @job = jobs(:stochastic_with_sequential)
    @job.update_schedule = "1 2 3" 
    JobsController.new.generate_output_of(@job)
    @prefix = "public/perl/" + @job.file_prefix
    assert !wait_until_completed( @prefix ), "generate should have returned an error"
  end
 
 test "should upload input.txt for random sequential network" do
    @job.is_deterministic = true
    @job.sequential = true
    run_test_on_job( @job, ".input.txt" )
  end
  
  test "should discretize data for random sequential network" do
    @job.is_deterministic = true 
    @job.sequential = true
    run_test_on_job( @job, ".discretized-input.txt" )
  end
  
  test "should generate wiring diagram for random sequential network" do 
    @job.wiring_diagram = true
    @job.is_deterministic = true 
    @job.sequential = true
    run_test_on_job( @job, ".wiring-diagram." + @job.wiring_diagram_format )
  end
  
  test "should generate function file for random sequential network" do
    @job.show_functions = true
    @job.is_deterministic = true 
    @job.sequential = true
    run_test_on_job( @job, ".functionfile.txt" )
  end

  test "should generate function file with n lines for random sequential network" do
    @job.show_functions = true
    @job.is_deterministic = true 
    @job.sequential = true
    run_test_on_job( @job, ".functionfile.txt" )
    function_file = @prefix + ".functionfile.txt"
    number_of_functions = `wc -l < #{function_file}`
    assert_equal( (@job.nodes+1)*10, number_of_functions.chop.to_i )
  end

  test "should generate state space for random sequential network" do
    @job.state_space = true
    @job.is_deterministic = true 
    @job.sequential = true
    run_test_on_job( @job, ".out." + @job.state_space_format )
  end
  
  test "should generate all files for random sequential network" do
    @job.show_discretized = true
    @job.wiring_diagram = true
    @job.show_functions = true
    @job.state_space = true
    @job.is_deterministic = true 
    @job.sequential = true
    run_test_on_job( @job, [ ".discretized-input.txt", ".wiring-diagram." +
    @job.wiring_diagram_format, ".functionfile.txt", ".out." + @job.state_space_format] )
    assert wait_until_completed( @prefix ), "this should work without errors"
  end
  
  
 test "should upload input.txt for sequential network" do
    @job.is_deterministic = true
    @job.sequential = true
    @job.update_schedule = "1 2 3" 
    run_test_on_job( @job, ".input.txt" )
  end
  
  test "should discretize data for sequential network" do
    @job.is_deterministic = true 
    @job.sequential = true
    @job.update_schedule = "1 2 3" 
    run_test_on_job( @job, ".discretized-input.txt" )
  end
  
  test "should generate wiring diagram for sequential network" do 
    @job.wiring_diagram = true
    @job.is_deterministic = true 
    @job.sequential = true
    @job.update_schedule = "1 2 3" 
    run_test_on_job( @job, ".wiring-diagram." + @job.wiring_diagram_format )
  end
  
  test "should generate function file for sequential network" do
    @job.show_functions = true
    @job.is_deterministic = true 
    @job.sequential = true
    @job.update_schedule = "1 2 3" 
    run_test_on_job( @job, ".functionfile.txt" )
  end

  test "should generate function file with n lines for sequential network" do
    @job.show_functions = true
    @job.is_deterministic = true 
    @job.sequential = true
    @job.update_schedule = "1 2 3" 
    run_test_on_job( @job, ".functionfile.txt" )
    function_file = @prefix + ".functionfile.txt"
    number_of_functions = `wc -l < #{function_file}`
    assert_equal( (@job.nodes+1)*10, number_of_functions.chop.to_i )
  end

  test "should generate state space for sequential network" do
    @job.state_space = true
    @job.is_deterministic = true 
    @job.sequential = true
    @job.update_schedule = "1 2 3" 
    run_test_on_job( @job, ".out." + @job.state_space_format )
  end
  
  test "should generate all files for sequential network" do
    @job.show_discretized = true
    @job.wiring_diagram = true
    @job.show_functions = true
    @job.state_space = true
    @job.is_deterministic = true 
    @job.sequential = true
    @job.update_schedule = "1 2 3" 
    run_test_on_job( @job, [ ".discretized-input.txt", ".wiring-diagram." +
    @job.wiring_diagram_format, ".functionfile.txt", ".out." + @job.state_space_format] )
    assert wait_until_completed( @prefix ), "this should work without errors"
  end
  
######## inconsistent data #########

  test "should upload inconsistent data" do
    @job = jobs(:inconsistent_data)
    run_test_on_job( @job, ".input.txt" )
  end
  
  test "should discretize data for inconsistent timecourse" do
    @job = jobs(:inconsistent_data)
    run_test_on_job( @job, ".discretized-input.txt" )
  end
  
  test "should generate wiring diagram for inconsistent timecourse" do
    @job = jobs(:inconsistent_data)
    @job.wiring_diagram = true
    run_test_on_job( @job, ".wiring-diagram." + @job.wiring_diagram_format )
  end
  
  test "should generate function file for inconsistent timecourse" do
    @job = jobs(:inconsistent_data)
    @job.show_functions = true
    run_test_on_job( @job, ".functionfile.txt" )
  end

  test "should generate state space for inconsistent timecourse" do
    @job = jobs(:inconsistent_data)
    @job.state_space = true
    run_test_on_job( @job, ".out." + @job.state_space_format )
  end
  
  test "should generate all files for inconsistent timecourse" do
    @job = jobs(:inconsistent_data)
    @job.show_discretized = true
    @job.wiring_diagram = true
    @job.show_functions = true
    @job.state_space = true
    run_test_on_job( @job, [ ".discretized-input.txt", ".wiring-diagram." +
    @job.wiring_diagram_format, ".functionfile.txt", ".out." + @job.state_space_format] )
  end
  
 test "should generate only wiring diagram for deterministic network for
 inconsistent timecourse" do 
    @job = jobs(:inconsistent_data)
    @job.is_deterministic = true
    @job.wiring_diagram = true
    run_test_on_job(@job, ".wiring-diagram." + @job.wiring_diagram_format )
 end

 test "should upload input.txt for deterministic network for inconsistent
 timecourse" do
    @job = jobs(:inconsistent_data)
    @job.is_deterministic = true
    run_test_on_job( @job, ".input.txt" )
  end
  
  test "should discretize data for deterministic network for inconsistent
  timecourse" do
    @job = jobs(:inconsistent_data)
    @job.is_deterministic = true 
    run_test_on_job( @job, ".discretized-input.txt" )
  end
  
  test "x should generate wiring diagram for deterministic network for
  inconsistent timecourse" do 
    @job = jobs(:inconsistent_data)
    @job.wiring_diagram = true
    @job.is_deterministic = true 
    run_test_on_job( @job, ".wiring-diagram." + @job.wiring_diagram_format )
  end
  
  test "should generate function file for deterministic network for
  inconsistent timecourse" do
    @job = jobs(:inconsistent_data)
    @job.show_functions = true
    @job.is_deterministic = true 
    run_test_on_job( @job, ".functionfile.txt" )
  end

  test "should generate function file with n lines for deterministic network
  for inconsistent timecourse" do
    @job = jobs(:inconsistent_data)
    @job.show_functions = true
    @job.is_deterministic = true 
    run_test_on_job( @job, ".functionfile.txt" )
    function_file = @prefix + ".functionfile.txt"
    number_of_functions = `wc -l < #{function_file}`
    assert_equal( (@job.nodes+1)*10, number_of_functions.chop.to_i )
  end

  test "should generate state space for deterministic network for inconsistent
  timecourse" do
    @job = jobs(:inconsistent_data)
    @job.state_space = true
    @job.is_deterministic = true 
    run_test_on_job( @job, ".out." + @job.state_space_format )
  end
  
  test "should generate all files for deterministic network for inconsistent
  timecourse" do
    @job = jobs(:inconsistent_data)
    @job.show_discretized = true
    @job.wiring_diagram = true
    @job.show_functions = true
    @job.state_space = true
    @job.is_deterministic = true 
    run_test_on_job( @job, [ ".discretized-input.txt", ".wiring-diagram." +
    @job.wiring_diagram_format, ".functionfile.txt", ".out." + @job.state_space_format] )
    assert wait_until_completed( @prefix ), "this should work without errors"
  end
  
  test "data should be consistent" do 
    run_test_on_job(@job, ".discretized-input.txt")
    discretized_datafile = Dir.getwd + "/" + @prefix + ".discretized-input0.txt"
    assert JobsController.new.data_consistent?(discretized_datafile, 2, @job.nodes)
  end

  test "data should not be consistent" do 
    @job = jobs(:inconsistent_data)
    run_test_on_job(@job, ".discretized-input.txt")
    
    discretized_datafile = Dir.getwd + "/" + @prefix + ".discretized-input0.txt"
    controller = JobsController.new
    assert !controller.data_consistent?(discretized_datafile, "2", @job.nodes)
  end
  
  test "should make data consistent" do 
    @job = jobs(:inconsistent_data)
    run_test_on_job(@job, ".discretized-input.txt")
  
    discretized_datafiles = Dir.getwd + "/" + @prefix + ".discretized-input0.txt"
    controller = JobsController.new
    controller.generate_output_of(@job)
    assert !controller.data_consistent?(discretized_datafiles, "2", @job.nodes), "Data should not be consistent before making it consistent"
    outfiles = controller.make_data_consistent(discretized_datafiles, "2", @job.nodes)
    assert outfiles, "Make data consistent did not create any files"
    #puts "outfiles: " + outfiles.to_s
    outfiles.each do |file|
      #puts file
      #puts File.open(file, 'r').read
    end
    assert controller.data_consistent?(outfiles, "2", @job.nodes), "Data should now be discretized"
  end

  test "should create file after making the data consistent" do
    @job = jobs(:inconsistent_data)
    @job.show_discretized = true
    run_test_on_job( @job,  ".discretized-input.txt" )
    
    discretized_datafile = Dir.getwd + "/" + @prefix + ".discretized-input0.txt"
    controller = JobsController.new
    controller.generate_output_of(@job)
    assert !controller.data_consistent?(discretized_datafile, "2", @job.nodes)
    assert controller.make_data_consistent(discretized_datafile, "2",
    @job.nodes)
  end
  
  test "should create empty file after making the data consistent and exit" do
    @job = jobs(:bad_inconsistent_data)
    @job.show_discretized = true
    run_test_on_job( @job,  ".discretized-input.txt" )
    
    discretized_datafile = Dir.getwd + "/" + @prefix + ".discretized-input0.txt"
    controller = JobsController.new
    controller.generate_output_of(@job)
    assert !controller.data_consistent?(discretized_datafile, "2", @job.nodes)
    assert !controller.make_data_consistent(discretized_datafile, "2",
    @job.nodes)
  end
  
  test "should make data consistent for two timecourses" do 
    @job = jobs(:inconsistent_data_two_timecourses)
    run_test_on_job(@job, ".discretized-input.txt")
    discretized_datafiles = [Dir.getwd + "/" + @prefix +
      ".discretized-input0.txt", Dir.getwd + "/" + @prefix + 
      ".discretized-input1.txt"]
    controller = JobsController.new
    controller.generate_output_of(@job)
    assert !controller.data_consistent?(discretized_datafiles, "2", @job.nodes), "Data should not be consistent before making it consistent"
    outfiles = controller.make_data_consistent(discretized_datafiles, "2", @job.nodes)
    assert outfiles, "Make data consistent did not create any files"
    assert controller.data_consistent?(outfiles, "2", @job.nodes), "Data should now be discretized"
  end

  test "should not return true for isConsistent if passed nonexisting file" do 
    discretized_datafile = Dir.getwd + "/nonexisting.txt"
    controller = JobsController.new
    assert !controller.data_consistent?(discretized_datafile, "2", 3), "should not return true if file does not exist"
  end
  
  test "should not return true for isConsistent if passed empty file" do 
    empty_file = Dir.getwd + "/empty.txt"
    `touch #{empty_file}`
    assert FileTest.exists?(empty_file)
    controller = JobsController.new
    assert !controller.data_consistent?(empty_file, "2", 3), "should not return true if file is empty"
    `rm #{empty_file}`
    assert !FileTest.exists?(empty_file)
  end

#  test "should not generate this file" do 
#    assert !run_test_on_job( @job, "not-existing-file" )
#  end

end
