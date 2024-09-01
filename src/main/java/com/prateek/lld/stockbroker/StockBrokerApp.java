package com.prateek.lld.stockbroker;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

class User{
    String userId;
    String name;
    String email;
    Portfolio portfolio;
    Double totalFunds;
    User(String userId , String name, String email){
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.portfolio = new Portfolio();
        this.totalFunds = 0.0;
    }

    void addFund(double amount){
        totalFunds += amount;
    }
}

@Data
class Portfolio{
    Double totalPortfolioValue;
    Double totalProfitAndLoss;
    Map<String , Holding> holdingMap;
    Portfolio(){
        holdingMap = new HashMap<>();
        totalPortfolioValue = 0.0;
        totalProfitAndLoss = 0.0;
    }
    void addStock(Stock stock , Integer quantity){
        Holding holding = holdingMap.get(stock.id);
        if(holding!=null){
            holding.avgPurchasePrice = (holding.avgPurchasePrice * holding.quantity  + stock.currentPrice* quantity) / (holding.quantity + quantity);
            holding.quantity+=quantity;
        }
        else{
            holdingMap.put(stock.id , new Holding(stock.id , stock.currentPrice , quantity , stock.currentPrice));
        }
        stock.addPortfolio(this);
        updatePortfolio();
    }
    void removeStock(Stock stock , Integer quantity){
        Holding holding = holdingMap.get(stock.id);
        if(holding!=null || holding.quantity  <  quantity){
            System.out.println("Not enough stock available to sold , decrease the quantity");
        }
        if(holding.quantity == quantity){
            holdingMap.remove(holding);
            stock.removePortfolio(this);
        }
        else {
            holding.avgPurchasePrice = (holding.avgPurchasePrice * holding.quantity - stock.currentPrice* quantity) / (holding.quantity - quantity);
            holding.quantity -= quantity;
        }
        updatePortfolio();
    }
    void updateStockPrice(String stockId ,Double price){
        Holding holding = holdingMap.get(stockId);
        if(holding!=null){
            holding.currentPrice = price;
        }
        updatePortfolio();
    }

    void updatePortfolio(){
        calculatePortfolioValue();
        calculateTotalProfitAndLoss();
    }
    void calculatePortfolioValue(){
        Double price  = 0.0;
        for(Holding holding : holdingMap.values()){
            price += holding.currentPrice * holding.quantity;
        }
        this.totalPortfolioValue = price;
    }

    void calculateTotalProfitAndLoss(){
        Double profitAndLoss = 0.0;
        for(Holding holding : holdingMap.values()){
            profitAndLoss = profitAndLoss + (holding.currentPrice - holding.avgPurchasePrice)* holding.quantity;
        }
        this.totalProfitAndLoss = profitAndLoss;
    }
}


@Data
@AllArgsConstructor
class Holding{
    String stockId;
    Double avgPurchasePrice;
    Integer quantity;
    Double currentPrice;
}

class Stock{
    String id;
    String name;
    Double currentPrice;
    List<Portfolio> portfolios;
    Stock(String id , String name , double currentPrice){
        portfolios = new ArrayList<>();
        this.id = id;
        this.name = name;
        this.currentPrice = currentPrice;
    }
    void updateStockPrice(Double price){
        this.currentPrice = price;
        updateAllPortFolios();
    }

    void updateAllPortFolios(){
        for(Portfolio portfolio : portfolios){
            portfolio.updateStockPrice(id , currentPrice);
        }
    }

    void addPortfolio(Portfolio portfolio){
        portfolios.add(portfolio);
    }
    void removePortfolio(Portfolio portfolio){
        portfolios.remove(portfolio);
    }
}


class Exchange{
    static Exchange exchange   = new Exchange();
    List<StockBroker> stockBrokers;
    void execute(Order order){
        System.out.println("order is executed successfully");
        order.updateOrder(OrderStatus.COMPLETED);
    }
    private Exchange(){
        stockBrokers = new ArrayList<>();
    }
    void cancelOrder(Order order){

    }

    void addStockBroker(StockBroker stockBroker){
        stockBrokers.add(stockBroker);
    }

    void updateStockPrice(String stockId , double price){
        for(StockBroker stockBroker : stockBrokers){
            stockBroker.updateStockPrice(stockId , price);
        }
    }

    void addStock(Stock stock){
        for(StockBroker stockBroker : stockBrokers){
            stockBroker.addStock(stock);
        }
    }

    static Exchange getExchange(){ return exchange;}
}

abstract class Order{
    String orderID;
    String userId;
    String stockId;
    Integer quantity;
    OrderStatus orderStatus;
    UserManager userManager;
    Exchange exchange;
    StockManager stockManager;
    abstract void placeOrder();
    abstract void updateOrder(OrderStatus orderStatus);
    Order(String userId , String stockId , Integer quantity){
        this.userId = userId;
        this.stockId = stockId;
        this.quantity = quantity;
        this.orderID = UUID.randomUUID().toString();
        orderStatus = OrderStatus.INITIATED;
        userManager = UserManager.getUserManager();
        exchange = Exchange.getExchange();
        stockManager = StockManager.getStockManager();
    }
}

enum OrderStatus{
    INITIATED , IN_PROGRESS,COMPLETED, FAILED,CANCELLED
}

