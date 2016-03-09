/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Messenger {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   public String username;
   /**
    * Creates a new instance of Messenger
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Messenger (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         username = user;
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Messenger

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
         if(outputHeader){
            for(int i = 1; i <= numCol; i++){
               System.out.print(rsmd.getColumnName(i) + "\t");
            }
            System.out.println();
            outputHeader = false;
         }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
      // creates a statement object 
      Statement stmt = this._connection.createStatement (); 

      // issues the query instruction 
      ResultSet rs = stmt.executeQuery (query); 

      /* 
       ** obtains the metadata object for the returned result set.  The metadata 
       ** contains row and column info. 
       */ 
      ResultSetMetaData rsmd = rs.getMetaData (); 
      int numCol = rsmd.getColumnCount (); 
      int rowCount = 0; 

      // iterates through the result set and saves the data returned by the query. 
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>(); 
      while (rs.next()){
         List<String> record = new ArrayList<String>(); 
         for (int i=1; i<=numCol; ++i) 
            record.add(rs.getString (i)); 
         result.add(record); 
      }//end while 
      stmt.close (); 
      return result; 
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      int rowCount = 0;

      // iterates through the result set and count nuber of results.
      if(rs.next()){
         rowCount++;
      }//end while
      stmt.close ();
      return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current 
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
      Statement stmt = this._connection.createStatement ();

      ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
               "Usage: " +
               "java [-classpath <classpath>] " +
               Messenger.class.getName () +
               " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Messenger esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Messenger object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Messenger (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println();
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println(".........................");
            System.out.println("0. EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 0: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch

            if (authorisedUser != null) {
               boolean usermenu = true;
               while(usermenu) {
                  System.out.println();
                  System.out.println("MAIN MENU");
                  System.out.println("---------");
                  System.out.println("1. Add to contact list");
                  System.out.println("2. Delete from contact list");
                  System.out.println("3. Browse contact list");
                  System.out.println("4. Add to blocked list");
                  System.out.println("5. Delete from blocked list");
                  System.out.println("6. Browse blocked list");
                  System.out.println("7. Create a new chat");
                  System.out.println("8. Browse chats");
                  System.out.println(".........................");
                  System.out.println("0. Log out");
                  switch (readChoice()){
                     case 1: AddToContact(esql, authorisedUser); break;
                     case 2: DeleteFromContact(esql, authorisedUser); break;
                     case 3: ListContacts(esql, authorisedUser); break;
                     case 4: AddToBlocked(esql, authorisedUser); break;
                     case 5: DeleteFromBlocked(esql, authorisedUser); break;
                     case 6: ListBlocked(esql, authorisedUser); break;
                     case 7: NewChat(esql, authorisedUser); break;
                     case 8: SelectChat(esql, authorisedUser); break;
                     case 0: usermenu = false; break;
                     default : System.out.println("Unrecognized choice!"); break;
                  }
               }
            }
         }//end while
      }

      catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
            "\n\n*******************************************************\n" +
            "              User Interface      	               \n" +
            "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(Messenger esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();

         //Creating empty contact\block lists for a user
         esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('block')");
         int block_id = esql.getCurrSeqVal("user_list_list_id_seq");
         esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('contact')");
         int contact_id = esql.getCurrSeqVal("user_list_list_id_seq");

         String query = String.format("INSERT INTO USR (phoneNum, login, password, block_list, contact_list) VALUES ('%s','%s','%s',%s,%s)", phone, login, password, block_id, contact_id);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Messenger esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s';", login, password);
         int userNum = esql.executeQuery(query);
         if (userNum > 0)
            return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

   /*
    * Prompt user for another user to add to contact list
    **/
   public static void AddToContact(Messenger esql, String username){
      try {
         System.out.print("Enter username: ");
         String userToAdd = in.readLine();
         String query = "SELECT contact_list FROM USR WHERE login = '";
//         System.out.println(username);
         query += username;
         query += "';";

         // users own contact list id
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         String index = result.get(0).get(0);
//         System.out.println(index);
         query = String.format("INSERT INTO USER_LIST_CONTAINS(list_id, list_member) VALUES ('%s', '%s')", index, userToAdd);
         esql.executeUpdate(query);
         System.out.print("User <");
         System.out.print(userToAdd);
         System.out.println("> added to contact list");
         return;
      }

      catch (Exception e) {
         System.out.println(e.getMessage());
         return;
      }

   }//end

   /*
    * Allows user to delete selected contact from contact list
    **/
   public static void DeleteFromContact(Messenger esql, String username) {
      try {
         System.out.print("Enter user to be deleted: ");
         String input = in.readLine();
         String query = String.format("SELECT a.list_member FROM USER_LIST_CONTAINS a, USR b WHERE b.login = '%s' AND b.contact_list = a.list_id;", username);
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         // check to verify that user is in contact list
//         System.out.print(result.size());
//         System.out.println(" users in contact list: ");
/*         boolean flag = false;
//         System.out.print(input);
         for (int i = 0; i < result.size(); i++) {
            if (input.equals(result.get(i).get(0))) {
               flag = true;
               System.out.println(result.get(i).get(0));
               break;
            }
         }
         if (!flag) {
            System.out.println("user not found in contact list");
//            return;
         }*/
         query = String.format("SELECT contact_list FROM USR WHERE login = '%s';", username);
         result = esql.executeQueryAndReturnResult(query);
         query = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_member = '%s' AND list_id = '%s';", input, result.get(0).get(0));
         esql.executeUpdate(query);
         System.out.print("User <");
         System.out.print(input);
         System.out.println("> removed from contact list");
         return;
      }

      catch (Exception e) {
         System.out.println(e.getMessage());
         return;
      }
   }

   /*
    * Loops over contact list and prints each contact on its own line
    **/
   public static void ListContacts(Messenger esql, String username){
      try {
         String query = String.format("SELECT a.list_member FROM USER_LIST_CONTAINS a, USR b WHERE b.login = '%s' AND b.contact_list = a.list_id;", username);
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         System.out.print(result.size());
         System.out.println(" users in contact list: ");
         for (int i = 0; i < result.size(); i++) {
            System.out.println(result.get(i).get(0));
         }
         return;

      }

      catch (Exception e) {
         System.out.println(e.getMessage());
         return;
      }

   }//end

   /* 
    * Add another user to one's own blocked list
    **/
   public static void AddToBlocked(Messenger esql, String username){
      try {
         System.out.print("Enter username: ");
         String userToAdd = in.readLine();
         String query = "SELECT block_list FROM USR WHERE login = '";
//         System.out.println(username);
         query += username;
         query += "';";
         List<List<String>> result = esql.executeQueryAndReturnResult(query);

         // ID of user's blocked list
         String index = result.get(0).get(0);
//         System.out.println(index);
         query = String.format("INSERT INTO USER_LIST_CONTAINS(list_id, list_member) VALUES ('%s', '%s')", index, userToAdd);
         esql.executeUpdate(query);
         System.out.print("User <");
         System.out.print(userToAdd);
         System.out.println("> added to blocked list");
         return;
      }

      catch (Exception e) {
         System.out.println(e.getMessage());
         return;
      }

   }//end

   /*
    * Allow user to delete selected user from own blocked list
    **/
   public static void DeleteFromBlocked(Messenger esql, String username) {
      try {
         System.out.print("Enter user to be deleted: ");
         String input = in.readLine();
         String query = String.format("SELECT a.list_member FROM USER_LIST_CONTAINS a, USR b WHERE b.login = '%s' AND b.block_list = a.list_id;", username);
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
//         System.out.print(result.size());
//         System.out.println(" users in blocked list: ");
/*         boolean flag = false;
//         System.out.print(input);
         for (int i = 0; i < result.size(); i++) {
            if (input.equals(result.get(i).get(0))) {
               flag = true;
               System.out.println(result.get(i).get(0));
               break;
            }
         }
         if (!flag) {
            System.out.println("user not found in contact list");
//            return;
         }*/
         query = String.format("SELECT block_list FROM USR WHERE login = '%s';", username);
         result = esql.executeQueryAndReturnResult(query);
         query = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_member = '%s' AND list_id = '%s';", input, result.get(0).get(0));
         esql.executeUpdate(query);
         System.out.print("User <");
         System.out.print(input);
         System.out.println("> removed from blocked list");
         return;
      }

      catch (Exception e) {
         System.out.println(e.getMessage());
         return;
      }
   }

   /*
    * List all users in one's blocked list, each on its own line
    **/
   public static void ListBlocked(Messenger esql, String username){
      try {
         String query = String.format("SELECT a.list_member FROM USER_LIST_CONTAINS a, USR b WHERE b.login = '%s' AND b.block_list = a.list_id;", username);
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         System.out.print(result.size());
         System.out.println(" users in blocked list: ");
         for (int i = 0; i < result.size(); i++) {
            System.out.println(result.get(i).get(0));
         }
         return;

      }

      catch (Exception e) {
         System.out.println(e.getMessage());
         return;
      }

   }//end


   /*
    * Allow user to create new chat
    * Chat can contain any number of users, not restricted to contact list
    * After starting chat, user will be prompted to create a message
    **/
   public static void NewChat(Messenger esql, String username){
      try {
         System.out.println("Enter users to chat with, each on its own line");
         System.out.println("Enter empty line to finish");
         List<String> users = new ArrayList<String>();
         while(true) {
            String input = in.readLine();
            if (input.equals("")) break;
            users.add(input);
         }
         String type = "";
         if (users.size() == 0) {
            System.out.println("No users added");
            return;
         }
         else if (users.size() == 1) {
            type = "private";
         }
         else {
            type = "group";
         }

         String query = String.format("INSERT INTO CHAT(chat_type, init_sender) VALUES('%s', '%s');", type, username);
         esql.executeUpdate(query);
      
         query = "SELECT MAX(chat_id) FROM CHAT;";
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         String id = result.get(0).get(0);

         for (int i = 0; i < users.size(); i++) {
            query = String.format("INSERT INTO CHAT_LIST(chat_id, member) VALUES(%s, '%s');", id, users.get(i));
            esql.executeUpdate(query);
         }
         query = String.format("INSERT INTO CHAT_LIST(chat_id, member) VALUES(%s, '%s');", id, username);
         esql.executeUpdate(query);
         System.out.println("...Chat created");

         System.out.println("Enter message(300 characters)");
         String message = in.readLine();

         if (message.length() > 300) {
            System.out.println("Message too long");
            return;
         }
         query = String.format("INSERT INTO MESSAGE(msg_text, msg_timestamp, sender_login, chat_id) VALUES('%s', NOW(), '%s', %s);", message, username, id);
         esql.executeUpdate(query);

         System.out.println();
         System.out.println("...Message sent");

      }

      catch (Exception e) {
         System.out.println(e.getMessage());
      }
   }//end 

   /*
    * Lists all chats that user is a part of
    * Chats are identified by a serial sequence of numbers,
    * as well as all the members of the chat
    * Following the list of chats, the user will be prompted to 
    * select a chat to perform further actions on
    * The chosen chat will be displayed by showing the last 10 messages sent
    * A menu of options will then be show to allow further actions
    **/
   public static void SelectChat(Messenger esql, String username) {
      try {
         String query = String.format("SELECT chat_id FROM CHAT_LIST WHERE member = '%s';", username);
         List<List<String>> chats = esql.executeQueryAndReturnResult(query);
         //System.out.println(username);
         System.out.println(query);
         System.out.println(chats.size());

         //print out all the members of each chat
         for (int i = 0; i < chats.size(); i++) {
            query = String.format("SELECT member FROM CHAT_LIST WHERE chat_id = '%s';", chats.get(i).get(0));
            List<List<String>> mems = esql.executeQueryAndReturnResult(query); 
            System.out.print(i);
            System.out.print(": ");
            for (int j = 0; j < mems.size(); j++) {
               System.out.print(mems.get(j).get(0).trim());
               System.out.print(", ");
            }
            System.out.println();
         }

         System.out.println();
         //System.out.print("Select chat number: ");
         int input = readChoice();
         String chat_num = chats.get(input).get(0);
         query = String.format("SELECT * FROM MESSAGE WHERE chat_id = %s ORDER BY msg_timestamp;", chat_num);
         List<List<String>> messages = esql.executeQueryAndReturnResult(query);
         int msg_num = 0;
         msg_num = DisplayTen(esql, messages, msg_num);

         //menu
         boolean chatmenu = true;
         while (chatmenu) {
            messages = esql.executeQueryAndReturnResult(query);
            System.out.println();
            System.out.println("----------CHAT MENU----------");
            System.out.println("1: Display next 10 messages");
            System.out.println("2: Add user to chat");
            System.out.println("3: Remove user from chat");
            System.out.println("4: Add message to chat");
            System.out.println("5: Edit previous message");
            System.out.println("6: Delete previous message");
            System.out.println("7: Remove entire chat");
            System.out.println(".........................");
            System.out.println("0: Exit chat viewer");
            System.out.print("Enter choice: ");

            switch(readChoice()) {
               case 1: msg_num = DisplayTen(esql, messages, msg_num); break;
               case 2: AddUserToChat(esql, username, chat_num); break;
               case 3: RemoveUserFromChat(esql, username, chat_num); break;
               case 4: AddMessage(esql, username, chat_num);
                       msg_num -= 10;
                       if (msg_num < 0) msg_num = 0;
                       break;
               case 5: EditMessage(esql, username, chat_num); 
                       msg_num -= 10;
                       if (msg_num < 0) msg_num = 0;
                       break;
               case 6: DeleteMessage(esql, username, chat_num);
                       msg_num -= 10;
                       if (msg_num < 0) msg_num = 0;
                       break;
               case 7: DeleteChat(esql, username, chat_num); break;
               case 0: chatmenu = false; break;
               default: System.out.println("Unrecognized choice!"); break;
            }//end switch
         }
      }

      catch(Exception e) {
         System.out.println(e.getMessage());
      }
      return;
   }

   /*
    * Shows initial sender, timestamp, message id, and message content
    * of specified message
    **/ 
   public static void DisplayMessage(Messenger esql, String id) {
      try {
         String query = String.format("SELECT * FROM MESSAGE WHERE msg_id = %s;", id);
         List<List<String>> result = esql.executeQueryAndReturnResult(query);
         System.out.print("Sender: ");
         System.out.println(result.get(0).get(3));
         System.out.print("Time: ");
         System.out.println(result.get(0).get(2));
         System.out.print("Message ID: ");
         System.out.println(result.get(0).get(0));
         System.out.println();
         System.out.println(result.get(0).get(1));
         //System.out.println();
      }

      catch (Exception e) {
         System.out.println(e.getMessage());
      }

      return;
   }

   /*
    * Displays next ten newest messages, 
    * starting with message indicated by msg_num
    * @return the updated starting message number
    **/
   public static int DisplayTen (Messenger esql, List<List<String>> messages, int msg_num) {
      try {
         System.out.println();
         System.out.println("-------------------------");
         System.out.println();
         if (msg_num >= messages.size()) 
            System.out.println("No older messages");
         for (; msg_num < msg_num + 10; msg_num++) {
            if (msg_num >= messages.size()) {
               return msg_num;
            }
            DisplayMessage(esql, messages.get(msg_num).get(0));
            System.out.println("-------------------------");
            System.out.println();
         }
      }

      catch (Exception e) {
         System.out.println(e.getMessage());
      }
      return msg_num;
   }

   /*
    * Allows creator of chat to add new user
    **/
   public static void AddUserToChat (Messenger esql, String username, String chat_num) {
      try {
         String query = String.format("SELECT * FROM CHAT WHERE chat_id = %s AND init_sender = '%s';", chat_num, username);
         // will be 1 if user is owner of chat, 0 otherwise
         int check = esql.executeQuery(query);

         if (check < 1) {
            System.out.println("Only the owner of a chat can add users");
            return;
         }
         System.out.print("Enter user to add: ");
         String userToAdd = in.readLine();
         query = String.format("INSERT INTO CHAT_LIST(chat_id, member) VALUES(%s, '%s');", chat_num, userToAdd);
         esql.executeUpdate(query);
         System.out.println("User added");
      }
      
      catch (Exception e) {
         System.out.println(e.getMessage());
      }
      return;
   }

   /*
    * Allows creator of chat to remove user
    **/
   public static void RemoveUserFromChat (Messenger esql, String name, String num) {
      try {
         String query = String.format("SELECT * FROM CHAT WHERE chat_id = %s AND init_sender = '%s';", num, name);
         // will be 1 if user is owner of chat, 0 otherwise
         int check = esql.executeQuery(query);

         if (check < 1) {
            System.out.println("Only the owner of a chat can add users");
            return;
         }
         System.out.print("Enter user to remove: ");
         String userToRemove = in.readLine();
         query = String.format("DELETE FROM CHAT_LIST WHERE chat_id = %s AND member = '%s';", num, userToRemove);
         esql.executeUpdate(query);
         System.out.println("User removed");
      }
      
      catch (Exception e) {
         System.out.println(e.getMessage());
      }
      return;
   }

   /*
    * User can write new message for a chat
    **/
   public static void AddMessage (Messenger esql, String name, String num) {
      try {
         System.out.println("Enter message(300 characters)");
         String message = in.readLine();
         if (message.length() > 300) {
            System.out.println("Message too long");
            return;
         }

         String query = String.format("INSERT INTO MESSAGE(msg_text, msg_timestamp, sender_login, chat_id) VALUES('%s', NOW(), '%s', %s);", message, name, num);
         esql.executeUpdate(query);

         System.out.println();
         System.out.println("...Message sent");
      }

      catch (Exception e) {
         System.out.println(e.getMessage());
      }
      return;
   }

   /*
    * User can edit a previously written message
    **/
   public static void EditMessage (Messenger esql, String name, String num) {
      try {
         System.out.print("Enter message number: ");
         String msg_num = in.readLine();
         String query = String.format("SELECT * FROM MESSAGE WHERE msg_id = %s AND sender_login = '%s' AND chat_id = %s;", msg_num, name, num);
         
         // 1 if valid message, 0 otherwise
         int check = esql.executeQuery(query);
         if (check < 1) {
            System.out.println("Not a valid message");
            return;
         }

         System.out.println("Enter new message(300 characters)");
         String message = in.readLine();
         if (message.length() > 300) {
            System.out.println("Message too long");
            return;
         }

         query = String.format("UPDATE MESSAGE SET msg_text = '%s' WHERE msg_id = %s;", message, msg_num);
         esql.executeUpdate(query);
         System.out.println();
         System.out.println("...Edit complete");
      }

      catch (Exception e) {
         System.out.println(e.getMessage());
      }
   }

   /*
    * User can delete a previously written message
    **/
   public static void DeleteMessage (Messenger esql, String name, String num) {
      try {
         System.out.print("Enter message number: ");
         String msg_num = in.readLine();
         String query = String.format("SELECT * FROM MESSAGE WHERE msg_id = %s AND sender_login = '%s' AND chat_id = %s;", msg_num, name, num);
         
         // 1 if valid message, 0 otherwise
         int check = esql.executeQuery(query);
         if (check < 1) {
            System.out.println("Not a valid message");
            return;
         }

         query = String.format("DELETE FROM MESSAGE WHERE msg_id = %s;", msg_num);
         esql.executeUpdate(query);
         System.out.println();
         System.out.println("...Message deleted");
      }

      catch (Exception e) {
         System.out.println(e.getMessage());
      }
   }

   /*
    * Owner of chat can delete entire chat, along with all associated messages
    **/
   public static void DeleteChat (Messenger esql, String name, String num) {
      try {
         System.out.print("Delete entire chat and all associated messages? (y/n): ");
         if (!in.readLine().equals('y')) {
            return;
         }
         String q = String.format("SELECT * FROM CHAT WHERE chat_id = %s AND init_sender = name;", num, name);
         
         // 1 if valid message, 0 otherwise
         int check = esql.executeQuery(q);
         if (check < 1) {
            System.out.println("Only the owner can remove a chat");
            return;
         }

         q = String.format("DELETE FROM MESSAGES WHERE chat_id = %s;", num);
         esql.executeUpdate(q);
         q = String.format("DELETE FROM CHAT_LIST WHERE chat_id = %s;", num);
         esql.executeUpdate(q);
         q = String.format("DELETE FROM CHAT WHERE chat_id = %s;", num);
         esql.executeUpdate(q);
         System.out.println();
         System.out.println("...Chat deleted");
      }

      catch (Exception e) {
         System.out.println(e.getMessage());
      }
   }



   /*
    * I don't know what this is for
    **/
   public static void Query6(Messenger esql){
      // Your code goes here.
      // ...
      // ...
   }//end Query6

}//end Messenger




































