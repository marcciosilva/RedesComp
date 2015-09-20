/********************************************************************************
** Form generated from reading UI file 'ServidorForm.ui'
**
** Created by: Qt User Interface Compiler version 4.8.6
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_SERVIDORFORM_H
#define UI_SERVIDORFORM_H

#include <QtCore/QVariant>
#include <QtGui/QAction>
#include <QtGui/QApplication>
#include <QtGui/QButtonGroup>
#include <QtGui/QHeaderView>
#include <QtGui/QMainWindow>
#include <QtGui/QMenuBar>
#include <QtGui/QStatusBar>
#include <QtGui/QWidget>

QT_BEGIN_NAMESPACE

class Ui_ServidorForm
{
public:
    QWidget *centralwidget;
    QMenuBar *menubar;
    QStatusBar *statusbar;

    void setupUi(QMainWindow *ServidorForm)
    {
        if (ServidorForm->objectName().isEmpty())
            ServidorForm->setObjectName(QString::fromUtf8("ServidorForm"));
        ServidorForm->resize(800, 600);
        centralwidget = new QWidget(ServidorForm);
        centralwidget->setObjectName(QString::fromUtf8("centralwidget"));
        ServidorForm->setCentralWidget(centralwidget);
        menubar = new QMenuBar(ServidorForm);
        menubar->setObjectName(QString::fromUtf8("menubar"));
        menubar->setGeometry(QRect(0, 0, 800, 22));
        ServidorForm->setMenuBar(menubar);
        statusbar = new QStatusBar(ServidorForm);
        statusbar->setObjectName(QString::fromUtf8("statusbar"));
        ServidorForm->setStatusBar(statusbar);

        retranslateUi(ServidorForm);

        QMetaObject::connectSlotsByName(ServidorForm);
    } // setupUi

    void retranslateUi(QMainWindow *ServidorForm)
    {
        ServidorForm->setWindowTitle(QApplication::translate("ServidorForm", "ServidorForm", 0, QApplication::UnicodeUTF8));
    } // retranslateUi

};

namespace Ui {
    class ServidorForm: public Ui_ServidorForm {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_SERVIDORFORM_H
