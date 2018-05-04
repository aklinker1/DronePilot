package com.klinker.android.drone_pilot;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import static com.klinker.android.drone_pilot.Controller.HOST;

public class ArmRequest extends StringRequest {

    public ArmRequest(boolean armed, Response.Listener<String> listener, Response.ErrorListener error) {
        super(
                Method.POST,
                String.format(
                        "%s/arm?arm=%b",
                        HOST, armed
                ),
                listener,
                error
        );
    }

}
