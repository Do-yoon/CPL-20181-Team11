#include "global.h"

Log::Log(char* buffer, bool err) {
	strcpy(this->buffer, buffer);
	this->err = err;
}
bool Log::error() {
	return this->err;
}