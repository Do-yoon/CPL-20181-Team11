#include "global.h"

TextMessage::TextMessage(char* buffer, int msgLen) {
	strcpy(this->buffer, buffer);
	this->msgLen = msgLen;
}
char* TextMessage::getCallingNumber() {

}
char* TextMessage::getCallingName() {

}
char* TextMessage::getTextMessage() {

}