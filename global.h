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

typedef enum MessageType { //어떤 정보를 담는 메시지인지 표시하기 위한 정의를 갖는 열거형
	SENDTEXTMSG, CALLINGTO, CALLINGFAIL, SENDTEXTMSGFAIL, GETNOTIFICATION
};
typedef enum ServerState { //서버 상태를 표시하는 열거형 데이터 타입
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

class Message {			// 메시지 정보를 담고 그 정보를 처리하여 사용자에게 전달하는 메소드를 포함하는 클래스
protected:
	char* buffer;
public:
	char* getBuffer();	//문자열 포인터 반환
	char* getMessage();	//문자열 포인터 반환
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