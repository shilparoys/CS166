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
            System.out.println();
            System.out.println("=====================");
            System.out.println("\tMAIN MENU");
            System.out.println("---------------------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            System.out.println("---------------------");
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
                System.out.println();
                System.out.println("=====================");
                System.out.println("\tUSER MENU");
                System.out.println("---------------------");
		        System.out.println("1.  Contact List Menu");
		        System.out.println("2.  Blocked List Menu");
                System.out.println("3.  Chat Menu");
                System.out.println("4.  Delete Own Account");
                System.out.println("9.  Log out");
                System.out.println("---------------------");
                switch (readChoice()){
                   case 1: contactListMenu(esql, authorisedUser); break;
		           case 2: blockListMenu(esql, authorisedUser); break;
                   case 3: chatMenu(esql, authorisedUser); break;
                   case 4: usermenu = DeleteOwnAccount(esql, authorisedUser); break;
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
   /**
    * Block List Menu provides users the option to
    * Add users to blocked list, 
    * delete users from blocked list, 
    * browse users in blocked list
    * or go back to USER MENU
    */
   public static void blockListMenu(Messenger esql, String authorizedUser){
       boolean blockMenu = true;
       while(blockMenu){
       System.out.println();
       System.out.println("=====================");
	   System.out.println("\tBLOCK LIST MENU");
       System.out.println("---------------------");
	   System.out.println("1.  Add to blocked list");
	   System.out.println("2.  Delete from block list");
	   System.out.println("3.  Browse blocked list");
	   System.out.println("4.  Back to USER MENU");
       System.out.println("---------------------");
	   switch(readChoice()){
	       case 1: AddToBlocked(esql, authorizedUser); break;
	       case 2: DeleteFromBlocked(esql, authorizedUser); break;
	       case 3: ListBlocked(esql, authorizedUser); break;
	       case 4: blockMenu = false; break;
	       default: System.out.println("Unrecognized choice!"); break;
	   }
	   
       }
   }
   
   /**
    * Contact List Menu provides users the option to
    * Add users to contact list, 
    * delete users from contact list, 
    * browse users in contact list
    * or go back to USER MENU
    */
   public static void contactListMenu(Messenger esql, String authorizedUser){
       boolean contactMenu = true;
       while(contactMenu){
       System.out.println();
       System.out.println("=====================");
	   System.out.println("\tCONTACT LIST MENU");
       System.out.println("---------------------");
	   System.out.println("1.  Add to contact list");
	   System.out.println("2.  Delete from contact list");
	   System.out.println("3.  Browse contact list");
	   System.out.println("4.  Back to USER MENU");
       System.out.println("---------------------");
	   switch(readChoice()){
	       case 1: AddToContact(esql, authorizedUser); break;
	       case 2: DeleteFromContact(esql, authorizedUser); break;
	       case 3: ListContacts(esql, authorizedUser); break;
	       case 4: contactMenu = false; break;
	       default: System.out.println("Unrecognized choice!"); break;
	   }
	   
       }
       
    }//end contactListMenu

   /**
    * isBlankEntry is an input validation helper function
    * if user enters empty string, they are prompted
    * to provide valid input 
    */
   public static boolean isBlankEntry(String variable){
       if(variable.isEmpty()){
	   System.out.println("Must enter something");
	   return true;
       }
       return false;
      
   }//end isBlankEntry

   /**
    * isValidEntry is an input validation helper function
    * verifies if username exists
    */ 
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

   }//end isValidEntry

   /**
    * Prompts user to select user to add to their contact list
    */
   public static void AddToContact(Messenger esql, String authorizedUser){
        try{
            //ask user to give contact username
            System.out.print("Enter username to add to contact list: ");
            String contactUserId = in.readLine();
	    
	        if(isBlankEntry(contactUserId))
		    return;
        
	        if(isValidEntry(esql, contactUserId))
		    return; 
	    

            String query = String.format("SELECT contact_list FROM USR WHERE login = '%s'", authorizedUser);
            List<List<String>> contacts = esql.executeQueryAndReturnResult(query);
            String l_id = contacts.get(0).get(0);
            query = String.format("INSERT INTO USER_LIST_CONTAINS(list_id, list_member) VALUES ('%s', '%s')", l_id, contactUserId);
            esql.executeUpdate(query);
            String print = contactUserId + " is in the contact list";
            System.out.println(print);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
            return;
        }   
   }//end AddToContact

   /**
    * Members on the User's contact list are displayed
    */
   public static void ListContacts(Messenger esql, String authorizedUser){
        try{
            String query = String.format("SELECT a.list_member FROM USER_LIST_CONTAINS a, USR b WHERE b.login = '%s' AND b.contact_list = a.list_id", authorizedUser);
            List<List<String>> contacts = esql.executeQueryAndReturnResult(query);
            System.out.println();
            System.out.println("....................");
            System.out.println("Contact List Members: ");
            for(int i = 0; i < contacts.size(); i++){
                System.out.println(contacts.get(i).get(0));
            }
            return;
        }
        catch(Exception e){
            System.err.println(e.getMessage());
            return;
        }
   }//end

   /**
    * User is prompted to select user to add to their contact list
    */
   public static void DeleteFromContact(Messenger esql, String authorizedUser){
        try{
            System.out.print("Enter username to remove from contact list: ");
            String contactUserId = in.readLine();

	    if(isBlankEntry(contactUserId))
		return;
		
	    if(isValidEntry(esql, contactUserId))
		return;
		
            String query = String.format("SELECT contact_list FROM USR WHERE login = '%s'", authorizedUser);
            List<List<String>> contacts = esql.executeQueryAndReturnResult(query);
            String l_id = contacts.get(0).get(0);
            query = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id = '%s' AND list_member ='%s'", l_id, contactUserId);
            esql.executeUpdate(query);
            String print = contactUserId + " is removed from contact list";
            System.out.println(print);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
   }//end DeleteFromContact

   /**
    * User is prompted to select user to add to their blocked list
    */
   public static void AddToBlocked(Messenger esql, String authorizedUser){
        try{
            System.out.print("Enter username to add to blocked list: ");
            String blockUserId = in.readLine();

            if(isBlankEntry(blockUserId))
		return;

	    if(isValidEntry(esql, blockUserId))
		return;
		
            String query = String.format("SELECT block_list FROM USR WHERE login = '%s'", authorizedUser);
            List<List<String>> blocked = esql.executeQueryAndReturnResult(query);
            String l_id = blocked.get(0).get(0);
            query = String.format("INSERT INTO USER_LIST_CONTAINS(list_id, list_member) VALUES ('%s', '%s')", l_id, blockUserId);
            esql.executeUpdate(query);
            String print = blockUserId + " is in the block list";
            System.out.println(print);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
        
   }//end AddToBlocked

   /**
    * User is prompted to select a user to remove from their blocked list
    */
   public static void DeleteFromBlocked(Messenger esql, String authorizedUser){
        try{
            System.out.print("Enter username to remove from blocked list: ");
            String blockUserId = in.readLine();

            if(isBlankEntry(blockUserId))
		return;

            if(isValidEntry(esql, blockUserId))
		return;

            String query = String.format("SELECT block_list FROM USR WHERE login = '%s'", authorizedUser);
            List<List<String>> blocked = esql.executeQueryAndReturnResult(query);
            String l_id = blocked.get(0).get(0);
            query = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id = '%s' AND list_member ='%s'", l_id, blockUserId);
            esql.executeUpdate(query);
            String print = blockUserId + " is removed from block list";
            System.out.println(print);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
   }//end DeleteFromBlocked

   /**
    * Members on the User's blocked list are displayed
    */
   public static void ListBlocked(Messenger esql, String authorizedUser){
     try{
            String query = String.format("SELECT a.list_member FROM USER_LIST_CONTAINS a, USR b WHERE b.login = '%s' AND b.block_list = a.list_id", authorizedUser);
            List<List<String>> blocked = esql.executeQueryAndReturnResult(query);
            System.out.println();
            System.out.println("....................");
            System.out.println("Blocked List Members: ");
            for(int i = 0; i < blocked.size(); i++){
                System.out.println(blocked.get(i).get(0));
            }
            return;
        }
        catch(Exception e){
            System.err.println(e.getMessage());
            return;
        }
   }//end ListBlocked

   /**
    * Chat Menu provides users the option to
    * create a new chat,
    * go to chat viewer menu where they are given more options
    * or go back to USER MENU
    */
   public static void chatMenu(Messenger esql, String authorizedUser){
        boolean chatmenu = true;
        while(chatmenu){
            System.out.println();
            System.out.println("=====================");
            System.out.println("\tCHAT MENU");
	        System.out.println("---------------------");
            System.out.println("1.  Create a chat");
            System.out.println("2.  Chat viewer");
            System.out.println("3.  Back to USER MENU");
            System.out.println("---------------------");
            switch (readChoice()){
                case 1: createChat(esql, authorizedUser); break;
                case 2: chatViewer(esql, authorizedUser); break;
                case 3: chatmenu = false; break;
                default : System.out.println("Unrecognized choice!"); break;
            }
        }
        return;
   }//end chatMenu

   /**
    * helper function verifies if author is initial sender
    * called in addChatMember and removeChatMember
    * Only initial sender has authority to add or remove chat members
    */
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
   }//end isValidSender

   /**
    * Prompts user to enter usernames of users they want to start chat with
    * names are entered on separate lines
    * 
    */
   public static void createChat(Messenger esql, String authorizedUser){
        try{
            //get names of people who you want to chat with 
            System.out.println("Enter usernames on separate lines to include in chat");
            System.out.println("End with a q");
            String type;
            List<String> chatUsers = new ArrayList<String>();
            String input = in.readLine();
            while (!input.equals("q")){
                chatUsers.add(input);
                input = in.readLine();
            }
            //decide on what kind of chat it is
            chatUsers.remove("q");
            if(chatUsers.size() == 0 ){
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

            int chatId = esql.getCurrSeqVal("chat_chat_id_seq");

            //adding authorized user to chat
            query = String.format("INSERT INTO CHAT_LIST(chat_id, member) VALUES('%d', '%s')", chatId, authorizedUser);
            esql.executeUpdate(query);
            
            //adding everyone else into the chat
            for(int i = 0; i < chatUsers.size(); i++){
                query = String.format("INSERT INTO CHAT_LIST(chat_id, member) VALUES('%d', '%s')", chatId, chatUsers.get(i));
                esql.executeUpdate(query);
            }

            String print = "New chat has been created with " + authorizedUser + " ";
            for(int i = 0; i < chatUsers.size(); i++){
                print += chatUsers.get(i) + " " ;
            }
            System.out.println(print);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
   }//end createChat
 
   /**
    * User is prompted to chose a chat to browse by entering chat id
    * chat id identifies each chat with serial number sequence
    * chat id and chat members are listed
    * Once a chat is selected, user is brought to chat viewer menu
    */ 
   public static void chatViewer(Messenger esql, String authorizedUser){
        try{
            //get the chatid associated with the authorizedUser
            String query = String.format("SELECT chat_id FROM CHAT_LIST WHERE member = '%s'", authorizedUser);
            List<List<String>> chats = esql.executeQueryAndReturnResult(query);
            
            if(chats.size() == 0){
                System.out.println("No chats");
                return;
            }
            //print out the memebers in the associated chat
            for(int i = 0 ; i < chats.size(); i++){
                query = String.format("SELECT member FROM CHAT_LIST where chat_id = '%s'", chats.get(i).get(0));
                List<List<String>> members = esql.executeQueryAndReturnResult(query);
                String print = i + ": ";
                System.out.print(print);
                for(int j = 0; j < members.size(); j++){
                    System.out.print(members.get(j).get(0).trim());
                    System.out.print(" ");
                }
                System.out.println();
            }
            System.out.println("Enter chat number to browse: ");
            String input = in.readLine();
            int inpt = Integer.parseInt(input);
            String chatNum = chats.get(inpt).get(0);
            chatViewerMenu(esql, authorizedUser, chatNum);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
        
   }//end chatViewer
   
   /**
    * Chat Viewer Menu first prompts user to select a chat id
    */
   public static void chatViewerMenu(Messenger esql, String authorizedUser, String chatNum){
        boolean chatmenu = true;
        while(chatmenu){
            System.out.println();
            System.out.println("=====================");
            System.out.println("\tCHAT VIEWER MENU");
            System.out.println("---------------------");
            System.out.println("1.  Display Message");
            System.out.println("2.  Add Message");
            System.out.println("3.  Edit Message");
            System.out.println("4.  Delete Message");
            System.out.println("5.  Add user to chat");
            System.out.println("6.  Remove user from chat");
            System.out.println("7.  Delete chat");
            System.out.println("8.  Back to CHAT MENU");
            System.out.println("---------------------");
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
   }//end chatViewerMenu
 
   /**
    * Lists all messages in chat
    * Displays message id, initial sender, time, and message itself
    */
   public static void displayMessage(Messenger esql, String authorizeduser, String chatId){
        try{
            String query = String.format("SELECT * FROM MESSAGE WHERE chat_id = '%s' ORDER BY msg_timestamp", chatId);
            List<List<String>> messages = esql.executeQueryAndReturnResult(query);
            int numMessages = messages.size();
            if(numMessages == 0){
                System.out.println("....................");
                System.out.println("No messages");
                System.out.println("....................");
                return;
            }

           int end;
            int j = 0;
            int i = numMessages -1 - j;
            if(numMessages < 10){
                end = 0;
            }
            else{
                end = numMessages - 10 - j;
            }
            
            System.out.println("....................");
            System.out.println("\tMessages");
            for(; i >= end && end >= 0 ; i--){
                query = String.format("SELECT * FROM MESSAGE where msg_id = '%s'", messages.get(i).get(0));
                List<List<String>> one = esql.executeQueryAndReturnResult(query);
                
                System.out.println("....................");
                String print = "Message Id: ";
                print += one.get(0).get(0);
                System.out.println(print);

                String print1 = "Sender: ";
                print1 += one.get(0).get(3);
                System.out.println(print1);

                String print2 = "Time: ";
                print2 += one.get(0).get(2);
                System.out.println(print2);

                String print3 = "Message: ";
                print3 += one.get(0).get(1).trim();
                System.out.println(print3);
                System.out.println("....................");
                
                if(i == end && numMessages > 10){
                    if(i == 0)
                        break;
                    System.out.println("More messages?");
                    String response = in.readLine();

                    if(response.equals("y")){
                        j++;
                        end = numMessages -10 - j;
                    }
                    else{
                        break;
                    }
                }
            }

        }
        catch(Exception e){
            System.err.println(e.getMessage());
        } 
   } //endDisplayMessage
    
   /**
    * User is prompted to select member to add to current chat
    */ 
   public static void addChatMember(Messenger esql, String authorizedUser, String chatId){

        try{
            System.out.print("Enter username to add to chat: ");
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
   }//end addChatMember

   /**
    * User is prompted to select member to remove from current chat
    */ 
   public static void removeChatMember(Messenger esql, String authorizedUser, String chatId){
           try{
             System.out.print("Enter user name to remove from chat: ");
            String chatName = in.readLine();
             if(isBlankEntry(chatName))
	        	return;

            if(isValidSender(esql, authorizedUser, chatId))
	        	return;

            if(isValidEntry(esql, chatName))
	        	return;
            String query = String.format("DELETE FROM CHAT_LIST WHERE chat_id = '%s' AND member = '%s'", chatId, chatName);
            esql.executeUpdate(query);
            String print = chatName + " is removed from the chat";
            System.out.println(print);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
 }//end removeChatMember

   /**
    * Allows user to delete current chat if they are authorized to do so
    */ 
   public static void deleteChat(Messenger esql, String authorizedUser, String chatId){
        try{   
             //checking if it is initial sender
            if(isValidSender(esql, authorizedUser, chatId))
		        return;
            
            String query = String.format("DELETE FROM MESSAGE WHERE chat_id = '%s'", chatId);
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
            
   }//end deleteChat

   /**
    * User is prompted to add a message
    */
   public static void addMessage(Messenger esql, String authorizedUser, String chatId){

        try{
            System.out.print("Enter a message: ");
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
    }//end addMessage
   
   /**
    * Allows user to delete message by entering a valid message id
    */
   public static void deleteMessage(Messenger esql, String authorizedUser, String chatId){

        try{
            System.out.print("Enter message Id: ");
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
    }//end deleteMessage
   
   /**
    * Allows user to edite message by entering a valid message id
    */
   public static void editMessage(Messenger esql, String authorizedUser, String chatId){

        try{
            System.out.print("Enter message Id: ");
            String messageId = in.readLine();
            
            if(isBlankEntry(messageId))
		return;
            System.out.print("Enter new message: ");
            String message = in.readLine();
            if(message == ""){
                System.out.print("Enter new message: ");
                return;
            }

            String query = String.format("UPDATE MESSAGE SET msg_text = '%s' WHERE msg_id = '%s'", message, messageId);
            esql.executeUpdate(query);
            System.out.println("Message is edited");
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
    }//emd editMessage
   
   /**
    * Allows user delete their own account only if there is no linked
    * information for their account
    */
   public static boolean DeleteOwnAccount(Messenger esql, String authorizedUser){
        try{
            String query = String.format("SELECT chat_id FROM CHAT_LIST WHERE member = '%s'", authorizedUser);
            List<List<String>> chats = esql.executeQueryAndReturnResult(query);
            if(chats.size() == 0){
	            query = String.format("DELETE FROM Usr WHERE login = '%s'", authorizedUser);
                esql.executeUpdate(query);
                System.out.println ("User successfully deleted!");
                return false;
            }
            else{
                System.out.println("Information linked to this account, cannot delete account");
                return true;
            }
        }catch(Exception e){
            System.err.println(e.getMessage());
            return true;
        }
   }




}//end Messenger
