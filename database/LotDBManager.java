package database;

import objects.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
/**
 * File Name: LotDBManager.java
 * UWF Parking App
 *
 * This class holds parking-related tables and SQL statements to add to them.
 * For now, all Lot-related functionality is complete.
 * Next iteration will see to violation-related features and map-locations.
 * The option to save a report to a text file will also be considered.
 *
 * @author Julien
 * @version 1.0
 */
public class LotDBManager
{
    /**
     * Used to access database
     */
    private Connection conn;

    /**
     * Used to issue general SQL statements
     */
    private Statement stat;

    /**
     * Used for specific SQL statements
     */
    private PreparedStatement pStat;

    /**
     * Used to store the results from a query
     */
    private ResultSet result;

    /**
     * Default constructor that reads the properties file and initializes access to the database
     * The tables are created if they do not exist and a couple of lots are added and updated
     */
    public LotDBManager ()
    {
        ParkingLot e = new ParkingLot("E", 180, 0, 0,
                0, 0, 0, 180, 0, true);
        ParkingLot z = new ParkingLot("Z", 125, 0, 0,
                0, 0, 0, 125, 0, true);
        try
        {
            SimpleDataSource.init("database/database.properties");
            this.openConnection();
            this.stat = this.conn.createStatement();
            this.createTables(0);
            addLot(e);
            updateLotCars("E", 74);
            addLot(z);
            updateLotCars("Z", 44);
            //stat.execute("DROP TABLE Lot");
        }
        catch (Exception ex)
        {
            System.out.println("exception in creating user db manager");
            ex.printStackTrace();
        }
    }

    /**
     * Method used show to open the connection
     */
    public void openConnection ()
    {
        try
        {
            conn = SimpleDataSource.getConnection();
        }
        catch (Exception e)
        {
            System.out.println("connection already open");
        }
    }

    /**
     * Method used to create the Lot tables.
     * 0 - Lots, 1 - Violations, other - Map
     * Always checks to makes sure the tables do not
     * exist before creating them.
     * @param tableToCreate determines what table to create
     */
    public void createTables (int tableToCreate)
    {
        try
        {
            DatabaseMetaData meta = conn.getMetaData();
            result = meta.getTables(null, null, "%", null);
            while (this.result.next())
                switch (tableToCreate)
                {
                    case 0:
                        if (result.getString("TABLE_NAME").equals("LOT"))
                            return;
                    case 1:
                        if (result.getString("TABLE_NAME").equals("VIOLATION"))
                            return;
                    default:
                        if (result.getString("TABLE_NAME").equals("MAP"))
                            return;
                }
            switch (tableToCreate)
            {
                case 0:
                    stat.execute("CREATE TABLE Lot (Lot_ID VARCHAR(3), Total INTEGER, Available INTEGER, " +
                            "Occupied INTEGER, Reserved INTEGER, Handicapped INTEGER, Commuter INTEGER, " +
                            "Resident INTEGER, Staff INTEGER, Visitor INTEGER, Motorcycle INTEGER, " +
                            "Status VARCHAR(10), Violations INTEGER, Time TIMESTAMP)");
                    System.out.println("Lot table created");
                    break;
                case 1:
                    stat.execute("CREATE TABLE Violation (Lot_ID VARCHAR2(3), Summary VARCHAR(100), " +
                            "Violation_ID VARCHAR(10), Time TIMESTAMP");
                    System.out.println("Violation table created");
                    break;
                default:
                    stat.execute("CREATE TABLE Map (Lot_ID VARCHAR(3), Map_Location VARCHAR(50)");
                    System.out.println("Map table created");
            }
        }
        catch (SQLException s)
        {
            System.out.println("sql exception in creating lot tables");
            s.printStackTrace();
        }
    }

