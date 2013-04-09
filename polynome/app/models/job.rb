class Job < ActiveRecord::Base
  validates_presence_of :nodes, :on => :create
  validates_numericality_of :nodes, :only_integer => true, :message => "Number of nodes must be an integer between 1 and 11", :on => :create
  validates_numericality_of :nodes, :less_than => 150, :message => "Number of nodes is too big!", :on => :create
  validates_numericality_of :nodes, :greater_than => 0, :message => "Number of nodes is too small!", :on => :create
  validate :check_update_schedule

  ## Check update schedule for correctness
  ## for now this is only checking if it is n numbers, not if each number is
  ## used exactly once
  # check update schedule to be blank or the right regex
  def check_update_schedule
    if update_schedule 
        errors.add_to_base("Update schedule not valid " + update_schedule) unless
        update_schedule.match( /^\s*((\d+\s*){#{nodes}})?\s*$/ ) 
    end
  end

  def file_prefix
    @file_prefix
  end

  def file_prefix=(file_prefix)
    @file_prefix = file_prefix
  end

end
