#include <iostream>
#include "utilities.h"
#include "constants.h"

using namespace std;

int main (int argc, uchar *argcv[]){
  
  unlong * bases = new unlong[3];
  bases[0] = 2;
  bases[1] = 2;
  bases[2] = 2;

  cerr << "IN: " << argcv[1] << endl;
  argcv[1][0] -= '0';
  argcv[1][1] -= '0';
  argcv[1][2] -= '0';

  unlong x = ternaryToDecimal(3, argcv[1], bases);
  cerr << "OUT: " << x << endl;
  decimalToTernary(x, argcv[1], bases, 3);
  
  return 0;
}
