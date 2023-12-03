module com.example.scheduler {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires kotlin.stdlib;
    requires javafx.media;
    requires javafx.graphics;

    requires org.kordamp.ikonli.javafx;
    requires java.sql;
    requires com.dustinredmond.fxtrayicon;
    requires MaterialFX;
    requires de.jensd.fx.glyphs.fontawesome;
    requires kotlinx.coroutines.core.jvm;
    requires jdk.javadoc;
    requires org.apache.poi.ooxml;
    requires kotlinx.coroutines.javafx;
    requires java.rmi;
    requires jdk.hotspot.agent;
    requires org.apache.logging.log4j.core;
    requires org.junit.jupiter.api;
    requires com.fasterxml.jackson.databind;
    requires jbcrypt;

    opens com.example.scheduler to javafx.graphics;
    opens com.example.scheduler.controller to javafx.fxml;
    exports com.example.scheduler;
}