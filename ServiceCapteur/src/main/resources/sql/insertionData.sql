USE P2I2P4;

DELETE FROM capteur;
DELETE FROM installation;
DELETE FROM station;
DELETE FROM localisation;
DELETE FROM typeCapteur;

INSERT INTO typeCapteur (libelleType, unite, symbol)
VALUES ('Température', 'Degré Celsius', '°C'),
       ('Humidité', 'Pourcentage', '%'),
       ('Luminosité', 'Lux', 'lux'),
       ('Concentration CO2', 'Parties par million', 'ppm'),
       ('Concentration O2', 'Parties par million', 'ppm'),
       ('Bruit', 'Décibel', 'dB'),
       ('Particules fines', 'Parties par million', 'ppm'),
       ('Pression', 'Pascal', 'Pa');

INSERT INTO localisation(latitude, longitude, libelle)
VALUES (45.625536, 5.132806, 'Maison Maxou'),
       (45.780080, 4.857453, 'Tête d Or'),
       (45.782736, 4.890568, 'Périph Buers'),
       (45.783769, 4.872619, 'TC INSA LYON'),
       (45.744530, 4.862280, 'Chez Loulou'),
       (45.781659, 4.870688, 'BU Lyon 1'),
       (45.780126, 4.872831, 'Résidence C'),
       (45.780067, 4.874183, 'Résidence D'),
       (45.782319, 4.876694, 'BMC'),
       (45.786516, 4.883496, 'IUT'),
       (45.784526, 4.883217, 'Résidence A'),
       (45.784623, 4.883957, 'Résidence B');

INSERT INTO station (nomStation)
VALUES ('Station Charlie'),
       ('Station Barkhane');

INSERT INTO installation (dateDebut, dateFin, idLocalisation, idStation)
VALUES ('2020-05-01', '2020-05-9', 1, 1),
       ('2020-05-10', '2020-06-01', 7, 1),
       ('2020-05-01', '2020-05-19', 2, 2),
       ('2020-05-20', '2020-06-01', 5, 2);


INSERT INTO capteur (idTypeCapteur, idStation)
VALUES (6, 1),
       (3, 1),
       (2, 1),
       (1, 1),
       (4, 2),
       (7, 2),
       (8, 2),
       (5, 2);
