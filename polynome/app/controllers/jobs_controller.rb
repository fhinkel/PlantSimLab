require 'digest/md5'
require 'ftools'
require "functionparser/partial_input"
require 'pp'


include React
include Spawn
include Macaulay

class JobsController < ApplicationController
  layout "main"

  def functionfile_name(prefix)
    "public/perl/" + prefix + ".functionfile.txt"
  end
  
  def dotfile_name(prefix)
    "public/perl/" + prefix + ".wiring-diagram.dot"
  end
    
  def graphfile_name(prefix, file_format)
    "public/perl/" + prefix + ".wiring-diagram." + file_format
  end

  
  def index
    @job = Job.new(:is_deterministic => false, :nodes => 3, :input_data => 
    "# First time course 
1.2  2.3  3.4
1.1  1.2  1.3
2.2  2.3  2.4
0.1  0.2  0.3", :input_function =>
  "f1 = x1")
    @job.save
    @error_message = params[:error_message]
  end

  def generate
    initialize_job
    generate_output_of (@job)
  end

  def initialize_job 
    if(!params || !params[:job])
      logger.info "Inside Redirect!"
      redirect_to :action => "index"
      return
    end
    
    
    # FBH if we use params and change them, then the new values will be
    # printed to the form, if we change a variable in @job, the variable on
    # the form will not change 

    if(params[:job][:input_file])
      logger.info "Reading :input_file into :input_data"
      params[:job][:input_data] = params[:job][:input_file].read
      params[:job].delete(:input_file)
    end
    if(params[:job][:input_function_file])
      logger.info "Reading :input_function_file into :input_function"
      params[:job][:input_function] = params[:job][:input_function_file].read
      params[:job].delete(:input_function_file)
    end
    @job = Job.new(params[:job])
  end

 # this one need a job to be passed (needed for testing)
  def generate_output_of ( job )
    @job = job
    generate_output
  end

  def generate_output
    # Boolean, not multistate yet  
    @p_value = "2"
    # create file prefix using md5 check sum as part of the filename
    @job.file_prefix = 'files/files-' + Digest::MD5.hexdigest( @job.input_data )
    logger.info "@job.file_prefix: "+ @job.file_prefix 
    
    if (@job.valid?)
        logger.info "job.valid? " + @job.valid?.to_s  
        # create the dummy file to avoid RouteErrors
        self.write_done_file("0", "")
    else 
        logger.info "job.valid? " + @job.valid?.to_s  
        self.write_done_file("2", "Please check the data you input.")
        return
    end


    datafile = "public/perl/" + @job.file_prefix + ".input_function.txt"
    File.open(Rails.root.join(datafile), 'w') {|file| file.write(@job.input_function) }
    
    datafile = "public/perl/" + @job.file_prefix + ".input.txt"
    File.open(Rails.root.join(datafile), 'w') {|file| file.write(@job.input_data) }

    if File.zero? Rails.root.join(datafile)
        logger.info "Zero input data"
        @error_message = "Please enter your input data or upload a file. If you do not want to use time course data and are interested in the state space, you can use <a href=\"http://dvd.vbi.vt.edu/\">DVD</a>."
        logger.info @error_message
        self.write_done_file("2", "<font color=red>" +  @error_message + "</font><br> ") 
        @error_message = ""
        return
    end

    # split is also checking the input format
    datafiles = self.split_data_into_files(datafile)
    if (!datafiles)
        # TODO make this error message nice
        self.write_done_file("2", "<font color=red>" +  @error_message + "</font><br> ") 
        @error_message = ""
        return 
    end
    
    # create the dummy file to avoid RouteErrors
    self.write_done_file("0", "")

    n_react_threshold = 5 
    n_simulation_threshold = 10
    
    ## All checking of any input should be done before we spawn, so the user
    #receives feedback about invalid options right away and not after some time
    # ( = everything) 
    if !@job.is_deterministic && @job.nodes > n_simulation_threshold && @job.state_space
      @error_message = "A stochastic model with more than #{n_simulation_threshold} nodes cannot be simulated.  Sorry!"
      self.write_done_file("2", "<font color=red>" +  @error_message + "</font><br> ")
      @error_message = ""
      return
    end
    
    # check for correct input options, don't set any options here.
    logger.info "Sequential update: " + @job.sequential.to_s
    if (@job.sequential)
        logger.info "Update_schedule :" +@job.update_schedule + ":"
        if ( !@job.is_deterministic )
            logger.info "Not deterministic"
            @error_message = "Sequential updates can only be chosen for deterministic models. Exiting"
            self.write_done_file("2", "<font color=red>" +  @error_message+ "</font><br> ") 
            return
        end
        if ( @job.update_schedule == "")
            logger.info "Update sequential but no schedule given, doing
            sequential udpate with random update schedule"
        end
    end
   

    if ( @job.state_space && @job.nodes > n_simulation_threshold )
        logger.info "Too many variables to simulate"
        @error_message = "Too many variables to simulate, running all computations but the simulations. "
        @job.state_space = false
        self.write_done_file("0", "<font color=red>" +  @error_message+ "</font><br> ") 
    end
        
    # overwrite functions with given functions
    input_function_file = "public/perl/" + @job.file_prefix + ".input_function.txt"
    
    unless File.zero?(Rails.root.join(input_function_file))
      if FileTest.exists?(Rails.root.join(input_function_file))
        partial_input = Hash.new
        partial_input = PartialInput.parse_into_hash File.open(Rails.root.join(input_function_file))
        logger.info "##########"
        logger.info partial_input 

        if partial_input.nil? 
          @error_message = "Partial information about input functions incorrect."
          logger.warn "Error: partial information about input functions incorrect"
          self.write_done_file("2", "<font color=red>" +  @error_message + "</font><br> ")
          @error_message = ""
          return 
        end
      end
    end

    spawn do
      #TODO this will change to a single new filename, waiting for Brandy
      discretized_datafiles = datafiles.collect do |datafile|
        datafile.gsub(/input/, 'discretized-input')
      end

      self.discretize_data(datafiles, discretized_datafiles, @p_value)

      #concatenate_discretized_files
      first = TRUE
      File.open(Rails.root.join("public/perl/" + @job.file_prefix +
      ".discretized-input.txt"), 'w') {
          |f| discretized_datafiles.each do |datafile|
              unless (first)
                  f.write("#\n")
              end
              first = FALSE
              f.write(File.open(Rails.root.join(datafile), 'r').read)
          end
      }

      # react: n <= n_react_threshold, is_deterministic
      # simulation of stochastic network n <= n_simulation_threshold 

      if @job.state_space
        @job.show_functions = true
      end
      
      if !@job.show_functions && !@job.wiring_diagram
        self.write_done_file("1", "Thats all folks!")
        return
      end
      
      do_wiring_diagram_version = @job.wiring_diagram && !@job.show_functions 
      # if function file is not needed, then run shorter version of minset/sgfan
      #    which produces a wiring diagram but not a function file.
   
      generate_picture = false
      if do_wiring_diagram_version 
        if @job.nodes <= n_react_threshold
          if !data_consistent?(discretized_datafiles, @p_value, @job.nodes)
            logger.info "inconsistent"
            run_react(@job.nodes, @job.file_prefix, discretized_datafiles)
            generate_picture = true
          else
            logger.info "consistent"
            self.generate_wiring_diagram(discretized_datafiles,
                @job.wiring_diagram_format, @p_value, @job.nodes)
          end
        else  
          if !data_consistent?(discretized_datafiles, @p_value, @job.nodes)
               discretized_datafiles = self.make_data_consistent(discretized_datafiles, @p_value, @job.nodes)
               if (!discretized_datafiles)
                 return 
               end
          end
          logger.info "Minsets generate wiring diagram"
          self.minsets_generate_wiring_diagram(discretized_datafiles,
             @job.wiring_diagram_format, @p_value, @job.nodes)
        end
      else
        @functionfile_name = self.functionfile_name(@job.file_prefix)
        if @job.is_deterministic
          if @job.nodes <= n_react_threshold
             run_react(@job.nodes, @job.file_prefix, discretized_datafiles)
             generate_picture = true
          else
             if !data_consistent?(discretized_datafiles, @p_value, @job.nodes)
               discretized_datafiles = self.make_data_consistent(discretized_datafiles, @p_value, @job.nodes)
               if (!discretized_datafiles)
                 return 
               end
             end
             self.minsets_generate_functionfile(discretized_datafiles, @functionfile_name, @p_value, @job.nodes)           
             generate_picture = true
          end
        else # stochastic model 
          #if @job.nodes <= n_simulation_threshold
            if !data_consistent?(discretized_datafiles, @p_value, @job.nodes)
               discretized_datafiles = self.make_data_consistent(discretized_datafiles, @p_value, @job.nodes)
               if (!discretized_datafiles)
                 return 
               end
            end
            self.sgfan(discretized_datafiles, @p_value, @job.nodes)
            generate_picture = true
          #else
            #logger.warn("internal error: should not be here because we checked for this error before")
          #end
        end

        if !partial_input.nil? 
          logger.info "Partial Function information"
          functionfile_name = self.functionfile_name(@job.file_prefix)
          new_functions = PartialInput.overwrite_file(partial_input, File.open(Rails.root.join(functionfile_name)))
          logger.info "###############"
          logger.info new_functions

          # write functions to functionfile
          logger.info functionfile_name
          File.open(Rails.root.join(functionfile_name), 'w') { |out| 
            new_functions.each {|line| 
              out << line
              logger.info line
            }
          }
          
          multiple_functionfile = Rails.root.join(functionfile_name.gsub("functionfile", "multiplefunctionfile"))
          if FileTest.exists?(multiple_functionfile)
            logger.info "Multiple file exists #{multiple_functionfile}"
            new_multifile = PartialInput.overwrite_file(partial_input, File.open(multiple_functionfile))
            File.open(multiple_functionfile, 'w') { |out| new_multifile.each {|line| out << line}}
          end
        end
      end
      
      if generate_picture 
        # run simulation
        logger.info "Starting simulation of state space."
          
        show_probabilities_state_space = @job.show_probabilities_state_space ?  "1" : "0"
        wiring_diagram = @job.wiring_diagram ? "1" : "0"
        logger.info("wiring_diagram #{wiring_diagram}")
        state_space = @job.state_space ? "1" : "0"

        # for synchronous updates or stochastic sequential updates
        if (!@job.sequential || @job.update_schedule == "" )
            @job.update_schedule ="0"
        end
        
        # for stochastic sequential updates, sequential has to be set to 0
        # for dvd_stochastic_runner.pl to run correctly 
        stochastic_sequential_update = "0"
        if (@job.sequential && @job.update_schedule == "0")
            stochastic_sequential_update = "1"
            @job.sequential = false
        end
        
        sequential = @job.sequential ? "1" : "0"

        # concatenate update schedule into one string with _ as separators
        # so we can pass it to dvd_stochastic_runner.pl
        @job.update_schedule = @job.update_schedule.gsub(/\s+/, "_" )
        logger.info "Update Schedule :" + @job.update_schedule + ":"

        functionfile_name = self.functionfile_name(@job.file_prefix)
        logger.info "Functionfile : " + functionfile_name

        logger.info "cd #{Rails.root}; perl #{Rails.root.join('public/perl/dvd_stochastic_runner.pl')} #{@job.nodes} #{@p_value.to_s} 1 #{stochastic_sequential_update} #{Rails.root.join('public/perl',@job.file_prefix)} #{@job.state_space_format} #{@job.wiring_diagram_format} #{wiring_diagram} #{state_space} #{sequential} #{@job.update_schedule} #{show_probabilities_state_space} 1 0 #{Rails.root.join(functionfile_name)}"
        
        simulation_output = `cd #{Rails.root}; perl #{Rails.root.join('public/perl/dvd_stochastic_runner.pl')} #{@job.nodes} #{@p_value.to_s} 1 #{stochastic_sequential_update} #{Rails.root.join('public/perl',@job.file_prefix)} #{@job.state_space_format} #{@job.wiring_diagram_format} #{wiring_diagram} #{state_space} #{sequential} #{@job.update_schedule} #{show_probabilities_state_space} 1 0 #{Rails.root.join(functionfile_name)}`

        logger.info "simulation output: " + simulation_output 
        simulation_output = simulation_output.gsub("\n", "") 

        multiple_functionfile = Rails.root.join(functionfile_name.gsub("functionfile", "multiplefunctionfile"))
        logger.info multiple_functionfile
        if FileTest.exists?(multiple_functionfile)
          logger.info "Multiple file exists"


          # after simulating just one network copy the multiple networks into
          # the function file that the user can display
          File.copy(multiple_functionfile, Rails.root.join(functionfile_name) )
        end
      end
      self.write_done_file("1",  simulation_output)
    end
  end
 
  def write_done_file(done, simulation_output)
    # Tell the website we are done
    `echo 'var done = #{done}' > #{Rails.root.join('public/perl', @job.file_prefix + '.done.js')}`;
    `echo "var simulation_output = '#{simulation_output}'" >> #{Rails.root.join('public/perl', @job.file_prefix + '.done.js')}`;
  end
  
  # TODO FBH: This function is doing the checking at the moment, should
  # probably restructure 
  # We won't need this function anymore as soon as Brandy has rewritten M2
  # code to accept single file with #
  # This should only do the error checking! 
  def split_data_into_files(datafile)

    datafiles = []
    output = NIL
    File.open(Rails.root.join(datafile)) do |file| 
        counter = 0
        something_was_written = FALSE
        while line = file.gets 
            # parse lines and break into different files at #
            if( line.match( /^\s*\#+/ ) )
                if (something_was_written && output) 
                    output.close
                    output = NIL
                end
                something_was_written = FALSE
            else 
                if (!something_was_written) 
                    outputfile_name = datafile.gsub(/input/,"input" +
                    counter.to_s)
                    counter +=1
                    output = File.open(Rails.root.join(outputfile_name), "w") 
                    datafiles.push((Rails.root.join(outputfile_name)).to_s)
                    #datafiles.push( "../" + outputfile_name)
                    #datafiles.push(Dir.getwd + "/" + outputfile_name)
                end
                # check if line matches @n_nodes digits
                nodes_minus_one = (@job.nodes - 1).to_s
                if (line.match( /^\s*(\.?\d+\.?\d*\s+){#{nodes_minus_one}}\.?\d+\.?\d*\s*$/ ) ) 
                    output.puts line
                    logger.info "write line" + line
                    something_was_written = TRUE
                else
                    @error_message = "The data you entered is invalid. This :#{line.chop!}: is not a correct line."
                    logger.warn "Error: Input data not correct. This :#{line}: is not a correct line."
                    return NIL
                end
            end
        end 
        file.close
        if (output) 
            output.close
        end
    end
    return datafiles
  end
 


 # 
  def discretize_data(infiles, outfiles, p_value)    
    # infiles: list of input file names to be discretized together
    # outfiles: the names of the output discretized files.  The length
    #    of infiles and outfiles should be identical.
    macaulay2(
      :m2_command => "discretize(#{m2_string(infiles)}, #{m2_string(outfiles)}, #{p_value})",
      :m2_file => "Discretize.m2",
      :m2_wait => 1
      )
  end
  
  def generate_wiring_diagram(discretized_data_files, file_format, p_value, n_nodes)
    dotfile = self.dotfile_name(@job.file_prefix)
    graphfile = self.graphfile_name(@job.file_prefix, file_format)

    logger.info macaulay2(
      :m2_command => "wd(#{m2_string(discretized_data_files)}, ///../#{dotfile}///, #{p_value}, #{n_nodes})",
      :m2_file => "wd.m2",
      :post_m2_command => "dot -T #{file_format} -o #{Rails.root.join(graphfile)} #{Rails.root.join(dotfile)}",
      :m2_wait => 1
      )
  end

  def minsets_generate_wiring_diagram(discretized_data_files, file_format, p_value, n_nodes)
    dotfile = self.dotfile_name(@job.file_prefix)
    graphfile = self.graphfile_name(@job.file_prefix, file_format)
    logger.info dotfile
    logger.info graphfile
    logger.info discretized_data_files 

    macaulay2(
      :m2_command => "minsetsWD(#{m2_string(discretized_data_files)}, ///../#{dotfile}///, #{p_value}, #{n_nodes})",
      :m2_file => "minsets-web.m2",
      :post_m2_command => "dot -T #{file_format} -o #{graphfile} #{dotfile}",
      :m2_wait => 1
      )
  end
  
  def data_consistent?(discretized_data_files, p_value, n_nodes)
    ret_val = macaulay2(
      :m2_command => "isConsistent(#{m2_string(discretized_data_files)}, #{p_value}, #{n_nodes})",
      :m2_file => "isConsistent.m2",
      :m2_wait => 1
      )
    # 0 inconsistent
    # 1 consistent
    logger.info "data is consistent returned " + ret_val + ", 0 inconsistent,
    42 consistent"
    logger.info "return #{ret_val == "42"}"
    ret_val == "42" 
  end

  def make_data_consistent(infiles, p_value, n_nodes)
    logger.info("in make_data_consistent")
    consistent_datafile = "public/perl/" + @job.file_prefix + ".consistent-input.txt"
    macaulay2(
      :m2_command => "makeConsistent(#{m2_string(infiles)}, #{n_nodes}, ///../#{consistent_datafile}///)",
      :m2_file => "incons.m2",
      :m2_wait => 1
      )
    if (File.zero?(consistent_datafile))
       logger.info "Time course was bad and after throwing out inconsistent time courses nothing was left. Maybe you need to choose a different type of model."
       @error_message  = "Time course was bad and after throwing out inconsistent time courses nothing was left. Maybe you need to choose a different type of model."
       self.write_done_file("2", "<font color=red>" +  @error_message + "</font><br> ") 
       @error_message = ""
       return NIL 
    end
    outfiles = self.split_data_into_files(consistent_datafile)
    if (!outfiles)
      # TODO make this error message nice
      @error_message = "The data you entered is invalid, after discretization data did not get split correclty."
      self.write_done_file("2", "<font color=red>" +  @error_message + "</font><br> ") 
      @error_message = ""
      return NIL
    end
    logger.info "outfiles: " + outfiles.to_s
    outfiles.each do |file|
      logger.info file
      logger.info File.open(Rails.root.join(file), 'r').read
    end
    outfiles
  end

  def sgfan(discretized_data_files, p_value, n_nodes)
    functionfile = self.functionfile_name(@job.file_prefix)
    logger.info "calling sgfan!"
    macaulay2(
      :m2_command => "sgfan(#{m2_string(discretized_data_files)}, ///../#{functionfile}///, #{p_value}, #{n_nodes})",
      :m2_file => "func.m2",
      :m2_wait => 1
      )
    functionfile
  end
  
  def minsets_generate_functionfile(discretized_data_files, functionfile, p_value, n_nodes)
    logger.info discretized_data_files 
    logger.info functionfile + "in minsets_generate_functionfile"

    macaulay2(
      :m2_command => "minsetsPDS(#{m2_string(discretized_data_files)}, ///../#{functionfile}///, #{p_value}, #{n_nodes})",
      :m2_file => "minsets-web.m2",
      :m2_wait => 1
      )
  end
end
