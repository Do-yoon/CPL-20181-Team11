#include "global.h"

int main() {
	Server* serverInstance = Server::getInstance();
	int thr_id;
	pthread_t p_thread[3];
	int status;

	//create 3 thread
	//2 for loops, 1 for running logic

	thr_id = pthread_create(&p_thread[0], NULL, serverInstance->loopForMobile, (void *)NULL);
	thr_id = pthread_create(&p_thread[1], NULL, serverInstance->loopForVoice, (void *)NULL);
	thr_id = pthread_create(&p_thread[2], NULL, serverInstance->run(), (void *)NULL);
	
	pthread_join(p_thread[0], (void **)&status);
	pthread_join(p_thread[1], (void **)&status);
	pthread_join(p_thread[2], (void **)&status);

	printf("program end\n");
	return 0;
}