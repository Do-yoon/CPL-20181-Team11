#include "global.h"

Calling::Calling(char* buffer, int msgLen) {
	strcpy(this->buffer, buffer);
	this->msgLen = msgLen;
} 
char* Calling::getCallingNumber() {
	return &this->buffer[2];
}