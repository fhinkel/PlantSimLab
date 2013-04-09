class CreateJobs < ActiveRecord::Migration
  def self.up
    create_table :jobs do |t|
      t.integer :nodes
      t.boolean :wiring_diagram
      t.string  :wiring_diagram_format
      t.boolean :state_space
      t.string  :state_space_format
      t.boolean :show_discretized
      t.boolean :show_functions
      t.string  :input_data
      t.string  :function_data
      t.boolean :show_probabilities_wiring_diagram
      t.boolean :show_probabilities_state_space
      t.boolean :is_deterministic
      t.boolean :sequential
      t.string  :update_schedule

      t.timestamps
    end
  end

  def self.down
    drop_table :jobs
  end
end
