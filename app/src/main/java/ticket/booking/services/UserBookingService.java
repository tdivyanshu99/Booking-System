package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.utils.UserServiceUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class UserBookingService
{
    private User user;

    private static final String USER_PATH = "app/src/main/resources/users.json";

    private ObjectMapper objectMapper = new ObjectMapper(); // used to serialize and deserialize the Json file content

    private List<User> userList;

    public UserBookingService(User user) throws IOException {
        this.user = user;
        userList = loadUsers();
    }

    public UserBookingService() throws IOException{
        userList = loadUsers();
    }

    public List<User> loadUsers() throws IOException{
        //InputStream inputStream = getClass().getClassLoader().getResourceAsStream("users.json");
        File users = new File(USER_PATH);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        //return objectMapper.readValue(inputStream, new TypeReference<List<User>>() {});
        return objectMapper.readValue(users, new TypeReference<List<User>>(){}); // maps the plane text to user entity. Deserialization
    }
    public Boolean loginUser(){
        Optional<User> foundUser = userList.stream().filter(user1 -> {
            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        return foundUser.isPresent();
    }

    public Boolean signUp(User user1){
        try{
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        }catch (IOException ex){
            return Boolean.FALSE;
        }
    }

    private void saveUserListToFile() throws IOException {
        File usersFile = new File(USER_PATH);
        objectMapper.writeValue(usersFile, userList); // Serialization/ Saving to JSON file
    }

    public void fetchBooking() {
        Optional<User> userFetched = userList.stream().filter(user1 -> {
            return user1.getName().equals(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), user1.getHashedPassword());
        }).findFirst();
        if(userFetched.isPresent()){
            userFetched.get().printTickets();
        }
    }

    public Boolean CancelBooking(String TicketId) {
        boolean removed = user.getTicketsBooked().removeIf(ticket -> ticket.getTicketId().equals(TicketId));

        if (removed) {
            System.out.println("Ticket with ID " + TicketId + " has been canceled.");
            return Boolean.TRUE;
        }else{
            System.out.println("No ticket found with ID " + TicketId);
            return Boolean.FALSE;
        }

    }

    public List<Train> getTrains(String source, String destination){
        try{
            TrainService trainService = new TrainService();
            System.out.println(trainService.getTrainList().size());
            return trainService.searchTrains(source, destination);
        }catch(IOException ex){
            return new ArrayList<>();
        }
    }

    public List<List<Integer>> fetchSeats(Train train){
        return train.getSeats();
    }

    public Boolean bookTrainSeat(Train train, int row, int seat) {
        try{
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();
            if (row >= 0 && row < seats.size() && seat >= 0 && seat < seats.get(row).size()) {
                if (seats.get(row).get(seat) == 0) {
                    seats.get(row).set(seat, 1);
                    train.setSeats(seats);
                    trainService.addTrain(train);
                    return true; // Booking successful
                } else {
                    return false; // Seat is already booked
                }
            } else {
                return false; // Invalid row or seat index
            }
        }catch (IOException ex){
            return Boolean.FALSE;
        }
    }

}

// JSON --> Object (user) => Deserialize
// Object (user) --> JSON => Serialize
