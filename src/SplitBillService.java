
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;
class Pair{
    String first;
    double second;
    Pair(String first,double second){
        this.first=first;
        this.second=second;

    }
}
public class SplitBillService {
    public void addUser(String name) {
        String insertQuery = "INSERT INTO users (name) VALUES (?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) { // Enable key retrieval

            st.setString(1, name);
            int cnt = st.executeUpdate();
            System.out.println(cnt + " User Added Successfully");

            // Fetching the generated user ID
            try (ResultSet rs = st.getGeneratedKeys()) {
                if (rs.next()) { // Check if a key was generated
                    int userId = rs.getInt(1); // Get the generated user ID
                    System.out.println("Generated User ID: " + userId); // Display the user ID
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createGroup(int[] users) {
        String query = "INSERT INTO `groups` (users) VALUES (?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) { // Enable key retrieval

            // Convert the users array to a JSON-like format or any appropriate format as per your DB design
            String jsonArray = Arrays.toString(users).replaceAll("\\s+", ""); // Optional: Remove whitespace
            st.setString(1, jsonArray);

            int cnt = st.executeUpdate();
            System.out.println(cnt + " group is created successfully");

            // Fetching the generated group ID
            try (ResultSet rs = st.getGeneratedKeys()) {
                if (rs.next()) { // Check if a key was generated
                    int groupId = rs.getInt(1); // Get the generated group ID
                    System.out.println("Generated Group ID: " + groupId); // Display the group ID
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public  void addTransaction(int payerId,int grpid,double amount, String description){
        String insertQuery="INSERT INTO transactions (payerid, groupid, amount, description) VALUES (?, ?, ?, ?)";
        try(Connection con=DBConnection.getConnection();
            PreparedStatement st=con.prepareStatement(insertQuery)){
            st.setInt(1,payerId);
            st.setInt(2,grpid);
            st.setDouble(3,amount);
            st.setString(4,description);
            int cnt=st.executeUpdate();
            System.out.println(cnt+" transaction added");

        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }

    }
    public int recieveSplits(HashMap<Integer, Double> map, int gid, Scanner sc) {
        String sql = "SELECT users FROM `groups` WHERE groupid = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {

            st.setInt(1, gid);  // Use gid parameter passed to the method
            ResultSet rs = st.executeQuery();

            if (rs.next()) {
                // Get the users as a comma-separated string (e.g., "1,2,3,4")
                String usersStr = rs.getString("users");
               // System.out.println("Users String: " + usersStr);

                // Check for null or empty string
                if (usersStr == null || usersStr.isEmpty()) {
                    System.out.println("No users found for the specified group.");
                    return -1;
                }

                // Split the string into individual user IDs
                String[] stringArray = usersStr.replaceAll("[\\[\\]\\s]", "").split(",");

                // Convert to an integer array
                int[] arr = Arrays.stream(stringArray)
                        .mapToInt(Integer::parseInt)
                        .toArray();
                int sum=0;
                for (int x : arr) {
                    System.out.print("Enter user"+x+" part:");
                    double temp=sc.nextDouble();
                    sum+=temp;
                    map.put(x,temp);
                }

                System.out.println();
               return  sum;

            } else {
                System.out.println("No group found with the specified groupid.");
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Print stack trace for better debugging
        }
        return 0;
    }
    public void setBalance(HashMap<Integer, Double> map, int gid, int pid, int tamount, Scanner sc) {
        String query1 = "UPDATE group_balances SET balance = ? WHERE userid = ? AND groupid = ?;";
        String query2 = "INSERT INTO group_balances (userid, groupid, balance) VALUES (?, ?, ?);";
        String query = "SELECT balance FROM group_balances WHERE groupid = ?;";
        String query3 = "SELECT balance FROM group_balances WHERE groupid = ? AND userid=?;";

        try (Connection con = DBConnection.getConnection()) {
            // Check if any record exists for the group ID
            boolean exists;
            double existingBalance = 0;

            try (PreparedStatement st = con.prepareStatement(query)) {
                st.setInt(1, gid);
                ResultSet rs = st.executeQuery();
                exists = rs.next();  // Check if any record exists for the group ID
            }

            // Now we are in the context where we know if records exist
            try (PreparedStatement st1 = con.prepareStatement(query1);
                 PreparedStatement st2 = con.prepareStatement(query2);
                 PreparedStatement st3 = con.prepareStatement(query3)) {

                for (Map.Entry<Integer, Double> entry : map.entrySet()) {
                    int uid = entry.getKey();
                    double bal = (uid == pid) ? (tamount - entry.getValue()) : (-1 * entry.getValue());

                    if (exists) {
                        // Update existing balance
                        st3.setInt(1, gid);
                        st3.setInt(2, uid);
                        ResultSet rs3 = st3.executeQuery();

                        if (rs3.next()) { // Ensure there is a balance to read
                            existingBalance = rs3.getDouble("balance");
                            bal += existingBalance; // Combine existing balance
                        }
                        // Update balance for user
                        st1.setDouble(1, bal);
                        st1.setInt(2, uid);
                        st1.setInt(3, gid);
                        int cnt1 = st1.executeUpdate();
                        //System.out.println(cnt1 + " entry updated for user ID: " + uid);

                    } else {
                        // Insert new balance
                        st2.setInt(1, uid);
                        st2.setInt(2, gid);
                        st2.setDouble(3, bal);
                        int cnt2 = st2.executeUpdate();
                        //System.out.println(cnt2 + " entry inserted for user ID: " + uid);
                    }
                }
            } catch (SQLException e) {
                System.out.println("Error during update/insert: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
    public void viewTransactions(int gid) {
        String query = "SELECT * FROM transactions WHERE groupid = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement(query)) {

            // Set the groupid parameter in the query
            st.setInt(1, gid);

            // Execute the query
            try (ResultSet rs = st.executeQuery()) {

                // Print table header
                System.out.printf("%-10s %-10s %-10s %-20s\n", "Payer ID", "Group ID", "Amount", "Description");
                System.out.println("--------------------------------------------------------------");

                // Fetch and display records in tabular format
                while (rs.next()) {
                    int payerId = rs.getInt("payerid");
                    int groupId = rs.getInt("groupid");
                    double amount = rs.getDouble("amount");
                    String description = rs.getString("description");

                    // Display in formatted table
                    System.out.printf("%-10d %-10d %-10.2f %-20s\n", payerId, groupId, amount, description);
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void settleBill(int gid) {
        String query = "SELECT userid, balance FROM group_balances WHERE groupid = ?;";
        String fetchName = "SELECT name FROM users WHERE userid = ?;";
        ArrayList<Pair> posBal = new ArrayList<>();
        ArrayList<Pair> negBal = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement(query)) {

            // Set the groupid parameter before executing the query
            st.setInt(1, gid);

            // Fetch balances from group_balances
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    int uid = rs.getInt("userid");
                    double bal = rs.getDouble("balance");

                    // Fetch the user's name based on their user id
                    try (PreparedStatement st2 = con.prepareStatement(fetchName)) {
                        st2.setInt(1, uid);
                        try (ResultSet rs1 = st2.executeQuery()) {
                            if (rs1.next()) {
                                String uname = rs1.getString("name");

                                if (bal > 0) {
                                    posBal.add(new Pair(uname, bal));
                                } else {
                                    negBal.add(new Pair(uname, bal));
                                }
                            }
                        }
                    }
                }
            }

            // Settle the bill between users
            int m = posBal.size();
            int n = negBal.size();
            int i = 0, j = 0;

            while (i < m && j < n) {
                String posusr = posBal.get(i).first;
                double pos = posBal.get(i).second;
                String negusr = negBal.get(j).first;
                double neg = -1 * negBal.get(j).second; // Convert negative balance to positive

                if (pos >= neg) {
                    System.out.println(negusr + " will pay " + neg + " to " + posusr);
                    pos -= neg;
                    j++; // Move to the next negative balance user
                    if (pos == 0) i++; // Move to the next positive balance user only if current is settled
                } else {
                    System.out.println(negusr + " will pay " + pos + " to " + posusr);
                    neg -= pos;
                    i++; // Move to the next positive balance user
                    if (neg == 0) j++; // Move to the next negative balance user only if current is settled
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public boolean isValidUser(int uid){
        String query="SELECT userid FROM users";
        try(Connection con=DBConnection.getConnection();
            Statement st=con.createStatement();
            ResultSet rs=st.executeQuery(query);){
              while (rs.next()){
                  int temp=rs.getInt("userid");
                  if(temp==uid) return  true;
              }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return  false;
    }

    public boolean isValidGroup(int gid){
        String query="SELECT groupid FROM `groups`";
        try(Connection con=DBConnection.getConnection();
            Statement st=con.createStatement();
            ResultSet rs=st.executeQuery(query);){
            while (rs.next()){
                int temp=rs.getInt("groupid");
                if(temp==gid) return  true;
            }
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return  false;

    }
    public void setBalanceZero(int gid){
        String query="UPDATE group_balances SET balance=? where groupid=?;";
        try(Connection con=DBConnection.getConnection();
            PreparedStatement st=con.prepareStatement(query);){
            st.setInt(1,0);
            st.setInt(2,gid);
            int cnt=st.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());

        }
    }





}
