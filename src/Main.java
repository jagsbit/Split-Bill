
import java.util.HashMap;
import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        //creating objects
        SplitBillService sbs=new SplitBillService();
        Scanner sc=new Scanner(System.in);
        while(true){
            System.out.println("1. Create User\n2. Create Group\n3. Add Transaction\n4. View Transactions\n5. Settle Bill\n6. Exit");
            int choice=sc.nextInt();
            sc.nextLine();
            switch (choice){
                case 1:
                    createUser(sc,sbs);
                    break;
                case 2:
                    createGroup(sc,sbs);
                    break;
                case 3:
                    addTransaction(sc,sbs);
                    break;
                case 4:
                    viewTransactions(sc,sbs);
                    break;
                case 5:
                    settleBill(sc,sbs);
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Enter Correct Choice");
                    break;
            }
        }

    }

    private static void settleBill(Scanner sc, SplitBillService sbs) {
        System.out.println("Enter GroupId:");
        int gid=sc.nextInt();
        if(!sbs.isValidGroup(gid)){
            System.out.println("This Group Doesn't Exist.. Enter Valid GroupId");
            return;
        }
        sbs.settleBill(gid);
    }

    private static void viewTransactions(Scanner sc, SplitBillService sbs) {
        System.out.println("Enter GroupId:");
        int gid=sc.nextInt();
        if(!sbs.isValidGroup(gid)){
            System.out.println("This Group Doesn't Exits..Enter Correct GroupId..");
            return;
        }
        sbs.viewTransactions(gid);
    }

    private static void createGroup(Scanner sc, SplitBillService sbs) {
        System.out.println("Enter the number of Users in the group");
        int n=sc.nextInt();
        int[] users=new int[n];
        for(int i=0;i<n;i++){
            System.out.println("Enter Id of user"+(i+1)+" :");
            int temp=sc.nextInt();
            if(!sbs.isValidUser(temp)){
                System.out.println("User doesn't exist in the system....");
                return;
            }
            users[i]=temp;


        }
        sbs.createGroup(users);
        System.out.println();
    }

    private static void addTransaction(Scanner sc,SplitBillService sbs) {
        System.out.println("Enter the group id:");
        int grpid=sc.nextInt();
        if(!sbs.isValidGroup(grpid)){
            System.out.println("This Group Doesn't Exits.. Create Group First or Enter Correct GroupId..");
            return;
        }
        System.out.println("Enter the id of Payer:");
        int payerid=sc.nextInt();
        if(!sbs.isValidUser(payerid)){
            System.out.println("This user Doesn't Exists..");
            return;
        }
        System.out.println("Enter Total amount:");
        int tamount=sc.nextInt();
        sc.nextLine();
        System.out.println("Enter Transaction Description");
        String desc=sc.nextLine();

        HashMap<Integer,Double> map=new HashMap<>();

        int sum=sbs.recieveSplits(map,grpid,sc);
        if(sum!=tamount){
            System.out.println("Splits Not Equal to Total Amount... Enter Correct Splits");
            return;
        }
        //System.out.println(map);

        sbs.setBalance(map,grpid,payerid,tamount,sc);


        sbs.addTransaction(payerid,grpid,tamount,desc);


    }

    private static void createUser(Scanner sc,SplitBillService sbs) {
        System.out.println("Enter the name of the user");
        String uName=sc.nextLine();
        sbs.addUser(uName);

    }
}