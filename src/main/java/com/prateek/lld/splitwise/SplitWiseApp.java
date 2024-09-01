package com.prateek.lld.splitwise;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@Data
class User{
  String userID;
  String name;
  List<Group> groupList;
}

@AllArgsConstructor
class Expense{
  String expenseID;
  String name;
  Split split;
}
@Data
abstract class Split{
  double amount;
  Map<String , Double> splitAmountMap;
  abstract Map<String,Double> calculateSplitAmount();
}

interface SplitFactory{
   Split getSplit();
}

class PercentageSplitFactory implements SplitFactory{

  @Override
  public PercentageSplit getSplit() {
    return new PercentageSplit();
  }
}

class EqualSplitFactory implements SplitFactory{

  @Override
  public EqualSplit getSplit() {
    return new EqualSplit();
  }
}

class SplitByExactAmountFactory implements SplitFactory{

  @Override
  public SplitByExactAmount getSplit() {
    return new SplitByExactAmount();
  }
}

class PercentageSplit extends  Split{
   Map<String , Double> percentageMap;
  PercentageSplit(){
     percentageMap = new HashMap<>();
   }
   void add(String userID , Double percentage){
     percentageMap.put(userID , percentage);
   }
  void add(List<String> userIds , List<Double> percentages){
    for(int i=0;i<userIds.size();i++){
      percentageMap.put(userIds.get(i) , percentages.get(i));
    }
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
   SplitByExactAmount(){
    exactAmountMap = new HashMap<>();
  }
  void add(String userID , Double amount){
    exactAmountMap.put(userID , amount);
  }
 void add(List<String> userIds , List<Double> amounts){
     for(int i=0;i<userIds.size();i++){
      exactAmountMap.put(userIds.get(i) , amounts.get(i));
      }
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
  List<User> users;
  EqualSplit(){
    users = new ArrayList<>();
  }
  void add(String userID){
    users.add(new User());
  }

  @Override
  Map<String, Double> calculateSplitAmount() {
    for(User user : users){
       splitAmountMap.put(user.userID , amount/users.size());
    }
    return splitAmountMap;
  }
}
class Group {
  String groupId;
  Set<User> users;
  Map<String, List<Balance>> userToAmountMap;
  Map<String, Expense> expenseMap;

  void addExpense(Expense expense) {
    expenseMap.put(expense.expenseID, expense);
  }

  void deleteExpense(Expense expense) {
    expenseMap.remove(expense.expenseID, expense);
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

  void deleteExpense(String groupId ,Expense expense){
    groupMap.get(groupId).deleteExpense(expense);
  }




}


class SplitWise{


}
public class SplitWiseApp {


}
