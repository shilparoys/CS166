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
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("USER MENU");
                System.out.println("---------");
		System.out.println("1.  Contact List Menu");
		System.out.println("2.  Blocked List Menu");
                System.out.println("3.  Chat Menu");
                System.out.println("4.  Delete Own Account");
                System.out.println("9.  Log out");
                switch (readChoice()){
                   case 1: contactListMenu(esql, authorisedUser); break;
		   case 2: blockListMenu(esql, authorisedUser); break;
                   case 3: chatMenu(esql, authorisedUser); break;
                   case 4: DeleteOwnAccount(esql, authorisedUser); break;
                   case 9: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
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

         String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
	 if (userNum > 0)
		return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

   public static void blockListMenu(Messenger esql, String authorizedUser){
       boolean blockMenu = true;
       while(blockMenu){
	   System.out.println("BLOCK LIST MENU");
	   System.out.println("---------");
	   System.out.println("1.  Add to blocked list");
	   System.out.println("2.  Delete from block list");
	   System.out.println("3.  Browse blocked list");
	   System.out.println("4.  Log out");
	   switch(readChoice()){
	       case 1: AddToBlocked(esql, authorizedUser); break;
	       case 2: DeleteFromBlocked(esql, authorizedUser); break;
	       case 3: ListBlocked(esql, authorizedUser); break;
	       case 4: blockMenu = false; break;
	       default: System.out.println("Unrecognized choice!"); break;
	   }
	   
       }
   }
   
   public static void contactListMenu(Messenger esql, String authorizedUser){
       boolean contactMenu = true;
       while(contactMenu){
	   System.out.println("CONTACT LIST MENU");
	   System.out.println("---------");
	   System.out.println("1.  Add to contact list");
	   System.out.println("2.  Delete from contact list");
	   System.out.println("3.  Browse contact list");
	   System.out.println("4.  Log out");
	   switch(readChoice()){
	       case 1: AddToContact(esql, authorizedUser); break;
	       case 2: DeleteFromContact(esql, authorizedUser); break;
	       case 3: ListContacts(esql, authorizedUser); break;
	       case 4: contactMenu = false; break;
	       default: System.out.println("Unrecognized choice!"); break;
	   }
	   
       }
       
    }
   
   public static boolean isBlankEntry(String variable){
       if(variable.isEmpty()){
	   System.out.println("Must enter something");
	   return true;
       }
       return false;
      
   }
   
   public static boolean isValidEntry(Messenger esql, String variable){
         try{
            String query = String.format("SELECT * FROM USR WHERE login = '%s'", variable);
            int userNum = esql.executeQuery(query);
            if(userNum <= 0){
                System.out.println("Given user id does not exist");
                return true;
            }
            return false;
          }
          catch(Exception e){
                System.err.println(e.getMessage());
                return false;
         }

   }
   public static void AddToContact(Messenger esql, String authorizedUser){
        try{
            //ask user to give contact username
            System.out.println("Enter userId to add to contact list");
            String contactUserId = in.readLine();
	    
	    if(isBlankEntry(contactUserId))
		return;
        
	    if(isValidEntry(esql, contactUserId))
		return; 
	    

            String query = String.format("SELECT contact_list FROM USR WHERE login = '%s'", authorizedUser);
            List<List<String>> result = esql.executeQueryAndReturnResult(query);
            String index = result.get(0).get(0);
            query = String.format("INSERT INTO USER_LIST_CONTAINS(list_id, list_member) VALUES ('%s', '%s')", index, contactUserId);
            esql.executeUpdate(query);
            String print = contactUserId + " is in the contact list";
            System.out.println(print);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
            return;
        }   
   }//end

   public static void ListContacts(Messenger esql, String authorizedUser){
        try{
            String query = String.format("SELECT a.list_member FROM USER_LIST_CONTAINS a, USR b WHERE b.login = '%s' AND b.contact_list = a.list_id", authorizedUser);
            List<List<String>> result = esql.executeQueryAndReturnResult(query);
            for(int i = 0; i < result.size(); i++){
                System.out.println(result.get(i).get(0));
            }
            return;
        }
        catch(Exception e){
            System.err.println(e.getMessage());
            return;
        }
   }//end

   public static void DeleteFromContact(Messenger esql, String authorizedUser){
        try{
            System.out.println("Enter userId to remove from contact list");
            String contactUserId = in.readLine();

	    if(isBlankEntry(contactUserId))
		return;
		
	    if(isValidEntry(esql, contactUserId))
		return;
		
            String query = String.format("SELECT contact_list FROM USR WHERE login = '%s'", authorizedUser);
            List<List<String>> result = esql.executeQueryAndReturnResult(query);
            String index = result.get(0).get(0);
            query = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id = '%s' AND list_member ='%s'", index, contactUserId);
            esql.executeUpdate(query);
            String print = contactUserId + " is removed from contact list";
            System.out.println(print);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
   }//end 

   public static void AddToBlocked(Messenger esql, String authorizedUser){
        try{
            System.out.println("Enter userId to add to blocked list");
            String blockUserId = in.readLine();

            if(isBlankEntry(blockUserId))
		return;

	    if(isValidEntry(esql, blockUserId))
		return;
		
            String query = String.format("SELECT block_list FROM USR WHERE login = '%s'", authorizedUser);
            List<List<String>> result = esql.executeQueryAndReturnResult(query);
            String index = result.get(0).get(0);
            query = String.format("INSERT INTO USER_LIST_CONTAINS(list_id, list_member) VALUES ('%s', '%s')", index, blockUserId);
            esql.executeUpdate(query);
            String print = blockUserId + " is in the block list";
            System.out.println(print);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
        
   }//end Query6

   public static void DeleteFromBlocked(Messenger esql, String authorizedUser){
        try{
            System.out.println("Enter userId to remove from blocked list");
            String blockUserId = in.readLine();

            if(isBlankEntry(blockUserId))
		return;

            if(isValidEntry(esql, blockUserId))
		return;

            String query = String.format("SELECT block_list FROM USR WHERE login = '%s'", authorizedUser);
            List<List<String>> result = esql.executeQueryAndReturnResult(query);
            String index = result.get(0).get(0);
            query = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id = '%s' AND list_member ='%s'", index, blockUserId);
            esql.executeUpdate(query);
            String print = blockUserId + " is removed from block list";
            System.out.println(print);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
   }//end 

   public static void ListBlocked(Messenger esql, String authorizedUser){
     try{
            String query = String.format("SELECT a.list_member FROM USER_LIST_CONTAINS a, USR b WHERE b.login = '%s' AND b.block_list = a.list_id", authorizedUser);
            List<List<String>> result = esql.executeQueryAndReturnResult(query);
            for(int i = 0; i < result.size(); i++){
                System.out.println(result.get(i).get(0));
            }
            return;
        }
        catch(Exception e){
            System.err.println(e.getMessage());
            return;
        }
   }//end Query6

   public static void messageMenu(Messenger esql, String authorizedUser){
       boolean messageMenu = true;
       while(messageMenu){
	    System.out.println("MESSAGE MENU");
            System.out.println("---------");
            System.out.println("1.  Add Message");
            System.out.println("2.  Edit Message");
            System.out.println("3.  Delete Message");
	    System.out.println("4.  Logout");
	    switch(readChoice()){
		case 1: addMessage(esql, authorizedUser, "5");
		case 2: editMessage(esql, authorizedUser, "5");
		case 3: deleteMessage(esql, authorizedUser, "5");
		case 4: messageMenu = false; break;
		default: System.out.println("Unrecognized choice!"); break;
	    }
       }
   }
  
   public static void chatMenu(Messenger esql, String authorizedUser){
        boolean chatmenu = true;
        while(chatmenu){
            System.out.println("CHAT MENU");
            System.out.println("---------");
            System.out.println("1.  Create a chat");
            System.out.println("2.  Chat viewer");
            System.out.println("3.  Log out");
            switch (readChoice()){
                case 1: createChat(esql, authorizedUser); break;
                case 2: chatViewer(esql, authorizedUser); break;
                case 3: chatmenu = false; break;
                default : System.out.println("Unrecognized choice!"); break;
            }
        }
        return;
   }//end 

   public static boolean isValidSender(Messenger esql, String authorizedUser, String chatId){
	try{
	    String query = String.format("SELECT * FROM CHAT WHERE init_sender = '%s' AND chat_id = '%s'", authorizedUser, chatId);
            int userNum = esql.executeQuery(query);
            if(userNum <= 0){
                System.out.println("Not initial sender");
                return true;
            }
	    return false;
	}
	catch(Exception e){
	    System.err.println(e.getMessage());
	    return false;
	}
   }
   
   public static void createChat(Messenger esql, String authorizedUser){
        try{
            //get names of people who you want to chat with 
            System.out.println("Please add usernames to include in chat");
            System.out.println("End with a q");
            String type;
            List<String> chatUsers = new ArrayList<String>();
            String input = in.readLine();
            while (!input.equals("q")){
                chatUsers.add(input);
                input = in.readLine();
            }
            //decide on what kind of chat it is
            if(chatUsers.size() == 0){
                System.out.println("Need to enter a user name");
                return;
            }
            else if(chatUsers.size() == 1){
                type = "private";
            }
            else{
                type = "group";
            }

            //updating the chatId
            String query = String.format("INSERT INTO CHAT(chat_type, init_sender) VALUES('%s', '%s')", type, authorizedUser);
            esql.executeUpdate(query);

            query = "SELECT MAX(chat_id) FROM CHAT";
            List<List<String>> result = esql.executeQueryAndReturnResult(query);
            String chatId = result.get(0).get(0);

            //adding everyone else into the chat
            for(int i = 0; i < chatUsers.size(); i++){
                query = String.format("INSERT INTO CHAT_LIST(chat_id, member) VALUES('%s', '%s')", chatId, chatUsers.get(i));
                esql.executeUpdate(query);
            }

            //adding authorized user to chat
            query = String.format("INSERT INTO CHAT_LIST(chat_id, member) VALUES('%s', '%s')", chatId, authorizedUser);
            esql.executeUpdate(query);

            String print = "New chat has been created with " + authorizedUser + " ";
            for(int i = 0; i < chatUsers.size(); i++){
                print += chatUsers.get(i) + " " ;
            }
            System.out.println(print);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
   } 

   public static void chatViewer(Messenger esql, String authorizedUser){
        try{
            //get the chatid associated with the authorizedUser
            String query = String.format("SELECT chat_id FROM CHAT_LIST WHERE member = '%s'", authorizedUser);
            List<List<String>> chats = esql.executeQueryAndReturnResult(query);

            //print out the memebers in the associated chat
            for(int i = 0 ; i < chats.size(); i++){
                query = String.format("SELECT member FROM CHAT_LIST where chat_id = '%s'", chats.get(i).get(0));
                List<List<String>> members = esql.executeQueryAndReturnResult(query);
                String print = i + ": ";
                System.out.println(print);
                for(int j = 0; j < members.size(); j++){
                    System.out.print(members.get(j).get(0).trim());
                    System.out.print(" ");
                }
                System.out.println();
            }
            System.out.println("Enter chat number to browse");
            String input = in.readLine();
            int inpt = Integer.parseInt(input);
            String chatNum = chats.get(inpt).get(0);
            query = String.format("SELECT * FROM MESSAGE WHERE chat_id = '%s' ORDER BY msg_timestamp", chatNum);
            List<List<String>> messsages = esql.executeQueryAndReturnResult(query);
            chatMenu1(esql, authorizedUser, chatNum);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
        
   }
   
   public static void chatMenu1(Messenger esql, String authorizedUser, String chatNum){
        boolean chatmenu = true;
        while(chatmenu){
            System.out.println(" MENU");
            System.out.println("---------");
            System.out.println("1.  Display Message");
            System.out.println("2.  Add Message");
            System.out.println("3.  Edit Message");
            System.out.println("4.  Delete Message");
            System.out.println("5.  Add user to chat");
            System.out.println("6.  Remove user from chat");
            System.out.println("7.  Delete chat");
            System.out.println("8.  Logout");
            switch (readChoice()){
                case 1: displayMessage(esql, authorizedUser, chatNum); break;
                case 2: addMessage(esql, authorizedUser, chatNum); break;
                case 3: editMessage(esql, authorizedUser, chatNum); break;
                case 4: deleteMessage(esql, authorizedUser, chatNum); break;
                case 5: addChatMember(esql, authorizedUser, chatNum); break;
                case 6: removeChatMember(esql, authorizedUser, chatNum); break;
                case 7: deleteChat(esql, authorizedUser, chatNum);
                case 8: chatmenu = false; break;
                default : System.out.println("Unrecognized choice!"); break;
            }
        }
        return;
   }//end 

   public static void displayMessage(Messenger esql, String authorizeduser, String chatId){
   }
   public static void addChatMember(Messenger esql, String authorizedUser, String chatId){

        try{
             System.out.println("Enter user name to add to chat");
            String chatName = in.readLine();
	    
            if(isBlankEntry(chatName))
		return;
            if(isValidSender(esql, authorizedUser, chatId))
		return;

	    if(isValidEntry(esql, chatName))
		return;
            
            String query = String.format("INSERT INTO CHAT_LIST(chat_id, member) VALUES('%s', '%s')", chatId, chatName);
            esql.executeUpdate(query);
            String print = chatName + " is added to the chat";
            System.out.println(print);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
   }

   public static void removeChatMember(Messenger esql, String authorizedUser, String chatId){
           try{
             System.out.println("Enter user name to remove from chat");
            String chatName = in.readLine();
             if(isBlankEntry(chatName))
		return;

            if(isValidSender(esql, authorizedUser, chatId))
		return;

            if(isValidEntry(esql, chatName))
		return;
            String query = String.format("DELETE FROM CHAT_LIST(chat_id, member) VALUES('%s', '%s')", chatId, chatName);
            esql.executeUpdate(query);
            String print = chatName + " is removed from the chat";
            System.out.println(print);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
 }

   public static void deleteChat(Messenger esql, String authorizedUser, String chatId){
        try{   
             //checking if it is initial sender
            if(isValidSender(esql, authorizedUser, chatId))
		return;
            
            String query = String.format("DELETE FROM MESSAGES WHERE chat_id = '%s'", chatId);
            esql.executeUpdate(query);
            query = String.format("DELETE FROM CHAT_LIST WHERE chat_id = '%s'", chatId);
            esql.executeUpdate(query);
            query = String.format("DELETE FROM CHAT WHERE chat_id = '%s'", chatId);
            esql.executeUpdate(query);
            System.out.println("Chat deleted");
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
            
   }

   public static void addMessage(Messenger esql, String authorizedUser, String chatId){

        try{
            System.out.println("Enter a message");
            String message = in.readLine();
            
            if(isBlankEntry(message))
		return;

            String query = String.format("INSERT INTO MESSAGE(msg_text, msg_timestamp, sender_login, chat_id) VALUES('%s', NOW(), '%s', '%s')", message, authorizedUser, chatId);
            esql.executeUpdate(query);
            System.out.println("Message is added");
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
   
   public static void deleteMessage(Messenger esql, String authorizedUser, String chatId){

        try{
            System.out.println("Enter messageId");
            String messageId = in.readLine();
            
            if(isBlankEntry(messageId))
		return;
            String query = String.format("DELETE FROM MESSAGE WHERE msg_id = '%s'", messageId);
            esql.executeUpdate(query);
            System.out.println("Message is deleted");
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
   
   public static void editMessage(Messenger esql, String authorizedUser, String chatId){

        try{
            System.out.println("Enter messageId");
            String messageId = in.readLine();
            
            if(isBlankEntry(messageId))
		return;
            System.out.println("Enter message");
            String message = in.readLine();
            if(message == ""){
                System.out.println("enter message");
                return;
            }

            String query = String.format("UPDATE MESSAGE SET msg_text = '%s' WHERE msg_id = '%s'", message, messageId);
            esql.executeUpdate(query);
            System.out.println("Message is edited");
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
   
   public static void DeleteOwnAccount(Messenger esql, String authorizedUser){
      //code
   }




























}//end Messenger
