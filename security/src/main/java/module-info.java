module security {
    requires java.desktop;
    requires com.google.common;
    requires com.google.gson;
    requires java.prefs;
    requires miglayout.swing;
    requires miglayout.core;
    requires image;

    opens com.udacity.catpoint.security.data to com.google.gson;

}