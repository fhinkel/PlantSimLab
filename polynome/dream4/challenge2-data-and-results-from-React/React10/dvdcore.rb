
require 'pp'

class DVDCore < Struct.new(:file_prefix, :nodes, :pvalue)  
  FUNCTIONFILE_SUFFIX = ".functionfile.txt"
  WIRINGDIAGRAM_DOT_SUFFIX = ".wiring_diagram.dot"
  STATESPACE_DOT_SUFFIX = ".state_space.dot"
  
  def debug_dump(obj)
    pp obj
  end
  
  def error_log(msg)
    # TODO: Make this log somewhere useful
    puts msg
  end
    
  def run
    @function_data = Array.new
    @functions = Array.new
    
    load_function_data
    #| debug_dump @function_data
    
    if !valid_data?
      error_log "ERROR: Can't do anything without valid data."
      return false
    end
    
    #| debug_dump @functions
      
    if true # show_wiring_diagram
      generate_wiring_diagram_dot_file
    end
      
    if false # update_stochastic
      set_update_stochastic
    end  
      
    if false # update_sequential
      if update_schedule.nil? || update_schedule.empty?
        error_log "ERROR: Can't do sequential update with empty update schedule."
        return false
      end
      update_schedule = sanitize_input(update_schedule)
      # TODO: _check_and_set_update_schedule(update_schedule)
    end
      
    if true # all_trajectories
      # TODO: generate_state_space_dot_file_without_simulation(statespace)
    else
      if initial_state.nil? || initial_state.empty?
        error_log "ERROR: Can't simulate a single trajectory without an initial state."
        return false
      end
      initial_state = sanitize_input(initial_state)
      # TODO: sim(initial_state, update_sequential, update_schedule, statespace)
    end
  end
  
  def sanitize_input(data)
    data.strip! # strip whitespace from beginning and end of strings
    data.gsub!(/(\d+)\s+/, "\\1 ") # strip extra whitespace between digits
    data.gsub!(/ /, "_") # replace spaces with an underscore
  end
  
  # identity has to be added to simulate a delay and the probabilites
  # have to be set
  def set_update_stochastic
    for count in 1..nodes
      # adding identity
      @functions[count - 1].push make_func_obj("x#{count}", "")
      
      # probability for using update function on node $count, should be
      # 1/n_nodes
      @functions[count - 1][0][:probability] = 1.0 / nodes.to_i
      
      # all the other times delays should be used
      @functions[count - 1][1][:probability] = 1 - @functions[count - 1][0][:probability];
    end
  end
  
  def generate_state_space_dot_file_without_simulation    
    f = File.new(file_prefix + STATESPACE_DOT_SUFFIX, "w")
    f.puts "digraph test {"
    
    fixed_points = Array.new
    
    # recu( 0, 1 ) -- does this need fixed_points?
    
    for x in 0..(pvalue**nodes-1)
      for y in 0..(pvalue**nodes-1)
        
        
#              if ( scalar( $Adj[$x][$y] ) > 0 ) {
                
#                  if ( $x == $y ) {    #fixed point
                                       # add state number and probability
#                      push( @fixed_points, [ $x, $Adj[$x][$y] ] );
#                  }
#                  if ( !$Show_probabilities_in_state_space ) {    
                          # make an edge from @y to @ans
                          # graph with arrows without probablities
