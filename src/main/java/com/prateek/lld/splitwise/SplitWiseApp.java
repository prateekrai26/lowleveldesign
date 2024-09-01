package com.prateek.lld.splitwise;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Data
class User{
  String userID;
  String name;
  List<Group> groupList;
  User(String userID , String name){
      this.userID = userID;
      this.name = name;
      groupList = new ArrayList<>();
  }
}

@AllArgsConstructor
@Data
class Expense{
  String expenseID;
  String name;
  Split split;
}
@Data
abstract class Split{
  double amount;
  String payingUserId;
  Map<String , Double> splitAmountMap;
  abstract Map<String,Double> calculateSplitAmount();
  Split(){
      splitAmountMap = new HashMap<>();
  }
}

class PercentageSplit extends  Split{
   Map<String , Double> percentageMap;
   PercentageSplit(String payingUserId , double amount , Map<String , Double> percentageMap){
     this.percentageMap = percentageMap;
     this.amount = amount;
     this.payingUserId =  payingUserId;
       calculateSplitAmount();
   }

  @Override
  Map<String, Double> calculateSplitAmount() {
    for(String userID : percentageMap.keySet()){
       splitAmountMap.put(userID , percentageMap.get(userID) * amount/100);
    }
    return splitAmountMap;
  }
}


class SplitByExactAmount extends Split{
  Map<String , Double> exactAmountMap;
   SplitByExactAmount(String payingUserId ,double amount , Map<String, Double> exactAmountMap){
    this.exactAmountMap = exactAmountMap;
    this.amount = amount;
       this.payingUserId =  payingUserId;
       calculateSplitAmount();
  }

  void add(String userID , Double amount){
    exactAmountMap.put(userID , amount);
  }
  @Override

  Map<String, Double> calculateSplitAmount() {
     for(String userID : exactAmountMap.keySet()){
       splitAmountMap.put(userID , exactAmountMap.get(userID));
     }
     return splitAmountMap;
  }
}

class EqualSplit extends Split{
  Set<String> users;
  EqualSplit(String payingUserId ,double amount , List<String> users){
    this.users = new HashSet<>(users);
    this.amount = amount;
      this.payingUserId =  payingUserId;
    calculateSplitAmount();
  }
  void add(String userID){
    users.add(userID);
  }

  void remove(String userId){
       users.remove(userId);
  }

  @Override
  Map<String, Double> calculateSplitAmount() {
    for(String userId : users){
       splitAmountMap.put(userId , amount/users.size());
    }
    return splitAmountMap;
  }
}

@Data
class Group {
  String groupId;
  String name;
  Set<String> users;
  Map<String, List<Balance>> userToAmountMap;
  Map<String, Expense> expenseMap;
  Group(String groupId , String name){
      this.groupId = groupId;
      this.name = name;
      users = new HashSet<>();
      userToAmountMap = new HashMap<>();
      expenseMap = new HashMap<>();
  }
  void addExpense(Expense expense) {
     expenseMap.put(expense.expenseID, expense);
  }

  void deleteExpense(Expense expense) {
    expenseMap.remove(expense.expenseID, expense);
  }
  void addUser(String userId) {
      users.add(userId);
  }
}

class Balance{
  String id;
  Double amount;
  String userId;
}
class Owe extends Balance{
   Double collectMoney(Double amount){
     return null;
   }
}

class Owes extends Balance{
   void sendMoney(Double amount){
   }
}

class UserManager{
  static UserManager userManager = new UserManager();
  Map<String , User> userMap;
  private UserManager(){
     userMap = new HashMap<>();
  }

  void addUser(User user){
    userMap.put(user.userID , user);
  }

  User getUser(String userID){
    return userMap.get(userID);
  }

  static UserManager getInstance(){
    return userManager;
  }
}

interface BalanceCalculator{
      Map<String , List<Balance>> calculateBalance(List<Expense> expenses);
}

class DefaultBalanceCalculator implements BalanceCalculator{

