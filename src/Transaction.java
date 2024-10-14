public class Transaction {
    private int payerId;
    private double amount;
    private String description;

    public Transaction(int payerId, double amount, String description) {
        this.payerId = payerId;
        this.amount = amount;
        this.description = description;
    }

    public int getPayerId() {
        return payerId;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }
}