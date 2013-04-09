#!/usr/bin/env ruby

require 'rubygems'
require 'fileutils'

# ruby script to run with C-c C-c

# 5 timecourses with perturbation, 1 steady state 
# 10 knockout time courses

# split timecourses in one file separated by # into multiple files. Add to

# each timecourse the appropriate perturbation, for TS1-5 that is pertubarion
# 1-5, for all other no perturbation

# rename TS 6-15 into KO 1-10

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
  for option in ["super", "sub"]
    File.open("fileman-size10-#{k}-#{option}.txt", "w") do |file|
      file.puts "P=2; N=15;"
      file.puts "WT ={"
      for i in 1 .. 5 do 
        file.puts "\"#{k}_TS#{i}.txt\","
      end
      file.puts "\"#{k}_TS#{6}.txt\""
      file.puts "};"
      file.puts "KO ={"
      for i in 1 .. 9 do 
        file.puts "(#{i},\"#{k}_KO#{i}.txt\"),"
      end
      file.puts "(10,\"#{k}_KO#{10}.txt\")"
      file.puts "};"
      file.puts "REV = {};"
      file.puts "CMPLX = {};"
      file.puts "BIO = {\"Bio-network#{k}-#{option}set.txt\"};"
      file.puts "MODEL = {};"
      file.puts "PARAMS = {\"params-size10-1.txt\"};"
      #file.puts "PARAMS = {\"paramsBioWiring.txt\"};"
    end
  end
end


for k in 1 .. 5 do 
  split_data_into_files("../../challenge2-Boolean-data/Bool/size10-#{k}-Bool.txt", k)
  add_pertubation_timecourse("#{k}_TS1.txt", " 1 0 0 0 0")
  add_pertubation_timecourse("#{k}_TS2.txt", " 0 1 0 0 0")
  add_pertubation_timecourse("#{k}_TS3.txt", " 0 0 1 0 0")
  add_pertubation_timecourse("#{k}_TS4.txt", " 0 0 0 1 0")
  add_pertubation_timecourse("#{k}_TS5.txt", " 0 0 0 0 1")
  add_pertubation_timecourse("#{k}_TS6.txt", " 0 0 0 0 0")
  for i in 7 .. 16 do 
    add_pertubation_timecourse("#{k}_TS#{i}.txt", " 0 0 0 0 0")
    j = i - 6 
    FileUtils.mv("#{k}_TS#{i}.txt", "#{k}_KO#{j}.txt") 
  end
  write_fileman( k) 
  for option in ["super", "sub"]
    puts "run react for network #{k} #{option}set"
    puts `./React fileman-size10-#{k}-#{option}.txt output-#{k}-#{option}.txt`
  end
end