    /**
     * Method used to add to the Lot table.
     * (SEND OVER SUPERVISOR-CREATED OBJECT)
     * (Call once per lot, use updateLot for updates)
     * @param p lot to add
     */
    public void addLot (ParkingLot p)
    {
        String query = "INSERT INTO Lot VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try
        {
            conn.setAutoCommit(false);                                      // disable automatic SQL statements for now

            pStat = conn.prepareStatement(query);                           // prepare the statement
            pStat.setString(1, p.getLotID());
            pStat.setInt(2, p.getTotal());
            pStat.setInt(3, p.getAvailable());
            pStat.setInt(4, p.getOccupied());
            pStat.setInt(5, p.getReserved());
            pStat.setInt(6, p.getHandicapped());
            pStat.setInt(7, p.getCommuter());
            pStat.setInt(8, p.getResident());
            pStat.setInt(9, p.getStaff());
            pStat.setInt(10, p.getVisitor());
            pStat.setInt(11, p.getMotorcycle());
            if (p.isOpen())
                pStat.setString(12, "open");
            else
                pStat.setString(12, "close");

            pStat.setInt(13, p.getViolations());
            pStat.setTimestamp(14, new Timestamp(p.getRecordDate().getTime()));

            pStat.executeUpdate();                                                      // update the statement
            conn.commit();                                                              // and send it to the table

            conn.setAutoCommit(true);                                               // turn on automatic SQL statements
            //System.out.println("added lot");
        }
        catch (SQLException s)
        {
            System.out.println("sql exception in addUser");
            s.printStackTrace();
        }
    }

    /**
     * Method used to create a ParkingLot object.
     * A lot is retrieved through lotID
     * and latest timestamp (date and time).
     * Will also check to see if lot exists
     * and return false if it doesn't.
     *
     * @param lotID the name of the lot to find
     * @return whether or not the user can see a lot's info
     */
    public ParkingLot getLot (String lotID)
    {

        ParkingLot lotToReturn = new ParkingLot();
        String query1 = String.format("SELECT * FROM Lot" +
                " WHERE Lot_ID = '%s'", lotID);
        String query2 = String.format("SELECT * FROM Lot l1 " +
                "WHERE Lot_ID = '%s'" +
                "AND Time IN " +
                "(SELECT MAX(Time) FROM Lot l2 WHERE l1.Lot_ID = l2.Lot_ID)", lotID);
        try
        {
            conn.setAutoCommit(true);
            result = stat.executeQuery(query1);
            if (!result.next())
            {
                //System.out.println("lot does not exist");
                return null;
            }

            result = stat.executeQuery(query2);
            while (result.next())
            {
                lotToReturn.setLotID(result.getString(1));
                lotToReturn.setTotal(result.getInt(2));
                lotToReturn.setAvailable(result.getInt(3));
                lotToReturn.setOccupied(result.getInt(4));
                lotToReturn.setReserved(result.getInt(5));
                lotToReturn.setHandicapped(result.getInt(6));
                lotToReturn.setCommuter(result.getInt(7));
                lotToReturn.setResident(result.getInt(8));
                lotToReturn.setStaff(result.getInt(9));
                lotToReturn.setVisitor(result.getInt(10));
                lotToReturn.setMotorcycle(result.getInt(11));
                if (result.getString(12).equals("OPEN") || result.getString(12).equals("open"))
                    lotToReturn.setOpen(true);
                else
                    lotToReturn.setOpen(false);
                lotToReturn.setViolations(result.getInt(13));
                lotToReturn.setRecordDate(new Date(result.getTimestamp(14).getTime()));
            }
            //System.out.println("updated lot");
        }
        catch (Exception e)
        {
            System.out.println("lot doesn't exist");
            e.printStackTrace();
            return null;
        }
        //System.out.println("return lot for lotDBManager.getLot()");
        return lotToReturn;
    }

    /**
     * Method used to update a lot's number of cars and violations.
     * Creates a lot object from latest info
     * Applies it back after adjusting available
     * & occupied spaces. Updates the Timestamp.
     * Adds it back to the table.
     * Return false if lot doesn't exist.
     * @param lotID   name of lot to find
     * @param numCars number of cars to enter
     * @param violations number of violations to enter
     * @return ParkingLot object updated.
     */
    public ParkingLot updateLot (String lotID, int numCars, int violations)
    {
        ParkingLot tempLot = getLot(lotID);

        if (tempLot == null)
        {
            //System.out.println("Lot does not exist");
            return null;
        }
        else
        {
            if (tempLot.getTotal() < numCars)
                tempLot.setTotal(numCars);
            tempLot.setOccupied(numCars);
            tempLot.setAvailable(tempLot.getTotal() - numCars);
            tempLot.setRecordDate(new Date());
            tempLot.setViolations(tempLot.getViolations() + violations);
            addLot(tempLot);
        }
        return tempLot;
    }

