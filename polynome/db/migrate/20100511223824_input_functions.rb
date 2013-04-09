class InputFunctions < ActiveRecord::Migration
  def self.up
    add_column :jobs, :input_function, :string

  end

  def self.down
    remove_column :job, :function_data
  end
end
