CC = g++ -O3
CC32 = g++ -m32 -O3

utilities.o: utilities.cc utilities.h constants.h
	$(CC) -c utilities.cc

PDS.o: PDS.cc PDS.h constants.h utilities.h
	$(CC) -c PDS.cc

Table.o: Table.cc Table.h constants.h utilities.h
	$(CC) -c Table.cc

cyclone.o: cyclone.cc constants.h PDS.h Table.h cyclone.h utilities.h
	$(CC) -c cyclone.cc

shell.o: shell.cc constants.h PDS.h Table.h cyclone.h utilities.h
	$(CC) -c shell.cc

simple.o: simple.cc constants.h Table.h
	$(CC) -c simple.cc

tabletest.o: tabletest.cc constants.h Table.h
	$(CC) -c tabletest.cc

convtest.o: utilities.h constants.h convtest.cc
	$(CC) -c convtest.cc 

Cyclone: cyclone.o shell.o PDS.o constants.h PDS.h Table.h Table.o cyclone.h utilities.o
	$(CC) -o $@ cyclone.o PDS.o Table.o shell.o utilities.o

Cyclone64: cyclone.o shell.o PDS.o constants.h PDS.h Table.h Table.o cyclone.h utilities.o
	$(CC) -o $@ cyclone.o PDS.o Table.o shell.o utilities.o

CycloneTest: cyclone.o shell.o PDS.o constants.h PDS.h Table.h Table.o cyclone.h utilities.o
	$(CC) -o $@ cyclone.o PDS.o Table.o shell.o utilities.o
	./Cyclone testmodel -v

toBMV:
	cp ./*[*.h,*.cc,Makefile] ~/VBI/BMV/Resources/CycloneSource
	cp Cyclone ~/VBI/BMV/Resources/Cyclone

clean:
	rm *.o

Simple: simple.o Table.o constants.h Table.h
	$(CC) -o $@ simple.o Table.o utilities.o

TTest: tabletest.o Table.o constants.h Table.h
	$(CC) -o $@ tabletest.o Table.o

ConvTest: convtest.o constants.h utilities.o
	$(CC) -o $@ convtest.o utilities.o