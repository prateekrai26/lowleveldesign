package com.prateek.lld.notificationservice;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
class Event{
    String eventID;
    List<String> userIds;
    String notificationMessage;
}


interface NotificationObservable{
    void addNotificationChannel(Channel channel);
    void removeNotificationChannel(Channel channel);
    void sendNotification(Event event);
}

class User implements NotificationObservable{
    String userId;
    String email;
    String phone;
    List<Channel> channels;

    User(String userId, String email, String phone) {
        this.userId = userId;
        this.email = email;
        this.phone = phone;
        channels = new ArrayList<>();
    }
    @Override
    public void addNotificationChannel(Channel channel) {
        channels.add(channel);
    }

    @Override
    public void removeNotificationChannel(Channel channel) {
        channels.remove(channel);
    }

    @Override
    public void sendNotification(Event event) {
        for(Channel channel : channels){
            channel.process(userId , event.notificationMessage);
        }
    }
}

interface Channel{
    void process(String userId, String message);
}

class EmailChannel implements Channel{

    @Override
    public void process(String userId , String message) {
        System.out.println("Email send for user--" + userId + "with message" + message);
    }
}

class SMSChannel implements Channel{

    @Override
    public void process(String userId , String message) {
        System.out.println("SMS send for user--" + userId + "with message" + message);
    }
}
class UserManager{
    Map<String, User> userMap = new HashMap<String, User>();

    void addUser(User user){
        userMap.put(user.userId, user);
    }
    User getUser(String userId){
        return userMap.get(userId);
    }

    void subscribeNotification(String userId , Channel channel){
        userMap.get(userId).addNotificationChannel(channel);
    }

    void unsubscribeNotification(String userId , Channel channel){
        userMap.get(userId).removeNotificationChannel(channel);
    }
}



public class NotificationService {

    UserManager userManager = new UserManager();


    void addUser(User user){
        userManager.addUser(user);
    }

    void sendNotification(Event event){
        for(String userId : event.userIds){
            User user = userManager.getUser(userId);
            user.sendNotification(event);
        }
    }

    void addNotificationChannel(String userId , Channel channel){
        userManager.subscribeNotification(userId , channel);
    }





    public static void main(String[] args) {

        User user = new User("123" , "prateekrai2602@gmail.com" , "9455400754");
        User user2 = new User("234" , "hi.prateekrai@gmail.com" , "9455400754");


        NotificationService service = new NotificationService();

        service.addUser(user);
        service.addUser(user2);

        service.addNotificationChannel(user.userId, new EmailChannel());
        service.addNotificationChannel(user2.userId, new SMSChannel());
        service.addNotificationChannel(user2.userId ,new EmailChannel());

        service.sendNotification(new Event("id1" , List.of("123" ,"234") , "Welcome to Notification Service"));

    }
}
