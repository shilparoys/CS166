DROP TABLE Work_proj;

DROP TABLE Work_dept;

DROP TABLE Work_in;

DROP TABLE Graduate;

DROP TABLE Department;

DROP TABLE Project;

DROP TABLE Professor;



CREATE TABLE Professor(
   ssn CHAR(9) NOT NULL,
   name CHAR(50) NOT NULL,
   age int,
   rank CHAR(10),
   specialty CHAR(100),
   PRIMARY KEY(ssn)
);


CREATE TABLE Project(
   pid INTEGER NOT NULL, 
   sponsor CHAR(30),
   start_date DATE NOT NULL,
   end_date DATE, 
   budget FLOAT,
   manager CHAR(9) NOT NULL,
   PRIMARY KEY(pid),
   FOREIGN KEY(manager) REFERENCES Professor(ssn)
);


CREATE TABLE Department(
   dno INTEGER NOT NULL,
   dname CHAR(30) NOT NULL,
   office CHAR(50),
   chairman CHAR(9) NOT NULL
   PRIMARY KEY(dno),
   FOREIGN KEY(chairman) REFERENCES Professor(ssn)
);


/* I cannot set grad_advisor NOT NULL otherwise the first 
   graduate student inserted into this table must have 
   him/hersel as grad advisor.
*/

CREATE TABLE Graduate(
   ssn CHAR(9) NOT NULL,
   age INTEGER,
   name CHAR(50) NOT NULL,
   deg_prog CHAR(100),
   grad_advisor CHAR(9),
   major INTEGER NOT NULL,
   PRIMARY KEY(ssn),
   FOREIGN KEY(grad_advisor) REFERENCES Graduate(ssn),
   FOREIGN KEY(major) REFERENCES Department(dno)
);


CREATE TABLE Work_proj(
   grad_ssn CHAR(9),
   professor_ssn CHAR(9) NOT NULL,
   project_pid INTEGER,
   since DATE NOT NULL,
   PRIMARY KEY(grad_ssn, project_pid),
   FOREIGN KEY(grad_ssn) REFERENCES Graduate(ssn),
   FOREIGN KEY(professor_ssn) REFERENCES Professor(ssn),
   FOREIGN KEY(project_pid) REFERENCES Project(pid)
);


/* There is no way to restrict the total constraint in DDL 
   part of SQL for this relationship.
*/

CREATE TABLE Work_Dept(
   professor_ssn CHAR(9),
   dept_dno INTEGER  NOT NULL,
   pc_time INTEGER,
   PRIMARY KEY(professor_ssn, dept_dno),
   FOREIGN KEY(professor_ssn) REFERENCES Professor(ssn),
   FOREIGN KEY(dept_dno) REFERENCES Department(dno)
);


/* There is no way to restrict the total constraint in DDL 
   part of SQL for this relationship.
*/

CREATE TABLE Work_in(
   professor_ssn CHAR(9),
   project_pid INTEGER,
   PRIMARY KEY(professor_ssn, project_pid),
   FOREIGN KEY(professor_ssn) REFERENCES Professor(ssn),
   FOREIGN KEY(project_pid) REFERENCES Project(pid)
);


