package src;

import java.sql.*;

public class StockDatabase {
    private final String databaseFilename, notificationTableName, personalStockListTableName;
    private Connection conn;

    StockDatabase(String fileName) {
        databaseFilename = fileName;
        notificationTableName = "notifications";
        personalStockListTableName = "personal_list";
    }

    public void connect() {
        String url = "jdbc:sqlite:" + databaseFilename;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.err.println("SQLException in StockDatabase.connect()");
        }
        createTables();
    }

    private void createTables() {
        // SQL statement for creating a new table
        String sqlQueryTable1 = "CREATE TABLE IF NOT EXISTS " + notificationTableName + " (\n"
                + "    email text NOT NULL,\n" + "    stocksymbol text NOT NULL,\n"
                + "    below_or_above text NOT NULL,\n" + "    price double NOT NULL,\n"
                + "		UNIQUE (email, stocksymbol, below_or_above)\n);";

        String sqlQueryTable2 = "CREATE TABLE IF NOT EXISTS " + personalStockListTableName + " (\n"
                + "    email text NOT NULL,\n" + "    stocksymbol text NOT NULL,\n"
                + "		UNIQUE (email, stocksymbol)\n);";
        try {
            Statement stmt1 = conn.createStatement();
            stmt1.execute(sqlQueryTable1);
            Statement stmt2 = conn.createStatement();
            stmt2.execute(sqlQueryTable2);

        } catch (SQLException e) {
            System.err.println("SQLException in StockDatabase.createTables()");
        }
    }

    public void insertStockNotification(StockNotification sn) {
        String sql = "REPLACE INTO " + notificationTableName + " (email,stocksymbol,below_or_above,price) VALUES(?,?,?,?)";
        try {
            PreparedStatement preparedStatement = prepareStatement(sn, sql);
            preparedStatement.setDouble(4, sn.getPrice());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("SQLException in StockDatabase.insertStockNotification(StockNotification)");
        }
    }

    public void insertStockObjectToPersonalList(StockObjectPlusEmail stockObjectPlusEmail) {
        String sql = "REPLACE INTO " + personalStockListTableName + " (email,stocksymbol) VALUES(?,?)";
        try {
            PreparedStatement preparedStatement = prepareStatement(stockObjectPlusEmail, sql);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("SQLException in StockDatabase.insertStockObjectToPersonalList(StockObjectPlusEmail stockObjectPlusEmail, String email)");
        }
    }

    public ResultSet selectStocksWithEmail(String email, RequestStockList.StockType stockType) {
        String sql = "";
        if(stockType == RequestStockList.StockType.STOCK_NOTIFICATION) {
            sql = "SELECT * FROM " + notificationTableName + " WHERE email = ?";
        }
        else if(stockType == RequestStockList.StockType.STOCK_OBJECT) {
            sql = "SELECT * FROM " + personalStockListTableName + " WHERE email = ?";
        }
        ResultSet rs = null;
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, email);
            rs = preparedStatement.executeQuery();
        } catch (SQLException e) {
            System.err.println("SQLException in StockDatabase.selectStocksWithEmail(String)");
        }
        return rs;

    }

    public ResultSet selectAllNotifications() {
        String sql = "SELECT * FROM " + notificationTableName;
        ResultSet rs = null;
        try {
            Statement statement = conn.createStatement();
            rs = statement.executeQuery(sql);

        } catch (SQLException e) {
            System.err.println("SQLException in StockDatabase.selectAllNotifications()");
        }
        return rs;
    }


    public void selectAndPrintAll(RequestStockList.StockType stockType) {
        String sql = "SELECT * FROM " + notificationTableName;

        ResultSet rs = null;
        try {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            rs = preparedStatement.executeQuery();
        } catch (SQLException e1) {
            System.err.println("SQLException in StockDatabase.selectAndPrintAll() Exception: e1");
        }
        try {
            while (rs.next()) {
                System.out.println("-----------------------------------------------\n");
                System.out.println(rs.getString(1));
                System.out.println(rs.getString(2));
                System.out.println(rs.getString(3));
                System.out.println(rs.getDouble(4));
                System.out.println("-----------------------------------------------\n\n\n");
            }
        } catch (SQLException e2) {
            System.err.println("SQLException in StockDatabase.connect() Exception: e2");
        }
    }

    public void removeNotification(StockNotification stockNotification) {
        String sql = "delete from " + notificationTableName + " where email = ? AND stocksymbol = ? AND below_or_above = ?";
        try {
            PreparedStatement preparedStatement = prepareStatement(stockNotification, sql);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("SQLException in StockDatabase.removeNotification(StockNotification)");
        }
    }

    public void removeStockObject(StockObjectPlusEmail stockObjectPlusEmail) {
        String sql = "delete from " + personalStockListTableName + " where email = ? AND stocksymbol = ?";
        try {
            PreparedStatement preparedStatement = prepareStatement(stockObjectPlusEmail, sql);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("SQLException in StockDatabase.removeStockObject(StockObjectPlusEmail stockObjectPlusEmail, String email)");
        }
    }

    public boolean duplicateEntryExists(StockNotification stockNotification) {
        String sql = "SELECT * FROM " + notificationTableName + " WHERE email = ? AND stocksymbol = ? AND below_or_above = ?";

        try {
            PreparedStatement preparedStatement = prepareStatement(stockNotification, sql);
            ResultSet rs = preparedStatement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("SQLException in StockDatabase.duplicateEntryExists(StockNotification)");
        }

        return false;
    }

    public boolean duplicateEntryExists(StockObjectPlusEmail stockObjectPlusEmail) {
        String sql = "SELECT * FROM " + personalStockListTableName + " WHERE email = ? AND stocksymbol = ?";

        try {
            PreparedStatement preparedStatement = prepareStatement(stockObjectPlusEmail, sql);
            ResultSet rs = preparedStatement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("SQLException in StockDatabase.duplicateEntryExists(StockObjectPlusEmail stockObjectPlusEmail, String email)");
        }

        return false;
    }


    private PreparedStatement prepareStatement(StockNotification stockNotification, String query) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(query);
        preparedStatement.setString(1, stockNotification.getEmail());
        preparedStatement.setString(2, stockNotification.getSymbol());
        if(stockNotification.getBelowOrAbove() == StockNotification.BelowOrAbove.BELOW) {
            preparedStatement.setString(3, "Below");
        }
        else {
            preparedStatement.setString(3, "Above");
        }
        return preparedStatement;
    }

    private PreparedStatement prepareStatement(StockObjectPlusEmail stockObjectPlusEmail, String query) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement(query);
        preparedStatement.setString(1, stockObjectPlusEmail.getEmail());
        preparedStatement.setString(2, stockObjectPlusEmail.getStockObject().getSymbol());

        return preparedStatement;
    }

}
