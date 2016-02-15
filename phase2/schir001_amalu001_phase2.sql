DROP TABLE Chat;

DROP TABLE Message;

DROP TABLE User_List;

DROP TABLE Users;




CREATE TABLE Users(
   phoneNum CHAR(13) NOT NULL,
   login  CHAR(50),
   password CHAR(50) NOT NULL,
   status CHAR(140),
   UNIQUE(phoneNum, login),
   PRIMARY KEY(login)
);

CREATE TABLE User_List(
   list_type CHAR(10) NOT NULL,
   ulogin CHAR(50),
   UNIQUE(ulogin),
   FOREIGN KEY(ulogin) REFERENCES Users(login)
);

CREATE TABLE Message(
   id INT,
   text CHAR(140) NOT NULL,
   timestamp TIMESTAMP NOT NULL,
   status CHAR(30) NOT NULL,
   PRIMARY KEY(id)
);



CREATE TABLE Chat(
   id INT,
   chat_type CHAR(10) NOT NULL,
   PRIMARY KEY(id)
);

