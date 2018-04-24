#include "global.h"

Server* Server::getInstance() {

}
void Server::quit() {

}
void Server::CallingTo() {

}
void Server::TextTo() {

}
char* Server::getNotification() {

}
void Server::getMessageFromBuffer() {
	char temp[BUFSIZ];
	char message[BUFSIZ];
	int curser = 0; //커서 조작
	Message* msg;
	Message* pushMessage;
	int leftLen = 0;

	while (read(clntsock, &temp[leftLen], (BUFSIZ - leftLen)) > 0) {
		msg = new Message(temp);
		while (msg->getBufferLen() <= (BUFSIZ - curser - 1)) {
			strncpy(message, temp, msg->getBufferLen());
			pushMessage = new Message(message);
			MessageQ.push(pushMessage);
			curser += msg->getBufferLen();
		}
		//버퍼에 남은 길이 가져올 필요 있음.
		leftLen = BUFSIZ - curser;
		curser = 0;
		delete(msg);
	}
}

void Server::loopForVoice() {
	/*get voice file*/
	/*voice instance enqueue*/
	/*mp3 to text*/
	/*text parsing*/

	
}

void Server::loopForMobile() {
	
	char buffer[BUFLEN];
	struct sockaddr_in serv_addr, clnt_addr;
	char temp[20];
	int len, msg_size;

	if ((servsock = socket(AF_INET, SOCK_STREAM, 0)) == -1) {// 소켓 생성
		printf("Server : Can't open stream socket\n");
		exit(0);
	}
	memset(&serv_addr, 0x00, sizeof(serv_addr));
	//server_Addr 을 NULL로 초기화

	server_addr.sin_family = AF_INET;
	server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	server_addr.sin_port = htons(PORTNUM);
	//server_addr 셋팅

	if (bind(servsock, (struct sockaddr *)&server_addr, sizeof(server_addr)) <0)
	{//bind() 호출
		printf("Server : Can't bind local address.\n");
		exit(0);
	}

	if (listen(servsock, 5) < 0)
	{//소켓을 수동 대기모드로 설정
		cout << "Server : Can't listening connect." << endl;
		exit(0);
	}

	memset(buffer, 0x00, sizeof(buffer));
	cout << "Server : waiting connection request." << endl;
	len = sizeof(clnt_addr);
	Message* msg;
	time_t logTime;

	while (true) //accept 대기
	{
		clntsock = accept(servsock, (struct sockaddr *)&clnt_addr, &len);

		if (clntsock > 0) {
			/*맥 주소를 받아온다*/
			/*현재 기계가 기다리고 있는 기계가 맞는지 받아온다*/
			
			logTime = clock();

			while (true) {			//휴대전화가 연결이 되어있으면
				/*해당 기계의 notification을 가져오고,
				사용자의 요청에 따라 백그라운드 앱에 요청을 보낸다*/
				getNotification();
				getMessageFromBuffer();

				if ((clock() - logTime) / (CLOCKS_PER_SEC) >= 15.0) { //로그 기록 조건문
					logTime = clock();
					writeToLogFile();
				}

				while (!MessageQ.empty()) {
					switch (MessageQ.front->getMessageType()) {
					case SENDTEXTMSG:
						
						break;
					case CALLINGTO:
					case CALLINGFAIL:
					case SENDTEXTMSGFAIL:
					case GETNOTIFICATION:
					}
					MessageQ.pop();
				}
			}

			break;

		}
	}

	close(servsock);

}

void Server::run() {

	while (true) {
		switch (this->state) {
		case WAITFORCONNECT:

		case CONNECTED:
		case WAITNOTIFICATION:
		case GETTINGNOTIFICATION:
		case SENDINGTEXTMSG:
		case CALLINGTOSB:
		case QUIT:
		}
	}
}