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
	int curser = 0; //Ŀ�� ����
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
		//���ۿ� ���� ���� ������ �ʿ� ����.
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

	if ((servsock = socket(AF_INET, SOCK_STREAM, 0)) == -1) {// ���� ����
		printf("Server : Can't open stream socket\n");
		exit(0);
	}
	memset(&serv_addr, 0x00, sizeof(serv_addr));
	//server_Addr �� NULL�� �ʱ�ȭ

	server_addr.sin_family = AF_INET;
	server_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	server_addr.sin_port = htons(PORTNUM);
	//server_addr ����

	if (bind(servsock, (struct sockaddr *)&server_addr, sizeof(server_addr)) <0)
	{//bind() ȣ��
		printf("Server : Can't bind local address.\n");
		exit(0);
	}

	if (listen(servsock, 5) < 0)
	{//������ ���� ������ ����
		cout << "Server : Can't listening connect." << endl;
		exit(0);
	}

	memset(buffer, 0x00, sizeof(buffer));
	cout << "Server : waiting connection request." << endl;
	len = sizeof(clnt_addr);
	Message* msg;
	time_t logTime;

	while (true) //accept ���
	{
		clntsock = accept(servsock, (struct sockaddr *)&clnt_addr, &len);

		if (clntsock > 0) {
			/*�� �ּҸ� �޾ƿ´�*/
			/*���� ��谡 ��ٸ��� �ִ� ��谡 �´��� �޾ƿ´�*/
			
			logTime = clock();

			while (true) {			//�޴���ȭ�� ������ �Ǿ�������
				/*�ش� ����� notification�� ��������,
				������� ��û�� ���� ��׶��� �ۿ� ��û�� ������*/
				getNotification();
				getMessageFromBuffer();

				if ((clock() - logTime) / (CLOCKS_PER_SEC) >= 15.0) { //�α� ��� ���ǹ�
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