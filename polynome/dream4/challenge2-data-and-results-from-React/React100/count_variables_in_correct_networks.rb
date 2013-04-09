require 'rubygems'
require "dvdcore.rb"

def add_componentwise(sum, output) 
  for i in 0..output.length-1 do 
    for j in 0..output[i].length-1 do
      if ( sum[i].nil? )
        sum[i] = Array.new
      end
      if ( sum[i][j].nil? )
        sum[i][j] = output[i][j]
      else
        sum[i][j] = output[i][j] + sum[i][j]
      end
    end
  end
  sum
end

#split output into functionfiles
def split_data_into_files(datafile)
  datafiles = []
  output = NIL
  File.open(datafile) do |file| 
      counter = 1 
      something_was_written = FALSE
      while line = file.gets 
          # parse lines and break into different files at #
          if( line.match( /^\s*Model/ ) )
              if (something_was_written && output) 
                  output.close
                  output = NIL
              end
              something_was_written = FALSE
          else 
              if (!something_was_written) 
                  outputfile_name = datafile.gsub(/\.txt/, "-" + counter.to_s + ".functionfile.txt")
                  counter +=1
                  output = File.open(outputfile_name, "w") 
                  datafiles.push(outputfile_name)
              end
              # check if line matches @n_nodes digits
              if (line.match( /^\s*f\d*/) )
                  output.puts line
                  something_was_written = TRUE
              end
          end
      end 
      file.close
      if (output) 
          output.close
      end
  end
  datafiles
end



for k in 1..5 do 
  sum = Array.new()
  output = Array.new()
  functionfiles = split_data_into_files("output-#{k}.txt")
  functionfiles.each do |functionfile|
    file_prefix = functionfile.sub(/\.functionfile\.txt/, "")
    dvd = DVDCore.new(file_prefix, 110, 2)
    dvd.run
    output = dvd.observe_dependencies_in_array
    add_componentwise(sum, output)
  end
  outfile = File.new("output-#{k}-edges.txt", "w")
    for i in 0..sum.length-1 do 
      for j in 0..sum[i].length-1 do
        outfile.print sum[i][j] * 100 / 15
        outfile.print "\t"
      end
      outfile.puts ""
    end
  outfile.close
end



quit


