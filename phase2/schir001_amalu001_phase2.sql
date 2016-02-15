DROP TABLE UContains;

DROP TABLE Block;

DROP TABLE Contact;

DROP TABLE Chat_List;

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
   PRIMARY KEY(list_type),
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

CREATE TABLE Chat_List(
	cl_login CHAR(50) NOT NULL,
	cl_id INT,
	UNIQUE(cl_login),
	PRIMARY KEY(cl_login),
	FOREIGN KEY(cl_login) REFERENCES USERS(login),
	FOREIGN KEY(cl_id) REFERENCES Chat(id)
);

CREATE TABLE Contact(
	c_login CHAR(50) NOT NULL,
	c_list_type CHAR(10) NOT NULL,
	UNIQUE(c_login),
	PRIMARY KEY(c_login),
	FOREIGN KEY(c_login) REFERENCES USERS(login),
	FOREIGN KEY(c_list_type) REFERENCES User_List(list_type)
);

CREATE TABLE Block(
	b_login CHAR(50) NOT NULL,
	b_list_type CHAR(10) NOT NULL,
	UNIQUE(b_login),
	PRIMARY KEY(b_login),
	FOREIGN KEY(b_login) REFERENCES USERS(login),
	FOREIGN KEY(b_list_type) REFERENCES User_List(list_type)
);

CREATE TABLE UContains(
	b_login CHAR(50),
	b_list_type CHAR(10),
	PRIMARY KEY(b_login),
	FOREIGN KEY(b_login) REFERENCES USERS(login),
	FOREIGN KEY(b_list_type) REFERENCES User_List(list_type)
);



