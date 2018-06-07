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
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Scanner; //for reading single char for input for the status update for bookflight

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
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
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
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
	}
	
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
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
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
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count number of results.
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
		if (rs.next()) return rs.getInt(1);
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
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Plane");
				System.out.println("2. Add Pilot");
				System.out.println("3. Add Flight");
				System.out.println("4. Add Technician");
				System.out.println("5. Book Flight");
				System.out.println("6. List number of available seats for a given flight.");
				System.out.println("7. List total number of repairs per plane in descending order");
				System.out.println("8. List total number of repairs per year in ascending order");
				System.out.println("9. Find total number of passengers with a given status");
				System.out.println("10. < EXIT");
				
				switch (readChoice()){
					case 1: AddPlane(esql); break;
					case 2: AddPilot(esql); break;
					case 3: AddFlight(esql); break;
					case 4: AddTechnician(esql); break;
					case 5: BookFlight(esql); break;
					case 6: ListNumberOfAvailableSeats(esql); break;
					case 7: ListsTotalNumberOfRepairsPerPlane(esql); break;
					case 8: ListTotalNumberOfRepairsPerYear(esql); break;
					case 9: FindPassengersCountWithStatus(esql); break;
					case 10: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

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

	public static void AddPlane(DBproject esql) {//1
        try{
		    System.out.print("\tPlease Enter Aircraft Maker: ");
		    String maker = in.readLine();
		    System.out.print("\tPlease Enter Aircraft Model: ");
		    String model = in.readLine();
		    System.out.print("\tPlease Enter Aircraft Age: ");
		    String age = in.readLine();
		    System.out.print("\tPlease Enter Aircraft Seats: ");
		    String seats = in.readLine();
		    System.out.print("\n");

		    String find_max="SELECT MAX(P.id) from Plane P;";
		    int test=0;
		    List<List<String>> max_count=esql.executeQueryAndReturnResult(find_max);
		    String Info="SELECT * FROM Plane P Where P.id= ";
		    for(List<String> row : max_count)
		    {
		     for(String s: row)
		     {
		       //System.out.println(s + "\t");
		       test = Integer.parseInt(s);
			
		     }
		     //System.out.println("\n");
		    }
		    if(test==0){
		    test++;
		    Info += "\'"+test+"\';";
		    String update_plane="INSERT INTO Plane (id, make, model, age, seats) VALUES "
		    +" ("+test+", \'"+maker+"\',\'"+model+"\',\'"+age+"\',\'"+seats+"\');";
		    System.out.print("\tPlane Information entered: "+"\n");
		    esql.executeUpdate(update_plane);
		    esql.executeQueryAndPrintResult(Info);	
		    }
		    else{
		    test++;
		    Info += "\'"+test+"\';";
		    String update_plane="INSERT INTO Plane (id, make, model, age, seats) VALUES "
		    +" ("+test+", \'"+maker+"\',\'"+model+"\',\'"+age+"\',\'"+seats+"\');";
		    System.out.print("\tPlane Information entered: "+"\n");	
		    esql.executeUpdate(update_plane);
		    esql.executeQueryAndPrintResult(Info);	 
		    }
        }
        catch(Exception e){
    		System.err.println (e.getMessage());
        }	

	}

	public static void AddPilot(DBproject esql) {//2

        try{

            // Get the maximum id in the pilot table.
            String find_max = "SELECT MAX(P.ID) AS max_value FROM Pilot P;";
            List<List<String>> rs = esql.executeQueryAndReturnResult(find_max);

            int rowCount = rs.size();
            int maxRecord = 0;
            int i = 0;
            int j = 0;
            int numCol = rs.get(0).size();

            // Get the max value for the return query
            for(List<String> row : rs){
                for(String s: row){
                    maxRecord = Integer.parseInt(s);
                }
            }

            // Get user input for the pilot full name and nationality
            String fname;
            String nationality;
            System.out.println("\nEnter the pilots full name:\t");
            fname = in.readLine();
            System.out.println("\nEnter the pilots nationality:\t");
            nationality = in.readLine();

            
            // Insert into database. If there are no records in table insert new pilot with 
            // id = 0. Otherwise just increment the max id returned by max id query 
            // by 1 and insert pilot table.
            String insert_pilot = "INSERT INTO Pilot(id, fullname, nationality) "; 
            String print_record = "SELECT * FROM Pilot P WHERE P.id = ";
            if(rowCount == 0){
                insert_pilot += "VALUES(0,\'" + fname + "\',\'" + nationality + "\');";
                print_record += maxRecord + ";";
                esql.executeUpdate(insert_pilot);
                System.out.println("Added pilot " + fname + " from " + nationality + " with id: " + maxRecord + ".\n");
                int newRec = esql.executeQueryAndPrintResult(print_record);
            }
            else{
                maxRecord++;
                insert_pilot += "VALUES(" + maxRecord + ",\'" + fname + "\',\'" + nationality + "\');";
                print_record += maxRecord + ";";
                esql.executeUpdate(insert_pilot);
                System.out.println("Added pilot " + fname + " from " + nationality + " with id: " + maxRecord + ".\n");
                int newRec = esql.executeQueryAndPrintResult(print_record);

            }
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
	}

	public static void AddFlight(DBproject esql) {//3
		// Given a pilot, plane and flight, adds a flight in the DB
	    try{

            int pilotID;
            int planeID;
            System.out.print("\nPlease provide a pilot ID and plane ID to add a flight.");
            System.out.print("\nEnter pilot id: ");
            pilotID = Integer.parseInt(in.readLine());
            System.out.print("\nEnter plane id: ");
            planeID = Integer.parseInt(in.readLine());;

            // Get the max value of fiid primary key.
            String find_max_fiid = "SELECT MAX(F.fiid) AS max_value FROM FlightInfo F;";
            List<List<String>> flightInfo_rs = esql.executeQueryAndReturnResult(find_max_fiid);
            int fiid = 0;

            // If FlightInfo table is not empty create a new id with the value of the max id plus one.
            if(flightInfo_rs.size() > 0){
                for(List<String> row : flightInfo_rs){
                    for(String s: row){
                        fiid = Integer.parseInt(s);
                    }
                }
                fiid++; // Use next value in sequence
            }

            // Used to check if the plane id or pilot id exist.
            String plane_query = "SELECT * FROM Plane WHERE id = " + planeID;
            String pilot_query = "SELECT * FROM Pilot WHERE id = " + pilotID;
            List<List<String>> plane_rs = esql.executeQueryAndReturnResult(plane_query);
            List<List<String>> pilot_rs = esql.executeQueryAndReturnResult(pilot_query);

            // Used to add a record with fiid, fligh_id, pilot_id, and plane_id into FlightInfo table
            String insert_flightInfo_query = "INSERT INTO FlightInfo(fiid, flight_id, pilot_id, plane_id) VALUES(";

            // If both the pilot id and plane id exists, create a new flight record in the flight table, 
            // then create a new record in the FlightInfo table. Otherwise, ask user if they want to create
            // a new plane and a new pilot, then create new flight record and finally insert a new record into FlightInfo.
          
            if(plane_rs.size() > 0 && pilot_rs.size() > 0){
                int flightID = InsertFlight(esql);
                System.out.print("\nFlight number created: " + flightID + "\n");
                insert_flightInfo_query += fiid + ", "  + flightID + ", " + pilotID + ", " + planeID + ");";
                esql.executeUpdate(insert_flightInfo_query);
                // Print inserted record
                String fiid_query = "SELECT * FROM FlightInfo F WHERE F.fiid = " + fiid + ";";
                int rowNum = esql.executeQueryAndPrintResult(fiid_query);                
            }
            else{
                System.out.print("\nInvalid pilot id and plane id combination.\n");

                String max_plane = "SELECT MAX(P.id) AS max_value FROM Plane P;";
                String max_pilot = "SELECT MAX(P.id) AS max_value FROM Pilot P;";

                // Find if either plane or pilot does not exist, ask user if they 
                // want to add it.
                if(plane_rs.size() == 0 && pilot_rs.size() > 0){
                    System.out.print("\nPilot ID exists, but plane ID does not exist." 
                                      + "\nDo you want to add a new plane?\n");
                    Scanner sc = new Scanner(System.in);
                    // String input = sc.nextLine();
                    char option = sc.next().charAt(0);

                    if(option == 'Y' || option == 'y'){
                        AddPlane(esql);
                        // Get the plane ID we just added
                        List<List<String>> max_plane_rs = esql.executeQueryAndReturnResult(max_plane);
                        for(List<String> row : max_plane_rs){
                             for(String s: row){
                                 planeID = Integer.parseInt(s);
                             }
                         }

                         // Get the flight details and insert new records in Flight and FlightInfo tables
                         int flightID = InsertFlight(esql);

                         System.out.print("\nFlight number created: " + flightID + "\n");
                         insert_flightInfo_query += fiid + ", "  + flightID + ", " + pilotID + ", " + planeID + ");";
                         esql.executeUpdate(insert_flightInfo_query);

                         // Print inserted record
                         String fiid_query = "SELECT * FROM FlightInfo F WHERE F.fiid = " + fiid + ";";
                         int rowNum = esql.executeQueryAndPrintResult(fiid_query);                                                               
                    }

                    else{
                        System.out.print("\nReturning to main menu.\n");
                    }            
                }

                else if(pilot_rs.size()==0 && plane_rs.size() > 0){
                    System.out.print("\nPlane ID exists, but pilot ID does not exist." 
                                      + "\nDo you want to add a new pilot?\n");
                    Scanner sc = new Scanner(System.in);
                    // String input = sc.nextLine();
                    char option = sc.next().charAt(0);

                    if(option == 'Y' || option == 'y'){
                        AddPilot(esql);
                        // Get the pilot ID we just added
                        List<List<String>> max_pilot_rs = esql.executeQueryAndReturnResult(max_pilot);
                        for(List<String> row : max_pilot_rs){
                             for(String s: row){
                                 pilotID = Integer.parseInt(s);
                             }
                         }

                         // Get the flight details and insert new records in Flight and FlightInfo tables
                         int flightID = InsertFlight(esql);

                         System.out.print("\nFlight number created: " + flightID + "\n");
                         insert_flightInfo_query += fiid + ", "  + flightID + ", " + pilotID + ", " + planeID + ");";
                         esql.executeUpdate(insert_flightInfo_query);

                         // Print inserted record
                         String fiid_query = "SELECT * FROM FlightInfo F WHERE F.fiid = " + fiid + ";";
                         int rowNum = esql.executeQueryAndPrintResult(fiid_query);   
                    }
                    
                    else{
                        System.out.print("\nReturning to main menu.\n");
                    }            
                }

                else{
                    System.out.print("\nNeither the plane ID nor the pilot ID exist." 
                                      + "\nDo you want to add a new plane and a new pilot (y/n)?\n");
                    Scanner sc = new Scanner(System.in);
                    // String input = sc.nextLine();
                    char option = sc.next().charAt(0);



                    if(option == 'Y' || option == 'y'){
                        // Add the plane and get the plane ID we just added
                        AddPlane(esql);
                        List<List<String>> max_plane_rs = esql.executeQueryAndReturnResult(max_plane);
                        for(List<String> row : max_plane_rs){
                             for(String s: row){
                                 planeID = Integer.parseInt(s);
                             }
                         }

                        // Add the pilot and get the pilot ID we just added
                        AddPilot(esql);
                        List<List<String>> max_pilot_rs = esql.executeQueryAndReturnResult(max_pilot);
                        for(List<String> row : max_pilot_rs){
                             for(String s: row){
                                 pilotID = Integer.parseInt(s);
                             }
                         }

                         // Get the flight details and insert new records in Flight and FlightInfo tables
                         int flightID = InsertFlight(esql);

                         System.out.print("\nFlight number created: " + flightID + "\n");
                         insert_flightInfo_query += fiid + ", "  + flightID + ", " + pilotID + ", " + planeID + ");";
                         esql.executeUpdate(insert_flightInfo_query);

                         // Print inserted record
                         String fiid_query = "SELECT * FROM FlightInfo F WHERE F.fiid = " + fiid + ";";
                         int rowNum = esql.executeQueryAndPrintResult(fiid_query);   
                    }
                    
                    else{
                        System.out.print("\nReturning to main menu.\n");
                    }            
                }
            }
	    }
        catch(Exception e){
		    System.err.println (e.getMessage());
	       }

	}

	public static void AddTechnician(DBproject esql) {//4        

        try{
            // Get the maximum id in the technician table.
            String find_max = "SELECT MAX(T.ID) AS max_value FROM Technician T;";
            List<List<String>> rs = esql.executeQueryAndReturnResult(find_max);

            int rowCount = rs.size();
            int maxRecord = 0;
            int i = 0;
            int j = 0;
            int numCol = rs.get(0).size();

            // Get the max value for the return query
            for(List<String> row : rs){
                for(String s: row){
                    maxRecord = Integer.parseInt(s);
                }
            }

            // Get user input for the technician full name
            String fname;
            System.out.println("\nEnter the technician's full name:\t");
            fname = in.readLine();
            
            // Insert into database. If there are no records in table insert new technician with 
            // id = 0. Otherwise just increment the max id returned by query 
            // by 1 and insert technician.
            String insert_technician = "INSERT INTO Technician(id, full_name) "; 
            String print_record = "SELECT * FROM Technician T WHERE T.id = ";
            if(rowCount == 0){
                insert_technician += "VALUES(0,\'" + fname + "\');";
                print_record += rowCount + ";";
                esql.executeUpdate(insert_technician);
                System.out.println("Added technician " + fname + " with id: " + maxRecord + "\n");
                int newRec = esql.executeQueryAndPrintResult(print_record);            
            }
            else{
                maxRecord++;
                insert_technician += " VALUES("+ maxRecord + ", \'" + fname + "\');";          
                print_record += maxRecord + ";";
                esql.executeUpdate(insert_technician);
                System.out.println("\nAdded technician " + fname + " with id: " + maxRecord + ".\n");
                int newRec = esql.executeQueryAndPrintResult(print_record);
            }
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }

	}

	public static void BookFlight(DBproject esql) 
{//5
		// Given a customer and a flight that he/she wants to book, add a reservation to the DB
	try{ 
			System.out.print("\tPlease Enter Customer Information: \n");	
			System.out.print("\tPlease Enter Passenger ID: ");
			String input1 = in.readLine();
			System.out.print("\tPlease Enter Flight Number: ");
			String input2 = in.readLine();
			String find_res="SELECT R.rnum from Reservation R, Customer C, Flight F WHERE C.id=R.cid AND F.fnum=R.fid AND C.id=\'"+input1+"\' AND F.fnum=\'"+input2+"\';";
			String find_stat="SELECT R.status from Reservation R, Customer C, Flight F WHERE C.id=R.cid AND F.fnum=R.fid AND C.id=\'"+input1+"\' AND F.fnum=\'"+input2+"\';";
			int test=0;
			List<List<String>> find_rnum=esql.executeQueryAndReturnResult(find_res);

			if(find_rnum.size()==0)
			{

				String find_flight="SELECT F.fnum FROM Flight F WHERE fnum=\'"+input2+"\';";
				List<List<String>>find_fnum=esql.executeQueryAndReturnResult(find_flight);
				if(find_fnum.size()==0)
				{
					System.out.print("\tFlight Does Not Exist Please Enter Info: \n");
					AddFlight(esql);

				}

				
				String find_pass="SELECT C.id FROM Customer C WHERE C.id=\'"+input1+"\';";
				//checking to see if the passenger exists if not then add the passenger to the passenger table
				List<List<String>> find_passId=esql.executeQueryAndReturnResult(find_pass);
				if(find_passId.size()==0)
				{
					System.out.print("\tCustomer Does Not Exist Please Enter Info: \n");
					String fname,lname,gtype,dob,address,phone,zipcode;
					System.out.print("\tCustomer First Name: ");
					fname=in.readLine();
					System.out.print("\tCustomer Last Name: ");
					lname=in.readLine();
					System.out.print("\tCustomer Gender: ");
					gtype=in.readLine();
					System.out.print("\tCustomer DOB: ");
					dob=in.readLine();
					System.out.print("\tCustomer Address: ");
					address=in.readLine();
					System.out.print("\tCustomer Phone Number: ");
					phone=in.readLine();
					System.out.print("\tCustomer Zip: ");
					zipcode=in.readLine();
					
					String insert_cus="INSERT INTO Customer (id, fname, lname, gtype, dob, address, phone, zipcode)"  
							+" VALUES(\'"+input1+"\',\'"+fname+"\', \'"+lname+"\',\'"+gtype+"\',\'"+dob+"\', \'"+address+"\', \'"+phone+"\', \'"+zipcode+"\');"; 
					esql.executeUpdate(insert_cus);
					String find_max="SELECT Max(R.rnum) from Reservation R;";
					List<List<String>> find_rnumMax=esql.executeQueryAndReturnResult(find_max);
					for(List<String> row : find_rnumMax)
					{
					 for(String s: row)
					 {
					   test = Integer.parseInt(s);
					 }
					}
					test++;			
					System.out.print("\tPlease Enter New Status of W,R,C: ");
					Scanner sc = new Scanner(System.in);
					char Val = sc.next().charAt(0);
					String insert_res="INSERT INTO Reservation (rnum, cid, fid, status) VALUES("+test+",\'"+input1+"\',\'"+input2+"\', \'"+Val+"\');";  
					String query="SELECT R.status FROM Reservation R, Customer C, Flight F WHERE C.id=R.cid AND F.fnum=R.fid AND C.id=\'"+input1+"\' AND F.fnum=\'"+input2+"\';";
					esql.executeUpdate(insert_res);
					System.out.print("\tStatus has been updated to: \n ");
					esql.executeQueryAndPrintResult(query);
				}							
			}
			else
			{	
				List<List<String>> current_status=esql.executeQueryAndReturnResult(find_stat);
				System.out.print("\tCurrent status: "+current_status+"\n");
				System.out.print("\tPlease Enter New Status of W,R,C: ");
				Scanner sc = new Scanner(System.in);
				char Val = sc.next().charAt(0);
				String update_res="UPDATE Reservation SET status= \'"+Val+"\' WHERE cid= \'"+input1+"\' AND fid= \'"+input2+"\'";
				String query="SELECT R.status FROM Reservation R, Customer C, Flight F WHERE C.id=R.cid AND F.fnum=R.fid AND C.id=\'"+input1+"\' AND F.fnum=\'"+input2+"\';";
				esql.executeUpdate(update_res);
				System.out.print("\tStatus has been updated to: \n ");
				esql.executeQueryAndPrintResult(query);
			}
	}	
	catch(Exception e)
	{
		System.err.println (e.getMessage());
	}

}

	public static void ListNumberOfAvailableSeats(DBproject esql) {//6
		// For flight number and date, find the number of availalbe seats (i.e. total plane capacity minus booked seats )
    	try{
            String query= "SELECT P.seats - F.num_sold AS Available_Seats FROM Plane P, FlightInfo FI, Flight F WHERE P.ID = FI.plane_id AND FI.flight_id=F.fnum AND F.fnum= ";
		    String query2 = " AND F.actual_departure_date= ";

            // Get user input    		
            System.out.print("\tPlease Enter Flight Number:  ");
	    	String input = in.readLine();
	    	query += "\'"+input+"\'";
	    	System.out.print("\tPlease Enter Flight Date: ");
	    	String input2 = in.readLine();

	    	query2 += "\'"+input2+"\'";
	    	query += query2;
	    	System.out.print("\tTotal Remaining Seats for Flight Number  "+ input + "\n");
	    	esql.executeQueryAndPrintResult(query);
		}
        catch(Exception e){
            System.err.println (e.getMessage());
	   }	
	}

	public static void ListsTotalNumberOfRepairsPerPlane(DBproject esql) {//7
		// Count number of repairs per planes and list them in descending order
        try{
            String query = "SELECT P.id, P.make, P.model, P.age, P.seats, count(*) AS totalRepairs " 
                            + "FROM Repairs R, Plane P "
                            + "WHERE R.plane_id = P.id "
                            + "GROUP BY P.id "
                            + "ORDER BY totalRepairs DESC;";
            int rowCount = esql.executeQueryAndPrintResult(query);
            System.out.println("\nTotal row(s): " + rowCount + "\n");
            } 
        catch(Exception e){
                System.err.println(e.getMessage());
            }
	}

	public static void ListTotalNumberOfRepairsPerYear(DBproject esql) {//8
		// Count repairs per year and list them in ascending order
        try{
            String query = "SELECT EXTRACT(YEAR FROM R.repair_date) AS repair_year, count(*) AS totalRepairs "
                         + "FROM Repairs R "
                         + "GROUP BY repair_year "
                         + "ORDER BY repair_year ASC;";
            int rowCount = esql.executeQueryAndPrintResult(query);
            System.out.println("\ntotal row(s): " + rowCount + "\n");
            } 
        catch(Exception e){
                System.err.println(e.getMessage());
            }
	}
	
	public static void FindPassengersCountWithStatus(DBproject esql) {//9
		// Find how many passengers there are with a status (i.e. W,C,R) and list that number.
	    try{
            String query= "SELECT COUNT(R.status) AS Total FROM Customer C, Reservation R, Flight F WHERE  C.id=R.cid AND R.fid=F.fnum AND R.status= ";
    		String query2 = " AND F.fnum= ";
	    	System.out.print("\tPlease Enter Passenger Status W,C,R: ");
	    	String input = in.readLine();
	    	query += "\'"+input+"\'";
	    	System.out.print("\tPlease Enter Flight Number: ");
	    	String input2 = in.readLine();
	    	query2 += "\'"+input2+"\'";
	    	query += query2;
	    	System.out.print("\tTotal Number of Passengers with Status "+ input + "\n");
	    	esql.executeQueryAndPrintResult(query);
		}
	   catch(Exception e){
	    	System.err.println (e.getMessage());
	   }		
	}
    
    /*
     * Helper functions
    */

    // Adds a flight to the flight table and returns the primary key inserted. 
    public static int InsertFlight(DBproject esql){
	    int max_value = 0;
        try{
            // Get user input for the flight details information
            System.out.print("\tPlease Enter Ticket Cost: ");
		    String tCost = in.readLine();
		    System.out.print("\tPlease Enter Number of Seats Sold: ");
		    String numSold = in.readLine();
		    System.out.print("\tPlease Enter Number of Stops: ");
		    String numStops = in.readLine();
		    System.out.print("\tPlease Enter Actual Departure Date: ");
		    String actDep = in.readLine();
		    System.out.print("\tPlease Enter Actual Arrival Time: ");
		    String actArv = in.readLine();
		    System.out.print("\tPlease Enter Arrival Airport: ");
		    String arvAir = in.readLine();
		    System.out.print("\tPlease Enter Departure Airport: ");
		    String depAir = in.readLine();
		    System.out.print("\n");

            // Get the max id in the reservations table and increment it by one, then
            // insert a new flight.
		    String find_max="SELECT MAX(F.fnum) from Flight F;";
		    List<List<String>> rs = esql.executeQueryAndReturnResult(find_max);
		    String info = "SELECT * FROM Flight F Where F.fnum= ";

		    String insert_flight = "INSERT INTO Flight (fnum, cost, num_sold, num_stops,"
                                 + " actual_departure_date, actual_arrival_date, arrival_airport, departure_airport)";

            // If flight table has no records create new flight and insert it. Otherwise, get max flight number from flight
            // table, increment it by one and insert this as the new flight number.
            if(rs.size()==0){
                info += "\'"+max_value+"\';";
		        insert_flight += " VALUES  (" + max_value + ", \'" + tCost + "\',\'" + numSold + "\',\'" + numStops + "\',\'" 
                              + actDep + "\'," + " \'" + actArv + "\',\'" + arvAir + "\',\'" + depAir + "\');";
		        System.out.print("\tFlight Information entered: "+"\n");
		        esql.executeUpdate(insert_flight);
		        esql.executeQueryAndPrintResult(info);                    
            }
            else{
                // Get the max flight number in the flight table
		        for(List<String> row : rs){
		            for(String s: row){
                        max_value = Integer.parseInt(s);
		            }
                }
		        max_value++;
		        info += "\'"+max_value+"\';";
		        insert_flight += " VALUES  (" + max_value + ", \'" + tCost + "\',\'" + numSold + "\',\'" + numStops + "\',\'" 
                             + actDep + "\'," + " \'" + actArv + "\',\'" + arvAir + "\',\'" + depAir + "\');";
		        System.out.print("\tFlight Information entered: "+"\n");
		        esql.executeUpdate(insert_flight);
		        esql.executeQueryAndPrintResult(info);	
           }
        }
        catch(Exception e){
            System.err.println (e.getMessage());
        }
       return max_value;
    }





























}





