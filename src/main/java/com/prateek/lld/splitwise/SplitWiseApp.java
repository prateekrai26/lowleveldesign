package com.prateek.lld.splitwise;


import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
class User{
  String userID;
  String name;
  List<Group> groupList;
}
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

class Percentage extends  Split{
   Map<String , Double> percentageMap;
   Percentage(){
     percentageMap = new HashMap<>();
   }
   void add(String userID , Double percentage){
     percentageMap.put(userID , percentage);
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

  @Override
  Map<String, Double> calculateSplitAmount() {
     for(String userID : exactAmountMap.keySet()){
       splitAmountMap.put(userID , exactAmountMap.get(userID));
     }
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
       splitAmountMap.put(user , amount/users.size())
    }
    return splitAmountMap;
  }
}

enum SplitType{
  PERCENTAGE , EQUAL
}
class Group{
  String groupId;
  List<User> users;
  Map<String , List<Amount>> userToAmountMap;
  List<Expense> expenses;
}

class Amount{
  String id;
  Double amount;
  String userId;
}
class Owe extends Amount{
   Double collectMoney(Double amount){
     return null;
   }
}

class Owes extends Amount{
   void sendMoney(Double amount){

   }
}


class SplitWise{


}
public class SplitWiseApp {


}
