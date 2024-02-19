package com.lei6393.trouve.example.server.nonsingleton;

import com.lei6393.trouve.server.dispatch.TrouveRequestDispatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class EntranceController {
    @RequestMapping("**")
    public void entrance(HttpServletRequest request,
                         HttpServletResponse response) throws Throwable {
        TrouveRequestDispatcher.entrance(request, response);
    }
}
