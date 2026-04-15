module GestionPharmacie {
    requires java.sql;
    requires java.desktop;
    requires java.prefs;
    
   
    exports main;
    exports controllers;
    exports dao;
    exports models;
    exports services;
    exports utils;
    exports views;
  
    exports test;
}