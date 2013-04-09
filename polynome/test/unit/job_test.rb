require 'test_helper'

class JobTest < ActiveSupport::TestCase
  fixtures :jobs
  
  def test_show_index
  #  get :index
  #  assert_response :success
  end
  
  def test_nodes_numeric
    job = Job.create({:nodes => "-3"})
    assert !job.valid?

    job = Job.create({:nodes => "0"})
    assert !job.valid?

    job = Job.create({:nodes => "1"})
    assert job.valid?

    job = Job.create({:nodes => "11"})
    assert job.valid?

    job = Job.create({:nodes => "12"})
    assert job.valid?
    
    job = Job.create({:nodes => "1002"})
    assert !job.valid?

    job = Job.create({:nodes => "1.3"})
    assert !job.valid?

    job = Job.create({:nodes => "asdhas"})
    assert !job.valid?

  end

end
