public class User {
    private int UserId;
    private String name;
    private double balance;

    public User(int UserId, String name,double balance) {
        this.UserId = UserId;
        this.name = name;
        this.balance=balance;
    }

    public int getMemberId() {
        return UserId;
    }

    public String getName() {
        return name;
    }
    public double getBalance(){
        return balance;
    }
}
