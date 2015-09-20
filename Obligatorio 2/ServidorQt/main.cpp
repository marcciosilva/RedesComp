#include <QApplication>

#include "Servidor.h"

int main(int argc, char *argv[]) {
	// initialize resources, if needed
	// Q_INIT_RESOURCE(resfile);

	QApplication app(argc, argv);

	// create and show your widgets here
	Servidor v;
	v.show();
	return app.exec();
}
