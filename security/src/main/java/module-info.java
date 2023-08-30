module com.security {
    requires java.desktop;
    requires com.google.common;
    requires transitive com.google.gson;
    requires java.prefs;
    requires miglayout.swing;
    requires miglayout.core;
    requires image;

   opens com.security.data to com.google.gson;
}