package com.jerry.http_client_util.controller;

import com.jerry.http_client_util.utility.HttpClientUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class httpClientController {

    @GetMapping("/")
    public ResponseEntity index() {
        String uri = "https://github.com/xz1996/HttpClientUtil";
        String response = HttpClientUtil.getResponseEntity(HttpClientUtil.sendHttpsGet(uri));
        return new ResponseEntity(response, HttpStatus.OK);
    }
}