    /**
     * Method used to update a lot's available spaces.
     * Creates a lot object from latest info
     * Applies it back after adjusting available
     * & occupied spaces. Updates the Timestamp.
     * Adds it back to the table.
     * Return false if lot doesn't exist.
     * @param lotID   name of lot to find
     * @param numCars number of cars to enter
     * @return ParkingLot object updated.
     */
    public ParkingLot updateLotCars (String lotID, int numCars)
    {
        ParkingLot tempLot = getLot(lotID);

        if (tempLot == null)
        {
            System.out.println("Lot does not exist");
            return null;
        }
        else
        {
            if (tempLot.getTotal() < numCars)
                tempLot.setTotal(numCars);
            tempLot.setOccupied(numCars);
            tempLot.setAvailable(tempLot.getTotal() - numCars);
            tempLot.setRecordDate(new Date());
            addLot(tempLot);
        }
        return tempLot;
    }

    /**
     * Method used to update a lot's number of violations.
     * Creates a lot object from latest info
     * Applies it back after adjusting available
     * & occupied spaces. Updates the Timestamp.
     * Adds it back to the table.
     * Return false if lot doesn't exist.
     * @param lotID   name of lot to find
     * @param violations number of violations to enter
     * @return ParkingLot object updated.
     */
    public ParkingLot updateLotViolations (String lotID, int violations)
    {
        ParkingLot tempLot = getLot(lotID);

        if (tempLot == null)
        {
            //System.out.println("Lot does not exist");
            return null;
        }
        else
        {
            tempLot.setViolations(violations);
            tempLot.setRecordDate(new Date());
            addLot(tempLot);
        }
        return tempLot;
    }

    /**
     * Method used to update a lot's space information.
     * Use Strings to classify spaces
     * - "resident", "staff", "motorcycle", ...
     * Updates latest row.
     * Return false if lot doesn't exist.
     * @param lotID    name of lot to find
     * @param category type of space
     * @return ParkingLot object updated.
     */
    public ParkingLot updateLotSpaces (String lotID, String category, int numSpaces)
    {
        String query = "UPDATE Lot SET " + category + " = ? " +
                "WHERE Lot_ID = ? " +
                "AND Time = (SELECT max(Time) FROM Lot WHERE Lot_ID = ?";

        ParkingLot tempLot = getLot(lotID);
        try
        {
            conn.setAutoCommit(false);                                      // disable automatic SQL statements for now

            pStat = conn.prepareStatement(query);                           // prepare the statement
            pStat.setInt(1, numSpaces);
            pStat.setString(2, lotID);
            pStat.setString(3, lotID);

            pStat.executeUpdate();                                                      // update the statement
            conn.commit();                                                              // and send it to the table

            conn.setAutoCommit(true);                                               // turn on automatic SQL statements
            //System.out.println("executed command");
        }
        catch (SQLException s)
        {
            //System.out.println("lot does not exist");
            return null;
        }
        return tempLot;
    }

    /**
     * Method used to update a lot's availability
     * A new row is made and added with new time.
     * Will also check to see if lot exists
     * and return false if it doesn't.
     * @param lotID  name of lot to find
     * @param isOpen a lot's status (open/true or close/false)
     * @return ParkingLot object updated
     */
    public ParkingLot updateLotStatus (String lotID, boolean isOpen)
    {
        ParkingLot tempLot = getLot(lotID);

        if (tempLot == null)
        {
            //System.out.println("Lot does not exist");
            return null;
        }
        else
        {
            tempLot.setOpen(isOpen);
            tempLot.setRecordDate(new Date());
            addLot(tempLot);
        }
        return tempLot;
    }

    /**
     * Method used show to close the connection
     */
    public void closeConnection()
    {
        try
        {
            conn.close();
        }
        catch (Exception e)
        {
            System.out.println("no connection open");
        }
    }
}