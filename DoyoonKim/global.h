#pragma once

#include <iostream>
#include <queue>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <pthread.h>
#include <aws/core/utils/StringUtils.h>
#include <aws/core/utils/HashingUtils.h>
#include <aws/core/utils/json/JsonSerializer.h>
#include <aws/core/utils/xml/XmlSerializer.h>

#define PORTNUM 9190

using namespace std;

class Message;
class Calling;
class TextMessage;
class Server;

typedef enum MessageType { //� ������ ��� �޽������� ǥ���ϱ� ���� ���Ǹ� ���� ������
	SENDTEXTMSG, CALLINGTO, CALLINGFAIL, SENDTEXTMSGFAIL, GETNOTIFICATION
};
typedef enum ServerState { //���� ���¸� ǥ���ϴ� ������ ������ Ÿ��
	WAITFORCONNECT, CONNECTED, WAITNOTIFICATION, GETTINGNOTIFICATION, SENDINGTEXTMSG, CALLINGTOSB, QUIT
};

pthread_mutex_t posixMutex = PTHREAD_MUTEX_INITIALIZER;

class Voice {
protected:
	char* text;
	char* fileName;
public:
	void mp3ToText();
	char* getText(char*);

};

class Message {			// �޽��� ������ ��� �� ������ ó���Ͽ� ����ڿ��� �����ϴ� �޼ҵ带 �����ϴ� Ŭ����
protected:
	char* buffer;
public:
	char* getBuffer();	//���ڿ� ������ ��ȯ
	char* getMessage();	//���ڿ� ������ ��ȯ
	MessageType getMessageType();
	int getBufferLen();
	Message(char*);
};

class Calling : protected Message {
public:
	Calling(char*, int);
	char* getCallingNumber();
	char* getCallingName();
};

class TextMessage : protected Message {
public:
	TextMessage(char*, int);
	char* getCallingNumber();
	char* getCallingName();
	char* getTextMessage();
};

class Log {
private:
	char* buffer;
	bool err;
public:
	Log(char*, bool);
	bool error();
};

class Authorization {
private:
	FILE* conf;
public:
	void configFileOpen();
	void setAuthorize();
	FILE* getConfigFilePtr();
};

class Server {
private:
	ServerState state;
	Server() {};
	Server(const Server& other);
	queue<char*> NotificationQ;
	queue<Message*> MessageQ;
	queue<Voice*> VoiceQ;
	queue<Log*> LogQ;
	static Server* instance;
	int servsock, clntsock;
	Authorization* config;
public:
	static Server* getInstance() {
		return instance;
	}
	
	void quit();
	void loopForMobile();
	void loopForVoice();
	void CallingTo();
	void TextTo();
	char* getNotification();
	void getMessageFromBuffer();
	void writeToLogFile();
	void run();	
};