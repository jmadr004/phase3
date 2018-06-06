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
import java.util.Scanner;

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
				System.out.print (rs.getString (i) + "\t" );
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

		//iterates through the result set and count nuber of results.
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
	
	}catch(Exception e){
		System.err.println (e.getMessage());
	   }	
	}

	public static void AddPilot(DBproject esql) {//2
	}

	public static void AddFlight(DBproject esql) {//3
		// Given a pilot, plane and flight, adds a flight in the DB
	try{
		System.out.print("\tPlease Enter Ticket Cost: ");
		String TCost = in.readLine();
		System.out.print("\tPlease Enter Number of Seats Sold: ");
		String NumSold = in.readLine();
		System.out.print("\tPlease Enter Number of Stops: ");
		String NumStops = in.readLine();
		System.out.print("\tPlease Enter Actual Departure Date: ");
		String ActDep = in.readLine();
		System.out.print("\tPlease Enter Actual Arrival Time: ");
		String ActArv = in.readLine();
		System.out.print("\tPlease Enter Arrival Airport: ");
		String ArvAir = in.readLine();
		System.out.print("\tPlease Enter Departure Airport: ");
		String DepAir = in.readLine();
		System.out.print("\n");

		String find_max="SELECT MAX(F.fnum) from Flight F;";
		int test=0;
		List<List<String>> max_count=esql.executeQueryAndReturnResult(find_max);
		String Info="SELECT * FROM Flight F Where F.fnum= ";
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
		String update_flight="INSERT INTO Flight (fnum, cost, num_sold, num_stops,"
		+" actual_departure_date, actual_arrival_date, arrival_airport, departure_airport)"
		+" VALUES  ("+test+", \'"+TCost+"\',\'"+NumSold+"\',\'"+NumStops+"\',\'"+ActDep+"\'," 			
		+" \'"+ActArv+"\',\'"+ArvAir+"\',\'"+DepAir+"\');";
		System.out.print("\tFlight Information entered: "+"\n");
		esql.executeUpdate(update_flight);
		esql.executeQueryAndPrintResult(Info);	
		}
		else{
		test++;
		Info += "\'"+test+"\';";
		String update_flight="INSERT INTO Flight (fnum, cost, num_sold, num_stops,"
		+" actual_departure_date, actual_arrival_date, arrival_airport, departure_airport)"
		+" VALUES  ("+test+", \'"+TCost+"\',\'"+NumSold+"\',\'"+NumStops+"\',\'"+ActDep+"\'," 			
		+" \'"+ActArv+"\',\'"+ArvAir+"\',\'"+DepAir+"\');";
		System.out.print("\tFlight Information entered: "+"\n");
		esql.executeUpdate(update_flight);
		esql.executeQueryAndPrintResult(Info);	 
		}
	
	}catch(Exception e){
		System.err.println (e.getMessage());
	   }
	
	}

	public static void AddTechnician(DBproject esql) {//4
	}

	public static void BookFlight(DBproject esql) {//5
		// Given a customer and a flight that he/she wants to book, add a reservation to the DB
		//
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
	}

	public static void ListTotalNumberOfRepairsPerYear(DBproject esql) {//8
		// Count repairs per year and list them in ascending order
	}
	
	public static void FindPassengersCountWithStatus(DBproject esql) {//9
	// Find how many passengers there are with a status (i.e. W,C,R) and list that number.
	    try{String query= "SELECT COUNT(R.status) AS Total FROM Customer C, Reservation R, Flight F WHERE  C.id=R.cid AND R.fid=F.fnum AND R.status= ";
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
}
