<?php

/*
 * This class checks the consistency of a dataset
 *
 */
class consistent
{
  /*
   * @const INCONSISTENT_LINE_SEP (string)
   * @description the line sep to use when a line is inconsistent
   */
  const INCONSISTENT_LINE_SEP = "#";

  /*
   * @var _pairs (array)
   * @private
   * @description all the found transitions
   */
  private $_pairs = array();

  /*
   * @var _lineHold (string)
   * @private
   * @description used to temporarily hold a possible inconsistent value
   */
  private $_lineHold = null;
  
  /*
   * @var _linePrev (string)
   * @private
   * @description the line last seen on the loop
   */
  private $_linePrev = null; 
  
  /*
   * @var _output (array)
   * @private
   * @description the lines of the output
   */
  private $_output = array();
  
  /*
   * @var _data (array)
   * @private
   * @description the lines of the input
   */
  private $_data = array();
  
  /*
   * @var _debug (boolean)
   * @private
   * @description whether to show debug logging or not
   */
  private $_debug = false;

  /*
   * Method which checks the consistency of a dataset
   *
   * @var infile (string) - filename to read the dataset to
   * @var outfile (string) - filename to write the output to or null for stdout
   * @var debug (boolean) - whether to show debug logging or not
   *
   * @usage
   *      $c = new consistent;
   *      $c->check("data.txt", null, true);
   */
  public function check($infile, $outfile = null, $debug = false) {
    $this->_debug = $debug;
    $this->_data = file($infile);
    $this->_validate();
    if($outfile) {
      // if we have an output file, write the contents to disk
      file_put_contents($outfile, implode($this->_output, "\n"));
    } else {
      // otherwise just spit the output out on standard out
      implode($this->_output, "\n");
    }
  }

  /*
   * Performs the actual dataset validation
   *
   */
  private function _validate() {
    // loop over all the data
    foreach($this->_data as $key=>$line) {
      $line = trim($line); // get rid of the trailing newline
      
      // see if we are in a hold state
      $this->_checkHold($key);
      
      // if we have an existing pair, we need to check for inconsistency
      // so save this line for the next loop
      if(isset($this->_pairs[$this->_linePrev])) {
        $this->_lineHold = $line;
        continue;
      }

      // if we haven't already written this line to the output, do so now
      if(!isset($this->_output[$key])) {
        $this->_output[$key] = $line;
      }

      // if this is the first line in the file, we won't have a previous
      if($this->_linePrev) {
        $this->_log($this->_linePrev, $line, true);
        $this->_pairs[$this->_linePrev] = $line;
      }

      // save this line for the next loop
      $this->_linePrev = $line;
    }

    // we must check the hold one more time, otherwise we
    // might miss an incosistency on the last line
    $this->_checkHold($key);
  }

  /*
   * Checks to see if we had an actual inconsistency
   *
   */
  private function _checkHold($key) {
    if($this->_lineHold) {
      if($this->_pairs[$this->_linePrev] != $this->_lineHold) {
        $this->_log($this->_linePrev, $this->_lineHold, false);
    
        // data is inconsistent, so write out the sep to the inconsistent line
        $this->_output[$key - 1] = self::INCONSISTENT_LINE_SEP;

        // here we reset the transitions table as we just created a new
        // time course. if new time courses aren't truly separate, then we
        // need to remove this line
        $this->_pairs = array();
      } else {
        $this->_log($this->_linePrev, $this->_lineHold, true);
        $this->_output[$key - 1] = $this->_lineHold;
      }

      // gotta save off this line for the next loop
      $this->_linePrev = $this->_lineHold;
      
      // whether we had a real inconsistency or not, clear out the check state
      $this->_lineHold = null;
    }
  }

  /*
   * Prints out a debug log line showing the transition + whether
   * it's consistent or not
   *
   */
  private function _log($val1, $val2, $consistent) {
    if(!$this->_debug) return;
    printf("%s transitioning to %s%s\n", $val1, $val2, $consistent ? "" : " - not consistent");
  }
}

// example usage
$c = new consistent;
$c->check("data.txt", "output.txt", true);
