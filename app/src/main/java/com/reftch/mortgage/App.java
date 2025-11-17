package com.reftch.mortgage;

import com.reftch.annotation.Scan;
import com.reftch.http.server.Server;

@Scan({
        "com.reftch.mortgage.controller",
        "com.reftch.mortgage.service"
})
public class App {

    public static void main(String[] args) {
        Server.run();
    }
}
