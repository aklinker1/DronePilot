package com.klinker.android.drone_pilot;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by aklinker1 on 4/7/18.
 */

public class PingRequest extends StringRequest {

    public PingRequest(Response.Listener<String> listener, Response.ErrorListener error) {
        super(
                Method.GET,
                Controller.HOST + "/ping?calledAt=" + System.currentTimeMillis(),
                listener,
                error
        );
    }

}
