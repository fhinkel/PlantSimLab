irb
require "lib/dvdcore.rb"

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


require "lib/dvdcore.rb"

for k in 1..5 do 
  sum = Array.new()
  output = Array.new()
  functionfiles = split_data_into_files("/Users/fhinkel/Sites/github/htdocs/public/files/output-#{k}.txt")
  functionfiles.each do |functionfile|
    file_prefix = functionfile.sub(/\.functionfile\.txt/, "")
    file_prefix.sub!(/\/Users\/fhinkel\/Sites\/github\/htdocs\//,"")
    puts file_prefix
    dvd = DVDCore.new(file_prefix, 15, 2)
    #dvd = DVDCore.new(file_prefix, 15, 2)
    dvd.run
    output = dvd.observe_dependencies_in_array
    add_componentwise(sum, output)
  end
  outfile = File.new("/Users/fhinkel/Sites/github/htdocs/public/files/output-#{k}-edges.txt", "w")
    for i in 0..sum.length-1 do 
      for j in 0..sum[i].length-1 do
        outfile.print sum[i][j] * 100 / 130
        outfile.print "\t"
      end
      outfile.puts ""
    end
  outfile.close
end

cp public/files/*edges.txt ~/Sites/polynome-master/dream4/React/

sum
a = Array.new(2)
a[0] = [1,2,3]
a[1] = [55,66,77]
b = Array.new(2)
b[0] = [1000,2000,30000]
b[1] = [50000,60000,7000]
sum = a
output = b

functionfile = "/Users/fhinkel/Documents/Research/Rails/polynomevt/htdocs/public/files/output-1-120.functionfile.txt"
functionfile = "/Users/fhinkel/Sites/github/htdocs/public/files/output-1-120.functionfile.txt"

  functionfile = functionfiles.first
  file_prefix = functionfile.sub(/\.functionfile\.txt/, "")
  file_prefix.sub!(/\/Users\/fhinkel\/Sites\/github\/htdocs\//,"")

add_componentwise(a,b)
sum


quit


