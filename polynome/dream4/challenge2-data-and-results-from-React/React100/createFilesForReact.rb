#!/usr/bin/env ruby

require 'rubygems'
require 'fileutils'

# ruby script to run with C-c C-c

# 10 timecourses with perturbation
# 100 knockout time courses
# 1 steady state 

# split timecourses in one file separated by # into multiple files. Add to

# each timecourse the appropriate perturbation, for TS1-10 that is perturbation
# 1-10, for all other no perturbation

# rename TS 11-110 into KO 1-100
# rename steady state wildtype 111 to TS 11

# write fileman to have TS and KO data it in

# run react

#irb

def split_data_into_files(datafile, number)
  datafiles = []
  output = NIL
  File.open(datafile) do |file| 
    counter = 1
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
          outputfile_name = number.to_s + "_TS" + counter.to_s + ".txt"
          counter +=1
          output = File.open(outputfile_name, "w") 
          datafiles.push(Dir.getwd + "/" + outputfile_name)
        end
        output.puts line
        something_was_written = TRUE 
      end
    end 
    file.close
    if (output) 
      output.close
    end
  end
end

def add_pertubation_timecourse(datafile, addon)
  unless File.exists?(datafile) 
    puts "Error, #{datafile} does not exist"
    return
  end
  File.open(datafile, "r+") do |file| 
    lines = file.readlines()
    lines.each do |line|
      line.chop!
      line.strip!
      unless line.empty? 
        line.sub!(/\z/,  addon )
      end
    end
    file.rewind()
    file.puts lines
  end
end

def write_fileman( k )
  File.open("fileman-size100-#{k}.txt", "w") do |file|
    file.puts "P=2; N=110;"
    file.puts "WT ={"
    for i in 1 .. 10 do 
      file.puts "\"#{k}_TS#{i}.txt\","
    end
    file.puts "\"#{k}_TS#{11}.txt\""
    file.puts "};"
    file.puts "KO ={"
    for i in 1 .. 99 do 
      file.puts "(#{i},\"#{k}_KO#{i}.txt\"),"
    end
    file.puts "(100,\"#{k}_KO#{100}.txt\")"
    file.puts "};"
    file.puts "REV = {};"
    file.puts "CMPLX = {};"
    file.puts "BIO = {};"
    file.puts "MODEL = {};"
    file.puts "PARAMS = {\"params-size100.txt\"};"
  end
end

def backtick(cmd)
  IO.popen(cmd) { |io|
    while line = io.gets
      puts line
    end
  }
end


for k in 1 .. 5 do 
  split_data_into_files("../challenge2-Boolean-data/Bool-100/size100-#{k}-Bool.txt", k)

  arr = Array.new(10, " 0")
  for i in 1..10 do 
    arr[i-1] = " 1"
    add_pertubation_timecourse("#{k}_TS#{i}.txt", arr.to_s)
    arr[i-1] = " 0"
  end
  for i in 11 .. 110 do 
    add_pertubation_timecourse("#{k}_TS#{i}.txt", arr.to_s)
    j = i - 10
    FileUtils.mv("#{k}_TS#{i}.txt", "#{k}_KO#{j}.txt") 
  end
  add_pertubation_timecourse("#{k}_TS111.txt", arr.to_s)
  FileUtils.mv("#{k}_TS111.txt", "#{k}_TS11.txt") 
  write_fileman(k) 
  puts "run react for network #{k}"
  #backtick("./React fileman-size100-#{k}.txt output-#{k}.txt")
end

