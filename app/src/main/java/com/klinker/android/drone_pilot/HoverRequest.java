package com.klinker.android.drone_pilot;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import static com.klinker.android.drone_pilot.Controller.HOST;

public class HoverRequest extends StringRequest {

    public HoverRequest(boolean hovering, Response.Listener<String> listener, Response.ErrorListener error) {
        super(
                Method.POST,
                String.format(
                        "%s/hover?hover=%b",
                        HOST, hovering
                ),
                listener,
                error
        );
    }

}
