package com.reftch.mortgage;

import com.reftch.annotation.WebApplication;
import com.reftch.http.server.Server;

@WebApplication
public class App {

    public static void main(String[] args) {
        Server.run();
    }
}
