#include <stdio.h>

#include <sys/resource.h>
#include <stdlib.h>
#include <sys/time.h>
#include <time.h>
#include "omp.h"

/* Size of the string to match against.  A small size is used here for debugging; you will need
a _much_ larger size for any performance testing . */

#define STRINGSIZE 100000000
#define STATE1 1
#define STATE2 2
#define STATE3 3
#define REJECTSTATE -1
/* Probability of repeating a character */

#define PROB rand()%1000<197



char *buildString() {
	int i;
	char *s = (char *)malloc(sizeof(char)*(STRINGSIZE));
	if (s == NULL) {
		printf("\nOut of memory!\n");
		exit(1);
	}
	int max = STRINGSIZE;
	/* seed the rnd generator (use a fixed number rather than the time for testing) */
	srand((unsigned int)time(NULL));
	/* And build a long string that might actually match */
	int j = 0;
	while (j<max) {
		s[j++] = 'a';
		while (PROB && j<max - 2)
			s[j++] = 'a';
		s[j++] = 'b';
		while (PROB && j<max - 1)
			s[j++] = 'b';
		s[j++] = (rand() % 2 == 1) ? 'c' : 'd';
		while (((PROB && j<max) || max - j<3) && j<max)
			s[j++] = (rand() % 2 == 1) ? 'c' : 'd';
	}

	return s;
}

//DFA
int enter0(char x){
	if (x == 'a'){
		return 1;
	}
	else{
		return -1;
	}
}
int enter1(char x){
	if (x == 'a'){
		return 1;
	}
	else if (x == 'b'){
		return 2;
	}
	else{
		return -1;
	}
}
int enter2(char x){
	if (x == 'b') {
		return 2;
	}
	else if (x == 'c' || x == 'd'){
		return 3;
	}
	else{
		return -1;
	}
}
int enter3(char x){
	if (x == 'c' || x == 'd'){
		return 3;
	}
	else if (x == 'a'){
		return 1;
	}
	else{
		return -1;
	}
}
// enter the dfa with character x at state state
int enter(char x, int state){
	switch (state){
	case 0:
		return enter0(x);
	case 1:
		return enter1(x);
	case 2:
		return enter2(x);
	case 3:
		return enter3(x);
	defaut:
		return -1;
	}
}
void executeThreads(int threadAmt, int state_amt,int states2[threadAmt][state_amt], char * string)
{
	omp_set_num_threads(threadAmt);
	int stringPartitionSize = STRINGSIZE/threadAmt;
	int tAmt = threadAmt;
	int sAmt = state_amt;

	// parrallel section
	#pragma omp parallel 
	{
		int stringsize = STRINGSIZE;
		int string_index;		
		int threadId = omp_get_thread_num();

		int states3[sAmt];
		int j;


		// initiate the states
		// thread 0 starts at 0
		// all other threads need to simulate for all start states
		if(threadId == 0){
			states3[0] = 0;
		}else{
			for(j = 0 ; j < sAmt ; j++)
			{
				states3[j] = j+1;
			}
		}

		// initialize the partition size
		// thread 0 picks up remainder

		int partitionSize = stringsize / tAmt ;
		int remain = stringsize - (partitionSize * tAmt);

		if(threadId == 0 && stringsize % tAmt != 0){
			partitionSize += stringsize - (tAmt * partitionSize);
			remain = 0;
		}
		
		string_index = (threadId * partitionSize) + remain;
		int end_index = string_index+partitionSize;
		

		for(string_index  ; string_index < end_index ; string_index++){

			int state_index;
			// thread 0 knows it's start state, so no loop is needed
			if(threadId == 0 ) {
				states3[0] = enter(string[string_index], states3[0]);	
			} 
			// all other threads must loop through different start states of dfa
			else {			
				for( state_index = 0 ; state_index < sAmt ; state_index++)
				{
					states3[state_index] = enter(string[string_index], states3[state_index]);
					
				}
			}
		}

		// copy results into shared array after map is completed
		for(j = 0 ; j < state_amt ; j++){
			states2[threadId][j] = states3[j]; 
		}				
	}

}

int main(int argc, char* argv[])
{
	
	char * string;
	string = buildString();
	// string = "abcab";
	// printf("STRING : %s\n",string);

	int state = 0;
	int state_amt = 4;
	int threadAmt;	
	
	if(argc < 2 )
	{
		threadAmt = 4;
	}else{
		threadAmt = atoi(argv[1]);
		threadAmt = threadAmt+1;
	}


	// printf("threads = %d, states : %d string size : %d\n",threadAmt , state_amt, STRINGSIZE);
	
	
	// 1D : each thread
	// 2D : for each start state
	int states2[threadAmt][state_amt-1];
	

	printf("START\n");
	struct timeval start, end;
	gettimeofday(&start, NULL);
	
	// 10 executions
	int i;
	for(i = 0 ; i < 10 ; i++)
	{ 
		//executeThreads creates map in states2
		states2[0][0] = 0;
		executeThreads(threadAmt, state_amt - 1 , states2, string);
		
		//initial state is states[0][0] as we start at 0th index on the 0th state
		state = states2[0][0];
		
		//use mapping
		if(threadAmt > 1){	
			int i;
			for(i = 1  ; i < threadAmt ; i++)
			{	
				//break if reject state		
				if(state == REJECTSTATE)
					break;							
				// -1 since the index is increased by 1
				state = states2[i][state-1];		
				
			}
		}
	}
	gettimeofday(&end,NULL);
	printf("end state = %d\nelapsed time = %ld\n\n", state,((end.tv_sec * 1000000 + end.tv_usec)
		  - (start.tv_sec * 1000000 + start.tv_usec))/1000);
	
	return 0;
}
