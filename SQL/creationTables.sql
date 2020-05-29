CREATE SCHEMA IF NOT EXISTS P2I2P4;
USE P2I2P4;

DROP TABLE IF EXISTS installation;
DROP TABLE IF EXISTS localisation;
DROP TABLE IF EXISTS mesure;
DROP TABLE IF EXISTS capteur;
DROP TABLE IF EXISTS typeCapteur;
DROP TABLE IF EXISTS station;

CREATE TABLE station(
    idStation INT(10) AUTO_INCREMENT,
    nomStation VARCHAR(100),

    PRIMARY KEY(idStation)
);

CREATE TABLE typeCapteur(
    idTypeCapteur INT(4) AUTO_INCREMENT,
    libelleType VARCHAR(100),
    unite VARCHAR(100),
    symbol VARCHAR(10),

    PRIMARY KEY(idTypeCapteur)
);

CREATE TABLE capteur(
    idCapteur INT(10) AUTO_INCREMENT,
    idTypeCapteur INT(4) NOT NULL,
    idStation INT(4) NOT NULL,

    PRIMARY KEY(idCapteur),
    FOREIGN KEY(idTypeCapteur) REFERENCES typeCapteur(idTypeCapteur),
    FOREIGN KEY(idStation) REFERENCES station(idStation)
);

CREATE TABLE mesure(
   idMesure INT(10) AUTO_INCREMENT,
   idCapteur INT(10) NOT NULL,
   valeur DOUBLE NOT NULL,
   dateMesure DATETIME NOT NULL,

   PRIMARY KEY(idMesure),
   FOREIGN KEY(idCapteur) REFERENCES capteur(idCapteur)
);

CREATE TABLE localisation(
     idLocalisation INT(10) AUTO_INCREMENT,
     latitude DOUBLE NOT NULL,
     longitude DOUBLE NOT NULL,
     libelle VARCHAR(100),

     PRIMARY KEY(idLocalisation)
);

CREATE TABLE installation(
     dateDebut DATETIME DEFAULT NOW(),
     dateFin DATETIME,
     idLocalisation INT(10) NOT NULL,
     idStation INT(10) NOT NULL,

     PRIMARY KEY(idStation, idLocalisation, dateDebut),
     FOREIGN KEY(idStation) REFERENCES station(idStation),
     FOREIGN KEY(idLocalisation) REFERENCES localisation(idLocalisation)
);