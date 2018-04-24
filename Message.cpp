#include "global.h"

char* Message::getBuffer() {
	return buffer;
}

char* Message::getMessage() {
	return &buffer[3];
}

MessageType Message::getMessageType() {
	return (MessageType)buffer[0];
}

int Message::getBufferLen() {
	short temp = buffer[1] << 8;
	temp = temp & (buffer[2] & 0xFF);
	return (int)(temp + 3);
}
