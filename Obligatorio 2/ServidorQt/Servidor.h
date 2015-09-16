/* 
 * File:   Servidor.h
 * Author: gaston
 *
 * Created on September 16, 2015, 12:52 AM
 */

#ifndef _SERVIDOR_H
#define	_SERVIDOR_H

#include "ui_Servidor.h"

class Servidor : public QDialog {
	Q_OBJECT
public:
	Servidor();
	virtual ~Servidor();

public slots:
	void updateLogger(const QString& texto);	
private:
	Ui::Servidor widget;
};

#endif	/* _SERVIDOR_H */