#                      print $Dot_file "node$x -> node$y\n";
#                  }
#                  else {    # graph probablities
#                      printf $Dot_file "node$x -> node$y [label= \"%.2f",
#                          $Adj[$x][$y];
#                      printf $Dot_file "\"];\n";
#                  }
#              }


      end
    end
    
    f.puts "}"
    f.close
  end
 

  def generate_wiring_diagram_dot_file
    # put output in an array so we can sort/uniq it
    output = Array.new 
    
    output.push "digraph test {"
    
    # loop over function families and create the nodes
    for ind in (1..@functions.length)
      output.push "node#{ind.to_s} [label=\"x#{ind.to_s}\", shape=\"box\"];"
    end
    
    # loop over all functions and create a list of all transitions
    ind = 1
    @functions.each do |function_family|
      function_family.each do |function|
        vars = Hash.new
        
        # find all x\d variables in the raw function
        matches = function[:raw].scan(/x(\d+)/)
        if ( matches.size ) 
          # store the max added to the hash so we can 
          # simulate what scalar(@var) would do in Perl
          max = 0
          matches.each do |match|
            i = match[0].to_i
            vars[ i ] = i
            max = i > max ? i : max
          end
          
          # we add +1 here as scalar(@var) in Perl is the
          # largest index in the array + 1
          for j in (0..max+1)
            if !vars[ j ].nil?
              from = vars[ j ]
              output.push "node#{from} -> node#{ind};"
            end
          end # end for loop
          
        end  # end if matches.size
        
      end # end function_family.each
      
      ind = ind + 1
      
    end # end @functions.each
    
    output.push "}"
    
    # remove duplicates and sort
    output.sort!.uniq!
    
    #| debug_dump output
    
    # write *.wiring_diagram.dot file
    f = File.new(file_prefix + WIRINGDIAGRAM_DOT_SUFFIX, "w")
    f.puts output.join("\n")
    f.close
  end
  
  ## observe wiring diagram dependencies in 2 d array. The array can be used to
  ## find union/intersection of edges for multiple models 
  # output[fi][xj] = 1 if there's an edge in the dependency graph xj ->
  # xi
  # for multiple models output[fi] can be added up to find the variables
  # that show up for fi in every model
  # FBH
  def observe_dependencies_in_array
   
    # initialize double array with 0s
    output = Array.new(@functions.length)
    for i in 1 .. @functions.length do 
      output[i-1] = Array.new(@functions.length, 0)
    end
    
    
    # loop over all functions and create a list of all transitions
    ind = 1
    @functions.each do |function_family|
      function_family.each do |function|
        vars = Hash.new
        
        # find all x\d variables in the raw function
        matches = function[:raw].scan(/x(\d+)/)
        if ( matches.size ) 
          # store the max added to the hash so we can 
          # simulate what scalar(@var) would do in Perl
          max = 0
          matches.each do |match|
            i = match[0].to_i
            vars[ i ] = i
            max = i > max ? i : max
          end
          
          # we add +1 here as scalar(@var) in Perl is the
          # largest index in the array + 1
          for j in (0..max+1)
            if !vars[ j ].nil?
              from = vars[ j ]
             output[ind-1][from-1] = 1
              #output.push "node#{from} -> node#{ind};"
            end
          end # end for loop
          
        end  # end if matches.size
        
      end # end function_family.each
      
      ind = ind + 1
      
    end # end @functions.each

    output.each do |function|
      print function 
      puts ""
    end
    output
  end

  def valid_data?
    function_count = 0
    @function_data.each do |functions|
      functions.each do |line|
        # split the probability away from the function
        (func, probability) = line.split("#")
        
        # make sure probability is > 0 and <= 1
        # if not, then assume equal distribution of probability
        probability = probability.to_f
        if ( probability <= 0 || probability > 1 )
          probability = 1 / functions.length
        end
        
        # strip whitespace (both inside and outside the string)
        func.gsub!(/\s*/, "")
        
        # check to see if the function is empty (is this possible even?)
        if ( func.empty? )
          error_log "ERROR: Empty function on " + (function_count+1).to_s
          return false
        end
        
        # check for unacceptable characters
        if ( !func.match( /[^(x)(\d)(\()(\))(\+)(\-)(\*)(\^)]/ ).nil? )
          error_log "ERROR: Found unacceptable character(s) in line"
          return false
        end
        
        # check if there are equal numbers of opening and closing parenthesis
        if ( func.count("(") != func.count(")") )
          error_log "ERROR: Missing paranthesis in line"
          return false
        end
        
        # check to see if the index of x is acceptable
        matches = func.scan(/x(\d+)/)
        if ( matches.size ) 
          matches.each do |match|
            if match[0].to_i < 1 || match[0].to_i > nodes
              error_log "ERROR: Index of x out of range in line"
              return false
            end
          end
        end
        
        # check to see if function is starting properly
        if ( !func.match( /^[\)\*\^]/ ).nil? )
          error_log "ERROR: Incorrect syntax in line. Inappropriate char at start of function."
          return false
        end
        
        # check to see if function is ending properly
        if ( !func.match( /[^\)\d]$/ ).nil? )
          error_log "ERROR: Incorrect syntax in line. Inappropriate char at end of function."
          return false
        end
        
        # check to see if x always has an index
        if ( !func.match( /x\D/ ).nil? )
          error_log "ERROR: Incorrect syntax in line. Check x variable."
          return false
        end
        
        # check to see if ^ always has a number following
        if ( !func.match( /\^\D/ ).nil? )
          error_log "ERROR: Incorrect syntax in line. Check exponent value."
          return false
        end
        
        if ( 
             !func.match( /[\+\-\*\(][\)\+\-\*\^]/ ).nil? ||
             !func.match( /\)[\(\d x]/ ).nil? ||
             !func.match( /\d[\( x]/ ).nil?
           )
           error_log "ERROR: Incorrect syntax in line. Read the tutorial for correct formatting rules.";
           return false
        end
        
        make_eval_func(func)
        
        @functions[function_count] = Array.new unless !@functions[function_count].nil?
        @functions[function_count].push(make_func_obj(func, probability))
      end
      
      # check if stochastic flag passed and more than one function
      if false # TODO: change this once we have flags
        if @functions[function_count].length > 1
          error_log "ERROR: Update stochastic but more than one function for f_" + function_count.to_s
          return false
        end
      end
      
      # check if sequential flag passed and more than one function
      if false # TODO: change this once we have flags
        if @functions[function_count].length > 1
          error_log "ERROR: Update sequential but more than one function for f_" + function_count.to_s
          return false
        end
      end
      
      # check if all trajectories flag passed and more than one function
      if false # TODO: change this once we have flags
        if @functions[function_count].length > 1
          error_log "ERROR: Trajectory from a single initial state but more than one function for f_" + function_count.to_s
          return false
        end
      end
      
      function_count = function_count + 1
    end
    
    return true
  end
  
  def make_func_obj(func, probability)
    {
      :func => make_eval_func(func),
      :raw => func,
      :probability => probability
    }
  end
  
  def make_eval_func(func)
    # replace carret with double stars in eval func
    eval_func = func.gsub(/\^/, "**")
    
    # make the code ready for eval
    eval_func.gsub!(/x(\d+)/, "x[\\1]")
  end
 
  # 
  def load_function_data
    f = File.new(file_prefix + FUNCTIONFILE_SUFFIX)
    in_backet = false
    function_count = 0
    while ( line = f.gets )
      line.strip!
      if ( line.include?("{") )
        in_backet = true
        next
      elsif ( in_backet && !line.match(/\d/).nil? ) 
        if ( line.include?("}") )
          line.gsub!(/\s*\}/, "")
          in_backet = false
          function_count = function_count + 1
        end
        @function_data[function_count] = Array.new unless !@function_data[function_count].nil?
        @function_data[function_count].push(line)
      elsif ( in_backet && line.include?("}") )
        in_backet = false
        function_count = function_count + 1
        next
      elsif ( !line.match(/\d/).nil? )
        @function_data[function_count] = Array.new
        @function_data[function_count].push(line.split("=").pop.strip!)
        function_count = function_count + 1
      end
    end
  end
end
