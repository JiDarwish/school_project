/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.google.gson.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jidar
 */
public class entryServlet extends HttpServlet {

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        String passengerReqestBody = "{\"function\": \"List\", \"teamId\": \"IN102-2\", \"teamKey\": \"c4fe461a10cffe4857aff6f76d3615e7\", \"requestId\": \"123\"}";
        String passagiersUrlString = "http://fys.securidoc.nl:11111/Passenger";

        String passagiersResponse = requestGetResponseString(passengerReqestBody, passagiersUrlString);

        ///////////////////////////////////////Done with the requests 
        
        ////////Now get the request parameters in variables
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String ticketNumber = request.getParameter("ticketNumber");
        ////////End getting parameters


        
        //Methods for manipulating the JsonString into a usable Arraylist for both Passengers and tickets
        ArrayList<Passenger> listPassengers = getPassengers(passagiersResponse);//Passengers ArrayList

        if (checkValidLogIn(firstName, lastName, ticketNumber, listPassengers, out)){
            request.getRequestDispatcher("welkom_en.html").forward(request, response);
        } else {
            out.println("Go to hell");
        }



    }
    
    static boolean checkValidLogIn(String fName, String lName, String ticketNum, ArrayList<Passenger> allThePassengers, PrintWriter someOut) throws MalformedURLException, IOException{
        for (int i = 0; i < allThePassengers.size(); i++) {
            if(allThePassengers.get(i).firstName.equalsIgnoreCase(fName) && allThePassengers.get(i).lastName.equalsIgnoreCase(lName)){
                String ticketsUrl = "http://fys.securidoc.nl:11111/Ticket";
                String ticketsRequestBody = "{\"function\": \"List\", \"teamId\": \"IN102-2\", \"teamKey\": \"c4fe461a10cffe4857aff6f76d3615e7\", \"requestId\": \"123\"}";
                String ticketsResponse = requestGetResponseString(ticketsRequestBody, ticketsUrl);//Get the response as string
                ArrayList<Ticket> ticketsAsArrayList = getTickets(ticketsResponse);//Turn the response String into arrayList of type Ticket
                return ticketExistance(ticketNum, ticketsAsArrayList);//Returns true if filled ticket number exists in the tickets list
            }
        }
        return false;//If first and list name didn't match
    }
    
    static boolean ticketExistance(String ticketnum, ArrayList<Ticket> allTheTickets){
        for (int i = 0; i < allTheTickets.size(); i++) {
            if (allTheTickets.get(i).ticketNumber.equalsIgnoreCase(ticketnum)){
                return true;
            }
        }
        return false;
    }
    
    
    static ArrayList<Passenger> getPassengers(String passengerAsString){
        Gson gson = new Gson();
        JsonObject passengerResJsonObject = gson.fromJson(passengerAsString, JsonObject.class);

        JsonObject thePassengers = (JsonObject) passengerResJsonObject.get("passengers");

        List<JsonElement> values = thePassengers.entrySet()
                .stream()
                .map(i -> i.getValue())
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Passenger> listPassengers = new ArrayList<>();

        for (int i = 0; i < values.size(); i++) {
            Passenger willBeAdded = gson.fromJson(values.get(i), Passenger.class);
            listPassengers.add(i, willBeAdded);
        }
        return listPassengers;
    }
    
    static ArrayList<Ticket> getTickets(String TicketsAsString){
        Gson gson = new Gson();
        
        JsonObject ticketResJsonObject = gson.fromJson(TicketsAsString, JsonObject.class);
        
        JsonObject theTickets = (JsonObject) ticketResJsonObject.get("tickets");
        
        List<JsonElement> values = theTickets.entrySet()
                .stream()
                .map(i -> i.getValue())
                .collect(Collectors.toCollection(ArrayList::new));
        
        ArrayList<Ticket> listTickets = new ArrayList<>();
        
        for (int i = 0; i < values.size(); i++) {
            Ticket willBeAdded = gson.fromJson(values.get(i), Ticket.class);
            listTickets.add(i, willBeAdded);
        }
        return listTickets;
    }
    
    
    
    

    static String requestGetResponseString(String dataNaarServer, String urlString) throws MalformedURLException, IOException {
        String jsonData = dataNaarServer;
        URL obj = new URL(urlString);

        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

        connection.setRequestMethod("POST");

        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        connection.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            wr.writeBytes(jsonData);
            wr.flush();
        }

        StringBuffer res;
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            String output;
            res = new StringBuffer();
            while ((output = in.readLine()) != null) {
                res.append(output);
            }
        }

        return res.toString();

    }
}
