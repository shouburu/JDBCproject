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
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Arrays;
import java.io.Console;
import java.text.*;
import java.io.*;
import java.util.*;
import java.text.ParseException;


/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Messenger {
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));
    
      public static String uname;

   public Messenger (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

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
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

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

   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();
	
	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup


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
              uname = authorisedUser;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Add to contact list");
                System.out.println("2. Browse contact list");
                System.out.println("3. Write a new message");
                System.out.println("4. Read notification list");
                System.out.println("5. Update Status");
                System.out.println("6. Delete Contact");
                System.out.println(".........................");
                System.out.println("9. Log out");
                switch (readChoice()){
                   case 1: AddToContact(esql); break;
                   case 2: ListContacts(esql); break;
                   case 3: NewMessage(esql); break;
                   case 4: ReadNotifications(esql); break;
                   case 5: UpdateStatus(esql); break;
                   case 6: DeleteContact(esql); break;
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

   public static void AddToContact(Messenger esql){
     try{
          //Grab the username of the person to be adeed, then get the block_list and contact_list from current user
          System.out.print("\tEnter their username: ");
            String contactname = in.readLine();
            String query1 = String.format("SELECT contact_list, block_list FROM Usr WHERE login = '%s'", uname);
            List<List<String>> clist = esql.executeQueryAndReturnResult(query1);
            String usercontactlist = clist.get(0).get(0);
            String userblocklist = clist.get(0).get(1);
          
          //If the username to be added is in the block list, remove it
          String query = String.format("SELECT * FROM USER_LIST_CONTAINS ULC WHERE %s = ULC.list_id AND ULC.list_member='%s'", userblocklist, contactname );
          int userNum = esql.executeQuery(query);
          if( userNum > 0 ) {
            String query3 = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id = '%s' AND list_member = '%s'", userblocklist,  contactname);
            esql.executeUpdate(query3);
          }
          //Update Contacts
          String query2 = String.format("INSERT INTO USER_LIST_CONTAINS (list_id, list_member ) VALUES (%s,'%s')", usercontactlist, contactname);
            esql.executeUpdate(query2);
        return;
    }catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }

   }//end

   public static void ListContacts(Messenger esql){
     try{
          //Grab contact_list from current user, then grab the contact list and status of the users in that list
          String query1 = String.format("SELECT contact_list FROM Usr WHERE login = '%s'", uname);
            List<List<String>> clist = esql.executeQueryAndReturnResult(query1);
            String query2 = String.format("SELECT L.list_member, U.status FROM USER_LIST_CONTAINS L, USR U WHERE L.list_id = %s AND L.list_member = U.login", clist.get(0).get(0) );
            List<List<String>> clist2 = esql.executeQueryAndReturnResult(query2);
          
          //Print out all the contacts
          for( int i = 0; i < clist2.size(); i++ ) {
            for( int j = 0; j < clist2.get(i).size(); j++ ) {
              System.out.print( clist2.get(i).get(j) + "\t");
            }
            System.out.println();
          }
          return;
    } catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
   }
   
    public static void ListBlockedContacts(Messenger esql){
     try{
          
          //Grab block_list from current user, then grab the block list
          String query1 = String.format("SELECT block_list FROM Usr WHERE login = '%s'", uname);
          List<List<String>> clist = esql.executeQueryAndReturnResult(query1);
          String query2 = String.format("SELECT L.list_member FROM USER_LIST_CONTAINS L WHERE L.list_id = %s", clist.get(0).get(0) );
          List<List<String>> clist2 = esql.executeQueryAndReturnResult(query2);
          
          //Print out list of blocked users
          for( int i = 0; i < clist2.size(); i++ ) {
            for( int j = 0; j < clist2.get(i).size(); j++ ) {
              System.out.print("\t"+ clist2.get(i).get(j));
            }
            System.out.println();
          }
          System.out.println();
          return;
          
    }catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
   }//end
   
    public static void UpdateStatus(Messenger esql){
      try{
          //Grab status and update current status
          System.out.println("\tEnter your status(140 characters): ");
          String status = in.readLine();
          String query = String.format("UPDATE USR SET status='%s' WHERE login = '%s'", status, uname);
          esql.executeUpdate(query);
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
   }//end 
   
    public static void DeleteContact(Messenger esql){
      try{
          //Grabs contact name to be deleted, and deletes from the USER_LIST_CONTAINS table where currentuser.contact_list=UserListContaints.list_id
          System.out.println("\tEnter contact name to be deleted: ");
          String delname = in.readLine();
          String query1 = String.format("SELECT contact_list FROM Usr WHERE login = '%s'", uname);
          List<List<String>> clist = esql.executeQueryAndReturnResult(query1);
          String query = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id = '%s' AND list_member = '%s'", clist.get(0).get(0), delname);

          esql.executeUpdate(query);
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
   }//end 

   public static void NewMessage(Messenger esql){
      // Your code goes here.
      // ...
      // ...

      // MESSAGE SETUP:::::::::::::::::::::::::::;;
      try{
          // OVERHEAD::::::::::::::::::::::::::
          //1) Serialize message
          int s = Serializer(esql, "MESSAGE", "msg_id");
          //2) Who to send it to?
          System.out.print("Recipient Name? \n");
          String recipient = in.readLine();
          //System.out.println(recipient);
            //Update the notification for that user or call function that sends message (which will then update notification)
          //3) Chat to add message to

          // OVERHEAD::::::::::::::::::::::::::
      
          //Prompt for message input
          System.out.println("Say whats on your mind " + uname + ".\n");
          System.out.println("(300 char limit) \n");

          Console console = System.console();
          String user_input = console.readLine("<(^^)>");

          //System.out.println(user_input);

          //set timestamp to current time 
          Date date = new Date();
          SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
          String timestamp = df.format(date);
          //System.out.println(timestamp); // 12/01/2011 4:48:16 PM

          Date dtime = null;
          //ask about destruct timestamp from user
          System.out.print("Would you like to setup a self destruct for this message? (y/n) ");
          String ans = in.readLine();
          //System.out.println(ans);

          if(ans.equals("y") || ans.equals("yes") || ans.equals("Y")){
              System.out.print("When should your message be deleted? (MM/dd/yyyy h:mm:ss a (\"a\" stands for am or pm)): ");
              String temp = in.readLine();
              System.out.println(temp);
              try{
                  dtime = df.parse(temp);
              } catch(ParseException e) {
                System.out.println("Please use the proper format for the destruction date. Try Again!");
              }
          }

          String dtimestamp = df.format(dtime); 
          String query;

          System.out.println("dtime " + dtimestamp);
          System.out.println("timestamp " + timestamp);

          if(dtimestamp != null){
              //Add message to database under msg_text (later will update with specific user information as well)
              String beta_query = "INSERT INTO MESSAGE (serial, msg_text, sender_login, msg_timestamp, destr_timestamp) VALUES ('%s', '%s', '" + timestamp + "' '" + dtimestamp + "')";
              query = String.format(beta_query, s, user_input, uname);
          }
          else{
              String beta_query = "INSERT INTO MESSAGE (serial, msg_text, sender_login, msg_timestamp) VALUES ('%s', '%s', '" + timestamp + "')";
              query = String.format(beta_query, s, user_input, uname);
          }
          esql.executeUpdate(query);
          System.out.println("Message sent.");

          //TEST WHETHER MESSAGE PROPERLY SENT
          /*
          String test_query = String.format("SELECT M.msg_text, M.msg_timestamp FROM MESSAGE M, USR U WHERE U.login = %s", uname);
          esql.executeQueryAndPrintResult(test_query);
          */

      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
      // MESSAGE SETUP:::::::::::::::::::::::::::;;
      
   }//end 
   
   public static void BlockContact(Messenger esql){
     try{
          //Grab the username of the person to be blocked, then get the block_list and contact_list from current user
          System.out.print("\tEnter their username: ");
            String contactname = in.readLine();
            String query1 = String.format("SELECT block_list, contact_list FROM Usr WHERE login = '%s'", uname);
            List<List<String>> clist = esql.executeQueryAndReturnResult(query1);
              String userblocklist = clist.get(0).get(0);
              String usercontactlist = clist.get(0).get(1);
          
        //If the username to be blocked is in the conact, remove it
        String query = String.format("SELECT * FROM USER_LIST_CONTAINS ULC WHERE %s = ULC.list_id AND ULC.list_member='%s'", usercontactlist, contactname );
        int userNum = esql.executeQuery(query);
        if( userNum > 0 ) {
          String query3 = String.format("DELETE FROM USER_LIST_CONTAINS WHERE list_id = '%s' AND list_member = '%s'", usercontactlist,  contactname);
          esql.executeUpdate(query3);
        }
          //Update the blocked list
          String query4 = String.format("INSERT INTO USER_LIST_CONTAINS (list_id, list_member ) VALUES (%s,'%s')", userblocklist, contactname);
            esql.executeUpdate(query4);
          return;
          
    }catch(Exception e){
         System.err.println (e.getMessage ());
         return;
      }
    }



   public static void EditMessage(Messenger esql, int message_id){
     //Allow edit of message by pulling message by id
   }//end

   public static int Serializer(Messenger esql, String table_name, String attr){
       try{
            String query = "SELECT MAX(TN."+ attr +") FROM " + table_name + " TN";
            List<List<String>> clist = esql.executeQueryAndReturnResult(query);
            int num = Integer.parseInt(clist.get(0).get(0));
            int serial = num + 1;
            return serial;
       } catch(Exception e){
            System.err.println (e.getMessage ());
            return -1;
       }
   }

   public static void DeleteMessage(Messenger esql){
   
   }

   public static void ReadNotifications(Messenger esql){
      // Your code goes here.
      // ...
      // ...
   }//end

   public static void Query6(Messenger esql){
      // Your code goes here.
      // ...
      // ...
   }//end Query6

}//end Messenger

