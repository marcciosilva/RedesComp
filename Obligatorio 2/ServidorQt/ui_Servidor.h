/********************************************************************************
** Form generated from reading UI file 'Servidor.ui'
**
** Created by: Qt User Interface Compiler version 4.8.6
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_SERVIDOR_H
#define UI_SERVIDOR_H

#include <QtCore/QVariant>
#include <QtGui/QAction>
#include <QtGui/QApplication>
#include <QtGui/QButtonGroup>
#include <QtGui/QDialog>
#include <QtGui/QGridLayout>
#include <QtGui/QHeaderView>
#include <QtGui/QTextBrowser>

QT_BEGIN_NAMESPACE

class Ui_Servidor
{
public:
    QGridLayout *gridLayout;
    QTextBrowser *logger;

    void setupUi(QDialog *Servidor)
    {
        if (Servidor->objectName().isEmpty())
            Servidor->setObjectName(QString::fromUtf8("Servidor"));
        Servidor->resize(400, 300);
        gridLayout = new QGridLayout(Servidor);
        gridLayout->setObjectName(QString::fromUtf8("gridLayout"));
        logger = new QTextBrowser(Servidor);
        logger->setObjectName(QString::fromUtf8("logger"));

        gridLayout->addWidget(logger, 0, 0, 1, 1);


        retranslateUi(Servidor);

        QMetaObject::connectSlotsByName(Servidor);
    } // setupUi

    void retranslateUi(QDialog *Servidor)
    {
        Servidor->setWindowTitle(QApplication::translate("Servidor", "Servidor", 0, QApplication::UnicodeUTF8));
    } // retranslateUi

};

namespace Ui {
    class Servidor: public Ui_Servidor {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_SERVIDOR_H