    @Override
    public Map<String, List<Balance>> calculateBalance(List<Expense> expenses) {
       AtomicReference<Double> totalExpense = new AtomicReference<>(0.0);
        expenses.stream().forEach(x-> totalExpense.set(totalExpense.get() + x.split.amount));
        Map<String ,Double> totalExpenseMap = new HashMap<>();
        Map<String , Double> payingUserAmoutMap = new HashMap<>();
        for(Expense expense : expenses){
           Map<String , Double> map = expense.getSplit().getSplitAmountMap();
           for(String userID : map.keySet()){
               totalExpenseMap.put(userID , totalExpenseMap.getOrDefault(userID , 0.0) + map.get(userID));
           }
            payingUserAmoutMap.put(expense.getSplit().payingUserId,  payingUserAmoutMap.getOrDefault(expense.getSplit().payingUserId , 0.0) + expense.split.amount);
        }
        Map<String ,  Double > balanceMap = new HashMap<>();
        for(String userId  : totalExpenseMap.keySet()){
            Double expenseAmount = totalExpenseMap.get(userId);
            Double amountPaid =  payingUserAmoutMap.getOrDefault(userId , 0.0);
            Double diff =  amountPaid - expenseAmount;
            balanceMap.put(userId , diff);
        }
        return null;
    }
}

class GroupManager{
  static GroupManager groupManager = new GroupManager();
  Map<String , Group> groupMap;
  private GroupManager(){
    groupMap = new HashMap<>();
  }
  void addGroup(Group group){
    groupMap.put(group.groupId , group);
  }

  void addExpense(String groupId ,Expense expense){
     groupMap.get(groupId).addExpense(expense);
  }

  List<String> getUsersFromGroup(String groupId){
      return new ArrayList<>(groupMap.get(groupId).getUsers());
  }

  void deleteExpense(String groupId ,Expense expense){
    groupMap.get(groupId).deleteExpense(expense);
  }


  static GroupManager getInstance(){
      return groupManager;
  }
  void addUserToGroup(String groupId , String userID){
      groupMap.get(groupId).addUser(userID);
  }
  void createGroup(String userId , String groupId ,String name){

      groupMap.put(groupId , new Group(groupId , name));
      groupMap.get(groupId).addUser(userId);

  }

   Map<String, Expense> getExpenses(String groupId) {
      return groupMap.get(groupId).getExpenseMap();
    }
}


class SplitWise{

    UserManager  userManager = UserManager.getInstance();

    GroupManager groupManager = GroupManager.getInstance();

   static SplitWise splitWise = new SplitWise();

    private SplitWise(){}

    static SplitWise getInstance() {
        return splitWise;
    }

    void createUser(User user){
        userManager.addUser(user);
    }

    void createGroup(String userId , String groupId ,String groupName){
        User user = userManager.getUser(userId);
        if(user != null){
             groupManager.createGroup(userId , groupId , groupName);
        }
    }
    void addMemberToGroup(String userId , String groupId){
        if(userManager.getUser(userId)!=null) {
            groupManager.addUserToGroup(groupId, userId);
        }
    }
    void addExpense(String groupId , Expense expense){
        groupManager.addExpense(groupId , expense);
    }

    List<String> getUsersFromGroup(String groupId){
        return groupManager.getUsersFromGroup(groupId);
    }

    Map<String , Expense> getExpenseMap(String groupId){
        return groupManager.getExpenses(groupId);
    }

}
public class SplitWiseApp {

    public static void main(String[] args) {
        SplitWise splitWise = SplitWise.getInstance();
        splitWise.createUser(new User("user1" , "prateek"));
        splitWise.createUser(new User("user2" , "surbhhi"));
        BalanceCalculator balanceCalculator = new DefaultBalanceCalculator();
        splitWise.createGroup("user1" , "group1" , "travel");

        splitWise.addMemberToGroup("user2" , "group1");

        System.out.println(splitWise.getUsersFromGroup("group1"));

        splitWise.addExpense("group1" ,
                new Expense("expense1" , "bill" , new PercentageSplit("user1" , 100.0 ,
                Map.of("user1" , 50.0 , "user2" , 50.0
        ))));

        splitWise.addExpense("group1" ,
                new Expense("expense2" , "bill" , new SplitByExactAmount("user2" , 250.0 ,
                        Map.of("user1" , 150.0 , "user2" , 100.0
                        ))));

        System.out.println(splitWise.getExpenseMap("group1"));

        balanceCalculator.calculateBalance(new ArrayList<>(splitWise.getExpenseMap("group1").values()));
    }
}