class UserManager{
    Map<String , User>  users;
    static UserManager  userManager = new UserManager();
    User getUser(String userId){
        return users.get(userId);
    }
    private UserManager(){
        users = new HashMap<>();
    }
    static UserManager getUserManager(){
        return userManager;
    }

    void addFund(String userId , double amount){
        if(users.get(userId)!= null){
            users.get(userId).addFund(amount);
        }
    }

    public void addUser(User user) {
        users.put(user.userId , user);
    }
}

class StockManager{
    Map<String , Stock> stocks;
    static StockManager  stockManager = new StockManager();
    Stock getStock(String stockId){
        return stocks.get(stockId);
    }
    private StockManager(){
        stocks = new HashMap<>();
    }
    static StockManager getStockManager(){
        return stockManager;
    }

    void addStock(Stock stock){
        stocks.put(stock.id , stock);
    }

    void updatePrice(String stockId ,double price){
        Stock stock = getStock(stockId);
        stock.updateStockPrice(price);
    }
}

class BuyOrder extends Order{

    BuyOrder(String userId , String stockId, Integer quantity){
        super(userId , stockId , quantity);
    }
    @Override
    void placeOrder() {
        User user = userManager.getUser(userId);
        Stock stock = stockManager.getStock(stockId);
        if (user.totalFunds >= stock.currentPrice * quantity) {
            exchange.execute(this);
        }
        else{
            System.out.println("Funds not available");
        }
    }
    @Override
    void updateOrder(OrderStatus orderStatus){
        if(OrderStatus.COMPLETED.equals(orderStatus)){
            User user = userManager.getUser(userId);
            Stock stock = stockManager.getStock(stockId);
            user.portfolio.addStock(stock ,quantity);
            user.totalFunds-= stock.currentPrice * quantity;
        }
        else{
            System.out.println("failed to process order");
        }
    }
}

class OrderManager{
    Map<String , Order > orders;
    static OrderManager orderManager = new OrderManager();
    private OrderManager(){
        orders = new HashMap<>();
    }
    static OrderManager getOrderManager(){
        return orderManager;
    }
    void placeOrder(Order order){
        orders.put(UUID.randomUUID().toString() , order);
        order.placeOrder();
    }
}


class SellOrder extends Order{

    SellOrder(String userId , String stockId, Integer quantity){
        super(userId , stockId , quantity);
    }
    @Override
    void placeOrder() {
        User user = userManager.getUser(userId);
        if(user.portfolio.holdingMap.get(stockId).quantity >= quantity){
            exchange.execute(this);
        }
    }

    @Override
    void updateOrder(OrderStatus orderStatus) {
        if(orderStatus == OrderStatus.COMPLETED){
            User user = userManager.getUser(userId);
            Stock stock = stockManager.getStock(stockId);
            user.portfolio.removeStock(stock , quantity);
            user.totalFunds+= stock.currentPrice * quantity;
        }
        else{
            System.out.println("failed to process Order");
        }
    }
}

class StockBroker {
    static StockBroker stockBroker = new StockBroker();
    UserManager userManager = UserManager.getUserManager();
    StockManager stockManager = StockManager.getStockManager();
    OrderManager orderManager = OrderManager.getOrderManager();
    void updateStockPrice(String stockId , double price){
        stockManager.updatePrice(stockId , price);
    }
    private StockBroker(){
    }
    static StockBroker getStockBroker(){
        return stockBroker;
    }

    void addUser(User user){
        userManager.addUser(user);
    }

    public void addStock(Stock stock) {
        stockManager.addStock(stock);
    }
    void addFund(String userId , double amount){
        userManager.addFund(userId , amount);
    }

    double getStockPrice(String stockId ){
        return  stockManager.getStock(stockId).currentPrice;
    }

    Portfolio getPortFolio(String userId ){
        return userManager.getUser(userId).portfolio;
    }

    void placeOrder(Order order){
        orderManager.placeOrder(order);
    }

    Double getFund(String userId){
        return userManager.getUser(userId).totalFunds;
    }
}


public class StockBrokerApp {
    public static void main(String[] args) {
        Exchange exchange = Exchange.getExchange();
        StockBroker stockBroker =  StockBroker.getStockBroker();
        exchange.addStockBroker(stockBroker);
        stockBroker.addUser(new User("userId1" , "Prateek" , "abc@gmail.com"));
        stockBroker.addFund("userId1" , 100);

        exchange.addStock(new Stock("LIC" , "Life Insurance of India" , 10.0));
        exchange.addStock(new Stock("TATA" , "TATA" , 30));


        System.out.println(stockBroker.getStockPrice("LIC"));

        stockBroker.placeOrder(new BuyOrder("userId1" , "LIC" , 5));

        System.out.println(stockBroker.getPortFolio("userId1"));

        exchange.updateStockPrice("LIC" , 5);
        System.out.println(stockBroker.getPortFolio("userId1"));
        System.out.println(stockBroker.getFund("userId1"));
        stockBroker.placeOrder(new BuyOrder("userId1" , "TATA" , 5));

        System.out.println(stockBroker.getPortFolio("userId1"));

        stockBroker.placeOrder(new BuyOrder("userId1" , "TATA" , 1));

        System.out.println(stockBroker.getPortFolio("userId1"));
        System.out.println(stockBroker.getFund("userId1"));


        exchange.updateStockPrice("TATA" , 35);
        System.out.println(stockBroker.getPortFolio("userId1"));
        System.out.println(stockBroker.getFund("userId1"));
    }

}

